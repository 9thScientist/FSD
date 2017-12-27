package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class AccountDebitReq implements CatalystSerializable {
    private int accountId;
    private int amount;

    private AccountDebitReq() {
    }

    public AccountDebitReq(int amount, int account) {
        this.amount = amount;
        this.accountId = account;
    }

    public int getAmount() {
        return amount;
    }

    public int getAccountId() {
        return accountId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(accountId);
        bufferOutput.writeInt(amount);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        accountId = bufferInput.readInt();
        amount = bufferInput.readInt();
    }
}
