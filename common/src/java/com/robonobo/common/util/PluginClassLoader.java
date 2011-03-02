package com.robonobo.common.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * To use this as the default classloader, set the system property:
 * java.system.class.loader=com.robonobo.common.util.PluginClassLoader
 * as a jvm parameter.
 * To retrieve it at runtime:
 * ClassLoader cl = ClassLoader.getSystemClassLoader();
 * if(cl instanceof PluginClassLoader) {
 * 	((PluginClassLoader)cl).addPluginDir(pluginDir);
 * }
 * @author macavity
 *
 */
public class PluginClassLoader extends URLClassLoader {
	Log log = LogFactory.getLog(PluginClassLoader.class);
	
	public PluginClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
	}
	
	/**
	 * Makes classes and resources in every jar file in pluginDir available to this jvm
	 * @param pluginDir
	 */
	public void addPluginDir(File pluginDir) throws IOException {
		for (File file : pluginDir.listFiles()) {
			if(file.getName().endsWith(".jar"))
				addURL(file.toURI().toURL());
		}
	}
}
