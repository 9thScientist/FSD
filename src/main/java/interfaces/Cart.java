package interfaces;

import business.Book;

public interface Cart extends Resource {
    void add(Book b);
    Sale buy(Account bankAcc);
}
