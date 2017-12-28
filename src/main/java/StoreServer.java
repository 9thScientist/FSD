import business.BankImpl;
import business.Book;
import business.StoreImpl;
import com.*;
import interfaces.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Log;
import remote.RemoteBank;
import remote.RemoteCart;
import rmi.*;

import javax.sound.midi.SysexMessage;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StoreServer {
    private static Lock lock = new ReentrantLock();
    private static Log log = new Log("store");

    public static Bank lookupBank() {
        ThreadContext tc = new SingleThreadContext("s-%d", new Serializer());
        Transport t = new NettyTransport();
        Address addr = new Address("localhost:11192");
        Reference<Bank> ref = new Reference<>(addr, 1, Bank.class);
        Connection c;

        try {
            c = tc.execute(() ->
                    t.client().connect(addr)
            ).join().get();

            return new RemoteBank(tc, c, 1, ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Log log = new Log("bank");
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Address address = new Address("localhost:11191");
        DistributedObject d = new DistributedObject(address);

        registMessages(tc);
        assignHandlers(t, tc, address, d);
        assignLogHandlers(tc);

        Bank b = lookupBank();
        Store store = new StoreImpl(b);
        d.exportObject(Store.class, (Exportable) store);

        System.out.println("Server ready on " + address.toString() + ".");
    }

    private static void assignLogHandlers(ThreadContext tc) {
    }

    private static void assignHandlers(Transport t, ThreadContext tc, Address address, DistributedObject d) {
        tc.execute(()-> {
            t.server().listen(address, (c)-> {
                /*
                 * Store Handlers
                 */
                c.handler(StoreSearchReq.class, (m) -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Store store = (Store) d.get(m.getStoreId());
                    String title = m.getTitle();

                    Book book = store.search(title);

                    return Futures.completedFuture(new StoreSearchRep(book));
                });
                c.handler(StoreMakeCartReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Store store = (Store) d.get(m.getStoreId());

                    Cart cart =  store.newCart();
                    Reference<Cart> ref = d.exportObject(Cart.class, (Exportable) cart);

                    if (m.getContext() != null)
                        log.append(cart);

                    return Futures.completedFuture(new StoreMakeCartRep(ref));
                });
                c.handler(StoreGetHistoryReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Store store = (Store) d.get(m.getStoreId());

                    List<Sale> history = store.getHistory();
                    List<Reference<Sale>> refs = d.exportList(Sale.class, (List) history);

                    return Futures.completedFuture(new StoreGetHistoryRep(refs));
                });

                /*
                 * Cart Handlers
                 */
                c.handler(CartAddReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Cart cart = (Cart) d.get(m.getCartId());
                    Book book = m.getBook();

                    cart.add(book);

                    if (m.getContext() != null)
                        log.append(cart);

                    return Futures.completedFuture(new CartAddRep());
                });
                c.handler(CartBuyReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Cart cart = (Cart) d.get(m.getCartId());
                    Account clientAccount = DistributedObject.importObject(m.getClientAccount());

                    Sale sale = cart.buy(clientAccount);
                    Reference<Sale> ref = d.exportObject(Sale.class, (Exportable) sale);

                    if (m.getContext() != null)
                        log.append(sale);

                    return Futures.completedFuture(new CartBuyRep(ref));
                });

                /*
                 * Sale Handlers
                 */
                c.handler(SaleGetSoldReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Sale sale = (Sale) d.get(m.getSaleId());
                    List<Book> sold = sale.getSold();

                    return Futures.completedFuture(new SaleGetSoldRep(sold));
                });
                c.handler(SaleIsPaidReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Sale sale = (Sale) d.get(m.getSaleId());

                    Boolean paid = sale.isPaid();

                    return Futures.completedFuture(new SaleIsPaidRep(paid));
                });
            });
        });
    }

    private static void registMessages(ThreadContext tc) {
        tc.serializer().register(Reference.class);
        tc.serializer().register(Context.class);

        tc.serializer().register(StoreGetHistoryReq.class);
        tc.serializer().register(StoreGetHistoryRep.class);
        tc.serializer().register(StoreMakeCartReq.class);
        tc.serializer().register(StoreMakeCartRep.class);
        tc.serializer().register(StoreSearchReq.class);
        tc.serializer().register(StoreSearchRep.class);

        tc.serializer().register(SaleGetSoldReq.class);
        tc.serializer().register(SaleGetSoldRep.class);
        tc.serializer().register(SaleIsPaidReq.class);
        tc.serializer().register(SaleIsPaidRep.class);

        tc.serializer().register(CartAddReq.class);
        tc.serializer().register(CartAddRep.class);
        tc.serializer().register(CartBuyReq.class);
        tc.serializer().register(CartBuyRep.class);
    }
}
