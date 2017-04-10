package cn.uway.ucloude.uts.web.admin.support.csrf;

import javax.servlet.http.HttpServletRequest;

/**
 * 配置在 velocity tools 中
 *
 * <input type="hidden" name="csrfToken" value="$csrfTool.getToken($request)"/>
 *
 * @author magic.s.g.xie
 */
public class CSRFTool {
    public static String getToken(HttpServletRequest request) {
        return CSRFTokenManager.getToken(request.getSession());
    }
}
