package com.stv.debug;

import org.jetbrains.annotations.Nullable;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.stv.deskplatform.BuildConfig;

/**
 * @author hechuan1 on 2021/1/27.
 */
public abstract class ViewDebugToolBaseActivity extends FragmentActivity {
    private static final String TAG = "ViewDebugTool";

    private boolean mIsDebugViewAdded;
    @Nullable
    private ViewDebugger mViewDebugger;

    @Override
    protected void onStart() {
        super.onStart();

        if (!BuildConfig.DEBUG) {
            return;
        }

        if (mIsDebugViewAdded) {
            return;
        }

        addDebugToolView();
        mIsDebugViewAdded = true;
    }

    private void addDebugToolView() {

        DebugToolView debugToolView = new DebugToolView(this);
        debugToolView.setVisibility(View.INVISIBLE);

        ViewGroup contentView = findContentView();
        if (contentView != null) {
            contentView.addView(debugToolView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            Log.w(TAG, "Cannot find contentView of this Activity, view debug tool cannot run");
        }

        mViewDebugger = new ViewDebugger(debugToolView);
    }

    @Nullable
    private ViewGroup findContentView() {
        ViewGroup contentView = findViewById(android.R.id.content);
        if (contentView == null) {
            Window window = getWindow();
            if (window != null) {
                View decorView = window.getDecorView();
                if ((decorView instanceof ViewGroup)) {
                    ViewGroup decorViewGroup = (ViewGroup) decorView;
                    contentView = (ViewGroup) decorViewGroup.getChildAt(0);
                }
            }
        }
        return contentView;
    }

    // Android Framework 传递的参数 event 一定是非空
    @Override
    public boolean dispatchKeyEvent(@SuppressWarnings("NullableProblems") @NonNull KeyEvent event) {
        if (BuildConfig.DEBUG) {
            if (mViewDebugger!=null && mViewDebugger.handleKey(event)) {
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }
}
