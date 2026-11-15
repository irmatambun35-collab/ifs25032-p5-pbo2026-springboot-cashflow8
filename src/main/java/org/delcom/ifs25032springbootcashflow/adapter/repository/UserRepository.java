package org.delcom.ifs25032springbootcashflow.adapter.repository;

import org.delcom.ifs25032springbootcashflow.domain.entity.Role;
import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import java.util.Optional;

/** Implementasi in-memory untuk unit test. */
public class UserRepository extends InMemoryRepository<User> implements IUserRepository {
    @Override protected int idOf(User u) { return u.getId(); }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        User u = new User(nextId(), username, passwordHash, fullName, role, null);
        store.put(u.getId(), u);
        return u;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }
}
