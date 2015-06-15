package com.concur.mobile.platform.ui.common.widget;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.concur.mobile.platform.ui.common.R;

public class CalendarPicker extends View {

    // ////////////////////////////////////////////
    // COMPLETION LISTENER
    // ////////////////////////////////////////////

    public static interface OnDateChangedListener {

        abstract void onDateChanged(CalendarPicker view, int year, int month, int day);
    }

    // ////////////////////////////////////////////
    // CONSTANTS
    // ////////////////////////////////////////////
    // private final static String LOG_TAG = "CalPick";

    public final static int DEFAULT_BACKGROUND_COLOR = 0xFFFFFFFF;
    public final static int DEFAULT_TEXT_COLOR = 0xFF444444;
    public final static int DEFAULT_GRID_COLOR = 0xFFFFFFFF;
    public final static int DEFAULT_HILITE_COLOR = 0xAA1E67D4;
    public final static int DEFAULT_ARROW_COLOR = 0xAA1E67D4;
    private final static float DEFAULT_STROKE_WIDTH = 1.0f;
    private final static int DEFAULT_PADDING = 0;

    private final static int COLUMN_COUNT = 7;
    private final static int ROW_COUNT = 7; // 6 weeks and a header

    private final static String[] DAY_OF_WEEK = {
            DateUtils.getDayOfWeekString(Calendar.SUNDAY, DateUtils.LENGTH_MEDIUM),
            DateUtils.getDayOfWeekString(Calendar.MONDAY, DateUtils.LENGTH_MEDIUM),
            DateUtils.getDayOfWeekString(Calendar.TUESDAY, DateUtils.LENGTH_MEDIUM),
            DateUtils.getDayOfWeekString(Calendar.WEDNESDAY, DateUtils.LENGTH_MEDIUM),
            DateUtils.getDayOfWeekString(Calendar.THURSDAY, DateUtils.LENGTH_MEDIUM),
            DateUtils.getDayOfWeekString(Calendar.FRIDAY, DateUtils.LENGTH_MEDIUM),
            DateUtils.getDayOfWeekString(Calendar.SATURDAY, DateUtils.LENGTH_MEDIUM), };

    // ////////////////////////////////////////////
    // MEMBERS
    // ////////////////////////////////////////////

    protected class CalendarDayRect {

        public RectF mRect;
        public int mDay;

        public CalendarDayRect(int day, float left, float top, float right, float bottom) {
            mRect = new RectF(left, top, right, bottom);
            mDay = day;
        }

        public CalendarDayRect(CalendarDayRect cdr) {
            mRect = new RectF(cdr.mRect);
            mDay = cdr.mDay;
        }
    }

    protected OnDateChangedListener mDateChangedListener;
    protected GestureDetector mGestureDetector;

    protected ShapeDrawable mLeftTri;
    protected ShapeDrawable mRightTri;

    protected int mArrowColor;
    protected Paint mTextPaint;
    protected Paint mGridPaint;
    protected Paint mHilitePaint;

    protected Calendar mCal;
    protected float[] mGridLines;
    protected CalendarDayRect[] mDayRects;
    protected CalendarDayRect mTouchedDay;

    protected int mPadL;
    protected int mPadR;
    protected int mPadT;
    protected int mPadB;
    protected int mWidth;
    protected int mHeight;
    protected int mLeft;
    protected int mRight;
    protected int mTop;
    protected int mBottom;
    protected float mCellW;
    protected float mCellH;

    protected RectF mHeaderRect;
    protected RectF mLeftArrowRect;
    protected RectF mRightArrowRect;

    // ////////////////////////////////////////////
    // CONSTRUCTION
    // ////////////////////////////////////////////

    public CalendarPicker(Context context) {
        super(context);
        initCalendar();
    }

