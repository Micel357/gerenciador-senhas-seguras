# Manual do Usuário

## Modo terminal

Execute `mvn spring-boot:run` ou o JAR. O serviço local inicia em `127.0.0.1:8080` e o terminal solicita criar ou informar a senha mestra. Digite `help` para listar comandos.

| Comando | Uso |
| --- | --- |
| `add` | `add gmail pessoa@email.test` e depois informe a senha |
| `generate` | `generate banco pessoa` cria e salva uma senha aleatória |
| `list` | Lista IDs, serviço, usuário e datas, sem exibir senhas |
| `search` | `search banco` busca por serviço ou usuário |
| `get` | `get <id>` mostra os dados e a senha do item escolhido |
| `update` | `update <id>` solicita uma nova senha |
| `remove` | `remove <id>` solicita confirmação antes de apagar |
| `validate` | Solicita uma senha e informa validade e estimativa de força |
| `stats` | Mostra total de itens, serviços únicos e datas |
| `backup` | Cria uma cópia cifrada do cofre |
| `backups` | Lista as cópias locais disponíveis |
| `restore` | `restore <nome-do-arquivo>` restaura e valida uma cópia |
| `quit` | Bloqueia o cofre e encerra o aplicativo |

## API REST local

A API só aceita conexões no computador em que o aplicativo está rodando. Cada pessoa que clonar o projeto executa a aplicação e mantém seus próprios arquivos em `data/`; não há hospedagem ou serviço remoto necessário. Inicie o backend sem CLI com:

```bash
java -jar target/gerenciador-senhas-seguras-1.0.0.jar --app.cli.enabled=false
```

As chamadas usam JSON. O fluxo de sessão é: `GET /api/v1/auth/csrf`, depois `POST /api/v1/vault/initialize` na primeira execução ou `POST /api/v1/vault/unlock` nas seguintes. O cliente deve manter o cookie de sessão e enviar o token CSRF no cabeçalho indicado pela resposta. `POST /api/v1/vault/lock` bloqueia o cofre e invalida a sessão atual.

| Método e rota | Função |
| --- | --- |
| `GET /api/v1/vault/status` | Informa se o cofre existe e está desbloqueado |
| `POST /api/v1/vault/initialize` | Cria o cofre (`masterPassword`, `confirmation`) |
| `POST /api/v1/vault/unlock` | Desbloqueia (`masterPassword`) |
| `POST /api/v1/vault/lock` | Bloqueia o cofre e encerra a sessão atual |
| `GET /api/v1/entries` | Lista resumos; `?query=termo` filtra por serviço ou usuário |
| `POST /api/v1/entries` | Adiciona (`service`, `username`, `password`) |
| `POST /api/v1/entries/generate` | Gera e salva senha (`service`, `username`, opções opcionais) |
| `GET /api/v1/entries/{id}` | Consulta um item e revela sua senha |
| `PUT /api/v1/entries/{id}` | Atualiza a senha (`password`) |
| `DELETE /api/v1/entries/{id}` | Remove um item |
| `POST /api/v1/passwords/validate` | Valida (`password`) e estima sua força |
| `GET /api/v1/stats` | Retorna estatísticas do cofre |
| `GET /api/v1/backups` | Lista os backups cifrados |
| `POST /api/v1/backups` | Cria um backup cifrado |
| `POST /api/v1/backups/{fileName}/restore` | Valida e restaura um backup |

As listagens, buscas, criação e atualização retornam somente resumo sem a senha. O endpoint de consulta por ID é o caminho explícito para revelar uma senha.

## Postman (opcional)

Importe `postman/gerenciador-senhas-seguras.postman_collection.json`. Configure `baseUrl` como `http://127.0.0.1:8080` e defina sua senha mestra em uma variável local privada; não salve nem compartilhe esse valor. A coleção busca o token CSRF, mantém o cookie de sessão e inclui exemplos de desbloqueio, CRUD, geração, validação, estatísticas, backup, restauração e bloqueio. Os dados de exemplo são fictícios.

Postman é apenas um cliente para explorar a API. A aplicação roda pelo terminal sem Postman, e `mvn test` executa os testes automatizados.

## Proteção dos dados

- O cofre é cifrado integralmente com AES-256-GCM e a chave é derivada da senha mestra por Argon2id.
- O arquivo de cofre e os backups nunca devem ser adicionados ao Git ou compartilhados sem proteção.
- A senha mestra não pode ser recuperada.
- A lista e a busca não mostram senhas; a consulta detalhada revela apenas a senha escolhida.
- Use `quit` para bloquear o cofre e encerrar o processo.
