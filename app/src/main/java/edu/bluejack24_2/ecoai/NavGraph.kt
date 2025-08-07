package edu.bluejack24_2.ecoai

import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bluejack24_2.ecoai.ui.screen.UserListScreen
import edu.bluejack24_2.ecoai.ui.screen.AddWasteScreen
import edu.bluejack24_2.ecoai.ui.screen.CreatePostScreen
import edu.bluejack24_2.ecoai.ui.screen.EditProfileScreen
import edu.bluejack24_2.ecoai.ui.screen.ForgotPasswordScreen
import edu.bluejack24_2.ecoai.ui.screen.HistoryScreen
import edu.bluejack24_2.ecoai.ui.screen.HomeScreen
import edu.bluejack24_2.ecoai.ui.screen.LandingScreen
import edu.bluejack24_2.ecoai.ui.screen.LoginScreen
import edu.bluejack24_2.ecoai.ui.screen.NotificationScreen
import edu.bluejack24_2.ecoai.ui.screen.PostDetailScreen
import edu.bluejack24_2.ecoai.ui.screen.ProfileScreen
import edu.bluejack24_2.ecoai.ui.screen.ProgressScreen
import edu.bluejack24_2.ecoai.ui.screen.RegisterScreen
import edu.bluejack24_2.ecoai.ui.screen.SearchScreen
import edu.bluejack24_2.ecoai.ui.screen.SettingsScreen
import edu.bluejack24_2.ecoai.ui.screen.UserProfileScreen
import edu.bluejack24_2.ecoai.ui.screen.WasteDatabaseScreen
import edu.bluejack24_2.ecoai.ui.screen.WasteDetailScreen
import edu.bluejack24_2.ecoai.viewmodel.AuthViewModel
import edu.bluejack24_2.ecoai.viewmodel.ProgressViewModel
import edu.bluejack24_2.ecoai.viewmodel.WasteViewModel
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
