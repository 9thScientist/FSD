package remote;

import interfaces.Resource;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.transport.Connection;
import rmi.Context;
import rmi.Reference;

public abstract class Remote implements Resource {
    protected final ThreadContext tc;
    protected final Connection c;
    protected final int id;
    private final Reference reference;

    public Remote(ThreadContext tc, Connection c, Integer id, Reference reference) {
        this.tc = tc;
        this.c = c;
        this.id = id;
        this.reference = reference;

        tc.serializer().register(Reference.class);
        tc.serializer().register(Context.class);
        registerMessages();
    }

    public Reference getReference() {
        return reference;
    }

    public abstract void registerMessages();

    public Remote clone() {
        return this;
    }
}
