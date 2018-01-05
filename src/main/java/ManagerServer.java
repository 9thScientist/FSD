import com.Request;
import com.transactions.*;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;
import rmi.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ManagerServer {
    private static AtomicInteger nextId = new AtomicInteger(0);
    private static Map<Integer, Transaction> transactions = new HashMap<>();
    private static Log log = new Log("manager");

    private static ThreadContext tc = new SingleThreadContext("manager-%d", new Serializer());
    private static Transport t = new NettyTransport();
    private static Address address;

    public static void main(String[] args) {
        address = new Address("localhost:4000");

        registerSerializers(tc.serializer());

        System.out.println("Recovering log");
        tc.execute(() -> {
            log.open().thenRun(ManagerServer::recover)
                      .thenRun(ManagerServer::handleTransactions);
        });
    }

    private static void handleTransactions() {
        tc.execute(() -> {
            System.out.println("Listening on " + address);
            t.server().listen(address, c -> {
                c.handler(ManagerBeginReq.class, m -> {
                    Context ctx = new Context(address, nextId.incrementAndGet());
                    return Futures.completedFuture(new ManagerBeginRep(ctx));
                });
                c.handler(ManagerAddResourceReq.class, m -> {
                    int xid = m.getContext().getContextId();
                    Transaction transaction = transactions.get(xid);

                    if (transaction == null) {
                        transaction = new Transaction(m.getContext());
                        transactions.put(xid, transaction);
                    }

                    Address addr = m.getAddress();
                    int resourceId = transaction.addParticipant(addr);

                    return Futures.completedFuture(new ManagerAddResourceRep(resourceId));
                });
                c.handler(ManagerCommitReq.class, m -> {
                    handleCommit(m.getContext());
                    return Futures.completedFuture(new ManagerCommitRep());
                });
            });
        }).join();
    }

    public static void handleCommit(Context context) {
        CompletableFuture<Void> r = new CompletableFuture<>();

        Transaction tr = transactions.get(context.getContextId());
        Transport tt = new NettyTransport();
        Address[] participants = convertAddresses(tr.addresses);

        Clique c = new Clique(tt, 0, participants);
        int size = participants.length;

        Map<Integer, Boolean> ready = new HashMap<>();
        AtomicInteger commits = new AtomicInteger(0);

        ThreadContext tc = new SingleThreadContext("commit-%d", new Serializer());
        registerSerializers(tc.serializer());

        tc.execute(() -> {
            c.handler(ManagerAskReq.class, (s, m) -> {
                boolean res = tr.stateIs(Transaction.State.COMMIT);
                c.send(s, new ManagerAskRep(res, context));
            });
            c.handler(ManagerAbortRep.class, (s, m) -> {
                if (commits.incrementAndGet() == size-1) {
                    log.append(new ManagerComplete(context));
                    tr.setComplete();
                }
            });
            c.handler(ManagerCommitRep.class, (s, m) -> {
                if (commits.incrementAndGet() == size-1) {
                    log.append(new ManagerComplete(context));
                    tr.setComplete();
                }
            });
            c.handler(ManagerPreparedRep.class, (s, m) -> {
                ready.put(s, m.isOk());

                if (ready.size() < size-1) {
                    return;
                }

                if (ready.values().stream().allMatch(Boolean::valueOf)) {
                    Request req = new ManagerCommitReq(context);

                    log.append(req);
                    broadcast(c, size, req);

                    tr.setCommit();
                } else {
                    log.append(new ManagerAbortReq());
                    ready.keySet().stream()
                         .filter(ready::get)
                         .forEach(i -> c.send(i, new ManagerAbortReq(context)));

                    tr.setAbort();
                }
            });

            c.open().thenRun(() -> {
                IntStream.range(1, size).forEach(i -> {
                    c.send(i, new ManagerPreparedReq(context, i));
                });
            });
        });
    }

    private static Address[] convertAddresses(List<Address> participants) {
        Address[] ret = new Address[participants.size()];

        ret[0] = address;
        IntStream.range(0, ret.length)
                .forEach(i ->  {
                    Address participant = participants.get(i);
                    ret[i] = new Address(participant.host(), participant.port() + 100);
                });

        return ret;
    }

    private static void broadcast(Clique c, int size, Object o) {
        IntStream.range(1, size)
                 .forEach(i -> c.send(i, o));
    }

    public static CompletableFuture<Void> recover() {
        CompletableFuture<Void> r = new CompletableFuture<>();

        tc.execute(() -> {
            log.handler(ManagerAddResourceReq.class, (i, req) -> {
                int xid = req.getContext().getContextId();
                Transaction t = transactions.get(xid);

                if (t == null) {
                    t = new Transaction(req.getContext());
                    transactions.put(xid, t);
                }

                t.addParticipant(req.getAddress());
            });
            log.handler(ManagerCommitReq.class, (i, req) -> {
                int xid = req.getContext().getContextId();
                Transaction t = transactions.get(xid);

                t.setCommit();
            });
            log.handler(ManagerAbortReq.class, (i, req) -> {
                int xid = req.getContext().getContextId();
                Transaction t = transactions.get(xid);

                t.setAbort();
            });
            log.handler(ManagerComplete.class, (i, comp) -> {
                int xid = comp.getContext().getContextId();
                Transaction t = transactions.get(xid);

                t.setComplete();
            });

            log.open().thenRun(() -> {
                transactions.values()
                        .stream()
                        .filter(t -> t.stateIs(Transaction.State.BEGIN)
                                || t.stateIs(Transaction.State.ABORT))
                        .forEach(Transaction::abort);

                transactions.values()
                        .stream()
                        .filter(t -> t.stateIs(Transaction.State.COMMIT))
                        .forEach(Transaction::commit);

                r.complete(null);
            });
        });

        return r;
    }

    private static void registerSerializers(Serializer serializer) {
        serializer.register(Context.class);
        serializer.register(ManagerComplete.class);
        serializer.register(ManagerAddResourceReq.class);
        serializer.register(ManagerAddResourceRep.class);
        serializer.register(ManagerCommitReq.class);
        serializer.register(ManagerCommitRep.class);
        serializer.register(ManagerAbortReq.class);
        serializer.register(ManagerAbortRep.class);
        serializer.register(ManagerPreparedReq.class);
        serializer.register(ManagerPreparedRep.class);
        serializer.register(ManagerAskReq.class);
        serializer.register(ManagerAskRep.class);
    }


    private static class Transaction {
        public Context context;
        public List<Address> addresses;

        public enum State {BEGIN, COMMIT, ABORT, COMPLETE}
        private State state;

        public Transaction(Context context) {
            this.context = context;
            this.state = State.BEGIN;
            addresses = new ArrayList<>();
            addresses.add(address);
        }

        public void setCommit() {
            state = State.COMMIT;
        }

        public void setAbort() {
            state = State.ABORT;
        }

        public void setComplete() {
            state = State.COMPLETE;
        }

        public void commit() {
            ManagerServer.handleCommit(context);
        }

        public void abort() {
            Clique c = new Clique(t, 0, (Address[]) addresses.toArray());
            broadcast(c, addresses.size(), new ManagerAbortReq());
        }

        private int addParticipant(Address address) {
            if (!addresses.contains(address)) {
                addresses.add(address);
            }

            return addresses.indexOf(address);
        }


        public boolean stateIs(State s) {
            return state == s;
        }
    }

    private static class ManagerComplete implements CatalystSerializable {
        private Context context;

        private ManagerComplete() {

        }

        private ManagerComplete(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        @Override
        public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
            serializer.writeObject(context, bufferOutput);
        }

        @Override
        public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
            context = serializer.readObject(bufferInput);
        }
    }
}

