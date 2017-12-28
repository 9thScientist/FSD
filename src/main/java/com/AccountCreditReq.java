package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class AccountCreditReq implements CatalystSerializable {
    private int accountId;
    private int amount;
    private Context context;

    private AccountCreditReq() {
    }

    public AccountCreditReq(int accountId, int amount, Context context) {
        this.accountId = accountId;
        this.amount = amount;
        this.context = context;
    }

    public int getAmount() {
        return amount;
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
        bufferOutput.writeInt(amount);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        accountId = bufferInput.readInt();
        amount = bufferInput.readInt();
        context = serializer.readObject(bufferInput);
    }
}
