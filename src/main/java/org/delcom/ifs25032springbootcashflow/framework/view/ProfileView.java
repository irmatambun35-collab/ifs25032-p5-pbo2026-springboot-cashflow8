package org.delcom.ifs25032springbootcashflow.framework.view;

import org.delcom.ifs25032springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25032springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25032springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private final TextField fullName = new TextField("Nama lengkap");
    private final Avatar avatar = new Avatar();
    private User user;

    public ProfileView(IUserRepository users, UserUseCase userUseCase, FileStorage storage) {
        this.users = users;
        fullName.setWidth("320px");
        PasswordField password = new PasswordField("Password baru (kosongkan jika tidak diubah)");
        password.setWidth("320px");
        avatar.setHeight("80px");
        avatar.setWidth("80px");
        UploadSupport.FileHolder holder = new UploadSupport.FileHolder();
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            try {
                userUseCase.updateProfile(user.getId(), fullName.getValue(), password.getValue());
                if (holder.hasFile()) {
                    userUseCase.updatePhoto(user.getId(), storage.save(holder.stream(), holder.getName()));
                }
                Notification.show("Profil berhasil disimpan");
                UI.getCurrent().getPage().reload();
            } catch (DomainException ex) {
                Notification.show("Gagal: " + ex.getMessage());
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });
        PageCard card = new PageCard();
        VerticalLayout form = new VerticalLayout(avatar, fullName, password, new Paragraph("Foto profil (opsional)"),
                UploadSupport.create(holder), progress, save);
        form.setPadding(false);
        card.add(form);
        add(new H3("Profil"), card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        user = AuthSession.currentUser(users);
        if (user == null) return;
        fullName.setValue(user.getFullName());
        avatar.setName(user.getFullName());
        if (user.getPhotoPath() != null) avatar.setImage(user.getPhotoPath());
    }
}
