package rmi;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

public class DistributedObject {
    public static <T> T importObject(Reference<T> ref) throws Exception {
        Class<T> remoteType;

        remoteType = (Class<T>) Class.forName("remote.Remote" + ref.getType().getSimpleName());

        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Connection c = tc.execute(() ->
           t.client().connect(ref.getAddress())
        ).join().get();

        return remoteType
               .getDeclaredConstructor(tc.getClass(), c.getClass(), Integer.class)
               .newInstance(tc, c, ref.getId());
    }

    public static <T> Reference<T> exportObject(Address address, int id, Class<T> interfce) {
        return new Reference<T>(address, id, interfce);
    }
}
