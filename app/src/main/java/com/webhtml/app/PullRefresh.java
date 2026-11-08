package com.webhtml.app;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class PullRefresh {

    private final Context context;
    private final WebView webView;
    private CustomSwipeRefreshLayout swipeRefreshLayout;

    public PullRefresh(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
    }

    public CustomSwipeRefreshLayout setup(android.view.ViewGroup.LayoutParams layoutParams) {
        // Prosleđujemo webView da bi CustomSwipeRefreshLayout znao poziciju skrola
        swipeRefreshLayout = new CustomSwipeRefreshLayout(context, webView);
        
        // --- PODEŠAVANJE BOJA KRUŽIĆA ---
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.parseColor("#1A1A1C"));
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#CAD967"));
        
        if (webView.getParent() != null) {
            ((android.view.ViewGroup) webView.getParent()).removeView(webView);
        }

        swipeRefreshLayout.addView(webView, new CustomSwipeRefreshLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ));

        if (layoutParams != null) {
            swipeRefreshLayout.setLayoutParams(layoutParams);
        }

        swipeRefreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1500);
        });

        return swipeRefreshLayout;
    }

    /**
     * Custom SwipeRefreshLayout baziran na tvom radnom kodu, 
     * sa dodatom proverom da je stranica na samom vrhu (scrollY == 0).
     */
    public static class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
        private final WebView targetWebView;
        private float startY = 0f;
        private float startX = 0f;
        private long startTime = 0L;
        private boolean isIntentionalPull = false;
        private final int touchSlop;

        public CustomSwipeRefreshLayout(Context context, WebView webView) {
            super(context);
            this.targetWebView = webView;
            this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            // Ako nismo na samom vrhu stranice, odmah prekidamo i dozvoljavamo normalan skrol sajta
            if (targetWebView != null && targetWebView.getScrollY() > 0) {
                isIntentionalPull = false;
                return false;
            }

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = ev.getY();
                    startX = ev.getX();
                    startTime = System.currentTimeMillis();
                    isIntentionalPull = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Dodatna provera u pokretu
                    if (targetWebView != null && targetWebView.getScrollY() > 0) {
                        return false;
                    }

                    float diffY = ev.getY() - startY;
                    float diffX = Math.abs(ev.getX() - startX);
                    long elapsedTime = System.currentTimeMillis() - startTime;

                    // Ako je korisnik povukao levo-desno više nego gore-dole -> ignoriši
                    if (diffX > diffY) {
                        return false;
                    }

                    // USLOV: Pomeraj veći od 150px I vreme duže od 150ms
                    if (diffY > 150 && elapsedTime > 150) {
                        isIntentionalPull = true;
                    } else if (!isIntentionalPull) {
                        return false;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isIntentionalPull = false;
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }
    }
}
