package rmi;

import com.transactions.ManagerAskReq;
import com.transactions.*;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Manager {
    private static final AtomicInteger nextXID = new AtomicInteger(0);

    private static final ThreadLocal<Context> context =
            new ThreadLocal<>();
    private static final ThreadLocal<List<Address>> participants =
            new ThreadLocal<>().withInitial(ArrayList::new);
    private static Map<Integer, Transaction> logTransactions = new HashMap<>();
    private static final Log log = new Log("Manager");

    private static ThreadContext tc = new SingleThreadContext("mngr-%d", new Serializer());
    private static Transport t = new NettyTransport();
    private static Address address;

    public static void begin() throws AddressNotSetException {
        if (address == null)
            throw new AddressNotSetException("Manager's address is not set");

        Context context = new Context(address, nextXID.incrementAndGet());
        participants.get().add(address);

        tc.execute(() -> {
            t.server().listen(address, c ->
                    c.handler(ManagerAddResourceReq.class, m -> {
                        participants.get().add(m.getReference().getAddress());

                        log.append(m);
                    }));
        });

        Manager.context.set(context);
    }

    public static void commit() {
        Context current = context.get();
        Address[] participants = (Address[]) Manager.participants.get().stream()
                                .map(p -> new Address(p.host(), p.port() + 100 )).toArray();

        Clique c = new Clique(t, 0, participants);

        Map<Integer, Boolean> ready = new HashMap<>();
        int size = participants.length;

        tc.execute(() -> {
            AtomicInteger commits = new AtomicInteger(0);
            c.handler(ManagerAskReq.class, (s, m) -> {
                int xid = m.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                boolean r = false;
                if (t.stateIs(Transaction.State.COMMIT))
                    r = true;

                c.send(s, new ManagerAskRep(r, m.getContext()));
            });
            c.handler(ManagerAbortRep.class, (s, m) -> {
                if (commits.incrementAndGet() == size) {
                    log.append(new ManagerComplete(current));
                }
            });
            c.handler(ManagerCommitRep.class, (s, m) -> {
                if (commits.incrementAndGet() == size) {
                    log.append(new ManagerComplete(current));
                }
            });
            c.handler(ManagerPreparedRep.class, (s, m) -> {
                if (m.isOk())
                    ready.put(s, true);
                else
                    ready.put(s, false);

                if (ready.size() == size) {
                    if (ready.values().stream().allMatch(Boolean::valueOf)) {
                        log.append(new ManagerCommitReq());
                        broadcast(c, size, new ManagerCommitReq());
                    } else {
                        log.append(new ManagerAbortReq());
                        ready.keySet().stream()
                                      .filter(ready::get)
                                      .forEach(i -> c.send(i, new ManagerAbortReq()));
                    }
                }
            });

            c.open().thenRun(() ->
                    broadcast(c, size, new ManagerPreparedReq())
            );
        });

        context.set(null);
    }

    public static void add(Context ctx, Reference ref) throws Exception {
        Connection c = tc.execute(() ->
                t.client().connect(ctx.getAddress())
        ).join().get();

        ManagerAddResourceRep r = (ManagerAddResourceRep) tc.execute(() ->
                c.sendAndReceive(new ManagerAddResourceReq(ctx, ref))
        ).join().get();
    }

    public static Address getAddress() {
        return address;
    }

    public static void setAddress(Address address) {
        Manager.address = address;
    }


    public static Context getContext() {
        return context.get();
    }

    public static void setContext(Context c) {
        Manager.context.set(c);
    }

    private static void broadcast(Clique c, int size, Object o) {
        IntStream.range(0, size)
                .forEach(i -> c.send(i, o));
    }

    public static void recover() {
        tc.execute(() -> {
            log.handler(ManagerAddResourceReq.class, (i, req) -> {
                int xid = req.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                if (t == null) {
                    t = new Transaction(req.getContext());
                    logTransactions.put(xid, t);
                }

                t.add(req.getReference());
            });
            log.handler(ManagerCommitReq.class, (i, req) -> {
               int xid = req.getContext().getContextId();
               Transaction t = logTransactions.get(xid);

               t.setCommit();
            });
            log.handler(ManagerAbortReq.class, (i, req) -> {
                int xid = req.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                t.setAbort();
            });
            log.handler(ManagerComplete.class, (i, comp) -> {
                int xid = comp.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                t.apply();
            });

            log.open().thenRun(() -> {
                logTransactions.values()
                                .stream()
                                .filter(t -> t.stateIs(Transaction.State.BEGIN)
                                          || t.stateIs(Transaction.State.ABORT))
                                .forEach(Transaction::abort);

                logTransactions.values()
                        .stream()
                        .filter(t -> t.stateIs(Transaction.State.COMMIT))
                        .forEach(Transaction::commit);
            });
        });
    }

    public static class Transaction {
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

        public void add(Reference ref) {
            addresses.add(ref.getAddress());
        }

        public void setCommit() {
            state = State.COMMIT;
        }

        public void setAbort() {
            state = State.ABORT;
        }

        public void commit() {
            participants.set(addresses);
            Manager.commit();
        }

        public void abort() {
            Clique c = new Clique(t, 0, (Address[]) addresses.toArray());
            broadcast(c, addresses.size(), new ManagerAbortReq());
        }

        public void apply() {
            state = State.COMPLETE;
        }


        public boolean stateIs(State s) {
            return state == s;
        }
    }

    private static class ManagerComplete {
        private Context context;

        private ManagerComplete(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }
    }
}
