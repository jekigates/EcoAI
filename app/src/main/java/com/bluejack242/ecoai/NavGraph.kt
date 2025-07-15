package com.bluejack242.ecoai

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bluejack242.ecoai.ui.auth.LandingScreen
import com.bluejack242.ecoai.ui.auth.LoginScreen
import com.bluejack242.ecoai.ui.auth.RegisterScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") {
            LandingScreen(
                onLoginClick = { navController.navigate("login")},
                onGetStartedClick = { navController.navigate("register") }
            )
        }
        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginClick = { email, password ->

                },
                onForgotPasswordClick = { /* handle forgot password */ }
            )
        }
        composable("register") {
            RegisterScreen(
                onLoginClick = { navController.navigate("login")},
                onRegisterSubmit = { fName, lName, email, pass, confirm ->
                    // handle registration
                }
            )
        }
    }
}
