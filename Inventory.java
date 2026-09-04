import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


interface ItemRepository {

    boolean isEmpty();

    int size();

    /** @throws ItemNotFoundException if no item with this ID exists. */
    Item findById(String id);

    boolean idExists(String id);

    /** @throws DuplicateItemException if the item's ID is already in use. */
    void addItem(Item item);

    /**
     * Removes and returns the item with the given ID.
     * @throws ItemNotFoundException if no item with this ID exists.
     */
    Item removeItem(String id);

    List<Item> getItemsByCategory(Category category);

    List<Item> getAllItems();

    List<Item> getLowStockItems();

    /** @throws InsufficientItemsException if fewer than 2 items are stored. */
    List<Item> getSortedItems(String sortBy, boolean ascending);
}

public class Inventory implements ItemRepository {

    private final List<Item> items = new ArrayList<>();

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Item findById(String id) {
        for (Item item : items) {
            if (item.getId().equalsIgnoreCase(id)) {
                return item;
            }
        }
        throw new ItemNotFoundException();
    }

    @Override
    public boolean idExists(String id) {
        for (Item item : items) {
            if (item.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addItem(Item item) {
        if (idExists(item.getId())) {
            throw new DuplicateItemException(item.getId());
        }
        items.add(item);
    }

    @Override
    public Item removeItem(String id) {
        Item item = findById(id); // throws ItemNotFoundException if missing
        items.remove(item);
        return item;
    }

    @Override
    public List<Item> getItemsByCategory(Category category) {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    @Override
    public List<Item> getLowStockItems() {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getQuantity() <= 5) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public List<Item> getSortedItems(String sortBy, boolean ascending) {
        if (items.size() < 2) {
            throw new InsufficientItemsException("Not enough items to sort (minimum of 2 required).");
        }
        List<Item> sorted = new ArrayList<>(items);
        Comparator<Item> comparator = sortBy.equalsIgnoreCase("quantity")
                ? Comparator.comparingInt(Item::getQuantity)
                : Comparator.comparingDouble(Item::getPrice);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        sorted.sort(comparator);
        return sorted;
    }
}