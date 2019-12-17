package com.chaek.android.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.chaek.android.caterpillarindicator.R
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

open class CaterpillarIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs), View.OnClickListener, OnPageChangeListener {
    private var isRoundRectangleLine = true
    private var isCaterpillar = true
    private var mFootLineColor: Int
    private var mTextSizeNormal: Int
    private var mTextSizeSelected: Int
    private var mTextColorNormal: Int
    private var mTextColorSelected: Int
    private var mFooterLineHeight: Int
    private var mItemLineWidth: Int
    /**
     * item count
     */
    private var mItemCount = 0
    private var textCenterFlag: Int
    private var textFont: Int
    private var mCurrentScroll = 0
    private var mSelectedTab = 0
    private var linePaddingBottom = 0
    private var isClickEvent = false
    private var mTitles: List<TitleInfo>? = null
    private var mViewPager: ViewPager? = null
    /**
     * line paint
     */
    private var mPaintFooterLine: Paint? = null
    /**
     * line RectF
     */
    private var drawLineRect: RectF? = null
    private var startLeft = 0
    private var targetLeft = 0
    private var startRight = 0
    private var targetRight = 0
    private var indicatorLeft = 0
    private var indicatorRight = 0
    private var animator: ValueAnimator? = null
    /**
     * set foot line height
     *
     * @param mFooterLineHeight foot line height (int)
     */
    fun setFooterLineHeight(mFooterLineHeight: Int) {
        this.mFooterLineHeight = dip2px(mFooterLineHeight.toFloat())
        invalidate()
    }

    fun setLinePaddingBottom(paddingBottom: Int) {
        linePaddingBottom = paddingBottom
        invalidate()
    }

    fun setTextCenterFlag(centerFlag: Int) {
        textCenterFlag = centerFlag
        invalidate()
    }

    /**
     * item width
     *
     * @param mItemLineWidth item width(dp)
     */
    fun setItemLineWidth(mItemLineWidth: Int) {
        this.mItemLineWidth = dip2px(mItemLineWidth.toFloat())
        invalidate()
    }

    fun setCaterpillar(caterpillar: Boolean) {
        isCaterpillar = caterpillar
        invalidate()
    }

    /**
     * is round line
     *
     * @param roundRectangleLine true (yes) false (no )
     */
    fun setRoundRectangleLine(roundRectangleLine: Boolean) {
        isRoundRectangleLine = roundRectangleLine
    }

    private fun initDraw() {
        mPaintFooterLine = Paint()
        mPaintFooterLine!!.isAntiAlias = true
        mPaintFooterLine!!.style = Paint.Style.FILL
        drawLineRect = RectF(0f, 0f, 0f, 0f)
    }

    fun setFootLineColor(mFootLineColor: Int) {
        this.mFootLineColor = mFootLineColor
        invalidate()
    }

    /**
     * set text normal size(dp)
     *
     * @param mTextSizeNormal normal text size
     */
    fun setTextSizeNormal(mTextSizeNormal: Int) {
        this.mTextSizeNormal = dip2px(mTextSizeNormal.toFloat())
        updateItemText()
    }

    fun setTextSizeSelected(mTextSizeSelected: Int) {
        this.mTextSizeSelected = dip2px(mTextSizeSelected.toFloat())
        updateItemText()
    }

    fun setTextColorNormal(mTextColorNormal: Int) {
        this.mTextColorNormal = mTextColorNormal
        updateItemText()
    }

    fun setTextColorSelected(mTextColorSelected: Int) {
        this.mTextColorSelected = mTextColorSelected
        updateItemText()
    }

