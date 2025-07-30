#!/bin/bash

# Script de compilação para o Gerenciador de Senhas Seguras
# Autor: Projeto ADS - Terceiro Período
# Data: Janeiro 2024

echo "=== Gerenciador de Senhas Seguras - Script de Compilação ==="
echo

# Verifica se o Java está instalado
if ! command -v java &> /dev/null; then
    echo "❌ Java não encontrado. Por favor, instale o Java 11 ou superior."
    exit 1
fi

# Verifica a versão do Java
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ Java 11 ou superior é necessário. Versão atual: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java encontrado: $(java -version 2>&1 | head -n 1)"

# Verifica se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não encontrado. Por favor, instale o Maven 3.6 ou superior."
    echo "   Ubuntu/Debian: sudo apt install maven"
    echo "   CentOS/RHEL: sudo yum install maven"
    echo "   macOS: brew install maven"
    exit 1
fi

echo "✅ Maven encontrado: $(mvn -version | head -n 1)"
echo

# Limpa compilações anteriores
echo "🧹 Limpando compilações anteriores..."
mvn clean > /dev/null 2>&1

# Compila o projeto
echo "🔨 Compilando o projeto..."
if mvn compile > compile.log 2>&1; then
    echo "✅ Compilação bem-sucedida!"
else
    echo "❌ Erro na compilação. Verifique o arquivo compile.log para detalhes."
    exit 1
fi

# Executa os testes
echo "🧪 Executando testes..."
if mvn test > test.log 2>&1; then
    echo "✅ Todos os testes passaram!"
else
    echo "⚠️  Alguns testes falharam. Verifique o arquivo test.log para detalhes."
    echo "   O projeto ainda pode ser executado, mas pode haver problemas."
fi

# Gera o JAR executável
echo "📦 Gerando JAR executável..."
if mvn package > package.log 2>&1; then
    echo "✅ JAR gerado com sucesso!"
    echo "   Arquivo: target/password-manager-1.0.0.jar"
else
    echo "❌ Erro ao gerar JAR. Verifique o arquivo package.log para detalhes."
    exit 1
fi

echo
echo "🎉 Compilação concluída com sucesso!"
echo
echo "Para executar o programa:"
echo "   java -jar target/password-manager-1.0.0.jar"
echo
echo "Para ver a ajuda:"
echo "   java -jar target/password-manager-1.0.0.jar help"
echo
echo "Documentação disponível em:"
echo "   - README.md (documentação técnica)"
echo "   - MANUAL_USUARIO.md (manual do usuário)"
echo

