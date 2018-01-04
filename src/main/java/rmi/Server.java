package rmi;

import com.transactions.*;
import com.Request;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Server {
    private Map<Integer, Transaction> logTransactions = new HashMap<>();
    private final Map<Class<? extends Request>, Consumer<Object>> handlers = new HashMap();
    private Address addr;
    protected DistributedObject objs;
    protected Log log;

    private List<Object> save = new ArrayList<>();
    protected ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());
    private Transport t = new NettyTransport();

    public Server(Address addr, String logName) {
        this.addr = addr;

        objs = new DistributedObject(addr);
        log = new Log(logName);

        tc.serializer().register(ManagerAddResourceReq.class);
        tc.serializer().register(ManagerAddResourceRep.class);
        registerMessages(tc.serializer());
        registerLogHandlers(objs);
    }

    public void startTransaction(Exportable obj, Request request) {
        obj.lock();
        Manager.add(request.getContext(), objs.exportObject(obj.getClass(), obj));

        if (request != null) {
            log.append(request);
            backup(save);
        }

        handleManager(obj);
    }

    private void handleManager(Exportable obj) {
        Context current = Manager.getContext();
        Address[] addresses = new Address[2];
        addresses[0] = new Address(addr.host(), addr.port() + 100 );
        addresses[1] = current.getAddress();

        tc.execute(() -> {
            Clique c = new Clique(t, 0, addresses);

            c.handler(ManagerAbortReq.class, (s, m) -> {
               log.append(m);
               obj.unlock();
               Manager.setContext(null);
               rollback(save);

               c.send(1, new ManagerAbortRep());
               c.close();
            });
            c.handler(ManagerCommitReq.class, (s, m) -> {
                log.append(m);
                obj.unlock();
                Manager.setContext(null);

                c.send(1, new ManagerCommitRep());
                c.close();
            });
            c.handler(ManagerPreparedReq.class, (s, m) -> {
                log.append(m);
                c.send(1, new ManagerPreparedRep(true));
            });

            c.open();
        });
    }

    public void recover() {
        System.out.println("starting recoverng...");
        tc.execute(() -> {
            log.handler(ManagerPreparedReq.class, (id, prep) -> {
                System.out.println("Found prepared");
                int xid = prep.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                t.prepare();
            });
            log.handler(ManagerCommitReq.class, (id, commit) -> {
                System.out.println("Found commit");
                int xid = commit.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                t.commit();
            });
            for(Class<? extends Request> cls: handlers.keySet())
                log.handler(cls, (id, req) -> {
                    System.out.println("Found request: " + cls.getName());
                    int xid = req.getContext().getContextId();
                    Transaction t = logTransactions.get(xid);

                    if(t == null) {
                        t = new Transaction(req.getContext());
                        logTransactions.put(xid, t);
                    }

                    t.add(req);
                });

            log.open().thenRun(() -> {
                logTransactions.values().stream()
                        .filter(Transaction::isIncomplete)
                        .forEach(Transaction::askManager);
            });
        });
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

    public abstract void run(Address addr, Transport t);

    private class Transaction {
        private Context ctx;
        private List<Request> requests = new ArrayList<>();
        private boolean prepared = false, committed = false;

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

        public void prepare() {
            prepared = true;
        }

        public boolean isIncomplete() {
            return prepared && !committed;
        }

        public void askManager() {
            Address[] addresses = new Address[] {
                new Address(addr.host(), addr.port() + 100),
                ctx.getAddress()
            };

            tc.execute(() -> {
                Clique c = new Clique(t, 0, addresses);

                c.handler(ManagerAskRep.class, (s, r) -> {
                    int xid = r.getContext().getContextId();
                    Transaction t = logTransactions.get(xid);

                    if (r.isCommit())
                        t.commit();

                    c.close();
                });

                c.open().thenRun(() ->{
                    c.send(1, new ManagerAskReq(ctx));
                });
            });

        }
    }
}
