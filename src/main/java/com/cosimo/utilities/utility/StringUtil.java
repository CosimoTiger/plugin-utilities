package com.cosimo.utilities.utility;

import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class that contains static helper methods for Strings.
 */
public class StringUtil {

    /**
     * Returns the input {@link String} with its first letter capitalized
     *
     * @param input Nullable {@link String} to capitalize
     * @return Input {@link String} argument with it's first letter capitalized
     */
    @Nullable
    public static String capitalize(@Nullable String input) {
        return input == null || input.isEmpty() ? input : Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    /**
     * Returns the possessive form of the input {@link String}
     *
     * @param input Nullable {@link String} to make possessive
     * @param sCase {@link String} to append to the input in the case the input does not end in {@code s}
     * @param noneCase {@link String} to append to the input in the case the input does end in {@code s}
     * @return Input {@link String} argument's possessive form
     */
    @Nullable
    public static String possessive(@Nullable String input, @Nonnull String sCase, @Nonnull String noneCase) {
        return input == null ? null : input.concat(input.endsWith("s") ? noneCase : sCase);
    }

    /**
     * Returns the possessive form of the input {@link String}
     *
     * @param input Nullable {@link String} to make possessive
     * @return Input {@link String} argument's possessive form
     */
    @Nullable
    public static String possessive(@Nullable String input) {
        return possessive(input, "'s", "'");
    }

    /**
     * Returns the input {@link String} with each word's first letter capitalized
     *
     * @param input Nullable {@link String} to capitalize
     * @return Input {@link String} argument with each first letter capitalized
     */
    @Nullable
    public static String toTitleCase(String input) {
        final String delimiter = " ";
        return input == null ? null : Arrays.stream(input.split(delimiter))
                .map(StringUtil::capitalize)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Returns a translated input {@link String} for use with {@code &} to represent colors
     *
     * @param input Nullable {@link String} to translate
     * @return Translated input {@link String} argument
     */
    @Nullable
    public static String format(@Nullable String input) {
        return input == null ? null : ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * Returns a translated input {@link String} for use with {@code &} to represent colors
     *
     * @param input Nullable {@link String} to translate
     * @param args Nullable {@link Object} array to format the input {@link String}
     * @return Translated input {@link String} argument
     */
    @Nullable
    public static String format(@Nullable String input, @Nullable Object... args) {
        // To satisfy String.format(...) the input must not be null.
        return input == null ? null : format(String.format(input, args));
    }

    /**
     * Returns a translated {@link List} for use with {@code &} to represent colors
     *
     * @param input Nullable {@link List} to translate
     * @return Translated input {@link List} argument
     */
    @Nullable
    public static List<String> format(@Nullable List<String> input) {
        return input == null ? null : input.stream().map(StringUtil::format).collect(Collectors.toList());
    }

    /**
     * Returns a {@link List} from the input {@link String} using the delimiter provided
     *
     * @param input Nullable {@link String} to transform
     * @param delimiter {@link Character} to split on
     * @return {@link List} from the input {@link String} argument
     */
    @Nullable
    public static List<String> toList(@Nullable String input, char delimiter) {
        return toList(input, String.valueOf(delimiter));
    }

    /**
     * Returns a {@link List} from the input {@link String} using the delimiter provided
     *
     * @param input Nullable {@link String} to transform
     * @param delimiter {@link String} to split on
     * @return {@link List} from the input {@link String} argument
     */
    @Nullable
    public static List<String> toList(@Nullable String input, String delimiter) {
        return input == null ? null : Arrays.stream(input.split(delimiter)).collect(Collectors.toList());
    }

}