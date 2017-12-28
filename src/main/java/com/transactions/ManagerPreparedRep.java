package com.transactions;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class ManagerPreparedRep implements CatalystSerializable {
    private boolean ok;

    public ManagerPreparedRep() {

    }

    public ManagerPreparedRep(boolean ok) {
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeBoolean(ok);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        ok = bufferInput.readBoolean();
    }
}
