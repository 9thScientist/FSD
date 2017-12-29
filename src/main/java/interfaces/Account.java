package interfaces;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Account {
    void credit(int amount);
    void debit(int amount);
    List<Integer> getTransactions();
    CompletableFuture<Void> transfer(Account to, int amount);
}
