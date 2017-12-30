package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class AccountGetTransactionsReq extends Request implements CatalystSerializable {
    private int accountId;

    private AccountGetTransactionsReq() {
    }

    public AccountGetTransactionsReq(int accountId, Context context) {
        super(context);
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(accountId);
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        accountId = bufferInput.readInt();
        super.readObject(bufferInput, serializer);
    }
}
