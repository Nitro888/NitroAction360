package com.github.axet.threads;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * keep one instance of RecursiveThreadExecutor
 * 
 * @author axet
 * 
 */
public class RecursiveStaticExecutor {
    static AtomicInteger counter = new AtomicInteger();
    static RecursiveThreadExecutor es = null;

    public RecursiveStaticExecutor() {
        counter.incrementAndGet();
    }

    @Override
    protected void finalize() {
        if (counter.decrementAndGet() == 0) {
            synchronized (counter) {
                if (es != null) {
                    es.close();
                    es = null;
                }
            }
        }
    }

    public RecursiveThreadExecutor getInstance() {
        synchronized (counter) {
            if (es == null)
                es = new RecursiveThreadExecutor();

            return es;
        }
    }
}