    public CalendarPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCalendar();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarPicker);

        setTextColor(a.getColor(R.styleable.CalendarPicker_textColor, DEFAULT_TEXT_COLOR));
        setGridColor(a.getColor(R.styleable.CalendarPicker_gridColor, DEFAULT_GRID_COLOR));
        setHighlightColor(a.getColor(R.styleable.CalendarPicker_hiliteColor, DEFAULT_HILITE_COLOR));
        setArrowColor(a.getColor(R.styleable.CalendarPicker_arrowColor, DEFAULT_ARROW_COLOR));

        a.recycle();
    }

    protected final void initCalendar() {

        mArrowColor = DEFAULT_ARROW_COLOR;

        Path leftArrow = new Path();
        leftArrow.moveTo(-(float) Math.sin(30) * 20, 0);
        leftArrow.lineTo(0, 10);
        leftArrow.lineTo(-(float) Math.sin(30) * 20, 20);
        PathShape leftArrowShape = new PathShape(leftArrow, 20, 20);
        mLeftTri = new ShapeDrawable(leftArrowShape);
        mLeftTri.getPaint().setColor(mArrowColor);

        Path rightArrow = new Path();
        rightArrow.lineTo(0, 20);
        rightArrow.lineTo(-(float) Math.sin(30) * 20, 10);
        rightArrow.close();
        rightArrow.close();
        PathShape rightArrowShape = new PathShape(rightArrow, 20, 20);
        mRightTri = new ShapeDrawable(rightArrowShape);
        mRightTri.getPaint().setColor(mArrowColor);

        mGridPaint = new Paint();
        mGridPaint.setColor(DEFAULT_GRID_COLOR);
        mGridPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        mGridPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(DEFAULT_TEXT_COLOR);
        mTextPaint.setTextAlign(Align.CENTER);

        mHilitePaint = new Paint();
        mHilitePaint.setColor(DEFAULT_HILITE_COLOR);
        mHilitePaint.setStyle(Paint.Style.FILL);

        mCal = Calendar.getInstance();

        mGridLines = null;

        mGestureDetector = new GestureDetector(getContext(), new CalendarGestureListener());

        setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
        setPadding(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING);
    }

    // ////////////////////////////////////////////
    // ACCESSORS
    // ////////////////////////////////////////////

    public void init(int year, int month, int day, OnDateChangedListener listener) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(year, month, day);
        init(c, listener);
    }

    public void init(Calendar cal, OnDateChangedListener listener) {
        mCal = cal;
        setOnDateChangedListener(listener);
        invalidate();
    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        mDateChangedListener = listener;
    }

    protected void forwardMonth() {
        mCal.add(Calendar.MONTH, 1);
        mTouchedDay = null;
        invalidate();
    }

    protected void backMonth() {
        mCal.add(Calendar.MONTH, -1);
        mTouchedDay = null;
        invalidate();
    }

    protected void forwardYear() {
        mCal.add(Calendar.YEAR, 1);
        mTouchedDay = null;
        invalidate();
    }

    protected void backYear() {
        mCal.add(Calendar.YEAR, -1);
        mTouchedDay = null;
        invalidate();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public int getTextColor() {
        return mTextPaint.getColor();
    }

    public void setGridColor(int color) {
        mGridPaint.setColor(color);
        invalidate();
    }

    public int getGridColor() {
        return mGridPaint.getColor();
    }

    public void setHighlightColor(int color) {
        mHilitePaint.setColor(color);
        invalidate();
    }

    public int getHiliteColor() {
        return mHilitePaint.getColor();
    }

    public void setArrowColor(int color) {
        mArrowColor = color;
        mLeftTri.getPaint().setColor(mArrowColor);
        mRightTri.getPaint().setColor(mArrowColor);
        invalidate();
    }

    public int getArrowColor() {
        return mArrowColor;
    }

    // ////////////////////////////////////////////
    // USING
    // ////////////////////////////////////////////

    protected class CalendarGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            if (y > mCellH) {

                // In the grid. Go find the day.
                // TODO: This can definitely be optimized to minimize comparisons
                if (mDayRects != null) {
                    // Somehow someone once got to this point without having mDayRects init'd.
                    // If so, just ignore the touch and fall out.
                    final int DAY_COUNT = COLUMN_COUNT * (ROW_COUNT - 1);
                    for (int i = 0; i < DAY_COUNT; i++) {
                        if (mDayRects[i] != null && mDayRects[i].mRect.contains(x, y)) {
                            mTouchedDay = new CalendarDayRect(mDayRects[i]);
                            invalidate(); // TODO: Figure out how to re-draw if we invalidate just the rect
                            break;
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            float x = e.getX();
            float y = e.getY();

            if (mLeftArrowRect.contains(x, y)) {
                backMonth();
            } else if (mRightArrowRect.contains(x, y)) {
                forwardMonth();
            } else {
                if (mTouchedDay != null && mTouchedDay.mRect.contains(x, y)) {
                    if (mDateChangedListener != null) {
                        mDateChangedListener.onDateChanged(CalendarPicker.this, mCal.get(Calendar.YEAR),
                                mCal.get(Calendar.MONTH), mTouchedDay.mDay);
                    }
                }
            }

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {

            float y1 = e1.getY();
            float y2 = e2.getY();

            final float vThreshold = 250.0f;

            if (y1 >= mCellH && y2 >= mCellH) {
                if (vX < -vThreshold) {
                    forwardMonth();
                } else if (vX > vThreshold) {
                    backMonth();
                }
            }

            return true;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Let the gesture detector see everything but we don't care about the return.
        // In fact, we want it to always return true because if the gesture detector returns
        // false it will break the gesture and not properly handle later events.
        mGestureDetector.onTouchEvent(event);

        // Consume the touch
        return true;

    }

    // ////////////////////////////////////////////
    // DRAWING
    // ////////////////////////////////////////////

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = 0;
        int height = 0;

        // int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        // int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        // Width
        width = wSpecSize;
        // if (wSpecMode == MeasureSpec.EXACTLY) {
        // width = wSpecSize;
        // } else {
        // width = (int) ((float) wSpecSize * 0.8f);
        // }

        // Height
        height = hSpecSize;
        // if (hSpecMode == MeasureSpec.EXACTLY) {
        // height = hSpecSize;
        // } else {
        // height = (int) ((float) hSpecSize * 0.8f);
        // }

        setMeasuredDimension(width, height);

        // Clear our grids. It will be recalced on first draw.
        mGridLines = null;

        // Save some frequently used, unchanging, values
        mPadL = getPaddingLeft();
        mPadR = getPaddingRight();
        mPadT = getPaddingTop();
        mPadB = getPaddingBottom();
        mWidth = width;
        mHeight = height;
        mLeft = mPadL;
        mRight = mWidth - mPadR;
        mTop = mPadT;
        mBottom = mHeight - mPadB;

        // Calc our grid spacing
        mCellW = (mRight - mLeft) / (float) COLUMN_COUNT;
        mCellH = (mBottom - mTop) / (float) ROW_COUNT;

        float size = Math.min((mWidth / 5.0f), mTop + mCellH / 2) * 1.5f;
        mLeftArrowRect = new RectF(mLeft, mTop, size, size);
        mRightArrowRect = new RectF(mRight - size, mTop, mRight, size);
        mHeaderRect = new RectF(mLeftArrowRect.right, mTop, mRightArrowRect.left, mCellH);
        mLeftArrowRect.inset(10, 10);
        mRightArrowRect.inset(10, 10);

        Rect r = new Rect();
        mLeftArrowRect.round(r);
        mLeftTri.setBounds(r);
        mRightArrowRect.round(r);
        mRightTri.setBounds(r);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Rect border = new Rect(mLeft, mTop, mRight, mBottom);
        //
        // canvas.drawRect(border, mGridPaint);
        drawHeader(canvas);
        drawGrid(canvas);
        drawDates(canvas);
    }

    protected void drawHeader(Canvas canvas) {

        // Draw arrows
        mLeftTri.draw(canvas);
        mRightTri.draw(canvas);

        float halfCellH = mCellH / 2;

        // TODO: This could use work. Need to find a way to scale the text
        // to only fit in the middle 60% of the width.
        float textSize = halfCellH * 0.7f;
        mTextPaint.setTextSize(textSize);

        // Draw month label
        String monthName = DateFormat.format("MMMM yyyy", mCal).toString();

        // Put the header baseline in the center of the header cell
        // Horizontally center the header in the middle of the full width
        // mTextPaint.setColor(0xFFFF0000);
        // canvas.drawText(monthName, 2 + (mWidth) / 2, 2 + mTop + halfCellH, mTextPaint);
        // mTextPaint.setColor(DEFAULT_TEXT_COLOR);
        mTextPaint.setShadowLayer(1, 2, 2, DEFAULT_TEXT_COLOR & 0x88FFFFFF);
        canvas.drawText(monthName, (mWidth) / 2, mTop + halfCellH, mTextPaint);
        mTextPaint.setShadowLayer(0, 0, 0, DEFAULT_TEXT_COLOR);

        // Draw day labels
        textSize = halfCellH * 0.4f;
        mTextPaint.setTextSize(textSize);

        float vPos = mCellH - 3;
        float halfCellW = mCellW / 2;

        for (int day = 0; day < 7; day++) {
            String dayName = DAY_OF_WEEK[day];

            canvas.drawText(dayName, (mCellW * day) + halfCellW, vPos, mTextPaint);
        }
    }

    protected void drawGrid(Canvas canvas) {

        if (mGridLines == null) {
            mGridLines = new float[((COLUMN_COUNT - 1) + (ROW_COUNT - 1)) * 4]; // vertical lines + horizontal lines * 4 coords
                                                                                // per line

            // Calc our grid spacing
            float gridT = mTop + mCellH; // Top cell is for header

            float pos;
            int idx;

            for (int day = 1; day <= (COLUMN_COUNT - 1); day++) {
                pos = mLeft + (mCellW * day);
                idx = (day - 1) * 4;

                mGridLines[idx + 0] = pos;
                mGridLines[idx + 1] = gridT;
                mGridLines[idx + 2] = pos;
                mGridLines[idx + 3] = mBottom;
            }

            for (int week = 1; week <= (ROW_COUNT - 1); week++) {
                pos = mTop + (mCellH * week);
                idx = 24 + ((week - 1) * 4);

                mGridLines[idx + 0] = mLeft;
                mGridLines[idx + 1] = pos;
                mGridLines[idx + 2] = mRight;
                mGridLines[idx + 3] = pos;
            }
        }

        canvas.drawLines(mGridLines, mGridPaint);
    }

    protected void drawDates(Canvas canvas) {

        Calendar firstDay = (Calendar) mCal.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        int firstWeekday = firstDay.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = mCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        float textSize = (mCellH / 2) * 0.6f;
        mTextPaint.setTextSize(textSize);

        float asc = mTextPaint.ascent();
        float desc = mTextPaint.descent();
        float halfTextH = (-asc + desc) / 2;

        float gridT = mTop + mCellH; // Top cell is for header
        float halfCellW = mCellW / 2;
        float textBaseLine = (mCellH / 2) + halfTextH - 1;

        float hPos;
        float vPos;

        // If a day is touched then highlight it
        if (mTouchedDay != null) {
            canvas.drawRect(mTouchedDay.mRect, mHilitePaint);
        }

        // Clear out the old rects
        // TODO: Way optimize this. The rects should not change unless we get measured again.
        // Find a way to reuse the rects (perhaps populate all rects and use mDay to indicate in-use)
        mDayRects = new CalendarDayRect[COLUMN_COUNT * (ROW_COUNT - 1)];

        Calendar today = Calendar.getInstance();
        boolean isThisMonth = false;
        int thisDay = 0;
        if (today.get(Calendar.YEAR) == mCal.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == mCal.get(Calendar.MONTH)) {
            isThisMonth = true;
            thisDay = mCal.get(Calendar.DAY_OF_MONTH);
        }

        for (int week = 0; week < (ROW_COUNT - 1); week++) {
            for (int day = 0; day < COLUMN_COUNT; day++) {
                int dom = (week * 7) + day - firstWeekday + 2;
                if (dom > 0 && dom <= daysInMonth) {
                    String dayText = Integer.toString(dom);

                    CalendarDayRect dayRect = new CalendarDayRect(dom, mLeft + (day * mCellW), gridT + (week * mCellH),
                            mLeft + (day * mCellW) + mCellW, gridT + (week * mCellH) + mCellH);

                    mDayRects[week * 7 + day] = dayRect;

                    hPos = dayRect.mRect.left + halfCellW;
                    vPos = dayRect.mRect.top + textBaseLine;

                    boolean theDay = isThisMonth && (thisDay == dom);
                    if (theDay) {
                        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
                        mTextPaint.setUnderlineText(true);
                    }

                    canvas.drawText(dayText, hPos, vPos, mTextPaint);

                    if (theDay) {
                        mTextPaint.setTypeface(Typeface.DEFAULT);
                        mTextPaint.setUnderlineText(false);
                    }

                }
            }
        }

    }
}
