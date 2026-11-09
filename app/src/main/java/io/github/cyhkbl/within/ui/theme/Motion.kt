package io.github.cyhkbl.within.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * M3 Expressive 的动效层：弹性回弹 + 快进慢出。
 * 官方那套 spring 在组件里内置，这里在自定义转场和手势动画里显式使用。
 */
object Motion {

    /** 强调动效：轻微过冲，用在弹窗、FAB、选中态。 */
    fun <T> emphasized(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.72f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** 空间动效：位移类。 */
    fun <T> spatial(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessLow
    )

    /** 速度动效：颜色、透明度这类，短促。 */
    fun <T> effects(durationMillis: Int = 180): FiniteAnimationSpec<T> = tween(durationMillis)

    val emphasizedOffset: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.78f,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset(2, 2)
    )

    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
