import business.BankImpl;
import com.*;
import interfaces.Account;
import interfaces.Bank;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Transport;
import rmi.*;

import java.util.List;

public class BankServer extends Server {
    private Bank bank;

    BankServer(Bank bank, io.atomix.catalyst.transport.Address addr, String logName) {
        super(addr, logName);
        this.bank = bank;
    }

    public static void main(String[] args) {
        io.atomix.catalyst.transport.Address address = new io.atomix.catalyst.transport.Address("localhost:11192");
        Bank bank = new BankImpl();

        BankServer srv = new BankServer(bank, address, "bank");
        srv.objs.exportObject(Bank.class, (Exportable) bank);

        srv.recover().thenRun(() -> {
            srv.start();
        });
    }

    public void backup(List<Object> save) {
        save.add(bank.clone());
        save.add(objs.clone());
    }

    public void rollback(List<Object> save) {
        bank = (Bank) save.get(0);
        objs = (DistributedObject) save.get(1);
    }

    public void run(io.atomix.catalyst.transport.Address address, Transport t) {
        tc.execute(()-> {
            System.out.println("Server ready on " + address + ".");
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

    public void registerMessages(Serializer serializer) {
        serializer.register(Reference.class);
        serializer.register(Context.class);

        serializer.register(BankMakeAccountReq.class);
        serializer.register(BankMakeAccountRep.class);
        serializer.register(AccountTransferReq.class);
        serializer.register(AccountTransferRep.class);
        serializer.register(AccountGetTransactionsReq.class);
        serializer.register(AccountGetTransactionsRep.class);
        serializer.register(AccountDebitReq.class);
        serializer.register(AccountDebitRep.class);
        serializer.register(AccountCreditReq.class);
        serializer.register(AccountCreditRep.class);
    }

    @Override
    public void registerLogHandlers(DistributedObject objs) {
        logHandler(BankMakeAccountReq.class, req -> {
            Bank b = (Bank) objs.get(req.getBankId());

            Account acc = b.newAccount(req.getInitialBalance());
            objs.exportObject(Account.class, (Exportable) acc);
        });
        logHandler(AccountCreditReq.class, req -> {
            Account acc = (Account) objs.get(req.getAccountId());

            if (acc != null)
                acc.credit(req.getAmount());
        });
        logHandler(AccountDebitReq.class, req -> {
            Account acc = (Account) objs.get(req.getAccountId());

            if (acc != null)
                acc.debit(req.getAmount());
        });
        logHandler(AccountTransferReq.class, req -> {
            Account acc = (Account) objs.get(req.getAccountId());

            if (acc != null)
                acc.debit(req.getAmount());
        });
    }
}
