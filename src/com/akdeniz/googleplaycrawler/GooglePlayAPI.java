package com.akdeniz.googleplaycrawler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.message.BasicNameValuePair;

import com.akdeniz.googleplaycrawler.GooglePlay.AndroidAppDeliveryData;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidCheckinProto;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidCheckinRequest;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidCheckinResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidEventProto;
import com.akdeniz.googleplaycrawler.GooglePlay.BrowseResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsRequest;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsRequest.Builder;
import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.BuyResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.DetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.ListResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.ResponseWrapper;
import com.akdeniz.googleplaycrawler.GooglePlay.ReviewResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.SearchResponse;
import com.akdeniz.googleplaycrawler.GooglePlay.UploadDeviceConfigRequest;
import com.akdeniz.googleplaycrawler.GooglePlay.UploadDeviceConfigResponse;
import com.niuniucpi.CPIUtils;
import com.niuniucpi.woker.ProtobufParser;

/**
 * This class provides
 * <code>checkin, search, details, bulkDetails, browse, list and download</code>
 * capabilities. It uses <code>Apache Commons HttpClient</code> for POST and GET
 * requests.
 * 
 * <p>
 * <b>XXX : DO NOT call checkin, login and download consecutively. To allow
 * server to catch up, sleep for a while before download! (5 sec will do!) Also
 * it is recommended to call checkin once and use generated android-id for
 * further operations.</b>
 * </p>
 * 
 * @author akdeniz
 * 
 */
public class GooglePlayAPI {

	private static final String CHECKIN_URL = "https://android.clients.google.com/checkin";
	private static final String URL_LOGIN = "https://android.clients.google.com/auth";
	private static final String C2DM_REGISTER_URL = "https://android.clients.google.com/c2dm/register3";
	private static final String FDFE_URL = "https://android.clients.google.com/fdfe/";
	private static final String LIST_URL = FDFE_URL + "list";
	private static final String BROWSE_URL = FDFE_URL + "browse";
	private static final String DETAILS_URL = FDFE_URL + "details";
	private static final String SEARCH_URL = FDFE_URL + "search";
	private static final String BULKDETAILS_URL = FDFE_URL + "bulkDetails?au=1";
	private static final String PURCHASE_URL = FDFE_URL + "purchase";
	private static final String REVIEWS_URL = FDFE_URL + "rev";
	private static final String UPLOADDEVICECONFIG_URL = FDFE_URL + "uploadDeviceConfig";
	private static final String RECOMMENDATIONS_URL = FDFE_URL + "rec";
	private static final String DELIVERY_URL = FDFE_URL + "delivery";

	private static final String ACCOUNT_TYPE_HOSTED_OR_GOOGLE = "HOSTED_OR_GOOGLE";

	public static String txtCellSimOperator;
	
	private String AndroidCheckInServertoken;
	private String token;
	private String androidID;
	private String email;
	private String password;
	private HttpClient client;
	private String securityToken;
	private String Digest;
	private String localization;
	private String useragent;
	private String a2dmToken;
	
	private static boolean bMonitorPacket = true; //設定proxy
	
	public static enum REVIEW_SORT {
		NEWEST(0), HIGHRATING(1), HELPFUL(2);

		public int value;

		private REVIEW_SORT(int value) {
			this.value = value;
		}
	}

	public static enum RECOMMENDATION_TYPE {
		ALSO_VIEWED(1), ALSO_INSTALLED(2);

		public int value;

		private RECOMMENDATION_TYPE(int value) {
			this.value = value;
		}
	}

	public static boolean getMonitorPacket() {
		return bMonitorPacket;
	}
	public static void setMonitorPacket(boolean value) {
		bMonitorPacket = value;
	}

	/**
	 * Default constructor. ANDROID ID and Authentication token must be supplied
	 * before any other operation.
	 */
	public GooglePlayAPI() {
	}

	/**
	 * Constructs a ready to login {@link GooglePlayAPI}.
	 */
	public GooglePlayAPI(String email, String password, String androidID) {
		this(email, password);
		this.setAndroidID(androidID);
	}

	/**
	 * If this constructor is used, Android ID must be generated by calling
	 * <code>checkin()</code> or set by using <code>setAndroidID</code> before
	 * using other abilities.
	 */
	public GooglePlayAPI(String email, String password) {
		this.setEmail(email);
		this.password = password;
		HttpClient client = new DefaultHttpClient(getConnectionManager());
		if (bMonitorPacket) {
			try {
				client.getConnectionManager().getSchemeRegistry().register(Utils.getMockedScheme());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HttpHost proxy = new HttpHost(CPIUtils.ProxyURL, CPIUtils.ProxyPORT);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);		
		}
		setClient(client);
		// setUseragent("Android-Finsky/3.10.14 (api=3,versionCode=8016014,sdk=15,device=GT-I9300,hardware=aries,product=GT-I9300)");
		setUseragent(String.format("Android-Finsky/3.10.14 (api=3,versionCode=8016014,sdk=%s,device=%s,hardware=%s,product=%s)", Utils.txtSDKVersion, Utils.txtProductModel, Utils.txtProductBoard, Utils.txtProductModel));
	}

	/**
	 * Connection manager to allow concurrent connections.
	 * 
	 * @return {@link ClientConnectionManager} instance
	 */
	public static ClientConnectionManager getConnectionManager() {
		PoolingClientConnectionManager connManager = new PoolingClientConnectionManager(
				SchemeRegistryFactory.createDefault());
		connManager.setMaxTotal(100);
		connManager.setDefaultMaxPerRoute(30);
		return connManager;
	}

