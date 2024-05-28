package com.example.meatup

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.meatup.ui.components.BottomNavigationBar
import com.example.meatup.ui.components.MeatShopsMap
import com.example.meatup.ui.screens.SausageScreen
import com.example.meatup.ui.screens.*
import com.example.meatup.ui.theme.AppTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation by mutableStateOf<Location?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        setContent {
            AppTheme {
                var authState by remember { mutableStateOf(AuthState.LOGIN) }
                val currentUser = remember { mutableStateOf(auth.currentUser) }
                val userDetails = remember { mutableStateOf(Quadruple("", "", "", "")) }
                val snackbarHostState = remember { SnackbarHostState() }
                var showPasswordChangedSnackbar by remember { mutableStateOf(false) }
                LocalContext.current

                val locationPermissionRequest = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (granted) {
                        getUserLocation()
                    }
                }

                if (currentUser.value != null) {
                    Scaffold(
                        snackbarHost = {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                SnackbarHost(
                                    hostState = snackbarHostState,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )
                            }
                        },
                        bottomBar = {
                            BottomNavigationBar(selectedItem = authState) { selectedState, requiresLocation ->
                                authState = selectedState
                                if (requiresLocation && userLocation == null) {
                                    locationPermissionRequest.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (authState) {
                                AuthState.USER_DETAILS -> UserDetailsScreen(
                                    onDetailsSubmitted = { firstName, lastName, phoneNumber, country ->
                                        userDetails.value = Quadruple(firstName, lastName, phoneNumber, country)
                                        authState = AuthState.PROFILE
                                    },
                                    onBack = { authState = AuthState.PROFILE }
                                )
                                AuthState.PROFILE -> ProfileScreen(
                                    userEmail = currentUser.value!!.email ?: "",
                                    onLogout = {
                                        auth.signOut()
                                        currentUser.value = null
                                        authState = AuthState.LOGIN
                                    },
                                    onEditDetails = { authState = AuthState.USER_DETAILS },
                                    onChangePassword = { authState = AuthState.CHANGE_PASSWORD },
                                    showPasswordChangedSnackbar = showPasswordChangedSnackbar
                                )
                                AuthState.CHANGE_PASSWORD -> ChangePasswordScreen(
                                    onPasswordChangeSuccess = {
                                        authState = AuthState.PROFILE
                                        showPasswordChangedSnackbar = true
                                    },
                                    onBack = { authState = AuthState.PROFILE }
                                )
                                AuthState.NEAREST_MEAT_SHOPS -> {
                                    if (userLocation != null) {
                                        MeatShopsMap(userLocation!!)
                                    } else {
                                        Text("Fetching location...")
                                    }
                                }
                                AuthState.SAUSAGE_ANIMATION -> SausageScreen()
                                else -> {
                                    ProfileScreen(
                                        userEmail = currentUser.value!!.email ?: "",
                                        onLogout = {
                                            auth.signOut()
                                            currentUser.value = null
                                            authState = AuthState.LOGIN
                                        },
                                        onEditDetails = { authState = AuthState.USER_DETAILS },
                                        onChangePassword = { authState = AuthState.CHANGE_PASSWORD },
                                        showPasswordChangedSnackbar = showPasswordChangedSnackbar
                                    )
                                }
                            }
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
                        AuthState.USER_DETAILS -> UserDetailsScreen(
                            onDetailsSubmitted = { firstName, lastName, phoneNumber, country ->
                                userDetails.value = Quadruple(firstName, lastName, phoneNumber, country)
                                authState = AuthState.PROFILE
                            },
                            onBack = { authState = AuthState.LOGIN }
                        )
                        AuthState.PROFILE -> ProfileScreen(
                            userEmail = currentUser.value!!.email ?: "",
                            onLogout = {
                                auth.signOut()
                                currentUser.value = null
                                authState = AuthState.LOGIN
                            },
                            onEditDetails = { authState = AuthState.USER_DETAILS },
                            onChangePassword = { authState = AuthState.CHANGE_PASSWORD },
                            showPasswordChangedSnackbar = showPasswordChangedSnackbar
                        )
                        AuthState.NEAREST_MEAT_SHOPS -> {
                            if (userLocation != null) {
                                MeatShopsMap(userLocation!!)
                            } else {
                                Text("Fetching location...")
                            }
                        }
                        AuthState.CHANGE_PASSWORD -> ChangePasswordScreen(
                            onPasswordChangeSuccess = {
                                authState = AuthState.PROFILE
                                showPasswordChangedSnackbar = true
                            },
                            onBack = { authState = AuthState.PROFILE }
                        )

                        AuthState.SAUSAGE_ANIMATION -> TODO()
                    }
                }
            }
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                userLocation = location
            }
        }
    }
}

enum class AuthState {
    LOGIN, REGISTER, USER_DETAILS, PROFILE, CHANGE_PASSWORD, NEAREST_MEAT_SHOPS, SAUSAGE_ANIMATION
}

// Quadruple data class to hold four values
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
