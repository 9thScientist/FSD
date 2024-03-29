package com;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import business.Book;
import io.atomix.catalyst.serializer.Serializer;

public class StoreSearchRep implements CatalystSerializable {
    private Book book;

    private StoreSearchRep() {
    }

    public StoreSearchRep(Book book) {
        this.book = book;
    }

    public Book getBook() {
        return book;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(book, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        book = serializer.readObject(bufferInput);
    }
}
