package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class SaleIsPaidReq extends Request implements CatalystSerializable {
    private int saleId;

    private SaleIsPaidReq() {
    }

    public int getSaleId() {
        return saleId;
    }

    public SaleIsPaidReq(int saleId, Context context) {
        super(context);
        this.saleId = saleId;
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
