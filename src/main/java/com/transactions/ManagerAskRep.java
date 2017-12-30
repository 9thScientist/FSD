package com.transactions;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class ManagerAskRep implements CatalystSerializable {
    private boolean commit;
    private Context context;

    public ManagerAskRep() {
    }

    public ManagerAskRep(boolean commit, Context context) {
        this.commit = commit;
        this.context = context;
    }

    public boolean isCommit() {
        return commit;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeBoolean(commit);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        commit = bufferInput.readBoolean();
        context = serializer.readObject(bufferInput);
    }
}
