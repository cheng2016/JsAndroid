package com.example.cheng.js;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
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

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		urlBackTypeMap = new HashMap<String, String>();


		this.setContentView(R.layout.kds_webview_title_layout);

		rootView = (RelativeLayout) this.findViewById(R.id.root);
		mKdsWebView = (KdsWebView) this.findViewById(R.id.wv_kdsWebView);//new KdsWebView(this);
		mProgressBar = (ProgressBar) this.findViewById(R.id.wv_ProgressBar);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mKdsWebView.isHardwareAccelerated())
			mKdsWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		// 设置这个主要是用于流畅的滑动
		mKdsWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
		if (Build.VERSION.SDK_INT >= 19)
		mKdsWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		//mKdsWebView.getSettings().setBlockNetworkImage(true);//用于支持图片的显示
		mKdsWebView.getSettings().setJavaScriptEnabled(true);
			mKdsWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
			String appCachePath = getApplicationContext().getCacheDir()
					.getAbsolutePath()
					+ "/webcache";
			mKdsWebView.getSettings().setDatabaseEnabled(true);
			mKdsWebView.getSettings().setDatabasePath(appCachePath);
			mKdsWebView.getSettings().setDomStorageEnabled(true);
			mKdsWebView.getSettings().setAppCachePath(appCachePath);
			mKdsWebView.getSettings().setAllowFileAccess(true);
			mKdsWebView.getSettings().setAppCacheEnabled(true);
			mKdsWebView.getSettings().setCacheMode(cacheMode);
			mKdsWebView.getSettings().setSavePassword(false);
			mKdsWebView.getSettings().setSaveFormData(true);
	}
	
	private int cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		//++ 处理title
		rl_title_layout = (RelativeLayout) this.findViewById(R.id.rl_title_layout);
		rl_title_layout.setVisibility(visibility);

		//-- 处理title
	}
	private int visibility = View.GONE;
	public void setTitleVisibility(int visibility){
		this.visibility = visibility;
	}
	
	public void setTitle(String title){
		if(txt_title != null)
			txt_title.setText(title);
	}
	/**
	 * 添加与H5交互
	 * 
	 * @param javascriptInterface
	 * @param interfaceName
	 */
	public void addJavascriptInterface(JavascriptInterface javascriptInterface,
			String interfaceName) {

		if (javascriptInterface != null && mKdsWebView != null) {
			mKdsWebView.addJavascriptInterface(javascriptInterface,
					"fdsf");
			if (Build.VERSION.SDK_INT < 17) {
				mKdsWebView.removeJavascriptInterface("accessibility");
				mKdsWebView.removeJavascriptInterface("accessibilityTraversal");
				mKdsWebView.removeJavascriptInterface("searchBoxJavaBridge_");
			}

		}
	}
	/**
	 * 添加与H5交互
	 * 
	 * @param javascriptInterface
	 */
	public void addJavascriptInterface(JavascriptInterface javascriptInterface) {

		if (javascriptInterface != null && mKdsWebView != null) {

			Log.i("WebkitSherlockFragment",
					"addJavascriptInterface true:interfaceName:"
							+ "test");

			mKdsWebView.addJavascriptInterface(javascriptInterface,
					"test");
			if (Build.VERSION.SDK_INT < 17) {
				mKdsWebView.removeJavascriptInterface("accessibility");
				mKdsWebView.removeJavascriptInterface("accessibilityTraversal");
				mKdsWebView.removeJavascriptInterface("searchBoxJavaBridge_");
			}

		} else {
			Log.i("WebkitActivity",
					"addJavascriptInterface false javascriptInterface:"
							+ javascriptInterface + ",KdsWebView:"
							+ mKdsWebView);
		}
	}
	
	public void resetLoadUrl(String url) {
		if (mKdsWebView != null)
			mKdsWebView.loadUrl(url);
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
		// TODO Auto-generated method stub
		super.onDestroy();

		getKdsWebView().removeAllViews();
		getKdsWebView().destroy();
	}


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && mKdsWebView.canGoBack()) {
            mKdsWebView.destroy();
			this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
