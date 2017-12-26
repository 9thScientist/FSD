package runnable;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

public class Server {
    public static void main(String[] args) {
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("srv-%d", new Serializer());

        Address address = new Address(":" + Integer.parseInt(args[0]));
        // DistributedObject d = new DistributedObject(address);

        registMessages();
        assignHandlers(t, tc, address);

        System.out.println("Server ready on " + address.toString() + ".");
    }

    private static void assignHandlers(Transport t, ThreadContext tc, Address address) {
    }

    private static void registMessages() {
    }
}
