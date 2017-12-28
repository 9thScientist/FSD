package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class BankMakeAccountReq implements CatalystSerializable {
    private int bankId;
    private int initialBalance;
    private Context context;

    private BankMakeAccountReq() {
    }

    public BankMakeAccountReq(int bankId, int initialBalance, Context context) {
        this.bankId = bankId;
        this.initialBalance = initialBalance;
        this.context = context;
    }

    public int getInitialBalance() {
        return initialBalance;
    }

    public int getBankId() {
        return bankId;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(bankId);
        bufferOutput.writeInt(initialBalance);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        bankId = bufferInput.readInt();
        initialBalance = bufferInput.readInt();
        context = serializer.readObject(bufferInput);
    }
}
