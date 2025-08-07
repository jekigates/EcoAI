package edu.bluejack24_2.ecoai.ui.component


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.items.wigglebutton.WiggleButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import edu.bluejack24_2.ecoai.utils.LanguageManager
import edu.bluejack24_2.ecoai.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String) {
    val items = listOf(
        NavBarItem(
            route = "home",
            icon = R.drawable.baseline_home_24,
            backgroundIcon = R.drawable.baseline_home_24,
            label = LanguageManager.getString("home")
        ),
        NavBarItem(
            route = "progress",
            icon = R.drawable.baseline_bar_chart_24,
            backgroundIcon = R.drawable.baseline_bar_chart_24,
            label = LanguageManager.getString("progress")
        ),
        NavBarItem(
            route = "create_post",
            icon = R.drawable.baseline_create_24,
            backgroundIcon = R.drawable.baseline_create_24,
            label = LanguageManager.getString("create")
        ),
        NavBarItem(
            route = "notifications",
            icon = R.drawable.baseline_notifications_24,
            backgroundIcon = R.drawable.baseline_notifications_24,
            label = LanguageManager.getString("notification")
        ),
        NavBarItem(
            route = "profile",
            icon = R.drawable.baseline_person_24,
            backgroundIcon = R.drawable.baseline_person_24,
            label = LanguageManager.getString("profile")
        )
    )

    var selectedIndex by remember {
        mutableIntStateOf(items.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0)
    }

    val colorScheme = MaterialTheme.colorScheme
    AnimatedNavigationBar(
        selectedIndex = selectedIndex,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(85.dp)
            .navigationBarsPadding(),
        barColor = colorScheme.surface,
        ballColor = colorScheme.primary,
        cornerRadius = shapeCornerRadius(25.dp),
    ) {
        items.forEachIndexed { index, item ->
            WiggleButton(
                modifier = Modifier.fillMaxSize(),
                isSelected = selectedIndex == index,
                icon = item.icon,
                backgroundIcon = item.backgroundIcon,
                contentDescription = item.label,
                onClick = {
                    if (selectedIndex != index) {
                        selectedIndex = index
                        navController.navigate(item.route)
                    }
                },
                wiggleColor = colorScheme.secondaryContainer,
                outlineColor = colorScheme.primary
            )
        }
    }
}

data class NavBarItem(
    val route: String,
    val icon: Int,
    val backgroundIcon: Int,
    val label: String
)
