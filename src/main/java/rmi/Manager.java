package rmi;

import com.transactions.*;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;
import pt.haslab.ekit.Log;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Manager {
    private static final AtomicInteger nextXID = new AtomicInteger(0);

    private static final ThreadLocal<Context> context =
            new ThreadLocal<>();
    private static final ThreadLocal<List<Address>> participants =
            new ThreadLocal<>().withInitial(ArrayList::new);
    private static final ThreadLocal<Log> logger =
            new ThreadLocal<>().withInitial(() -> new Log("Mngr"));

    private static ThreadContext tc = new SingleThreadContext("mngr-%d", new Serializer());
    private static Transport t = new NettyTransport();
    private static Address address;

    public static void begin() throws AddressNotSetException {
        Log log = logger.get();

        if (address == null)
            throw new AddressNotSetException("Manager's address is not set");

        Context context = new Context(address, nextXID.incrementAndGet());
        participants.get().add(address);

        tc.execute(() -> {
            t.server().listen(address, c ->
                    c.handler(ManagerAddResourceReq.class, m -> {
                        participants.get().add(m.getReference().getAddress());

                        log.append(m.getReference());
                    }));
        });

        Manager.context.set(context);
    }

    public static void commit() {
        Context current = context.get();
        Log log = logger.get();
        Address[] participants = (Address[]) Manager.participants.get().stream()
                                .map(p -> new Address(p.host(), p.port() + 100 )).toArray();

        Clique c = new Clique(t, 0, participants);

        Map<Integer, Boolean> ready = new HashMap<>();
        int size = participants.length;

        tc.execute(() -> {
            c.handler(ManagerAbortRep.class, (s, m) -> {

            });
            c.handler(ManagerCommitRep.class, (s, m) -> {

            });
            c.handler(ManagerPreparedRep.class, (s, m) -> {
                if (m.isOk())
                    ready.put(s, true);
                else
                    ready.put(s, false);

                if (ready.size() == size) {
                    if (ready.values().stream().allMatch(Boolean::valueOf)) {
                        broadcast(c, size, new ManagerCommitReq());
                        log.append(new ManagerCommitReq());
                    } else {
                        ready.keySet().stream()
                                      .filter(ready::get)
                                      .forEach(i -> c.send(i, new ManagerAbortReq()));
                        log.append(new ManagerAbortReq());
                    }
                }
            });

            c.open().thenRun(() ->
                    broadcast(c, size, new ManagerPreparedReq())
            );
        });

        context.set(null);
    }

    public static void add(Context ctx, Reference ref) throws Exception {
        Connection c = tc.execute(() ->
                t.client().connect(ctx.getAddress())
        ).join().get();

        ManagerAddResourceRep r = (ManagerAddResourceRep) tc.execute(() ->
                c.sendAndReceive(new ManagerAddResourceReq(ctx, ref))
        ).join().get();
    }

    public static Address getAddress() {
        return address;
    }

    public static void setAddress(Address address) {
        Manager.address = address;
    }


    public static Context getContext() {
        return context.get();
    }

    public static void setContext(Context c) {
        Manager.context.set(c);
    }

    private static void broadcast(Clique c, int size, Object o) {
        IntStream.range(0, size)
                .forEach(i -> c.send(i, o));
    }
}
