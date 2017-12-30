package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class AccountCreditReq extends Request implements CatalystSerializable {
    private int accountId;
    private int amount;

    private AccountCreditReq() {
    }

    public AccountCreditReq(int accountId, int amount, Context context) {
        super(context);
        this.accountId = accountId;
        this.amount = amount;
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
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        accountId = bufferInput.readInt();
        amount = bufferInput.readInt();
        super.readObject(bufferInput, serializer);
    }
}
