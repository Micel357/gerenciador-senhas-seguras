# Manual do Usuário - Gerenciador de Senhas Seguras

> Estado atual: este repositório ainda não contém `src/main` nem `src/test`. Os comandos de compilação e execução dependem da futura adição do código Java.


## Introdução

O Gerenciador de Senhas Seguras é uma aplicação que ajuda você a criar, armazenar e gerenciar suas senhas de forma segura. Todas as senhas são criptografadas e protegidas por uma senha mestra que apenas você conhece.

## Instalação

### Pré-requisitos
- Java 11 ou superior instalado no seu computador
- Arquivo `password-manager-1.0.0.jar` baixado

### Verificando o Java
Abra o terminal/prompt de comando e digite:
```bash
java -version
```

Se aparecer uma versão 11 ou superior, você está pronto para usar o programa.

## Primeira Execução

### Iniciando o Programa
1. Abra o terminal/prompt de comando
2. Navegue até a pasta onde está o arquivo JAR
3. Execute o comando:
```bash
java -jar password-manager-1.0.0.jar
```

### Configuração Inicial
Na primeira execução, você precisará criar uma **senha mestra**:

```
=== Configuração Inicial ===
Esta é a primeira execução. Configure sua senha mestra.
A senha mestra protege todas as suas senhas armazenadas.
IMPORTANTE: Se você esquecer a senha mestra, não será possível recuperar suas senhas!

Digite sua senha mestra: ********
Confirme sua senha mestra: ********
```

#### ⚠️ IMPORTANTE sobre a Senha Mestra
- **Nunca esqueça sua senha mestra!** Não há como recuperá-la
- Use uma senha forte com pelo menos 8 caracteres
- Inclua letras maiúsculas, minúsculas, números e símbolos
- Anote em local seguro se necessário

### Exemplo de Senha Mestra Forte
- ✅ `MinhaSenh@Segura2024!`
- ✅ `C@s@Azul#123`
- ❌ `123456` (muito fraca)
- ❌ `senha` (muito fraca)

## Usando o Programa

### Modo Interativo
Após a autenticação, você verá:
```
=== Modo Interativo ===
Digite 'help' para ver os comandos disponíveis.
Digite 'quit' para sair.

password-manager>
```

### Comandos Básicos

#### 1. Ver Ajuda
```
password-manager> help
```
Mostra todos os comandos disponíveis.

#### 2. Adicionar uma Nova Senha
```
password-manager> add gmail joao@email.com
Adicionando nova entrada para gmail (joao@email.com)
Digite a senha: ********
Senha adicionada com sucesso!
ID: abc123-def456-ghi789
Força da senha: Forte
```

**Explicação:**
- `add` = comando para adicionar
- `gmail` = nome do serviço/site
- `joao@email.com` = seu nome de usuário/email

#### 3. Gerar uma Senha Automática
```
password-manager> generate facebook maria123
Configurar regras de geração? (s/N): N
Senha gerada e adicionada com sucesso!
ID: xyz789-abc123-def456
Senha gerada: K9#mP2$nQ7@vR5!wX8
Força da senha: Muito Forte
```

**Vantagens da geração automática:**
- Cria senhas muito seguras
- Você não precisa pensar em uma senha
- Cada senha é única e aleatória

#### 4. Ver Todas as Suas Senhas
```
password-manager> list
=== Lista de Entradas ===
ID                                   Serviço              Usuário              Criado em
----------------------------------------------------------------------------------------------------
abc123-def456-ghi789                 gmail                joao@email.com       2024-01-15T10:30:45
xyz789-abc123-def456                 facebook             maria123             2024-01-15T10:35:22

Total: 2 entrada(s)
```

#### 5. Buscar Senhas
```
password-manager> search gmail
=== Resultados da Busca: gmail ===
ID                                   Serviço              Usuário              Criado em
----------------------------------------------------------------------------------------------------
abc123-def456-ghi789                 gmail                joao@email.com       2024-01-15T10:30:45

Encontrado(s): 1 entrada(s)
```

#### 6. Ver uma Senha Específica
```
password-manager> get abc123-def456-ghi789
=== Detalhes da Entrada ===
ID: abc123-def456-ghi789
Serviço: gmail
Usuário: joao@email.com
Criado em: 2024-01-15T10:30:45
Atualizado em: 2024-01-15T10:30:45
Senha: MinhaSenh@Gmail123
Força da senha: Forte
```

#### 7. Atualizar uma Senha
```
password-manager> update abc123-def456-ghi789
Atualizando senha para: gmail (joao@email.com)
Digite a nova senha: ********
Senha atualizada com sucesso!
Força da senha: Muito Forte
```

#### 8. Remover uma Senha
```
password-manager> remove abc123-def456-ghi789
Removendo entrada: gmail (joao@email.com)
Tem certeza? (s/N): s
Entrada removida com sucesso!
```

#### 9. Validar uma Senha
```
password-manager> validate
=== Validador de Senhas ===
Digite a senha para validar: ********

=== Resultado da Validação ===
Senha válida (regras padrão): SIM
Força da senha: Muito Forte (95/100)
```

