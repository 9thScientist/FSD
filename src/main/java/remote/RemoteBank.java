package remote;

import com.BankMakeAccountRep;
import com.BankMakeAccountReq;
import interfaces.Account;
import interfaces.Bank;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.Context;
import rmi.DistributedObject;
import rmi.Manager;
import rmi.Reference;

import java.sql.Ref;

public class RemoteBank extends Remote implements Bank {
    public RemoteBank(ThreadContext tc, Connection c, Integer id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public Account newAccount(int balance) {
        try {
            Context ctx = Manager.context.get();

            if (ctx != null)
                Manager.add(ctx, getReference());

            BankMakeAccountRep r = (BankMakeAccountRep) tc.execute(() ->
                    c.sendAndReceive(new BankMakeAccountReq(id, balance, ctx))
            ).join().get();

            return DistributedObject.importObject(r.getAccount());
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void registerMessages() {
        tc.serializer().register(BankMakeAccountReq.class);
        tc.serializer().register(BankMakeAccountRep.class);
    }
}
