#!/bin/bash

version=0.1.3

mkdir -p dist

## compile java files
echo "Compiling java files"
mkdir -p bin
javac -d bin -cp ..:../release/linux/lib/swt.jar:../release/linux/lib/RXTXcomm.jar ../jifi/Jifi.java

############################## linux ##################################
linux_dir=jifi_linux32
mkdir -p ${linux_dir}

## compile launcher
g++ src/linux_launcher.cpp -o linux/Jifi

## moves files
cp -r bin/* ${linux_dir}
cp linux/* ${linux_dir}
mkdir -p ${linux_dir}/lib
cp linux/lib/* ${linux_dir}/lib

echo "Making Linux tar"
tar -czf dist/jifi_${version}_linux32.tar.gz ${linux_dir}

rm -rf ${linux_dir}

############################## windows #######################################
win_dir=jifi_win32
mkdir -p ${win_dir}

## moves files
cp -r bin/* ${win_dir}
cp windows/* ${win_dir}
mkdir -p ${win_dir}/lib
cp windows/lib/* ${win_dir}/lib

echo "Making Windows Zip"
zip -q dist/jifi_${version}_win32.zip -r ${win_dir}

rm -rf ${win_dir}

rm -rf bin

