package interfaces;

import interfaces.Bank;

public interface Cart {
    void add(Book b);
    int value();
    void buy(Bank bank, int client);
}
