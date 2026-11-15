package org.delcom.ifs25032springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25032springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25032springbootcashflow.domain.entity.*;
import org.delcom.ifs25032springbootcashflow.domain.exception.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainTest {
    private static Transaction tx(int id, long amount, TransactionType type, int minute) {
        return new Transaction(id, 1, "t" + id, amount, type, null, LocalDateTime.of(2026, 1, 1, 10, minute));
    }

    @Test
    void passwordHasher() {
        String h = PasswordHasher.hash("rahasia");
        assertEquals(64, h.length());
        assertTrue(PasswordHasher.verify("rahasia", h));
        assertFalse(PasswordHasher.verify("salah", h));
        assertFalse(PasswordHasher.verify("rahasia", null));
        assertThrows(IllegalStateException.class, () -> PasswordHasher.hashWith("x", "ALGO-TIDAK-ADA"));
    }

    @Test
    void entitiesAndEnums() {
        User u = new User(1, "a", "h", "A", Role.USER, null);
        u.setFullName("B"); u.setPasswordHash("x"); u.setPhotoPath("/uploads/p.png");
        assertEquals("B", u.getFullName());
        assertEquals("x", u.getPasswordHash());
        assertEquals("/uploads/p.png", u.getPhotoPath());
        assertEquals(1, u.getId());
        assertEquals("a", u.getUsername());
        assertEquals(Role.USER, u.getRole());
        assertEquals(2, Role.values().length);

        Transaction t = tx(1, 10, TransactionType.INCOME, 1);
        t.setDescription("d"); t.setAmount(5); t.setType(TransactionType.EXPENSE); t.setProofPath("/uploads/x.png");
        assertEquals("d", t.getDescription());
        assertEquals(5, t.getAmount());
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals("/uploads/x.png", t.getProofPath());
        assertEquals(1, t.getUserId());
        assertNotNull(t.getCreatedAt());
        assertEquals("Pemasukan", TransactionType.INCOME.getLabel());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.toString());
    }

    @Test
    void sortOptions() {
        Transaction a = tx(1, 300, TransactionType.EXPENSE, 1);
        Transaction b = tx(2, 100, TransactionType.INCOME, 2);
        Transaction c = tx(3, 200, TransactionType.INCOME, 3);
        assertEquals(5, SortOption.values().length);
        assertEquals(List.of(3, 2, 1), ids(sorted(SortOption.NEWEST, a, b, c)));
        assertEquals(List.of(2, 3, 1), ids(sorted(SortOption.AMOUNT_ASC, a, b, c)));
        assertEquals(List.of(1, 3, 2), ids(sorted(SortOption.AMOUNT_DESC, a, b, c)));
        assertEquals(List.of(2, 3, 1), ids(sorted(SortOption.INCOME_FIRST, a, b, c)));
        assertEquals(List.of(1, 2, 3), ids(sorted(SortOption.EXPENSE_FIRST, a, b, c)));
        assertEquals("Terbaru", SortOption.NEWEST.getLabel());
        assertEquals("Terbaru", SortOption.NEWEST.toString());
    }

    private static List<Transaction> sorted(SortOption o, Transaction... ts) {
        List<Transaction> l = new ArrayList<>(List.of(ts));
        l.sort(o.getComparator());
        return l;
    }

    private static List<Integer> ids(List<Transaction> l) { return l.stream().map(Transaction::getId).toList(); }

    @Test
    void exceptions() {
        assertEquals("Transaksi dengan ID 7 tidak ditemukan", new EntityNotFoundException("Transaksi", 7).getMessage());
        assertInstanceOf(DomainException.class, new ValidationException("x"));
    }
}
