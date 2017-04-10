package cn.uway.ucloude.uts.web.admin.support.xss;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author magic.s.g.xie.
 */
public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) request), response);

    }

    @Override
    public void destroy() {

    }
}
