package rmi;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedObject {
    private Map<Integer, Object> objects;
    private AtomicInteger counter;
    private Address address;

    public DistributedObject(Address address) {
        this.address = address;
        objects = new TreeMap<>();
        counter = new AtomicInteger(0);
    }

    public static <T> T importObject(Reference<T> ref) throws Exception {
        Class<T> remoteType;

        remoteType = (Class<T>) Class.forName("remote.Remote" + ref.getType().getSimpleName());

        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Connection c = tc.execute(() ->
           t.client().connect(ref.getAddress())
        ).join().get();

        return remoteType
               .getDeclaredConstructor(tc.getClass(), c.getClass(), Integer.class, Reference.class)
               .newInstance(tc, c, ref.getId(), ref);
    }

    public <T> Reference<T> exportObject(Class<T> type, Object o) {
        int id = counter.incrementAndGet();
        objects.put(id, o);

        return new Reference<T>(address, id, type);
    }

    public Object get(int id) {
        return objects.get(id);
    }

    public <T> T get(Reference<T> reference) {
        return (T) objects.get(reference.getId());
    }
}
