package io.github.rysefoxx.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public class AsyncDatabaseManager {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    /***
     * Executes a runnable async. If a callback is provided, it will be called on success or failure.
     * @param runnable The runnable to execute.
     * @param callback The callback to call on success or failure. Can be null.
     */
    public void executeAsync(@NotNull Runnable runnable, @Nullable DatabaseCallback callback) {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                runnable.run();
                if (callback != null) callback.onComplete();
            } catch (Throwable throwable) {
                if (callback != null) callback.onFailure(throwable);
            }
        });
    }

    /**
     * Executes a runnable async without a callback.
     *
     * @param runnable The runnable to execute.
     */
    public void executeAsync(@NotNull Runnable runnable) {
        executeAsync(runnable, null);
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdownExecutorService() {
        if (EXECUTOR_SERVICE.isShutdown()) return;
        EXECUTOR_SERVICE.shutdown();
    }

}