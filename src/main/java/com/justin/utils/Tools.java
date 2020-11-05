package com.justin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.justin.entity.ExcelEntiey;
import com.justin.entity.FrameEntiey;

import cn.hutool.core.convert.Convert;
import cn.hutool.poi.excel.ExcelReader;

/**
 * 
 * @author Administrator
 * 
 */
public class Tools {
	// 为选择框初始化一个默认的打开路径
	private static String lastChooseFileDirectory = getDirectoryName();

	// 获取表格中的数据并映射为ExcelEntiey
	public static List<ExcelEntiey> getExcelEntiey(String path) throws Exception{		
		
			ExcelReader reader = new ExcelReader(new File(path), 0);
			List<ExcelEntiey> list = reader.read(0, 1, ExcelEntiey.class);
			reader.close();
			return list;
		
	}

	// 获取当前所有设备
	public static HashSet<String> getDevices() {
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec("cmd /c adb devices");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Set<String> set = new HashSet<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				if (line.endsWith("device")) {
					String[] str = line.split("device");
					for (int i = 0; i < str.length; i++) {
						set.add(str[0].toString());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		process.destroy();
		return (HashSet<String>) set;
	}

	/**
	 * 下划线的时间格式
	 * 
	 * @return
	 */
	public static String getDateTime() {
		LocalDateTime date1 = LocalDateTime.now();
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH_mm_ss");
		String date2Str = formatter2.format(date1);
		return date2Str;
	}

	/**
	 * 标准时间格式
	 * 
	 * @return
	 */
	public static String getDateTime1() {
		LocalDateTime date1 = LocalDateTime.now();
		DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String date2Str = formatter2.format(date1);
		return date2Str;
	}

	public static void println(Object object) {
		// 注意此处System.out不能删除 否则将会触发递归调用
		System.out.println(getDateTime1() + ":" + object);
	}

	/**
	 * 调出文件选择框 并返回选择的文件路径
	 * 
	 * @return
	 */
	public static String getFilePath() {
		JFileChooser jf = new JFileChooser();
		jf.setVisible(true);
		jf.setDialogTitle("选择文件");
		int result = jf.showOpenDialog(null);
		if (result == JFileChooser.CANCEL_OPTION) {
			return null;
		} else {
			String path = jf.getSelectedFile().getAbsolutePath();
			lastChooseFileDirectory = path;
			return path;
		}
	}

	/**
	 * 输入框 并返回用户输入的内容
	 * 
	 * @param title 标题
	 * @return String
	 */
	public String getJOptionPaneShowInputDialog(String title) {
		String context = JOptionPane.showInputDialog(null, title, "提示", JOptionPane.PLAIN_MESSAGE);
		if (context.equals("") || context == null) {
			Tools.println("context:" + "空");
			return null;
		} else {
			Tools.println("context:" + context);
			return context;
		}
	}

	

	/**
	 * 选择框
	 * 
	 * @param title   标题
	 * @param options 数组
	 * @return 选择的数组下标值int
	 */
	public int getJOptionPaneShowOptionDialog(String title, Object[] options) {
		return JOptionPane.showOptionDialog(null, title, "提示", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[0]);
	}

	/**
	 * 返回选择的路径（此操作为保存时使用）
	 * 
	 * @return
	 */
	public static String getFilePathOfSave() {
		JFileChooser jf = new JFileChooser(lastChooseFileDirectory);
		// 只能选择文件夹
		jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jf.setDialogType(JFileChooser.SAVE_DIALOG);// 设置保存对话框
		int result = jf.showOpenDialog(null);
		if (result == JFileChooser.CANCEL_OPTION) {
			return null;
		} else {
			String path = jf.getSelectedFile().getAbsolutePath();
			lastChooseFileDirectory = path;
			println(path);
			return path;
		}
	}

	/**
	 * 返回adb -s deviceId
	 * 
	 * @return
	 */
	public static String getAdb(String deviceId) {
		return "adb -s  " + deviceId + " ";
	}

	/**
	 * 将当前的设置导出到本地文件中
	 */
	public static void writeConf(FrameEntiey entiey) {		
		File perentPath = new File("");
		String filePath = perentPath.getAbsolutePath()+"\\"+"conf.properties";		
		OutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		Properties properties = new Properties();
		properties.setProperty("deviceID", Convert.toStr(entiey.getDeviceID(), "请选择"));
		properties.setProperty("baudRate", Convert.toStr(entiey.getBaudRate(), "125K"));
		properties.setProperty("canChannelIndex", Convert.toStr(entiey.getCanChannelIndex(), "0"));
		properties.setProperty("canIndex", Convert.toStr(entiey.getCanIndex(), "0"));
		properties.setProperty("casePath", Convert.toStr(entiey.getCasePath(), ""));
		properties.setProperty("modelPath", Convert.toStr(entiey.getModelPath(), ""));		
		try {
			properties.store(out, "");
		} catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	/**
	 * 读取上一次的配置信息
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	
	public static void readConf(FrameEntiey entiey)  {
		try {
			Properties properties = new Properties();
			File perentPath = new File("");
			String filePath = perentPath.getAbsolutePath()+"\\"+"conf.properties";
			File confFile = new File(filePath);
			if(confFile.exists()) {
				properties.load(new FileReader(filePath));
				entiey.setBaudRate(properties.getProperty("baudRate", "125K"));
				entiey.setDeviceID(properties.getProperty("deviceID", "请选择"));
				entiey.setCanChannelIndex(properties.getProperty("canChannelIndex", "0"));
				entiey.setCanIndex(properties.getProperty("canIndex", "0"));
				entiey.setCasePath(properties.getProperty("casePath", ""));
				entiey.setModelPath(properties.getProperty("modelPath", ""));
			}
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "文件解析异常");
		}
		
	}
	public static void scheduleTask(String text, final int ms) {
		// 换算成秒
		final int count = ms / 1000;
		final Object[] options = { "确定" + "(" + count + "s)" };
		final JOptionPane op = new JOptionPane(text, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				options);
		final JDialog dialog = op.createDialog("提示");
		// 创建一个新计时器
		Timer timer = new Timer();
		// X秒后执行该任务
		timer.schedule(new TimerTask() {
			public void run() {
				for (int i = count - 1; i >= 0; i--) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					options[0] = "确定" + "(" + i + "s)";
					// 更新UI
					op.updateUI();
				}
				dialog.setVisible(false);
				dialog.dispose();
			}
		}, 10);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setAlwaysOnTop(true);
		dialog.setModal(false);
		dialog.setVisible(true);
	}
	/**
	 * 获取当前目录名称
	 * @return
	 */
	public static String getDirectoryName() {
		File directoryName = new File("");
		
		return directoryName.getAbsolutePath();
	}
}
