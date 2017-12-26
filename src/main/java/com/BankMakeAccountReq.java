package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankMakeAccountReq implements CatalystSerializable {
    private int initialBalance;

    public BankMakeAccountReq() {
    }

    public BankMakeAccountReq(int initialBalance) {
        this.initialBalance = initialBalance;
    }

    public int getInitialBalance() {
        return initialBalance;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(initialBalance);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        initialBalance = bufferInput.readInt();
    }
}
