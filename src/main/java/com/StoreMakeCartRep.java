package com;

import interfaces.Cart;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

public class StoreMakeCartRep implements CatalystSerializable {
    private Reference<Cart> cart;

    private StoreMakeCartRep() {
    }

    public StoreMakeCartRep(Reference<Cart> cart) {
        this.cart = cart;
    }

    public Reference<Cart> getCart() {
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
