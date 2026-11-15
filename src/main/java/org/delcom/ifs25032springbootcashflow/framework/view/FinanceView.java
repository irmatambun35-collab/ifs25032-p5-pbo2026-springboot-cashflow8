package org.delcom.ifs25032springbootcashflow.framework.view;

import org.delcom.ifs25032springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25032springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25032springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25032springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25032springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25032springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25032springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;

    private final Span total = new Span(), income = new Span(), expense = new Span(), balance = new Span();
    private final TextField search = new TextField();
    private final Select<SortOption> sort = new Select<>();
    private final Grid<Transaction> grid;
    private User user;

    public FinanceView(IUserRepository users, FinanceUseCase finance, FinancePresenter presenter, FileStorage storage) {
        this.users = users;
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;

        FlexLayout cards = new FlexLayout(card("Total transaksi", total), card("Pemasukan", income),
                card("Pengeluaran", expense), card("Saldo", balance));
        cards.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cards.addClassName("summary-cards");

        search.setPlaceholder("Cari keterangan...");
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());
        sort.setItems(SortOption.values());
        sort.setValue(SortOption.NEWEST);
        sort.setLabel("Urutkan");
        sort.addValueChangeListener(e -> refresh());
        Button reload = new Button("Muat ulang", e -> refresh());
        Button addIncome = new Button("Tambah Pemasukan", e -> openForm(TransactionType.INCOME));
        addIncome.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        Button addExpense = new Button("Tambah Pengeluaran", e -> openForm(TransactionType.EXPENSE));
        addExpense.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        FlexLayout toolbar = new FlexLayout(search, sort, reload, addIncome, addExpense);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        toolbar.addClassName("toolbar");

        grid = TransactionGrid.create(presenter, this::confirmDelete);
        add(new H3("Catatan Keuangan"), cards, toolbar, grid);
    }

    private Div card(String title, Span value) {
        value.addClassName("card-value");
        Div d = new Div(new Span(title), value);
        d.addClassName("summary-card");
        return d;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        user = AuthSession.currentUser(users);
        if (user != null) refresh();
    }

    private void refresh() {
        if (user == null) return;
        grid.setItems(finance.list(user.getId(), search.getValue(), sort.getValue()));
        FinanceUseCase.Summary s = finance.summary(user.getId());
        total.setText(String.valueOf(s.count()));
        income.setText(presenter.money(s.income()));
        expense.setText(presenter.money(s.expense()));
        balance.setText(presenter.money(s.balance()));
    }

    private void openForm(TransactionType type) {
        ModalDialog dialog = new ModalDialog("Tambah " + type.getLabel(), type.getLabel());
        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setWidthFull();
        UploadSupport.FileHolder holder = new UploadSupport.FileHolder();
        VerticalLayout body = new VerticalLayout(description, amount, new Paragraph("Bukti transaksi (opsional)"),
                UploadSupport.create(holder));
        body.setPadding(false);
        dialog.add(body);

        Button cancel = new Button("Batal", e -> dialog.close());
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            dialog.setBusy(true);
            save.setEnabled(false);
            try {
                String proof = holder.hasFile() ? storage.save(holder.stream(), holder.getName()) : null;
                Transaction t = finance.add(user.getId(), description.getValue(), amount.getValue(), type, proof);
                Notification.show(presenter.added(t));
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                Notification.show(presenter.error(ex));
            } finally {
                dialog.setBusy(false);
                save.setEnabled(true);
            }
        });
        dialog.addActions(cancel, save);
        dialog.open();
    }

    private void confirmDelete(Transaction t) {
        ModalDialog dialog = new ModalDialog("Hapus Transaksi", "Konfirmasi");
        dialog.add(new Paragraph("Hapus transaksi berikut?"), new Paragraph(presenter.line(t)));
        Button cancel = new Button("Batal", e -> dialog.close());
        Button delete = new Button("Hapus");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        delete.addClickListener(e -> {
            dialog.setBusy(true);
            try {
                finance.delete(user.getId(), t.getId());
                Notification.show(presenter.deleted(t.getId()));
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                Notification.show(presenter.error(ex));
            } finally {
                dialog.setBusy(false);
            }
        });
        dialog.addActions(cancel, delete);
        dialog.open();
    }
}
