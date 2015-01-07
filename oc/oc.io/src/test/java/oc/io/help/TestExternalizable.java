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
package oc.io.help;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import oc.io.ExternalizableFactory;

/**
 * @author oliver
 * 
 */
public class TestExternalizable implements Externalizable {

	private int myId = 0;

	public TestExternalizable() {
		super();
	}

	public TestExternalizable(final int myId) {
		super();
		this.myId = myId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeInt(myId);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		myId = in.readInt();
	}

	/**
	 * @return the myId
	 */
	public int getMyId() {
		return myId;
	}

	/**
	 * @param myId
	 *            the myId to set
	 */
	public void setMyId(final int myId) {
		this.myId = myId;
	}

	/**
	 * @return a factory that produces TestExternalizable
	 */
	public static ExternalizableFactory<TestExternalizable> getTestExternalizableFactory() {
		return new ExternalizableFactory<TestExternalizable>() {
			@Override
			public TestExternalizable construct() {
				return new TestExternalizable();
			}
		};
	}

	/**
	 * @return a Comparator that allows to sort TestExternalizable in ascending
	 *         order
	 */
	public static Comparator<TestExternalizable> getTestExternalizableComparator() {
		return new Comparator<TestExternalizable>() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(java.lang.Object,
			 * java.lang.Object)
			 */
			@Override
			public int compare(final TestExternalizable o1, final TestExternalizable o2) {
				if (o1 == null) {
					return o2 == null ? 0 : -1;
				} else if (o2 == null) {
					return 1;
				}
				return Integer.valueOf(o1.getMyId()).compareTo(Integer.valueOf(o2.getMyId()));
			}
		};

	}
}
