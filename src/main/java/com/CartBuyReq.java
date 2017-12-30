package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;
import rmi.Reference;

public class CartBuyReq extends Request implements CatalystSerializable {
    private Reference<Account> clientAccount;
    private int cartId;

    private CartBuyReq() {
    }

    public CartBuyReq(Reference<Account> clientAccount, int cartId, Context context) {
        super(context);
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
        bufferOutput.writeInt(cartId);
        serializer.writeObject(clientAccount, bufferOutput);
        super.writeObject(bufferOutput, serializer);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartId = bufferInput.readInt();
        clientAccount = serializer.readObject(bufferInput);
        super.readObject(bufferInput, serializer);
    }
}
