#!/bin/zsh


# Note: updating the version needs to be also done in /exist-xar-package/exist.xml
HOME=`pwd`/
export CLASSPATH=.:hbci4j-core-3.1.85.jar:./exist-distribution-5.2.0/lib/exist-core-5.2.0.jar:./exist-distribution-5.2.0/lib/xmldb-api-1.7.0.jar:./jaxb/jaxb-api-2.4.0.jar:./jaxb/jaxb-impl-2.3.6.jar:./jaxb/jaxb-core-3.0.2.jar:./jaxb/activation-1.1.1.jar
#export JDK_JAVA_OPTIONS='--add-modules=javax.xml.bind'

cd $HOME

#cleanup
ls /tmp/passport_*.dat
rm /tmp/passport_*.dat

ls /etc/passport_*.dat
rm /etc/passport_*.dat

ls $HOME/passport.dat
rm $HOME/passport.dat

echo --- "building test:"
echo HOME = $HOME
pwd
javac -cp $CLASSPATH -source 8 -target 8 -Xlint:unchecked Test.java

echo --- "start HBCI/FinTS test"
#java -cp $CLASSPATH hbci4java.existdb.OnlineBanking A1 10020030 DEDBDEUBA DE997007002412345678 123456789 123456789 123456789 null null null null#java hbci4java.existdb.HttpMessageHandler https://docman.bintellix.com/api/bintellix/hbci/12345678/send.xml https://docman.bintellix.com/api/bintellix/hbci/12345678/receive.xml bix-accounting bix-acc
java -cp $CLASSPATH Test

#sudo mv /home/admin/hbci4j-existdb-0.5.jar /web/services/prog/exist-5.2.0/data/expathrepo/hbci4java-0.5/content/

#docker cp hbci4java-0.5.xar exist-docman:/exist/autodeploy
#docker restart exist-docman
#docker logs -n1000 exist-docman
##/var/lib/docker/overlay2/*/*/exist/logs/expath-repo.log