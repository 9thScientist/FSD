package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

public class AccountCreditReq implements CatalystSerializable {
    private int amount;
    private Reference<Account> account;

    public AccountCreditReq() {
    }

    public AccountCreditReq(int amount, Reference<Account> account) {
        this.amount = amount;
        this.account = account;
    }

    public int getAmount() {
        return amount;
    }

    public Reference<Account> getAccount() {
        return account;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(amount);
        serializer.writeObject(account, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        amount = bufferInput.readInt();
        account = serializer.readObject(bufferInput);
    }
}
