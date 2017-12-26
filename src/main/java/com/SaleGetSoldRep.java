package com;

import business.Book;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.List;

public class SaleGetSoldRep implements CatalystSerializable {
    List<Book> soldBooks;

    public SaleGetSoldRep() {
    }

    public SaleGetSoldRep(List<Book> soldBooks) {
        this.soldBooks = soldBooks;
    }

    public List<Book> getSoldBooks() {
        return soldBooks;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        serializer.writeObject(soldBooks, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        soldBooks = serializer.readObject(bufferInput);
    }
}
