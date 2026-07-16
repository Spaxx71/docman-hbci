@echo off
echo %JAVA_HOME% 

cd /D "D:\setup.git\host3.intersecurity.net\exist\files\db\apps\hbci4java\build"

:cleanup
del .\docman.jar
del .\hbci4java\existdb\*.class

set CLASSPATH=.;hbci4j-core-3.1.43.jar;.\exist-distribution-5.2.0\lib\exist-core-5.2.0.jar;.\exist-distribution-5.2.0\lib\xmldb-api-1.7.0.jar;
 
:javac -Xlint:unchecked ./io/bintellix/docman/*.java
javac -source 8 -target 8 ./hbci4java/existdb/*.java

:javap -v ./hbci4java/existdb/DocmanModule.class | grep "major version"

jar cfe ./hbci4j-existdb-0.5.jar hbci4java.existdb.OnlineBanking ./hbci4java/existdb/*.class
:jar cf ./hbci4j-existdb-0.5.jar ./hbci4java/existdb/*.class

:java -cp %CLASSPATH%;hbci4j-existdb-0.5.jar hbci4java.existdb.OnlineBanking

:install 
:docker cp hbci4java-0.5.xar exist-docman:/exist/autodeploy
:docker restart exist-docman

