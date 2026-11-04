package br.com.fiap.fides

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.fiap.fides.core.pdf.ImportedPdf
import br.com.fiap.fides.core.pdf.PdfImportService
import br.com.fiap.fides.core.pdf.PdfPageRenderer
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.repository.LocalDocumentRepository
import br.com.fiap.fides.data.repository.LocalSignerRepository
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.model.SignerRole
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.usecase.AddSignatureFieldUseCase
import br.com.fiap.fides.domain.usecase.CreateDocumentDraftUseCase
import br.com.fiap.fides.domain.usecase.DeleteSignatureFieldUseCase
import br.com.fiap.fides.domain.usecase.DocumentReview
import br.com.fiap.fides.domain.usecase.DocumentValidationException
import br.com.fiap.fides.domain.usecase.MoveSignatureFieldUseCase
import br.com.fiap.fides.domain.usecase.ReviewDocumentUseCase
import br.com.fiap.fides.domain.usecase.SendDocumentUseCase
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import kotlin.math.roundToInt

@Composable
internal fun DocumentFlowScreen(
    database: FidesDatabase,
    sessionRepository: SessionRepository,
    existingDocumentId: String? = null,
    onFinished: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val session by sessionRepository.session.collectAsState(initial = null)
    val documentRepository = remember(database) { LocalDocumentRepository(database) }
    val signerRepository = remember(database) { LocalSignerRepository(database.signerDao(), database.userDao()) }
    val importService = remember(context) { PdfImportService(context) }
    val pageRenderer = remember { PdfPageRenderer() }
    var step by rememberSaveable { mutableStateOf(1) }
    var title by rememberSaveable { mutableStateOf("") }
    var imported by remember { mutableStateOf<ImportedPdf?>(null) }
    var signers by remember { mutableStateOf(emptyList<Signer>()) }
    var selectedIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var selectedRoles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var fieldSignerId by rememberSaveable { mutableStateOf<String?>(null) }
    var documentId by rememberSaveable { mutableStateOf<String?>(null) }
    var fields by remember { mutableStateOf(emptyList<br.com.fiap.fides.domain.model.SignatureField>()) }
    var selectedPageIndex by rememberSaveable { mutableStateOf(0) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var review by remember { mutableStateOf<DocumentReview?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var editingDocument by remember { mutableStateOf<Document?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) {
            message = "Seleção de arquivo cancelada."
            return@rememberLauncherForActivityResult
        }
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

    LaunchedEffect(existingDocumentId) {
        signers = signerRepository.getAll()
        val existing = existingDocumentId?.let { documentRepository.getById(it) } ?: return@LaunchedEffect
        val pageCount = runCatching { pageRenderer.pageCount(existing.localFilePath) }.getOrDefault(1)
        editingDocument = existing
        title = existing.title
        imported = ImportedPdf(
            documentId = existing.id,
            originalFileName = existing.originalFileName,
            localFilePath = existing.localFilePath,
            pageCount = pageCount,
            sizeBytes = File(existing.localFilePath).length(),
        )
        selectedIds = existing.signers.map { it.signerId }.toSet()
        selectedRoles = existing.signers.associate { it.signerId to it.role.name }
        fieldSignerId = existing.signers.firstOrNull()?.signerId
        documentId = existing.id
        fields = existing.signatureFields
        message = "Editando a versão ${existing.version}."
    }

    LaunchedEffect(imported?.localFilePath, selectedPageIndex) {
        val filePath = imported?.localFilePath ?: return@LaunchedEffect
        pageBitmap = runCatching { pageRenderer.renderPage(filePath, selectedPageIndex) }.getOrNull()
    }

    fun createDraftAndContinue() {
        val ownerId = session?.userId ?: return
        val pdf = imported ?: return
        scope.launch {
            busy = true
            runCatching {
                val selected = signers.filter { it.id in selectedIds }.map {
                    DocumentSigner(
                        documentId = pdf.documentId,
                        signerId = it.id,
                        role = SignerRole.valueOf(selectedRoles[it.id] ?: SignerRole.CONTRACTOR.name),
                        required = true,
                    )
                }
                val current = editingDocument
                if (current == null) {
                    CreateDocumentDraftUseCase(documentRepository)(ownerId, title, pdf.originalFileName, pdf.localFilePath, selected)
                } else {
                    val selectedSignerIds = selected.map { it.signerId }.toSet()
                    current.copy(
                        title = title.trim(),
                        originalFileName = pdf.originalFileName,
                        localFilePath = pdf.localFilePath,
                        updatedAt = Instant.now(),
                        signers = selected.map { it.copy(documentId = current.id) },
                        signatureFields = fields.filter { it.signerId in selectedSignerIds },
                    ).also { documentRepository.save(it) }
                }
            }.onSuccess {
                documentId = it.id
                step = 3
                message = null
            }.onFailure { message = it.message ?: "Não foi possível criar o rascunho." }
            busy = false
        }
    }

    fun addFieldAt(x: Float, y: Float) {
        val id = documentId ?: return
        val signerId = fieldSignerId ?: selectedIds.firstOrNull() ?: return
        scope.launch {
            busy = true
            val width = .36f
            val height = .12f
            val normalizedX = x.coerceIn(0f, 1f - width)
            val normalizedY = y.coerceIn(0f, 1f - height)
            runCatching { AddSignatureFieldUseCase(documentRepository)(id, signerId, selectedPageIndex, normalizedX, normalizedY, width, height) }
                .onSuccess { newField ->
                    val updatedFields = fields + newField
                    fields = updatedFields
                    fieldSignerId = signers.firstOrNull { signer ->
                        signer.id in selectedIds && updatedFields.none { field -> field.signerId == signer.id }
                    }?.id ?: fieldSignerId
                    message = "Campo de ${signers.firstOrNull { it.id == signerId }?.fullName ?: "assinatura"} adicionado à página ${selectedPageIndex + 1}."
                }
                .onFailure { message = it.message ?: "Não foi possível adicionar o campo." }
            busy = false
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

    fun moveField(field: br.com.fiap.fides.domain.model.SignatureField, x: Float, y: Float) {
        val id = documentId ?: return
        scope.launch {
            busy = true
            runCatching { MoveSignatureFieldUseCase(documentRepository)(id, field.id, x, y, field.width, field.height) }
                .onSuccess { fields = it.signatureFields; message = null }
                .onFailure { message = it.message ?: "Não foi possível mover o campo." }
            busy = false
        }
    }

    fun deleteField(fieldId: String) {
        val id = documentId ?: return
        scope.launch {
            busy = true
            runCatching { DeleteSignatureFieldUseCase(documentRepository)(id, fieldId) }
                .onSuccess { fields = fields.filterNot { it.id == fieldId }; message = "Campo excluído." }
                .onFailure { message = it.message ?: "Não foi possível excluir o campo." }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Novo documento", style = MaterialTheme.typography.headlineLarge)
        Text("Etapa $step de 4", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            when (step) {
                1 -> DocumentFileStep(
                    title = title,
                    onTitleChange = { title = it },
                    imported = imported,
                    busy = busy,
                    onPick = { picker.launch(arrayOf("application/pdf")) },
                    onClear = {
                        imported = null
                        title = ""
                        selectedPageIndex = 0
                        pageBitmap = null
                        message = "Arquivo removido. Selecione outro PDF."
                    },
                )
                2 -> SignerSelectionStep(
                    signers = signers,
                    selected = selectedIds,
                    onToggle = { id ->
                        message = null
                        if (id in selectedIds) {
                            selectedIds = selectedIds - id
                            selectedRoles = selectedRoles - id
                            if (fieldSignerId == id) fieldSignerId = selectedIds.firstOrNull()
                        } else {
                            selectedIds = selectedIds + id
                            selectedRoles = selectedRoles + (id to SignerRole.CONTRACTOR.name)
                            if (fieldSignerId == null) fieldSignerId = id
                        }
                    },
                    roles = selectedRoles,
                    onRole = { signerId, selectedRole ->
                        message = null
                        selectedRoles = selectedRoles + (signerId to selectedRole)
                    },
                )
                3 -> FieldStep(
                    bitmap = pageBitmap,
                    pageIndex = selectedPageIndex,
                    pageCount = imported?.pageCount ?: 1,
                    fields = fields,
                    signers = signers.filter { it.id in selectedIds },
                    signerRoles = selectedRoles,
                    selectedSignerId = fieldSignerId,
                    onSelectSigner = { fieldSignerId = it },
                    busy = busy,
                    onPageChange = { selectedPageIndex = it },
                    onAddAt = ::addFieldAt,
                    onMove = ::moveField,
                    onDelete = ::deleteField,
                )
                4 -> ReviewStep(
                    review = review,
                    participantNames = signers.associate { it.id to it.fullName },
                    bitmap = pageBitmap,
                    pageIndex = selectedPageIndex,
                    pageCount = imported?.pageCount ?: 1,
                    fields = fields,
                    onPageChange = { selectedPageIndex = it },
                    busy = busy,
                ) {
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
            Spacer(Modifier.height(16.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { if (step == 1) onFinished() else step -= 1 },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
            ) { Text(if (step == 1) "Cancelar" else "Voltar") }
            if (step < 4) {
                Button(
                    enabled = !busy && when (step) {
                        1 -> title.isNotBlank() && imported != null
                        2 -> selectedIds.isNotEmpty()
                        3 -> selectedIds.isNotEmpty() && selectedIds.all { signerId -> fields.any { it.signerId == signerId } }
                        else -> false
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    onClick = {
                        when (step) {
                            1 -> {
                                message = null
                                step = 2
                            }
                            2 -> createDraftAndContinue()
                            3 -> loadReview()
                        }
                    },
                ) { Text(if (step == 3) "Confirmar configuração" else "Continuar") }
            }
        }
    }
}

@Composable
private fun DocumentFileStep(
    title: String,
    onTitleChange: (String) -> Unit,
    imported: ImportedPdf?,
    busy: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Documento", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(title, onTitleChange, Modifier.fillMaxWidth(), label = { Text("Título") }, singleLine = true)
        Text(
            "No seletor do Android, use o botão Voltar do dispositivo para cancelar.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = onPick,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        ) { Text(if (busy) "Importando..." else if (imported == null) "Selecionar PDF" else "Trocar PDF") }
        imported?.let {
            Text("${it.originalFileName} · ${it.pageCount} página(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = onClear,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            ) { Text("Remover arquivo selecionado") }
        }
    }
}

@Composable
private fun SignerSelectionStep(
    signers: List<Signer>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    roles: Map<String, String>,
    onRole: (String, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Participantes", style = MaterialTheme.typography.titleLarge)
        Text("Selecione pelo menos um signatário.")
        signers.forEach { signer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = signer.id in selected, onCheckedChange = { onToggle(signer.id) })
                        Column { Text(signer.fullName); Text(signer.email, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    if (signer.id in selected) {
                        Text("Papel no documento", style = MaterialTheme.typography.labelLarge)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SignerRole.entries.forEach { item ->
                                OutlinedButton(
                                    onClick = { onRole(signer.id, item.name) },
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(if (roles[signer.id] == item.name) "✓ ${item.label()}" else item.label())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun SignerRole.label(): String = when (this) {
    SignerRole.CONTRACTOR -> "Contratante"
    SignerRole.CONTRACTED -> "Contratado"
    SignerRole.WITNESS -> "Testemunha"
}

@Composable
private fun FieldStep(
    bitmap: Bitmap?,
    pageIndex: Int,
    pageCount: Int,
    fields: List<br.com.fiap.fides.domain.model.SignatureField>,
    signers: List<Signer>,
    signerRoles: Map<String, String>,
    selectedSignerId: String?,
    onSelectSigner: (String) -> Unit,
    busy: Boolean,
    onPageChange: (Int) -> Unit,
    onAddAt: (Float, Float) -> Unit,
    onMove: (br.com.fiap.fides.domain.model.SignatureField, Float, Float) -> Unit,
    onDelete: (String) -> Unit,
) {
    var pageSize by remember { mutableStateOf(IntSize.Zero) }
    var activeFieldId by remember { mutableStateOf<String?>(null) }
    var activeX by remember { mutableStateOf(0f) }
    var activeY by remember { mutableStateOf(0f) }
    var didDrag by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Campos de assinatura", style = MaterialTheme.typography.titleLarge)
        Text("Toque no PDF para posicionar um campo. O retângulo permanece dentro da página.")
        Text("Campo para", style = MaterialTheme.typography.labelLarge)
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            signers.forEach { signer ->
                val role = runCatching {
                    SignerRole.valueOf(signerRoles[signer.id] ?: SignerRole.CONTRACTOR.name)
                }.getOrDefault(SignerRole.CONTRACTOR)
                OutlinedButton(
                    onClick = { onSelectSigner(signer.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "${if (selectedSignerId == signer.id) "✓ " else ""}${signer.fullName}\n${role.label()}",
                        maxLines = 2,
                    )
                    Text(signer.email, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
        }
        val missingSigners = signers.filter { signer -> fields.none { it.signerId == signer.id } }
        if (missingSigners.isNotEmpty()) {
            Text(
                text = "Falta posicionar assinatura para: ${missingSigners.joinToString { it.fullName }}.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Página ${pageIndex + 1} de ${pageCount.coerceAtLeast(1)}",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onPageChange((pageIndex - 1).coerceAtLeast(0)) },
                        enabled = pageIndex > 0,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                    ) { Text("Anterior") }
                    OutlinedButton(
                        onClick = { onPageChange((pageIndex + 1).coerceAtMost((pageCount - 1).coerceAtLeast(0))) },
                        enabled = pageIndex < pageCount - 1,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.small,
                    ) { Text("Próxima") }
                }
            }
        }
        OutlinedButton(
            onClick = { fields.lastOrNull()?.let { onDelete(it.id) } },
            enabled = !busy && fields.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        ) { Text("Excluir última assinatura") }
        bitmap?.let { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(page.width.toFloat() / page.height.toFloat())
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onSizeChanged { pageSize = it }
                    .pointerInput(pageIndex, page, fields, busy) {
                        detectTapGestures { offset ->
                            if (busy || pageSize.width <= 0 || pageSize.height <= 0) return@detectTapGestures
                            val normalizedX = offset.x / pageSize.width
                            val normalizedY = offset.y / pageSize.height
                            val touchedExistingField = fields.any { field ->
                                field.pageIndex == pageIndex &&
                                    normalizedX in field.x..(field.x + field.width) &&
                                    normalizedY in field.y..(field.y + field.height)
                            }
                            if (!touchedExistingField) onAddAt(normalizedX, normalizedY)
                        }
                    }
                    .pointerInput(pageIndex, page, fields, busy) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (busy || pageSize.width <= 0 || pageSize.height <= 0) return@detectDragGestures
                                val normalizedX = offset.x / pageSize.width
                                val normalizedY = offset.y / pageSize.height
                                val hit = fields.lastOrNull { field ->
                                    field.pageIndex == pageIndex &&
                                        normalizedX in field.x..(field.x + field.width) &&
                                        normalizedY in field.y..(field.y + field.height)
                                }
                                activeFieldId = hit?.id
                                activeX = hit?.x ?: normalizedX
                                activeY = hit?.y ?: normalizedY
                                didDrag = false
                            },
                            onDrag = { change, amount ->
                                if (activeFieldId != null && pageSize.width > 0 && pageSize.height > 0) {
                                    change.consume()
                                    val field = fields.firstOrNull { it.id == activeFieldId } ?: return@detectDragGestures
                                    activeX = (activeX + amount.x / pageSize.width).coerceIn(0f, 1f - field.width)
                                    activeY = (activeY + amount.y / pageSize.height).coerceIn(0f, 1f - field.height)
                                    didDrag = true
                                }
                            },
                            onDragEnd = {
                                val field = fields.firstOrNull { it.id == activeFieldId }
                                if (field != null && didDrag) {
                                    onMove(field, activeX, activeY)
                                }
                                activeFieldId = null
                                didDrag = false
                            },
                            onDragCancel = {
                                activeFieldId = null
                                didDrag = false
                            },
                        )
                    },
            ) {
                Image(page.asImageBitmap(), contentDescription = "Página ${pageIndex + 1} do PDF", modifier = Modifier.fillMaxSize())
                SignatureFieldOverlays(
                    fields = fields,
                    pageIndex = pageIndex,
                    pageSize = pageSize,
                    labels = signers.associate { signer ->
                        val role = runCatching {
                            SignerRole.valueOf(signerRoles[signer.id] ?: SignerRole.CONTRACTOR.name)
                        }.getOrDefault(SignerRole.CONTRACTOR)
                        signer.id to "${signer.fullName} — ${role.label()}"
                    },
                )
            }
        } ?: Text("Carregando página do PDF...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = { onAddAt(.08f, .78f) }, enabled = !busy, shape = MaterialTheme.shapes.small) { Text("Adicionar no centro inferior") }
        fields.forEach { field ->
            val signer = signers.firstOrNull { it.id == field.signerId }
            val role = runCatching {
                SignerRole.valueOf(signerRoles[field.signerId] ?: SignerRole.CONTRACTOR.name)
            }.getOrDefault(SignerRole.CONTRACTOR)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${signer?.fullName ?: field.signerId} · ${role.label()} · página ${field.pageIndex + 1}")
                OutlinedButton(onClick = { onDelete(field.id) }, enabled = !busy, shape = MaterialTheme.shapes.small) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun SignatureFieldOverlays(
    fields: List<br.com.fiap.fides.domain.model.SignatureField>,
    pageIndex: Int,
    pageSize: IntSize,
    labels: Map<String, String>,
) {
    if (pageSize.width <= 0 || pageSize.height <= 0) return
    val density = LocalDensity.current
    fields.filter { it.pageIndex == pageIndex }.forEach { field ->
        val fieldWidth = with(density) { (field.width * pageSize.width).toDp() }
        val fieldHeight = with(density) { (field.height * pageSize.height).toDp() }
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (field.x * pageSize.width).roundToInt(),
                        y = (field.y * pageSize.height).roundToInt(),
                    )
                }
                .width(fieldWidth)
                .height(fieldHeight)
                .background(Color(0xE6DDD2F5))
                .border(2.dp, Color(0xFF3F05BE))
                .padding(4.dp),
        ) {
            Text(
                text = labels[field.signerId] ?: "Signatário não encontrado",
                color = Color(0xFF321087),
                fontSize = 9.sp,
                lineHeight = 10.sp,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun ReviewStep(
    review: DocumentReview?,
    participantNames: Map<String, String>,
    bitmap: Bitmap?,
    pageIndex: Int,
    pageCount: Int,
    fields: List<br.com.fiap.fides.domain.model.SignatureField>,
    onPageChange: (Int) -> Unit,
    busy: Boolean,
    onSend: () -> Unit,
) {
    var pageSize by remember { mutableStateOf(IntSize.Zero) }
    val labels = review?.document?.signers?.associate { participant ->
        val name = participantNames[participant.signerId] ?: participant.signerId
        participant.signerId to "$name — ${participant.role.label()}"
    }.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Revisão", style = MaterialTheme.typography.titleLarge)
        review?.let {
            Text(it.document.title, style = MaterialTheme.typography.titleMedium)
            Text("${it.requiredSignerCount} signatário(s) obrigatório(s)")
            Text("${it.fieldCount} campo(s) de assinatura")
            it.document.signers.forEach { participant ->
                val name = participantNames[participant.signerId] ?: participant.signerId
                val fieldCount = it.document.signatureFields.count { field -> field.signerId == participant.signerId }
                Text("$name · ${participant.role.label()} · $fieldCount campo(s)")
            }
            Text(it.message, color = MaterialTheme.colorScheme.primary)
        }
        bitmap?.let { page ->
            Text("Página ${pageIndex + 1} de $pageCount", style = MaterialTheme.typography.labelLarge)
            Box(
                modifier = Modifier.fillMaxWidth()
                    .aspectRatio(page.width.toFloat() / page.height.toFloat())
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onSizeChanged { pageSize = it },
            ) {
                Image(page.asImageBitmap(), contentDescription = "Revisão da página ${pageIndex + 1}", modifier = Modifier.fillMaxSize())
                SignatureFieldOverlays(fields, pageIndex, pageSize, labels)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { onPageChange((pageIndex - 1).coerceAtLeast(0)) }, enabled = pageIndex > 0) { Text("Anterior") }
                OutlinedButton(onClick = { onPageChange((pageIndex + 1).coerceAtMost(pageCount - 1)) }, enabled = pageIndex + 1 < pageCount) { Text("Próxima") }
            }
        }
        Button(onClick = onSend, enabled = !busy, shape = MaterialTheme.shapes.small) { Text(if (busy) "Enviando..." else "Enviar solicitação") }
    }
}
