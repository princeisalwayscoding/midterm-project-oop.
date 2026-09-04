/**
 * Thrown when a raw input fails format or bounds validation
 * (e.g. empty input, wrong number format, out-of-range value).
 */
class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}

/** Thrown by the repository when a lookup by ID fails. */
class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
        super("Item not found!");
    }
}

/** Thrown when attempting to add an item whose ID already exists. */
class DuplicateItemException extends RuntimeException {
    public DuplicateItemException(String id) {
        super("Item ID '" + id + "' already exists! Please use a unique ID.");
    }
}

/** Thrown when a raw category string does not match a valid category. */
class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String rawInput) {
        super("Category " + rawInput + " does not exist!");
    }
}

/** Thrown when a sort is requested but fewer than 2 items exist. */
class InsufficientItemsException extends RuntimeException {
    public InsufficientItemsException(String message) {
        super(message);
    }
}

/**
 * Pure validation/parsing logic - takes a raw String, returns a parsed
 * value, or throws a custom exception describing exactly what was wrong.
 * Nothing in this class touches System.out or Scanner: it has no idea
 * it's being used by a console app, which means it could just as easily
 * back a GUI later on.
 *
 * Numeric parsing leans on Java's own Integer/Double parsing + try-catch
 * for bounds/overflow handling. The one thing try-catch alone cannot catch
 * is an invalid leading zero (Integer.parseInt("01") happily returns 1),
 * so a small manual character scan handles just that one shape check -
 * no regex needed anywhere in this class.
 */
public class InputValidator {

    public static final int MIN_QUANTITY = 0;
    public static final int MAX_QUANTITY = 100_000;
    public static final double MIN_PRICE_EXCLUSIVE = 0.00;
    public static final double MAX_PRICE = 1_000_000.00;

    private static final int ID_MIN_LEN = 3;
    private static final int ID_MAX_LEN = 10;
    private static final int NAME_MAX_LEN = 50;

    public static String requireNonEmpty(String raw, String fieldName) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty. Please try again.");
        }
        return raw.trim();
    }

    /** Alphanumeric only, 3-10 characters. */
    public static String parseId(String raw) {
        String input = requireNonEmpty(raw, "ID");
        if (input.length() < ID_MIN_LEN || input.length() > ID_MAX_LEN) {
            throw new InvalidInputException("Invalid ID! Must be " + ID_MIN_LEN + "-" + ID_MAX_LEN + " characters long.");
        }
        for (char c : input.toCharArray()) {
            if (!Character.isLetterOrDigit(c) || c > 127) {
                throw new InvalidInputException("Invalid ID! Must contain letters and digits only.");
            }
        }
        return input;
    }

    /** Printable characters only, max 50 characters. */
    public static String parseName(String raw) {
        String input = requireNonEmpty(raw, "Name");
        if (input.length() > NAME_MAX_LEN) {
            throw new InvalidInputException("Invalid Name! Maximum length is " + NAME_MAX_LEN + " characters.");
        }
        for (char c : input.toCharArray()) {
            if (c < 0x20 || c > 0x7E) {
                throw new InvalidInputException("Invalid Name! Only printable characters are allowed.");
            }
        }
        return input;
    }

    /** Whole number, 0 - 100,000, no leading zeroes. */
    public static int parseQuantity(String raw) {
        String input = requireNonEmpty(raw, "Quantity");
        if (!isValidIntegerFormat(input)) {
            throw new InvalidInputException("Invalid Quantity! Must be a whole number with no leading zeroes, "
                    + "negative signs, decimals, letters, or symbols.");
        }
        try {
            long value = Long.parseLong(input);
            if (value < MIN_QUANTITY || value > MAX_QUANTITY) {
                throw new InvalidInputException("Invalid Quantity! Must be between " + MIN_QUANTITY
                        + " and " + MAX_QUANTITY + ".");
            }
            return (int) value;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid Quantity! Number is too large.");
        }
    }

    /** Decimal, strictly greater than 0.00, up to 1,000,000.00, no leading zeroes. */
    public static double parsePrice(String raw) {
        String input = requireNonEmpty(raw, "Price");
        if (!isValidDecimalFormat(input)) {
            throw new InvalidInputException("Invalid Price! Must be a valid number with no leading zeroes, "
                    + "negative signs, scientific notation, letters, or symbols.");
        }
        try {
            double value = Double.parseDouble(input);
            if (value <= MIN_PRICE_EXCLUSIVE || value > MAX_PRICE) {
                throw new InvalidInputException("Invalid Price! Must be greater than " + MIN_PRICE_EXCLUSIVE
                        + " and up to " + MAX_PRICE + ".");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid Price! Number is too large.");
        }
    }

    /** @throws CategoryNotFoundException if the raw text isn't one of the 3 valid categories. */
    public static Category parseCategory(String raw) {
        String input = requireNonEmpty(raw, "Category");
        Category category = Category.fromStringOrNull(input);
        if (category == null) {
            throw new CategoryNotFoundException(input);
        }
        return category;
    }

    public static int parseMenuChoice(String raw, int min, int max) {
        String input = requireNonEmpty(raw, "Choice");
        if (!isValidIntegerFormat(input)) {
            throw new InvalidInputException("Invalid choice! Please enter a whole number between " + min + " and " + max + ".");
        }
        try {
            int value = Integer.parseInt(input);
            if (value < min || value > max) {
                throw new InvalidInputException("Invalid choice! Please enter a number between " + min + " and " + max + ".");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid choice! Number is too large.");
        }
    }

    public static boolean parseConfirmation(String raw) {
        String input = requireNonEmpty(raw, "Confirmation").toLowerCase();
        if (input.equals("y") || input.equals("yes")) return true;
        if (input.equals("n") || input.equals("no")) return false;
        throw new InvalidInputException("Please answer with 'y' or 'n'.");
    }

    // ---------------------------------------------------------------
    // Manual format checks (no regex)
    // ---------------------------------------------------------------

    /** True for "0" or any digit string with no leading zero (e.g. "0", "7", "450"). */
    private static boolean isValidIntegerFormat(String s) {
        if (s.isEmpty()) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return !(s.length() > 1 && s.charAt(0) == '0');
    }

    /** Whole numbers (via isValidIntegerFormat) or "digits.digits" with a valid integer part. */
    private static boolean isValidDecimalFormat(String s) {
        if (s.isEmpty()) return false;
        int dotIndex = s.indexOf('.');
        if (dotIndex == -1) {
            return isValidIntegerFormat(s);
        }
        if (s.indexOf('.', dotIndex + 1) != -1) return false; // more than one dot
        String integerPart = s.substring(0, dotIndex);
        String fractionalPart = s.substring(dotIndex + 1);
        if (fractionalPart.isEmpty() || !isValidIntegerFormat(integerPart)) return false;
        for (char c : fractionalPart.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}