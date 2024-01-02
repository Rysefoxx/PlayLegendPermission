package io.github.rysefoxx.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@UtilityClass
public class Util {

    /**
     * Checks if the given input is an integer.
     *
     * @param input The input
     * @return true if the input is an integer, otherwise false
     */
    public boolean isInteger(@NotNull String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }
}