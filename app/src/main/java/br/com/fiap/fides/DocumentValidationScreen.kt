package br.com.fiap.fides

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.fiap.fides.core.pdf.DocumentValidationService
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.repository.LocalDocumentRepository
import br.com.fiap.fides.data.repository.LocalSignerRepository
import br.com.fiap.fides.domain.model.Document
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun DocumentValidationScreen(
    database: FidesDatabase,
    onBack: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(database) { LocalDocumentRepository(database) }
    val signerRepository = remember(database) { LocalSignerRepository(database.signerDao(), database.userDao()) }
    val validationService = remember { DocumentValidationService(context) }
    var token by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<ValidationResult?>(null) }
    val qrLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        val scan = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, activityResult.resultCode, activityResult.data)
        val scanned = scan?.contents
        if (!scanned.isNullOrBlank()) {
            token = scanned.substringAfterLast("/")
            message = null
            scope.launch {
                loading = true
                result = runCatching { validate(repository, signerRepository, token, null) }
                    .getOrElse { error ->
                        message = error.message ?: "Não foi possível validar o QR Code."
                        null
                    }
                loading = false
            }
        }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            loading = true
            message = null
            result = runCatching {
                val candidate = File.createTempFile("fides-validation-", ".pdf", context.cacheDir)
                context.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) { "Não foi possível ler o PDF selecionado." }
                    candidate.outputStream().use { output -> input.copyTo(output) }
                }
                val data = validationService.readValidationData(candidate)
                validate(repository, signerRepository, data.token, data.sha256)
            }.getOrElse { error ->
                message = error.message ?: "Não foi possível analisar o PDF."
                null
            }
            loading = false
        }
    }

    fun validateTypedToken() {
        val normalized = token.trim().substringAfterLast("/")
        if (normalized.isBlank()) {
            message = "Informe o código de validação."
            return
        }
        scope.launch {
            loading = true
            message = null
            result = runCatching { validate(repository, signerRepository, normalized, null) }
                .getOrElse { error ->
                    message = error.message ?: "Não foi possível validar o código."
                    null
                }
            loading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            OutlinedButton(onClick = onBack, enabled = !loading) { Text("Voltar") }
            Text("Validar documento", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 14.dp))
            Text("Consulte o registro público e, quando disponível, confira a integridade do PDF completo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Validar por código ou QR Code", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it; message = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Código de validação") },
                        placeholder = { Text("Cole o token do QR Code") },
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = ::validateTypedToken, enabled = !loading, modifier = Modifier.weight(1f)) { Text("Consultar") }
                        OutlinedButton(
                            onClick = {
                                val activity = context as? Activity ?: return@OutlinedButton
                                qrLauncher.launch(
                                    IntentIntegrator(activity)
                                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                                        .setPrompt("Aponte a câmera para o QR Code do Fides")
                                        .setBeepEnabled(false)
                                        .setOrientationLocked(false)
                                        .createScanIntent(),
                                )
                            },
                            enabled = !loading,
                            modifier = Modifier.weight(1f),
                        ) { Text("Ler QR Code") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { pdfLauncher.launch("application/pdf") }, enabled = !loading) { Text("Selecionar PDF") }
                    }
                    Text("O QR Code contém apenas o token. A validação criptográfica depende do PDF completo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (loading) item { Text("Validando documento...") }
        message?.let { current -> item { Text(current, color = MaterialTheme.colorScheme.error) } }
        result?.let { validation ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(validation.headline, style = MaterialTheme.typography.titleMedium, color = if (validation.valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        validation.document?.let { document ->
                            Text(document.title, style = MaterialTheme.typography.titleLarge)
                            Text("Status: ${document.status.displayLabel()}")
                            Text("Token: ${document.validationToken.orEmpty()}", style = MaterialTheme.typography.bodySmall)
                            Text("Integridade do arquivo: ${validation.integrityLabel}")
                            Text("Signatários", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                            validation.signers.forEach { signer -> Text("${signer.first} · ${signer.second}") }
                        }
                    }
                }
            }
        }
    }
}

private data class ValidationResult(
    val valid: Boolean,
    val headline: String,
    val document: Document?,
    val signers: List<Pair<String, String>>,
    val integrityLabel: String,
)

private suspend fun validate(
    repository: LocalDocumentRepository,
    signerRepository: LocalSignerRepository,
    token: String?,
    sha256: String?,
): ValidationResult {
    if (token.isNullOrBlank()) return ValidationResult(false, "Identificador inválido ou ausente.", null, emptyList(), "Não analisada")
    val document = repository.getByValidationToken(token)
        ?: return ValidationResult(false, "Documento não encontrado para este identificador.", null, emptyList(), "Não analisada")
    val signerDetails = signerRepository.getAll().associateBy { it.id }
    val signers = document.signers.map { participant ->
        val signer = signerDetails[participant.signerId]
        val whenSigned = participant.signedAt?.atZone(ZoneId.systemDefault())?.format(
DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm z", Locale.getDefault()),
        )
        val status = if (whenSigned == null) "Pendente" else "Assinado em $whenSigned"
        val contact = listOfNotNull(
            signer?.email?.takeIf { it.isNotBlank() },
            signer?.phone?.takeIf { it.isNotBlank() },
        ).joinToString(" · ").ifBlank { "Contato não informado" }
        (signer?.fullName ?: participant.signerId) to "$contact · ${participant.role.name} · $status"
    }
    if (document.status == br.com.fiap.fides.domain.model.DocumentStatus.CANCELLED) {
        return ValidationResult(false, "Documento cancelado.", document, signers, "Não aplicável")
    }
    val integrity = when {
        sha256 == null -> "Registro encontrado; envie o PDF completo para verificar o hash."
        document.finalSha256.isNullOrBlank() -> "Hash ainda não disponível para este documento."
        sha256.equals(document.finalSha256, ignoreCase = true) -> "Íntegra (SHA-256 confere)."
        else -> "Comprometida (o arquivo foi alterado)."
    }
    val valid = sha256 == null || integrity.startsWith("Íntegra")
    val headline = when {
        !valid -> "Documento alterado ou com integridade comprometida."
        sha256 == null -> "Registro localizado. Integridade não analisada."
        else -> "Documento autenticado."
    }
    return ValidationResult(valid, headline, document, signers, integrity)
}
