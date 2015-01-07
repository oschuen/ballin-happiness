/**
 * Copyright (C) 2014 Oliver Schünemann
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
 * @since 14.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.io.base;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Test;

/**
 * @author oliver
 * 
 */
public class DecoupledInputStreamTestCase {

	/**
	 * Test method for {@link oc.io.base.DecoupledInputStream#read()}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRead() throws IOException {
		final int testReads = 40000;
		final byte b[] = new byte[testReads];
		final TestInputStream testInputStream = new TestInputStream(2 * testReads);
		final DecoupledInputStream decInputStream = new DecoupledInputStream(testInputStream);
		assertEquals(testReads, decInputStream.read(b));
		testInputStream.waitReads(2 * testReads);
		assertEquals(testReads, decInputStream.read(b));
		assertEquals(-1, decInputStream.read(b));
		decInputStream.close();
	}

	/**
	 * Tests that {@link oc.io.base.DecoupledInputStream#read()} terminates
	 * normally when IOException is thrown in wrapped InputStream
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadException() throws IOException {
		final byte b[] = new byte[1];

		// Temporary disable logging for avoiding annoying Exception trace
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		final LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
		final Level currentLevel = loggerConfig.getLevel();
		loggerConfig.setLevel(Level.FATAL);
		ctx.updateLoggers();

		final TestInputStream testInputStream = new TestInputStream(new IOException(
				"This exception is thrown due to a test scenario. This is expected behaviour"));
		final DecoupledInputStream decInputStream = new DecoupledInputStream(testInputStream);
		assertEquals(-1, decInputStream.read(b));
		decInputStream.close();

		loggerConfig.setLevel(currentLevel);
		ctx.updateLoggers();
	}

	/**
	 * Test method for {@link oc.io.base.DecoupledInputStream#skip(long)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSkip() throws IOException {
		final int skipDist = 20000;
		final int testReads = 40000;
		final byte b[] = new byte[testReads];
		final TestInputStream testInputStream = new TestInputStream(2 * testReads);
		final DecoupledInputStream decInputStream = new DecoupledInputStream(testInputStream);
		assertEquals(testReads, decInputStream.read(b));
		decInputStream.skip(0);
		decInputStream.skip(skipDist);
		assertEquals(testReads - skipDist, decInputStream.read(b));
		assertEquals(-1, decInputStream.read(b));
		decInputStream.close();
	}

	/**
	 * Test method for {@link oc.io.base.DecoupledInputStream#close()}.
	 */
	@Test
	public void testClose() {
	}

	public final class TestInputStream extends InputStream {
		private final Semaphore waitReadSem = new Semaphore(0);
		private final Logger logger = LogManager.getLogger(TestInputStream.class);
		private int available;
		private final IOException throwException;

		/**
		 * @param available
		 *            number of available reads
		 */
		public TestInputStream(final int available) {
			super();
			this.available = available;
			throwException = null;
		}

		/**
		 * using this constructor will lead to an IOException whenever a client
		 * tries to read from this inputStream
		 * 
		 * @param throwException
		 */
		public TestInputStream(final IOException throwException) {
			this.throwException = throwException;
			available = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#read()
		 */
		@Override
		public int read() throws IOException {
			if (throwException == null) {
				available--;
				if (available >= 0) {
					waitReadSem.release();
					return 0;
				}
			} else {
				throw throwException;
			}
			return -1;
		}

		public void waitReads(final int reads) {
			try {
				waitReadSem.tryAcquire(reads, 1, TimeUnit.MINUTES);
			} catch (final InterruptedException e) {
				logger.error("Unexpected thread end", e);
			}
		}
	}
}
