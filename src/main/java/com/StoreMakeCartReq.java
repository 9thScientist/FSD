package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class StoreMakeCartReq implements CatalystSerializable {
    private int storeId;
    private Context context;

    private StoreMakeCartReq() {
    }

    public StoreMakeCartReq(int storeId, Context context) {
        this.storeId = storeId;
        this.context = context;
    }

    public int getStoreId() {
        return storeId;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(storeId);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        storeId = bufferInput.readInt();
        context = serializer.readObject(bufferInput);
    }
}
