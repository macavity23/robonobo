package com.robonobo.common.pageio.paginator;

import static java.lang.Math.*;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.util.*;

import com.robonobo.common.pageio.buffer.FilePageBuffer;
import com.robonobo.common.pageio.buffer.SimplePageInfoStore;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.external.buffer.PageInfo;

/**
 * Makes the first eight pages four times smaller, so that we start feeding data to consumers earlier - should make for
 * faster playback start
 * 
 * @author macavity
 */
public class QuickStartFilePaginator extends EqualSizeFilePaginator {
	public QuickStartFilePaginator(int pageSize, long totalSize, int totalTime) {
		super(pageSize, totalSize, totalTime);
	}

	@Override
	public void paginate(ByteChannel c, PageBuffer pb) throws IOException {
		List<PageInfo> pageInfos = new ArrayList<PageInfo>();
		long totalMillis = totalTime * 1000;
		int pageNum = 0;
		int quickStartBytes = pageSz * 2;
		int smallPgSz = pageSz / 4;
		int numSmallPages;
		if (totalSz <= quickStartBytes)
			numSmallPages = (int) ((totalSz % smallPgSz == 0) ? (totalSz / smallPgSz) : (totalSz / smallPgSz) + 1);
		else
			numSmallPages = 8;
		int byteOffset = 0;
		for (int i = 0; i < numSmallPages; i++) {
			long timeOffset = (long) (((float) byteOffset / totalSz) * totalMillis);
			int thisPageSz = smallPgSz;
			if ((totalSz < quickStartBytes) && (i == (numSmallPages - 1)) && (totalSz % smallPgSz != 0)) {
				// Last page will be smaller
				thisPageSz = (int) (totalSz % smallPgSz);
			}
			PageInfo pi = new PageInfo(pageNum, byteOffset, timeOffset, thisPageSz, 0);
			pageInfos.add(pi);
			pageNum++;
			byteOffset += thisPageSz;
		}
		if (totalSz > quickStartBytes) {
			long bytesLeft = totalSz - quickStartBytes;
			int numFullSzPgs = (int) ((bytesLeft % pageSz == 0) ? (bytesLeft / pageSz) : (bytesLeft / pageSz) + 1);
			for (int i = 0; i < numFullSzPgs; i++) {
				long timeOffset = (long) (((float) byteOffset / totalSz) * totalMillis);
				int thisPageSz = pageSz;
				if (((pageNum - 8) == numFullSzPgs - 1) && (bytesLeft % pageSz != 0)) {
					// Last page will be smaller
					thisPageSz = (int) (totalSz % pageSz);
				}
				PageInfo pi = new PageInfo(pageNum, byteOffset, timeOffset, thisPageSz, 0);
				pageInfos.add(pi);
				pageNum++;
				byteOffset += thisPageSz;
			}
		}
		pb.putAllPageInfo(pageInfos);
	}

	public static void main(String[] args) throws Exception {
		// Test
		long[] testSzs = new long[] { 64 * 1024 - 1, 64 * 1024, 64 * 1024 + 1, 1024 * 1024, 2 * 1024 * 1024,
				2 * 1024 * 1024 + 1 };
		int pageSz = 32 * 1024;
		for (long totalSz : testSzs) {
			System.out.println("Testing total size " + totalSz);
			String sid = "test:flarp-" + totalSz;
			SimplePageInfoStore pis = new SimplePageInfoStore();
			pis.init(sid);
			File tmpf = File.createTempFile("foo", "bar");
			PageBuffer pb = new FilePageBuffer(sid, tmpf, pis);
			QuickStartFilePaginator pag = new QuickStartFilePaginator(pageSz, totalSz, 1000);
			pag.paginate(null, pb);
			Map<Long, PageInfo> pageInfos = pis.getAllPageInfo(sid);
			for (long pn = 0; pageInfos.containsKey(pn); pn++) {
				PageInfo pi = pageInfos.get(pn);
				System.out.println(pi);
			}
			System.out.println();
			tmpf.delete();
		}
	}
}
