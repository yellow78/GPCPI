package com.akdeniz.googleplaycrawler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.swing.JTextField;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;

import com.akdeniz.googleplaycrawler.GooglePlay.AndroidBuildProto;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidCheckinProto;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidCheckinRequest;
import com.akdeniz.googleplaycrawler.GooglePlay.AndroidEventProto;
import com.akdeniz.googleplaycrawler.GooglePlay.DeviceConfigurationProto;
import com.akdeniz.googleplaycrawler.misc.Base64;
import com.akdeniz.googleplaycrawler.misc.DummyX509TrustManager;

/**
 * 
 * @author akdeniz
 * 
 */
public class Utils {

	private static final Random rand = new Random();
    private static final String GOOGLE_PUBLIC_KEY = "AAAAgMom/1a/v0lblO2Ubrt60J2gcuXSljGFQXgcyZWveWLEwo6prwgi3"
	    + "iJIZdodyhKZQrNWp5nKJ3srRXcUW+F1BD3baEVGcmEgqaLZUNBjm057pKRI16kB0YppeGx5qIQ5QjKzsR8ETQbKLNWgRY0Q"
	    + "RNVz34kMJR3P/LgHax/6rmf5AAAAAwEAAQ==";
    
    public static String txtManufacture;
	public static String txtProductName;
	public static String txtProductDevice;
	public static String txtBuildId;
	public static String txtBuildVersionIncremental;
	public static String txtAndroidVersion;
	public static String txtProductBoard;
	public static String txtProductModel;
	public static String txtCellSimOperator;
	public static String txtLocale;
	public static String txtTimeZone;
	public static String txtSDKVersion;
	public static String txtCOMM;
	public static String txtIMEI;

