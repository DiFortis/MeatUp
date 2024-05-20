package com.example.meatup

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.meatup.ui.screens.*
import com.example.meatup.ui.theme.MeatUpTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        setContent {
            MeatUpTheme {
                var authState by remember { mutableStateOf(AuthState.LOGIN) }
                val currentUser = remember { mutableStateOf(auth.currentUser) }

                if (currentUser.value != null) {
                    ProfileScreen(
                        userEmail = currentUser.value!!.email ?: "",
                        onLogout = {
                            auth.signOut()
                            currentUser.value = null
                            authState = AuthState.LOGIN
                        }
                    )
                } else {
                    when (authState) {
                        AuthState.LOGIN -> LoginScreen(
                            onLoginSuccess = { user -> currentUser.value = user },
                            onRegisterClick = { authState = AuthState.REGISTER }
                        )
                        AuthState.REGISTER -> RegisterScreen(
                            onRegisterSuccess = { user -> currentUser.value = user },
                            onLoginClick = { authState = AuthState.LOGIN }
                        )
                    }
                }
            }
        }
    }
}

enum class AuthState {
    LOGIN, REGISTER
}