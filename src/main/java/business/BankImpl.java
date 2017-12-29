package business;

import com.AccountCreditReq;
import interfaces.Account;
import interfaces.Bank;
import rmi.Exportable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BankImpl extends Exportable implements Bank {
    private Map<Integer, AccountImpl> accounts = new HashMap<>();
    private AtomicInteger id = new AtomicInteger(0);

    public Account newAccount(int initialBalance) {
        AccountImpl acc = new AccountImpl(initialBalance);

        accounts.put(id.getAndIncrement(), acc);

        return acc;
    }

    private void setId(AtomicInteger value) {
        id.set(value.get());
    }

    private void setAccounts(Map<Integer, AccountImpl> accounts) {
        this.accounts = new HashMap<>();

        accounts.forEach((k, v) -> this.accounts.put(k, v.clone()));
    }

    public BankImpl clone() {
        BankImpl copy = new BankImpl();
        copy.setId(this.id);
        copy.setAccounts(this.accounts);

        return copy;
    }

    private class AccountImpl extends Exportable implements Account {
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

        private void setTransactions(List<Integer> transactions) {
            this.transactions = new ArrayList<>();

            for(Integer t: transactions)
                this.transactions.add(t);
        }

        public AccountImpl clone() {
            AccountImpl copy = new AccountImpl(balance);
            copy.setTransactions(getTransactions());

            return copy;
        }
    }
}
