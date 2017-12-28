package remote;

import business.Book;
import com.*;
import interfaces.Cart;
import interfaces.Sale;
import interfaces.Store;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.Context;
import rmi.DistributedObject;
import rmi.Manager;
import rmi.Reference;

import java.util.List;

public class RemoteStore extends Remote implements Store {
    public RemoteStore(ThreadContext tc, Connection c, Integer id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public Book search(String title) {
        try {
            Context ctx = Manager.context.get();

            if (ctx != null)
                Manager.add(ctx, getReference());

            StoreSearchRep r = (StoreSearchRep) tc.execute(() ->
                c.sendAndReceive(new StoreSearchReq(title, id, ctx))
            ).join().get();

            return r.getBook();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Sale> getHistory() {
        try {
            Context ctx = Manager.context.get();

            if (ctx != null)
                Manager.add(ctx, getReference());

            StoreGetHistoryRep r = (StoreGetHistoryRep) tc.execute(() ->
                c.sendAndReceive(new StoreGetHistoryReq(id, ctx))
            ).join().get();

            return DistributedObject.importList(r.getSales());
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Cart newCart() {
        try {
            Context ctx = Manager.context.get();

            if (ctx != null)
                Manager.add(ctx, getReference());

            StoreMakeCartRep r = (StoreMakeCartRep) tc.execute(() ->
                    c.sendAndReceive(new StoreMakeCartReq(id, ctx))
            ).join().get();

            return DistributedObject.importObject(r.getCart());
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public void registerMessages() {
        tc.serializer().register(StoreMakeCartReq.class);
        tc.serializer().register(StoreMakeCartRep.class);
        tc.serializer().register(StoreGetHistoryReq.class);
        tc.serializer().register(StoreGetHistoryRep.class);
        tc.serializer().register(StoreSearchReq.class);
        tc.serializer().register(StoreSearchRep.class);
    }
}
