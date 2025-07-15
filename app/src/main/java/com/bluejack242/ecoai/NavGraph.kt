package com.bluejack242.ecoai

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bluejack242.ecoai.ui.auth.LandingScreen
import com.bluejack242.ecoai.ui.auth.LoginScreen
import com.bluejack242.ecoai.ui.auth.RegisterScreen
import com.bluejack242.ecoai.ui.auth.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "landing") {
        composable("landing") {
            LandingScreen(
                onLoginClick = { navController.navigate("login") },
                onGetStartedClick = { navController.navigate("register") }
            )
        }

        composable("login") {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(
                viewModel = authViewModel,
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("home") },
                onForgotPasswordClick = {  }
            )
        }

        composable("register") {
            val authViewModel: AuthViewModel = viewModel()
            RegisterScreen(
                viewModel = authViewModel,
                onLoginClick = { navController.navigate("login") },
                onRegisterSuccess = { navController.navigate("home") }
            )
        }

//        composable("home") {
//            HomeScreen()
//        }
    }
}
