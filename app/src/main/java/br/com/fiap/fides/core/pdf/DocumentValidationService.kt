package br.com.fiap.fides.core.pdf

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object ValidationTokenGenerator {
    fun create(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

data class PdfValidationData(
    val token: String?,
    val sha256: String,
)

class DocumentValidationService(context: android.content.Context) {
    init {
        PDFBoxResourceLoader.init(context.applicationContext)
    }

    suspend fun readValidationData(file: File): PdfValidationData = withContext(Dispatchers.IO) {
        require(file.isFile) { "O PDF informado não foi encontrado." }
        val text = PDDocument.load(file).use { document ->
            PDFTextStripper().getText(document)
        }
        val token = TOKEN_PATTERN.find(text)?.groupValues?.getOrNull(1)
        PdfValidationData(token, sha256(file))
    }

    suspend fun sha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read = input.read(buffer)
            while (read >= 0) {
                if (read > 0) digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    private companion object {
        val TOKEN_PATTERN = Regex("Codigo de validacao:\\s*([A-Za-z0-9_-]+)")
    }
}
