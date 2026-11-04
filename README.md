# Fides

Aplicativo Android nativo para preparar, enviar, assinar e acompanhar documentos PDF em um fluxo local.

## O que o app faz

O Fides possui dois perfis:

- Administrador: cadastra signatários, importa PDFs, configura campos de assinatura, revisa, envia e acompanha documentos.
- Signatário: acessa somente documentos vinculados ao seu e-mail, visualiza o PDF, assina e exporta o resultado.

O fluxo atual é local. PDFs, usuários, signatários, eventos e versões são armazenados no banco Room privado do aplicativo. Não há backend remoto nesta versão.

## Funcionalidades atuais

- Login local com senha protegida por hash.
- Cadastro local de administradores e signatários.
- Seleção do tipo de conta no cadastro.
- Controle de acesso por perfil.
- Cadastro, edição, listagem e seleção de signatários.
- Importação e visualização de PDFs.
- Seleção de papéis: contratante, contratado e testemunha.
- Inclusão, posicionamento e exclusão de campos de assinatura.
- Revisão e envio local de documentos.
- Histórico, status parcial e status concluído.
- Assinatura manuscrita local.
- Geração de nova versão após assinatura.
- Visualização e exportação do PDF final.
- Preferências de aparência: Sistema, Claro e Escuro.

## Contas fictícias para demonstração

As contas abaixo são locais e fictícias. Não use senhas ou e-mails pessoais no aplicativo.

### Administradores

| Nome | E-mail | Senha |
| --- | --- | --- |
| Victor Almeida | `admin.victor@fides.local` | `fides1234` |
| Lucas | `admin.lucas@fides.local` | `fides1234` |

### Signatários

| Nome | E-mail | Senha |
| --- | --- | --- |
| Ana Teste | `ana.teste@fides.local` | `fides1234` |
| Bruno Teste | `bruno.teste@fides.local` | `fides1234` |

Os usuários podem criar uma nova conta pela opção **Criar uma conta** na tela de login. O cadastro permite escolher entre **Administrador** e **Signatário**. Essa abertura de cadastro é adequada para a demonstração local; em uma versão com backend, a criação de administradores deverá ter uma política de autorização própria.

## Como executar

1. Abra o projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Use um emulador ou aparelho com Android 8.0 (API 26) ou superior.
4. Execute a configuração `app`.
5. Na tela de login, use uma conta fictícia ou crie uma nova conta.

Para gerar o APK de depuração:

```bash
./gradlew assembleDebug
```

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

O APK é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Roteiros rápidos de demonstração

### Administrador

1. Entre com `admin.victor@fides.local`.
2. Abra **Novo documento**.
3. Importe um PDF.
4. Informe o título e selecione um ou mais signatários.
5. Defina o papel de cada participante.
6. Posicione os campos de assinatura na página.
7. Revise e envie o documento.
8. Consulte o status e o histórico.

### Signatário

1. Saia da conta do administrador.
2. Entre com `ana.teste@fides.local`.
3. Abra um documento vinculado ao e-mail.
4. Revise as páginas e desenhe a assinatura.
5. Confirme e exporte o documento assinado.

## Arquitetura

- `presentation`: telas Compose, navegação e estado visual.
- `domain`: modelos, contratos, regras e casos de uso.
- `data`: Room, DAOs, entidades, mapeadores e repositórios locais.
- `core`: segurança, PDF, tema e utilitários compartilhados.

Tecnologias principais:

- Kotlin.
- Jetpack Compose e Material 3.
- Room.
- Coroutines e StateFlow.
- Android `PdfRenderer`.
- PDFBox-Android para geração do PDF assinado.
- JUnit e testes instrumentados Android.

## Equipe

Integrantes identificados no repositório e no histórico de desenvolvimento:

- Victor — mantenedor do repositório e responsável pela integração.
- Lucas (`lucasgiovani97`) — colaborador responsável pelo PR do fluxo local de assinatura.

Não foram adicionados outros nomes sem confirmação no repositório ou na planilha.

## Git e colaboração

- `main`: versão integrada e estável.
- `feature/`: novas funcionalidades.
- `fix/`: correções.
- `docs/`: documentação.
- `test/`: validações isoladas.

Use commits pequenos no padrão Conventional Commits:

```text
feat(auth): add local account registration
docs(project): improve README usage guide
test(auth): validate account registration
```

Não faça commits diretamente na `main`. Abra um Pull Request, valide o build e atualize a planilha após a integração.

## Limitações conhecidas

- O envio é local e não usa servidor remoto.
- A assinatura é uma representação manuscrita local, sem infraestrutura PKI.
- O APK release ainda precisa ser assinado para distribuição final.
- A validação final de PDFs inválidos, estados de erro, acessibilidade e alguns casos de movimentação de campos permanece em andamento.
