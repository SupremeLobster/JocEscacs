@echo off
chcp 65001 > nul
java -Dprism.dirtyopts=false -Dfile.encoding=UTF-8 -jar jar/JocEscacs.jar src/config2.json 3 -g
pause
