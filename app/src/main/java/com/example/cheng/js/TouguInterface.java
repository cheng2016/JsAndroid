package com.example.cheng.js;

/**
 * 东莞投顾项目，盈通证券客户端提供给js代码调用的接口定义类
 * @author wanlh
 *
 */
public interface TouguInterface {

	/**
	 * 搜索证券,键盘精灵
	 * 投顾的js代码启动原生的键盘精灵接口
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 * @param stockCodes 股票代码串 已添加的股票代码,可为空.eg:"000001,000002,000003"
	 */
	public void showKeyBoardTG(String backMethodName, String stockCodes);
	
	/**
	 * 登录状态判断接口
	 * 投顾的js代码获取交易登录状态接口
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 */
	public void getLoginStateTG(String backMethodName);
	
	/**
	 * 个股详情页面
	 * 投顾的js代码启动原生代码的股票详情页面接口
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 * @param stockCode 股票代码
	 * @param marketId  市场代码
	 */
	public void gotoStockDetailTG(String backMethodName, String stockCode, String marketId);
	
	/**
	 * 用户注册
	 * 投顾的js代码调用原生的注册界面
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 */
	public void ShowRegisterView(String backMethodName);
	
	/**
	 * 实盘/模拟盘登录
	 * 投顾的js代码调用原生的登录界面
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 */
	public void gotoTradeLoginViewTG(String backMethodName);
	
	/**
	 * (实盘)开户js接口
	 * 投顾的js代码调用原生开户接口
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 */
	public void selfserviceAccount(String backMethodName);
	
	/**
	 * (实盘)建仓js接口
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 * @param stockCode 目标证券代码
	 * @param flag 买卖标记（是买入还是卖出操作）0 买；1卖;2：撤单
	 * @param backMethodName 需要回调的方法名
	 */
	public void gotoTradePlaceAnOrderTG(String backMethodName, String stockCode, String flag);

    /**
     * (实盘)持仓查询js接口
     * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
     */
	public void gotoTradePositionViewTG(String backMethodName);
	
	/**
	 * 分享接口
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 * @param title 分享内容标题
	 * @param url 分享内容摘要
	 * @param summary 分享内容的web url
	 */
	public void showShareTG(String backMethodName, String title, String url, String summary);
	
	/**
	 * 获取用户信息
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 * @param userId 用户Id
	 */
	public void getUserInforTG(String backMethodName, String userId);
	
	/**
	 * 提升用户等级接口
	 * @param backMethodName 回调函数名 用于给js回调传递数据的方法名称
	 * @param signUpFlag 是否报名参加了股神大赛
	 * @param userId 用户id
	 */
	public void useridLevelUp(String backMethodName, String signUpFlag, String userId);
	
	/**
	 * 左上角返回接口
	 */
	public void closeCurrentWindow();
	
	/**
	 * 用于点击H5页面按钮时调用该方法创建新的窗体加载页面
	 * @param linkUrl 页面加载的url
	 */
	public void openNewWindow(String linkUrl);
	
	/**
	 * 用于H5展示短暂提示信息 Toast
	 * @param backMethodName
	 * @param message
	 */
	public void showToast(String backMethodName, String message);

	/**
	 * 调用原生的相册、拍照的接口
	 * @param backMethodName
	 */
	public void pickImage(String backMethodName);

	/**
	 * 调用原生的关闭WebViewActivity接口
	 * @param backMethodName
	 */
	public void youwenTalkingTimeOut(String backMethodName);

	/**
	 * 调用建投优问 视频开户SDK界面
	 * @param IDNumber
	 */
	public void openAccountVideoAuth(String IDNumber);

	/**
	 * 调用调仓记录
	 */
	public void gotoTransferRecord(String id);

	/**
	 * 调用组合配置
	 */
	public void gotoGroupConfig(String id);

	/**
	 * 跳转至资讯详情页面，参数资讯ID
	 */
	public void gotoInfoDetail(String infoID);

	/**
	 * 跳转至资讯详情页面，参数资讯ID
	 */
	public void gotoMoreInfos(String codes);

	/**
	 *
	 */
	public void setTGTotalIncomeRate(String zsyl);

	/**
	 * 调用登录界面
	 */
	public void gotoLoginPage();
}
