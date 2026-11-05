package br.com.fiap.fides

import android.util.Patterns
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.repository.SignerRepository
import kotlinx.coroutines.launch
import java.util.UUID
import java.security.SecureRandom

@Composable
internal fun PersistentSignersScreen(canManage: Boolean, repository: SignerRepository) {
    if (!canManage) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FidesHeader()
            Text("Acesso restrito", style = MaterialTheme.typography.headlineLarge)
            Text("A gestão de signatários está disponível somente para administradores.")
        }
        return
    }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var signers by remember { mutableStateOf(emptyList<Signer>()) }
    var search by rememberSaveable { mutableStateOf("") }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var inviteSigner by remember { mutableStateOf<Signer?>(null) }
    var invitePassword by remember { mutableStateOf("") }
    var selectedSignerId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<Signer?>(null) }

    fun reload() {
        scope.launch {
            runCatching { repository.getAll() }
                .onSuccess {
                    signers = it
                    feedback = null
                }
                .onFailure { feedback = "Não foi possível carregar os signatários." }
        }
    }

    fun clearForm() {
        showForm = false
        editingId = null
        name = ""
        email = ""
        phone = ""
        error = null
    }

    LaunchedEffect(Unit) { reload() }

    val filtered = signers.filter {
        search.isBlank() || it.fullName.contains(search, true) || it.email.contains(search, true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FidesHeader()
            Spacer(Modifier.size(18.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Signatários", style = MaterialTheme.typography.headlineLarge)
                    Text("${signers.size} pessoas cadastradas", color = MaterialTheme.colorScheme.tertiary)
                }
                Button(onClick = { if (showForm) clearForm() else showForm = true }, shape = MaterialTheme.shapes.small) {
                    Text(if (showForm) "Cancelar" else "Adicionar")
                }
            }
            feedback?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        inviteSigner?.let { signer ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Acesso criado", style = MaterialTheme.typography.titleMedium)
                        Text("Compartilhe as instruções com ${signer.fullName}.")
                        Text("A senha temporária será exibida somente nesta mensagem de compartilhamento.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { shareSignerAccess(context, signer, invitePassword) },
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text("Compartilhar acesso")
                            }
                            TextButton(onClick = {
                                inviteSigner = null
                                invitePassword = ""
                            }) {
                                Text("Fechar")
                            }
                        }
                    }
                }
            }
        }
        if (showForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(name, { name = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Nome completo") })
                        OutlinedTextField(email, { email = it; error = null }, Modifier.fillMaxWidth(), label = { Text("E-mail") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email))
                        OutlinedTextField(phone, { phone = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Telefone") })
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(onClick = {
                            val current = signers.firstOrNull { it.id == editingId }
                            error = when {
                                name.isBlank() -> "Informe o nome completo."
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Informe um e-mail válido."
                                phone.isBlank() -> "Informe o telefone."
                                signers.any { it.email.equals(email.trim(), true) && it.id != editingId } -> "Este e-mail já está cadastrado."
                                else -> null
                            }
                            if (error == null) {
                                val generatedPassword = if (editingId == null) generateTemporaryPassword() else ""
                                val signer = Signer(
                                    id = editingId ?: UUID.randomUUID().toString(),
                                    fullName = name.trim(),
                                    email = email.trim().lowercase(),
                                    active = current?.active ?: true,
                                    phone = phone.trim(),
                                    passwordHash = if (editingId == null) {
                                        PasswordHasher().hash(generatedPassword.toCharArray())
                                    } else {
                                        current?.passwordHash.orEmpty()
                                    },
                                )
                                scope.launch {
                                    runCatching { repository.save(signer) }
                                        .onSuccess {
                                            if (editingId == null) {
                                                inviteSigner = signer
                                                invitePassword = generatedPassword
                                                feedback = "Signatário cadastrado. Compartilhe as instruções de acesso."
                                            }
                                            clearForm()
                                            reload()
                                        }
                                        .onFailure { error = "Não foi possível salvar o signatário." }
                                }
                            }
                        }, shape = MaterialTheme.shapes.small) { Text("Salvar signatário") }
                    }
                }
            }
        }
        item {
            OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Pesquisar por nome ou e-mail") })
        }
        items(filtered, key = { it.id }) { signer ->
            PersistentSignerCard(
                signer = signer,
                selected = selectedSignerId == signer.id,
                onSelect = { selectedSignerId = signer.id },
                onShare = {
                    if (signer.accountRole != br.com.fiap.fides.domain.model.UserRole.ADMIN) {
                        val temporaryPassword = generateTemporaryPassword()
                        val updatedSigner = signer.copy(
                            passwordHash = PasswordHasher().hash(temporaryPassword.toCharArray()),
                        )
                        scope.launch {
                            runCatching { repository.save(updatedSigner) }
                                .onSuccess {
                                    shareSignerAccess(context, updatedSigner, temporaryPassword)
                                    feedback = "Nova senha temporária preparada para compartilhamento."
                                }
                                .onFailure { feedback = "Não foi possível preparar o acesso." }
                        }
                    }
                },
                onEdit = {
                    if (signer.accountRole != br.com.fiap.fides.domain.model.UserRole.ADMIN) {
                    editingId = signer.id
                    name = signer.fullName
                    email = signer.email
                    phone = signer.phone
                    error = null
                    showForm = true
                    }
                },
                onDelete = { if (signer.accountRole != br.com.fiap.fides.domain.model.UserRole.ADMIN) pendingDelete = signer },
            )
        }
    }

    pendingDelete?.let { signer ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Excluir signatário?") },
            text = { Text("${signer.fullName} não poderá ser usado em novos documentos.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    scope.launch {
                        runCatching { repository.delete(signer.id) }
                            .onSuccess { reload() }
                            .onFailure { feedback = "Não foi possível excluir o signatário." }
                    }
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun PersistentSignerCard(
    signer: Signer,
    selected: Boolean,
    onSelect: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onSelect), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(signer.fullName.take(2).uppercase(), Modifier.size(42.dp), color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(signer.fullName, style = MaterialTheme.typography.titleMedium)
                Text(signer.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (signer.phone.isBlank()) "Telefone não informado" else signer.phone, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (signer.accountRole == br.com.fiap.fides.domain.model.UserRole.ADMIN) {
                    Text("Administrador elegível para assinatura", color = MaterialTheme.colorScheme.primary)
                }
                if (selected) {
                    Text("Selecionado", color = MaterialTheme.colorScheme.tertiary)
                }
                if (signer.accountRole != br.com.fiap.fides.domain.model.UserRole.ADMIN) {
                    Column {
                        TextButton(onClick = onShare) { Text("Compartilhar acesso") }
                        Row {
                            TextButton(onClick = onEdit) { Text("Editar") }
                            TextButton(onClick = onDelete) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }
}

private fun generateTemporaryPassword(): String {
    val bytes = ByteArray(12)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun shareSignerAccess(context: Context, signer: Signer, temporaryPassword: String) {
    val message = "Fides\n\nOlá, ${signer.fullName}. Você recebeu uma solicitação de assinatura.\n\nAcesse o aplicativo Fides com:\nE-mail: ${signer.email}\nSenha temporária: $temporaryPassword\n\nAltere a senha após o primeiro acesso."
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Convite para assinar no Fides")
                putExtra(Intent.EXTRA_TEXT, message)
            },
            "Compartilhar acesso",
        ),
    )
}
