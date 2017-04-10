package cn.uway.ucloude.compiler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.uway.ucloude.utils.ClassHelper;
import cn.uway.ucloude.utils.StringUtil;

public abstract class AbstractCompiler implements Compiler {

	private static Compiler COMPILER;

    public static void setCompiler(Compiler compiler) {
        if (compiler == null) {
            throw new IllegalArgumentException("compiler should not be null");
        }
        AbstractCompiler.COMPILER = compiler;
    }

    public static Compiler getCompiler() {
        if (AbstractCompiler.COMPILER == null) {
            AbstractCompiler.COMPILER = new JdkCompiler();
        }
        return AbstractCompiler.COMPILER;
    }

    public static void setCompiler(String compiler) {
        if ("jdk".equals(compiler)) {
            setCompiler(new JdkCompiler());
        } else {
            throw new IllegalArgumentException("compiler[" + compiler + "] error ");
        }
    }

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");

    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");

    public Class<?> compile(String code) {
        code = code.trim();
        Matcher matcher = PACKAGE_PATTERN.matcher(code);
        String pkg;
        if (matcher.find()) {
            pkg = matcher.group(1);
        } else {
            pkg = "";
        }
        matcher = CLASS_PATTERN.matcher(code);
        String cls;
        if (matcher.find()) {
            cls = matcher.group(1);
        } else {
            throw new IllegalArgumentException("No such class name in " + code);
        }
        String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;
        try {
            return Class.forName(className, true, ClassHelper.getCallerClassLoader(getClass()));
        } catch (ClassNotFoundException e) {
            if (!code.endsWith("}")) {
                throw new IllegalStateException("The java code not endsWith \"}\", code: " + code + "");
            }
            try {
                return doCompile(className, code);
            } catch (RuntimeException t) {
                throw t;
            } catch (Throwable t) {
                throw new IllegalStateException("Failed to compile class, cause: " + t.getMessage() + ", class: " + className + ", code: " + code + ", stack: " + StringUtil.toString(t));
            }
        }
    }

    protected abstract Class<?> doCompile(String name, String source) throws Throwable;


}
