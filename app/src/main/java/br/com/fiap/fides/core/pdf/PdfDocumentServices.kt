package br.com.fiap.fides.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

data class PdfPageInfo(val index: Int, val width: Int, val height: Int)

class PdfPageRenderer {
    suspend fun pageCount(filePath: String): Int = withContext(Dispatchers.IO) {
        openRenderer(filePath).use { it.pageCount }
    }

    suspend fun pageInfo(filePath: String, pageIndex: Int): PdfPageInfo = withContext(Dispatchers.IO) {
        openRenderer(filePath).use { renderer ->
            validatePageIndex(pageIndex, renderer.pageCount)
            renderer.openPage(pageIndex).use { page -> PdfPageInfo(pageIndex, page.width, page.height) }
        }
    }

    suspend fun renderPage(filePath: String, pageIndex: Int): Bitmap = withContext(Dispatchers.IO) {
        openRenderer(filePath).use { renderer ->
            validatePageIndex(pageIndex, renderer.pageCount)
            renderer.openPage(pageIndex).use { page ->
                Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888).also { bitmap ->
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
            }
        }
    }

    private fun openRenderer(filePath: String): PdfRenderer {
        return try {
            PdfRenderer(ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY))
        } catch (exception: Exception) {
            throw IOException("Não foi possível abrir o PDF para visualização.", exception)
        }
    }
}

class PdfExportService(private val context: Context) {
    suspend fun exportTo(sourceFilePath: String, destination: Uri): Long = withContext(Dispatchers.IO) {
        val source = File(sourceFilePath)
        require(source.isFile) { "O PDF original não foi encontrado." }
        val input = source.inputStream()
        val output = context.contentResolver.openOutputStream(destination)
            ?: throw IOException("O destino de exportação não pôde ser aberto.")
        input.use { sourceStream -> output.use { destinationStream -> sourceStream.copyTo(destinationStream) } }
        source.length()
    }
}

internal fun validatePageIndex(pageIndex: Int, pageCount: Int) {
    require(pageIndex in 0 until pageCount) { "Página fora dos limites do PDF." }
}
