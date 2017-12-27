package com;

import interfaces.Sale;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

public class CartBuyRep implements CatalystSerializable {
    private Reference<Sale> sale;

    private CartBuyRep() {
    }

    public CartBuyRep(Reference<Sale> sale) {
        this.sale = sale;
    }

    public Reference<Sale> getSale() {
        return sale;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(sale, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        sale = serializer.readObject(bufferInput);
    }
}
