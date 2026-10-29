# Fides — Arquitetura da Sprint 1

## Objetivo

O aplicativo é nativo Android, escrito em Kotlin, com Jetpack Compose e Material 3. A Sprint 1 usa persistência local e não depende de backend, rede ou serviços externos.

## Camadas

### Presentation

Contém as telas Compose, navegação e estado de sessão. As telas não devem acessar DAOs diretamente. Operações persistentes devem passar por repositórios e serviços de domínio.

### Domain

Contém modelos, enums, contratos de repositório, políticas de acesso e regras que não dependem do Android. Os modelos de domínio são separados das entidades Room para permitir uma futura implementação remota.

### Data

Contém Room, DAOs, entidades, migrações, DataStore e implementações locais dos repositórios. O banco guarda metadados e caminhos dos arquivos; os PDFs ficam no armazenamento privado do aplicativo.

### Core

Contém segurança, tema, componentes compartilhados e serviços de PDF. A importação valida o arquivo e copia o conteúdo para o diretório privado antes de qualquer fluxo de edição ou assinatura.

## Fluxos persistentes

1. A autenticação consulta o usuário local e compara a senha derivada com salt individual.
2. A sessão é salva no DataStore e restaurada na abertura do aplicativo.
3. Operações de signatários passam pelo repositório protegido por sessão Admin.
4. Documentos, relações com participantes e campos são gravados em transação Room.
5. Alterações de documento devem gerar nova versão após o envio; o original não deve ser sobrescrito.
6. PDFs importados e versões geradas permanecem em arquivos privados; o banco guarda seus caminhos e metadados.

## Segurança e acesso

- `ADMIN` gerencia signatários e documentos sob sua gestão.
- `SIGNER` acessa somente documentos relacionados ao seu usuário e seus próprios campos.
- A restrição deve existir na camada de negócio/repositório, além de ocultar ou desabilitar controles de interface.
- Senhas não são armazenadas em texto puro.
- Não são solicitadas permissões de armazenamento amplo, rede, câmera, contatos ou localização para o fluxo local.

## Migrações

As alterações estruturais do Room devem incluir migração versionada e schema exportado. A versão atual registra as tabelas de usuários, signatários, documentos, participantes, campos e os metadados de versão do documento.

## PDF

`PdfImportService` copia o URI selecionado pelo Storage Access Framework para `files/documents`, valida a quantidade de páginas com `PdfRenderer` e remove temporários quando a validação falha. A POC da biblioteca e o viewer multipágina permanecem nas tarefas de PDF correspondentes.

## Próximos contratos

As próximas funcionalidades devem preservar os contratos atuais e adicionar casos de uso para criação, envio idempotente, histórico, campos normalizados, assinatura manuscrita, conclusão e exportação pelo seletor Android. Não implementar backend nesta etapa.
