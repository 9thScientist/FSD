import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store {
    private Map<Integer, Book> collection = new HashMap<>();
    private ArrayList<Sale> history = new ArrayList<>();
    private int storeAccount;

    Store(Bank b) {
        storeAccount = b.newAccount(1000);

        collection.put(1, new Book(1, 5, "Lev Tolstoy", "The Death of Ivan Ilyich"));
        collection.put(2, new Book(2, 10, "Haruki Murakami", "Kafka on the Shore"));
        collection.put(3, new Book(3, 15, "John Steinbeck", "East of Eden"));
        collection.put(4, new Book(4, 20, "Daniel Keys", "Flowers for Algernon"));
        collection.put(5, new Book(5, 25, "Markus Zusak", "The Book Thief"));
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

    public class Cart {
        private List<Book> wishes = new ArrayList<>();

        public void add(Book b) {
            wishes.add(b);
        }

        public int value() {
            return wishes
                .stream()
                .map(Book::getPrice)
                .reduce(0, (acc, price) -> acc + price);
        }

        public void buy(Bank bank, int clientAcc) {
            Sale s = new Sale(wishes);

            history.add(s);

            bank.transfer(clientAcc, storeAccount, value())
                .thenRun(() -> s.setPaid());

            wishes.clear();
        }
    }

    public class Sale {
        private List<Book> sold;
        private boolean paid;

        public Sale(List<Book> sold) {
            this.paid = false;
            this.sold = new ArrayList<>();

            for(Book b: sold)
                this.sold.add(b);
        }

        private void setPaid() {
            paid = true;
        }
    }
}
