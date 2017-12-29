package interfaces;

import business.Book;

import java.util.List;

public interface Store extends Resource {
    Book search(String title);
    List<Sale> getHistory();
    Cart newCart();
}
