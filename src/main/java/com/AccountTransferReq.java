package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

public class AccountTransferReq implements CatalystSerializable {
    private Reference<Account> from;
    private Reference<Account> to;
    private int amount;

    public AccountTransferReq() {

    }

    public AccountTransferReq(Reference<Account> from, Reference<Account> to, int amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public Reference<Account> getTo() {
        return to;
    }

    public Reference<Account> getFrom() {
        return from;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(amount);
        serializer.writeObject(to, bufferOutput);
        serializer.writeObject(from, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        amount = bufferInput.readInt();
        to = serializer.readObject(bufferInput);
        from = serializer.readObject(bufferInput);
    }
}
