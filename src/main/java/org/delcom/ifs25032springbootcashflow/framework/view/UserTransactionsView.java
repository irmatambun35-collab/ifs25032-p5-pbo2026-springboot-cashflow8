package org.delcom.ifs25032springbootcashflow.framework.view;

import org.delcom.ifs25032springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25032springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25032springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25032springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25032springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25032springbootcashflow.domain.entity.Transaction;

@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private final UserUseCase userUseCase;
    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final ComboBox<User> picker = new ComboBox<>("Pengguna");
    private final Paragraph summary = new Paragraph();
    private final Grid<Transaction> grid;

    public UserTransactionsView(IUserRepository users, UserUseCase userUseCase, FinanceUseCase finance,
                                FinancePresenter presenter) {
        this.users = users;
        this.userUseCase = userUseCase;
        this.finance = finance;
        this.presenter = presenter;
        picker.setItemLabelGenerator(u -> u.getFullName() + " (" + u.getUsername() + ")");
        picker.addValueChangeListener(e -> show(e.getValue()));
        grid = TransactionGrid.create(presenter, null); // read-only: tanpa tombol hapus
        add(new H3("Transaksi Pengguna (read-only)"), picker, summary, grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.requireAdmin(event, users, FinanceView.class, LoginView.class) == null) return;
        picker.setItems(userUseCase.list());
        event.getLocation().getQueryParameters().getSingleParameter("userId").ifPresent(id -> {
            try {
                picker.setValue(userUseCase.get(Integer.parseInt(id)));
            } catch (Exception ignored) {
                // ID tidak valid: biarkan pilihan kosong
            }
        });
    }

    private void show(User u) {
        if (u == null) {
            grid.setItems();
            summary.setText("");
            return;
        }
        grid.setItems(finance.list(u.getId(), "", SortOption.NEWEST));
        summary.setText(presenter.balanceLine(finance.getBalance(u.getId())));
    }
}
