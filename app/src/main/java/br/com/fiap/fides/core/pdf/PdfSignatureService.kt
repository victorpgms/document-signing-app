package br.com.fiap.fides.core.pdf

import android.content.Context
import android.graphics.BitmapFactory
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.SignatureRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class PdfSignatureException(message: String, cause: Throwable? = null) : IOException(message, cause)

/** Writes captured signature images to a new PDF version and never overwrites the imported original. */
class PdfSignatureService(context: Context) {
    init {
        PDFBoxResourceLoader.init(context.applicationContext)
    }

    suspend fun applySignatures(
        document: Document,
        signatures: List<SignatureRecord>,
        outputFile: File,
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
                                field.width * pageWidth,
                                field.height * pageHeight,
                            )
                        }
                    } finally {
                        bitmap.recycle()
                    }
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
}
