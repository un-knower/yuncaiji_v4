package cn.uway.framework.accessor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.uway.framework.connection.ConnectionInfo;
import cn.uway.framework.task.GatherPathEntry;
import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.IoUtil;

/**
 * http方式接入 要求请求为get方式时，url如果没有经过URLEncoder.encode，则其url的参数名称与值不能包含&、=
 * 
 * @author linp 20150714
 * 
 */
public class HttpAccessor extends AbstractAccessor {

	private static final ILogger LOGGER = LoggerManager
			.getLogger(HttpAccessor.class);

	// 请求信息的编码
	private static final String REQUEST_ENCODE = "UTF-8";

	public static final String HTTPHEAD = "http://";

	// 最大次数请求
	public static final int DEFAULT_MAX_RETRY_TIMES = 5;

	// 请求超时时长
	public static final int DEFAULT_MAX_TIMEOUT = 10000; // ms

	// 返回流的编码
	public String respose_encode = null;

	HttpURLConnection conn = null;

	private InputStream currIn;

	public void setConnectionInfo(ConnectionInfo connInfo) {
	}

	@Override
	public AccessOutObject access(GatherPathEntry path) throws Exception {
		String urlPath = path.getPath().startsWith(HTTPHEAD)
				? path.getPath()
				: HTTPHEAD + path.getPath();
		// 数据请求方式控制 get post
		currIn = sendGet(urlPath.split("\\?")[0], urlPath.split("\\?")[1]);
		HttpAccessOutObject out = new HttpAccessOutObject();
		out.setOutObject(currIn);
		// 定义原始对象为url, 不包括其他传参
		out.setRawAccessName(urlPath.split("\\?")[0]);
		// 设置流的编码
		out.setEncode(respose_encode);
		return out;
	}

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return URL 所代表远程资源的响应结果
	 * @throws Exception
	 */
	private InputStream sendGet(String url, String param) throws Exception {
		InputStream err = null;
		String urlNameString = url + "?" + param;
		// get方式需要将参数值做url编码转换
		urlNameString = encodeURL(urlNameString);
		LOGGER.debug("请求url为：" + urlNameString);
		int httpStatusCode = -1;
		// 记录请求次数
		int maxRetryTimes = 1;

		for (int i = 0; i < DEFAULT_MAX_RETRY_TIMES; i++) {
			httpStatusCode = -1;
			try {
				URL surl = new URL(urlNameString);
				conn = (HttpURLConnection) surl.openConnection();
				conn.setConnectTimeout(DEFAULT_MAX_TIMEOUT);
				conn.setReadTimeout(DEFAULT_MAX_TIMEOUT);
				conn.setInstanceFollowRedirects(true);
				conn.setRequestMethod("GET");
				configureConnectionGET(conn);
				conn.connect();
			} catch (Exception e) {
				conn.disconnect();
				if (i + 1 >= maxRetryTimes)
					throw new Exception("Network connect fail");
				continue;
			}

			try {
				httpStatusCode = conn.getResponseCode();
				if (httpStatusCode == 200) {
					if (getResponseLength(conn) == 22) {
						LOGGER.debug("请求相应长度Content-Length:22，无body内容，可能服务器异常，重新请求");
						continue;
					}
					getResponseEncode(conn);
					return conn.getInputStream();
				} else {
					LOGGER.debug("" + httpStatusCode);
					err = conn.getErrorStream();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IoUtil.closeQuietly(err);
			}
		}
		httpStatusCode = 404;
		throw new Exception("Connect web server fail");
	}

	/**
	 * 向指定URL发送POST方法的请求
	 * 
	 * @param url
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private InputStream sendPost(String url, String param) throws Exception {
		InputStream err = null;
		int httpStatusCode = -1;
		int maxRetryTimes = 1;

		for (int i = 0; i < DEFAULT_MAX_RETRY_TIMES; i++) {
			httpStatusCode = -1;
			try {
				URL surl = new URL(url);
				conn = (HttpURLConnection) surl.openConnection();
				conn.setConnectTimeout(DEFAULT_MAX_TIMEOUT);
				conn.setReadTimeout(DEFAULT_MAX_TIMEOUT);
				conn.setInstanceFollowRedirects(true);
				conn.setRequestMethod("POST");
				configureConnectionPOST(conn);
				conn.connect();
			} catch (Exception e) {
				conn.disconnect();
				if (i + 1 >= maxRetryTimes)
					throw new Exception("Network connect fail");
				continue;
			}

			try {
				OutputStream os = conn.getOutputStream();
				// 设置post流的编码
				os.write(param.getBytes(REQUEST_ENCODE));
				os.flush();
				os.close();

				httpStatusCode = conn.getResponseCode();
				if (httpStatusCode == 200) {
					if (getResponseLength(conn) == 22) {
						LOGGER.debug("请求相应长度Content-Length:22，无body内容，可能服务器异常，重新请求");
						continue;
					}
					getResponseEncode(conn);
					return conn.getInputStream();
				} else {
					LOGGER.debug("" + httpStatusCode);
					err = conn.getErrorStream();
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IoUtil.closeQuietly(err);
			}
		}
		httpStatusCode = 404;
		throw new Exception("Connect web server fail");
	}

	// 从head中获取返回流的编码
	private void getResponseEncode(HttpURLConnection http)
			throws UnsupportedEncodingException {
		Map<String, String> header = getHttpResponseHeader(http);
		for (Map.Entry<String, String> entry : header.entrySet()) {
			String key = entry.getKey() != null ? entry.getKey() + ":" : "";
			if (key.startsWith("Content-Type")) {
				String[] contentType = entry.getValue().split(";");
				// 第二个为http返回的编码
				respose_encode = contentType[1].split("=")[1];
				break;
			}
		}
		if (respose_encode == null) {
			respose_encode = REQUEST_ENCODE;
		}
	}

	// 从head中获取返回流的长度
	private int getResponseLength(HttpURLConnection http)
			throws UnsupportedEncodingException {
		Map<String, String> header = getHttpResponseHeader(http);
		int length = 0;
		for (Map.Entry<String, String> entry : header.entrySet()) {
			String key = entry.getKey() != null ? entry.getKey() + ":" : "";
			if (key.startsWith("Content-Length")) {
				length = Integer.valueOf(entry.getValue().trim());
			}
		}
		return length;
	}

	// 读取返回流的head信息
	private Map<String, String> getHttpResponseHeader(HttpURLConnection http)
			throws UnsupportedEncodingException {
		Map<String, String> header = new LinkedHashMap<String, String>();
		for (int i = 0;; i++) {
			String mine = http.getHeaderField(i);
			if (mine == null)
				break;
			header.put(http.getHeaderFieldKey(i), mine);
		}
		return header;
	}

	// 将url中参数值进行urlencode转换
	@SuppressWarnings("deprecation")
	private String urlEncode(String urlparm) {
		try {
			return URLEncoder.encode(urlparm, REQUEST_ENCODE);
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(urlparm);
		}
	}

	// 将整个url进行urlencode转换
	private String encodeURL(String url) {
		StringBuilder sbd = new StringBuilder();
		sbd.append(url.substring(0, url.indexOf("?") + 1));
		String parmStr = url.substring(url.indexOf("?") + 1).trim();
		if (parmStr.length() > 0) {
			String[] parms = parmStr.split("&");
			for (String parm : parms) {
				String[] kv = parm.split("=");
				if (kv.length == 2) {
					sbd.append(kv[0]).append("=").append(urlEncode(kv[1]))
							.append("&");
				}
			}
		}
		return sbd.toString().substring(0, sbd.length() - 1);
	}

	private void configureConnectionGET(HttpURLConnection conn) {
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		conn.setRequestProperty("User-Agent", "Uway");
		// 设置请求传输信息的编码
		conn.setRequestProperty("Accept-Charset", REQUEST_ENCODE);
	}

	private void configureConnectionPOST(HttpURLConnection conn) {
		// 发送POST请求必须设置如下两行
		conn.setDoOutput(true);
		conn.setDoInput(true);
		configureConnectionGET(conn);
	}

	@Override
	public boolean beforeAccess() {
		// 处理参数发送参数
		return super.beforeAccess();
	}

	@Override
	public void close() {
		IoUtil.closeQuietly(currIn);
		if (conn != null)
			conn.disconnect();
	}

}
