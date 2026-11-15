package org.delcom.ifs25032springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25032springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25032springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25032springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25032springbootcashflow.domain.entity.*;
import org.delcom.ifs25032springbootcashflow.domain.exception.*;
import org.delcom.ifs25032springbootcashflow.usecase.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UseCaseTest {
    private UserRepository users;
    private TransactionRepository txRepo;
    private AuthUseCase auth;
    private UserUseCase userUc;
    private FinanceUseCase finance;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        txRepo = new TransactionRepository();
        auth = new AuthUseCase(users);
        userUc = new UserUseCase(users);
        finance = new FinanceUseCase(txRepo);
    }

    // ---------- Auth ----------
    @Test
    void registerAndLogin() throws Exception {
        User u = auth.register("  budi ", "pass1", " Budi S ");
        assertEquals("budi", u.getUsername());
        assertEquals("Budi S", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertEquals(u.getId(), auth.login("BUDI", "pass1").getId());
    }

    @Test
    void registerValidation() throws Exception {
        auth.register("budi", "pass1", "Budi");
        assertThrows(ValidationException.class, () -> auth.register(null, "pass1", "X"));
        assertThrows(ValidationException.class, () -> auth.register("ab", "pass1", "X"));
        assertThrows(ValidationException.class, () -> auth.register("abc", null, "X"));
        assertThrows(ValidationException.class, () -> auth.register("abc", "123", "X"));
        assertThrows(ValidationException.class, () -> auth.register("abc", "pass1", null));
        assertThrows(ValidationException.class, () -> auth.register("abc", "pass1", " "));
        assertThrows(ValidationException.class, () -> auth.register("budi", "pass1", "Dup"));
    }

    @Test
    void loginFailures() throws Exception {
        auth.register("budi", "pass1", "Budi");
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));
        assertThrows(ValidationException.class, () -> auth.login("budi", null));
        assertThrows(ValidationException.class, () -> auth.login("tidak-ada", "pass1"));
        assertThrows(ValidationException.class, () -> auth.login(null, "pass1"));
    }

    @Test
    void ensureRootAdmin() throws Exception {
        User a = auth.ensureRootAdmin("admin", "admin");
        assertEquals(Role.ADMIN, a.getRole());
        assertEquals(a.getId(), auth.ensureRootAdmin("admin", "lain").getId());
        assertEquals(1, users.findAll().size());
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin(null, "x"));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin(" ", "x"));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("x", null));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("x", " "));
    }

    // ---------- User ----------
    @Test
    void userUseCase() throws Exception {
        User u = auth.register("budi", "pass1", "Budi");
        assertEquals(1, userUc.list().size());
        assertEquals("Budi", userUc.get(u.getId()).getFullName());
        assertThrows(EntityNotFoundException.class, () -> userUc.get(99));

        userUc.updateProfile(u.getId(), " Budi Baru ", null);
        assertEquals("Budi Baru", users.findById(u.getId()).orElseThrow().getFullName());
        assertTrue(PasswordHasher.verify("pass1", users.findById(u.getId()).orElseThrow().getPasswordHash()));
        userUc.updateProfile(u.getId(), "Budi Baru", "");
        assertTrue(PasswordHasher.verify("pass1", users.findById(u.getId()).orElseThrow().getPasswordHash()));
        userUc.updateProfile(u.getId(), "Budi Baru", "baru123");
        assertTrue(PasswordHasher.verify("baru123", users.findById(u.getId()).orElseThrow().getPasswordHash()));

        assertThrows(ValidationException.class, () -> userUc.updateProfile(u.getId(), null, null));
        assertThrows(ValidationException.class, () -> userUc.updateProfile(u.getId(), " ", null));
        assertThrows(ValidationException.class, () -> userUc.updateProfile(u.getId(), "X", "123"));
        assertThrows(EntityNotFoundException.class, () -> userUc.updateProfile(99, "X", null));

        assertEquals("/uploads/f.png", userUc.updatePhoto(u.getId(), "/uploads/f.png").getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> userUc.updatePhoto(99, "/x"));
    }

    // ---------- Finance ----------
    @Test
    void addValidation() throws Exception {
        assertThrows(ValidationException.class, () -> finance.add(1, null, "10", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.add(1, " ", "10", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.add(1, "x", "10", null, null));
        assertThrows(ValidationException.class, () -> finance.add(1, "x", null, TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.add(1, "x", " ", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.add(1, "x", "abc", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.add(1, "x", "0", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.add(1, "x", "-5", TransactionType.INCOME, null));
        assertEquals(15L, FinanceUseCase.parseAmount(" 15 "));
    }

    @Test
    void addListSearchSortSummary() throws Exception {
        finance.add(1, "Gaji", "5000000", TransactionType.INCOME, null);
        finance.add(1, "Makan siang", "25000", TransactionType.EXPENSE, "/uploads/a.png");
        finance.add(1, "Bonus", "100000", TransactionType.INCOME, null);
        finance.add(2, "Punya orang lain", "1", TransactionType.INCOME, null);

        assertEquals(3, finance.list(1, null, null).size());
        assertEquals(1, finance.list(1, "  MAKAN ", SortOption.NEWEST).size());
        assertEquals(3, finance.list(1, "", SortOption.NEWEST).size());
        assertEquals("Makan siang", finance.list(1, null, SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals("Gaji", finance.list(1, null, SortOption.AMOUNT_DESC).get(0).getDescription());

        FinanceUseCase.Summary s = finance.summary(1);
        assertEquals(3, s.count());
        assertEquals(5_100_000, s.income());
        assertEquals(25_000, s.expense());
        assertEquals(5_075_000, s.balance());
        assertEquals(5_075_000, finance.getBalance(1));
        assertEquals(0, finance.summary(3).balance());
    }

    @Test
    void updateAndDelete() throws Exception {
        Transaction t = finance.add(1, "Gaji", "100", TransactionType.INCOME, "/uploads/a.png");

        finance.update(1, t.getId(), " Gaji Baru ", "200", TransactionType.EXPENSE, null);
        Transaction u = txRepo.findById(t.getId()).orElseThrow();
        assertEquals("Gaji Baru", u.getDescription());
        assertEquals(200, u.getAmount());
        assertEquals(TransactionType.EXPENSE, u.getType());
        assertEquals("/uploads/a.png", u.getProofPath());
        finance.update(1, t.getId(), "Gaji Baru", "200", TransactionType.EXPENSE, "/uploads/b.png");
        assertEquals("/uploads/b.png", txRepo.findById(t.getId()).orElseThrow().getProofPath());

        assertThrows(ValidationException.class, () -> finance.update(1, t.getId(), " ", "1", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.update(1, 0, "x", "1", TransactionType.INCOME, null));
        assertThrows(EntityNotFoundException.class, () -> finance.update(1, 99, "x", "1", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.update(2, t.getId(), "x", "1", TransactionType.INCOME, null));

        assertThrows(ValidationException.class, () -> finance.delete(2, t.getId()));
        assertThrows(ValidationException.class, () -> finance.delete(1, -1));
        assertThrows(EntityNotFoundException.class, () -> finance.delete(1, 99));
        finance.delete(1, t.getId());
        assertTrue(finance.list(1, null, null).isEmpty());
    }

    @Test
    void inMemoryRepositories() {
        assertTrue(users.findBy(u -> true).isEmpty());
        assertFalse(users.deleteById(1));
        User u = users.save("x", "h", "X", Role.USER);
        assertEquals(List.of(u), users.findBy(e -> e.getId() == u.getId()));
        assertTrue(users.deleteById(u.getId()));
        assertTrue(users.findByUsername("x").isEmpty());
    }
}
