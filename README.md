# Gerenciador de Senhas Seguras

Aplicação local em Java 21 para guardar, gerar e validar senhas. O projeto usa Spring Boot e Spring Security, oferece um modo interativo pelo terminal e uma API REST local. O cofre é cifrado no próprio computador; nenhum serviço externo ou conta é necessário.

## Requisitos

- JDK 21 ou superior
- Maven 3.9 ou superior

## Executar

Compile e execute com Maven:

```bash
mvn clean verify
mvn spring-boot:run
```

Na primeira execução, crie uma senha mestra com pelo menos 12 caracteres. O terminal oferece comandos como `add`, `generate`, `list`, `search`, `get`, `update`, `remove`, `validate`, `stats`, `backup`, `backups`, `restore` e `quit`. Digite `help` no programa para ver o uso.

Para gerar e executar o JAR:

```bash
mvn clean package
java -jar target/gerenciador-senhas-seguras-1.0.0.jar
```

Para iniciar somente a API, sem o terminal interativo:

```bash
java -jar target/gerenciador-senhas-seguras-1.0.0.jar --app.cli.enabled=false
```

## API e Postman

A API escuta somente em `http://127.0.0.1:8080`. Consulte [docs/MANUAL_USUARIO.md](docs/MANUAL_USUARIO.md) para autenticação, rotas e importação da coleção em `postman/`. Postman é opcional: o programa e os testes Maven não dependem dele.

## Dados e segurança

O cofre completo é cifrado com AES-256-GCM. A senha mestra deriva a chave com Argon2id. O arquivo local fica em `data/vault.json`; os backups cifrados ficam em `data/backups/`. Esses caminhos são ignorados pelo Git. Faça cópias de segurança dos seus arquivos e mantenha sua senha mestra em um local seguro.

As senhas salvas não são incluídas em listagens ou buscas. A consulta de um item pelo comando `get` ou endpoint `GET /api/v1/entries/{id}` revela a senha após desbloquear o cofre. A API permanece restrita a `127.0.0.1`, e o projeto inclui uma verificação contínua que compila o código e executa os testes JUnit em Java 21.

## Testes

```bash
mvn test
```

Os testes automatizados cobrem regras de senha, geração, cifra do cofre, CRUD, backup/restauração e uma sequência de integração da API. A coleção Postman pode ser executada separadamente para explorar os endpoints.

Veja [docs/INSTALACAO.md](docs/INSTALACAO.md) e [docs/MANUAL_USUARIO.md](docs/MANUAL_USUARIO.md) para mais detalhes.
