# Instalação

## Requisitos

- JDK 21 ou superior (`java -version`)
- Maven 3.9 ou superior (`mvn -version`)
- Windows, Linux ou macOS

O Maven baixa as dependências do Spring Boot e do Bouncy Castle na primeira compilação. Não é necessário instalar Postman para executar o programa ou rodar os testes automatizados.

## Compilar e executar

Na raiz do repositório:

```bash
mvn clean verify
mvn spring-boot:run
```

O modo padrão inicia o servidor local e o terminal interativo. O servidor fica disponível em `127.0.0.1:8080`. Para executar somente o backend REST:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--app.cli.enabled=false
```

Também é possível compilar um JAR executável:

```bash
mvn clean package
java -jar target/gerenciador-senhas-seguras-1.0.0.jar
```

No Windows, `compile.bat` executa a validação Maven e `run.bat` inicia o JAR compilado. Em Linux/macOS, use `chmod +x compile.sh && ./compile.sh`.

## Primeiro uso

Na primeira inicialização, crie uma senha mestra com pelo menos 12 caracteres e confirme-a. Em execuções seguintes, desbloqueie o cofre com a mesma senha. Ela não é armazenada em texto simples; se for esquecida, não há recuperação das senhas.

## Arquivos locais

- `data/vault.json`: cofre cifrado
- `data/backups/`: cópias cifradas criadas pelo comando `backup`

Os arquivos ficam fora do controle de versão pelo `.gitignore`. Faça backups adicionais em um local seguro. A API aceita conexões somente em `127.0.0.1`; o cofre não é enviado a serviços externos.

## Solução de problemas

- **`java` não encontrado:** instale o JDK 21 e confira o `PATH`.
- **`mvn` não encontrado:** instale Maven 3.9 ou superior.
- **Porta 8080 ocupada:** encerre o outro processo ou inicie com `--server.port=8081`; o endereço continuará restrito a `127.0.0.1`.
- **Cofre inválido ou senha mestra incorreta:** confirme a senha; se o arquivo foi alterado, restaure uma cópia de segurança válida.
- **Sem entrada de terminal mascarada:** execute em um terminal interativo; em terminais sem `Console`, o programa avisa que as teclas digitadas podem aparecer.
