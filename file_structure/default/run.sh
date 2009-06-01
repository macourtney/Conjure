#!/bin/bash

SCRIPT_DIR=`dirname $0`
HOME_DIR=`(cd $SCRIPT_DIR; pwd)`

LIB_DIR=$HOME_DIR/lib
JARS_PATH=`echo $LIB_DIR/*.jar | sed 's/ /:/g'`

VENDOR_DIR=$HOME_DIR/vendor
APP_DIR=$HOME_DIR/app
CONFIG_DIR=$HOME_DIR/config
SCRIPT_DIR=$HOME_DIR/script
DB_DIR=$HOME_DIR/db

DIRS_PATH=$VENDOR_DIR:$APP_DIR:$CONFIG_DIR:$SCRIPT_DIR:$DB_DIR

CLASSPATH=$JARS_PATH:$DIRS_PATH

if [ -z "$1" ]; then
    COMMAND="java -cp $CLASSPATH clojure.lang.Repl"
else
    scriptname=$1
    COMMAND="java -cp $CLASSPATH clojure.lang.Script $scriptname -- $*"
fi

echo $COMMAND
echo

$COMMAND
