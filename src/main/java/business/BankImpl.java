package business;

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

    public Account newAccount(int initialBalance) {
        AccountImpl acc = new AccountImpl(initialBalance);

        accounts.put(id.getAndIncrement(), acc);

        return acc;
    }

    private class AccountImpl implements Account {
        private int balance;
        private ArrayList<Integer> transactions = new ArrayList<>();

        AccountImpl(int balance) {
            this.balance = balance;
        }

        public boolean transfer(Account to, int amount) {
            debit(amount);
            to.credit(amount);

            return true;
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
