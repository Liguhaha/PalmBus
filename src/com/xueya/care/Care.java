package com.xueya.care;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.xueya.bean.StationInfo;
import com.xueya.db.StationDao;
import com.xueya.palmbus.R;
import com.xueya.tools.JsonTools;

public class Care extends Fragment implements OnClickListener {
	private List<StationInfo> stations;
	private ListView lvCare;
	private ImageView ivAddCare;
	private ImageView ivFlush;
	private StationInfo station = null;
	private LinearLayout mLinearLayout = null;
	private static LinearLayout llFluAdd = null;
	private StationDao Dao = null;
	private boolean[] flag;
	private boolean[] chron;
	private Chronometer[] arrayChronometer;
	private MyAdapter myAdapter = null;

	private final static Care mCare = new Care();

	private Care() {
	}
	
	public static Care getCare() {
		return mCare;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Dao = new StationDao(getActivity());
		stations = Dao.findAll();// 从数据库中取出所有关注站点信息
//		sendAll(stations);// 发送请求
		flag = new boolean[stations.size()];
		chron = new boolean[stations.size()];
		arrayChronometer = new Chronometer[stations.size()];
		for (int i = 0; i < stations.size(); i++) {
			arrayChronometer[i] = new Chronometer(getActivity());
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_care, null, false);
		lvCare = (ListView) view.findViewById(R.id.lv_dis_care);

		ivAddCare = (ImageView) view.findViewById(R.id.iv_add_care);
		ivFlush = (ImageView) view.findViewById(R.id.iv_flush);
		llFluAdd = (LinearLayout) view.findViewById(R.id.ll_fl_add);
		ivAddCare.setOnClickListener(this);
		ivFlush.setOnClickListener(this);

		myAdapter = new MyAdapter();
		lvCare.setAdapter(myAdapter);

		//每个条目的点击事件
		lvCare.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				//传递get_id过去
				Intent intent = new Intent(getActivity(),ArriveInfoActivity.class);
				intent.putExtra("get_id", stations.get(position).getGetID());
				intent.putExtra("line_number", stations.get(position).getLineNumber());
				intent.putExtra("position", stations.get(position).getPosition());
				startActivity(intent);
			}
		});

		lvCare.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				System.out.println("onScrollStateChanged");
				llFluAdd.setVisibility(View.INVISIBLE);
				if (!handler.hasMessages(1))
					handler.sendEmptyMessageDelayed(1, 5000);
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		return view;
	}

	/**
	 * 向服务器提交数据
	 * 
	 * @param busLineID
	 *            get_id
	 * @param blsID
	 *            bls_id
	 * @param position
	 *            编号
	 * @param index
	 *            listview中的position
	 */
	// http://its-xy.aliapp.com/mobile/lastbus
	public void sendArriveStation(String busLineID, String blsID,
			String position, final int index) {
		Log.i("SEND", "SEND");
		AsyncHttpClient client = new AsyncHttpClient();
		String url = "http://its-xy.aliapp.com/mobile/lastbus";
		RequestParams params = new RequestParams();
		params.put("busLineID", busLineID);
		params.put("blsID", blsID);
		params.put("position", position);
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String content) {
				super.onSuccess(content);
				String[] result = getArriveStation(content, index);// 到站信息
				stations.get(index).setArriveInfo(result);
				myAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
			}

		});
	}

	/**
	 * 
	 * 解析json集合 status 1没有数据 0返回到数据
	 * 
	 * @param result
	 *            需要解析的字符串
	 * @param index
	 *            解析的编号
	 * @param return 返回字符串数组 长度不确定
	 * 
	 */
	public String[] getArriveStation(String result, int index) {
		JSONObject jo = JsonTools.getJsonObj(result);
		int status = 0;
		status = jo.getInt("status"); // 0找到结果 1未找到结果
		if (status == 0) {
			JSONArray jsArray = JsonTools.getJsonArray("last", jo);
			int size = jsArray.size();// 数组中元素的个数
			String[] arrive = new String[size];
			for (int i = 0; i < size; i++) {
				arrive[i] = jsArray.getString(i);
				System.out.println(arrive[i]);
			}
			chron[index] = true;
			return arrive;
		}
		return null;
	}

	/**
	 * 发送请求
	 * 
	 * @param stations
	 *            需要得到数据的StationInfo集合
	 */
	public void sendAll(List<StationInfo> stations) {
		int size = stations.size();
		System.out.println("stations size:  " + size);
		StationInfo station;
		for (int i = 0; i < size; i++) {
			station = stations.get(i);
			sendArriveStation(station.getGetID(), station.getBlsID(),
					station.getPosition(), i);
		}
	}

	/**
	 * 自动刷新
	 */

	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				llFluAdd.setVisibility(View.VISIBLE);
				break;
			}
		};
	};
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Log.i("haldler", "sendAll");
			sendAll(stations);
			handler.postDelayed(this, 1000 * 15);
		}
	};

	private void autoSendStart() {
		sendAll(stations);
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, 1000 * 15);
	}

	private void autoSendStop() {
		handler.removeCallbacks(runnable);
	}

	// 删除事件
	class Cancel implements android.content.DialogInterface.OnClickListener {
		private int position;

		public Cancel(int position) {
			this.position = position;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			long i = Dao.delete(stations.get(position).getGetID());
			if (i > 0) {
				stations = Dao.findAll();
				// 减去判断数组的下标
				boolean[] temp = new boolean[flag.length - 1];
				for (int j = 0; j < position; j++) {
					temp[j] = flag[j];
				}
				for (int j = position; j < temp.length; j++) {
					temp[j] = flag[j + 1];
				}
				flag = temp;
				myAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return stations.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			station = stations.get(position);
			final View view = View.inflate(getActivity(),
					R.layout.list_item_displaycare, null);

			TextView tvLineNumber = (TextView) view
					.findViewById(R.id.tv_linenumber);
			TextView tvCareStation = (TextView) view
					.findViewById(R.id.tv_care_station);
			TextView tvBusArrive1 = (TextView) view
					.findViewById(R.id.tv_busarrive1);
			TextView tvBusArrive2 = (TextView) view
					.findViewById(R.id.tv_busarrive2);
			TextView tvBusArrive3 = (TextView) view
					.findViewById(R.id.tv_busarrive3);

			TextView tvDisc = (TextView) view.findViewById(R.id.tv_disc);
			final ImageView ivDevelop = (ImageView) view
					.findViewById(R.id.iv_develop);
			ImageView ivAlarm = (ImageView) view.findViewById(R.id.iv_alarms);

			Chronometer chronometer = (Chronometer) view
					.findViewById(R.id.chronometer);
			ivAlarm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity(), "闹钟" + position,
							Toast.LENGTH_SHORT).show();
				}
			});
			mLinearLayout = (LinearLayout) view.findViewById(R.id.ll_detail);
			ImageView ivDelete = (ImageView) view.findViewById(R.id.iv_delete);
			ivDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(getActivity()).setTitle("提示")
							.setMessage("确定取消关注吗？")
							.setNegativeButton("否", null)
							.setPositiveButton("是", new Cancel(position))
							.show();
				}
			});
			ivDevelop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!flag[position]) {
						ivDevelop
								.setImageResource(R.drawable.navigation_collapse);
						mLinearLayout.setVisibility(View.VISIBLE);
						flag[position] = true;
					} else if (flag[position]) {
						mLinearLayout.setVisibility(View.GONE);
						ivDevelop
								.setImageResource(R.drawable.navigation_expand);
						flag[position] = false;
					}
					MyAdapter.this.notifyDataSetChanged();
				}
			});
			if (flag[position]) {
				mLinearLayout.setVisibility(View.VISIBLE);
				ivDevelop.setImageResource(R.drawable.navigation_collapse);
			}

			tvLineNumber.setText(station.getLineNumber());
			tvCareStation.setText(station.getStopName());

			tvDisc.setText(station.getStartName() + " 开往 "
					+ station.getEndName() + " 方向");

			if (station.getArriveInfo() != null) {
				if (chron[position]) {
					arrayChronometer[position].setBase(SystemClock
							.elapsedRealtime());
					chron[position] = false;
				}
				chronometer.setBase(arrayChronometer[position].getBase());
				chronometer.start();
				int length = station.getArriveInfo().length;
				switch (length) {
				case 1:
					tvBusArrive1.setText("第一趟车还有" + station.getArriveInfo()[0]
							+ "站到达");
					break;
				case 2:
					tvBusArrive1.setText("第一趟车还有" + station.getArriveInfo()[0]
							+ "站到达");
					tvBusArrive2.setText("第二趟车还有" + station.getArriveInfo()[1]
							+ "站到达");
					break;
				case 3:
					tvBusArrive1.setText("第一趟车还有" + station.getArriveInfo()[0]
							+ "站到达");
					tvBusArrive2.setText("第二趟车还有" + station.getArriveInfo()[1]
							+ "站到达");
					tvBusArrive3.setText("第三趟车还有" + station.getArriveInfo()[2]
							+ "站到达");
					break;
				}
			}

			return view;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_add_care:
			startActivity(new Intent(getActivity(), AddCareActivity.class));
			break;
		case R.id.iv_flush:
			autoSendStart();
			break;
		default:
			break;
		}
	}
	
	

	@Override
	public void onResume() {
		autoSendStart();
		super.onResume();
	}

	@Override
	public void onPause() {
		autoSendStop();
		System.out.println("onPause()");
		super.onPause();
	}


}
