package com.transactions;

import com.Request;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import rmi.Context;

public class ManagerAddResourceReq extends Request implements CatalystSerializable {
    private Address address;

    private ManagerAddResourceReq() {
    }

    public ManagerAddResourceReq(Context context, Address address) {
        super(context);
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(address, bufferOutput);
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        address = serializer.readObject(bufferInput);
        super.readObject(bufferInput, serializer);
    }
}
