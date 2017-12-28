package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

public class Context implements CatalystSerializable {
    private Address address;
    private int contenxtId;

    private Context() {
    }

    public Context(Address address, int contenxtId) {
        this.address = address;
        this.contenxtId = contenxtId;
    }

    public Address getAddress() {
        return address;
    }

    public int getContenxtId() {
        return contenxtId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(contenxtId);
        serializer.writeObject(address, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        contenxtId = bufferInput.readInt();
        address = serializer.readObject(bufferInput);
    }
}
