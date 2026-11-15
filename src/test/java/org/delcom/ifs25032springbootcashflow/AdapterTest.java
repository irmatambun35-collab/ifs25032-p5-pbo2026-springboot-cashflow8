package org.delcom.ifs25032springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25032springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25032springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25032springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25032springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25032springbootcashflow.domain.exception.ValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AdapterTest {
    private final FinancePresenter p = new FinancePresenter();

    @Test
    void presenterFormats() {
        Transaction t = new Transaction(3, 1, "Gaji", 1_000_000, TransactionType.INCOME, null,
                LocalDateTime.of(2026, 3, 5, 9, 7));
        assertEquals("Rp 1,000,000.00", p.money(1_000_000));
        assertEquals("Rp 0.00", p.money(0));
        assertEquals("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]", p.line(t));
        assertEquals("Saldo: Rp 4,975,000.00", p.balanceLine(4_975_000));
        assertEquals("05 Mar 2026 09:07", p.dateTime(t.getCreatedAt()));
        assertEquals("Pengeluaran", p.typeLabel(TransactionType.EXPENSE));
        assertTrue(p.added(t).startsWith("Transaksi berhasil ditambahkan: [3]"));
        assertEquals("Transaksi [3] berhasil dihapus", p.deleted(3));
        assertEquals("Gagal: oops", p.error(new Exception("oops")));
    }

    @Test
    void fileStorageSavesAllowedImages(@TempDir Path dir) throws Exception {
        FileStorage fs = new FileStorage(dir.resolve("up").toString());
        String url = fs.save(new ByteArrayInputStream(new byte[] {1, 2, 3}), "Foto.PNG");
        assertTrue(url.startsWith("/uploads/") && url.endsWith(".png"));
        assertTrue(Files.exists(fs.root().resolve(url.substring("/uploads/".length()))));
    }

    @Test
    void fileStorageRejectsBadInput(@TempDir Path dir) {
        FileStorage fs = new FileStorage(dir.toString());
        InputStream in = new ByteArrayInputStream(new byte[] {1});
        assertThrows(ValidationException.class, () -> fs.save(in, "virus.exe"));
        assertThrows(ValidationException.class, () -> fs.save(in, "tanpa-ekstensi"));
        assertThrows(ValidationException.class, () -> fs.save(in, null));
        InputStream broken = new InputStream() {
            @Override public int read() throws IOException { throw new IOException("rusak"); }
        };
        assertThrows(ValidationException.class, () -> fs.save(broken, "a.jpg"));
    }

    @Test
    void fileStorageCannotCreateRoot(@TempDir Path dir) throws Exception {
        Path file = Files.writeString(dir.resolve("bukan-folder"), "x");
        assertThrows(UncheckedIOException.class, () -> new FileStorage(file.resolve("sub").toString()));
    }
}
