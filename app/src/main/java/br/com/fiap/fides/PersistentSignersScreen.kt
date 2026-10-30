package br.com.fiap.fides

import android.util.Patterns
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.repository.SignerRepository
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
internal fun PersistentSignersScreen(canManage: Boolean, repository: SignerRepository) {
    if (!canManage) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Acesso restrito", style = MaterialTheme.typography.headlineLarge)
            Text("A gestao de signatarios esta disponivel somente para Administradores.")
        }
        return
    }

    val scope = rememberCoroutineScope()
    var signers by remember { mutableStateOf(emptyList<Signer>()) }
    var search by rememberSaveable { mutableStateOf("") }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSignerId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<Signer?>(null) }

    fun reload() {
        scope.launch { signers = runCatching { repository.getAll() }.getOrDefault(emptyList()) }
    }

    fun clearForm() {
        showForm = false
        editingId = null
        name = ""
        email = ""
        phone = ""
        password = ""
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
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Signatarios", style = MaterialTheme.typography.headlineLarge)
                    Text("${signers.size} pessoas cadastradas", color = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = { if (showForm) clearForm() else showForm = true }, shape = MaterialTheme.shapes.small) {
                    Text(if (showForm) "Cancelar" else "Adicionar")
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
                        OutlinedTextField(password, { password = it; error = null }, Modifier.fillMaxWidth(), label = { Text(if (editingId == null) "Senha" else "Nova senha (opcional)") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password))
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(onClick = {
                            val current = signers.firstOrNull { it.id == editingId }
                            error = when {
                                name.isBlank() -> "Informe o nome completo."
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Informe um e-mail valido."
                                phone.isBlank() -> "Informe o telefone."
                                editingId == null && password.length < 6 -> "A senha deve ter pelo menos 6 caracteres."
                                signers.any { it.email.equals(email.trim(), true) && it.id != editingId } -> "Este e-mail ja esta cadastrado."
                                else -> null
                            }
                            if (error == null) {
                                val signer = Signer(
                                    id = editingId ?: UUID.randomUUID().toString(),
                                    fullName = name.trim(),
                                    email = email.trim().lowercase(),
                                    active = current?.active ?: true,
                                    phone = phone.trim(),
                                    passwordHash = if (password.isBlank()) current?.passwordHash.orEmpty() else PasswordHasher().hash(password.toCharArray()),
                                )
                                scope.launch {
                                    runCatching { repository.save(signer) }
                                        .onSuccess { reload(); clearForm() }
                                        .onFailure { error = "Nao foi possivel salvar o signatario." }
                                }
                            }
                        }, shape = MaterialTheme.shapes.small) { Text("Salvar signatario") }
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
                onEdit = {
                    editingId = signer.id
                    name = signer.fullName
                    email = signer.email
                    phone = signer.phone
                    password = ""
                    error = null
                    showForm = true
                },
                onDelete = { pendingDelete = signer },
            )
        }
    }

    pendingDelete?.let { signer ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Excluir signatario?") },
            text = { Text("${signer.fullName} nao podera ser usado em novos documentos.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    scope.launch { runCatching { repository.delete(signer.id) }; reload() }
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
                Text(signer.phone, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (selected) {
                    Text("Selecionado", color = MaterialTheme.colorScheme.primary)
                }
                Row {
                    TextButton(onClick = onEdit) { Text("Editar") }
                    TextButton(onClick = onDelete) { Text("Excluir") }
                }
            }
        }
    }
}
