package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class BankMakeAccountReq extends Request implements CatalystSerializable {
    private int bankId;
    private int initialBalance;

    private BankMakeAccountReq() {
    }

    public BankMakeAccountReq(int bankId, int initialBalance, Context context) {
        super(context);
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
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        bankId = bufferInput.readInt();
        initialBalance = bufferInput.readInt();
        super.readObject(bufferInput, serializer);
    }
}
