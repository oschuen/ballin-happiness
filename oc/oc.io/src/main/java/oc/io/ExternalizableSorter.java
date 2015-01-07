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
 * @since 24.07.2014
 * @version 1.0
 * @author oliver
 */
package oc.io;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MergeSort implementation that is able to sort a complete file of
 * Externalizable objects using a Comparator. Hence the objects must not
 * implement Comparable. This sorter works like a TreeSet, there will not be 2
 * elements in the resulting file where the comparator says that they are equal.
 * 
 * @author oliver
 */
public class ExternalizableSorter<T extends Externalizable> {
	protected final Comparator<T> comparator;
	protected final File f;
	protected final File tempDir;
	protected final ExternalizableFactory<T> factory;
	protected final int maxSize;
	protected Deque<File> splittedFiles = new LinkedList<>();
	protected static final int MaxSize = 50000;
	protected static final int reservoireSize = 20 * 1024 * 1024; // 20 MB
	Logger logger = LogManager.getLogger(ExternalizableSorter.class);

	/**
	 * Constructor for a MergeSorter with DefaulMaxSize first level sorted files
	 * 
	 * @param f
	 *            File that shall be sorted. This file will be replaced by a
	 *            sorted version
	 * @param tempDir
	 *            Directory that the sorter can use for its intermediate files
	 * @param factory
	 *            Factory for creating instances of T
	 * @param comparator
	 *            Comparator that shall be used for sorting
	 */
	public ExternalizableSorter(final File f, final File tempDir,
			final ExternalizableFactory<T> factory, final Comparator<T> comparator) {
		this(f, tempDir, factory, comparator, MaxSize);
	}

	/**
	 * Constructor setting the size of the first generated files.
	 * 
	 * @param f
	 *            File that shall be sorted. This file will be replaced by a
	 *            sorted version
	 * @param tempDir
	 *            Directory that the sorter can use for its intermediate files
	 * @param factory
	 *            Factory for creating instances of T
	 * @param comparator
	 *            Comparator that shall be used for sorting
	 * @param maxSize
	 *            Size of the first generated files. Note: the bigger this
	 *            number is, the faster the sorter will work.
	 */
	public ExternalizableSorter(final File f, final File tempDir,
			final ExternalizableFactory<T> factory, final Comparator<T> comparator,
			final int maxSize) {
		super();
		this.f = f;
		this.tempDir = tempDir;
		this.factory = factory;
		this.comparator = comparator;
		this.maxSize = maxSize;
	}

	/**
	 * Stores a first level file.
	 * 
	 * @param data
	 *            A presorted Set of elements
	 * @throws IOException
	 */
	protected void storeToTempFile(final Set<T> data) throws IOException {
		final File nextFile = File.createTempFile("merge", "dat", tempDir);
		final ExternalizableWriter<T> writer = new ExternalizableWriter<>(nextFile);
		for (final T t : data) {
			writer.writeExternalizable(t);
		}
		writer.close();
		splittedFiles.add(nextFile);
	}

	private int preSortSingleFile(final ExternalizableIterator<T> iter, final int max)
			throws IOException {
		final TreeSet<T> preSorter = new TreeSet<>(comparator);
		int counter = 0;
		while (iter.hasNext()) {
			counter++;
			preSorter.add(iter.next());
			if (preSorter.size() >= max) {
				storeToTempFile(preSorter);
				preSorter.clear();
			}
		}
		if (!preSorter.isEmpty()) {
			storeToTempFile(preSorter);
			preSorter.clear();
		}
		return counter;
	}

	private void prepareIterator(final ExternalizableIterator<T> iter, final int count)
			throws IOException {
		for (int i = 0; i < count && iter.hasNext(); ++i) {
			iter.next();
		}
	}

