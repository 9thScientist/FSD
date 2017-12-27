package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

public class AccountTransferReq implements CatalystSerializable {
    private int from;
    private Reference<Account> to;
    private int amount;

    private AccountTransferReq() {

    }

    public AccountTransferReq(int from, Reference<Account> to, int amount) {
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
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        amount = bufferInput.readInt();
        to = serializer.readObject(bufferInput);
        from = bufferInput.readInt();
    }
}
