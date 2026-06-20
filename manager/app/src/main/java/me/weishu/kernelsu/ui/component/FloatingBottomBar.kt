package me.weishu.kernelsu.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun RowScope.FloatingBottomBarItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Tắt hiệu ứng ripple mặc định để giữ phong cách tối giản
                role = Role.Tab,
                onClick = onClick
            )
            .fillMaxHeight()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun FloatingBottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: () -> Int,
    onSelected: (index: Int) -> Unit,
    tabsCount: Int,
    content: @Composable RowScope.() -> Unit
) {
    // Sử dụng màu sắc tiêu chuẩn của Material 3
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val indicatorColor = MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier = modifier
            .shadow(10.dp, CircleShape) // Đổ bóng nổi tiêu chuẩn
            .background(containerColor, CircleShape)
            .height(64.dp)
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BoxWithConstraints {
            val totalWidth = maxWidth
            val tabWidth = totalWidth / tabsCount.coerceAtLeast(1)
            
            val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
            val targetIndex = selectedIndex()
            
            // Animation trượt tiêu chuẩn thay thế cho DampedDragAnimation của miuix
            val animatedOffset by animateFloatAsState(
                targetValue = targetIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "indicatorOffset"
            )

            // Khối nền làm nổi bật Tab đang được chọn (Indicator)
            Box(
                Modifier
                    .offset {
                        val xOffset = animatedOffset * tabWidth.toPx()
                        IntOffset(if (isLtr) xOffset.roundToInt() else -xOffset.roundToInt(), 0)
                    }
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            
            // Chứa các icon/text bên trong
            Row(
                modifier = Modifier.matchParentSize(),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}