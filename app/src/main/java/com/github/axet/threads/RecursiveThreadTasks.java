package com.github.axet.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.axet.threads.RecursiveThreadExecutor.Task;

public class RecursiveThreadTasks {
    RecursiveThreadExecutor es;

    List<Task> tasks = new ArrayList<Task>();

    AtomicBoolean interrupted;

    public RecursiveThreadTasks(RecursiveThreadExecutor e) {
        this.es = e;
        interrupted = new AtomicBoolean(false);
    }

    public RecursiveThreadTasks(RecursiveThreadExecutor e, AtomicBoolean interrupted) {
        this.es = e;
        this.interrupted = interrupted;
    }

    public void execute(Runnable r) {
        Task t = new Task(r) {
            @Override
            public boolean interrupted() {
                return interrupted.get();
            }
        };
        tasks.add(t);
        es.execute(t);
    }

    public void waitTermination() throws InterruptedException {
        try {
            for (Task r : tasks) {
                es.waitTermination(r);

                // we may lose some exception occured in next tasks
                if (r.e != null) {
                    if (r.e instanceof InterruptedException)
                        throw (InterruptedException) r.e;
                    else if (r.e instanceof RuntimeException)
                        throw (RuntimeException) r.e;
                    else
                        throw new RuntimeException(r.e);
                }
            }
        } catch (InterruptedException e) {
            interrupted.set(true);
            throw e;
        }
    }
}
