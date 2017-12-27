package com;

import interfaces.Sale;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

import java.util.List;

public class StoreGetHistoryRep implements CatalystSerializable {
    private List<Reference<Sale>> sales;

    private StoreGetHistoryRep() {
    }

    public StoreGetHistoryRep(List<Reference<Sale>> sales) {
        this.sales = sales;
    }

    public List<Reference<Sale>> getSales() {
        return sales;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(sales, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        sales = serializer.readObject(bufferInput);
    }
}
