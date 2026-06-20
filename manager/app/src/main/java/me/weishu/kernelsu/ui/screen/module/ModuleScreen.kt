package me.weishu.kernelsu.ui.screen.module

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.net.toUri
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.screen.destinations.FlashScreenDestination // Cần Rebuild để sinh file
import me.weishu.kernelsu.ui.screen.destinations.ModuleRepoDestination
import me.weishu.kernelsu.ui.screen.destinations.ExecuteModuleActionDestination
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.util.download
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.webui.WebUIActivity

@Destination
@Composable
fun ModulePager(
    navigator: DestinationsNavigator // Sử dụng navigator mới
) {
    val context = LocalContext.current
    val resource = LocalResources.current
    val viewModel = viewModel<ModuleViewModel>()
    val scope = rememberCoroutineScope()
    val rawUiState by viewModel.uiState.collectAsStateWithLifecycle()

    val webUILauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.fetchModuleList(resort = false) }

    // Logic khởi tạo giữ nguyên
    LaunchedEffect(Unit) {
        viewModel.refreshEnvironmentState()
        viewModel.initializePreferences()
        viewModel.fetchModuleList(checkUpdate = true, resort = true)
    }

    val actions = ModuleActions(
        onRefresh = { viewModel.fetchModuleList(checkUpdate = true) },
        onSearchStatusChange = { viewModel.updateSearchStatus(it) },
        onSearchTextChange = { text -> viewModel.updateSearchText(text) },
        onClearSearch = { viewModel.updateSearchText("") },
        onRequestUpdateConfirmation = { module, updateInfo -> viewModel.requestUpdateConfirmation(module, updateInfo) },
        onRequestUninstallConfirmation = { module -> viewModel.requestUninstallConfirmation(module) },
        onDismissConfirmRequest = { viewModel.dismissConfirmRequest() },
        onConfirmUpdate = { request ->
            scope.launch {
                download(
                    url = request.downloadUrl,
                    fileName = request.fileName,
                    onDownloaded = { uri ->
                        // Chuyển sang Destination với tham số
                        navigator.navigate(FlashScreenDestination(FlashIt.FlashModules(listOf(uri))))
                        viewModel.markNeedRefresh()
                    },
                    onDownloading = { /* ... */ },
                )
            }
            viewModel.dismissConfirmRequest()
        },
        // Chuyển sang Destination
        onOpenRepo = { navigator.navigate(ModuleRepoDestination) },
        onToggleSortActionFirst = { viewModel.toggleSortActionFirst() },
        onToggleSortEnabledFirst = { viewModel.toggleSortEnabledFirst() },
        onOpenWebUi = { module ->
            webUILauncher.launch(
                Intent(context, WebUIActivity::class.java)
                    .setData("kernelsu://webui/${module.id}".toUri())
                    .putExtra("id", module.id)
            )
        },
        onToggleModule = { module -> viewModel.toggleModule(module) },
        onUninstallModule = { module -> viewModel.uninstallModule(module) },
        onUndoUninstallModule = { module -> viewModel.undoUninstallModule(module) },
        onOpenFlash = { uris ->
            if (uris.isNotEmpty()) {
                navigator.navigate(FlashScreenDestination(FlashIt.FlashModules(uris)))
                viewModel.markNeedRefresh()
            }
        },
        onExecuteModuleAction = { module ->
            // Truyền tham số id vào Destination
            navigator.navigate(ExecuteModuleActionDestination(module.id))
            viewModel.markNeedRefresh()
        },
    )

    ModulePagerMaterial(
        uiState = rawUiState,
        confirmDialogState = rawUiState.confirmDialogState,
        moduleEvent = viewModel.moduleEvent,
        actions = actions,
        bottomInnerPadding = 0.dp // Scaffold tự xử lý
    )
}