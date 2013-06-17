
@echo off
set JAVA_HOME=..\..\..\cont\eng\bin;
set PATH=%JAVA_HOME%/bin;..\..\..\cont\bin;%PATH%;
set CLASSPATH=%JAVA_HOME%\lib;%JAVA_HOME%\jre\lib;..\..\..\cont\common\lib;
set JLIB=..\..\..\cont\common\lib
set C1=%JLIB%\ant.jar;%JLIB%\commons-collections-3.1.jar;%JLIB%\commons-dbcp-1.2.1.jar;%JLIB%\commons-el.jar;
set C2=%JLIB%\commons-pool-1.2.jar;%JLIB%\jasper-compiler.jar;%JLIB%\jasper-runtime.jar;
set C3=%JLIB%\jsp-api.jar;%JLIB%\naming-common.jar;%JLIB%\naming-factory.jar;%JLIB%\naming-java.jar;
set C4=%JLIB%\naming-resources.jar;%JLIB%\servlet-api.jar;%JLIB%\tools.jar;%JLIB%\commons-httpclient-3.0.1.jar;
set C5=%JLIB%\jcommon-0.9.6.jar;%JLIB%\jfreechart-0.9.21.jar;%JLIB%\jspsmart.jar;%JLIB%\log4j-1.2.9.jar;
set C6=%JLIB%\junit.3.8.1.jar;%JLIB%\commons-codec-1.2.jar;%JLIB%\commons-logging-api.jar;%JLIB%\mail.jar;
set C7=%JLIB%\activation.jar;%JLIB%\jtds-1.2.2.jar;
set CLASSLIB=%C1%;%C2%;%C3%;%C4%;%C5%;%C6%;%C7%

rem del /S/Q lib\qorg.jar
 echo x > lib\qorg.jar
xcopy /C /Y /S /R /E %JLIB%\qorg-*.jar lib\qorg.jar
set QO-LIB=lib\qorg.jar

rem del /S/Q lib\myUtilrg.jar
 echo x > lib\myUtilrg.jar
xcopy /C /Y /E %JLIB%\myUtilrg-*.jar lib\myUtilrg.jar
set MYUTIL-LIB=lib\myUtilrg.jar

rem del /S/Q lib\vrg.jar
 echo x > lib\vrg.jar
xcopy /C /Y /E %JLIB%\vrg-*.jar lib\vrg.jar
set V-LIB=lib\vrg.jar

set CLASSLIB=%C1%;%C2%;%C3%;%C4%;%C5%;%C6%;%C7%;%QO-LIB%;%MYUTIL-LIB%;%V-LIB%


javac -classpath %CLASSLIB%  IVRCall.java


xcopy /C /Y  .\IVRCall.class ..\..\WebRoot\WEB-INF\classes\com\eivr

echo 如果编译成功,请直接重启动windows服务中的小灵呼LCall服务即可生效.
pause