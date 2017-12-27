package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class BankMakeAccountReq implements CatalystSerializable {
    private int bankId;
    private int initialBalance;

    private BankMakeAccountReq() {
    }

    public BankMakeAccountReq(int bankId, int initialBalance) {
        this.bankId = bankId;
        this.initialBalance = initialBalance;
    }

    public int getInitialBalance() {
        return initialBalance;
    }

    public int getBankId() {
        return bankId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(bankId);
        bufferOutput.writeInt(initialBalance);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        bankId = bufferInput.readInt();
        initialBalance = bufferInput.readInt();
    }
}
