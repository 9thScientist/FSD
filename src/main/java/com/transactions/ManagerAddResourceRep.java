package com.transactions;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class ManagerAddResourceRep implements CatalystSerializable {
    private int resourceId;

    private ManagerAddResourceRep() {

    }

    public ManagerAddResourceRep(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return resourceId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(resourceId);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        resourceId = bufferInput.readInt();
    }
}
