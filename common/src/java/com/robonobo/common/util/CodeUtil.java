package com.robonobo.common.util;

import static com.robonobo.common.util.TextUtil.*;

import java.lang.reflect.Method;

import com.robonobo.common.exceptions.SeekInnerCalmException;

public class CodeUtil {
	public static int javaMajorVersion() {
		String fullVersion = System.getProperty("java.version");
		String[] vNums = fullVersion.split("\\.");
		return Integer.parseInt(vNums[1]);
	}

	public static String shortClassName(Class<?> clazz) {
		String fqClass = clazz.getName();
		return fqClass.substring(fqClass.lastIndexOf('.') + 1);
	}

	public static void setBeanProperty(Object obj, String propName, String value) {
		try {
			Method m = obj.getClass().getDeclaredMethod("set" + capitalizeFirst(propName), String.class);
			m.invoke(obj, value);
		} catch (Exception e) {
			throw new SeekInnerCalmException(e);
		}
	}
	
	public static void setBeanProperty(Object obj, String propName, boolean value) {
		try {
			Method m = obj.getClass().getDeclaredMethod("set" + capitalizeFirst(propName), boolean.class);
			m.invoke(obj, value);
		} catch (Exception e) {
			throw new SeekInnerCalmException(e);
		}
	}
}
