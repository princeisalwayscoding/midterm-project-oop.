/**
 * The three valid store categories. Item holds one of these instead of
 * needing a separate subclass per category.
 */
enum Category {
    CLOTHING("Clothing"),
    ELECTRONICS("Electronics"),
    ENTERTAINMENT("Entertainment");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Case-insensitive lookup. Returns null (rather than throwing) so the
     * caller (InputValidator) can decide how to report an unknown category.
     */
    public static Category fromStringOrNull(String input) {
        for (Category category : values()) {
            if (category.displayName.equalsIgnoreCase(input)) {
                return category;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

/**
 * Represents a single inventory item.
 *
 * ENCAPSULATION: fields are private and only reachable through getters and
 * the few setters that are actually needed (name/quantity/price can be
 * updated; id and category cannot).
 */
public class Item {

    private final String id;
    private String name;
    private int quantity;
    private double price;
    private final Category category;

    public Item(String id, String name, int quantity, double price, Category category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    /** Table row WITHOUT the category column (used under a category heading). */
    public String toTableRow() {
        return String.format("%-10s %-25s %-10d %-12.2f", id, name, quantity, price);
    }

    /** Table row WITH the category column. */
    public String toTableRowWithCategory() {
        return String.format("%-10s %-25s %-10d %-12.2f %-15s", id, name, quantity, price, category.getDisplayName());
    }

    @Override
    public String toString() {
        return "ID: " + id + "\nName: " + name + "\nQuantity: " + quantity
                + "\nPrice: " + String.format("%.2f", price) + "\nCategory: " + category.getDisplayName();
    }
}