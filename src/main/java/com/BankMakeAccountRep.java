package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import remote.RemoteAccount;
import rmi.Reference;

import java.sql.Ref;

public class BankMakeAccountRep implements CatalystSerializable {
    private Reference<RemoteAccount> account;

    public BankMakeAccountRep() {
    }

    public BankMakeAccountRep(Reference<RemoteAccount> account) {
        this.account = account;
    }

    public Reference<RemoteAccount> getAccount() {
        return account;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(account);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        account = serializer.readObject(bufferInput);
    }
}
