import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Composable
fun CustomCheckbox() {
    var rememberMe by remember { mutableStateOf(false) }

    Checkbox(
        checked = rememberMe,
        onCheckedChange = { rememberMe = it },
        colors = CheckboxDefaults.colors(
            checkedColor = Color(0xFF39007E)
        )
    )
}