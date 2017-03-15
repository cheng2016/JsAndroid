package com.example.cheng.js;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TouguShowH5Activity extends WebkitActivity implements ISubTabView{

	/**与js数据交互的接口*/
//	protected TextView closeView;
	private Activity mActivity;
	public final static String ACTIVITY_TYPE = "ACTIVITY_TYPE";
	public final static String ACTIVITY_TYPE_PUSH_MSG = "ACTIVITY_TYPE_PUSH_MSG";


	public interface HandleUnLoginListener{
		void onHandleUnLogin();
	}

	/**
	 * 返回键类型：0：按照web页一页一页返回；1：直接finish Activity; 
	 * <p>2: 不销毁,后台缓存Activity;(当前类启动模式需为singleInstance);
	 */
	private int backType = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mActivity = this;

		String url = this.getIntent().getStringExtra("key_h5url");

		//Logger.d("tag", "1-TouguShowH5Activity url:"+url);
		//[需求]：将带参数的url转化成实际的url链接
//		url = UrlParamsManager.getInstance(this).toNewUrl(url);
		//Logger.d("tag", "2-TouguShowH5Activity url:"+url);

		int visibilityTile = this.getIntent().getIntExtra("key_titleVisibility", View.GONE);
		setTitleVisibility(visibilityTile);

		backType = this.getIntent().getIntExtra("backType", 0);

		this.setUrl("https://teste.csc108.com/fmall/main");



//		Logger.d("TouguShowH5Activity1", "加载完成");

		getKdsWebView().setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				// TODO Auto-generated method stub
				super.onReceivedTitle(view, title);
				String functionCode = getIntent().getStringExtra("functionCode");
				if ("KDS_YouWen_YWYD".equalsIgnoreCase(functionCode))
					setTitle("优问");
				else if(!TextUtils.isEmpty(functionCode) &&
						(functionCode.startsWith("KDS_TICKET")
						|| functionCode.startsWith("KDS_PHONE_TICKET"))){
					//[需求]加载建投公募基金、我的Level 2等Web界面时不读取 Html页面的Title.

				}else
					setTitle(title);
			}

			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100)
					mProgressBar.setVisibility(View.GONE);

				super.onProgressChanged(view, newProgress);
			}

			//支持webview调用原生相册进行上传图片到WebView中
			// Android > 4.1.1 调用这个方法
			public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType, String capture) {

				mUploadMessage = uploadMsg;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");

				TouguShowH5Activity.this.startActivityForResult(
						Intent.createChooser(intent, "完成操作需要使用"),
						FILECHOOSER_RESULTCODE);
			}

			// 3.0 + 调用这个方法
			public void openFileChooser(ValueCallback<Uri> uploadMsg,
										String acceptType) {
				mUploadMessage = uploadMsg;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				TouguShowH5Activity.this.startActivityForResult(
						Intent.createChooser(intent, "完成操作需要使用"),
						FILECHOOSER_RESULTCODE);
			}

			// Android < 3.0 调用这个方法
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				mUploadMessage = uploadMsg;
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				TouguShowH5Activity.this.startActivityForResult(
						Intent.createChooser(intent, "完成操作需要使用"),
						FILECHOOSER_RESULTCODE);
			}
		});
		getKdsWebView().setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

				int handleBackType = getIntent().getIntExtra("backType", 0);

				handlePageFinished(url, getIntent());
			}
		});


		String url1 = getUrl();

		getKdsWebView().loadUrl(url1);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private ValueCallback<Uri> mUploadMessage;
	private final static int FILECHOOSER_RESULTCODE=1;  
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode,  
	        Intent intent) {
		
	}

	private void commitBitmap(Intent intent){
		if(intent == null)
			return;
		
		Uri originalUri = intent.getData();
    	ContentResolver resolver = getContentResolver();
    	try {
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
			String base64Str = bitmaptoString(bitmap, 25);

			//键盘精灵
		    Map<String, String> map = new HashMap<String, String>();
		    map.put("imageData", base64Str);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * bitmap 压缩并转成base64编码字符串
	 * @param bitmap
	 * @param bitmapQuality
	 * @return
	 */
	public String bitmaptoString(Bitmap bitmap, int bitmapQuality) {
		 
		return "";
	}


	private void handleBackAction() {
	}


	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(backType == 1)
			return super.onKeyDown(keyCode,event);
		
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			// 系统软键盘返回键统一以顶部返回键事件处理:
//			handleBackAction();
			this.finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
	
	@Override
	public void onDestroy() {
		if (getKdsWebView() != null) {
			getKdsWebView().setWebChromeClient(null);
			getKdsWebView().setWebViewClient(null);
			getKdsWebView().getSettings().setJavaScriptEnabled(false);
			getKdsWebView().clearCache(true);
		}

		// TODO Auto-generated method stub
		super.onDestroy();
        //注销广播接收器

	}
	
	@Override
	public boolean AddZixuan() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delZiXuan() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void fastTrade() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Activity getCurrentAct() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFromID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getModeID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void goBack() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEnableFastTrade() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurrentSubView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showOrHideAddStock(boolean arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 仅第一次页面加载完毕时
	 */
	private boolean isFirstPageFinished = false;

	/**
	 * 处理Web/H5页面加载完毕回调方法:
	 */
	protected void handlePageFinished(String url, Intent intent){
	}

	/**
	 * 建投 业务办理等App传递参数给Web界面指定JS方法:
	 * @param custid    客户代码
	 * @param fundid    资金账号
	 * @param userrole  用户角色
	 * @param imei      国际移动设备身份码
	 * @param orgid     操作机构
	 * @param ticket    ticket票据
	 * @param ip        ip地址
	 * @param targetUrl 目的地址
	 */
	private void setUserDataAndTargetUrl(String custid, String fundid, String userrole,
                                         String imei, String orgid, String ticket, String ip, String targetUrl) {
		getKdsWebView().loadJsMethodUrl("javascript:setUserDataAndTargetUrl('"+custid+"','"+fundid+"','"+userrole+"','"+imei+"','"+orgid+"','"+ticket+"','"+ip+"','"+targetUrl+"')");
		// setUserDataAndTargetUrl("180300015626","80316041","1","355470061278541","1803","J+fw3+IZcIPd4GwL/nSe7ZtwmOgJKZZssTzVAqAyShc=#XW249kXBWVM2SU/UpUIknWGi4IPdT/vNktrpUddn3FPPgPnyuIoaiJKR84fIguVVQdCYx4wTfUOfvkO6eh3yfYntS7zfEH+5hjjC5I0nxUSnb5hYPvMzjIUA4mG16o8bU9kTm5HZL37F6NQZV1i4dA==#2#0#1803#10","192.168.1.103","https://www.baidu.com");
	}

	/**
	 * 建投 业务办理等App传递参数给Web界面指定JS方法:
	 * @param custid    客户代码
	 * @param fundid    资金账号
	 * @param userrole  用户角色
	 * @param imei      国际移动设备身份码
	 * @param orgid     操作机构
	 * @param ticket    ticket票据
	 * @param ip        ip地址
	 * @param targetUrl 目的地址
	 */
	private void setUserData(String custid, String fundid, String userrole,
                             String imei, String orgid, String ticket, String ip, String targetUrl) {
	}
}
