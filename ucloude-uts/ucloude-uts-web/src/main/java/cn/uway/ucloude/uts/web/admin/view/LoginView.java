package cn.uway.ucloude.uts.web.admin.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.uway.ucloude.uts.web.security.VerificationCodeManager;

@Controller
@RequestMapping("login")
public class LoginView {
	@RequestMapping("index")
	public String index(Model model, HttpServletRequest request) {
		VerificationCodeManager vcm = new VerificationCodeManager();
		model.addAttribute("isNeedCode", vcm.isNeedCode(request.getSession()));
		return "login/index";
	}

	@RequestMapping("verifyImg")
	public void verifyImg(HttpServletRequest request, HttpServletResponse response) {
		VerificationCodeManager vcm = new VerificationCodeManager();
		vcm.getRandcode(request, response);
	}
}
