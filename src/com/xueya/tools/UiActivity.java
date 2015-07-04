package com.xueya.tools;

import android.app.ActionBar;
import android.app.Activity;

public class UiActivity {
	/**
	 * 设置标题栏
	 */
	public static void setMyTitle(int resID,Activity activity) {
		ActionBar actionBar = activity.getActionBar();
		// actionBar.setCustomView(R.layout.title_main);
		actionBar.setCustomView(resID);
		actionBar.setDisplayShowCustomEnabled(true); // 使自定义的普通View能在title栏显示
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();
	}
}
