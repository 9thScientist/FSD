package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

public class AccountCreditReq implements CatalystSerializable {
    private int accountId;
    private int amount;

    private AccountCreditReq() {
    }

    public AccountCreditReq(int amount, int accountId) {
        this.amount = amount;
        this.accountId = accountId;
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
