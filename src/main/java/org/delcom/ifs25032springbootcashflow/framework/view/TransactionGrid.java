package org.delcom.ifs25032springbootcashflow.framework.view;

import org.delcom.ifs25032springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25032springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25032springbootcashflow.domain.entity.TransactionType;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.Comparator;
import java.util.function.Consumer;

/** Grid transaksi yang dipakai FinanceView (dengan hapus) dan UserTransactionsView (read-only). */
public final class TransactionGrid {
    private TransactionGrid() {}

    public static Grid<Transaction> create(FinancePresenter presenter, Consumer<Transaction> onDelete) {
        Grid<Transaction> grid = new Grid<>();
        GridStyles.apply(grid);
        grid.addColumn(Transaction::getId).setHeader("ID").setSortable(true).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true).setFlexGrow(2);
        grid.addColumn(t -> presenter.money(t.getAmount())).setHeader("Jumlah")
                .setComparator(Comparator.comparingLong(Transaction::getAmount)).setAutoWidth(true);
        grid.addComponentColumn(t -> {
            Span s = new Span(presenter.typeLabel(t.getType()));
            s.getElement().getThemeList().add(t.getType() == TransactionType.INCOME ? "badge success" : "badge error");
            return s;
        }).setHeader("Tipe").setComparator(Comparator.comparing(Transaction::getType)).setAutoWidth(true);
        grid.addComponentColumn(t -> {
            if (t.getProofPath() == null) return new Span("-");
            Image img = new Image(t.getProofPath(), "Bukti");
            img.setWidth("40px");
            img.setHeight("40px");
            img.addClassName("proof-thumb");
            img.addClickListener(e -> new ImagePreviewDialog(t.getProofPath()).open());
            return img;
        }).setHeader("Bukti").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(t -> presenter.dateTime(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt)).setAutoWidth(true);
        if (onDelete != null) {
            grid.addComponentColumn(t -> {
                Button b = new Button(VaadinIcon.TRASH.create(), e -> onDelete.accept(t));
                b.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                b.setAriaLabel("Hapus");
                return b;
            }).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);
        }
        return grid;
    }
}
