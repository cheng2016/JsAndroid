/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zxing.view;

import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.example.cheng.js.R;
import com.google.zxing.ResultPoint;
import com.zxing.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 */
public final class ViewfinderView extends View {
	private static final String TAG = "log";
	/**
	 * Ë¢ÐÂ½çÃæµÄÊ±¼ä
	 */
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;

	/**
	 * ËÄ¸öÂÌÉ«±ß½Ç¶ÔÓ¦µÄ³¤¶È
	 */
	private int ScreenRate;
	
	/**
	 * ËÄ¸öÂÌÉ«±ß½Ç¶ÔÓ¦µÄ¿í¶È
	 */
	private static final int CORNER_WIDTH = 10;
	/**
	 * É¨Ãè¿òÖÐµÄÖÐ¼äÏßµÄ¿í¶È
	 */
	private static final int MIDDLE_LINE_WIDTH = 6;
	
	/**
	 * É¨Ãè¿òÖÐµÄÖÐ¼äÏßµÄÓëÉ¨Ãè¿ò×óÓÒµÄ¼äÏ¶
	 */
	private static final int MIDDLE_LINE_PADDING = 5;
	
	/**
	 * ÖÐ¼äÄÇÌõÏßÃ¿´ÎË¢ÐÂÒÆ¶¯µÄ¾àÀë
	 */
	private static final int SPEEN_DISTANCE = 5;
	
	/**
	 * ÊÖ»úµÄÆÁÄ»ÃÜ¶È
	 */
	private static float density;
	/**
	 * ×ÖÌå´óÐ¡
	 */
	private static final int TEXT_SIZE = 16;
	/**
	 * ×ÖÌå¾àÀëÉ¨Ãè¿òÏÂÃæµÄ¾àÀë
	 */
	private static final int TEXT_PADDING_TOP = 30;
	
	/**
	 * »­±Ê¶ÔÏóµÄÒýÓÃ
	 */
	private Paint paint;
	
	/**
	 * ÖÐ¼ä»¬¶¯ÏßµÄ×î¶¥¶ËÎ»ÖÃ
	 */
	private int slideTop;
	
	/**
	 * ÖÐ¼ä»¬¶¯ÏßµÄ×îµ×¶ËÎ»ÖÃ
	 */
	private int slideBottom;
	
	/**
	 * ½«É¨ÃèµÄ¶þÎ¬ÂëÅÄÏÂÀ´£¬ÕâÀïÃ»ÓÐÕâ¸ö¹¦ÄÜ£¬ÔÝÊ±²»¿¼ÂÇ
	 */
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	
	private final int resultPointColor;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;

	boolean isFirst;
	
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		density = context.getResources().getDisplayMetrics().density;
		//½«ÏñËØ×ª»»³Édp
		ScreenRate = (int)(20 * density);

		paint = new Paint();
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);

		resultPointColor = resources.getColor(R.color.possible_result_points);
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	@Override
	public void onDraw(Canvas canvas) {
		//ÖÐ¼äµÄÉ¨Ãè¿ò£¬ÄãÒªÐÞ¸ÄÉ¨Ãè¿òµÄ´óÐ¡£¬È¥CameraManagerÀïÃæÐÞ¸Ä
		Rect frame = CameraManager.get().getFramingRect();
		if (frame == null) {
			return;
		}
		
		//³õÊ¼»¯ÖÐ¼äÏß»¬¶¯µÄ×îÉÏ±ßºÍ×îÏÂ±ß
		if(!isFirst){
			isFirst = true;
			slideTop = frame.top;
			slideBottom = frame.bottom;
		}
		
		//»ñÈ¡ÆÁÄ»µÄ¿íºÍ¸ß
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		
		//»­³öÉ¨Ãè¿òÍâÃæµÄÒõÓ°²¿·Ö£¬¹²ËÄ¸ö²¿·Ö£¬É¨Ãè¿òµÄÉÏÃæµ½ÆÁÄ»ÉÏÃæ£¬É¨Ãè¿òµÄÏÂÃæµ½ÆÁÄ»ÏÂÃæ
		//É¨Ãè¿òµÄ×ó±ßÃæµ½ÆÁÄ»×ó±ß£¬É¨Ãè¿òµÄÓÒ±ßµ½ÆÁÄ»ÓÒ±ß
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);
		
		

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {

			//»­É¨Ãè¿ò±ßÉÏµÄ½Ç£¬×Ü¹²8¸ö²¿·Ö
			paint.setColor(Color.GREEN);
			canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
					frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top
					+ ScreenRate, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
					frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top
					+ ScreenRate, paint);
			canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
					+ ScreenRate, frame.bottom, paint);
			canvas.drawRect(frame.left, frame.bottom - ScreenRate,
					frame.left + CORNER_WIDTH, frame.bottom, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,
					frame.right, frame.bottom, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,
					frame.right, frame.bottom, paint);

			
			//»æÖÆÖÐ¼äµÄÏß,Ã¿´ÎË¢ÐÂ½çÃæ£¬ÖÐ¼äµÄÏßÍùÏÂÒÆ¶¯SPEEN_DISTANCE
			slideTop += SPEEN_DISTANCE;
			if(slideTop >= frame.bottom){
				slideTop = frame.top;
			}
			canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH/2, frame.right - MIDDLE_LINE_PADDING,slideTop + MIDDLE_LINE_WIDTH/2, paint);
			
			
			//»­É¨Ãè¿òÏÂÃæµÄ×Ö
			paint.setColor(Color.WHITE);
			paint.setTextSize(TEXT_SIZE * density);
			paint.setAlpha(0x40);
			paint.setTypeface(Typeface.create("System", Typeface.BOLD));
			canvas.drawText(getResources().getString(R.string.scan_text), frame.left, (float) (frame.bottom + (float)TEXT_PADDING_TOP *density), paint);
			
			

			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 3.0f, paint);
				}
			}

			
			//Ö»Ë¢ÐÂÉ¨Ãè¿òµÄÄÚÈÝ£¬ÆäËûµØ·½²»Ë¢ÐÂ
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
					frame.right, frame.bottom);
			
		}
	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

}
