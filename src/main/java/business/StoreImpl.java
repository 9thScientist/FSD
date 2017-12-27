package business;

import interfaces.*;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import rmi.Exportable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreImpl extends Exportable implements Store {
    private Map<Integer, Book> collection = new HashMap<>();
    private ArrayList<Sale> history = new ArrayList<>();
    private Account storeAccount;

    public StoreImpl(Bank b) {
        storeAccount = b.newAccount(1000);

        collection.put(1, new Book(1, 5, "Lev Tolstoy", "The Death of Ivan Ilyich"));
        collection.put(2, new Book(2, 10, "Haruki Murakami", "Kafka on the Shore"));
        collection.put(3, new Book(3, 15, "John Steinbeck", "East of Eden"));
        collection.put(4, new Book(4, 20, "Daniel Keys", "Flowers for Algernon"));
        collection.put(5, new Book(5, 25, "Markus Zusak", "The buisness.Book Thief"));
    }

    public Book search(String title) {
        for(Book b: collection.values())
            if (b.getTitle().equals(title))
                return b;

        return null;
    }

    public List<Sale> getHistory() {
        return (List<Sale>) history.clone();
    }

    public Cart newCart() {
        return new CartImpl();
    }

    public class CartImpl extends Exportable implements Cart {
        private List<Book> wishes = new ArrayList<>();

        public void add(Book b) {
            wishes.add(b);
        }

        private int value() {
            return wishes
                .stream()
                .map(Book::getPrice)
                .reduce(0, (acc, price) -> acc + price);
        }

        public Sale buy(Account client) {
            SaleImpl s = new SaleImpl(wishes);

            history.add(s);

            client.transfer(storeAccount, value());
            s.setPaid();

            wishes.clear();
            return s;
        }
    }

    public class SaleImpl extends Exportable implements Sale {
        private ArrayList<Book> sold;
        private boolean paid;

        public SaleImpl(List<Book> sold) {
            this.paid = false;
            this.sold = new ArrayList<>();

            for(Book b: sold)
                this.sold.add(b);
        }

        private void setPaid() {
            paid = true;
        }

        public List<Book> getSold() {
            return (List<Book>) sold.clone();
        }

        public boolean isPaid() {
            return paid;
        }
    }
}
