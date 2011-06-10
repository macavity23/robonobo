package com.robonobo.core.mina;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.core.api.proto.CoreApi.NodeList;
import com.robonobo.mina.external.NodeLocator;

/**
 * Uses the newer Sonar server to locate nodes. Multiple urls can be specified in the config
 * 
 * @author Ray
 * 
 */
public class SonarNodeLocator implements NodeLocator {
	Log log = LogFactory.getLog(getClass());
	List<String> urls = new ArrayList<String>();
	HttpClient client;

	public SonarNodeLocator() {
	}

	public SonarNodeLocator(String uri) {
		urls.add(uri);
	}

	public void addLocatorUri(String uri) {
		urls.add(uri);
	}

	@Override
	public void setHttpClient(HttpClient client) {
		this.client = client;
	}

	public List<Node> locateSuperNodes(Node myNodeDesc) {
		List<Node> result = new ArrayList<Node>();
		for (int i = 0; i < urls.size(); i++) {
			String url = urls.get(i);
			try {
				result.addAll(locateSuperNodesUsingUri(myNodeDesc, url));
			} catch (IOException e) {
				log.error("Error fetching supernodes from "+url, e);
			}
		}
		return result;
	}

	public List<Node> locateSuperNodesUsingUri(Node myNodeDesc, String uri) throws IOException {
		List<Node> result = new ArrayList<Node>();
		HttpPost post = new HttpPost(uri);
		post.setEntity(new ByteArrayEntity(myNodeDesc.toByteArray()));
		HttpEntity body = null;
		try {
			HttpResponse resp = client.execute(post);
			body = resp.getEntity();
			int statusCode = resp.getStatusLine().getStatusCode();
			if(statusCode == 200) {
				InputStream is = body.getContent();
				try {
					NodeList nl = NodeList.parseFrom(is);
					log.debug("Sonar server @ " + uri + " returned " + nl.getNodeCount() + " supernodes");
					result.addAll(nl.getNodeList());
				} finally {
					is.close();
				}
			} else
				log.error("Sonar returned status "+statusCode+" from url "+uri);
		} finally {
			if(body != null)
				EntityUtils.consume(body);
		}
		return result;
	}

	public String toString() {
		return "SonarNodeLocator (" + urls.size() + " urls)";
	}
}
