package org.delcom.ifs25032springbootcashflow.framework.view;

import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public final class UploadSupport {
    private UploadSupport() {}

    /** Menampung file hasil upload sementara sebelum disimpan lewat FileStorage. */
    public static class FileHolder {
        private volatile String name;
        private volatile byte[] data;

        void set(String name, byte[] data) { this.name = name; this.data = data; }
        void clear() { this.name = null; this.data = null; }
        public boolean hasFile() { return name != null && data != null; }
        public String getName() { return name; }
        public InputStream stream() { return new ByteArrayInputStream(data); }
    }

    public static Upload create(FileHolder holder) {
        Upload upload = new Upload(UploadHandler.inMemory((meta, bytes) -> holder.set(meta.fileName(), bytes)));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.addFileRemovedListener(e -> holder.clear());
        return upload;
    }
}
