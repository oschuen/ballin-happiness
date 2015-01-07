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

/**
 * @author oliver
 * 
 */
public class TestManyExternalizable extends TestExternalizable {
	private boolean resolved = false;

	public TestManyExternalizable() {
		super();
	}

	public TestManyExternalizable(final int myId, final boolean resolved) {
		super(myId);
		this.resolved = resolved;
	}

	/**
	 * @return the resolved
	 */
	public boolean isResolved() {
		return resolved;
	}

	/**
	 * @param resolved
	 *            the resolved to set
	 */
	public void setResolved(final boolean resolved) {
		this.resolved = resolved;
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
		out.writeBoolean(resolved);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see oc.io.help.TestExternalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		resolved = in.readBoolean();
	}
}
