package me.weishu.kernelsu.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
// Import các Destination (cần Rebuild để sinh file)
import me.weishu.kernelsu.ui.screen.destinations.ColorPaletteDestination
import me.weishu.kernelsu.ui.screen.destinations.AppProfileTemplateDestination
import me.weishu.kernelsu.ui.screen.destinations.AboutDestination
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel

@Destination
@Composable
fun SettingPager(
    navigator: DestinationsNavigator // Sử dụng navigator mới
) {
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    val actions = SettingsScreenActions(
        onSetCheckUpdate = viewModel::setCheckUpdate,
        onSetCheckModuleUpdate = viewModel::setCheckModuleUpdate,
        // Chuyển hướng bằng Destination
        onOpenTheme = { navigator.navigate(ColorPaletteDestination) },
        onSetUiModeIndex = { /* Đã loại bỏ */ },
        onOpenProfileTemplate = { navigator.navigate(AppProfileTemplateDestination) },
        onSetSuCompatMode = viewModel::setSuCompatMode,
        onSetKernelUmountEnabled = viewModel::setKernelUmountEnabled,
        onSetSelinuxHideEnabled = viewModel::setSelinuxHideEnabled,
        onSetSulogEnabled = viewModel::setSulogEnabled,
        onSetAdbRootEnabled = viewModel::setAdbRootEnabled,
        onSetDefaultUmountModules = viewModel::setDefaultUmountModules,
        onSetEnableWebDebugging = viewModel::setEnableWebDebugging,
        onSetAutoJailbreak = viewModel::setAutoJailbreak,
        // Chuyển hướng bằng Destination
        onOpenAbout = { navigator.navigate(AboutDestination) },
    )

    SettingPagerMaterial(
        uiState = uiState, 
        actions = actions,
        bottomInnerPadding = 0.dp // Scaffold tự xử lý
    )
}