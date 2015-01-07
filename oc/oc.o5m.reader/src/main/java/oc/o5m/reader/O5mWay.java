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

import oc.osm.type.Node;
import oc.osm.type.Way;

/**
 * Helper for O5mReader to read ways from o5m files. This is needed because some
 * informations within the stream are given only relative like wayId etc.
 * 
 * @author oliver
 */
public class O5mWay extends O5mDataObject {

	private long lastWayId;
	private long wayId;
	private long lastReference;
	private final List<Long> refs = new ArrayList<>();

	/**
	 * reads the next way from the stream
	 */
	@Override
	public void readFromBuffer(final ByteBuffer buffer, final O5mReader reader) throws IOException {
		super.readFromBuffer(buffer, reader);
		refs.clear();
		lastWayId = wayId = lastWayId + reader.readSigned64(buffer);
		if (buffer.hasRemaining()) {
			super.readUidAuthorTSFromBuffer(buffer, reader);
		} else {
			return;
		}
		if (buffer.hasRemaining()) {
			final int sizeOfRefs = reader.readUnsigned32(buffer);
			final int refstart = buffer.position();
			while (buffer.position() < sizeOfRefs + refstart) {
				lastReference += reader.readSigned64(buffer);
				refs.add(Long.valueOf(lastReference));
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
		lastWayId = 0;
		lastReference = 0;
		refs.clear();
		super.reset();
	}

	/**
	 * Converts the read informations to a Way instance for further processing
	 * 
	 * @return Way matching the last read O5MWay
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public Way getAsWay() {
		final Way way = new Way();
		way.setId(wayId);
		way.setProps(props);
		for (final Long nodeRef : refs) {
			final Node node = new Node();
			node.setId(nodeRef);
			way.addNode(node);
		}
		return way;
	}

	/**
	 * @return the wayId
	 */
	public long getWayId() {
		return wayId;
	}

	/**
	 * @return the refs
	 */
	public List<Long> getRefs() {
		return refs;
	}
}
