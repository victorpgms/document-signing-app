package br.com.fiap.fides.core.pdf

import android.content.Context
import android.graphics.BitmapFactory
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.SignatureRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.text.Normalizer
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

class PdfSignatureException(message: String, cause: Throwable? = null) : IOException(message, cause)

data class PdfSignerSummary(
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val signedAtEpochMillis: Long?,
    val signatureFilePath: String?,
)

/** Writes captured signature images to a new PDF version and never overwrites the imported original. */
class PdfSignatureService(context: Context) {
    private val appContext = context.applicationContext

    init {
        PDFBoxResourceLoader.init(appContext)
    }

    suspend fun applySignatures(
        document: Document,
        signatures: List<SignatureRecord>,
        outputFile: File,
        signerLabels: Map<String, String> = emptyMap(),
        signerSummaries: List<PdfSignerSummary> = emptyList(),
        validationToken: String? = null,
        includeSummary: Boolean = false,
    ): File = withContext(Dispatchers.IO) {
        val sourceFile = File(document.localFilePath)
        if (!sourceFile.isFile) throw PdfSignatureException("O PDF original não foi encontrado.")
        if (signatures.isEmpty()) throw PdfSignatureException("Nenhuma assinatura capturada para aplicar.")

        outputFile.parentFile?.mkdirs()
        val temporaryFile = File(outputFile.parentFile, "${outputFile.name}.tmp")
        try {
            PDDocument.load(sourceFile).use { pdf ->
                signatures.distinctBy { it.fieldId }.forEach { record ->
                    val field = document.signatureFields.firstOrNull { it.id == record.fieldId }
                        ?: throw PdfSignatureException("O campo da assinatura não foi encontrado.")
                    val page = runCatching { pdf.getPage(field.pageIndex) }
                        .getOrElse { throw PdfSignatureException("A página do campo da assinatura não foi encontrada.", it) }
                    val bitmap = BitmapFactory.decodeFile(record.localFilePath)
                        ?: throw PdfSignatureException("A imagem da assinatura não pôde ser lida.")
                    try {
                        val signatureBitmap = bitmap
                        val image = LosslessFactory.createFromImage(pdf, signatureBitmap)
                        val pageWidth = page.mediaBox.width
                        val pageHeight = page.mediaBox.height
                        val x = field.x * pageWidth
                        val y = pageHeight - ((field.y + field.height) * pageHeight)
                        val fieldWidth = field.width * pageWidth
                        val fieldHeight = field.height * pageHeight
                        PDPageContentStream(
                            pdf,
                            page,
                            PDPageContentStream.AppendMode.APPEND,
                            true,
                            true,
                        ).use { stream ->
                            stream.drawImage(
                                image,
                                x,
                                y,
                                fieldWidth,
                                fieldHeight,
                            )
                            signerLabels[field.signerId]?.takeIf { it.isNotBlank() }?.let { label ->
                                val labelHeight = minOf(14f, fieldHeight * .28f).coerceAtLeast(9f)
                                val fontSize = minOf(8f, labelHeight * .62f)
                                val safeLabel = label
                                    .replace('—', '-')
                                    .filter { it.code in 32..255 }
                                    .take((fieldWidth / (fontSize * .52f)).toInt().coerceAtLeast(1))
                                stream.setNonStrokingColor(255, 255, 255)
                                stream.addRect(x, y, fieldWidth, labelHeight)
                                stream.fill()
                                stream.beginText()
                                stream.setNonStrokingColor(50, 16, 135)
                                stream.setFont(PDType1Font.HELVETICA_BOLD, fontSize)
                                stream.newLineAtOffset(x + 3f, y + (labelHeight - fontSize) / 2f)
                                stream.showText(safeLabel)
                                stream.endText()
                            }
                        }
                    } finally {
                        bitmap.recycle()
                    }
                }
                if (includeSummary && !validationToken.isNullOrBlank()) {
                    appendSignatureSummaryStyled(pdf, document, signerSummaries, validationToken)
                }
                pdf.save(temporaryFile)
            }
            if (outputFile.exists() && !outputFile.delete()) {
                throw PdfSignatureException("Não foi possível substituir a versão assinada anterior.")
            }
            if (!temporaryFile.renameTo(outputFile)) {
                temporaryFile.copyTo(outputFile, overwrite = false)
                temporaryFile.delete()
            }
            outputFile
        } catch (exception: PdfSignatureException) {
            temporaryFile.delete()
            throw exception
        } catch (exception: Exception) {
            temporaryFile.delete()
            throw PdfSignatureException("Não foi possível gerar o PDF assinado.", exception)
        }
    }

