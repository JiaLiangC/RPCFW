package raft.common.utils;

import org.checkerframework.checker.units.qual.A;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

public class AutoCloseLock implements AutoCloseable {

    public static AutoCloseLock acquire(final Lock lock){
        return new AutoCloseLock(lock);
    }

    private final  Lock underlyingLock;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    public AutoCloseLock(Lock l){
        this.underlyingLock = l;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false,true)){
            underlyingLock.unlock();
        }
    }
}
