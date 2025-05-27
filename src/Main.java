import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

interface Searchable {
    String getSearchTerm();
    String getContentType();
    String getName();

    default String getStringRepresentation() {
        return getName() + " — " + getContentType();
    }
}

final class Article implements Searchable {
    private final String title;
    private final String text;

    public Article(String title, String text) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Название статьи не может быть пустым");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Текст статьи не может быть пустым");
        this.title = title;
        this.text = text;
    }

    @Override public String getSearchTerm() { return (title + " " + text).toLowerCase(Locale.ROOT); }
    @Override public String getContentType() { return "ARTICLE"; }
    @Override public String getName() { return title; }
    @Override public String toString() { return title + "\n" + text; }
}

abstract class Product implements Searchable {
    private final String name;

    public Product(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Название продукта не может быть пустым");
        this.name = name;
    }

    @Override public String getSearchTerm() { return name.toLowerCase(Locale.ROOT); }
    @Override public String getContentType() { return "PRODUCT"; }
    @Override public String getName() { return name; }
    public abstract double getPrice();
    public abstract boolean isSpecial();
    @Override public abstract String toString();
}

class SimpleProduct extends Product {
    private final double price;

    public SimpleProduct(String name, double price) {
        super(name);
        if (price <= 0) throw new IllegalArgumentException("Цена должна быть больше 0: " + price);
        this.price = price;
    }

    @Override public double getPrice() { return price; }
    @Override public boolean isSpecial() { return false; }
    @Override public String toString() { return String.format(Locale.US, "%s: %.2f", getName(), getPrice()); }
}

class DiscountedProduct extends Product {
    private final double basePrice;
    private final int discount;

    public DiscountedProduct(String name, double basePrice, int discount) {
        super(name);
        if (basePrice <= 0) throw new IllegalArgumentException("Базовая цена должна быть больше 0: " + basePrice);
        if (discount < 0 || discount > 100) throw new IllegalArgumentException("Скидка должна быть от 0 до 100%: " + discount);
        this.basePrice = basePrice;
        this.discount = discount;
    }

    @Override public double getPrice() { return basePrice * (100 - discount) / 100; }
    @Override public boolean isSpecial() { return true; }
    @Override public String toString() { return String.format(Locale.US, "%s: %.2f (%d%% скидка)", getName(), getPrice(), discount); }
}

class FixPriceProduct extends Product {
    private static final double FIXED_PRICE = 99.99;

    public FixPriceProduct(String name) { super(name); }
    @Override public double getPrice() { return FIXED_PRICE; }
    @Override public boolean isSpecial() { return true; }
    @Override public String toString() { return String.format("%s: Фиксированная цена %.2f", getName(), FIXED_PRICE); }
}

class ProductBasket {
    private final List<Product> products = new ArrayList<>();

    public void addProduct(Product product) { products.add(product); }

    public List<Product> removeProductsByName(String name) {
        List<Product> removed = new ArrayList<>();
        Iterator<Product> iterator = products.iterator();
        while (iterator.hasNext()) {
            Product p = iterator.next();
            if (p.getName().equalsIgnoreCase(name)) {
                removed.add(p);
                iterator.remove();
            }
        }
        return removed;
    }

    public void printBasket() {
        double total = 0;
        int specialCount = 0;
        for (Product p : products) {
            System.out.println(p);
            total += p.getPrice();
            if (p.isSpecial()) specialCount++;
        }
        System.out.printf(Locale.US, "Итого: %.2f%nСпециальных товаров: %d%n", total, specialCount);
    }
}

class SearchEngine {
    private final List<Searchable> items = new ArrayList<>();

    public void add(Searchable item) {
        if (item == null) throw new IllegalArgumentException("Элемент не может быть null");
        items.add(item);
    }

    public List<Searchable> search(String query) {
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        List<Searchable> results = new ArrayList<>();
        for (Searchable item : items) {
            if (item.getSearchTerm().contains(lowerQuery)) results.add(item);
        }
        return results;
    }

    public Searchable findBestMatch(String query) throws Exception {
        if (query == null || query.isEmpty()) throw new IllegalArgumentException("Пустой запрос");
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        Searchable best = null;
        int maxCount = 0;
        for (Searchable item : items) {
            int count = countOccurrences(item.getSearchTerm(), lowerQuery);
            if (count > maxCount || (count == maxCount && best == null)) {
                maxCount = count;
                best = item;
            }
        }
        if (best == null) throw new Exception("Не найдено: " + query);
        return best;
    }

    private int countOccurrences(String text, String substr) {
        int count = 0, index = 0;
        while ((index = text.indexOf(substr, index)) != -1) {
            count++;
            index += substr.length();
        }
        return count;
    }
}

public class Main {
    public static void main(String[] args) {
        ProductBasket basket = new ProductBasket();
        SearchEngine engine = new SearchEngine();

        // Добавление товаров
        basket.addProduct(new SimpleProduct("Хлеб", 30.0));
        basket.addProduct(new DiscountedProduct("Молоко", 50.0, 20));
        basket.addProduct(new FixPriceProduct("Книга"));
        basket.addProduct(new SimpleProduct("Хлеб", 25.0));

        // Добавление в поисковик
        engine.add(new Article("Выпечка", "Рецепты хлеба"));
        engine.add(new DiscountedProduct("Молоко", 50.0, 20));

        // Демонстрация корзины
        System.out.println("=== Исходная корзина ===");
        basket.printBasket();

        // Удаление товаров
        System.out.println("\n=== Удаление 'Хлеб' ===");
        List<Product> removed = basket.removeProductsByName("Хлеб");
        removed.forEach(System.out::println);

        System.out.println("\n=== Корзина после удаления ===");
        basket.printBasket();

        // Поиск
        System.out.println("\n=== Результаты поиска 'хлеб' ===");
        engine.search("хлеб").forEach(item ->
                System.out.println(item.getStringRepresentation()));
    }
}