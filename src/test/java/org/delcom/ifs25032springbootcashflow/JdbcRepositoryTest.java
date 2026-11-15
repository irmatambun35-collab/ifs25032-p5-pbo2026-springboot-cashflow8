package org.delcom.ifs25032springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25032springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25032springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25032springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25032springbootcashflow.domain.entity.*;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcRepositoryTest {
    private JdbcUserRepository users;
    private JdbcTransactionRepository txs;

    @BeforeEach
    void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        SchemaInitializer.initialize(ds);
        SchemaInitializer.initialize(ds); // idempoten
        users = new JdbcUserRepository(ds);
        txs = new JdbcTransactionRepository(ds);
    }

    @Test
    void userCrud() {
        User u = users.save("Budi", "hash", "Budi S", Role.USER);
        assertTrue(u.getId() > 0);
        assertEquals("Budi S", users.findByUsername("budi").orElseThrow().getFullName());
        assertTrue(users.findByUsername("zzz").isEmpty());
        assertEquals(Role.USER, users.findById(u.getId()).orElseThrow().getRole());
        assertTrue(users.findById(999).isEmpty());

        u.setFullName("Baru"); u.setPasswordHash("h2"); u.setPhotoPath("/uploads/a.png");
        users.update(u);
        User r = users.findById(u.getId()).orElseThrow();
        assertEquals("Baru", r.getFullName());
        assertEquals("h2", r.getPasswordHash());
        assertEquals("/uploads/a.png", r.getPhotoPath());

        users.save("admin", "h", "Admin", Role.ADMIN);
        assertEquals(2, users.findAll().size());
        assertEquals(1, users.findBy(x -> x.getRole() == Role.ADMIN).size());
        assertTrue(users.deleteById(u.getId()));
        assertFalse(users.deleteById(u.getId()));
    }

    @Test
    void transactionCrud() {
        User u = users.save("budi", "h", "Budi", Role.USER);
        Transaction t = txs.save(u.getId(), "Gaji", 5_000_000L, TransactionType.INCOME, "/uploads/a.png");
        txs.save(u.getId(), "Makan", 25_000L, TransactionType.EXPENSE, null);

        Transaction r = txs.findById(t.getId()).orElseThrow();
        assertEquals("Gaji", r.getDescription());
        assertEquals(5_000_000L, r.getAmount());
        assertEquals(TransactionType.INCOME, r.getType());
        assertEquals("/uploads/a.png", r.getProofPath());
        assertEquals(t.getCreatedAt(), r.getCreatedAt());
        assertTrue(txs.findById(999).isEmpty());
        assertEquals(2, txs.findByUserId(u.getId()).size());
        assertTrue(txs.findByUserId(999).isEmpty());
        assertEquals(2, txs.findAll().size());
        assertEquals(1, txs.findBy(x -> x.getAmount() > 100_000).size());

        r.setDescription("Gaji+"); r.setAmount(6); r.setType(TransactionType.EXPENSE); r.setProofPath(null);
        txs.update(r);
        Transaction u2 = txs.findById(t.getId()).orElseThrow();
        assertEquals("Gaji+", u2.getDescription());
        assertNull(u2.getProofPath());

        assertTrue(txs.deleteById(t.getId()));
        assertFalse(txs.deleteById(t.getId()));
    }

    @Test
    void sqlErrorsAreWrapped() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1"); // tanpa skema
        JdbcUserRepository bad = new JdbcUserRepository(ds);
        JdbcTransactionRepository badTx = new JdbcTransactionRepository(ds);
        assertThrows(IllegalStateException.class, bad::findAll);              // query
        assertThrows(IllegalStateException.class, () -> bad.deleteById(1));   // update
        assertThrows(IllegalStateException.class, () -> badTx.save(1, "x", 1, TransactionType.INCOME, null)); // insert
        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(brokenDataSource()));
    }

    private static DataSource brokenDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:x;INIT=RUNSCRIPT FROM 'tidak-ada.sql'");
        return ds;
    }
}
