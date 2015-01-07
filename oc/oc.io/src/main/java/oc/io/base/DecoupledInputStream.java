/**
 * Copyright (C) 2015 Oliver Schünemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 03.01.2015
 * @version 1.0
 * @author oliver
 */
package oc.io.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This inputStream uses a second thread for reading the data from the source
 * input stream. When there is a blocking device behind this thread, the user of
 * the DecoupledInputstream might not been blocked, because it can read data out
 * of the buffer. Only when buffer is empty it must wait until the reader thread
 * has gathered enough data for continuing.
 * 
 * @author oliver
 * 
 */
public class DecoupledInputStream extends InputStream {

	private final Thread readerThread;
	private static int defaultBufferSize = 1 << 20;
	private static int defaultReadBufferSize = 1 << 16;
	private final Lock bufferLock = new ReentrantLock();
	private final Semaphore readTrigger = new Semaphore(1);
	private final Semaphore availableTrigger = new Semaphore(1);
	private final Semaphore closeSemaphore = new Semaphore(0);
	private final byte b[] = new byte[defaultBufferSize];
	private int free = defaultBufferSize;
	private int in = 0;
	private int out = 0;
	private boolean closing = false;
	private boolean sourceClosed = false;
	private static final Logger logger = LogManager.getLogger(DecoupledInputStream.class);

	public DecoupledInputStream(final InputStream source) throws IOException {
		super();
		readerThread = new Thread(new SourceReader(source));
		readerThread.start();
	}

	/**
	 * waits until the given amount of data is available in the stream or stream
	 * end is reached.
	 * 
	 * @param amount
	 *            of data needed before method returns.
	 * @return the amount of available data. Can be smaller than amount, when
	 *         not enough data is available, or buffer is to small
	 * @throws IOException
	 */
	private int waitAvailable(final int amount) throws IOException {
		final int maxAmount = amount > defaultReadBufferSize ? defaultReadBufferSize : amount;
		int currAvailable = available();
		while (!sourceClosed && currAvailable < maxAmount) {
			try {
				availableTrigger.tryAcquire(1, TimeUnit.SECONDS);
				currAvailable = available();
			} catch (final InterruptedException e) {
				throw new InterruptedIOException("Thread was interrupted while reading");
			}
		}
		return Math.min(maxAmount, currAvailable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		final int got = waitAvailable(1);
		bufferLock.lock();
		try {
			if (got >= 1) {
				final int r = b[out];
				out = (out + 1) % defaultBufferSize;
				free++;
				if (free > defaultReadBufferSize) {
					readTrigger.release();
				}
				return r;
			}
			if (available() > 0) {
				System.out.println("hier");
			}
			return -1;
		} finally {
			bufferLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] bb, final int off, final int len) throws IOException {
		int total = 0;
		int got = waitAvailable(len);
		while (got > 0 && total < len) {
			bufferLock.lock();
			try {
				int copy = got;
				if (total + copy > len) {
					copy = len - total;
				}
				if (out + copy > defaultBufferSize) {
					copy = defaultBufferSize - out;
				}
				System.arraycopy(b, out, bb, off + total, copy);
				total += copy;
				free += copy;
				out = (out + copy) % defaultBufferSize;
				if (free > defaultReadBufferSize) {
					readTrigger.release();
				}
				if (total < len) {
					got = waitAvailable(len - total);
				} else {
					got = -1;
				}
			} finally {
				bufferLock.unlock();
			}
		}
		if (total <= 0 && available() > 0) {
			System.out.println("hier");
		}

		return total > 0 ? total : -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(final long n) throws IOException {
		final byte sb[] = new byte[1000];
		long skipped = 0;
		int got = read(sb, 0, n > sb.length ? sb.length : (int) n);
		skipped += got > 0 ? got : 0;
		while (got >= 0 && skipped < n) {
			got = read(sb, 0, n - skipped > sb.length ? sb.length : (int) (n - skipped));
			skipped += got > 0 ? got : 0;
		}
		return skipped;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		bufferLock.lock();
		try {
			return defaultBufferSize - free;
		} finally {
			bufferLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if (!sourceClosed) {
			logger.debug("Close input stream");
			closing = true;
			readTrigger.release();
			closeSemaphore.acquireUninterruptibly();
			// release closeSemaphore if InputStream is closed a second time
			closeSemaphore.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark(final int readlimit) {
		// Mark is not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("Mark and reset are not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return false;
	}

	/**
	 * This Runnable reads the data from the source input stream and copies it
	 * to the intermediate buffer.
	 * 
	 * @author oliver
	 */
	private class SourceReader implements Runnable {
		private final InputStream source;
		private final byte rb[] = new byte[defaultReadBufferSize];

		public SourceReader(final InputStream source) {
			super();
			this.source = source;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				int got = source.read(rb);
				while (!closing && got > 0) {
					readTrigger.acquireUninterruptibly();
					bufferLock.lock();
					try {
						if (free > defaultReadBufferSize) {
							if (in + got < defaultBufferSize) {
								System.arraycopy(rb, 0, b, in, got);
							} else {
								System.arraycopy(rb, 0, b, in, defaultBufferSize - in);
								System.arraycopy(rb, defaultBufferSize - in, b, 0, got
										- (defaultBufferSize - in));
							}
							in = (in + got) % defaultBufferSize;
							free -= got;
							availableTrigger.release();
							if (free > defaultReadBufferSize) {
								readTrigger.release();
							}
							got = source.read(rb);
						}
					} finally {
						bufferLock.unlock();
					}
				}
				source.close();
			} catch (final IOException e) {
				logger.error("IOException during source reading", e);
			} finally {
				sourceClosed = true;
				closeSemaphore.release();
			}
		}
	}
}
