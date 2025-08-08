package com.stv.debug;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author hechuan1 on 2020/9/27.
 */
public class ViewDebugger {
    private static final String TAG = "ViewDebugger";
    private static final boolean SEARCH_NON_VIEW_GROUP = true;

    @NonNull
    private final IDebugView mDebugView;
    private boolean mEnabled;
    @NonNull
    private final List<View> mViewsAtCursor = new ArrayList<>();
    @NonNull
    private final List<Point> mPointOffsetViewGroup = new ArrayList<>();
    private boolean mIsShowingCurrentView = false;
    private int mIndexOfShowingViews;
    private boolean mIsShowingFocusedView = false;

    @NonNull
    private int[] mSecretKeyCodes_1 = {KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_CHANNEL_DOWN,
            KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_CHANNEL_UP};
    @NonNull
    private int[] mSecretKeyCodes_2 = {KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_7};
    @Nullable
    private int[] mCurrentActiveSecretKeyCodes;
    private int mSecretKeyIndex = 0;

    @NonNull
    private final Integer[] mDirectionKeyCodes = {KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT};
    private final List<Integer> mDirectionKeyCodesList = Arrays.asList(mDirectionKeyCodes);
    @NonNull
    private final int[] mMovingSteps = {1, 5, 10, 20, 50, 100};
    private int mMovingStep=10;
    @Nullable
    private View mCurrentShowingView;

    public ViewDebugger(@NonNull IDebugView debugView) {
        mDebugView = debugView;
    }

    public boolean handleKey(@NonNull KeyEvent keyEvent) {
        if (mEnabled) {
            handleDebugKey(keyEvent);
            return true;
        } else {
            handleEnableKey(keyEvent);
            return false;
        }
    }

    private void handleEnableKey(@NonNull KeyEvent keyEvent) {
        if (keyEvent.getAction() != KeyEvent.ACTION_UP) {
            return;
        }

        int keyCode = keyEvent.getKeyCode();
        if (mSecretKeyIndex == 0) {
            if (keyCode == mSecretKeyCodes_1[mSecretKeyIndex]) {
                mCurrentActiveSecretKeyCodes = mSecretKeyCodes_1;
            } else if (keyCode == mSecretKeyCodes_2[mSecretKeyIndex]) {
                mCurrentActiveSecretKeyCodes = mSecretKeyCodes_2;
            }
        }

        if (mCurrentActiveSecretKeyCodes != null) {
            if (keyCode == mCurrentActiveSecretKeyCodes[mSecretKeyIndex]) {
                mSecretKeyIndex++;
                if (mSecretKeyIndex >= mCurrentActiveSecretKeyCodes.length) {
                    enableDebugTool(true);

                    mSecretKeyIndex = 0;
                    mCurrentActiveSecretKeyCodes = null;
                }
            } else {
                mCurrentActiveSecretKeyCodes = null;
                // 如果不匹配的是序列的非首个键值, 则从序列的第一个键值开始重新匹配.
                if (mSecretKeyIndex > 0) {
                    mSecretKeyIndex = 0;
                    handleEnableKey(keyEvent);
                }
            }
        }
    }

