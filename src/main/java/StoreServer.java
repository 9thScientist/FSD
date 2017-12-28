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
import rmi.*;

import java.util.List;

public class StoreServer extends Server {
    private Store store;

    public StoreServer(Store store, Address addr, String logName) {
        super(addr, logName);
        this.store = store;
    }

    public static void main(String[] args) {
        Bank b = lookupBank();
        Store store = new StoreImpl(b);
        Address address = new Address("localhost:11191");

        StoreServer srv = new StoreServer(store, address, "store");
        srv.objs.exportObject(Store.class, (Exportable) store);

        System.out.println("Server ready on " + address.toString() + ".");
    }

    @Override
    protected void backup(List<Object> save) {
        save.add(store);
        save.add(objs);
    }

    @Override
    protected void rollback(List<Object> save) {
        store = (Store) objs.get(0);
        objs = (DistributedObject) objs.get(1);
    }

    @Override
    public void run(Address address, Transport t) {
        tc.execute(()-> {
            t.server().listen(address, (c)-> {
                /*
                 * Store Handlers
                 */
                c.handler(StoreSearchReq.class, (m) -> {
                    Store store = (Store) objs.get(m.getStoreId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) store, null);

                    String title = m.getTitle();
                    Book book = store.search(title);

                    return Futures.completedFuture(new StoreSearchRep(book));
                });
                c.handler(StoreMakeCartReq.class, m -> {
                    Store store = (Store) objs.get(m.getStoreId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) store, null);

                    Cart cart =  store.newCart();
                    Reference<Cart> ref = objs.exportObject(Cart.class, (Exportable) cart);

                    return Futures.completedFuture(new StoreMakeCartRep(ref));
                });
                c.handler(StoreGetHistoryReq.class, m -> {
                    Store store = (Store) objs.get(m.getStoreId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) store, null);

                    List<Sale> history = store.getHistory();
                    List<Reference<Sale>> refs = objs.exportList(Sale.class, (List) history);

                    return Futures.completedFuture(new StoreGetHistoryRep(refs));
                });

                /*
                 * Cart Handlers
                 */
                c.handler(CartAddReq.class, m -> {
                    Cart cart = (Cart) objs.get(m.getCartId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) cart, null);

                    Book book = m.getBook();
                    cart.add(book);

                    return Futures.completedFuture(new CartAddRep());
                });
                c.handler(CartBuyReq.class, m -> {
                    Cart cart = (Cart) objs.get(m.getCartId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) cart, null);

                    Account clientAccount = DistributedObject.importObject(m.getClientAccount());
                    Sale sale = cart.buy(clientAccount);

                    Reference<Sale> ref = objs.exportObject(Sale.class, (Exportable) sale);
                    return Futures.completedFuture(new CartBuyRep(ref));
                });

                /*
                 * Sale Handlers
                 */
                c.handler(SaleGetSoldReq.class, m -> {
                    Sale sale = (Sale) objs.get(m.getSaleId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) sale, null);

                    List<Book> sold = sale.getSold();

                    return Futures.completedFuture(new SaleGetSoldRep(sold));
                });
                c.handler(SaleIsPaidReq.class, m -> {
                    Sale sale = (Sale) objs.get(m.getSaleId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) sale, null);

                    Boolean paid = sale.isPaid();

                    return Futures.completedFuture(new SaleIsPaidRep(paid));
                });
            });
        });
    }

    public void registerMessages() {
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

    private static Bank lookupBank() {
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

}
