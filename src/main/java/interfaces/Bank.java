package interfaces;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Bank {
    CompletableFuture<Boolean> transfer(int from, int to, int amount);
    int newAccount(int balance);
    List<Integer> getTransactions(int account);
}
