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
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import rmi.DistributedObject;
import rmi.Reference;

import java.util.List;

public class StoreServer {
    public static void main(String[] args) {
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Address address = new Address(":11191");
        DistributedObject d = new DistributedObject(address);

        registMessages(tc);
        assignHandlers(t, tc, address, d);

        // TODO this bank should be in another Server
        Bank b = new BankImpl();
        Store store = new StoreImpl(b);
        d.exportObject(Store.class, store);

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

                    Cart cart = store.newCart();
                    Reference ref = d.exportObject(Cart.class, cart);

                    return Futures.completedFuture(new StoreMakeCartRep(ref));
                });
                c.handler(StoreGetHistoryReq.class, m -> {
                    Store store = (Store) d.get(m.getStoreId());

                    List<Sale> history = store.getHistory();

                    return Futures.completedFuture(new StoreGetHistoryRep(history));
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
                    Account account = (Account) m.getClientAccount();

                    Sale sale = cart.buy(account);
                    d.exportObject(Sale.class, sale);

                    return Futures.completedFuture(new CartBuyRep(sale));
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

                    Boolean paid = sale.isPaid();

                    return Futures.completedFuture(new SaleIsPaidRep(paid));
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

        tc.serializer().register(Sale.class);
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
