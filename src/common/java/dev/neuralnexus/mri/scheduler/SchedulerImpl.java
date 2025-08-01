/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class SchedulerImpl implements Scheduler {
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static boolean managed = true;
    private static final ForkJoinPool defaultScheduler = create();
    private static Supplier<ForkJoinPool> backgroundScheduler = () -> defaultScheduler;

    @Override
    public ForkJoinPool pool() {
        return backgroundScheduler.get();
    }

    @Override
    public void shutdownBackgroundScheduler() {
        if (backgroundScheduler != null && managed) {
            backgroundScheduler.get().shutdown();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void replaceBackgroundScheduler(
            Supplier<Executor> backgroundScheduler, boolean managed) {
        shutdownBackgroundScheduler();
        SchedulerImpl.managed = managed;
        SchedulerImpl.backgroundScheduler = (Supplier<ForkJoinPool>) (Object) backgroundScheduler;
    }

    private static ForkJoinPool create() {
        int i = Runtime.getRuntime().availableProcessors() - 1;
        if (i < 1) {
            i = 1;
        }

        return new ForkJoinPool(
                i,
                pool -> {
                    ForkJoinWorkerThread worker =
                            new ForkJoinWorkerThread(pool) {
                                protected void onTermination(Throwable throwable) {
                                    if (throwable != null) {
                                        logger.warn(this.getName() + " died", throwable);
                                    } else {
                                        logger.debug(this.getName() + " shutdown");
                                    }
                                    super.onTermination(throwable);
                                }
                            };
                    worker.setName("taterapi-" + WORKER_COUNT.getAndIncrement());
                    worker.setDaemon(true);
                    return worker;
                },
                (thread, throwable) ->
                        logger.error("Caught exception in thread " + thread, throwable),
                true);
    }
}
