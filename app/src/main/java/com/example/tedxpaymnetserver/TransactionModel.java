package com.example.tedxpaymnetserver;

public class TransactionModel {
    private final String refNo;
    private final String amount;
    private final String timestamp;

    public TransactionModel(String refNo, String amount, String timestamp) {
        this.refNo = refNo;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getRefNo() {
        return this.refNo;
    }

    public String getAmount() {
        return this.amount;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

}