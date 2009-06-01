@echo off

set HOME_DIR=.
set LIB_DIR=%HOME_DIR%\lib

set CLOJURE_JAR=%LIB_DIR%\clojure-1.0.0.jar
set CLOJURE_CONTRIB_JAR=%LIB_DIR%\clojure-contrib.jar
set DERBY_JAR=%LIB_DIR%\derby.jar
set HTML_PARSER_JAR=%LIB_DIR%\htmlparser.jar
set JETTY_JAR=%LIB_DIR%\jetty-6.1.18.jar
set JETTY_UTIL_JAR=%LIB_DIR%\jetty-util-6.1.18.jar
set SERVLET_API_JAR=%LIB_DIR%\servlet-api-2.5-20081211.jar

set JARS_PATH=%CLOJURE_JAR%;%CLOJURE_CONTRIB_JAR%;%DERBY_JAR%;%HTML_PARSER_JAR%;%JETTY_JAR%;%JETTY_UTIL_JAR%;%SERVLET_API_JAR%

set VENDOR_DIR=%HOME_DIR%\vendor
set APP_DIR=%HOME_DIR%\app
set CONFIG_DIR=%HOME_DIR%\config
set SCRIPT_DIR=%HOME_DIR%\script
set DB_DIR=%HOME_DIR%\db

set DIRS_PATH=%VENDOR_DIR%;%APP_DIR%;%CONFIG_DIR%;%SCRIPT_DIR%;%DB_DIR%

set CLASS_PATH=%JARS_PATH%;%DIRS_PATH%

@echo on
@echo %CLASS_PATH%
@echo off

IF (%1)==() (
    java -cp %CLASS_PATH% jline.ConsoleRunner clojure.lang.Repl
) ELSE (
    java -cp %CLASS_PATH% clojure.lang.Script %1 -- %*
)