	private static long TimeMsec;
	private static long LoggingId;
	private static String MacAddr;
	private static String SerialNumber;
	/**
     * 產生MAC
     */
	private static String generateMacAddr() {
        String mac = "83898E";
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
	
    /**
     * Parses key-value response into map.
     */
    public static Map<String, String> parseResponse(String response) {

	Map<String, String> keyValueMap = new HashMap<String, String>();
	StringTokenizer st = new StringTokenizer(response, "\n\r");

	while (st.hasMoreTokens()) {
	    String[] keyValue = st.nextToken().split("=");
	    keyValueMap.put(keyValue[0], keyValue[1]);
	}

	return keyValueMap;
    }

    private static PublicKey createKey(byte[] keyByteArray) throws Exception {

	int modulusLength = readInt(keyByteArray, 0);
	byte[] modulusByteArray = new byte[modulusLength];
	System.arraycopy(keyByteArray, 4, modulusByteArray, 0, modulusLength);
	BigInteger modulus = new BigInteger(1, modulusByteArray);

	int exponentLength = readInt(keyByteArray, modulusLength + 4);
	byte[] exponentByteArray = new byte[exponentLength];
	System.arraycopy(keyByteArray, modulusLength + 8, exponentByteArray, 0, exponentLength);
	BigInteger publicExponent = new BigInteger(1, exponentByteArray);

	return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
    }

    /**
     * Encrypts given string with Google Public Key.
     * 
     */
    public static String encryptString(String str2Encrypt) throws Exception {

	byte[] keyByteArray = Base64.decode(GOOGLE_PUBLIC_KEY, Base64.DEFAULT);

	byte[] header = new byte[5];
	byte[] digest = MessageDigest.getInstance("SHA-1").digest(keyByteArray);
	header[0] = 0;
	System.arraycopy(digest, 0, header, 1, 4);

	//String a = Base64.encodeToString(header, 10);
	//String b = Base64.encodeToString(digest, 10);
	PublicKey publicKey = createKey(keyByteArray);

	Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING");
	byte[] bytes2Encrypt = str2Encrypt.getBytes("UTF-8");
	int len = ((bytes2Encrypt.length - 1) / 86) + 1;
	byte[] cryptedBytes = new byte[len * 133];

	for (int j = 0; j < len; j++) {
	    cipher.init(1, publicKey);
	    byte[] arrayOfByte4 = cipher.doFinal(bytes2Encrypt, j * 86, (bytes2Encrypt.length - j * 86));
	    System.arraycopy(header, 0, cryptedBytes, j * 133, header.length);
	    System.arraycopy(arrayOfByte4, 0, cryptedBytes, j * 133 + header.length, arrayOfByte4.length);
	}
	return Base64.encodeToString(cryptedBytes, 10);
    }

    private static int readInt(byte[] data, int offset) {
	return (0xFF & data[offset]) << 24 | (0xFF & data[(offset + 1)]) << 16 | (0xFF & data[(offset + 2)]) << 8
		| (0xFF & data[(offset + 3)]);
    }

    /**
     * Reads all contents of the input stream.
     * 
     */
    public static byte[] readAll(InputStream inputStream) throws IOException {

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	byte[] buffer = new byte[1024];

	int k = 0;
	for (; (k = inputStream.read(buffer)) != -1;) {
	    outputStream.write(buffer, 0, k);
	}

	return outputStream.toByteArray();
    }

    public static String bytesToHex(byte[] bytes) {
	final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	char[] hexChars = new char[bytes.length * 2];
	int v;
	for (int j = 0; j < bytes.length; j++) {
	    v = bytes[j] & 0xFF;
	    hexChars[j * 2] = hexArray[v >>> 4];
	    hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	}
	return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
	int len = s.length();
	byte[] data = new byte[len / 2];
	for (int i = 0; i < len; i += 2) {
	    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
	}
	return data;
    }

    public static Scheme getMockedScheme() throws NoSuchAlgorithmException, KeyManagementException {
	SSLContext sslcontext = SSLContext.getInstance("TLS");

	sslcontext.init(null, new TrustManager[] { new DummyX509TrustManager() }, null);
	SSLSocketFactory sf = new SSLSocketFactory(sslcontext);
	Scheme https = new Scheme("https", 443, sf);

	return https;
    }

    /**
     * Generates android checkin request with properties of "Galaxy S3".
     * 
     * <a href=
     * "http://www.glbenchmark.com/phonedetails.jsp?benchmark=glpro25&D=Samsung+GT-I9300+Galaxy+S+III&testgroup=system"
     * > http://www.glbenchmark.com/phonedetails.jsp?benchmark=glpro25&D=Samsung
     * +GT-I9300+Galaxy+S+III&testgroup=system </a>
     */
    public static AndroidCheckinRequest generateAndroidCheckinRequest() {

	return AndroidCheckinRequest
		.newBuilder()
		.setId(0)
		.setCheckin(
			AndroidCheckinProto
				.newBuilder()
				.setBuild(
					AndroidBuildProto.newBuilder()
						.setId("samsung/m0xx/m0:4.0.4/IMM76D/I9300XXALF2:user/release-keys")
						.setProduct("smdk4x12").setCarrier("Google").setRadio("I9300XXALF2")
						.setBootloader("PRIMELA03").setClient("android-google")
						.setTimestamp(new Date().getTime() / 1000).setGoogleServices(16).setDevice("m0")
						.setSdkVersion(21).setModel("GT-I9300").setManufacturer("Samsung")
						.setBuildProduct("m0xx").setOtaInstalled(false)).setLastCheckinMsec(0)
				.setCellOperator("310260").setSimOperator("310260").setRoaming("mobile-notroaming")
				.setUserNumber(0)).setLocale("en_US").setTimeZone("Europe/Istanbul").setVersion(3)
		.setDeviceConfiguration(getDeviceConfigurationProto()).setFragment(0).build();
    }
    
    /*
     *  i9500 first checkin 
     */
    public static AndroidCheckinRequest i9500generateAndroidCheckinRequest(boolean manual) {

    	TimeMsec = new Date().getTime();
    	BigInteger bi = new BigInteger("17137588275979357708"); 
    	LoggingId = bi.longValue();
    	MacAddr = generateMacAddr();
    	SerialNumber = generateSerialNumber();
    	
    	if (manual) {
    		return AndroidCheckinRequest
    				.newBuilder()
    				.setId(4068046457402367336L)
    				.setDigest("Hg4zHRZMkHXVIEbFiWwryQ==")
    				.setCheckin(
    					AndroidCheckinProto
    						.newBuilder()
    						.setBuild(
    							AndroidBuildProto.newBuilder()
    								.setId("samsung/ja3gzs/ja3g:4.4.2/KOT49H/I9500ZSUDNF2:user/release-keys")
    								.setProduct("unknown")
    								.setCarrier("samsung")
    								.setBootloader("unknown")
    								.setClient("android-samsung")
    								.setTimestamp(1343731608)
    								.setGoogleServices(6599070)
    								.setDevice("ja3g")
    								.setSdkVersion(15)
    								.setModel("GT-I9500")
    								.setManufacturer("samsung")
    								.setBuildProduct("ja3qzs")
    								.setOtaInstalled(false))
    						.setLastCheckinMsec(0)
    						.addEvent(AndroidEventProto.newBuilder()    
    								.setTag("event_log_start")    
    								.setTimeMsec(TimeMsec))
		    				.setCellOperator("46601")
		    				.setSimOperator("46601")
		    				.setRoaming("WIFI::")
		    				.setUserNumber(0))      
    				.setLocale("zh_CN")
    				.setLoggingId(LoggingId)
    				.addMacAddr(MacAddr)
    				.setMeid(Utils.txtIMEI)
    				.setTimeZone("GMT")  
    				.setSecurityToken(857322519027675233L)
    				.setVersion(2)
    				.addOtaCert("EpM08UR7lB0NqY+TM3E69Yk2Rsk=")
    				.setSerialNumber("C1FB6B2D18544B0A")
    				.setDeviceConfiguration(i9500getDeviceConfigurationProto())
    				.addMacAddrType("wifi")
    				.setFragment(0)
    				.setUserSerialNumber(0)
    				.build();	
    	} else return generateAndroidCheckinRequest();
    }
    
    /*
     * i9500 第二次 checkin
     */
    public static AndroidCheckinRequest i9500generateAndroidCheckinRequest_last(boolean manual) {

    	if (manual) {
    		return AndroidCheckinRequest
    				.newBuilder()
    				.setId(4068046457402367336L)
    				.setCheckin(
    					AndroidCheckinProto
    						.newBuilder()
    						.setBuild(
    							AndroidBuildProto.newBuilder()
    								.setId("samsung/ja3gzs/ja3g:4.4.2/KOT49H/I9500ZSUDNF2:user/release-keys")
    								.setProduct("unknown")
    								.setCarrier("samsung")
    								.setBootloader("unknown")
    								.setClient("android-samsung")
    								.setTimestamp(1343731608)
    								.setGoogleServices(6599070)
    								.setDevice("ja3g")
    								.setSdkVersion(15)
    								.setModel("GT-I9500")
    								.setManufacturer("samsung")
    								.setBuildProduct("ja3qzs")
    								.setOtaInstalled(false))
    						.setLastCheckinMsec(TimeMsec)
		    				.setCellOperator("46601")
		    				.setSimOperator("46601")
		    				.setRoaming("WIFI::")
		    				.setUserNumber(0))      
    				.setLocale("zh_CN")
    				.setLoggingId(LoggingId)
    				.addMacAddr(MacAddr)
    				.setMeid(Utils.txtIMEI)
    				.setTimeZone("GMT")  
    				.setSecurityToken(857322519027675233L)
    				.setVersion(2)
    				.addOtaCert("EpM08UR7lB0NqY+TM3E69Yk2Rsk=")
    				.setSerialNumber("C1FB6B2D18544B0A")
    				.setDeviceConfiguration(i9500getDeviceConfigurationProto())
    				.addMacAddrType("wifi")
    				.setFragment(0)
    				.setUserSerialNumber(0)
    				.build();	
    	} else return generateAndroidCheckinRequest();
    }
    
    /*
     *  全新Android first checkin 
     */
    public static AndroidCheckinRequest generateAndroidCheckinRequest(boolean manual) {

    	TimeMsec = new Date().getTime();
    	//LoggingId = rand.nextLong();
    	BigInteger bi = new BigInteger("17137588275979357708"); 
    	LoggingId = bi.longValue();
    	MacAddr = generateMacAddr();
    	SerialNumber = generateSerialNumber();
    	
    	if (manual) {
    		return AndroidCheckinRequest
    				.newBuilder()
    				.setId(0)
    				.setCheckin(
    					AndroidCheckinProto
    						.newBuilder()
    						.setBuild(
    							AndroidBuildProto.newBuilder()
    								.setId(String.format(
    										"%s/%s/%s:%s/%s/%s:user/release-keys", 
    										txtManufacture, 
    										txtProductName,
    										txtProductDevice,
    										txtAndroidVersion,
    										txtBuildId,
    										txtBuildVersionIncremental))
    								.setProduct("unknown")
    								.setCarrier(txtManufacture)
    								.setBootloader("unknown")
    								.setClient("android-google")
    								.setTimestamp(new Date().getTime() / 1000)
    								.setGoogleServices(7899036).setDevice(txtProductDevice)
    								.setSdkVersion(Integer.parseInt(Utils.txtSDKVersion))
    								.setModel(txtProductModel).setManufacturer(txtManufacture)
    								.setBuildProduct(txtProductName).setOtaInstalled(false))
    						.setLastCheckinMsec(0)
    						.addEvent(AndroidEventProto.newBuilder()    
    								.setTag("event_log_start")    
    								.setTimeMsec(TimeMsec))
		    				.setCellOperator("46601")
		    				.setSimOperator("46601")
		    				.setRoaming("WIFI::")
		    				.setUserNumber(0))      
    				.setLocale("zh_CN")
    				.setLoggingId(LoggingId)
    				.addMacAddr(MacAddr)
    				.setMeid(Utils.txtIMEI)
    				.setTimeZone("GMT")  
    				.setSecurityToken(857322519027675233L)
    				.setVersion(2)
    				.addOtaCert("EpM08UR7lB0NqY+TM3E69Yk2Rsk=")
    				.setSerialNumber("C1FB6B2D18544B0A")
    				.setDeviceConfiguration(getDeviceConfigurationProto())
    				.addMacAddrType("wifi")
    				.setFragment(0)
    				.setUserSerialNumber(0)
    				.build();	
    	} else return generateAndroidCheckinRequest();
    }
    
    /*
     * 全新 Android 第二次 checkin
     */
    public static AndroidCheckinRequest generateAndroidCheckinRequest_last(boolean manual) {

    	if (manual) {
    		return AndroidCheckinRequest
    				.newBuilder()
    				.setId(0)
    				.setCheckin(
    					AndroidCheckinProto
    						.newBuilder()
    						.setBuild(
    							AndroidBuildProto.newBuilder()
    								.setId(String.format(
    										"%s/%s/%s:%s/%s/%s:user/release-keys", 
    										txtManufacture, 
    										txtProductName,
    										txtProductDevice,
    										txtAndroidVersion,
    										txtBuildId,
    										txtBuildVersionIncremental))
    								.setProduct(txtProductBoard)
    								.setRadio(txtBuildVersionIncremental)
    								.setCarrier(txtManufacture)
    								.setBootloader("unknown")
    								.setClient("android-google")
    								.setTimestamp(new Date().getTime() / 1000)
    								.setGoogleServices(7899036).setDevice(txtProductDevice)
    								.setSdkVersion(Integer.parseInt(Utils.txtSDKVersion))
    								.setModel(txtProductModel).setManufacturer(txtManufacture)
    								.setBuildProduct(txtProductName).setOtaInstalled(false))
    						.setLastCheckinMsec(TimeMsec)
		    						.setCellOperator(txtCellSimOperator)
		    						.setSimOperator(txtCellSimOperator)
		    						.setRoaming("mobile-notroaming")
		    						.setUserNumber(0))      
    				.setLocale(txtLocale)
    				.setLoggingId(LoggingId)
    				.addMacAddr(MacAddr)
    				.setMeid(Utils.txtIMEI)
    				.setTimeZone("Asia/Taipei")              
    				.setVersion(3)
    				.setSerialNumber(SerialNumber)
    				.setDeviceConfiguration(getDeviceConfigurationProto())
    				.addMacAddrType("wifi")
    				.setFragment(0).build();	
    	} else return generateAndroidCheckinRequest();
    }
   /* 
    public static AndroidCheckinRequest generateAndroidCheckinRequest(boolean manual) {

    	if (manual) {
    		return AndroidCheckinRequest
    				.newBuilder()
    				.setId(0)
    				.setDigest("a1PPnDfPObkrz3cIrtTGLA==")
    				.setCheckin(
    					AndroidCheckinProto
    						.newBuilder()
    						.setBuild(
    							AndroidBuildProto.newBuilder()
    								.setId(String.format(
    										"%s/%s/%s:%s/%s/%s:user/release-keys", 
    										txtManufacture, 
    										txtProductName,
    										txtProductDevice,
    										txtAndroidVersion,
    										txtBuildId,
    										txtBuildVersionIncremental))
    								.setProduct(txtProductBoard)
    								.setCarrier("Google")
    								.setRadio(txtBuildVersionIncremental)
    								.setBootloader("PRIMELA03")
    								.setClient("android-google")
    								.setTimestamp(new Date().getTime() / 1000)
    								.setGoogleServices(6599070).setDevice(txtProductDevice)
    								.setSdkVersion(Integer.parseInt(Utils.txtSDKVersion))
    								.setModel(txtProductModel)
    								.setManufacturer(txtManufacture)
    								.setBuildProduct(txtProductName)
    								.setOtaInstalled(false))
    						.setLastCheckinMsec(0)
    						.addEvent(AndroidEventProto.newBuilder()
    			                    .setTag("event_log_start")
    			                    .setTimeMsec(new Date().getTime()))
		    						.setCellOperator(txtCellSimOperator)
		    						.setSimOperator(txtCellSimOperator)
		    						.setRoaming("WIFI::")
		    						.setUserNumber(0))
    				.setLocale(txtLocale)
    				.setLoggingId(rand.nextLong())
    				.addMacAddr(generateMacAddr())
    				.setMeid(txtIMEI)
    				.setTimeZone(txtTimeZone)
    				.setVersion(3)
    				.addOtaCert("EpM08UR7lB0NqY+TM3E69Yk2Rsk=")
		    		.setSerialNumber(generateSerialNumber())
		    		.setDeviceConfiguration(getDeviceConfigurationProto())
		    		.addMacAddrType("wifi")
		    		.setFragment(0).build();	
    	} else return generateAndroidCheckinRequest();
    }
*/    
    /**
     * GT-I9505
     * 
     */
    public static AndroidCheckinRequest generateAndroidCheckinRequest2(boolean manual, String Digest, long time, String SerialNumber, String MacAddr, long LoggingId) {
    	
    	
    	if (manual) {
    		return AndroidCheckinRequest
    				.newBuilder()
    				.setId(new BigInteger("31F7010511FDE31E", 16).longValue())
    				.setDigest(Digest)
    				.setCheckin(
    					AndroidCheckinProto
    						.newBuilder()
    						.setBuild(
    							AndroidBuildProto.newBuilder()
    								.setId("samsung/GT-I9505/GT-I9505:4.0.4/IMM76D/I9505XXBLH1:user/release-keys")
    								.setProduct("unknown")
    								.setCarrier("samsung")
    								.setBootloader("unknown")
    								.setClient("android-samsung")
    								.setTimestamp(1343731608)//(new Date().getTime() / 1000)
    								.setGoogleServices(15)
    								.setDevice("GT-I9505")
    								.setSdkVersion(15)
    								.setModel("GT-I9505")
    								.setManufacturer("samsung")
    								.setBuildProduct("GT-I9505"))
    						.setLastCheckinMsec(new BigInteger("1440657122982").longValue())
    						//.addEvent(AndroidEventProto.newBuilder()
    			            //        .setTag("event_log_start")
    			            //        .setTimeMsec(new Date().getTime()))
		    						.setCellOperator(txtCellSimOperator)
		    						.setSimOperator(txtCellSimOperator)
		    						.setRoaming("mobile-notroaming"))
    				.setLocale(txtLocale)
    				.setLoggingId(new BigInteger("48618CADB267A4B4", 16).longValue())
    				.addMacAddr(generateMacAddr())
    				.setMeid(txtIMEI)
    				.setTimeZone(txtTimeZone)
    				.setSecurityToken(new BigInteger("02b9be724a389e92", 16).longValue())
    				.setVersion(2)
    				.addOtaCert("EpM08UR7lB0NqY+TM3E69Yk2Rsk=")
		    		.setSerialNumber("763DDDB572F84BA6")
		    		.setDeviceConfiguration(getDeviceConfigurationProto())
		    		.build();	
    	} else return generateAndroidCheckinRequest();
    }
    
    /**
     * GT-I9300 generate AndroidEventProto LOG事件
     * 
     */
    public static AndroidEventProto generateAndroidEventProto(){
    	return AndroidEventProto
    			.newBuilder()
    			.setTag("event_log_start")
    			.build();
    }
    
    /**
     * GT-I9300 generate AndroidBuildProto 設備資訊
     * 
     */
    public static AndroidCheckinProto generateAndroidCheckinProto(){
    	return AndroidCheckinProto
    			.newBuilder()
    			.setBuild(AndroidBuildProto.newBuilder()
						.setId("samsung/m0xx/m0:4.0.4/IMM76D/I9300XXBLH1:user/release-keys")
						.setProduct("unknown")
						.setCarrier("samsung")
						.setBootloader("unknown")
						.setClient("android-samsung")
						.setTimestamp(1343731608)//(new Date().getTime() / 1000)
						.setGoogleServices(6599070)
						.setDevice("m0")
						.setSdkVersion(15)
						.setModel("GT-I9300")
						.setManufacturer("samsung")
						.setBuildProduct("m0xx")
						.setOtaInstalled(false))
    			.setCellOperator(txtCellSimOperator)
				.setSimOperator(txtCellSimOperator)
				.setRoaming("WIFI::")
				.setUserNumber(0)
    			.build();
    }
    
    /**
     * GT-I9300
     * 
     */
    public static AndroidCheckinRequest generateAndroidCheckinRequest3(boolean manual, String Digest, long time, String SerialNumber2, String MacAddr, long LoggingId2) {
    	
    	String GSFID = "3FB77357BE91C3AD";//i9300_GSFID
		String SecurityToken = "6de09da78cf11f6c";//i9300 SecurityToken
		String OtaCert = "EpM08UR7lB0NqY+TM3E69Yk2Rsk=";//i9300 OtaCert
		String SerialNumber = "C1FB6B2D18544B0A";//i9300 SerialNumber
		String LoggingId = "71AF72067D8DE9EA";//i9300 LoggingId
		
    	if (manual) {
    		return AndroidCheckinRequest
    				.newBuilder()
    				.setId(new BigInteger(GSFID, 16).longValue())
    				.setDigest(Digest)
    				.setLocale(txtLocale)
    				.setLoggingId(new BigInteger(LoggingId, 16).longValue())
    				.addMacAddr(generateMacAddr())
    				.setMeid(txtIMEI)
    				.setTimeZone(txtTimeZone)
    				.setSecurityToken(new BigInteger(SecurityToken, 16).longValue())
    				.setVersion(2)
    				.addOtaCert(OtaCert)
		    		.setSerialNumber(SerialNumber)
		    		.setDeviceConfiguration(getDeviceConfigurationProto())
		    		.addMacAddrType("wifi")
		    		.setFragment(0)
		    		.build();	
    	} else return generateAndroidCheckinRequest();
    }
    
    /**
     * GT-I9300
     * 
     */
    /*
    public static AndroidCheckinRequest generateAndroidCheckinRequest3(boolean manual, String Digest, long time, String SerialNumber, String MacAddr, long LoggingId) {
    	
    	
    	if (manual) {
    		return AndroidCheckinRequest
    				.newBuilder()
    				.setImei("1")
    				.setId(new BigInteger("3FB77357BE91C3AD", 16).longValue())
    				.setDigest(Digest)
    				.setCheckin(
    					AndroidCheckinProto
    						.newBuilder()
    						.setBuild(
    							AndroidBuildProto.newBuilder()
    								.setId("samsung/m0xx/m0:4.0.4/IMM76D/I9300XXBLH1:user/release-keys")
    								.setProduct("unknown")
    								.setCarrier("samsung")
    								.setBootloader("unknown")
    								.setClient("android-samsung")
    								.setTimestamp(1343731608)//(new Date().getTime() / 1000)
    								.setGoogleServices(6599070)
    								.setDevice("m0")
    								.setSdkVersion(15)
    								.setModel("GT-I9300")
    								.setManufacturer("samsung")
    								.setBuildProduct("m0xx")
    								.setOtaInstalled(false))
    						.setLastCheckinMsec(new BigInteger("1439274780644").longValue())
    						//.addEvent(AndroidEventProto.newBuilder()
    			            //        .setTag("event_log_start")
    			            //        .setTimeMsec(new Date().getTime()))
		    						.setCellOperator(txtCellSimOperator)
		    						.setSimOperator(txtCellSimOperator)
		    						.setRoaming("WIFI::")
		    						.setUserNumber(0)
		    						)
    				.setLocale(txtLocale)
    				.setLoggingId(new BigInteger("71AF72067D8DE9EA", 16).longValue())
    				.addMacAddr(generateMacAddr())
    				.setMeid(txtIMEI)
    				.setTimeZone(txtTimeZone)
    				.setSecurityToken(new BigInteger("6de09da78cf11f6c", 16).longValue())
    				.setVersion(2)
    				.addOtaCert("EpM08UR7lB0NqY+TM3E69Yk2Rsk=")
		    		.setSerialNumber("C1FB6B2D18544B0A")
		    		.setDeviceConfiguration(getDeviceConfigurationProto())
		    		.addMacAddrType("wifi")
		    		.setFragment(0)
		    		.build();	
    	} else return generateAndroidCheckinRequest();
    }
    */
    
    //i9500
    public static DeviceConfigurationProto i9500getDeviceConfigurationProto() {
    	return DeviceConfigurationProto
    		.newBuilder()
    		.setTouchScreen(3)
    		.setKeyboard(2)
    		.setNavigation(1)
    		.setScreenLayout(3)
    		.setHasHardKeyboard(true)
    		.setHasFiveWayNavigation(false)
    		.setScreenDensity(160)
    		.setGlEsVersion(131072)
    		.addAllSystemSharedLibrary(
    			Arrays.asList("android.test.runner", "com.android.future.usb.accessory", "com.android.location.provider",
    			    "com.google.android.maps", "com.google.android.media.effects",
    				"com.google.widevine.software.drm", "javax.obex"))
    		.addAllSystemAvailableFeature(
    			Arrays.asList("android.hardware.audio.low_latency", 
    				"android.hardware.bluetooth", "android.hardware.camera",
    				"android.hardware.camera.autofocus", "android.hardware.camera.flash",
    				"android.hardware.camera.front", "android.hardware.faketouch", "android.hardware.location",
    				"android.hardware.location.gps", "android.hardware.location.network",
    				"android.hardware.microphone", "android.hardware.screen.landscape",
    				"android.hardware.screen.portrait", "android.hardware.sensor.accelerometer",
    				"android.hardware.sensor.compass",
    				"android.hardware.sensor.gyroscope",
    				"android.hardware.telephony",
    				"android.hardware.telephony.gsm", "android.hardware.touchscreen",
    				"android.hardware.touchscreen.multitouch", "android.hardware.touchscreen.multitouch.distinct",
    				"android.hardware.touchscreen.multitouch.jazzhand", "android.hardware.usb.accessory",
    				"android.hardware.usb.host", "android.hardware.wifi", "android.hardware.wifi.direct",
    				"android.software.live_wallpaper", "android.software.sip", "android.software.sip.voip",
    				"com.google.android.feature.GOOGLE_BUILD"))
    		.addAllNativePlatform(Arrays.asList("armeabi-v7a", "armeabi"))
    		.setScreenWidth(1066)
    		.setScreenHeight(552)
    		.addAllSystemSupportedLocale(
    			Arrays.asList("af", "af_ZA", "am", "am_ET", "ar", "ar_EG", "bg", "bg_BG", "ca", "ca_ES", "cs", "cs_CZ",
    				"da", "da_DK", "de", "de_AT", "de_CH", "de_DE", "de_LI", "el", "el_GR", "en", "en_AU", "en_CA",
    				"en_GB", "en_NZ", "en_SG", "en_US", "es", "es_ES", "es_US", "fa", "fa_IR", "fi", "fi_FI", "fr",
    				"fr_BE", "fr_CA", "fr_CH", "fr_FR", "hi", "hi_IN", "hr", "hr_HR", "hu", "hu_HU", "in", "in_ID",
    				"it", "it_CH", "it_IT", "iw", "iw_IL", "ja", "ja_JP", "ko", "ko_KR", "lt", "lt_LT", "lv",
    				"lv_LV", "ms", "ms_MY", "nb", "nb_NO", "nl", "nl_BE", "nl_NL", "pl", "pl_PL", "pt", "pt_BR",
    				"pt_PT", "rm", "rm_CH", "ro", "ro_RO", "ru", "ru_RU", "sk", "sk_SK", "sl", "sl_SI", "sr",
    				"sr_RS", "sv", "sv_SE", "sw", "sw_TZ", "th", "th_TH", "tl", "tl_PH", "tr", "tr_TR", "ug",
    				"ug_CN", "uk", "uk_UA", "vi", "vi_VN", "zh_CN", "zh_TW", "zu", "zu_ZA"))
    		.addAllGlExtension(
    			Arrays.asList("GL_APPLE_texture_format_BGRA8888", "GL_ARB_texture_non_power_of_two",
    					"GL_EXT_texture_format_BGRA8888", "GL_OES_EGL_image",
    					"GL_OES_blend_equation_separate", "GL_OES_blend_func_separate",
    					"GL_OES_blend_subtract", "GL_OES_byte_coordinates",
    					"GL_OES_compressed_ETC1_RGB8_texture", "GL_OES_compressed_paletted_texture",
    					"GL_OES_depth24", "GL_OES_depth32",
    					"GL_OES_draw_texture", "GL_OES_element_index_uint",
    					"GL_OES_fbo_render_mipmap", "GL_OES_framebuffer_object",
    					"GL_OES_packed_depth_stencil", "GL_OES_point_size_array",
    					"GL_OES_point_sprite", "GL_OES_rgb8_rgba8",
    					"GL_OES_single_precision", "GL_OES_stencil1",
    					"GL_OES_stencil4", "GL_OES_stencil8",
    					"GL_OES_stencil_wrap", "GL_OES_texture_cube_map",
    					"GL_OES_texture_env_crossbar", "GL_OES_texture_mirrored_repeat")).build();
        }
    
    //原始
    public static DeviceConfigurationProto getDeviceConfigurationProto() {
	return DeviceConfigurationProto
		.newBuilder()
		.setTouchScreen(3)
		.setKeyboard(1)
		.setNavigation(1)
		.setScreenLayout(2)
		.setHasHardKeyboard(false)
		.setHasFiveWayNavigation(false)
		.setScreenDensity(320)
		.setGlEsVersion(131072)
		.addAllSystemSharedLibrary(
			Arrays.asList("android.test.runner", "com.android.future.usb.accessory", "com.android.location.provider",
				"com.android.nfc_extras", "com.google.android.maps", "com.google.android.media.effects",
				"com.google.widevine.software.drm", "javax.obex"))
		.addAllSystemAvailableFeature(
			Arrays.asList("android.hardware.bluetooth", "android.hardware.camera",
				"android.hardware.camera.autofocus", "android.hardware.camera.flash",
				"android.hardware.camera.front", "android.hardware.faketouch", "android.hardware.location",
				"android.hardware.location.gps", "android.hardware.location.network",
				"android.hardware.microphone", "android.hardware.nfc", "android.hardware.screen.landscape",
				"android.hardware.screen.portrait", "android.hardware.sensor.accelerometer",
				"android.hardware.sensor.barometer", "android.hardware.sensor.compass",
				"android.hardware.sensor.gyroscope", "android.hardware.sensor.light",
				"android.hardware.sensor.proximity", "android.hardware.telephony",
				"android.hardware.telephony.gsm", "android.hardware.touchscreen",
				"android.hardware.touchscreen.multitouch", "android.hardware.touchscreen.multitouch.distinct",
				"android.hardware.touchscreen.multitouch.jazzhand", "android.hardware.usb.accessory",
				"android.hardware.usb.host", "android.hardware.wifi", "android.hardware.wifi.direct",
				"android.software.live_wallpaper", "android.software.sip", "android.software.sip.voip",
				"com.cyanogenmod.android", "com.cyanogenmod.nfc.enhanced",
				"com.google.android.feature.GOOGLE_BUILD", "com.nxp.mifare", "com.tmobile.software.themes"))
		.addAllNativePlatform(Arrays.asList("armeabi-v7a", "armeabi"))
		.setScreenWidth(720)
		.setScreenHeight(1184)
		.addAllSystemSupportedLocale(
			Arrays.asList("af", "af_ZA", "am", "am_ET", "ar", "ar_EG", "bg", "bg_BG", "ca", "ca_ES", "cs", "cs_CZ",
				"da", "da_DK", "de", "de_AT", "de_CH", "de_DE", "de_LI", "el", "el_GR", "en", "en_AU", "en_CA",
				"en_GB", "en_NZ", "en_SG", "en_US", "es", "es_ES", "es_US", "fa", "fa_IR", "fi", "fi_FI", "fr",
				"fr_BE", "fr_CA", "fr_CH", "fr_FR", "hi", "hi_IN", "hr", "hr_HR", "hu", "hu_HU", "in", "in_ID",
				"it", "it_CH", "it_IT", "iw", "iw_IL", "ja", "ja_JP", "ko", "ko_KR", "lt", "lt_LT", "lv",
				"lv_LV", "ms", "ms_MY", "nb", "nb_NO", "nl", "nl_BE", "nl_NL", "pl", "pl_PL", "pt", "pt_BR",
				"pt_PT", "rm", "rm_CH", "ro", "ro_RO", "ru", "ru_RU", "sk", "sk_SK", "sl", "sl_SI", "sr",
				"sr_RS", "sv", "sv_SE", "sw", "sw_TZ", "th", "th_TH", "tl", "tl_PH", "tr", "tr_TR", "ug",
				"ug_CN", "uk", "uk_UA", "vi", "vi_VN", "zh_CN", "zh_TW", "zu", "zu_ZA"))
		.addAllGlExtension(
			Arrays.asList("GL_EXT_debug_marker", "GL_EXT_discard_framebuffer", "GL_EXT_multi_draw_arrays",
				"GL_EXT_shader_texture_lod", "GL_EXT_texture_format_BGRA8888",
				"GL_IMG_multisampled_render_to_texture", "GL_IMG_program_binary", "GL_IMG_read_format",
				"GL_IMG_shader_binary", "GL_IMG_texture_compression_pvrtc", "GL_IMG_texture_format_BGRA8888",
				"GL_IMG_texture_npot", "GL_IMG_vertex_array_object", "GL_OES_EGL_image",
				"GL_OES_EGL_image_external", "GL_OES_blend_equation_separate", "GL_OES_blend_func_separate",
				"GL_OES_blend_subtract", "GL_OES_byte_coordinates", "GL_OES_compressed_ETC1_RGB8_texture",
				"GL_OES_compressed_paletted_texture", "GL_OES_depth24", "GL_OES_depth_texture",
				"GL_OES_draw_texture", "GL_OES_egl_sync", "GL_OES_element_index_uint",
				"GL_OES_extended_matrix_palette", "GL_OES_fixed_point", "GL_OES_fragment_precision_high",
				"GL_OES_framebuffer_object", "GL_OES_get_program_binary", "GL_OES_mapbuffer",
				"GL_OES_matrix_get", "GL_OES_matrix_palette", "GL_OES_packed_depth_stencil",
				"GL_OES_point_size_array", "GL_OES_point_sprite", "GL_OES_query_matrix", "GL_OES_read_format",
				"GL_OES_required_internalformat", "GL_OES_rgb8_rgba8", "GL_OES_single_precision",
				"GL_OES_standard_derivatives", "GL_OES_stencil8", "GL_OES_stencil_wrap",
				"GL_OES_texture_cube_map", "GL_OES_texture_env_crossbar", "GL_OES_texture_float",
				"GL_OES_texture_half_float", "GL_OES_texture_mirrored_repeat", "GL_OES_vertex_array_object",
				"GL_OES_vertex_half_float")).build();
    }
}
