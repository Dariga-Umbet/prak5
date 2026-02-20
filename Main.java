import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class ConfigurationManager {

    private static volatile ConfigurationManager instance;
    private Map<String, String> settings;
    private ConfigurationManager() {
        settings = new HashMap<>();
    }
    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    public void set(String key, String value) {
        settings.put(key, value);
    }
    public String get(String key) {
        if (!settings.containsKey(key)) {
            throw new RuntimeException("Настройка не найдена: " + key);
        }
        return settings.get(key);
    }

    public void saveToFile(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            writer.write(entry.getKey() + "=" + entry.getValue());
            writer.newLine();
        }
        writer.close();
    }
    public void loadFromFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("Файл не найден");

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("=");
            settings.put(parts[0], parts[1]);
        }
        reader.close();
    }
}

class Report {
    String header;
    String content;
    String footer;

    public void show() {
        System.out.println(header);
        System.out.println(content);
        System.out.println(footer);
        System.out.println("--------------");
    }
}

interface IReportBuilder {
    void setHeader(String header);
    void setContent(String content);
    void setFooter(String footer);
    Report getReport();
}

class TextReportBuilder implements IReportBuilder {

    private Report report = new Report();
    public void setHeader(String header) {
        report.header = "TEXT HEADER: " + header;
    }
    public void setContent(String content) {
        report.content = content;
    }
    public void setFooter(String footer) {
        report.footer = "TEXT FOOTER: " + footer;
    }
    public Report getReport() {
        return report;
    }
}

class HtmlReportBuilder implements IReportBuilder {

    private Report report = new Report();
    public void setHeader(String header) {
        report.header = "<h1>" + header + "</h1>";
    }
    public void setContent(String content) {
        report.content = "<p>" + content + "</p>";
    }
    public void setFooter(String footer) {
        report.footer = "<footer>" + footer + "</footer>";
    }
    public Report getReport() {
        return report;
    }
}

class ReportDirector {
    public void constructReport(IReportBuilder builder) {
        builder.setHeader("Отчет 2026");
        builder.setContent("Продажи выросли на 20%");
        builder.setFooter("Конец отчета");
    }
}

class Product implements Cloneable {
    String name;
    double price;
    int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    public Product clone() {
        return new Product(name, price, quantity);
    }
}

class Discount implements Cloneable {
    String name;
    double value;

    public Discount(String name, double value) {
        this.name = name;
        this.value = value;
    }
    public Discount clone() {
        return new Discount(name, value);
    }
}

class Order implements Cloneable {
    List<Product> products = new ArrayList<>();
    List<Discount> discounts = new ArrayList<>();
    double deliveryCost;
    String paymentMethod;

    public Order clone() {
        Order copy = new Order();
        copy.deliveryCost = this.deliveryCost;
        copy.paymentMethod = this.paymentMethod;

        for (Product p : products) {
            copy.products.add(p.clone());
        }
        for (Discount d : discounts) {
            copy.discounts.add(d.clone());
        }
        return copy;
    }

    public void show() {
        System.out.println("Order:");
        for (Product p : products) {
            System.out.println(p.name + " " + p.price + " x" + p.quantity);
        }
        System.out.println("Delivery: " + deliveryCost);
        System.out.println("Payment: " + paymentMethod);
        System.out.println("-----------");
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        Runnable task = () -> {
            ConfigurationManager config = ConfigurationManager.getInstance();
            config.set("theme", "dark");
            System.out.println("HashCode: " + config.hashCode());
        };
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("\n=== BUILDER ===");
        ReportDirector director = new ReportDirector();

        IReportBuilder textBuilder = new TextReportBuilder();
        director.constructReport(textBuilder);
        textBuilder.getReport().show();

        IReportBuilder htmlBuilder = new HtmlReportBuilder();
        director.constructReport(htmlBuilder);
        htmlBuilder.getReport().show();

        System.out.println("\n=== PROTOTYPE ===");

        Order order1 = new Order();
        order1.products.add(new Product("Phone", 500, 1));
        order1.deliveryCost = 25;
        order1.paymentMethod = "Card";

        Order order2 = order1.clone();
        order2.products.get(0).name = "Laptop";

        order1.show();
        order2.show();
    }
}