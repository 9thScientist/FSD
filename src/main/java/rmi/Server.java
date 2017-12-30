package rmi;

import com.Request;
import com.transactions.*;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.CatalystSerializable;
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
    private Map<Integer, Transaction> logTransactions;
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

        registerMessages();
    }

    public void startTransaction(Exportable obj, CatalystSerializable request) {
        obj.lock();

        if (request != null) {
            log.append(request);
            backup(save);
        }

        handleManager(obj);
    }

    public void handleManager(Exportable obj) {
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
            });
            c.handler(ManagerCommitReq.class, (s, m) -> {
                log.append(m);
                obj.unlock();
                Manager.setContext(null);

                c.send(1, new ManagerCommitRep());
            });
            c.handler(ManagerPreparedReq.class, (s, m) -> {
                log.append(m);
                c.send(1, new ManagerPreparedRep(true));
            });

            c.open();
        });
    }

    public void recover() {
        tc.execute(() -> {
            log.handler(ManagerPreparedReq.class, (id, prep) -> {
                int xid = prep.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                t.prepare();
            });
            log.handler(ManagerCommitReq.class, (id, commit) -> {
                int xid = commit.getContext().getContextId();
                Transaction t = logTransactions.get(xid);

                t.apply();
            });

            for(Class<? extends Request> cls: handlers.keySet())
                log.handler(cls, (id, req) -> {
                    int xid = req.getContext().getContextId();
                    Transaction t = logTransactions.get(xid);

                    if(t == null) {
                        t = new Transaction(req.getContext());
                        logTransactions.put(xid, t);
                    }

                    t.add(req);
                });

            log.open().thenRun(() -> {

            });
        });
    }

    protected abstract void backup(List<Object> save);

    protected abstract void rollback(List<Object> save);

    public abstract void registerMessages();

    public abstract void registerLogHandlers();

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
        private boolean prepared = false;

        Transaction(Context ctx) {
            this.ctx = ctx;
        }

        public void add(Request req) {
            requests.add(req);
        }

        public void apply() {
            for(Request req: requests) {
                Consumer<Object> handler = handlers.get(req.getClass());
                if (handler != null)
                    handler.accept(req);
            }
        };

        public void prepare() {
            prepared = true;
        }

        public void askManager() {
        }
    }
}
