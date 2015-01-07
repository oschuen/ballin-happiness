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
 * @since 06.10.2014
 * @version 1.0
 * @author oliver
 */
package oc.io;

import java.io.File;
import java.io.IOException;

import oc.io.help.TestExternalizable;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author oliver
 * 
 */
public class TestStreamWriteAndRead {

	private static final int NUMBER_OF_TEST_ELEMENTS = 1024;

	@Test
	public void test() throws IOException {
		final File f = File.createTempFile("random", "test");
		final ExternalizableWriter<TestExternalizable> writer = new ExternalizableWriter<>(f);
		for (int i = 0; i < NUMBER_OF_TEST_ELEMENTS; ++i) {
			writer.writeExternalizable(new TestExternalizable(i));
		}
		writer.close();

		final ExternalizableIterator<TestExternalizable> iter = new ExternalizableIterator<>(f,
				new ExternalizableFactory<TestExternalizable>() {
					@Override
					public TestExternalizable construct() {
						return new TestExternalizable();
					}
				});
		int count = 0;
		while (iter.hasNext()) {
			final TestExternalizable test = iter.next();
			Assert.assertEquals(count, test.getMyId());
			count++;
		}
	}
}
