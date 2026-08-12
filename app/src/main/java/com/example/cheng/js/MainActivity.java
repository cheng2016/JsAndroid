package com.example.cheng.js;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zxing.activity.CaptureActivity;

public class MainActivity extends AppCompatActivity implements NativeBridge.Host {

    private static final String TAG = "MainActivity";
    private static final String DEMO_URL = "file:///android_asset/demo.html";
    private static final int REQ_SCAN = 1001;
    private static final int REQ_CAMERA_PERMISSION = 1002;
    private static final int REQ_FILE_CHOOSER = 1003;
    private static final int REQ_BRIDGE_PICK = 1004;

    private WebView webView;
    private TextView statusView;
    private ValueCallback<Uri[]> filePathCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("JsAndroid 交互演示");

        statusView = findViewById(R.id.textView2);
        webView = findViewById(R.id.wv);
        setupWebView(webView);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(DEMO_URL);
        }

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String payload = "{\"type\":\"push\",\"msg\":\"来自 Native 的主动推送\",\"ts\":"
                        + System.currentTimeMillis() + "}";
                evaluateJavascript("typeof onNativeMessage==='function'&&onNativeMessage("
                        + payload + ")");
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatus("从 JS 获取到的数据：");
                evaluateJavascript("typeof resetDemo==='function'&&resetDemo()");
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TouguShowH5Activity.class));
            }
        });
    }

    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.addJavascriptInterface(new NativeBridge(this), "NativeBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return handleBridgeUrl(request.getUrl().toString());
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleBridgeUrl(url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQ_FILE_CHOOSER);
                } catch (Exception e) {
                    MainActivity.this.filePathCallback = null;
                    Toast.makeText(MainActivity.this, "无法打开文件选择器", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("网页提示 (alert)")
                        .setMessage(message)
                        .setPositiveButton("确定", (d, w) -> result.confirm())
                        .setCancelable(false)
                        .show();
                updateStatus("拦截 alert: " + message);
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("网页确认 (confirm)")
                        .setMessage(message)
                        .setPositiveButton("确定", (d, w) -> result.confirm())
                        .setNegativeButton("取消", (d, w) -> result.cancel())
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                      final JsPromptResult result) {
                final EditText input = new EditText(MainActivity.this);
                input.setText(defaultValue == null ? "" : defaultValue);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(message == null ? "网页输入 (prompt)" : message)
                        .setView(input)
                        .setPositiveButton("确定", (d, w) -> result.confirm(input.getText().toString()))
                        .setNegativeButton("取消", (d, w) -> result.cancel())
                        .show();
                return true;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String line = consoleMessage.messageLevel() + " | " + consoleMessage.message()
                        + " @" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber();
                Log.d(TAG, "console: " + line);
                evaluateJavascript("typeof onConsoleFromNative==='function'&&onConsoleFromNative("
                        + NativeBridge.jsonStringLiteral(line) + ")");
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                evaluateJavascript("typeof onLoadProgress==='function'&&onLoadProgress(" + newProgress + ")");
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (title != null && !title.startsWith("file:")) {
                    setTitle(title);
                }
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                updateStatus("触发下载: " + url);
                Toast.makeText(MainActivity.this, "检测到下载请求，已转系统浏览器", Toast.LENGTH_SHORT).show();
                openExternalUrl(url);
                evaluateJavascript("typeof onDownloadRequested==='function'&&onDownloadRequested("
                        + NativeBridge.jsonStringLiteral(url) + ","
                        + NativeBridge.jsonStringLiteral(mimeType) + ")");
            }
        });
    }

    private boolean handleBridgeUrl(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.toLowerCase();

        // tel / mailto / sms — classic system schemes
        if (lower.startsWith("tel:") || lower.startsWith("mailto:") || lower.startsWith("sms:")
                || lower.startsWith("smsto:")) {
            openExternalUrl(url);
            return true;
        }

        if (lower.startsWith("jsbridge://scan")) {
            startQrScan();
            return true;
        }
        if (lower.startsWith("jsbridge://browser")) {
            Uri uri = Uri.parse(url);
            String target = uri.getQueryParameter("url");
            if (target != null) {
                openExternalUrl(target);
            }
            return true;
        }
        if (lower.startsWith("jsbridge://toast")) {
            Uri uri = Uri.parse(url);
            String msg = uri.getQueryParameter("msg");
            Toast.makeText(this, msg == null ? "" : msg, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (lower.startsWith("jsbridge://route")) {
            Uri uri = Uri.parse(url);
            String lng = uri.getQueryParameter("lng");
            String lat = uri.getQueryParameter("lat");
            if (lng == null || lat == null) {
                String path = uri.getPath();
                if (path != null) {
                    String[] parts = path.replaceFirst("^/", "").split("/");
                    if (parts.length >= 2) {
                        lng = parts[0];
                        lat = parts[1];
                    }
                }
            }
            updateStatus("经纬度: lng=" + lng + ", lat=" + lat);
            evaluateJavascript("typeof onRouteResult==='function'&&onRouteResult("
                    + NativeBridge.jsonStringLiteral(String.valueOf(lng)) + ","
                    + NativeBridge.jsonStringLiteral(String.valueOf(lat)) + ")");
            return true;
        }
        if (lower.startsWith("startscanning://")) {
            startQrScan();
            return true;
        }
        if (lower.startsWith("startrouting:///")) {
            String s = url.substring("startrouting:///".length());
            String[] str = s.split("/");
            if (str.length >= 2) {
                updateStatus("经纬度: lng=" + str[0] + ", lat=" + str[1]);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_FILE_CHOOSER) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                } else if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                }
            }
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
            return;
        }

        if (requestCode == REQ_BRIDGE_PICK) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                String uri = data.getData().toString();
                updateStatus("已选择文件: " + uri);
                evaluateJavascript("typeof onFilePicked==='function'&&onFilePicked("
                        + NativeBridge.jsonStringLiteral(uri) + ")");
            } else {
                evaluateJavascript("typeof onFilePicked==='function'&&onFilePicked(null)");
            }
            return;
        }

        if (requestCode == REQ_SCAN && resultCode == RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle != null ? bundle.getString("result") : null;
            updateStatus("扫码结果: " + scanResult);
            evaluateJavascript("typeof setScanResult==='function'&&setScanResult("
                    + NativeBridge.jsonStringLiteral(scanResult) + ")");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCapture();
            } else {
                Toast.makeText(this, "需要相机权限才能扫码", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void updateStatus(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setText(text == null ? "" : text);
            }
        });
    }

    @Override
    public void evaluateJavascript(final String script) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (webView != null) {
                    webView.evaluateJavascript(script, null);
                }
            }
        });
    }

    @Override
    public void startQrScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
            return;
        }
        launchCapture();
    }

    private void launchCapture() {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQ_SCAN);
    }

    @Override
    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "选择文件"), REQ_BRIDGE_PICK);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开文件选择器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void reloadWebView() {
        if (webView != null) {
            webView.reload();
        }
    }

    @Override
    public void goBackWebView() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            Toast.makeText(this, "没有上一页", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean canGoBackWebView() {
        return webView != null && webView.canGoBack();
    }

    @Override
    public void closePage() {
        finish();
    }

    @Override
    public void openExternalUrl(String url) {
        if (url == null || url.length() == 0) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "无法打开: " + url, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setActivityTitle(String title) {
        setTitle(title == null ? "" : title);
        updateStatus("页面标题已设为: " + title);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
