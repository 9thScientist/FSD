package com.transactions;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class ManagerAddResourceReq implements CatalystSerializable {
    private Context context;
    private Reference reference;

    private ManagerAddResourceReq() {
    }

    public ManagerAddResourceReq(Context context, Reference reference) {
        this.context = context;
        this.reference = reference;
    }

    public Context getContext() {
        return context;
    }

    public Reference getReference() {
        return reference;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(context, bufferOutput);
        serializer.writeObject(reference, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        context = serializer.readObject(bufferInput);
        reference = serializer.readObject(bufferInput);
    }
}
