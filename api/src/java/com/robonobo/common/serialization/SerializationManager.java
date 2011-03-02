package com.robonobo.common.serialization;

import java.io.*;
import java.util.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;

/**
 * TODO This class is a bit of a hack, it uses some old-style class-based
 * serialization, and some new protocol-buffer stuff
 * 
 * @author macavity
 */
public class SerializationManager {
	private static final int HTTP_TIMEOUT_MS = 30000;
	public static final int MAX_HTTP_CONNECTIONS_PER_HOST = 4;

	Map serializers = new HashMap();
	ObjectSerializer genericSerializer = new GenericXMLSerializer();
	HttpClient client;
	Log log = LogFactory.getLog(getClass());
	String username, password;

	public SerializationManager() {
		// Use a multithreaded connection manager - keeps a set of connections
		// open to the server and reuses them
		HttpConnectionManagerParams httpParams = new HttpConnectionManagerParams();
		httpParams.setSoTimeout(HTTP_TIMEOUT_MS);
		httpParams.setDefaultMaxConnectionsPerHost(MAX_HTTP_CONNECTIONS_PER_HOST);
		HttpConnectionManager connMgr = new MultiThreadedHttpConnectionManager();
		connMgr.setParams(httpParams);
		client = new HttpClient(connMgr);
	}

	public void setCreds(String user, String pwd) {
		username = user;
		password = pwd;
	}
	
	/**
	 * Visits the supplied url, throws an exception if we get any return code
	 * other than 200-OK
	 */
	public void hitUrl(String url) throws IOException {
		log.debug("Hitting url "+url);
		GetMethod get = new GetMethod(url);
		try {
			client.getState().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			int statusCode = client.executeMethod(get);
			if (statusCode != 200)
				throw new IOException("Failed to hit " + url + ", status code was " + statusCode);
		} finally {
			get.releaseConnection();
		}
	}

	public void deleteObjectAtUrl(String url) throws IOException {
		log.debug("Deleting object at "+url);
		DeleteMethod del = new DeleteMethod(url);
		try {
			client.getState().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			int statusCode = client.executeMethod(del);
			if (statusCode != 200)
				throw new IOException("Failed to delete object at " + url + ", status code was " + statusCode);
		} finally {
			del.releaseConnection();
		}
	}

	public <T> T getObject(Class<T> cl, InputStream in) throws IOException {
		return (T) getSerializerForClass(cl).getObject(in);
	}

	@SuppressWarnings("unchecked")
	public void getObjectFromUrl(AbstractMessage.Builder bldr, String url)
			throws IOException, SerializationException {
		log.debug("Getting object from "+url);
		GetMethod get = new GetMethod(url);
		try {
			if (username != null) {
				client.getState().setAuthenticationPreemptive(true);
				client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			}
			int statusCode = client.executeMethod(get);
			switch (statusCode) {
			case 200:
				bldr.mergeFrom(get.getResponseBodyAsStream());
				break;
			case 404:
				throw new ResourceNotFoundException("Server could not find resource for "+url);
			case 401:
				throw new UnauthorizedException("Server did not allow us to access url "+url+" with supplied credentials");
			case 500:
				throw new IOException("Unable to get object from url '"+url+"', server said: " + get.getResponseBodyAsString());
			default:
				throw new IOException("Url '"+url+"' replied with status " + statusCode);
			}
		} finally {
			get.releaseConnection();
		}
	}

	public void putObject(Object obj, OutputStream out) throws IOException {
		getSerializerForClass(obj.getClass()).putObject(obj, out);
	}

	public void putObjectToUrl(GeneratedMessage msg, String url) throws IOException {
		putObjectToUrl(msg, url, null);
	}
	
	public void putObjectToUrl(GeneratedMessage msg, String url, AbstractMessage.Builder bldr) throws IOException {
		log.debug("Putting object to "+url);
		PutMethod put = new PutMethod(url);
		try {
			put.setRequestEntity(new ByteArrayRequestEntity(msg.toByteArray()));
			// set auth
			client.getState().setAuthenticationPreemptive(true);
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			int statusCode = client.executeMethod(put);
			switch (statusCode) {
			case 200:
				if(bldr != null) {
					byte[] bodBytes = put.getResponseBody();
					bldr.mergeFrom(bodBytes);
				}
				return;
			default:
				throw new IOException("Server replied with status " + statusCode + ": " + put.getResponseBodyAsString());
			}
		} finally {
			put.releaseConnection();
		}
	}

	public void registerSerializer(ObjectSerializer s) {
		serializers.put(s.getDefaultClass(), s);
	}

	protected ObjectSerializer getSerializerForClass(Class cl) {
		ObjectSerializer serializer = genericSerializer;
		for (Iterator iter = serializers.keySet().iterator(); iter.hasNext();) {
			Class clazz = (Class) iter.next();
			if (cl.isAssignableFrom(clazz)) {
				serializer = (ObjectSerializer) serializers.get(clazz);
				break;
			}
		}
		return serializer;
	}
}
