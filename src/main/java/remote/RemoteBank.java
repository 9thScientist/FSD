package remote;

import com.BankMakeAccountRep;
import com.BankMakeAccountReq;
import interfaces.Account;
import interfaces.Bank;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.DistributedObject;
import rmi.Reference;

public class RemoteBank extends Remote implements Bank {
    public RemoteBank(ThreadContext tc, Connection c, int id, Reference reference) {
        super(tc, c, id, reference);
    }

    @Override
    public Account newAccount(int balance) {
        try {
            BankMakeAccountRep r = (BankMakeAccountRep) tc.execute(() ->
                    c.sendAndReceive(new BankMakeAccountReq())
            ).join().get();

            return DistributedObject.importObject(r.getAccount());
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public void registerMessages() {
        tc.serializer().register(BankMakeAccountReq.class);
        tc.serializer().register(BankMakeAccountRep.class);
    }
}
