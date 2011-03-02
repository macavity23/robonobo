package com.robonobo.mina;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.mina.external.Application;
import com.robonobo.mina.external.MinaConfig;
import com.robonobo.mina.external.MinaControl;
import com.robonobo.mina.instance.MinaInstance;

public class Mina {
	public static MinaControl newInstance(MinaConfig config, Application app, ScheduledThreadPoolExecutor executor) {
		return new MinaInstance(config, app, executor);
	}
}
