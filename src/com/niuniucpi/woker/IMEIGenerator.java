package com.niuniucpi.woker;

public final class IMEIGenerator {

	public static String getIMEI() {
		int rnd = 0;
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 14) {
			rnd = (int)(Math.random() * 100) + 1;
			if (sb.length() == 0 || (rnd >= 1 && rnd <= 90)) { // not allow 0
				int value = (int)(Math.random() * 8) + 1;
				sb.append(String.valueOf(value));	
			} else {
				int value = (int)(Math.random() * 10);
				sb.append(String.valueOf(value));
			}
			
		}
		// checksum
		String result = sb.toString();
		int dbl = 0;
		for (int i = 0; i < result.length(); i++) {
			if (i % 2 == 1) {
				for (char c : String.valueOf(Integer.parseInt(result.substring(i, i + 1)) * 2).toCharArray()) {
					dbl += Integer.parseInt(String.valueOf(c));
				}	
			} else {
				dbl += Integer.parseInt(result.substring(i, i + 1));
			}
		}
		int div = 10 - (dbl % 10 == 0 ? 10 : dbl % 10);
		sb.append(String.valueOf(div));
		return sb.toString();
	}
	
}
