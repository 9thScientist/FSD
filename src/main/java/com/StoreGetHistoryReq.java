package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class StoreGetHistoryReq extends Request implements CatalystSerializable {
    private int storeId;

    private StoreGetHistoryReq() {
    }

    public StoreGetHistoryReq(int storeId, Context context) {
        super(context);
        this.storeId = storeId;
    }

    public int getStoreId() {
        return storeId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(storeId);
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        storeId = bufferInput.readInt();
        super.readObject(bufferInput, serializer);
    }
}
