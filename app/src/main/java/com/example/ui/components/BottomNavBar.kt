package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.PrimaryBlue

sealed class NavItem(
    val titleHindi: String,
    val titleEnglish: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen,
    val testTag: String
) {
    object Home : NavItem("होम", "Home", Icons.Filled.Home, Icons.Outlined.Home, Screen.Home, "nav_home")
    object Jobs : NavItem("काम खोजें", "Jobs", Icons.Filled.Work, Icons.Outlined.WorkOutline, Screen.WorkerJobs, "nav_jobs")
    object Applications : NavItem("मेरे आवेदन", "Applied", Icons.Filled.Checklist, Icons.Outlined.Checklist, Screen.MyApplications, "nav_applications")
    object Company : NavItem("कंपनी", "Company", Icons.Filled.Business, Icons.Outlined.Business, Screen.CompanyDashboard, "nav_company")
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    isHindi: Boolean,
    onTabSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem.Home,
        NavItem.Jobs,
        NavItem.Applications,
        NavItem.Company
    )

    NavigationBar(
        modifier = modifier
            .testTag("bottom_nav_bar")
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = when (item.screen) {
                is Screen.Home -> currentScreen is Screen.Home
                is Screen.WorkerJobs -> currentScreen is Screen.WorkerJobs || currentScreen is Screen.JobDetails
                is Screen.MyApplications -> currentScreen is Screen.MyApplications
                is Screen.CompanyDashboard -> currentScreen is Screen.CompanyDashboard || currentScreen is Screen.PostJob || currentScreen is Screen.JobApplicants
                else -> false
            }

            NavigationBarItem(
                modifier = Modifier.testTag(item.testTag),
                selected = isSelected,
                onClick = { onTabSelected(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = if (isHindi) item.titleHindi else item.titleEnglish
                    )
                },
                label = {
                    Text(
                        text = if (isHindi) item.titleHindi else item.titleEnglish,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f)
                )
            )
        }
    }
}
