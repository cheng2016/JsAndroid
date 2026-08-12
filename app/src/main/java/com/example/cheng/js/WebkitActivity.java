package com.example.cheng.js;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class WebkitActivity extends Activity {

    protected RelativeLayout rl_title_layout;
    protected RelativeLayout rootView;
    protected TextView txt_title, txt_tiaocang, txt_bianji, txt_share;
    private KdsWebView mKdsWebView;
    public ProgressBar mProgressBar;
    private String mUrl;
    private RelativeLayout rl_tougu_tiaocang, rl_tougu_bianji, rl_tougu_share;
    /** Web页面后退类型管理栈表 */
    public Map<String, String> urlBackTypeMap;

    private int cacheMode = WebSettings.LOAD_DEFAULT;
    private int visibility = View.GONE;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urlBackTypeMap = new HashMap<String, String>();

        this.setContentView(R.layout.kds_webview_title_layout);

        rootView = (RelativeLayout) this.findViewById(R.id.root);
        mKdsWebView = (KdsWebView) this.findViewById(R.id.wv_kdsWebView);
        mProgressBar = (ProgressBar) this.findViewById(R.id.wv_ProgressBar);
        txt_title = (TextView) this.findViewById(R.id.txt_title);

        WebSettings settings = mKdsWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(cacheMode);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rl_title_layout = (RelativeLayout) this.findViewById(R.id.rl_title_layout);
        rl_title_layout.setVisibility(visibility);
    }

    public void setTitleVisibility(int visibility) {
        this.visibility = visibility;
    }

    public void setTitle(String title) {
        if (txt_title != null) {
            txt_title.setText(title);
        }
    }

    /**
     * 添加与H5交互
     */
    public void addJavascriptInterface(Object javascriptInterface, String interfaceName) {
        if (javascriptInterface != null && mKdsWebView != null && interfaceName != null) {
            mKdsWebView.addJavascriptInterface(javascriptInterface, interfaceName);
        }
    }

    /**
     * 添加与H5交互（默认名 test）
     */
    public void addJavascriptInterface(Object javascriptInterface) {
        if (javascriptInterface != null && mKdsWebView != null) {
            Log.i("WebkitActivity", "addJavascriptInterface true:interfaceName:test");
            mKdsWebView.addJavascriptInterface(javascriptInterface, "test");
        } else {
            Log.i("WebkitActivity",
                    "addJavascriptInterface false javascriptInterface:"
                            + javascriptInterface + ",KdsWebView:" + mKdsWebView);
        }
    }

    public void resetLoadUrl(String url) {
        if (mKdsWebView != null) {
            mKdsWebView.loadUrl(url);
        }
    }

    public KdsWebView getKdsWebView() {
        return mKdsWebView;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public void onDestroy() {
        if (mKdsWebView != null) {
            mKdsWebView.removeAllViews();
            mKdsWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mKdsWebView != null && mKdsWebView.canGoBack()) {
            mKdsWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