    private fun updateItemText() {
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v is TextView) {
                if (v.isSelected) {
                    v.setTextColor(mTextColorSelected)
                    v.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeSelected.toFloat())
                } else {
                    v.setTextColor(mTextColorNormal)
                    v.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeNormal.toFloat())
                }
            }
        }
    }

    private fun dip2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
        return startValue + (fraction * (endValue - startValue).toFloat()).roundToInt()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val a = width.toFloat() / mViewPager!!.width.toFloat()
        onScrolledPositionOffset(((width + mViewPager!!.pageMargin) * position + positionOffsetPixels * a).toInt())
    }

    override fun onPageSelected(position: Int) {
        onSwitched(position)
    }

    override fun onPageScrollStateChanged(state: Int) {}

    @Synchronized
    fun onSwitched(position: Int) {
        if (mSelectedTab == position) {
            return
        }
        restTextStatus(mSelectedTab, position)
        if (isClickEvent) {
            updatePositionAnimate(mSelectedTab, position)
        }
        mSelectedTab = position
    }

    /**
     * @param startPosition 上一个position
     * @param newPosition   结束的position
     */
    private fun updatePositionAnimate(startPosition: Int, newPosition: Int) {
        if (animator != null && animator!!.isRunning) {
            animator!!.cancel()
        }
        val cursorWidth = width / mItemCount
        mItemLineWidth = if (mItemLineWidth > cursorWidth) cursorWidth else mItemLineWidth
        startLeft = cursorWidth * startPosition + (cursorWidth - mItemLineWidth) / 2
        startRight = startLeft + mItemLineWidth
        targetLeft = cursorWidth * newPosition + (cursorWidth - mItemLineWidth) / 2
        targetRight = targetLeft + mItemLineWidth
        animator = ValueAnimator.ofFloat(0.0f, 1.0f)
        animator?.interpolator = FastOutSlowInInterpolator()
        animator?.duration = 300
        animator?.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            setIndicatorPosition(lerp(startLeft, targetLeft, fraction), lerp(startRight, targetRight, fraction))
        }
        animator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator: Animator) {
                isClickEvent = false
            }
        })
        animator?.start()
    }

    private fun setIndicatorPosition(left: Int, right: Int) {
        indicatorLeft = left
        indicatorRight = right
        ViewCompat.postInvalidateOnAnimation(this)
    }

    private fun onScrolledPositionOffset(offset: Int) {
        if (isClickEvent) {
            return
        }
        mCurrentScroll = offset
        val scrollX: Float
        val cursorWidth: Int
        if (mItemCount != 0) {
            cursorWidth = width / mItemCount
            scrollX = (mCurrentScroll - mSelectedTab * width) / mItemCount.toFloat()
        } else {
            cursorWidth = width
            scrollX = mCurrentScroll.toFloat()
        }
        mItemLineWidth = if (mItemLineWidth > cursorWidth) cursorWidth else mItemLineWidth
        val mItemLeft: Int
        val mItemRight: Int
        if (mItemLineWidth < cursorWidth) {
            mItemLeft = (cursorWidth - mItemLineWidth) / 2
            mItemRight = cursorWidth - mItemLeft
        } else {
            mItemLeft = 0
            mItemRight = cursorWidth
        }
        var leftX = 0
        var rightX = 0
        val isHalf = abs(scrollX) < cursorWidth / 2
        if (isCaterpillar) {
            if (scrollX < 0) {
                if (isHalf) {
                    leftX = (mSelectedTab * cursorWidth + scrollX * 2 + mItemLeft).toInt()
                    rightX = mSelectedTab * cursorWidth + mItemRight
                } else { //点击
                    leftX = (mSelectedTab - 1) * cursorWidth + mItemLeft
                    rightX = (mSelectedTab * cursorWidth + mItemRight + (scrollX + cursorWidth / 2) * 2).toInt()
                }
            } else if (scrollX > 0) {
                if (isHalf) {
                    leftX = mSelectedTab * cursorWidth + mItemLeft
                    rightX = (mSelectedTab * cursorWidth + mItemRight + scrollX * 2).toInt()
                } else {
                    leftX = (mSelectedTab * cursorWidth + mItemLeft + (scrollX - cursorWidth / 2) * 2).toInt()
                    rightX = (mSelectedTab + 1) * cursorWidth + mItemRight
                }
            } else {
                leftX = mSelectedTab * cursorWidth + mItemLeft
                rightX = mSelectedTab * cursorWidth + mItemRight
            }
        } else {
            leftX = (mSelectedTab * cursorWidth + scrollX + mItemLeft).toInt()
            rightX = (mSelectedTab * cursorWidth + scrollX + mItemRight).toInt()
        }

        setIndicatorPosition(leftX, rightX)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaintFooterLine!!.color = mFootLineColor
        val bottomY = height - paddingBottom - linePaddingBottom.toFloat()
        //set foot line height
        val topY = bottomY - mFooterLineHeight
        drawLineRect!!.left = indicatorLeft.toFloat()
        drawLineRect!!.right = indicatorRight.toFloat()
        drawLineRect!!.bottom = bottomY
        drawLineRect!!.top = topY
        val roundXY = if (isRoundRectangleLine) mFooterLineHeight / 2 else 0
        canvas.drawRoundRect(drawLineRect!!, roundXY.toFloat(), roundXY.toFloat(), mPaintFooterLine!!)
    }

    /**
     * init indication
     *
     * @param startPosition init select pos
     * @param tabs          title list
     * @param mViewPager    ViewPage
     */
    fun init(startPosition: Int, tabs: List<TitleInfo>, mViewPager: ViewPager) {
        removeAllViews()
        mSelectedTab = startPosition
        this.mViewPager = mViewPager
        this.mViewPager!!.addOnPageChangeListener(this)
        mTitles = tabs
        mItemCount = tabs.size
        weightSum = mItemCount.toFloat()
        if (mSelectedTab > tabs.size) {
            mSelectedTab = tabs.size
        }
        for (i in 0 until mItemCount) {
            add(tabs[i].name, i)
        }
        mViewPager.currentItem = mSelectedTab
        invalidate()
        requestLayout()
    }

    fun initTitle(mViewPager: ViewPager, list: List<String?>) {
        val len = list.size
        val tabs: MutableList<TitleInfo> = ArrayList()
        if (len > 0) {
            for (aList in list) {
                tabs.add(TitleInfo(aList))
            }
        }
        init(0, tabs, mViewPager)
    }

    fun initTitle(mViewPager: ViewPager, vararg list: String?) {
        val len = list.size
        val tabs: MutableList<TitleInfo> = ArrayList()
        if (len > 0) {
            for (aList in list) {
                tabs.add(TitleInfo(aList))
            }
        }
        init(mViewPager.currentItem, tabs, mViewPager)
    }

    fun initTitle(mViewPager: ViewPager, @ArrayRes titleList: Int) {
        val list = resources.getStringArray(titleList)
        val len = list.size
        val tabs: MutableList<TitleInfo> = ArrayList()
        if (len > 0) {
            for (aList in list) {
                tabs.add(TitleInfo(aList))
            }
        }
        init(mViewPager.currentItem, tabs, mViewPager)
    }

    fun initTitle(mViewPager: ViewPager, vararg list: Int) {
        val len = list.size
        val tabs: MutableList<TitleInfo> = ArrayList()
        if (len > 0) {
            for (aList in list) {
                tabs.add(TitleInfo(context.getString(aList)))
            }
        }
        init(mViewPager.currentItem, tabs, mViewPager)
    }

    private fun add(label: String?, position: Int) {
        val text = TextView(context)
        val params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        text.gravity = Gravity.CENTER
        text.layoutParams = params
        if (textCenterFlag == LINE_CENTER) {
            text.setPadding(0, 0, 0, linePaddingBottom)
        }
        if (textFont == FONT_BOLD) {
            text.typeface = Typeface.DEFAULT_BOLD
        }
        text.text = label
        setTabTextSize(text, position == mSelectedTab)
        text.id = BASE_ID + position
        text.setOnClickListener(this)
        addView(text)
    }

    override fun onClick(v: View) {
        val position = v.id - BASE_ID
        isClickEvent = true
        updatePositionAnimate(mSelectedTab, position)
        setCurrentTab(position)
        mViewPager!!.currentItem = position
    }

    /**
     * get title list size
     *
     * @return list size
     */
    private val titleCount: Int
        get() = if (mTitles != null) mTitles!!.size else 0

    private fun restTextStatus(oldPosition: Int, newPosition: Int) {
        if (oldPosition == newPosition) {
            return
        }
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            if (v is TextView) {
                setTabTextSize(v, i == newPosition)
                v.isSelected = i == newPosition
            }
        }
    }

    @Synchronized
    fun setCurrentTab(index: Int) {
        if (index < 0 || index >= titleCount) {
            return
        }
        restTextStatus(mSelectedTab, index)
        mSelectedTab = index
    }

    /**
     * set select textView textSize&textColor state
     *
     * @param tab      TextView
     * @param selected is Select
     */
    private fun setTabTextSize(tab: View, selected: Boolean) {
        if (tab is TextView) {
            tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, if (selected) mTextSizeSelected.toFloat() else mTextSizeNormal.toFloat())
            tab.setTextColor(if (selected) mTextColorSelected else mTextColorNormal)
            tab.typeface = if (textFont == FONT_NORMAL || textFont == FONT_SELECT_BOLD && !selected) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (mCurrentScroll == 0 && mSelectedTab != 0) {
            mCurrentScroll = (width + mViewPager!!.pageMargin) * mSelectedTab
        }
    }

    /**
     * title
     */
    class TitleInfo(var name: String?)

    companion object {
        private const val TAG = "CaterpillarIndicator"
        private const val BASE_ID = 0xffff00
        private const val FOOTER_COLOR = -0x3bbb
        private const val ITEM_TEXT_COLOR_NORMAL = -0x666667
        private const val ITEM_TEXT_COLOR_SELECT = -0x3bbb
        private const val TEXT_CENTER = 0
        private const val LINE_CENTER = 1

        private const val FONT_NORMAL = 0
        private const val FONT_SELECT_BOLD = 1
        private const val FONT_BOLD = 2
    }

    init {
        isFocusable = true
        val a = context.obtainStyledAttributes(attrs, R.styleable.CaterpillarIndicator)
        mFootLineColor = a.getColor(R.styleable.CaterpillarIndicator_slide_footer_color, FOOTER_COLOR)
        mTextSizeNormal = a.getDimensionPixelSize(R.styleable.CaterpillarIndicator_slide_text_size_normal, dip2px(12f))
        mTextSizeSelected = a.getDimensionPixelSize(R.styleable.CaterpillarIndicator_slide_text_size_selected, dip2px(mTextSizeNormal.toFloat()))
        mFooterLineHeight = a.getDimensionPixelOffset(R.styleable.CaterpillarIndicator_slide_footer_line_height, dip2px(3f))
        mTextColorSelected = a.getColor(R.styleable.CaterpillarIndicator_slide_text_color_selected, ITEM_TEXT_COLOR_SELECT)
        mTextColorNormal = a.getColor(R.styleable.CaterpillarIndicator_slide_text_color_normal, ITEM_TEXT_COLOR_NORMAL)
        isCaterpillar = a.getBoolean(R.styleable.CaterpillarIndicator_slide_caterpillar, true)
        isRoundRectangleLine = a.getBoolean(R.styleable.CaterpillarIndicator_slide_round, true)
        mItemLineWidth = a.getDimension(R.styleable.CaterpillarIndicator_slide_item_width, dip2px(24f).toFloat()).toInt()
        linePaddingBottom = a.getDimension(R.styleable.CaterpillarIndicator_slide_padding_bottom, 0f).toInt()
        textCenterFlag = a.getInt(R.styleable.CaterpillarIndicator_slide_text_center_flag, TEXT_CENTER)
        textFont = a.getInt(R.styleable.CaterpillarIndicator_slide_text_font, FONT_NORMAL)
        setWillNotDraw(false)
        initDraw()
        a.recycle()
    }
}