/**
 * EXPRESS: delivery fee = base fee x 1.5. Available for every parcel.
 */
public class ExpressService implements DeliveryService {

    private static final double MULTIPLIER = 1.5;

    @Override
    public String getName() {
        return "Express";
    }

    @Override
    public double calculateFee(double baseFee) {
        return baseFee * MULTIPLIER;
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
