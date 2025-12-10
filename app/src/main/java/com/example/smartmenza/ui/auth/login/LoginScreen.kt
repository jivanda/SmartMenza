package com.example.smartmenza.ui.auth.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import com.example.smartmenza.R
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.LoginRequest
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.navigation.Route
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.smartmenza.data.remote.GoogleLoginRequest


@Composable
fun LoginScreen(
    navController: NavController,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                scope.launch {
                    try {
                        val response = RetrofitInstance.api.googleLogin(GoogleLoginRequest(idToken))

                        if (response.isSuccessful) {
                            val body = response.body()
                            val userId = body?.userId

                            if (body != null && userId != null) {
                                prefs.saveUser(
                                    ime = body.username!!,
                                    email = body.email!!,
                                    uloga = body.uloga!!,
                                    userId = userId
                                )

                                navController.navigate(Route.StudentHome.route) {
                                    popUpTo(Route.Login.route) { inclusive = true }
                                }
                            } else {
                                errorMessage = "Neispravan odgovor servera."
                            }
                        } else {
                            errorMessage = "Google prijava neuspješna (${response.code()})"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Greška: ${e.message}"
                    }
                }
            }
        } catch (e: ApiException) {
            errorMessage = "Google error: ${e.message}"
        }
    }


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
                                                    val uloga = body.uloga ?: "Student"
                                                    val emailRes = body.email ?: email

                                                    prefs.saveUser(ime, emailRes, uloga, userId)

                                                    Log.d("LOGIN", "Uspjeh: ${body.poruka}")

                                                    isLoading = false
                                                    navController.navigate(Route.StudentHome.route) {
                                                        popUpTo(Route.Login.route) { inclusive = true }
                                                    }
                                                } else {
                                                    errorMessage = "Greška: Korisnički ID nije primljen."
                                                    isLoading = false
                                                }
                                            } else {
                                                errorMessage = "Neispravni podaci (${response.code()})"
                                                isLoading = false
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Greška: ${e.message}"
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

                        Button(
                            onClick = {
                                googleLauncher.launch(googleSignInClient.signInIntent)
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
                            Text("Prijava putem Googlea")
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
