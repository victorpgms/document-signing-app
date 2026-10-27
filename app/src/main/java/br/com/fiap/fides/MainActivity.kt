package br.com.fiap.fides

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FidesApp()
        }
    }
}

@Composable
private fun FidesApp() {
    MaterialTheme {
        Surface {
            Text(text = stringResource(R.string.app_name))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FidesAppPreview() {
    FidesApp()
}
