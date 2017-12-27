package interfaces;

import business.Book;

public interface Cart {
    void add(Book b);
    Sale buy(Account bankAcc);
}
