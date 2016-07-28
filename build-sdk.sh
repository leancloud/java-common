#!/bin/bash

export PATH=$PATH:/usr/local/bin

set -e

if [ $# != 1 ] ; then
  echo "USAGE: $0 VERSION"
  echo " e.g.: $0 1.4.4"
  exit 1;
fi
version=$1
echo "Building sdk $version..."
##replace VERSIONrsions
mvn versions:set -DnewVersion=$version
mvn versions:commit
sed -i '' "s/sdkVersion = .*;/sdkVersion = \"JavaSDK\/$version\";/" src/main/java/com/avos/avoscloud/internal/impl/DefaultClientConfiguration.java

mvn install
