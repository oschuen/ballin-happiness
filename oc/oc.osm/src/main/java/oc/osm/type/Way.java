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

/**
 * Ways are used for streets, rivers etc. as well as for simple areas like lakes
 * and so on. Each Way contains a list of Nodes, where Nodes can be stored
 * multiple times in one way, for example in case of a roundabout. Like Nodes
 * each way has its unique ID and can have several properties that describe the
 * usage of it.
 * 
 * @see <a href="https://wiki.openstreetmap.org/wiki/Way">Way</a> for further
 *      details
 * 
 * 
 * @author oliver
 */
public class Way implements Externalizable {
	protected long id = 0;
	protected List<Node> nodes = new ArrayList<>();
	protected final Map<String, String> props = new HashMap<String, String>();
	protected final Set<String> keySet = props.keySet();

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
	 * Adds a Node to the List of nodes.
	 * 
	 * @param ref
	 *            node that is referenced by this way
	 */
	public void addNode(final Node ref) {
		nodes.add(ref);
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
	 * @return the nodes
	 */
	public List<Node> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	public void setNodes(final List<Node> nodes) {
		this.nodes.clear();
		this.nodes.addAll(nodes);
	}

	/**
	 * @return true when way contains no nodes
	 */
	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	/**
	 * @return extension of this way
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public Bounds getBounds() {
		Bounds bounds = null;
		for (final Node node : nodes) {
			if (bounds == null) {
				bounds = new Bounds(node.getLat(), node.getLon(), node.getLat(), node.getLon());
			} else {
				bounds.increaseBounds(node.getLat(), node.getLon());
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
		out.writeInt(nodes.size());
		for (final Node node : nodes) {
			node.writeExternal(out, false);
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
		nodes.clear();
		id = in.readLong();
		int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final String key = StreamIo.readString(in);
			final String value = StreamIo.readString(in);
			props.put(key, value);
		}
		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final Node node = new Node();
			node.readExternal(in);
			nodes.add(node);
		}
	}

	/**
	 * Factory that creates instances of Way
	 * 
	 * @author oliver
	 */
	public static class WayFactory implements ExternalizableFactory<Way> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see oc.io.ExternalizableFactory#construct()
		 */
		@Override
		public Way construct() {
			return new Way();
		}
	}
}
