package com.justin.mavenTest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.apache.log4j.Logger;
import com.justin.entity.FrameEntiey;
import com.justin.service.ManageService;
import com.justin.utils.MPrintStream;
import com.justin.utils.ThreadPool;
import com.justin.utils.Tools;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.swing.ClipboardUtil;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.Font;
import javax.swing.ScrollPaneConstants;

@SuppressWarnings("serial")
public class FiveDivisionTest extends JFrame implements Observer {
	private static Logger loger = Logger.getLogger(FiveDivisionTest.class);
	private JPanel contentPane;
	private String deviceId;
	private JLabel messageLabel, labelPassNum, labelFailNum;
	private JComboBox<String> comboBoxGetDeviceID, comboBoxCANIndex;
	private static JTextArea textArea;
	private ManageService manageService;
	private static MPrintStream mPrintStream;
	private JTextField chooseExcelPathTextField;
	private JComboBox<String> comboBoxBaudRate;
	private FrameEntiey frameEntiey;
	private JTextField textFieldModelPath;
	private JButton buttonStart, buttonEnd;
	private JComboBox<String> comboBoxCANChannelIndex;
	private JTextField textFieldTestNum;
	//添加一条注释and

	/**
	 * Launch the application.
	 */
	static {
		loger.debug("程序正在启动");
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName();

		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		// 调用OPENCV一定要加载此库
		// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		/**
		 * OPENCV依赖的DLL文件，未在环境变量中配置，会报错
		 */
		System.loadLibrary("opencv_java2413");

	}

