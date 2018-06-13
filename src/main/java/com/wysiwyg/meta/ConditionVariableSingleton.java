package com.wysiwyg.meta;

import java.lang.InterruptedException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.*;

public class ConditionVariableSingleton {
    protected static Lock lock;
    protected static Condition conditionVariable;

    private ConditionVariableSingleton() {}

    public static synchronized Condition getConditionVariableInstance() {
        if (lock == null) {
            synchronized (ConditionVariableSingleton.class) {
                if (lock == null) {
                    lock = new ReentrantLock();
                    conditionVariable = lock.newCondition();
                }
            }
        }
        return conditionVariable;
    }

    public static void await() throws InterruptedException {
        getConditionVariableInstance();
        lock.lock();
        try {
            conditionVariable.await();
        } finally {
            lock.unlock();
        }
        
    }

    public static void broadcast() {
        getConditionVariableInstance();
        lock.lock();
        conditionVariable.signalAll();
        lock.unlock();
    }
}