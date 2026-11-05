# Fides

## Assinaturas de documentos com clareza, controle e confiança

O Fides é um aplicativo Android nativo criado para simplificar o ciclo de vida de documentos PDF que precisam ser preparados, assinados e acompanhados.

Em um único fluxo, o usuário pode importar um documento, selecionar os participantes, definir exatamente onde cada assinatura será inserida, enviar a solicitação, acompanhar o andamento e validar o resultado final.

> Uma experiência de assinatura organizada, visual e acessível, com os dados armazenados localmente no dispositivo.

## Por que usar o Fides?

- **Fluxo completo:** da preparação do PDF à exportação do documento assinado.
- **Controle por perfil:** administradores gerenciam documentos e participantes; signatários acessam apenas o que está vinculado a eles.
- **Precisão visual:** os campos de assinatura são posicionados diretamente sobre as páginas do PDF.
- **Rastreabilidade:** eventos, versões, status e participantes ficam registrados no histórico do documento.
- **Validação integrada:** documentos concluídos podem ser consultados por código, QR Code ou pelo próprio PDF.
- **Privacidade local:** nesta versão, os dados permanecem no armazenamento privado do aplicativo.

## Experiência do produto

O Fides foi desenhado para tornar um processo normalmente fragmentado mais simples de entender e executar.

- Interface construída com Jetpack Compose e Material 3.
- Identidade visual baseada em azul, roxo, amarelo e fundo claro, com suporte a tema escuro.
- Navegação organizada por contexto: documentos, signatários, perfil e acompanhamento.
- Idioma padrão em inglês, com suporte a português conforme a configuração do aparelho.
- Ícone e elementos visuais próprios da marca Fides.

## Funcionalidades

### Preparação e gerenciamento de documentos

- Importação e visualização de arquivos PDF.
- Definição de título e identificação do documento.
- Suporte a documentos com múltiplas páginas.
- Criação, edição e exclusão protegida de rascunhos próprios.
- Cancelamento de solicitações pendentes com preservação do histórico.
- Criação de nova versão para alterações posteriores ao envio.
- Preservação do arquivo original durante o fluxo.

### Participantes e campos de assinatura

- Cadastro administrativo de signatários.
- Seleção de administradores elegíveis como participantes, sem duplicação de contas.
- Exibição de nome, e-mail e telefone dos participantes.
- Definição do papel no documento: contratante, contratado ou testemunha.
- Um ou mais participantes por documento.
- Vários campos de assinatura para o mesmo participante.
- Posicionamento dos campos diretamente na página do PDF.
- Validação para manter os campos dentro dos limites da página.
- Movimentação e exclusão de campos antes do envio.
- Revisão do documento, participantes e campos antes da solicitação.

### Assinatura e acompanhamento

- Assinatura manuscrita realizada no próprio aplicativo.
- Acesso do signatário condicionado ao e-mail vinculado à solicitação.
- Bloqueio de novas assinaturas em solicitações canceladas.
- Conclusão somente depois das assinaturas obrigatórias.
- Status de rascunho, aguardando assinaturas, parcialmente assinado, concluído e cancelado.
- Histórico de eventos e responsáveis.
- Visualização e exportação do PDF final.

### Acesso e contas

- Cadastro local de administradores e signatários.
- Escolha do tipo de conta no cadastro.
- Autenticação com senha protegida por hash.
- Controle de acesso entre os perfis.
- Perfil com nome, e-mail e telefone/celular.
- Atualização de dados de contato e troca de senha.
- Geração de credencial temporária para novos signatários.
- Compartilhamento nativo das instruções de acesso pelo Android.

### Validação e autenticidade

- Geração de identificador de validação para documentos concluídos.
- Hash SHA-256 para verificação da integridade do PDF analisado.
- QR Code contendo somente o token de validação.
- Página de resumo adicionada ao final do documento concluído.
- Resumo com participantes, papéis, status, datas, horários, assinaturas e código de validação.
- Consulta sem login por token, QR Code ou importação do PDF.
- Diferenciação entre registro encontrado e integridade criptográfica confirmada.

