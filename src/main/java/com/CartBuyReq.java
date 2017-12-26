package com;

import interfaces.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import remote.RemoteAccount;
import rmi.Reference;

import java.sql.Ref;

public class CartBuyReq implements CatalystSerializable {
    private Reference<RemoteAccount> clientAccount;

    public CartBuyReq() {
    }

    public CartBuyReq(Reference<RemoteAccount> clientAccount) {
        this.clientAccount = clientAccount;
    }

    public Reference<RemoteAccount> getClientAccount() {
        return clientAccount;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(clientAccount, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        clientAccount = serializer.readObject(bufferInput);
    }
}
