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
import remote.RemoteBank;
import remote.RemoteCart;
import rmi.DistributedObject;
import rmi.Exportable;
import rmi.Reference;

import javax.sound.midi.SysexMessage;
import java.util.List;

public class StoreServer {
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
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Address address = new Address("localhost:11191");
        DistributedObject d = new DistributedObject(address);

        registMessages(tc);
        assignHandlers(t, tc, address, d);

        Bank b = lookupBank();
        Store store = new StoreImpl(b);
        d.exportObject(Store.class, (Exportable) store);

        System.out.println("Server ready on " + address.toString() + ".");
    }

    private static void assignHandlers(Transport t, ThreadContext tc, Address address, DistributedObject d) {
        tc.execute(()-> {
            t.server().listen(address, (c)-> {
                /*
                 * Store Handlers
                 */
                c.handler(StoreSearchReq.class, (m) -> {
                    Store store = (Store) d.get(m.getStoreId());

                    String title = m.getTitle();

                    Book book = store.search(title);

                    return Futures.completedFuture(new StoreSearchRep(book));
                });
                c.handler(StoreMakeCartReq.class, m -> {
                    Store store = (Store) d.get(m.getStoreId());

                    Cart cart =  store.newCart();
                    Reference<Cart> ref = d.exportObject(Cart.class, (Exportable) cart);

                    return Futures.completedFuture(new StoreMakeCartRep(ref));
                });
                c.handler(StoreGetHistoryReq.class, m -> {
                    Store store = (Store) d.get(m.getStoreId());

                    List<Sale> history = store.getHistory();
                    List<Reference<Sale>> refs = d.exportList(Sale.class, (List) history);

                    return Futures.completedFuture(new StoreGetHistoryRep(refs));
                });

                /*
                 * Cart Handlers
                 */
                c.handler(CartAddReq.class, m -> {
                    Cart cart = (Cart) d.get(m.getCartId());
                    Book book = m.getBook();

                    cart.add(book);

                    return Futures.completedFuture(new CartAddRep());
                });
                c.handler(CartBuyReq.class, m -> {
                    Cart cart = (Cart) d.get(m.getCartId());
                    Account clientAccount = null;
                    try {
                        clientAccount = DistributedObject.importObject(m.getClientAccount());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Sale sale = cart.buy(clientAccount);
                    System.out.println("Transfer processed: " + sale.isPaid());
                    Reference<Sale> ref = d.exportObject(Sale.class, (Exportable) sale);

                    return Futures.completedFuture(new CartBuyRep(ref));
                });

                /*
                 * Sale Handlers
                 */
                c.handler(SaleGetSoldReq.class, m -> {
                    Sale sale = (Sale) d.get(m.getSaleId());
                    List<Book> sold = sale.getSold();

                    return Futures.completedFuture(new SaleGetSoldRep(sold));
                });
                c.handler(SaleIsPaidReq.class, m -> {
                    Sale sale = (Sale) d.get(m.getSaleId());

                    boolean paid = sale.isPaid();
                    System.out.println("Is it paid?: " + paid);

                    return Futures.completedFuture(new SaleIsPaidRep(paid));
                });

                /*
                 * Bank async handler
                 */
                c.handler(AccountTransferRep.class, m -> {
                    
                    return null;
                });
            });
        });
    }

    private static void registMessages(ThreadContext tc) {
        tc.serializer().register(Reference.class);

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
