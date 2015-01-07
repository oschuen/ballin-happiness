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
 * @since 08.09.2014
 * @version 1.0
 * @author oliver
 */
package oc.o5m.reader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import oc.osm.handler.OsmHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <a href="https://wiki.openstreetmap.org/wiki/O5m">O5M</a> is a file format
 * for osm data that is optimized for a small storage footprint and fast
 * reading. This implements a reader for it, using the common @see
 * oc.osm.handler.OsmHandler as callback for found osm elements. The reader does
 * no check whether the elements in the osm file have ascending ids. This must
 * be checked by the user of it.
 * 
 * @author oliver
 */
public final class O5mReader {

	public static final int STRING_PAIR_CACHE_SIZE = 15000;
	public static final int MAX_CACHEABLE_STRING_LENGTH = 252;

	public static final int NODE_ENTRY = 0x010;
	public static final int WAY_ENTRY = 0x11;
	public static final int RELATION_ENTRY = 0x12;
	public static final int BOUNDING_BOX = 0xDB;
	public static final int FILE_TIMESTAMP = 0xDC;
	public static final int HEADER = 0xE0;
	public static final int SYNC_POINT = 0xEE;
	public static final int JUMP_POINT = 0xEF;
	public static final int END_OF_FILE = 0xFE;
	public static final int RESET = 0xFF;

	private final String stringPairs[][] = new String[STRING_PAIR_CACHE_SIZE][2];
	private int cacheWritePos = 0;

	private static final Logger logger = LogManager.getLogger(O5mReader.class.getName());

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void readFile(final File f, final OsmHandler handler) throws IOException {
		ByteBuffer buffer = null;
		final BufferedInputStream in = new BufferedInputStream(new FileInputStream(f), 1 << 16);
		final int bufferSize = 1000;
		byte readBuffer[] = new byte[bufferSize];
		long numberOfNodes = 0;
		long numberOfWays = 0;
		long numberOfRelations = 0;
		boolean goOn = true;
		final O5mNode node = new O5mNode();
		final O5mWay way = new O5mWay();
		final O5mRelation relation = new O5mRelation();
		while (goOn) {
			final int blockType = in.read();

			if (blockType >= 0 && blockType <= 0xEF) {
				final int blockLength = readUnsigned32(in);
				if (blockLength > readBuffer.length) {
					readBuffer = new byte[blockLength + bufferSize];
				}
				int got = in.read(readBuffer, 0, blockLength);
				while (got < blockLength) {
					got += in.read(readBuffer, got, blockLength - got);
				}
				buffer = ByteBuffer.wrap(readBuffer, 0, blockLength);
			}
			switch (blockType) {
			case NODE_ENTRY:
				numberOfNodes++;
				if (numberOfNodes % 100000000 == 0) {
					logger.debug("Number of Nodes read : {}", numberOfNodes);
				}
				node.readFromBuffer(buffer, this);
				handler.newNode(node.getAsNode());
				break;
			case WAY_ENTRY:
				if (numberOfWays == 0) {
					logger.debug("--------------   Start of Ways   -------------------");
				}
				numberOfWays++;
				if (numberOfWays % 10000000 == 0) {
					logger.debug("Number of Ways read : {}", numberOfWays);
				}
				way.readFromBuffer(buffer, this);
				handler.newWay(way.getAsWay());
				break;
			case RELATION_ENTRY:
				if (numberOfRelations == 0) {
					logger.debug("-----------   Start of Relations   -----------------");
				}
				numberOfRelations++;
				if (numberOfRelations % 1000000 == 0) {
					logger.debug("Number of Relation read : {}", numberOfRelations);
				}
				relation.readFromBuffer(buffer, this);
				handler.newRelation(relation.getAsRelation());
				break;
			case BOUNDING_BOX:
			case FILE_TIMESTAMP:
			case HEADER:
			case SYNC_POINT:
			case JUMP_POINT:
				break;
			case RESET:
				node.reset();
				way.reset();
				relation.reset();
				break;
			case END_OF_FILE:
				goOn = false;
				break;
			default:
				throw new IOException("Unexpected Block Type in File " + blockType);
			}
		}
		logger.debug("Number of Nodes     : {}", numberOfNodes);
		logger.debug("Number of Ways      : {}", numberOfWays);
		logger.debug("Number of Relations : {}", numberOfRelations);
	}

