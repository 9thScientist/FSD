package remote;

import com.transactions.*;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import rmi.Context;
import rmi.Manager;
import rmi.Reference;

public class RemoteManager extends Remote {
    public RemoteManager(ThreadContext tc, Connection c, Integer id, Reference reference) {
        super(tc, c, id, reference);
    }
    public void begin() {
        try {
            ManagerBeginRep r = (ManagerBeginRep) tc.execute(() ->
                    c.sendAndReceive(new ManagerBeginReq())
            ).join().get();

            Manager.setContext(r.getContext());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return;
    }

    public void commit() {
        try {
            Context ctx = Manager.getContext();
            ManagerCommitRep r = (ManagerCommitRep) tc.execute(() ->
                    c.sendAndReceive(new ManagerCommitReq(ctx))
            ).join().get();
        } catch(Exception e) {
            e.printStackTrace();
        }

        Manager.setContext(null);
        return;
    }

    public int add(Address address) {
        try {
            Context ctx = Manager.getContext();
            ManagerAddResourceRep r = (ManagerAddResourceRep) tc.execute(() ->
                    c.sendAndReceive(new ManagerAddResourceReq(ctx, address))
            ).join().get();

            return r.getResourceId();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public void registerMessages() {
        tc.serializer().register(ManagerAddResourceReq.class);
        tc.serializer().register(ManagerAddResourceRep.class);
        tc.serializer().register(ManagerCommitReq.class);
        tc.serializer().register(ManagerCommitRep.class);
    }
}