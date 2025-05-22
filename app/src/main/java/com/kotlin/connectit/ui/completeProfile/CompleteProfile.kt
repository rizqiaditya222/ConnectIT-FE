
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.Icon


@Composable
fun CompleteProfile(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .background(Color(0xFF191A1F))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bubble_register),
                    contentDescription = "Background Bubbles",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.9f)
                        .padding(end = 16.dp, bottom = 32.dp)
                )

                Column {
                    Row(
                        modifier = Modifier
//                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .padding(top = 32.dp, start = 24.dp, end = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.padding(end = 10.dp)

                            )

                            Text(
                                text = "Complete your profile",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White,
                            )
                        }

                        Text(
                            text = "Skip",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF39007E),
                            modifier = Modifier.clickable {
                                onLoginClick()
                            }
                        )
                    }
                    Card (modifier = Modifier) {

                    }
                }

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
                    label = "Username",
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = Icons.Default.Person
                )

                TextField(
                    label = "Full Name",
                    value = password,
                    onValueChange = { password = it },
                    leadingIcon = Icons.Default.Person
                )

                TextField(
                    label = "Phone Number",
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    leadingIcon = Icons.Default.Phone
                )
                TextField(
                    label = "Birth Date",
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    leadingIcon = Icons.Default.DateRange
                )

                TextField(
                    label = "Address",
                    value = password,
                    onValueChange = { password = it },
                    leadingIcon = Icons.Default.LocationOn
                )

            }

            CustomButton(
                text = "Finish",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                onClick = {
                    println("Register button pressed")
                    onRegisterClick()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompleteProfilePreview() {
    CompleteProfile(
        onRegisterClick = {},
        onLoginClick = {}
    )
}