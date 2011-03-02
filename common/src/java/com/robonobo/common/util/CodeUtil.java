package com.robonobo.common.util;

public class CodeUtil {
	public static int javaMajorVersion() {
		String fullVersion = System.getProperty("java.version");
		String[] vNums = fullVersion.split("\\.");
		return Integer.parseInt(vNums[1]);
	}

	public static String friendlyClassName(Class<?> clazz) {
		String fqClass = clazz.getName();
		return fqClass.substring(fqClass.lastIndexOf('.')+1);
	}
}
