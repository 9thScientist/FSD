package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class SaleIsPaidReq implements CatalystSerializable {
    private int saleId;
    private Context context;

    private SaleIsPaidReq() {
    }

    public int getSaleId() {
        return saleId;
    }

    public Context getContext() {
        return context;
    }

    public SaleIsPaidReq(int saleId, Context context) {
        this.saleId = saleId;
        this.context = context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(saleId);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        saleId = bufferInput.readInt();
        context = serializer.readObject(bufferInput);
    }
}
