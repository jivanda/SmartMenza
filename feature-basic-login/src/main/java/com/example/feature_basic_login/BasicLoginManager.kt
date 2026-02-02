package com.example.feature_basic_login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core_ui.R
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.LoginRequest
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import com.example.smartmenza.utils.LoginManager
import kotlinx.coroutines.launch

class BasicLoginManager(
    private val onLoginSuccess: () -> Unit,
    private val onGoToRegister: () -> Unit,
    private val loginHandlers: List<LoginManager>,
) : LoginManager {
    @Composable
    override fun LoginScreen() {
        val subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)

        val context = LocalContext.current
        val prefs = remember { UserPreferences(context) }
        val scope = rememberCoroutineScope()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        loginHandlers.forEach { it.OnComposeStart() }

        SmartMenzaTheme {
            Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBeige) {
                Column(modifier = Modifier.fillMaxSize()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                            .background(SpanRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SmartMenza",
                            style = TextStyle(
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = subtlePattern,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.06f),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .offset(y = (-40).dp)
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Prijava",
                                style = MaterialTheme.typography.headlineLarge
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Lozinka") },
                                visualTransformation = PasswordVisualTransformation()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        isLoading = true
                                        errorMessage = null

                                        scope.launch {
                                            try {
                                                val response = RetrofitInstance.api.login(
                                                    LoginRequest(email = email, password = password)
                                                )

                                                if (response.isSuccessful) {
                                                    val body = response.body()
                                                    val userId = body?.userId

                                                    if (body != null && userId != null) {
                                                        val ime = body.username ?: "Korisnik"
                                                        val uloga = body.role ?: "Student"
                                                        val emailRes = body.email ?: email

                                                        prefs.saveUser(
                                                            ime,
                                                            emailRes,
                                                            uloga = uloga,
                                                            userId = userId
                                                        )

                                                        Log.d("LOGIN", "Uspjeh: ${body.poruka}")
                                                        onLoginSuccess()
                                                    } else {
                                                        errorMessage =
                                                            "Greška: Korisnički ID nije primljen."
                                                    }
                                                } else {
                                                    errorMessage =
                                                        "Neispravni podaci (${response.code()})"
                                                }
                                            } catch (e: Exception) {
                                                errorMessage = "Greška: ${e.message}"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    } else {
                                        errorMessage = "Molimo unesite email i lozinku"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpanRed,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Potvrdi",
                                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            loginHandlers.forEach {
                                Button(
                                    onClick = {
                                        it.onButtonClicked()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(it.buttonText)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(onClick = onGoToRegister) {
                                Text("Nemaš račun? Registriraj se", color = SpanRed)
                            }

                            errorMessage?.let {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = it,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Text(
                            text = "Powered by SPAN",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun OnComposeStart() {
    }

    override fun onButtonClicked() {}

    override val buttonText: String
        get() = "Obična prijava"
}