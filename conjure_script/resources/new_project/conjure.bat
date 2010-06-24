@echo off

set HOME_DIR=.
set LIB_DIR=%HOME_DIR%\lib

set JARS_PATH=%LIB_DIR%\*

set SRC_DIR=%HOME_DIR%\src
set RESOURCES_DIR=%HOME_DIR%\resources
set TEST_DIR=%HOME_DIR%\test

set DIRS_PATH=%SRC_DIR%;%RESOURCES_DIR%;%TEST_DIR%

set CLASS_PATH=%JARS_PATH%;%DIRS_PATH%

@echo on
@echo %CLASS_PATH%
@echo off

IF (%1)==() (
    java -cp %CLASS_PATH% clojure.lang.Repl
) ELSE (
    java -cp %CLASS_PATH% clojure.main execute.clj %*
)