    private fun appendSignatureSummaryLegacy(
        pdf: PDDocument,
        document: Document,
        signers: List<PdfSignerSummary>,
        validationToken: String,
    ) {
        val chunks = signers.ifEmpty {
            listOf(PdfSignerSummary("Nenhum participante", "", "", "", null, null))
        }.chunked(4)
        chunks.forEachIndexed { index, chunk ->
            val page = PDPage(PDRectangle.A4)
            pdf.addPage(page)
            PDPageContentStream(pdf, page).use { stream ->
                val width = page.mediaBox.width
                val height = page.mediaBox.height
                stream.setNonStrokingColor(0, 106, 204)
                stream.addRect(0f, height - 78f, width, 78f)
                stream.fill()
                stream.beginText()
                stream.setNonStrokingColor(255, 255, 255)
                stream.setFont(PDType1Font.HELVETICA_BOLD, 22f)
                stream.newLineAtOffset(36f, height - 45f)
                stream.showText("FIDES")
                stream.endText()

                writeText(stream, 36f, height - 108f, "Comprovante de assinaturas", 16f, bold = true, color = intArrayOf(63, 5, 190))
                writeText(stream, 36f, height - 130f, "Documento: ${safe(document.title)}", 10f)
                writeText(stream, 36f, height - 146f, "Arquivo original: ${safe(document.originalFileName)}", 10f)
                writeText(stream, 36f, height - 162f, "Código de validação: ${safe(validationToken)}", 10f, bold = true)
                val qr = createQrBitmap("fides://validar/$validationToken")
                val qrImage = LosslessFactory.createFromImage(pdf, qr)
                stream.drawImage(qrImage, width - 132f, height - 190f, 96f, 96f)
                qr.recycle()

                var y = height - 210f
                chunk.forEach { signer ->
                    stream.setStrokingColor(226, 232, 240)
                    stream.addRect(36f, y - 90f, width - 204f, 78f)
                    stream.stroke()
                    writeText(stream, 48f, y - 30f, safe(signer.fullName), 11f, bold = true, color = intArrayOf(17, 24, 39))
                    writeText(stream, 48f, y - 46f, "E-mail: ${safeOrDash(signer.email)}", 9f)
                    writeText(stream, 48f, y - 60f, "Telefone: ${safeOrDash(signer.phone)}", 9f)
                    writeText(stream, 48f, y - 74f, "Papel: ${safeOrDash(signer.role)}", 9f)
                    val status = if (signer.signedAtEpochMillis == null) "Pendente" else "Assinado em ${formatInstant(signer.signedAtEpochMillis)}"
                    writeText(stream, 260f, y - 30f, "Status: $status", 9f, bold = true)
                    val signaturePath = signer.signatureFilePath
                    if (!signaturePath.isNullOrBlank()) {
                        BitmapFactory.decodeFile(signaturePath)?.let { bitmap ->
                            try {
                                stream.drawImage(LosslessFactory.createFromImage(pdf, bitmap), width - 180f, y - 78f, 125f, 42f)
                            } finally {
                                bitmap.recycle()
                            }
                        }
                    }
                    y -= 104f
                }
                writeText(stream, 36f, 32f, "Página de resumo adicionada pelo Fides. O conteúdo original foi preservado.", 8f, color = intArrayOf(100, 116, 139))
                if (chunks.size > 1) writeText(stream, width - 100f, 32f, "${index + 1}/${chunks.size}", 8f, color = intArrayOf(100, 116, 139))
            }
        }
    }

