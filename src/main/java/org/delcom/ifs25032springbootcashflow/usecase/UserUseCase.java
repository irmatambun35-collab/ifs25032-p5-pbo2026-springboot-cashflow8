package org.delcom.ifs25032springbootcashflow.usecase;

import org.delcom.ifs25032springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25032springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25032springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import java.util.List;

public class UserUseCase {
    private final IUserRepository users;

    public UserUseCase(IUserRepository users) { this.users = users; }

    public List<User> list() { return users.findAll(); }

    public User get(int id) throws EntityNotFoundException {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    public User updateProfile(int id, String fullName, String newPassword) throws DomainException {
        User user = get(id);
        if (fullName == null || fullName.isBlank()) throw new ValidationException("Nama lengkap wajib diisi");
        if (newPassword != null && !newPassword.isEmpty() && newPassword.length() < 4) {
            throw new ValidationException("Password minimal 4 karakter");
        }
        user.setFullName(fullName.trim());
        if (newPassword != null && !newPassword.isEmpty()) user.setPasswordHash(PasswordHasher.hash(newPassword));
        return users.update(user);
    }

    public User updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        User user = get(id);
        user.setPhotoPath(photoPath);
        return users.update(user);
    }
}
