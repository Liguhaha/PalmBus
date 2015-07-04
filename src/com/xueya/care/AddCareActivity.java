package com.xueya.care;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.xueya.bean.BusInfo;
import com.xueya.palmbus.R;
import com.xueya.tools.JsonTools;

public class AddCareActivity extends Activity implements OnClickListener {

	private ImageView ivBack;
	private EditText etSearch;
	private Button btSearch;
	private String result;
	private List<BusInfo> busResult;
	private ListView lvBusInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_care);

		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.title_addcare);
		actionBar.setDisplayShowCustomEnabled(true); // 使自定义的普通View能在title栏显示
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();

		ivBack = (ImageView) findViewById(R.id.iv_back);
		etSearch = (EditText) findViewById(R.id.et_bus_number);
		btSearch = (Button) findViewById(R.id.bt_search);
		lvBusInfo = (ListView) findViewById(R.id.lv_businfo);
		ivBack.setOnClickListener(this);
		btSearch.setOnClickListener(this);
		etSearch.addTextChangedListener(watcher);

	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// 发送请求
			getRecordFromServer();
			// lvBusInfo.setAdapter(new MyAdapter());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	// 向服务器提交数据
	private void getRecordFromServer() {
		String busNumber = etSearch.getText().toString().trim();
		AsyncHttpClient client = new AsyncHttpClient();
		String url = getString(R.string.server_url) + "mobile/busline";
		RequestParams params = new RequestParams();
		params.put("name", busNumber);
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String content) {
				super.onSuccess(content);
				result = content;
				// Toast.makeText(AddCareActivity.this, "成功" +
				// content,Toast.LENGTH_SHORT).show();
				getResultList(result);
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
//				Toast.makeText(AddCareActivity.this, "失败" + content,
//						Toast.LENGTH_SHORT).show();
			}

		});
	}

	/*
	 * 解析json得到list集合
	 */
	private void getResultList(String result) {
		JSONObject jo = JsonTools.getJsonObj(result);
		int status = 0;
		status = jo.getInt("status"); // 1找到结果 2未找到结果
		if (status == 1) {
			JSONArray jsArray = JsonTools.getJsonArray("lines", jo);
			int size = jsArray.size();// 数组中元素的个数
			List<BusInfo> list = new ArrayList<BusInfo>();
			for (int i = 0; i < size; i++) {
				JSONObject js = jsArray.getJSONObject(i);// 获取指定数组元素
				BusInfo busInfo = new BusInfo();
				busInfo = addInfo(js);
				list.add(busInfo);
			}
			busResult = list;
			lvBusInfo.setAdapter(new MyAdapter());
		} else {
			Toast.makeText(AddCareActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
					.show();
		}

	}

	// 返回车辆信息
	private BusInfo addInfo(JSONObject js) {
		BusInfo info = new BusInfo();
		info.setEnd_stop(js.getString("end_stop"));
		info.setId(js.getString("id"));
		info.setLine_name(js.getString("line_name"));
		info.setStart_stop(js.getString("start_stop"));
		return info;
	}

	// 点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			this.finish();
			break;
		case R.id.bt_search:
			getRecordFromServer();
			break;
		default:
			break;
		}
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return busResult.size();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			BusInfo busInfo = busResult.get(position);
			View view = View.inflate(AddCareActivity.this,
					R.layout.list_item_add_care, null);
			// 线路编号
			TextView tvLineNumber = (TextView) view
					.findViewById(R.id.tv_line_number);
			//文字说明
			TextView tvDis = (TextView) view.findViewById(R.id.tv_disc);

			tvLineNumber.setText(busInfo.getLine_name());
			tvDis.setText("从 "+busInfo.getStart_stop()+" 开往 "+busInfo.getEnd_stop()+" 方向");

			// 每个条目的点击事件
			lvBusInfo
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							Intent intent = new Intent();
							intent.putExtra("busID", busResult.get(position)
									.getId());
							intent.putExtra("start_stop",
									busResult.get(position).getStart_stop());
							intent.putExtra("end_stop", busResult.get(position)
									.getEnd_stop());
							intent.putExtra("line_name", busResult
									.get(position).getLine_name());
							intent.setClass(AddCareActivity.this,
									ChoseCareStation.class);
							startActivity(intent);
						}
					});

			return view;
		}

	}
}
