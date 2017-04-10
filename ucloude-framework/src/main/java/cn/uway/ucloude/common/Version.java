package cn.uway.ucloude.common;

import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import cn.uway.ucloude.log.ILogger;
import cn.uway.ucloude.log.LoggerManager;
import cn.uway.ucloude.utils.ClassHelper;

public final class Version {
	private Version() {}

    private static final ILogger LOGGER = LoggerManager.getLogger(Version.class);

    private static final String VERSION = getVersion(Version.class, "0.0.1-SNAPSHOT");

    static {
        // ����Ƿ�����ظ���jar��
        Version.checkDuplicate(Version.class);
    }

    public static String getVersion(){
        return VERSION;
    }

    public static String getVersion(Class<?> cls, String defaultVersion) {
        try {
            // ���Ȳ���MANIFEST.MF�淶�еİ汾��
            String version = cls.getPackage().getImplementationVersion();
            if (version == null || version.length() == 0) {
                version = cls.getPackage().getSpecificationVersion();
            }
            if (version == null || version.length() == 0) {
                // ����淶��û�а汾�ţ�����jar������ȡ�汾��
                CodeSource codeSource = cls.getProtectionDomain().getCodeSource();
                if(codeSource == null) {
                    LOGGER.info("No codeSource for class " + cls.getName() + " when getVersion, use default version " + defaultVersion);
                }
                else {
                    String file = codeSource.getLocation().getFile();
                    if (file != null && file.length() > 0 && file.endsWith(".jar")) {
                        file = file.substring(0, file.length() - 4);
                        int i = file.lastIndexOf('/');
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        }
                        i = file.indexOf("-");
                        if (i >= 0) {
                            file = file.substring(i + 1);
                        }
                        while (file.length() > 0 && ! Character.isDigit(file.charAt(0))) {
                            i = file.indexOf("-");
                            if (i >= 0) {
                                file = file.substring(i + 1);
                            } else {
                                break;
                            }
                        }
                        version = file;
                    }
                }
            }
            // ���ذ汾�ţ����Ϊ�շ���ȱʡ�汾��
            return version == null || version.length() == 0 ? defaultVersion : version;
        } catch (Throwable e) { // �������ݴ�
            // �����쳣������ȱʡ�汾��
            LOGGER.error("return default version, ignore exception " + e.getMessage(), e);
            return defaultVersion;
        }
    }

    public static void checkDuplicate(Class<?> cls, boolean failOnError) {
        checkDuplicate(cls.getName().replace('.', '/') + ".class", failOnError);
    }

    public static void checkDuplicate(Class<?> cls) {
        checkDuplicate(cls, false);
    }

    public static void checkDuplicate(String path, boolean failOnError) {
        try {
            // ��ClassPath���ļ�
            Enumeration<URL> urls = ClassHelper.getCallerClassLoader(Version.class).getResources(path);
            Set<String> files = new HashSet<String>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url != null) {
                    String file = url.getFile();
                    if (file != null && file.length() > 0) {
                        files.add(file);
                    }
                }
            }
            // ����ж�����ͱ�ʾ�ظ�
            if (files.size() > 1) {
                String error = "Duplicate class " + path + " in " + files.size() + " jar " + files;
                if (failOnError) {
                    throw new IllegalStateException(error);
                } else {
                    LOGGER.error(error);
                }
            }
        } catch (Throwable e) { // �������ݴ�
            LOGGER.error(e.getMessage(), e);
        }
    }
}
