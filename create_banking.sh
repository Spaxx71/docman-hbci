#!/bin/zsh


# Note: updating the version needs to be also done in /exist-xar-package/exist.xml
HOME=`pwd`/hbci4java/build/
export CLASSPATH=.:hbci4j-core-3.1.85.jar:./exist-distribution-5.2.0/lib/exist-core-5.2.0.jar:./exist-distribution-5.2.0/lib/xmldb-api-1.7.0.jar:./jaxb/jaxb-api-2.4.0.jar:./jaxb/jaxb-impl-2.3.6.jar:./jaxb/jaxb-core-3.0.2.jar:./jaxb/activation-1.1.1.jar
#export JDK_JAVA_OPTIONS='--add-modules=javax.xml.bind'

cd $HOME

# cleanup
rm $HOME/hbci4j-existdb-0.5.jar
rm $HOME/hbci4java/existdb/*.class
rm $HOME/exist-xar-package/content/*.jar


echo --- "building hbci4j:"
javac -cp $CLASSPATH -source 8 -target 8 -Xlint:unchecked  hbci4java/existdb/*.java

jar cfe hbci4j-existdb-0.5.jar hbci4java.existdb.OnlineBanking ./hbci4java/existdb/*.class

echo --- "show content of newly build jar:"
jar tf hbci4j-existdb-0.5.jar

echo --- "build exist package:"

# build exist .xar package
cp hbci4j-existdb-0.5.jar ./exist-xar-package/content
cp hbci4j-core-3.1.85.jar ./exist-xar-package/content

# -0 
cd exist-xar-package
zip -r -0 "../../hbci4java-0.5.xar" ./* -x "*.DS_Store"

echo "package building done."
