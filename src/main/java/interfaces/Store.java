package interfaces;

import java.util.List;

public interface Store {
    Book search(String title);
    List<Sale> getHistory();
}
