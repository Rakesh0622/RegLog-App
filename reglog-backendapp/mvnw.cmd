@ECHO OFF
setlocal

REM ==== Resolve java ==========================================================
IF NOT DEFINED JAVA_HOME SET JAVA_HOME=
IF NOT "%JAVA_HOME%"=="" (
  SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"
) ELSE (
  FOR /F "delims=" %%I IN ('where java 2^>NUL') DO IF NOT DEFINED JAVA_EXE SET JAVA_EXE=%%I
)
IF NOT DEFINED JAVA_EXE SET JAVA_EXE=java

REM ==== Project base ==========================================================
SET SCRIPT_DIR=%~dp0
IF "%SCRIPT_DIR:~-1%"=="\" SET SCRIPT_DIR=%SCRIPT_DIR:~0,-1%
SET MAVEN_PROJECTBASEDIR=%SCRIPT_DIR%

REM ==== Distribution settings =================================================
SET MVN_VERSION=3.9.9
SET DIST_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip
SET DISTS_BASE=%USERPROFILE%\.m2\wrapper\dists
SET VERSION_DIR=%DISTS_BASE%\apache-maven-%MVN_VERSION%-bin\apache-maven-%MVN_VERSION%
SET MVN_CMD=%VERSION_DIR%\bin\mvn.cmd

REM ==== Reuse a previously downloaded dist ====================================
REM (classic wrapper layout stores it under a hashed sub-folder)
FOR /F "usebackq delims=" %%I IN (
  `dir /b /s "%DISTS_BASE%\apache-maven-%MVN_VERSION%-bin\mvn.cmd" 2^>NUL`
) DO IF NOT DEFINED MVN_CMD SET MVN_CMD=%%I

REM ==== Install Maven if missing ==============================================
IF EXIST "%MVN_CMD%" GOTO run

ECHO Installing Apache Maven %MVN_VERSION% ...
SET ZIP_FILE=%TEMP%\apache-maven-%MVN_VERSION%-bin.zip
powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%DIST_URL%', '%ZIP_FILE%')"
IF ERRORLEVEL 1 (
  ECHO Failed to download Maven distribution.
  EXIT /B 1
)

SET STAGE=%TEMP%\apache-maven-%MVN_VERSION%-stage
IF EXIST "%STAGE%" rmdir /s /q "%STAGE%"
mkdir "%STAGE%"
powershell -NoProfile -Command "Expand-Archive -LiteralPath '%ZIP_FILE%' -DestinationPath '%STAGE%' -Force"
IF ERRORLEVEL 1 (
  ECHO Failed to extract Maven distribution.
  EXIT /B 1
)

IF NOT EXIST "%DISTS_BASE%\apache-maven-%MVN_VERSION%-bin" mkdir "%DISTS_BASE%\apache-maven-%MVN_VERSION%-bin"
IF EXIST "%VERSION_DIR%" rmdir /s /q "%VERSION_DIR%"
MOVE /Y "%STAGE%\apache-maven-%MVN_VERSION%" "%VERSION_DIR%" >NUL
rmdir /s /q "%STAGE%"
DEL /Q "%ZIP_FILE%" 2>NUL
SET MVN_CMD=%VERSION_DIR%\bin\mvn.cmd

:run
REM ==== Invoke Maven ==========================================================
CALL "%MVN_CMD%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" %*
EXIT /B %ERRORLEVEL%