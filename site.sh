#!/bin/sh

rm -dfr multiverse-site/build

xsltproc \
 --stringparam chunk.section.depth 0 \
 --stringparam section.autolabel 1 \
 --stringparam section.label.includes.component.label 1 \
 --stringparam html.stylesheet style.css \
 -o multiverse-site/build/site/manual/ /usr/share/xml/docbook/stylesheet/nwalsh/xhtml/chunk.xsl multiverse-site/src/docbook/manual.xml

cp multiverse-site/src/docbook/*.css multiverse-site/build/site/manual

groovy multiverse-site/menu.groovy

mkdir multiverse-site/build/site/charts
cp charts/*.* multiverse-site/build/site/charts

gradle multiverse-gamma:javadoc

cp -r multiverse-gamma/build/docs/javadoc/ multiverse-site/build/site/

