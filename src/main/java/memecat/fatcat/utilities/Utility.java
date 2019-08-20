package memecat.fatcat.utilities;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * An utility class that contains static methods.
 */
public class Utility {

    /**
     * Returns the greatest integer from a given array of integers by using simple code for performance reasons.
     *
     * @param numbers Array of integers
     * @return Greatest integer from the given array
     * @throws IllegalArgumentException If the numbers argument is null
     */
    public static int max(@NotNull int... numbers) {
        Preconditions.checkArgument(numbers != null, "Array of integers can't be null");
        int max = Integer.MIN_VALUE;

        for (int number : numbers) {
            if (number > max) {
                max = number;
            }
        }

        return max;
    }
}