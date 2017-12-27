package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Reference;

public class CartBuyReq implements CatalystSerializable {
    private Reference<Account> clientAccount;
    private int cartId;

    public CartBuyReq() {
    }

    public CartBuyReq(Reference<Account> clientAccount, int cartId) {
        this.clientAccount = clientAccount;
        this.cartId = cartId;
    }

    public Reference<Account> getClientAccount() {
        return clientAccount;
    }

    public int getCartId() {
        return cartId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(clientAccount, bufferOutput);
        bufferOutput.writeInt(cartId);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        clientAccount = serializer.readObject(bufferInput);
        cartId = bufferInput.readInt();
    }
}
