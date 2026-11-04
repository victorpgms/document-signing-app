# Resultado de validação — 02/11/2026

## Verificações concluídas

- `testDebugUnitTest`: 19 testes, sem falhas ou erros.
- `lintDebug`: concluído sem erro bloqueante.
- `assembleDebug`: APK de depuração gerado.
- `assembleRelease`: APK de release não assinado gerado.
- `git diff --check`: nenhuma inconsistência de espaços ou fim de linha.
- APK de depuração mais recente instalado e validado no BlueStacks.
- Fluxo sequencial de assinaturas validado com Victor Almeida, Ana Teste e Bruno Teste.
- Estados parcialmente assinado e concluído conferidos no aplicativo e no PDF.
- Exportação de cópia em PDF validada para administrador e signatários.

## Avisos não bloqueantes do lint

- Atualizações de Gradle, Kotlin e AndroidX disponíveis; a atualização foi deixada para uma etapa isolada para não misturar migração de dependências com a estabilização funcional.
- Avisos de `TrustManager` pertencem ao Bouncy Castle transitivo usado pelo PDFBox. O aplicativo não declara permissão de internet e o fluxo atual é local.
- O ícone legado ocupa a região quadrada inteira e deve receber uma variante adaptativa antes da publicação em loja.

## Validações pendentes em dispositivo

- Executar `connectedDebugAndroidTest` com um aparelho ou AVD disponível.
- Validar contraste e navegação nos temas Claro e Escuro em tamanhos de tela diferentes.

## Entrega para publicação

O APK de release atual é não assinado. A configuração do keystore e a geração do artefato final assinado ficarão sob responsabilidade do mantenedor da branch principal.
