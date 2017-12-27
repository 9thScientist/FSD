package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class StoreMakeCartReq implements CatalystSerializable {
    private int storeId;

    public int getStoreId() {
        return storeId;
    }

    public StoreMakeCartReq() {
    }

    public StoreMakeCartReq(int storeId) {
        this.storeId = storeId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(storeId);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        storeId = bufferInput.readInt();
    }
}
