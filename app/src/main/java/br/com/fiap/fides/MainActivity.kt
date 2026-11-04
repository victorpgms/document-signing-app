package br.com.fiap.fides

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.fiap.fides.core.ui.theme.FidesTheme
import br.com.fiap.fides.data.local.DemoUserSeeder
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.local.FidesDatabaseProvider
import br.com.fiap.fides.data.local.SessionStore
import br.com.fiap.fides.data.local.ThemeMode
import br.com.fiap.fides.data.local.ThemePreferenceStore
import br.com.fiap.fides.data.repository.AdminOnlySignerRepository
import br.com.fiap.fides.data.repository.LocalAuthRepository
import br.com.fiap.fides.data.repository.LocalDocumentRepository
import br.com.fiap.fides.data.repository.LocalSignerRepository
import br.com.fiap.fides.domain.repository.AuthenticatedUser
import br.com.fiap.fides.domain.repository.AuthRepository
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.repository.UserSession
import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.UserRole
import br.com.fiap.fides.domain.repository.SignerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

private enum class FidesRoute(val route: String, val label: String) {
    LOGIN("login", "Login"),
    VALIDATE("validate", "Validar documento"),
    REGISTER("register", "Criar conta"),
    DOCUMENTS("documents", "Documentos"),
    SIGNER_DASHBOARD("signer-dashboard", "Pendências"),
    SIGNERS("signers", "Signatários"),
    PROFILE("profile", "Perfil"),
    CREATE_DOCUMENT("create-document", "Novo documento"),
    ADMIN_DOCUMENT_DETAILS("admin-document-details", "Detalhes do documento"),
    SIGNER_SIGN("signer-sign", "Assinar documento"),
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FidesApp(
                database = FidesDatabaseProvider.get(applicationContext),
                sessionRepository = SessionStore(applicationContext),
                themePreferenceStore = ThemePreferenceStore(applicationContext),
            )
        }
    }
}

