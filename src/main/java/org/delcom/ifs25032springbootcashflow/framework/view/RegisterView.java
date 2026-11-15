package org.delcom.ifs25032springbootcashflow.framework.view;

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

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;

    public RegisterView(IUserRepository users, AuthUseCase auth) {
        this.users = users;
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField fullName = new TextField("Nama lengkap");
        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        Button submit = new Button("Daftar");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickListener(e -> {
            progress.setVisible(true);
            submit.setEnabled(false);
            try {
                AuthSession.login(auth.register(username.getValue(), password.getValue(), fullName.getValue()));
                Notification.show("Registrasi berhasil");
                UI.getCurrent().navigate(FinanceView.class);
            } catch (ValidationException ex) {
                Notification.show(ex.getMessage());
            } finally {
                progress.setVisible(false);
                submit.setEnabled(true);
            }
        });
        Button back = new Button("Sudah punya akun? Masuk", e -> UI.getCurrent().navigate(LoginView.class));
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        PageCard card = new PageCard();
        VerticalLayout form = new VerticalLayout(new H2("Daftar Akun"), fullName, username, password, progress, submit, back);
        form.setPadding(false);
        card.add(form);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) event.forwardTo(FinanceView.class);
    }
}
