package org.delcom.ifs25032springbootcashflow.framework.auth;

import org.delcom.ifs25032springbootcashflow.domain.entity.Role;
import org.delcom.ifs25032springbootcashflow.domain.entity.User;
import org.delcom.ifs25032springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Sesi login: VaadinSession + HTTP session + cookie bertanda tangan (HMAC). */
public final class AuthSession {
    private static final String USER_KEY = "cashflow.userId";
    private static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final String COOKIE = "cashflow_uid";
    private static final int MAX_AGE = 8 * 3600;

    private AuthSession() {}

    public static void login(User user) {
        VaadinSession vs = VaadinSession.getCurrent();
        vs.setAttribute(USER_KEY, user.getId());
        vs.setAttribute(LOGGED_OUT_KEY, null);
        WrappedSession ws = VaadinRequest.getCurrent().getWrappedSession();
        ws.setAttribute(USER_KEY, user.getId());
        ws.removeAttribute(LOGGED_OUT_KEY);
        writeCookie(sign(user.getId()), MAX_AGE);
    }

    public static void logout() {
        VaadinSession vs = VaadinSession.getCurrent();
        vs.setAttribute(USER_KEY, null);
        vs.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        WrappedSession ws = VaadinRequest.getCurrent().getWrappedSession();
        ws.removeAttribute(USER_KEY);
        ws.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        writeCookie("", 0);
    }

    public static User currentUser(IUserRepository repo) {
        VaadinSession vs = VaadinSession.getCurrent();
        VaadinRequest req = VaadinRequest.getCurrent();
        if (vs == null || req == null) return null;
        WrappedSession ws = req.getWrappedSession();
        if (Boolean.TRUE.equals(vs.getAttribute(LOGGED_OUT_KEY))
                || Boolean.TRUE.equals(ws.getAttribute(LOGGED_OUT_KEY))) return null;
        Integer id = (Integer) vs.getAttribute(USER_KEY);
        if (id == null) id = (Integer) ws.getAttribute(USER_KEY);
        if (id == null) id = idFromCookie(req);
        if (id == null) return null;
        Optional<User> user = repo.findById(id);
        user.ifPresent(u -> vs.setAttribute(USER_KEY, u.getId()));
        return user.orElse(null);
    }

    /** Guard halaman ADMIN: mengembalikan user bila lolos, atau melakukan forward dan mengembalikan null. */
    public static User requireAdmin(BeforeEnterEvent event, IUserRepository repo, Class<? extends com.vaadin.flow.component.Component> home,
                                    Class<? extends com.vaadin.flow.component.Component> login) {
        User user = currentUser(repo);
        if (user == null) { event.forwardTo(login); return null; }
        if (user.getRole() != Role.ADMIN) { event.forwardTo(home); return null; }
        return user;
    }

    private static Integer idFromCookie(VaadinRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (!COOKIE.equals(c.getName())) continue;
            String[] parts = c.getValue().split("\\.");
            if (parts.length != 2) return null;
            try {
                int id = Integer.parseInt(parts[0]);
                return sign(id).equals(c.getValue()) ? id : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static String sign(int id) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return id + "." + HexFormat.of().formatHex(mac.doFinal(Integer.toString(id).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String secret() {
        String s = System.getProperty("APP_SECRET");
        if (s == null) s = System.getenv("APP_SECRET");
        return s != null ? s : "cashflow-dev-secret-change-me";
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse res = VaadinService.getCurrentResponse();
        if (res == null) return;
        Cookie c = new Cookie(COOKIE, value);
        c.setPath("/");
        c.setHttpOnly(true);
        c.setMaxAge(maxAge);
        res.addCookie(c);
    }
}
