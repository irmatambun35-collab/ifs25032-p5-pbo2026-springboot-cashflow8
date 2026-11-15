package org.delcom.ifs25032springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotEnvTest {
    @Test
    void loadsKeysAndSkipsCommentsAndExisting(@TempDir Path dir) throws Exception {
        Path f = dir.resolve(".env");
        Files.writeString(f, "# komentar\n\nDOTENV_A=1\nDOTENV_B=\"dua\"\nDOTENV_C='tiga'\nbaris-rusak\nDOTENV_D=4\nDOTENV_E=x\nDOTENV_F=\"abc\nPATH=diabaikan\n");
        System.setProperty("DOTENV_D", "asli");
        try {
            DotEnv.load(f);
            assertEquals("1", System.getProperty("DOTENV_A"));
            assertEquals("dua", System.getProperty("DOTENV_B"));
            assertEquals("tiga", System.getProperty("DOTENV_C"));
            assertEquals("asli", System.getProperty("DOTENV_D"));
            assertEquals("x", System.getProperty("DOTENV_E"));
            assertEquals("\"abc", System.getProperty("DOTENV_F"));
            assertNotEquals("diabaikan", System.getProperty("PATH"));
        } finally {
            for (String k : new String[] {"DOTENV_A", "DOTENV_B", "DOTENV_C", "DOTENV_D", "DOTENV_E", "DOTENV_F"}) System.clearProperty(k);
        }
    }

    @Test
    void unreadableFileThrows(@TempDir Path dir) {
        assertThrows(java.io.UncheckedIOException.class, () -> DotEnv.load(dir)); // direktori, bukan file
    }

    @Test
    void missingFileIsIgnored(@TempDir Path dir) {
        DotEnv.load(dir.resolve("tidak-ada.env"));
        DotEnv.load(); // .env di working dir: ada atau tidak, tidak boleh error
        assertNull(System.getProperty("DOTENV_X"));
    }
}
