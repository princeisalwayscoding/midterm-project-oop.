import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;


public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ItemRepository inventory = new Inventory();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readMenuChoice("Enter choice: ", 1, 9);
            System.out.println();

            switch (choice) {
                case 1 -> addItem();
                case 2 -> updateItem();
                case 3 -> removeItem();
                case 4 -> displayItemsByCategory();
                case 5 -> displayAllItems();
                case 6 -> searchItem();
                case 7 -> sortItems();
                case 8 -> displayLowStockItems();
                case 9 -> {
                    System.out.println("Exiting the Inventory Management System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("[Error] Unexpected choice.");
            }
            System.out.println();
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("========================================");
        System.out.println("Menu");
        System.out.println("1 - Add Item");
        System.out.println("2 - Update Item");
        System.out.println("3 - Remove Item");
        System.out.println("4 - Display Items by Category");
        System.out.println("5 - Display All Items");
        System.out.println("6 - Search Item");
        System.out.println("7 - Sort Items");
        System.out.println("8 - Display Low Stock Items");
        System.out.println("9 - Exit");
        System.out.println("========================================");
    }

    // ---------------------------------------------------------------
    // 1. Add Item
    // ---------------------------------------------------------------
    private static void addItem() {
        Category category = readValidCategory("Enter Category (Clothing/Electronics/Entertainment): ");

        String id = readValidId("Enter Item ID (alphanumeric, 3-10 chars): ");
        if (inventory.idExists(id)) {
            System.out.println(new DuplicateItemException(id).getMessage());
            return;
        }

        String name = readValidName("Enter Item Name: ");
        int quantity = readValidQuantity("Enter Quantity: ");
        double price = readValidPrice("Enter Price: ");

        try {
            inventory.addItem(new Item(id, name, quantity, price, category));
        } catch (DuplicateItemException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("Item added successfully!");
    }

    // ---------------------------------------------------------------
    // 2. Update Item
    // ---------------------------------------------------------------
    private static void updateItem() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. Nothing to update.");
            return;
        }

        String id = readNonEmptyLine("Enter Item ID to update: ");
        Item item;
        try {
            item = inventory.findById(id);
        } catch (ItemNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }

        int fieldChoice = readMenuChoice(
                "What do you want to update?\n1 - Quantity\n2 - Price\nEnter choice: ", 1, 2);

        if (fieldChoice == 1) {
            int newQuantity = readValidQuantity("Enter new Quantity: ");
            int oldQuantity = item.getQuantity();
            item.setQuantity(newQuantity);
            System.out.println("Quantity of Item " + item.getName() + " is updated from "
                    + oldQuantity + " to " + newQuantity);
        } else {
            double newPrice = readValidPrice("Enter new Price: ");
            double oldPrice = item.getPrice();
            item.setPrice(newPrice);
            System.out.println("Price of Item " + item.getName() + " is updated from "
                    + String.format("%.2f", oldPrice) + " to " + String.format("%.2f", newPrice));
        }
    }

    // ---------------------------------------------------------------
    // 3. Remove Item
    // ---------------------------------------------------------------
    private static void removeItem() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. Nothing to remove.");
            return;
        }

        String id = readNonEmptyLine("Enter Item ID to remove: ");
        Item item;
        try {
            item = inventory.findById(id);
        } catch (ItemNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }

        boolean confirmed = readConfirmation(
                "Are you sure you want to remove '" + item.getName() + "'? (y/n): ");
        if (!confirmed) {
            System.out.println("Removal cancelled.");
            return;
        }

        try {
            Item removed = inventory.removeItem(id);
            System.out.println("Item " + removed.getName() + " has been removed from the inventory");
        } catch (ItemNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 4. Display Items by Category
    // ---------------------------------------------------------------
    private static void displayItemsByCategory() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }

        Category category = readValidCategory("Enter Category (Clothing/Electronics/Entertainment): ");

        List<Item> categoryItems = inventory.getItemsByCategory(category);
        if (categoryItems.isEmpty()) {
            System.out.println("No items found in " + category.getDisplayName() + ".");
            return;
        }

        System.out.println("Items in category: " + category.getDisplayName());
        printTableHeader(false);
        for (Item item : categoryItems) {
            System.out.println(item.toTableRow());
        }
    }

    // ---------------------------------------------------------------
    // 5. Display All Items
    // ---------------------------------------------------------------
    private static void displayAllItems() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. No items to display.");
            return;
        }

        printTableHeader(true);
        for (Item item : inventory.getAllItems()) {
            System.out.println(item.toTableRowWithCategory());
        }
    }

    // ---------------------------------------------------------------
    // 6. Search Item
    // ---------------------------------------------------------------
    private static void searchItem() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. Nothing to search.");
            return;
        }

        String id = readNonEmptyLine("Enter Item ID to search: ");
        try {
            Item item = inventory.findById(id);
            System.out.println("Item found:");
            System.out.println(item);
        } catch (ItemNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 7. Sort Items
    // ---------------------------------------------------------------
    private static void sortItems() {
        if (inventory.size() < 2) {
            System.out.println("Not enough items to sort (minimum of 2 required).");
            return;
        }

        int sortByChoice = readMenuChoice("Sort by:\n1 - Quantity\n2 - Price\nEnter choice: ", 1, 2);
        String sortBy = (sortByChoice == 1) ? "quantity" : "price";

        int orderChoice = readMenuChoice("Order:\n1 - Ascending\n2 - Descending\nEnter choice: ", 1, 2);
        boolean ascending = (orderChoice == 1);

        List<Item> sorted;
        try {
            sorted = inventory.getSortedItems(sortBy, ascending);
        } catch (InsufficientItemsException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("Items sorted by " + sortBy + " (" + (ascending ? "Ascending" : "Descending") + "):");
        printTableHeader(true);
        for (Item item : sorted) {
            System.out.println(item.toTableRowWithCategory());
        }
    }

    // ---------------------------------------------------------------
    // 8. Display Low Stock Items
    // ---------------------------------------------------------------
    private static void displayLowStockItems() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty. No items to display.");
            return;
        }

        List<Item> lowStock = inventory.getLowStockItems();
        if (lowStock.isEmpty()) {
            System.out.println("No low stock items found (all items have quantity above 5).");
            return;
        }

        System.out.println("Low Stock Items (Quantity <= 5):");
        printTableHeader(true);
        for (Item item : lowStock) {
            System.out.println(item.toTableRowWithCategory());
        }    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static String readNonEmptyLine(String prompt) {
        return promptUntilValid(() -> InputValidator.requireNonEmpty(readLine(prompt), "Input"));
    }

    private static String readValidId(String prompt) {
        return promptUntilValid(() -> InputValidator.parseId(readLine(prompt)));
    }

    private static String readValidName(String prompt) {
        return promptUntilValid(() -> InputValidator.parseName(readLine(prompt)));
    }

    private static int readValidQuantity(String prompt) {
        return promptUntilValid(() -> InputValidator.parseQuantity(readLine(prompt)));
    }

    private static double readValidPrice(String prompt) {
        return promptUntilValid(() -> InputValidator.parsePrice(readLine(prompt)));
    }

    private static int readMenuChoice(String prompt, int min, int max) {
        return promptUntilValid(() -> InputValidator.parseMenuChoice(readLine(prompt), min, max));
    }

    private static boolean readConfirmation(String prompt) {
        return promptUntilValid(() -> InputValidator.parseConfirmation(readLine(prompt)));
    }

    private static Category readValidCategory(String prompt) {
        Category result = null;
        boolean valid = false;
        while (!valid) {
            try {
                result = InputValidator.parseCategory(readLine(prompt));
                valid = true;
            } catch (InvalidInputException e) {
                System.out.println("[Error] " + e.getMessage());
            } catch (CategoryNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
        return result;
    }

    private static <T> T promptUntilValid(Supplier<T> supplier) {
        T result = null;
        boolean valid = false;
        while (!valid) {
            try {
                result = supplier.get();
                valid = true;
            } catch (InvalidInputException e) {
                System.out.println("[Error] " + e.getMessage());
            }
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Table printing
    // ---------------------------------------------------------------
    private static void printTableHeader(boolean withCategory) {
        if (withCategory) {
            System.out.println(String.format("%-10s %-25s %-10s %-12s %-15s",
                    "ID", "Name", "Quantity", "Price", "Category"));
            System.out.println("-".repeat(75));
        } else {
            System.out.println(String.format("%-10s %-25s %-10s %-12s",
                    "ID", "Name", "Quantity", "Price"));
            System.out.println("-".repeat(60));
        }
    }
}