package rmi;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

public class Reference<T> implements CatalystSerializable {
    private Address address;
    private int id;
    private Class<T> type;

    public Reference() {
    }

    public Reference(Address address, int id, Class<T> type) {
        this.address = address;
        this.id = id;
        this.type = type;
    }

    public Address getAddress() {
        return address;
    }

    public int getId() {
        return id;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(id);
        serializer.writeObject(address, bufferOutput);
        bufferOutput.writeString(type.getName());
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        id = bufferInput.readInt();
        address = serializer.readObject(bufferInput);

        try {
            type = (Class<T>) Class.forName(bufferInput.readString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