	/**
	 * returns a string pair from the string cache
	 * 
	 * @param rel
	 *            number of entries in cache before current write position
	 * @return the string pair at the cache position
	 */
	private String[] getRelStringPair(final int rel) {
		return stringPairs[(STRING_PAIR_CACHE_SIZE + cacheWritePos - rel) % STRING_PAIR_CACHE_SIZE]
				.clone();
	}

	/**
	 * Stores a string within string cache
	 * 
	 * @param pair
	 *            of strings to store within cache
	 */
	private void storeStringPair(final String pair[]) {
		stringPairs[cacheWritePos][0] = pair[0];
		stringPairs[cacheWritePos][1] = pair[1];
		cacheWritePos++;
		cacheWritePos %= STRING_PAIR_CACHE_SIZE;
	}

	/**
	 * reads the author of an osm element from the stream
	 * 
	 * @param buffer
	 *            buffer containing author information next
	 * @return string pair containing author information. [0] = UID and [1] =
	 *         author name
	 * @throws IOException
	 */
	public String[] readAuthor(final ByteBuffer buffer) throws IOException {
		final ByteArrayOutputStream bao[] = new ByteArrayOutputStream[] {
				new ByteArrayOutputStream(), new ByteArrayOutputStream() };
		final int reference = readUnsigned32(buffer);
		String ret[] = null;
		if (reference == 0) {
			int wc = 0; // Word Counter
			int bc = 0; // Byte Counter
			ret = new String[2];
			while (wc < 2) {
				final byte b = buffer.get();
				bc++;
				if (b == 0) {
					wc++;
				} else {
					bao[wc].write(b);
				}
			}
			if (bao[0].size() > 0) {
				final long uid = readUnsigned64(ByteBuffer.wrap(bao[0].toByteArray()));
				if (uid == 0) {
					ret[0] = "0";
				} else {
					ret[0] = Long.toString(uid);
				}
			} else {
				ret[0] = "";
			}
			if (bao[1].size() > 0) {
				ret[1] = new String(bao[1].toByteArray(), "UTF-8");
			} else {
				ret[1] = "";
			}
			if (bc <= MAX_CACHEABLE_STRING_LENGTH) {
				storeStringPair(ret);
			}
		} else {
			ret = getRelStringPair(reference);
		}

		return ret;

	}

	/**
	 * reads a string pair from stream. This might be a key value pair etc.
	 * 
	 * @param buffer
	 *            containing a string pair next
	 * @return the string pair
	 * @throws IOException
	 */
	public String[] readStringPair(final ByteBuffer buffer) throws IOException {
		final ByteArrayOutputStream bao[] = new ByteArrayOutputStream[] {
				new ByteArrayOutputStream(), new ByteArrayOutputStream() };
		final int reference = readUnsigned32(buffer);
		String ret[] = null;
		if (reference == 0) {
			int wc = 0; // Word Counter
			int bc = 0; // Byte Counter
			ret = new String[2];
			while (wc < 2) {
				final byte b = buffer.get();
				bc++;
				if (b == 0) {
					wc++;
				} else {
					bao[wc].write(b);
				}
			}
			ret[0] = new String(bao[0].toByteArray(), "UTF-8");
			ret[1] = new String(bao[1].toByteArray(), "UTF-8");
			if (bc <= MAX_CACHEABLE_STRING_LENGTH) {
				storeStringPair(ret);
			}
		} else {
			ret = getRelStringPair(reference);
		}

		return ret;
	}

