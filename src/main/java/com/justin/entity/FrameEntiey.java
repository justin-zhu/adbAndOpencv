package com.justin.entity;

import com.justin.utils.Tools;

public class FrameEntiey {
	//程序运行状
	private boolean isRun;
	//设备ID
	private String deviceID;
	//波特率
	private String baudRate;
	//CAN盒号
	private String canIndex;
	//CAN盒通道号
	private String canChannelIndex;
	//用例文件路径
	private String casePath;
	//模版图片路径
	private String modelPath;
	//记录PASS数据
	private int passNum;
	//记录FAIL数据
	private int failNum;
	//指定测试次数
	private int testNum;
	//为模版模式指定一个状态
	private boolean isTemplateModel;
	//记录本程序所在的目录
	private String projectDirectory;
	
	public int getTestNum() {
		return testNum;
	}
	public boolean isTemplateModel() {
		return isTemplateModel;
	}
	public void setTemplateModel(boolean isTemplateModel) {
		this.isTemplateModel = isTemplateModel;
	}
	public void setTestNum(int testNum) {
		this.testNum = testNum;
	}
	public int getPassNum() {
		return passNum;
	}
	public void setPassNum(int passNum) {
		this.passNum = passNum;
	}
	public int getFailNum() {
		return failNum;
	}
	public void setFailNum(int failNum) {
		this.failNum = failNum;
	}
	
	public String getModelPath() {
		return modelPath;
	}
	public boolean isRun() {
		return isRun;
	}
	public void setRun(boolean isRun) {
		this.isRun = isRun;
	}
	public void setModelPath(String modelPath) {
		Tools.println("图片模板路径:"+modelPath);
		this.modelPath = modelPath;
	}
	/**
	 * @return 设备ID
	 */
	public String getDeviceID() {
		return deviceID;
	}
	/**
	 * @return 波特率
	 */
	public String getBaudRate() {
		return baudRate;
	}
	/**
	 * @return CAN盒下标
	 */
	public String getCanIndex() {
		return canIndex;
	}
	/**
	 * @return CAN盒通道
	 */
	public String getCanChannelIndex() {
		return canChannelIndex;
	}
	/**
	 * @param 设备ID
	 */
	public void setDeviceID(String deviceID) {
		Tools.println("设备ID:"+deviceID);
		this.deviceID = deviceID;
	}
	/**
	 * @param 波特率
	 */
	public void setBaudRate(String baudRate) {
		Tools.println("波特率:"+baudRate);
		this.baudRate = baudRate;
	}
	/**
	 * @param CAN盒下标
	 */
	public void setCanIndex(String canIndex) {
		Tools.println("CAN下标:"+canIndex);
		this.canIndex = canIndex;
	}
	/**
	 * @param CAN盒通道号
	 */
	public void setCanChannelIndex(String canChannelIndex) {
		Tools.println("CAN盒通道号:"+canChannelIndex);
		this.canChannelIndex = canChannelIndex;
	}
	/**
	 * @return 用例路径
	 */
	public String getCasePath() {
		return casePath;
	}
	/**
	 * @param 用例路径
	 */
	public void setCasePath(String casePath) {
		Tools.println("用例路径:" + casePath);
		this.casePath = casePath;
	}
	public String getProjectDirectory() {
		return projectDirectory;
	}
	public void setProjectDirectory(String projectDirectory) {
		this.projectDirectory = projectDirectory;
	}
	@Override
	public String toString() {
		return "FrameEntiey [deviceID=" + deviceID + ", baudRate=" + baudRate + ", canIndex=" + canIndex
				+ ", canChannelIndex=" + canChannelIndex + ", casePath=" + casePath + ", modelPath=" + modelPath + "]";
	}
	
}
