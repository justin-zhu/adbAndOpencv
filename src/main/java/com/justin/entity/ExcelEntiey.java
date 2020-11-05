package com.justin.entity;

import com.justin.utils.Tools;

import cn.hutool.core.convert.Convert;

public class ExcelEntiey {

	String comment;
	String type;
	Integer sleep;
	String head;
	String dataBody;
	Integer count;
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getSleep() {
		int sleepNum = Convert.toInt(sleep, 0);
		try {
			Tools.println("延时:"+sleepNum);
			Thread.sleep(sleepNum);
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
		return Convert.toInt(sleep, 0);
	}
	public void setSleep(Integer sleep) {
		this.sleep = sleep;
	}
	public String getHead() {
		return head;
	}
	public void setHead(String head) {
		this.head = head;
	}
	public String getDataBody() {
		return dataBody;
	}
	public void setDataBody(String dataBody) {
		this.dataBody = dataBody;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return "[comment=" + comment + ", type=" + type + ", sleep=" + sleep + ", head=" + head
				+ ", dataBody=" + dataBody + ", count=" + count + "]";
	}	
	
}
