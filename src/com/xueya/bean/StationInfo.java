package com.xueya.bean;


public class StationInfo {
	private String SqlID = "null";
	private String getID;//通过这个ID确定是否修改
	private String position;
	private String blsID;
	private String ID;
	private String stopName;
	private String startName;
	private String endName;
	private String lineNumber;
	
	private String[] arriveInfo;//到站信息
//	private Chronometer chronometer;//定时器

	public StationInfo() {
	}

	public StationInfo(String blsID, String ID, String stopName) {
		this.blsID = blsID;
		this.ID = ID;
		this.stopName = stopName;
	}
	
	public StationInfo(String SqlID, String getID, String position, String blsID, String ID, String stopName, String startName, String endName, String lineNumber) {
		this.SqlID = SqlID;
		this.getID = getID;
		this.position = position;
		this.blsID = blsID;
		this.ID = ID;
		this.stopName = stopName;
		this.startName = startName;
		this.endName = endName;
		this.lineNumber = lineNumber;
	}

	public String getSqlID() {
		return SqlID;
	}

	public void setSqlID(String sqlID) {
		SqlID = sqlID;
	}
	

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getGetID() {
		return getID;
	}

	public void setGetID(String getID) {
		this.getID = getID;
	}

	public String getBlsID() {
		return blsID;
	}

	public void setBlsID(String blsID) {
		this.blsID = blsID;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	public String getStartName() {
		return startName;
	}

	public void setStartName(String startName) {
		this.startName = startName;
	}

	public String getEndName() {
		return endName;
	}

	public void setEndName(String endName) {
		this.endName = endName;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String[] getArriveInfo() {
		return arriveInfo;
	}

	public void setArriveInfo(String[] arriveInfo) {
		this.arriveInfo = arriveInfo;
	}

//	public Chronometer getChronometer() {
//		return chronometer;
//	}
//
//	public void setChronometer(Chronometer chronometer) {
//		this.chronometer = chronometer;
//	}

	
	
}
