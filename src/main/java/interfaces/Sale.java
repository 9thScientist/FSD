package interfaces;

import business.Book;

import java.util.List;

public interface Sale extends Resource {
    List<Book> getSold();
    boolean isPaid();
}
