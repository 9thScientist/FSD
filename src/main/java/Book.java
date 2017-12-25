public class Book {
    private int isbn, price;
    private String author, title;

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
}
