package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.NavigationTab
import com.example.ui.screens.*
import com.example.ui.theme.ManulaSocialAITheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            var isShowingBrandScreen by remember { mutableStateOf(false) }

            // Handle Toast messages
            LaunchedEffect(uiState.toastMessage) {
                uiState.toastMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            // Back Press Handling
            BackHandler(enabled = uiState.isShowingResultScreen || isShowingBrandScreen) {
                if (isShowingBrandScreen) {
                    isShowingBrandScreen = false
                } else if (uiState.isShowingResultScreen) {
                    viewModel.navigateBackToHome()
                }
            }

            ManulaSocialAITheme(themeMode = uiState.themeMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (!uiState.isShowingResultScreen && !isShowingBrandScreen) {
                            AppTopBar(
                                activeBrandName = uiState.activeBrandProfile?.name,
                                onSettingsClick = { viewModel.setNavTab(NavigationTab.SETTINGS) },
                                onBrandClick = { isShowingBrandScreen = true }
                            )
                        }
                    },
                    bottomBar = {
                        if (!uiState.isShowingResultScreen && !isShowingBrandScreen) {
                            AppBottomNavBar(
                                selectedTab = uiState.currentNavTab,
                                onTabSelected = { tab ->
                                    viewModel.setNavTab(tab)
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when {
                            isShowingBrandScreen -> {
                                BrandProfileScreen(
                                    viewModel = viewModel,
                                    onBack = { isShowingBrandScreen = false }
                                )
                            }
                            uiState.isShowingResultScreen -> {
                                ResultScreen(viewModel = viewModel)
                            }
                            else -> {
                                when (uiState.currentNavTab) {
                                    NavigationTab.HOME -> HomeScreen(viewModel = viewModel)
                                    NavigationTab.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                                    NavigationTab.FAVORITES -> FavoritesScreen(viewModel = viewModel)
                                    NavigationTab.SETTINGS -> SettingsScreen(
                                        viewModel = viewModel,
                                        onOpenBrandProfiles = { isShowingBrandScreen = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
