package com.xueya.marker;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xueya.palmbus.R;

@SuppressLint({ "SdCardPath", "NewApi" })
public class MarKerInfo extends Activity implements OnClickListener {

	private EditText etMarkerInfo;
	private Button btReport, btCamera;
	private boolean isFis = true;
	private ImageView[] imageViewMarker;
	private RelativeLayout[] rLayout;
	private int pre = -1;
	private String strPath = "";
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyTitle(R.layout.title_normal);
		setContentView(R.layout.marker_info);

		imageViewMarker = new ImageView[6];
		rLayout = new RelativeLayout[6];
		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("标 记");
		ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		// 找到组件
		imageViewMarker[0] = (ImageView) findViewById(R.id.iv_jam);
		imageViewMarker[1] = (ImageView) findViewById(R.id.iv_accident);
		imageViewMarker[2] = (ImageView) findViewById(R.id.iv_construction);
		imageViewMarker[3] = (ImageView) findViewById(R.id.iv_police);
		imageViewMarker[4] = (ImageView) findViewById(R.id.iv_control);
		imageViewMarker[5] = (ImageView) findViewById(R.id.iv_ponding);
		rLayout[0] = (RelativeLayout) findViewById(R.id.rl_jam);
		rLayout[1] = (RelativeLayout) findViewById(R.id.rl_accident);
		rLayout[2] = (RelativeLayout) findViewById(R.id.rl_construction);
		rLayout[3] = (RelativeLayout) findViewById(R.id.rl_police);
		rLayout[4] = (RelativeLayout) findViewById(R.id.rl_control);
		rLayout[5] = (RelativeLayout) findViewById(R.id.rl_ponding);
		etMarkerInfo = (EditText) findViewById(R.id.et_markerinfo);
		btCamera = (Button) findViewById(R.id.bt_addphoto);
		btReport = (Button) findViewById(R.id.bt_report);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();

		btReport.setOnClickListener(this);
		btCamera.setOnClickListener(this);
		for (int i = 0; i < 6; i++) {
			imageViewMarker[i].setOnClickListener(new MyOnClickListener());
		}
	}

	public void setMyTitle(int resID) {
		ActionBar actionBar = getActionBar();
		// actionBar.setCustomView(R.layout.title_main);
		actionBar.setCustomView(resID);
		actionBar.setDisplayShowCustomEnabled(true); // 使自定义的普通View能在title栏显示
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.show();
	}

	class MyOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (pre != -1) {
				imageViewMarker[pre].setSelected(false);
				rLayout[pre].setBackgroundColor(Color.TRANSPARENT);
			}
			v.setSelected(true);
			for (int i = 0; i < 6; i++) {
				if (imageViewMarker[i].isSelected()) {
					rLayout[i].setBackgroundColor(Color.rgb(211, 209, 209));
					pre = i;
				}
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		switch (requestCode) {
		case 0:
			if (intent != null) {
				// 取到返回数据
				Bundle extras = intent.getExtras();
				Bitmap originalBitmap1 = null;
				if (extras != null) {
					originalBitmap1 = (Bitmap) extras.get("data");
				}
				if (originalBitmap1 != null) {
					Bitmap bitmap = resizeImage(originalBitmap1, 200, 200);
					saveBitmap(bitmap);
					if (isFis) {
						isFis = false;
						btCamera.setText("替换照片");
						btCamera.setBackgroundColor(Color.parseColor("#FFD700"));
					}
				}
			}
			break;
		}
	}

	/**
	 * 保存拍照的照片
	 * 
	 * @param bt
	 */
	public void saveBitmap(Bitmap bt) {
		File file = new File("/sdcard/busnotwait/");
		strPath = file.getPath() + "/busjam.jpg";
		if (!file.exists()) {
			file.mkdirs();
		}
		try {
			FileOutputStream out = new FileOutputStream(file.getPath()
					+ "/busjam.jpg");
			bt.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图片缩放
	 * 
	 * @param originalBitmap
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	private Bitmap resizeImage(Bitmap originalBitmap, int newWidth,
			int newHeight) {
		// 定义欲转换成的宽、高
		int width = originalBitmap.getWidth();
		int height = originalBitmap.getHeight();
		// 计算宽、高缩放率
		float scanleWidth = (float) newWidth / width;
		float scanleHeight = (float) newHeight / height;
		// System.out.println(scanleHeight + "..." + scanleWidth);
		Matrix matrix = new Matrix();
		matrix.postScale(scanleWidth, scanleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width,
				height, matrix, true);
		return resizedBitmap;
	}

	public void putMarkerData() {
		editor.putInt("index", pre);
		editor.putString("text", etMarkerInfo.getText().toString());
		editor.putString("path", strPath);
		editor.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_addphoto:
			Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
			startActivityForResult(intent, 0);
			break;
		case R.id.bt_report:
			putMarkerData();
			if (pre == 0 || pre == 1 || pre == 2 || pre == 3 || pre == 4
					|| pre == 5) {
				this.setResult(1);
				this.finish();
			} else {
				Toast.makeText(getApplicationContext(), "请选择类型",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.iv_back:
			this.finish();
			break;
		}
	}
}
