package com.transactions;

import com.Request;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class ManagerPreparedReq extends Request implements CatalystSerializable{

    private int resourceId;

    public ManagerPreparedReq() {}

    public ManagerPreparedReq(Context context, int resourceId) {
        super(context);
        this.resourceId = resourceId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        super.readObject(bufferInput, serializer);
    }

    public int getResourceId() {
        return resourceId;
    }
}
