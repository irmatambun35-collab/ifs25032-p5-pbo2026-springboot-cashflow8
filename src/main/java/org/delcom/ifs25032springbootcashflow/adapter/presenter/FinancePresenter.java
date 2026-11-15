package org.delcom.ifs25032springbootcashflow.adapter.presenter;

import org.delcom.ifs25032springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25032springbootcashflow.domain.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FinancePresenter {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

    public String money(long amount) {
        return "Rp " + String.format(Locale.US, "%,.2f", BigDecimal.valueOf(amount));
    }

    public String typeLabel(TransactionType type) { return type.getLabel(); }

    public String line(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription() + " | " + money(t.getAmount())
                + " | [" + typeLabel(t.getType()) + "]";
    }

    public String balanceLine(long balance) { return "Saldo: " + money(balance); }

    public String dateTime(LocalDateTime time) { return time.format(DATE); }

    public String added(Transaction t) { return "Transaksi berhasil ditambahkan: " + line(t); }

    public String deleted(int id) { return "Transaksi [" + id + "] berhasil dihapus"; }

    public String error(Exception e) { return "Gagal: " + e.getMessage(); }
}
