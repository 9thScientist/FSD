package interfaces;

import business.Book;
import business.StoreImpl;

import java.util.List;

public interface Store {
    Book search(String title);
    List<StoreImpl.Sale> getHistory();
    Cart newCart();
}
