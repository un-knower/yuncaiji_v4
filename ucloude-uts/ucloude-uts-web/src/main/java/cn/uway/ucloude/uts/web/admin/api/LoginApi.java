package cn.uway.ucloude.uts.web.admin.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.MD5Utils;
import cn.uway.ucloude.uts.web.access.domain.LockType;
import cn.uway.ucloude.uts.web.access.domain.LoginConfigInfo;
import cn.uway.ucloude.uts.web.access.domain.LoginLockInfo;
import cn.uway.ucloude.uts.web.access.domain.UserInfo;
import cn.uway.ucloude.uts.web.access.face.LoginConfigAccess;
import cn.uway.ucloude.uts.web.access.face.UserManager;
import cn.uway.ucloude.uts.web.admin.AbstractMVC;
import cn.uway.ucloude.uts.web.admin.vo.RestfulResponse;
import cn.uway.ucloude.uts.web.security.UtsGrantedAuthority;
import cn.uway.ucloude.uts.web.security.VerificationCodeManager;

@RestController
@RequestMapping("admin")
public class LoginApi extends AbstractMVC {
	@Autowired
	private AuthenticationManager utsAuthenticationManager;
	@Autowired
	private LoginConfigAccess loginConfigService;
	@Autowired
	private UserManager userManager;

	/**
	 * 登录
	 * 
	 * @param userName
	 * @param password
	 * @param verifyCode
	 * @param request
	 * @return
	 */
	@RequestMapping("login")
	public RestfulResponse login(String userName, String password, String verifyCode, HttpServletRequest request) {
		RestfulResponse response = new RestfulResponse();
		VerificationCodeManager vcm = new VerificationCodeManager();
		String verifyMsg = vcm.check(verifyCode, request);
		if (verifyMsg != null) {
			response.setSuccess(false);
			response.setMsg(verifyMsg);
			response.setCode("true");
			return response;
		}
		try {
			Authentication authentication = utsAuthenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(userName, entryPassword(password)));
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(authentication);
			HttpSession session = request.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
			vcm.clear(request);
			response.setSuccess(true);
			response.setMsg("登录成功！");
		} catch (LockedException e) {
			response.setCode(vcm.isNeedCode(request.getSession()) ? "true" : "false");
			response.setSuccess(false);
			response.setMsg("用户已锁定！");
		} catch (DisabledException e) {
			response.setCode(vcm.isNeedCode(request.getSession()) ? "true" : "false");
			response.setSuccess(false);
			response.setMsg("用户已禁用！");
		} catch (BadCredentialsException e) {
			UserInfo userInfo = userManager.getUserInfo(userName);
			if (userInfo != null) {
				postLoginLockLog(userInfo.getUserId(), request.getRemoteAddr());
			}
			response.setCode(vcm.addErrorCount(request) ? "true" : "false");
			response.setSuccess(false);
			response.setMsg("用户名或密码错误！");
		} catch (AuthenticationException ae) {
			response.setCode(vcm.isNeedCode(request.getSession()) ? "true" : "false");
			response.setSuccess(false);
			response.setMsg("登录异常！");
			LoggerManager.getLogger(this.getClass()).info(ae.toString());
		}
		return response;
	}

	/**
	 * 验证码校验
	 * 
	 * @param verifyCode
	 * @param request
	 * @return
	 */
	@RequestMapping("checkVerifyCode")
	public RestfulResponse checkVerifyCode(String verifyCode, HttpServletRequest request) {
		RestfulResponse response = new RestfulResponse();
		VerificationCodeManager vcm = new VerificationCodeManager();
		String verifyMsg = vcm.check(verifyCode, request);
		response.setSuccess(verifyMsg == null);
		response.setMsg(verifyMsg);
		return response;
	}

	/**
	 * 获取当前用户菜单
	 * 
	 * @return
	 */
	@RequestMapping("getmenu")
	public RestfulResponse getMenu() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UtsGrantedAuthority uga = (UtsGrantedAuthority) userDetails.getAuthorities().toArray()[0];
		RestfulResponse response = new RestfulResponse();
		response.setSuccess(true);
		response.setMsg("获取成功！");
		response.setData(uga.getRootMenu());
		return response;
	}

	/**
	 * 获取当前用户资料
	 * 
	 * @return
	 */
	@RequestMapping("getuserinfo")
	public RestfulResponse getUserInfo() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UtsGrantedAuthority uga = (UtsGrantedAuthority) userDetails.getAuthorities().toArray()[0];
		RestfulResponse response = new RestfulResponse();
		response.setSuccess(true);
		response.setMsg("获取成功！");
		response.setData(uga.getUtsUserInfo());
		return response;
	}

	/**
	 * 加密密码
	 * 
	 * @param password
	 * @return
	 */
	private String entryPassword(String password) {
		return MD5Utils.getMD5String(password).toLowerCase().substring(8, 24);
	}

	private void postLoginLockLog(int userId, String ip) {
		LoginConfigInfo loginConfig = loginConfigService.getLoginCinfigInfo();
		if (loginConfig == null) {
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		LoginLockInfo lockInfo = loginConfigService.getLoginLockInfo(userId,
				loginConfig.getUserLockCode() == LockType.LOCKUSERNO ? null : ip, calendar.getTime());
		if (lockInfo == null) {
			lockInfo = new LoginLockInfo();
			lockInfo.setUserId(userId);
			lockInfo.setCreateDate(new Date());
			lockInfo.setPwdErrorCounter(1);
			lockInfo.setUpdateDate(new Date());
			lockInfo.setLoginIp(ip);
			lockInfo.setUserLockCode(String.valueOf(loginConfig.getUserLockCode().getValue()));
			loginConfigService.AddLoginLockInfo(lockInfo);
		} else {
			lockInfo.setPwdErrorCounter(lockInfo.getPwdErrorCounter() + 1);
			if (lockInfo.getPwdErrorCounter() >= loginConfig.getPwdErrorCount()) {
				lockInfo.setLockDate(new Date());
				userManager.lockUser(userId);
			}
			loginConfigService.UpdateLoginLockInfo(lockInfo);
		}
	}
}
