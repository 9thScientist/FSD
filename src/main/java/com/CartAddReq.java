package com;

import business.Book;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class CartAddReq implements CatalystSerializable {
    private int cartId;
    private Book book;

    private CartAddReq() {
    }

    public CartAddReq(int id, Book book) {
        this.cartId = id;
        this.book = book;
    }

    public Book getBook() {
        return book;
    }

    public int getCartId() {
        return cartId;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(cartId);
        serializer.writeObject(book, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cartId = bufferInput.readInt();
        book = serializer.readObject(bufferInput);
    }
}
