package remote;

import com.*;
import interfaces.Account;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.Context;
import rmi.Manager;
import rmi.Reference;

import java.util.List;

public class RemoteAccount extends Remote implements Account {

    public RemoteAccount(ThreadContext tc, Connection c, Integer id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public void credit(int amount) {
        try {
            Context ctx = Manager.getContext();

            AccountCreditRep r = (AccountCreditRep) tc.execute(() ->
                c.sendAndReceive(new AccountCreditReq(id, amount, ctx))
            ).join().get();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return;
    }

    @Override
    public void debit(int amount) {
        try {
            Context ctx = Manager.getContext();

            AccountDebitRep r = (AccountDebitRep) tc.execute(() ->
                    c.sendAndReceive(new AccountDebitReq(id, amount, ctx))
            ).join().get();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return;
    }

    @Override
    public List<Integer> getTransactions() {
        try {
            Context ctx = Manager.getContext();

            AccountGetTransactionsRep r = (AccountGetTransactionsRep) tc.execute(() ->
                    c.sendAndReceive(new AccountGetTransactionsReq(id, ctx))
            ).join().get();

            return r.getTransactions();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean transfer(Account to, int amount) {
        try {
            Reference toRef = ((RemoteAccount) to).getReference();
            Context ctx = Manager.getContext();

            AccountTransferRep r = (AccountTransferRep) tc.execute(() ->
                    c.sendAndReceive(new AccountTransferReq(id, toRef, amount, ctx))
            ).join().get();

            return r.isSuccess();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void registerMessages() {
        tc.serializer().register(AccountCreditReq.class);
        tc.serializer().register(AccountCreditRep.class);
        tc.serializer().register(AccountDebitReq.class);
        tc.serializer().register(AccountDebitRep.class);
        tc.serializer().register(AccountTransferReq.class);
        tc.serializer().register(AccountTransferRep.class);
    }
}
