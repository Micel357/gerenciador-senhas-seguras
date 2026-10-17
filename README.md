# Gerenciador de Senhas Seguras

Projeto planejado como gerenciador de senhas em Java com Maven. **Estado atual:** há configuração Maven, scripts e documentação, mas os arquivos Java descritos nos guias ainda não estão neste repositório. Portanto, não há aplicativo executável nem testes automatizados para rodar neste momento.

## Estrutura atual

```text
.
├── src/
│   ├── main/java/       # Código Java futuro (convenção Maven)
│   └── test/java/       # Testes Java futuros (convenção Maven)
├── tests/               # Orientação sobre os testes
├── docs/
│   ├── INSTALACAO.md
│   └── MANUAL_USUARIO.md
├── compile.sh
├── compile.bat
├── run.bat
├── pom.xml
├── .gitignore
└── README.md
```

O `pom.xml` permanece na raiz, conforme a convenção do Maven. Quando o código for adicionado, a estrutura adequada será `src/main/java/`, `src/main/resources/` e `src/test/java/`.

A arquitetura, os comandos planejados e as instruções estão em [instalação](docs/INSTALACAO.md) e [manual do usuário](docs/MANUAL_USUARIO.md). As pastas `src/main/java/` e `src/test/java/` são estruturas preparadas, sem implementação. Os scripts dependem do código futuro e podem falhar antes disso.
