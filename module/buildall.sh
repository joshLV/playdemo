#!/bin/bash

# 必须安装 expect 软件包
BUILDMODULE_EXPECT=`pwd`/buildmodule.expect

for dir in `find . -name build.xml`
do
  path=`dirname $dir`
  if [ $path != . ]
  then
    echo $path
    cd $path
    $BUILDMODULE_EXPECT
    cd -
  fi
done