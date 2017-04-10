#!/usr/bin/env bash

VERSION="0.0.1-SNAPSHOT"

UTS_BIN="${BASH_SOURCE-$0}"
echo "$UTS_BIN"
UTS_BIN="$(dirname "${UTS_BIN}")"
echo "$UTS_BIN"
UTS_Bin_Dir="$(cd "${UTS_BIN}"; pwd)"
echo "$UTS_Bin_Dir"
cd $UTS_Bin_Dir
mvn clean install -U -DskipTests
Dist_Bin_Dir="$UTS_Bin_Dir/dist/ucloude-uts-$VERSION-bin"
echo "$Dist_Bin_Dir"
mkdir -p $Dist_Bin_Dir

Dist_Bin_Dir="$(cd "$(dirname "${Dist_Bin_Dir}/.")"; pwd)"

mkdir -p $Dist_Bin_Dir
# 打包
Startup_Dir="$UTS_Bin_Dir/ucloude-uts/ucloude-uts-startup/"
cd $Startup_Dir
mvn clean assembly:assembly -DskipTests -Pdefault

mkdir -p ${Dist_Bin_Dir}/lib
cp -r $UTS_Bin_Dir/lib/*  ${Dist_Bin_Dir}/lib
cp -r $Startup_Dir/target/ucloude-uts-bin/ucloude-uts/*  ${Dist_Bin_Dir}

mkdir -p $Dist_Bin_Dir/war/jetty/lib
mvn clean assembly:assembly -DskipTests -Pucloude-uts-web
cp -rf $Startup_Dir/target/ucloude-uts-bin/ucloude-uts/lib  $Dist_Bin_Dir/war/jetty
cp -rf $UTS_Bin_Dir/ucloude-uts/ucloude-uts-web/target/ucloude-uts-web-$VERSION.war $Dist_Bin_Dir/war/ucloude-uts-web.war

 cd $UTS_Bin_Dir/dist
 zip -r ucloude-uts-$VERSION-bin.zip ucloude-uts-$VERSION-bin/*
 rm -rf ucloude-uts-$VERSION-bin
