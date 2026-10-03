import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.storytale.example.Res
import org.jetbrains.compose.storytale.example.compose_multiplatform

@Composable
fun ComposeLogo(
    modifier: Modifier = Modifier.size(96.dp),
) {
    Image(
        painter = painterResource(Res.drawable.compose_multiplatform),
        contentDescription = "Compose Multiplatform Logo",
        modifier = modifier,
    )
}
