package storytale.gallery.demo

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun PrimaryButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  enabled: Boolean = true,
  size: PrimaryButtonSize = PrimaryButtonSize.Medium,
) {
  Button(
    modifier = modifier,
    onClick = onClick,
    enabled = enabled,
  ) {
    Text(
      text = text,
      fontSize = when (size) {
        PrimaryButtonSize.Small -> 12.sp
        PrimaryButtonSize.Medium -> 14.sp
        PrimaryButtonSize.Large -> 20.sp
      },
    )
  }
}

enum class PrimaryButtonSize {
  Small,
  Medium,
  Large,
}
