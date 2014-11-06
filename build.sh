#!/bin/bash
#  Build script to be run by the build server
#

if [[ $# -eq 0 ]] ; then
    echo "Usage: $0 build_number" 
    exit 1
fi

BUILD_NUMBER=$1

git submodule init
git submodule update

./gradlew clean assembleRelease generateReleaseJavaDoc

