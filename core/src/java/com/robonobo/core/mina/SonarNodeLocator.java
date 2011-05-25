package com.robonobo.core.mina;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.core.api.proto.CoreApi.NodeList;
import com.robonobo.mina.external.NodeLocator;

/**
 * Uses the newer Sonar server to locate nodes. Multiple urls can be specified
 * in the config
 * 
 * @author Ray
 * 
 */
public class SonarNodeLocator implements NodeLocator {
	Log log = LogFactory.getLog(getClass());
	List<String> urls = new ArrayList<String>();

	public SonarNodeLocator() {
	}

	public SonarNodeLocator(String uri) {
		urls.add(uri);
	}

	public void addLocatorUri(String uri) {
		urls.add(uri);
	}

	public List<Node> locateSuperNodes(Node myNodeDesc) {
		List<Node> result = new ArrayList<Node>();
		for(int i = 0; i < urls.size(); i++) {
			result.addAll(locateSuperNodesUsingUri(myNodeDesc, urls.get(i)));
		}
		return result;
	}

	public List<Node> locateSuperNodesUsingUri(Node myNodeDesc, String uri) {
		List<Node> result = new ArrayList<Node>();
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(uri);
		try {
			post.setRequestEntity(new ByteArrayRequestEntity(myNodeDesc.toByteArray()));
			int status = client.executeMethod(post);
			switch(status) {
			case 200:
				NodeList nl = NodeList.parseFrom(post.getResponseBody());
				result.addAll(nl.getNodeList());
				log.debug("Sonar server @ " + uri +" returned "+nl.getNodeCount()+" supernodes");
				break;
			default:
				log.error("Sonar error: returned status '" + post.getStatusText() + "' from url "+uri);
				log.error(post.getResponseBodyAsString());
			}
		} catch(IOException e) {
			log.error("IOException locating supernodes", e);
		}
		return result;
	}

	public String toString() {
		return "SonarNodeLocator (" + urls.size() + " urls)";
	}
}
