package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

public class Context implements CatalystSerializable {
    private Address address;
    private int contextId;

    private Context() {
    }

    public Context(Address address, int contextId) {
        this.address = address;
        this.contextId = contextId;
    }

    public Address getAddress() {
        return address;
    }

    public int getContextId() {
        return contextId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(contextId);
        serializer.writeObject(address, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        contextId = bufferInput.readInt();
        address = serializer.readObject(bufferInput);
    }
}
