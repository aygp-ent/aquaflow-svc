@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
set MAVEN_HOME=%MAVEN_PROJECTBASEDIR%.mvn\maven
set MAVEN_ZIP=%MAVEN_PROJECTBASEDIR%.mvn\maven.zip
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip

@REM Download Maven if not present
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    if exist "%MAVEN_PROJECTBASEDIR%.mvn\apache-maven-3.9.6\bin\mvn.cmd" (
        set "MAVEN_HOME=%MAVEN_PROJECTBASEDIR%.mvn\apache-maven-3.9.6"
    ) else (
        echo Maven not found. Downloading Apache Maven 3.9.6...
        powershell -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%'"
        echo Extracting Maven...
        powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_PROJECTBASEDIR%.mvn' -Force"
        del "%MAVEN_ZIP%"
        set "MAVEN_HOME=%MAVEN_PROJECTBASEDIR%.mvn\apache-maven-3.9.6"
        echo Maven installed successfully.
    )
)

@REM Set JAVA_HOME if not set
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
    )
)

set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

@REM Run Maven
"%MAVEN_HOME%\bin\mvn.cmd" %*
