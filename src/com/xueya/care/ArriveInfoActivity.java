package com.xueya.care;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.xueya.bean.StationInfo;
import com.xueya.palmbus.R;
import com.xueya.tools.JsonTools;

public class ArriveInfoActivity extends Activity implements OnClickListener {

	private String getID;// 传递进来的值 getID
	private String lineNumber;// 传递进来的值 线路编号
	private String getPosition;// 传递进来的值 position
	private List<StationInfo> stations;
	private ListView lvStation;
	private boolean Judge;

	public final static String ARRIVE_URL = "http://its-xy.aliapp.com/mobile/onlinebus";
	private Map<String, Object> map = new HashMap<String, Object>();// 获取到的map集合
	private MyAdapter myAdapter = new MyAdapter(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arrive_info);

		// 获取传递进来的值
		Intent intent = getIntent();
		getID = intent.getStringExtra("get_id");// 根据getID找到对应的sharedpreference
		lineNumber = intent.getStringExtra("line_number");
		getPosition = intent.getStringExtra("position");
		stations = JsonTools.getResultList(getShared("station", getID));
		init();

	}

	/**
	 * 初始化显示 找到组件
	 */
	private void init() {
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.title_normal);
		actionBar.setDisplayShowCustomEnabled(true); // 使自定义的普通View能在title栏显示
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();

		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText(lineNumber);
		ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);
		ImageView ivFlush = (ImageView) findViewById(R.id.iv_flush);
		ivFlush.setVisibility(View.VISIBLE);
		ivFlush.setOnClickListener(this);
		lvStation = (ListView) findViewById(R.id.lv_station);
		lvStation.setAdapter(myAdapter);

		TextView tvStart = (TextView) findViewById(R.id.tv_start);
		TextView tvEnd = (TextView) findViewById(R.id.tv_end);

		tvStart.setText(stations.get(0).getStopName());
		tvEnd.setText(stations.get(stations.size() - 1).getStopName());
	}

	/**
	 * 获取sharedpreference值
	 * 
	 * @param name
	 *            sharedpreference名称
	 * @param key
	 *            键
	 * @return 内容
	 */
	public String getShared(String name, String key) {
		SharedPreferences preferences = getSharedPreferences(name, MODE_PRIVATE);
		String result = preferences.getString(key, "");
		return result;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.iv_flush:
			Judge = true;
			autoSendStart();
			break;
		}
	}

	/**
	 * 向服务器提交数据
	 * 
	 * @param busLineID
	 *            线路编号
	 * @param url
	 *            服务器地址
	 */
	public void sendArriveStation(String busLineID, String url) {
		Log.i("SEND", "SEND");
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("busLineID", busLineID);
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String content) {
				super.onSuccess(content);
				if (Judge) {
					Toast.makeText(ArriveInfoActivity.this, "刷新成功",
							Toast.LENGTH_SHORT).show();
				}
				Judge = false;
				// 解析json
				map = JsonTools.getArriveInfo(content);
				myAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				Judge = false;
			}

		});
	}

	private static Handler handler = new Handler();

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Log.i("haldler", "sendArriveStation");
			sendArriveStation(getID, ARRIVE_URL);
			handler.postDelayed(this, 1000 * 15);
		}
	};

	/**
	 * 自动提交数据
	 */
	private void autoSendStart() {
		sendArriveStation(getID, ARRIVE_URL);
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, 1000 * 15);
	}

	/**
	 * 停止提交数据
	 */
	private void autoSendStop() {
		handler.removeCallbacks(runnable);
	}

	class MyAdapter extends BaseAdapter {

		private Context mContext;

		public MyAdapter(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return stations.size();
		}

		@Override
		public Object getItem(int position) {
			return stations.get(position);
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
				convertView = mInflater.inflate(R.layout.list_item_arriveinfo,
						parent, false);
				holder.tvStopNum = (TextView) convertView
						.findViewById(R.id.tv_stop_num);
				holder.tvStopName = (TextView) convertView
						.findViewById(R.id.tv_stop_name);
				holder.ivBus = (ImageView) convertView
						.findViewById(R.id.iv_bus);
				holder.viewDown = convertView.findViewById(R.id.view_down);
				holder.tvBusNum = (TextView) convertView
						.findViewById(R.id.tv_bus_num);
				holder.llUp = (LinearLayout) convertView
						.findViewById(R.id.ll_up);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position < 9) {
				holder.tvStopNum.setText("0" + (position+1));
			} else {
				holder.tvStopNum.setText((position+1) + "");
			}
			if (position == 0) {
				holder.llUp.setVisibility(View.VISIBLE);
				holder.viewDown.setVisibility(View.VISIBLE);
			} else if (position == stations.size() - 1) {
				
				holder.llUp.setVisibility(View.GONE);
				holder.viewDown.setVisibility(View.INVISIBLE);
			} else {
				holder.llUp.setVisibility(View.GONE);
				holder.viewDown.setVisibility(View.VISIBLE);
			}
			if ((position + "").equals(getPosition)) {
				holder.tvStopName.setTextColor(Color.parseColor("#1E90FF"));
			} else {
				holder.tvStopName.setTextColor(Color.parseColor("#000000"));
			}
			Object num = null;
			if(map!=null){
				num = map.get(position + "");
			}
			if (num == null) {
				holder.ivBus.setVisibility(View.INVISIBLE);
			} else {
				holder.ivBus.setVisibility(View.VISIBLE);
			}
			if (num == null || (Integer) num <= 1) {
				holder.tvBusNum.setVisibility(View.GONE);
			} else {
				holder.tvBusNum.setVisibility(View.VISIBLE);
				holder.tvBusNum.setText(num + "");
			}
			holder.tvStopName.setText(stations.get(position).getStopName());

			return convertView;
		}

		public final class ViewHolder {
			private TextView tvStopNum;// 站点编号
			private TextView tvStopName;// 站点名称
			private ImageView ivBus;// 公交图标
			private View viewDown;// 连接线
			private TextView tvBusNum;// 车辆数量
			private LinearLayout llUp;
		}

	}

	@Override
	protected void onResume() {
		autoSendStart();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		autoSendStop();
		super.onDestroy();
	}

}
