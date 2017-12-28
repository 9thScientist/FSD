package rmi;

import com.transactions.ManagerAddResourceRep;
import com.transactions.ManagerAddResourceReq;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

public class Manager {
    public static ThreadLocal<Context> context = new ThreadLocal<>();

    private static ThreadContext tc = new SingleThreadContext("mngr-%d", new Serializer());
    private static Transport t = new NettyTransport();

    public static void add(Context ctx, Reference ref) throws Exception {
        Connection c = tc.execute(() ->
                t.client().connect(ctx.getAddress())
        ).join().get();

        ManagerAddResourceRep r = (ManagerAddResourceRep) tc.execute(() ->
               c.sendAndReceive(new ManagerAddResourceReq(ctx, ref))
        ).join().get();
    }
}
