package org.delcom.ifs25032springbootcashflow.usecase;

import org.delcom.ifs25032springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25032springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25032springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25032springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25032springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25032springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25032springbootcashflow.domain.repository.ITransactionRepository;
import java.util.List;

public class FinanceUseCase {
    public record Summary(int count, long income, long expense) {
        public long balance() { return income - expense; }
    }

    private final ITransactionRepository transactions;

    public FinanceUseCase(ITransactionRepository transactions) { this.transactions = transactions; }

    public static long parseAmount(String text) throws ValidationException {
        if (text == null || text.isBlank()) throw new ValidationException("Jumlah wajib diisi");
        long value;
        try {
            value = Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat");
        }
        if (value <= 0) throw new ValidationException("Jumlah harus lebih dari 0");
        return value;
    }

    private static void validate(String description, TransactionType type) throws ValidationException {
        if (description == null || description.isBlank()) throw new ValidationException("Keterangan wajib diisi");
        if (type == null) throw new ValidationException("Tipe transaksi wajib dipilih");
    }

    public Transaction add(int userId, String description, String amountText, TransactionType type, String proofPath)
            throws ValidationException {
        validate(description, type);
        long amount = parseAmount(amountText);
        return transactions.save(userId, description.trim(), amount, type, proofPath);
    }

    public List<Transaction> list(int userId, String keyword, SortOption sort) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        SortOption s = sort == null ? SortOption.NEWEST : sort;
        return transactions.findByUserId(userId).stream()
                .filter(t -> k.isEmpty() || t.getDescription().toLowerCase().contains(k))
                .sorted(s.getComparator())
                .toList();
    }

    private Transaction owned(int userId, int id) throws DomainException {
        if (id <= 0) throw new ValidationException("ID tidak valid");
        Transaction t = transactions.findById(id).orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (t.getUserId() != userId) throw new ValidationException("Transaksi bukan milik Anda");
        return t;
    }

    /** proofPath null = bukti lama dipertahankan. */
    public Transaction update(int userId, int id, String description, String amountText, TransactionType type,
                              String proofPath) throws DomainException {
        Transaction t = owned(userId, id);
        validate(description, type);
        t.setAmount(parseAmount(amountText));
        t.setDescription(description.trim());
        t.setType(type);
        if (proofPath != null) t.setProofPath(proofPath);
        return transactions.update(t);
    }

    public void delete(int userId, int id) throws DomainException {
        owned(userId, id);
        transactions.deleteById(id);
    }

    public Summary summary(int userId) {
        List<Transaction> all = transactions.findByUserId(userId);
        long in = all.stream().filter(t -> t.getType() == TransactionType.INCOME).mapToLong(Transaction::getAmount).sum();
        long out = all.stream().filter(t -> t.getType() == TransactionType.EXPENSE).mapToLong(Transaction::getAmount).sum();
        return new Summary(all.size(), in, out);
    }

    public long getBalance(int userId) { return summary(userId).balance(); }
}
