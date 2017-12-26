import business.Book;
import interfaces.*;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import remote.RemoteBank;
import remote.RemoteStore;
import rmi.Reference;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Client {
    public static Store lookupStore() {
        ThreadContext tc = new SingleThreadContext("s-%d", new Serializer());
        Transport t = new NettyTransport();
        Address addr = new Address(":1191");
        Reference<Store> ref = new Reference<>(addr, 0, Store.class);
        Connection c;

        try {
            c = (Connection) tc.execute(() ->
                    t.client().connect(addr)
            ).join().get();

            return new RemoteStore(tc, c, 0, ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bank lookupBank() {
        ThreadContext tc = new SingleThreadContext("s-%d", new Serializer());
        Transport t = new NettyTransport();
        Address addr = new Address(":1192");
        Reference<Bank> ref = new Reference<>(addr, 0, Bank.class);
        Connection c;

        try {
            c = (Connection) tc.execute(() ->
                    t.client().connect(addr)
            ).join().get();

            return new RemoteBank(tc, c, 0, ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Bank bank = lookupBank();
        Store store = lookupStore();

        Book b = store.search("The buisness.Book Thief");

        Cart cart = store.newCart();
        cart.add(b);

        Account acc = bank.newAccount(500);
        cart.buy(acc);

        List<Sale> myBooks = store.getHistory();

        for(Sale s: myBooks)
            for(Book bb: s.getSold())
                System.out.println(bb.getAuthor());
    }
}
