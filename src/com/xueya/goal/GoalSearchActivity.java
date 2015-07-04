package com.xueya.goal;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionResult.SuggestionInfo;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.xueya.bean.MyPlanNode;
import com.xueya.main.WayActivity;
import com.xueya.palmbus.R;
import com.xueya.tools.MyTransitRouteResult;

public class GoalSearchActivity extends Activity implements
		OnGetSuggestionResultListener, OnClickListener {
	private SuggestionSearch mSuggestionSearch = null;
	private Button btSearch = null;// 搜索按钮
	private EditText etPlace = null;// 地址输入框
	private ImageView ivBack = null;// 后退
	private ListView lvSugInfo = null;
	private String PLACE;// 获取传递进来的值
	private String CITY = "北京";// 当前所在城市 默认是北京

	// 定位相关
	private LocationClient mLocationClient = null;
	private LocationClientOption option = null;
	private BDLocationListener myListener = new MyLocationListener();

	private MyTransitRouteResult myTransitRouteResult = MyTransitRouteResult
			.getMyTransitRouteLine();

	private List<SuggestionResult.SuggestionInfo> sugInfo = new ArrayList<SuggestionResult.SuggestionInfo>();
	private MyAdapter mAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 初始化
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.activity_goal_search);

		init();// 初始化标题、找到组件
		initMap();// 初始化搜索模块，注册搜索事件

		mAdapter = new MyAdapter(GoalSearchActivity.this);
		lvSugInfo.setAdapter(mAdapter);
	}

	// 初始化标题 、找到组件
	private void init() {
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.title_search);
		actionBar.setDisplayShowCustomEnabled(true); // 使自定义的普通View能在title栏显示
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();

		PLACE = getIntent().getExtras().get("place") + "";

		btSearch = (Button) findViewById(R.id.bt_search);
		btSearch.setOnClickListener(this);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);
		lvSugInfo = (ListView) findViewById(R.id.lv_suggestion);
		etPlace = (EditText) findViewById(R.id.et_search);

		System.out.println("当前城市为：" + myTransitRouteResult.getCITY());
		// 如果城市为空的话 重新定位
		if (myTransitRouteResult.getCITY().equals("")) {
			mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
			mLocationClient.registerLocationListener(myListener); // 注册监听函数
			option = new LocationClientOption();
			option.setOpenGps(true);// 打开gps
			// option.setCoorType("bd09ll"); // 设置坐标类型
			// option.setScanSpan(1000);// 设置扫描间隔/毫秒
			option.setIsNeedAddress(true);// 设置是否需要地址信息 默认为false
			mLocationClient.setLocOption(option);
			System.out.println("定位");
			mLocationClient.start();
		} else {
			CITY = myTransitRouteResult.getCITY();
		}

		//监听输入框字符变化
		etPlace.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() <= 0)
					return;
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(s.toString().trim()).city(CITY));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	// 初始化搜索模块，注册搜索事件监听
	private void initMap() {
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
			// 未找到相关结果
		}
		// 清理集合中以前的数据
		sugInfo.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null)
				sugInfo.add(info);
		}
		mAdapter.notifyDataSetChanged();

		lvSugInfo.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (PLACE.equals("start")) {// 从起点传进来的
					MyPlanNode stPlanNode = new MyPlanNode();
					stPlanNode.setCity(sugInfo.get(position).city);
					stPlanNode.setDistrict(sugInfo.get(position).district);
					stPlanNode.setKey(sugInfo.get(position).key);
					myTransitRouteResult.setStPlanNode(stPlanNode);
//					Toast.makeText(GoalSearchActivity.this, "起点", 0).show();
				} else if (PLACE.equals("end")) {

					MyPlanNode enPlanNode = new MyPlanNode();
					enPlanNode.setCity(sugInfo.get(position).city);
					enPlanNode.setDistrict(sugInfo.get(position).district);
					enPlanNode.setKey(sugInfo.get(position).key);
					myTransitRouteResult.setEnPlanNode(enPlanNode);
//					Toast.makeText(GoalSearchActivity.this, "终点", 0).show();
				}
				Intent intent = new Intent(GoalSearchActivity.this,
						WayActivity.class);
				intent.putExtra("FROM", "GOAL_FRAGMENT");
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_search:
			String place = etPlace.getText().toString().trim();
			boolean flag = mSuggestionSearch
					.requestSuggestion((new SuggestionSearchOption()).keyword(
							place).city(CITY));
			if (!flag)
				Toast.makeText(this, "检索失败,请重试！", Toast.LENGTH_SHORT).show();
			break;
		case R.id.iv_back:
			finish();
		default:
			break;
		}
	}

	class MyAdapter extends BaseAdapter {
		private Context mContext = null;

		public MyAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return sugInfo.size();
		}

		@Override
		public Object getItem(int position) {
			return sugInfo.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater mInflater = LayoutInflater.from(mContext);
				convertView = mInflater.inflate(R.layout.list_item_suginfo,
						null);

				holder.tvKey = (TextView) convertView.findViewById(R.id.tv_key);
				holder.tvCity = (TextView) convertView
						.findViewById(R.id.tv_city);
				holder.tvDistrict = (TextView) convertView
						.findViewById(R.id.tv_district);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			SuggestionResult.SuggestionInfo info = (SuggestionInfo) getItem(position);
			if (info != null) {
				holder.tvKey.setText(info.key);
				holder.tvCity.setText(info.city);
				holder.tvDistrict.setText(info.district);
			}

			return convertView;
		}

		public final class ViewHolder {
			public TextView tvKey;
			public TextView tvCity;
			public TextView tvDistrict;
		}

	}

	// 定位
	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation bdLocation) {
			if (bdLocation == null)
				return;

			switch (bdLocation.getLocType()) {
			case 161:
				if (bdLocation.getCity() != null) {
					System.out.println("详细地址信息：" + bdLocation.getAddrStr());
					System.out.println("街道信息：" + bdLocation.getStreet());
					CITY = bdLocation.getCity();
					myTransitRouteResult.setCITY(CITY);
					option.setOpenGps(false);// 关闭gps
					System.out.println(CITY);
				}
				break;
			case 68:
				Toast.makeText(GoalSearchActivity.this, "网络连接失败，请检查网络设置",
						Toast.LENGTH_SHORT).show();
				break;
			case 62:
				Toast.makeText(GoalSearchActivity.this, "向服务器请求数据失败",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				if (bdLocation.getCity() != null) {
					CITY = bdLocation.getCity();
					myTransitRouteResult.setCITY(CITY);
					option.setOpenGps(false);// 关闭gps
					System.out.println(CITY);
				}
				break;
			}

		}
	}

	@Override
	protected void onDestroy() {
		mSuggestionSearch.destroy();
		// 退出时销毁定位
		if (mLocationClient != null)
			mLocationClient.stop();
		if (option != null) {
			option.setOpenGps(false);// 关闭gps
		}
		super.onDestroy();
	}

}
