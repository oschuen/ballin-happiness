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
 * @since 16.10.2014
 * @version 1.0
 * @author oliver
 */
package oc.io;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

/**
 * Filters out of a file of Externalizables a subset defined by an ID File. It
 * is WhiteList and BlackList filtering supported. WhiteList means, all
 * elements, that correspond with a given id are put into the result file,
 * blackList means the opposite, all elements, that are not listed are put into
 * the resulting file. It is expected that the elements are sorted by the id in
 * the source file. This filter is limited to classes that can be defined by a
 * simple Long ID.
 * 
 * @author oliver
 */
public final class ExternalizableFilter {

	/**
	 * Don't create instances of this.
	 */
	private ExternalizableFilter() {
		super();
	}

	/**
	 * Filters the sourceFile by using the ID File to either black or white List
	 * filter.
	 * 
	 * @param sourceFile
	 *            File containing the superset of elements that shall be
	 *            filtered. File must be sorted in the same order as the ID
	 *            File.
	 * @param destFile
	 *            File where the result shall be stored to
	 * @param idFile
	 *            ID File containing the elements that shall be filtered out or
	 *            in depending on the whiteList parameter
	 * @param factory
	 *            Factory constructing elements of Type T
	 * @param sourceHandler
	 *            handler that evaluates the ID from the source elements
	 * @param whiteList
	 *            true when whiteList filtering shall be done
	 * @throws IOException
	 */
	public static <T extends Externalizable> void filter(final File sourceFile,
			final File destFile, final File idFile, final ExternalizableFactory<T> factory,
			final SourceHandler<T> sourceHandler, final boolean whiteList) throws IOException {
		final ExternalizableIterator<T> sourceIter = new ExternalizableIterator<>(sourceFile,
				factory);
		final ExternalizableIterator<ID> idIter = new ExternalizableIterator<>(idFile,
				new IDFactory());
		final ExternalizableWriter<T> destWriter = new ExternalizableWriter<>(destFile);
		ID id = idIter.hasNext() ? idIter.next() : null;
		T source = sourceIter.hasNext() ? sourceIter.next() : null;
		while (!(id == null || source == null)) {
			while (source != null && sourceHandler.getId(source) < id.getId()) {
				if (!whiteList) {
					destWriter.writeExternalizable(source);
				}
				source = sourceIter.hasNext() ? sourceIter.next() : null;
			}
			if (source != null && sourceHandler.getId(source) == id.getId()) {
				if (whiteList) {
					destWriter.writeExternalizable(source);
				}
				source = sourceIter.hasNext() ? sourceIter.next() : null;
			}
			id = idIter.hasNext() ? idIter.next() : null;
		}
		if (!whiteList) {
			while (source != null) {
				destWriter.writeExternalizable(source);
				source = sourceIter.hasNext() ? sourceIter.next() : null;
			}
		}
		sourceIter.close();
		idIter.close();
		destWriter.close();
	}

	/**
	 * Represents a long as class used for filter IDs.
	 * 
	 * @author oliver
	 */
	public static class ID implements Externalizable {
		private long id = -1;

		public ID() {
			super();
		}

		/**
		 * Constructor directly setting fields
		 * 
		 * @param id
		 */
		public ID(final long id) {
			super();
			this.id = id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
		 */
		@Override
		public void writeExternal(final ObjectOutput out) throws IOException {
			out.writeLong(id);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
		 */
		@Override
		public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
			id = in.readLong();
		}

		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(final long id) {
			this.id = id;
		}
	}

	/**
	 * Factory implementation of IDs
	 * 
	 * @author oliver
	 */
	public static class IDFactory implements ExternalizableFactory<ID> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see oc.io.ExternalizableFactory#construct()
		 */
		@Override
		public ID construct() {
			return new ID();
		}
	}

	/**
	 * Comparator for ID objects used by algorithm to find elements that must be
	 * filtered.
	 * 
	 * @author oliver
	 */
	public static class IDComparator implements Comparator<ID> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final ID o1, final ID o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			} else if (o2 == null) {
				return 1;
			}

			final long compare = o1.getId() - o2.getId();
			if (compare < 0) {
				return -1;
			} else if (compare > 0) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * Interface that is needed by algorithm to get the ID from a given Source
	 * object.
	 * 
	 * @author oliver
	 */
	public interface SourceHandler<T extends Externalizable> {
		long getId(T instance);
	}
}
