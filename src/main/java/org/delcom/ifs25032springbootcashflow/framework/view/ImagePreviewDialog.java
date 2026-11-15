package org.delcom.ifs25032springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

/** Modal preview: klik gambar toggle 1.0x / 1.8x; tombol Perkecil, Perbesar, Tutup di kanan footer. */
public class ImagePreviewDialog extends ModalDialog {
    private static final double MIN = 1.0, MAX = 1.8;
    private final Image image;
    private double zoom = MIN;

    public ImagePreviewDialog(String src) {
        super("Bukti Transaksi", "Preview");
        setWidth("720px");
        image = new Image(src, "Bukti transaksi");
        image.addClassName("zoomable-image");
        image.addClickListener(e -> setZoom(zoom == MIN ? MAX : MIN));
        Div box = new Div(image);
        box.addClassName("image-scroll");
        add(box);
        Button out = new Button("Perkecil", e -> setZoom(MIN));
        Button in = new Button("Perbesar", e -> setZoom(MAX));
        Button close = new Button("Tutup", e -> close());
        addActions(out, in, close);
        setZoom(MIN);
    }

    private void setZoom(double z) {
        zoom = z;
        image.getStyle().set("width", (int) Math.round(z * 100) + "%");
    }
}
