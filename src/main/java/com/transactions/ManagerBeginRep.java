package com.transactions;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class ManagerBeginRep implements CatalystSerializable{
    private Context context;

    public ManagerBeginRep() {}

    public ManagerBeginRep(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        context = serializer.readObject(bufferInput);
    }
}
