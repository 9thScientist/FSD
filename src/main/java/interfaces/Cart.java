package interfaces;

import business.Book;

public interface Cart {
    void add(Book b);
    int value();
    void buy(Bank bank, int client);
}
