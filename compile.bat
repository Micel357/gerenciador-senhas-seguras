@echo off
setlocal

echo === Gerenciador de Senhas Seguras: validacao e compilacao ===
java -version
if errorlevel 1 (
    echo JDK 21 ou superior nao encontrado.
    exit /b 1
)
mvn -version
if errorlevel 1 (
    echo Maven 3.9 ou superior nao encontrado.
    exit /b 1
)
mvn clean verify
if errorlevel 1 exit /b %errorlevel%

echo JAR gerado em target\gerenciador-senhas-seguras-1.0.0.jar
endlocal
