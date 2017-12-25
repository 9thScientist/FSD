import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Bank {
    private Map<Integer, Account> accounts = new HashMap<>();
    private AtomicInteger id = new AtomicInteger(0);

    public CompletableFuture<Void> transfer(int fromId, int toId, int amount) {
        // async para que a livraria não dependa do banco
        CompletableFuture<Void> r = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            // Falta locks por todo o lado. Temos mesmo de os por? :'(
            Account from = accounts.get(fromId);
            Account to = accounts.get(toId);

            from.debit(amount);
            to.credit(amount);
            r.complete(null);
        });

        return r;
    }

    public int newAccount(int initialBalance) {
        Account acc = new Account(initialBalance);
        int accId = id.getAndIncrement();

        accounts.put(accId, acc);

        // Retorno o id porque sendo uma aplicação diferente não me parece bem passar a referência.
        return accId;
    }

    public List<Integer> getTransactions(int accId) {
        Account acc = accounts.get(accId);
        return (List<Integer>) acc.transactions.clone();
    }

    private class Account {
        private int balance;
        private ArrayList<Integer> transactions = new ArrayList<>();

        Account(int balance) {
            this.balance = balance;
        }

        public synchronized void debit(int amount) {
            balance -= amount;
            transactions.add(-amount);
        }

        public synchronized void credit(int amount) {
            balance += amount;
            transactions.add(amount);
        }
    }
}
