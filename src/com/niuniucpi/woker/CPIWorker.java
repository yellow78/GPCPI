package com.niuniucpi.woker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import javax.crypto.NoSuchPaddingException;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.akdeniz.googleplaycrawler.DownloadData;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidCheckinResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DocV2;
import com.akdeniz.googleplaycrawler.GooglePlay.SearchResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.UploadDeviceConfigResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import com.akdeniz.googleplaycrawler.GooglePlayException;
import com.akdeniz.googleplaycrawler.Utils;
import com.niuniucpi.CPIUtils;

public class CPIWorker {

	private String mAccount;
	public String getAccount() {
		return mAccount;
	}
	
	private String mPassword;
	public String getPassword() {
		return mPassword;
	}
	
	private String mAndroidId;
	public String getAndroidId() {
		return mAndroidId;
	}
	
	private String mPackageName;
	public String getPackageName() {
		return mPackageName;
	}
	
	private String mKeyword;
	public String getKeyword() {
		return mKeyword;
	}
	
	private CPIWorkerAdapter mAdapter;
	public CPIWorkerAdapter getWorkerAdapter() {
		return mAdapter;
	}
	
	private byte[] mCheckINKey = null;
	public byte[] getCheckINKey() {
		return mCheckINKey;
	}
	
	private byte[] mCheckINHeader = null;
	public byte[] getCheckINHeader() {
		return mCheckINHeader;
	}
	
	private byte[] mCheckINFooter = null;
	public byte[] getCheckINFooter() {
		return mCheckINFooter;
	}
	
	private int mCurrentVersionCode = 0;
	public int getCurrentVersionCode() {
		return mCurrentVersionCode;
	}
	
	int delayStart;
	int delayEnd;
	
	private GooglePlayAPI service;
	
	private boolean bMonitorPacket = true;//porxy
	
	public CPIWorker(CPIWorkerAdapter adapter, String account, String password, String androidId, String packageName, String keyWord, int delaystart, int delayend) {
		mAdapter = adapter;
		mAccount = account;
		mPassword = password;
		mAndroidId = androidId;
		mPackageName = packageName;
		mKeyword = keyWord;
		delayStart = delaystart;
		delayEnd = delayend;
		GooglePlayAPI.setMonitorPacket(bMonitorPacket);
		service = new GooglePlayAPI(account, password, androidId);
		service.setLocalization(Locale.getDefault().getCountry());
	}
	
	public void doCPI() {		
		new Thread(new Runnable() {
			@Override
			public void run() {
				runCPI();
			}
		}).start();
	}
	
