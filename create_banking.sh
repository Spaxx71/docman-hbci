#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

CORE=hbci4j-core-4.1.17.jar
EXIST_CORE=exist-distribution-5.2.0/lib/exist-core-5.2.0.jar
XMLDB=exist-distribution-5.2.0/lib/xmldb-api-1.7.0.jar
CLASSPATH=".:${CORE}:${EXIST_CORE}:${XMLDB}"

rm -f hbci4java/existdb/*.class
javac --release 8 -cp "$CLASSPATH" hbci4java/existdb/*.java
jar cfe hbci4j-existdb-0.6.jar hbci4java.existdb.OnlineBanking hbci4java/existdb/*.class

mkdir -p exist-xar-package/content exist-xar-package/build
# Keep only the jars referenced by exist.xml
find exist-xar-package/content -type f -name '*.jar' ! -name 'hbci4j-core-4.1.17.jar' ! -name 'hbci4j-existdb-0.6.jar' -delete
cp -f "$CORE" exist-xar-package/content/hbci4j-core-4.1.17.jar
cp -f hbci4j-existdb-0.6.jar exist-xar-package/content/hbci4j-existdb-0.6.jar

(
  cd exist-xar-package
  rm -f build/hbci4java-0.6.0.xar
  zip -r build/hbci4java-0.6.0.xar expath-pkg.xml exist.xml repo.xml pre-install.xql post-install.xql build.xml content
)
echo "Created exist-xar-package/build/hbci4java-0.6.0.xar"
