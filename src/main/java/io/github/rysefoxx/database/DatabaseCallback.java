package io.github.rysefoxx.database;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
public interface DatabaseCallback {

    void onComplete();

    void onFailure(Throwable throwable);
}
