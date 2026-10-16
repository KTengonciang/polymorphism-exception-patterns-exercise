import java.util.Map;
import java.util.regex.Pattern;

/**
 * Base class for every parcel type.
 *
 * Holds the details all parcels share (encapsulated as private fields) and
 * declares the rules that differ per type as abstract methods. Each subclass
 * answers "what's my chargeable weight?" and "what's my base fee?" its own
 * way, and the rest of the program calls these without ever checking which
 * type of parcel it has. That's the polymorphism.
 */
public abstract class Parcel {

    private static final Pattern TRACKING_PATTERN = Pattern.compile("^ABC\\d{4}$");
    // Starts with a letter; then letters (including ñ, é), spaces, periods, hyphens, apostrophes
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\p{L}[\\p{L} .'-]*$");
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;

    private final String trackingNumber;
    private final String receiverName;
    private final double weight;
    private DeliveryService deliveryService;
    private ParcelStatus status;

    protected Parcel(String trackingNumber, String receiverName, double weight)
            throws InvalidParcelException {
        this.trackingNumber = validateTrackingNumber(trackingNumber);
        this.receiverName = validateReceiverName(receiverName);
        if (weight <= 0) {
            throw new InvalidParcelException("Weight must be greater than 0 kg.");
        }
        // Each subclass constructor checks the weight against its own maximum.
        this.weight = weight;
        this.status = ParcelStatus.PENDING;
    }

    // ------------------------------------------------------------------
    // What each parcel type decides for itself
    // ------------------------------------------------------------------

    /** "Document", "Box", or "Fragile". */
    public abstract String getTypeName();

    /** Details only this parcel type has (label -> value), shown on the review screen. */
    public abstract Map<String, String> getTypeDetails();

    /** The weight the fee is based on (kg). */
    public abstract double getChargeableWeight();

    /** Fee before the delivery service is applied (Php). */
    public abstract double getBaseFee();

    // ------------------------------------------------------------------
    // Shared behavior
    // ------------------------------------------------------------------

    /** Final fee: the delivery service (Strategy) applied to this parcel's base fee. */
    public double getDeliveryFee() {
        return deliveryService.calculateFee(getBaseFee());
    }

    /**
     * Assigns a delivery service. The parcel is only changed if the service
     * is allowed, so a rejected Same Day request leaves the old service as is.
     */
    public void setDeliveryService(DeliveryService service) throws ServiceNotAllowedException {
        if (!service.isAllowedFor(this)) {
            throw new ServiceNotAllowedException(service.getRestrictionMessage());
        }
        this.deliveryService = service;
    }

    public void markDelivered() throws ParcelAlreadyDeliveredException {
        if (isDelivered()) {
            throw new ParcelAlreadyDeliveredException();
        }
        this.status = ParcelStatus.DELIVERED;
    }

    public boolean isDelivered() {
        return status == ParcelStatus.DELIVERED;
    }

    public String getTrackingNumber() { return trackingNumber; }
    public String getReceiverName() { return receiverName; }
    public double getWeight() { return weight; }
    public DeliveryService getDeliveryService() { return deliveryService; }
    public ParcelStatus getStatus() { return status; }

    // ------------------------------------------------------------------
    // Validation rules, kept in one place. The constructors use them, and
    // Main uses the same methods to check each input as it's typed, so the
    // rules never get out of sync.
    // ------------------------------------------------------------------

    /** Returns the tracking number in uppercase if valid ("ABC" + 4 digits). */
    public static String validateTrackingNumber(String input) throws InvalidParcelException {
        String value = (input == null) ? "" : input.trim().toUpperCase();
        if (!TRACKING_PATTERN.matcher(value).matches()) {
            throw new InvalidParcelException(
                    "Tracking number must be \"ABC\" followed by 4 digits (e.g. ABC0001).");
        }
        return value;
    }

    /** Returns the name trimmed, with repeated spaces collapsed ("Juan   Cruz" -> "Juan Cruz"). */
    public static String validateReceiverName(String input) throws InvalidParcelException {
        String value = (input == null) ? "" : input.trim().replaceAll("\\s+", " ");
        if (value.isEmpty()) {
            throw new InvalidParcelException("Receiver name cannot be empty.");
        }
        if (!NAME_PATTERN.matcher(value).matches()) {
            throw new InvalidParcelException(
                    "Receiver name can only have letters, spaces, periods, hyphens, and apostrophes.");
        }
        if (value.length() < MIN_NAME_LENGTH || value.length() > MAX_NAME_LENGTH) {
            throw new InvalidParcelException("Receiver name must be "
                    + MIN_NAME_LENGTH + " to " + MAX_NAME_LENGTH + " characters.");
        }
        return value;
    }

    public static double validateWeight(double weight, double maxWeight, String typeName)
            throws InvalidParcelException {
        if (weight <= 0) {
            throw new InvalidParcelException("Weight must be greater than 0 kg.");
        }
        if (weight > maxWeight) {
            throw new InvalidParcelException(
                    typeName + " parcels can weigh at most " + formatKg(maxWeight) + " kg.");
        }
        return weight;
    }

    /** 0.5 -> "0.5", 50.0 -> "50" */
    protected static String formatKg(double kg) {
        return (kg == Math.floor(kg)) ? String.valueOf((long) kg) : String.valueOf(kg);
    }
}
