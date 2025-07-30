# Guia de Instalação - Gerenciador de Senhas Seguras

## Requisitos do Sistema

### Obrigatórios
- **Java 11 ou superior** (JRE ou JDK)
- **Sistema Operacional**: Windows 10+, Linux, macOS 10.14+
- **Memória RAM**: Mínimo 512 MB disponível
- **Espaço em Disco**: 50 MB livres

### Para Desenvolvimento (Opcional)
- **Maven 3.6 ou superior**
- **IDE Java** (IntelliJ IDEA, Eclipse, VS Code)

## Instalação do Java

### Windows
1. Acesse: https://adoptium.net/
2. Baixe o **Eclipse Temurin 11** ou superior
3. Execute o instalador e siga as instruções
4. Verifique a instalação:
   ```cmd
   java -version
   ```

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-11-jre
```

### Linux (CentOS/RHEL)
```bash
sudo yum install java-11-openjdk
```

### macOS
```bash
# Com Homebrew
brew install openjdk@11

# Ou baixe de: https://adoptium.net/
```

## Instalação do Maven (Para Desenvolvimento)

### Windows
1. Acesse: https://maven.apache.org/download.cgi
2. Baixe o arquivo ZIP
3. Extraia para `C:\Program Files\Apache\maven`
4. Adicione `C:\Program Files\Apache\maven\bin` ao PATH
5. Verifique: `mvn -version`

### Linux
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven
```

### macOS
```bash
brew install maven
```

## Instalação do Gerenciador de Senhas

### Opção 1: Usar JAR Pré-compilado (Recomendado)
1. Baixe o arquivo `password-manager-1.0.0.jar`
2. Coloque em uma pasta de sua escolha
3. Execute:
   ```bash
   java -jar password-manager-1.0.0.jar
   ```

### Opção 2: Compilar do Código Fonte

#### Windows
1. Baixe/clone o projeto completo
2. Abra o prompt de comando na pasta do projeto
3. Execute:
   ```cmd
   compile.bat
   ```
4. Se bem-sucedido, execute:
   ```cmd
   run.bat
   ```

#### Linux/macOS
1. Baixe/clone o projeto completo
2. Abra o terminal na pasta do projeto
3. Execute:
   ```bash
   ./compile.sh
   ```
4. Se bem-sucedido, execute:
   ```bash
   java -jar target/password-manager-1.0.0.jar
   ```

## Verificação da Instalação

### Teste Básico
Execute o comando:
```bash
java -jar password-manager-1.0.0.jar help
```

Você deve ver a tela de ajuda do programa.

### Teste Completo
1. Execute o programa:
   ```bash
   java -jar password-manager-1.0.0.jar
   ```
2. Configure uma senha mestra de teste
3. Adicione uma senha de teste
4. Liste as senhas
5. Saia do programa

## Solução de Problemas

### "java: command not found"
**Problema**: Java não está instalado ou não está no PATH.

**Solução**:
1. Instale o Java conforme instruções acima
2. Reinicie o terminal/prompt
3. Verifique: `java -version`

### "Error: Could not find or load main class"
**Problema**: Arquivo JAR corrompido ou incompleto.

**Solução**:
1. Baixe novamente o arquivo JAR
2. Ou recompile o projeto:
   ```bash
   mvn clean package
   ```

### "mvn: command not found"
**Problema**: Maven não está instalado (apenas para desenvolvimento).

**Solução**:
1. Instale o Maven conforme instruções acima
2. Ou use o JAR pré-compilado

### Erro de Permissão (Linux/macOS)
**Problema**: Script não tem permissão de execução.

**Solução**:
```bash
chmod +x compile.sh
./compile.sh
```

### Erro de Memória
**Problema**: Java não consegue alocar memória suficiente.

**Solução**:
```bash
java -Xmx512m -jar password-manager-1.0.0.jar
```

## Estrutura de Arquivos Após Instalação

```
pasta-do-projeto/
├── password-manager-1.0.0.jar    # Programa principal
├── README.md                     # Documentação técnica
├── MANUAL_USUARIO.md            # Manual do usuário
├── INSTALACAO.md                # Este arquivo
├── compile.sh                   # Script de compilação (Linux/macOS)
├── compile.bat                  # Script de compilação (Windows)
├── run.bat                      # Script de execução (Windows)
└── src/                         # Código fonte (se baixou o projeto completo)
    ├── main/
    └── test/
```

## Arquivos Criados Durante o Uso

O programa criará estes arquivos na pasta onde for executado:

- `master.auth` - Dados da senha mestra (criptografados)
- `passwords.dat` - Banco de dados de senhas (criptografado)
- `*.backup` - Arquivos de backup (quando solicitados)

### ⚠️ IMPORTANTE
- **Nunca delete** os arquivos `master.auth` e `passwords.dat`
- **Faça backup** destes arquivos regularmente
- **Mantenha seguro** o local onde executa o programa

## Desinstalação

Para remover completamente o programa:

1. **Faça backup** dos seus dados:
   ```bash
   java -jar password-manager-1.0.0.jar backup .final
   ```

2. **Delete os arquivos**:
   - `password-manager-1.0.0.jar`
   - `master.auth`
   - `passwords.dat`
   - `*.backup` (se não quiser manter)

3. **Opcional**: Desinstale o Java se não usar para outras coisas

## Atualizações

Para atualizar para uma nova versão:

1. **Faça backup** dos dados atuais
2. **Substitua** o arquivo JAR pela nova versão
3. **Execute** normalmente (seus dados serão preservados)

## Suporte

Se tiver problemas na instalação:

1. Verifique se atende aos requisitos mínimos
2. Consulte a seção "Solução de Problemas"
3. Verifique se o Java está corretamente instalado
4. Entre em contato com o suporte técnico

---

**Dica**: Mantenha este arquivo junto com o programa para consultas futuras!

