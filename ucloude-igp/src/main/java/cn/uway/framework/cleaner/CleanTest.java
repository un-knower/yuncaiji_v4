package cn.uway.framework.cleaner;

import java.util.ArrayList;
import java.util.List;

import cn.uway.framework.cleaner.LocalFileCleaner;
import cn.uway.framework.cleaner.TimerFileCleaner;


public class CleanTest {
	public static void main(String[] args) {
		NeFileCleanRule n = new NeFileCleanRule();
		n.setFilesCleanTime(1440);
		LocalFileCleaner l = new LocalFileCleaner();
		l.setCleanPath("/home/tianjing/windows File/PublicLoggerJar/lib/bak/");
		l.setCleanRule(n);
		List<String> list = new ArrayList<String>();
		list.add("*.jar");
		l.setMappings(list);
		TimerFileCleaner t = new TimerFileCleaner();
		t.setCleaner(l);
		t.setPeriod(1440);
		t.start();
	}

}
