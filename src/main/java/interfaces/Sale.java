package interfaces;

import business.Book;

import java.util.List;

public interface Sale {
    List<Book> getSold();
    boolean isPaid();
}
