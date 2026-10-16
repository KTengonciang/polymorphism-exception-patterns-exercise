/**
 * Thrown when a delivery service can't be used for a parcel
 * (Same Day on a parcel over 10 kg chargeable weight).
 */
public class ServiceNotAllowedException extends Exception {

    private static final long serialVersionUID = 1L;

    public ServiceNotAllowedException(String message) {
        super(message);
    }
}