	/**
	 * reads a single string from the byte stream
	 * 
	 * @param buffer
	 *            containing a single stream next
	 * @return the string from the stream
	 * @throws IOException
	 */
	public String readSingleString(final ByteBuffer buffer) throws IOException {
		final ByteArrayOutputStream bao = new ByteArrayOutputStream();
		final int reference = readUnsigned32(buffer);
		String ret[] = null;
		if (reference == 0) {
			int wc = 0; // Word Counter
			int bc = 0; // Byte Counter
			ret = new String[2];
			while (wc < 1) {
				final byte b = buffer.get();
				bc++;
				if (b == 0) {
					wc++;
				} else {
					bao.write(b);
				}
			}
			ret[0] = new String(bao.toByteArray(), "UTF-8");
			ret[1] = "";
			if (bc <= MAX_CACHEABLE_STRING_LENGTH) {
				storeStringPair(ret);
			}
		} else {
			ret = getRelStringPair(reference);
		}

		return ret[0];
	}

	/**
	 * reads a Unsigned int 32 from a ByteBuffer. Note that the result is a
	 * signed value, maybe you have to do something magical in case of negative
	 * values
	 * 
	 * @param buffer
	 * @return int from the buffer
	 */
	public int readUnsigned32(final ByteBuffer buffer) {
		int shift = 0;
		byte b = buffer.get();
		int ret = b & 0x7f;
		while ((b & 0x80) == 0x80) {
			shift += 7;
			b = buffer.get();
			ret += (b & 0x7f) << shift;
		}
		return ret;
	}

	/**
	 * reads a signed int 32 from a ByteBuffer.
	 * 
	 * @param buffer
	 * @return int from the buffer
	 */
	public int readSigned32(final ByteBuffer buffer) {
		long ret = readUnsigned64(buffer);
		if ((ret & 0x01) == 0x01) {
			ret = -1 - (ret >> 1);
		} else {
			ret = ret >> 1;
		}
		return (int) ret;
	}

	/**
	 * reads a Unsigned long from an InputStream. Note that the result is a
	 * signed value, maybe you have to do something magical in case of negative
	 * values
	 * 
	 * @param in
	 *            inputstream containing a unsigned long as next
	 * @return long from the stream
	 */
	public long readUnsigned64(final InputStream in) throws IOException {
		int shift = 0;
		int b = in.read();
		long ret = b & 0x7f;
		while ((b & 0x80) == 0x80) {
			shift += 7;
			b = in.read();
			ret += ((long) b & 0x7f) << shift;
		}
		return ret;
	}

	/**
	 * reads a unsigned int from an InputStream.Note that the result is a signed
	 * value, maybe you have to do something magical in case of negative values
	 * 
	 * @param in
	 * @return int from the stream
	 */
	public int readUnsigned32(final InputStream in) throws IOException {
		int shift = 0;
		int b = in.read();
		int ret = b & 0x7f;
		while ((b & 0x80) == 0x80) {
			shift += 7;
			b = in.read();
			ret += (b & 0x7f) << shift;
		}
		return ret;
	}

	/**
	 * reads a Unsigned long from a ByteBuffer. Note that the result is a signed
	 * value, maybe you have to do something magical in case of negative values
	 * 
	 * @param buffer
	 * @return long from the buffer
	 */
	public long readUnsigned64(final ByteBuffer buffer) {
		int shift = 0;
		byte b = buffer.get();
		long ret = b & 0x7f;
		while ((b & 0x80) == 0x80) {
			shift += 7;
			b = buffer.get();
			ret += ((long) b & 0x7f) << shift;
		}
		return ret;
	}

	/**
	 * reads a signed long from a ByteBuffer.
	 * 
	 * @param buffer
	 * @return long from the buffer
	 */
	public long readSigned64(final ByteBuffer buffer) {
		int shift = 6;
		byte b = buffer.get();
		long ret = (b & 0x7e) >> 1;
		final long sign = b & 0x01;
		while ((b & 0x80) == 0x80) {
			b = buffer.get();
			ret += ((long) b & 0x7f) << shift;
			shift += 7;
		}
		if (sign == 1) {
			ret = -1 - ret;
		}
		return ret;
	}
}
