/**
 * Copyright (c) 2025 p0t4t0sandwich - dylan@sperrer.ca
 * This project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
package dev.neuralnexus.mri.scheduler;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;

/** Scheduler abstraction */
// Yoinked from TaterLib
public interface Scheduler {
    Logger logger = LoggerFactory.getLogger("MassResourceInterchange-scheduler");

    @ApiStatus.Internal
    ForkJoinPool pool();

    @ApiStatus.Internal
    void shutdownBackgroundScheduler();

    @ApiStatus.Internal
    void replaceBackgroundScheduler(Supplier<Executor> backgroundScheduler, boolean managed);

    /**
     * Run a task asynchronously.
     *
     * @param run: The task to run asynchronously.
     */
    default void runAsync(Runnable run) {
        pool().submit(run);
    }

    /**
     * Run a task asynchronously, after a delay.
     *
     * @param run: The task to run asynchronously.
     * @param delay: The delay in ticks to wait before running the task.
     */
    default void runLaterAsync(Runnable run, Long delay) {
        pool().submit(
                        () -> {
                            try {
                                Thread.sleep(delay * 1000 / 20);
                            } catch (InterruptedException e) {
                                logger.error(
                                        "Something went wrong while executing an async task", e);
                            }
                            run.run();
                        });
    }

    /**
     * Run a task asynchronously, repeating it every period seconds.
     *
     * @param run The task to run asynchronously.
     * @param delay The delay in seconds to wait before running the task.
     * @param period The period in seconds to repeat the task.
     */
    default ForkJoinTask<Object> repeatAsync(Runnable run, Long delay, Long period) {
        return pool().submit(
                        () -> {
                            try {
                                Thread.sleep(delay * 1000 / 20);
                            } catch (InterruptedException e) {
                                logger.error(
                                        "Something went wrong while executing an async task", e);
                            }
                            while (true) {
                                try {
                                    Thread.sleep(period * 1000 / 20);
                                } catch (InterruptedException e) {
                                    logger.error(
                                            "Something went wrong while executing an async task",
                                            e);
                                }
                                run.run();
                            }
                        });
    }
}
