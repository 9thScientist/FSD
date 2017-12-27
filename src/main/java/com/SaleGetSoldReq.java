package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class SaleGetSoldReq implements CatalystSerializable {
    private int saleId;

    public SaleGetSoldReq() {
    }

    public SaleGetSoldReq(int saleId) {
        this.saleId = saleId;
    }

    public int getSaleId() {
        return saleId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(saleId);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        saleId = bufferInput.readInt();
    }
}
