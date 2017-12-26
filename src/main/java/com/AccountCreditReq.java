package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountCreditReq implements CatalystSerializable {
    private int amount;

    public AccountCreditReq() {
    }

    public AccountCreditReq(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(amount);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        amount = bufferInput.readInt();
    }
}
