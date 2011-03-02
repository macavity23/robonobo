package com.robonobo.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Put ByteBuffers on at one end, read bytes off the other as an input stream.
 * Avoids unnecessary array copying. Also allows you to peek() at the first
 * byte, and to place a 'pretend' EOF at a specified point in the stream (this
 * is to accomodate protocol buffers reading from this class, as
 * Builder.mergeFrom(InputStream) always tries to read the entire stream).
 * 
 * @author Will Morton
 * 
 */
public class ByteBufferInputStream extends InputStream implements PeekableInputStream {
	LinkedList<ByteBuffer> queuedBufs = new LinkedList<ByteBuffer>();
	int queuedBytes;
	ByteBuffer currentBuf;
	boolean closed;
	Lock lock = new ReentrantLock();
	Condition dataAvailable = lock.newCondition();
	int pretendEofBytesLeft = -1;
	Log log = LogFactory.getLog(getClass());

	public ByteBufferInputStream() {
	}

	@Override
	public int read() throws IOException {
		lock.lock();
		try {
			if(pretendEofBytesLeft == 0)
				return -1;
			while (currentBuf == null) {
				if (closed)
					return -1;
				else
					dataAvailable.await();
			}
			byte result = currentBuf.get();
			if(pretendEofBytesLeft > 0)
				pretendEofBytesLeft--;
			moveBufsUp();
			return result;
		} catch (InterruptedException e) {
			throw new IOException("Interrupted while waiting on data");
		} finally {
			lock.unlock();
		}
	}

	public int peek() throws IOException {
		lock.lock();
		try {
			while (currentBuf == null) {
				if (closed)
					return -1;
				else
					dataAvailable.await();
			}
			return currentBuf.get(0);
		} catch (InterruptedException e) {
			throw new IOException("Interrupted while waiting on data");
		} finally {
			lock.unlock();
		}
	}

	public int available() {
		lock.lock();
		try {
			int cbb = (currentBuf == null) ? 0 : currentBuf.remaining();
			int result = cbb + queuedBytes;
			if(pretendEofBytesLeft < 0 || pretendEofBytesLeft > result)
				return result;
			else
				return pretendEofBytesLeft;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns the number of bytes before a null byte is located in the stream, or -1 if no null byte is available
	 * @return
	 */
	public int locateNullByte() {
		lock.lock();
		try {
			if(currentBuf == null)
				return -1;
			int result = 0;
			for(int i=currentBuf.position();i<currentBuf.limit();i++) {
				if(currentBuf.get(i) == 0)
					return result;
				result++;
			}
			for (ByteBuffer buf : queuedBufs) {
				for(int i=0;i<buf.limit();i++) {
					if(buf.get(i) == 0)
						return result;
					result++;
				}
			}
			return -1;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if ((off + len) > b.length)
			throw new IndexOutOfBoundsException("Requested params won't fit in supplied buffer");
		lock.lock();
		try {
			int numToRead = len;
			while (numToRead > 0) {
				if(pretendEofBytesLeft == 0) {
					if(numToRead == len)
						return -1;
					else
						return (len - numToRead);
				}
				while (currentBuf == null) {
					if (closed) {
						if (numToRead == len)
							return -1;
						else
							return (len - numToRead);
					} else {
						dataAvailable.await();
					}
				}
				int toReadNow = (numToRead <= currentBuf.remaining()) ? numToRead : currentBuf.remaining();
				if(pretendEofBytesLeft > 0 && pretendEofBytesLeft < toReadNow)
					toReadNow = pretendEofBytesLeft;
				int destPos = off + (len - numToRead);
				currentBuf.get(b, destPos, toReadNow);
				numToRead -= toReadNow;
				if(pretendEofBytesLeft > 0)
					pretendEofBytesLeft -= toReadNow;
				moveBufsUp();
			}
			return len;
		} catch (InterruptedException e) {
			throw new IOException("Interrupted while waiting on data");
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long skip(long n) throws IOException {
		lock.lock();
		try {
			long numToSkip = n;
			while (numToSkip > 0) {
				if(pretendEofBytesLeft == 0)
					return (n - numToSkip);
				while (currentBuf == null) {
					if (closed)
						return (n - numToSkip);
					else
						dataAvailable.await();
				}
				int skipNow = (int) ((numToSkip <= currentBuf.remaining()) ? numToSkip : currentBuf.remaining());
				if(pretendEofBytesLeft > 0 && pretendEofBytesLeft < skipNow)
					skipNow = pretendEofBytesLeft;
				currentBuf.position(currentBuf.position() + skipNow);
				numToSkip -= skipNow;
				if(pretendEofBytesLeft > 0)
					pretendEofBytesLeft -= skipNow;
				moveBufsUp();
			}
			return n;
		} catch (InterruptedException e) {
			throw new IOException("Interrupted while waiting on data");
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Do not modify the parameters of the buffer after you have added it, or
	 * Bad Things will happen
	 */
	public void addBuffer(ByteBuffer buf) {
		// Only add buffers that have at least one byte available - makes code
		// in other methods simpler
		if (buf.remaining() == 0)
			return;
		lock.lock();
		try {
			if (currentBuf == null)
				currentBuf = buf;
			else {
				queuedBufs.add(buf);
				queuedBytes += buf.remaining();
			}
			dataAvailable.signalAll();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() throws IOException {
		lock.lock();
		try {
			closed = true;
			dataAvailable.signalAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Specifies that in n bytes time, this stream should return -1 to all reads
	 * until clearPretendEof() is called
	 */
	public void setPretendEof(int n) {
		lock.lock();
		pretendEofBytesLeft = n;
		lock.unlock();
	}

	public void clearPretendEof() {
		lock.lock();
		pretendEofBytesLeft = -1;
		lock.unlock();
	}
	
	protected void moveBufsUp() {
		if (currentBuf.remaining() == 0) {
			if (queuedBufs.size() == 0)
				currentBuf = null;
			else {
				currentBuf = queuedBufs.removeFirst();
				queuedBytes -= currentBuf.remaining();
			}
		}
	}
}
