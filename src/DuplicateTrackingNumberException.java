/**
 * Thrown when adding a parcel whose tracking number is already in use.
 */
public class DuplicateTrackingNumberException extends Exception {

    private static final long serialVersionUID = 1L;

    public DuplicateTrackingNumberException(String trackingNumber) {
        super("Tracking number " + trackingNumber + " already exists!");
    }
}
