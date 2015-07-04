package com.xueya.tools;

import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.VehicleInfo;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.xueya.bean.MyPlanNode;

public class MyTransitRouteResult {

	private String CITY = "";// 当前所在城市
	private LatLng latLng = null;// 当前经纬点

	private BDLocation bdLocation = null;
	private MyPlanNode stPlanNode = null;// 起点信息
	private MyPlanNode enPlanNode = null;// 终点信息

	private TransitRouteResult myTransitRouteResult = null;// 获取到的所有换乘结果
	private WalkingRouteResult myWalkingRouteResult = null;//获取到的所有步行结果
	
	
	// 单例
	private static MyTransitRouteResult myTransitRouteLine = new MyTransitRouteResult();

	private MyTransitRouteResult() {
	}

	public static MyTransitRouteResult getMyTransitRouteLine() {
		return myTransitRouteLine;
	}

	/**
	 * 设置当前所在城市
	 * 
	 * @param CITY
	 *            需要设置的城市
	 */
	public void setCITY(String CITY) {
		this.CITY = CITY;
	}

	/**
	 * 得到当前所在城市
	 * 
	 * @return 当前所在城市
	 */
	public String getCITY() {
		return CITY;
	}

	/**
	 * 设置经纬点
	 * 
	 * @param latLng
	 *            需要设置的经纬点
	 */
	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}

	/**
	 * 返回起点信息
	 * 
	 * @return 起点信息
	 */
	public MyPlanNode getStPlanNode() {
		return stPlanNode;
	}

	/**
	 * 设置起点信息
	 * 
	 * @param stPlanNode
	 *            起点信息
	 */
	public void setStPlanNode(MyPlanNode stPlanNode) {
		this.stPlanNode = stPlanNode;
	}

	/**
	 * 返回终点信息
	 * 
	 * @return 终点信息
	 */
	public MyPlanNode getEnPlanNode() {
		return enPlanNode;
	}

	/**
	 * 设置终点信息
	 * 
	 * @param enPlanNode
	 *            终点信息
	 */
	public void setEnPlanNode(MyPlanNode enPlanNode) {
		this.enPlanNode = enPlanNode;
	}

	/**
	 * 返回经纬点
	 * 
	 * @return 经纬点
	 */
	public LatLng getLatLng() {
		return latLng;
	}

	/**
	 * 返回位置信息
	 * 
	 * @return 位置信息BDLocation
	 */
	public BDLocation getBdLocation() {
		return bdLocation;
	}

	/**
	 * 设置位置信息
	 * 
	 * @param bdLocation
	 *            位置信息
	 */
	public void setBdLocation(BDLocation bdLocation) {
		this.bdLocation = bdLocation;
	}

	
	/**
	 * 获取到的所有步行结果
	 * @return 获取到的所有步行结果
	 */
	public WalkingRouteResult getMyWalkingRouteResult() {
		return myWalkingRouteResult;
	}
	
	/**
	 * 设置所有步行结果
	 * @param myWalkingRouteResult 所有步行结果
	 */
	public void setMyWalkingRouteResult(WalkingRouteResult myWalkingRouteResult) {
		this.myWalkingRouteResult = myWalkingRouteResult;
	}

	/**
	 * 设置获取到的所有换乘结果
	 * 
	 * @param myTransitRouteResult
	 *            获取到的换乘结果
	 */
	public void setTransitRouteResult(TransitRouteResult myTransitRouteResult) {
		this.myTransitRouteResult = myTransitRouteResult;
	}

	/**
	 * 获取到的所有换乘结果
	 * 
	 * @return 获取到的所有换乘结果
	 */
	public TransitRouteResult getMyTransitRouteResult() {
		return myTransitRouteResult;
	}

	/**
	 * 换乘路线方案集合
	 * 
	 * @return 换乘路线方案集合
	 */
	public List<TransitRouteLine> getmTransitRouteLines() {
		return getMyTransitRouteResult().getRouteLines();// 所有换乘路线方案
	}

	/**
	 * 根据编号获取方案
	 * 
	 * @param n
	 *            编号
	 * @return 改编号对应的方案
	 */
	public TransitRouteLine getTransitRouteLine(int n) {
		return getmTransitRouteLines().get(n);// 第n个方案
	}

	/**
	 * 根据方案编号获取总距离
	 * 
	 * @param n
	 *            方案号
	 * @return 该方案的总距离
	 */
	public int getAllDistance(int n) {
		return getTransitRouteLine(n).getDistance();// 第n套方案的总距离
	}

	/**
	 * 根据方案数获取总耗时
	 * 
	 * @param n
	 *            方案号
	 * @return 该方案的总耗时
	 */
	public int getAllDuration(int n) {
		return getTransitRouteLine(n).getDuration();// 第n套方案的总时间
	}

	/**
	 * 根据方案号获取换乘路段集合
	 * 
	 * @param n
	 *            方案号
	 * @return 换乘路段集合
	 */
	public List<TransitRouteLine.TransitStep> getTransitSteps(int n) {
		List<TransitRouteLine.TransitStep> transitStepsList = getTransitRouteLine(
				n).getAllStep();
		return transitStepsList;// 第n套方案换乘集合
	}

	/**
	 * 根据方案号获取该方案主要情况
	 * 
	 * @param n
	 *            方案号
	 * @return 字符串数组 String[0]车辆编号 1站台数量 2步行距离
	 */
	public String[] getmTransitStep(int n) {
		int walkDis = 0;// 步行距离
		int stationNum = 0;// 站台数
		String busTitle = "";// 车辆编号
		int m = getTransitSteps(n).size();// 该方案节点个数
		int flag = 0;
		for (int i = 0; i < m; i++) {
			TransitRouteLine.TransitStep transitStep = getTransitSteps(n)
					.get(i);// 获取第i个节点
			String stepType = "";
			if (transitStep.getStepType() == null) {
				if (transitStep.getVehicleInfo() == null) {
					stepType = "WALKING";// 该节点为步行信息
				} else {
					stepType = "UNKNOW";// 公交或地铁
				}
			} else
				stepType = transitStep.getStepType().toString();// 获取节点类型
			if (stepType.equals("BUSLINE") || stepType.equals("SUBWAY")
					|| stepType.equals("UNKNOW")) {
				VehicleInfo vehicleInfo = transitStep.getVehicleInfo();
				if (flag == 0) {
					busTitle += vehicleInfo.getTitle();// 交通线路名称
				} else if (flag == 1) {
					busTitle += " → " + vehicleInfo.getTitle();// 交通线路名称
				}
				stationNum += vehicleInfo.getPassStationNum();// 经过的站台数量
				flag = 1;
			} else if (stepType.equals("WAKLING")) {
				walkDis += transitStep.getDistance();// 步行距离
			}
		}

		String[] guidInfo = new String[3];
		guidInfo[0] = busTitle;
		guidInfo[1] = stationNum + "";
		guidInfo[2] = walkDis + "";
		return guidInfo;
	}

}