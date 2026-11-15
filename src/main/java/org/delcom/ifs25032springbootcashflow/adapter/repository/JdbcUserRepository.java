package org.delcom.ifs25032springbootcashflow.adapter.repository;

import org.delcom.ifs25032springbootcashflow.domain.entity.Role;
import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;

public class JdbcUserRepository implements IUserRepository {
    private static final String COLS = "id, username, password_hash, full_name, role, photo_path";
    private final DataSource ds;

    public JdbcUserRepository(DataSource ds) { this.ds = ds; }

    private static User map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"),
                rs.getString("full_name"), Role.valueOf(rs.getString("role")), rs.getString("photo_path"));
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        int id = Jdbc.insert(ds,
                "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)",
                ps -> { ps.setString(1, username); ps.setString(2, passwordHash); ps.setString(3, fullName);
                        ps.setString(4, role.name()); });
        return new User(id, username, passwordHash, fullName, role, null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Jdbc.query(ds, "SELECT " + COLS + " FROM users WHERE LOWER(username) = LOWER(?)",
                ps -> ps.setString(1, username), JdbcUserRepository::map).stream().findFirst();
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Jdbc.query(ds, "SELECT " + COLS + " FROM users WHERE id = ?",
                ps -> ps.setInt(1, id), JdbcUserRepository::map).stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return Jdbc.query(ds, "SELECT " + COLS + " FROM users ORDER BY id", ps -> {}, JdbcUserRepository::map);
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) { return findAll().stream().filter(predicate).toList(); }

    @Override
    public User update(User u) {
        Jdbc.update(ds, "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?",
                ps -> { ps.setString(1, u.getPasswordHash()); ps.setString(2, u.getFullName());
                        ps.setString(3, u.getPhotoPath()); ps.setInt(4, u.getId()); });
        return u;
    }

    @Override
    public boolean deleteById(Integer id) {
        return Jdbc.update(ds, "DELETE FROM users WHERE id = ?", ps -> ps.setInt(1, id)) > 0;
    }
}
