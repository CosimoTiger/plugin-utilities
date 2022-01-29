package memecat.fatcat.utilities.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An utility class that contains static methods.
 */
public class Utility {

    /**
     * Returns the greatest integer from a given {@link Iterable} object of {@link Integer}s by using simple code for
     * performance reasons.
     *
     * @param numbers An {@link Iterable} object of {@link Integer}s
     * @return Greatest integer from the given array
     * @throws IllegalArgumentException If the numbers argument is null
     */
    public static int max(@Nonnull Iterable<Integer> numbers) {
        int max = Integer.MIN_VALUE;

        for (int number : numbers) {
            if (number > max) {
                max = number;
            }
        }

        return max;
    }

    /**
     * Returns the input {@link String} with it's first letter capitalized.
     *
     * @param input Nullable {@link String} to capitalize
     * @return Input {@link String} argument with it's first letter capitalized
     */
    @Nullable
    public static String capitalize(@Nullable String input) {
        return input == null || input.isEmpty() ? input : Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    @Nullable
    public static String possessive(@Nullable String input, @Nonnull String sCase, @Nonnull String noneCase) {
        return input == null ? null : input.concat(input.endsWith("s") ? noneCase : sCase);
    }

    @Nullable
    public static String possessive(@Nullable String input) {
        return possessive(input, "'s", "'");
    }

    /**
     * Returns the greatest integer from a given array of integers by using simple code for performance reasons.
     *
     * @param numbers Array of integers
     * @return Greatest integer from the given array
     * @throws IllegalArgumentException If the numbers argument is null
     */
    public static int max(int... numbers) {
        int max = Integer.MIN_VALUE;

        for (int number : numbers) {
            if (number > max) {
                max = number;
            }
        }

        return max;
    }
}