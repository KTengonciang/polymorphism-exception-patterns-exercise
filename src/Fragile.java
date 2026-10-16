import java.util.Map;

/**
 * FRAGILE (extends BOX)
 * - Everything a Box has, plus a handling note
 * - Max weight: 30 kg
 * - Chargeable weight: same rule as Box (inherited, not rewritten)
 * - Base fee = Box base fee + Php 100 handling fee
 */
public class Fragile extends Box {

    public static final double MAX_WEIGHT = 30.0;
    private static final double HANDLING_FEE = 100.0;

    private final String handlingNote;

    public Fragile(String trackingNumber, String receiverName, double weight,
                   int length, int width, int height, String handlingNote)
            throws InvalidParcelException {
        super(trackingNumber, receiverName, weight, length, width, height);
        validateWeight(weight, MAX_WEIGHT, "Fragile"); // stricter than Box's 50 kg
        this.handlingNote = validateHandlingNote(handlingNote);
    }

    public static String validateHandlingNote(String input) throws InvalidParcelException {
        String value = (input == null) ? "" : input.trim();
        if (value.isEmpty()) {
            throw new InvalidParcelException("Handling note cannot be empty.");
        }
        return value;
    }

    public String getHandlingNote() { return handlingNote; }

    @Override
    public String getTypeName() {
        return "Fragile";
    }

    @Override
    public Map<String, String> getTypeDetails() {
        Map<String, String> details = super.getTypeDetails(); // keeps Box's dimensions
        details.put("Handling Note", handlingNote);
        return details;
    }

    @Override
    public double getBaseFee() {
        return super.getBaseFee() + HANDLING_FEE;
    }
}
