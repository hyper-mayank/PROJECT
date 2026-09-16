package com.hydraz.store.models;

public class PendingDelivery {
    private final int id;
    private final int transactionId;
    private final String ign;
    private final String serverTarget;
    private final String command;

    public PendingDelivery(int id, int transactionId, String ign, String serverTarget, String command) {
        this.id = id;
        this.transactionId = transactionId;
        this.ign = ign;
        this.serverTarget = serverTarget;
        this.command = command;
    }

    public int getId() {
        return id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public String getIgn() {
        return ign;
    }

    public String getServerTarget() {
        return serverTarget;
    }

    public String getCommand() {
        return command;
    }
}
