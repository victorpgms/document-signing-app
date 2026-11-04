
# Document Signing App

Aplicativo mobile nativo para preparação, envio, assinatura e acompanhamento de documentos digitais em formato PDF.

## Sobre o projeto

A aplicação permitirá que administradores configurem documentos e solicitações de assinatura, enquanto signatários poderão consultar e assinar documentos diretamente pelo aplicativo.

## Funcionalidades previstas

- Autenticação de administradores e signatários
- Cadastro e gerenciamento de signatários
- Importação e visualização de documentos PDF
- Posicionamento de campos de assinatura
- Revisão e envio de solicitações
- Assinatura de documentos
- Acompanhamento do status dos documentos

## Tecnologias

- Kotlin e Android nativo
- Jetpack Compose e Material 3
- Room e DataStore
- Coroutines
- Android `PdfRenderer`
- PDFBox-Android para geração do PDF assinado
- JUnit e testes instrumentados Android

## Execução local

1. Abra o projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Execute em um emulador ou aparelho com Android 8.0 (API 26) ou superior.

Contas fictícias:

- Administrador: `victor@fides.local` / `fides1234`
- Signatários: `ana.teste@fides.local` ou `bruno.teste@fides.local` / `fides1234`

O aplicativo funciona localmente na Sprint 1. PDFs e registros são mantidos no armazenamento privado do aplicativo, e a exportação usa o seletor de documentos do Android.

## Organização do desenvolvimento

O desenvolvimento será realizado em equipe, utilizando Git e GitHub.

### Padrão de branches

- `main`: versão integrada e estável
- `feature/`: novas funcionalidades
- `fix/`: correções
- `docs/`: documentação
- `refactor/`: melhorias internas sem alterar funcionalidades

### Padrão de commits

Utilizar Conventional Commits:

- `feat:` nova funcionalidade
- `fix:` correção
- `docs:` documentação
- `refactor:` refatoração
- `test:` testes
- `chore:` manutenção e configuração

### Regras de colaboração

1. Não realizar commits diretamente na main.
2. Criar uma branch para cada tarefa.
3. Consultar a planilha de acompanhamento antes de iniciar.
4. Manter commits pequenos e descritivos.
5. Abrir Pull Request para integrar alterações.
6. Resolver conflitos antes da integração.
7. Utilizar Squash Merge.
8. Atualizar a planilha após concluir as tarefas.

## Equipe

Integrantes a adicionar.

## Status

Em desenvolvimento.
