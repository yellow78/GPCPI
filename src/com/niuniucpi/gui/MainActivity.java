package com.niuniucpi.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JWindow;

import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import com.akdeniz.googleplaycrawler.Utils;
import com.niuniucpi.CPIUtils;
import com.niuniucpi.woker.CPIWorker;
import com.niuniucpi.woker.CPIWorkerAdapter;
import com.niuniucpi.woker.IMEIGenerator;

public class MainActivity {
	private JFrame frmJavacpi;
	private JTextField txtAccount;
	private JTextField txtPassword;
	private JTextField txtAndroidId;	
	private JTextField txtPackageName;
	private JTextField txtKeyword;
	private JTextField txtCheckINKey;
	private JTextField txtKeyHeader;
	private JTextField txtKeyFooter;
	private JTextField txtManufacture;
	private JTextField txtProductName;
	private JTextField txtProductDevice;
	private JTextField txtBuildId;
	private JTextField txtBuildVersionIncremental;
	private JTextField txtAndroidVersion;
	private JTextField txtProductBoard;
	private JTextField txtProductModel;
	private JTextField txtCellSimOperator;
	private JTextField txtLocale;
	private JTextField txtTimeZone;
	private JTextField txtDelayStart;
	private JTextField txtDelayEnd;
	private JTextField txtSDKVersion;
	private JTextField txtCOMM;
	private JTextField txtIMEI;
	private JLabel lblResult;
	private JButton btnAutoCPI;
	private JButton btnClear;
	private JButton btnSaveDevice;
	private JButton btnReadDevice;
	private JButton btnImei;
	
