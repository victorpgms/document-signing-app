# Fides — Guia oficial do produto, identidade visual e desenvolvimento

> **Projeto:** Fides — Document Management & Signing  
> **Atividade:** FIAP Challenge LetsSign — Sprint 1  
> **Documento:** `FIDES_PROJECT_GUIDE.md` — manter na raiz do repositório  
> **Versão do guia:** 1.0 · **Data:** 23/10/2026  
> **Plataforma:** Android nativo · **Idioma deste documento:** português do Brasil  
> **Entrega da Sprint 1:** 05/11/2026, 23h59 (horário de Brasília)  
> **Status:** referência de produto e padrões; **não** significa que o código já foi implementado.

## Índice

1. [Fontes oficiais e ordem de prioridade](#1-fontes-oficiais-e-ordem-de-prioridade)
2. [Produto e objetivo](#2-produto-e-objetivo)
3. [Escopo e limites da Sprint 1](#3-escopo-e-limites-da-sprint-1)
4. [Perfis, permissões e regras de negócio](#4-perfis-permissões-e-regras-de-negócio)
5. [Fluxos completos](#5-fluxos-completos)
6. [Mapa de telas e tarefas](#6-mapa-de-telas-e-tarefas)
7. [Identidade visual e filosofia de interface](#7-identidade-visual-e-filosofia-de-interface)
8. [Paleta de cores e tokens](#8-paleta-de-cores-e-tokens)
9. [Light Mode e Dark Mode](#9-light-mode-e-dark-mode)
10. [Logotipos, ícone e recursos de marca](#10-logotipos-ícone-e-recursos-de-marca)
11. [Tipografia e Google Fonts](#11-tipografia-e-google-fonts)
12. [Componentes, layout, acessibilidade e conteúdo](#12-componentes-layout-acessibilidade-e-conteúdo)
13. [Idiomas e internacionalização Android](#13-idiomas-e-internacionalização-android)
14. [Tecnologias, bibliotecas e arquitetura](#14-tecnologias-bibliotecas-e-arquitetura)
15. [Persistência, dados, PDFs e assinaturas](#15-persistência-dados-pdfs-e-assinaturas)
16. [Autenticação, privacidade e segurança](#16-autenticação-privacidade-e-segurança)
17. [Estados de interface, erros e feedback](#17-estados-de-interface-erros-e-feedback)
18. [Divisão de responsabilidades e dependências](#18-divisão-de-responsabilidades-e-dependências)
19. [Git, branches, commits e Pull Requests](#19-git-branches-commits-e-pull-requests)
20. [Critérios de conclusão e testes](#20-critérios-de-conclusão-e-testes)
21. [Instruções obrigatórias para IAs e agentes](#21-instruções-obrigatórias-para-ias-e-agentes)
22. [Decisões a sincronizar com a planilha](#22-decisões-a-sincronizar-com-a-planilha)
23. [Referências técnicas oficiais](#23-referências-técnicas-oficiais)

---

## 1. Fontes oficiais e ordem de prioridade

| Fonte | Link / local | Finalidade |
|---|---|---|
| Enunciado da FIAP | Documento oficial disponibilizado na atividade | Requisitos mínimos e entregáveis obrigatórios. |
| Código-fonte | [GitHub — document-signing-app](https://github.com/victorpgms/document-signing-app) | Estado real e implementado do aplicativo. |
| Guia de produto | `FIDES_PROJECT_GUIDE.md` (este documento) | Nome, marca, padrões, arquitetura combinada, comportamento e convenções. |
| Planejamento | [Planilha mestre de desenvolvimento — OneDrive](https://1drv.ms/x/c/a6367a07b7ba8450/IQDHllxvWvEqRY2FUA458qieAZo_-GyItlNFyRTUSK6lrGY?e=MhL3Vn) | IDs LS, responsáveis, prioridades, status, dependências, prazos, arquivos e PRs. |
| Design | [Figma — Fides: Document Management & Signing](https://www.figma.com/design/zNyP9rqxH4wxEOdiUSQ9jw/Fides--Document-Management---Signing?node-id=0-1&t=JtTt4k9bfou7jrCG-1) | Fonte visual para telas, componentes, comportamento gráfico e pares Light/Dark. |
| Exportação do design | `stitch_fides_document_management_signing.zip` (arquivo disponibilizado pela equipe) | Referência auxiliar para leitura visual/HTML. **Não é código Android de produção.** |
| Manual inicial | `README.md` | Instruções de setup, execução, arquitetura existente e links. |

**Hierarquia para resolver conflitos:**

1. **Enunciado FIAP** prevalece em requisitos obrigatórios e escopo da atividade.
2. **Código atual da branch base** prevalece para o que já existe tecnicamente; não afirmar funcionalidades implementadas sem verificar.
3. **Este guia + decisões aprovadas na planilha** prevalecem em identidade, padrões e comportamento do produto.
4. **Planilha mestre** é a fonte viva de tarefas, atribuições, dependências e status.
5. **Figma** é a referência visual; não determina funcionalidades não previstas na Sprint 1.
6. **Orientações da tarefa do integrante** valem dentro do escopo autorizado.

Caso duas fontes entrem em conflito, **não tomar decisão silenciosamente**: registrar o conflito, expor impacto e solicitar alinhamento da equipe. Nenhum status deste documento substitui a planilha atualizada.

## 2. Produto e objetivo

- **Nome oficial de exibição:** **Fides**.
- **Categoria:** gerenciamento de documentos e coleta de assinaturas eletrônicas manuscritas.
- **Produto:** aplicativo mobile Android **nativo**.
- **Objetivo da Sprint 1:** permitir que um Administrador prepare um PDF, selecione signatários, posicione campos e envie uma solicitação local; permitir que cada Signatário visualize e assine seus campos; acompanhar progresso e salvar o PDF final.
- **Proposta de experiência:** **assinar documentos não precisa ser complicado.**
- **Tom de marca:** confiável, humano, organizado, claro, profissional, contemporâneo, discreto.
- **Públicos funcionais:** Administrador e Signatário.

**Significado funcional do envio nesta sprint:** registrar uma solicitação **dentro do aplicativo e do mesmo dispositivo/base local**. O fluxo poderá ser demonstrado trocando de conta. Não é um envio real a outros aparelhos, e-mails ou serviços externos.

**Cuidado com nomenclatura:** não confundir **assinatura eletrônica manuscrita de demonstração** com certificação digital, assinatura qualificada, ICP-Brasil ou garantia jurídica automática.

## 3. Escopo e limites da Sprint 1

### 3.1 Funcionalidades a entregar

- Login local com e-mail/senha para **Admin** e **Signatário**.
- Cadastro e edição de signatários com **nome completo, e-mail, telefone e senha**.
- Lista de signatários com opção de seleção; classificação no fluxo do documento como **Contratante, Contratado ou Testemunha**.
- Importar PDF existente pelo seletor do Android; informar título; detectar arquivo inválido/corrompido/não processável.
- Renderizar **todas as páginas** do PDF e navegar por elas.
- Inserir, arrastar, reposicionar e excluir campos de assinatura **antes do envio**, vinculados a um signatário.
- Permitir vários campos para a mesma pessoa e impedir campos fora dos limites da página.
- Revisar PDF, participantes, campos/páginas e mensagem de convite antes de confirmar.
- Registrar a solicitação local sem duplicidade após falhas/repetição de clique.
- Permitir visualização de documentos recebidos, pendentes, assinados e concluídos, conforme o perfil.
- Coletar uma assinatura desenhada com o dedo, revisar/refazer **antes** da confirmação e aplicar ao PDF.
- Persistir status individual, status geral e eventos de criação/envio/assinatura/conclusão com data e hora.
- Marcar o documento **Concluído** somente após todos os signatários obrigatórios terem assinado.
- Gerar e visualizar o PDF final com as assinaturas incorporadas; permitir **salvar uma cópia** pelo seletor Android.
- Manter informações após fechar/reabrir o app; impedir acesso de terceiros a documentos não compartilhados com eles.
- Oferecer **Light/Dark** e **inglês/português brasileiro**, usando recursos nativos Android conforme as seções 9 e 13.

### 3.2 Fora do escopo funcional

**Não implementar nem prometer no app**:

- Certificado digital ICP-Brasil, A1/A3, assinatura qualificada ou autenticação de identidade robusta.
- Biometria, reconhecimento facial, selfie como validação jurídica.
- Blockchain, HSM, carimbo do tempo RFC 3161, trilha criptográfica com validade jurídica, auditoria forense certificada.
- Geolocalização/IP como evidência legal; coleta de GPS para assinatura.
- Envio real por e-mail, push, SMS, WhatsApp, deeplink remoto ou sincronização entre celulares.
- Backend, API REST funcional, banco em nuvem e cadastro remoto — **previstos para Sprint 2**.
- Login Google, Apple, gov.br e recuperação de senha via e-mail.
- Pagamentos, planos, IA, chatbot, analytics avançado e gestão empresarial adicional.
- CPF obrigatório ou campos adicionais de cadastro que não estejam no enunciado.

**ATENÇÃO:** algumas telas exportadas pelo Stitch/Figma contêm textos ou ícones que sugerem recursos acima. **Esses elementos não autorizam a implementação nem afirmações falsas.** Adaptar textos e seções à funcionalidade real preservando a linguagem visual, sob coordenação da tarefa responsável e da auditoria LS-041.

## 4. Perfis, permissões e regras de negócio

| Regra | Comportamento requerido |
|---|---|
| Perfil `ADMIN` | Cadastra signatários, cria fluxos, importa PDFs, posiciona campos, envia, acompanha e consulta PDFs finais. |
| Perfil `SIGNER` | Consulta documentos enviados a ele, abre, assina seus próprios campos e visualiza versões permitidas. |
| Menor conjunto de participantes | Pelo menos **1 signatário** para enviar. |
| Participantes | Suporte a múltiplos; remover/substituir antes do envio. Papel: contratante, contratado ou testemunha. |
| Campo de assinatura | Ligado a **um** participante; pode haver vários campos para o mesmo participante. |
| Coordenadas | Página, posição e dimensões relativas ao PDF para manter alinhamento entre tamanhos de tela. |
| Segurança da área | Campos não podem sair do limite visível/útil da página do PDF. |
| Rascunho | Editável até o envio. |
| Após envio | PDF original, participantes e posicionamento ficam **imutáveis** no fluxo enviado. Mudança exige uma **nova versão e novo fluxo**. |
| Assinatura | Só usuário autorizado pode registrar sua assinatura; não pode assinar campos de outros. |
| Assinatura múltipla | Se o usuário tiver vários campos, poderá capturar uma assinatura e aplicá-la aos seus campos indicados, com confirmação clara. |
| Conclusão | O documento só conclui quando todos os signatários definidos como obrigatórios assinam. |
| Repetição de envio | Sem registros duplicados: ação deve ser idempotente para a mesma solicitação. |
| Isolamento | Signatário só acessa documentos enviados a ele; Admin só acessa documentos sob sua gestão. |
| Histórico | Eventos registram tipo, participante, documento, instante e resultado. |

**Estados de domínio propostos:** `DRAFT`, `PENDING_SIGNATURES`, `PARTIALLY_SIGNED`, `COMPLETED`; falhas transitórias de envio/processamento devem ser representadas separadamente do estado persistente do documento. Adotar nomes finais na **LS-004** para que todos usem o mesmo contrato. Não criar enums paralelos.

## 5. Fluxos completos

### 5.1 Administrador

```text
Splash → Login → Home/Documentos
                ├─ Signatários → Listar → Cadastrar/Editar/Excluir
                ├─ Perfil → Sobre / Idioma / Aparência / Sair
                └─ Novo documento
                    1. Documento: título + selecionar/validar PDF
                    2. Signatários: escolher pessoas e papéis
                    3. Campos: visualizar PDF + posicionar campos
                    4. Revisão: conferir informações e mensagem
                       ↓
                    Enviar solicitação local
                       ↓
                    Sucesso / erro com nova tentativa segura
                       ↓
                    Detalhes: progresso + participantes + histórico
                       ↓
                    Concluído → PDF final → Salvar cópia
```

### 5.2 Signatário

```text
Splash → Login → Meus documentos
                ├─ Perfil → Sobre / Idioma / Aparência / Sair
                └─ Pendentes → Abrir PDF
                                ↓
                         Localizar seus campos
                                ↓
                         Capturar assinatura (dedo)
                                ↓
                         Limpar/refazer ou confirmar
                                ↓
                         Aplicar nos seus campos
                                ↓
                         Atualizar status/histórico
                                ↓
                         Sucesso → Consultar documento
```

### 5.3 Caminho obrigatório da demonstração

1. Admin entra e cadastra **Maria Silva** e **João Santos** (dados fictícios).
2. Cria `Contrato de prestação de serviços`, importa `Contrato.pdf` e seleciona ambos.
3. Define papéis, posiciona campos e envia localmente.
4. Sai; Maria entra, vê pendência, assina e confirma.
5. Admin entra novamente e vê **Parcialmente assinado**.
6. João entra e assina.
7. Admin vê **Concluído**, acessa histórico e salva uma cópia do PDF assinado.
8. Repetir visualmente em Light e Dark; testar idioma `en` e `pt-BR`.

Não usar dados pessoais reais ou documentos sensíveis na apresentação.

## 6. Mapa de telas e tarefas

**Fonte oficial de IDs UI e estados:** aba **MAPA DE TELAS** da planilha. Esta tabela é uma referência resumida, não substitui a aba.

| ID | Tela ou grupo | Perfis | Tarefas LS |
|---|---|---|---|
| UI-01 | Splash | Ambos | LS-006 |
| UI-02 | Login, validações e feedback | Ambos | LS-008, LS-009, LS-010 |
| UI-03 | Home/documentos do Admin | Admin | LS-011, LS-022 |
| UI-04 | Lista e busca de signatários | Admin | LS-012, LS-014, LS-015 |
| UI-05 | Cadastro/edição/exclusão de signatário | Admin | LS-013, LS-015 |
| UI-06 | Novo documento, passo 1/4 | Admin | LS-017, LS-018 |
| UI-07 | Seleção de signatários, passo 2/4 | Admin | LS-014, LS-019 |
| UI-08 | Editor/posicionamento, passo 3/4 | Admin | LS-024, LS-025, LS-026 |
| UI-09 | Revisão, passo 4/4 | Admin | LS-020 |
| UI-10 | Envio: processando, sucesso, erro | Admin | LS-021 |
| UI-11 | Detalhes, status e timeline | Admin | LS-022, LS-023, LS-030 |
| UI-12 | Home/documentos recebidos | Signatário | LS-011, LS-029 |
| UI-13 | Visualização e campos a assinar | Signatário | LS-025, LS-029 |
| UI-14 | Desenhar, limpar e refazer assinatura | Signatário | LS-027 |
| UI-15 | Confirmar, aplicar e sucesso | Signatário | LS-028, LS-029 |
| UI-16 | PDF final, somente leitura e exportação | Ambos, conforme autorização | LS-028, LS-030, LS-040 |
| UI-17 | Perfil, Sobre e logout | Ambos | LS-039 |
| UI-18 | Estados comuns e Design System | Ambos | LS-006, LS-041 |

**Navegação mínima:** Admin: `Documentos · Signatários · Perfil`. Signatário: `Documentos · Perfil`. Botão contextual **Novo documento** na Home Admin. Não duplicar BottomNavigation nem criar navegação diferente entre temas.

**Telas não equivalem necessariamente a Activities diferentes.** Preferir uma Activity com navegação Compose, rotas tipadas/consistentes e componentes reutilizados. Variações, diálogos e estados podem existir na mesma rota.

## 7. Identidade visual e filosofia de interface

**Direção:** minimalismo editorial + produto de produtividade moderno e confiável; precisão, respiro e clareza acima de efeitos decorativos. Inspirações de qualidade: Notion, Linear, Raycast e refinamento de produtos de consumo, **sem copiar marcas ou interfaces**, e respeitando Android/Material 3.

**Deve parecer:** discreto, sofisticado, tecnológico, humano, eficiente, calmo, profissional.

**Evitar:** gradientes ostensivos, neon, glassmorphism, sombras fortes, excesso de cards, bordas pesadas, ilustrações infantis, termos jurídicos desnecessários, personalizações por tela que quebrem consistência. Nada de referências visuais ou textuais a Apple/iOS no produto Android.

**Princípios de UX:**

1. A ação principal da tela deve ser óbvia e acessível.
2. Mostrar informação suficiente, sem duplicação de cards.
3. Usar listas simples quando cards não agregarem hierarquia.
4. Status devem ser legíveis sem depender só de cor.
5. Erros devem explicar o problema e oferecer próximo passo.
6. Deixar os documentos e assinaturas em primeiro plano.
7. Mesmo componente = mesma aparência e comportamento em todo o app.
8. Light e Dark são o mesmo aplicativo, não versões independentes.

**Figma = referência visual**, mas a implementação Kotlin/Compose deve preservar comportamento Android real, acessibilidade e escopo FIAP; não copiar código HTML/CSS do Stitch como se fosse código nativo.

## 8. Paleta de cores e tokens

### 8.1 Cores oficiais da marca (não substituir por outra paleta)

| Token de identidade | Cor | Uso |
|---|---|---|
| `BrandBlue` | `#006ACC` | Ação principal, CTA, progresso, seleção e foco. |
| `BrandPurple` | `#3F05BE` | Institucional, detalhes secundários; nunca competir com azul. |
| `BrandGold` | `#DC990E` | Acento pequeno e ocasional; nunca grandes fundos dourados. |
| `LightBackground` | `#F8FAFC` | Fundo principal claro. |
| `LightOnBackground` | `#111827` | Texto principal claro. |

A aparência final e proporção das cores dos logos oficiais **não podem ser adulteradas**. Tokens de UI podem ganhar variações tonais discretas **somente quando necessário para contraste/acessibilidade**, mantendo reconhecíveis as cores oficiais acima. Evitar inventar novos azuis, roxos ou dourados predominantes.

### 8.2 Neutros Light

| Token | Cor |
|---|---|
| `background` | `#F8FAFC` |
| `surface` | `#FFFFFF` |
| `surfaceMuted` | `#EDF2F7` |
| `outline` | `#E2E8F0` |
| `onBackground` | `#111827` |
| `onSurfaceVariant` | `#64748B` |
| `disabledText` | `#94A3B8` (validar legibilidade no contexto) |

### 8.3 Neutros Dark

| Token | Cor |
|---|---|
| `background` | `#0F1117` |
| `surface` | `#151922` |
| `surfaceElevated` | `#1B202B` |
| `fieldSurface` | `#202632` |
| `onBackground` | `#F3F4F6` |
| `onSurfaceVariant` | `#AEB6C2` |
| `mutedText` | `#7F8998` |

**No Dark Mode:** não usar preto puro (`#000000`) em toda a interface; manter leve diferença entre superfícies. O azul, roxo e dourado são **as mesmas cores de marca**; adaptar apenas contrastes funcionais quando necessário. Evitar neon. Campos/badges de estado podem ter fundos translúcidos suaves.

### 8.4 Cores semânticas

- **Sucesso/Concluído:** verde discreto (referência Light `#059669`).
- **Aviso/Pendente:** âmbar ou detalhe dourado moderado (referência Light `#D97706`).
- **Erro:** vermelho discreto (referência Light `#DC2626`).
- **Parcial:** azul com progresso textual, por exemplo `2 de 3 assinaturas`.
- **Rascunho:** cinza neutro.

Não tratar selo visual como certificação real. Para telas pequenas, badge = ícone/label/fundo leve; status não depende exclusivamente da cor.

### 8.5 Fonte única de cores no código

- Centralizar `ColorScheme`, `FidesTheme`, tokens adicionais e tipografia em `core/ui/theme/` (ou na estrutura real aprovada).
- Nunca hardcodar `Color(...)` diferente em cada tela.
- Usar `MaterialTheme.colorScheme.*` e tokens semânticos consistentes.
- **Material You / Dynamic Color:** não ativar por padrão na Sprint 1, para preservar a paleta oficial. Acompanhamento do **tema do sistema** é obrigatório, mas não significa trocar a marca pelas cores do papel de parede.

## 9. Light Mode e Dark Mode

### 9.1 Regras de escolha

1. Modo padrão: **Sistema** — seguir `isSystemInDarkTheme()`/configuração do Android.
2. Preferência manual no app (Perfil → Aparência), com três opções: **Sistema, Claro, Escuro**.
3. Persistir a preferência simples via **DataStore Preferences** (ou camada equivalente já aprovada no projeto).
4. Se o usuário escolher Sistema, mudanças no tema do Android devem refletir automaticamente no app.
5. Preferência manual sobrepõe Sistema até ser revertida.
6. Não persistir cores por tela nem criar temas para cada perfil.

### 9.2 Equivalência obrigatória de telas

**A mesma tela + mesmos dados + mesma rota + mesmo fluxo = duas paletas.** Se no Light existir botão, filtro, status, texto, participante, PDF, card, ícone ou ação, tudo deve existir na mesma posição lógica do Dark. Só muda o tratamento visual de cor/superfície.

- Mesmas fontes, escalas, ícones, cantos, espaçamentos, navegação e estados.
- Mesma lista de documentos e **mesmo status** em pares de teste Light/Dark.
- Não adicionar/omitir função só em um tema.
- Tema nunca modifica PDF importado nem conteúdo de documento.
- Área do PDF continua com as cores do **arquivo original** (geralmente folha branca) também no Dark; a superfície **ao redor** acompanha o tema.
- Área de captura de assinatura pode manter superfície branca/clara no Dark para legibilidade do traço; o restante da UI é escuro.
- Contraste mínimo apropriado e leitura acessível; foco e componentes de toque visíveis.

### 9.3 Teste de equivalência

Para cada UI-01 a UI-18: abrir com mesmos dados, alternar Light↔Dark, conferir rota, textos, campos, tamanhos, status, botões, comportamentos e ausência de layout quebrado.

## 10. Logotipos, ícone e recursos de marca

**Fonte oficial:** arquivos de imagens do Fides publicados no ImageKit. Os links abaixo são fornecidos pelo projeto. **Não redesenhar, recriar por IA, trocar proporções, aplicar novos gradientes, adulterar lettering ou gerar logos semelhantes.**

| Arquivo | URL oficial | Aplicação recomendada |
|---|---|---|
| `icone.png` | https://ik.imagekit.io/vpgms/fides/fides-logo/icone.png | Referência oficial para ícone do app/launcher. |
| `logo-branca-sem-fundo.png` | https://ik.imagekit.io/vpgms/fides/fides-logo/logo-branca-sem-fundo.png | **Logo completa preferencial no Dark**: letras brancas e transparência. |
| `logo-fundo-branco.png` | https://ik.imagekit.io/vpgms/fides/fides-logo/logo-fundo-branco.png | Composição oficial para aplicação em fundo branco, quando necessária. |
| `logo-fundo-preto.png` | https://ik.imagekit.io/vpgms/fides/fides-logo/logo-fundo-preto.png | Composição oficial histórica com fundo preto; **não usar como logo principal sobre superfícies Dark**, pois a borda/retângulo compromete a integração. |
| `logo-sem-fundo.png` | https://ik.imagekit.io/vpgms/fides/fides-logo/logo-sem-fundo.png | **Logo completa preferencial no Light**, quando tiver contraste adequado. |

### Aplicações

- **Splash Light:** marca completa apropriada para fundo claro, centralizada, sem decoração.
- **Splash Dark:** `logo-branca-sem-fundo.png` sobre superfície escura; sem retângulo preto adicional.
- **Login Light:** logo completa transparente adequada para superfícies claras.
- **Login Dark:** a **nova logo branca transparente**, no mesmo tamanho/posição que no Light.
- **Perfil / Sobre o Fides:** usar marca de forma discreta, com variante adequada ao tema.
- **App launcher:** usar `icone.png` como **referência** ao criar recursos Android adaptativos; conferir margens/recortes conforme guidelines Android, sem redesenhar arte.
- **Telas operacionais, listas, diálogos e PDF:** não colocar logo em cada elemento; a identidade aparece pelo Design System.
- **PDF importado/final:** **nunca** inserir watermark/logo do Fides automaticamente no conteúdo do usuário.

### Regras de incorporação ao Android

1. **Preferir assets locais** em `res/drawable(-nodpi)`/`res/mipmap-*` ou recursos vetoriais oficiais quando disponibilizados, não carregar a marca da internet em tempo de execução.
2. Copiar os arquivos oficiais para o repositório **apenas quando a equipe confirmar a autorização de uso e a versão final**.
3. Preservar transparência e qualidade; evitar bordas involuntárias.
4. Não aplicar tint automática nas logos multicoloridas.
5. Ter duas referências de imagem (`logoLight`, `logoDark`) selecionadas pelo tema, sem duplicar layouts.
6. Usar nomes de recurso Android válidos (snake_case sem hífen), por exemplo `fides_logo_light`, `fides_logo_dark`, `fides_launcher_art`.
7. Conferir legibilidade real em Android, não apenas no mockup.

## 11. Tipografia e Google Fonts

**Família oficial única:** **Inter** — disponível gratuitamente no Google Fonts sob licença **SIL Open Font License (OFL 1.1)**.

- **Google Fonts:** https://fonts.google.com/specimen/Inter
- **Repositório oficial Google Fonts:** https://github.com/google/fonts/tree/main/ofl/inter
- **Licença Inter:** https://github.com/google/fonts/blob/main/ofl/inter/OFL.txt
- **Guia de fontes no Jetpack Compose:** https://developer.android.com/develop/ui/compose/text/fonts

| Estilo de uso | Peso | Tamanho de partida (Android `sp`) |
|---|---:|---:|
| Título principal de tela | 600 (SemiBold) | 24–26 sp |
| Subtítulo/seção | 600 (SemiBold) | 18–22 sp |
| Corpo principal | 400 (Regular) | 16 sp |
| Corpo secundário | 400 (Regular) | 14 sp |
| Botões/labels | 500–600 | 14–16 sp |
| Legendas/metadados | 400–500 | 12–13 sp, sem perder acessibilidade |

- Trabalhar com `sp` e escala de fontes do Android; dimensões de layout em `dp`.
- Priorizar pesos **400, 500, 600**, usar 700 apenas em pontos realmente excepcionais.
- Centralizar `Typography` no Material 3; não escolher outra fonte por tela ou tema.
- **Preferência técnica para esta Sprint:** distribuir os arquivos Inter OFL necessários no APK (`res/font`) e configurar `FontFamily` com recursos locais, sem depender de rede. Registrar a licença OFL na documentação de terceiros do projeto; **nunca commitar a fonte de origem desconhecida**.
- API de Google Fonts baixáveis é alternativa oficial, mas não deve ser pré-requisito para o app funcionar offline. Não trocar automaticamente a Inter pela fonte do sistema sem consenso; usar fallback só em caso de falha.

## 12. Componentes, layout, acessibilidade e conteúdo

### 12.1 Componentes reutilizáveis

`FidesTopAppBar`, `FidesBottomNavigation`, `PrimaryButton`, `SecondaryButton`, `TextButton`, `FidesTextField`, `PasswordField`, `SearchField`, `StatusBadge`, `DocumentListItem`, `SignatoryListItem`, `EmptyState`, `ErrorState`, `LoadingState`, `FidesProgressIndicator`, `ParticipantChip`, `TimelineItem`, `PdfPageViewer`, `SignatureFieldOverlay`, `SignatureCanvas`, `ConfirmationDialog`, `SectionHeader`.

**Observação:** são conceitos de componentes; nomes de classes e pacotes definitivos pertencem à implementação e devem ser consistentes com o repositório.

### 12.2 Métricas de layout

- Grade base: **8 dp**, subdivisões de **4 dp**.
- Margem horizontal: **20 dp** em telefones padrão; até **24 dp** onde houver espaço.
- Espaçamento entre elementos: 4/8/12/16/24/32 dp conforme importância.
- Alvo tocável: **mínimo 48 × 48 dp**.
- Botões e campos: raio **12 dp** como padrão.
- Cards/contêineres: raio **16 dp**; não transformar todas as listas em cards.
- Bottom sheets: até **24 dp** nos cantos superiores, se necessários.
- Sombras quase inexistentes; preferir contraste sutil de superfície e bordas suaves.
- Projetar primeiro para smartphone vertical; tamanhos maiores devem continuar utilizáveis sem exigir layout de tablet nesta sprint.
- Usar `WindowInsets`/safe areas para barras de sistema, teclado e navegação inferior. Garantir ações visíveis com teclado aberto.

### 12.3 Acessibilidade

- Contraste de texto, estados de foco e botões apropriados ao tema (visar **WCAG AA** quando aplicável).
- `contentDescription` e semântica de acessibilidade em ícones acionáveis; não repetir descrição para ícones puramente decorativos.
- Indicadores de status precisam de ícone/texto, não apenas cor.
- Suportar fontes ampliadas e `fontScale` do sistema; evitar largura fixa para textos traduzidos.
- Ações destrutivas pedem confirmação.
- Espaçamento e campos devem funcionar com TalkBack e teclado/seletores do sistema.
- Não ocultar informações obrigatórias por truncamento indevido; checar landscape apenas para não quebrar, sem prometer experiência de tablet.

### 12.4 Microcopy

- Frases curtas, profissionais e humanas; evitar jargões e falsas promessas jurídicas.
- Exemplos PT-BR: **Novo documento**, **Quem precisa assinar?**, **Enviar para assinatura**, **Aguardando assinaturas**, **Documento concluído**, **Salvar cópia**.
- Exemplos EN: **New document**, **Who needs to sign?**, **Send for signing**, **Awaiting signatures**, **Document completed**, **Save a copy**.
- Não exibir "certificado", "assinatura com validade garantida", "ICP-Brasil" ou "criptografia certificada" sem implementação e validação correspondente.

## 13. Idiomas e internacionalização Android

### 13.1 Decisão de idiomas

**Nova regra deste guia, solicitada para a construção:**

- **Idioma-base dos recursos e fallback:** **inglês (`en`)**.
- **Tradução oficial:** **português do Brasil (`pt-BR`)**.
- **Idioma inicial visível:** seguir o **idioma do sistema Android** quando estiver entre os suportados; se não houver correspondência, usar **inglês** como fallback.
- **Escolha manual no app:** Perfil → Idioma → **Sistema / English / Português (Brasil)**.
- **Android 13+:** permitir seleção por aplicativo nas Configurações do sistema Android.
- **Android 12 e anteriores:** usar suporte compatível AndroidX se houver seletor interno.
- A preferência manual deve prevalecer sobre a detecção do sistema até o usuário escolher **Sistema** de novo.

> **Atenção:** o inglês é o idioma **base de desenvolvimento**, não uma imposição para abrir o app sempre em inglês. Um telefone em `pt-BR` deve abrir o Fides em português por padrão.

### 13.2 Padrão dos recursos

Arquivos de strings:

```text
app/src/main/res/
├── values/
│   ├── strings.xml           # inglês, fallback/base
│   └── plurals.xml           # se separado
└── values-pt-rBR/
    ├── strings.xml           # português do Brasil
    └── plurals.xml           # se separado
```

Exemplo **inglês** (`res/values/strings.xml`):

```xml
<resources>
    <string name="app_name">Fides</string>
    <string name="login_title">Welcome to Fides</string>
    <string name="action_sign">Sign document</string>
    <string name="action_save_copy">Save a copy</string>
    <string name="settings_language">Language</string>
    <string name="settings_appearance">Appearance</string>
    <plurals name="pending_signatures">
        <item quantity="one">%d signature pending</item>
        <item quantity="other">%d signatures pending</item>
    </plurals>
</resources>
```

Exemplo **português** (`res/values-pt-rBR/strings.xml`):

```xml
<resources>
    <string name="app_name">Fides</string>
    <string name="login_title">Bem-vindo ao Fides</string>
    <string name="action_sign">Assinar documento</string>
    <string name="action_save_copy">Salvar cópia</string>
    <string name="settings_language">Idioma</string>
    <string name="settings_appearance">Aparência</string>
    <plurals name="pending_signatures">
        <item quantity="one">%d assinatura pendente</item>
        <item quantity="other">%d assinaturas pendentes</item>
    </plurals>
</resources>
```

Em Compose, usar `stringResource(R.string...)` e `pluralStringResource(...)`; **nunca hardcodar textos de interface em `Text("...")`**, `Toast`, `Snackbar`, `contentDescription`, validações, diálogos, menus ou notificações locais. Nomes, títulos de documentos e conteúdo de PDFs **não** são traduzidos automaticamente.

### 13.3 APIs oficiais Android

- Configurar **per-app languages** conforme a versão do Gradle/Android adotada na LS-002/LS-003: `generateLocaleConfig` automático quando compatível ou `locale_config.xml` manual; não usar ambos ao mesmo tempo.
- Com geração automática, conferir a declaração do idioma-base sem qualificador (`unqualifiedResLocale=en` ou a sintaxe exigida pelo AGP utilizado) para que **inglês e português do Brasil apareçam efetivamente** entre os idiomas disponíveis no Android 13+. Validar o resultado no aparelho, pois a presença de `values-pt-rBR` por si só não prova que ambos estejam listados.
- Usar API oficial de preferências por aplicativo do Android 13+ (`LocaleManager`) ou `AppCompatDelegate.setApplicationLocales(...)` com AndroidX, conforme a arquitetura real.
- Ao usar `AppCompatDelegate` com Compose, verificar a exigência de `AppCompatActivity`; não misturar APIs de forma que a troca de idioma falhe.
- Preferência **Sistema** = lista de locales do app vazia, para herdar o sistema.
- A alteração de idioma deve atualizar as telas corretamente, sem perder documento/rascunho ou dados persistentes. A recriação da Activity por mudança de locale é comportamento esperado e deve ser testado.
- **Não criar motor próprio de tradução nem chamar API paga.** As duas traduções são recursos estáticos locais.

### 13.4 Datas, números e pluralização

- Persistir instantes de evento com representação inequívoca (`Instant`/epoch UTC); **formatar na UI segundo `Locale` e fuso do dispositivo**.
- Idioma inglês: exemplo `Oct 23, 2026`; PT-BR: `23 out. 2026` (formato concreto dependerá da API de localização).
- Não concatenar datas em strings fixas; usar classes `java.time` disponíveis conforme configuração do projeto.
- Não concatenar plural em código (`"1 signatures"`); usar `<plurals>`.
- Traduzir rótulos, mensagens, textos de exemplo/ajuda, acessibilidade e status. **Não traduzir nomes próprios nem arquivo PDF importado.**
- Se o Figma estiver em português, gerar a versão equivalente em inglês preservando layout/ações; testar quebra de linha nas duas línguas e temas.

### 13.5 Matriz mínima de testes de idioma e tema

| Android | Idioma do sistema | Preferência de idioma | Tema do sistema | Preferência de aparência | Esperado |
|---|---|---|---|---|---|
| Compatível | pt-BR | Sistema | Claro | Sistema | PT-BR + Light |
| Compatível | en-US | Sistema | Escuro | Sistema | EN + Dark |
| Compatível | outro idioma não suportado | Sistema | Claro | Sistema | EN (fallback) + Light |
| Compatível | pt-BR | English | Escuro | Claro | EN + Light |
| Compatível | en-US | Português (Brasil) | Claro | Escuro | PT-BR + Dark |
| Android 13+ | qualquer | Idioma do app definido nas configurações Android | qualquer | Sistema | Idioma do app respeitado; tema do sistema respeitado |

**Não presumir** que trocar a língua automaticamente traduza documentos, arquivos ou dados digitados pelo usuário.

## 14. Tecnologias, bibliotecas e arquitetura

### 14.1 Stack aprovada

| Camada | Tecnologia/padrão | Regra |
|---|---|---|
| Plataforma | Android nativo | Android Studio |
| Linguagem | Kotlin | Sem criar UI híbrida/web |
| Interface | Jetpack Compose + Material 3 | Um Design System |
| Navegação | Navigation Compose | Uma navegação por perfil, sem rotas duplicadas |
| Estado/UI | ViewModel + StateFlow + Coroutines | Estado previsível, sem I/O no Composable |
| Banco | Room / SQLite | Dados estruturados locais |
| Preferências simples | DataStore Preferences | Tema; preferências não sensíveis |
| Arquivos | Armazenamento interno privado + Storage Access Framework | Sem permissão de acesso geral ao storage |
| Visualização PDF | Android `PdfRenderer` ou alternativa da POC | Definição definitiva na LS-024 |
| Editor de campos | Canvas/gestos no Compose | Coordenadas relativas ao PDF |
| PDF assinado | Biblioteca gratuita/open-source escolhida na LS-024 | Avaliar PDFBox-Android ou equivalente por testes reais |
| Testes | JUnit, testes Android/Compose | Gratuitos; conforme dependências aprovadas |
| Fonte | Inter (Google Fonts, OFL) | Recursos locais, offline |
| Git | Git + GitHub | Branch + PR + revisão |

**Não assumir que a biblioteca PDF escolhida já está aprovada.** Fazer POC na **LS-024** validando **renderização, campos em páginas diferentes, PDF assinado e exportação** antes de fechar a dependência.

**Não adicionar tecnologias pagas**, tokens de IA, Firebase, autenticação remota, backend ou serviços online na Sprint 1 sem tarefa aprovada.

### 14.2 Organização arquitetural

Padrão **MVVM + Repository**, com Use Cases para regras não triviais:

```text
presentation / feature/
  auth/          # Login + sessão visual
  dashboard/     # Homes por perfil
  signers/       # Cadastro/lista de signatários
  documents/     # Criação, envio, acompanhamento
  pdfeditor/     # Visualizador e sobreposição dos campos
  signing/       # Captura, aplicação, confirmação
  profile/       # Perfil + aparência + idioma + logout

domain/
  model/         # User, Document, Participant, SignatureField, etc.
  repository/    # Interfaces de repositório
  usecase/       # Regras: enviar, assinar, concluir, versionar

data/
  local/         # Room, entidades e DAOs
  mapper/        # Domain <-> Room
  repository/    # Implementação local

core/
  ui/theme/      # Paleta, Typography, FidesTheme
  ui/components/ # Componentes reutilizados
  navigation/    # Rotas e regras de navegação
  pdf/           # Renderização/importação/exportação
  security/      # Hash, sessão, autorização
```

**Essa árvore é conceitual.** Inspecionar antes a estrutura real da branch `main`. Não recriar pacotes, duplicar classes nem fazer refatoração ampla só porque este documento mostrou um exemplo.

### 14.3 Separações obrigatórias

- **Composable:** exibição/entrada de eventos, sem SQL, I/O de arquivo ou hashing.
- **ViewModel:** estado de tela e coordenação de operações.
- **Use Case:** regras de domínio (envio, validação, conclusão, versões).
- **Repository:** abstrai a origem dos dados; preparar troca de implementação local por remota na Sprint 2.
- **Room Entity ≠ Domain Model:** usar mapeadores, evitar vazamento de entidades na UI.
- **PDF service:** fora dos Composables e sem acoplamento direto à sessão.
- **Navegação:** recebe IDs/argumentos leves, não serializa objetos gigantes em rotas.

## 15. Persistência, dados, PDFs e assinaturas

### 15.1 Onde ficam os dados

- **Room:** usuários, hashes/salts, perfis, signatários, documentos, versões, relação documento↔participante, campos, status, eventos, caminhos/metadados e assinaturas quando apropriado.
- **Arquivos internos privados:** PDF importado original, versões intermediárias e PDF final gerado. Banco guarda IDs/caminhos e metadados, não o arquivo inteiro como BLOB, salvo decisão técnica documentada.
- **DataStore:** opção de tema e preferências simples de interface. Locale deve ser tratado preferencialmente pelas APIs de idioma por app, não por sistema próprio paralelo.
- **Exportação:** usuário escolhe destino com `ACTION_CREATE_DOCUMENT` / `ActivityResultContracts.CreateDocument` ou mecanismo oficial equivalente; sem modificar a cópia interna.

### 15.2 Modelo mínimo conceitual

| Entidade/conceito | Campos exemplificativos |
|---|---|
| `User` | UUID, nome, email normalizado, telefone, perfil, hash da senha, salt |
| `Document` | UUID, título, dono/Admin, status, versão, arquivo original, arquivo atual, datas |
| `Participant` | UUID, documento, usuário signatário, papel, obrigatoriedade, status, assinadoEm |
| `SignatureField` | UUID, documento/versão, participante, página, `x`, `y`, largura, altura normalizados |
| `SignatureRecord` | UUID, documento, participante, referência gráfica/resultado, instante |
| `DocumentEvent` | UUID, documento, ator, evento, instante, detalhes seguros |

A definição dos nomes finais, relações, tipos, chaves e migrações pertence às **LS-004 e LS-005**. Prefira IDs UUID estáveis. Não incluir CPF/GPS/IP apenas porque aparece nos mockups.

### 15.3 Coordenadas PDF

- Persistir **página e retângulo normalizado** (`x`, `y`, `width`, `height`, geralmente dentro de 0–1), relativo à página do PDF — **não** pixels absolutos do celular.
- Ao renderizar, converter PDF↔área visível considerando zoom, scroll, margem, rotação e escala.
- Antes de salvar, validar que o retângulo está **inteiramente** nos limites permitidos da página.
- Vários campos podem apontar para o mesmo participante.
- O mesmo documento em dispositivos/visualizações diferentes deve manter o posicionamento relativo.
- Validar PDFs multipáginas, orientações diferentes, páginas com tamanhos distintos e arquivos inválidos durante LS-024/LS-026.

### 15.4 Imutabilidade, envio e assinatura

1. Criar **rascunho** editável.
2. Validar participantes e campos.
3. No envio, registrar uma **solicitação local única** e congelar conteúdo/campos do fluxo enviado.
4. Cada signatário autentica na instalação local e encontra somente seus documentos.
5. Capturar assinatura manuscrita no Canvas, permitir **Limpar/Refazer** antes da confirmação.
6. Aplicar a assinatura visual aos campos do participante, sem sobrescrever o PDF original.
7. Persistir nova versão/arquivo de saída e evento de assinatura; atualizar status individual/geral de modo consistente.
8. Se todos assinaram, marcar `COMPLETED`, salvar PDF final íntegro e exibi-lo em modo somente leitura.
9. Não aceitar segunda confirmação acidental que gere assinatura duplicada ou estado inconsistente.

**Não afirmar assinatura criptográfica, prova de autoria ou equivalência a certificação digital.** O protótipo captura um desenho de assinatura e o insere visualmente no documento.

## 16. Autenticação, privacidade e segurança

- **E-mail + senha**, sem autenticação social ou recuperação remota.
- Armazenar **hash derivado de senha com salt individual** via algoritmo de derivação adequado (p. ex., PBKDF2-HMAC-SHA256 com parâmetros testados no dispositivo, ou biblioteca gratuita auditada). Nunca armazenar senhas em texto puro, usar hash rápido isolado ou criar criptografia própria.
- Comparar credenciais de forma segura e não registrar senhas em logs.
- Manter sessão local limitada e encerrar corretamente ao fazer logout; limpar telas de conteúdo restrito.
- Todas as consultas de documento e assinatura devem verificar o **usuário autenticado, o papel e o vínculo ao documento** na camada de negócio/repositório, não apenas ocultar botões.
- Armazenar PDFs no diretório privado e usar SAF para importar/exportar, sem permissões excessivas de armazenamento.
- Não solicitar câmera, microfone, GPS, contatos ou internet só para funcionalidades que não existem.
- Evitar arquivos/documentos pessoais reais no repositório e na demonstração.
- Registro histórico deve conter eventos reais do app, sem forjar localização, IP, certificados ou hash legal.
- O modelo local da Sprint 1 **não é serviço multi-dispositivo de produção**; não prometer controles de segurança equivalentes a um backend corporativo.

## 17. Estados de interface, erros e feedback

Toda tela deve contemplar estados aplicáveis, nos dois temas e nas duas línguas:

- `Loading`: carregamento de documento, login, processamento e salvamento.
- `Empty`: nenhuma tarefa/documento/signatário, incluindo CTA contextual.
- `Error`: falha de autenticação, PDF inválido/corrompido, erro ao salvar/exportar, falha de envio local.
- `Success`: cadastro, envio, assinatura e exportação finalizados de fato.
- `Disabled`: botão inativo por validação/ausência de traço ou dados.
- `Selected/Focused`: campos, filtros, pessoas e retângulos de assinatura com destaque acessível.
- `Pending/Partial/Completed`: status no documento com texto e progresso individual.
- `Confirmation`: excluir pessoa, enviar documento, assinar definitivamente e logout.

**Regras:** não mostrar sucesso antes de persistir a ação; não ignorar exceções; feedback breve com causa e recuperação; `Tentar novamente` não duplica envio; cancelar exportação não marca erro fatal; mudanças de tema/idioma não apagam formulário nem geram assinaturas.

## 18. Divisão de responsabilidades e dependências

**A atribuição pessoal é dinâmica: consultar sempre as abas `Backlog completo` e `DIVISÃO E DEPENDÊNCIAS`. Não presumir quantidade de integrantes, nomes ou dono de tarefa apenas por memória.**

| Trilha | Escopo | IDs principais | Atenção a arquivos compartilhados |
|---|---|---|---|
| **A — Fundação e integração** | Android, Gradle, modelos, Room, tema, navegação, docs | LS-002 a LS-007 | Gradle, `domain/`, AppDatabase, `core/ui/`, `core/navigation/` |
| **B — Acesso e usuários** | Login, sessão, dashboards, signatários, Perfil/logout | LS-008 a LS-015, LS-039 | `core/security/`, `feature/auth/`, `feature/signers/` |
| **C — Documentos** | CRUD de documentos, PDF import, 4 passos, envio, status e histórico | LS-016 a LS-023 | `feature/documents/`, repositórios e DAOs |
| **D — PDF e assinatura** | POC, viewer, editor, captura, aplicação, finalização e PDF final | LS-024 a LS-030, LS-040 | `core/pdf/` e classes de `feature/signing/` |
| **E — QA e entrega** | Regras, E2E, auditoria visual, seed demo, bugs, APK, apresentação, vídeo e entrega | LS-031 a LS-037, LS-041, LS-042 | Testes, `release/`, `docs/`, correções em arquivos de outras frentes |
| **UX/Design (realizado)** | Nome, identidade, logos, protótipos Light/Dark | LS-038 | Figma é a referência aprovada; implementação visual em LS-006 e LS-041 |

**Sequência mínima segura:**

1. LS-002 cria o projeto Android.
2. LS-003, LS-004 e a POC de PDF LS-024 avançam com coordenação das dependências.
3. LS-005 (Room) e LS-006 (Design System/navegação) liberam as telas.
4. Frentes B, C e D evoluem em paralelo **respeitando dependências e contratos**.
5. LS-039, LS-040, LS-041 e LS-042 fecham lacunas de Perfil, exportação, fidelidade/estados e dados de demonstração.
6. LS-031/LS-032/LS-033 estabilizam; LS-034/LS-035/LS-036/LS-037 geram e entregam os artefatos.

**Conflitos a evitar:** duas pessoas alterando simultaneamente `build.gradle.kts`, `AppDatabase`, modelos/interfaces de domínio, navegação central, tema global ou `core/pdf/`. Se necessário, reservar arquivos na planilha ou combinar sequenciamento e um PR integrador.

## 19. Git, branches, commits e Pull Requests

**Repositório oficial:** https://github.com/victorpgms/document-signing-app  
**Clone:** `https://github.com/victorpgms/document-signing-app.git`

Regras:

1. **Não trabalhar nem dar push diretamente na `main`**; usar a exceção de proprietário apenas para casos autorizados pela equipe, não como procedimento padrão.
2. Confirmar responsável, status **Pronta** ou **Em desenvolvimento**, dependências, branch e diretórios autorizados na planilha.
3. Criar branch a partir da `main` atualizada, usando o nome previsto na linha LS. Ex.: `feature/LS-006-navigation`.
4. Commits pequenos, com ID da tarefa: `feat(ui): implement theme toggle [LS-006]`.
5. Fazer `git status` e revisar `git diff` antes de stage/commit. Não usar `git add .` indiscriminadamente.
6. Um **PR para `main`** com resumo, arquivos alterados, testes e evidências; aguardar revisão e checks combinados, sem bypass informal.
7. Após PR aprovado, preferir **Squash and Merge** com responsável autorizado.
8. Registrar PR, resultado do build, testes, SHA, merge e status na planilha.
9. Não alterar contratos compartilhados, interfaces públicas, DAO ou Gradle sem comunicar impacto e atualizar o planejamento.
10. Não fazer `force push`, reset destrutivo ou apagar trabalho alheio para resolver conflitos sem coordenação.

### Modelo de PR

```text
Título: [LS-XXX] Descrição clara

O que foi implementado:
- ...

Arquivos alterados:
- ...

Critérios de aceite atendidos:
- ...

Testes executados e resultados:
- ...

Light/Dark e EN/PT-BR verificados:
- ...

Dependências, limitações e pontos a revisar:
- ...
```

**Atualizar planilha não é opcional**. Se o integrante ou IA não possuir acesso à edição, entregar os valores para atualização; não afirmar que editou sem ter editado.

## 20. Critérios de conclusão e testes

Uma tarefa só fica **Concluída** quando:

- Código correspondente implementado e dentro do escopo LS.
- Critérios de aceite da planilha atendidos, com evidências.
- Projeto sincroniza e **compila**.
- Testes relevantes passam, sem erros bloqueantes conhecidos.
- Perfis, permissões e persistência funcionam no cenário aplicável.
- Light/Dark têm **o mesmo fluxo e conteúdo**.
- Interface existe em **EN e PT-BR** e respeita idioma/tema do sistema conforme projeto.
- Acessibilidade básica foi verificada em mudanças de UI.
- PR revisado e integrado à `main` conforme processo do grupo.
- Planilha atualizada, incluindo branch, PR, merge, SHA e observações.

### 20.1 Matriz de testes mínima de Sprint 1

- **Autenticação:** Admin e Signatário válidos; senha errada; e-mail inválido; logout; acesso proibido.
- **Cadastro:** validar campos; e-mail duplicado; editar; excluir com confirmação.
- **Documento:** título vazio; PDF inválido; multipáginas; sair e retornar; persistência.
- **Participantes:** zero selecionados deve bloquear envio; vários papéis e campos; exclusão/substituição pré-envio.
- **Campos:** mover/adicionar/excluir; limites da página; zoom/orientação; persistência proporcional.
- **Revisão/envio:** mensagem, resumo fiel, confirmação, falha/retry sem duplicidade, bloqueio pós-envio.
- **Assinatura:** pessoa não vê/assina campo alheio; limpar/refazer; confirmar; vários campos do mesmo signatário.
- **Status/histórico:** 0/N, parcial, N/N; eventos com data/hora; conclusão apenas após todos assinarem.
- **PDF final:** assinaturas nas coordenadas corretas; original preservado; abrir em somente leitura; salvar cópia; cancelar exportação.
- **Identidade/UI:** logos adequadas em Splash/Login/Perfil; sem retângulo preto no Dark; mesmas ações Light/Dark.
- **Idiomas/tema:** Android pt-BR/en/outro; alteração manual; mudança durante navegação; truncamento; font scaling.
- **Segurança:** arquivos privados, isolamento por usuário, credenciais não legíveis no banco/log.

### 20.2 Entregáveis da Sprint 1

- ZIP com código-fonte completo.
- APK Android instalável.
- Apresentação PPT/PDF com nome, integrantes e RM, problema, solução, tecnologias, fluxo, diferenciais, identidade visual e link do vídeo.
- Vídeo demonstrativo **até 5 minutos** com funcionalidades reais.

**Congelar o escopo antes da entrega.** A partir da estabilização, priorizar bugs bloqueantes, APK e apresentação, não novos recursos.

## 21. Instruções obrigatórias para IAs e agentes

Ao receber uma tarefa no repositório, **execute nesta ordem**:

1. Ler este arquivo por inteiro e conferir `README.md` e documentos `docs/` relevantes.
2. Consultar a planilha mestre, especialmente `DECISÕES`, `Backlog completo`, `MAPA DE TELAS`, `RASTREABILIDADE`, `DIVISÃO E DEPENDÊNCIAS` e `Controle de Pull Requests`.
3. Conferir o código da branch/`main` e não deduzir estado do projeto pelo protótipo.
4. Identificar ID LS, responsável, dependências, arquivos autorizados, critérios e testes mínimos.
5. Conferir referência visual correta no Figma e nos logos oficiais.
6. Confirmar qual idioma/tema e quais cenários a alteração atinge.
7. Propor alteração mínima; preservar contratos, estilo e funcionalidades existentes.
8. Implementar apenas arquivos permitidos e sem duplicar soluções.
9. Testar fluxo, persistência, segurança, EN/PT-BR e Light/Dark quando aplicáveis.
10. Preparar resumo, commit, PR, evidências e campos atualizados da planilha.

**Regras inegociáveis para IA:**

- Não alterar `main` nem fazer merge automaticamente sem autorização do processo.
- Não marcar conclusão por gerar código ou design.
- Não inventar status de tarefas, responsáveis, biblioteca aprovada, testes ou permissões.
- Não implementar recursos sugeridos pelo Figma fora da Sprint 1.
- Não renomear **Fides** nem criar outro Design System.
- Não criar uma experiência Dark diferente da Light.
- Não criar texto visível hardcoded; usar recursos de string EN/PT-BR.
- Não modificar classe compartilhada para resolver problema local sem avaliar impactos.
- Não misturar dados fictícios demonstrativos com dados reais.
- Não usar serviços pagos, tokens de IA, credenciais embutidas ou rede externa desnecessária.
- Em caso de dúvida, apresentar a escolha e sua consequência; não tomar decisão estrutural silenciosamente.

**Modelo curto de resposta da IA ao integrante:**

```text
Tarefa: LS-XXX — [nome]
Status real / responsável: [consultar planilha]
Dependências: [IDs]
Arquivos autorizados: [...]
O que já existe no repositório: [...]
Plano objetivo: [3–5 passos]
Alteração feita: [...]
Testes executados: [...]
Light/Dark + EN/PT-BR: [...]
Git/PR: [...]
Atualização necessária na planilha: [...]
Pontos pendentes: [...]
```

## 22. Decisões a sincronizar com a planilha

Este guia **acrescenta especificações de internacionalização e preferências** que não estavam detalhadas no planejamento revisado em 23/10/2026. Para manter **a planilha como fonte central**, sincronizar essas decisões no início da implementação correspondente.

**Atualização recomendada, sem alterar os IDs existentes sem consenso:**

| Local da planilha | O que registrar |
|---|---|
| `DECISÕES` | **Idiomas: inglês-base (`en`), tradução pt-BR, dispositivo como preferência inicial, seletor Sistema/English/Português (Brasil)**. |
| `DECISÕES` | Aparência padrão segue Sistema; opção manual Sistema/Claro/Escuro com preferência persistida. |
| **LS-006** | Design System inclui `FidesTheme` Light/Dark, tokens, fontes, componentes e base de strings localizadas. |
| **LS-039** | Perfil inclui seletor **Idioma** e **Aparência** simples, além de Sobre/logout; sincronizar critérios e horas se necessário. |
| **LS-041** | Auditoria inclui EN/PT-BR, locale do sistema, truncamento, escala de fonte e combinações com Light/Dark. |
| **LS-031/LS-032** | Incluir testes de idioma/tema entre as validações de regras e fluxos ponta a ponta. |
| `MAPA DE TELAS` | UI-17 e UI-18 devem incluir preferências/estados de idioma e tema quando adotados. |

Essas alterações **não são declaradas concluídas**. Até a planilha ser atualizada, tratar esta seção como **pendência explícita de sincronização**, não como permissão para alterar código compartilhado sem coordenação.

**Decisões ainda técnicas:** biblioteca definitiva de manipulação de PDF (**LS-024**), package Android definitivo e versões de SDK/dependências compatíveis (**LS-002/LS-003**), contratos exatos de entidades e enums (**LS-004**). Não inventar esses valores.

## 23. Referências técnicas oficiais

- Android Developers — [Jetpack Compose](https://developer.android.com/develop/ui/compose)
- Android Developers — [Material 3](https://developer.android.com/develop/ui/compose/designsystems/material3)
- Android Developers — [Temas no Compose](https://developer.android.com/develop/ui/compose/designsystems/material3#material_theming)
- Android Developers — [Idiomas por aplicativo](https://developer.android.com/guide/topics/resources/app-languages)
- Android Developers — [Localizar o aplicativo](https://developer.android.com/guide/topics/resources/localization)
- Android Developers — [Fontes no Compose](https://developer.android.com/develop/ui/compose/text/fonts)
- Android Developers — [Room](https://developer.android.com/training/data-storage/room)
- Android Developers — [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- Android Developers — [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider)
- Android Developers — [PdfRenderer](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer)
- Google Fonts — [Inter](https://fonts.google.com/specimen/Inter)
- Licença — [Inter OFL 1.1](https://github.com/google/fonts/blob/main/ofl/inter/OFL.txt)

---

## Registro de manutenção

| Data | Versão | Alteração |
|---|---|---|
| 23/10/2026 | 1.0 | Guia inicial consolidado com planilha mestre, identidade Fides, Figma, assets oficiais, arquitetura Sprint 1, Light/Dark, EN/PT-BR, segurança, colaboração e testes. |

**Regra de manutenção:** mudanças aprovadas de produto/design/arquitetura devem atualizar este arquivo e a aba `DECISÕES` da planilha em conjunto, com commit/PR identificado. A planilha continua sendo a fonte viva de **responsáveis, datas, status e dependências**. O Figma continua sendo a fonte de referência **visual**; este documento define padrões comuns que todos devem respeitar.
