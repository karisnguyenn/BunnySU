package me.weishu.kernelsu.ui.screen.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.weishu.kernelsu.ui.screen.destinations.InstallScreenDestination
import me.weishu.kernelsu.ui.screen.destinations.SuperUserScreenDestination 
import me.weishu.kernelsu.ui.screen.destinations.ModuleScreenDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.R
import me.weishu.kernelsu.magica.MagicaService
import me.weishu.kernelsu.ui.component.dialog.rememberLoadingDialog
import me.weishu.kernelsu.ui.viewmodel.HomeViewModel

@Destination<RootGraph>(start = true)
@Composable
fun HomePager(
    navigator: DestinationsNavigator 
) {
    val viewModel = viewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val loadingDialog = rememberLoadingDialog()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val actions = HomeActions(
        onInstallClick = { navigator.navigate(InstallScreenDestination) },
        onSuperuserClick = { if (!uiState.showRequireKernelWarning) navigator.navigate(SuperUserScreenDestination) },
        onModuleClick = { if (!uiState.showRequireKernelWarning) navigator.navigate(ModuleScreenDestination) },
        
        onOpenUrl = uriHandler::openUri,
        onJailbreakClick = {
            loadingDialog.showLoading()
            context.startService(Intent(context, MagicaService::class.java))
            scope.launch(Dispatchers.IO) {
                delay(30_000)
                withContext(Dispatchers.Main) {
                    loadingDialog.hide()
                    Toast.makeText(context, R.string.jailbreak_timeout, Toast.LENGTH_LONG).show()
                }
            }
        },
    )

    HomePagerMaterial(
        state = uiState,
        actions = actions,
        bottomInnerPadding = 0.dp 
    )
}