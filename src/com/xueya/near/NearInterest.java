package com.xueya.near;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.xueya.bean.MyPlanNode;
import com.xueya.main.WayActivity;
import com.xueya.palmbus.R;
import com.xueya.tools.MyTransitRouteResult;
import com.xueya.tools.UiActivity;

public class NearInterest extends Activity implements OnClickListener,
		TextWatcher, OnGetPoiSearchResultListener, OnGetRoutePlanResultListener {
	private PoiSearch mPoiSearch = null;
	private EditText etSearch = null;
	private Button btSearch = null;
	private LatLng latLng = null;
	private MyAdapter mAdapter = null;
	private ListView lvPoiResult = null;
	private String endPoint = "";
	private List<PoiInfo> poiInfos = new ArrayList<PoiInfo>();
	private MyTransitRouteResult myTransitRouteResult = MyTransitRouteResult
			.getMyTransitRouteLine();
	private RoutePlanSearch mRouteSearch = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_near_interest);
		// 创建POI检索实例
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		latLng = myTransitRouteResult.getLatLng();// 获取当前坐标
		mRouteSearch = RoutePlanSearch.newInstance();// 创建路线规划检索实例
		mRouteSearch.setOnGetRoutePlanResultListener(this);

		UiActivity.setMyTitle(R.layout.title_search, this);// 设置标题
		ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);
		btSearch = (Button) findViewById(R.id.bt_search);
		btSearch.setOnClickListener(this);
		etSearch = (EditText) findViewById(R.id.et_search);
		etSearch.addTextChangedListener(this);
		lvPoiResult = (ListView) findViewById(R.id.lv_near_poi);

		mAdapter = new MyAdapter();
		lvPoiResult.setAdapter(mAdapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPoiSearch != null)
			mPoiSearch.destroy();
		if(mRouteSearch!=null)
			mRouteSearch.destroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.bt_search:
			startSearch(etSearch.getText().toString());
			break;
		default:
			break;
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after) {
		if (s.length() <= 0)
			return;
		if (latLng == null) {
			Toast.makeText(this, "无法确定当前位置", Toast.LENGTH_SHORT).show();
		} else {
			System.out.println("###发起搜索了！！！");
			startSearch(s.toString());
		}
	}

	public void startSearch(String s) {
		mPoiSearch.searchNearby(new PoiNearbySearchOption().location(latLng)
				.keyword(s.toString().trim()));
		// mPoiSearch.searchInCity((new
		// PoiCitySearchOption().city("怀化").keyword(s.toString())));
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int before,
			int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		mAdapter.notifyDataSetChanged();
		if (result.getAllPoi() != null) {
			poiInfos = result.getAllPoi();
		}
		lvPoiResult.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				startBusSearch(position);
				endPoint = poiInfos.get(position).name;
				PlanNode planNodeSt = PlanNode.withLocation(latLng);//起点坐标
				PlanNode planNodeEn = PlanNode.withLocation(poiInfos.get(position).location);//终点坐标
				WalkingRoutePlanOption walkingRoutePlanOption = new WalkingRoutePlanOption().from(planNodeSt).to(planNodeEn);
				mRouteSearch.walkingSearch(walkingRoutePlanOption);
				
			}
		});
	}

	/**
	 * 发起公交检索
	 */
	public void startBusSearch(int position) {
		// 开始检索公交
		// LatLng endLatLng = poiInfos.get(position).location;
		MyPlanNode myPlanNode = new MyPlanNode();
		PoiInfo poiInfo = poiInfos.get(position);
		myPlanNode.setCity(poiInfo.city);
		myPlanNode.setKey(poiInfo.name);
		myTransitRouteResult.setEnPlanNode(myPlanNode);
		Intent inBus = new Intent();
		inBus.putExtra("FROM", "NEAR_INTEREST");
		inBus.setClass(NearInterest.this, WayActivity.class);
		startActivity(inBus);
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return poiInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return poiInfos.get(position);
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
				LayoutInflater mInflater = LayoutInflater
						.from(NearInterest.this);
				convertView = mInflater.inflate(R.layout.list_item_poi, parent,
						false);
				holder.tvName = (TextView) convertView
						.findViewById(R.id.tv_name);
				holder.tvAddress = (TextView) convertView
						.findViewById(R.id.tv_address);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			PoiInfo info = (PoiInfo) getItem(position);
			if (info != null) {
				holder.tvName.setText(info.name);
				holder.tvAddress.setText(info.address);
			}
			return convertView;
		}

		public final class ViewHolder {
			TextView tvName = null;
			TextView tvAddress = null;
		}

	}

	// 路线规划接口方法
	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			result.getSuggestAddrInfo();
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			myTransitRouteResult.setMyWalkingRouteResult(result);//保存步行结果
			//跳转到地图指示界面
			Intent inWalkMap = new Intent();
			inWalkMap.putExtra("index", 0);
			inWalkMap.putExtra("END_POINT", endPoint);
			inWalkMap.setClass(this, WalkingMapActivity.class);
			startActivity(inWalkMap);
//			List<WalkingRouteLine> listWalking = result.getRouteLines();// 获取所有步行规划路线
/*			Iterator<WalkingRouteLine> it = listWalking.iterator();
			while (it.hasNext()) {
				WalkingRouteLine walk = it.next();
				Log.i("route",
						"线路名称" + walk.getTitle() + "线路长度" + walk.getDistance()
								+ "线路耗时" + walk.getDuration());

			}*/
		}
	}
}
