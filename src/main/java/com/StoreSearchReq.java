package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class StoreSearchReq extends Request implements CatalystSerializable {
    private String title;
    private int storeId;

    private StoreSearchReq() {
    }

    public StoreSearchReq(String title, int storeId, Context context) {
        super(context);
        this.title = title;
        this.storeId = storeId;
    }

    public String getTitle() {
        return title;
    }

    public int getStoreId() {
        return storeId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(title);
        bufferOutput.writeInt(storeId);
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        title = bufferInput.readString();
        storeId = bufferInput.readInt();
        super.readObject(bufferInput, serializer);
    }
}
