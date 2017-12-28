package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class StoreSearchReq implements CatalystSerializable {
    private String title;
    private int storeId;
    private Context context;

    private StoreSearchReq() {
    }

    public StoreSearchReq(String title, int storeId, Context context) {
        this.title = title;
        this.storeId = storeId;
        this.context = context;
    }

    public String getTitle() {
        return title;
    }

    public int getStoreId() {
        return storeId;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(title);
        bufferOutput.writeInt(storeId);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        title = bufferInput.readString();
        storeId = bufferInput.readInt();
        context = serializer.readObject(bufferInput);
    }
}
