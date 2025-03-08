package com.suit.utility.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.suit.utility.ui.theme.SilentSyncTheme

@Composable
fun CommonButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(
            text = text
        )
    }
}

@Preview
@Composable
fun CommonButtonPreview() {
    SilentSyncTheme {
        Surface {
            CommonButton(
                text = "Some button text",
                onClick = {}
            )
        }
    }
}