@Composable
private fun FidesApp(
    database: FidesDatabase,
    sessionRepository: SessionRepository,
    themePreferenceStore: ThemePreferenceStore,
) {
    val navController = rememberNavController()
    val authRepository = remember { LocalAuthRepository(database.userDao(), database.signerDao()) }
    val signerRepository = remember {
        AdminOnlySignerRepository(LocalSignerRepository(database.signerDao(), database.userDao()), sessionRepository)
    }
    var accountReady by remember { mutableStateOf(false) }
    var currentRole by remember { mutableStateOf<UserRole?>(null) }
    var selectedSignerDocumentId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedAdminDocumentId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedDraftDocumentId by rememberSaveable { mutableStateOf<String?>(null) }
    val themeMode by themePreferenceStore.mode.collectAsState(initial = ThemeMode.SYSTEM)
    val systemDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    LaunchedEffect(database) {
        withContext(Dispatchers.Default) { DemoUserSeeder.ensure(database) }
        accountReady = true
    }
    LaunchedEffect(accountReady) {
        if (!accountReady) return@LaunchedEffect
        sessionRepository.session.first()?.let { restored ->
            currentRole = restored.role
            val destination = if (restored.role == UserRole.SIGNER) {
                FidesRoute.SIGNER_DASHBOARD.route
            } else {
                FidesRoute.DOCUMENTS.route
            }
            navController.navigate(destination) {
                popUpTo(FidesRoute.LOGIN.route) { inclusive = true }
            }
        }
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val navigationItems = if (currentRole == UserRole.SIGNER) {
        listOf(FidesRoute.SIGNER_DASHBOARD, FidesRoute.PROFILE)
    } else {
        listOf(FidesRoute.DOCUMENTS, FidesRoute.SIGNER_DASHBOARD, FidesRoute.SIGNERS, FidesRoute.PROFILE)
    }
    val showBottomBar = currentDestination?.route in navigationItems.map { it.route }
    val activity = LocalActivity.current
    val homeRoute = if (currentRole == UserRole.SIGNER) {
        FidesRoute.SIGNER_DASHBOARD.route
    } else {
        FidesRoute.DOCUMENTS.route
    }

    BackHandler(enabled = currentDestination?.route != FidesRoute.LOGIN.route) {
        if (currentRole == null) {
            navController.popBackStack(FidesRoute.LOGIN.route, inclusive = false)
        } else if (currentDestination?.route == homeRoute) {
            activity?.finish()
        } else {
            selectedSignerDocumentId = null
            selectedAdminDocumentId = null
            selectedDraftDocumentId = null
            navController.navigate(homeRoute) {
                popUpTo(homeRoute) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    FidesTheme(
        darkTheme = when (themeMode) {
            ThemeMode.SYSTEM -> systemDarkTheme
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        navigationItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Box(
                                        modifier = Modifier.size(40.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            when (item) {
                                                FidesRoute.DOCUMENTS -> "▤"
                                                FidesRoute.SIGNER_DASHBOARD -> "✓"
                                                FidesRoute.SIGNERS -> "♙"
                                                FidesRoute.PROFILE -> "◉"
                                                else -> "•"
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                                        )
                                    }
                                },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.tertiary,
                                    selectedTextColor = MaterialTheme.colorScheme.tertiary,
                                    indicatorColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
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
                        onOpenRegistration = { navController.navigate(FidesRoute.REGISTER.route) },
                        onOpenValidation = { navController.navigate(FidesRoute.VALIDATE.route) },
                        onLoginSuccess = {
                            sessionRepository.save(it)
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
                composable(FidesRoute.VALIDATE.route) {
                    DocumentValidationScreen(
                        database = database,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(FidesRoute.REGISTER.route) {
                    RegistrationScreen(
                        authRepository = authRepository,
                        onBack = { navController.popBackStack() },
                        onRegistrationSuccess = {
                            sessionRepository.save(it)
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
                composable(FidesRoute.DOCUMENTS.route) {
                    AdminDocumentsScreen(
                        database = database,
                        sessionRepository = sessionRepository,
                        onCreate = {
                            selectedDraftDocumentId = null
                            navController.navigate(FidesRoute.CREATE_DOCUMENT.route)
                        },
                        onOpenDocument = { documentId ->
                            selectedAdminDocumentId = documentId
                            navController.navigate(FidesRoute.ADMIN_DOCUMENT_DETAILS.route)
                        },
                    )
                }
                composable(FidesRoute.ADMIN_DOCUMENT_DETAILS.route) {
                    val documentId = selectedAdminDocumentId
                    if (documentId == null) {
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    } else {
                        DocumentDetailsScreen(
                            database = database,
                            sessionRepository = sessionRepository,
                            documentId = documentId,
                            onBack = {
                                selectedAdminDocumentId = null
                                navController.popBackStack()
                            },
                            onEditVersion = { draftId ->
                                selectedDraftDocumentId = draftId
                                navController.navigate(FidesRoute.CREATE_DOCUMENT.route)
                            },
                        )
                    }
                }
                composable(FidesRoute.SIGNER_DASHBOARD.route) {
                    SignerDashboardScreen(
                        database = database,
                        sessionRepository = sessionRepository,
                        onOpenDocument = { documentId ->
                            selectedSignerDocumentId = documentId
                            navController.navigate(FidesRoute.SIGNER_SIGN.route)
                        },
                    )
                }
                composable(FidesRoute.CREATE_DOCUMENT.route) {
                    DocumentFlowScreen(
                        database = database,
                        sessionRepository = sessionRepository,
                        existingDocumentId = selectedDraftDocumentId,
                        onFinished = {
                            selectedDraftDocumentId = null
                            navController.navigate(FidesRoute.DOCUMENTS.route) {
                                popUpTo(FidesRoute.DOCUMENTS.route) { inclusive = true }
                            }
                        },
                    )
                }
                composable(FidesRoute.SIGNER_SIGN.route) {
                    val documentId = selectedSignerDocumentId
                    if (documentId == null) {
                        SignerDashboardScreen(
                            database = database,
                            sessionRepository = sessionRepository,
                            onOpenDocument = { selectedSignerDocumentId = it },
                        )
                    } else {
                        SignerSigningScreen(
                            database = database,
                            sessionRepository = sessionRepository,
                            documentId = documentId,
                            onFinished = {
                                selectedSignerDocumentId = null
                                navController.navigate(FidesRoute.SIGNER_DASHBOARD.route) {
                                    popUpTo(FidesRoute.SIGNER_DASHBOARD.route) { inclusive = true }
                                }
                            },
                        )
                    }
                }
                composable(FidesRoute.SIGNERS.route) {
                    PersistentSignersScreen(
                        canManage = currentRole == UserRole.ADMIN,
                        repository = signerRepository,
                    )
                }
                composable(FidesRoute.PROFILE.route) {
                    ProfileScreen(
                        authRepository = authRepository,
                        sessionRepository = sessionRepository,
                        themeMode = themeMode,
                        onThemeModeChange = { selectedMode ->
                            scope.launch { themePreferenceStore.save(selectedMode) }
                        },
                        onLogout = {
                            scope.launch {
                                sessionRepository.clear()
                                currentRole = null
                                navController.navigate(FidesRoute.LOGIN.route) {
                                    popUpTo(FidesRoute.LOGIN.route) { inclusive = true }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    authRepository: AuthRepository,
    enabled: Boolean,
    onOpenRegistration: () -> Unit,
    onOpenValidation: () -> Unit,
    onLoginSuccess: suspend (AuthenticatedUser) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var authenticating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun submitLogin() {
        if (!enabled || authenticating || email.isBlank() || password.isBlank()) return
        val submittedEmail = email
        val submittedPassword = password.toCharArray()
        focusManager.clearFocus()
        authenticating = true
        error = null
        scope.launch {
            try {
                val authenticatedUser = withContext(Dispatchers.Default) {
                    authRepository.authenticate(submittedEmail, submittedPassword)
                }
                if (authenticatedUser == null) {
                    error = "E-mail ou senha inválidos."
                } else {
                    onLoginSuccess(authenticatedUser)
                }
            } catch (exception: Exception) {
                error = exception.message ?: "Não foi possível entrar. Tente novamente."
            } finally {
                submittedPassword.fill('\u0000')
                authenticating = false
            }
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
                    enabled = enabled && !authenticating,
                    singleLine = true,
                    label = { Text("E-mail institucional") },
                    placeholder = { Text("victor@fides.local") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled && !authenticating,
                    singleLine = true,
                    label = { Text("Senha de acesso") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submitLogin() }),
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                if (!enabled) {
                    Text("Preparando contas de demonstração...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = ::submitLogin,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = enabled && !authenticating && email.isNotBlank() && password.isNotBlank(),
                ) {
                    Text(if (authenticating) "Entrando..." else "Entrar  →")
                }
                TextButton(
                    onClick = onOpenRegistration,
                    enabled = !authenticating,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Criar uma conta")
                }
                TextButton(
                    onClick = onOpenValidation,
                    enabled = !authenticating,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Validar documento sem login")
                }
            }
        }
        Text(
            "Admins: admin.victor@fides.local ou admin.lucas@fides.local / fides1234\nSignatários: ana.teste@fides.local ou bruno.teste@fides.local / fides1234",
            modifier = Modifier.padding(top = 18.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RegistrationScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onRegistrationSuccess: suspend (AuthenticatedUser) -> Unit,
) {
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(UserRole.SIGNER) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var registering by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun submitRegistration() {
        if (registering || displayName.isBlank() || email.isBlank() || password.isBlank()) return
        if (password != confirmation) {
            error = "As senhas precisam ser iguais."
            return
        }
        val submittedPassword = password.toCharArray()
        focusManager.clearFocus()
        registering = true
        error = null
        scope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    authRepository.register(displayName, email, submittedPassword, role, phone)
                }
                if (result.user == null) {
                    error = result.error ?: "Não foi possível criar a conta."
                } else {
                    onRegistrationSuccess(result.user)
                }
            } catch (exception: Exception) {
                error = exception.message ?: "Não foi possível criar a conta."
            } finally {
                submittedPassword.fill('\u0000')
                registering = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FidesHeader()
        Spacer(Modifier.height(24.dp))
        Text("Criar conta", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Cadastre um acesso local para a demonstração.",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tipo de acesso", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = role == UserRole.SIGNER,
                        onClick = { role = UserRole.SIGNER },
                        label = { Text("Signatário") },
                    )
                    FilterChip(
                        selected = role == UserRole.ADMIN,
                        onClick = { role = UserRole.ADMIN },
                        label = { Text("Administrador") },
                    )
                }
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !registering,
                    singleLine = true,
                    label = { Text("Nome completo") },
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !registering,
                    singleLine = true,
                    label = { Text("E-mail") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !registering,
                    singleLine = true,
                    label = { Text("Telefone/celular com DDD") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !registering,
                    singleLine = true,
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                )
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !registering,
                    singleLine = true,
                    label = { Text("Confirmar senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardActions = KeyboardActions(onDone = { submitRegistration() }),
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = ::submitRegistration,
                    enabled = !registering && displayName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmation.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (registering) "Criando..." else "Criar conta")
                }
                TextButton(onClick = onBack, enabled = !registering, modifier = Modifier.fillMaxWidth()) {
                    Text("Voltar para o login")
                }
            }
        }
    }
}

@Composable
internal fun FidesHeader() {
    val logo = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        R.drawable.fides_logo_dark
    } else {
        R.drawable.fides_logo_light
    }
    Image(
        painter = painterResource(logo),
        contentDescription = stringResource(R.string.app_name),
        modifier = Modifier
            .width(96.dp)
            .height(32.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun SignerDashboardScreen(
    database: FidesDatabase,
    sessionRepository: SessionRepository,
    onOpenDocument: (String) -> Unit,
) {
    val session by sessionRepository.session.collectAsState(initial = null)
    val repository = remember(database) { LocalDocumentRepository(database) }
    var documents by remember { mutableStateOf(emptyList<br.com.fiap.fides.domain.model.Document>()) }
    var refreshKey by remember { mutableStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey += 1
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(session?.userId, refreshKey) {
        val userId = session?.userId ?: return@LaunchedEffect
        documents = repository.getAllForUser(userId).filter { document ->
            document.status != DocumentStatus.DRAFT && document.signers.any { it.signerId == userId }
        }.sortedBy { document -> document.signers.firstOrNull { it.signerId == userId }?.signedAt != null }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FidesHeader()
            Spacer(Modifier.height(18.dp))
            Text("Minhas assinaturas", style = MaterialTheme.typography.headlineLarge)
            Text("Documentos recebidos para sua assinatura", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (documents.isEmpty()) {
            item {
                Text("Nenhum documento recebido.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(documents, key = { it.id }) { document ->
            val currentParticipant = document.signers.firstOrNull { it.signerId == session?.userId }
            val currentUserHasSigned = currentParticipant?.signedAt != null
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(document.title, style = MaterialTheme.typography.titleLarge)
                    Text("${document.originalFileName} · ${document.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${document.signatureFields.count { it.signerId == session?.userId && it.required }} campo(s) seu(s)")
                    if (currentUserHasSigned) {
                        Text("Sua assinatura foi concluída", color = MaterialTheme.colorScheme.primary)
                        Button(
                            onClick = { onOpenDocument(document.id) },
                            shape = MaterialTheme.shapes.small,
                        ) { Text("Abrir arquivo") }
                    } else {
                        Button(
                            onClick = { onOpenDocument(document.id) },
                            shape = MaterialTheme.shapes.small,
                        ) { Text("Abrir e assinar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val session by sessionRepository.session.collectAsState(initial = null)
    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var profileMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var profileError by rememberSaveable { mutableStateOf<String?>(null) }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }
    var savingProfile by remember { mutableStateOf(false) }
    val versionName = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull()
            ?: "1.0.0"
    }
    LaunchedEffect(session?.userId) {
        val userId = session?.userId ?: return@LaunchedEffect
        authRepository.getProfile(userId)?.let { profile ->
            displayName = profile.displayName
            email = profile.email
            phone = profile.phone
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FidesHeader()
        Text("Perfil", style = MaterialTheme.typography.headlineLarge)
        Text("Preferências da conta e aparência", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it; profileError = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome completo") },
            singleLine = true,
            enabled = !savingProfile,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; profileError = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("E-mail") },
            singleLine = true,
            enabled = !savingProfile,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; profileError = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Telefone/celular com DDD") },
            singleLine = true,
            enabled = !savingProfile,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        Button(
            onClick = {
                val userId = session?.userId ?: return@Button
                scope.launch {
                    savingProfile = true
                    profileError = null
                    val result = authRepository.updateProfile(userId, displayName, email, phone)
                    result.user?.let {
                        sessionRepository.save(it)
                        profileMessage = "Dados de contato atualizados."
                    }
                    profileError = result.error
                    savingProfile = false
                }
            },
            enabled = !savingProfile && displayName.isNotBlank() && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
        ) { Text(if (savingProfile) "Salvando..." else "Salvar dados de contato") }
        profileMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        profileError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Segurança", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it; profileError = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Senha atual") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !savingProfile,
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; profileError = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nova senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !savingProfile,
        )
        OutlinedTextField(
            value = confirmNewPassword,
            onValueChange = { confirmNewPassword = it; profileError = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirmar nova senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !savingProfile,
        )
        OutlinedButton(
            onClick = {
                val userId = session?.userId ?: return@OutlinedButton
                if (newPassword != confirmNewPassword) {
                    profileError = "As novas senhas precisam ser iguais."
                    return@OutlinedButton
                }
                scope.launch {
                    savingProfile = true
                    val error = authRepository.updatePassword(userId, currentPassword.toCharArray(), newPassword.toCharArray())
                    profileError = error
                    if (error == null) {
                        currentPassword = ""
                        newPassword = ""
                        confirmNewPassword = ""
                        profileMessage = "Senha atualizada com sucesso."
                    }
                    savingProfile = false
                }
            },
            enabled = !savingProfile && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmNewPassword.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Atualizar senha") }
        Text("Aparência", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = themeMode == mode,
                    onClick = { onThemeModeChange(mode) },
                    label = {
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> "Sistema"
                                ThemeMode.LIGHT -> "Claro"
                                ThemeMode.DARK -> "Escuro"
                            },
                        )
                    },
                )
            }
        }
        Text("Sobre", style = MaterialTheme.typography.titleMedium)
        Text("Fides $versionName · Assinatura local de documentos PDF", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onLogout, shape = MaterialTheme.shapes.small) { Text("Sair") }
    }
}
