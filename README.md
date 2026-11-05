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
- Exclusão protegida de rascunhos próprios e cancelamento de solicitações pendentes com histórico.
- Administradores elegíveis como participantes sem criação de conta duplicada.
- Perfil com nome, e-mail, telefone/celular e troca de senha.
- Cadastro administrativo de signatários com credencial temporária gerada com `SecureRandom` e convite pelo compartilhamento nativo do Android.
- Página de resumo no PDF concluído com participantes, papéis, status, horários, representações visuais, token, QR Code e hash SHA-256.
- Validação pública por token, leitura de QR Code ou importação do PDF, sem login.
- Preferências de aparência: Sistema, Claro e Escuro.

As contas são criadas pelo usuário pela opção **Criar uma conta** na tela de login. O cadastro permite escolher entre **Administrador** e **Signatário**. Nesta versão, os dados são armazenados localmente no dispositivo.

## Como executar

1. Abra o projeto no Android Studio.
2. Aguarde a sincronização do Gradle.
3. Use um emulador ou aparelho com Android 8.0 (API 26) ou superior.
4. Execute a configuração `app`.
5. Na tela de login, selecione **Criar uma conta** e cadastre um acesso para iniciar o uso.

Para gerar o APK de depuração:

```bash
./gradlew assembleDebug
```

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

O APK é gerado em `app/build/outputs/apk/debug/app-debug.apk`.

## Fluxos principais

### Administrador

1. Crie ou acesse uma conta de administrador.
2. Abra **Novo documento**.
3. Importe um PDF.
4. Informe o título e selecione um ou mais signatários.
5. Defina o papel de cada participante.
6. Posicione os campos de assinatura na página.
7. Revise e envie o documento.
8. Consulte o status e o histórico.

### Signatário

1. Cadastre ou acesse uma conta de signatário com o e-mail vinculado à solicitação.
2. Saia da conta do administrador.
3. Abra um documento recebido.
4. Revise as páginas e desenhe a assinatura.
5. Confirme e exporte o documento assinado.

### Validação pública

1. Na tela de login, abra **Validar documento sem login**.
2. Consulte o token, leia o QR Code do resumo ou selecione o PDF final.
3. A consulta por token confirma a existência do registro. A verificação de integridade só é confirmada quando o PDF completo é analisado e o SHA-256 confere.

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

Integrantes informados no material compartilhado do projeto:

- Alberto da Silva Lima
- Lucas Giovani de Oliveira
- Victor Pedro Gaspar Martins Silva
- Guilherme Santana Paes
- Simara Santos Silva

As funções individuais podem ser detalhadas na planilha conforme a divisão de tarefas.

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
- O cadastro de signatários abre o compartilhamento nativo com uma senha temporária. Firebase, Brevo e envio automático por e-mail ainda exigem configuração de serviço externo e não fazem parte desta versão local.
- A validação pública consulta o banco Room da instalação atual. O QR Code não expõe dados pessoais, mas não substitui um backend público multi-dispositivo.
- A assinatura é uma representação manuscrita local, sem infraestrutura PKI.
- O APK release ainda precisa ser assinado para distribuição final.
- A validação final de PDFs inválidos, estados de erro, acessibilidade e alguns casos de movimentação de campos permanece em andamento.
