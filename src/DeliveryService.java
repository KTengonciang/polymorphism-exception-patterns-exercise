/**
 * Strategy pattern: each delivery service is its own class with its own
 * pricing rule and its own restriction. A Parcel just holds "some
 * DeliveryService" and asks it for the fee, so adding a new service later
 * means adding a class, not editing if/else chains.
 */
public interface DeliveryService {

    /** Service name shown to the user, e.g. "Express". */
    String getName();

    /** Turns the parcel's base fee into the final delivery fee. */
    double calculateFee(double baseFee);

    /** Whether this service can be used for the given parcel. */
    boolean isAllowedFor(Parcel parcel);

    /** Message shown when isAllowedFor() is false. */
    String getRestrictionMessage();
}
