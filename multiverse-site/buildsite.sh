#!/bin/sh

echo "Removed the old site"
rm -dfr target
mkdir target

echo "Generating the javadoc"
pushd .
cd ../multiverse-core
mvn  -DskipTests clean javadoc:javadoc
popd

mkdir target/site/
cp -R ../multiverse-core/target/site/apidocs target/site/apidocs

echo "Generating the website"
groovy menu.groovy
