package io.github.rysefoxx.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
@UtilityClass
public class TimeUtil {

    /**
     * ?: is a non-capturing group. This is used to group the regex without capturing it. <br>
     * \\d+ matches one or more digits. <br>
     * (?:(\\d+)y)? matches an optional group of one or more digits followed by a y. (Years) <br>
     * (?:(\\d+)mo)? matches an optional group of one or more digits followed by a mo. (Months) <br>
     * (?:(\\d+)w)? matches an optional group of one or more digits followed by a w. (Weeks) <br>
     * (?:(\\d+)d)? matches an optional group of one or more digits followed by a d. (Days) <br>
     * (?:(\\d+)h)? matches an optional group of one or more digits followed by a h. (Hours) <br>
     * (?:(\\d+)m)? matches an optional group of one or more digits followed by a m. (Minutes) <br>
     * (?:(\\d+)s)? matches an optional group of one or more digits followed by a s. (Seconds)
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile("(?:(\\d+)y)?(?:(\\d+)mo)?(?:(\\d+)w)?(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?");

    /**
     * The DateTimeFormatter used to convert a LocalDateTime to a readable string. The format is "dd.MM.yyyy HH:mm:ss".
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /**
     * Parses a duration string and adds it to the current time.
     *
     * @param durationString the string representing the duration (e.g., "5h30m10s").
     * @return the LocalDateTime after adding the duration to the current time.
     */
    public @Nullable LocalDateTime parseDuration(@NotNull String durationString) {
        Matcher matcher = DURATION_PATTERN.matcher(durationString);

        if (!matcher.matches()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.plusYears(parseDurationPart(matcher.group(1)))
                .plusMonths(parseDurationPart(matcher.group(2)))
                .plusWeeks(parseDurationPart(matcher.group(3)))
                .plusDays(parseDurationPart(matcher.group(4)))
                .plusHours(parseDurationPart(matcher.group(5)))
                .plusMinutes(parseDurationPart(matcher.group(6)))
                .plusSeconds(parseDurationPart(matcher.group(7)));
    }

    /**
     * Converts a duration in the datatype long
     *
     * @param part The part of the duration
     * @return The duration in the datatype long. 0 if the part is null.
     */
    private long parseDurationPart(@Nullable String part) {
        return part == null ? 0 : Long.parseLong(part);
    }

    /**
     * Converts a LocalDateTime to a readable string.
     *
     * @param localDateTime The LocalDateTime to convert.
     * @return The readable string in the format "dd.MM.yyyy HH:mm:ss".
     */
    public @NotNull String toReadableString(@NotNull LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIME_FORMATTER);
    }
}