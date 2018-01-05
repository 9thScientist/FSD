package rmi;

import com.Request;
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
import remote.RemoteManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class Server {
    private Map<Integer, Transaction> logTransactions = new HashMap<>();
    private final Map<Class<? extends Request>, Consumer<Object>> handlers = new HashMap();
    private io.atomix.catalyst.transport.Address addr;
    private RemoteManager manager;
    protected DistributedObject objs;
    private Clique clique;
    protected Log log;

    private List<Object> save = new ArrayList<>();
    protected ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());
    private Transport t = new NettyTransport();

    public Server(Address addr, String logName) {
        this.addr = addr;

        manager = lookupManager();
        objs = new DistributedObject(addr);
        log = new Log(logName);

        tc.serializer().register(ManagerAddResourceReq.class);
        tc.serializer().register(ManagerAddResourceRep.class);
        tc.serializer().register(ManagerPreparedReq.class);
        tc.serializer().register(ManagerPreparedRep.class);
        tc.serializer().register(ManagerCommitReq.class);
        tc.serializer().register(ManagerCommitRep.class);
        tc.serializer().register(ManagerAbortReq.class);
        tc.serializer().register(ManagerAbortRep.class);
        registerMessages(tc.serializer());
        registerLogHandlers(objs);
    }

    public void startTransaction(Exportable obj, Request request) {
        obj.lock();
        int me = manager.add(addr);

        if (request != null) {
            log.append(request);
            backup(save);
        }

        if (clique == null)
            handleManager(obj, me);
    }

    private void handleManager(Exportable obj, int me) {
        Context current = Manager.getContext();
        Address[] addresses = new Address[me + 1];
        addresses[0] = new Address(current.getAddress().host(), current.getAddress().port() + 100);
        addresses[1] = addresses[0];
        addresses[me] = new Address(addr.host(), addr.port() + 100 );

        clique = new Clique(t, me, addresses);

        tc.execute(() -> {
            clique.handler(ManagerAbortReq.class, (s, m) -> {
               log.append(m);
               obj.unlock();
               Manager.setContext(null);
               rollback(save);

               clique.send(0, new ManagerAbortRep());
               clique.close();
            });
            clique.handler(ManagerCommitReq.class, (s, m) -> {
                log.append(m);
                obj.unlock();
                Manager.setContext(null);

                clique.send(0, new ManagerCommitRep());
                clique.close();
            });
            clique.handler(ManagerPreparedReq.class, (s, m) -> {
                log.append(m);
                clique.send(0, new ManagerPreparedRep(true));
            });

            clique.onException((e) -> e.printStackTrace());

            clique.open();
        });
    }

    public CompletableFuture<Void> recover() {
        CompletableFuture<Void> r = new CompletableFuture<>();

        tc.execute(() -> {
            log.handler(ManagerPreparedReq.class, (id, prep) -> {
                int xid = prep.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                t.prepare(prep.getResourceId());
            });
            log.handler(ManagerCommitReq.class, (id, commit) -> {
                int xid = commit.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                t.commit();
            });
            for(Class<? extends Request> cls: handlers.keySet()) {
                log.handler(cls, (id, req) -> {
                    int xid = req.getContext().getContextId();
                    Transaction t = logTransactions.get(xid);

                    if (t == null) {
                        t = new Transaction(req.getContext());
                        logTransactions.put(xid, t);
                    }

                    t.add(req);
                });
            }

            System.out.println("Opening log");
            log.open().thenRun(() -> {
                CompletableFuture[] res = logTransactions.values().stream()
                        .filter(Transaction::isIncomplete)
                        .map(Transaction::askManager)
                        .toArray(CompletableFuture[]::new);

                CompletableFuture.allOf(res).thenRun(() ->
                        r.complete(null)
                );
            });
        });

        return r;
    }

    protected abstract void backup(List<Object> save);

    protected abstract void rollback(List<Object> save);

    public abstract void registerMessages(Serializer serializer);

    public abstract void registerLogHandlers(DistributedObject objs);

    public <T extends Request> void logHandler(Class<T> type, Consumer<T> rh) {
        handlers.put(type, r ->  rh.accept(type.cast(r)) );
    }

    public void start() {
        run(addr, t);
    }

    public abstract void run(io.atomix.catalyst.transport.Address addr, Transport t);

    private class Transaction {
        private Context ctx;
        private List<Request> requests = new ArrayList<>();
        private boolean prepared = false, committed = false;
        private int me;

        Transaction(Context ctx) {
            this.ctx = ctx;
        }

        public Context getContext() {
            return ctx;
        }

        public void add(Request req) {
            requests.add(req);
        }

        public void commit() {
            committed = true;

            for(Request req: requests) {
                Consumer<Object> handler = handlers.get(req.getClass());
                if (handler != null)
                    handler.accept(req);
            }
        }

        public void prepare(int resourceId) {
            prepared = true;
            me = resourceId;
        }

        public boolean isIncomplete() {
            return prepared && !committed;
        }

        public CompletableFuture<Void> askManager() {
            CompletableFuture<Void> res = new CompletableFuture<>();

            Address[] addresses = new io.atomix.catalyst.transport.Address[me + 1];
            addresses[0] = ctx.getAddress();
            addresses[me] = new io.atomix.catalyst.transport.Address(addr.host(), addr.port() + 100);

            tc.execute(() -> {
                if (clique == null)
                    clique = new Clique(t, me, addresses);

                clique.handler(ManagerAskRep.class, (s, r) -> {
                    int xid = r.getContext().getContextId();
                    Transaction t = logTransactions.get(xid);

                    if (r.isCommit())
                        t.commit();

                    clique.close();
                    res.complete(null);
                });

                clique.open().thenRun(() ->{
                    clique.send(0, new ManagerAskReq(ctx));
                });
            });

            return res;
        }
    }

    private RemoteManager lookupManager() {
        ThreadContext tc = new SingleThreadContext("s-%d", new Serializer());
        Transport t = new NettyTransport();
        io.atomix.catalyst.transport.Address addr = new io.atomix.catalyst.transport.Address("localhost:4000");
        Reference<Manager> ref = new Reference<>(addr, 1, Manager.class);

        Connection c;

        try {
            c = tc.execute(() ->
                    t.client().connect(addr)
            ).join().get();

            return new RemoteManager(tc, c, 1, ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
