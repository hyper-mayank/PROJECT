package com.hydraz.store.data;

public class Product {
    private String id;
    private String name;
    private double price;
    private double originalPrice;
    private String image;
    private String serverTarget;
    private String[] commands;

    public Product(String id, String name, double price, double originalPrice, String image, String serverTarget, String... commands) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.image = image;
        this.serverTarget = serverTarget;
        this.commands = commands;
    }

    public Product(String id, String name, double price, String image, String serverTarget, String... commands) {
        this(id, name, price, price, image, serverTarget, commands);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getOriginalPrice() { return originalPrice; }
    public String getImage() { return image; }
    public String getServerTarget() { return serverTarget; }
    public String[] getCommands() { return commands; }
}
