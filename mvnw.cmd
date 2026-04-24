@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements. See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership. The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License. You may obtain a copy of the License at
@REM
@REM   https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied. See the License for the
@REM specific language governing permissions and limitations
@REM under the License.

@REM Apache Maven Wrapper Script for Windows — version 3.2.0

@echo off
setlocal

set SCRIPT_DIR=%~dp0
set PROPS=%SCRIPT_DIR%.mvn\wrapper\maven-wrapper.properties

for /f "tokens=2 delims==" %%a in ('findstr "distributionUrl" "%PROPS%"') do set DISTRIBUTION_URL=%%a

for %%f in ("%DISTRIBUTION_URL%") do set DIST_FILENAME=%%~nxf
set DIST_NAME=%DIST_FILENAME:.zip=%
set MVN_CACHE=%USERPROFILE%\.m2\wrapper\dists\%DIST_NAME%
set MVN_BIN=%MVN_CACHE%\bin\mvn.cmd

if not exist "%MVN_BIN%" (
    echo Downloading Maven from %DISTRIBUTION_URL%
    if not exist "%MVN_CACHE%" mkdir "%MVN_CACHE%"
    set TMP_ZIP=%MVN_CACHE%\download.zip
    powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MVN_CACHE%\download.zip'"
    powershell -Command "Expand-Archive -Path '%MVN_CACHE%\download.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\' -Force"
    del "%MVN_CACHE%\download.zip"
    for /d %%d in ("%USERPROFILE%\.m2\wrapper\dists\apache-maven-*") do (
        if not "%%d"=="%MVN_CACHE%" ren "%%d" "%DIST_NAME%"
    )
)

"%MVN_BIN%" %*
endlocal
