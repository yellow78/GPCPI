package com.niuniucpi.woker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.niuniucpi.CPIUtils;

public class ProtobufParser {

	private Map<Object, Object> mProperties = new HashMap<Object, Object>();
	
	public Map<Object, Object> getProperties() {
		return mProperties;
	}
	
	private int mCurrentIndex = -1;
	
	private byte[] originalBytes;
	
	private String mResult;
	
	public String getResult() {
		return mResult;
	}
	
	public ProtobufParser(byte[] bytes) throws Exception {
		/*
		originalBytes = bytes;
		mCurrentIndex = 0;
		while (mCurrentIndex < bytes.length) {			
			byte type = (byte)(bytes[mCurrentIndex] & 7);
			byte id = (byte)((bytes[mCurrentIndex] & 120) >> 3);
			switch (type) {
			case 0:
				mCurrentIndex++;				
				long buffer0 = readLong();
				mProperties.put(String.valueOf(id), String.valueOf(buffer0));
				break;
			case 1:
				mCurrentIndex++;				
				long length1 = readLong();
				byte[] buffer1 = new byte[(int)length1];
				System.arraycopy(originalBytes, mCurrentIndex, buffer1, 0, (int)length1);
				try {
					mProperties.put(String.valueOf(id), new ProtobufParser(buffer1));
				} catch (Exception e) { }
				mCurrentIndex += length1;
				break;
			case 2:
				mCurrentIndex++;
				long length2 = readLong();
				byte[] buffer2 = new byte[(int)length2];
				System.arraycopy(originalBytes, mCurrentIndex, buffer2, 0, (int)length2);
				try {
					mProperties.put(String.valueOf(id), new ProtobufParser(buffer2));
				} catch (Exception e) { 
					mProperties.put(String.valueOf(id), buffer2);
					mCurrentIndex += length2;
				}
				break;
			case 3:
				break;
			case 4:
				break;
			case 5:
				break;
			default:
				throw new Exception();
			}	
		}
		*/
		File file = new File("buffer.dat");
		if (!file.exists()) file.createNewFile();
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(bytes);
		stream.flush();
		stream.close();
		Runtime rt = Runtime.getRuntime();
		ProcessBuilder builder = new ProcessBuilder("protoc", "--decode_raw");
		builder.redirectInput(new File("buffer.dat"));
		builder.redirectOutput(new File("buffer.txt"));
		Process p = builder.start();
		p.waitFor();
		file = new File("buffer.txt");
		if (file.exists()) {
			FileInputStream reader = new FileInputStream(file);
			byte[] buffer = IOUtils.toByteArray(reader);
			mResult = new String(buffer, "ASCII");
		}
	}
	
	private long readLong() {
		byte[] bytes = readValueBytes();
		return (bytes.length > 1) ? (int)CPIUtils.to8bitsLong(bytes) : bytes[0] & 0xff;
	}
	
	private byte[] readValueBytes() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte b = originalBytes[mCurrentIndex++];
		stream.write(b & 0xff);
		while ((b & (byte)0x80) == (byte)0x80) {
			b = originalBytes[mCurrentIndex++];
			stream.write(b & 0xff);
		}
		return stream.toByteArray();
	}
	
}
