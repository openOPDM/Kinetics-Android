package org.kineticsfoundation.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

/**
 * Special layout to simplify Lock screen functionality implementation
 * Created by akaverin on 6/27/13.
 */
public class LockInputLayout extends LinearLayout {

    private GestureDetector gestureDetector;
    private WeakReference<UnlockListener> weakListener;

    public LockInputLayout(Context context) {
        this(context, null);
    }

    public LockInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockInputLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) {
            return;
        }
        GestureListener listener = new GestureListener();
        gestureDetector = new GestureDetector(context, listener);
        gestureDetector.setOnDoubleTapListener(listener);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return true;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void setListener(UnlockListener listener) {
        this.weakListener = new WeakReference<UnlockListener>(listener);
    }

    public interface UnlockListener {
        void onUnlock();
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            LockInputLayout currentView = LockInputLayout.this;
            if (currentView.getParent() != null) {
                ViewGroup viewGroup = (ViewGroup) currentView.getParent();
                //noinspection ConstantConditions
                viewGroup.removeView(currentView);
            }

            UnlockListener realListener = weakListener.get();
            if (realListener != null) {
                realListener.onUnlock();
            }
            return true;
        }
    }
}