## Fluxos principais

### Para administradores

1. Criar ou acessar uma conta de administrador.
2. Cadastrar os signatários necessários.
3. Criar um novo documento e importar o PDF.
4. Definir título, participantes e papéis.
5. Posicionar os campos de assinatura nas páginas.
6. Revisar a configuração e enviar a solicitação.
7. Acompanhar status, histórico e assinaturas pendentes.
8. Consultar e exportar o documento concluído.

### Para signatários

1. Criar ou acessar uma conta com o e-mail vinculado à solicitação.
2. Visualizar os documentos recebidos.
3. Abrir o PDF e revisar as páginas.
4. Desenhar e confirmar a assinatura.
5. Consultar o status atualizado e exportar o resultado quando disponível.

### Para validação pública

1. Selecionar **Validar documento sem login** na tela inicial.
2. Informar o código, ler o QR Code ou selecionar um PDF.
3. Consultar o registro do documento.
4. Conferir participantes, status e resultado da integridade do arquivo.

## Arquitetura

O projeto utiliza uma organização em camadas para manter a interface desacoplada das regras de negócio e da persistência.

```text
Presentation  ->  Domain  ->  Data
      \            |          /
             Core compartilhado
```

- **Presentation:** telas Compose, navegação, temas e estado visual.
- **Domain:** modelos, contratos de repositório, regras de negócio e casos de uso.
- **Data:** banco Room, DAOs, entidades locais, mapeadores e repositórios.
- **Core:** autenticação, segurança, manipulação de PDF, tema e componentes compartilhados.

Essa separação permite substituir os repositórios locais por serviços remotos no futuro sem acoplar a interface a uma implementação específica.

## Tecnologias

- Kotlin.
- Android nativo.
- Jetpack Compose e Material 3.
- Room para persistência local.
- Coroutines e StateFlow.
- Android PdfRenderer para visualização.
- PDFBox-Android para geração e composição do PDF final.
- JUnit e testes instrumentados Android.
- Java 17.
- Android API 26 ou superior.

## Segurança e privacidade

- Senhas não são armazenadas em texto puro.
- Credenciais temporárias são geradas de forma segura para novos signatários.
- O QR Code não contém dados pessoais dos participantes.
- O documento original é preservado durante a criação das versões assinadas.
- O acesso às funções é separado por perfil.
- A validação distingue a existência do registro da integridade criptográfica do arquivo.

## Como executar

### Requisitos

- Android Studio.
- JDK 17.
- Android SDK com API 26 ou superior.
- Emulador Android ou aparelho compatível.

### Executar no Android Studio

1. Abra a pasta do projeto no Android Studio.
2. Aguarde a sincronização das dependências.
3. Selecione um emulador ou aparelho conectado.
4. Execute a configuração `app`.
5. Na tela inicial, selecione **Criar uma conta** para iniciar o uso.

### Gerar o APK de depuração

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

O arquivo será gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Versão atual do aplicativo: **1.4.0**.

## Escopo atual e evolução

O Fides está preparado para uma experiência local completa de preparação, assinatura e validação de documentos.

Nesta versão, o envio, a autenticação, a validação e o armazenamento funcionam localmente no dispositivo. Integrações com backend, sincronização entre aparelhos, notificações automáticas por e-mail e certificação digital PKI exigem uma etapa posterior de infraestrutura.

Também permanecem como evoluções naturais:

- assinatura de APK release para distribuição pública;
- validações adicionais para PDFs inválidos ou corrompidos;
- ampliação dos testes de acessibilidade;
- sincronização remota e consulta pública multi-dispositivo;
- integração com provedores de e-mail e backend.

## Equipe

- Alberto da Silva Lima
- Lucas Giovani de Oliveira
- Victor Pedro Gaspar Martins Silva
- Guilherme Santana Paes
- Simara Santos Silva

O Fides combina uma jornada simples para o usuário com uma base técnica preparada para crescer: documentos organizados, participantes identificados, assinaturas rastreáveis e validação acessível em um único aplicativo.
