package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

public class AccountGetTransactionsReq implements CatalystSerializable {
    private Reference<Account> account;

    public AccountGetTransactionsReq() {
    }

    public AccountGetTransactionsReq(Reference<Account> account) {
        this.account = account;
    }

    public Reference<Account> getAccount() {
        return account;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(account, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        account = serializer.readObject(bufferInput);
    }
}
