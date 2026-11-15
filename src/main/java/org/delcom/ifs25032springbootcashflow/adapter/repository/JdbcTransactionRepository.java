package org.delcom.ifs25032springbootcashflow.adapter.repository;

import org.delcom.ifs25032springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25032springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25032springbootcashflow.domain.repository.ITransactionRepository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;

public class JdbcTransactionRepository implements ITransactionRepository {
    private static final String COLS = "id, user_id, description, amount, type, proof_path, created_at";
    private final DataSource ds;

    public JdbcTransactionRepository(DataSource ds) { this.ds = ds; }

    private static Transaction map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Transaction(rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                rs.getLong("amount"), TransactionType.valueOf(rs.getString("type")), rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int id = Jdbc.insert(ds,
                "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                ps -> { ps.setInt(1, userId); ps.setString(2, description); ps.setLong(3, amount);
                        ps.setString(4, type.name()); ps.setString(5, proofPath);
                        ps.setTimestamp(6, Timestamp.valueOf(now)); });
        return new Transaction(id, userId, description, amount, type, proofPath, now);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return Jdbc.query(ds, "SELECT " + COLS + " FROM transactions WHERE user_id = ? ORDER BY id",
                ps -> ps.setInt(1, userId), JdbcTransactionRepository::map);
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return Jdbc.query(ds, "SELECT " + COLS + " FROM transactions WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcTransactionRepository::map).stream().findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return Jdbc.query(ds, "SELECT " + COLS + " FROM transactions ORDER BY id", ps -> {},
                JdbcTransactionRepository::map);
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public Transaction update(Transaction t) {
        Jdbc.update(ds, "UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?",
                ps -> { ps.setString(1, t.getDescription()); ps.setLong(2, t.getAmount());
                        ps.setString(3, t.getType().name()); ps.setString(4, t.getProofPath());
                        ps.setInt(5, t.getId()); });
        return t;
    }

    @Override
    public boolean deleteById(Integer id) {
        return Jdbc.update(ds, "DELETE FROM transactions WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }
}
