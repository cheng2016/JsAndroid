package com.example.cheng.js;

public interface BaseInterface {

	/**
	 * 跳转界面
	 * 
	 * @param url
	 * @param hasRefresh
	 */
	@android.webkit.JavascriptInterface
	public abstract void switchWebView(String url, int hasRefresh, int direction);

	/**
	 * 调用自定义键盘
	 * 
	 * @param type
	 */
	@android.webkit.JavascriptInterface
	public abstract void showKeyBoard(String type, String yCoordinate);

	/**
	 * 隐藏自定义键盘
	 */
	@android.webkit.JavascriptInterface
	public abstract void hideKeyBoard();

	/**
	 * 添加自选股
	 */
	@android.webkit.JavascriptInterface
	public abstract void addUserStock(String stockName, String stockCode,
                                      String marketId);

	/**
	 * 启动交易登录超时锁定页面
	 * 
	 * @param timerName
	 * @param actionType
	 */
	@android.webkit.JavascriptInterface
	public void timerAction(String timerName, String actionType);

	/**
	 * 保存数据到本地
	 * 
	 * @param key
	 * @param value
	 */
	@android.webkit.JavascriptInterface
	public void saveOrUpdateLocalData(String key, String value);

	/**
	 * 用于交易返回交易状态
	 * 
	 * @param loginAccount
	 *            为空时为登出状态，否则就是登陆状态
	 */
	@android.webkit.JavascriptInterface
	public void onLoginAccount(String loginAccount);
	
	/**
	 * 自助开户接口
	 */
	@android.webkit.JavascriptInterface
	public void selfserviceAccount();
	
	/**
	 * H5获取原生的键值对保存的数据，比如：资金账号等
	 * @param key
	 * @return
	 */
	@android.webkit.JavascriptInterface
	public String getLocalData(String key);

	/**
	 * 交易退出时会调用该方法
	 */
	@android.webkit.JavascriptInterface
	public void onCancelLogin();

	@android.webkit.JavascriptInterface
	public void onLoginTrade(String value);
	
	/**
	 * 用于接收H5传递过来的资金账号密码等数据
	 * @param jsonstr
	 */
	@android.webkit.JavascriptInterface
	public void onInterfaceA(String jsonstr);
	
	/**
	 * 关闭App应用
	 */
	@android.webkit.JavascriptInterface
	public void closeApp();

	/**
	 * JS调用打开加载第三方Web页面:
	 * @param isCloseCurrent	是否关闭当前已有WebActivity[0: 不关闭 1: 关闭];
	 * @param webUrl			要加载的第三方Web页面;
	 */
	@android.webkit.JavascriptInterface
	public void openThirdPartyWebInterface(String isCloseCurrent, String webUrl);

	/**
	 * JS调用弹出原生系统日期框:
	 * @param cutDate		当前默认选中日期
	 * @param minDate		最小可选日期, 可空
	 * @param maxDate		最大可选日期, 可空
	 * @param callbackFunction	确定后回调的JS函数
	 */
	@android.webkit.JavascriptInterface
	public void showDateDialog(String cutDate, String minDate, String maxDate, String callbackFunction);

	/**
	 * 跳转至股票个股详情界面
     */
	@android.webkit.JavascriptInterface
	public void JumpStockDetailInterface(String stockCode, String marketId);

	/**
	 * 调用第三方分享, 微信朋友/朋友圈:
	 * @param title			分享标题
	 * @param shareUrl		分享地址
	 */
	@android.webkit.JavascriptInterface
	public void shareToWechat(String title, String shareUrl);
	
	/**
	 * 调用第三方分享, 微信朋友/朋友圈:
	 * @param title			分享标题
	 * @param shareUrl		分享地址
	 * @param summary		分享内容摘要
	 */
	@android.webkit.JavascriptInterface
	public void shareToWechat(String title, String shareUrl, String summary);
}
