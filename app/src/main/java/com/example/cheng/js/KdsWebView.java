package com.example.cheng.js;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class KdsWebView extends WebView {

    public KdsWebView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public KdsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        
        this.setWebViewClient(new KdsWebViewClient());
    }
    
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        invalidate();
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    /**
     * 设置 KDS WebViewClient
     * @param kdsWebViewClient
     */
    public void setKdsWebViewClient(KdsWebViewClient kdsWebViewClient){
        this.setWebViewClient(kdsWebViewClient);
    }

    /**
     * 用于高版本Android系统 安全同线程调用JS方法
     * @param url
     */
    public void loadJsMethodUrl(final String url) {
        post(new Runnable() {
            @Override
            public void run() {
                loadUrl(url);
            }
        });
    }
}
