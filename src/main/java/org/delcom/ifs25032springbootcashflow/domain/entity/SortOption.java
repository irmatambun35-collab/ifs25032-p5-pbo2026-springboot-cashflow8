package org.delcom.ifs25032springbootcashflow.domain.entity;

import java.util.Comparator;

public enum SortOption {
    NEWEST("Terbaru", Comparator.comparing(Transaction::getCreatedAt).reversed()),
    AMOUNT_ASC("Jumlah naik", Comparator.comparingLong(Transaction::getAmount)),
    AMOUNT_DESC("Jumlah turun", Comparator.comparingLong(Transaction::getAmount).reversed()),
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparing((Transaction t) -> t.getType() != TransactionType.INCOME)
                    .thenComparing(Transaction::getId)),
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparing((Transaction t) -> t.getType() != TransactionType.EXPENSE)
                    .thenComparing(Transaction::getId));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() { return label; }
    public Comparator<Transaction> getComparator() { return comparator; }

    @Override
    public String toString() { return label; }
}
