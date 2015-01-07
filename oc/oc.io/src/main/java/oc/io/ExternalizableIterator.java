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
 * @since 01.07.2014
 * @version 1.0
 * @author oliver
 */
package oc.io;

import java.io.EOFException;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import oc.io.base.DecoupledInputStream;

/**
 * This class iterates over a file or an InputStream containing Externalizables.
 * It offers a classic Iterator signature to easily consuming the object. When
 * the end of the file or the inputstream is reached it is automatically closed.
 * When there is no need to consume all contained objects the stream can also be
 * closed using the close method.
 * 
 * @author oliver
 */
public class ExternalizableIterator<T extends Externalizable> {

	private final ObjectInputStream ois;
	private T next = null;
	private boolean brOpen = true;
	private final ExternalizableFactory<T> factory;
	private final DecoupledInputStream dIn;

	/**
	 * Constructor for iterating over file containing Externalizables.
	 * 
	 * @param f
	 *            File containing Externalizable object representations.
	 * @param factory
	 *            Factory constructing the concrete implementation of the
	 *            Externalizable
	 * @throws IOException
	 */
	public ExternalizableIterator(final File f, final ExternalizableFactory<T> factory)
			throws IOException {
		this(new FileInputStream(f), factory);
	}

	/**
	 * Constructor for iterating over an InputStream
	 * 
	 * @param in
	 *            InputStream containing Externalizable object representations.
	 * @param factory
	 *            Factory constructing the concrete implementation of the
	 *            Externalizable
	 * @throws IOException
	 */
	public ExternalizableIterator(final InputStream in, final ExternalizableFactory<T> factory)
			throws IOException {
		dIn = new DecoupledInputStream(in);
		final ObjectInputStream tempOis = new HeaderlessObjectInput(dIn);
		this.factory = factory;
		brOpen = true;
		ois = tempOis;
		next = findNext();
	}

	/**
	 * @return true when there is at least one element that can be returned
	 *         calling next
	 */
	public boolean hasNext() {
		return next != null;
	}

	/**
	 * the next element in the stream.
	 * 
	 * @return Null when there are no more elements
	 * @throws IOException
	 */
	public T next() throws IOException {
		final T ret = next;
		next = findNext();
		return ret;
	}

	/**
	 * Finds the next element in the stream.
	 * 
	 * @return null when there are no more elements available
	 * @throws IOException
	 */
	private T findNext() throws IOException {
		T ret = null;
		try {
			if (brOpen) {
				ret = factory.construct();
				try {
					ret.readExternal(ois);
				} catch (final EOFException eof) {
					ret = null;
				} catch (final IOException e) {
					e.printStackTrace();
					ret = null;
				}
				if (ret == null) {
					ois.close();
					brOpen = false;
				}

			}
		} catch (final Exception e) {
			throw new IOException("Error reading next", e);
		}
		return ret;
	}

	/**
	 * Closes the underlaying stream when no more objects are needed. Its not
	 * necessary to call this method when the iterator is called until hasNext
	 * returns false
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		ois.close();
		brOpen = false;
	}

	/**
	 * ObjectInputStream that reads a stream created by the ObjectOutputStream
	 * within ExternalizableWriter
	 * 
	 * @author oliver
	 */
	private static class HeaderlessObjectInput extends ObjectInputStream {

		/**
		 * @param in
		 * @throws IOException
		 */
		public HeaderlessObjectInput(final InputStream in) throws IOException {
			super(in);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.ObjectInputStream#readStreamHeader()
		 */
		@Override
		protected void readStreamHeader() throws IOException, StreamCorruptedException {
			// ExternalizableWriter is not writing an ObjectInputStream Header
		}
	}
}
