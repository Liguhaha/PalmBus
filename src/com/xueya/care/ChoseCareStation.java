package com.xueya.care;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.xueya.bean.StationInfo;
import com.xueya.db.StationDao;
import com.xueya.main.WayActivity;
import com.xueya.palmbus.R;
import com.xueya.tools.JsonTools;

public class ChoseCareStation extends Activity implements OnClickListener {

	private String getID, endStop, startStop, lineName;
	private String saveBlsID, saveID, saveStopName;
	private ImageView ivBack;
	private TextView tvDisc, tvLineName;
	private List<StationInfo> stationResult;
	private ListView lvStationInfo;
	private int FLAG = -1;// 记录选择的编号 默认赋值从数据库中取出 这里暂时为-1
	private int JUDGE = 0;// 全局变量 判断是否点击了条目 0为没有点击 1为点击 2为取消了原来的 需要在数据库中删除
	private ProgressDialog pd = null;
	private String result;//获取到的json

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chose_caretation);

		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.title_normal);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();

		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("关注站点");
		
		ivBack = (ImageView) findViewById(R.id.iv_back);
		lvStationInfo = (ListView) findViewById(R.id.lv_station_info);

		tvDisc = (TextView) findViewById(R.id.tv_disc);
		tvLineName = (TextView) findViewById(R.id.tv_line_name);

		ivBack.setOnClickListener(this);

		// 获取传递进来的值
		Intent intent = getIntent();
		getID = intent.getStringExtra("busID");
		startStop = intent.getStringExtra("start_stop");
		endStop = intent.getStringExtra("end_stop");
		lineName = intent.getStringExtra("line_name");

		init();
		getRecordFromServer();

	}

	// 初始化显示
	private void init() {
		tvDisc.setText("从 "+startStop+" 开往 "+endStop+" 方向");
		tvLineName.setText(lineName);
	}

	// 返回勾选的条目编号
	public int getFlag(String get_id) {
		StationDao Dao = new StationDao(this);
		int n = -1;
		for (int i = 0; i < stationResult.size(); i++) {
			Log.i("station",
					stationResult.get(i).getBlsID() + "-----"
							+ Dao.findBlsID(get_id));
			if (stationResult.get(i).getBlsID().equals(Dao.findBlsID(get_id))) {
				n = i;
				break;
			}
		}
		return n;
	}

	// 向服务器提交数据
	private void getRecordFromServer() {
		pd = ProgressDialog.show(this, null, "正在获取数据，请稍后..", false,
				true);
		AsyncHttpClient client = new AsyncHttpClient();
		String url = getString(R.string.server_url) + "mobile/buslinestop";
		RequestParams params = new RequestParams();
		params.put("busID", getID);
		System.out.println("getID:\t"+getID);
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String content) {
				super.onSuccess(content);
				pd.dismiss();
				getResultList(content);
				result = content;
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
			}

		});
	}

	/**
	 *  解析json得到list集合
	 * @param result 需要解析的字符串
	 */
	public void getResultList(String result) {
		stationResult = JsonTools.getResultList(result);
		if(stationResult!=null){
			lvStationInfo.setAdapter(new MyAdapter(stationResult));
		}else {
			Toast.makeText(ChoseCareStation.this, "抱歉，未找到结果",
					Toast.LENGTH_SHORT).show();
		}

	}


	// 点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			backCode();
			break;

		default:
			break;
		}

	}

	// 监听返回键
	@Override
	public void onBackPressed() {
		backCode();
		super.onBackPressed();
	}

	// 向上导航要执行的代码
	private void backCode() {
		Intent intent = new Intent(this, WayActivity.class);

		SharedPreferences preferences = getSharedPreferences("station", MODE_PRIVATE);
		Editor editor = preferences.edit();
		if (JUDGE == 1) {
			addSQL(getID, saveBlsID, saveID, saveStopName);
			editor.putString(getID, result);
			editor.commit();
		} else if (JUDGE == 2) {
			deleteSQL(getID);
			editor.remove(getID);
			editor.commit();
		}
		Bundle bundle = new Bundle();
		bundle.putString("FROM", "CARE_FRAGMENT");
		intent.putExtras(bundle);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	// 添加更新方法
	public void addSQL(String getID, String saveBlsID, String saveID,
			String saveStopName) {
		StationDao Dao = new StationDao(ChoseCareStation.this);
		long result = 0;
		if (Dao.findGetID(getID)) {
			result = Dao.update(getID, FLAG+"", saveBlsID, saveID, saveStopName,
					startStop, endStop, lineName);
		} else {
			result = Dao.add(getID, FLAG+"", saveBlsID, saveID, saveStopName, startStop,
					endStop, lineName);
		}
		if (result < 0) {
			Toast.makeText(ChoseCareStation.this, "添加失败", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// 删除方法
	public void deleteSQL(String getID) {
		StationDao Dao = new StationDao(ChoseCareStation.this);
		long result = Dao.delete(getID);
		if (result < 0) {
			Toast.makeText(ChoseCareStation.this, "删除失败", Toast.LENGTH_SHORT)
					.show();
		}
	}

	class MyAdapter extends BaseAdapter {
		private List<Boolean> mChecked;
		private List<StationInfo> stations;

		public MyAdapter(List<StationInfo> list) {
			stations = new ArrayList<StationInfo>();
			stations = list;

			mChecked = new ArrayList<Boolean>();
			int numCheck = getFlag(getID);
			FLAG = numCheck;
			/*
			 * Toast.makeText(ChoseCareStation.this, "numCheck" + numCheck, 0)
			 * .show();
			 */
			for (int i = 0; i < list.size(); i++) {
				if (numCheck == i) {
					mChecked.add(true);
				} else {
					mChecked.add(false);
				}
			}
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
			View view = View.inflate(ChoseCareStation.this,
					R.layout.list_item_chose_carestation, null);

			TextView tvStation = (TextView) view
					.findViewById(R.id.tv_station_name);
			CheckBox cbChose = (CheckBox) view.findViewById(R.id.cb_chose);
			final int index = position;
			cbChose.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					JUDGE = 1;
					// 只能选择一个的逻辑代码
					if (FLAG == -1) {//初始状态
						FLAG = index;//FLAG等于点击的position
						mChecked.set(FLAG, true);
						Log.i("FLAG", "==-1");
					} else if (FLAG != index) {//点击的其他条目
						mChecked.set(FLAG, false);//将原条目设置为false
						FLAG = index;//改变被点击的条目
						mChecked.set(FLAG, true);
						Log.i("FLAG", "!=index");
					} else if (FLAG == index) {
						mChecked.set(FLAG, !mChecked.get(index));
						Log.i("FLAG", "==index");
					}
					// 将数据赋值到变量中
					if (mChecked.get(index)) {
						saveBlsID = stations.get(index).getBlsID();
						saveID = stations.get(index).getID();
						saveStopName = stations.get(index).getStopName();
				//		Toast.makeText(ChoseCareStation.this, "点击的条目position为："+index, 0).show();
					} else if (!mChecked.get(index)) {
						JUDGE = 2;
					}
					// 更新listview
					MyAdapter.this.notifyDataSetChanged();
				}
			});

			tvStation.setText(stations.get(position).getStopName());
			cbChose.setChecked(mChecked.get(position));

			return view;
		}

	}
}
