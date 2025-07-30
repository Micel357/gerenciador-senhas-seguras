@echo off
REM Script de execução para Windows - Gerenciador de Senhas Seguras
REM Autor: Projeto ADS - Terceiro Período
REM Data: Janeiro 2024

echo === Gerenciador de Senhas Seguras ===
echo.

REM Verifica se o Java está instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java não encontrado. Por favor, instale o Java 11 ou superior.
    echo    Download: https://adoptium.net/
    pause
    exit /b 1
)

echo ✅ Java encontrado
echo.

REM Verifica se o JAR existe
if not exist "target\password-manager-1.0.0.jar" (
    echo ❌ Arquivo JAR não encontrado.
    echo    Execute primeiro: compile.bat ou mvn package
    pause
    exit /b 1
)

echo 🚀 Iniciando o Gerenciador de Senhas...
echo.

REM Executa o programa
java -jar target\password-manager-1.0.0.jar %*

echo.
echo 👋 Programa encerrado.
pause

