package cn.uway.ucloude.uts.core.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import cn.uway.ucloude.cmd.HttpCmdProcessor;
import cn.uway.ucloude.cmd.HttpCmdRequest;
import cn.uway.ucloude.cmd.HttpCmdResponse;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.uts.core.UtsContext;
import cn.uway.ucloude.uts.core.cluster.JobNode;

/**
 * 文件读取命令处理器
 * 
 * @author Uway-M3
 */
public abstract class ReadFileHttpCmd implements HttpCmdProcessor {
	protected String logFilePath;
	private UtsContext context;

	public ReadFileHttpCmd(UtsContext context) {
		this.context = context;
	}

	@Override
	public String nodeIdentity() {
		return context.getConfiguration().getIdentity();
	}

	@Override
	public String getCommand() {
		return HttpCmdNames.HTTP_CMD_LOG_READ;
	}

	@Override
	public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
		HttpCmdResponse response = new HttpCmdResponse();
		File file = new File(logFilePath);// 日志文件
		LoggerManager.getLogger(this.getClass()).info("日志文件:" + file.getAbsolutePath());
		if (file.isFile() && file.exists()) {
			try {
				int page = Integer.parseInt(request.getParam("page", "1"));// 页码，默认第一页
				int pageSize = Integer.parseInt(request.getParam("pageSize", "1000"));// 页大小，默认1000
				if (pageSize > 999999) {
					response.setSuccess(false);
					response.setMsg("页大小必须小于1000000");
					return response;
				}
				int startIndex = (page - 1) * pageSize;// 开始行号 包含
				int endIndex = page * pageSize;// 结束行号 不包含
				int lineIndex = 0;// 当前读取的行号
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				StringBuffer sb = new StringBuffer();
				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (lineIndex >= startIndex && lineIndex < endIndex) {
						sb.append(lineTxt);
						sb.append(System.getProperty("line.separator"));
					}
					lineIndex++;
				}
				read.close();

				DecimalFormat df = new DecimalFormat("000000");
				int count = lineIndex - startIndex;
				sb.insert(0, df.format(count < 0 ? 0 : count));// 将当前数据条数写入文本前6个字符
				response.setSuccess(true);
				response.setObj(sb.toString());
				response.setMsg("读取成功!");
			} catch (Exception ex) {
				response.setSuccess(false);
				response.setMsg(ex.toString());
			}
		} else {
			response.setSuccess(false);
			response.setMsg("未找到日志文件!");
		}
		return response;
	}

}
