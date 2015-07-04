package com.xueya.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.xueya.palmbus.R;

public class ZoomControlView extends RelativeLayout implements OnClickListener {
	private ImageView mImageViewZoomin;
	private ImageView mImageViewZoomout;
	private MapView mapView;
	private float maxZoomLevel;
	private float minZoomLevel;

	public ZoomControlView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ZoomControlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@SuppressLint("InflateParams") private void init() {
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.zoom_controls_layout, null);
		mImageViewZoomin = (ImageView) view.findViewById(R.id.zoomin);
		mImageViewZoomout = (ImageView) view.findViewById(R.id.zoomout);
		mImageViewZoomin.setOnClickListener(this);
		mImageViewZoomout.setOnClickListener(this);
		addView(view);
	}

	@Override
	public void onClick(View v) {
		if (mapView == null) {
			throw new NullPointerException(
					"you can call setMapView(MapView mapView) at first");
		}
		switch (v.getId()) {
		case R.id.zoomin: {
			MapStatusUpdate msu = MapStatusUpdateFactory.zoomIn();
			mapView.getMap().setMapStatus(msu);
			break;
		}
		case R.id.zoomout: {
			MapStatusUpdate msu = MapStatusUpdateFactory.zoomOut();
			mapView.getMap().setMapStatus(msu);
			break;
		}
		}
	}

	/**
	 * 与MapView设置关联
	 * 
	 * @param mapView
	 */
	public void setMapView(MapView mapView) {
		this.mapView = mapView;
		// 获取最大的缩放级别
		maxZoomLevel = mapView.getMap().getMaxZoomLevel();
		// 获取最大的缩放级别
		minZoomLevel = mapView.getMap().getMinZoomLevel();
	}

	/**
	 * 根据MapView的缩放级别更新缩放按钮的状态，当达到最大缩放级别，设置mButtonZoomin
	 * 为不能点击，反之设置mButtonZoomout
	 * 
	 * @param level
	 */
	public void refreshZoomButtonStatus(Float level) {
		if (level > minZoomLevel && level < maxZoomLevel) {
			if (!mImageViewZoomout.isEnabled()) {
				mImageViewZoomout.setEnabled(true);
			}
			if (!mImageViewZoomin.isEnabled()) {
				mImageViewZoomin.setEnabled(true);
			}
		} else if (level == minZoomLevel) {
			mImageViewZoomout.setEnabled(false);
		} else if (level == maxZoomLevel) {
			mImageViewZoomin.setEnabled(false);
		}
	}
}
