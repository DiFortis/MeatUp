package com.example.meatup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.meatup.ui.screens.*
import com.example.meatup.ui.theme.MeatUpTheme
import com.google.firebase.auth.FirebaseAuth
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
                val userDetails = remember { mutableStateOf(Pair("", "")) }

                if (currentUser.value != null) {
                    when (authState) {
                        AuthState.USER_DETAILS -> UserDetailsScreen { firstName, lastName ->
                            userDetails.value = Pair(firstName, lastName)
                            authState = AuthState.PROFILE
                        }
                        AuthState.PROFILE -> ProfileScreen(
                            userEmail = currentUser.value!!.email ?: "",
                            onLogout = {
                                auth.signOut()
                                currentUser.value = null
                                authState = AuthState.LOGIN
                            },
                            onEditDetails = { authState = AuthState.USER_DETAILS }
                        )
                        else -> {
                            ProfileScreen(
                                userEmail = currentUser.value!!.email ?: "",
                                onLogout = {
                                    auth.signOut()
                                    currentUser.value = null
                                    authState = AuthState.LOGIN
                                },
                                onEditDetails = { authState = AuthState.USER_DETAILS }
                            )
                        }
                    }
                } else {
                    when (authState) {
                        AuthState.LOGIN -> LoginScreen(
                            onLoginSuccess = { user ->
                                currentUser.value = user
                                authState = AuthState.PROFILE
                            },
                            onRegisterClick = { authState = AuthState.REGISTER }
                        )
                        AuthState.REGISTER -> RegisterScreen(
                            onRegisterSuccess = { user ->
                                currentUser.value = user
                                authState = AuthState.USER_DETAILS
                            },
                            onLoginClick = { authState = AuthState.LOGIN }
                        )

                        AuthState.USER_DETAILS -> TODO()
                        AuthState.PROFILE -> TODO()
                    }
                }
            }
        }
    }
}

enum class AuthState {
    LOGIN, REGISTER, USER_DETAILS, PROFILE
}
