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
 * @since 28.09.2014
 * @version 1.0
 * @author oliver
 */
package oc.osm.type;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oc.io.ExternalizableFactory;
import oc.io.StreamIo;
import oc.osm.type.Member.Type;

/**
 * Relations are used within OSM data to build complex structures like long
 * highways or forest areas. A Relation can contain Nodes, Ways and other
 * Relations.
 * 
 * @see <a href="https://wiki.openstreetmap.org/wiki/Relation">Relation</a> for
 *      further details
 * 
 * @author oliver
 */
public class Relation implements Externalizable {
	private long id = 0;
	private final List<Member> members = new ArrayList<>();
	private final Map<String, String> props = new HashMap<String, String>();
	private final Set<String> keySet = props.keySet();

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(final long id) {
		this.id = id;
	}

	/**
	 * adds a single Member to this relation
	 * 
	 * @param ref
	 *            referenced Node, Way or Relation packaged as member that is
	 *            referenced by this relation
	 */
	public void addMember(final Member ref) {
		members.add(ref);
	}

	/**
	 * adds a single property to this way
	 * 
	 * @param key
	 *            of property
	 * @param value
	 *            of property
	 */
	public void addProperty(final String key, final String value) {
		props.put(key, value);
	}

	/**
	 * 
	 * @param key
	 *            of property
	 * @return null when property key doesn't exist
	 */
	public String getProperty(final String key) {
		return props.get(key);
	}

	/**
	 * @param key
	 *            of property
	 * @param defaultValue
	 * @return defaultValue when key doesn't exist
	 */
	public String getProperty(final String key, final String defaultValue) {
		String ret = getProperty(key);
		if (ret == null) {
			ret = defaultValue;
		}
		return ret;
	}

	/**
	 * @return the props
	 */
	public Map<String, String> getProps() {
		return props;
	}

	/**
	 * @param props
	 *            the props to set
	 */
	public void setProps(final Map<String, String> props) {
		this.props.clear();
		this.props.putAll(props);
	}

	/**
	 * @return the nodes member contained within this relation
	 */
	public List<Member> getNodes() {
		final List<Member> nodes = new ArrayList<>();
		for (final Member member : members) {
			if (Type.NODE.equals(member.getType())) {
				nodes.add(member);
			}
		}
		return nodes;
	}

	/**
	 * @return the way member contained within this relation
	 */
	public List<Member> getWays() {
		final List<Member> ways = new ArrayList<>();
		for (final Member member : members) {
			if (Type.WAY.equals(member.getType())) {
				ways.add(member);
			}
		}
		return ways;
	}

	/**
	 * @return the Relation member contained within this relation
	 */
	public List<Member> getRelations() {
		final List<Member> relations = new ArrayList<>();
		for (final Member member : members) {
			if (Type.RELATION.equals(member.getType())) {
				relations.add(member);
			}
		}
		return relations;
	}

	/**
	 * @return all member
	 */
	public List<Member> getMembers() {
		return members;
	}

	/**
	 * replaces the existing members with the given ones
	 * 
	 * @param members
	 *            to copy to this list
	 */
	public void setMembers(final List<Member> members) {
		this.members.clear();
		this.members.addAll(members);
	}

	/**
	 * @return extension of this relation
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public Bounds getBounds() {
		Bounds bounds = null;
		for (final Member member : members) {
			switch (member.getType()) {
			case NODE:
				final Node node = (Node) member.getRef();
				if (bounds == null) {
					bounds = new Bounds(node.getLat(), node.getLon(), node.getLat(), node.getLon());
				} else {
					bounds.increaseBounds(node.getLat(), node.getLon());
				}
				break;
			case WAY:
				final Way way = (Way) member.getRef();
				final Bounds wayBounds = way.getBounds();
				if (wayBounds != null) {
					if (bounds == null) {
						bounds = new Bounds();
					} else {
						bounds.increaseBounds(way.getBounds());
					}
				}
				break;
			case RELATION:
				final Relation relation = (Relation) member.getRef();
				final Bounds relationBounds = relation.getBounds();
				if (relationBounds != null) {
					if (bounds == null) {
						bounds = new Bounds(relationBounds);
					} else {
						bounds.increaseBounds(relation.getBounds());
					}
				}
				break;
			default:
				break;
			}
		}
		return bounds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeInt(keySet.size());
		for (final String key : keySet) {
			StreamIo.writeString(out, key);
			StreamIo.writeString(out, props.get(key));
		}
		out.writeInt(members.size());
		for (final Member member : members) {
			member.writeExternal(out);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		props.clear();
		members.clear();
		id = in.readLong();
		int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final String key = StreamIo.readString(in);
			final String value = StreamIo.readString(in);
			props.put(key, value);
		}
		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final Member member = new Member();
			member.readExternal(in);
			members.add(member);
		}
	}

	/**
	 * Factory that creates instances of Relation
	 * 
	 * @author oliver
	 */
	public static class RelationFactory implements ExternalizableFactory<Relation> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see oc.io.ExternalizableFactory#construct()
		 */
		@Override
		public Relation construct() {
			return new Relation();
		}

	}
}
