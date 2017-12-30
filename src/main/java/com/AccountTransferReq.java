package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class AccountTransferReq extends Request implements CatalystSerializable {
    private int from;
    private Reference<Account> to;
    private int amount;

    private AccountTransferReq() {
    }

    public AccountTransferReq(int from, Reference<Account> to, int amount, Context context) {
        super(context);
        this.from = from;
        this.to = to;
        this.amount = amount;
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

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(amount);
        serializer.writeObject(to, bufferOutput);
        bufferOutput.writeInt(from);
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        amount = bufferInput.readInt();
        to = serializer.readObject(bufferInput);
        from = bufferInput.readInt();
        super.readObject(bufferInput, serializer);
    }
}
