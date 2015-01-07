/**
 * Copyright (C) 2014 Oliver Schünemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WIExternalizableHOUExternalizable ANY WARRANExternalizableY; 
 * without even the implied warranty of MERCHANExternalizableABILIExternalizableY or FIExternalizableNESS FOR A PARExternalizableICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 19.07.2014
 * @version 1.0
 * @author oliver
 */
package oc.osm.type;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import oc.io.StreamIo;

/**
 * Part of Relation that contains either a node, way or other relation.
 * 
 * @author oliver
 */
public class Member implements Externalizable {

	public enum Type {
		NODE, WAY, RELATION, MAX
	};

	private static Type types[] = Type.values();

	private Externalizable ref = null;
	private String role = "";
	private Type type = Type.MAX;

	public Member() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		StreamIo.writeString(out, role);
		out.writeByte(type.ordinal());
		if (!Type.MAX.equals(type)) {
			ref.writeExternal(out);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		role = StreamIo.readString(in);
		type = types[in.readByte()];
		switch (type) {
		case NODE:
			ref = new Node();
			break;
		case WAY:
			ref = new Way();
			break;
		case RELATION:
			ref = new Relation();
			break;
		default:
			ref = null;
		}
		if (ref != null) {
			ref.readExternal(in);
		}

	}

	/**
	 * @return the ref
	 */
	public Externalizable getRef() {
		return ref;
	}

	/**
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(final String role) {
		this.role = role;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param ref
	 *            the ref to set
	 */
	public void setRef(final Externalizable ref) {
		if (ref instanceof Node) {
			type = Type.NODE;
		} else if (ref instanceof Way) {
			type = Type.WAY;
		} else if (ref instanceof Relation) {
			type = Type.RELATION;
		} else {
			type = Type.MAX;
		}
		this.ref = ref;
	}
}
