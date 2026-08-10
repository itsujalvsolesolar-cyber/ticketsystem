package com.sujal.itsm.itams.enums;

public enum TransactionType {
    STOCK_IN("Stock In"),
    STOCK_OUT("Stock Out"),
    TRANSFER("Transfer"),
    ADJUSTMENT("Adjustment"),
    DAMAGED("Damaged"),
    REPAIR("Repair"),
    SCRAP("Scrap");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}