    private fun appendSignatureSummaryStyled(
        pdf: PDDocument,
        document: Document,
        signers: List<PdfSignerSummary>,
        validationToken: String,
    ) {
        val chunks = signers.ifEmpty {
            listOf(PdfSignerSummary("Nenhum participante", "", "", "", null, null))
        }.chunked(4)

        chunks.forEachIndexed { index, chunk ->
            val page = PDPage(PDRectangle.A4)
            pdf.addPage(page)
            PDPageContentStream(pdf, page).use { stream ->
                val width = page.mediaBox.width
                val height = page.mediaBox.height
                val contentWidth = width - 72f

                stream.setNonStrokingColor(248, 250, 252)
                stream.addRect(0f, 0f, width, height)
                stream.fill()

                stream.setNonStrokingColor(63, 5, 190)
                stream.addRect(0f, height - 92f, width, 92f)
                stream.fill()
                stream.setNonStrokingColor(220, 153, 14)
                stream.addRect(0f, height - 98f, width, 6f)
                stream.fill()

                BitmapFactory.decodeResource(appContext.resources, br.com.fiap.fides.R.drawable.fides_logo_dark)?.let { logo ->
                    try {
                        stream.drawImage(LosslessFactory.createFromImage(pdf, logo), 36f, height - 64f, 116f, 30f)
                    } finally {
                        logo.recycle()
                    }
                }
                writeText(stream, 174f, height - 47f, "Comprovante de assinaturas", 13f, bold = true, color = intArrayOf(255, 255, 255))

                val documentBottom = height - 220f
                stream.setNonStrokingColor(255, 255, 255)
                stream.addRect(36f, documentBottom, contentWidth, 100f)
                stream.fill()
                stream.setStrokingColor(226, 232, 240)
                stream.addRect(36f, documentBottom, contentWidth, 100f)
                stream.stroke()
                stream.setNonStrokingColor(220, 153, 14)
                stream.addRect(36f, documentBottom, 5f, 100f)
                stream.fill()

                writeText(stream, 54f, height - 146f, "Documento", 9f, bold = true, color = intArrayOf(100, 116, 139))
                writeText(stream, 54f, height - 164f, safe(document.title), 13f, bold = true, color = intArrayOf(17, 24, 39))
                writeText(stream, 54f, height - 183f, "Arquivo: ${safe(document.originalFileName)}", 9f)
                writeText(stream, 54f, height - 201f, "Codigo de validacao: ${safe(validationToken)}", 8f, bold = true, color = intArrayOf(63, 5, 190))

                val qr = createQrBitmap("fides://validar/$validationToken")
                try {
                    stream.drawImage(LosslessFactory.createFromImage(pdf, qr), width - 126f, height - 206f, 78f, 78f)
                } finally {
                    qr.recycle()
                }

                var cardTop = height - 240f
                val cardHeight = 78f
                chunk.forEach { signer ->
                    val cardBottom = cardTop - cardHeight
                    stream.setNonStrokingColor(255, 255, 255)
                    stream.addRect(36f, cardBottom, contentWidth, cardHeight)
                    stream.fill()
                    stream.setStrokingColor(226, 232, 240)
                    stream.addRect(36f, cardBottom, contentWidth, cardHeight)
                    stream.stroke()
                    stream.setNonStrokingColor(220, 153, 14)
                    stream.addRect(36f, cardBottom, 4f, cardHeight)
                    stream.fill()

                    writeText(stream, 52f, cardTop - 20f, safe(signer.fullName), 11f, bold = true, color = intArrayOf(17, 24, 39))
                    writeText(stream, 52f, cardTop - 37f, "E-mail: ${safeOrDash(signer.email)}", 8f)
                    writeText(stream, 52f, cardTop - 51f, "Telefone: ${safeOrDash(signer.phone)}", 8f)
                    writeText(stream, 52f, cardTop - 65f, "Papel: ${safeOrDash(signer.role)}", 8f)
                    val status = if (signer.signedAtEpochMillis == null) {
                        "Pendente"
                    } else {
                        "Assinado em ${formatInstant(signer.signedAtEpochMillis)}"
                    }
                    writeText(stream, 360f, cardTop - 20f, "Status: ${safe(status).take(30)}", 8f, bold = true, color = intArrayOf(63, 5, 190))

                    val signaturePath = signer.signatureFilePath
                    if (!signaturePath.isNullOrBlank()) {
                        BitmapFactory.decodeFile(signaturePath)?.let { bitmap ->
                            try {
                                stream.drawImage(LosslessFactory.createFromImage(pdf, bitmap), width - 150f, cardBottom + 10f, 105f, 34f)
                            } finally {
                                bitmap.recycle()
                            }
                        }
                    } else {
                        writeText(stream, width - 150f, cardBottom + 24f, "Sem assinatura", 8f, color = intArrayOf(100, 116, 139))
                    }
                    cardTop = cardBottom - 10f
                }

                writeText(stream, 36f, 32f, "Resumo adicionado pelo Fides. O conteudo original foi preservado.", 8f, color = intArrayOf(100, 116, 139))
                writeText(stream, width - 72f, 32f, "${index + 1}/${chunks.size}", 8f, color = intArrayOf(100, 116, 139))
            }
        }
    }

    private fun writeText(
        stream: PDPageContentStream,
        x: Float,
        y: Float,
        text: String,
        size: Float,
        bold: Boolean = false,
        color: IntArray = intArrayOf(71, 85, 105),
    ) {
        stream.beginText()
        stream.setNonStrokingColor(color[0], color[1], color[2])
        stream.setFont(if (bold) PDType1Font.HELVETICA_BOLD else PDType1Font.HELVETICA, size)
        stream.newLineAtOffset(x, y)
        stream.showText(safe(text).take(115))
        stream.endText()
    }

    private fun safeOrDash(value: String): String = if (value.isBlank()) "Nao informado" else value

    private fun safe(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .replace("—", "-")
        .filter { it.code in 32..126 }

    private fun formatInstant(epochMillis: Long): String = java.time.Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm z", Locale.forLanguageTag("pt-BR")))

    private fun createQrBitmap(content: String): Bitmap {
        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            240,
            240,
            mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M),
        )
        return Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).also { bitmap ->
            for (x in 0 until matrix.width) for (y in 0 until matrix.height) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }
}
