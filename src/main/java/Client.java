import business.Book;
import business.StoreImpl;
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
import rmi.Manager;
import rmi.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    public static Store lookupStore() {
        ThreadContext tc = new SingleThreadContext("s-%d", new Serializer());
        Transport t = new NettyTransport();
        Address addr = new Address("localhost:11191");
        Reference<Store> ref = new Reference<>(addr, 1, Store.class);

        Connection c;

        try {
            c = tc.execute(() ->
                    t.client().connect(addr)
            ).join().get();

            return new RemoteStore(tc, c, 1, ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bank lookupBank() {
        ThreadContext tc = new SingleThreadContext("s-%d", new Serializer());
        Transport t = new NettyTransport();
        Address addr = new Address("localhost:11192");
        Reference<Bank> ref = new Reference<>(addr, 1, Bank.class);
        Connection c;

        try {
            c = (Connection) tc.execute(() ->
                    t.client().connect(addr)
            ).join().get();

            return new RemoteBank(tc, c, 1, ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Manager.recover();
        
        Store store = lookupStore();
        Bank bank = lookupBank();

        Book b = store.search("The buisness.Book Thief");
        System.out.println("Found book: " + b.getTitle());

        Cart cart = store.newCart();
        System.out.println("Created cart: " + cart);

        cart.add(b);
        System.out.println("Added book to cart");

        Account acc = bank.newAccount(500);
        System.out.println("Created bank account: " + acc);

        cart.buy(acc);
        System.out.println("Bought books in my cart");

        List<Sale> myBooks = store.getHistory();
        System.out.println("Got history from store");

        for(Sale s: myBooks)
            for(Book bb: s.getSold())
                System.out.println(bb.getTitle());
    }
}
