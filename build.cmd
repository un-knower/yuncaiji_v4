@echo off
start mvn clean install -DskipTests
echo "ucloude: mvn clean install -DskipTests"
echo "ucloude: After sub window finished, close it , and press any key to continue" & pause>nul

set VERSION=0.0.1-SNAPSHOT
set BASE_HOME=%~dp0%
set DIST_BIN_DIR=ucloude-uts-%VERSION%-bin

md "%BASE_HOME%\dist"
md "%BASE_HOME%\dist\%DIST_BIN_DIR%"

set UTS_Bin_Dir=%BASE_HOME%dist\%DIST_BIN_DIR%

set Startup_Dir=%BASE_HOME%\ucloude-uts\ucloude-uts-startup
cd %Startup_Dir%
start mvn clean assembly:assembly -DskipTests -Pdefault
echo "ucloude-uts: mvn clean assembly:assembly -DskipTests -Pdefault"
echo "ucloude-uts: After sub window finished, close it , and press any key to continue" & pause>nul
echo "复制文件"
xcopy /e /y "%Startup_Dir%\target\ucloude-uts-bin\ucloude-uts" "%UTS_Bin_Dir%"
cd ..\..\

cd %Startup_Dir%
start mvn clean assembly:assembly -DskipTests -Pucloude-uts-web
echo "ucloude-uts: mvn clean assembly:assembly -DskipTests -Pucloude-uts-web"
echo "ucloude-uts: After sub window finished, close it , and press any key to continue" & pause>nul

xcopy /e /y "%Startup_Dir%\target\ucloude-uts-bin\ucloude-uts\lib" "%UTS_Bin_Dir%\war\jetty\lib"
cd ..\..\
echo "ucloude-uts: copy lib" & pause>nul
xcopy /e /y "%BASE_HOME%\ucloude-uts\ucloude-uts-web\target\ucloude-uts-web-%VERSION%.war" "%UTS_Bin_Dir%\war\ucloude-uts-web.war"
cd ..\..\
echo "ucloude-uts: copy web" & pause>nul