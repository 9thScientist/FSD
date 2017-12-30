package com.transactions;

import com.Request;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class ManagerAddResourceReq extends Request implements CatalystSerializable {
    private Reference reference;

    private ManagerAddResourceReq() {
    }

    public ManagerAddResourceReq(Context context, Reference reference) {
        super(context);
        this.reference = reference;
    }

    public Reference getReference() {
        return reference;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(reference, bufferOutput);
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        reference = serializer.readObject(bufferInput);
        super.readObject(bufferInput, serializer);
    }
}
