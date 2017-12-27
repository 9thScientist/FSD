package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class SaleIsPaidReq implements CatalystSerializable {
    private int saleId;

    public SaleIsPaidReq() {
    }

    public SaleIsPaidReq(int saleId) {
        this.saleId = saleId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(saleId);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        saleId = bufferInput.readInt();
    }

    public int getSaleId() {
        return saleId;
    }
}
