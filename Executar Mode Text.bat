@echo off
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -jar jar/JocEscacs.jar src/config2.json 3
pause
