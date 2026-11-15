package org.delcom.ifs25032springbootcashflow.domain.entity;

public enum TransactionType {
    INCOME("Pemasukan"), EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) { this.label = label; }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
