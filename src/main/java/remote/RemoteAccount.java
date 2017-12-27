package remote;

import com.*;
import interfaces.Account;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.Reference;

import java.util.List;

public class RemoteAccount extends Remote implements Account {

    public RemoteAccount(ThreadContext tc, Connection c, int id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public void credit(int amount) {
        try {
            AccountCreditRep r = (AccountCreditRep) tc.execute(() ->
                c.sendAndReceive(new AccountCreditReq(amount, account))
            ).join().get();

            return;
        } catch(Exception e) {
            return;
        }
    }

    @Override
    public void debit(int amount) {
        try {
            AccountDebitRep r = (AccountDebitRep) tc.execute(() ->
                    c.sendAndReceive(new AccountDebitReq(amount, account))
            ).join().get();

            return;
        } catch(Exception e) {
            return;
        }
    }

    @Override
    public List<Integer> getTransactions() {
        try {
            AccountGetTransactionsRep r = (AccountGetTransactionsRep) tc.execute(() ->
                    c.sendAndReceive(new AccountCreditReq())
            ).join().get();

            return r.getTransactions();
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public boolean transfer(Account to, int amount) {
        try {
            Reference ref = ((RemoteAccount) to).getReference();
            AccountTransferRep r = (AccountTransferRep) tc.execute(() ->
                    c.sendAndReceive(new AccountTransferReq(from, ref, amount))
            ).join().get();

            return r.isSuccess();
        } catch(Exception e) {
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
