package rmi;

import interfaces.Resource;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Exportable implements Resource {
    private Lock lock = new ReentrantLock();

    private int exportId = -1;

    public boolean isExported() {
        return exportId >= 0;
    }

    public void setExportId(int id) {
        this.exportId = id;
    }

    public int getExportId() {
        return exportId;
    }

    public abstract Exportable clone();

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
