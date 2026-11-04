package br.com.fiap.fides

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.repository.LocalDocumentRepository
import br.com.fiap.fides.data.repository.LocalSignerRepository
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.usecase.CancelDocumentUseCase
import br.com.fiap.fides.domain.usecase.DeleteDraftUseCase
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class AdminDocumentFilter(val label: String) {
    ALL("Todos"),
    ACTIVE("Em andamento"),
    COMPLETED("Concluídos"),
}

@Composable
internal fun AdminDocumentsScreen(
    database: FidesDatabase,
    sessionRepository: SessionRepository,
    onCreate: () -> Unit,
    onOpenDocument: (String) -> Unit,
) {
    val session by sessionRepository.session.collectAsState(initial = null)
    val documentRepository = remember(database) { LocalDocumentRepository(database) }
    val signerRepository = remember(database) { LocalSignerRepository(database.signerDao(), database.userDao()) }
    var documents by remember { mutableStateOf(emptyList<Document>()) }
    var signerNames by remember { mutableStateOf(emptyMap<String, String>()) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(AdminDocumentFilter.ALL) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var pendingAction by remember { mutableStateOf<DocumentAction?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(session?.userId) {
        val userId = session?.userId ?: return@LaunchedEffect
        loading = true
        runCatching {
            val loadedDocuments = documentRepository.getAllForUser(userId)
            val loadedSigners = signerRepository.getAll().associate { it.id to "${it.fullName} · ${it.email}" }
            loadedDocuments to loadedSigners
        }.onSuccess { (loadedDocuments, loadedSigners) ->
            documents = loadedDocuments
            signerNames = loadedSigners
            loadError = null
        }.onFailure {
            loadError = it.message ?: "Não foi possível carregar os documentos."
        }
        loading = false
    }

    val activeCount = documents.count { it.status in activeStatuses }
    val completedCount = documents.count { it.status == DocumentStatus.COMPLETED }
    val filteredDocuments = documents.filter { document ->
        val matchesFilter = when (selectedFilter) {
            AdminDocumentFilter.ALL -> true
            AdminDocumentFilter.ACTIVE -> document.status in activeStatuses
            AdminDocumentFilter.COMPLETED -> document.status == DocumentStatus.COMPLETED
        }
        val participantNames = document.signers.mapNotNull { signerNames[it.signerId] }
        val matchesSearch = search.isBlank() ||
            document.title.contains(search, ignoreCase = true) ||
            document.originalFileName.contains(search, ignoreCase = true) ||
            participantNames.any { it.contains(search, ignoreCase = true) }
        matchesFilter && matchesSearch
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FidesHeader()
            Spacer(Modifier.height(18.dp))
            Text("Visão geral", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Olá, ${session?.displayName ?: "Administrador"}", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Pesquisar") },
                placeholder = { Text("Documento ou signatário") },
            )
            Row(
                modifier = Modifier.padding(top = 10.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AdminDocumentFilter.entries.forEach { filter ->
                    val count = when (filter) {
                        AdminDocumentFilter.ALL -> documents.size
                        AdminDocumentFilter.ACTIVE -> activeCount
                        AdminDocumentFilter.COMPLETED -> completedCount
                    }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text("${filter.label} ($count)") },
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("▣", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Novo documento", style = MaterialTheme.typography.titleMedium)
                        Text("Carregue um PDF para coletar assinaturas", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(onClick = onCreate, shape = MaterialTheme.shapes.small) { Text("+ Criar") }
                }
            }
        }
        item { Text("Documentos", style = MaterialTheme.typography.titleMedium) }
        when {
            loading -> item { Text("Carregando documentos...", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            loadError != null -> item { Text(loadError!!, color = MaterialTheme.colorScheme.error) }
            filteredDocuments.isEmpty() -> item {
                Text(
                    if (documents.isEmpty()) "Nenhum documento criado." else "Nenhum documento corresponde aos filtros.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> items(filteredDocuments, key = { it.id }) { document ->
                AdminDocumentCard(
                    document = document,
                    participantNames = document.signers.mapNotNull { signerNames[it.signerId] },
                    onClick = { onOpenDocument(document.id) },
                    onDeleteDraft = { pendingAction = DocumentAction.DeleteDraft(document) },
                    onCancel = { pendingAction = DocumentAction.Cancel(document) },
                )
            }
        }
    }

    pendingAction?.let { action ->
        val document = action.document
        val isDelete = action is DocumentAction.DeleteDraft
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(if (isDelete) "Excluir rascunho?" else "Cancelar solicitação?") },
            text = {
                Text(
                    if (isDelete) {
                        "O rascunho \"${document.title}\" será removido e não poderá ser recuperado."
                    } else {
                        "O cancelamento impedirá novas assinaturas, mas preservará o registro e o histórico da solicitação."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingAction = null
                        val actorId = session?.userId ?: return@TextButton
                        scope.launch {
                            actionError = runCatching {
                                if (isDelete) DeleteDraftUseCase(documentRepository)(document.id, actorId)
                                else CancelDocumentUseCase(documentRepository)(document.id, actorId)
                                reloadDocuments(documentRepository, signerRepository, actorId) { loaded, names ->
                                    documents = loaded
                                    signerNames = names
                                }
                                null
                            }.getOrElse { it.message ?: "Não foi possível concluir a operação." }
                        }
                    },
                ) { Text(if (isDelete) "Excluir" else "Cancelar solicitação") }
            },
            dismissButton = { TextButton(onClick = { pendingAction = null }) { Text("Voltar") } },
        )
    }
    actionError?.let { error ->
        AlertDialog(
            onDismissRequest = { actionError = null },
            title = { Text("Não foi possível concluir") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = { actionError = null }) { Text("Fechar") } },
        )
    }
}

@Composable
private fun AdminDocumentCard(
    document: Document,
    participantNames: List<String>,
    onClick: () -> Unit,
    onDeleteDraft: () -> Unit,
    onCancel: () -> Unit,
) {
    val requiredSigners = document.signers.filter { it.required }
    val signedCount = requiredSigners.count { it.signedAt != null }
    val requiredCount = requiredSigners.size
    val progress = if (requiredCount == 0) 0f else signedCount.toFloat() / requiredCount
    val date = document.updatedAt.atZone(ZoneId.systemDefault()).format(documentDateFormatter)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(document.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                "$date · ${document.originalFileName} · versão ${document.version}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (participantNames.isNotEmpty()) {
                Text(participantNames.joinToString(), style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Progresso de assinaturas", style = MaterialTheme.typography.bodySmall)
                Text("$signedCount de $requiredCount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
            )
            Text(document.status.displayLabel(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (document.status == DocumentStatus.DRAFT) {
                    TextButton(onClick = onDeleteDraft) { Text("Excluir rascunho") }
                }
                if (document.status == DocumentStatus.SENT || document.status == DocumentStatus.PARTIALLY_SIGNED) {
                    TextButton(onClick = onCancel) { Text("Cancelar solicitação", color = MaterialTheme.colorScheme.tertiary) }
                }
            }
        }
    }
}

private sealed interface DocumentAction {
    val document: Document

    data class DeleteDraft(override val document: Document) : DocumentAction
    data class Cancel(override val document: Document) : DocumentAction
}

private suspend fun reloadDocuments(
    documentRepository: LocalDocumentRepository,
    signerRepository: LocalSignerRepository,
    userId: String,
    onLoaded: (List<Document>, Map<String, String>) -> Unit,
) {
    onLoaded(
        documentRepository.getAllForUser(userId),
        signerRepository.getAll().associate { it.id to it.fullName },
    )
}

internal fun DocumentStatus.displayLabel(): String = when (this) {
    DocumentStatus.DRAFT -> "Rascunho"
    DocumentStatus.SENT -> "Aguardando assinaturas"
    DocumentStatus.PARTIALLY_SIGNED -> "Parcialmente assinado"
    DocumentStatus.COMPLETED -> "Concluído"
    DocumentStatus.CANCELLED -> "Cancelado"
}

private val activeStatuses = setOf(DocumentStatus.DRAFT, DocumentStatus.SENT, DocumentStatus.PARTIALLY_SIGNED)
private val documentDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("pt-BR"))
