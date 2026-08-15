@echo off
rem ----------------------------------------------------------------------------
rem Gradle startup script for Windows
rem ----------------------------------------------------------------------------

nsetlocal
set DIRNAME=%~dp0
nset WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar
nif exist "%WRAPPER_JAR%" (
  java -jar "%WRAPPER_JAR%" %*
) else (
  echo Gradle wrapper jar not found. Run "gradle wrapper --gradle-version 8.14" to generate it, or install Gradle locally.
  exit /b 1
)
endlocal

