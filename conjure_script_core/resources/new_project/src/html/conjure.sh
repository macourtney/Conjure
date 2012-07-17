#!/bin/bash

SCRIPT_DIR=`dirname $0`
HOME_DIR=`(cd $SCRIPT_DIR; pwd)`

WEB_INF_DIR=$HOME_DIR/WEB-INF
LIB_DIR=$WEB_INF_DIR/lib
JARS_PATH=`echo $LIB_DIR/*.jar | sed 's/ /:/g'`

SRC_DIR=$WEB_INF_DIR/classes

DIRS_PATH=$SRC_DIR

CLASSPATH=$JARS_PATH:$DIRS_PATH

if [ -z "$1" ]; then
    COMMAND="java -cp $CLASSPATH clojure.lang.Repl"
else
    COMMAND="java -cp $CLASSPATH clojure.main execute.clj $*"
fi

echo $COMMAND
echo

$COMMAND
