@echo off

set HOME_DIR=.
set LIB_DIR=%HOME_DIR%\lib

set JARS_PATH=%LIB_DIR%\*

set VENDOR_DIR=%HOME_DIR%\vendor
set APP_DIR=%HOME_DIR%\app
set CONFIG_DIR=%HOME_DIR%\config
set SCRIPT_DIR=%HOME_DIR%\script
set DB_DIR=%HOME_DIR%\db
set TEST_DIR=%HOME_DIR%\test

set DIRS_PATH=%VENDOR_DIR%;%APP_DIR%;%CONFIG_DIR%;%SCRIPT_DIR%;%DB_DIR%;%TEST_DIR%

set CLASS_PATH=%JARS_PATH%;%DIRS_PATH%

@echo on
@echo %CLASS_PATH%
@echo off

IF (%1)==() (
    java -cp %CLASS_PATH% clojure.lang.Repl
) ELSE (
    java -cp %CLASS_PATH% clojure.main %*
)
