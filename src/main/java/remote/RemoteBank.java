package remote;

import com.BankMakeAccountRep;
import com.BankMakeAccountReq;
import interfaces.Account;
import interfaces.Bank;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.DistributedObject;
import rmi.Reference;

import java.sql.Ref;

public class RemoteBank extends Remote implements Bank {
    public RemoteBank(ThreadContext tc, Connection c, Integer id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public Account newAccount(int balance) {
        try {
            BankMakeAccountRep r = (BankMakeAccountRep) tc.execute(() ->
                    c.sendAndReceive(new BankMakeAccountReq(id, balance))
            ).join().get();

            return DistributedObject.importObject(r.getAccount());
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void registerMessages() {
        tc.serializer().register(Reference.class);
        tc.serializer().register(BankMakeAccountReq.class);
        tc.serializer().register(BankMakeAccountRep.class);
    }
}
