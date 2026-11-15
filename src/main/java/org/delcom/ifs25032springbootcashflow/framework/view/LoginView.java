package org.delcom.ifs25032springbootcashflow.framework.view;

import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25032springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25032springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;

    public LoginView(IUserRepository users, AuthUseCase auth) {
        this.users = users;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField username = new TextField("Username");
        username.setId("login-username");
        PasswordField password = new PasswordField("Password");
        password.setId("login-password");
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        Button login = new Button("Masuk");
        login.setId("login-submit");
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        login.addClickShortcut(com.vaadin.flow.component.Key.ENTER);
        login.addClickListener(e -> {
            progress.setVisible(true);
            login.setEnabled(false);
            try {
                User u = auth.login(username.getValue(), password.getValue());
                AuthSession.login(u);
                UI.getCurrent().navigate(FinanceView.class);
            } catch (ValidationException ex) {
                Notification.show(ex.getMessage());
            } finally {
                progress.setVisible(false);
                login.setEnabled(true);
            }
        });
        Button register = new Button("Belum punya akun? Daftar", e -> UI.getCurrent().navigate(RegisterView.class));
        register.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        PageCard card = new PageCard();
        VerticalLayout form = new VerticalLayout(new H2("Masuk ke Cashflow"), username, password, progress, login, register);
        form.setPadding(false);
        card.add(form);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) event.forwardTo(FinanceView.class);
    }
}
