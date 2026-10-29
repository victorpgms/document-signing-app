package br.com.fiap.fides

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.fiap.fides.core.ui.theme.FidesTheme
import br.com.fiap.fides.data.local.DemoUserSeeder
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.local.FidesDatabaseProvider
import br.com.fiap.fides.data.local.SessionStore
import br.com.fiap.fides.data.repository.LocalAuthRepository
import br.com.fiap.fides.domain.repository.AuthenticatedUser
import br.com.fiap.fides.domain.repository.AuthRepository
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.model.UserRole

private enum class FidesRoute(val route: String, val label: String) {
    LOGIN("login", "Login"),
    DOCUMENTS("documents", "Documentos"),
    SIGNER_DASHBOARD("signer-dashboard", "Pendências"),
    SIGNERS("signers", "Signatários"),
    PROFILE("profile", "Perfil"),
}

private data class DocumentPreview(
    val title: String,
    val metadata: String,
    val progress: Float,
    val progressLabel: String,
    val status: String,
)

private val previewDocuments = listOf(
    DocumentPreview("Contrato de prestação de serviço", "22 out. 2026  •  PDF (1,8 MB)", 0.66f, "2 de 3 assinadas", "Aguardando"),
    DocumentPreview("Acordo de Confidencialidade", "18 out. 2026  •  PDF (620 KB)", 0.5f, "1 de 2 assinadas", "Parcialmente assinado"),
    DocumentPreview("Proposta Comercial", "15 out. 2026  •  PDF (2,4 MB)", 1f, "2 de 2 assinadas (100%)", "Concluído"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FidesApp(
                database = FidesDatabaseProvider.get(applicationContext),
                sessionRepository = SessionStore(applicationContext),
            )
        }
    }
}

