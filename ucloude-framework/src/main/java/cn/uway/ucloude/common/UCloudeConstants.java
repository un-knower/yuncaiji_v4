package cn.uway.ucloude.common;

import java.util.regex.Pattern;

public interface UCloudeConstants {
	// 可用的处理器个数
    int AVAILABLE_PROCESSOR = Runtime.getRuntime().availableProcessors();

    String OS_NAME = System.getProperty("os.name");

    String USER_HOME = System.getProperty("user.home");

    Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");
    
    String LINE_SEPARATOR = System.getProperty("line.separator");
    

    int DEFAULT_PROCESSOR_THREAD = 32 + AVAILABLE_PROCESSOR * 5;
    

    String ADAPTIVE = "adaptive";
    
    int DEFAULT_BUFFER_SIZE = 16 * 1024 * 1024;
    
    // 默认集群名字
    String DEFAULT_CLUSTER_NAME = "defaultCluster";
}
