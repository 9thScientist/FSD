package interfaces;

import business.Book;

import java.util.List;

public interface Store {
    Book search(String title);
    List<Sale> getHistory();
}
