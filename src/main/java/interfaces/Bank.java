package interfaces;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Bank {
    Account newAccount(int balance);
}
