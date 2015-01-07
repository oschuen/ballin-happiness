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
package oc.io.help;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oliver
 * 
 */
public class TestOneExternalizable extends TestExternalizable {

	private List<TestManyExternalizable> manies = new ArrayList<>();

	public TestOneExternalizable() {
		super();
	}

	public TestOneExternalizable(final int myId, final List<TestManyExternalizable> manies) {
		super(myId);
		this.manies = manies;
	}

	/**
	 * @return the manies
	 */
	public List<TestManyExternalizable> getManies() {
		return manies;
	}

	/**
	 * @param manies
	 *            the manies to set
	 */
	public void setManies(final Map<Long, TestManyExternalizable> manies) {
		this.manies.clear();
		this.manies.addAll(manies.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * oc.io.help.TestExternalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(manies.size());
		for (final TestManyExternalizable many : manies) {
			many.writeExternal(out);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oc.io.help.TestExternalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		manies.clear();
		super.readExternal(in);
		final int amount = in.readInt();
		for (int i = 0; i < amount; ++i) {
			final TestManyExternalizable many = new TestManyExternalizable();
			many.readExternal(in);
			manies.add(many);
		}
	}
}
