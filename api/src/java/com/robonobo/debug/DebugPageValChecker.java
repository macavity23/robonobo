package com.robonobo.debug;

import java.nio.ByteBuffer;
import java.util.*;

import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.buffer.PageInfo;

// Class for debugging only!
//public class DebugPageValChecker {
//	private static DebugPageValChecker instance;
//	private Map pages = new HashMap();
//
//	public static DebugPageValChecker getInstance() {
//		if(instance == null)
//			instance = new DebugPageValChecker();
//		return instance;
//	}
//	
//	public void assertPage(Page p) {
//		Long pn = new Long(p.getPageNumber());
//		if(pages.containsKey(pn)) {
//			Page curP = (Page) pages.get(pn);
//			if(!p.equals(curP))
//				throw new RuntimeException();
//		} else {
//			pages.put(pn, duplicatePage(p));
//		}
//	}
//	
//	private Page duplicatePage(Page p) {
//		PageInfo pi = p.getPageInfo();
//		byte[] dataArr = p.getData().array();
//		byte[] newArr = new byte[dataArr.length];
//		System.arraycopy(dataArr, 0, newArr, 0, dataArr.length);
//		ByteBuffer dataCopy = ByteBuffer.wrap(newArr);
//		dataCopy.limit(p.getData().limit());
//		return new Page(pi, dataCopy);
//	}
//}
