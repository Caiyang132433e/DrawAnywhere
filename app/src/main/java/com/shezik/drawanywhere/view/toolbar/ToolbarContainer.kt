package com.shezik.drawanywhere.view.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView

/**
 * WindowManager root for the floating toolbar.
 *
 * A ComposeView inside a WRAP_CONTENT overlay window is measured with the window's
 * current width as its maximum. When the toolbar expands, the content therefore cannot
 * request a larger width and the window never grows (the right side gets clipped).
 *
 * This container measures its ComposeView with the display size as an upper bound and
 * reports the child's real size to WindowManager, so the window can grow/shrink with the
 * toolbar content instead of clipping it.
 */
class ToolbarContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    val composeView: ComposeView = ComposeView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    init {
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        addView(composeView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val metrics = resources.displayMetrics
        val width = MeasureSpec.makeMeasureSpec(metrics.widthPixels, MeasureSpec.AT_MOST)
        val height = MeasureSpec.makeMeasureSpec(metrics.heightPixels, MeasureSpec.AT_MOST)
        composeView.measure(width, height)
        setMeasuredDimension(composeView.measuredWidth, composeView.measuredHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
    }
}
