#!/bin/sh

export PATH=/home/languagetool/jdk-17.0.12/bin/:$PATH
export JAVA_HOME=/home/languagetool/jdk-17.0.12
java -jar /home/languagetool/languagetool-community-website2/build/libs/languagetool-community-website2-0.1.war
