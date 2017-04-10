package cn.uway.ucloude.uts.web.security;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.uway.ucloude.utils.StringUtil;

public class VerificationCodeManager {
	// 验证码 -KEY
	private String verificationCodeKey = "VERIFICATIONCODE";
	// 登录错误次数 - KEY
	private String errorCountKey = "PWDERRORCOUNT";
	// 验证码出现在第N次错误时 0表示每次都需要验证码
	private int startErrorCount = 3;
	private Random random = new Random();
	private String randString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";// 随机产生的字符串

	private int width = 80;// 图片宽
	private int height = 26;// 图片高
	private int lineSize = 40;// 干扰线数量
	private int stringNum = 4;// 随机产生字符数量
	/*
	 * 获得字体
	 */

	private Font getFont() {
		return new Font("Fixedsys", Font.CENTER_BASELINE, 18);
	}

	/*
	 * 获得颜色
	 */
	private Color getRandColor(int fc, int bc) {
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc - 16);
		int g = fc + random.nextInt(bc - fc - 14);
		int b = fc + random.nextInt(bc - fc - 18);
		return new Color(r, g, b);
	}

	/*
	 * 绘制字符串
	 */
	private String drowString(Graphics g, String randomString, int i) {
		g.setFont(getFont());
		g.setColor(new Color(random.nextInt(101), random.nextInt(111), random.nextInt(121)));
		String rand = String.valueOf(getRandomString(random.nextInt(randString.length())));
		randomString += rand;
		g.translate(random.nextInt(3), random.nextInt(3));
		g.drawString(rand, 13 * i, 16);
		return randomString;
	}

	/*
	 * 绘制干扰线
	 */
	private void drowLine(Graphics g) {
		int x = random.nextInt(width);
		int y = random.nextInt(height);
		int xl = random.nextInt(13);
		int yl = random.nextInt(15);
		g.drawLine(x, y, x + xl, y + yl);
	}

	/*
	 * 获取随机的字符
	 */
	private String getRandomString(int num) {
		return String.valueOf(randString.charAt(num));
	}

	/**
	 * 获取已经错误次数
	 * 
	 * @param session
	 * @return
	 */
	private int getErrorCount(HttpSession session) {
		int count = 0;
		Object objCount = session.getAttribute(errorCountKey);
		if (objCount != null) {
			try {
				count = Integer.parseInt(objCount.toString());
			} catch (Exception e) {
			}
		}
		return count;
	}

	/**
	 * 获取验证码
	 * 
	 * @param session
	 * @return
	 */
	private String getVerification(HttpSession session) {
		Object objCode = session.getAttribute(verificationCodeKey);
		if (objCode != null) {
			return objCode.toString();
		} else {
			return null;
		}
	}

	/**
	 * 生成随机图片
	 */
	public void getRandcode(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		if (!isNeedCode(session)) {
			return;
		}
		// BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		Graphics g = image.getGraphics();// 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
		g.fillRect(0, 0, width, height);
		g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, 18));
		g.setColor(getRandColor(110, 133));
		// 绘制干扰线
		for (int i = 0; i <= lineSize; i++) {
			drowLine(g);
		}
		// 绘制随机字符
		String randomString = "";
		for (int i = 1; i <= stringNum; i++) {
			randomString = drowString(g, randomString, i);
		}
		session.removeAttribute(verificationCodeKey);
		session.setAttribute(verificationCodeKey, randomString);
		g.dispose();
		try {
			ImageIO.write(image, "JPEG", response.getOutputStream());// 将内存中的图片通过流动形式输出到客户端
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 增加密码错误次数
	 * 
	 * @return true:需要验证码 false:不需要
	 */
	public boolean addErrorCount(HttpServletRequest request) {
		HttpSession session = request.getSession();
		int count = getErrorCount(session);
		session.removeAttribute(errorCountKey);
		session.setAttribute(errorCountKey, ++count);
		return count >= startErrorCount;
	}

	public void clear(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.removeAttribute(verificationCodeKey);
		session.removeAttribute(errorCountKey);
	}

	public String check(String code, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String msg = null;
		if (isNeedCode(session)) {
			if (!StringUtil.isNotEmpty(code)) {
				msg = "请输入验证码！";
			} else {
				String realCode = getVerification(session);
				if (realCode == null) {
					msg = "请获取并输入验证码！";
				} else {
					msg = realCode.equalsIgnoreCase(code) ? null : "验证码错误!";
				}
			}
		}
		return msg;
	}

	public boolean isNeedCode(HttpSession session) {
		return getErrorCount(session) >= startErrorCount;
	}
}
