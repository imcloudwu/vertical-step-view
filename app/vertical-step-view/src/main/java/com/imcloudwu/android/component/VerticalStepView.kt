package com.imcloudwu.android.component

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class VerticalStepView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val progressColor = Color.parseColor("#007AFF")

    private val radius = 10f
    private val dotX = radius * 3
    private val textX = dotX + 50f

    private var itemWidth = 200
    private var itemHeight = 150

    private var radiusAnimateValue = 1f
    private var alphaAnimateValue = 255

    private var currentProgress = 0f
    private var targetProgress = 0f

    private var steps = listOf("Step 1", "Step 2", "Step 3")
    private val dots = arrayListOf<Float>()

    private val dotStateManager = DotAnimationStateHandler()
    private val tempBound = Rect()

    private val textPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        typeface = Typeface.DEFAULT_BOLD
        textSize = 50f
        color = Color.GRAY
    }

    private val dotPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    init {
        val types = context.obtainStyledAttributes(attrs, R.styleable.VerticalStepView)
        textPaint.textSize = types.getDimension(R.styleable.VerticalStepView_textSize, 50f)
        textPaint.color = types.getColor(R.styleable.VerticalStepView_textColor, Color.GRAY)
        types.recycle()
    }

    private fun getDotPosition(index: Int): Float? {

        val idx = when {
            index < 0 -> 0
            index > steps.lastIndex -> steps.lastIndex
            else -> index
        }

        return dots.getOrNull(idx)
    }

    override fun onDraw(canvas: Canvas?) {

        canvas?.drawSteps()

        val firstDot = dots.firstOrNull() ?: 0f
        val lastDot = dots.lastOrNull() ?: 0f

        if (currentProgress <= firstDot)
            currentProgress = firstDot

        canvas?.drawLine(dotX, firstDot, dotX, lastDot, linePaint)
        canvas?.drawProgress(firstDot)
    }

    private fun Canvas.drawSteps() {

        dotPaint.color = Color.GRAY
        linePaint.color = Color.GRAY

        dots.clear()

        var y = 0f

        steps.forEach {
            y += itemHeight
            textPaint.getTextBounds(it, 0, it.length, tempBound)
            drawText(it, textX, y, textPaint)
            val centerY = y - tempBound.height() / 2
            drawCircle(dotX, centerY, radius, dotPaint)
            dots.add(centerY)
        }
    }

    private fun Canvas.drawProgress(firstDot: Float) {

        linePaint.color = progressColor
        dotPaint.color = progressColor
        dotPaint.alpha = 255

        drawLine(dotX, firstDot, dotX, currentProgress, linePaint)

        dots.forEach {

            if (it > currentProgress)
                return@forEach

            dotStateManager.pend(it)

            drawCircle(dotX, it, radius, dotPaint)
        }

        dotStateManager.runNext {

            dotPaint.alpha = alphaAnimateValue
            drawCircle(dotX, it, radius * radiusAnimateValue, dotPaint)

            if (!twinkleAnimator.isStarted)
                twinkleAnimator.start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        calculateItemSize()

        val desiredWidth = itemWidth + 100
        val desiredHeight = (steps.size + 1) * itemHeight

        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    private fun calculateItemSize() {

        val longestStr = steps.maxByOrNull { it.length } ?: ""

        textPaint.getTextBounds(longestStr, 0, longestStr.length, tempBound)

        itemWidth = tempBound.width()
        itemHeight = tempBound.height() + textPaint.textSize.toInt()
    }

    fun setSteps(steps: List<String>) {
        this.steps = steps
        invalidate()
    }

    fun move(step: Int) {

        val target = getDotPosition(step) ?: currentProgress

        if (target < currentProgress) {
            reset(target)
            return
        }

        animateMove(currentProgress, target)
    }

    private fun reset(progress: Float) {
        dotStateManager.reset()
        progressAnimator.cancel()
        twinkleAnimator.cancel()
        currentProgress = progress
        targetProgress = progress
        invalidate()
    }

    private fun animateMove(from: Float, to: Float) {

        val animating = progressAnimator.isRunning

        if (from == to)
            return

        if (animating) {
            targetProgress = to
            return
        }

        progressAnimator.duration = dots.filter { it in from..to }.size * 400L
        progressAnimator.setFloatValues(from, to)
        progressAnimator.start()
    }

    private var progressAnimator = ValueAnimator().apply {

        var cancel = false

        interpolator = LinearInterpolator()

        addUpdateListener {
            currentProgress = it.animatedValue as Float
            invalidate()
        }

        addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                cancel = false
            }

            override fun onAnimationCancel(animation: Animator?) {
                cancel = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!cancel && currentProgress < targetProgress)
                    animateMove(currentProgress, targetProgress)
            }
        })
    }

    private val twinkleAnimator = ValueAnimator().apply {

        val radiusValuesHolder = PropertyValuesHolder.ofFloat("radius", 1f, 3f)
        val alphaValuesHolder = PropertyValuesHolder.ofInt("alpha", 255, 0)

        addUpdateListener {
            radiusAnimateValue = it.getAnimatedValue(radiusValuesHolder.propertyName) as Float
            alphaAnimateValue = it.getAnimatedValue(alphaValuesHolder.propertyName) as Int
            invalidate()
        }

        addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                dotStateManager.start()
            }

            override fun onAnimationEnd(animation: Animator?) {
                dotStateManager.finish()
                dotStateManager.runNext { start() }
            }
        })

        setValues(radiusValuesHolder, alphaValuesHolder)
    }

    override fun onDetachedFromWindow() {
        dotStateManager.reset()
        progressAnimator.cancel()
        twinkleAnimator.cancel()
        super.onDetachedFromWindow()
    }
}

private class DotAnimationStateHandler {

    private val completed = mutableSetOf<Float>()
    private val pending = mutableSetOf<Float>()

    val next get() = pending.firstOrNull()
    val hasNext get() = pending.size > 0

    fun pend(item: Float) {
        if (!completed.contains(item))
            pending.add(item)
    }

    fun start() = runNext {
        completed.add(it)
    }

    fun finish() = runNext {
        pending.remove(it)
    }

    fun reset() {
        completed.clear()
        pending.clear()
    }

    inline fun runNext(block: (Float) -> Unit) {
        if (hasNext)
            block(next!!)
    }
}