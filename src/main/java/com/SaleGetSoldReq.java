package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class SaleGetSoldReq extends Request implements CatalystSerializable {
    private int saleId;

    private SaleGetSoldReq() {
    }

    public SaleGetSoldReq(int saleId, Context context) {
        super(context);
        this.saleId = saleId;
    }

    public int getSaleId() {
        return saleId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(saleId);
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        saleId = bufferInput.readInt();
        super.readObject(bufferInput, serializer);
    }
}
