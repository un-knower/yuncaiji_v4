package cn.uway.ucloude.uts.web.admin.support.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author magic.s.g.xie
 */
public final class CSRFTokenManager {

    static final String CSRF_PARAM_NAME = "csrfToken";

    public final static String CSRF_TOKEN_FOR_SESSION_ATTR_NAME = CSRFTokenManager.class.getSimpleName() + ".token";

    private CSRFTokenManager() {
    }

    public static String getToken(HttpSession session) {
        String token = null;

        synchronized (session) {
            token = (String) session.getAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME);
            if (null == token) {
                token = UUID.randomUUID().toString();
                session.setAttribute(CSRF_TOKEN_FOR_SESSION_ATTR_NAME, token);
            }
        }
        return token;
    }

    public static String getToken(HttpServletRequest request) {
        String token = request.getParameter(CSRF_PARAM_NAME);
        if (token == null || "".equals(token)) {
            token = request.getHeader(CSRF_PARAM_NAME);
        }
        return token;
    }

}
