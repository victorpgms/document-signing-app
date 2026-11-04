package br.com.fiap.fides

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import br.com.fiap.fides.core.pdf.PdfExportService
import br.com.fiap.fides.core.pdf.PdfPageRenderer
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.repository.LocalDocumentRepository
import br.com.fiap.fides.data.repository.LocalSignerRepository
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentEventType
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.usecase.CreateNewDocumentVersionUseCase
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
internal fun DocumentDetailsScreen(
    database: FidesDatabase,
    sessionRepository: SessionRepository,
    documentId: String,
    onBack: () -> Unit,
    onEditVersion: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val session by sessionRepository.session.collectAsState(initial = null)
    val repository = remember(database) { LocalDocumentRepository(database) }
    val signerRepository = remember(database) { LocalSignerRepository(database.signerDao()) }
    val renderer = remember { PdfPageRenderer() }
    var document by remember { mutableStateOf<Document?>(null) }
    var events by remember { mutableStateOf(emptyList<DocumentEvent>()) }
    var signerNames by remember { mutableStateOf(emptyMap<String, String>()) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    var pageIndex by rememberSaveable { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var exporting by remember { mutableStateOf(false) }
    var creatingVersion by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { destination ->
        val signedPath = document?.signedFilePath ?: return@rememberLauncherForActivityResult
        if (destination == null) return@rememberLauncherForActivityResult
        scope.launch {
            exporting = true
            message = runCatching {
                PdfExportService(context).exportTo(signedPath, destination)
                "PDF final exportado com sucesso."
            }.getOrElse { it.message ?: "Não foi possível exportar o PDF final." }
            exporting = false
        }
    }

    LaunchedEffect(documentId) {
        loading = true
        runCatching {
            val loaded = repository.getById(documentId) ?: error("Documento não encontrado.")
            val loadedEvents = repository.getEvents(documentId)
            val names = signerRepository.getAll().associate { it.id to it.fullName }
            Triple(loaded, loadedEvents, names)
        }.onSuccess { (loaded, loadedEvents, names) ->
            document = loaded
            events = loadedEvents
            signerNames = names
            message = null
        }.onFailure { message = it.message ?: "Não foi possível abrir o documento." }
        loading = false
    }

    val selectedFilePath = document?.signedFilePath ?: document?.localFilePath
    LaunchedEffect(selectedFilePath, pageIndex) {
        val filePath = selectedFilePath ?: return@LaunchedEffect
        runCatching {
            val count = renderer.pageCount(filePath)
            count to renderer.renderPage(filePath, pageIndex.coerceIn(0, (count - 1).coerceAtLeast(0)))
        }.onSuccess { (count, bitmap) ->
            pageCount = count
            pageBitmap = bitmap
        }.onFailure {
            pageBitmap = null
            message = it.message ?: "Não foi possível exibir o PDF."
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            OutlinedButton(onClick = onBack, shape = MaterialTheme.shapes.small) { Text("Voltar") }
            Spacer(Modifier.height(12.dp))
            Text("Detalhes do documento", style = MaterialTheme.typography.headlineLarge)
        }
        when {
            loading -> item { Text("Carregando documento...") }
            document != null -> {
                val current = document!!
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(current.title, style = MaterialTheme.typography.titleLarge)
                            Text(current.status.displayLabel(), color = MaterialTheme.colorScheme.primary)
                            Text("Arquivo: ${current.originalFileName}")
                            Text("Versão ${current.version}")
                            Text("${current.signers.count { it.signedAt != null }} de ${current.signers.count { it.required }} assinatura(s) concluída(s)")
                            if (current.status != DocumentStatus.DRAFT) {
                                OutlinedButton(
                                    onClick = {
                                        val actorId = session?.userId ?: return@OutlinedButton
                                        scope.launch {
                                            creatingVersion = true
                                            runCatching { CreateNewDocumentVersionUseCase(repository)(current.id, actorId) }
                                                .onSuccess { onEditVersion(it.id) }
                                                .onFailure { message = it.message ?: "Não foi possível criar uma nova versão." }
                                            creatingVersion = false
                                        }
                                    },
                                    enabled = !creatingVersion,
                                ) { Text(if (creatingVersion) "Criando versão..." else "Criar nova versão") }
                            }
                        }
                    }
                }
                item {
                    Text(if (current.signedFilePath != null) "PDF final" else "PDF original", style = MaterialTheme.typography.titleMedium)
                    pageBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Página ${pageIndex + 1} do PDF",
                            modifier = Modifier.fillMaxWidth()
                                .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Fit,
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OutlinedButton(
                                onClick = { pageIndex = (pageIndex - 1).coerceAtLeast(0) },
                                enabled = pageIndex > 0,
                            ) { Text("Anterior") }
                            Text("Página ${pageIndex + 1} de $pageCount", modifier = Modifier.padding(top = 12.dp))
                            OutlinedButton(
                                onClick = { pageIndex = (pageIndex + 1).coerceAtMost((pageCount - 1).coerceAtLeast(0)) },
                                enabled = pageIndex + 1 < pageCount,
                            ) { Text("Próxima") }
                        }
                    } ?: Text("Carregando visualização...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (current.signedFilePath != null) {
                        Button(
                            onClick = { exportLauncher.launch("${current.title}-assinado.pdf") },
                            enabled = !exporting,
                            modifier = Modifier.padding(top = 10.dp),
                        ) { Text(if (exporting) "Exportando..." else "Salvar cópia do PDF final") }
                    }
                }
                item {
                    Text("Participantes", style = MaterialTheme.typography.titleMedium)
                    current.signers.forEach { signer ->
                        val name = signerNames[signer.signerId] ?: signer.signerId
                        Text("$name · ${signer.role.displayLabel()} · ${if (signer.signedAt == null) "Pendente" else "Assinado"}")
                    }
                }
                item {
                    Text("Histórico", style = MaterialTheme.typography.titleMedium)
                    if (events.isEmpty()) {
                        Text("Nenhum evento registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        events.forEach { event ->
                            val actor = if (event.actorId == session?.userId) session?.displayName else signerNames[event.actorId]
                            val date = event.occurredAt.atZone(ZoneId.systemDefault()).format(eventDateFormatter)
                            Text("$date · ${event.type.displayLabel()}${actor?.let { " · $it" } ?: ""}")
                        }
                    }
                }
            }
        }
        message?.let { currentMessage -> item { Text(currentMessage, color = MaterialTheme.colorScheme.primary) } }
    }
}

private fun br.com.fiap.fides.domain.model.SignerRole.displayLabel(): String = when (this) {
    br.com.fiap.fides.domain.model.SignerRole.CONTRACTOR -> "Contratante"
    br.com.fiap.fides.domain.model.SignerRole.CONTRACTED -> "Contratado"
    br.com.fiap.fides.domain.model.SignerRole.WITNESS -> "Testemunha"
}

private fun DocumentEventType.displayLabel(): String = when (this) {
    DocumentEventType.CREATED -> "Documento criado"
    DocumentEventType.SENT -> "Solicitação enviada"
    DocumentEventType.SIGNED -> "Assinatura registrada"
    DocumentEventType.COMPLETED -> "Documento concluído"
}

private val eventDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
