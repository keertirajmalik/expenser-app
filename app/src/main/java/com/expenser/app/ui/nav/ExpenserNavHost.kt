package com.expenser.app.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.expenser.app.ui.category.CategoryScreen
import com.expenser.app.ui.dashboard.DashboardScreen
import com.expenser.app.ui.expense.ExpenseScreen
import com.expenser.app.ui.income.IncomeScreen
import com.expenser.app.ui.investment.InvestmentScreen
import com.expenser.app.ui.profile.ProfileScreen

private enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Dashboard("dashboard", "Dashboard", Icons.Filled.Dashboard),
    Income("income", "Income", Icons.Filled.Payments),
    Expenses("expenses", "Expenses", Icons.AutoMirrored.Filled.ReceiptLong),
    Investments("investments", "Investments", Icons.AutoMirrored.Filled.TrendingUp),
}

private const val PROFILE_ROUTE = "profile"
private const val CATEGORIES_ROUTE = "categories"

@Composable
fun ExpenserNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination

    Scaffold(
        // The floating pill supplies its own margins + system-bar padding; inner
        // screen TopAppBars own the top inset, so don't consume insets here.
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            FloatingNavBar(
                selectedRoute = { route -> currentDestination?.hierarchy?.any { it.route == route } == true },
                onSelect = { route ->
                    // Switch tabs and drop any pushed detail screen (categories/profile).
                    // No saveState/restoreState: restoring a tab's back stack could bring a
                    // detail screen back on top, making the tap look like a no-op.
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        },
    ) { padding ->
        val openProfile = { navController.navigate(PROFILE_ROUTE) { launchSingleTop = true } }
        val openCategories = { navController.navigate(CATEGORIES_ROUTE) { launchSingleTop = true } }
        NavHost(
            navController = navController,
            startDestination = Destination.Dashboard.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destination.Dashboard.route) { DashboardScreen(openCategories, openProfile) }
            composable(Destination.Income.route) { IncomeScreen(openCategories, openProfile) }
            composable(Destination.Expenses.route) { ExpenseScreen(openCategories, openProfile) }
            composable(Destination.Investments.route) { InvestmentScreen(openCategories, openProfile) }
            composable(CATEGORIES_ROUTE) { CategoryScreen(onBack = { navController.popBackStack() }) }
            composable(PROFILE_ROUTE) { ProfileScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
private fun FloatingNavBar(
    selectedRoute: (String) -> Boolean,
    onSelect: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Destination.entries.forEach { dest ->
                    NavPillItem(
                        label = dest.label,
                        icon = dest.icon,
                        selected = selectedRoute(dest.route),
                        onClick = { onSelect(dest.route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavPillItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = label, tint = fg)
        if (selected) {
            Text(label, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
