import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton pattern: there is exactly one ParcelManager for the whole
 * program, so every menu option works on the same list of parcels.
 * The constructor is private; the only way in is ParcelManager.getInstance().
 */
public class ParcelManager {

    private static ParcelManager instance;

    private final List<Parcel> parcels = new ArrayList<>();

    private ParcelManager() {
        // private: nobody outside this class can create a second manager
    }

    public static ParcelManager getInstance() {
        if (instance == null) {
            instance = new ParcelManager();
        }
        return instance;
    }

    public boolean isTrackingNumberTaken(String trackingNumber) {
        for (Parcel p : parcels) {
            if (p.getTrackingNumber().equalsIgnoreCase(trackingNumber.trim())) {
                return true;
            }
        }
        return false;
    }

    public void addParcel(Parcel parcel) throws DuplicateTrackingNumberException {
        if (parcel.getDeliveryService() == null) {
            throw new IllegalStateException("A parcel needs a delivery service before it is added.");
        }
        if (isTrackingNumberTaken(parcel.getTrackingNumber())) {
            throw new DuplicateTrackingNumberException(parcel.getTrackingNumber());
        }
        parcels.add(parcel);
    }

    /** Read-only view so nothing outside can add or remove parcels behind the manager's back. */
    public List<Parcel> getAllParcels() {
        return Collections.unmodifiableList(parcels);
    }

    public Parcel findParcel(String trackingNumber) throws ParcelNotFoundException {
        String key = (trackingNumber == null) ? "" : trackingNumber.trim();
        for (Parcel p : parcels) {
            if (p.getTrackingNumber().equalsIgnoreCase(key)) {
                return p;
            }
        }
        throw new ParcelNotFoundException();
    }

    /** Finds a parcel that can still be changed (exists and is not yet delivered). */
    public Parcel findPendingParcel(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        if (parcel.isDelivered()) {
            throw new ParcelAlreadyDeliveredException();
        }
        return parcel;
    }

    /** Changes the service; if the new one isn't allowed, the parcel keeps its old service. */
    public Parcel changeDeliveryService(String trackingNumber, DeliveryService newService)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException, ServiceNotAllowedException {
        Parcel parcel = findPendingParcel(trackingNumber);
        parcel.setDeliveryService(newService);
        return parcel;
    }

    public Parcel markDelivered(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        parcel.markDelivered();
        return parcel;
    }
}
