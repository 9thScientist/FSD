package business;

import interfaces.*;
import rmi.Exportable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreImpl extends Exportable implements Store {
    private Map<Integer, Book> collection = new HashMap<>();
    private ArrayList<Sale> history = new ArrayList<>();
    private Account storeAccount;

    private StoreImpl() {

    }

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

    private void setCollection(Map<Integer, Book> collection) {
        this.collection = new HashMap<>();
        collection.forEach((k,v) -> this.collection.put(k, v));
    }

    private void setHistory(List<Sale> history) {
        this.history = new ArrayList<>();
        history.forEach(s -> this.history.add((Sale) s.clone()));
    }

    private void setStoreAccount(Account storeAccount) {
        this.storeAccount = (Account) storeAccount.clone();
    }

    public StoreImpl clone() {
        StoreImpl copy = new StoreImpl();
        copy.setCollection(this.collection);
        copy.setHistory(this.history);
        copy.setStoreAccount(this.storeAccount);

        return copy;
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
            SaleImpl s = (SaleImpl) toSale();

            history.add(s);

            client.transfer(storeAccount, value());
            s.setPaid();

            wishes.clear();
            return s;
        }

        public Sale toSale() {
            return new SaleImpl(wishes);
        }

        private void setWishes(List<Book> wishes) {
            this.wishes = new ArrayList<>();
            wishes.forEach(w -> this.wishes.add(w));
        }

        public CartImpl clone() {
            CartImpl copy = new CartImpl();
            copy.setWishes(this.wishes);

            return copy;
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

        private void setPaid(boolean paid) {
            this.paid = paid;
        }

        public List<Book> getSold() {
            return (List<Book>) sold.clone();
        }

        public boolean isPaid() {
            return paid;
        }

        public SaleImpl clone() {
            SaleImpl copy = new SaleImpl(this.sold);
            copy.setPaid(this.paid);

            return copy;
        }
    }
}
