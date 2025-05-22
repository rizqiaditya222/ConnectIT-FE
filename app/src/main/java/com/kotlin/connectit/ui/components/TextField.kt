    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Email
    import androidx.compose.material.icons.filled.Lock
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.OutlinedTextField
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextFieldDefaults
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.vector.ImageVector
    import androidx.compose.ui.text.input.PasswordVisualTransformation
    import androidx.compose.ui.text.input.VisualTransformation
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    @Composable
    fun TextField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        isPassword: Boolean = false,
        leadingIcon: ImageVector? = null, // Tambahkan parameter icon opsional
        modifier: Modifier = Modifier
    ) {
        var passwordVisible by remember { mutableStateOf(false) }

        val icon = leadingIcon ?: if (isPassword) Icons.Default.Lock else Icons.Default.Email

        Column {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(label, color = Color.Gray, fontSize = 13.sp) },
                singleLine = true,
                visualTransformation = if (isPassword && !passwordVisible)
                    PasswordVisualTransformation() else VisualTransformation.None,
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1F222A),
                    unfocusedContainerColor = Color(0xFF1F222A),
                    disabledContainerColor = Color(0xFF2A2A2F),
                    cursorColor = Color(0xFF8B5CF6),
                    focusedIndicatorColor = Color(0xFF8B5CF6),
                    unfocusedIndicatorColor = Color(0xFF1F222A),
                ),
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                },
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
