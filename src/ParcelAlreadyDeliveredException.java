/**
 * Thrown when trying to change or deliver a parcel that is already delivered.
 * Main prints the exact wording each menu option needs, since the spec uses
 * a different message for Change Delivery Service and Mark as Delivered.
 */
public class ParcelAlreadyDeliveredException extends Exception {

    private static final long serialVersionUID = 1L;

    public ParcelAlreadyDeliveredException() {
        super("Parcel already delivered!");
    }
}
