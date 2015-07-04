package com.xueya.bean;

public class BusInfo {
	private String end_stop;
	private String id;
	private String line_name;
	private String start_stop;
	
	@Override
	public String toString() {
		return "BusInfo [end_stop=" + end_stop + ", id=" + id + ", line_name="
				+ line_name + ", start_stop=" + start_stop + "]";
	}

	public String getEnd_stop() {
		return end_stop;
	}

	public void setEnd_stop(String end_stop) {
		this.end_stop = end_stop;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLine_name() {
		return line_name;
	}

	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}

	public String getStart_stop() {
		return start_stop;
	}

	public void setStart_stop(String start_stop) {
		this.start_stop = start_stop;
	}


	
}
