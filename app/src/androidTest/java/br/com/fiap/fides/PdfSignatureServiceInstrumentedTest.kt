package br.com.fiap.fides

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.fiap.fides.core.pdf.PdfSignatureService
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.SignatureField
import br.com.fiap.fides.domain.model.SignatureRecord
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PdfSignatureServiceInstrumentedTest {
    @Test
    fun writesSignatureToNewReadablePdfAndPreservesOriginal() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(context.cacheDir, "pdf-signature-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val original = File(directory, "original.pdf")
            PDDocument().use { pdf ->
                pdf.addPage(PDPage(PDRectangle.LETTER))
                pdf.save(original)
            }
            val signatureFile = File(directory, "signature.png")
            Bitmap.createBitmap(80, 40, Bitmap.Config.ARGB_8888).also { bitmap ->
                Canvas(bitmap).drawColor(Color.rgb(16, 42, 67))
                signatureFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
            val field = SignatureField("field-1", "document-1", "signer-1", 0, .1f, .2f, .3f, .1f)
            val document = Document(
                id = "document-1",
                ownerId = "owner-1",
                title = "Teste",
                originalFileName = "original.pdf",
                localFilePath = original.absolutePath,
                status = DocumentStatus.COMPLETED,
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                signatureFields = listOf(field),
            )
            val record = SignatureRecord(
                id = "record-1",
                documentId = document.id,
                signerId = "signer-1",
                fieldId = field.id,
                localFilePath = signatureFile.absolutePath,
                createdAt = Instant.EPOCH,
            )
            val originalBytes = original.readBytes()
            val output = File(directory, "signed.pdf")

            val result = PdfSignatureService(context).applySignatures(document, listOf(record), output)

            assertEquals(output.absolutePath, result.absolutePath)
            assertTrue(output.isFile)
            assertArrayEquals(originalBytes, original.readBytes())
            ParcelFileDescriptor.open(output, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer -> assertEquals(1, renderer.pageCount) }
            }
        } finally {
            directory.deleteRecursively()
        }
    }
}
