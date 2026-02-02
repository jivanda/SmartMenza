package com.example.feature_google_login

import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.GoogleLoginRequest
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.utils.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import com.example.smartmenza.R as appR

class GoogleLoginManager(
    private val onLoginSuccess: () -> Unit
) : LoginManager {

    @Composable
    override fun LoginScreen() {}

    private var googleSignInClient: GoogleSignInClient? = null
    private var googleLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null

    @Composable
    override fun OnComposeStart() {
        val webClientId = stringResource(appR.string.default_web_client_id)

        val context = LocalContext.current
        val prefs = remember { UserPreferences(context) }
        val scope = rememberCoroutineScope()

        val gso = remember(webClientId) {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        }
        googleSignInClient = remember(gso) { GoogleSignIn.getClient(context, gso) }

        googleLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    scope.launch {
                        try {
                            val response =
                                RetrofitInstance.api.googleLogin(GoogleLoginRequest(idToken))

                            if (response.isSuccessful) {
                                val body = response.body()
                                val userId = body?.userId

                                if (body != null && userId != null) {
                                    prefs.saveUser(
                                        ime = body.username ?: "Korisnik",
                                        email = body.email ?: "",
                                        uloga = body.role ?: "Student",
                                        userId = userId
                                    )
                                    onLoginSuccess()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Google login manager", e.message.toString())
                        }
                    }
                }
            } catch (e: ApiException) {
                Log.e("Google login manager", e.message.toString())
            }
        }
    }

    override fun onButtonClicked() {
        val googleSignInClient = googleSignInClient ?: return
        val googleLauncher = googleLauncher ?: return
        googleSignInClient.signOut().addOnCompleteListener {
            googleLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    override val buttonText: String = "Prijava putem Googlea"
}
