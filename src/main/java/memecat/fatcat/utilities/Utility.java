package memecat.fatcat.utilities;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * An utility class that contains static methods.
 */
public class Utility {

    /**
     * Returns the greatest integer from a given array of integers.
     *
     * @param numbers Array of integers
     * @return Greatest integer from the given array
     */
    public static int max(@NotNull int... numbers) {
        Preconditions.checkArgument(numbers != null, "Array of integers shouldn't be null");
        int max = numbers[0];

        for (int number : numbers) {
            if (number > max) {
                max = number;
            }
        }

        return max;
    }
}