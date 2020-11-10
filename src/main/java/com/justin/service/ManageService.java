package com.justin.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.justin.entity.ExcelEntiey;
import com.justin.entity.FrameEntiey;
import com.justin.mavenTest.ImageRecognition;
import com.justin.mavenTest.JavaRunPyton;

import com.justin.utils.Tools;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

public class ManageService extends Observable {
	private static Logger loger = Logger.getLogger(ManageService.class);
	private JavaRunPyton javaRunPyton;
	private FrameEntiey frameEntiey;
	private  ImageRecognition imageRecognition;

	public ManageService(FrameEntiey frameEntiey) {
		this.frameEntiey = frameEntiey;
		javaRunPyton = new JavaRunPyton();
		imageRecognition = new ImageRecognition();
		imageRecognition.setFrameEntiey(frameEntiey);
	}

	/**
	 * 
	 * @param tempPath     模板图片路径
	 * @param originalPath 截图路路径
	 * @return
	 */
	public boolean getImagePatternResult(String tempPath, String originalPath) {
		// 读取图片文件
		Mat templateImage = Highgui.imread(tempPath, Highgui.CV_LOAD_IMAGE_COLOR);
		Mat originalImage = Highgui.imread(originalPath, Highgui.CV_LOAD_IMAGE_COLOR);
		return imageRecognition.matchImage(templateImage, originalImage);
	}

