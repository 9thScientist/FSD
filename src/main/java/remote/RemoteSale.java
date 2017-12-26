package remote;

import business.Book;
import com.SaleGetSoldRep;
import com.SaleGetSoldReq;
import com.SaleIsPaidRep;
import com.SaleIsPaidReq;
import interfaces.Sale;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.Reference;

import java.util.List;

public class RemoteSale extends Remote implements Sale {
    private List<Book> sold = null;

    public RemoteSale(ThreadContext tc, Connection c, int id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public List<Book> getSold() {
        if (sold != null)
            return sold;

        try {
            SaleGetSoldRep r = (SaleGetSoldRep) tc.execute(() ->
                    c.sendAndReceive(new SaleGetSoldReq())
            ).join().get();

            sold = r.getSoldBooks();
            return sold;
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public boolean isPaid() {
        try {
            SaleIsPaidRep r = (SaleIsPaidRep) tc.execute(() ->
                    c.sendAndReceive(new SaleIsPaidReq())
            ).join().get();

            return r.isPaid();
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public void registerMessages() {
    }
}
