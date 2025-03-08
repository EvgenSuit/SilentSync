package com.suit.utility.ui

import android.content.Context
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.suit.utility.ui.theme.SilentSyncTheme

val LocalSnackbarController = compositionLocalOf<SnackbarController> {
    error("No snackbar controller provided")
}

class SnackbarController(
    private val context: Context,
    private val snackbarHostState: SnackbarHostState
) {
    suspend fun showSnackbar(uiText: UIText) {
        val text = uiText.asString(context)
        dismiss()
        snackbarHostState.showSnackbar(text)
    }
    fun dismiss() = snackbarHostState.currentSnackbarData?.dismiss()
}

@Composable
fun CustomSnackbar(
    message: String?,
    onDismiss: () -> Unit
) {
    Snackbar(
        action = {
            IconButton(
                onClick = onDismiss
            ) {
                val icon = Icons.Filled.Close
                Icon(icon, contentDescription = icon.name)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(15.dp).imePadding()
    ) {
        message?.let {
            Text(it,
                style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview
@Composable
fun CustomSnackbarPreview() {
    SilentSyncTheme(darkTheme = false) {
        Surface {
            CustomSnackbar(
                message = "Snackbar message ".repeat(4),
                onDismiss = {}
            )
        }
    }
}