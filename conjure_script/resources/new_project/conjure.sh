#!/bin/bash

SCRIPT_DIR=`dirname $0`
HOME_DIR=`(cd $SCRIPT_DIR; pwd)`

LIB_DIR=$HOME_DIR/lib
JARS_PATH=`echo $LIB_DIR/*.jar | sed 's/ /:/g'`

SRC_DIR=$HOME_DIR/src
RESOURCES_DIR=$HOME_DIR/resources
TEST_DIR=$HOME_DIR/test

DIRS_PATH=$SRC_DIR:$RESOURCES_DIR:$TEST_DIR

CLASSPATH=$JARS_PATH:$DIRS_PATH

if [ -z "$1" ]; then
    COMMAND="java -cp $CLASSPATH clojure.lang.Repl"
else
    COMMAND="java -cp $CLASSPATH clojure.main execute.clj $*"
fi

echo $COMMAND
echo

$COMMAND
