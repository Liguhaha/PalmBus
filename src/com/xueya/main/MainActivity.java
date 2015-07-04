package com.xueya.main;

import java.io.File;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.Header;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.xueya.marker.MarKerInfo;
import com.xueya.near.NearActivity;
import com.xueya.near.NearInterest;
import com.xueya.palmbus.R;
import com.xueya.tools.JsonTools;
import com.xueya.tools.MyTransitRouteResult;
import com.xueya.tools.ScaleView;
import com.xueya.tools.ZoomControlView;

public class MainActivity extends Activity implements OnClickListener {
	private Button btBus = null;// 公交
	private Button btMyInfo = null;// 我的信息
	private Button btSign = null;
	private Button btNavi = null;
	private ImageView btRoad;
	private ImageView btLocation;
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private LinearLayout mMapViewLayout = null;
	// private LinearLayout llBtgroup = null;
	private LocationClient mClient = null;// 定位服务的客户端
	private MyLocationConfiguration.LocationMode mCurrentMode = null;
	private LocationManager locManager;
	private MyGpsListener gps_Listener = new MyGpsListener();
	boolean isFirstLoc = true;// 是否首次定位
	private Boolean road_flag = true;
	private BitmapDescriptor mCurrentMarker;
	private ScaleView mScaleView;
	private int width, height;
	private String text, strAddr;
	private Bitmap bitmap;
	private ImageButton ibSearch = null;
	private Marker marker;
	private PopupWindow popupWindow;
	private View contentView;
	private LatLng ll;// 当前位置经纬度
	public MyLocationListenner myLocationListenner = new MyLocationListenner();
	/**
	 * 用MapController完成地图控制
	 */
	private ZoomControlView mZoomControlView;
	private MyTransitRouteResult myTransitRouteResult = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 初始化地图
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// 初始化组件
		init();
		// 定位
		System.out.println("onCreate()");
	}

	// 初始化组件
	private void init() {
		// setMyTitle(R.layout.title_main);

		myTransitRouteResult = MyTransitRouteResult.getMyTransitRouteLine();

		// 获取地图控件引用
		mMapViewLayout = (LinearLayout) findViewById(R.id.mapView);
		// 找到组件
		btBus = (Button) findViewById(R.id.bt_bus);// 公交
		btMyInfo = (Button) findViewById(R.id.bt_care);// 我的信息
		btRoad = (ImageView) findViewById(R.id.road_map);// 路况
		btLocation = (ImageView) findViewById(R.id.iv_posi);// 定位
		btSign = (Button) findViewById(R.id.bt_sign);// 标记
		btNavi = (Button) findViewById(R.id.bt_near);
		ibSearch = (ImageButton) findViewById(R.id.ib_search);
		// llBtgroup = (LinearLayout) findViewById(R.id.ll_btgroup);
		mZoomControlView = (ZoomControlView) findViewById(R.id.zoomcontrolview);
		mScaleView = (ScaleView) findViewById(R.id.scaleview);

		// 点击事件
		btRoad.setOnClickListener(this);// 实时路况按钮
		btLocation.setOnClickListener(this);
		btBus.setOnClickListener(this);
		btNavi.setOnClickListener(this);
		btMyInfo.setOnClickListener(this);
		btSign.setOnClickListener(this);
		ibSearch.setOnClickListener(this);

		// 初始化地图状态
		MapStatus mapStatus = new MapStatus.Builder().zoom(12.0f).build();
		BaiduMapOptions mapOptions = new BaiduMapOptions();
		mapOptions.zoomControlsEnabled(false).scaleControlEnabled(false)
				.mapStatus(mapStatus);
		mMapView = new MapView(this, mapOptions);
		mMapViewLayout.addView(mMapView);
		mBaiduMap = mMapView.getMap();
		WindowManager wm = (WindowManager) getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		ScaleView.setMapView(mMapView, width, height);
		mZoomControlView.setMapView(mMapView);

		// 定位相关
		mBaiduMap.setMyLocationEnabled(true);// 开启定位图层
		mClient = new LocationClient(this);
		mClient.registerLocationListener(myLocationListenner);
		// 百度定位
		mCurrentMode = LocationMode.NORMAL;
		// GPS定位
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		autoLocation();

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
	 * 设置标题栏
	 */
	public void setMyTitle(int resID) {
		ActionBar actionBar = getActionBar();
		// actionBar.setCustomView(R.layout.title_main);
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
		mScaleView
				.refreshScaleView((int) Math.ceil(mBaiduMap.getMapStatus().zoom));
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
					strAddr = location.getAddrStr();
					ll = new LatLng(location.getLatitude(),
							location.getLongitude());
					myTransitRouteResult.setLatLng(ll);
					myTransitRouteResult.setCITY(location.getCity());
					myTransitRouteResult.setBdLocation(location);
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
	 * 开启GPS设置界面自动回调
	 */
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
		case 1:
			// 获得标记结果
			if (resultCode == 1) {
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				final int pre = sp.getInt("index", 0);
				String strPath = sp.getString("path", null);
				text = sp.getString("text", null);
				bitmap = getPhoto(strPath, 210, 200);
				markerAdd(pre);
				mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
					@Override
					public boolean onMarkerClick(final Marker marker1) {
						contentView = LayoutInflater.from(
								getApplicationContext()).inflate(
								R.layout.popupwindow, null);
						contentView.setBackgroundColor(Color.WHITE);
						popupWindow = new PopupWindow(getApplicationContext());
						popupWindow.setContentView(contentView);
						popupWindow
								.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
						popupWindow
								.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
						popupWindow.setBackgroundDrawable(new BitmapDrawable());
						popupWindow.setFocusable(false);
						popupWindow.setOutsideTouchable(true);
						popupWindow.showAsDropDown(btRoad);
						markerIfo(marker1, pre);
						return true;
					}
				});
			}
			break;
		}

	}

	/**
	 * 获取显示图片
	 * 
	 * @param path
	 * @return
	 */
	public Bitmap getPhoto(String path, int newWidth, int newHeight) {
		File file = new File(path);
		if (file.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(path);
			return bm;
		}
		return null;
	}

	/**
	 * 设置显示图片文字
	 * 
	 * @param marker
	 */
	public void markerIfo(final Marker marker1, int pre) {
		EditText tvDescribe = (EditText) contentView.findViewById(R.id.pop_tv);
		ImageView iv = (ImageView) contentView.findViewById(R.id.pop_iv);
		ImageView ivStatus = (ImageView) contentView
				.findViewById(R.id.iv_status);
		TextView tvAddr = (TextView) contentView.findViewById(R.id.tv_addr);
		ImageView ivDelete = (ImageView) contentView
				.findViewById(R.id.pop_delete);
		ImageView ivHappy = (ImageView) contentView
				.findViewById(R.id.pop_happy);
		ImageView ivAngry = (ImageView) contentView
				.findViewById(R.id.pop_angry);
		if (pre == 0) {
			String string = "堵车" + "\n" + strAddr;
			tvAddr.setText(string);
			ivStatus.setImageDrawable(getResources().getDrawable(
					R.drawable.report_icon_jam2));
		} else if (pre == 1) {
			String string = "事故" + "\n" + strAddr;
			tvAddr.setText(string);
			ivStatus.setImageDrawable(getResources().getDrawable(
					R.drawable.report_icon_accident2));
		} else if (pre == 2) {
			String string = "施工" + "\n" + strAddr;
			tvAddr.setText(string);
			ivStatus.setImageDrawable(getResources().getDrawable(
					R.drawable.report_icon_construction2));
		} else if (pre == 3) {
			String string = "执法" + "\n" + strAddr;
			tvAddr.setText(string);
			ivStatus.setImageDrawable(getResources().getDrawable(
					R.drawable.report_icon_police2));
		} else if (pre == 4) {
			String string = "管制" + "\n" + strAddr;
			tvAddr.setText(string);
			ivStatus.setImageDrawable(getResources().getDrawable(
					R.drawable.report_icon_control2));
		} else if (pre == 5) {
			String string = "积水" + "\n" + strAddr;
			tvAddr.setText(string);
			ivStatus.setImageDrawable(getResources().getDrawable(
					R.drawable.report_icon_ponding2));
		}
		tvDescribe.setTextColor(Color.rgb(105, 105, 105));
		tvDescribe.setWidth(width);
		if (bitmap == null) {
			tvDescribe.setText(text);
			iv.setVisibility(View.GONE);
		} else {
			tvDescribe.setText(text);
			iv.setImageBitmap(bitmap);
		}
		ivDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				marker1.remove();
				popupWindow.dismiss();
			}
		});
	}

	/**
	 * 添加标记
	 * 
	 * @param pre
	 */
	public void markerAdd(int pre) {
		if (pre == 0) {
			BitmapDescriptor bd = BitmapDescriptorFactory
					.fromResource(R.drawable.report_icon_jam2);
			OverlayOptions options = new MarkerOptions().position(ll).icon(bd);
			marker = (Marker) (mBaiduMap.addOverlay(options));
		} else if (pre == 1) {
			BitmapDescriptor bd = BitmapDescriptorFactory
					.fromResource(R.drawable.report_icon_accident2);
			OverlayOptions options = new MarkerOptions().position(ll).icon(bd);
			marker = (Marker) (mBaiduMap.addOverlay(options));
		} else if (pre == 2) {
			BitmapDescriptor bd = BitmapDescriptorFactory
					.fromResource(R.drawable.report_icon_construction2);
			OverlayOptions options = new MarkerOptions().position(ll).icon(bd);
			marker = (Marker) (mBaiduMap.addOverlay(options));
		} else if (pre == 3) {
			BitmapDescriptor bd = BitmapDescriptorFactory
					.fromResource(R.drawable.report_icon_police2);
			OverlayOptions options = new MarkerOptions().position(ll).icon(bd);
			marker = (Marker) (mBaiduMap.addOverlay(options));
		} else if (pre == 4) {
			BitmapDescriptor bd = BitmapDescriptorFactory
					.fromResource(R.drawable.report_icon_control2);
			OverlayOptions options = new MarkerOptions().position(ll).icon(bd);
			marker = (Marker) (mBaiduMap.addOverlay(options));
		} else if (pre == 5) {
			BitmapDescriptor bd = BitmapDescriptorFactory
					.fromResource(R.drawable.report_icon_ponding2);
			OverlayOptions options = new MarkerOptions().position(ll).icon(bd);
			marker = (Marker) (mBaiduMap.addOverlay(options));
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
		case R.id.bt_bus:// 公交
			Bundle busData = new Bundle();
			busData.putString("FROM", "GOAL_FRAGMENT");
			startActivity(new Intent(MainActivity.this, WayActivity.class)
					.putExtras(busData));
			break;
		case R.id.et_search:
			break;
		case R.id.bt_near:// 附近
			if (!myTransitRouteResult.getCITY().equals("")
					&& myTransitRouteResult.getBdLocation() != null
					&& myTransitRouteResult.getLatLng() != null) {
				Intent inNear = new Intent();
				inNear.setClass(this, NearActivity.class);
				startActivity(inNear);
			} else {
				Toast.makeText(this, "请等待定位成功", Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.bt_care:// 关注点击事件
			Intent inCare = new Intent();
			inCare.putExtra("FROM", "CARE_FRAGMENT");
			inCare.setClass(this, WayActivity.class);
			startActivity(inCare);
			break;
		case R.id.road_map:
			if (road_flag == true) {
				btRoad.setImageDrawable(getResources().getDrawable(
						R.drawable.main_icon_roadcondition_on));
				// 实时路况开启
				mBaiduMap.setTrafficEnabled(true);
				Toast.makeText(this, "实时路况已开启", Toast.LENGTH_SHORT).show();
				road_flag = false;
			} else {
				btRoad.setImageDrawable(getResources().getDrawable(
						R.drawable.main_icon_roadcondition_off));
				// 实时路况关闭
				mBaiduMap.setTrafficEnabled(false);
				Toast.makeText(this, "实时路况已关闭", Toast.LENGTH_SHORT).show();
				road_flag = true;
			}
			break;
		case R.id.iv_posi:
			switch (mCurrentMode) {
			case FOLLOWING:
				mCurrentMode = LocationMode.NORMAL;
				btLocation.setImageDrawable(getResources().getDrawable(
						R.drawable.main_icon_location));
				mBaiduMap
						.setMyLocationConfigeration(new MyLocationConfiguration(
								mCurrentMode, true, mCurrentMarker));
				locManager.removeUpdates(gps_Listener);
				mClient.stop();
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
			break;
		case R.id.bt_sign:
			if (ll != null) {
				Intent intent = new Intent(this, MarKerInfo.class);
				startActivityForResult(intent, 1);
			} else {
				Toast.makeText(getApplicationContext(), "网络异常或定位失败...",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.ib_search:
			if (!myTransitRouteResult.getCITY().equals("")
					&& myTransitRouteResult.getBdLocation() != null
					&& myTransitRouteResult.getLatLng() != null) {
				startActivity(new Intent(this, NearInterest.class));
			} else {
				Toast.makeText(this, "请等待定位成功", Toast.LENGTH_SHORT).show();
			}
			break;
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		System.out.println("onStart()");
	}

	@Override
	protected void onRestart() {
		System.out.println("onRestart()");
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		mBaiduMap = null;
		if (mClient != null)
			mClient.stop();
		if (mMapView != null)
			mMapView.onDestroy();
		System.out.println("onDestroy()");
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		System.out.println("onResume()");
		if (mMapView != null)
			mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mMapView != null)
			mMapView.onPause();
		System.out.println("onPause()");
		super.onPause();
	}

}
