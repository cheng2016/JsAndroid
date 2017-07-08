package com.example.cheng.js;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.zxing.activity.CaptureActivity;

public class MainActivity extends Activity {
    WebView webView;
    // String url = "file:///android_asset/index.html";
    String url = "file:///android_asset/test.html";
    private final static int SCANNIN_GREQUEST_CODE = 1;

    private static String scanResult;
    public final static String LONGITUDE = "longitude";// 经度
    public final static String LATITUDE = "latitude";// 维度
    TextView txt;
    private Handler hd = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        DensityUtil.px2dip(this,216);
        DensityUtil.px2dip(this,226);
        if(null!=savedInstanceState){
            webView.restoreState(savedInstanceState);
            Log.i(TAG, "restore state");
        }
    }
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 处理扫描结果（在界面上显示）
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            scanResult = bundle.getString("result");
            txt.setText("从js获取到得数据为：" + scanResult);
            webView.loadUrl("javascript:setScanResult('" + getJsonStr() + "')");
        }
    }
	
    private void init() {
        txt = (TextView) findViewById(R.id.textView2);
        webView = (WebView) this.findViewById(R.id.wv);
        // 设置字符集编码
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        // 开启JavaScript支持
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setSupportZoom(true);
//		webView.setWebChromeClient(new MyWebChromeClient());
        //通过单纯js来操纵安卓
        webView.addJavascriptInterface(new DemoJavascriptInterface(), "demo");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            //通过shoultOverrideUrlLoading方法判断url信息来操纵安卓，并传递信息
            public boolean shouldOverrideUrlLoading(WebView view,
                                                    final String url) {
                Log.e("myZxing_first", url);
                if (url.startsWith("startscanning://")) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(),
                            CaptureActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                    return true;
                }else if (url.startsWith("startrouting:///")) {
                    Log.e("myZxing_startscanning", url);
                    String s = url.substring(16, url.length());
                    String[] str = s.split("\\/");
                    Log.e("str[0]", str[0]);
/*					Intent intent = new Intent().setClass(
							getApplicationContext(), PoiActivity.class);
					intent.putExtra(LONGITUDE, Double.valueOf(str[0]));
					intent.putExtra(LATITUDE, Double.valueOf(str[1]));
					startActivity(intent);*/
                    txt.setText("从js获取到得数据为："+str[0]+"    "+str[1]);
                    return true;
                }
                return false;
            }
        });
        url = "https://teste.csc108.com/fmall/main";
        webView.loadUrl(url);
        Log.e("xdb", "url:" + url);

        //安卓给js数据
        findViewById(R.id.btn1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                webView.loadUrl("javascript:wave('"+setJsData()+"')");
                startActivity(new Intent().setClass(MainActivity.this,TouguShowH5Activity.class));
            }
        });


        //重置数据
        findViewById(R.id.btn2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                txt.setText("从js获取到得数据为：");
                webView.loadUrl("javascript:reset()");
            }
        });
    }	

    public String setJsData(){
        return "从安卓获取到得数据为：安卓数据";
    }	
	
    public static String getJsonStr() {
        return scanResult;
    }

    // 重写返回键，让网页返回正常化
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 自定义javascript接口用于给网页js调用安卓代码
    final class DemoJavascriptInterface {
        @JavascriptInterface
        public void clickOnAndroid() {
            hd.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(),
                            CaptureActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                }
            });
        }

        //js调用安卓并把数据传给安卓端
        @JavascriptInterface
        public void getHtmlJson(final String json) {
            // TODO Auto-generated method stub
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txt.setText("从html获取到得数据：" + json);
                }
            });
        }
    }
}
