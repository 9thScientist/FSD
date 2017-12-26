package business;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

public class Book implements CatalystSerializable {
    private int isbn, price;
    private String author, title;

    public Book() {

    }

    public Book(int isbn, int price, String author, String title) {
        this.isbn = isbn;
        this.price = price;
        this.author = author;
        this.title = title;
    }

    public int getIsbn() {
        return isbn;
    }

    public int getPrice() {
        return price;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(isbn);
        bufferOutput.writeInt(price);
        bufferOutput.writeString(author);
        bufferOutput.writeString(title);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        isbn = bufferInput.readInt();
        price = bufferInput.readInt();
        author = bufferInput.readString();
        title = bufferInput.readString();
    }
}
