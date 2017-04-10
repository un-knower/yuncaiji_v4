package cn.uway.ucloude.uts.web.admin.support;

import javax.servlet.http.HttpServletRequest;

public class AjaxUtils {
	   public static boolean isAjaxRequest(HttpServletRequest request) {
	        String requestedWith = request.getHeader("X-Requested-With");
	        return requestedWith != null && "XMLHttpRequest".equals(requestedWith);
	    }
}
