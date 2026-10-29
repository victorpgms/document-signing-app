package br.com.fiap.fides

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.fides.core.pdf.ImportedPdf
import br.com.fiap.fides.core.pdf.PdfImportService
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.repository.LocalDocumentRepository
import br.com.fiap.fides.data.repository.LocalSignerRepository
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.model.SignerRole
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.usecase.AddSignatureFieldUseCase
import br.com.fiap.fides.domain.usecase.CreateDocumentDraftUseCase
import br.com.fiap.fides.domain.usecase.DocumentReview
import br.com.fiap.fides.domain.usecase.DocumentValidationException
import br.com.fiap.fides.domain.usecase.ReviewDocumentUseCase
import br.com.fiap.fides.domain.usecase.SendDocumentUseCase
import kotlinx.coroutines.launch

@Composable
internal fun DocumentFlowScreen(
    database: FidesDatabase,
    sessionRepository: SessionRepository,
    onFinished: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val session by sessionRepository.session.collectAsState(initial = null)
    val documentRepository = remember(database) { LocalDocumentRepository(database) }
    val signerRepository = remember(database) { LocalSignerRepository(database.signerDao()) }
    val importService = remember(context) { PdfImportService(context) }
    var step by rememberSaveable { mutableStateOf(1) }
    var title by rememberSaveable { mutableStateOf("") }
    var imported by remember { mutableStateOf<ImportedPdf?>(null) }
    var signers by remember { mutableStateOf(emptyList<Signer>()) }
    var selectedIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var role by rememberSaveable { mutableStateOf(SignerRole.CONTRACTOR.name) }
    var documentId by rememberSaveable { mutableStateOf<String?>(null) }
    var fields by remember { mutableStateOf(emptyList<br.com.fiap.fides.domain.model.SignatureField>()) }
    var review by remember { mutableStateOf<DocumentReview?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            runCatching { importService.import(uri) }
                .onSuccess { pdf ->
                    imported = pdf
                    if (title.isBlank()) title = pdf.originalFileName.substringBeforeLast('.')
                    message = "PDF importado com ${pdf.pageCount} página(s)."
                }
                .onFailure { message = it.message ?: "Não foi possível importar o PDF." }
            busy = false
        }
    }

    LaunchedEffect(Unit) { signers = signerRepository.getAll() }

    fun createDraftAndContinue() {
        val ownerId = session?.userId ?: return
        val pdf = imported ?: return
        scope.launch {
            busy = true
            runCatching {
                val selected = signers.filter { it.id in selectedIds }.map {
                    DocumentSigner(pdf.documentId, it.id, SignerRole.valueOf(role), true)
                }
                CreateDocumentDraftUseCase(documentRepository)(ownerId, title, pdf.originalFileName, pdf.localFilePath, selected)
            }.onSuccess {
                documentId = it.id
                step = 3
                message = null
            }.onFailure { message = it.message ?: "Não foi possível criar o rascunho." }
            busy = false
        }
    }

    fun addField() {
        val id = documentId ?: return
        val signerId = selectedIds.firstOrNull() ?: return
        scope.launch {
            runCatching { AddSignatureFieldUseCase(documentRepository)(id, signerId, 0, .08f, .78f, .36f, .12f) }
                .onSuccess { fields = fields + it; message = "Campo adicionado à página 1." }
                .onFailure { message = it.message ?: "Não foi possível adicionar o campo." }
        }
    }

    fun loadReview() {
        val id = documentId ?: return
        scope.launch {
            runCatching { ReviewDocumentUseCase(documentRepository)(id) }
                .onSuccess { review = it; fields = it.document.signatureFields; step = 4; message = null }
                .onFailure { message = it.message ?: "Revise os dados do documento." }
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Novo documento", style = MaterialTheme.typography.headlineLarge)
        Text("Etapa $step de 4", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        when (step) {
            1 -> DocumentFileStep(title, { title = it }, imported, busy, { picker.launch(arrayOf("application/pdf")) })
            2 -> SignerSelectionStep(signers, selectedIds, { id -> selectedIds = if (id in selectedIds) emptySet() else setOf(id) }, role, { role = it })
            3 -> FieldStep(fields, busy, ::addField)
            4 -> ReviewStep(review, busy) {
                val id = documentId ?: return@ReviewStep
                val ownerId = session?.userId ?: return@ReviewStep
                scope.launch {
                    busy = true
                    runCatching { SendDocumentUseCase(documentRepository)(id, ownerId) }
                        .onSuccess { message = "Solicitação enviada localmente."; onFinished() }
                        .onFailure { message = it.message ?: "Não foi possível enviar." }
                    busy = false
                }
            }
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { if (step == 1) onFinished() else step -= 1 }) { Text(if (step == 1) "Cancelar" else "Voltar") }
            if (step < 4) {
                Button(
                    enabled = !busy && when (step) { 1 -> title.isNotBlank() && imported != null; 2 -> selectedIds.isNotEmpty(); 3 -> fields.isNotEmpty(); else -> false },
                    onClick = { when (step) { 1 -> step = 2; 2 -> createDraftAndContinue(); 3 -> loadReview() } },
                ) { Text("Continuar") }
            }
        }
    }
}

@Composable
private fun DocumentFileStep(title: String, onTitleChange: (String) -> Unit, imported: ImportedPdf?, busy: Boolean, onPick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Documento", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(title, onTitleChange, Modifier.fillMaxWidth(), label = { Text("Título") }, singleLine = true)
        Button(onClick = onPick, enabled = !busy) { Text(if (busy) "Importando..." else "Selecionar PDF") }
        imported?.let { Text("${it.originalFileName} · ${it.pageCount} página(s)", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
private fun SignerSelectionStep(signers: List<Signer>, selected: Set<String>, onToggle: (String) -> Unit, role: String, onRole: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Participantes", style = MaterialTheme.typography.titleLarge); Text("Selecione pelo menos um signatário.") }
        items(signers, key = { it.id }) { signer ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = signer.id in selected, onCheckedChange = { onToggle(signer.id) })
                    Column { Text(signer.fullName); Text(signer.email, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        item {
            Text("Papel do participante selecionado", modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SignerRole.values().forEach { item ->
                    OutlinedButton(onClick = { onRole(item.name) }) { Text(if (role == item.name) "✓ ${item.name}" else item.name) }
                }
            }
        }
    }
}

@Composable
private fun FieldStep(fields: List<br.com.fiap.fides.domain.model.SignatureField>, busy: Boolean, onAdd: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Campos de assinatura", style = MaterialTheme.typography.titleLarge)
        Text("Os campos usam coordenadas normalizadas e permanecem dentro da página.")
        Button(onClick = onAdd, enabled = !busy) { Text("Adicionar campo na página 1") }
        fields.forEach { field -> Text("Campo ${field.id.take(8)} · página ${field.pageIndex + 1} · ${field.width} × ${field.height}") }
    }
}

@Composable
private fun ReviewStep(review: DocumentReview?, busy: Boolean, onSend: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Revisão", style = MaterialTheme.typography.titleLarge)
        review?.let {
            Text(it.document.title, style = MaterialTheme.typography.titleMedium)
            Text("${it.requiredSignerCount} signatário(s) obrigatório(s)")
            Text("${it.fieldCount} campo(s) de assinatura")
            Text(it.message, color = MaterialTheme.colorScheme.primary)
        }
        Button(onClick = onSend, enabled = !busy) { Text(if (busy) "Enviando..." else "Enviar solicitação") }
    }
}
