package com.br.urlshortener.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.br.urlshortener.R
import com.br.urlshortener.viewmodel.UrlShortenerViewModel

enum class NavRoute(@field:StringRes val title: Int) {
    SplashScreenRoute(title = R.string.app_name),
    ShortenerUrlScreenRoute(title = R.string.list_shortener_url),
    UrlDetailScreenRoute(title = R.string.shortener_url_detail),
}

@Composable
internal fun UrlShortenerApp(
    modifier: Modifier = Modifier,
    viewModel: UrlShortenerViewModel = viewModel(factory = UrlShortenerViewModel.FACTORY),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = NavRoute.valueOf(backStackEntry?.destination?.route ?: NavRoute.SplashScreenRoute.name)

    val onClickItem: () -> Unit = {
        navController.navigate(NavRoute.UrlDetailScreenRoute.name)
        viewModel.putUiOnIdle()
    }

    val onBackPressed: () -> Unit = {
        backToShortenerUrlScreen(navController)
    }

    Scaffold(
        topBar = {
            UrlShortenerAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.SplashScreenRoute.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = NavRoute.SplashScreenRoute.name) {
                SplashScreen {
                    navController.navigate(NavRoute.ShortenerUrlScreenRoute.name)
                }
            }

            composable(route = NavRoute.ShortenerUrlScreenRoute.name) {
                UrlShortenerScreen(
                    modifier = modifier.fillMaxHeight(),
                    urlShortenerViewModel = viewModel,
                    onClickItem = onClickItem
                )
            }

            composable(route = NavRoute.UrlDetailScreenRoute.name) {
                viewModel.urlShortener.value?.let {
                    UrlDetailScreen(it.url, onBackPressed)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrlShortenerAppBar(
    currentScreen: NavRoute,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = Color.Unspecified,
            actionIconContentColor = Color.Unspecified
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

private fun backToShortenerUrlScreen(navController: NavHostController) {
    navController.popBackStack(NavRoute.ShortenerUrlScreenRoute.name, inclusive = false)
}

@Preview
@Composable
private fun UrlShortenerAppBarPreview() {
    UrlShortenerAppBar(
        currentScreen = NavRoute.ShortenerUrlScreenRoute,
        canNavigateBack = true,
        navigateUp = {}
    )
}
