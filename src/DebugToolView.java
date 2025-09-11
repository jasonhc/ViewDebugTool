package com.stv.debug;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DebugToolView extends View implements IDebugView {
    private static final int CURSOR_TEXT_SIZE = 40;
    private static final String SYS_PROP_KEY_HIGHLIGHT_COLOR_OF_SELECTED_VIEW = "debug.view_debugger.color";
    private static final String SYS_PROP_KEY_HIGHLIGHT_COLOR_OF_FOCUSED_VIEW = "debug.view_debugger.focus_color";
    private static final long SELECTED_VIEW_DEFAULT_HIGHLIGHT_COLOR = Color.WHITE;
    private static final int FOCUSED_VIEW_DEFAULT_HIGHLIGHT_COLOR = Color.BLUE;

    private int mCursorX, mCursorY;
    @NonNull
    private Rect mViewBounds = new Rect();
    @NonNull
    private String mViewInfo = "";
    @NonNull
    private Rect mFocusedViewBounds = new Rect();
    @NonNull
    private String mFocusedViewInfo = "";

    // 下面3个成员在 init()方法中被调用, 而 init()只被构造方法调用, 因此这3个成员是初始化了
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private TextPaint mTextPaint;
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private Paint mCursorPaint;
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private Paint mViewHighlightPaint;
    private Paint mFocusedViewHighlightPaint;

    private int mRight;
    private int mLeft;
    private int mBottom;
    private int mTop;
    private int mOriginX;
    private int mOriginY;
    private ViewDebugger mViewDebugger;

    public DebugToolView(Context context) {
        super(context);
        init(null, 0);
    }

    private void init(@Nullable AttributeSet attrs, int defStyle) {
        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(Color.WHITE);

        mCursorPaint = new Paint();
        mCursorPaint.setColor(Color.RED);
        mCursorPaint.setStrokeWidth(1.0f);

        mViewHighlightPaint = new Paint();
        mViewHighlightPaint.setColor(Color.WHITE);
        mViewHighlightPaint.setStrokeWidth(1.0f);
        mViewHighlightPaint.setStyle(Paint.Style.STROKE);
        mFocusedViewHighlightPaint = new Paint();
        mFocusedViewHighlightPaint.setColor(FOCUSED_VIEW_DEFAULT_HIGHLIGHT_COLOR);
        mFocusedViewHighlightPaint.setStrokeWidth(1.0f);
        mFocusedViewHighlightPaint.setStyle(Paint.Style.STROKE);
    }

    public DebugToolView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DebugToolView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;

        mOriginX = (mRight - mLeft) / 2;
        mOriginY = (mBottom - mTop) / 2;
        mCursorX=mOriginX;
        mCursorY=mOriginY;
    }

    // Android Framework 传递的参数 canvas 一定是非空
    @Override
    protected void onDraw(@SuppressWarnings("NullableProblems") @NonNull Canvas canvas) {
        super.onDraw(canvas);

        // draw cursor line
        canvas.drawLine(0, mCursorY, getRight(), mCursorY, mCursorPaint);
        canvas.drawLine(mCursorX, 0, mCursorX, getBottom(), mCursorPaint);

        // draw cursor's location (x,y)
        String debugInfo = "(" + mCursorX + ", " + mCursorY + ")";
        // 由于DebugToolView一定是全屏的View, 因此mCursorX, mCursorY就是屏幕坐标, 不需要转换
        Integer pixelColor = mViewDebugger.getRgbOfScreenPixel(mCursorX, mCursorY);
        if (pixelColor != null) {
            debugInfo += String.format("  #%06X", (pixelColor & 0xFFFFFF));
        }

        TextPaint largeTextPaint = new TextPaint(mTextPaint);
        largeTextPaint.setTextSize(CURSOR_TEXT_SIZE);
        canvas.drawText(debugInfo, 0, 50+CURSOR_TEXT_SIZE, largeTextPaint);

        // draw view's info & bound box
        if (!mViewInfo.isEmpty()) {
            canvas.drawText(mViewInfo, mViewBounds.left, mViewBounds.top, mTextPaint);
            mViewHighlightPaint.setColor(getHighlightColorOfSelectedView());
            canvas.drawRect(mViewBounds, mViewHighlightPaint);
        }

        // draw focused view's info & bound box
        if (!mFocusedViewInfo.isEmpty()) {
            canvas.drawText(mFocusedViewInfo, mFocusedViewBounds.left, mFocusedViewBounds.top, mTextPaint);
            mFocusedViewHighlightPaint.setColor(getHighlightColorOfFocusedView());
            canvas.drawRect(mFocusedViewBounds, mFocusedViewHighlightPaint);
        }
    }

    private int getHighlightColorOfSelectedView() {
        return (int) SystemProperties.getLong(SYS_PROP_KEY_HIGHLIGHT_COLOR_OF_SELECTED_VIEW, SELECTED_VIEW_DEFAULT_HIGHLIGHT_COLOR);
    }

    private int getHighlightColorOfFocusedView() {
        return (int) SystemProperties.getLong(SYS_PROP_KEY_HIGHLIGHT_COLOR_OF_FOCUSED_VIEW, FOCUSED_VIEW_DEFAULT_HIGHLIGHT_COLOR);
    }

    @Override
    public void moveCursor(int xOff, int yOff) {
        mCursorX += xOff;
        mCursorY += yOff;
        if (mCursorX < mLeft) {
            mCursorX = mLeft;
        } else if (mCursorX >= mRight) {
            mCursorX = mRight-1;
        }
        if (mCursorY < mTop) {
            mCursorY = mTop;
        } else if (mCursorY >= mBottom) {
            mCursorY = mBottom-1;
        }

        invalidate();
    }

    @Override
    public void moveToOrigin() {
        mCursorX=mOriginX;
        mCursorY=mOriginY;

        invalidate();
    }

    @Override
    public void hightlightView(@Nullable  Rect viewBounds, @NonNull String info) {
        mViewBounds = new Rect(viewBounds);
        mViewInfo = info;

        invalidate();
    }

    @Override
    public void hightlightFocusedView(@Nullable  Rect viewBounds, @NonNull String info) {
        mFocusedViewBounds = new Rect(viewBounds);
        mFocusedViewInfo = info;

        invalidate();
    }

    @Override
    public @NonNull Point getCursorLocation() {
        return new Point(mCursorX, mCursorY);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    void setDebuggerPresenter(ViewDebugger presenter) {
        mViewDebugger = presenter;
    }
}