@Composable
private fun FidesApp(database: FidesDatabase, sessionRepository: SessionRepository) {
    val navController = rememberNavController()
    val authRepository = remember { LocalAuthRepository(database.userDao()) }
    var accountReady by remember { mutableStateOf(false) }
    var currentRole by remember { mutableStateOf<UserRole?>(null) }
    LaunchedEffect(database) {
        DemoUserSeeder.ensure(database)
        accountReady = true
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.route in setOf(
        FidesRoute.DOCUMENTS.route,
        FidesRoute.SIGNERS.route,
        FidesRoute.PROFILE.route,
    )

    FidesTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        FidesRoute.entries.filter { it != FidesRoute.LOGIN }.forEach { item ->
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Text(item.label.first().toString()) },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
                    }
                }
            },
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = FidesRoute.LOGIN.route,
                modifier = Modifier.padding(paddingValues),
            ) {
                composable(FidesRoute.LOGIN.route) {
                    LoginScreen(
                        authRepository = authRepository,
                        enabled = accountReady,
                        onLoginSuccess = {
                            runCatching { sessionRepository.save(it) }
                            currentRole = it.role
                            val destination = if (it.role == UserRole.SIGNER) {
                                FidesRoute.SIGNER_DASHBOARD.route
                            } else {
                                FidesRoute.DOCUMENTS.route
                            }
                            navController.navigate(destination) {
                                popUpTo(FidesRoute.LOGIN.route) { inclusive = true }
                            }
                        },
                    )
                }
                composable(FidesRoute.DOCUMENTS.route) { DocumentsScreen() }
                composable(FidesRoute.SIGNER_DASHBOARD.route) { SignerDashboardScreen() }
                composable(FidesRoute.SIGNERS.route) { SignersScreen(canManage = currentRole == UserRole.ADMIN) }
                composable(FidesRoute.PROFILE.route) { ProfileScreen() }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    authRepository: AuthRepository,
    enabled: Boolean,
    onLoginSuccess: suspend (AuthenticatedUser) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCredentials by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(pendingCredentials) {
        val credentials = pendingCredentials ?: return@LaunchedEffect
        val authenticatedUser = authRepository.authenticate(
            credentials.first,
            credentials.second.toCharArray(),
        )
        pendingCredentials = null
        if (authenticatedUser == null) {
            error = "E-mail ou senha inválidos."
        } else {
            error = null
            onLoginSuccess(authenticatedUser)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FidesHeader()
        Spacer(Modifier.height(28.dp))
        Text("Bem-vindo ao Fides", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Seus documentos, assinaturas e processos em um só lugar.",
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    singleLine = true,
                    label = { Text("E-mail institucional") },
                    placeholder = { Text("victor@fides.local") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    singleLine = true,
                    label = { Text("Senha de acesso") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                Button(
                    onClick = { pendingCredentials = email to password },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = enabled && email.isNotBlank() && password.isNotBlank(),
                ) {
                    Text("Entrar  →")
                }
            }
        }
        Text(
            "Conta de demonstração: victor@fides.local / fides1234",
            modifier = Modifier.padding(top = 18.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FidesHeader() {
    val logo = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        R.drawable.fides_logo_dark
    } else {
        R.drawable.fides_logo_light
    }
    Image(
        painter = painterResource(logo),
        contentDescription = stringResource(R.string.app_name),
        modifier = Modifier.height(28.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun DocumentsScreen() {
    var search by rememberSaveable { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FidesHeader()
            Spacer(Modifier.height(18.dp))
            Text("Visão geral", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Olá, Victor", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Pesquisar documento, signatário...") },
                leadingIcon = { Text("⌕") },
            )
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = true, onClick = {}, label = { Text("Todos (8)") })
                FilterChip(selected = false, onClick = {}, label = { Text("Em andamento 2") })
                FilterChip(selected = false, onClick = {}, label = { Text("Concluídos 5") })
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("▣", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Novo documento", style = MaterialTheme.typography.titleMedium)
                        Text("Carregue PDF para coletar assinaturas", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(onClick = {}) { Text("+ Criar") }
                }
            }
        }
        item { Text("Documentos recentes", style = MaterialTheme.typography.titleMedium) }
        items(previewDocuments) { document -> DocumentCard(document) }
    }
}

@Composable
private fun DocumentCard(document: DocumentPreview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(document.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(document.metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Progresso de assinaturas", style = MaterialTheme.typography.bodySmall)
                Text(document.progressLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
                progress = { document.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
            )
            Text(document.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun SignerDashboardScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FidesHeader()
            Spacer(Modifier.height(18.dp))
            Text("Minhas assinaturas", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Documentos recebidos para sua assinatura",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Contrato de prestação de serviço", style = MaterialTheme.typography.titleLarge)
                    Text("Enviado hoje • 2 campos pendentes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = {}) { Text("Abrir documento") }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Documentos assinados", style = MaterialTheme.typography.titleMedium)
                    Text("Você ainda não concluiu este fluxo nesta demonstração.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SignersScreen(canManage: Boolean) {
    if (!canManage) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FidesHeader()
            Text("Acesso restrito", style = MaterialTheme.typography.headlineLarge)
            Text(
                "A gestão de signatários está disponível somente para usuários Administradores.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    var showForm by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSigner by rememberSaveable { mutableStateOf<String?>(null) }
    val addedSigners = remember { mutableStateListOf<Pair<String, String>>() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FidesHeader()
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Signatários", style = MaterialTheme.typography.headlineLarge)
                    Text("● 3 pessoas cadastradas", color = MaterialTheme.colorScheme.primary)
                }
                if (canManage) {
                    Button(onClick = { showForm = !showForm }) { Text(if (showForm) "Cancelar" else "Adicionar") }
                }
            }
            if (showForm && canManage) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; formError = null },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    singleLine = true,
                    label = { Text("Nome completo") },
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; formError = null },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    singleLine = true,
                    label = { Text("E-mail") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                formError?.let { message -> Text(message, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        when {
                            name.isBlank() -> formError = "Informe o nome completo."
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> formError = "Informe um e-mail válido."
                            else -> {
                                addedSigners.add(name.trim() to email.trim())
                                name = ""
                                email = ""
                                showForm = false
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) { Text("Salvar signatário") }
            }
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                singleLine = true,
                placeholder = { Text("Pesquisar por nome ou e-mail...") },
                leadingIcon = { Text("⌕") },
            )
            selectedSigner?.let { selected ->
                Text("Selecionado: $selected", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp))
            }
        }
        item { SignerCard("MS", "Maria Silva", "maria.silva@email.com", "(11) 98765-4321") { selectedSigner = "Maria Silva" } }
        item { SignerCard("JS", "João Santos", "joao.santos@email.com", "(21) 99123-4567") { selectedSigner = "João Santos" } }
        item { SignerCard("AC", "Ana Costa", "ana.costa@email.com", "(31) 98877-6655") { selectedSigner = "Ana Costa" } }
        items(addedSigners) { (addedName, addedEmail) ->
            SignerCard(
                initials = addedName.trim().split(" ").take(2).joinToString("") { it.first().uppercase() },
                name = addedName,
                email = addedEmail,
                phone = "Cadastro local",
            ) { selectedSigner = addedName }
        }
    }
}

@Composable
private fun SignerCard(initials: String, name: String, email: String, phone: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(initials, modifier = Modifier.size(42.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FidesHeader()
        Text("Perfil", style = MaterialTheme.typography.headlineLarge)
        Text("Preferências da conta e aparência", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = {}) { Text("Configurar aparência") }
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentsScreenPreview() {
    FidesTheme { DocumentsScreen() }
}