	public void creatModel() {
		// 获取表格中的数据
		Tools.println("进入模版制作");
		frameEntiey.setTemplateModel(true);
		//设置模式模式运行的次数 1
		frameEntiey.setTestNum(1);
		List<ExcelEntiey> entieyList = getBeanFromPath(frameEntiey.getCasePath());
		parseEntieyListAndProcess(entieyList,frameEntiey.getTestNum());
		//模板制作完成后将默认次数重置为-1
		frameEntiey.setTestNum(-1);
		Tools.println("模版制作完成");
		frameEntiey.setPassNum(0);
		JOptionPane.showMessageDialog(null, "模板已制作完成!");
	}
	/**
	 * 删除测试前的旧图片
	 */
	public void delModePathTempImage() {
		File file = new File(frameEntiey.getModelPath(), "tempImage");
		Tools.println("正在删除旧的图片文件");
		if(file.exists()) {
			try {
				//强制删除
				FileUtils.forceDelete(file);
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "删除旧图片失败,请查看是否被占用");
				e.printStackTrace();
			}			
		}
		//删除完成后 重新建立新的文件夹
		file.mkdirs();
	}
	/**
	 * 开始测试
	 * 
	 * @param frameEntiey
	 */
	public void startTest() {
		//获取模版路径 每次开启新的测试前 删除旧的图片文件 防止占用大量空间
		delModePathTempImage();
		// 禁用模版模式
		frameEntiey.setTemplateModel(false);
		// 获取用户指定的测试次数
		int testNum = frameEntiey.getTestNum();
		// 获取表格中的数据
		List<ExcelEntiey> entieyList = getBeanFromPath(frameEntiey.getCasePath());
		loger.debug("指定测试次数:" + testNum);
		if (testNum > 0) {
			loger.debug("大于0");
			parseEntieyListAndProcess(entieyList, testNum);
		} else {
			loger.debug("小于0");
			parseEntieyListAndProcess(entieyList);
		}

	}

	/**
	 * 如果指定的测试次数大于0测执行此方法
	 * @param entieyList
	 * @param testNum
	 */
	public void parseEntieyListAndProcess(List<ExcelEntiey> entieyList, int testNum) {
		loger.debug("指定次数:"+testNum+"次(-1代表无限次数)");
		// 定义一个计数器
		int currentNum = 0;
		while (currentNum < testNum) {
			for (ExcelEntiey excelEntiey : entieyList) {
				if (frameEntiey.isRun()) {
					switchMethod(excelEntiey);
				} else {
					Tools.println("程序已被强制结束");
					notifyObserversUpdateButtonStart();
					return;
				}
			}
			loger.debug("已完成一轮测试,当前第" + (currentNum+1) + "次");
			// 执行完一轮后 当前的测试次数加1
			currentNum++;
			loger.debug("历史次数:"+frameEntiey.getPassNum());
			frameEntiey.setPassNum(frameEntiey.getPassNum()+1);
			loger.debug("当前次数:"+(frameEntiey.getPassNum()));
			//通知界面更新数据
			notifyObserversUpdateTestNum();
			// 删除sdcard下的临时图片
			delSdcardImage();
			notifyObserversCleanJTextArea();
		}
		Tools.println("程序已执行完成!");
		notifyObserversUpdateButtonStart();
	}
	/**
	 * 通知更新测试次数
	 */
	private void notifyObserversUpdateTestNum() {
		setChanged();
		notifyObservers("updateTestNum");
	}
	/**
	 * 通知更新测试次数
	 */
	private void notifyObserversCleanJTextArea() {
		setChanged();
		notifyObservers("cleanJTextArea");
	}
	/**
	 * 如果指定的测试次数小于0则执行此方法
	 * @param excelEntieyList
	 */
	public void parseEntieyListAndProcess(List<ExcelEntiey> excelEntieyList) {
		
		int listSize = excelEntieyList.size();
		for (int i = 0; i < listSize; i++) {
			ExcelEntiey excelEntiey = excelEntieyList.get(i);
			if (frameEntiey.isRun()) {
				switchMethod(excelEntiey);
			} else {
				Tools.println("程序已被强制结束");
				notifyObserversUpdateButtonStart();
				excelEntieyList = null;
				return;
			}
			// 如果本次循环完成 将下标重置为-1 继续运行
			if (i == (listSize - 1)) {	
				//此处指定-1是因为再次进入for循环时，会自动+1，所以下标会变成0
				i = -1;				
				loger.debug("历史次数:"+frameEntiey.getPassNum());
				frameEntiey.setPassNum(frameEntiey.getPassNum()+1);
				loger.debug("当前次数:"+(frameEntiey.getPassNum()));
				notifyObserversUpdateTestNum();
				// 删除sdcard下的临时图片
				delSdcardImage();
				notifyObserversCleanJTextArea();
			}

		}

	}
	
	public void switchMethod(ExcelEntiey excelEntiey) {
		Tools.println("读取的数据:" + excelEntiey);
		switch (excelEntiey.getType()) {
		case "adb":
			adb(excelEntiey);
			break;
		case "sleep":
			excelEntiey.getSleep();
			break;
		case "message":
			sendMessage(excelEntiey);
			excelEntiey.getSleep();
			break;
		case "screen":
			//如果处于模版制作模式 则只截图 不做图片匹配
			if (frameEntiey.isTemplateModel()) {
				defaultShotScreenByTemplate(excelEntiey);
			} else {
				screenImageRecognition(excelEntiey);
			}
			break;
		default:
			break;
		}
	}
	/**
	 * 获取表格中的数据 并映射成一个list集合
	 * @param path
	 * @return
	 */
	public List<ExcelEntiey> getBeanFromPath(String path) {
		try {
			return Tools.getExcelEntiey(path);
		} catch (Exception e) {	
			notifyObserversUpdateButtonStart();
			Tools.scheduleTask("文件被占用或映射失败,请检查", 4000);
			throw new RuntimeException("文件被占用或映射失败,请检查");
		}
		
	}

	public void adb(ExcelEntiey excelEntiey) {
		String oldADBCommand = excelEntiey.getDataBody();
		String newADBCommand = oldADBCommand.replace("adb", Tools.getAdb(frameEntiey.getDeviceID()));
		String resultStr = RuntimeUtil.execForStr(newADBCommand);
		loger.debug("adb执行完成:" + newADBCommand + "," + resultStr);
		excelEntiey.getSleep();
	}

	/**
	 * 更新设备下拉框内容
	 * 
	 * @param box
	 */
	public void addItem(JComboBox<String> box) {
		box.removeAllItems();
		HashSet<String> set = Tools.getDevices();
		box.addItem("请选择");
		for (String string : set) {
			box.addItem(string.trim());
		}
	}

	/**
	 * 为用户提供简单的截图 并保存到指定的路径
	 */
	public void shotScreenByUser() {
		String currentTime = Tools.getDateTime();
		String savePath = Tools.getFilePathOfSave();
		if (!savePath.contains("已取消选择")) {
			shotScreen(currentTime);
			pullScreen(savePath, currentTime);
		}
	}

	/**
	 * 默认截图操作并保存到用户指定的模版路径下的tempImage中
	 * @return
	 */
	public String defaultShotScreen() {
		String currentTime = Tools.getDateTime();
		shotScreen(currentTime);
		String savePath = frameEntiey.getModelPath() + "\\" + "tempImage";
		pullScreen(savePath, currentTime);
		return savePath + "\\" + currentTime + ".jpg";
	}

	/**
	 * 模版模式制作
	 * @param entiey
	 * @return
	 */
	public void defaultShotScreenByTemplate(ExcelEntiey entiey) {
		String fileName = entiey.getDataBody().replace(".jpg", "");
		loger.debug(fileName);
		shotScreen(fileName);
		String savePath = frameEntiey.getModelPath();
		pullScreen(savePath,fileName );
		
	}

	/*
	 * 将截图保存到选定的目录下
	 */
	public void pullScreen(String savePath, String fileName) {
		String command = Tools.getAdb(frameEntiey.getDeviceID()) + " pull /sdcard/" + fileName + ".jpg" + " " + savePath
				+ "\\" + fileName + ".jpg";
		loger.debug("pull命令:" + command);
		String str = RuntimeUtil.execForStr(command);
		loger.debug("保存图片结果:" + str);
	}
	/*
	 * 比图
	 */
	public void screenImageRecognition(ExcelEntiey excelEntiey) {
		String tempPath = frameEntiey.getModelPath() + "\\" + excelEntiey.getDataBody();
		String originalPath = defaultShotScreen();

		loger.debug("模版图片路径:" + tempPath);
		loger.debug("截图图片路径:" + originalPath);
		boolean imagePatternResult = getImagePatternResult(tempPath, originalPath);
		if (imagePatternResult) {
			Tools.println("图片匹配成功");
		} else {
			Tools.println("图片匹配失败,程序已暂停");
			// 获取原来的失败次数并加1
			frameEntiey.setFailNum(frameEntiey.getFailNum() + 1);
			// 通知监听者改变UI数据						
			notifyObserversUpdateTestNum();
			notifyObserversUpdateButtonStart();
			// 发送日志记录报文
			sendErrorRec();
			// 拋出异常 然后终止当前线程程序运行
			//将运行状态置为FALSE
			frameEntiey.setRun(false);
			throw new RuntimeException("图片匹配失败");
		}
	}


	/**
	 * 通知更新开始按钮-可点击状态
	 */
	private void notifyObserversUpdateButtonStart() {
		setChanged();
		notifyObservers("updateButtonStart");
	}

	/**
	 * @param 传入一个文件名称 执行截图操作 截图的设备为已选择的设备ID
	 */
	public void shotScreen(String fileName) {
		String command = Tools.getAdb(frameEntiey.getDeviceID()) + " shell screencap -p /sdcard/" + fileName + ".jpg";
		Tools.println("截图命令:" + command);		
		RuntimeUtil.execForStr(command);
		sleep(1000);
	}

	/**
	 * 检查是否已选择有效设备ID
	 * 
	 * @return
	 */
	public boolean isCheckDevice() {
		if ("请选择".equals(frameEntiey.getDeviceID())) {
			JOptionPane.showMessageDialog(null, "请选择设备后，再执行操作");
			return false;
		}
		return true;
	}

	/**
	 * 休眠
	 * 
	 * @param ms
	 */
	public static void sleep(int ms) {
		try {
			Tools.println("延时(毫秒):" + ms);
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.toString();
		}
	}

	/**
	 * 发送日志记录报文 一般用在比图失败后调用
	 */
	public void sendErrorRec() {
		ExcelEntiey excelEntiey = new ExcelEntiey();
		excelEntiey.setHead("0x6dc");
		excelEntiey.setDataBody("0x10, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00");
		excelEntiey.setCount(1);
		sendMessage(excelEntiey);
	}

	/**
	 * 发送报文
	 */
	public void sendMessage(ExcelEntiey excelEntiey) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					String str = excelEntiey.getDataBody();
					// 添加报文前八位数据
					List<String> list = StrUtil.split(str, ',', -1);
					Tools.println(list);
					// 添加报文ID
					list.add(excelEntiey.getHead());
					// 添加设置的波特率
					list.add(frameEntiey.getBaudRate());
					list.add(frameEntiey.getCanIndex());// 设备ID
					list.add(frameEntiey.getCanChannelIndex());// 设备通道号
					list.add(excelEntiey.getCount().toString());// 报文发送的次数
					javaRunPyton.executeSend(list);
					list = null;
					
				}
			});
		} catch (InvocationTargetException e) {
			
			e.printStackTrace();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
	}

	/**
	 * 删除sdcard下的图片
	 */
	public void delSdcardImage() {
		String str = Tools.getAdb(frameEntiey.getDeviceID()) + " shell rm -rf /sdcard/*.jpg";
		loger.debug(str);
		String result = RuntimeUtil.execForStr(str);
		loger.debug(result);
	}
}
