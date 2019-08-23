package com.chaek.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ArrayRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chaek.android.caterpillarindicator.R;

import java.util.ArrayList;
import java.util.List;


public class CaterpillarIndicator extends LinearLayout implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private static final String TAG = "CaterpillarIndicator";

    private static final int BASE_ID = 0xffff00;
    private static final int FOOTER_COLOR = 0xFFFFC445;
    private static final int ITEM_TEXT_COLOR_NORMAL = 0xFF999999;
    private static final int ITEM_TEXT_COLOR_SELECT = 0xFFFFC445;
    private static final int TEXT_CENTER = 0;
    private static final int LINE_CENTER = 1;

    private boolean isRoundRectangleLine = true;
    private boolean isCaterpillar = true;
    private int mFootLineColor;
    private int mTextSizeNormal;
    private int mTextSizeSelected;
    private int mTextColorNormal;
    private int mTextColorSelected;
    private int mFooterLineHeight;
    private int mItemLineWidth;
    /**
     * item count
     */
    private int mItemCount = 0;
    private int textCenterFlag;
    private int mCurrentScroll = 0;
    private int mSelectedTab = 0;
    private int linePaddingBottom = 0;
    private boolean isClickEvent;
    private List<TitleInfo> mTitles;
    private ViewPager mViewPager;

    /**
     * indicator line is Rounded Rectangle
     */

    /**
     * line paint
     */
    private Paint mPaintFooterLine;
    /**
     * line RectF
     */
    private RectF drawLineRect;

    private int startLeft;
    private int targetLeft;
    private int startRight;
    private int targetRight;

    private int indicatorLeft;
    private int indicatorRight;


    private ValueAnimator animator;

    public CaterpillarIndicator(Context context) {
        this(context, null);
    }

    public CaterpillarIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CaterpillarIndicator);
        mFootLineColor = a.getColor(R.styleable.CaterpillarIndicator_slide_footer_color, FOOTER_COLOR);
        mTextSizeNormal = a.getDimensionPixelSize(R.styleable.CaterpillarIndicator_slide_text_size_normal, dip2px(mTextSizeNormal));
        mTextSizeSelected = a.getDimensionPixelSize(R.styleable.CaterpillarIndicator_slide_text_size_selected, dip2px(mTextSizeNormal));
        mFooterLineHeight = a.getDimensionPixelOffset(R.styleable.CaterpillarIndicator_slide_footer_line_height, dip2px(3));
        mTextColorSelected = a.getColor(R.styleable.CaterpillarIndicator_slide_text_color_selected, ITEM_TEXT_COLOR_SELECT);
        mTextColorNormal = a.getColor(R.styleable.CaterpillarIndicator_slide_text_color_normal, ITEM_TEXT_COLOR_NORMAL);
        isCaterpillar = a.getBoolean(R.styleable.CaterpillarIndicator_slide_caterpillar, true);
        isRoundRectangleLine = a.getBoolean(R.styleable.CaterpillarIndicator_slide_round, true);
        mItemLineWidth = (int) a.getDimension(R.styleable.CaterpillarIndicator_slide_item_width, dip2px(24));
        linePaddingBottom = (int) a.getDimension(R.styleable.CaterpillarIndicator_slide_padding_bottom, 0);
        textCenterFlag = a.getInt(R.styleable.CaterpillarIndicator_slide_text_center_flag, TEXT_CENTER);

        setWillNotDraw(false);
        initDraw();
        a.recycle();
    }


    /**
     * set foot line height
     *
     * @param mFooterLineHeight foot line height (int)
     */
    public void setFooterLineHeight(int mFooterLineHeight) {
        this.mFooterLineHeight = dip2px(mFooterLineHeight);
        invalidate();
    }

    public void setLinePaddingBottom(int paddingBottom) {
        this.linePaddingBottom = paddingBottom;
        invalidate();
    }

    public void setTextCenterFlag(int centerFlag) {
        this.textCenterFlag = centerFlag;
        invalidate();
    }


    /**
     * item width
     *
     * @param mItemLineWidth item width(dp)
     */
    public void setItemLineWidth(int mItemLineWidth) {
        this.mItemLineWidth = dip2px(mItemLineWidth);
        invalidate();
    }


    public void setCaterpillar(boolean caterpillar) {
        isCaterpillar = caterpillar;
        invalidate();
    }

    /**
     * is round line
     *
     * @param roundRectangleLine true (yes) false (no )
     */
    public void setRoundRectangleLine(boolean roundRectangleLine) {
        isRoundRectangleLine = roundRectangleLine;
    }

    private void initDraw() {
        mPaintFooterLine = new Paint();
        mPaintFooterLine.setAntiAlias(true);
        mPaintFooterLine.setStyle(Paint.Style.FILL);
        drawLineRect = new RectF(0, 0, 0, 0);
    }


    public void setFootLineColor(int mFootLineColor) {
        this.mFootLineColor = mFootLineColor;
        invalidate();
    }


    /**
     * set text normal size(dp)
     *
     * @param mTextSizeNormal normal text size
     */
    public void setTextSizeNormal(int mTextSizeNormal) {
        this.mTextSizeNormal = dip2px(mTextSizeNormal);
        updateItemText();
    }

    public void setTextSizeSelected(int mTextSizeSelected) {
        this.mTextSizeSelected = dip2px(mTextSizeSelected);
        updateItemText();
    }

    public void setTextColorNormal(int mTextColorNormal) {
        this.mTextColorNormal = mTextColorNormal;
        updateItemText();
    }

    public void setTextColorSelected(int mTextColorSelected) {
        this.mTextColorSelected = mTextColorSelected;
        updateItemText();
    }

    private void updateItemText() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                if (tv.isSelected()) {
                    tv.setTextColor(mTextColorSelected);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeSelected);
                } else {
                    tv.setTextColor(mTextColorNormal);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeNormal);
                }
            }
        }

    }


    private int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public int lerp(int startValue, int endValue, float fraction) {
        return startValue + Math.round(fraction * (float) (endValue - startValue));
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        float a = (float) getWidth() / (float) mViewPager.getWidth();
        onScrolledPositionOffset((int) ((getWidth() + mViewPager.getPageMargin()) * position + positionOffsetPixels * a));
    }

    @Override
    public void onPageSelected(int position) {
        onSwitched(position);
    }


    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public synchronized void onSwitched(int position) {
        if (mSelectedTab == position) {
            return;
        }
        restTextStatus(mSelectedTab, position);
        if (isClickEvent) {
            updatePositionAnimate(mSelectedTab, position);
        }
        mSelectedTab = position;
    }


    /**
     * @param startPosition 上一个position
     * @param newPosition   结束的position
     */
    private void updatePositionAnimate(int startPosition, final int newPosition) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        int cursorWidth = getWidth() / mItemCount;
        this.mItemLineWidth = mItemLineWidth > cursorWidth ? cursorWidth : mItemLineWidth;

        startLeft = cursorWidth * startPosition + (cursorWidth - mItemLineWidth) / 2;
        startRight = startLeft + mItemLineWidth;
        targetLeft = cursorWidth * newPosition + (cursorWidth - mItemLineWidth) / 2;
        targetRight = targetLeft + mItemLineWidth;


        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                setIndicatorPosition(lerp(startLeft, targetLeft, fraction), lerp(startRight, targetRight, fraction));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                isClickEvent = false;
            }
        });
        animator.start();

    }

    private void setIndicatorPosition(int left, int right) {
        this.indicatorLeft = left;
        this.indicatorRight = right;
        ViewCompat.postInvalidateOnAnimation(this);
    }


    private void onScrolledPositionOffset(int offset) {
        if (isClickEvent) {
            return;
        }
        mCurrentScroll = offset;
        float scroll_x;
        int cursorWidth;
        if (mItemCount != 0) {
            cursorWidth = getWidth() / mItemCount;
            scroll_x = (mCurrentScroll - ((mSelectedTab) * (getWidth()))) / mItemCount;
        } else {
            cursorWidth = getWidth();
            scroll_x = mCurrentScroll;
        }

        this.mItemLineWidth = mItemLineWidth > cursorWidth ? cursorWidth : mItemLineWidth;
        int mItemLeft;
        int mItemRight;
        if (mItemLineWidth < cursorWidth) {
            mItemLeft = (cursorWidth - mItemLineWidth) / 2;
            mItemRight = cursorWidth - mItemLeft;
        } else {
            mItemLeft = 0;
            mItemRight = cursorWidth;
        }

        int leftX = 0;
        int rightX = 0;
        boolean isHalf = Math.abs(scroll_x) < (cursorWidth / 2);

        if (isCaterpillar) {
            if (scroll_x < 0) {
                if (isHalf) {
                    leftX = (int) ((mSelectedTab) * cursorWidth + scroll_x * 2 + mItemLeft);
                    rightX = (mSelectedTab) * cursorWidth + mItemRight;
                } else {
                    //点击
                    leftX = (mSelectedTab - 1) * cursorWidth + mItemLeft;
                    rightX = (int) ((mSelectedTab) * cursorWidth + mItemRight + (scroll_x + (cursorWidth / 2)) * 2);
                }
            } else if (scroll_x > 0) {
                if (isHalf) {
                    leftX = mSelectedTab * cursorWidth + mItemLeft;
                    rightX = (int) ((mSelectedTab) * cursorWidth + mItemRight + scroll_x * 2);
                } else {
                    leftX = (int) (mSelectedTab * cursorWidth + mItemLeft + (scroll_x - (cursorWidth / 2)) * 2);
                    rightX = (mSelectedTab + 1) * cursorWidth + mItemRight;
                }
            } else {
                leftX = mSelectedTab * cursorWidth + mItemLeft;
                rightX = mSelectedTab * cursorWidth + mItemRight;
            }
        }else {
            leftX = (int) (mSelectedTab * cursorWidth + scroll_x + mItemLeft);
            rightX = (int) ((mSelectedTab) * cursorWidth + scroll_x + mItemRight);
        }
        setIndicatorPosition(leftX, rightX);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaintFooterLine.setColor(mFootLineColor);
        float bottomY = getHeight() - getPaddingBottom() - linePaddingBottom;
        //set foot line height
        float topY = bottomY - mFooterLineHeight;

        drawLineRect.left = indicatorLeft;
        drawLineRect.right = indicatorRight;
        drawLineRect.bottom = bottomY;
        drawLineRect.top = topY;
        int roundXY = isRoundRectangleLine ? (mFooterLineHeight / 2) : 0;
        canvas.drawRoundRect(drawLineRect, roundXY, roundXY, mPaintFooterLine);
    }

    /**
     * init indication
     *
     * @param startPosition init select pos
     * @param tabs          title list
     * @param mViewPager    ViewPage
     */
    public void init(int startPosition, List<TitleInfo> tabs, ViewPager mViewPager) {
        removeAllViews();
        this.mSelectedTab = startPosition;
        this.mViewPager = mViewPager;
        this.mViewPager.addOnPageChangeListener(this);
        this.mTitles = tabs;
        this.mItemCount = tabs.size();
        setWeightSum(mItemCount);
        if (mSelectedTab > tabs.size()) {
            mSelectedTab = tabs.size();
        }
        for (int i = 0; i < mItemCount; i++) {
            add(tabs.get(i).getName(), i);
        }
        mViewPager.setCurrentItem(mSelectedTab);
        invalidate();
        requestLayout();
    }

    public void initTitle(ViewPager mViewPager, List<String> list) {
        int len = list.size();
        List<TitleInfo> tabs = new ArrayList<>();
        if (len > 0) {
            for (String aList : list) {
                tabs.add(new TitleInfo(aList));
            }
        }
        init(0, tabs, mViewPager);
    }

    public void initTitle(ViewPager mViewPager, String... list) {
        int len = list.length;
        List<TitleInfo> tabs = new ArrayList<>();
        if (len > 0) {
            for (String aList : list) {
                tabs.add(new TitleInfo(aList));
            }
        }
        init(mViewPager.getCurrentItem(), tabs, mViewPager);
    }

    public void initTitle(ViewPager mViewPager, @ArrayRes int titleList) {
        String[] list = getResources().getStringArray(titleList);
        int len = list.length;
        List<TitleInfo> tabs = new ArrayList<>();
        if (len > 0) {
            for (String aList : list) {
                tabs.add(new TitleInfo(aList));
            }
        }
        init(mViewPager.getCurrentItem(), tabs, mViewPager);
    }

    public void initTitle(ViewPager mViewPager, int... list) {
        int len = list.length;
        List<TitleInfo> tabs = new ArrayList<>();
        if (len > 0) {
            for (int aList : list) {
                tabs.add(new TitleInfo(getContext().getString(aList)));
            }
        }
        init(mViewPager.getCurrentItem(), tabs, mViewPager);
    }

    protected void add(String label, int position) {
        TextView view = new TextView(getContext());
        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        view.setGravity(Gravity.CENTER);
        view.setLayoutParams(params);
        if (textCenterFlag == LINE_CENTER) {
            view.setPadding(0, 0, 0, linePaddingBottom);
        }
        view.setText(label);
        setTabTextSize(view, position == mSelectedTab);
        view.setId(BASE_ID + position);
        view.setOnClickListener(this);
        addView(view);
    }

    @Override
    public void onClick(View v) {
        int position = v.getId() - BASE_ID;
        isClickEvent = true;
        updatePositionAnimate(mSelectedTab, position);
        setCurrentTab(position);
        mViewPager.setCurrentItem(position);
    }

    /**
     * get title list size
     *
     * @return list size
     */
    public int getTitleCount() {
        return mTitles != null ? mTitles.size() : 0;
    }

    private void restTextStatus(int oldPosition, int newPosition) {
        if (oldPosition == newPosition) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                setTabTextSize(tv, i == newPosition);
                tv.setSelected(i == newPosition);
            }
        }
    }

    public synchronized void setCurrentTab(int index) {
        if (index < 0 || index >= getTitleCount()) {
            return;
        }
        restTextStatus(mSelectedTab, index);
        mSelectedTab = index;
    }

    /**
     * set select textView textSize&textColor state
     *
     * @param tab      TextView
     * @param selected is Select
     */
    private void setTabTextSize(View tab, boolean selected) {
        if (tab instanceof TextView) {
            TextView tv = (TextView) tab;
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, selected ? mTextSizeSelected : mTextSizeNormal);
            tv.setTextColor(selected ? mTextColorSelected : mTextColorNormal);
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mCurrentScroll == 0 && mSelectedTab != 0) {
            mCurrentScroll = (getWidth() + mViewPager.getPageMargin()) * mSelectedTab;
        }
    }


    /**
     * title
     */
    public static class TitleInfo {
        String name;

        public TitleInfo(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
