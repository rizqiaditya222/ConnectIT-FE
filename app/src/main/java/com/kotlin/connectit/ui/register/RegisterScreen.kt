
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.Checkbox
    import androidx.compose.material3.CheckboxDefaults
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.kotlin.connectit.R
    import com.kotlin.connectit_fe.ui.components.CustomButton

    @Composable
    fun RegisterScreen(
        onRegisterClick: () -> Unit,
        onLoginClick: () -> Unit
    ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var repeatPassword by remember { mutableStateOf("") }
        var agreeToTerms by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()  // Ensure it fills the whole screen
                .background(Color(0xFF191A1F))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bubble_register),
                        contentDescription = "Background Bubbles",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(.9f)
                            .padding(end = 16.dp, bottom = 32.dp)
                    )

                    Text(
                        text = "Register",
                        fontSize = 39.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 100.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it }
                    )

                    TextField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true
                    )

                    TextField(
                        label = "Repeat Password",
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it },
                        isPassword = true
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = agreeToTerms,
                            onCheckedChange = { agreeToTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF1F222A)
                            )
                        )
                        Text("I agree with terms and condition", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Normal)
                    }

                    // Key change: Make sure CustomButton onClick triggers the navigation callback
                    CustomButton(
                        text = "Register",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            println("Register button pressed")
                            onRegisterClick()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        Text("Already have an account? ", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Normal)
                        Text(
                            text = "Sign In",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier
                                .clickable {
                                    println("Login link clicked")
                                    onLoginClick()
                                }
                        )
                    }
                }
            }
        }
    }