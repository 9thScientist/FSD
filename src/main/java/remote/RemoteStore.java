package remote;

import business.Book;
import com.*;
import interfaces.Cart;
import interfaces.Sale;
import interfaces.Store;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.DistributedObject;
import rmi.Reference;

import java.util.List;

public class RemoteStore extends Remote implements Store {
    public RemoteStore(ThreadContext tc, Connection c, int id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public Book search(String title) {
        try {
            StoreSearchRep r = (StoreSearchRep) tc.execute(() ->
                c.sendAndReceive(new StoreSearchReq(title, id))
            ).join().get();

            return r.getBook();
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public List<Sale> getHistory() {
        try {
            StoreGetHistoryRep r = (StoreGetHistoryRep) tc.execute(() ->
                c.sendAndReceive(new StoreGetHistoryReq(id))
            ).join().get();

            return r.getSales();
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public Cart newCart() {
        try {
            StoreMakeCartRep r = (StoreMakeCartRep) tc.execute(() ->
                    c.sendAndReceive(new StoreMakeCartReq())
            ).join().get();

            return DistributedObject.importObject(r.getCart());
        } catch(Exception e) {
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
