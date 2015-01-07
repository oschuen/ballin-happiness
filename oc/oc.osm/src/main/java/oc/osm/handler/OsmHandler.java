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
 * @since 11.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.osm.handler;

import java.io.IOException;

import oc.osm.type.Node;
import oc.osm.type.Relation;
import oc.osm.type.Way;

/**
 * Handler or Observer that is used for every source file reader.
 * 
 * @author oliver
 */
public interface OsmHandler {

	/**
	 * Called once for every node in the source file
	 * 
	 * @param node
	 * @throws IOException
	 */
	void newNode(Node node) throws IOException;

	/**
	 * Called once for every way in the source file
	 * 
	 * @param way
	 * @throws IOException
	 */
	void newWay(Way way) throws IOException;

	/**
	 * Called once for every relation in the source file
	 * 
	 * @param relation
	 * @throws IOException
	 */
	void newRelation(Relation relation) throws IOException;
}