	private List<JTextField> mFields;
	private List<JButton> mButtons;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainActivity window = new MainActivity();
					window.frmJavacpi.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainActivity() {
		initialize();
	}	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmJavacpi = new JFrame();
		frmJavacpi.addWindowListener(frmJavacpi_WindowListener);
		frmJavacpi.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 14));
		frmJavacpi.setTitle("JavaCPI");
		frmJavacpi.setResizable(false);
		frmJavacpi.setBounds(100, 100, 640, 678);
		frmJavacpi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmJavacpi.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("GMail:");
		lblNewLabel.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblNewLabel.setBounds(12, 13, 134, 16);
		frmJavacpi.getContentPane().add(lblNewLabel);
		
		txtAccount = new JTextField();
		txtAccount.setBounds(156, 10, 466, 22);
		frmJavacpi.getContentPane().add(txtAccount);
		txtAccount.setColumns(10);
		
		txtPassword = new JTextField();
		txtPassword.setColumns(10);
		txtPassword.setBounds(156, 40, 466, 22);
		frmJavacpi.getContentPane().add(txtPassword);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblPassword.setBounds(12, 43, 134, 16);
		frmJavacpi.getContentPane().add(lblPassword);
		
		JLabel lblAndroidid = new JLabel("AndroidId:");
		lblAndroidid.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblAndroidid.setBounds(12, 73, 134, 16);
		frmJavacpi.getContentPane().add(lblAndroidid);
		
		txtAndroidId = new JTextField();
		txtAndroidId.setColumns(10);
		txtAndroidId.setBounds(156, 70, 466, 22);
		frmJavacpi.getContentPane().add(txtAndroidId);
		
		btnAutoCPI = new JButton("AutoCPI");
		btnAutoCPI.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		btnAutoCPI.setBounds(523, 616, 99, 25);
		frmJavacpi.getContentPane().add(btnAutoCPI);
		
		JLabel lblPackageName = new JLabel("Package:");
		lblPackageName.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblPackageName.setBounds(12, 103, 134, 16);
		frmJavacpi.getContentPane().add(lblPackageName);
		
		txtPackageName = new JTextField();
		txtPackageName.setColumns(10);
		txtPackageName.setBounds(156, 100, 466, 22);
		frmJavacpi.getContentPane().add(txtPackageName);
		
		txtKeyword = new JTextField();
		txtKeyword.setColumns(10);
		txtKeyword.setBounds(156, 130, 466, 22);
		frmJavacpi.getContentPane().add(txtKeyword);
		
		JLabel lblKeyword = new JLabel("Keyword:");
		lblKeyword.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblKeyword.setBounds(12, 133, 134, 16);
		frmJavacpi.getContentPane().add(lblKeyword);
		
		JLabel lblEnckey = new JLabel("CheckINKey:");
		lblEnckey.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblEnckey.setBounds(12, 163, 134, 16);
		frmJavacpi.getContentPane().add(lblEnckey);
		
		txtCheckINKey = new JTextField();
		txtCheckINKey.setColumns(10);
		txtCheckINKey.setBounds(156, 160, 466, 22);
		frmJavacpi.getContentPane().add(txtCheckINKey);
		
		btnClear = new JButton("Clear");
		btnClear.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		btnClear.setBounds(412, 616, 99, 25);
		frmJavacpi.getContentPane().add(btnClear);
		
		lblResult = new JLabel("Result");
		lblResult.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblResult.setBounds(12, 620, 263, 16);
		frmJavacpi.getContentPane().add(lblResult);
		
		JLabel lblDelay = new JLabel("Delay:");
		lblDelay.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblDelay.setBounds(12, 590, 70, 16);
		frmJavacpi.getContentPane().add(lblDelay);
		
		txtDelayStart = new JTextField();
		txtDelayStart.setText("30");
		txtDelayStart.setColumns(10);
		txtDelayStart.setBounds(94, 587, 75, 22);
		frmJavacpi.getContentPane().add(txtDelayStart);
		
		JLabel label_1 = new JLabel("\uFF5E");
		label_1.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		label_1.setBounds(181, 590, 19, 16);
		frmJavacpi.getContentPane().add(label_1);
		
		txtDelayEnd = new JTextField();
		txtDelayEnd.setText("40");
		txtDelayEnd.setColumns(10);
		txtDelayEnd.setBounds(201, 587, 75, 22);
		frmJavacpi.getContentPane().add(txtDelayEnd);
		
		JLabel lblSeconds = new JLabel("seconds");
		lblSeconds.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblSeconds.setBounds(288, 590, 70, 16);
		frmJavacpi.getContentPane().add(lblSeconds);
		
		JLabel lblRoproductbrand = new JLabel("ro.product.brand:");
		lblRoproductbrand.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblRoproductbrand.setBounds(12, 256, 134, 16);
		frmJavacpi.getContentPane().add(lblRoproductbrand);
		
		txtManufacture = new JTextField();
		txtManufacture.setText("samsung");
		txtManufacture.setColumns(10);
		txtManufacture.setBounds(156, 253, 161, 22);
		frmJavacpi.getContentPane().add(txtManufacture);
		
		JLabel lblRoproductname = new JLabel("ro.product.name:");
		lblRoproductname.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblRoproductname.setBounds(12, 286, 134, 16);
		frmJavacpi.getContentPane().add(lblRoproductname);
		
		txtProductName = new JTextField();
		txtProductName.setText("m0xx");
		txtProductName.setColumns(10);
		txtProductName.setBounds(156, 283, 161, 22);
		frmJavacpi.getContentPane().add(txtProductName);
		
		JLabel lblRoproductdevice = new JLabel("ro.product.device:");
		lblRoproductdevice.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblRoproductdevice.setBounds(12, 316, 134, 16);
		frmJavacpi.getContentPane().add(lblRoproductdevice);
		
		txtProductDevice = new JTextField();
		txtProductDevice.setText("m0");
		txtProductDevice.setColumns(10);
		txtProductDevice.setBounds(156, 313, 161, 22);
		frmJavacpi.getContentPane().add(txtProductDevice);
		
		JLabel lblRoproductbrand_1 = new JLabel("ro.build.id");
		lblRoproductbrand_1.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblRoproductbrand_1.setBounds(12, 346, 134, 16);
		frmJavacpi.getContentPane().add(lblRoproductbrand_1);
		
		txtBuildId = new JTextField();
		txtBuildId.setText("IMM76D");
		txtBuildId.setColumns(10);
		txtBuildId.setBounds(156, 343, 161, 22);
		frmJavacpi.getContentPane().add(txtBuildId);
		
		txtBuildVersionIncremental = new JTextField();
		txtBuildVersionIncremental.setText("I9300XXALF2");
		txtBuildVersionIncremental.setColumns(10);
		txtBuildVersionIncremental.setBounds(156, 373, 161, 22);
		frmJavacpi.getContentPane().add(txtBuildVersionIncremental);
		
		JLabel lblRobuildversionincremental = new JLabel("ro.build.version.incremental:");
		lblRobuildversionincremental.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblRobuildversionincremental.setBounds(12, 376, 134, 16);
		frmJavacpi.getContentPane().add(lblRobuildversionincremental);
		
		JLabel lblAndroidVer = new JLabel("android version:");
		lblAndroidVer.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblAndroidVer.setBounds(12, 405, 134, 16);
		frmJavacpi.getContentPane().add(lblAndroidVer);
		
		txtAndroidVersion = new JTextField();
		txtAndroidVersion.setText("4.0.4");
		txtAndroidVersion.setColumns(10);
		txtAndroidVersion.setBounds(156, 402, 161, 22);
		frmJavacpi.getContentPane().add(txtAndroidVersion);
		
		txtProductBoard = new JTextField();
		txtProductBoard.setText("smdk4x12");
		txtProductBoard.setColumns(10);
		txtProductBoard.setBounds(156, 432, 161, 22);
		frmJavacpi.getContentPane().add(txtProductBoard);
		
		JLabel lblRoproductboard = new JLabel("ro.product.board:");
		lblRoproductboard.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblRoproductboard.setBounds(12, 435, 134, 16);
		frmJavacpi.getContentPane().add(lblRoproductboard);
		
		JLabel lblRoproductmodel = new JLabel("ro.product.model:");
		lblRoproductmodel.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblRoproductmodel.setBounds(12, 465, 134, 16);
		frmJavacpi.getContentPane().add(lblRoproductmodel);
		
		txtProductModel = new JTextField();
		txtProductModel.setText("GT-I9300");
		txtProductModel.setColumns(10);
		txtProductModel.setBounds(156, 462, 161, 22);
		frmJavacpi.getContentPane().add(txtProductModel);
		
		JLabel lblCellsimoperator = new JLabel("CellSimOperator:");
		lblCellsimoperator.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblCellsimoperator.setBounds(12, 492, 134, 16);
		frmJavacpi.getContentPane().add(lblCellsimoperator);
		
		txtCellSimOperator = new JTextField();
		txtCellSimOperator.setText("310260");
		txtCellSimOperator.setColumns(10);
		txtCellSimOperator.setBounds(156, 492, 161, 22);
		frmJavacpi.getContentPane().add(txtCellSimOperator);
		
		JLabel lblLocale = new JLabel("Locale:");
		lblLocale.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblLocale.setBounds(12, 519, 134, 16);
		frmJavacpi.getContentPane().add(lblLocale);
		
		txtLocale = new JTextField();
		txtLocale.setText("zh_TW");
		txtLocale.setColumns(10);
		txtLocale.setBounds(156, 519, 161, 22);
		frmJavacpi.getContentPane().add(txtLocale);
		
		JLabel lblTimeZone = new JLabel("Time Zone:");
		lblTimeZone.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblTimeZone.setBounds(12, 546, 134, 16);
		frmJavacpi.getContentPane().add(lblTimeZone);
		
		txtTimeZone = new JTextField();
		txtTimeZone.setText("Asia/Taipei");
		txtTimeZone.setColumns(10);
		txtTimeZone.setBounds(156, 546, 161, 22);
		frmJavacpi.getContentPane().add(txtTimeZone);
		
		JLabel lblKeyheader = new JLabel("KeyHeader:");
		lblKeyheader.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblKeyheader.setBounds(12, 193, 134, 16);
		frmJavacpi.getContentPane().add(lblKeyheader);
		
		txtKeyHeader = new JTextField();
		txtKeyHeader.setColumns(10);
		txtKeyHeader.setBounds(156, 190, 466, 22);
		frmJavacpi.getContentPane().add(txtKeyHeader);
		
		txtKeyFooter = new JTextField();
		txtKeyFooter.setColumns(10);
		txtKeyFooter.setBounds(156, 220, 466, 22);
		frmJavacpi.getContentPane().add(txtKeyFooter);
		
		JLabel lblKeyfooter = new JLabel("KeyFooter:");
		lblKeyfooter.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblKeyfooter.setBounds(12, 223, 134, 16);
		frmJavacpi.getContentPane().add(lblKeyfooter);
		
		btnSaveDevice = new JButton("Save Device");
		btnSaveDevice.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		btnSaveDevice.setBounds(368, 586, 124, 25);
		frmJavacpi.getContentPane().add(btnSaveDevice);
		
		JLabel lblSdkVersion = new JLabel("sdk version:");
		lblSdkVersion.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		lblSdkVersion.setBounds(329, 253, 134, 16);
		frmJavacpi.getContentPane().add(lblSdkVersion);
		
		txtSDKVersion = new JTextField();
		txtSDKVersion.setText("15");
		txtSDKVersion.setColumns(10);
		txtSDKVersion.setBounds(461, 256, 161, 22);
		frmJavacpi.getContentPane().add(txtSDKVersion);
		
		JLabel label = new JLabel("\u96FB\u4FE1\u696D\u8005:");
		label.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		label.setBounds(327, 283, 134, 16);
		frmJavacpi.getContentPane().add(label);
		
		txtCOMM = new JTextField();
		txtCOMM.setText("46689");
		txtCOMM.setColumns(10);
		txtCOMM.setBounds(461, 284, 161, 22);
		frmJavacpi.getContentPane().add(txtCOMM);
		
		btnReadDevice = new JButton("Read Device");
		btnReadDevice.setFont(new Font("Microsoft JhengHei", Font.TRUETYPE_FONT, 12));
		btnReadDevice.setBounds(500, 586, 124, 25);
		frmJavacpi.getContentPane().add(btnReadDevice);
		
		JLabel lblImei = new JLabel("IMEI:");
		lblImei.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
		lblImei.setBounds(327, 316, 134, 16);
		frmJavacpi.getContentPane().add(lblImei);
		
		txtIMEI = new JTextField();
		txtIMEI.setText("");
		txtIMEI.setColumns(10);
		txtIMEI.setBounds(461, 317, 161, 22);
		frmJavacpi.getContentPane().add(txtIMEI);
		
		btnImei = new JButton("\u7522\u751FIMEI");
		btnImei.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
		btnImei.setBounds(498, 344, 124, 25);
		frmJavacpi.getContentPane().add(btnImei);
		
		btnAutoCPI.addActionListener(btnAutoCPI_Click);
		btnClear.addActionListener(btnClear_Click);
		btnSaveDevice.addActionListener(btnSaveDevice_Click);
		btnReadDevice.addActionListener(btnReadDevice_Click);
		btnImei.addActionListener(btnImei_Click);
	}
	
	// 表單事件
	private WindowAdapter frmJavacpi_WindowListener = new WindowAdapter() {
		@Override
		public void windowOpened(WindowEvent e) {
			MainActivity.this.mFields = new ArrayList<JTextField>();
			MainActivity.this.mButtons = new ArrayList<JButton>();
			
			mFields.add(txtAccount);
			mFields.add(txtPassword);
			mFields.add(txtAndroidId);
			mFields.add(txtPackageName);
			mFields.add(txtKeyword);
			mFields.add(txtCheckINKey);
			mFields.add(txtKeyHeader);
			mFields.add(txtKeyFooter);
			mFields.add(txtManufacture);
			mFields.add(txtProductName);
			mFields.add(txtProductDevice);
			mFields.add(txtBuildId);
			mFields.add(txtBuildVersionIncremental);
			mFields.add(txtAndroidVersion);
			mFields.add(txtProductBoard);
			mFields.add(txtProductModel);
			mFields.add(txtCellSimOperator);
			mFields.add(txtLocale);
			mFields.add(txtTimeZone);
			mFields.add(txtDelayStart);
			mFields.add(txtDelayEnd);			
			mFields.add(txtSDKVersion);
			mFields.add(txtCOMM);
			mFields.add(txtIMEI);
			mButtons.add(btnAutoCPI);
			mButtons.add(btnClear);
			mButtons.add(btnSaveDevice);
			mButtons.add(btnReadDevice);
			mButtons.add(btnImei);
			
			txtAccount.setText("");
			txtPassword.setText("");
			txtPackageName.setText("com.niuniu.is2048");
			txtKeyword.setText("com.niuniu.is2048");
		}
		@Override
		public void windowClosing(WindowEvent e) {
		}
	};
	
	// 產生imei按鈕click事件
	private ActionListener btnImei_Click = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			txtIMEI.setText(IMEIGenerator.getIMEI());
		}
	};
	
	// ReadDevice按鈕click事件
	private ActionListener btnReadDevice_Click = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			int ret = jfc.showOpenDialog(frmJavacpi);
			if (ret == JFileChooser.APPROVE_OPTION) {
				// read from file
				try {
				    BufferedReader reader = new BufferedReader(new FileReader(jfc.getSelectedFile()));
				    String buffer = null;
				    while ((buffer = reader.readLine()) != null) {
				    	int index = buffer.indexOf("=");
				    	if (index >= 0) {
				    		String key = buffer.substring(0,  index);
				    		String value = buffer.substring(index + 1, buffer.length());
				    		switch (key) {
				    		case "Manufacture":
				    			txtManufacture.setText(value);
				    			break;
				    		case "ProductName":
				    			txtProductName.setText(value);
				    			break;
				    		case "ProductDevice":
				    			txtProductDevice.setText(value);
				    			break;
				    		case "BuildId":
				    			txtBuildId.setText(value);
				    			break;
				    		case "BuildVersionIncremental":
				    			txtBuildVersionIncremental.setText(value);
				    			break;
				    		case "AndroidVersion":
				    			txtAndroidVersion.setText(value);
				    			break;
				    		case "ProductBoard":
				    			txtProductBoard.setText(value);
				    			break;
				    		case "ProductModel":
				    			txtProductModel.setText(value);
				    			break;
				    		case "CellSimOperator":
				    			txtCellSimOperator.setText(value);
				    			break;
				    		case "Locale":
				    			txtLocale.setText(value);
				    			break;
				    		case "TimeZone":
				    			txtTimeZone.setText(value);
				    			break;
				    		case "SDKVersion":
				    			txtSDKVersion.setText(value);
				    			break;
				    		case "COMM":
				    			txtCOMM.setText(value);
				    			break;				    			
				    		}
				    	}
				    }
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null,  "讀取檔案失敗, " + ex.getMessage());	
				}
			}
		}
	};
	
	// SaveDevice按鈕click事件
	private ActionListener btnSaveDevice_Click = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String fileName = "";
			try {
				fileName = JOptionPane.showInputDialog("輸入檔名");
			} catch (Exception e) {
				fileName = "";
			}
			if (!fileName.isEmpty()) {
				String newline = System.getProperty("line.separator");
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
					writer.write(String.format("Manufacture=%s%s", txtManufacture.getText(), newline));
					writer.write(String.format("ProductName=%s%s", txtProductName.getText(), newline));
					writer.write(String.format("ProductDevice=%s%s", txtProductDevice.getText(), newline));
					writer.write(String.format("BuildId=%s%s", txtBuildId.getText(), newline));
					writer.write(String.format("BuildVersionIncremental=%s%s", txtBuildVersionIncremental.getText(), newline));
					writer.write(String.format("AndroidVersion=%s%s", txtAndroidVersion.getText(), newline));
					writer.write(String.format("ProductBoard=%s%s", txtProductBoard.getText(), newline));
					writer.write(String.format("ProductModel=%s%s", txtProductModel.getText(), newline));
					writer.write(String.format("CellSimOperator=%s%s", txtCellSimOperator.getText(), newline));
					writer.write(String.format("Locale=%s%s", txtLocale.getText(), newline));
					writer.write(String.format("TimeZone=%s%s", txtTimeZone.getText(), newline));
					writer.write(String.format("SDKVersion=%s%s", txtSDKVersion.getText(), newline));
					writer.write(String.format("COMM=%s%s", txtCOMM.getText(), newline));
					writer.flush();
					writer.close();
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null,  "寫入檔案失敗, " + ex.getMessage());	
				}	
			}			
		}
	};
		
	// clearn按鈕click事件
	private ActionListener btnClear_Click = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			txtAccount.setText("");
			txtPassword.setText("");
			txtAndroidId.setText("");
			txtCheckINKey.setText("");
			txtKeyHeader.setText("");
			txtKeyFooter.setText("");
			lblResult.setText("");
		}
		
	};
	
	// 登入按鈕click事件
	private ActionListener btnAutoCPI_Click = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			lblResult.setText("開始進行CPI");
			String account = txtAccount.getText();
			String password = txtPassword.getText();
			String androidId = txtAndroidId.getText();
			String packageName = txtPackageName.getText();
			String keyWord = txtKeyword.getText();
			String checkinKey = txtCheckINKey.getText();
			if (account.isEmpty()) {
				JOptionPane.showMessageDialog(null,  "必需輸入帳號");
				txtAccount.grabFocus();
				return;
			}
			if (password.isEmpty()) {
				JOptionPane.showMessageDialog(null,  "必需輸入密碼");
				txtPassword.grabFocus();
				return;
			}
			if (packageName.isEmpty()) {
				JOptionPane.showMessageDialog(null,  "必需輸入App PackageName");
				txtPackageName.grabFocus();
				return;
			}
			if (keyWord.isEmpty()) {
				JOptionPane.showMessageDialog(null,  "必需輸入搜尋關鍵字");
				txtKeyword.grabFocus();
				return;
			}
			setInterfaceEnabled(false);
			// 設定型號
			Utils.txtAndroidVersion = txtAndroidVersion.getText();
			Utils.txtBuildId = txtBuildId.getText();
			Utils.txtBuildVersionIncremental = txtBuildVersionIncremental.getText();
			Utils.txtCellSimOperator = txtCellSimOperator.getText();
			Utils.txtLocale = txtLocale.getText();
			if (Utils.txtLocale.indexOf("_") < 0) {
				JOptionPane.showMessageDialog(null,  "必需設定正確語系, 例如 zh_TW, 非 zh-TW");
				txtLocale.grabFocus();
				return;
			}
			Utils.txtManufacture = txtManufacture.getText();
			Utils.txtProductBoard = txtProductBoard.getText();
			Utils.txtProductDevice = txtProductDevice.getText();
			Utils.txtProductModel = txtProductModel.getText();
			Utils.txtProductName = txtProductName.getText();
			Utils.txtTimeZone = txtTimeZone.getText();
			Utils.txtSDKVersion = txtSDKVersion.getText();
			Utils.txtCOMM = txtCOMM.getText();
			Utils.txtIMEI = txtIMEI.getText();
			CPIWorker worker =  new CPIWorker(workerAdapter, account, password, androidId, packageName, keyWord, Integer.parseInt(txtDelayStart.getText()), Integer.parseInt(txtDelayEnd.getText()));
			worker.doCPI();
		}
	
	};
	
	private void setInterfaceEnabled(boolean enabled) {
		for (int i = 0; i < mFields.size(); i++)
			mFields.get(i).setEnabled(enabled);
		for (int i = 0; i < mButtons.size(); i++)
			mButtons.get(i).setEnabled(enabled);
	}
	
	private CPIWorkerAdapter workerAdapter = new CPIWorkerAdapter() {

		@Override
		public void LoginSucceed(CPIWorker sender, GooglePlayAPI service) {
			lblResult.setText("登入成功");
			if (txtAndroidId.getText().isEmpty()) txtAndroidId.setText(service.getAndroidID());
			if (txtCheckINKey.getText().isEmpty()) txtCheckINKey.setText("0x" + CPIUtils.getHexString(service.getCheckINKey()));
			if (txtKeyHeader.getText().isEmpty()) txtKeyHeader.setText("0x" + CPIUtils.getHexString(service.getCheckINHeader()));
			if (txtKeyFooter.getText().isEmpty()) txtKeyFooter.setText("0x" + CPIUtils.getHexString(service.getCheckINFooter()));
		}

		@Override
		public void LoginFailed(CPIWorker sender, GooglePlayAPI service, Exception e) {
			lblResult.setText("登入失敗");
			//JOptionPane.showMessageDialog(null,  "登入失敗");
			setInterfaceEnabled(true);
		}

		@Override
		public void DownloadCompleted(CPIWorker sender) {
			lblResult.setText("執行成功");
			setInterfaceEnabled(true);
		}

		@Override
		public void DownloadFailed(CPIWorker sender) {
			//JOptionPane.showMessageDialog(null,  "下載失敗");
			lblResult.setText("下載失敗");
			setInterfaceEnabled(true);
		}

		@Override
		public void StartDownload() {
			lblResult.setText("開始下載");
		}

		@Override
		public void DisplayMessage(String message) {
			lblResult.setText("CPI訊息: " + message);
		}
		
	};								
}
