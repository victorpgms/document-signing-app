package br.com.fiap.fides

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.fiap.fides.core.ui.theme.FidesTheme

private enum class FidesRoute(
    val route: String,
    val label: String,
) {
    DOCUMENTS("documents", "Documentos"),
    SIGNERS("signers", "Signatários"),
    PROFILE("profile", "Perfil"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FidesApp() }
    }
}

@Composable
private fun FidesApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    FidesTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar {
                    FidesRoute.entries.forEach { item ->
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
                        )
                    }
                }
            },
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = FidesRoute.DOCUMENTS.route,
                modifier = Modifier.padding(paddingValues),
            ) {
                composable(FidesRoute.DOCUMENTS.route) { DocumentsScreen() }
                composable(FidesRoute.SIGNERS.route) { SignersScreen() }
                composable(FidesRoute.PROFILE.route) { ProfileScreen() }
            }
        }
    }
}

@Composable
private fun DocumentsScreen() {
    FidesPlaceholderScreen(
        title = "Documentos",
        description = "Seus documentos aparecerão aqui.",
        actionLabel = "Novo documento",
    )
}

@Composable
private fun SignersScreen() {
    FidesPlaceholderScreen(
        title = "Signatários",
        description = "Cadastre e acompanhe os participantes dos documentos.",
        actionLabel = "Adicionar signatário",
    )
}

@Composable
private fun ProfileScreen() {
    FidesPlaceholderScreen(
        title = "Perfil",
        description = "As preferências da conta ficarão disponíveis aqui.",
        actionLabel = "Configurar aparência",
    )
}

@Composable
private fun FidesPlaceholderScreen(
    title: String,
    description: String,
    actionLabel: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = description,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(actionLabel)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentsScreenPreview() {
    FidesTheme { DocumentsScreen() }
}
