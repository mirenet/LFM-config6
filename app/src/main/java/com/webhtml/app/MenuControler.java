package com.webhtml.app;

import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

public class MenuControler {

    private int tapCount = 0;
    private long lastTapTime = 0;
    private float lastX = 0;
    private float lastY = 0;
    private final WebView overlayWebView;
    
    private static final long TAP_TIMEOUT = 250; // Strogo brzo kuckanje
    private static final float MAX_DISTANCE_PX = 50.0f; // U istoj tački
    private static final float MOVE_SLOP = 20.0f;
    private float startX = 0;
    private float startY = 0;

    public MenuControler(WebView overlayWebView) {
        this.overlayWebView = overlayWebView;
    }

    public boolean handleTouchEvent(MotionEvent event) {
        // KADA JE MENI VEĆ OTVOREN: Ignorišemo sve tapove da se ne bi zatvorio na dalje kuckanje
        if (isMenuVisible()) {
            return false;
        }

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() > 1) {
                    tapCount = 0;
                    return false;
                }

                long currentTime = System.currentTimeMillis();
                float currentX = event.getX();
                float currentY = event.getY();
                
                startX = currentX;
                startY = currentY;

                if (tapCount > 0) {
                    float distance = (float) Math.hypot(currentX - lastX, currentY - lastY);
                    if ((currentTime - lastTapTime > TAP_TIMEOUT) || (distance > MAX_DISTANCE_PX)) {
                        tapCount = 0;
                    }
                }

                lastTapTime = currentTime;
                lastX = currentX;
                lastY = currentY;
                tapCount++;

                if (tapCount >= 6) {
                    tapCount = 0;
                    toggleMenu();
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (tapCount > 0) {
                    float moveX = Math.abs(event.getX() - startX);
                    float moveY = Math.abs(event.getY() - startY);
                    if (moveX > MOVE_SLOP || moveY > MOVE_SLOP) {
                        tapCount = 0;
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                tapCount = 0;
                break;
        }

        return false;
    }

    public void toggleMenu() {
        if (overlayWebView != null) {
            if (overlayWebView.getVisibility() == View.VISIBLE) {
                overlayWebView.setVisibility(View.GONE);
            } else {
                overlayWebView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void closeMenu() {
        if (overlayWebView != null) {
            overlayWebView.setVisibility(View.GONE);
        }
    }

    public boolean isMenuVisible() {
        return overlayWebView != null && overlayWebView.getVisibility() == View.VISIBLE;
    }
}
