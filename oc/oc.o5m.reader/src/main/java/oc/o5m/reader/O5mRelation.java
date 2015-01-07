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
 * @since 16.09.2014
 * @version 1.0
 * @author oliver
 */
package oc.o5m.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import oc.osm.type.Member;
import oc.osm.type.Member.Type;
import oc.osm.type.Node;
import oc.osm.type.Relation;
import oc.osm.type.Way;

/**
 * helper for O5mReader to read relations from o5m files. This is needed because
 * some informations within the stream are given only relative like relationId
 * etc.
 * 
 * @author oliver
 * 
 */
public class O5mRelation extends O5mDataObject {

	private long lastRelationId;
	private long relationId;
	private final long lastReference[] = new long[Type.MAX.ordinal()];
	private final List<Reference> refs = new ArrayList<>();

	/**
	 * reads the next relation from the stream
	 */
	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void readFromBuffer(final ByteBuffer buffer, final O5mReader reader) throws IOException {
		super.readFromBuffer(buffer, reader);
		refs.clear();
		lastRelationId = relationId = lastRelationId + reader.readSigned64(buffer);
		if (buffer.hasRemaining()) {
			super.readUidAuthorTSFromBuffer(buffer, reader);
		} else {
			return;
		}
		if (buffer.hasRemaining()) {
			final int sizeOfRefs = reader.readUnsigned32(buffer);
			final int refstart = buffer.position();
			while (buffer.position() < sizeOfRefs + refstart) {
				refs.add(new Reference(buffer, reader));
			}
		} else {
			return;
		}
		super.readProps(buffer, reader);
	}

	/**
	 * Must be called when a reset flag is within file to reset the stored
	 * values for relative data
	 */
	@Override
	public void reset() {
		lastRelationId = 0;
		relationId = 0;
		for (int i = 0; i < lastReference.length; ++i) {
			lastReference[i] = 0;
		}
		refs.clear();
		super.reset();
	}

	/**
	 * Converts the read informations to a Relation instance for further
	 * processing
	 * 
	 * @return Relation matching the last read O5MRelation
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public Relation getAsRelation() {
		final Relation relation = new Relation();
		relation.setId(relationId);
		relation.setProps(props);
		for (final Reference reference : refs) {
			final Member member = new Member();
			member.setRole(reference.getRole());
			switch (reference.getType()) {
			case NODE:
				final Node node = new Node();
				node.setId(reference.getId());
				member.setRef(node);
				break;
			case WAY:
				final Way way = new Way();
				way.setId(reference.getId());
				member.setRef(way);
				break;
			case RELATION:
				final Relation rel = new Relation();
				rel.setId(reference.getId());
				member.setRef(rel);
				break;
			default:
				continue;
			}
			relation.addMember(member);
		}
		return relation;
	}

	/**
	 * @return the relation
	 */
	public long getRelationId() {
		return relationId;
	}

	/**
	 * this corresponds to the member node within osm xml data
	 * 
	 * @return the refs
	 */
	public List<Reference> getRefs() {
		return refs;
	}

	/**
	 * Member of a Relation, that can be Nodes, Ways or other Relations.
	 * 
	 * @author oliver
	 */
	public class Reference {
		private final long id;
		private final Type type;
		private final String role;

		/**
		 * Constructor reading the Reference from the given ByteBuffer.
		 * 
		 * @param buffer
		 *            buffer containing a Member
		 * @param reader
		 *            active O4mReader that contains stored history
		 * @throws IOException
		 */
		public Reference(final ByteBuffer buffer, final O5mReader reader) throws IOException {
			final long deltaId = reader.readSigned64(buffer);
			final String rawRole = reader.readSingleString(buffer);
			final int rawType = rawRole.charAt(0) - 0x30;
			if (rawType >= 0 && rawType < Type.MAX.ordinal()) {
				type = Type.values()[rawType];
			} else {
				type = Type.MAX;
			}
			id = lastReference[type.ordinal()] += deltaId;
			role = rawRole.substring(1);
		}

		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}

		/**
		 * @return the type
		 */
		public Type getType() {
			return type;
		}

		/**
		 * @return the role
		 */
		public String getRole() {
			return role;
		}
	}
}