    private void handleDebugKey(@NonNull KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        int keyAction = keyEvent.getAction();

        if (mDirectionKeyCodesList.contains(keyCode)) {
            if (mIsShowingCurrentView) {
                if (keyAction == KeyEvent.ACTION_DOWN) {
                    showNextFocusedView(keyCode);
                } else if (keyAction == KeyEvent.ACTION_UP) {
                    hideNextFocusedView();
                }
            } else {
                if (keyAction != KeyEvent.ACTION_UP) {
                    moveCursorByDirectionKey(keyCode);
                }
            }
        } else if (keyAction == KeyEvent.ACTION_UP) {
            if (keyCode >= KeyEvent.KEYCODE_1 && keyCode < KeyEvent.KEYCODE_1 + mMovingSteps.length) {
                mMovingStep = mMovingSteps[keyCode - KeyEvent.KEYCODE_1];
            } else {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        showViewAtCursor();
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        if (mIsShowingCurrentView) {
                            mIsShowingCurrentView = false;
                            mCurrentShowingView = null;
                            mDebugView.hightlightView(null, "");
                        } else {
                            enableDebugTool(false);
                        }
                        break;
                    case KeyEvent.KEYCODE_0:
                        mDebugView.moveToOrigin();
                        break;
                }
            }
        }
    }

    private void showNextFocusedView(int keyCode) {
        if (mIsShowingFocusedView) {
            return;
        }

        mIsShowingFocusedView = true;
        if (mCurrentShowingView != null) {
            int direction = key2Direction(keyCode);
            if (direction != -1) {
                View nextFocusedView = mCurrentShowingView.focusSearch(direction);
                Log.d(TAG, "Next focused view on direction " + direction2Str(direction) + ": " + nextFocusedView);
                showView(nextFocusedView, false);
            }
        }
    }

    private void hideNextFocusedView() {
        mDebugView.hightlightFocusedView(null, "");
        mIsShowingFocusedView = false;
    }

    private int key2Direction(int keyCode) {
        int direction = -1;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                direction = View.FOCUS_UP;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                direction = View.FOCUS_DOWN;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                direction = View.FOCUS_LEFT;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                direction = View.FOCUS_RIGHT;
                break;
        }

        return direction;
    }

    private String direction2Str(int direction) {
        String directionStr = "";
        switch (direction) {
            case View.FOCUS_LEFT:
                directionStr = "LEFT";
                break;
            case View.FOCUS_RIGHT:
                directionStr = "RIGHT";
                break;
            case View.FOCUS_UP:
                directionStr = "UP";
                break;
            case View.FOCUS_DOWN:
                directionStr = "DOWN";
                break;
        }

        return directionStr;
    }

    private void moveCursorByDirectionKey(int keyCode) {
        Pair<Integer, Integer> xyOffset = key2Offset(keyCode);
        if (xyOffset.first != 0 || xyOffset.second != 0) {
            mDebugView.moveCursor(xyOffset.first, xyOffset.second);
//                mDebugView.showViewInfo(null, null);
            mIsShowingCurrentView = false;
        }
    }

    private Pair<Integer, Integer> key2Offset(int keyCode) {
        int xOff = 0, yOff = 0;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                xOff = 0;
                yOff = -mMovingStep;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                xOff = 0;
                yOff = mMovingStep;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                xOff = -mMovingStep;
                yOff = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                xOff = mMovingStep;
                yOff = 0;
                break;
        }
        return new Pair<>(xOff, yOff);
    }

    private void enableDebugTool(boolean enabled) {
        mEnabled = enabled;
        mDebugView.getView().setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }

    private void showViewAtCursor() {
        if (!mIsShowingCurrentView) {
            if (SEARCH_NON_VIEW_GROUP) {
                getAllViewsAtPoint2(mDebugView.getCursorLocation());
            } else {
                getAllViewsAtPoint(mDebugView.getCursorLocation());
            }
            dumpViewsAtCursor();

            if (!mViewsAtCursor.isEmpty()) {
                mIndexOfShowingViews = mViewsAtCursor.size() - 1;
                mIsShowingCurrentView = true;
            }
        }

        if (mIsShowingCurrentView) {
            //NOTE: mViewsAtCursor列表中的元素是在getAllViewsAtPoint()方法中加入的, getAllViewsAtPoint()保证了元素非空.
            //noinspection ConstantConditions
            mCurrentShowingView = mViewsAtCursor.get(mIndexOfShowingViews);
            showView(mCurrentShowingView, true);
            Log.d(TAG, "Current view: " + mCurrentShowingView +
                    ", view rect of screen: " + getViewRectOfScreen(mCurrentShowingView));

            if (mIndexOfShowingViews > 0) {
                mIndexOfShowingViews--;
            } else {
                mIndexOfShowingViews = mViewsAtCursor.size() - 1;
            }
        }
    }

    private void showView(@Nullable View view, boolean isCurrentView) {
        if (view == null) {
            return;
        }

        Rect rectRelative = new Rect(0, 0, view.getRight()-view.getLeft(), view.getBottom()-view.getTop());
        Rect rectScreen = getViewRectOfScreen(view);

        String viewInfo = view.getClass().getSimpleName()
                + viewId2String(view, view.getId())
                + ", " + rectRelative
                + ", " + rectScreen;

        if (isCurrentView) {
            mDebugView.hightlightView(rectScreen, viewInfo);
        } else {
            mDebugView.hightlightFocusedView(rectScreen, viewInfo);
        }
    }

    private Rect getViewRectOfScreen(View view) {
        Rect rectRelative = new Rect(0, 0, view.getRight()-view.getLeft(), view.getBottom()-view.getTop());
        Rect rectScreen = new Rect(rectRelative);

        rectRelative.offset(view.getLeft(), view.getTop());
        int[] viewScreenPos = new int[2];
        view.getLocationOnScreen(viewScreenPos);
        rectScreen.offset(viewScreenPos[0], viewScreenPos[1]);

        return rectScreen;
    }

    private String viewId2String(@NonNull View view, int id) {

        StringBuilder out = new StringBuilder(32);

        if (id != View.NO_ID) {
            final Resources r = view.getResources();
            if (id > 0 && (id >>> 24) != 0 && r != null) {
                try {
                    String pkgname;
                    switch (id&0xff000000) {
                        case 0x7f000000:
                            pkgname="app";
                            break;
                        case 0x01000000:
                            pkgname="android";
                            break;
                        default:
                            pkgname = r.getResourcePackageName(id);
                            break;
                    }
                    String typename = r.getResourceTypeName(id);
                    String entryname = r.getResourceEntryName(id);
                    out.append(" ");
                    out.append(typename);
                    out.append("/");
                    out.append(entryname);
                } catch (Resources.NotFoundException e) {
                }
            }
        }

        return out.toString();
    }

    private void dumpViewsAtCursor() {
        Log.d(TAG, "View hierarchy at cursor:");
        for (View view : mViewsAtCursor) {
            Log.d(TAG, "    " + view);
        }
    }

    private void getAllViewsAtPoint(@NonNull Point pointScreen) {
        // mDebugView放在View Tree的最上一层，因此其parent View一定是root view
        ViewParent viewParent = mDebugView.getView().getParent();
        if (viewParent == null) {
            return;
        }

        ViewGroup rootView = (ViewGroup) viewParent;
        ViewGroup nextViewGroup = rootView;
        mViewsAtCursor.clear();

        Point pointOffsetViewGroup = pointScreen;
        while (true) {
            int childCount = nextViewGroup.getChildCount();
            View viewFound = null;

            // 没有设置elevation属性的话, View的绘制顺序是按照添加到ViewGroup的顺序来决定的.
            // 第一个子View是最先绘制的, 最后一个子View最后绘制, 最后一个子View在图层的最上面.
            for (int i = childCount-1; i >= 0; i--) {
                View child = nextViewGroup.getChildAt(i);
                if (child == null || child instanceof IDebugView) continue;

                if (child.getVisibility()==View.VISIBLE && isPointInView(pointOffsetViewGroup, nextViewGroup, child)) {
                    mViewsAtCursor.add(child);
                    viewFound = child;
                    break;
                }
            }

            if (viewFound != null && viewFound instanceof ViewGroup) {
                transformPointToViewLocal(pointOffsetViewGroup, nextViewGroup, viewFound);
                nextViewGroup = ((ViewGroup) viewFound);
            } else {
                break;
            }
        }
    }

    // 这个方法和上面的getAllViewsAtPoint()方法区别是: 搜索到的最下层View不能是ViewGroup, 否则要继续搜索和该View同一级的下一个子View.
    private void getAllViewsAtPoint2(@NonNull Point pointScreen) {
        // mDebugView放在View Tree的最上一层，因此其parent View一定是root view
        ViewParent viewParent = mDebugView.getView().getParent();
        if (viewParent == null) {
            return;
        }

        ViewGroup rootView = (ViewGroup) viewParent;
        ViewGroup nextViewGroup = rootView;
        mViewsAtCursor.clear();
        mPointOffsetViewGroup.clear();

        Point pointOffsetViewGroup = pointScreen;
        int startChildIndex = nextViewGroup.getChildCount() - 1;
        while (true) {
            View viewFound = null;

            // 如果没有设置elevation属性, View的绘制顺序是按照添加到ViewGroup的顺序来决定的.
            // 第一个子View是最先绘制的, 最后一个子View最后绘制, 最后一个子View在图层的最上面.
            for (int i = startChildIndex; i >= 0; i--) {
                View child = nextViewGroup.getChildAt(i);
                if (child == null || child instanceof IDebugView) continue;

                if (child.getVisibility()==View.VISIBLE && isPointInView(pointOffsetViewGroup, nextViewGroup, child)) {
                    mViewsAtCursor.add(child);
                    mPointOffsetViewGroup.add(new Point(pointOffsetViewGroup));
                    viewFound = child;
                    break;
                }
            }

            if (viewFound != null && viewFound instanceof ViewGroup) {
                transformPointToViewLocal(pointOffsetViewGroup, nextViewGroup, viewFound);
                nextViewGroup = (ViewGroup) viewFound;
                startChildIndex = nextViewGroup.getChildCount() - 1;
            } else if (viewFound == null) {
                int viewCount = mViewsAtCursor.size();
                if (viewCount >= 2) {
                    nextViewGroup = (ViewGroup) mViewsAtCursor.get(viewCount - 2);
                    startChildIndex = findNextChildViewIndex(mViewsAtCursor.get(viewCount - 1), nextViewGroup);
                    Log.w(TAG, "!! Try next child view of " + nextViewGroup + ", startChildIndex=" + startChildIndex);
                    if (startChildIndex < 0) {
                        break;
                    }
                    mViewsAtCursor.remove(viewCount - 1);
                    pointOffsetViewGroup = new Point(mPointOffsetViewGroup.remove(viewCount - 1));
                }
            } else {
                // 已找到非ViewGroup的View, 停止搜索
                break;
            }
        }
    }

    private int findNextChildViewIndex(View currentChildView, ViewGroup parentView) {
        int childIndex = -1;

        for (int i = parentView.getChildCount() - 1; i >= 0; i--) {
            if (parentView.getChildAt(i) == currentChildView) {
                childIndex = i - 1;
                break;
            }
        }

        return childIndex;
    }

    private void transformPointToViewLocal(@NonNull Point point, @NonNull View parent, @NonNull View child) {
        point.offset(parent.getScrollX() - child.getLeft(), parent.getScrollY() - child.getTop());
    }

    private boolean isPointInView(@NonNull Point pointOffParentView, @NonNull View parentView, @NonNull View view) {
        Point pointOffChildView = new Point(pointOffParentView);
        transformPointToViewLocal(pointOffChildView, parentView, view);

        return pointOffChildView.x >= 0 && pointOffChildView.y >= 0
                && pointOffChildView.x < view.getRight() - view.getLeft()
                && pointOffChildView.y < view.getBottom() - view.getTop();
    }
}
