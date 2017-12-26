package interfaces;

import business.Book;

public interface Cart {
    void add(Book b);
    void buy(Account bankAcc);
}
