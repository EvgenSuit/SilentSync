package com.suit.utility.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.suit.utility.R
import com.suit.utility.ui.theme.SilentSyncTheme

@Composable
fun PermissionDialog(
    text: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
                .height(350.dp)
                .width(800.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(15.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.permission_request),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(text,
                    textAlign = TextAlign.Center)
                CommonButton(
                    text = stringResource(R.string.accept),
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
fun PermissionDialogPreview() {
    SilentSyncTheme {
        Surface {
            PermissionDialog(
                text = "Notification policy permission is required",
                onDismiss = {},
                onAccept = {}
            )
        }
    }
}