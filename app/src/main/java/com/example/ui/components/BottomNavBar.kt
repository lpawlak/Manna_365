package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.viewmodel.AppNavTab

@Composable
fun BibleBottomNavBar(
    selectedTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bible_bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = selectedTab == AppNavTab.TODAY,
            onClick = { onTabSelected(AppNavTab.TODAY) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavTab.TODAY) Icons.AutoMirrored.Filled.MenuBook else Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = "Dziś"
                )
            },
            label = { Text("Dziś") },
            modifier = Modifier.testTag("nav_tab_today")
        )

        NavigationBarItem(
            selected = selectedTab == AppNavTab.PLANS,
            onClick = { onTabSelected(AppNavTab.PLANS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavTab.PLANS) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday,
                    contentDescription = "Plany"
                )
            },
            label = { Text("Plany") },
            modifier = Modifier.testTag("nav_tab_plans")
        )

        NavigationBarItem(
            selected = selectedTab == AppNavTab.PROGRESS,
            onClick = { onTabSelected(AppNavTab.PROGRESS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavTab.PROGRESS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = "Postęp"
                )
            },
            label = { Text("Postęp") },
            modifier = Modifier.testTag("nav_tab_progress")
        )

        NavigationBarItem(
            selected = selectedTab == AppNavTab.SETTINGS,
            onClick = { onTabSelected(AppNavTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppNavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Ustawienia"
                )
            },
            label = { Text("Ustawienia") },
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}
