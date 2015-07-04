package com.xueya.goal;

import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.xueya.bean.History;
import com.xueya.bean.MyPlanNode;
import com.xueya.db.StationDao;
import com.xueya.palmbus.R;
import com.xueya.tools.MyTransitRouteResult;

public class Goal extends Fragment implements OnClickListener,
		OnGetRoutePlanResultListener {

	public final static int START = 1;
	public final static int END = 2;
	private ImageView exChange = null;// 交换起点终点图标
	private TextView tvStart = null;// 起点
	private TextView tvEnd = null;// 终点
	private ImageView ivHis = null;// 历史记录
	private String sKey = "", sCity = "", eKey = "", eCity = "";
	private String CITY = "北京";// 路径规划城市 默认是北京
	private ProgressDialog pd = null;

	private StationDao Dao = null;// 数据库工具

	private ListView lvBusInfo = null;
	private ListView lvHisInfo = null;
	private List<History> histories = null;
	private MyTransitRouteResult myTransitRouteResult = MyTransitRouteResult
			.getMyTransitRouteLine();
	private RoutePlanSearch mSearch = null;

	private final static Goal mGoal = new Goal();

	private Goal() {
	}

	public static Goal getGoal() {
		return mGoal;
	}

	public void clearData() {
		sKey = "";
		sCity = "";
		eKey = "";
		eCity = "";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Dao = new StationDao(getActivity());// 初始化数据库工具
		histories = Dao.findHisALL();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SDKInitializer.initialize(getActivity().getApplicationContext());
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(this);
		View view = inflater.inflate(R.layout.fragment_goal, null, false);
		init(view);// 初始化组件
		setDis();
		routePlan();
		return view;
	}

	// 初始化组件 设置点击事件
	private void init(View v) {
		exChange = (ImageView) v.findViewById(R.id.iv_exchange);
		exChange.setOnClickListener(this);
		ivHis = (ImageView) v.findViewById(R.id.iv_history);
		ivHis.setOnClickListener(this);
		ivHis.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				long result = Dao.delAllHis();
				clearHis();
				if (result >= 0) {
					ivHis.setVisibility(View.INVISIBLE);
					Toast.makeText(getActivity(), "历史记录已清空", Toast.LENGTH_SHORT)
							.show();
					histories = Dao.findHisALL();
					lvHisInfo.setAdapter(new HisAdapter());
					lvBusInfo.setAdapter(null);
				}
				return true;
			}
		});
		tvStart = (TextView) v.findViewById(R.id.tv_start);
		tvStart.setOnClickListener(this);
		tvEnd = (TextView) v.findViewById(R.id.tv_end);
		tvEnd.setOnClickListener(this);
		lvBusInfo = (ListView) v.findViewById(R.id.lv_bus);
		lvHisInfo = (ListView) v.findViewById(R.id.lv_his);
	}

	private void clearHis() {
		myTransitRouteResult = MyTransitRouteResult.getMyTransitRouteLine();
		myTransitRouteResult.setStPlanNode(null);
		myTransitRouteResult.setEnPlanNode(null);
		setDis();
	}

	/**
	 * 初始化显示
	 * 
	 */
	private void setDis() {
		MyPlanNode stPlanNode = myTransitRouteResult.getStPlanNode();
		MyPlanNode enPlanNode = myTransitRouteResult.getEnPlanNode();

//		if (stPlanNode == null && enPlanNode == null) {
		if (stPlanNode == null) {
			if (myTransitRouteResult.getLatLng() != null) {
				MyPlanNode temp = new MyPlanNode();
				temp.setKey("我的位置");
				temp.setCity("");
				myTransitRouteResult.setStPlanNode(temp);
			}
		}
		stPlanNode = myTransitRouteResult.getStPlanNode();
		enPlanNode = myTransitRouteResult.getEnPlanNode();
		if (stPlanNode != null) {
			sKey = stPlanNode.getKey();
			sCity = stPlanNode.getCity();
		} else {
			sKey = "";
			sCity = "";
		}
		if (enPlanNode != null) {
			eKey = enPlanNode.getKey();
			eCity = enPlanNode.getCity();
		} else {
			eKey = "";
			eCity = "";
		}

		tvStart.setText(sKey);
		tvEnd.setText(eKey);

		if (!sCity.equals("")) {
			CITY = sCity;
		} else if (!eCity.equals("")) {
			CITY = eCity;
		}

		if ((sKey.equals("") || eKey.equals("")) && histories.size() > 0) {
			lvHisInfo.setAdapter(new HisAdapter());
			lvHisInfo.setVisibility(View.VISIBLE);
		} else if (histories.size() == 0) {
			ivHis.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_exchange:
			// 交换起点终点
			if (!sKey.equals("") && !eKey.equals(""))
				exStartEnd();
			break;
		case R.id.tv_start:
			Intent sIntent = new Intent(getActivity(), GoalSearchActivity.class);
			sIntent.putExtra("place", "start");
			startActivity(sIntent);
			break;
		case R.id.tv_end:
			Intent eIntent = new Intent(getActivity(), GoalSearchActivity.class);
			eIntent.putExtra("place", "end");
			startActivity(eIntent);
			break;
		case R.id.iv_history:
			if (lvHisInfo.getVisibility() == View.VISIBLE) {
				lvHisInfo.setVisibility(View.GONE);
			} else {
				lvHisInfo.setAdapter(new HisAdapter());
				lvHisInfo.setVisibility(View.VISIBLE);
				lvBusInfo.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						lvHisInfo.setVisibility(View.GONE);
						return false;
					}
				});
			}
			break;
		default:
			break;
		}
	}

	// 交换两个起点终点
	public void exStartEnd() {
		MyPlanNode stPlanNode = myTransitRouteResult.getStPlanNode();
		MyPlanNode enPlanNode = myTransitRouteResult.getEnPlanNode();
		MyPlanNode temp = stPlanNode;
		stPlanNode = enPlanNode;
		enPlanNode = temp;
		myTransitRouteResult.setStPlanNode(stPlanNode);
		myTransitRouteResult.setEnPlanNode(enPlanNode);
		setDis();
		routePlan();
	}

	// 路径规划
	private void routePlan() {
		if (!sKey.equals("") && !eKey.equals("")) {
			PlanNode stNode = null;
			PlanNode enNode = null;
			if (sKey.equals("我的位置")) {
				stNode = PlanNode
						.withLocation(myTransitRouteResult.getLatLng());
				enNode = PlanNode.withCityNameAndPlaceName(eCity, eKey);
			} else if (eKey.equals("我的位置")) {
				stNode = PlanNode.withCityNameAndPlaceName(sCity, sKey);
				enNode = PlanNode
						.withLocation(myTransitRouteResult.getLatLng());
			} else {
				stNode = PlanNode.withCityNameAndPlaceName(sCity, sKey);
				enNode = PlanNode.withCityNameAndPlaceName(eCity, eKey);
			}
			pd = ProgressDialog.show(getActivity(), null, "页面加载中，请稍后..", false,
					true);
			mSearch.transitSearch((new TransitRoutePlanOption()).from(stNode)
					.city(CITY).to(enNode));
		}
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {
		pd.dismiss();
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(getActivity(), "抱歉，未找到结果", Toast.LENGTH_SHORT)
					.show();
			lvBusInfo.setAdapter(null);
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo();
			lvBusInfo.setAdapter(null);
			return;
		}// 检索结果正常返回
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			// 将记录添加进数据库
			Dao.MyInsert(sKey, eKey, CITY, sCity, eCity);
			// 返回数据库中的所有内容
			histories = Dao.findHisALL();
			// 显示搜索历史图标
			ivHis.setVisibility(View.VISIBLE);
			// 获取所有方案的集合
			myTransitRouteResult.setTransitRouteResult(result);
			lvHisInfo.setVisibility(View.GONE);
			lvBusInfo.setAdapter(new MyAdapter(getActivity()
					.getApplicationContext()));

			// listview的点击事件
			lvBusInfo.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					Intent intent = new Intent(getActivity(),
							BusGuideActivity.class);
					intent.putExtra("position", position);
					startActivity(intent);
				}
			});

		}
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
	}

	class MyAdapter extends BaseAdapter {
		LayoutInflater inflater = null;

		public MyAdapter(Context mContext) {
			this.inflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			return myTransitRouteResult.getmTransitRouteLines().size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_goalbus,
						parent, false);
				holder = new ViewHolder();
				holder.tvBusnumber = (TextView) convertView
						.findViewById(R.id.tv_busnumber);
				holder.tvDuration = (TextView) convertView
						.findViewById(R.id.tv_duration);
				holder.tvStationNum = (TextView) convertView
						.findViewById(R.id.tv_stationnum);
				holder.tvWalkDis = (TextView) convertView
						.findViewById(R.id.tv_walkdis);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String[] str = myTransitRouteResult.getmTransitStep(position);
			holder.tvBusnumber.setText(str[0]);
			holder.tvDuration
					.setText("大约"
							+ myTransitRouteResult.getAllDuration(position)
							/ 60 + "分钟");
			holder.tvStationNum.setText("共" + str[1] + "站");
			holder.tvWalkDis.setText("步行" + str[2] + "米");
			return convertView;
		}

		public final class ViewHolder {
			public TextView tvBusnumber;// 车辆编号
			public TextView tvDuration;// 所需时间
			public TextView tvStationNum;// 站台数量
			public TextView tvWalkDis;// 步行距离
		}
	}

	class HisAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return histories.size();
		}

		@Override
		public Object getItem(int position) {
			return histories.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HisViewHolder hisViewHolder = null;
			if (convertView == null) {
				hisViewHolder = new HisViewHolder();
				LayoutInflater mInflater = LayoutInflater.from(getActivity());
				convertView = mInflater.inflate(R.layout.list_item_history,
						parent, false);
				hisViewHolder.tvHis = (TextView) convertView
						.findViewById(R.id.tv_his);
				convertView.setTag(hisViewHolder);
			} else {
				hisViewHolder = (HisViewHolder) convertView.getTag();
			}
			hisViewHolder.tvHis.setText(histories.get(position).getStart()
					+ " — " + histories.get(position).getEnd());

			lvHisInfo.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					tvStart.setText(histories.get(position).getStart());
					tvEnd.setText(histories.get(position).getEnd());
					sKey = histories.get(position).getStart();
					eKey = histories.get(position).getEnd();
					sCity = histories.get(position).getsCity();
					eCity = histories.get(position).geteCity();
					CITY = histories.get(position).getSearch_city();
					// 起点信息
					MyPlanNode stPlanNode = new MyPlanNode();
					stPlanNode.setKey(sKey);
					stPlanNode.setCity(sCity);
					myTransitRouteResult.setStPlanNode(stPlanNode);
					// 终点信息
					MyPlanNode enPlanNode = new MyPlanNode();
					enPlanNode.setKey(eKey);
					enPlanNode.setCity(eCity);
					myTransitRouteResult.setEnPlanNode(enPlanNode);
					routePlan();
				}
			});

			return convertView;
		}

		public final class HisViewHolder {
			public TextView tvHis;
		}

	}

	/**
	 * 判断网络是否可用
	 * 
	 * @param context上下文
	 * @return true可用 false 不可用
	 */
	public boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	@Override
	public void onDestroy() {
		mSearch.destroy();
		super.onDestroy();
	}

}