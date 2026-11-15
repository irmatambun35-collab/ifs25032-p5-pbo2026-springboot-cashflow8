package org.delcom.ifs25032springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;

/** Dialog standar: header biru #2563EB + badge, footer abu-abu #F3F4F6 dengan tombol di kanan, progress bar. */
public class ModalDialog extends Dialog {
    private final ProgressBar progress = new ProgressBar();

    public ModalDialog(String title, String badgeText) {
        addClassName("app-dialog");
        setHeaderTitle(title);
        Span badge = new Span(badgeText);
        badge.getElement().getThemeList().add("badge contrast");
        getHeader().add(badge);
        progress.setIndeterminate(true);
        progress.setVisible(false);
        add(progress);
        setCloseOnOutsideClick(false);
    }

    public void setBusy(boolean busy) { progress.setVisible(busy); }

    public void addActions(Button... buttons) { getFooter().add(buttons); }
}
