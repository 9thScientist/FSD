package interfaces;

import java.util.List;

public interface Account {
    void credit(int amount);
    void debit(int amount);
    List<Integer> getTransactions();
    boolean transfer(Account to, int amount);
}
