# Checklist de QA da Sprint 1

## Autenticação e sessão

- [x] O botão Entrar responde sem congelar a interface.
- [ ] Credenciais inválidas exibem erro e permitem nova tentativa.
- [ ] Administrador e signatários são encaminhados aos painéis corretos.
- [ ] A sessão é restaurada após reiniciar o aplicativo.
- [ ] Sair encerra a sessão e volta ao login.

## Documentos e participantes

- [ ] Pesquisa, Todos, Em andamento e Concluídos usam dados reais.
- [ ] Os cartões abrem os detalhes do documento.
- [x] É possível selecionar vários signatários e um papel para cada um.
- [x] Cada campo é associado ao participante escolhido.
- [ ] Campos não podem sair dos limites da página.
- [ ] Arrastar e excluir campos não cria campos adicionais.
- [ ] A revisão mostra PDF, participantes, papéis e campos.
- [ ] Um envio repetido não cria outro evento de envio.
- [ ] Documento enviado não pode ser editado; alterações criam nova versão.

## Assinatura e PDF

- [x] O signatário visualiza todas as páginas antes de assinar.
- [x] O signatário acessa somente documentos vinculados.
- [x] A assinatura pode ser limpa e redesenhada antes da confirmação.
- [x] O status parcial aparece depois da primeira assinatura.
- [x] O status concluído aparece apenas depois de todas as assinaturas obrigatórias.
- [x] O PDF final contém as assinaturas nas posições corretas.
- [ ] O PDF original permanece inalterado.
- [x] Administrador e signatário autorizado conseguem salvar uma cópia.

## Compatibilidade e entrega

- [ ] PDF vazio, inválido e corrompido exibe erro.
- [ ] PDF multipágina, tamanhos variados e rotação são validados.
- [ ] Temas Sistema, Claro e Escuro preservam conteúdo e contraste.
- [ ] Estados vazio, carregando, erro e sucesso são compreensíveis.
- [ ] Testes unitários, instrumentados, assembleDebug e assembleRelease passam.
- [x] APK de depuração instala e abre no BlueStacks.
