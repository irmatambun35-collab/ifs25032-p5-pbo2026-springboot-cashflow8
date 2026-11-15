package org.delcom.ifs25032springbootcashflow.framework.view;

import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25032springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25032springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import java.util.Map;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private final UserUseCase userUseCase;
    private final Grid<User> grid = new Grid<>();

    public UsersView(IUserRepository users, UserUseCase userUseCase) {
        this.users = users;
        this.userUseCase = userUseCase;
        GridStyles.apply(grid);
        grid.addColumn(User::getId).setHeader("ID").setSortable(true).setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setSortable(true);
        grid.addColumn(u -> u.getRole().name()).setHeader("Role").setSortable(true);
        grid.addComponentColumn(u -> new Button("Lihat transaksi", e -> UI.getCurrent().navigate("user-transactions",
                QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))))).setHeader("Aksi");
        add(new H3("Daftar Pengguna"), grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.requireAdmin(event, users, FinanceView.class, LoginView.class) != null) {
            grid.setItems(userUseCase.list());
        }
    }
}
