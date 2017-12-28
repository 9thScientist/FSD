import business.BankImpl;
import com.*;
import interfaces.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Log;
import rmi.*;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankServer {
    private static Lock lock = new ReentrantLock();
    private static Log log = new Log("bank");

    public static void main(String[] args) {
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Address address = new Address("localhost:11192");
        DistributedObject d = new DistributedObject(address);

        registMessages(tc);
        assignHandlers(t, tc, address, d);

        Bank bank = new BankImpl();
        d.exportObject(Bank.class, (Exportable) bank);

        System.out.println("Server ready on " + address.toString() + ".");
    }

    private static void assignHandlers(Transport t, ThreadContext tc, Address address, DistributedObject d) {
        tc.execute(()-> {
            t.server().listen(address, (c)-> {
                c.handler(BankMakeAccountReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Bank bank = (Bank) d.get(m.getBankId());
                    int ib = m.getInitialBalance();

                    Account acc = bank.newAccount(ib);
                    Reference<Account> ref = d.exportObject(Account.class, (Exportable) acc);

                    if (m.getContext() != null)
                        log.append(acc);

                    return Futures.completedFuture(new BankMakeAccountRep(ref));
                });
                c.handler(AccountTransferReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Account from = (Account) d.get(m.getAccountId());
                    Account to = d.get(m.getTo());
                    int amount = m.getAmount();

                    boolean success = from.transfer(to, amount);

                    if (m.getContext() != null)
                        log.append(from);

                    return Futures.completedFuture(new AccountTransferRep(success));
                });
                c.handler(AccountGetTransactionsReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Account acc = (Account) d.get(m.getAccountId());
                    List<Integer> transactions = acc.getTransactions();

                    return Futures.completedFuture(new AccountGetTransactionsRep(transactions));
                });
                c.handler(AccountDebitReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.lock();

                    Account acc = (Account) d.get(m.getAccountId());
                    int amount = m.getAmount();

                    acc.debit(amount);

                    if (m.getContext() != null)
                        log.append(acc);

                    return Futures.completedFuture(new AccountDebitRep());
                });
                c.handler(AccountCreditReq.class, m -> {
                    Manager.context.set(m.getContext());
                    if (m.getContext() != null)
                        lock.unlock();

                    Account acc = (Account) d.get(m.getAccountId());
                    int amount = m.getAmount();

                    acc.credit(amount);

                    if (m.getContext() != null)
                        log.append(acc);

                    return Futures.completedFuture(new AccountCreditRep());
                });
            });
        });
    }

    private static void registMessages(ThreadContext tc) {
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
