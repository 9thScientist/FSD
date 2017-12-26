package interfaces;

import java.util.List;

public interface Account {
    void credit(int amount);
    void debit(int amount);
    List<Integer> getTransactions();
    CompletableFuture<Boolean> transfer(Account to, int amount);
}
