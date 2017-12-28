package rmi;

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
import java.util.List;

public abstract class Server {
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

    protected abstract void backup(List<Object> save);

    protected abstract void rollback(List<Object> save);

    public abstract void registerMessages();

    public void start() {
        run(addr, t);
    }

    public abstract void run(Address addr, Transport t);
}
