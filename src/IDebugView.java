package com.stv.debug;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * @author hechuan1 on 2020/9/27.
 */
interface IDebugView {
    void moveCursor(int x, int y);

    void moveToOrigin();

    void hightlightView(@NonNull Rect viewBounds, @NonNull String info);

    void hightlightFocusedView(@NonNull Rect viewBounds, @NonNull String info);

    @NonNull
    Point getCursorLocation();

    @NonNull
    View getView();
}
