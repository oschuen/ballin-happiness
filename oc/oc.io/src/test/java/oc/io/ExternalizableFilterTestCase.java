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
 * @since 15.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.io;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import oc.io.ExternalizableFilter.ID;
import oc.io.help.TestExternalizable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author oliver
 * 
 */
public class ExternalizableFilterTestCase {

	private File idFile = null;
	private File sourceFile = null;

	@Before
	public void setUp() throws IOException {
		idFile = File.createTempFile("idtemp", "dat");
		sourceFile = File.createTempFile("filtertemp", "dat");
		final ExternalizableWriter<ID> iDwriter = new ExternalizableWriter<>(idFile);
		final ExternalizableWriter<TestExternalizable> writer = new ExternalizableWriter<>(sourceFile);
		for (int i = 0; i < 20; ++i) {
			if (i % 2 == 0) {
				iDwriter.writeExternalizable(new ID(i));
			}
			writer.writeExternalizable(new TestExternalizable(i));
		}
		iDwriter.close();
		writer.close();
	}

	@After
	public void tearDown() {
		idFile.delete();
		sourceFile.delete();
		idFile = null;
		sourceFile = null;
	}

	/**
	 * Test method for
	 * {@link oc.io.ExternalizableFilter#filter(java.io.File, java.io.File, java.io.File, oc.io.ExternalizableFactory, oc.io.ExternalizableFilter.SourceHandler, boolean)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testWhiteListFilter() throws IOException {
		final File destFile = File.createTempFile("desttemp", "dat");
		ExternalizableFilter.filter(sourceFile, destFile, idFile,
				TestExternalizable.getTestExternalizableFactory(),
				new ExternalizableFilter.SourceHandler<TestExternalizable>() {

					@Override
					public long getId(final TestExternalizable instance) {
						return instance.getMyId();
					}
				}, true);
		final ExternalizableIterator<TestExternalizable> iter = new ExternalizableIterator<>(destFile,
				TestExternalizable.getTestExternalizableFactory());
		assertTrue(iter.hasNext());
		while (iter.hasNext()) {
			assertTrue(iter.next().getMyId() % 2 == 0);
		}
		iter.close();
		destFile.delete();
	}

	/**
	 * Test method for
	 * {@link oc.io.ExternalizableFilter#filter(java.io.File, java.io.File, java.io.File, oc.io.ExternalizableFactory, oc.io.ExternalizableFilter.SourceHandler, boolean)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testBlackListFilter() throws IOException {
		final File destFile = File.createTempFile("desttemp", "dat");
		ExternalizableFilter.filter(sourceFile, destFile, idFile,
				TestExternalizable.getTestExternalizableFactory(),
				new ExternalizableFilter.SourceHandler<TestExternalizable>() {

					@Override
					public long getId(final TestExternalizable instance) {
						return instance.getMyId();
					}
				}, false);
		final ExternalizableIterator<TestExternalizable> iter = new ExternalizableIterator<>(destFile,
				TestExternalizable.getTestExternalizableFactory());
		assertTrue(iter.hasNext());
		while (iter.hasNext()) {
			assertTrue(iter.next().getMyId() % 2 != 0);
		}
		iter.close();
		destFile.delete();
	}

	/**
	 * Checks that the IDComparator sorts in ascending order
	 * 
	 * @throws IOException
	 */
	@Test
	public void testIDComparator() throws IOException {
		final File tempDir = File.createTempFile("sortertemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		final ExternalizableSorter<ExternalizableFilter.ID> sorter = new ExternalizableSorter<>(idFile,
				tempDir, new ExternalizableFilter.IDFactory(),
				new ExternalizableFilter.IDComparator());
		sorter.process();
		final ExternalizableIterator<ExternalizableFilter.ID> iter = new ExternalizableIterator<>(
				idFile, new ExternalizableFilter.IDFactory());
		int lastId = -1;
		while (iter.hasNext()) {
			if (lastId < 0) {
				lastId = (int) iter.next().getId();
			} else {
				assertTrue(lastId < (int) iter.next().getId());
			}
		}
		iter.close();
	}
}
