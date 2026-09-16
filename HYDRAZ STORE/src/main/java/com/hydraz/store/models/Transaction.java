package com.hydraz.store.models;

public class Transaction {
    private int id;
    private String ign;
    private String cartItems;
    private String status;
    private String screenshotPath;
    private double amount;

    public Transaction(int id, String ign, String cartItems, String status, String screenshotPath, double amount) {
        this.id = id;
        this.ign = ign;
        this.cartItems = cartItems;
        this.status = status;
        this.screenshotPath = screenshotPath;
        this.amount = amount;
    }

    public Transaction(int id, String ign, String cartItems, String status, String screenshotPath) {
        this(id, ign, cartItems, status, screenshotPath, 0.0);
    }

    public int getId() {
        return this.id;
    }

    public String getIgn() {
        return this.ign;
    }

    public String getCartItems() {
        return this.cartItems;
    }

    public String getStatus() {
        return this.status;
    }

    public String getScreenshotPath() {
        return this.screenshotPath;
    }

    public double getAmount() {
        return this.amount;
    }
}

