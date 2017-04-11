package cn.uway.util.parquet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;


import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.util.FileEncodeUtil;

public class SchemaDS {
	private static ILogger LOG = LoggerManager.getLogger(SchemaDS.class);

	private static Pattern parrten = Pattern
			.compile("message\\s+(\\w+)\\s*\\{[\\s\\S]*?\\}");
	private HashMap<String, String> schemas = new HashMap<String, String>(200);

	/**
	 * schema文件名，支持多个文件
	 * 
	 * @param fileNames
	 */
	public SchemaDS(String[] fileNames) {
		loadSchema(fileNames);
	}

	private void loadSchema(String[] fileNames) {
		for (String fn : fileNames) {
			File file = new File(fn);
			try {
				String txt = FileUtils.readFileToString(file,FileEncodeUtil.detector(file));
				Matcher m = parrten.matcher(txt);
				while (m.find()) {
					schemas.put(m.group(1).toUpperCase(), m.group(0));
				}
			} catch (IOException e) {
				LOG.error("文件读取失败:{}", fn);
			}
			LOG.info("{}加载完成", fn);
		}

	}

	public String getSchema(String tblName) {
		return schemas.get(tblName.toUpperCase());
	}
}
