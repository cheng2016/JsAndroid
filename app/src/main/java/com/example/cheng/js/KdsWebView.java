package com.example.cheng.js;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.ValueCallback;
import android.webkit.WebView;

public class KdsWebView extends WebView {

    public KdsWebView(Context context) {
        this(context, null);
    }

    public KdsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWebViewClient(new KdsWebViewClient());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        invalidate();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setKdsWebViewClient(KdsWebViewClient kdsWebViewClient) {
        this.setWebViewClient(kdsWebViewClient);
    }

    /**
     * 安全地在主线程调用 JS（优先 evaluateJavascript）
     */
    public void loadJsMethodUrl(final String url) {
        post(new Runnable() {
            @Override
            public void run() {
                if (url != null && url.startsWith("javascript:")) {
                    evaluateJavascript(url.substring("javascript:".length()), (ValueCallback<String>) null);
                } else {
                    loadUrl(url);
                }
            }
        });
    }
}
