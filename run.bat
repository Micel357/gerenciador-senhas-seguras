@echo off
setlocal
set JAR=target\gerenciador-senhas-seguras-1.0.0.jar
if not exist "%JAR%" (
    echo JAR nao encontrado. Execute compile.bat primeiro.
    exit /b 1
)
java -jar "%JAR%" %*
endlocal
