package com.umbrella.morphbutton

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.graphics.withScale
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.math.MathUtils.lerp
import com.umbrella.morphbutton.util.dp
import com.umbrella.morphbutton.util.getColorX
import com.umbrella.morphbutton.util.getDrawableX
import com.umbrella.morphbutton.util.sp

class MorphButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    sealed class UIState {
        object Button : UIState()
        object Loading : UIState()
        object Animating : UIState()
    }

    /////////////////////////// Public State ///////////////////////////
    var textSize: Float = 16 * sp()
    var text: String = ""
        set(value) {
            field = value
            requestLayout()
        }

    var fromBgColor: Int = getColorX(R.color.gray)
    var toBgColor: Int = getColorX(R.color.light_green)
    var fromTextColor: Int = Color.BLACK
    var toTextColor: Int = getColorX(R.color.green)
    var btnRadius = 24 * dp()

    var iconDrawable = getDrawableX(R.drawable.ic_sync)
        set(value) {
            field = value
            setSizeIcon()
        }

    var iconPadding = 16 * dp()
        set(value) {
            field = value
            setSizeIcon()
        }

    private fun setSizeIcon() {
        sizeIcon = iconDrawable.intrinsicWidth + iconPadding
    }

    /////////////////////////// Internal State ///////////////////////////
    private var uiState: UIState = UIState.Button
    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
    }

    private val argbEvaluator = ArgbEvaluator()
    private fun getTextPaint() = paint.apply {
        color = argbEvaluator.evaluate(colorTextFraction, fromTextColor, toTextColor) as Int
        paint.textSize = this@MorphButton.textSize
    }

    private fun getBtnBgPaint() = paint.apply {
        color = argbEvaluator.evaluate(colorBgFraction, fromBgColor, toBgColor) as Int
    }

    private val textBound = Rect()
    private var sizeIcon: Float = 0f

    // fraction
    private var iconDegree: Float = 0f
    private var scaleIconFraction: Float = 0f
    private var scaleTextFraction: Float = 0f
    private var colorBgFraction: Float = 0f
    private var colorTextFraction: Float = 0f
    private var morphFraction: Float = 0f

    init {
        setPadding((24 * dp()).toInt(), (12 * dp()).toInt(), (24 * dp()).toInt(), (12 * dp()).toInt())
        text = "Refresh"
        setSizeIcon()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // calculate the size of button
        getTextPaint().let { paint ->
            paint.getTextBounds(text, 0, text.length, textBound)
            val widthBtn = textBound.width() + paddingStart + paddingEnd
            val heightBtn = textBound.height() + paddingTop + paddingBottom
            setMeasuredDimension(widthBtn, heightBtn)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // calculate the bound of the button
        iconDrawable.let { drawable ->
            val left = w / 2 - drawable.intrinsicWidth / 2
            val top = h / 2 - drawable.intrinsicHeight / 2
            val right = left + drawable.intrinsicWidth
            val bottom = top + drawable.intrinsicHeight
            drawable.setBounds(left, top, right, bottom)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // draw the btn and morph it to circle
        getBtnBgPaint().let { paint ->
            val left = lerp(0f, width / 2f - sizeIcon / 2f, morphFraction)
            val top = lerp(0f, height / 2f - sizeIcon / 2f, morphFraction)
            val right = lerp(width.toFloat(), width / 2f + sizeIcon / 2f, morphFraction)
            val bottom = lerp(height.toFloat(), height.toFloat() / 2f + sizeIcon / 2f, morphFraction)
            val radius = lerp(this.btnRadius, sizeIcon / 2f, morphFraction)
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, paint)
        }
        // scale down the text
        getTextPaint().let { paint ->
            val scaleX = lerp(1f, 0f, scaleTextFraction)
            val scaleY = scaleX
            val pivotX = width / 2f
            val pivotY = height / 2f
            canvas.withScale(scaleX, scaleY, pivotX, pivotY) {
                val xPos = width / 2 - textBound.width() / 2f - textBound.left
                val yPos = height / 2 + textBound.height() / 2f - textBound.bottom
                canvas.drawText(text, xPos, yPos, paint)
            }
        }
        // scale up the icon
        iconDrawable.let { drawable ->
            val scaleX = lerp(0f, 1f, scaleIconFraction)
            val scaleY = scaleX
            val pivotX = width / 2f
            val pivotY = height / 2f
            canvas.withScale(scaleX, scaleY, pivotX, pivotY) {
                // rotate the icon if it appears
                if (scaleIconFraction > 0) {
                    invalidate()
                }
                rotate(iconDegree, pivotX, pivotY)
                // Anticlockwise direction
                val iconSpeed = 6
                iconDegree = (iconDegree - iconSpeed) % 360
                drawable.draw(canvas)
            }
        }
    }

    fun setUIState(uiState: UIState): Boolean {
        if (this.uiState == UIState.Animating || this.uiState == uiState) {
            return false
        }
        val isReverse = uiState != UIState.Loading
        runAnimation(isReverse).apply {
            doOnEnd {
                this@MorphButton.uiState = uiState
            }
        }
        return true
    }

    private fun runAnimation(isReverse: Boolean): AnimatorSet {
        val values = if (isReverse) {
            floatArrayOf(1f, 0f)
        } else {
            floatArrayOf(0f, 1f)
        }
        val animatorList = listOf(
            ValueAnimator.ofFloat(*values).apply {
                addUpdateListener {
                    colorBgFraction = it.animatedValue as Float
                    colorTextFraction = it.animatedValue as Float
                    invalidate()
                }
            },
            ValueAnimator.ofFloat(*values).apply {
                addUpdateListener {
                    scaleTextFraction = it.animatedValue as Float
                    morphFraction = it.animatedValue as Float
                    invalidate()
                }
            },
            ValueAnimator.ofFloat(*values).apply {
                addUpdateListener {
                    scaleIconFraction = it.animatedValue as Float
                    invalidate()
                }
            }
        ).let {
            if (isReverse) {
                it.reversed()
            } else {
                it
            }
        }
        return AnimatorSet().apply {
            playSequentially(
                animatorList
            )
            interpolator = FastOutSlowInInterpolator()
            doOnStart {
                this@MorphButton.uiState = UIState.Animating
            }
            duration = 250L
            start()
        }
    }
}
