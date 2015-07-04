package com.xueya.near;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
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
import com.xueya.palmbus.R;
import com.xueya.tools.MyTransitRouteResult;
import com.xueya.tools.UiActivity;

public class NearPoi extends Activity implements OnGetPoiSearchResultListener ,OnGetRoutePlanResultListener,OnClickListener{
	private String getValue = "";
	private PoiSearch mPoiSearch = null;
	private LatLng latLng = null;
	private List<PoiInfo> poiInfos = new ArrayList<PoiInfo>();
	private MyAdapter mAdapter = null;
	private ListView lvPoiResult = null;
	private ProgressDialog pd = null;
	private RoutePlanSearch mRouteSearch = null;
	private ImageView ivBack = null;
	private String endPoint = "";
	private MyTransitRouteResult myTransitRouteResult = MyTransitRouteResult
			.getMyTransitRouteLine();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_near_poi);
		// 获取传递进来的值
		getValue = getIntent().getStringExtra("INTEREST_KEY");
		UiActivity.setMyTitle(R.layout.title_normal, this);
		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("附近" + getValue);
		lvPoiResult = (ListView) findViewById(R.id.lv_near_poi);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		latLng = myTransitRouteResult.getLatLng();// 获取当前坐标
		startSearch(getValue);
		mRouteSearch = RoutePlanSearch.newInstance();// 创建路线规划检索实例
		mRouteSearch.setOnGetRoutePlanResultListener(this);

		mAdapter = new MyAdapter();
		lvPoiResult.setAdapter(mAdapter);

	}

	/**
	 * 发起搜索
	 * 
	 * @param key
	 *            搜索的关键字
	 */
	public void startSearch(String key) {
		pd = ProgressDialog.show(this, null, "正在获取数据，请稍后..", false,
				true);
		mPoiSearch.searchNearby(new PoiNearbySearchOption().location(latLng)
				.keyword(key.toString().trim()));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		super.onDestroy();
		if (mPoiSearch != null)
			mPoiSearch.destroy();
		if(mRouteSearch!=null)
			mRouteSearch.destroy();
	}

	// 兴趣点检索接口
	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		mAdapter.notifyDataSetChanged();
		pd.dismiss(); 
		if (result.getAllPoi() != null) {
			poiInfos = result.getAllPoi();
		}
		lvPoiResult.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				PlanNode planNodeSt = PlanNode.withLocation(latLng);//起点坐标
				endPoint = poiInfos.get(position).name;
				PlanNode planNodeEn = PlanNode.withLocation(poiInfos.get(position).location);//终点坐标
				WalkingRoutePlanOption walkingRoutePlanOption = new WalkingRoutePlanOption().from(planNodeSt).to(planNodeEn);
				mRouteSearch.walkingSearch(walkingRoutePlanOption);
				
			}
		});
	}
	
	
	
	
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
		}
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
				LayoutInflater mInflater = LayoutInflater.from(NearPoi.this);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;

		default:
			break;
		}
	}
}
