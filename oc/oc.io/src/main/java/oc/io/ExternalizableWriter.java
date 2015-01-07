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

import java.io.BufferedOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Writer that writes Objects derived from Externalizable to a file or an
 * OutputStream. Files written with this Writer can be read using
 * ExternalizableIterator.
 * 
 * @author oliver
 */
public class ExternalizableWriter<T extends Externalizable> {
	private final ObjectOutputStream oos;
	private final Logger logger = LogManager.getLogger(ExternalizableWriter.class);

	/**
	 * Constructor for writing Externalizable Objects to a File.
	 * 
	 * @param f
	 *            File that is used for writing Externalizables to. An existing
	 *            file will be overwritten.
	 * @throws IOException
	 */
	public ExternalizableWriter(final File f) throws IOException {
		this(new FileOutputStream(f));
	}

	/**
	 * Constructor for writing the Externalizable Objects to any OutputStream.
	 * 
	 * @param os
	 *            destination for the Externalizables.
	 * @throws IOException
	 */
	public ExternalizableWriter(final OutputStream os) throws IOException {
		ObjectOutputStream tempOos = null;
		int size = 20;
		while (tempOos == null && size > 10) {
			try {
				tempOos = new HeaderLessObjectOutput(new BufferedOutputStream(os, 1 << size));
			} catch (final OutOfMemoryError e) {
				logger.warn("Not enough memory, reduce write cache by half");
				size--;
			}
		}
		if (tempOos == null) {
			tempOos = new HeaderLessObjectOutput(os);
		}
		this.oos = tempOos;
	}

	/**
	 * writes a single object to the underlaying stream
	 * 
	 * @param externalizable
	 *            the object to write to the stream
	 * @throws IOException
	 */
	public void writeExternalizable(final T externalizable) throws IOException {
		externalizable.writeExternal(oos);
	}

	/**
	 * closes the Stream and all wrapped stream as well.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		oos.close();
	}

	/**
	 * ObjectOutputStream that is not having a stream header.
	 * 
	 * @author oliver
	 */
	private class HeaderLessObjectOutput extends ObjectOutputStream {

		/**
		 * @param out
		 * @throws IOException
		 */
		public HeaderLessObjectOutput(final OutputStream out) throws IOException {
			super(out);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.ObjectOutputStream#writeStreamHeader()
		 */
		@Override
		protected void writeStreamHeader() throws IOException {
			// Do not write a header, that the stream is connectable .
		}
	}
}
