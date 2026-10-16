import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BOX
 * - Length, width, height in cm (whole numbers, > 0)
 * - Max weight: 50 kg
 * - Volumetric weight = (L x W x H) / 3500
 * - Chargeable weight = the bigger of actual and volumetric weight,
 *   rounded UP to the next whole kg (2.0 -> 2, 2.1 -> 3)
 * - Base fee = Php 40 x chargeable weight
 */
public class Box extends Parcel {

    public static final double MAX_WEIGHT = 50.0;
    public static final int MAX_DIMENSION = 1000; // 10 m per side
    private static final double VOLUMETRIC_DIVISOR = 3500.0;
    private static final double RATE_PER_KG = 40.0;

    private final int length;
    private final int width;
    private final int height;

    public Box(String trackingNumber, String receiverName, double weight,
               int length, int width, int height) throws InvalidParcelException {
        super(trackingNumber, receiverName, weight);
        validateWeight(weight, MAX_WEIGHT, "Box");
        this.length = validateDimension(length, "Length");
        this.width = validateDimension(width, "Width");
        this.height = validateDimension(height, "Height");
    }

    public static int validateDimension(int value, String label) throws InvalidParcelException {
        if (value <= 0) {
            throw new InvalidParcelException(label + " must be a whole number greater than 0 cm.");
        }
        if (value > MAX_DIMENSION) {
            throw new InvalidParcelException(label + " cannot be more than " + MAX_DIMENSION + " cm.");
        }
        return value;
    }

    public int getLength() { return length; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public double getVolumetricWeight() {
        // long so very large boxes can't overflow an int
        long volume = (long) length * width * height;
        return volume / VOLUMETRIC_DIVISOR;
    }

    @Override
    public String getTypeName() {
        return "Box";
    }

    @Override
    public Map<String, String> getTypeDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Dimensions", length + " x " + width + " x " + height + " cm");
        return details;
    }

    @Override
    public double getChargeableWeight() {
        return Math.ceil(Math.max(getWeight(), getVolumetricWeight()));
    }

    @Override
    public double getBaseFee() {
        return RATE_PER_KG * getChargeableWeight();
    }
}
