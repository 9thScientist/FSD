package business;

import interfaces.Account;
import interfaces.Bank;
import io.atomix.catalyst.concurrent.Futures;
import org.omg.CORBA.TIMEOUT;
import rmi.Exportable;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BankImpl extends Exportable implements Bank {
    private Map<Integer, AccountImpl> accounts = new HashMap<>();
    private AtomicInteger id = new AtomicInteger(0);

    public Account newAccount(int initialBalance) {
        AccountImpl acc = new AccountImpl(initialBalance);

        accounts.put(id.getAndIncrement(), acc);

        return acc;
    }

    private class AccountImpl extends Exportable implements Account {
        private int balance;
        private ArrayList<Integer> transactions = new ArrayList<>();

        AccountImpl(int balance) {
            this.balance = balance;
        }

        public CompletableFuture<Void> transfer(Account to, int amount) {
            CompletableFuture<Void> r = new CompletableFuture<>();

            CompletableFuture.runAsync(() -> {
                debit(amount);
                to.credit(amount);
            }).thenRun(() -> r.complete(null));

            return r;
        }

        public List<Integer> getTransactions() {
            return (List<Integer>) transactions.clone();
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
