package me.weishu.kernelsu.ui.component.dialog

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.parcelize.Parcelize
import kotlin.coroutines.resume

private const val TAG = "DialogComponent"

interface ConfirmDialogVisuals : Parcelable {
    val title: String
    val content: String?
    val isMarkdown: Boolean
    val isHtml: Boolean
    val confirm: String?
    val dismiss: String?
}

@Parcelize
private data class ConfirmDialogVisualsImpl(
    override val title: String,
    override val content: String?,
    override val isMarkdown: Boolean,
    override val isHtml: Boolean,
    override val confirm: String?,
    override val dismiss: String?,
) : ConfirmDialogVisuals {
    companion object {
        val Empty: ConfirmDialogVisuals = ConfirmDialogVisualsImpl("", "", isMarkdown = false, isHtml = false, confirm = null, dismiss = null)
    }
}

interface DialogHandle {
    val isShown: Boolean
    val dialogType: String
    fun show()
    fun hide()
}

interface LoadingDialogHandle : DialogHandle {
    suspend fun <R> withLoading(block: suspend () -> R): R
    fun showLoading()
}

sealed interface ConfirmResult {
    object Confirmed : ConfirmResult
    object Canceled : ConfirmResult
}

interface ConfirmDialogHandle : DialogHandle {
    val visuals: ConfirmDialogVisuals

    fun showConfirm(
        title: String,
        content: String? = null,
        markdown: Boolean = false,
        html: Boolean = false,
        confirm: String? = null,
        dismiss: String? = null
    )

    suspend fun awaitConfirm(
        title: String,
        content: String? = null,
        markdown: Boolean = false,
        html: Boolean = false,
        confirm: String? = null,
        dismiss: String? = null
    ): ConfirmResult
}

private abstract class DialogHandleBase(
    val visible: MutableState<Boolean>,
    val coroutineScope: CoroutineScope
) : DialogHandle {
    override val isShown: Boolean
        get() = visible.value

    override fun show() {
        coroutineScope.launch {
            visible.value = true
        }
    }

    final override fun hide() {
        coroutineScope.launch {
            visible.value = false
        }
    }

    override fun toString(): String {
        return dialogType
    }
}

private class LoadingDialogHandleImpl(
    visible: MutableState<Boolean>,
    coroutineScope: CoroutineScope
) : LoadingDialogHandle, DialogHandleBase(visible, coroutineScope) {
    override suspend fun <R> withLoading(block: suspend () -> R): R {
        return coroutineScope.async {
            try {
                visible.value = true
                block()
            } finally {
                visible.value = false
            }
        }.await()
    }

    override fun showLoading() {
        show()
    }

    override val dialogType: String get() = "LoadingDialog"
}

typealias NullableCallback = (() -> Unit)?

interface ConfirmCallback {

    val onConfirm: NullableCallback

    val onDismiss: NullableCallback

    val isEmpty: Boolean get() = onConfirm == null && onDismiss == null

    companion object {
        operator fun invoke(
            onConfirmProvider: () -> NullableCallback,
            onDismissProvider: () -> NullableCallback
        ): ConfirmCallback {
            return object : ConfirmCallback {
                override val onConfirm: NullableCallback
                    get() = onConfirmProvider()