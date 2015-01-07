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
 * @since 13.10.2014
 * @version 1.0
 * @author oliver
 */
package oc.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import oc.io.ReferenceResolver.ReferedHandler;
import oc.io.ReferenceResolver.RefererHandler;
import oc.io.help.TestManyExternalizable;
import oc.io.help.TestOneExternalizable;

import org.junit.Before;
import org.junit.Test;

/**
 * @author oliver
 * 
 */
public class TestReferenceResolver {

	private File oneFile = null;
	private File manyFile = null;
	private final ExternalizableFactory<TestOneExternalizable> oneFactory = new ExternalizableFactory<TestOneExternalizable>() {

		@Override
		public TestOneExternalizable construct() {
			return new TestOneExternalizable();
		}
	};

	private final ExternalizableFactory<TestManyExternalizable> manyFactory = new ExternalizableFactory<TestManyExternalizable>() {

		@Override
		public TestManyExternalizable construct() {
			return new TestManyExternalizable();
		}
	};

	private final RefererHandler<TestOneExternalizable, TestManyExternalizable> oneHandler = new RefererHandler<TestOneExternalizable, TestManyExternalizable>() {

		@Override
		public long getId(final TestOneExternalizable one) {
			return one.getMyId();
		}

		@Override
		public List<Long> getRefs(final TestOneExternalizable one) {
			final List<Long> refs = new ArrayList<>();
			for (final TestManyExternalizable many : one.getManies()) {
				refs.add(Long.valueOf(many.getMyId()));
			}
			return refs;
		}

		@Override
		public void setResolvedRefs(final TestOneExternalizable one,
				final Map<Long, TestManyExternalizable> manies) {
			one.setManies(manies);
		}
	};

	private final ReferedHandler<TestManyExternalizable> manyHandler = new ReferedHandler<TestManyExternalizable>() {

		@Override
		public long getId(final TestManyExternalizable many) {
			return many.getMyId();
		}

		@Override
		public void setId(final TestManyExternalizable many, final long id) {
			many.setMyId((int) id);
		}
	};

	@Before
	public void setup() throws IOException {
		oneFile = File.createTempFile("onefile", "dat");
		manyFile = File.createTempFile("manyfile", "dat");
		final ExternalizableWriter<TestOneExternalizable> oneWriter = new ExternalizableWriter<>(
				oneFile);
		final ExternalizableWriter<TestManyExternalizable> manyWriter = new ExternalizableWriter<>(
				manyFile);
		final int manyToOne = 5;
		final int oneAmount = 10;
		final int manyAmount = manyToOne * oneAmount;
		final List<TestManyExternalizable> manies = new ArrayList<>();

		for (int i = 0; i < manyAmount; ++i) {
			manies.add(new TestManyExternalizable(i, false));
			manyWriter.writeExternalizable(new TestManyExternalizable(i, true));
		}
		Collections.shuffle(manies);
		for (int i = 0; i < oneAmount; ++i) {
			final List<TestManyExternalizable> oneManies = new ArrayList<>();
			for (int j = 0; j < manyToOne; ++j) {
				final TestManyExternalizable many = manies.get(i * manyToOne + j);
				oneManies.add(many);
			}
			oneWriter.writeExternalizable(new TestOneExternalizable(i, oneManies));
		}
		oneWriter.close();
		manyWriter.close();
	}

	/**
	 * Test method for
	 * {@link mm.io.ReferenceResolver#resolveReferences(java.io.File, java.io.File, java.io.File, java.io.File, mm.io.ExternalizableFactory, mm.io.ExternalizableFactory, mm.io.ReferenceResolver.ReferedHandler, mm.io.ReferenceResolver.RefererHandler)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testResolveReferences() throws IOException {
		final File tempDir = File.createTempFile("resolvetemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		final File destFile = File.createTempFile("resolved", "dat");

		ReferenceResolver.resolveReferences(destFile, oneFile, manyFile, tempDir, oneFactory,
				manyFactory, manyHandler, oneHandler);

	}
}
