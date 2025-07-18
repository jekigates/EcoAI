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
import com.bluejack242.ecoai.ui.HomeScreen
import com.bluejack242.ecoai.ui.auth.CreateNewPasswordScreen
import com.bluejack242.ecoai.ui.auth.ForgotPasswordScreen
import com.bluejack242.ecoai.ui.view.CreatePostScreen

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
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }

        composable("register") {
            val authViewModel: AuthViewModel = viewModel()
            RegisterScreen(
                viewModel = authViewModel,
                onLoginClick = { navController.navigate("login") }
            )
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                currentRoute = ""
            )
        }

        composable("forgot_password") {
            val authViewModel: AuthViewModel = viewModel()
            ForgotPasswordScreen(
                viewModel = authViewModel,
                navController
            )
        }

        composable("create_new_password/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val authViewModel: AuthViewModel = viewModel()
            CreateNewPasswordScreen(
                email = email,
                viewModel = authViewModel,
                onSuccess = { navController.navigate("home") })
        }

        composable("create_post") {
            CreatePostScreen(
                navController = navController
            )
        }

    }
}
