package com.xueya.bean;
//历史记录
public class History {
	private String Start;
	private String End;
	private String search_city;
	private String sCity;
	private String eCity;
	private String time;
	
	
	public History(String Start, String End, String search_city ,String sCity, String eCity){
		this.Start = Start;
		this.End = End;
		this.search_city = search_city;
		this.sCity = sCity;
		this.eCity = eCity;
	}
	
	public History(String Start, String End, String search_city ,String sCity, String eCity, String time){
		this.Start = Start;
		this.End = End;
		this.search_city = search_city;
		this.sCity = sCity;
		this.eCity = eCity;
		this.time = time;
	}
	
	public String getStart() {
		return Start;
	}
	public void setStart(String start) {
		Start = start;
	}
	public String getEnd() {
		return End;
	}
	public void setEnd(String end) {
		End = end;
	}

	public String getSearch_city() {
		return search_city;
	}

	public void setSearch_city(String search_city) {
		this.search_city = search_city;
	}

	public String getsCity() {
		return sCity;
	}

	public void setsCity(String sCity) {
		this.sCity = sCity;
	}

	public String geteCity() {
		return eCity;
	}

	public void seteCity(String eCity) {
		this.eCity = eCity;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	
	
	
}
