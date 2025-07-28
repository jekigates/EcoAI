package com.bluejack242.ecoai.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == "progress",
            onClick = { navController.navigate("add_waste") },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Progress") },
            label = { Text("Progress") }
        )
        NavigationBarItem(
            selected = currentRoute == "create",
            onClick = { navController.navigate("create_post") },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "Create") },
            label = { Text("Create") }
        )
        NavigationBarItem(
            selected = currentRoute == "notifications",
            onClick = { navController.navigate("notifications") },
            icon = { Icon(Icons.Default.Email, contentDescription = "Notification") },
            label = { Text("Notification") }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
