package rmi;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DistributedObject {
    private Map<Integer, Exportable> objects;
    private AtomicInteger counter;
    private io.atomix.catalyst.transport.Address address;

    public DistributedObject(io.atomix.catalyst.transport.Address address) {
        this.address = address;
        objects = new TreeMap<>();
        counter = new AtomicInteger(0);
    }

    public static <T> T importObject(Reference<T> ref) {
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        try {
            Class<T> remoteType = (Class<T>) Class.forName("remote.Remote" + ref.getType().getSimpleName());

            Connection c = tc.execute(() ->
                    t.client().connect(ref.getAddress())
            ).join().get();

            return remoteType
                    .getDeclaredConstructor(ThreadContext.class, Connection.class, Integer.class, Reference.class)
                    .newInstance(tc, c, ref.getId(), ref);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> Reference<T> exportObject(Class<T> type, Exportable o) {
        if (o.isExported())
            return new Reference<>(address, o.getExportId(), type);

        int id = counter.incrementAndGet();
        objects.put(id, o);
        o.setExportId(id);

        return new Reference<>(address, id, type);
    }

    public static <T> List<T> importList(List<Reference<T>> list) throws Exception {
        return list.stream()
                .map(reference -> {
                    try {
                        return importObject(reference);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    public <T> List<Reference<T>> exportList(Class<T> type, List<? extends Exportable> list){
        return list.stream()
                .map(exportable -> exportObject(type, exportable))
                .collect(Collectors.toList());
    }

    public DistributedObject clone() {
        DistributedObject copy = new DistributedObject(this.address);
        copy.counter = new AtomicInteger(counter.get());

        copy.objects = new TreeMap<>();
        objects.forEach((k,v) -> copy.objects.put(k, (Exportable) v.clone()));

        return copy;
    }

    public Object get(int id) {
        return objects.get(id);
    }

    public <T> T get(Reference<T> address) {
        return (T) objects.get(address.getId());
    }
}
