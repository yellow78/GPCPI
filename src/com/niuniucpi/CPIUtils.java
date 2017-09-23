package com.niuniucpi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;

import com.akdeniz.googleplaycrawler.GooglePlayException;
import com.akdeniz.googleplaycrawler.Utils;

public class CPIUtils {
	
	public static final String ProxyURL = "172.20.239.170";
	public static final int ProxyPORT = 8181;
	
	public static String getHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte __byte : bytes) {
			sb.append(String.format("%02x", __byte & 0xff));
		}
		return sb.toString();
	}
	
	public static byte[] executePOSTResponse(String url, boolean monitorRequired, String[][] headers, byte[] postData) throws GooglePlayException, UnsupportedOperationException, IOException {
		PoolingClientConnectionManager connManager = new PoolingClientConnectionManager(
				SchemeRegistryFactory.createDefault());
		connManager.setMaxTotal(100);
		connManager.setDefaultMaxPerRoute(30);
		HttpClient client = new DefaultHttpClient(connManager);
		try {
			client.getConnectionManager().getSchemeRegistry().register(Utils.getMockedScheme());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (monitorRequired) {			
			HttpHost proxy = new HttpHost(CPIUtils.ProxyURL, CPIUtils.ProxyPORT);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);		
		}
		HttpPost httppost = new HttpPost(url);
		if (headers != null) {
			for (String[] param : headers) {
				if (param[0] != null && param[1] != null) {
					httppost.setHeader(param[0], param[1]);
				}
			}
		}
		httppost.setEntity(new ByteArrayEntity(postData));
		HttpResponse response = client.execute(httppost);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new GooglePlayException(new String(Utils.readAll(response.getEntity().getContent())));
		}
		HttpEntity entity = response.getEntity();
		InputStream stream = entity.getContent();
		return IOUtils.toByteArray(stream);
	}
	
	public static byte[] arrayMerge(byte[]... args) {
		byte[] buffer = null;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			for (int i = 0; i < args.length; i++) {
				stream.write(args[i]);
			}
			buffer = stream.toByteArray();
			stream.close();
		} catch (Exception e) { }		
		return buffer;
	}
	
	public static byte[] makeStartDownloadBytes(String packageName) {
		try {
			String downloadPackageName = "confirmFreeDownload?doc=".concat(packageName);
			byte[] packageBytes = downloadPackageName.getBytes("ASCII");		
			long time = new Date().getTime();
			ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
			buffer.putLong(time);
			byte[] times = to7bits(buffer.array());
			byte length = (byte)(packageBytes.length + times.length + 3);
			return CPIUtils.arrayMerge(new byte[] { makeKey(1, 2) }, new byte[] { length }, new byte[] { makeKey(1, 0) }, times, new byte[] { makeKey(2, 2) }, new byte[] { (byte)packageBytes.length }, packageBytes);
		} catch (Exception e) { }
		return null;
	}
	
	public static byte makeKey(int key, int type) {
		return (byte)((byte)(key << 3) | (byte)type);
	}
	
	public static long to8bitsLong(byte[] array) {
		List<String> lstbuffer = new ArrayList<String>();
		for (int i = array.length- 1; i >= 0; i--) {
			lstbuffer.add(getBin(array[i]).substring(1)); // 去掉高位元並交換
		}
		StringBuilder sb = new StringBuilder();
		for (String bin : lstbuffer) {
			sb.append(bin);
		}
		List<Object> lstvalues = new ArrayList<Object>();
		String sbin = sb.toString();
		for (int i = sbin.length() - 1; i >= 0; i -= 8) {
			String bin = "";
			for (int j = i; j > i - 8; j--) {
				if (j >= 0) {
					bin = sbin.substring(j, j + 1) + bin;
				}
			}
			int value = binString2Byte(bin) & 0xff;
			System.out.print(String.format("%02x=%s,", value, bin));
			lstvalues.add(value);
		}
		System.out.println("");
		long total = 0;
		for (int i = 0; i < lstvalues.size(); i++) {
		//int j = 0;
		//for (int i = lstvalues.size() - 1; i >= 0; i--) {
			int bv = (int)lstvalues.get(i);
			System.out.print(String.format("%02x", bv & 0xff));
			System.out.print("=");
			long value = (long)(bv * Math.pow(256, i));
			//long value = (long)(bv * Math.pow(256, j++));
			System.out.print(String.valueOf(value) + ",");
			total += value;
		}
		return total;
	}
	
	public static byte[] to7bits(byte[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) sb.append(getBin(array[i]));
		List<Object> lstBytes = new ArrayList<Object>();
		String sbin = sb.toString();
		for (int i = sbin.length() - 1; i >= 0; i -= 7) {
			String bin = "";
			for (int j = i; j > i - 7; j--) {
				if (j >= 0) bin = sbin.substring(j, j + 1) + bin;
			}
			lstBytes.add(binString2Byte(bin));
		}
		byte[] bytes = new byte[lstBytes.size()];
		int i = 0;
		for (Object value : lstBytes) {
			bytes[i++] = (byte)value;
		}
		int cuts = 0;
		for (int j = bytes.length - 1; j >= 0; j--) {
			if (bytes[j] == 0) cuts++; else break;
		}
		if (cuts > 0) {
			byte[] newbytes = new byte[bytes.length - cuts];
			System.arraycopy(bytes, 0, newbytes, 0, newbytes.length);
			for (int j = 0; j < newbytes.length - 1; j++) {
				newbytes[j] |= (byte)0x80;
			}
			return newbytes;
		} else {
			for (int j = 0; j < bytes.length - 1; j++) {
				bytes[j] |= (byte)0x80;
			}
			return bytes;
		}
	}
	
	public static String getStringFromBytes(byte[] bytes) {
		return new String(bytes);
	}
	
	public static int getIntFromBytes(byte[] bytes) {
		int value = 0;
		for (int i = 0; i < bytes.length; i++) {
			value += (bytes[i] & 0xff) * (16 ^ i);
		}
		return value;
	}
	
	public static long getLongFromBytes(byte[] bytes) {
		long value = 0;
		for (int i = 0; i < bytes.length; i++) {
			value += (bytes[i] & 0xff) * (16 ^ i);
		}
		return value;
	}
	
	public static byte binString2Byte(String bin) {
		int s = 0;
		int j = 0;
		for (int i = bin.length() - 1; i >= 0; i--) {
			s += Math.pow(2, j++) * Integer.parseInt(bin.substring(i, i + 1));
		}
		return (byte)s;
	}
	
	public static String getBin(byte value) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.valueOf((value & 128) >> 7));
		sb.append(String.valueOf((value & 64) >> 6));
		sb.append(String.valueOf((value & 32) >> 5));
		sb.append(String.valueOf((value & 16) >> 4));
		sb.append(String.valueOf((value & 8) >> 3));
		sb.append(String.valueOf((value & 4) >> 2));
		sb.append(String.valueOf((value & 2) >> 1));
		sb.append(String.valueOf(value & 1));
		return sb.toString();
	}
	
}
