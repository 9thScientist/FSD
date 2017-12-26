package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.List;

public class AccountGetTransactionsRep implements CatalystSerializable {
    private List<Integer> transactions;

    public AccountGetTransactionsRep() {
    }

    public AccountGetTransactionsRep(List<Integer> transactions) {
        this.transactions = transactions;
    }

    public List<Integer> getTransactions() {
        return transactions;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(transactions, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transactions = serializer.readObject(bufferInput);
    }
}
