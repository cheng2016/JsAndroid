/**
 * 
 */
package com.example.cheng.js;

import android.app.Activity;

/**
 * 子Tab管理接口，常用语在TabActivity中调用子Tab的方法
 * 如：添加自选股，刷新，买卖等操作。
 * @author duminghui
 *
 */
public interface ISubTabView
{
	/**
	 * 添加自选股
	 */
	public boolean AddZixuan();
	
	/**
	 * 手动刷新数据
	 */
	public void refresh();
	/**
	 * 删除自选股
	 */
	public boolean delZiXuan();
	
	/**
	 * 更改添加/删除自选股状态，若显示添加，则删除隐藏，反之亦然
	 * @param showAdd 是否显示“添加自选”
	 * @param hideAll true表示全部隐藏
	 */
	public void showOrHideAddStock(boolean showAdd, boolean hideAll);
	
	
	public void setCurrentSubView();
	
	/**
	 * 快速交易
	 */
	public void fastTrade();
	
	public int getFromID();
	
	/**
	 * 获得当前的ModeID
	 * @return
	 */
	public int getModeID();
	
	/**
	 * 是否允许闪电下单
	 * @return
	 */
	public boolean isEnableFastTrade();
	
	/**
	 * 后退，返回
	 */
	public void goBack();
	
	/**
	 * 获取当前的Activity实例
	 * @return
	 */
	public Activity getCurrentAct();
	
}
