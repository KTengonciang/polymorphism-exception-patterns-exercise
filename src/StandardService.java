/**
 * STANDARD: delivery fee = base fee. Available for every parcel.
 */
public class StandardService implements DeliveryService {

    @Override
    public String getName() {
        return "Standard";
    }

    @Override
    public double calculateFee(double baseFee) {
        return baseFee;
    }

    @Override
    public boolean isAllowedFor(Parcel parcel) {
        return true;
    }

    @Override
    public String getRestrictionMessage() {
        return "";
    }
}
