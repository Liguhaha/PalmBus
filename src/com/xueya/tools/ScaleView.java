package com.xueya.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.utils.DistanceUtil;
import com.xueya.palmbus.R;

public class ScaleView extends View {
	private Paint mPaint;
	/**
	 * 比例尺的宽度
	 */
	private int scaleWidth;
	/**
	 * 比例尺的高度
	 */
	private int scaleHeight = 4;
	/**
	 * 比例尺上面字体的颜色
	 */
	private int textColor = Color.BLACK;
	/**
	 * 比例尺上边的字体
	 */
	private String text = "";
	/**
	 * 字体大小
	 */
	private int textSize = 16;
	/**
	 * 比例尺与字体间的距离
	 */
	private int scaleSpaceText = 8;
	/**
	 * 百度地图最大缩放级别
	 */
	private static final int MAX_LEVEL = 19;
	/**
	 * 各级比例尺分母值数组
	 */
	private static final int[] SCALES = { 20, 50, 100, 200, 500, 1000, 2000,
			5000, 10000, 20000, 25000, 50000, 100000, 200000, 500000, 1000000,
			2000000 };
	/**
	 * 各级比例尺上面的文字数组
	 */
	private static final String[] SCALE_DESCS = { "20米", "50米", "100米", "200米",
			"500米", "1公里", "2公里", "5公里", "10公里", "20公里", "25公里", "50公里",
			"100公里", "200公里", "500公里", "1000公里", "2000公里" };

	private static MapView mapView;
	private static int widthWin;
	private static int heightWin;

	/**
	 * 与MapView设置关联
	 * 
	 * @param mapView
	 */
	public static void setMapView(MapView mapView1, int widthWin1,
			int heightWin1) {
		mapView = mapView1;
		heightWin = heightWin1;
		widthWin = widthWin1;
	}

	public ScaleView(Context context) {
		this(context, null);
	}

	public ScaleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScaleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
	}

	/**
	 * 绘制上面的文字和下面的比例尺，因为比例尺是.9.png，我们需要利用drawNinepath方法绘制比例尺
	 */
	@Override
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = scaleWidth;

		mPaint.setColor(textColor);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(textSize);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		float textWidth = mPaint.measureText(text);

		canvas.drawText(text, (width - textWidth) / 2, textSize, mPaint);

		Rect scaleRect = new Rect(0, textSize + scaleSpaceText, scaleWidth,
				textSize + scaleSpaceText + scaleHeight);
		drawNinepath(canvas, R.drawable.icon_scale, scaleRect);
	}

	/**
	 * 手动绘制.9.png图片
	 * 
	 * @param canvas
	 * @param resId
	 * @param rect
	 */
	private void drawNinepath(Canvas canvas, int resId, Rect rect) {
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resId);
		NinePatch patch = new NinePatch(bmp, bmp.getNinePatchChunk(), null);
		patch.draw(canvas, rect);
	}

	/**
	 * 测量ScaleView的方法，
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSize = getWidthSize(widthMeasureSpec);
		int heightSize = getHeightSize(heightMeasureSpec);
		setMeasuredDimension(widthSize, heightSize);
	}

	/**
	 * 测量ScaleView的宽度
	 * 
	 * @param widthMeasureSpec
	 * @return
	 */
	private int getWidthSize(int widthMeasureSpec) {
		return MeasureSpec.getSize(widthMeasureSpec);
	}

	/**
	 * 测量ScaleView的高度
	 * 
	 * @param widthMeasureSpec
	 * @return
	 */
	private int getHeightSize(int heightMeasureSpec) {
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		int height = 0;
		switch (mode) {
		case MeasureSpec.AT_MOST:
			height = textSize + scaleSpaceText + scaleHeight;
			break;
		case MeasureSpec.EXACTLY: {
			height = MeasureSpec.getSize(heightMeasureSpec);
			break;
		}
		case MeasureSpec.UNSPECIFIED: {
			height = Math.max(textSize + scaleSpaceText + scaleHeight,
					MeasureSpec.getSize(heightMeasureSpec));
			break;
		}
		}

		return height;
	}

	/**
	 * 根据缩放级别，得到对应比例尺在SCALES数组中的位置（索引）
	 * 
	 * @param zoomLevel
	 * @return
	 */
	private static int getScaleIndex(int zoomLevel) {
		return MAX_LEVEL - zoomLevel;
	}

	/**
	 * 根据缩放级别，得到对应比例尺
	 * 
	 * @param zoomLevel
	 * @return
	 */
	public static int getScale(int zoomLevel) {
		return SCALES[getScaleIndex(zoomLevel)];
	}

	/**
	 * 根据缩放级别，得到对应比例尺文字
	 * 
	 * @param zoomLevel
	 * @return
	 */
	public static String getScaleDesc(int zoomLevel) {
		return SCALE_DESCS[getScaleIndex(zoomLevel)];
	}

	/**
	 * 根据地图当前中心位置的纬度，当前比例尺，得出比例尺图标应该显示多长（多少像素）
	 * 
	 * @param map
	 * @param scale
	 * @return
	 */
	public static int meterToPixels(MapView map, int scale) {
		int mScaleMaxWidth;
		int mScaleMaxHeight;
		mScaleMaxWidth = widthWin >> 2;
		mScaleMaxHeight = heightWin >> 1;
		Point stpoint = new Point(0, mScaleMaxHeight);
		Point edpoint = new Point(mScaleMaxWidth, mScaleMaxHeight);
		double distance = DistanceUtil.getDistance(map.getMap().getProjection()
				.fromScreenLocation(stpoint), map.getMap().getProjection()
				.fromScreenLocation(edpoint));
		int dis = 0;
		int width = 20;
		if (map.getMap().getMapStatus().zoom == 19) {
			dis = SCALES[0];
			width = 52;
		} else if (map.getMap().getMapStatus().zoom == 3) {
			dis = SCALES[16];
			width = 80;
		} else {
			for (int i = 1; i < SCALES.length; i++) {
				if (SCALES[i - 1] <= distance && distance < SCALES[i]) {
					dis = SCALES[i - 1];
					break;
				}
			}
			width = (int) (dis * mScaleMaxWidth / distance);
		}
		return width;

	}

	/**
	 * 设置比例尺的宽度
	 * 
	 * @param scaleWidth
	 */
	public void setScaleWidth(int scaleWidth) {
		this.scaleWidth = scaleWidth;
	}

	/**
	 * 设置比例尺的上面的 text 例如 200公里
	 * 
	 * @param text
	 */
	private void setText(String text) {
		this.text = text;
	}

	/**
	 * 设置字体大小
	 * 
	 * @param textSize
	 */
	public void setTextSize(int textSize) {
		this.textSize = textSize;
		invalidate();
	}

	/**
	 * 根据缩放级别更新ScaleView的文字以及比例尺的长度
	 * 
	 * @param level
	 */
	public void refreshScaleView(int level) {
		if (mapView == null) {
			throw new NullPointerException(
					"you can call setMapView(MapView mapView) at first");
		}
		String text = getScaleDesc(level);
		setText(text);
		setScaleWidth(meterToPixels(mapView, getScale(level)));
		invalidate();
	}
}
