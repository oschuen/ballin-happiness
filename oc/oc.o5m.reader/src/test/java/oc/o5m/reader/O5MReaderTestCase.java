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
 * @since 19.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.o5m.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import oc.osm.handler.OsmHandler;
import oc.osm.type.Member;
import oc.osm.type.Node;
import oc.osm.type.Relation;
import oc.osm.type.Way;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * @author oliver
 * 
 */
public class O5MReaderTestCase {

	/**
	 * Test method for
	 * {@link oc.o5m.reader.O5mReader#readFile(java.io.File, oc.osm.handler.OsmHandler)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadFile() throws IOException {
		final O5mReader o5mReader = new O5mReader();
		final InputStream is = O5MReaderTestCase.class.getResourceAsStream("junit.o5m");
		final File f = File.createTempFile("junit_test", "o5m");
		FileUtils.copyInputStreamToFile(is, f);

		o5mReader.readFile(f, new OsmHandler() {

			@Override
			public void newWay(final Way way) throws IOException {
				assertEquals(2, way.getId());
				assertEquals(1, way.getNodes().size());
				final Node refNode = way.getNodes().get(0);
				assertEquals(1, refNode.getId());
				assertEquals("motorway", way.getProperty("highway"));
			}

			@Override
			public void newRelation(final Relation relation) throws IOException {
				assertEquals(3, relation.getId());
				assertEquals(1, relation.getMembers().size());
				final Member member = relation.getMembers().get(0);
				assertEquals(Member.Type.WAY, member.getType());
				final Way way = (Way) member.getRef();
				assertEquals(2, way.getId());
				assertEquals("inner", member.getRole());
				assertEquals("multipolygon", relation.getProperty("type"));
			}

			@Override
			public void newNode(final Node node) throws IOException {
				assertEquals(1, node.getId());
				assertTrue(520000000 < node.getLat());
				assertTrue(530000000 > node.getLat());
				assertTrue(130000000 < node.getLon());
				assertTrue(140000000 > node.getLon());
				assertEquals("Test Punkt", node.getProperty("name"));
			}
		});
	}

}
