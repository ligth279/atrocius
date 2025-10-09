@echo off
set "JRE_SUBDIR=jre\jdk-25+36-jre"
start "" "%~dp0%JRE_SUBDIR%\bin\java.exe" -jar "%~dp0scheduler-1.0-SNAPSHOT.jar"
