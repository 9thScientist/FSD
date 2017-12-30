package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public abstract class Request implements CatalystSerializable {
    private Context context;

    public Request() {

    }

    public Request(Context context) {
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
