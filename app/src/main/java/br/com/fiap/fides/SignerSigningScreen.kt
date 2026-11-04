package br.com.fiap.fides

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import br.com.fiap.fides.core.pdf.PdfExportService
import br.com.fiap.fides.core.pdf.PdfPageRenderer
import br.com.fiap.fides.core.pdf.PdfSignatureService
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.repository.LocalDocumentRepository
import br.com.fiap.fides.data.repository.LocalSignerRepository
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.SignerRole
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.usecase.ApplySignatureUseCase
import br.com.fiap.fides.domain.usecase.CaptureSignatureUseCase
import br.com.fiap.fides.domain.usecase.CompleteDocumentUseCase
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
internal fun SignerSigningScreen(
    database: FidesDatabase,
    sessionRepository: SessionRepository,
    documentId: String,
    onFinished: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val repository = remember(database) { LocalDocumentRepository(database) }
    val signerRepository = remember(database) { LocalSignerRepository(database.signerDao()) }
    val pageRenderer = remember { PdfPageRenderer() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val session by sessionRepository.session.collectAsState(initial = null)
    var document by remember { mutableStateOf<Document?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var signedFilePath by remember { mutableStateOf<String?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewPageIndex by remember { mutableStateOf(0) }
    var previewPageCount by remember { mutableStateOf(0) }
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var drawingSize by remember { mutableStateOf(IntSize.Zero) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { destination ->
        val sourcePath = signedFilePath ?: return@rememberLauncherForActivityResult
        if (destination == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            exportMessage = runCatching {
                PdfExportService(context).exportTo(sourcePath, destination)
                "PDF assinado exportado com sucesso."
            }.getOrElse { it.message ?: "Não foi possível exportar o PDF assinado." }
            busy = false
        }
    }

    LaunchedEffect(documentId, session?.userId) {
        val loaded = repository.getById(documentId)
        if (loaded == null || session?.userId == null || loaded.signers.none { it.signerId == session?.userId }) {
            document = null
            signedFilePath = null
            error = "Este documento não está vinculado ao usuário autenticado."
        } else {
            document = loaded
            signedFilePath = loaded.signedFilePath
            error = null
        }
    }

    val currentSignerHasSigned = document?.signers
        ?.firstOrNull { it.signerId == session?.userId }
        ?.signedAt != null

    val previewPath = signedFilePath ?: document?.localFilePath
    LaunchedEffect(previewPath, previewPageIndex) {
        val filePath = previewPath ?: return@LaunchedEffect
        runCatching {
            val count = pageRenderer.pageCount(filePath)
            count to pageRenderer.renderPage(filePath, previewPageIndex.coerceIn(0, (count - 1).coerceAtLeast(0)))
        }.onSuccess { (count, bitmap) ->
            previewPageCount = count
            previewBitmap = bitmap
        }.onFailure {
            previewBitmap = null
            error = it.message ?: "Não foi possível visualizar o PDF."
        }
    }

    fun clearDrawing() {
        strokes.clear()
        currentStroke = emptyList()
        error = null
    }

    fun removeLastStroke() {
        when {
            currentStroke.isNotEmpty() -> currentStroke = emptyList()
            strokes.isNotEmpty() -> strokes.removeAt(strokes.lastIndex)
        }
        error = null
    }

    fun confirmSignature() {
        val currentDocument = document ?: return
        val signerId = session?.userId ?: return
        if (strokes.isEmpty() && currentStroke.isEmpty()) {
            error = "Desenhe sua assinatura antes de confirmar."
            return
        }
        scope.launch {
            busy = true
            error = runCatching {
                val signaturePath = saveSignature(context, strokes + listOf(currentStroke).filter { it.isNotEmpty() }, drawingSize)
                currentDocument.signatureFields
                    .filter { it.signerId == signerId && it.required }
                    .forEach { field ->
                        CaptureSignatureUseCase(repository)(currentDocument.id, signerId, field.id, signaturePath)
                    }
                ApplySignatureUseCase(repository)(currentDocument.id, signerId)
                val refreshed = repository.getById(currentDocument.id)
                if (refreshed?.signers?.all { !it.required || it.signedAt != null } == true) {
                    CompleteDocumentUseCase(repository)(currentDocument.id, signerId)
                }
                val latest = repository.getById(currentDocument.id)
                    ?: throw IllegalStateException("Documento não encontrado após a assinatura.")
                val signedFile = File(context.filesDir, "documents/${latest.id}-signed-v${latest.version}.pdf")
                val signerNames = signerRepository.getAll().associate { it.id to it.fullName }
                val signerLabels = latest.signers.associate { participant ->
                    val name = signerNames[participant.signerId] ?: participant.signerId
                    participant.signerId to "$name — ${participant.role.signatureLabel()}"
                }
                PdfSignatureService(context).applySignatures(
                    document = latest,
                    signatures = repository.getSignatures(latest.id),
                    outputFile = signedFile,
                    signerLabels = signerLabels,
                )
                repository.save(latest.copy(signedFilePath = signedFile.absolutePath))
                null
            }.getOrElse { it.message ?: "Não foi possível salvar a assinatura." }
            busy = false
            if (error == null) {
                document = repository.getById(currentDocument.id)
                signedFilePath = document?.signedFilePath
                clearDrawing()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onFinished,
                    enabled = !busy,
                    shape = MaterialTheme.shapes.small,
                ) { Text("Voltar") }
                Text("Assinar documento", style = MaterialTheme.typography.headlineLarge)
                document?.let {
                    Text(it.title, style = MaterialTheme.typography.titleLarge)
                    Text("${it.signatureFields.count { field -> field.signerId == session?.userId && field.required }} campo(s) obrigatório(s) para você")
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (currentSignerHasSigned) "Sua assinatura foi registrada" else "Revise o documento",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (currentSignerHasSigned && signedFilePath != null) {
                        Button(
                            onClick = { exportLauncher.launch("${document?.title ?: "documento"}-assinado.pdf") },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                        ) { Text("Salvar cópia em PDF") }
                        Text(
                            "A cópia contém todas as assinaturas registradas até o momento.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        exportMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                    }
                    previewBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Página ${previewPageIndex + 1} do PDF",
                            modifier = Modifier.fillMaxWidth().aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat()),
                            contentScale = ContentScale.Fit,
                        )
                        Text(
                            "Página ${previewPageIndex + 1} de $previewPageCount",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { previewPageIndex = (previewPageIndex - 1).coerceAtLeast(0) },
                                enabled = previewPageIndex > 0,
                                modifier = Modifier.weight(1f),
                            ) { Text("Anterior") }
                            OutlinedButton(
                                onClick = { previewPageIndex = (previewPageIndex + 1).coerceAtMost((previewPageCount - 1).coerceAtLeast(0)) },
                                enabled = previewPageIndex + 1 < previewPageCount,
                                modifier = Modifier.weight(1f),
                            ) { Text("Próxima") }
                        }
                    } ?: Text("Carregando PDF...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (document != null && !currentSignerHasSigned) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Desenhe sua assinatura", style = MaterialTheme.typography.titleMedium)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Color.White)
                                .onSizeChanged { drawingSize = it }
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { currentStroke = listOf(it) },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            currentStroke = currentStroke + change.position
                                        },
                                        onDragEnd = {
                                            if (currentStroke.isNotEmpty()) strokes += currentStroke
                                            currentStroke = emptyList()
                                        },
                                        onDragCancel = { currentStroke = emptyList() },
                                    )
                                },
                        ) {
                            (strokes + listOf(currentStroke).filter { it.isNotEmpty() }).forEach { stroke ->
                                val path = Path()
                                stroke.forEachIndexed { index, point ->
                                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                                }
                                drawPath(path, color = Color(0xFF102A43), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = ::removeLastStroke,
                                enabled = !busy && (strokes.isNotEmpty() || currentStroke.isNotEmpty()),
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                            ) { Text("Desfazer último traço") }
                            OutlinedButton(
                                onClick = ::clearDrawing,
                                enabled = !busy && (strokes.isNotEmpty() || currentStroke.isNotEmpty()),
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small,
                            ) { Text("Limpar tudo") }
                        }
                        Button(
                            onClick = ::confirmSignature,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                        ) { Text(if (busy) "Salvando..." else "Confirmar assinatura") }
                    }
                }
            }
        }
        error?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error) }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

private fun saveSignature(context: Context, strokes: List<List<Offset>>, size: IntSize): String {
    require(size.width > 0 && size.height > 0) { "A área de assinatura ainda não está pronta." }
    val bitmap = createBitmap(720, 320)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.rgb(16, 42, 67)
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    val scaleX = 720f / size.width
    val scaleY = 320f / size.height
    strokes.forEach { stroke ->
        if (stroke.isEmpty()) return@forEach
        val path = AndroidPath()
        stroke.forEachIndexed { index, point ->
            val x = point.x * scaleX
            val y = point.y * scaleY
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)
    }
    val file = File(context.filesDir, "signatures/${UUID.randomUUID()}.png").apply { parentFile?.mkdirs() }
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    bitmap.recycle()
    return file.absolutePath
}

private fun SignerRole.signatureLabel(): String = when (this) {
    SignerRole.CONTRACTOR -> "Contratante"
    SignerRole.CONTRACTED -> "Contratado"
    SignerRole.WITNESS -> "Testemunha"
}
