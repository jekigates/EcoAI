package com.bluejack242.ecoai

import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluejack242.ecoai.ui.screen.UserListScreen
import com.bluejack242.ecoai.ui.screen.AddWasteScreen
import com.bluejack242.ecoai.ui.screen.CreatePostScreen
import com.bluejack242.ecoai.ui.screen.EditProfileScreen
import com.bluejack242.ecoai.ui.screen.ForgotPasswordScreen
import com.bluejack242.ecoai.ui.screen.HistoryScreen
import com.bluejack242.ecoai.ui.screen.HomeScreen
import com.bluejack242.ecoai.ui.screen.LandingScreen
import com.bluejack242.ecoai.ui.screen.LoginScreen
import com.bluejack242.ecoai.ui.screen.NotificationScreen
import com.bluejack242.ecoai.ui.screen.PostDetailScreen
import com.bluejack242.ecoai.ui.screen.ProfileScreen
import com.bluejack242.ecoai.ui.screen.ProgressScreen
import com.bluejack242.ecoai.ui.screen.RegisterScreen
import com.bluejack242.ecoai.ui.screen.SearchScreen
import com.bluejack242.ecoai.ui.screen.SettingsScreen
import com.bluejack242.ecoai.ui.screen.UserProfileScreen
import com.bluejack242.ecoai.ui.screen.WasteDatabaseScreen
import com.bluejack242.ecoai.ui.screen.WasteDetailScreen
import com.bluejack242.ecoai.viewmodel.AuthViewModel
import com.bluejack242.ecoai.viewmodel.ProgressViewModel
import com.bluejack242.ecoai.viewmodel.WasteViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {

    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val startDestination = if (isLoggedIn) "home" else "landing"

    NavHost(navController, startDestination = startDestination) {
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

        composable("search/{query}") { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchScreen(navController = navController, query = query)
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
                onLogout = onLogout
            )
        }

        composable("add_waste") {
            val viewModel: WasteViewModel = viewModel()
            AddWasteScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable("progress") {
            val viewModel: ProgressViewModel = viewModel()

            ProgressScreen(
                navController = navController,
                onItemClick = { itemId ->
                    navController.navigate("wasteDetail/$itemId")
                },
                viewModel = viewModel
            )
        }
        composable("followers_list/{userIds}") { backStackEntry ->
            val userIds = backStackEntry.arguments?.getString("userIds")?.split(",") ?: emptyList()
            UserListScreen(
                title = "Followers",
                userIds = userIds,
                navController = navController
            )
        }

        composable("following_list/{userIds}") { backStackEntry ->
            val userIds = backStackEntry.arguments?.getString("userIds")?.split(",") ?: emptyList()
            UserListScreen(
                title = "Following",
                userIds = userIds,
                navController = navController
            )
        }

        composable("history") {
            HistoryScreen(
                navController = navController,
                onItemClick = { itemId -> navController.navigate("wasteDetail/$itemId") }
            )
        }

        composable(
            "wasteDetail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            val viewModel: ProgressViewModel = viewModel()

            WasteDetailScreen(
                viewModel = viewModel,
                itemId = itemId,
                onDone = { navController.popBackStack() }
            )
        }

        composable("waste_database") {
            WasteDatabaseScreen(navController = navController)
        }
    }
}
