package com;

import business.StoreImpl;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.List;

public class StoreGetHistoryRep implements CatalystSerializable {
    private List<StoreImpl.Sale> sales;

    public StoreGetHistoryRep() {
    }

    public StoreGetHistoryRep(List<StoreImpl.Sale> sales) {
        this.sales = sales;
    }

    public List<StoreImpl.Sale> getSales() {
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
