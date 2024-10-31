#!/bin/bash

export PATH=/home/languagetool/grails-cli-6.2.1/bin:$PATH
export PATH=/home/languagetool/jdk-17.0.12/bin/:$PATH
export JAVA_HOME=/home/languagetool/jdk-17.0.12
export GRAILS_HOME=/home/languagetool/grails-cli-6.2.1
export PATH=/home/languagetool/apache-maven-3.9.9/bin/:$PATH
cd /home/languagetool/languagetool && \
  git pull && \
  mvn clean install -DskipTests
cd /home/languagetool/languagetool-community-website2 && ./build-war.sh