	/**
	 * presorts the complete file to the first level files.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	protected void presortFile() throws IOException {
		ExternalizableIterator<T> iter = new ExternalizableIterator<>(f, factory);
		int counter = 0;
		int max = maxSize;
		while (iter.hasNext() && max > 0) {
			try {
				counter += preSortSingleFile(iter, max);
			} catch (final OutOfMemoryError error) {
				iter.close();
				if (counter > 0) {
					iter = new ExternalizableIterator<>(f, factory);
				}
				prepareIterator(iter, counter);
				max = max >>> 1;
				logger.warn(
						"Out of Memory Exception occured. Reduce Max Elements in pre sorted files to {} elements",
						max);
			}
		}
		iter.close();
	}

	/**
	 * this is the MergeSort implementation. I merges to files together in the
	 * way that the smallest element is at the beginning of the file and the
	 * biggest at the end.
	 * 
	 * @param dest
	 *            Destination file where the merge result is stored in
	 * @param source1
	 *            File 1 that shall be merged
	 * @param source2
	 *            File 2 that shall be merged
	 * @throws IOException
	 */
	protected void mergeTwoFiles(final File dest, final File source1, final File source2)
			throws IOException {
		final ExternalizableWriter<T> writer = new ExternalizableWriter<>(dest);
		final ExternalizableIterator<T> iter1 = new ExternalizableIterator<>(source1, factory);
		final ExternalizableIterator<T> iter2 = new ExternalizableIterator<>(source2, factory);
		T element1 = iter1.hasNext() ? iter1.next() : null;
		T element2 = iter2.hasNext() ? iter2.next() : null;
		while (!(element1 == null || element2 == null)) {
			final int compare = comparator.compare(element1, element2);
			if (compare < 0) {
				writer.writeExternalizable(element1);
				element1 = iter1.hasNext() ? iter1.next() : null;
			} else if (compare == 0) {
				element1 = iter1.hasNext() ? iter1.next() : null;
			} else {
				writer.writeExternalizable(element2);
				element2 = iter2.hasNext() ? iter2.next() : null;
			}
		}
		if (element1 != null) {
			writer.writeExternalizable(element1);
		}
		if (element2 != null) {
			writer.writeExternalizable(element2);
		}
		while (iter1.hasNext()) {
			writer.writeExternalizable(iter1.next());
		}
		while (iter2.hasNext()) {
			writer.writeExternalizable(iter2.next());
		}
		writer.close();
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	protected void mergeManyFiles(final File dest, final List<File> source) throws IOException {
		final ExternalizableWriter<T> writer = new ExternalizableWriter<>(dest);
		final TreeSet<SortNode> sortedSet = new TreeSet<>();
		final List<ExternalizableIterator<T>> iters = new ArrayList<>();
		for (final File file : source) {
			final ExternalizableIterator<T> iter = new ExternalizableIterator<>(file, factory);
			iters.add(iter);
			final SortNode set = new SortNode(iter);
			while (set.hasNext() && sortedSet.contains(set)) {
				set.next();
			}
			if (set.hasNext()) {
				sortedSet.add(set);
			}
		}
		while (!sortedSet.isEmpty()) {
			final SortNode smallest = sortedSet.pollFirst();
			final T element = smallest.next();
			writer.writeExternalizable(element);
			while (smallest.hasNext() && sortedSet.contains(smallest)) {
				smallest.next();
			}
			if (smallest.hasNext()) {
				sortedSet.add(smallest);
			}
		}
		for (final ExternalizableIterator<T> externalizableIterator : iters) {
			if (externalizableIterator.hasNext()) {
				logger.error("Iterator is not totally consumed");
			}
		}
		writer.close();
	}

	/**
	 * Processes the complete sorting algorithm
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void process() throws IOException {
		presortFile();
		while (splittedFiles.size() > 2) {
			logger.debug("Still {} files left", splittedFiles.size());
			final List<File> files = new ArrayList<>();
			for (int i = 0; i < 32 && !splittedFiles.isEmpty(); ++i) {
				files.add(splittedFiles.pollFirst());
			}
			if (splittedFiles.isEmpty()) {
				mergeManyFiles(f, files);
			} else {
				final File dest = File.createTempFile("merge", "dat", tempDir);
				mergeManyFiles(dest, files);
				splittedFiles.addLast(dest);
			}
			for (final File file : files) {
				file.delete();
			}
		}
		if (splittedFiles.size() == 2) {
			final File source1 = splittedFiles.pollFirst();
			final File source2 = splittedFiles.pollFirst();
			mergeTwoFiles(f, source1, source2);
			source1.delete();
			source2.delete();
		} else if (splittedFiles.size() == 1) {
			final File source1 = splittedFiles.pollFirst();
			final ExternalizableWriter<T> writer = new ExternalizableWriter<>(f);
			final ExternalizableIterator<T> iter = new ExternalizableIterator<>(source1, factory);
			while (iter.hasNext()) {
				writer.writeExternalizable(iter.next());
			}
			writer.close();
			source1.delete();
			iter.close();
		}
	}

	/**
	 * When many files are merged at a time this is used as a wrapper for the
	 * input itterator to allow finding the next element to store in the
	 * resulting stream, by simply using a TreeSet and store this in.
	 * 
	 * @author oliver
	 */
	private class SortNode implements Comparable<SortNode> {
		private final ExternalizableIterator<T> iter;
		private T element = null;

		/**
		 * Constructor wrapping the input Iterator
		 * 
		 * @param iter
		 * @throws IOException
		 */
		public SortNode(final ExternalizableIterator<T> iter) throws IOException {
			super();
			this.iter = iter;
			element = iter.hasNext() ? iter.next() : null;
		}

		/**
		 * @return true when there are more elements to store
		 */
		public boolean hasNext() {
			return element != null;
		}

		/**
		 * @return the next element from the wrapped iterator
		 * @throws IOException
		 */
		public T next() throws IOException {
			final T current = element;
			element = iter.hasNext() ? iter.next() : null;
			return current;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(final SortNode o) {
			if (o == null) {
				return 1;
			}
			return comparator.compare(element, o.element);
		}
	}
}
