@ECHO OFF
setlocal

set MAVEN_CMD_LINE_ARGS=%*

set WRAPPER_JAR=%~dp0\.mvn\wrapper\maven-wrapper.jar
if not exist "%WRAPPER_JAR%" (
  echo Could not find %WRAPPER_JAR%
  exit /b 1
)

set JAVA_EXE=
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto execute
set JAVA_EXE=java.exe

:execute
"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS%
exit /b %ERRORLEVEL%
