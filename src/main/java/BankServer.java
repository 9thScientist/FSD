import business.BankImpl;
import com.*;
import interfaces.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import rmi.*;

import java.util.List;

public class BankServer extends Server {
    private Bank bank;

    BankServer(Bank bank, Address addr, String logName) {
        super(addr, logName);
        this.bank = bank;
    }

    public static void main(String[] args) {
        Address address = new Address("localhost:11192");
        Bank bank = new BankImpl();

        BankServer srv = new BankServer(bank, address, "bank");
        srv.objs.exportObject(Bank.class, (Exportable) bank);

        srv.start();
        System.out.println("Server ready on " + address + ".");
    }

    public void backup(List<Object> save) {
        save.add(bank.clone());
        save.add(objs.clone());
    }

    public void rollback(List<Object> save) {
        bank = (Bank) save.get(0);
        objs = (DistributedObject) save.get(1);
    }

    public void run(Address address, Transport t) {
        tc.execute(()-> {
            t.server().listen(address, (c)-> {
                c.handler(BankMakeAccountReq.class, m -> {
                    Bank bank = (Bank) objs.get(m.getBankId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) bank, m);

                    int ib = m.getInitialBalance();

                    Account acc = bank.newAccount(ib);
                    Reference<Account> ref = objs.exportObject(Account.class, (Exportable) acc);

                    return Futures.completedFuture(new BankMakeAccountRep(ref));
                });
                c.handler(AccountTransferReq.class, m -> {
                    Account from = (Account) objs.get(m.getAccountId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) from, m);

                    Account to = objs.get(m.getTo());
                    int amount = m.getAmount();

                    boolean success = from.transfer(to, amount);

                    return Futures.completedFuture(new AccountTransferRep(success));
                });
                c.handler(AccountGetTransactionsReq.class, m -> {
                    Account acc = (Account) objs.get(m.getAccountId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) acc, null);

                    List<Integer> transactions = acc.getTransactions();

                    return Futures.completedFuture(new AccountGetTransactionsRep(transactions));
                });
                c.handler(AccountDebitReq.class, m -> {
                    Account acc = (Account) objs.get(m.getAccountId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) acc, m);

                    int amount = m.getAmount();
                    acc.debit(amount);

                    return Futures.completedFuture(new AccountDebitRep());
                });
                c.handler(AccountCreditReq.class, m -> {
                    Account acc = (Account) objs.get(m.getAccountId());

                    Manager.setContext(m.getContext());

                    if (m.getContext() != null)
                        startTransaction((Exportable) acc, m);

                    int amount = m.getAmount();
                    acc.credit(amount);

                    return Futures.completedFuture(new AccountCreditRep());
                });
            });
        });
    }

    public void registerMessages() {
        tc.serializer().register(Reference.class);
        tc.serializer().register(Context.class);

        tc.serializer().register(BankMakeAccountReq.class);
        tc.serializer().register(BankMakeAccountRep.class);
        tc.serializer().register(AccountTransferReq.class);
        tc.serializer().register(AccountTransferRep.class);
        tc.serializer().register(AccountGetTransactionsReq.class);
        tc.serializer().register(AccountGetTransactionsRep.class);
        tc.serializer().register(AccountDebitReq.class);
        tc.serializer().register(AccountDebitRep.class);
        tc.serializer().register(AccountCreditReq.class);
        tc.serializer().register(AccountCreditRep.class);
    }
}
