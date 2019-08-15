package memecat.fatcat.utilities.menu.attribute;

/**
 * Enum constants that are used for creating chest inventories that follow the pattern of having rows of 9 item slots.
 * <p>
 * Maximal row amount is 6 because at bigger inventory sizes the inventory rendering becomes glitched.
 */
public enum Rows {

    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);

    private final int amount;
    private final int size;

    Rows(int amount) {
        this.amount = amount;
        this.size = amount * 9;
    }

    /**
     * Returns the integer amount of rows.
     *
     * @return Amount of rows
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Returns the size of an inventory depending on the amount of rows.
     * <p>
     * Slot amount or size is calculated as rows × 9.
     *
     * @return Amount of inventory slots
     */
    public int getSize() {
        return size;
    }
}