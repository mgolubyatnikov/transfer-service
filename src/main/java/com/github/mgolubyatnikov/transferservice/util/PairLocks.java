package com.github.mgolubyatnikov.transferservice.util;

import com.google.common.util.concurrent.Striped;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;

public class PairLocks<T> {

    private final Striped<Lock> locks;

    public PairLocks() {
        this.locks = Striped.lazyWeakLock(8);
    }

    public PairLock get(T object1, T object2) {
        Iterator<Lock> lockIterator = locks.bulkGet(Arrays.asList(object1, object2)).iterator();

        Lock lock1 = lockIterator.next();
        Lock lock2 = lockIterator.next();

        return new PairLock(lock1, lock2);
    }

    public static class PairLock {

        private final Lock lock1;
        private final Lock lock2;

        public PairLock(Lock lock1, Lock lock2) {
            this.lock1 = lock1;
            this.lock2 = lock2;
        }

        public void lock() {
            lock1.lock();
            lock2.lock();
        }

        public void unlock() {
            lock2.unlock();
            lock1.unlock();
        }
    }
}
