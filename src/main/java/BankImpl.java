import interfaces.Account;
import interfaces.Bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class BankImpl implements Bank {
    private Map<Integer, AccountImpl> accounts = new HashMap<>();
    private AtomicInteger id = new AtomicInteger(0);

    public CompletableFuture<Boolean> transfer(int fromId, int toId, int amount) {
        // async para que a livraria não dependa do banco
        CompletableFuture<Boolean> r = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            // Falta locks por todo o lado. Temos mesmo de os por? :'(
            AccountImpl from = accounts.get(fromId);
            AccountImpl to = accounts.get(toId);

            from.debit(amount);
            to.credit(amount);
            r.complete(true);
        });

        return r;
    }

    public int newAccount(int initialBalance) {
        AccountImpl acc = new AccountImpl(initialBalance);
        int accId = id.getAndIncrement();

        accounts.put(accId, acc);

        // Retorno o id porque sendo uma aplicação diferente não me parece bem passar a referência.
        return accId;
    }

    public List<Integer> getTransactions(int accId) {
        AccountImpl acc = accounts.get(accId);
        return (List<Integer>) acc.transactions.clone();
    }

    private class AccountImpl implements Account {
        private int balance;
        private ArrayList<Integer> transactions = new ArrayList<>();

        AccountImpl(int balance) {
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
