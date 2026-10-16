/**
 * Thrown when no parcel matches the tracking number entered.
 */
public class ParcelNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public ParcelNotFoundException() {
        super("Parcel not found!");
    }
}
