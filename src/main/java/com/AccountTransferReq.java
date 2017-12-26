package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import remote.Remote;
import remote.RemoteAccount;
import rmi.Reference;

import java.sql.Ref;

public class AccountTransferReq implements CatalystSerializable {
    private Reference<RemoteAccount> to;
    private int amount;

    public AccountTransferReq() {

    }

    public AccountTransferReq(Reference<RemoteAccount> to, int amount) {
        this.to = to;
        this.amount = amount;
    }

    public Reference<RemoteAccount> getTo() {
        return to;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(amount);
        serializer.writeObject(to, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        amount = bufferInput.readInt();
        to = serializer.readObject(bufferInput);
    }
}
