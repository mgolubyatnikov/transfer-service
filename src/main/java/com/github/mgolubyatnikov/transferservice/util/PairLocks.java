package com.github.mgolubyatnikov.transferservice.util;

import com.google.common.util.concurrent.Striped;

import java.util.concurrent.locks.Lock;

public class PairLocks<T extends Comparable<T>> {

    private final Striped<Lock> locks;

    public PairLocks() {
        this.locks = Striped.lazyWeakLock(8);
    }

    public PairLock get(T object1, T object2) {
        T min;
        T max;

        if (object1.compareTo(object2) < 0) {
            min = object1;
            max = object2;
        } else {
            min = object2;
            max = object1;
        }

        Lock lock1 = locks.get(min);
        Lock lock2 = locks.get(max);

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
            lock1.unlock();
            lock2.unlock();
        }
    }
}
