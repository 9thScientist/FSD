package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class SaleIsPaidRep implements CatalystSerializable {
    private boolean paid;

    private SaleIsPaidRep() {
    }

    public SaleIsPaidRep(boolean paid) {
        this.paid = paid;
    }

    public boolean isPaid() {
        return paid;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeBoolean(paid);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        paid = bufferInput.readBoolean();
    }
}
