package org.delcom.ifs25032springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(ui -> {
            var cfg = ui.getUI().getLoadingIndicatorConfiguration();
            cfg.setFirstDelay(150);
            cfg.setSecondDelay(600);
            cfg.setThirdDelay(1500);
        });
    }
}