	private int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
	    // variable so that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	private void delay() {
		delay(delayStart, delayEnd);
	}
	
	private void delay(int delaystart, int delayend) {
		final long delaySeconds = randInt(delaystart, delayend);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mAdapter.DisplayMessage("©µ¿ð " + String.valueOf(delaySeconds) + " ¬í");
			}
		});		
		try {
			Thread.sleep(delaySeconds * 1000);
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}
	
	private UploadDeviceConfigResponse deviceResponse;
	private AndroidCheckinResponse checkinResponse;
	
	private Map<String, Object> uploadDeviceConfigAdditional;
	
	private String TOCResult = "";
	
	private void runCPI() {
		uploadDeviceConfigAdditional = new HashMap<String, Object>();
		try {
			if (service.getAndroidID().isEmpty()) {
				String androidID = service.CPI_checkin();
				mCheckINKey = service.getCheckINKey();
				mCheckINHeader = service.getCheckINHeader();
				mCheckINFooter = service.getCheckINFooter();
				service.CPI_AC2DM();
				service.login();
				service.gms_ac2dmlogin();
				service.googleapis_userinfologin();
				service.gms_accountidlogin();
				service.gms_AndroidCheckInServerlogin();
				service.googleapis_gcmlogin();
				service.cl_calendarlogin();
				service.gma_maillogin();
				service.CPI_lastcheckin();
				deviceResponse = service.uploadDeviceConfig(uploadDeviceConfigAdditional);
				service.gms_cookielogin();
				service.gms_cryptauthlogin();
				service.gms_reminderslogin();
				service.gms_ads_measurementlogin();
				service.gms_cplogin();
				service.gms_Multilogin();
				service.androidsecurelogin();
				
				//service.gms_reportinglogin();
				//service.gms_managerlogin();

			} else {
				service.login();
			}
		} catch (Exception e) {
			final Exception ex = e;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mAdapter.LoginFailed(CPIWorker.this, service, ex);
				}
			});
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mAdapter.LoginSucceed(CPIWorker.this, service);
			}
		});
		// ¨ú¥XAndroidId
		mAndroidId = service.getAndroidID();
		// ·j´MApp
		SearchResponse response = null;
		try {
			response = service.search(mKeyword, 0, 30);
		} catch (Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mAdapter.DownloadFailed(CPIWorker.this);
				}
			});
			return;
		}
		boolean bFound = false;
		if (response.getDocCount() > 0) {
			DocV2 apps = response.getDoc(0);			
			for (int i = 0; i < apps.getChildCount(); i++) {
				DocV2 app = apps.getChild(i);
				String packageName = app.getBackendDocid();
				if (packageName.equals(mPackageName)) {
					bFound = true;
					int vc = app.getDetails().getAppDetails().getVersionCode();
					int ot = app.getOffer(0).getOfferType();
					long totalBytes = app.getDetails().getAppDetails().getInstallationSize();
					boolean paid = app.getOffer(0).getCheckoutFlowRequired();			
					try {						
						TOCResult = service.getTOCResult();
						sendLog();
						mAdapter.StartDownload();
						download(packageName, vc, ot, paid);
					} catch (Exception e) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								mAdapter.DownloadFailed(CPIWorker.this);
							}
						});
						return;			
					}					
					break;
				}
			}
		}
		if (bFound) {
			/*
			byte[] bytes = new byte[] {
				0x09, (byte)0xEF, (byte)0xAD, (byte)0x80, 0x6F, (byte)0xE6, (byte)0x85, 0x0D, 0x31, 0x12, 0x17, 0x0A, 0x13, 0x63, 0x6F, 0x6D, 0x2E, 0x66, 0x72, 0x75, 0x69, 0x74, 0x74, 0x62, 0x2E, 0x61, 0x6E, 0x64, 0x72, 0x6F, 0x69, 0x64, 0x10, 0x17, 0x1A, (byte)0xB7, 0x02, 0x41, 0x42, 0x46, 0x45, 0x74, 0x31, 0x57, 0x56, 0x41, 0x72, 0x79, 0x59, 0x65, 0x4B, 0x58, 0x76, 0x32, 0x62, 0x6A, 0x71, 0x7A, 0x4F, 0x34, 0x35, 0x5F, 0x30, 0x36, 0x6F, 0x49, 0x30, 0x50, 0x63, 0x68, 0x4B, 0x43, 0x4E, 0x55, 0x54, 0x45, 0x6B, 0x4F, 0x36, 0x64, 0x39, 0x57, 0x50, 0x61, 0x56, 0x4C, 0x58, 0x32, 0x55, 0x41, 0x74, 0x67, 0x48, 0x54, 0x41, 0x6F, 0x4A, 0x30, 0x36, 0x41, 0x30, 0x74, 0x37, 0x69, 0x64, 0x50, 0x65, 0x41, 0x74, 0x6B, 0x53, 0x36, 0x31, 0x4A, 0x31, 0x58, 0x38, 0x79, 0x52, 0x36, 0x4B, 0x49, 0x32, 0x67, 0x4A, 0x57, 0x31, 0x63, 0x78, 0x51, 0x58, 0x37, 0x36, 0x33, 0x30, 0x52, 0x75, 0x6E, 0x4D, 0x2D, 0x54, 0x56, 0x6E, 0x4F, 0x75, 0x7A, 0x30, 0x78, 0x7A, 0x39, 0x68, 0x64, 0x37, 0x36, 0x31, 0x5F, 0x33, 0x67, 0x49, 0x6B, 0x4F, 0x4E, 0x63, 0x49, 0x49, 0x32, 0x48, 0x72, 0x46, 0x6E, 0x59, 0x58, 0x2D, 0x78, 0x32, 0x7A, 0x48, 0x57, 0x39, 0x4C, 0x71, 0x42, 0x43, 0x61, 0x59, 0x54, 0x70, 0x6B, 0x50, 0x78, 0x53, 0x34, 0x52, 0x45, 0x6D, 0x66, 0x56, 0x56, 0x4B, 0x78, 0x74, 0x6D, 0x47, 0x79, 0x64, 0x38, 0x51, 0x70, 0x56, 0x65, 0x65, 0x72, 0x4B, 0x61, 0x36, 0x55, 0x41, 0x54, 0x70, 0x79, 0x6C, 0x62, 0x48, 0x49, 0x39, 0x7A, 0x63, 0x51, 0x70, 0x37, 0x71, 0x33, 0x34, 0x52, 0x47, 0x66, 0x37, 0x68, 0x77, 0x51, 0x44, 0x71, 0x52, 0x43, 0x6B, 0x31, 0x4D, 0x63, 0x6F, 0x52, 0x51, 0x6C, 0x53, 0x34, 0x42, 0x75, 0x75, 0x61, 0x37, 0x4C, 0x51, 0x32, 0x66, 0x6E, 0x54, 0x46, 0x73, 0x69, 0x50, 0x33, 0x5F, 0x5A, 0x53, 0x63, 0x53, 0x49, 0x51, 0x77, 0x33, 0x38, 0x4A, 0x50, 0x4D, 0x38, 0x39, 0x41, 0x69, 0x74, 0x2D, 0x7A, 0x31, 0x70, 0x45, 0x38, 0x32, 0x6F, 0x66, 0x6A, 0x7A, 0x2D, 0x38, 0x2D, 0x68, 0x42, 0x47, 0x73, 0x62, 0x49, 0x53, 0x35, 0x41, 0x56, 0x48, 0x65, 0x76, 0x54, 0x4D, 0x46, 0x4B, 0x58, 0x42, 0x42, 0x7A, 0x39, 0x46, 0x70, 0x37, 0x63, 0x75, 0x69, 0x56, 0x49, 0x54, 0x42, 0x73, 0x35, 0x47, 0x35, 0x50, 0x75, 0x68, 0x79, 0x48, 0x48, 0x70, 0x4E, 0x42, 0x34, 0x21, (byte)0xDB, (byte)0xD7, (byte)0xB9, 0x1E, (byte)0xFD, (byte)0xCA, 0x1E, 0x50, 0x2A, 0x04, 0x0A, 0x02, 0x08, 0x03
			};
			*/
			// this.sendCompleted(bytes);
			try {
				delay(10, 20); // ©µ¿ð
				service.gms_android_device_managerlogin();
				delay(); // ©µ¿ð
				this.sendCompleted();
				delay(10, 20); // ©µ¿ð
				service.bulkDetails(Arrays.asList(new String[] { mPackageName }));
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						mAdapter.DownloadCompleted(CPIWorker.this);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						mAdapter.DownloadFailed(CPIWorker.this);
					}
				});				
			}			
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mAdapter.DownloadFailed(CPIWorker.this);
				}
			});
		}
	}
	
	private void sendLog() throws IOException {
		sendLog(CPIUtils.makeStartDownloadBytes(this.mPackageName));
	}
	
	private void sendLog(byte[] bytes) throws IOException {
		/*
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
		zipStream.write(bytes);
		zipStream.finish();
		bytes = byteStream.toByteArray();
		*/		
		HttpEntity httpEntity = executePost("https://android.clients.google.com/fdfe/log", new ByteArrayEntity(bytes), new String[][] {
			{ "Content-Type", "application/x-protobuffer" },
			{ "Accept-Language", Utils.txtLocale.replace("_", "-") },
			{ "X-DFE-MCCMNC", Utils.txtCOMM },
			{ "Authorization", String.format("GoogleLogin auth=%s", service.getToken()) },
			{ "X-DFE-Enabled-Experiments", "cl:details.double_fetch_social_data" },
			{ "X-DFE-Unsupported-Experiments", TOCResult.replace("cl:details.double_fetch_social_data,", "") },
			{ "X-DFE-Device-Id", service.getAndroidID() },
			{ "X-DFE-Client-Id", "am-android-samsung" },
			{ "X-DFE-Logging-Id", "" },
			// { "User-Agent", String.format("Dalvik/1.6.0 (Linux; U; Android %s; %s Build/%s)", Utils.txtAndroidVersion, Utils.txtProductModel, Utils.txtBuildId) },
			{ "User-Agent", String.format("Android-Finsky/3.10.14 (api=3,versionCode=8016014,sdk=%s,device=%s,hardware=%s,product=%s)", Utils.txtSDKVersion, Utils.txtProductModel, Utils.txtProductBoard, Utils.txtProductModel) },
			{ "X-DFE-Request-Params", "timeoutMs=2500" },
			{ "X-DFE-Filter-Level", "3" },
			{ "Host", "android.clients.google.com" },
			{ "Connection", "Keep-Alive" }
		});
	}
	
	private void sendCompleted(byte[] bytes) throws IOException {		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
		zipStream.write(bytes);
		zipStream.finish();
		bytes = byteStream.toByteArray();
		
		HttpEntity httpEntity = executePost("https://android.clients.google.com/config", new ByteArrayEntity(bytes), new String[][] {
			{ "Content-Type", "application/x-protobuffer" },
			// { "User-Agent", "Android-Checkin/2.0 (generic JRO03E)" },
			// { "User-Agent", "Dalvik/1.6.0 (Linux; U; Android 4.0.4; GT-I9300 Build/JZO54K)" },
			// { "User-Agent", "Dalvik/1.6.0 (Linux; U; Android 4.1.1; Nexus S Build/JRO03E)" },
			{ "User-Agent", String.format("Dalvik/1.6.0 (Linux; U; Android %s; %s Build/%s)", Utils.txtAndroidVersion, Utils.txtProductModel, Utils.txtBuildId) },
			{ "Host", "android.clients.google.com" },
			{ "Connection", "Keep-Alive" },
			{ "Accept-Encoding", "gzip" }
		});
	}
	
	private void sendCompleted() throws IOException {		
		// setUseragent("Android-Finsky/3.10.14 (api=3,versionCode=8016014,sdk=15,device=GT-I9300,hardware=aries,product=GT-I9300)");
		/*
		byte[] header = new byte[] {
			0x09, 0x67, 0x42, 0x55, 0x77, (byte)0xFE, 0x19, 0x01, 0x39, 0x12, 0x17, 0x0A, 0x13, 0x63, 0x6F, 0x6D, 0x2E, 0x66, 0x72, 0x75, 0x69, 0x74, 0x74, 0x62, 0x2E, 0x61, 0x6E, 0x64, 0x72, 0x6F, 0x69, 0x64, 0x10, 0x17, 0x1A, (byte)0xF7, 0x01
		};
		byte[] body = mCheckINKey.getBytes();
		byte[] footer = new byte[] {
			0x21, 0x7A, (byte)0x95, 0x10, 0x3B, (byte)0x93, (byte)0xC4, 0x57, 0x24, 0x2A, 0x04, 0x0A, 0x02, 0x08, 0x03
		};		
		byte[] bytes = new byte[header.length + body.length + footer.length];
		System.arraycopy(header, 0, bytes, 0, header.length);
		System.arraycopy(body, 0, bytes, header.length, body.length);
		System.arraycopy(footer, 0, bytes, header.length + body.length, footer.length);
		*/
		byte[] pBytes = mPackageName.getBytes("ASCII");
		byte[] bytes = CPIUtils.arrayMerge(
				new byte[] { 0x09 },
				mCheckINHeader,
				new byte[] { 0x12 },
				new byte[] { (byte)(pBytes.length + 4) },
				new byte[] { 0x0A },
				new byte[] { (byte)pBytes.length },
				pBytes,
				new byte[] { 0x10 },
				// new byte[] { (byte)0xFF }, // unknown
				new byte[] { (byte)mCurrentVersionCode },
				new byte[] { 0x1A },
				this.mCheckINKey,
				new byte[] { 0x21 },
				mCheckINFooter);
				// new byte[] { 0x2A, 0x04, 0x0A, 0x02, 0x08, 0x03 });
		sendCompleted(bytes);
	}
	
	private HttpEntity executePost(String url, HttpEntity postData, String[][] headerParams)
			throws IOException {
		HttpPost httppost = new HttpPost(url);

		if (headerParams != null) {
			for (String[] param : headerParams) {
				if (param[0] != null && param[1] != null) {
					httppost.setHeader(param[0], param[1]);
				}
			}
		}

		httppost.setEntity(postData);

		return executeHttpRequest(httppost);
	}
	
	private HttpEntity executeHttpRequest(HttpUriRequest request) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient(GooglePlayAPI.getConnectionManager());
		try {
			client.getConnectionManager().getSchemeRegistry().register(Utils.getMockedScheme());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bMonitorPacket) {			
			HttpHost proxy = new HttpHost(CPIUtils.ProxyURL, CPIUtils.ProxyPORT);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);		
		}
		HttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new GooglePlayException(new String(Utils.readAll(response.getEntity().getContent())));
		}
		return response.getEntity();
	}
	
	private void download(String packageName, int versionCode, int offerType, boolean paid) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		mCurrentVersionCode = versionCode;
		DownloadData data = null;
		if (paid) {
			data = service.delivery(packageName, versionCode, offerType);
		}
		else {
			data = service.download(packageName, versionCode, offerType);
		}
		File file = new File(packageName.concat(".apk"));
		if (!file.exists()) file.createNewFile();
		InputStream in = data.openApp();
		FileOutputStream out = new FileOutputStream(file);
		byte[] buffer = new byte[1024 * 16];
		int length;
		long received = 0;
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
			received += length;
		}
		out.flush();
		out.close();
		in.close();
	}

}
