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
 * @since 10.10.2014
 * @version 1.0
 * @author oliver
 */
package oc.io;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import oc.io.help.TestExternalizable;

import org.junit.Test;

/**
 * @author oliver
 * 
 */
public class ExternalizableSorterTestCase {

	/**
	 * Test method for {@link mm.io.ExternalizableSorter#process()}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcess() throws IOException {
		final File tempFile = File.createTempFile("sorter", "dat");
		final File tempDir = File.createTempFile("sortertemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		final ExternalizableWriter<TestExternalizable> writer = new ExternalizableWriter<>(tempFile);
		final ExternalizableSorter<TestExternalizable> sorter = new ExternalizableSorter<>(tempFile, tempDir,
				TestExternalizable.getTestExternalizableFactory(),
				TestExternalizable.getTestExternalizableComparator());
		final List<Integer> values = new ArrayList<>();
		for (int i = 0; i < 500000; ++i) {
			values.add(Integer.valueOf(i));
		}
		Collections.shuffle(values);
		for (final Integer integer : values) {
			writer.writeExternalizable(new TestExternalizable(integer));
		}
		writer.close();
		sorter.process();
		int lastValue = -1;
		boolean firstTest = true;
		int number = 0;
		for (final ExternalizableIterator<TestExternalizable> iter = new ExternalizableIterator<>(
				tempFile, TestExternalizable.getTestExternalizableFactory()); iter.hasNext();) {
			final TestExternalizable test = iter.next();
			if (firstTest) {
				firstTest = false;
			} else {
				assertTrue(lastValue < test.getMyId());
			}
			lastValue = test.getMyId();
			number++;
		}
		System.out.println("Found " + number + " items");
		tempFile.delete();
		tempDir.delete();
	}
}