	/**
	 * Performs authentication on "ac2dm" service and match up android id,
	 * security token and email by checking them in on this server.
	 * 
	 * This function sets check-inded android ID and that can be taken either by
	 * using <code>getToken()</code> or from returned
	 * {@link AndroidCheckinResponse} instance.
	 * 
	 */
	public AndroidCheckinResponse checkin() throws Exception {

		// this first checkin is for generating android-id
		AndroidCheckinResponse checkinResponse = postCheckin(Utils.generateAndroidCheckinRequest()
				.toByteArray());
		this.setAndroidID(BigInteger.valueOf(checkinResponse.getAndroidId()).toString(16));
		setSecurityToken((BigInteger.valueOf(checkinResponse.getSecurityToken()).toString(16)));

		String c2dmAuth = loginAC2DM();
		String[][] data = new String[][] {
			{ "app", "com.android.vending" },
			{ "sender", "932144863878" },
			{ "device", new BigInteger(this.getAndroidID(), 16).toString() } };
		// HttpEntity responseEntity = executePost(C2DM_REGISTER_URL, data, getHeaderParameters(c2dmAuth, null));
			HttpEntity responseEntity = executePost(C2DM_REGISTER_URL, data, new String[][] { 
				{ "Authorization", String.format("AidLogin %s:%s", 
						new BigInteger(this.getAndroidID(), 16).toString(), new BigInteger(CPIUtils.getHexString(this.getCheckINFooter())).toString()) },
				{ "Content-Type", "application/x-www-form-urlencoded" },
				{ "Host", "android.clients.google.com" },
				{ "Connection", "Keep-Alive" },
				{ "User-Agent", String.format("AndroidC2DM/1.0 (%s %s)", Utils.txtProductDevice, Utils.txtBuildId) },
			});
		Utils.parseResponse(new String(Utils.readAll(responseEntity.getContent())));

		AndroidCheckinRequest.Builder checkInbuilder = AndroidCheckinRequest.newBuilder(Utils
				.generateAndroidCheckinRequest());

		AndroidCheckinRequest build = checkInbuilder
				.setId(new BigInteger(this.getAndroidID(), 16).longValue())
				.setSecurityToken(new BigInteger(getSecurityToken(), 16).longValue())
				.addAccountCookie("[" + getEmail() + "]").addAccountCookie(c2dmAuth).build();
		// this is the second checkin to match credentials with android-id
		return postCheckin(build.toByteArray());
	}
	
	private static final Random rand = new Random();
	/**
     * 產生MAC
     */
	private static String generateMacAddr() {
        String mac = "b407f9";
        for (int i = 0; i < 6; i++)
            mac += Integer.toString(rand.nextInt(16), 16);
        return mac;
    }
	
	/**
     * 產生MEID
     */
	private static String generateMeid() {
        // http://en.wikipedia.org/wiki/International_Mobile_Equipment_Identity
        // We start with a known base, and generate random MEID
        String meid = "35503104";
        for (int i = 0; i < 6; i++)
            meid += Integer.toString(rand.nextInt(10));

        // Luhn algorithm (check digit)
        int sum = 0;
        for (int i = 0; i < meid.length(); i++) {
            int c = Integer.parseInt(String.valueOf(meid.charAt(i)));
            if ((meid.length() - i - 1) % 2 == 0) {
                c *= 2;
                c = c % 10 + c / 10;
            }

            sum += c;
        }
        int check = (100 - sum) % 10;
        meid += Integer.toString(check);

        return meid;
    }
	
	/**
     * 產生Serial
     */
	private static String generateSerialNumber() {
        String serial = "3933E6";
        for (int i = 0; i < 10; i++)
            serial += Integer.toString(rand.nextInt(16), 16);
        serial = serial.toUpperCase();
        return serial;
    }
	
	public String CPI_checkin() throws Exception {

		// this first checkin is for generating android-id
		AndroidCheckinResponse checkinResponse = postCheckin(Utils.i9500generateAndroidCheckinRequest(true).toByteArray());
		this.setAndroidID(BigInteger.valueOf(checkinResponse.getAndroidId()).toString(16));
		this.setSecurityToken((BigInteger.valueOf(checkinResponse.getSecurityToken()).toString(16)));
		this.setDigest(checkinResponse.getDigest());
		//String[] checkindata = new String[]{this.getAndroidID(),this.getSecurityToken()};
		return this.androidID;
	}
	
	public void CPI_AC2DM() throws Exception {

		String c2dmAuth = loginAC2DM();
		String[][] data = new String[][] {
			{ "app", "com.android.vending" },
			{ "sender", "932144863878" },
			{ "device", new BigInteger(this.getAndroidID(), 16).toString() } };
		// HttpEntity responseEntity = executePost(C2DM_REGISTER_URL, data, getHeaderParameters(c2dmAuth, null));
			HttpEntity responseEntity = executePost(C2DM_REGISTER_URL, data, new String[][] { 
				{ "Authorization", String.format("AidLogin %s:%s", 
						new BigInteger(this.androidID, 16).toString(), new BigInteger(this.securityToken, 16).toString()) },
				{ "Content-Type", "application/x-www-form-urlencoded" },
				{ "Host", "android.clients.google.com" },
				{ "Connection", "Keep-Alive" },
				{ "User-Agent", String.format("AndroidC2DM/1.0 (%s %s)", Utils.txtProductDevice, Utils.txtBuildId) },
			});
		Utils.parseResponse(new String(Utils.readAll(responseEntity.getContent())));
	}

	public void CPI_lastcheckin() throws Exception {

		AndroidCheckinRequest.Builder checkInbuilder = AndroidCheckinRequest.newBuilder(Utils
				.i9500generateAndroidCheckinRequest_last(true));

		AndroidCheckinRequest build = checkInbuilder
				.setId(new BigInteger(this.androidID, 16).longValue())
				.setSecurityToken(new BigInteger(this.securityToken, 16).longValue())
				.setDigest(this.Digest)
				//.addAccountCookie("[" + getEmail() + "]").addAccountCookie(c2dmAuth).build();
		        .addAccountCookie("[" + this.getEmail() + "@gmail.com" + "]").addAccountCookie(this.AndroidCheckInServertoken).build();
		postCheckin(build.toByteArray());
		// this is the second checkin to match credentials with android-id
	}
	
