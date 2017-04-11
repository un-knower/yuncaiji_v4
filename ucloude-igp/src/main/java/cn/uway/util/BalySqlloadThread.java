package cn.uway.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;


/**
 * <p>
 * Title: sql load
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: uway
 * </p>
 * 
 * @author zhoujian
 * @version 1.0
 */

public class BalySqlloadThread extends Thread {
	
	private static final ILogger LOG = LoggerManager.getLogger(BalySqlloadThread.class);

	private String execcmd;

	private int tableIndex;

	private int time = -1;

	public BalySqlloadThread() {

	}

	public void setTime(int time) {
		this.time = time;
	}

	public void run() {
		try {
			runcmd(getExeccmd());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int runcmd(String cmd) throws Exception {
		int retvalue = 0;

		Process proc = Runtime.getRuntime().exec(cmd);

		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "Error");
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "Output");
		errorGobbler.start();
		outputGobbler.start();

		LOG.debug("waitfor");
		proc.waitFor();
		if (time == -1) {
			Thread.sleep(1000 * 5);
		}
		retvalue = proc.exitValue();
		LOG.debug("sqlldr.exitvalue=" + retvalue);

		proc.destroy();

		return retvalue;
	}

	class StreamGobbler extends Thread {

		InputStream is;

		String type;

		StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				@SuppressWarnings("unused")
				String line = null;
				while ((line = br.readLine()) != null) {
					// System.out.println(line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public String getExeccmd() {
		return execcmd;
	}

	public void setExeccmd(String execcmd) {
		LOG.debug(execcmd);
		this.execcmd = execcmd;
	}

	public int getTableIndex() {
		return tableIndex;
	}

	public void setTableIndex(int tableIndex) {
		this.tableIndex = tableIndex;
	}
}
