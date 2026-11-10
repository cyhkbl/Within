package io.github.cyhkbl.within.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.cyhkbl.within.LedgerApp
import io.github.cyhkbl.within.data.LedgerRepository
import io.github.cyhkbl.within.data.db.TransactionEntity
import io.github.cyhkbl.within.ui.assets.AssetsScreen
import io.github.cyhkbl.within.ui.calendar.CalendarScreen
import io.github.cyhkbl.within.ui.components.SymbolText
import io.github.cyhkbl.within.ui.detail.TransactionDetailScreen
import io.github.cyhkbl.within.ui.entry.EntryScreen
import io.github.cyhkbl.within.ui.entry.EntryViewModel
import io.github.cyhkbl.within.ui.home.HomeScreen
import io.github.cyhkbl.within.ui.home.HomeViewModel
import io.github.cyhkbl.within.ui.more.MoreScreen
import io.github.cyhkbl.within.ui.stats.StatsScreen
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import io.github.cyhkbl.within.R

private data class Destination(val route: String, @StringRes val label: Int, val icon: String)

private val destinations = listOf(
    Destination("home", R.string.nav_00, "receipt_long"),
    Destination("stats", R.string.nav_01, "pie_chart"),
    Destination("assets", R.string.nav_02, "account_balance_wallet"),
    Destination("more", R.string.nav_03, "menu")
)

@Composable
fun LedgerRoot(repo: LedgerRepository) {
    val nav = rememberNavController()
    val factory = remember(repo) {
        viewModelFactory {
            initializer { HomeViewModel(repo) }
        }
    }

    NavHost(
        navController = nav,
        startDestination = "home",
        enterTransition = {
            slideInVertically(animationSpec = tween(260)) { it / 12 } + fadeIn(tween(220))
        },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = {
            slideOutVertically(animationSpec = tween(240)) { it / 12 } + fadeOut(tween(180))
        }
    ) {
        composable("home") {
            val vm: HomeViewModel = viewModel(factory = factory)
            val state by vm.state.collectAsState()
            MainShell(
                nav = nav,
                currentRoute = "home",
                onNavigate = { nav.navigate(it) { launchSingleTop = true; popUpTo("home") { saveState = true } } }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .statusBarsPadding()
                ) {
                    HomeScreen(
                        state = state,
                        onPrevMonth = vm::previousMonth,
                        onNextMonth = vm::nextMonth,
                    onSelectMonth = vm::setMonth,
                        onAdd = { nav.navigate("entry") },
                        onOpenCalendar = { nav.navigate("calendar") },
                        onTxClick = { tx -> nav.navigate("detail/${tx.id}") }
                    )
                }
            }
        }

        composable(
            route = "entry",
            enterTransition = { slideInVertically(tween(280)) { it } + fadeIn(tween(180)) },
            exitTransition = { slideOutVertically(tween(260)) { it } + fadeOut(tween(160)) }
        ) {
            val entryFactory = remember(repo) {
                viewModelFactory { initializer { EntryViewModel(repo) } }
            }
            val vm: EntryViewModel = viewModel(factory = entryFactory)
            val state by vm.state.collectAsState()
            EntryScreen(
                state = state,
                onKindChange = vm::switchKind,
                onCategory = vm::selectCategory,
                onKey = vm::pressKey,
                onNote = vm::setNote,
                onAccount = vm::selectAccount,
                onTimestamp = vm::setTimestamp,
                onSave = { vm.save { nav.popBackStack() } },
                onClose = { nav.popBackStack() }
            )
        }

        composable("calendar") {
            CalendarScreen(
                repo = repo,
                onBack = { nav.popBackStack() },
                onTxClick = { id -> nav.navigate("detail/$id") }
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            TransactionDetailScreen(
                repo = repo,
                txId = backStackEntry.arguments?.getLong("id") ?: 0L,
                onBack = { nav.popBackStack() }
            )
        }

        composable("stats") {
            MainShell(
                nav = nav,
                currentRoute = "stats",
                onNavigate = { nav.navigate(it) { launchSingleTop = true; popUpTo("home") { saveState = true } } }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .statusBarsPadding()
                ) { StatsScreen(repo) }
            }
        }

        composable("assets") {
            MainShell(
                nav = nav,
                currentRoute = "assets",
                onNavigate = { nav.navigate(it) { launchSingleTop = true; popUpTo("home") { saveState = true } } }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .statusBarsPadding()
                ) { AssetsScreen(repo) }
            }
        }

        composable("more") {
            MainShell(
                nav = nav,
                currentRoute = "more",
                onNavigate = { nav.navigate(it) { launchSingleTop = true; popUpTo("home") { saveState = true } } }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .statusBarsPadding()
                ) { MoreScreen(repo) }
            }
        }
    }
}

@Composable
private fun MainShell(
    nav: NavHostController,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0)
            ) {
                destinations.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = { onNavigate(dest.route) },
                        icon = {
                            SymbolText(
                                name = dest.icon,
                                size = 24.dp,
                                color = if (currentRoute == dest.route)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text(stringResource(dest.label)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    ) { padding -> content(padding) }
}
