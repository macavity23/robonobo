package com.robonobo.remote.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.remote.RemoteCall;
import com.robonobo.common.util.CodeUtil;

/**
 * The client end of a connection to a remoting server
 * @author macavity
 */
public abstract class JbossRemotingFacade {
	protected String url;
	protected String secret;
	protected Client client;
	private String myClass;
	protected Log log = LogFactory.getLog(getClass());
	private Thread startupThread;
	
	public JbossRemotingFacade(final String url, final String remoteSubsystem, String secret) {
		this.url = url;
		this.secret = secret;
		myClass = CodeUtil.shortClassName(getClass());
		startupThread = new Thread(new CatchingRunnable() {
			public void doRun() throws Exception {
				while(true) {
					synchronized (JbossRemotingFacade.this) {
						try {
							log.info(myClass+" attempting to connect to "+url);
							InvokerLocator locator = new InvokerLocator(url);
							Client tryClient = new Client(locator, remoteSubsystem);
							tryClient.connect();
							client = tryClient;
							log.info(myClass+" successfully connected to "+url);
							startupThread = null;
							return;
						} catch (Exception e) {
							log.error("Caught exception starting "+myClass+": "+e.getMessage()+" - sleeping 30s");
						}						
					}
					Thread.sleep(30000L);
				}
			}
		}); 
		startupThread.start();
	}
	
	public void stop() {
		if(client != null)
			client.disconnect();
		if(startupThread != null)
			startupThread.interrupt();
	}
	
	protected Object invoke(String methodName, Object arg, List<? extends Object> extraArgs) {
		synchronized (this) {
			if(client == null) {
				log.error(myClass+" not calling remote method "+methodName+": not yet connected");
				return null;
			}
		}
		RemoteCall params = new RemoteCall(secret, methodName, arg);
		if(extraArgs != null)
			params.setExtraArgs(extraArgs);
		try {
			Object result = client.invoke(params);
			return result;
		} catch(Throwable t) {
			// TODO If the connection has failed, restart it
			log.error("Error making remote call to method "+methodName, t);
			return null;
		}
	}

}
