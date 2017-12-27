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
import io.atomix.catalyst.transport.Connection;
import rmi.Reference;

public class RemoteCart extends Remote implements Cart {
    public RemoteCart(ThreadContext tc, Connection c, int id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public void add(Book b) {
        try {
            CartAddRep r = (CartAddRep) tc.execute(() ->
                c.sendAndReceive(new CartAddReq(id, b))
            ).join().get();

            return;
        } catch(Exception e) {
            return;
        }
    }

    @Override
    public Sale buy(Account bankAcc) {
        try {
            CartBuyRep r = (CartBuyRep) tc.execute(() ->
                c.sendAndReceive(new CartBuyReq())
            ).join().get();

            return r.getSale();
        } catch(Exception e) {
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
