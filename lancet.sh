#!/bin/bash

SCRIPT_DIR=`dirname $0`
HOME_DIR=`(cd $SCRIPT_DIR; pwd)`

LIB_DIR=$HOME_DIR/file_structure/default/lib
JARS_PATH=`echo $LIB_DIR/*.jar | sed 's/ /:/g'`

CLASSPATH=.:$JARS_PATH

COMMAND="java -cp $CLASSPATH clojure.lang.Script build.clj"

echo $COMMAND
echo

$COMMAND
