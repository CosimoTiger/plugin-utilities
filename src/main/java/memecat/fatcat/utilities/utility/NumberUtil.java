package memecat.fatcat.utilities.utility;

import javax.annotation.Nonnull;

public class NumberUtil {

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
        for(int number : numbers) {
            max = Math.max(number, max);
        }
        return max;
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
        for(int number : numbers) {
            max = Math.max(number, max);
        }
        return max;
    }

    /**
     * Returns the smallest integer from a given {@link Iterable} object of {@link Integer}s by using simple code for
     * performance reasons.
     *
     * @param numbers An {@link Iterable} object of {@link Integer}s
     * @return Smallest integer from the given array
     * @throws IllegalArgumentException If the numbers argument is null
     */
    public static int min(@Nonnull Iterable<Integer> numbers) {
        int min = Integer.MAX_VALUE;
        for(int number : numbers) {
            min = Math.min(number, min);
        }
        return min;
    }

    /**
     * Returns the smallest integer from a given array of integers by using simple code for performance reasons.
     *
     * @param numbers Array of integers
     * @return Smallest integer from the given array
     * @throws IllegalArgumentException If the numbers argument is null
     */
    public static int min(int... numbers) {
        int min = Integer.MAX_VALUE;
        for(int number : numbers) {
            min = Math.min(number, min);
        }
        return min;
    }

}
