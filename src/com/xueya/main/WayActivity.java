package com.xueya.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.xueya.care.Care;
import com.xueya.goal.Goal;
import com.xueya.palmbus.R;
import com.xueya.tools.MyTransitRouteResult;

public class WayActivity extends Activity implements OnClickListener {

	private ImageView ivBack = null;// 回退导航
	private ImageView ivCare = null;// 关注
	private ImageView ivGoal = null;// 目的地
	private FragmentManager fm = null;
	private FragmentTransaction ft = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_way);
		// instance = this;
		// 自定义标题
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(R.layout.title_wayactivity);
		actionBar.setDisplayShowCustomEnabled(true); // 使自定义的普通View能在title栏显示
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();

		// 判断网络是否可用
		if (!isNetworkConnected(this)) {
			new AlertDialog.Builder(this).setTitle("当前网络不可用")
					.setMessage("请检查网络设置")
					.setPositiveButton("设置", new MyOnclickListener())
					.setNegativeButton("取消", null).show();
		}

		initControl();
		initFragment(getBundleStr("FROM"));
	}

	// 跳转到网络设置界面
	class MyOnclickListener implements
			android.content.DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Intent SettingsIntent = new Intent("android.settings.SETTINGS");
			startActivity(SettingsIntent);
		}

	}

	// 找到组件 设置点击事件
	private void initControl() {
		ivBack = (ImageView) findViewById(R.id.nav_left);
		ivBack.setOnClickListener(this);
		ivCare = (ImageView) findViewById(R.id.iv_care);
		ivCare.setOnClickListener(this);
		ivGoal = (ImageView) findViewById(R.id.iv_goal);
		ivGoal.setOnClickListener(this);
	}

	private void initFragment(String str) {
		fm = getFragmentManager();
		ft = fm.beginTransaction();
		if (str.equals("GOAL_FRAGMENT")||str.equals("NEAR_INTEREST")) {
			ivGoal.setSelected(true);
			ft.replace(R.id.liner, Goal.getGoal());
		} else {
			ivCare.setSelected(true);
			ft.replace(R.id.liner, Care.getCare());
		}

		ft.commit();
	}

	// 点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nav_left:// 返回
			//清空clearPlanNode
			clearPlanNode();
			finish();
			/*Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);*/
			break;
		case R.id.iv_care:
			ivCare.setSelected(true);
			ivGoal.setSelected(false);
			ft = fm.beginTransaction();
			ft.replace(R.id.liner, Care.getCare());
			ft.commit();
			break;
		case R.id.iv_goal:
			ivGoal.setSelected(true);
			ivCare.setSelected(false);
			ft = fm.beginTransaction();
			ft.replace(R.id.liner, Goal.getGoal());
			ft.commit();
			break;
		default:
			break;
		}
	}
	
	// 监听返回键
		@Override
		public void onBackPressed() {
			clearPlanNode();
/*			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);*/
			finish();
			super.onBackPressed();
		}
		
		/**
		 * 清空MyPlanode
		 */
		public void clearPlanNode(){
			MyTransitRouteResult myTransitRouteResult = MyTransitRouteResult.getMyTransitRouteLine();
			myTransitRouteResult.setStPlanNode(null);
			myTransitRouteResult.setEnPlanNode(null);
			Goal mGoal = Goal.getGoal();
			mGoal.clearData();
		}

	/**
	 *  获取传递进来的值
	 * @param key 键
	 * @return值
	 */
	public String getBundleStr(String key) {
		Bundle bun = new Bundle();
		bun = getIntent().getExtras();
		return bun.getString(key);
	}

	/**
	 *  判断网络是否可用
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
}
