package interfaces;

import io.atomix.catalyst.transport.Address;

public interface Manager {
    void begin();
    void commit();
    int add(Address address);
}
