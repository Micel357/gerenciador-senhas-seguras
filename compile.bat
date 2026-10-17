@echo off
REM Script de compilação para Windows - Gerenciador de Senhas Seguras
REM Autor: Projeto ADS - Terceiro Período
REM Data: Janeiro 2024

echo === Gerenciador de Senhas Seguras - Compilação ===
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
java -version 2>&1 | findstr "version"

REM Verifica se o Maven está instalado
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven não encontrado. Por favor, instale o Maven 3.6 ou superior.
    echo    Download: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo ✅ Maven encontrado
mvn -version 2>&1 | findstr "Apache Maven"
echo.

REM Limpa compilações anteriores
echo 🧹 Limpando compilações anteriores...
mvn clean >nul 2>&1

REM Compila o projeto
echo 🔨 Compilando o projeto...
mvn compile >compile.log 2>&1
if %errorlevel% neq 0 (
    echo ❌ Erro na compilação. Verifique o arquivo compile.log para detalhes.
    pause
    exit /b 1
)
echo ✅ Compilação bem-sucedida!

REM Executa os testes
echo 🧪 Executando testes...
mvn test >test.log 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  Alguns testes falharam. Verifique o arquivo test.log para detalhes.
    echo    O projeto ainda pode ser executado, mas pode haver problemas.
) else (
    echo ✅ Todos os testes passaram!
)

REM Gera o JAR executável
echo 📦 Gerando JAR executável...
mvn package >package.log 2>&1
if %errorlevel% neq 0 (
    echo ❌ Erro ao gerar JAR. Verifique o arquivo package.log para detalhes.
    pause
    exit /b 1
)
echo ✅ JAR gerado com sucesso!
echo    Arquivo: target\password-manager-1.0.0.jar

echo.
echo 🎉 Compilação concluída com sucesso!
echo.
echo Para executar o programa:
echo    java -jar target\password-manager-1.0.0.jar
echo    ou execute: run.bat
echo.
echo Para ver a ajuda:
echo    java -jar target\password-manager-1.0.0.jar help
echo.
echo Documentação disponível em:
echo    - README.md (documentação técnica)
echo    - docs/MANUAL_USUARIO.md (manual do usuário)
echo.
pause

