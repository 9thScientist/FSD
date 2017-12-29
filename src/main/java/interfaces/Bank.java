package interfaces;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Bank extends Resource {
    Account newAccount(int balance);
}
