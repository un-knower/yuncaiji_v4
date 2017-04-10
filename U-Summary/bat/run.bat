@color 1f
@set _TITLE=U-Summary������
@title %_TITLE%
@set CLASSPATH=%CLASSPATH%;.\lib\U-Summary-1.0.0.0.jar;
@set EXTLIBDIRS=.\lib;

@rem oracle����jar����λ�á�
@set ORACLE_DRIVER_JAR=.\lib\ojdbc6.jar

@rem oracle��binĿ¼·��������ʹ��oci��ʽ��������д��
@set ORACLE_HOME_BIN=

@set CLASSPATH=%CLASSPATH%;%ORACLE_DRIVER_JAR%
@echo ��·��:
@echo %CLASSPATH%
@echo ����������......
@java -server -DoEscapeAnalysis -Xms1024m -Xmx8192m -Djava.ext.dirs=%EXTLIBDIRS%;"%JAVA_HOME%\jre\lib\ext" -cp %CLASSPATH% cn.uway.usummary.Runner 2>.\log\std_err.log
@if %ERRORLEVEL% NEQ 0 goto err
:err
@title �������쳣������ԭ����Ϊ��%_TITLE%����
@color 47
@echo �������쳣����ϸ������鿴logĿ¼�µ���־��
@pause
