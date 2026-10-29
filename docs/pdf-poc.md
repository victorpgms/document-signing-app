# POC de PDF

## Decisão

Na Sprint 1, o aplicativo utiliza `android.graphics.pdf.PdfRenderer`, disponível no Android, para validar arquivos importados e renderizar páginas sem adicionar uma biblioteca nativa externa.

## Cobertura da POC

- Importação: `PdfImportService` copia o URI selecionado para o diretório privado do aplicativo e rejeita arquivos vazios ou inválidos.
- Leitura: `PdfPageRenderer` abre o arquivo privado, informa a quantidade de páginas, consulta dimensões e renderiza uma página por vez em `Bitmap`.
- Exportação: `PdfExportService` copia o PDF para o URI fornecido pelo Storage Access Framework sem alterar o arquivo original.
- Limites: o índice de página é validado antes da abertura e falhas de leitura são propagadas como erro recuperável.

## Próxima integração

LS-025 pode usar `PdfPageRenderer` para navegação multipágina. O editor de campos da LS-026 deve converter posições da tela para coordenadas normalizadas e persistir esses campos no documento.
