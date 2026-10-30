# POC de PDF

## Decisao

O aplicativo usa `android.graphics.pdf.PdfRenderer` para validar PDFs importados e renderizar paginas no Android. Para gravar imagens de assinatura no arquivo final, a POC selecionou `com.tom-roush:pdfbox-android:2.0.27.0`.

O PDFBox-Android e inicializado no servico de PDF e recebe apenas arquivos privados do aplicativo. A implementacao usa coordenadas normalizadas da interface e converte a origem superior da tela para a origem inferior do PDF.

## Cobertura

- `PdfImportService` copia o URI selecionado para o armazenamento privado e rejeita arquivo vazio, invalido ou corrompido.
- `PdfPageRenderer` informa paginas, dimensoes e renderiza uma pagina por vez.
- O editor persiste campos vinculados ao signatario, permite mover dentro dos limites e excluir enquanto o documento e rascunho.
- `PdfSignatureService` aplica cada imagem capturada no campo correspondente e salva uma nova versao assinada, preservando o PDF original.
- `PdfExportService` envia a versao assinada para um URI escolhido pelo usuario sem modificar o arquivo privado.

## Limites conhecidos

- A POC considera a orientacao visual normal da pagina. PDFs com rotacao especifica devem ser validados em um teste dedicado.
- O armazenamento continua local nesta Sprint 1; a futura implementacao remota deve respeitar os contratos de repositorio existentes.
