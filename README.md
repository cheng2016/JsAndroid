# JsAndroid
Js与原生安卓进行交互案列

![](screenshot/20170301162221.png)   ![](screenshot/20170301162011.png)

## Code 
#### 初始化webview 设置  

    /**
     * Convenience method to set some generic defaults for a
     * given WebView
     *
     * @param webView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpWebViewDefaults(WebView webView) {
        //开启硬件加速后，WebView渲染页面更加快速，拖动也更加顺滑。
        // 但有个副作用就是容易会出现页面加载白块同时界面闪烁现象。
        // 解决这个问题的方法是设置WebView暂时关闭硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && webView.isHardwareAccelerated()) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        WebSettings settings = webView.getSettings();
        // 设置字符集编码
        settings.setDefaultTextEncodingName("UTF-8");
        //支持数据保存
        settings.setSavePassword(true);
        settings.setSaveFormData(true);
        //支持缩放
        settings.setSupportZoom(true);
        // Enable Javascript
        settings.setJavaScriptEnabled(true);
        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//自适应屏幕
        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            settings.setDisplayZoomControls(false);
        }
        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        //如果SDK版本大于19则使用缓存数据
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        //加载图片优化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            settings.setLoadsImagesAutomatically(true);//图片自动缩放 打开
        } else {
            settings.setLoadsImagesAutomatically(false);//图片自动缩放 关闭
        }
        //支持localStorage
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
    }
    
    
#### webview 销毁 
    @Override
    protected void onDestroy() {
        mGroupLayout.removeAllViews();
        webView.stopLoading();
        webView.removeAllViews();
        webView.destroy();
        webView = null;
        mGroupLayout = null;
        super.onDestroy();
    }



## Thanks

[WebView 你踩过的坑](http://blog.csdn.net/hytfly/article/details/48489251)

[how-to-listen-for-a-webview-finishing-loading-a-url](https://stackoverflow.com/questions/3149216/how-to-listen-for-a-webview-finishing-loading-a-url)

[WebView 滑动卡顿汇总](http://www.cnblogs.com/liu-fei/p/5622446.html)

## 杂谈
[Android开发收集的工具类集合](https://github.com/vondear/RxTools)





