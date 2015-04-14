@echo off

IF %JAVA_HOME%=="" (
    java -jar %~dp0\..\build\libs\memjc.jar %*
) ELSE (
    %JAVA_HOME%\bin\java -jar %~dp0\..\build\libs\memjc.jar %*
)
