package remote;

import com.*;
import interfaces.Account;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RemoteAccount extends Remote implements Account {

    public RemoteAccount(ThreadContext tc, Connection c, Integer id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public void credit(int amount) {
        try {
            AccountCreditRep r = (AccountCreditRep) tc.execute(() ->
                c.sendAndReceive(new AccountCreditReq(amount, id))
            ).join().get();

            return;
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void debit(int amount) {
        try {
            AccountDebitRep r = (AccountDebitRep) tc.execute(() ->
                    c.sendAndReceive(new AccountDebitReq(amount, id))
            ).join().get();

            return;
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public List<Integer> getTransactions() {
        try {
            AccountGetTransactionsRep r = (AccountGetTransactionsRep) tc.execute(() ->
                    c.sendAndReceive(new AccountGetTransactionsReq(id))
            ).join().get();

            return r.getTransactions();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CompletableFuture<Void> transfer(Account to, int amount) {
        CompletableFuture<Void> r = new CompletableFuture<>();
        Reference ref = ((RemoteAccount) to).getReference();

        tc.execute(() -> {
            c.sendAndReceive(new AccountTransferReq(id, ref, amount))
            .thenRun(() -> r.complete(null))
            .whenComplete((x,y) -> {
                System.out.println("RES: " + x);
                System.out.println("EXC: " + y);
            });
        });


        return r;
    }

    @Override
    public void registerMessages() {
        tc.serializer().register(Reference.class);

        tc.serializer().register(AccountCreditReq.class);
        tc.serializer().register(AccountCreditRep.class);
        tc.serializer().register(AccountDebitReq.class);
        tc.serializer().register(AccountDebitRep.class);
        tc.serializer().register(AccountTransferReq.class);
        tc.serializer().register(AccountTransferRep.class);
    }
}
