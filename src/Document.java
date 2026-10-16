import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DOCUMENT
 * - Number of pages: 1 to 100
 * - Max weight: 0.5 kg
 * - Chargeable weight = actual weight
 * - Base fee = Php 50 flat
 */
public class Document extends Parcel {

    public static final double MAX_WEIGHT = 0.5;
    public static final int MIN_PAGES = 1;
    public static final int MAX_PAGES = 100;
    private static final double FLAT_FEE = 50.0;

    private final int numberOfPages;

    public Document(String trackingNumber, String receiverName, double weight, int numberOfPages)
            throws InvalidParcelException {
        super(trackingNumber, receiverName, weight);
        validateWeight(weight, MAX_WEIGHT, "Document");
        this.numberOfPages = validatePages(numberOfPages);
    }

    public static int validatePages(int pages) throws InvalidParcelException {
        if (pages < MIN_PAGES || pages > MAX_PAGES) {
            throw new InvalidParcelException(
                    "Number of pages must be from " + MIN_PAGES + " to " + MAX_PAGES + ".");
        }
        return pages;
    }

    public int getNumberOfPages() { return numberOfPages; }

    @Override
    public String getTypeName() {
        return "Document";
    }

    @Override
    public Map<String, String> getTypeDetails() {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Pages", String.valueOf(numberOfPages));
        return details;
    }

    @Override
    public double getChargeableWeight() {
        return getWeight();
    }

    @Override
    public double getBaseFee() {
        return FLAT_FEE;
    }
}
