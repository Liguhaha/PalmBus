package com.xueya.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StationSQLiteHelper extends SQLiteOpenHelper {

	public StationSQLiteHelper(Context context) {
		super(context, "station.db", null, 1);
	}

	/**
	 * 创建数据库 station 表名 id 主键自增 get_id搜索的id position站点所在集合的编号  bls_id 汽车id station_id 站点id
	 * stop_name 站点名称 line_number线路编号 start起点 end终点
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// 保存站点信息的表
		db.execSQL("create table [station] ([id] integer not null primary key autoincrement"
				+ ", [get_id] varchar(20),[position] varchar(20), [bls_id] varchar(20), [station_id] text, [stop_name] "
				+ "varchar(20), [line_number] varchar(20), [start] varchar(20), [end] varchar(20))");
		// 保存历史记录的表
		db.execSQL("create table [history] ([id] integer not null primary key autoincrement,"
				+ "[start] varchar(20),[end] varchar(20),[search_city] varchar[20], [s_city] varchar[20]"
				+ ", [e_city] varchar[20], [save_time] varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 更新数据库版本的操作
	}

}
