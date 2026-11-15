package org.delcom.ifs25032springbootcashflow.usecase;

import org.delcom.ifs25032springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25032springbootcashflow.domain.entity.Role;
import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;

public class AuthUseCase {
    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) { this.users = users; }

    public User register(String username, String password, String fullName) throws ValidationException {
        String u = username == null ? "" : username.trim();
        if (u.length() < 3) throw new ValidationException("Username minimal 3 karakter");
        if (password == null || password.length() < 4) throw new ValidationException("Password minimal 4 karakter");
        if (fullName == null || fullName.isBlank()) throw new ValidationException("Nama lengkap wajib diisi");
        if (users.findByUsername(u).isPresent()) throw new ValidationException("Username sudah dipakai");
        return users.save(u, PasswordHasher.hash(password), fullName.trim(), Role.USER);
    }

    public User login(String username, String password) throws ValidationException {
        String u = username == null ? "" : username.trim();
        User user = users.findByUsername(u).orElse(null);
        if (user == null || password == null || !PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    public User ensureRootAdmin(String username, String password) throws ValidationException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ValidationException("ROOT_USERNAME dan ROOT_PASSWORD wajib diisi");
        }
        return users.findByUsername(username.trim())
                .orElseGet(() -> users.save(username.trim(), PasswordHasher.hash(password), "Administrator", Role.ADMIN));
    }
}
