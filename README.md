# Gerenciador de Senhas Seguras

Um gerenciador de senhas robusto e seguro desenvolvido em Java, com funcionalidades de geração, validação e armazenamento criptografado de senhas.

## Características Principais

- **Geração de Senhas Seguras**: Gera senhas aleatórias baseadas em regras customizáveis
- **Validação de Força**: Avalia a força das senhas e fornece sugestões de melhoria
- **Criptografia AES-GCM**: Armazenamento seguro com criptografia de nível militar
- **Interface CLI**: Interface de linha de comando intuitiva e completa
- **Autenticação por Senha Mestra**: Proteção adicional com senha mestra
- **Backup e Restauração**: Funcionalidades de backup para proteção de dados

## Requisitos do Sistema

- Java 11 ou superior
- Maven 3.6 ou superior
- Sistema operacional: Windows, Linux ou macOS

## Instalação e Compilação

1. Clone ou baixe o projeto
2. Navegue até o diretório do projeto
3. Compile o projeto:
```bash
mvn clean compile
```

4. Execute os testes:
```bash
mvn test
```

5. Gere o JAR executável:
```bash
mvn package
```

## Execução

### Modo Interativo
```bash
java -jar target/password-manager-1.0.0.jar
```

### Comandos Diretos
```bash
# Adicionar uma nova senha
java -jar target/password-manager-1.0.0.jar add gmail usuario@email.com

# Gerar uma nova senha
java -jar target/password-manager-1.0.0.jar generate facebook meuusuario

# Listar todas as senhas
java -jar target/password-manager-1.0.0.jar list

# Buscar senhas
java -jar target/password-manager-1.0.0.jar search gmail
```

## Comandos Disponíveis

### Gerenciamento de Senhas
- `add <serviço> <usuário>` - Adiciona uma nova senha
- `generate <serviço> <usuário>` - Gera e adiciona uma nova senha
- `get <id>` - Mostra uma senha específica
- `list` - Lista todas as entradas
- `search <termo>` - Busca entradas por termo
- `update <id>` - Atualiza uma senha existente
- `remove <id>` - Remove uma entrada

### Utilitários
- `validate` - Valida a força de uma senha
- `stats` - Mostra estatísticas do armazenamento
- `backup [sufixo]` - Cria backup dos dados
- `help` - Mostra ajuda completa

## Segurança

### Criptografia
- **Algoritmo**: AES-256-GCM (Advanced Encryption Standard)
- **Derivação de Chave**: PBKDF2 com SHA-256 (100.000 iterações)
- **Autenticação**: GCM fornece autenticação integrada
- **IV Aleatório**: Cada criptografia usa um IV único

### Senha Mestra
- Mínimo de 8 caracteres
- Recomendado: pelo menos 3 tipos de caracteres (maiúsculas, minúsculas, números, especiais)
- Armazenada como hash PBKDF2 com salt aleatório
- Nunca armazenada em texto puro

### Armazenamento
- Senhas criptografadas individualmente
- Metadados não sensíveis em JSON
- Arquivo de dados local (não enviado para nuvem)
- Funcionalidade de backup manual

## Estrutura do Projeto

```
password-manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mycompany/passwordmanager/
│   │   │       ├── Main.java
│   │   │       ├── model/
│   │   │       │   ├── PasswordEntry.java
│   │   │       │   └── PasswordRules.java
│   │   │       ├── service/
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── EncryptionService.java
│   │   │       │   ├── PasswordGenerator.java
│   │   │       │   ├── PasswordManager.java
│   │   │       │   └── PasswordValidator.java
│   │   │       ├── storage/
│   │   │       │   └── PasswordStorage.java
│   │   │       └── cli/
│   │   │           └── CLIHandler.java
│   │   └── resources/
│   └── test/
│       └── java/
│           └── com/mycompany/passwordmanager/
│               └── service/
│                   ├── EncryptionServiceTest.java
│                   ├── PasswordGeneratorTest.java
│                   └── PasswordValidatorTest.java
├── pom.xml
└── README.md
```

## Exemplos de Uso

### Primeira Execução
```
=== Gerenciador de Senhas Seguras ===
Versão 1.0.0

=== Configuração Inicial ===
Esta é a primeira execução. Configure sua senha mestra.
Digite sua senha mestra: ********
Confirme sua senha mestra: ********
Senha mestra configurada com sucesso!
```

### Adicionando uma Senha
```
password-manager> add gmail joao@email.com
Adicionando nova entrada para gmail (joao@email.com)
Digite a senha: ********
Senha adicionada com sucesso!
ID: abc123-def456-ghi789
Força da senha: Forte
```

### Gerando uma Senha
```
password-manager> generate facebook maria123
Configurar regras de geração? (s/N): s
Comprimento mínimo (padrão 12): 16
Comprimento máximo (padrão 64): 20
Incluir maiúsculas? (S/n): S
Incluir minúsculas? (S/n): S
Incluir números? (S/n): S
Incluir caracteres especiais? (S/n): S

Senha gerada e adicionada com sucesso!
ID: xyz789-abc123-def456
Senha gerada: K9#mP2$nQ7@vR5!wX8
Força da senha: Muito Forte
```

## Arquitetura

### Componentes Principais

1. **PasswordManager**: Coordena todas as operações e atua como fachada
2. **PasswordGenerator**: Gera senhas seguras baseadas em regras customizáveis
3. **PasswordValidator**: Valida força e conformidade das senhas
4. **EncryptionService**: Fornece criptografia AES-GCM e hashing PBKDF2
5. **AuthService**: Gerencia autenticação e senha mestra
6. **PasswordStorage**: Persistência em JSON com busca e indexação
7. **CLIHandler**: Interface de linha de comando

### Fluxo de Dados

1. **Autenticação**: Usuário fornece senha mestra
2. **Derivação de Chave**: PBKDF2 deriva chave de criptografia
3. **Operação**: Usuário executa comando (add, get, etc.)
4. **Criptografia**: Senhas são criptografadas antes do armazenamento
5. **Persistência**: Dados salvos em arquivo JSON local
6. **Descriptografia**: Senhas descriptografadas apenas quando solicitadas

## Desenvolvimento

### Executando Testes
```bash
mvn test
```

### Gerando Relatório de Cobertura
```bash
mvn jacoco:report
```

### Análise de Código
```bash
mvn spotbugs:check
```

## Contribuição

1. Faça fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## Licença

Este projeto é licenciado sob a MIT License - veja o arquivo LICENSE para detalhes.

## Suporte

Para suporte e dúvidas:
- Abra uma issue no repositório
- Consulte a documentação técnica
- Verifique os testes unitários para exemplos de uso

## Roadmap

### Versão 1.1
- [ ] Interface gráfica (GUI)
- [ ] Importação/exportação de senhas
- [ ] Gerador de senhas pronunciáveis
- [ ] Análise de senhas duplicadas

### Versão 1.2
- [ ] Sincronização em nuvem
- [ ] Autenticação de dois fatores
- [ ] Histórico de senhas
- [ ] Notificações de expiração

### Versão 2.0
- [ ] API REST
- [ ] Aplicativo móvel
- [ ] Extensão para navegadores
- [ ] Compartilhamento seguro de senhas

