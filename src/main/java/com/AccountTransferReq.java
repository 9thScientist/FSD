package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class AccountTransferReq implements CatalystSerializable {
    private int from;
    private Reference<Account> to;
    private int amount;
    private Context context;

    private AccountTransferReq() {

    }

    public AccountTransferReq(int from, Reference<Account> to, int amount, Context context) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.context = context;
    }

    public Reference<Account> getTo() {
        return to;
    }

    public int getAccountId() {
        return from;
    }

    public int getAmount() {
        return amount;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(amount);
        serializer.writeObject(to, bufferOutput);
        bufferOutput.writeInt(from);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        amount = bufferInput.readInt();
        to = serializer.readObject(bufferInput);
        from = bufferInput.readInt();
        context = serializer.readObject(bufferInput);
    }
}
