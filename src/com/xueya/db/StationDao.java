package com.xueya.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xueya.bean.History;
import com.xueya.bean.StationInfo;

public class StationDao {

	private static final String SQLITENAME = "station";
	private static final String GETID = "get_id";
	private static final String POSITION = "position";
	private static final String BLSID = "bls_id";
	private static final String STATIONID = "station_id";
	private static final String STOPNAME = "stop_name";
	private static final String START = "start";
	private static final String END = "end";
	private static final String LINE_NUMBER = "line_number";

	private static final String HIS_START = "start";
	private static final String HIS_END = "end";
	private static final String HIS_SAVA_TIME = "save_time";
	private static final String HIS_SEARCH_CITY = "search_city";
	private static final String HIS_NAME = "history";
	private static final String HIS_SCITY = "s_city";
	private static final String HIS_ECITY = "e_city";

	private StationSQLiteHelper helper;

	// 在构造方法里面完成对helper的初始化
	public StationDao(Context context) {
		helper = new StationSQLiteHelper(context);
	}

	/**
	 * 
	 * @param get_id
	 *            搜索id
	 * @param bls_id
	 *            汽车id
	 * @param position
	 *            该站点所在集合编号
	 * @param station_id
	 *            站点id
	 * @param stop_name
	 *            站点名称
	 * @param start
	 *            起点
	 * @param end
	 *            终点
	 * @param line_number
	 *            线路编号
	 * @return
	 */
	public long add(String get_id, String position, String bls_id,
			String station_id, String stop_name, String start, String end,
			String line_number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(GETID, get_id);
		values.put(POSITION, position);
		values.put(BLSID, bls_id);
		values.put(STATIONID, station_id);
		values.put(STOPNAME, stop_name);
		values.put(START, start);
		values.put(END, end);
		values.put(LINE_NUMBER, line_number);
		long result = db.insert(SQLITENAME, null, values);
		return result;

	}

	/**
	 * 删除一条数据
	 * 
	 * @param getID
	 *            需要删除的id
	 * @return 返回正数代表删除成功 负数代表删除失败
	 */
	public long delete(String getID) {
		SQLiteDatabase db = helper.getWritableDatabase();
		long result = db.delete(SQLITENAME, GETID + "=?",
				new String[] { getID });
		db.close();
		return result;
	}

	/**
	 * 修改一条数据
	 * 
	 * @param id
	 *            需要修改的id
	 * @param get_id
	 *            搜索id
	 * @param position
	 *            该站点所在集合编号
	 * @param bls_id
	 *            新的bls_id
	 * @param station_id
	 *            新的station_id
	 * @param stop_name
	 *            新的stop_name
	 * @param start
	 *            新的起点
	 * @param end
	 *            新的终点
	 * @param line_number
	 *            新的线路
	 * @return 正数成功 负数失败
	 */
	public long update(String id, String get_id, String position,
			String bls_id, String station_id, String stop_name, String start,
			String end, String line_number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(GETID, get_id);
		values.put(POSITION, position);
		values.put(BLSID, bls_id);
		values.put(STATIONID, station_id);
		values.put(STOPNAME, stop_name);
		values.put(START, start);
		values.put(END, end);
		values.put(LINE_NUMBER, line_number);
		int result = db.update(SQLITENAME, values, "id=?", new String[] { id });
		db.close();
		return result;
	}

	/**
	 * 修改一条数据
	 * 
	 * @param get_id
	 *            搜索id
	 * @param bls_id
	 *            新的bls_id
	 * @param position
	 *            该站点所在集合编号
	 * @param station_id
	 *            新的station_id
	 * @param stop_name
	 *            新的stop_name
	 * @param start
	 *            新的起点
	 * @param end
	 *            新的终点
	 * @param line_number
	 *            新的线路
	 * @return 正数成功 负数失败
	 */
	public long update(String get_id, String position, String bls_id,
			String station_id, String stop_name, String start, String end,
			String line_number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(POSITION, position);
		values.put(BLSID, bls_id);
		values.put(STATIONID, station_id);
		values.put(STOPNAME, stop_name);
		values.put(START, start);
		values.put(END, end);
		values.put(LINE_NUMBER, line_number);
		int result = db.update(SQLITENAME, values, GETID + "=?",
				new String[] { get_id });
		db.close();
		return result;
	}

