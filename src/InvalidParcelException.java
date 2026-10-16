/**
 * Thrown when a parcel detail breaks a rule (bad tracking number format,
 * weight over the limit, pages out of range, etc.).
 */
public class InvalidParcelException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidParcelException(String message) {
        super(message);
    }
}
