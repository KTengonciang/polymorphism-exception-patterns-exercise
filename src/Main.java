import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Console menu for the Parcel Delivery System.
 *
 * This class only handles input and output. The rules (fees, weight limits,
 * Same Day restriction, duplicate checks) live in the Parcel classes, the
 * DeliveryService strategies, and ParcelManager.
 *
 * Any prompt accepts "cancel" to go back to the main menu.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final ParcelManager manager = ParcelManager.getInstance();

    private static final String CANCEL = "cancel";
    // Up to 2 decimal places, like a real parcel scale: 2, 1.5, 0.25, .5
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^(\\d+(\\.\\d{1,2})?|\\.\\d{1,2})$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[+-]?\\d{1,9}$");

    /** Thrown internally when the user types "cancel"; caught at the main menu. */
    private static class CancelledException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        CancelledException() {
            super("Cancelled.");
        }
    }

    public static void main(String[] args) {
        printTitle();

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("[ Main Menu ]");
            System.out.println();
            System.out.println("1 - Add Parcel");
            System.out.println("2 - View All Parcels");
            System.out.println("3 - Change Delivery Service");
            System.out.println("4 - Mark as Delivered");
            System.out.println("5 - Exit");
            System.out.println();

            int choice = readMainMenuChoice();
            System.out.println();
            printDivider();

            try {
                switch (choice) {
                    case 1: addParcel(); break;
                    case 2: viewAllParcels(); break;
                    case 3: changeDeliveryService(); break;
                    case 4: markAsDelivered(); break;
                    case 5:
                        running = false;
                        System.out.println("Thank you for using the Parcel Delivery System. Goodbye!");
                        printDivider();
                        break;
                }
            } catch (CancelledException e) {
                System.out.println();
                System.out.println("Cancelled. Returning to the main menu.");
            }
        }
    }

    // =====================================================================
    // 1. ADD PARCEL
    // =====================================================================

    private static void addParcel() {
        printSectionHeader("Add Parcel");
        System.out.println("(Type 'cancel' at any prompt to go back)");
        System.out.println();

        System.out.println("Parcel Type:");
        System.out.println("1 - Document");
        System.out.println("2 - Box");
        System.out.println("3 - Fragile");
        int type = readChoice("Choice: ", 1, 3);
        System.out.println();

        String trackingNumber = readTrackingNumber();
        String receiverName = readReceiverName();

        Parcel parcel;
        try {
            switch (type) {
                case 1: {
                    double weight = readWeight(Document.MAX_WEIGHT, "Document");
                    int pages = readPages();
                    parcel = new Document(trackingNumber, receiverName, weight, pages);
                    break;
                }
                case 2: {
                    double weight = readWeight(Box.MAX_WEIGHT, "Box");
                    int[] dims = readDimensions();
                    parcel = new Box(trackingNumber, receiverName, weight, dims[0], dims[1], dims[2]);
                    break;
                }
                default: {
                    double weight = readWeight(Fragile.MAX_WEIGHT, "Fragile");
                    int[] dims = readDimensions();
                    String note = readHandlingNote();
                    parcel = new Fragile(trackingNumber, receiverName, weight,
                            dims[0], dims[1], dims[2], note);
                    break;
                }
            }
        } catch (InvalidParcelException e) {
            // Every field was already checked as it was typed, so this is only a safety net.
            System.out.println(e.getMessage());
            return;
        }

        // Pick a service; if Same Day isn't allowed, show the message and pick again.
        System.out.println();
        while (true) {
            DeliveryService service = readDeliveryService();
            try {
                parcel.setDeliveryService(service);
                break;
            } catch (ServiceNotAllowedException e) {
                System.out.println(e.getMessage());
                System.out.println();
            }
        }

        System.out.println();
        System.out.println("Tracking No.:      " + parcel.getTrackingNumber());
        System.out.println("Type:              " + parcel.getTypeName());
        System.out.println("Receiver:          " + parcel.getReceiverName());
        for (Map.Entry<String, String> detail : parcel.getTypeDetails().entrySet()) {
            System.out.printf("%-19s%s%n", detail.getKey() + ":", detail.getValue());
        }
        System.out.println("Chargeable Weight: " + formatWeight(parcel.getChargeableWeight()));
        System.out.println("Delivery Service:  " + parcel.getDeliveryService().getName());
        System.out.println("Delivery Fee:      " + formatPeso(parcel.getDeliveryFee()));
        if (!readYesNo("Add this parcel? (Y/N): ")) {
            System.out.println();
            System.out.println("Parcel not added.");
            return;
        }

        try {
            manager.addParcel(parcel);
        } catch (DuplicateTrackingNumberException e) {
            // Also checked at the tracking number prompt; kept as a safety net.
            System.out.println(e.getMessage());
            return;
        }

        System.out.println();
        System.out.println("Parcel added successfully! Delivery fee: " + formatPeso(parcel.getDeliveryFee()));
    }

    // =====================================================================
    // 2. VIEW ALL PARCELS
    // =====================================================================

    private static void viewAllParcels() {
        printSectionHeader("View All Parcels");
        List<Parcel> parcels = manager.getAllParcels();
        if (parcels.isEmpty()) {
            System.out.println("No parcels have been added yet.");
            return;
        }

        String format = "%-12s %-9s %-20s %12s  %-9s %17s  %-9s%n";
        System.out.printf(format, "Tracking No.", "Type", "Receiver", "Chg. Weight",
                "Service", "Delivery Fee", "Status");
        System.out.println("-".repeat(98));
        for (Parcel p : parcels) {
            System.out.printf(format,
                    p.getTrackingNumber(),
                    p.getTypeName(),
                    shorten(p.getReceiverName(), 20),
                    formatWeight(p.getChargeableWeight()),
                    p.getDeliveryService().getName(),
                    formatPeso(p.getDeliveryFee()),
                    p.getStatus().getLabel());
        }
        System.out.println("-".repeat(98));
        long delivered = parcels.stream().filter(Parcel::isDelivered).count();
        System.out.println("Total parcels: " + parcels.size()
                + " (" + (parcels.size() - delivered) + " Pending, " + delivered + " Delivered)");
    }

    // =====================================================================
    // 3. CHANGE DELIVERY SERVICE
    // =====================================================================

    private static void changeDeliveryService() {
        printSectionHeader("Change Delivery Service");
        if (manager.getAllParcels().isEmpty()) {
            System.out.println("No parcels have been added yet.");
            return;
        }
        System.out.println("(Type 'cancel' at any prompt to go back)");
        System.out.println();

        Parcel parcel;
        while (true) {
            String trackingNumber = readLine("Enter Tracking Number: ");
            try {
                parcel = manager.findPendingParcel(trackingNumber);
                break;
            } catch (ParcelNotFoundException e) {
                System.out.println("Parcel not found!");
                System.out.println();
            } catch (ParcelAlreadyDeliveredException e) {
                System.out.println("Parcel already delivered!");
                return;
            }
        }

        System.out.println();
        System.out.println("Current service: " + parcel.getDeliveryService().getName()
                + " (" + formatPeso(parcel.getDeliveryFee()) + ")");
        System.out.println();
        DeliveryService newService = readDeliveryService();

        if (newService.getName().equals(parcel.getDeliveryService().getName())) {
            System.out.println();
            System.out.println("Parcel already uses " + newService.getName() + ".");
            return;
        }

        // Not allowed: show the message and keep the old service. No need to confirm.
        if (!newService.isAllowedFor(parcel)) {
            System.out.println();
            System.out.println(newService.getRestrictionMessage());
            return;
        }

        System.out.println();
        double newFee = newService.calculateFee(parcel.getBaseFee());
        if (!readYesNo("Change to " + newService.getName() + " for " + formatPeso(newFee) + "? (Y/N): ")) {
            System.out.println();
            System.out.println("Delivery service not changed.");
            return;
        }

        try {
            manager.changeDeliveryService(parcel.getTrackingNumber(), newService);
            System.out.println();
            System.out.println("Delivery service changed! New fee: " + formatPeso(parcel.getDeliveryFee()));
        } catch (ServiceNotAllowedException e) {
            // The parcel keeps its old service.
            System.out.println();
            System.out.println(e.getMessage());
        } catch (ParcelNotFoundException e) {
            System.out.println("Parcel not found!");
        } catch (ParcelAlreadyDeliveredException e) {
            System.out.println("Parcel already delivered!");
        }
    }

    // =====================================================================
    // 4. MARK AS DELIVERED
    // =====================================================================

    private static void markAsDelivered() {
        printSectionHeader("Mark as Delivered");
        if (manager.getAllParcels().isEmpty()) {
            System.out.println("No parcels have been added yet.");
            return;
        }
        System.out.println("(Type 'cancel' at any prompt to go back)");
        System.out.println();

        Parcel parcel;
        while (true) {
            String trackingNumber = readLine("Enter Tracking Number: ");
            try {
                parcel = manager.findPendingParcel(trackingNumber);
                break;
            } catch (ParcelNotFoundException e) {
                System.out.println("Parcel not found!");
                System.out.println();
            } catch (ParcelAlreadyDeliveredException e) {
                System.out.println("Parcel was already delivered.");
                return;
            }
        }

        System.out.println();
        System.out.println(parcel.getTypeName() + " for " + parcel.getReceiverName()
                + " (" + parcel.getDeliveryService().getName() + ")");
        if (!readYesNo("Mark this parcel as delivered? (Y/N): ")) {
            System.out.println();
            System.out.println("Parcel was not marked as delivered.");
            return;
        }

        try {
            manager.markDelivered(parcel.getTrackingNumber());
            System.out.println();
            System.out.println("Parcel delivered successfully!");
        } catch (ParcelNotFoundException e) {
            System.out.println("Parcel not found!");
        } catch (ParcelAlreadyDeliveredException e) {
            System.out.println("Parcel was already delivered.");
        }
    }

    // =====================================================================
    // INPUT HELPERS (each one keeps asking until the input is valid)
    // =====================================================================

    private static String readTrackingNumber() {
        while (true) {
            String input = readLine("Enter Tracking Number: ");
            try {
                String trackingNumber = Parcel.validateTrackingNumber(input);
                if (manager.isTrackingNumberTaken(trackingNumber)) {
                    System.out.println("Tracking number " + trackingNumber + " already exists!");
                    continue;
                }
                return trackingNumber;
            } catch (InvalidParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String readReceiverName() {
        while (true) {
            String input = readLine("Enter Receiver Name: ");
            try {
                return Parcel.validateReceiverName(input);
            } catch (InvalidParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static double readWeight(double maxWeight, String typeName) {
        while (true) {
            String input = readLine("Enter Weight (kg): ");
            if (!DECIMAL_PATTERN.matcher(input).matches()) {
                System.out.println("Weight must be a number with up to 2 decimal places (e.g. 1.5).");
                continue;
            }
            try {
                return Parcel.validateWeight(Double.parseDouble(input), maxWeight, typeName);
            } catch (InvalidParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static int readPages() {
        while (true) {
            int pages = readInt("Enter Number of Pages: ");
            try {
                return Document.validatePages(pages);
            } catch (InvalidParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static int[] readDimensions() {
        return new int[] {
            readDimension("Enter Length (cm): ", "Length"),
            readDimension("Enter Width (cm): ", "Width"),
            readDimension("Enter Height (cm): ", "Height")
        };
    }

    private static int readDimension(String prompt, String label) {
        while (true) {
            int value = readInt(prompt);
            try {
                return Box.validateDimension(value, label);
            } catch (InvalidParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String readHandlingNote() {
        while (true) {
            String input = readLine("Enter Handling Note: ");
            try {
                return Fragile.validateHandlingNote(input);
            } catch (InvalidParcelException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static DeliveryService readDeliveryService() {
        System.out.println("Delivery Service:");
        System.out.println("1 - Standard");
        System.out.println("2 - Express");
        System.out.println("3 - Same Day");
        int choice = readChoice("Choice: ", 1, 3);
        switch (choice) {
            case 1: return new StandardService();
            case 2: return new ExpressService();
            default: return new SameDayService();
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt).toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("Please enter Y or N.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (INTEGER_PATTERN.matcher(input).matches()) {
                return Integer.parseInt(input);
            }
            System.out.println("Please enter a whole number.");
        }
    }

    private static int readChoice(String prompt, int min, int max) {
        while (true) {
            String input = readLine(prompt);
            if (INTEGER_PATTERN.matcher(input).matches()) {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            }
            System.out.println("Invalid choice. Please enter a number from " + min + " to " + max + ".");
        }
    }

    /** Main menu choice: same as readChoice, but 'cancel' has nothing to go back to here. */
    private static int readMainMenuChoice() {
        while (true) {
            String input = readRawLine("Choice: ");
            if (INTEGER_PATTERN.matcher(input).matches()) {
                int value = Integer.parseInt(input);
                if (value >= 1 && value <= 5) {
                    return value;
                }
            }
            System.out.println("Invalid choice. Please enter a number from 1 to 5.");
        }
    }

    /** Reads a line; typing 'cancel' backs out to the main menu. */
    private static String readLine(String prompt) {
        String input = readRawLine(prompt);
        if (input.equalsIgnoreCase(CANCEL)) {
            throw new CancelledException();
        }
        return input;
    }

    private static String readRawLine(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) {
            // Input stream closed (e.g. Ctrl+D / Ctrl+Z); exit cleanly instead of crashing.
            System.out.println();
            System.out.println("No more input. Exiting.");
            System.exit(0);
        }
        return sc.nextLine().trim();
    }

    // =====================================================================
    // DISPLAY HELPERS
    // =====================================================================

    private static String formatPeso(double amount) {
        return String.format("Php %,.2f", amount);
    }

    /** Box weights are always whole kg ("3 kg"); document weights show as entered ("0.35 kg"). */
    private static String formatWeight(double kg) {
        if (kg == Math.floor(kg)) {
            return String.format("%d kg", (long) kg);
        }
        return new java.math.BigDecimal(String.valueOf(kg)).stripTrailingZeros().toPlainString() + " kg";
    }

    private static String shorten(String text, int maxLength) {
        return (text.length() <= maxLength) ? text : text.substring(0, maxLength - 3) + "...";
    }

    private static void printTitle() {
        printDivider();
        System.out.println("                        PARCEL DELIVERY SYSTEM");
        printDivider();
    }

    private static void printSectionHeader(String title) {
        System.out.println();
        System.out.println("-- " + title + " --");
    }

    private static void printDivider() {
        System.out.println("=".repeat(72));
    }
}
