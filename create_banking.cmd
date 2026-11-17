@echo off
setlocal
cd /D "%~dp0"

set CORE=hbci4j-core-4.1.17.jar
set EXIST_CORE=exist-distribution-5.2.0\lib\exist-core-5.2.0.jar
set XMLDB=exist-distribution-5.2.0\lib\xmldb-api-1.7.0.jar
set CLASSPATH=.;%CORE%;%EXIST_CORE%;%XMLDB%

echo Compiling with %CORE% ...
if exist hbci4java\existdb\*.class del /Q hbci4java\existdb\*.class

javac --release 8 -cp "%CLASSPATH%" hbci4java\existdb\*.java
if errorlevel 1 (
  echo Compile failed, retry with release 11...
  javac --release 11 -cp "%CLASSPATH%" hbci4java\existdb\*.java
  if errorlevel 1 exit /b 1
)

jar cfe hbci4j-existdb-0.6.jar hbci4java.existdb.OnlineBanking hbci4java\existdb\*.class
if errorlevel 1 exit /b 1

echo Packaging XAR 0.6.0 ...
if not exist exist-xar-package\content mkdir exist-xar-package\content
copy /Y %CORE% exist-xar-package\content\hbci4j-core-4.1.17.jar >nul
copy /Y hbci4j-existdb-0.6.jar exist-xar-package\content\hbci4j-existdb-0.6.jar >nul

powershell -NoProfile -Command ^
  "$pkg='exist-xar-package'; $out=Join-Path $pkg 'build'; New-Item -ItemType Directory -Force -Path $out | Out-Null; $zip=Join-Path $out 'hbci4java-0.6.0.xar'; if (Test-Path $zip) { Remove-Item $zip -Force }; Add-Type -AssemblyName System.IO.Compression.FileSystem; $tmp=Join-Path $env:TEMP ('hbci-xar-' + [guid]::NewGuid()); New-Item -ItemType Directory -Force -Path $tmp | Out-Null; Copy-Item (Join-Path $pkg 'expath-pkg.xml') $tmp; Copy-Item (Join-Path $pkg 'exist.xml') $tmp; Copy-Item (Join-Path $pkg 'repo.xml') $tmp -ErrorAction SilentlyContinue; Copy-Item (Join-Path $pkg 'pre-install.xql') $tmp -ErrorAction SilentlyContinue; Copy-Item (Join-Path $pkg 'post-install.xql') $tmp -ErrorAction SilentlyContinue; Copy-Item (Join-Path $pkg 'build.xml') $tmp -ErrorAction SilentlyContinue; New-Item -ItemType Directory -Force -Path (Join-Path $tmp 'content') | Out-Null; Copy-Item (Join-Path $pkg 'content\*') (Join-Path $tmp 'content') -Force; [System.IO.Compression.ZipFile]::CreateFromDirectory($tmp, $zip); Remove-Item $tmp -Recurse -Force; Write-Host ('Created ' + $zip)"

echo Done.
endlocal
