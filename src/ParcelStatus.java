/**
 * A parcel is either still waiting to be delivered or already delivered.
 */
public enum ParcelStatus {
    PENDING("Pending"),
    DELIVERED("Delivered");

    private final String label;

    ParcelStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
