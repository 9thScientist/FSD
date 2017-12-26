package com;

import business.StoreImpl;
import interfaces.Cart;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import remote.Remote;
import remote.RemoteCart;
import rmi.Reference;

public class StoreMakeCartRep implements CatalystSerializable {
    private Reference<RemoteCart> cart;

    public StoreMakeCartRep() {
    }

    public StoreMakeCartRep(Reference<RemoteCart> cart) {
        this.cart = cart;
    }

    public Reference<RemoteCart> getCart() {
        return cart;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(cart, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cart = serializer.readObject(bufferInput);
    }
}
