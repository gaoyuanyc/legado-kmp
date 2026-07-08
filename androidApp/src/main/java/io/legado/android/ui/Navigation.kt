package io.legado.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.legado.android.ui.pages.BookshelfPage
import io.legado.android.ui.pages.SettingsPage
import io.legado.android.ui.pages.SourcePage
import io.legado.shared.model.Book
import io.legado.shared.model.BookSource

/**
 * App navigation routes.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Bookshelf : Screen("bookshelf", "Bookshelf", Icons.Default.Book)
    object Sources : Screen("sources", "Sources", Icons.Default.List)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

/**
 * Main app navigation composable.
 */
@Composable
fun LegadoNavigation(
    navController: NavHostController = rememberNavController(),
    books: List<Book> = emptyList(),
    sources: List<BookSource> = emptyList()
) {
    val items = listOf(Screen.Bookshelf, Screen.Sources, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Bookshelf.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Bookshelf.route) {
                BookshelfPage(
                    books = books,
                    onBookClick = { /* TODO: navigate to reader */ },
                    onSearchClick = { /* TODO: open search */ }
                )
            }
            composable(Screen.Sources.route) {
                SourcePage(
                    sources = sources,
                    onSourceClick = { /* TODO: edit source */ },
                    onAddSource = { /* TODO: add source */ }
                )
            }
            composable(Screen.Settings.route) {
                SettingsPage()
            }
        }
    }
}