	// String sql = "select * from "+SQLITENAME+" where "+GETID+"=id";
	/**
	 * 查询该记录是否存在
	 * 
	 * @param getID
	 *            id
	 * @return true 存在 false不存在
	 */
	public boolean findGetID(String getID) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query(SQLITENAME, null, GETID + "=?",
				new String[] { String.valueOf(getID) }, null, null, null);
		boolean bl = cursor.moveToNext();
		cursor.close();
		db.close();
		return bl;
	}

	public String findBlsID(String get_id) {
		String bls_id = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query(SQLITENAME, new String[] { BLSID }, GETID
				+ "=?", new String[] { get_id }, null, null, null);
		while (cursor.moveToNext()) {
			bls_id = cursor.getString(cursor.getColumnIndex(BLSID));
		}
		return bls_id;
	}

	/**
	 * 遍历数据库中的数据
	 * 
	 * @return 返回List<StationInfo>集合
	 */
	public List<StationInfo> findAll() {
		SQLiteDatabase db = helper.getReadableDatabase();
		List<StationInfo> stations = new ArrayList<StationInfo>();
		Cursor cursor = db.query(SQLITENAME, new String[] { "id", POSITION,
				GETID, BLSID, STATIONID, STOPNAME, START, END, LINE_NUMBER },
				null, null, null, null, null);
		while (cursor.moveToNext()) {
			String id = cursor.getInt(cursor.getColumnIndex("id")) + "";
			String get_id = cursor.getString(cursor.getColumnIndex(GETID));
			String position = cursor.getString(cursor.getColumnIndex(POSITION));
			String bls_id = cursor.getString(cursor.getColumnIndex(BLSID));
			String station_id = cursor.getString(cursor
					.getColumnIndex(STATIONID));
			String stop_name = cursor
					.getString(cursor.getColumnIndex(STOPNAME));
			String start = cursor.getString(cursor.getColumnIndex(START));
			String end = cursor.getString(cursor.getColumnIndex(END));
			String line_number = cursor.getString(cursor
					.getColumnIndex(LINE_NUMBER));
			StationInfo station = new StationInfo(id, get_id, position, bls_id,
					station_id, stop_name, start, end, line_number);
			stations.add(station);
		}
		cursor.close();
		db.close();
		return stations;
	}

	// HISTORY
	/**
	 * 插入历史记录
	 * 
	 * @param start起点
	 * @param end终点
	 * @param search_city搜索城市
	 * @param sCity起点城市
	 * @param eCity终点城市
	 * @return插入结果 -1说明插入失败
	 */
	public long add(String start, String end, String search_city, String sCity,
			String eCity) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HIS_START, start);
		values.put(HIS_END, end);
		values.put(HIS_SEARCH_CITY, search_city);
		values.put(HIS_SCITY, sCity);
		values.put(HIS_ECITY, eCity);
		values.put(HIS_SAVA_TIME, System.currentTimeMillis() + "");
		long result = db.insert(HIS_NAME, null, values);
		db.close();
		return result;
	}

	/**
	 * 获取数据库中的条目个数
	 * 
	 * @param name
	 *            数据库名称
	 * @return 数据库条目个数
	 */
	public long size(String name) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select count(*)from " + name, null);
		cursor.moveToFirst();
		Long count = cursor.getLong(0);
		cursor.close();
		return count;
	}

	/**
	 * 更新历史数据 如果数据大于5 更新最早的那条数据
	 * 
	 * @param start新的起点
	 * @param end新的终点
	 * @param search_city新的搜索城市
	 * @param sCity
	 *            新的起点城市
	 * @param eCity
	 *            新的终点城市
	 * @return更新的条目个数
	 */
	public long update(String start, String end, String search_city,
			String sCity, String eCity) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HIS_START, start);
		values.put(HIS_END, end);
		values.put(HIS_SEARCH_CITY, search_city);
		values.put(HIS_SCITY, sCity);
		values.put(HIS_ECITY, eCity);
		values.put(HIS_SAVA_TIME, System.currentTimeMillis() + "");

		long result = db.update(HIS_NAME, values, HIS_START + "=? AND "
				+ HIS_END + "=? AND " + HIS_SEARCH_CITY + " =? AND "
				+ HIS_SCITY + " =? AND " + HIS_ECITY + "=?", new String[] {
				start, end, search_city, sCity, eCity });
		db.close();
		return result;
	}

	/**
	 * 添加方法 保持记录只有五个
	 * 
	 * @param start
	 *            起点
	 * @param end
	 *            终点
	 * @param search_city
	 *            搜索城市
	 * @param sCity
	 *            起点城市
	 * @param eCity
	 *            终点城市
	 * @return -1代表添加失败
	 */
	public long MyInsert(String start, String end, String search_city,
			String sCity, String eCity) {
		long result = update(start, end, search_city, sCity, eCity);
		System.out.println("size   " + size(HIS_NAME));
		if (result == 0) {// 没有更新原有的数据
			if (size(HIS_NAME) < 5) {// 如果历史记录小于5条
				result = add(start, end, search_city, sCity, eCity);
			} else {// 否则
				SQLiteDatabase db = helper.getWritableDatabase();
				Cursor cursor = db.rawQuery("select * from " + HIS_NAME
						+ " where " + HIS_SAVA_TIME + "=(select min("
						+ HIS_SAVA_TIME + ") from " + HIS_NAME + ")", null);
				cursor.moveToFirst();
				String save_time = cursor.getString(cursor
						.getColumnIndex(HIS_SAVA_TIME));
				System.out.println("sava_time " + save_time);
				ContentValues values = new ContentValues();
				values.put(HIS_START, start);
				values.put(HIS_END, end);
				values.put(HIS_SEARCH_CITY, search_city);
				values.put(HIS_SCITY, sCity);
				values.put(HIS_ECITY, eCity);
				values.put(HIS_SAVA_TIME, System.currentTimeMillis() + "");
				result = db.update(HIS_NAME, values, HIS_SAVA_TIME + "=?",
						new String[] { save_time });
				cursor.close();
				db.close();
			}
		}

		return result;
	}

	/**
	 * 清除所有记录
	 * 
	 * @return
	 */
	public long delAllHis() {
		SQLiteDatabase db = helper.getWritableDatabase();
		long result = db.delete(HIS_NAME, null, null);
		return result;
	}

	/**
	 * 按照时间返回历史记录集合
	 * 
	 * @return历史记录集合
	 */
	public List<History> findHisALL() {
		SQLiteDatabase db = helper.getReadableDatabase();
		List<History> historys = new ArrayList<History>();
		Cursor cursor = db.rawQuery("select * from " + HIS_NAME + " order by "
				+ HIS_SAVA_TIME + " desc", null);
		while (cursor.moveToNext()) {
			String start = cursor.getString(cursor.getColumnIndex(HIS_START));
			String end = cursor.getString(cursor.getColumnIndex(HIS_END));
			String search_city = cursor.getString(cursor
					.getColumnIndex(HIS_SEARCH_CITY));
			String sCity = cursor.getString(cursor.getColumnIndex(HIS_SCITY));
			String eCity = cursor.getString(cursor.getColumnIndex(HIS_ECITY));
			String time = cursor
					.getString(cursor.getColumnIndex(HIS_SAVA_TIME));
			History history = new History(start, end, search_city, sCity,
					eCity, time);
			historys.add(history);
		}
		cursor.close();
		db.close();
		return historys;
	}


}
