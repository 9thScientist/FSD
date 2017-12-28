package com;

import business.Book;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Context;

public class CartAddReq implements CatalystSerializable {
    private int cartId;
    private Book book;
    private Context context;

    private CartAddReq() {
    }

    public CartAddReq(int cartId, Book book, Context context) {
        this.cartId = cartId;
        this.book = book;
        this.context = context;
    }

    public Book getBook() {
        return book;
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
        serializer.writeObject(book, bufferOutput);
        serializer.writeObject(context, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartId = bufferInput.readInt();
        book = serializer.readObject(bufferInput);
        context = serializer.readObject(bufferInput);
    }
}
