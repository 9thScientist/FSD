package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class CartBuyReq implements CatalystSerializable {
    private Reference<Account> clientAccount;
    private int cartId;
    private Context context;

    private CartBuyReq() {
    }

    public CartBuyReq(Reference<Account> clientAccount, int cartId, Context context) {
        this.clientAccount = clientAccount;
        this.cartId = cartId;
        this.context = context;
    }

    public Reference<Account> getClientAccount() {
        return clientAccount;
    }

    public int getCartId() {
        return cartId;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(cartId);
        serializer.writeObject(clientAccount, bufferOutput);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartId = bufferInput.readInt();
        clientAccount = serializer.readObject(bufferInput);
        context = serializer.readObject(bufferInput);
    }
}
