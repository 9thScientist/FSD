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
import rmi.DistributedObject;
import rmi.Reference;

import java.util.List;

public class BankServer {
    public static void main(String[] args) {
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Address address = new Address(":11192");
        DistributedObject d = new DistributedObject(address);

        registMessages(tc);
        assignHandlers(t, tc, address, d);

        Bank bank = new BankImpl();
        d.exportObject(Bank.class, bank);

        System.out.println("Server ready on " + address.toString() + ".");
    }

    private static void assignHandlers(Transport t, ThreadContext tc, Address address, DistributedObject d) {
        tc.execute(()-> {
            t.server().listen(address, (c)-> {
                c.handler(BankMakeAccountReq.class, m -> {
                    Bank bank = (Bank) d.get(m.getBankId());
                    int ib = m.getInitialBalance();

                    Account acc = bank.newAccount(ib);
                    Reference ref = d.exportObject(Account.class, acc);

                    return Futures.completedFuture(new BankMakeAccountRep(ref));
                });
                c.handler(AccountTransferReq.class, m -> {
                    Account from = d.get(m.getFrom());
                    Account to = d.get(m.getTo());
                    int amount = m.getAmount();

                    boolean success = from.transfer(to, amount);

                    return Futures.completedFuture(new AccountTransferRep(success));
                });
                c.handler(AccountGetTransactionsReq.class, m -> {
                    Account acc = d.get(m.getAccount());

                    List<Integer> transactions = acc.getTransactions();

                    return Futures.completedFuture(new AccountGetTransactionsRep(transactions));
                });
                c.handler(AccountDebitReq.class, m -> {
                    Account acc = d.get(m.getAccount());
                    int amount = m.getAmount();

                    acc.debit(amount);

                    return Futures.completedFuture(new AccountDebitRep());
                });
                c.handler(AccountCreditReq.class, m -> {
                    Account acc = d.get(m.getAccount());
                    int amount = m.getAmount();

                    acc.credit(amount);

                    return Futures.completedFuture(new AccountCreditRep());
                });
            });
        });
    }

    private static void registMessages(ThreadContext tc) {
        tc.serializer().register(Reference.class);

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
