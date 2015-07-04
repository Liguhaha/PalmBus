package com.xueya.near;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xueya.palmbus.R;
import com.xueya.tools.UiActivity;

public class NearActivity extends Activity implements OnClickListener{
	
	private ImageView ivBack = null;//后退
	private LinearLayout llInterest = null;
	private TextView tvBusStop = null;
	private TextView tvHotel = null;
	private TextView tvFood = null;
	private TextView tvSport = null;
	private TextView tvShop = null;
	private TextView tvKtv = null;
	
	private Intent intent = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_near);
		UiActivity.setMyTitle(R.layout.title_normal,this);
		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("附 近");
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);
		llInterest = (LinearLayout) findViewById(R.id.ll_interest);
		llInterest.setOnClickListener(this);
		myFindSet();
		intent = new Intent();
	}
	
	
	private void myFindSet(){
		tvBusStop = (TextView) findViewById(R.id.tv_bus_stop);
		tvBusStop.setOnClickListener(this);
		tvHotel = (TextView) findViewById(R.id.tv_hotel);
		tvHotel.setOnClickListener(this);
		tvFood = (TextView) findViewById(R.id.tv_food);
		tvFood.setOnClickListener(this);
		tvSport = (TextView) findViewById(R.id.tv_sport);
		tvSport.setOnClickListener(this);
		tvShop = (TextView) findViewById(R.id.tv_shop);
		tvShop.setOnClickListener(this);
		tvKtv = (TextView) findViewById(R.id.tv_ktv);
		tvKtv.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			this.finish();
			break;
		case R.id.ll_interest:
			//搜索附近兴趣点
			startActivity(new Intent(this,NearInterest.class));
			break;
		case R.id.tv_bus_stop:
			intent.putExtra("INTEREST_KEY", "公交站");
			intent.setClass(this, NearPoi.class);
			startActivity(intent);
			break;
		case R.id.tv_hotel:
			intent.putExtra("INTEREST_KEY", "酒店");
			intent.setClass(this, NearPoi.class);
			startActivity(intent);
			break;
		case R.id.tv_food:
			intent.putExtra("INTEREST_KEY", "美食");
			intent.setClass(this, NearPoi.class);
			startActivity(intent);
			break;
		case R.id.tv_sport:
			intent.putExtra("INTEREST_KEY", "运动");
			intent.setClass(this, NearPoi.class);
			startActivity(intent);
			break;
		case R.id.tv_shop:
			intent.putExtra("INTEREST_KEY", "商店");
			intent.setClass(this, NearPoi.class);
			startActivity(intent);
			break;
		case R.id.tv_ktv:
			intent.putExtra("INTEREST_KEY", "ktv");
			intent.setClass(this, NearPoi.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
}
