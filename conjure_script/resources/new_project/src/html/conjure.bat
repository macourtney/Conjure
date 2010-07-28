@echo off

set HOME_DIR=.
set WEB_INF=%HOME_DIR%\WEB-INF
set LIB_DIR=%WEB_INF%\lib

set JARS_PATH=%LIB_DIR%\*

set SRC_DIR=%WEB_INF%\classes

set DIRS_PATH=%SRC_DIR%

set CLASS_PATH=%JARS_PATH%;%DIRS_PATH%

@echo on
@echo %CLASS_PATH%
@echo off

IF (%1)==() (
    java -cp %CLASS_PATH% clojure.lang.Repl
) ELSE (
    java -cp %CLASS_PATH% clojure.main execute.clj %*
)
