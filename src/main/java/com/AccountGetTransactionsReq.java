package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class AccountGetTransactionsReq implements CatalystSerializable {
    private int accountId;
    private Context context;

    private AccountGetTransactionsReq() {
    }

    public AccountGetTransactionsReq(int accountId, Context context) {
        this.accountId = accountId;
        this.context = context;
    }

    public int getAccountId() {
        return accountId;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(accountId);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        accountId = bufferInput.readInt();
        context = serializer.readObject(bufferInput);
    }
}
