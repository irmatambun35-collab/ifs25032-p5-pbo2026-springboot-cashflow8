package org.delcom.ifs25032springbootcashflow.adapter.storage;

import org.delcom.ifs25032springbootcashflow.domain.exception.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class FileStorage {
    private static final Set<String> ALLOWED = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp");
    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path root() { return root; }

    /** Menyimpan file dengan nama UUID + ekstensi; mengembalikan path relatif URL, mis. /uploads/abc.png. */
    public String save(InputStream in, String originalName) throws ValidationException {
        String name = originalName == null ? "" : originalName.toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        String ext = dot < 0 ? "" : name.substring(dot);
        if (!ALLOWED.contains(ext)) throw new ValidationException("File harus berupa gambar (png, jpg, jpeg, gif, webp)");
        String stored = UUID.randomUUID() + ext;
        try {
            Files.copy(in, root.resolve(stored));
        } catch (IOException e) {
            throw new ValidationException("Gagal menyimpan file");
        }
        return "/uploads/" + stored;
    }
}
