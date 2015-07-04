package com.xueya.tools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.xueya.bean.StationInfo;

public class JsonTools {
	
	public static JSONObject getJsonObj(String json) {
		return JSONObject.fromObject(json);
	}
	
	public static JSONObject getJsonObj(String key, JSONObject jo) {
		return jo.getJSONObject(key);
	}
	
	public static JSONArray getJsonObjs(String key, JSONObject jo) {
		return jo.getJSONArray(key);
	}

	public static String toString(Object obj) {
		return JSONObject.fromObject(obj).toString();
	}

	public static String toJSON(String key, Object value) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(key, value);
		return jsonObject.toString();
	}

	public static JSONObject getJSONObject(String key, JSONObject jsonObject) {
		return jsonObject.getJSONObject(key);
	}



	public static JSONArray getJsonArray(String key, JSONObject jsonObject) {
		return jsonObject.getJSONArray(key);
	}
	
	public static <T> T get(JSONObject obj, Class<T> cls)
			throws InstantiationException, IllegalAccessException,
			NoSuchFieldException, SecurityException {
		T t = cls.newInstance();
		@SuppressWarnings("unchecked")
		Set<Map.Entry<String, Object>> entrySet = obj.entrySet();
		Iterator<Entry<String, Object>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> m = iterator.next();
			String key = m.getKey();
			Object val = m.getValue();
			Field field = cls.getDeclaredField(key);
			field.setAccessible(true);
			field.set(t, val);
		}
		return t;
	}

	public static <T> List<T> getList(JSONArray jsonArray, Class<T> cls)
			throws InstantiationException, IllegalAccessException, NoSuchFieldException {
		List<T> list = new ArrayList<T>();
		for (int i = 0; i < jsonArray.size(); i++) {
			T t = cls.newInstance();
			JSONObject obj = (JSONObject) jsonArray.get(i);
			@SuppressWarnings("unchecked")
			Set<Map.Entry<String, Object>> entrySet = obj.entrySet();
			Iterator<Entry<String, Object>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> m = iterator.next();
				String key = m.getKey();
				Object val = m.getValue();
				Field field = cls.getDeclaredField(key);
				field.setAccessible(true);
				field.set(t, val);
			}
			list.add(t);
		}
		return list;
	}
	
	
	/**
	 * 解析json得到站点信息集合
	 * @param result 需要解析的json
	 * @return 解析成功返回站点集合 否则返回null
	 */
	public static List<StationInfo> getResultList(String result) {
		JSONObject jo = getJsonObj(result);
		int status = -1;
		status = jo.getInt("status"); // 1找到结果 2未找到结果
		if (status == 1) {
			JSONArray jsArray = JsonTools.getJsonArray("stops", jo);
			int size = jsArray.size();// 数组中元素的个数
			List<StationInfo> list = new ArrayList<StationInfo>();
			for (int i = 0; i < size; i++) {
				JSONObject js = jsArray.getJSONObject(i);// 获取指定数组元素
				StationInfo stationInfo = new StationInfo();
				stationInfo = addInfo(js);
				list.add(stationInfo);
			}
			return list;
		}
		return null;
	}

	// 返回站点信息
	private static StationInfo addInfo(JSONObject js) {
		StationInfo info = new StationInfo();
		info.setBlsID(js.getString("bls_id"));
		info.setID(js.getString("id"));
		info.setStopName(js.getString("stop_name"));
		return info;
	}
	
	/**
	 * 解析json得到车辆运行情况
	 * @param result 需要解析的字符串
	 * @return 返回字符串数组 大小为该站点总数量 值为该站点车辆数量
	 */
	public static Map<String, Object> getArriveInfo(String result){
		JSONObject jo = getJsonObj(result);
		int status = -1;
		status = jo.getInt("status");
		if(status == 0){
			JSONObject js = jo.getJSONObject("result");
			Map<String, Object> map = new HashMap<String, Object>();
			@SuppressWarnings("unchecked")
			Set<Map.Entry<String, Object>> entrySet = js.entrySet();
			Iterator<Entry<String, Object>> iterator = entrySet.iterator();
			while(iterator.hasNext()){
				Map.Entry<String, Object> m = iterator.next();
				String key = m.getKey();
				Object value = m.getValue();
				map.put(key, value);
			}
			return map;
		}
		return null;
	}
	
/*	public static String[] getArrayString(String result, int size){
		Map<String, Object> map = getArriveInfo(result);
		Set<Map.Entry<String, Object>> set = map.entrySet();
		String[] str = new String[size];
		for(Iterator<Map.Entry<String, Object>> it = set.iterator(); it.hasNext();){
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
			System.out.println(entry.getKey()+"----"+entry.getValue());
			System.out.println("get---"+map.get("0"));
			str[Integer.parseInt(entry.getKey())] = entry.getValue().toString();
		}
		return str;
	}*/

}