#### 10. Ver Estatísticas
```
password-manager> stats
=== Estatísticas ===
Estatísticas do Armazenamento:
- Total de entradas: 5
- Serviços únicos: 4
- Entrada mais antiga: 2024-01-15T10:30:45
- Entrada mais recente: 2024-01-15T11:45:30
```

#### 11. Fazer Backup
```
password-manager> backup
Backup criado com sucesso!
```

#### 12. Sair do Programa
```
password-manager> quit
Sessão encerrada com segurança.
```

## Dicas de Uso

### 1. Organizando suas Senhas
Use nomes descritivos para os serviços:
- ✅ `gmail-pessoal`
- ✅ `gmail-trabalho`
- ✅ `banco-santander`
- ✅ `netflix`

### 2. Nomes de Usuário
Seja específico com os nomes de usuário:
- ✅ `joao.silva@email.com`
- ✅ `joaosilva123`
- ❌ `usuario` (muito genérico)

### 3. Backup Regular
Faça backup regularmente:
```
password-manager> backup .2024-01-15
```
Isso cria um backup com a data no nome.

### 4. Senhas Fortes
Características de uma senha forte:
- **Comprimento**: Pelo menos 12 caracteres
- **Variedade**: Maiúsculas, minúsculas, números, símbolos
- **Única**: Diferente para cada serviço
- **Aleatória**: Sem padrões óbvios

### 5. Usando IDs
- Cada senha tem um ID único
- Use o comando `list` para ver os IDs
- Copie e cole o ID para evitar erros

## Configuração Avançada

### Regras de Geração Personalizadas
Quando usar `generate`, você pode personalizar:

```
password-manager> generate banco-itau minha.conta
Configurar regras de geração? (s/N): s
Comprimento mínimo (padrão 12): 16
Comprimento máximo (padrão 64): 20
Incluir maiúsculas? (S/n): S
Incluir minúsculas? (S/n): S
Incluir números? (S/n): S
Incluir caracteres especiais? (S/n): S
Caracteres especiais (padrão: !@#$%^&*()_+-=[]{}|;:,.<>?): !@#$%
```

### Comandos Diretos (Sem Modo Interativo)
Você também pode executar comandos diretamente:

```bash
# Listar senhas
java -jar password-manager-1.0.0.jar list

# Adicionar senha
java -jar password-manager-1.0.0.jar add instagram meuusuario

# Buscar senhas
java -jar password-manager-1.0.0.jar search banco
```

## Segurança

### O que o Programa Protege
- ✅ Suas senhas são criptografadas com AES-256
- ✅ A senha mestra nunca é armazenada em texto puro
- ✅ Cada criptografia usa um código único (IV)
- ✅ Os dados ficam apenas no seu computador

### O que Você Deve Proteger
- 🔒 **Senha Mestra**: Nunca compartilhe com ninguém
- 🔒 **Arquivo de Dados**: Faça backup em local seguro
- 🔒 **Computador**: Use antivírus e mantenha atualizado

### Sinais de Senhas Fracas
O programa avisa quando uma senha é fraca:
```
AVISO: Senha não atende aos critérios de segurança recomendados.
```

## Solução de Problemas

### Esqueci a Senha Mestra
**Infelizmente, não há como recuperar.** Você precisará:
1. Deletar os arquivos `master.auth` e `passwords.dat`
2. Começar do zero
3. **Por isso é importante fazer backup da senha mestra!**

### Erro ao Iniciar
```bash
# Verifique se o Java está instalado
java -version

# Verifique se o arquivo existe
ls -la password-manager-1.0.0.jar

# Execute com mais informações de erro
java -jar password-manager-1.0.0.jar
```

### Arquivo Corrompido
Se aparecer erro de arquivo corrompido:
1. Restaure do backup mais recente
2. Use o comando: `backup .pre-restore`

### Senha Mestra Não Aceita
- Verifique se não há Caps Lock ativado
- Certifique-se de digitar exatamente como configurou
- Lembre-se que senhas são sensíveis a maiúsculas/minúsculas

## Arquivos Criados

O programa cria estes arquivos na pasta onde está executando:

- `master.auth` - Informações da senha mestra (criptografadas)
- `passwords.dat` - Suas senhas (criptografadas)
- `passwords.dat.backup` - Backup automático (quando usar comando backup)

### ⚠️ IMPORTANTE
- **Nunca delete** estes arquivos manualmente
- **Faça backup** destes arquivos regularmente
- **Não compartilhe** estes arquivos com ninguém

## Boas Práticas

### 1. Rotina de Uso
1. Abra o programa
2. Autentique com senha mestra
3. Faça suas operações
4. Saia com `quit` (salva automaticamente)

### 2. Backup Semanal
```
password-manager> backup .semana-01
password-manager> backup .semana-02
```

### 3. Senhas Únicas
- Use uma senha diferente para cada serviço
- Nunca reutilize senhas importantes
- Use o gerador automático sempre que possível

### 4. Atualização Regular
- Atualize senhas importantes a cada 3-6 meses
- Use o comando `update` para isso

## Suporte

Se tiver problemas:
1. Consulte este manual
2. Verifique se seguiu todos os passos
3. Certifique-se que o Java está atualizado
4. Entre em contato com o suporte técnico

---

**Lembre-se: A segurança das suas senhas depende principalmente de manter sua senha mestra segura e fazer backups regulares!**

