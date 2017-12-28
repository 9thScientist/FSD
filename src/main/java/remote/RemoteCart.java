package remote;

import business.Book;
import com.CartAddRep;
import com.CartAddReq;
import com.CartBuyRep;
import com.CartBuyReq;
import interfaces.Account;
import interfaces.Cart;
import interfaces.Sale;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import rmi.Context;
import rmi.DistributedObject;
import rmi.Manager;
import rmi.Reference;

public class RemoteCart extends Remote implements Cart {
    public RemoteCart(ThreadContext tc, Connection c, Integer id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public void add(Book b) {
        try {
            Context ctx = Manager.context.get();

            if (ctx != null)
                Manager.add(ctx, getReference());

            CartAddRep r = (CartAddRep) tc.execute(() ->
                c.sendAndReceive(new CartAddReq(id, b, ctx))
            ).join().get();

            return;
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public Sale buy(Account from) {
        try {
            Reference clientRef = ((RemoteAccount) from).getReference();
            Context ctx = Manager.context.get();

            if (ctx != null)
                Manager.add(ctx, getReference());

            CartBuyRep r = (CartBuyRep) tc.execute(() ->
                c.sendAndReceive(new CartBuyReq(clientRef, id, ctx))
            ).join().get();

            return DistributedObject.importObject(r.getSale());
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void registerMessages() {
        tc.serializer().register(CartAddReq.class);
        tc.serializer().register(CartAddRep.class);
        tc.serializer().register(CartBuyReq.class);
        tc.serializer().register(CartBuyRep.class);
    }
}
