package com.xueya.bean;

public class MyPlanNode {
	private String key;//关键字
	private String district;//行政区
	private String city;//所在城市

	
	
	@Override
	public String toString() {
		return "MyPlanNode [key=" + key + ", district=" + district + ", city="
				+ city + "]";
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	
}
