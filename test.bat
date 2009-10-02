@echo off

set HOME_DIR=.
set LIB_DIR=%HOME_DIR%/file_structure/default/lib

set JARS_PATH=%LIB_DIR%\*

set CLASS_PATH=.;%JARS_PATH%

REM @echo on
REM @echo %CLASS_PATH%
REM @echo off

java -cp %CLASS_PATH% clojure.main test.clj %*