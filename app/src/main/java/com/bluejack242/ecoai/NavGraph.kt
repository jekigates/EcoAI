package com.bluejack242.ecoai

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluejack242.ecoai.ui.screen.AddWasteScreen
import com.bluejack242.ecoai.ui.screen.CreateNewPasswordScreen
import com.bluejack242.ecoai.ui.screen.CreatePostScreen
import com.bluejack242.ecoai.ui.screen.EditProfileScreen
import com.bluejack242.ecoai.ui.screen.ForgotPasswordScreen
import com.bluejack242.ecoai.ui.screen.HomeScreen
import com.bluejack242.ecoai.ui.screen.LandingScreen
import com.bluejack242.ecoai.ui.screen.LoginScreen
import com.bluejack242.ecoai.ui.screen.NotificationScreen
import com.bluejack242.ecoai.ui.screen.PostDetailScreen
import com.bluejack242.ecoai.ui.screen.ProfileScreen
import com.bluejack242.ecoai.ui.screen.RegisterScreen
import com.bluejack242.ecoai.ui.screen.SearchScreen
import com.bluejack242.ecoai.ui.screen.SettingsScreen
import com.bluejack242.ecoai.ui.screen.UserProfileScreen
import com.bluejack242.ecoai.ui.screen.SettingsScreen
import com.bluejack242.ecoai.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(navController: NavHostController,
             isDarkTheme: Boolean,
             onThemeChange: (Boolean) -> Unit,
             isNotificationEnabled: Boolean,
             onNotificationChange: (Boolean) -> Unit,
             onLogout: () -> Unit) {

    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val startDestination = if (isLoggedIn) "home" else "landing"

    NavHost(navController, startDestination = startDestination) {
        composable("landing") {
            LandingScreen (
                onLoginClick = { navController.navigate("login") },
                onGetStartedClick = { navController.navigate("register") }
            )
        }

        composable("login") {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen (
                viewModel = authViewModel,
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("home") },
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }

        composable("register") {
            val authViewModel: AuthViewModel = viewModel()
            RegisterScreen (
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
            CreateNewPasswordScreen (
                email = email,
                viewModel = authViewModel,
                onSuccess = { navController.navigate("home") })
        }

        composable("create_post") {
            CreatePostScreen(
                navController = navController
            )
        }

        composable("profile") {
            ProfileScreen(navController = navController, currentRoute = "profile")
        }

        composable("edit_profile") {
            EditProfileScreen(navController)
        }

        composable("post_detail/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(postId = postId, navController = navController)
        }

        composable("search") {
            SearchScreen(navController = navController, currentRoute = "search")
        }

        composable("user_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileScreen(userId = userId, navController = navController)
        }

        composable("notifications") {
            NotificationScreen(navController, currentRoute = "notifications")
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                isNotificationEnabled = isNotificationEnabled,
                onNotificationChange = onNotificationChange,
                onLogout = onLogout
            )
        }

        composable("add_waste") {
            AddWasteScreen(
                onCameraClick = { /* handle */ },
                onGalleryClick = { /* handle */ }
            )
        }
    }
}