	/*
	public AndroidCheckinResponse checkin(boolean manual) throws Exception {

		//String password = Utils.encryptString(this.password);
		if (manual) {
			// this first checkin is for generating android-id
			String Digest1 ="a1PPnDfPObkrz3cIrtTGLA==";//i9300_Digest_first
			String LastCheckinMsec = "1439274780644";//i9300 LastCheckinMsec
			String SerialNumber = "C1FB6B2D18544B0A";//i9300 SerialNumber
	    	String firstlogtime = "1439278028945";//i9300 logtime
	    	long newlogtime = new Date().getTime();
	    	
			//String Digest1 ="60t+/Yy+7xGgjWPlbJo9eA==";//i9505
			//String Digest2 ="1-9514a44a6a06f94a2d583f60f4288ce548add17d";
			//String MacAddr = "B7D33440CE9A";//i9300 mac
			String MacAddr = generateMacAddr();
			long LoggingId = rand.nextLong();
			
			//log 登入事件
			AndroidEventProto.Builder checkEventProtofirst = AndroidEventProto.newBuilder(Utils
					.generateAndroidEventProto());
			
			//設備資訊+log 登入事件
			AndroidCheckinProto.Builder checkInProtofirst = AndroidCheckinProto.newBuilder(Utils.
					generateAndroidCheckinProto());
			checkInProtofirst.setLastCheckinMsec(new BigInteger(LastCheckinMsec).longValue())
			.addEvent(checkEventProtofirst
					.setTimeMsec(new BigInteger(firstlogtime).longValue()))
			.addEvent(checkEventProtofirst
					.setTimeMsec(newlogtime))
			//.addEvent(checkEventProtonew)
			.build();

			//完整封包
			AndroidCheckinRequest.Builder checkInbuilderfirst = AndroidCheckinRequest.newBuilder(Utils
					.generateAndroidCheckinRequest3(true, Digest1, 0, SerialNumber, MacAddr, LoggingId));

			AndroidCheckinRequest buildfirst = checkInbuilderfirst
					.setDigest(Digest1)
					.setCheckin(checkInProtofirst)
					.build();
			
			AndroidCheckinResponse checkinResponse = postCheckin(buildfirst.toByteArray());
			
			this.setAndroidID(BigInteger.valueOf(checkinResponse.getAndroidId()).toString(16));
			setSecurityToken((BigInteger.valueOf(checkinResponse.getSecurityToken()).toString(16)));

			String Digest2 = checkinResponse.getDigest();
			if(Digest2 == "")
			{
				Digest2 = Digest1;
			}
			//long aa = new BigInteger("02b9be724a389e92", 16).longValue();
			//long a = BigInteger.valueOf(checkinResponse.getAndroidId()).longValue();
			//String b = BigInteger.valueOf(checkinResponse.getSecurityToken()).toString(16);
			
			
			String c2dmAuth = loginAC2DM();
			String[][] data = new String[][] {
				{ "app", "com.android.vending" },
				{ "sender", "932144863878" },
				{ "device", new BigInteger(this.getAndroidID(), 16).toString() } };
			// HttpEntity responseEntity = executePost(C2DM_REGISTER_URL, data, getHeaderParameters(c2dmAuth, null));
				byte[] footer = this.getCheckINFooter();
				byte[] reversed = new byte[footer.length];
				int j = 0;
				for (int i = footer.length - 1; i >= 0; i--) reversed[j++] = footer[i];
				HttpEntity responseEntity = executePost(C2DM_REGISTER_URL, data, new String[][] { 
					{ "Authorization", String.format("AidLogin %s:%s", 
							//new BigInteger(this.getAndroidID(), 16).toString(), new BigInteger(CPIUtils.getHexString(reversed), 16).toString()) },
							new BigInteger(this.getAndroidID(), 16).toString(), BigInteger.valueOf(checkinResponse.getSecurityToken()).toString()) },
					{ "Content-Type", "application/x-www-form-urlencoded" },
					{ "Host", "android.clients.google.com" },
					{ "Connection", "Keep-Alive" },
					{ "User-Agent", String.format("AndroidC2DM/1.0 (%s %s)", Utils.txtProductDevice, Utils.txtBuildId) },
				});
			Utils.parseResponse(new String(Utils.readAll(responseEntity.getContent())));
			
			//long logtime = checkinResponse.getTimeMsec();
			AndroidCheckinRequest.Builder checkInbuilder = AndroidCheckinRequest.newBuilder(Utils
					.generateAndroidCheckinRequest3(true, Digest2, 0, SerialNumber, MacAddr, LoggingId));

			AndroidCheckinRequest build = checkInbuilder
					.addAccountCookie("[" + getEmail() + "]").addAccountCookie(c2dmAuth)
					.setDigest(Digest2)
					.setCheckin(checkInProtofirst
							.setLastCheckinMsec(newlogtime)
							.clearEvent())
					.build();
			// this is the second checkin to match credentials with android-id
			return postCheckin(build.toByteArray());
			// return checkinResponse;
		} else return checkin();
	}
    */
	
