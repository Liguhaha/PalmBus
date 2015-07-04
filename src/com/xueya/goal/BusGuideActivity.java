package com.xueya.goal;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine.TransitStep;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.xueya.palmbus.R;
import com.xueya.tools.MyTransitRouteResult;

public class BusGuideActivity extends Activity implements OnClickListener {

	private MyTransitRouteResult myTransitRouteResult = null;
	private List<TransitRouteLine.TransitStep> listTransitSteps = null;
	private ListView lvGuide = null;
	private ImageView ivBack = null;// 后退

	private TextView tvBusNum = null;// 车辆编号
	private TextView tvStaNum = null;// 站台数量
	private TextView tvWalkDis = null;// 步行距离
	private TextView tvDuration = null;// 所需时间

	private LinearLayout llMapGuid = null; // 地图浏览

	private int getN = -1;// 传递进来的值

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_guide);

		init();

		lvGuide.setAdapter(new MyAdapter(this));
	}

	// 初始化、找到组件
	public void init() {

		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.title_normal);
		actionBar.setDisplayShowCustomEnabled(true); // 使自定义的普通View能在title栏显示
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();

		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);
		tvBusNum = (TextView) findViewById(R.id.tv_bus_num);
		tvStaNum = (TextView) findViewById(R.id.tv_station_num);
		tvWalkDis = (TextView) findViewById(R.id.tv_walk_dis);
		tvDuration = (TextView) findViewById(R.id.tv_need_time);
		lvGuide = (ListView) findViewById(R.id.lv_bus_guide);
		llMapGuid = (LinearLayout) findViewById(R.id.ll_mapguid);
		llMapGuid.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TransitRouteResult result = myTransitRouteResult
						.getMyTransitRouteResult();
				TransitRouteLine routeLine = result.getRouteLines().get(getN);
				boolean flag = true;
				for (int i = 0; i < routeLine.getAllStep()
						.size(); i++) {
					if (routeLine.getAllStep().get(i)
							.getStepType() == null) {
						flag = false;break;
					}
				}
				if(flag){
					Intent mMap = new Intent(BusGuideActivity.this,
							BusGuidMapActivity.class);
					mMap.putExtra("index", getN);
					startActivity(mMap);
				}else{
					Toast.makeText(BusGuideActivity.this, "无法预览线路", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// 获取传递进来的值
		Intent intent = getIntent();
		getN = intent.getIntExtra("position", -1);

		myTransitRouteResult = MyTransitRouteResult.getMyTransitRouteLine();// 获取所有结果
		listTransitSteps = myTransitRouteResult.getTransitSteps(getN);// 获取换乘路段集合

		String[] str = myTransitRouteResult.getmTransitStep(getN);
		// [0]车辆编号 1站台数量 2步行距离
		tvBusNum.setText(str[0]);
		tvStaNum.setText("共" + str[1] + "站");
		tvDuration.setText("大约" + myTransitRouteResult.getAllDuration(getN)
				/ 60 + "分钟");
		tvWalkDis.setText("步行" + str[2] + "米");
	}

	class MyAdapter extends BaseAdapter {

		private Context mContext = null;

		public MyAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return listTransitSteps.size();
		}

		@Override
		public Object getItem(int position) {
			return listTransitSteps.get(position);
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
				convertView = mInflater.inflate(R.layout.list_item_busguide,
						parent, false);

				holder.tvDescribe = (TextView) convertView
						.findViewById(R.id.tv_describe);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			TransitStep transitStep = listTransitSteps.get(position);
			holder.tvDescribe.setText(transitStep.getInstructions());// 设置文字描述

			return convertView;
		}

		public final class ViewHolder {
			public TextView tvDescribe;// 文字描述
		}

	}

	// 点击事件
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
