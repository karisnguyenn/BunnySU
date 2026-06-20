package me.weishu.kernelsu.ui.screen.superuser

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.weishu.kernelsu.ui.screen.destinations.AppProfileDestination
import me.weishu.kernelsu.ui.screen.destinations.SulogDestination
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel

@Destination
@Composable
fun SuperUserPager(
    navigator: DestinationsNavigator 
) {
    val viewModel = viewModel<SuperUserViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (uiState.groupedApps.isEmpty()) {
            viewModel.initializePreferences()
            viewModel.loadAppList().join()
        } else if (viewModel.isNeedRefresh) {
            viewModel.loadAppList(resort = false).join()
        }
    }

    val onSearchTextChange: (String) -> Unit = viewModel::updateSearchText
    
    // Chuyển sang dùng navigator.navigate
    val onOpenProfile: (GroupedApps) -> Unit = { group ->
        navigator.navigate(AppProfileDestination(group.uid))
        viewModel.markNeedRefresh()
    }

    val actions = SuperUserActions(
        onRefresh = { viewModel.loadAppList(force = true) },
        onOpenSulog = { navigator.navigate(SulogDestination) },
        onSearchTextChange = onSearchTextChange,
        onSearchStatusChange = viewModel::updateSearchStatus,
        onClearSearch = { onSearchTextChange("") },
        onToggleShowSystemApps = { viewModel.toggleShowSystemApps() },
        onToggleShowOnlyPrimaryUserApps = { viewModel.toggleShowOnlyPrimaryUserApps() },
        onUpdateSortOption = { viewModel.updateSortOption(it) },
        onOpenProfile = onOpenProfile,
    )

    SuperUserPagerMaterial(
        uiState = uiState,
        actions = actions,
        bottomInnerPadding = 0.dp 
    )
}