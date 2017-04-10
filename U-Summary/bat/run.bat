@color 1f
@set _TITLE=U-Summary运行中
@title %_TITLE%
@set CLASSPATH=%CLASSPATH%;.\lib\U-Summary-1.0.0.0.jar;
@set EXTLIBDIRS=.\lib;

@rem oracle驱动jar包的位置。
@set ORACLE_DRIVER_JAR=.\lib\ojdbc6.jar

@rem oracle的bin目录路径，若不使用oci方式，则不需填写。
@set ORACLE_HOME_BIN=

@set CLASSPATH=%CLASSPATH%;%ORACLE_DRIVER_JAR%
@echo 类路径:
@echo %CLASSPATH%
@echo 程序已启动......
@java -server -DoEscapeAnalysis -Xms1024m -Xmx8192m -Djava.ext.dirs=%EXTLIBDIRS%;"%JAVA_HOME%\jre\lib\ext" -cp %CLASSPATH% cn.uway.usummary.Runner 2>.\log\std_err.log
@if %ERRORLEVEL% NEQ 0 goto err
:err
@title 程序发生异常，窗口原标题为“%_TITLE%”。
@color 47
@echo 程序发生异常，详细内容请查看log目录下的日志。
@pause