	/**
	 * Logins AC2DM server and returns authentication string.
	 */
	public String loginAC2DM() throws IOException {
		HttpEntity c2dmResponseEntity = executePost(URL_LOGIN,
				new String[][] {
					
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "has_permission", "1" },
			{ "add_account", "1" },
			{ "Passwd", this.password },
			{ "service", "ac2dm" },
			{ "source", "android" },
			{ "androidId", this.androidID },
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0].toLowerCase() },
			{ "RefreshServices", "1" },},
				
				new String[][] {
							{ "User-Agent", String.format("GoogleLoginService/1.3 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
						});

		Map<String, String> c2dmAuth = Utils.parseResponse(new String(Utils.readAll(c2dmResponseEntity
				.getContent())));
		
		this.setAndroida2dmToken(c2dmAuth.get("Token"));
		
		return c2dmAuth.get("Auth");
	}

	public Map<String, String> c2dmRegister(String application, String sender) throws IOException {

		String c2dmAuth = loginAC2DM();
		String[][] data = new String[][] {
				{ "app", application },
				{ "sender", sender },
				{ "device", new BigInteger(this.getAndroidID(), 16).toString() } };
		HttpEntity responseEntity = executePost(C2DM_REGISTER_URL, data,
				getHeaderParameters(c2dmAuth, null));
		return Utils.parseResponse(new String(Utils.readAll(responseEntity.getContent())));
	}

	/**
	 * Equivalent of <code>setToken</code>. This function does not performs
	 * authentication, it simply sets authentication token.
	 */
	public void login(String token) throws Exception {
		setToken(token);
	}

	/**
	 * Authenticates on server with given email and password and sets
	 * authentication token. This token can be used to login instead of using
	 * email and password every time.
	 */
	public void login() throws Exception {

		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "has_permission", "1" },
			{ "EncryptedPasswd", this.a2dmToken },
			{ "service", "androidmarket" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "app", "com.android.vending" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0] },
			{"RefreshServices","1"}},
			new String[][] {
				{ "User-Agent", String.format("GoogleLoginService/1.3 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
			});
		
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
	}
	
	/**
	 * service : ac2dm app : com.google.android.gms
	 */
	public void gms_ac2dmlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "Email", this.getEmail() },
			{ "has_permission", "1" },
			{ "Token", this.a2dmToken },
			{ "service", "ac2dm" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "device_country", Utils.txtLocale.split("_")[1] },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0] },
			{ "RefreshServices", "1" }, },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
			});
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}

	/**
	 * service : oauth2:https://www.googleapis.com/auth/gcm
	 */
	public void googleapis_gcmlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/gcm" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
				{ "device", this.getAndroidID() },
				{ "app", "com.google.android.gms" }
			});
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * oauth2:https://www.googleapis.com/auth/userinfo.profile
	 */
	public void googleapis_userinfologin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/userinfo.profile" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "check_email", "1" },
			{ "get_accountid", "1" },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
				{ "device", this.getAndroidID() },
				{ "app", "com.google.android.gms" }
			});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms service : ^^_account_id_^^
	 */
	public void gms_accountidlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "^^_account_id_^^" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "check_email", "1" },
			{ "get_accountid", "1" },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
				{ "device", this.getAndroidID() },
				{ "app", "com.google.android.gms"}
			});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms service : oauth2:https://www.googleapis.com/auth/cryptauth
	 */
	public void gms_cryptauthlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0] },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/cryptauth" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "check_email", "1" },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
				{ "device", this.getAndroidID() },
				{ "app", "com.google.android.gms"}
			});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms service : AndroidCheckInServer
	 */
	public void gms_AndroidCheckInServerlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "AndroidCheckInServer" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "check_email", "1" },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
				{ "device", this.getAndroidID() },
				{ "app", "com.google.android.gms"}
			});
		
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		if (response.containsKey("Auth")) {
			setAndroidCheckInServerToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
	}
	
	/**
	 * app : com.google.android.gms service : oauth2:https://www.googleapis.com/auth/ads_measurement
	 */
	public void gms_ads_measurementlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/ads_measurement" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
				
				new String[][] {
					{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
					{ "device", this.getAndroidID() },
					{ "app", "com.google.android.gms"}
				});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms service : oauth2:https://www.googleapis.com/auth/emeraldsea.mobileapps.doritos.cookie
	 */
	public void gms_cookielogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/emeraldsea.mobileapps.doritos.cookie" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "check_email", "1" },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
				new String[][] {
					{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
					{ "device", this.getAndroidID() },
					{ "app", "com.google.android.gms"}
				});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms service : oauth2:https://www.googleapis.com/auth/userlocation.reporting
	 */
	public void gms_reportinglogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/userlocation.reporting" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "check_email", "1" },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "Token", this.a2dmToken },
			 },
				new String[][] {
					{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
					{ "device", this.getAndroidID() },
					{ "app", "com.google.android.gms"}
				});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms 
	 * service : oauth2:https://www.googleapis.com/auth/plus.circles.read https://www.googleapis.com/auth/plus.circles.write https://www.googleapis.com/auth/plus.media.upload https://www.googleapis.com/auth/plus.pages.manage https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/plus.profiles.read https://www.googleapis.com/auth/plus.profiles.write https://www.googleapis.com/auth/plus.stream.read https://www.googleapis.com/auth/plus.peopleapi.readwrite https://www.googleapis.com/auth/plus.applications.manage https://www.googleapis.com/auth/plus.settings
	 */
	public void gms_Multilogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/plus.circles.read https://www.googleapis.com/auth/plus.circles.write https://www.googleapis.com/auth/plus.media.upload https://www.googleapis.com/auth/plus.pages.manage https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/plus.profiles.read https://www.googleapis.com/auth/plus.profiles.write https://www.googleapis.com/auth/plus.stream.read https://www.googleapis.com/auth/plus.peopleapi.readwrite https://www.googleapis.com/auth/plus.applications.manage https://www.googleapis.com/auth/plus.settings" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "check_email", "1" },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
				new String[][] {
					{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
					{ "device", this.getAndroidID() },
					{ "app", "com.google.android.gms"}
				});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms 
	 * service : oauth2:https://www.googleapis.com/auth/login_manager
	 */
	public void gms_managerlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/login_manager" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "Token", this.a2dmToken },
			 },
				new String[][] {
					{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
					{ "device", this.getAndroidID() },
					{ "app", "com.google.android.gms"}
				});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms 
	 * service : oauth2:https://www.googleapis.com/auth/reminders
	 */
	public void gms_reminderslogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/reminders" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "EncryptedPasswd", this.a2dmToken },
			 },
				new String[][] {
					{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
					{ "device", this.getAndroidID() },
					{ "app", "com.google.android.gms"}
				});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.android.vending 
	 * service : androidsecure
	 */
	public void androidsecurelogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "has_permission", "1" },
			{ "Token", this.a2dmToken },
			{ "service", "androidsecure" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "app", "com.android.vending" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "RefreshServices", "1" },
			 },
			new String[][] {
				{ "User-Agent", String.format("GoogleLoginService/1.3 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
			});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * app : com.google.android.gms 
	 * service : oauth2:https://www.googleapis.com/auth/android_device_manager
	 */
	public void gms_android_device_managerlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/android_device_manager" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "check_email", "1" },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.gms"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "Token", this.a2dmToken },
			 },
				new String[][] {
					{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) },
					{ "device", this.getAndroidID() },
					{ "app", "com.google.android.gms"}
				});
		
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * service : cl app : com.google.android.syncadapters.calendar
	 */
	public void cl_calendarlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "has_permission", "1" },
			{ "EncryptedPasswd", this.a2dmToken },
			{ "service", "cl" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "app", "com.google.android.syncadapters.calendar" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "device_country", Utils.txtLocale.split("_")[1] },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0] },
			{ "RefreshServices", "1" }, },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
			});
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * service : mail app : com.google.android.gms
	 */
	public void gma_maillogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "has_permission", "1" },
			{ "EncryptedPasswd", this.a2dmToken },
			{ "service", "mail" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "device_country", Utils.txtLocale.split("_")[1] },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0] },
			{ "RefreshServices", "1" }, },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
			});
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * service : cp app : com.google.android.gms
	 */
	public void gms_cplogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "Email", this.getEmail() + "@gmail.com" },
			{ "has_permission", "1" },
			{ "Token", this.a2dmToken },
			{ "service", "cp" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "app", "com.google.android.gms" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "device_country", Utils.txtLocale.split("_")[1] },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0] },
			{ "RefreshServices", "1" }, },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
			});
		/*
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
		*/
	}
	
	/**
	 * 不確定APP
	 * app : com.google.android.googlequicksearchbox service : oauth2:https://www.googleapis.com/auth/googlenow^
	 */
	public void googlenowlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1] },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0] },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "oauth2:https://www.googleapis.com/auth/googlenow" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "get_accountid", "1" },
			{ "Email", this.getEmail() },
			{ "app", "com.google.android.googlequicksearchbox" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.googlequicksearchbox"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "Token", this.a2dmToken },
			 },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
			});
		
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
	}
	
	/**
	 * 不確定APP
	 * app : com.google.android.googlequicksearchbox
	 * weblogin:service%3Dhist%26continue%3Dhttps%253A%252F%252Fwww.google.com.tw&de=1
	 */
	public void webloginlogin() throws Exception {
		
		HttpEntity responseEntity = executePost(URL_LOGIN, new String[][] {
			{ "device_country", Utils.txtLocale.split("_")[1] },
			{ "operatorCountry", Utils.txtLocale.split("_")[1].toLowerCase() },
			{ "lang", Utils.txtLocale.split("_")[0] },
			{ "sdk_version", Utils.txtSDKVersion },
			{ "google_play_services_version", "7899034" },
			{ "accountType", ACCOUNT_TYPE_HOSTED_OR_GOOGLE },
			{ "system_partition", "1" },
			{ "has_permission", "1" },
			{ "service", "weblogin:service%3Dhist%26continue%3Dhttps%253A%252F%252Fwww.google.com.tw&de=1" },
			{ "source", "android" },
			{ "androidId", this.getAndroidID() },
			{ "get_accountid", "1" },
			{ "Email", this.getEmail() },
			{ "app", "com.google.android.googlequicksearchbox" },
			{ "client_sig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "callerPkg", "com.google.android.googlequicksearchbox"},
			{ "callerSig", "38918a453d07199354f8b19af05ec6562ced5788" },
			{ "Token", this.a2dmToken },
			 },
			new String[][] {
				{ "User-Agent", String.format("GoogleAuth/1.4 (%s %s)", Utils.txtProductModel, Utils.txtBuildId) }
			});
		
		Map<String, String> response = Utils.parseResponse(new String(Utils.readAll(responseEntity
				.getContent())));
		if (response.containsKey("Auth")) {
			setToken(response.get("Auth"));
		}
		else {
			throw new GooglePlayException("Authentication failed!");
		}
	}

	/**
	 * Equivalent of <code>search(query, null, null)</code>
	 */
	public SearchResponse search(String query) throws IOException {
		return search(query, null, null);
	}

	/**
	 * Fetches a search results for given query. Offset and numberOfResult
	 * parameters are optional and <code>null</code> can be passed!
	 */
	public SearchResponse search(String query, Integer offset, Integer numberOfResult)
			throws IOException {

		ResponseWrapper responseWrapper = executeGETRequest(SEARCH_URL, new String[][] {
				{ "c", "3" },
				{ "q", query },
				{ "o", (offset == null) ? null : String.valueOf(offset) },
				{ "n", (numberOfResult == null) ? null : String.valueOf(numberOfResult) }, });

		return responseWrapper.getPayload().getSearchResponse();
	}

	/**
	 * Fetches detailed information about passed package name. If it is needed to
	 * fetch information about more than one application, consider to use
	 * <code>bulkDetails</code>.
	 */
	public DetailsResponse details(String packageName) throws IOException {
		ResponseWrapper responseWrapper = executeGETRequest(DETAILS_URL, new String[][] { {
				"doc",
				packageName }, });

		return responseWrapper.getPayload().getDetailsResponse();
	}

	/** Equivalent of details but bulky one! */
	public BulkDetailsResponse bulkDetails(List<String> packageNames) throws IOException {

		Builder bulkDetailsRequestBuilder = BulkDetailsRequest.newBuilder();
		bulkDetailsRequestBuilder.addAllDocid(packageNames);

		ResponseWrapper responseWrapper = executePOSTRequest(BULKDETAILS_URL, bulkDetailsRequestBuilder
				.build().toByteArray(), "application/x-protobuf");

		return responseWrapper.getPayload().getBulkDetailsResponse();
	}

	/** Fetches available categories */
	public BrowseResponse browse() throws IOException {

		return browse(null, null);
	}

	public BrowseResponse browse(String categoryId, String subCategoryId) throws IOException {

		ResponseWrapper responseWrapper = executeGETRequest(BROWSE_URL, new String[][] {
				{ "c", "3" },
				{ "cat", categoryId },
				{ "ctr", subCategoryId } });

		return responseWrapper.getPayload().getBrowseResponse();
	}

	/**
	 * Equivalent of <code>list(categoryId, null, null, null)</code>. It fetches
	 * sub-categories of given category!
	 */
	public ListResponse list(String categoryId) throws IOException {
		return list(categoryId, null, null, null);
	}

	/**
	 * Fetches applications within supplied category and sub-category. If
	 * <code>null</code> is given for sub-category, it fetches sub-categories of
	 * passed category.
	 * 
	 * Default values for offset and numberOfResult are "0" and "20" respectively.
	 * These values are determined by Google Play Store.
	 */
	public ListResponse list(String categoryId, String subCategoryId, Integer offset,
			Integer numberOfResult) throws IOException {
		ResponseWrapper responseWrapper = executeGETRequest(LIST_URL, new String[][] {
				{ "c", "3" },
				{ "cat", categoryId },
				{ "ctr", subCategoryId },
				{ "o", (offset == null) ? null : String.valueOf(offset) },
				{ "n", (numberOfResult == null) ? null : String.valueOf(numberOfResult) }, });

		return responseWrapper.getPayload().getListResponse();
	}

	/**
	 * Downloads given application package name, version and offer type. Version
	 * code and offer type can be fetch by <code>details</code> interface.
	 **/
	public DownloadData download(String packageName, int versionCode, int offerType)
			throws IOException {

		BuyResponse buyResponse = purchase(packageName, versionCode, offerType);

		return new DownloadData(this, buyResponse.getPurchaseStatusResponse().getAppDeliveryData());

	}

	public DownloadData delivery(String packageName, int versionCode, int offerType)
			throws IOException {
		ResponseWrapper responseWrapper = executeGETRequest(DELIVERY_URL, new String[][] {
				{ "ot", String.valueOf(offerType) },
				{ "doc", packageName },
				{ "vc", String.valueOf(versionCode) }, });

		AndroidAppDeliveryData appDeliveryData = responseWrapper.getPayload().getDeliveryResponse()
				.getAppDeliveryData();
		return new DownloadData(this, appDeliveryData);
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

	/**
	 * Posts given check-in request content and returns
	 * {@link AndroidCheckinResponse}.
	 */
	private AndroidCheckinResponse postCheckin(byte[] request) throws IOException {

		HttpEntity httpEntity = executePost(CHECKIN_URL, new ByteArrayEntity(request), new String[][] {
				// { "User-Agent", "Android-Checkin/2.0 (generic JRO03E); gzip" },
			{ "Content-Type", "application/x-protobuffer" },
			{ "User-Agent", String.format("Dalvik/1.6.0 (Linux; U; Android %s; %s Build/%s)", Utils.txtAndroidVersion, Utils.txtProductModel, Utils.txtBuildId) },
			{ "Host", "android.clients.google.com" }, });
		
		InputStream stream = httpEntity.getContent();
		byte[] bytes = IOUtils.toByteArray(stream);		
		stream.close();
		
		mCheckINHeader = new byte[8];
		mCheckINFooter = new byte[8];
		
		System.arraycopy(bytes, 10, mCheckINHeader, 0, 8);
		System.arraycopy(bytes, 19, mCheckINFooter, 0, 8);
		
		String buffer = new String(bytes, "ASCII");
		int index = buffer.indexOf("ABFEt1") - 2;
		mCheckINKey = new byte[bytes.length - index];
		System.arraycopy(bytes, index, mCheckINKey, 0, mCheckINKey.length);
		
		stream = new ByteArrayInputStream(bytes);		
		AndroidCheckinResponse ret = AndroidCheckinResponse.parseFrom(stream);
		return ret;
	}

	/**
	 * This function is used for fetching download url and donwload cookie, rather
	 * than actual purchasing.
	 */
	private BuyResponse purchase(String packageName, int versionCode, int offerType)
			throws IOException {

		ResponseWrapper responseWrapper = executePOSTRequest(PURCHASE_URL,
				null,
				new String[][] {
					{ "ot", String.valueOf(offerType) },
					{ "doc", packageName },
					{ "vc", String.valueOf(versionCode) }, });

		return responseWrapper.getPayload().getBuyResponse();
	}

	/**
	 * Fetches url content by executing GET request with provided cookie string.
	 */
	public InputStream executeDownload(String url, String cookie) throws IOException {

		String[][] headerParams = new String[][] {
				{ "Cookie", cookie },
				// { "User-Agent", "AndroidDownloadManager/4.1.1 (Linux; U; Android 4.1.1; Nexus S Build/JRO03E)" }, };
				{ "User-Agent", "AndroidDownloadManager" }, };

		HttpEntity httpEntity = executeGet(url, null, headerParams);
		return httpEntity.getContent();
	}

	/**
	 * Fetches the reviews of given package name by sorting passed choice.
	 * 
	 * Default values for offset and numberOfResult are "0" and "20" respectively.
	 * These values are determined by Google Play Store.
	 */
	public ReviewResponse reviews(String packageName, REVIEW_SORT sort, Integer offset,
			Integer numberOfResult) throws IOException {
		ResponseWrapper responseWrapper = executeGETRequest(REVIEWS_URL, new String[][] {
				{ "doc", packageName },
				{ "sort", (sort == null) ? null : String.valueOf(sort.value) },
				{ "o", (offset == null) ? null : String.valueOf(offset) },
				{ "n", (numberOfResult == null) ? null : String.valueOf(numberOfResult) } });

		return responseWrapper.getPayload().getReviewResponse();
	}

	/**
	 * Uploads device configuration to google server so that can be seen from web
	 * as a registered device!!
	 * 
	 * @see https://play.google.com/store/account
	 */
	public UploadDeviceConfigResponse uploadDeviceConfig() throws Exception {

		UploadDeviceConfigRequest request = UploadDeviceConfigRequest.newBuilder()
				.setDeviceConfiguration(Utils.getDeviceConfigurationProto()).build();
		ResponseWrapper responseWrapper = executePOSTRequest(UPLOADDEVICECONFIG_URL,
				request.toByteArray(), "application/x-protobuf");
		return responseWrapper.getPayload().getUploadDeviceConfigResponse();
	}
	
	private String mUploadDeviceConfigTimestamp = "";
	public String getUploadDeviceConfigTimestamp() {
		return mUploadDeviceConfigTimestamp;
	}
	
	public UploadDeviceConfigResponse uploadDeviceConfig(Map<String, Object> additional) throws Exception {

		UploadDeviceConfigRequest request = UploadDeviceConfigRequest.newBuilder()
				.setDeviceConfiguration(Utils.getDeviceConfigurationProto()).build();
		ResponseWrapper responseWrapper = executePOSTRequest(UPLOADDEVICECONFIG_URL,
				request.toByteArray(), "application/x-protobuf", additional);
		if (additional.containsKey("content")) {
			try {
				byte[] bytes = (byte[])additional.get("content");
				int length = bytes[6];
				byte[] newbytes = new byte[length];
				System.arraycopy(bytes, 7, newbytes, 0, newbytes.length);
				mUploadDeviceConfigTimestamp = new String(newbytes, "ASCII");
			} catch (Exception e) { }
		}
		return responseWrapper.getPayload().getUploadDeviceConfigResponse();
	}

	/**
	 * Fetches the recommendations of given package name.
	 * 
	 * Default values for offset and numberOfResult are "0" and "20" respectively.
	 * These values are determined by Google Play Store.
	 */
	public ListResponse recommendations(String packageName, RECOMMENDATION_TYPE type, Integer offset,
			Integer numberOfResult) throws IOException {
		ResponseWrapper responseWrapper = executeGETRequest(RECOMMENDATIONS_URL, new String[][] {
				{ "c", "3" },
				{ "doc", packageName },
				{ "rt", (type == null) ? null : String.valueOf(type.value) },
				{ "o", (offset == null) ? null : String.valueOf(offset) },
				{ "n", (numberOfResult == null) ? null : String.valueOf(numberOfResult) } });

		return responseWrapper.getPayload().getListResponse();
	}

	/* =======================Helper Functions====================== */

	/**
	 * Executes GET request and returns result as {@link ResponseWrapper}.
	 * Standard header parameters will be used for request.
	 * 
	 * @see getHeaderParameters
	 * */
	private ResponseWrapper executeGETRequest(String path, String[][] datapost) throws IOException {

		HttpEntity httpEntity = executeGet(path, datapost, getHeaderParameters(this.getToken(), null));
		return GooglePlay.ResponseWrapper.parseFrom(httpEntity.getContent());

	}

	/**
	 * Executes POST request and returns result as {@link ResponseWrapper}.
	 * Standard header parameters will be used for request.
	 * 
	 * @see getHeaderParameters
	 * */
	private ResponseWrapper executePOSTRequest(String path, String[][] headers, String[][] datapost) throws IOException {
		if (headers != null) {
			String[][] defaultHeaders = getHeaderParameters(this.getToken(), null);
			String[][] newHeaders = new String[headers.length + defaultHeaders.length][2];
			System.arraycopy(defaultHeaders, 0, newHeaders, 0, defaultHeaders.length);
			System.arraycopy(headers, 0, newHeaders, defaultHeaders.length, headers.length);
			HttpEntity httpEntity = executePost(path, datapost, newHeaders);
			return GooglePlay.ResponseWrapper.parseFrom(httpEntity.getContent());
		} else {
			return executePOSTRequest(path, datapost);
		}
	}
	
	private ResponseWrapper executePOSTRequest(String path, String[][] datapost) throws IOException {
		String[][] newHeaders = getHeaderParameters(this.getToken(), null);
		HttpEntity httpEntity = executePost(path, datapost, newHeaders);
		return GooglePlay.ResponseWrapper.parseFrom(httpEntity.getContent());
	}

	/**
	 * Executes POST request and returns result as {@link ResponseWrapper}.
	 * Content type can be specified for given byte array.
	 */
	private ResponseWrapper executePOSTRequest(String url, byte[] datapost, String contentType)
			throws IOException {

		HttpEntity httpEntity = executePost(url, new ByteArrayEntity(datapost),
				getHeaderParameters(this.getToken(), contentType));
		return GooglePlay.ResponseWrapper.parseFrom(httpEntity.getContent());

	}
	
	private ResponseWrapper executePOSTRequest(String url, byte[] datapost, String contentType, Map<String, Object> additional)
			throws IOException {

		HttpEntity httpEntity = executePost(url, new ByteArrayEntity(datapost),
				getHeaderParameters(this.getToken(), contentType));
		InputStream stream = httpEntity.getContent();
		byte[] bytes = IOUtils.toByteArray(stream);	
		additional.put("content", bytes);
		return GooglePlay.ResponseWrapper.parseFrom(new ByteArrayInputStream(bytes));

	}

	/**
	 * Executes POST request on given URL with POST parameters and header
	 * parameters.
	 */
	private HttpEntity executePost(String url, String[][] postParams, String[][] headerParams)
			throws IOException {

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();

		for (String[] param : postParams) {
			if (param[0] != null && param[1] != null) {
				formparams.add(new BasicNameValuePair(param[0], param[1]));
			}
		}

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");

		return executePost(url, entity, headerParams);
	}

	/**
	 * Executes POST request on given URL with {@link HttpEntity} typed POST
	 * parameters and header parameters.
	 */
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

	/**
	 * Executes GET request on given URL with GET parameters and header
	 * parameters.
	 */
	private HttpEntity executeGet(String url, String[][] getParams, String[][] headerParams)
			throws IOException {

		if (getParams != null) {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();

			for (String[] param : getParams) {
				if (param[0] != null && param[1] != null) {
					formparams.add(new BasicNameValuePair(param[0], param[1]));
				}
			}

			url = url + "?" + URLEncodedUtils.format(formparams, "UTF-8");
		}

		HttpGet httpget = new HttpGet(url);

		if (headerParams != null) {
			for (String[] param : headerParams) {
				if (param[0] != null && param[1] != null) {
					httpget.setHeader(param[0], param[1]);
				}
			}
		}

		return executeHttpRequest(httpget);
	}

	/** Executes given GET/POST request */
	private HttpEntity executeHttpRequest(HttpUriRequest request) throws ClientProtocolException,
			IOException {

		HttpResponse response = getClient().execute(request);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new GooglePlayException(new String(Utils.readAll(response.getEntity().getContent())));
		}

		return response.getEntity();
	}
	
	private HttpEntity executeGetWithoutBody(String url, String[][] headerParams)
			throws IOException {
		HttpGet httpget = new HttpGet(url);

		if (headerParams != null) {
			for (String[] param : headerParams) {
				if (param[0] != null && param[1] != null) {
					httpget.setHeader(param[0], param[1]);
				}
			}
		}

		return executeHttpRequest(httpget);
	}
	
	private String mTOCResult = "";
	
	public String getTOCResult() {
		if (mTOCResult == "") mTOCResult = getToc();
		return mTOCResult;
	}
	
	private String getToc() {
		try {
			HttpEntity httpEntity = executeGetWithoutBody("https://android.clients.google.com/fdfe/toc", new String[][] {
				{ "X-DFE-Device-Config-Token", this.getUploadDeviceConfigTimestamp() },
				{ "Accept-Language", Utils.txtLocale.replace("_", "-") },
				{ "X-DFE-MCCMNC", Utils.txtCOMM },
				{ "Authorization", String.format("GoogleLogin auth=%s", this.getToken()) },
				{ "X-DFE-Device-Id", this.getAndroidID() },
				{ "X-DFE-Client-Id", "am-android-samsung" },
				{ "X-DFE-Logging-Id", "" },
				{ "User-Agent", String.format("Android-Finsky/3.10.14 (api=3,versionCode=8016014,sdk=%s,device=%s,hardware=%s,product=%s)", Utils.txtSDKVersion, Utils.txtProductModel, Utils.txtProductBoard, Utils.txtProductModel) },
				{ "X-DFE-Request-Params", "timeoutMs=2500" },
				{ "X-DFE-Filter-Level", "3" },
				{ "Host", "android.clients.google.com" },
				{ "Connection", "Keep-Alive" }
			});
			byte[] bytes = IOUtils.toByteArray(httpEntity.getContent());
			
			try {
				ProtobufParser p = new ProtobufParser(bytes);
				String result = p.getResult();
				int index = result.indexOf("    5 {");
				if (index >= 0) {
					int endindex = result.indexOf("}", index + 1);
					String values = result.substring(index + 7, endindex);
					String[] parts = values.split("\r\n");
					StringBuilder sb = new StringBuilder();
					for (String line : parts) {
						String __line = line.trim();
						if (!__line.isEmpty()) {
							String[] dataparts = __line.split("1:");
							for (String dpart : dataparts) {
								String __dpart = dpart.trim();
								if (!__dpart.isEmpty()) {
									String __buffer = __dpart;
									if (__buffer.startsWith("\"")) __buffer = __buffer.substring(1);
									if (__buffer.endsWith("\"")) __buffer = __buffer.substring(0, __buffer.length() - 1);
									if (sb.length() > 0) sb.append(",");
									sb.append(String.format("%s", __buffer));
								}
							}
						}
					}
					return sb.toString();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Gets header parameters for GET/POST requests. If no content type is given,
	 * default one is used!
	 */
	private String[][] getHeaderParameters(String token, String contentType) {
		/*
		return new String[][] {
				//{ "Accept-Language", getLocalization() != null ? getLocalization() : "en-EN" },
				{ "Accept-Language", Utils.txtLocale.replace("_", "-") },
				{ "Authorization", "GoogleLogin auth=" + token },
				{ "X-DFE-Enabled-Experiments", "cl:billing.select_add_instrument_by_default" },
				{ "X-DFE-Unsupported-Experiments", "nocache:billing.use_charging_poller,market_emails,buyer_currency,prod_baseline,checkin.set_asset_paid_app_field,shekel_test,content_ratings,buyer_currency_in_app,nocache:encrypted_apk,recent_changes" },
				{ "X-DFE-Device-Id", this.getAndroidID() },
				{ "X-DFE-Client-Id", "am-android-google" },
				// { "User-Agent", getUseragent() },
				{ "User-Agent", String.format("Android-Finsky/4.8.19 (api=3,versionCode=80280019,sdk=%s,device=%s,hardware=unknown,product=%s)", Utils.txtSDKVersion, Utils.txtProductModel, Utils.txtProductModel) },
				{ "X-DFE-SmallestScreenWidthDp", "320" },
				{ "X-DFE-Filter-Level", "3" },
				{ "Host", "android.clients.google.com" },
				{ "Content-Type", (contentType != null) ? contentType : "application/x-www-form-urlencoded; charset=UTF-8" },
				{ "X-DFE-MCCMNC", Utils.txtCOMM  },
				{ "X-DFE-Request-Params", "timeoutMs=35000" }};
		*/
		return new String[][] {
			// { "Content-Type", "application/x-protobuffer" },
			{ "Content-Type", (contentType != null) ? contentType : "application/x-www-form-urlencoded; charset=UTF-8" },
			{ "Accept-Language", Utils.txtLocale.replace("_", "-") },
			{ "X-DFE-MCCMNC", Utils.txtCOMM },
			{ "Authorization", String.format("GoogleLogin auth=%s", this.getToken()) },
			{ "X-DFE-Enabled-Experiments", "cl:search.cap_local_suggestions_2,cl:search.hide_ordinals_from_search_results,cl:details.hide_download_count_in_title,cl:auth.use_reauth_api,cl:enable_rapid_auto_update" },
			{ "X-DFE-Unsupported-Experiments", mTOCResult.replace("nocache:dfe:dc:1,nocache:dfe:uc:TW,nocache:dfe:ci:1882,cl:details.details_page_v2_enabled,cl:details.double_fetch_social_data,", "") },
			{ "X-DFE-Device-Id", this.getAndroidID() },
			{ "X-DFE-Client-Id", "am-android-samsung" },
			{ "X-DFE-Logging-Id", "" },
			// { "User-Agent", String.format("Dalvik/1.6.0 (Linux; U; Android %s; %s Build/%s)", Utils.txtAndroidVersion, Utils.txtProductModel, Utils.txtBuildId) },
			{ "User-Agent", String.format("Android-Finsky/3.10.14 (api=3,versionCode=8016014,sdk=%s,device=%s,hardware=%s,product=%s)", Utils.txtSDKVersion, Utils.txtProductModel, Utils.txtProductBoard, Utils.txtProductModel) },
			{ "X-DFE-Request-Params", "timeoutMs=2500" },
			{ "X-DFE-Filter-Level", "3" },
			{ "Host", "android.clients.google.com" },
			{ "Connection", "Keep-Alive" }
		};
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAndroidID() {
		return androidID;
	}

	public void setAndroidID(String androidID) {
		this.androidID = androidID;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}
	
	public void setDigest(String Digest) {
		this.Digest = Digest;
	}

	public HttpClient getClient() {
		return client;
	}

	/**
	 * Sets {@link HttpClient} instance for internal usage of GooglePlayAPI. It is
	 * important to note that this instance should allow concurrent connections.
	 * 
	 * @see getConnectionManager
	 * 
	 * @param client
	 */
	public void setClient(HttpClient client) {
		this.client = client;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLocalization() {
		return localization;
	}

	/**
	 * Localization string that will be used in each request to server. Using this
	 * option you can fetch localized informations such as reviews and
	 * descriptions.
	 * <p>
	 * Note that changing this value has no affect on localized application list
	 * that server provides. It depends on only your IP location.
	 * <p>
	 * 
	 * @param localization
	 *          can be <b>en-EN, en-US, tr-TR, fr-FR ... (default : en-EN)</b>
	 */
	public void setLocalization(String localization) {
		this.localization = localization;
	}

	/**
	 * @return the useragent
	 */
	public String getUseragent() {
		return useragent;
	}

	/**
	 * @param useragent the useragent to set
	 */
	public void setUseragent(String useragent) {
		this.useragent = useragent;
	}
	
	/**
	 * @return the AndroidCheckInServertoken
	 */
	public void setAndroidCheckInServerToken(String token) {
		this.AndroidCheckInServertoken = token;
	}
	
	public void setAndroida2dmToken(String token) {
		this.a2dmToken = token;
	}
}
