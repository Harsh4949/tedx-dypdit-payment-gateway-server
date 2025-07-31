package com.example.tedxpaymnetserver;

public class TransactionData {

    public String refNo;
    private final String amount;
    private final String timeReceived;
    private final String serverHolder;
    private final String bankName;
    private String status;

    public TransactionData(String refNo, String amount, String timeReceived, String serverHolder, String bankName) {
        this.refNo = refNo;
        this.amount = amount;
        this.timeReceived = timeReceived;
        this.serverHolder = serverHolder;
        this.bankName = bankName;
        status="Pending";

    }

    public String getRefNo() {
        return this.refNo;
    }

    public String getAmount() {
        return this.amount;
    }

    public String getTimestamp() {
        return this.timeReceived;
    }

    public String getServerHolder() {
        return this.serverHolder;
    }

    public String getBankName() {
        return this.bankName;
    }

    public String getStatus() {
        return this.status;
    }


    public void setStatus(String success) {

        this.status=success;
    }
}
