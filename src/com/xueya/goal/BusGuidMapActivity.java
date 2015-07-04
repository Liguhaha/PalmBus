package com.xueya.goal;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.Header;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.xueya.palmbus.R;
import com.xueya.tools.JsonTools;
import com.xueya.tools.MyTransitRouteResult;
import com.xueya.tools.ZoomControlView;

public class BusGuidMapActivity extends Activity implements OnClickListener {

	private MyTransitRouteResult myTransitRouteResult = null;
	private ImageView btLocation;
	private ZoomControlView mZoomControlView;
	private MapView mMapView = null;
	private LinearLayout llMapView = null;
	private BaiduMap mBaiduMap = null;
	private LocationClient mClient = null;// 定位服务的客户端
	private LocationMode mCurrentMode = null;
	private LocationManager locManager;
	public MyLocationListenner myLocationListenner = new MyLocationListenner();
	boolean isFirstLoc = true;// 是否首次定位
	private LatLng ll;// 当前位置经纬度
	private int flag = 0;
	private BitmapDescriptor mCurrentMarker;
	private MyGpsListener gps_Listener = new MyGpsListener();

	private int getIntent = -1;// 传递进来的值

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_guidmap);

		// 获取传递进来的值
		Intent intent = getIntent();
		getIntent = intent.getIntExtra("index", -1);
		System.out.println("---"+getIntent);

		myTransitRouteResult = MyTransitRouteResult.getMyTransitRouteLine();
		init();
	}

	private void init() {
		setMyTitle(R.layout.title_normal);
		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle.setText("路径规划");
		ivBack.setOnClickListener(this);

		// 找到组件
		btLocation = (ImageView) findViewById(R.id.iv_location);// 定位
		btLocation.setOnClickListener(this);
		mZoomControlView = (ZoomControlView) findViewById(R.id.zoomcontrolview);// 自定义缩放控件
		llMapView = (LinearLayout) findViewById(R.id.ll_mapview);

		// 初始化地图状态
		MapStatus mapStatus = new MapStatus.Builder().zoom(12.0f).build();
		BaiduMapOptions mapOptions = new BaiduMapOptions();
		mapOptions.zoomControlsEnabled(false).scaleControlEnabled(true)
				.mapStatus(mapStatus);
		mMapView = new MapView(this, mapOptions);
		llMapView.addView(mMapView);
		mBaiduMap = mMapView.getMap();
		mZoomControlView.setMapView(mMapView);

		// 定位相关
		mBaiduMap.setMyLocationEnabled(true);// 开启定位图层
		mClient = new LocationClient(this);
		mClient.registerLocationListener(myLocationListenner);
		// 百度定位
		mCurrentMode = LocationMode.NORMAL;
		// GPS定位
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// autoLocation();

		mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

			@Override
			public void onMapLoaded() {
				TransitRouteResult result = myTransitRouteResult
						.getMyTransitRouteResult();
				TransitRouteLine routeLine = result.getRouteLines().get(getIntent);
				TransitRouteOverlay overlay = new MyTransitRouteOverlay(
						mBaiduMap);
				// 创建公交路线规划线路覆盖物
				overlay.setData(routeLine);
				// 将公交路线规划覆盖物添加到地图中
				overlay.addToMap();
				overlay.zoomToSpan();
			}

		});

		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChange(MapStatus status) {
				refreshScaleAndZoomControl();
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus status) {
			}

			@Override
			public void onMapStatusChangeStart(MapStatus status) {
			}
		});
	}

	/**
	 * 定位SDK监听函数
	 * 
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// mapView 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
			}
			Log.d("LocSDK", sb.toString());
			if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				MyLocationData locData = new MyLocationData.Builder()
						.accuracy(location.getRadius())
						// 此处设置开发者获取到的方向信息，顺时针0-360
						.direction(100).latitude(location.getLatitude())
						.longitude(location.getLongitude()).build();
				mBaiduMap.setMyLocationData(locData);
				if (isFirstLoc) {
					// System.out.println("网络定位01");
					isFirstLoc = false;
					ll = new LatLng(location.getLatitude(),
							location.getLongitude());
					myTransitRouteResult.setLatLng(ll);
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
					mBaiduMap.animateMapStatus(u);
					mClient.stop();// 关闭服务
				} else {
					// System.out.println("网络定位02");
					ll = new LatLng(location.getLatitude(),
							location.getLongitude());
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
					mBaiduMap.animateMapStatus(u);
				}

			}
		}
	}

	/**
	 * 用于自动定位
	 */
	private void autoLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mClient.setLocOption(option);
		mClient.start();
	}

	private class MyGpsListener implements LocationListener {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
			// 当GPS LocationProvider 可用时，更新位置
			updateView(locManager.getLastKnownLocation(provider));
		}

		@Override
		public void onProviderDisabled(String provider) {
			updateView(null);
		}

		@Override
		public void onLocationChanged(Location location) {
			// 当GPS定位信息发生改变时，更新位置
			updateView(location);
		}
	}

	/**
	 * 用于GPS定位
	 */
	public void updateView(Location newLocation) {
		if (newLocation != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("实时的位置信息:\n");
			sb.append("经度：\n");
			sb.append(newLocation.getLongitude());
			sb.append("\n 纬度：");
			sb.append(newLocation.getLatitude());
			sb.append("\n 高度：");
			sb.append(newLocation.getAltitude());
			sb.append("\n 速度：");
			sb.append(newLocation.getSpeed());
			sb.append("\n 方向：");
			sb.append(newLocation.getBearing());
			Log.d("LocGPS", sb.toString());
			toBaiduLoc(newLocation.getLongitude(), newLocation.getLatitude());
		} else {
			Log.d("LocGPS", "not run");
			autoLocation();
		}
	}

	/**
	 * 转换坐标方法
	 * 
	 * @param lng
	 *            经度
	 * @param lat
	 *            纬度
	 * @return
	 */
	private StringBuilder toBaiduLoc(double lng, double lat) {

		AsyncHttpClient client = new AsyncHttpClient();
		String url = "http://api.map.baidu.com/geoconv/v1/?coords=" + lng + ","
				+ lat + "&ak=t6HifKVGPfWli2acqLUhVjVG";

		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] data) {

				System.out.println(new String(data));
				JSONObject jo = JsonTools.getJsonObj(new String(data));
				JSONArray result = JsonTools.getJsonObjs("result", jo);
				final double lng = result.getJSONObject(0).getDouble("x");
				final double lat = result.getJSONObject(0).getDouble("y");

				MyLocationData locData = new MyLocationData.Builder()
						.accuracy(97.256254f).direction(100).latitude(lat)
						.longitude(lng).build();
				if (mBaiduMap != null) {
					// System.out.println("GPS定位");
					mBaiduMap.setMyLocationData(locData);
					ll = new LatLng(lat, lng);
					myTransitRouteResult.setLatLng(ll);
					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
					mBaiduMap.animateMapStatus(u);
				}
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
			}
		});
		return null;
	}

	/**
	 * 设置标题栏
	 */
	public void setMyTitle(int resID) {
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(resID);
		actionBar.setDisplayShowCustomEnabled(true); // 使自定义的普通View能在title栏显示
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();
	}

	/**
	 * 更新缩放按钮的状态
	 */
	public void refreshScaleAndZoomControl() {
		mZoomControlView.refreshZoomButtonStatus(mBaiduMap.getMapStatus().zoom);
	}

	private class MyTransitRouteOverlay extends TransitRouteOverlay {

		public MyTransitRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 0:
			if (locManager
					.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						5000, 8, gps_Listener);
			} else {
				autoLocation();
			}
			break;
		}
	}

	/**
	 * 查看GPS是否打开
	 */
	public void initGPS() {
		// 判断GPS模块是否开启，如果没有则开启
		final AlertDialog alertDialog;
		if (!locManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			alertDialog = new AlertDialog.Builder(this).setTitle("开启GPS定位")
					.setMessage("打开GPS提高定位精度").setPositiveButton("设置", null)
					.setNegativeButton("取消", null).create();
			alertDialog.show();
			// 设置 按钮
			Button button = alertDialog
					.getButton(DialogInterface.BUTTON_POSITIVE);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 转到手机设置界面，用户设置GPS
					Intent intent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivityForResult(intent, 0);// 设置完成后返回到原来的界面
					alertDialog.dismiss();
				}
			});
			// 取消 按钮
			Button button2 = alertDialog
					.getButton(DialogInterface.BUTTON_NEGATIVE);
			button2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.dismiss();
					autoLocation();
				}
			});
		} else {
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					5000, 8, gps_Listener);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:// 回退
			this.finish();
			break;
		case R.id.iv_location:// 定位
			if (flag == 0) {
				autoLocation();
				flag = 1;
				break;
			} else {
				switch (mCurrentMode) {
				case FOLLOWING:
					mCurrentMode = LocationMode.NORMAL;
					btLocation.setImageDrawable(getResources().getDrawable(
							R.drawable.main_icon_location));
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					locManager.removeUpdates(gps_Listener);
					break;
				case NORMAL:
					mCurrentMode = LocationMode.FOLLOWING;
					btLocation.setImageDrawable(getResources().getDrawable(
							R.drawable.main_icon_follow));
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					initGPS();
					break;
				default:
					break;
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		mBaiduMap = null;
		if (mClient != null)
			mClient.stop();
		if (mMapView != null)
			mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		if (mMapView != null)
			mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mMapView != null)
			mMapView.onPause();
		super.onPause();
	}
}
