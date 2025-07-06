package com.example.tedxpaymnetserver;

public class TransactionData {

    public String refNo;
    private final String amount;
    private final String timeReceived;
    private final String serverHolder;
    private final String bankName;

    public TransactionData(String refNo, String amount, String timeReceived, String serverHolder, String bankName) {
        this.refNo = refNo;
        this.amount = amount;
        this.timeReceived = timeReceived;
        this.serverHolder = serverHolder;
        this.bankName = bankName;
    }
}
