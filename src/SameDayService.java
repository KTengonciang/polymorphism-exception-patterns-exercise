/**
 * SAME DAY: delivery fee = (base fee x 2) + Php 100.
 * Only for parcels with a chargeable weight of 10 kg or less.
 */
public class SameDayService implements DeliveryService {

    private static final double MULTIPLIER = 2.0;
    private static final double SURCHARGE = 100.0;
    private static final double MAX_CHARGEABLE_WEIGHT = 10.0;

    @Override
    public String getName() {
        return "Same Day";
    }

    @Override
    public double calculateFee(double baseFee) {
        return (baseFee * MULTIPLIER) + SURCHARGE;
    }

    @Override
    public boolean isAllowedFor(Parcel parcel) {
        return parcel.getChargeableWeight() <= MAX_CHARGEABLE_WEIGHT;
    }

    @Override
    public String getRestrictionMessage() {
        return "Same Day delivery is only for parcels up to 10 kg!";
    }
}
