package com.cheek.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.chaek.android.R;

import java.util.List;



public class CaterpillarIndicator extends LinearLayout implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private static final String TAG = "SlideViewPageTitleBar";

    private static final int BASE_ID = 0xffff00;
    private static final int FOOTER_COLOR = 0xFFFFC445;
    private static final int ITEM_TEXT_COLOR_NORMAL = 0xFF999999;
    private static final int ITEM_TEXT_COLOR_SELECT = 0xFFFFC445;

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
    private int mCurrentScroll = 0;
    private int mSelectedTab = 0;
    private boolean isNewClick;
    /**
     * item view id index
     */
    private int mCurrID = 0;
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


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        float a = (float) getWidth() / (float) mViewPager.getWidth();
        onScrolled((int) ((getWidth() + mViewPager.getPageMargin()) * position + positionOffsetPixels * a));
    }

    @Override
    public void onPageSelected(int position) {
        onSwitched(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isNewClick) {
            isNewClick = false;
            return;
        }

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

        float leftX;
        float rightX;

        boolean isHalf = Math.abs(scroll_x) < (cursorWidth / 2);
        mPaintFooterLine.setColor(mFootLineColor);
        if (isCaterpillar) {
            if (scroll_x < 0) {
                if (isHalf) {
                    leftX = (mSelectedTab) * cursorWidth + scroll_x * 2 + mItemLeft;
                    rightX = (mSelectedTab) * cursorWidth + mItemRight;
                } else {
                    leftX = (mSelectedTab - 1) * cursorWidth + mItemLeft;
                    rightX = (mSelectedTab) * cursorWidth + mItemRight + (scroll_x + (cursorWidth / 2)) * 2;
                }
            } else if (scroll_x > 0) {
                if (isHalf) {
                    leftX = mSelectedTab * cursorWidth + mItemLeft;
                    rightX = (mSelectedTab) * cursorWidth + mItemRight + scroll_x * 2;
                } else {
                    leftX = mSelectedTab * cursorWidth + mItemLeft + (scroll_x - (cursorWidth / 2)) * 2;
                    rightX = (mSelectedTab + 1) * cursorWidth + mItemRight;
                }

            } else {
                leftX = mSelectedTab * cursorWidth + mItemLeft;
                rightX = mSelectedTab * cursorWidth + mItemRight;
            }
        } else {
            leftX = mSelectedTab * cursorWidth + scroll_x + mItemLeft;
            rightX = (mSelectedTab) * cursorWidth + scroll_x + mItemRight;

        }
        float bottomY = getHeight();
        //set foot line height
        float topY = bottomY - mFooterLineHeight;

        drawLineRect.left = leftX;
        drawLineRect.right = rightX;
        drawLineRect.bottom = bottomY;
        drawLineRect.top = topY;

        int roundXY = isRoundRectangleLine ? (mFooterLineHeight / 2) : 0;
        canvas.drawRoundRect(drawLineRect, roundXY, roundXY, mPaintFooterLine);
    }

    private void onScrolled(int h) {
        mCurrentScroll = h;
        invalidate();
    }

    public synchronized void onSwitched(int position) {
        if (mSelectedTab == position) {
            return;
        }
        setCurrentTab(position);

    }

    /**
     * init indication
     *
     * @param startPos   init select pos
     * @param tabs       title list
     * @param mViewPager ViewPage
     */
    public void init(int startPos, List<TitleInfo> tabs, ViewPager mViewPager) {
        removeAllViews();
        this.mViewPager = mViewPager;
        this.mViewPager.addOnPageChangeListener(this);
        this.mTitles = tabs;
        this.mItemCount = tabs.size();
        setWeightSum(mItemCount);
        for (int i = 0; i < mItemCount; i++) {
            add(tabs.get(i).getName());
        }
        mViewPager.setCurrentItem(startPos);
        invalidate();
        requestLayout();
    }


    protected void add(String label) {
        TextView view = new TextView(getContext());
        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        view.setGravity(Gravity.CENTER);
        view.setLayoutParams(params);
        view.setText(label);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeNormal);
        view.setTextColor(mTextColorNormal);
        view.setId(BASE_ID + (mCurrID++));
        view.setOnClickListener(this);
        addView(view);
    }

    @Override
    public void onClick(View v) {
        int position = v.getId() - BASE_ID;
        isNewClick = true;
        mViewPager.setCurrentItem(position, true);
    }

    /**
     * get title list size
     *
     * @return list size
     */
    public int getTitleCount() {
        return mTitles != null ? mTitles.size() : 0;
    }

    public synchronized void setCurrentTab(int index) {
        if (index < 0 || index >= getTitleCount()) {
            return;
        }
        View oldTab = getChildAt(mSelectedTab);
        if (oldTab != null) {
            oldTab.setSelected(false);
            setTabTextSize(oldTab, false);
        }

        mSelectedTab = index;
        View newTab = getChildAt(mSelectedTab);
        if (newTab != null) {
            setTabTextSize(newTab, true);
            newTab.setSelected(true);
        }


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
