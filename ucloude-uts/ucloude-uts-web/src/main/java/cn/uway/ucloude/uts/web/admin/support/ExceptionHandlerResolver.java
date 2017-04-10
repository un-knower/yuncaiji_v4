package cn.uway.ucloude.uts.web.admin.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.serialize.JsonConvert;
import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;

public class ExceptionHandlerResolver implements HandlerExceptionResolver {
	   private static final ILogger LOGGER = LoggerManager.getLogger("[UTS-Web]");


	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		// TODO Auto-generated method stub
		if (AjaxUtils.isAjaxRequest(request)) {
            PrintWriter writer = null;
            try {
                writer = response.getWriter();
                RestfulResponse restfulResponse = new RestfulResponse();
                restfulResponse.setSuccess(false);
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                restfulResponse.setMsg(sw.toString());
                String json = JsonConvert.serialize(restfulResponse);
                assert json != null;
                writer.write(json);
                writer.flush();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            LOGGER.error(ex.getMessage(), ex);
//            request.setAttribute("message", ex.getMessage());
//            return new ModelAndView("common/error");
        }
        return null;
	}

}
