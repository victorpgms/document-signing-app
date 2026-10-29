package br.com.fiap.fides.core.pdf

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

data class ImportedPdf(
    val documentId: String,
    val originalFileName: String,
    val localFilePath: String,
    val pageCount: Int,
    val sizeBytes: Long,
)

class InvalidPdfException(message: String, cause: Throwable? = null) : IOException(message, cause)

class PdfImportService(private val context: Context) {
    suspend fun import(uri: Uri, documentId: String = UUID.randomUUID().toString()): ImportedPdf =
        withContext(Dispatchers.IO) {
            val originalFileName = context.displayName(uri)
            val storageDirectory = File(context.filesDir, DOCUMENTS_DIRECTORY).apply { mkdirs() }
            val temporaryFile = File(storageDirectory, "$documentId.tmp")
            val destinationFile = File(storageDirectory, "$documentId.pdf")

            try {
                copyToTemporaryFile(uri, temporaryFile)
                val pageCount = validatePdf(temporaryFile)
                if (!temporaryFile.renameTo(destinationFile)) {
                    temporaryFile.copyTo(destinationFile, overwrite = true)
                    temporaryFile.delete()
                }
                ImportedPdf(
                    documentId = documentId,
                    originalFileName = originalFileName,
                    localFilePath = destinationFile.absolutePath,
                    pageCount = pageCount,
                    sizeBytes = destinationFile.length(),
                )
            } catch (exception: InvalidPdfException) {
                temporaryFile.delete()
                destinationFile.delete()
                throw exception
            } catch (exception: Exception) {
                temporaryFile.delete()
                destinationFile.delete()
                throw InvalidPdfException("Não foi possível importar o PDF selecionado.", exception)
            }
        }

    private fun copyToTemporaryFile(uri: Uri, target: File) {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw InvalidPdfException("O arquivo selecionado não pôde ser lido.")
        input.use { source ->
            target.outputStream().use { destination -> source.copyTo(destination) }
        }
        if (target.length() == 0L) {
            throw InvalidPdfException("O arquivo selecionado está vazio.")
        }
    }

    private fun validatePdf(file: File): Int {
        return try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    if (renderer.pageCount <= 0) {
                        throw InvalidPdfException("O PDF não possui páginas válidas.")
                    }
                    renderer.pageCount
                }
            }
        } catch (exception: InvalidPdfException) {
            throw exception
        } catch (exception: Exception) {
            throw InvalidPdfException("O arquivo selecionado não é um PDF válido ou está corrompido.", exception)
        }
    }

    private fun Context.displayName(uri: Uri): String {
        val queriedName = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        return sanitizePdfFileName(queriedName ?: uri.lastPathSegment ?: "documento.pdf")
    }

    private companion object {
        const val DOCUMENTS_DIRECTORY = "documents"
    }
}

internal fun sanitizePdfFileName(name: String): String {
    val sanitized = name.substringAfterLast('/').substringAfterLast('\\')
        .replace(Regex("[^A-Za-z0-9._ -]"), "_")
        .trim()
    return if (sanitized.isBlank()) "documento.pdf" else sanitized
}
