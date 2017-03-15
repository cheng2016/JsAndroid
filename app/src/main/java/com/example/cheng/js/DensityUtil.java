package com.example.cheng.js;

import android.content.Context;
import android.util.Log;

/**
 * Created by wangke on 2017/3/3.
 */

public class DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        Log.i("DensityUtil","scale："+context.getResources().getDisplayMetrics(). density);
        final float scale = context.getResources().getDisplayMetrics(). density;
        Log.i("DensityUtil---px："+pxValue,"dp："+(int) (pxValue / scale + 0.5f));
        return (int) (pxValue / scale + 0.5f);
    }
}
