package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class SaleGetSoldReq implements CatalystSerializable {
    private int saleId;
    private Context context;

    private SaleGetSoldReq() {
    }

    public SaleGetSoldReq(int saleId, Context context) {
        this.saleId = saleId;
        this.context = context;
    }

    public int getSaleId() {
        return saleId;
    }

    public Context getContext() {
        return context;
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