	public static void main(String[] args) throws InvocationTargetException, InterruptedException {

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				try {
					FiveDivisionTest frame = new FiveDivisionTest();
					frame.setTitle(Tools.getDirectoryName());
					
					mPrintStream = new MPrintStream(System.out, textArea);
					System.setOut(mPrintStream);
					// 调用new WindowAutoHide构造方法可以使程序贴边自动隐藏
					// new WindowAutoHide(frame);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 */
	public FiveDivisionTest() throws IOException {
		frameEntiey = new FrameEntiey();
		manageService = new ManageService(frameEntiey);
		// 让当前类监听manageSerice的通知 并更新测试数据
		manageService.addObserver(this);
		deviceId = "请选择";
		// 禁用最大化
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1184, 773);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(2, 2, 2, 2));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		// 支持拖放功能 暂未开发
		dropFile();
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.setForeground(Color.BLACK);

		tabbedPane.addTab("五部自动化", null, panel, null);
		panel.setLayout(null);

		JLabel lblsn = new JLabel("选择设备ID:");
		lblsn.setBounds(10, 25, 73, 15);
		panel.add(lblsn);

		comboBoxGetDeviceID = new JComboBox<String>();
		comboBoxGetDeviceID.setBounds(10, 50, 152, 21);
		manageService.addItem(comboBoxGetDeviceID);
		frameEntiey.setDeviceID("请选择");
		comboBoxGetDeviceID.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// 监听设备选择
				if (e.getStateChange() == ItemEvent.SELECTED) {
					loger.debug("设备已变化");
					deviceId = e.getItem().toString();
					frameEntiey.setDeviceID(deviceId);
				}
			}
		});
		panel.add(comboBoxGetDeviceID);

		JButton buttonRefreDevice = new JButton("刷新设备");
		buttonRefreDevice.setBounds(172, 49, 92, 23);
		buttonRefreDevice.setToolTipText("添加新设备后，点此按钮");
		buttonRefreDevice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 监听设备刷新
				manageService.addItem(comboBoxGetDeviceID);
			}
		});
		panel.add(buttonRefreDevice);

		JButton buttonCopyID = new JButton("复制ID");
		buttonCopyID.setBounds(274, 49, 97, 23);
		buttonCopyID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClipboardUtil.setStr(deviceId);
				JOptionPane.showMessageDialog(null, "复制成功");
			}
		});
		panel.add(buttonCopyID);

		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_1.setBounds(10, 81, 870, 609);
		panel.add(tabbedPane_1);

		JPanel j1 = new JPanel();
		tabbedPane_1.addTab("项目", null, j1, null);
		tabbedPane_1.setEnabledAt(0, true);
		j1.setLayout(null);
		buttonStart = new JButton("开始");
		buttonStart.setBackground(new Color(102, 102, 102));
		buttonStart.setActionCommand("buttonStart");
		buttonStart.addActionListener(actionListener);
		
		buttonStart.setBounds(587, 6, 93, 23);
		j1.add(buttonStart);

		JLabel labelChooserCaseFile = new JLabel("选择用例文件:");
		labelChooserCaseFile.setBounds(10, 10, 93, 15);
		j1.add(labelChooserCaseFile);

		chooseExcelPathTextField = new JTextField();
		chooseExcelPathTextField.setBounds(94, 7, 380, 21);
		j1.add(chooseExcelPathTextField);
		chooseExcelPathTextField.setColumns(10);
		chooseExcelPathTextField.setEnabled(false);

		JButton buttonChooseExcel = new JButton("选择文件");
		buttonChooseExcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String path = Tools.getFilePath();
				if(null!=path) {
					chooseExcelPathTextField.setText(path);
					frameEntiey.setCasePath(path);
				}
				
			}
		});
		buttonChooseExcel.setBounds(484, 6, 93, 23);
		j1.add(buttonChooseExcel);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(0, 75, 855, 505);
		j1.add(panel_1);
		panel_1.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 10, 835, 495);
		scrollPane.setPreferredSize(new Dimension(835, 495));
		panel_1.add(scrollPane);
		textArea = new JTextArea();
		textArea.setForeground(new Color(0, 0, 0));
		textArea.setFont(new Font("宋体", Font.PLAIN, 15));
		scrollPane.setViewportView(textArea);
		scrollPane.setAutoscrolls(true);
		textArea.setAutoscrolls(true);
		messageLabel = new JLabel("日志");
		messageLabel.setBounds(10, 50, 258, 15);
		j1.add(messageLabel);

		buttonEnd = new JButton("结束");
		buttonEnd.setEnabled(false);
		buttonEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// 点击结束按钮后 将自身按钮重置为不可点击状态
				buttonEnd.setEnabled(false);
				// 将运行状态重置为false 程序会执行当当条命令后停止
				frameEntiey.setRun(false);
			}
		});
		buttonEnd.setBounds(690, 6, 93, 23);
		j1.add(buttonEnd);

		comboBoxBaudRate = new JComboBox<String>();
		comboBoxBaudRate.setBounds(381, 50, 107, 21);
		panel.add(comboBoxBaudRate);
		comboBoxBaudRate.addItem("125K");
		comboBoxBaudRate.addItem("500K");
		// 默认波特率显示125k
		comboBoxBaudRate.setSelectedIndex(0);
		// 波特率
		frameEntiey.setBaudRate(comboBoxBaudRate.getSelectedItem().toString());
		Tools.println("默认选择：" + comboBoxBaudRate.getSelectedItem().toString());
		comboBoxBaudRate.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {

				if (e.getStateChange() == ItemEvent.SELECTED) {
					// 监听波特率变化
					loger.debug("波特率已变化");
					frameEntiey.setBaudRate(e.getItem().toString());
				}
			}
		});

		JLabel label = new JLabel("波特率:");
		label.setBounds(381, 25, 54, 15);
		panel.add(label);

		JLabel lblCan = new JLabel("CAN设备号:");
		lblCan.setBounds(498, 25, 65, 15);
		panel.add(lblCan);

		JLabel label_2 = new JLabel("通道号:");
		label_2.setBounds(595, 25, 54, 15);
		panel.add(label_2);

		comboBoxCANIndex = new JComboBox<String>();
		comboBoxCANIndex.addItem("0");
		comboBoxCANIndex.addItem("1");
		comboBoxCANIndex.addItem("2");
		comboBoxCANIndex.addItem("3");
		comboBoxCANIndex.addItem("4");
		comboBoxCANIndex.addItem("5");
		// 将CAN0设置为默认值
		comboBoxCANIndex.setSelectedIndex(0);
		// CAN盒下标
		frameEntiey.setCanIndex(comboBoxCANIndex.getSelectedItem().toString());
		comboBoxCANIndex.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String index = e.getItem().toString();
					loger.debug("CAN盒下标已变化");
					frameEntiey.setCanIndex(index);
				}
			}
		});
		comboBoxCANIndex.setBounds(498, 50, 81, 21);
		panel.add(comboBoxCANIndex);

		comboBoxCANChannelIndex = new JComboBox<String>();
		comboBoxCANChannelIndex.addItem("0");
		comboBoxCANChannelIndex.addItem("1");
		// 将通道0设置为默认值
		comboBoxCANChannelIndex.setSelectedIndex(0);
		frameEntiey.setCanChannelIndex(comboBoxCANChannelIndex.getSelectedItem().toString());
		// 监听用户的更改操作并更新通道号
		comboBoxCANChannelIndex.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String index = e.getItem().toString();
					// 设备通道号
					loger.debug("CAN通道已变化");
					frameEntiey.setCanChannelIndex(index);
				}

			}
		});
		comboBoxCANChannelIndex.setBounds(595, 50, 81, 21);
		panel.add(comboBoxCANChannelIndex);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(
				new TitledBorder(null, "\u5DE5\u5177\u96C6", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(890, 396, 269, 294);
		panel.add(panel_2);
		panel_2.setLayout(null);

		JButton buttonShotScreenByUser = new JButton("截图");
		buttonShotScreenByUser.setBounds(137, 23, 93, 56);
		buttonShotScreenByUser.setActionCommand("buttonShotScreenByUser");
		buttonShotScreenByUser.addActionListener(actionListener);
		panel_2.add(buttonShotScreenByUser);

		JButton buttonLoadPreviouConf = new JButton("载入上次配置");
		buttonLoadPreviouConf.addActionListener(actionListener);
		buttonLoadPreviouConf.setActionCommand("buttonLoadPreviouConf");

		buttonLoadPreviouConf.setBounds(10, 23, 117, 23);
		panel_2.add(buttonLoadPreviouConf);

		JButton buttonSaveCurrentConf = new JButton("保存当前配置");
		buttonSaveCurrentConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Tools.writeConf(frameEntiey);
				JOptionPane.showMessageDialog(null, "配置文件已保存到项目根目录,conf.properties");
			}
		});
		buttonSaveCurrentConf.setBounds(10, 56, 117, 23);
		panel_2.add(buttonSaveCurrentConf);

		JButton btnOnlyTest = new JButton("Only Test");
		btnOnlyTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				manageService.delSdcardImage();
			}
		});
		btnOnlyTest.setBounds(10, 261, 93, 23);
		panel_2.add(btnOnlyTest);

		JButton buttonModeCreate = new JButton("模版制作");
		buttonModeCreate.addActionListener(actionListener);
		buttonModeCreate.setActionCommand("buttonModeCreate");

		buttonModeCreate.setBounds(10, 91, 117, 23);
		panel_2.add(buttonModeCreate);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(
				new TitledBorder(null, "\u53C2\u6570\u533A", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setBounds(890, 160, 269, 226);
		panel.add(panel_3);
		panel_3.setLayout(null);

		JLabel label_3 = new JLabel("图片模版路径:");
		label_3.setBounds(10, 27, 84, 15);
		panel_3.add(label_3);

		textFieldModelPath = new JTextField();
		textFieldModelPath.setEditable(false);
		textFieldModelPath.setBounds(10, 52, 177, 21);
		panel_3.add(textFieldModelPath);
		textFieldModelPath.setColumns(10);

		JButton buttonChoosePath = new JButton("选择");
		buttonChoosePath.setActionCommand("buttonChoosePath");
		buttonChoosePath.addActionListener(actionListener);
		buttonChoosePath.setBounds(197, 51, 62, 23);
		panel_3.add(buttonChoosePath);

		JLabel labelTestNum = new JLabel("测试次数:(为-1时,将持续测试)");
		labelTestNum.setBounds(10, 83, 249, 15);
		panel_3.add(labelTestNum);

		textFieldTestNum = new JTextField();
		textFieldTestNum.setText("-1");
		loger.debug("测试次数默认为-1");
		textFieldTestNum.setBounds(10, 107, 177, 21);
		panel_3.add(textFieldTestNum);
		textFieldTestNum.setColumns(10);

		JButton buttonInfinite = new JButton("持续");

		buttonInfinite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 为-1测持续测试
				textFieldTestNum.setText("-1");
				frameEntiey.setTestNum(-1);
			}
		});
		buttonInfinite.setBounds(197, 106, 62, 23);
		panel_3.add(buttonInfinite);

		JLabel labelPass = new JLabel("PASS");
		// 打开不透明开关
		labelPass.setOpaque(true);
		labelPass.setBackground(new Color(0, 250, 154));
		labelPass.setFont(new Font("宋体", Font.PLAIN, 20));
		labelPass.setHorizontalAlignment(SwingConstants.CENTER);
		labelPass.setBounds(890, 95, 54, 55);
		panel.add(labelPass);

		labelPassNum = new JLabel("0");
		labelPassNum.setHorizontalAlignment(SwingConstants.CENTER);
		labelPassNum.setOpaque(true);
		labelPassNum.setBackground(new Color(0, 250, 154));
		labelPassNum.setFont(new Font("宋体", Font.PLAIN, 15));
		labelPassNum.setBounds(954, 95, 54, 55);
		panel.add(labelPassNum);

		JLabel labelFail = new JLabel("FAIL");
		labelFail.setHorizontalAlignment(SwingConstants.CENTER);
		labelFail.setOpaque(true);
		labelFail.setFont(new Font("宋体", Font.PLAIN, 20));
		labelFail.setBackground(new Color(205, 92, 92));
		labelFail.setBounds(1018, 95, 54, 55);
		panel.add(labelFail);

		labelFailNum = new JLabel("0");
		labelFailNum.setBackground(new Color(205, 92, 92));
		labelFailNum.setFont(new Font("宋体", Font.PLAIN, 15));
		labelFailNum.setOpaque(true);
		labelFailNum.setHorizontalAlignment(SwingConstants.CENTER);
		labelFailNum.setBounds(1082, 95, 54, 55);
		panel.add(labelFailNum);

		setVisible(true);
	}

	public void dropFile() {
		new DropTarget(this, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				// 表示接受复制移动的操作
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				try {
					// 获取传输的数据
					Transferable data = dtde.getTransferable();
					// 获取数据支持的格式
					DataFlavor[] flavors = data.getTransferDataFlavors();
					for (DataFlavor fla : flavors) {
						if (fla.equals(DataFlavor.javaFileListFlavor)) {
							List<?> fileList = (List<?>) data.getTransferData(fla);
							int i = JOptionPane.showOptionDialog(FiveDivisionTest.this, "操作类型", "请选择",
									JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
									new String[] { "PUSH文件", "安装文件" }, "传输文件");
							Tools.println("操作的值:" + i);
							if (manageService.isCheckDevice()) {
								for (Object f : fileList) {
									final String path = f.toString();
									Tools.println("path:" + path);

								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void update(Observable o, Object arg) {
		String command = arg.toString();
		if(command.equals("updateTestNum")) {
			loger.debug("监听到次数已发生变化,更新UI");
			labelPassNum.setText(frameEntiey.getPassNum() + "");
			labelFailNum.setText(frameEntiey.getFailNum() + "");
		}
		if(command.equals("updateButtonStart")) {
			loger.debug("监听到按钮状态通知,更新开始按钮为可点击");
			buttonStart.setEnabled(true);
		}
		
	}
	

	private ActionListener actionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			//开始按钮
			if(command.equals("buttonStart")) {				
				ThreadPool.getCachedThreadPool().execute(new Runnable() {

					@Override
					public void run() {
						if (manageService.isCheckDevice()) {
							//将结束按钮状态更改为可点击状态
							buttonEnd.setEnabled(true);
							//程序运行后将开始按钮置灰 禁止再次点击
							buttonStart.setEnabled(false);
							// 开始测试执行 并将状态更改为true
							frameEntiey.setRun(true);
							// 获取当前用户指定的测试次数
							int testNum = Convert.toInt(textFieldTestNum.getText(), -1);
							loger.debug("文本框中的指定次数:" + testNum);
							frameEntiey.setTestNum(testNum);
							manageService.startTest();
						}

					}
				});
			}
			//加载上次保存的配置
			if(command.equals("buttonLoadPreviouConf")) {
				// 读取项目目录下的配置文件 并将属于重新赋值给frameEntiey
				Tools.readConf(frameEntiey);
				comboBoxGetDeviceID.setSelectedItem(frameEntiey.getDeviceID());
				comboBoxBaudRate.setSelectedItem(frameEntiey.getBaudRate());
				comboBoxCANIndex.setSelectedItem(frameEntiey.getCanIndex());
				comboBoxCANChannelIndex.setSelectedItem(frameEntiey.getCanChannelIndex());
				chooseExcelPathTextField.setText(frameEntiey.getCasePath());
				textFieldModelPath.setText(frameEntiey.getModelPath());
			}
			// 截图按钮
			if (command.equals("buttonShotScreenByUser")) {
				Tools.println("执行截图操作");
				if (manageService.isCheckDevice()) {
					manageService.shotScreenByUser();
					Tools.scheduleTask("截图完成", 2000);
				}
			}
			// 选择模版路径
			if (command.equals("buttonChoosePath")) {
				Tools.println("执行模板选择");
				String path = Tools.getFilePathOfSave();
				if(null!=path) {
					loger.debug("模板路径:" + path);
					textFieldModelPath.setText(path);
					// 将adb截图保存到用户指定目录下的tempImage中
					File file = new File(path, "tempImage");
					if (!file.exists()) {
						loger.debug("文件夹不存在，已新建完成");
						file.mkdirs();
					}
					frameEntiey.setModelPath(path);
				}
			}
			//模板制作
			if (command.equals("buttonModeCreate")) {
				Tools.println("执行模板制作");
				ThreadPool.getCachedThreadPool().execute(new Runnable() {

					@Override
					public void run() {
						if (manageService.isCheckDevice()) {
							// 开始测试执行 并将状态更改为true
							frameEntiey.setRun(true);
							frameEntiey.setTestNum(1);
							manageService.creatModel();
						}
							
					}
				});
			}

		}
	};
}
