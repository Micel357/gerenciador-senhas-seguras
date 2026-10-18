#!/usr/bin/env sh
set -eu

echo "=== Gerenciador de Senhas Seguras: validação e compilação ==="
command -v java >/dev/null 2>&1 || { echo "JDK 21 ou superior não encontrado." >&2; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "Maven 3.9 ou superior não encontrado." >&2; exit 1; }

java -version
mvn -version
mvn clean verify

echo "JAR gerado em target/gerenciador-senhas-seguras-1.0.0.jar"
