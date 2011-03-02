package com.robonobo.midas.search;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.proto.CoreApi.SearchResponse;
import com.robonobo.midas.model.MidasStream;
import com.robonobo.midas.model.MidasStreamAttribute;

@Service("searchService")
public class SearchServiceImpl implements SearchService {
	// TODO: Put this in a config file somewhere
	public static final int MAX_SEARCH_RESULTS = 100;
	private QueryParser queryParser;
	@Autowired
	private SessionFactory sessionFactory;

	public SearchServiceImpl() {
		queryParser = new QueryParser("title", new StandardAnalyzer());
	}

	/* (non-Javadoc)
	 * @see com.robonobo.midas.search.SearchService#search(java.lang.String, java.lang.String, int)
	 */
	@Override
	public SearchResponse search(String searchType, String queryStr, int firstResult) throws IOException {
		if(!searchType.equals("stream"))
			throw new SeekInnerCalmException();
		Session session = sessionFactory.getCurrentSession();
		FullTextSession searchSession = Search.createFullTextSession(session);
		
		org.apache.lucene.search.Query luceneQuery;
		try {
			luceneQuery = queryParser.parse(buildLuceneQuery(queryStr));
		} catch (ParseException e) {
			throw new IOException(e);
		}
		
		// Create hibernate search query
		FullTextQuery hibQuery = searchSession.createFullTextQuery(luceneQuery, MidasStream.class, MidasStreamAttribute.class);
		hibQuery.setMaxResults(MAX_SEARCH_RESULTS);
		hibQuery.setFirstResult(firstResult);
		
		// Make the response
		SearchResponse.Builder srb = SearchResponse.newBuilder();
		srb.setFirstResult(firstResult);
		srb.setTotalResults(hibQuery.getResultSize());
		for (Object resultObj : hibQuery.list()) {
			if(resultObj instanceof MidasStream)
				srb.addObjectId(((MidasStream)resultObj).getStreamId());
			else
				srb.addObjectId(((MidasStreamAttribute)resultObj).getStream().getStreamId());
		}
		return srb.build();
	}
	
	private String buildLuceneQuery(String queryStr) {
		// The query here is for Streams or StreamAttributes, so we include the fields of both 
		StringBuffer sb = new StringBuffer();
		sb.append("title:\"").append(queryStr);
		sb.append("\" OR description:\"").append(queryStr);
		sb.append("\" OR value:\"").append(queryStr).append("\"");
		return sb.toString();
	}
}
