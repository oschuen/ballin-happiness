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
 * @since 21.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import oc.io.ExternalizableFactory;
import oc.io.ExternalizableIterator;
import oc.io.ExternalizableWriter;
import oc.osm.type.Node;
import oc.osm.type.Way;
import oc.resolve.supp.WayNodeResolver;

import org.junit.Before;
import org.junit.Test;

/**
 * @author oliver
 * 
 */
public class WayResolverTestCase {

	private File wayFile = null;
	private File nodeFile = null;
	private final Random random = new Random(System.nanoTime());
	private final int numberOfWays = 5000;
	private final int numberOfNodes = 5000;
	private final List<Way> allWay = new ArrayList<>();
	private final ExternalizableFactory<Way> wayFactory = new Way.WayFactory();

	@Before
	public void setUp() throws IOException {
		wayFile = File.createTempFile("way", "dat");
		nodeFile = File.createTempFile("node", "dat");
		allWay.clear();
		final List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < numberOfWays; ++i) {
			final Way way = new Way();
			way.setId(i);
			allWay.add(way);
		}
		for (int i = 0; i < numberOfNodes; ++i) {
			final Node node = new Node();
			node.setLat(i);
			node.setLon(i + 1);
			node.setId(i);
			node.addProperty("Resolved", "true");
			nodes.add(node);
		}
		for (final Way way : allWay) {
			addMember(way, nodes);
		}
		final ExternalizableWriter<Way> wayWriter = new ExternalizableWriter<>(wayFile);
		for (final Way way : allWay) {
			wayWriter.writeExternalizable(way);
		}
		wayWriter.close();
		final ExternalizableWriter<Node> nodeWriter = new ExternalizableWriter<>(nodeFile);
		for (final Node node : nodes) {
			nodeWriter.writeExternalizable(node);
		}
		nodeWriter.close();
	}

	private void addMember(final Way way, final List<Node> possibleMember) {
		final int count = 4 + random.nextInt(40);
		Node closeNode = null;
		for (int i = 0; i < count; ++i) {
			final Node addMember = possibleMember.get(random.nextInt(possibleMember.size()));
			final Node refNode = new Node();
			refNode.setId(addMember.getId());
			way.addNode(refNode);
			if (i == 0 && random.nextInt(4) == 1) {
				closeNode = new Node();
				closeNode.setId(addMember.getId());
				way.addNode(refNode);
			}
		}
		if (closeNode != null) {
			way.addNode(closeNode);
		}
	}

	/**
	 * Examins a way that all its referenced Nodes are resolved correctly and
	 * that the order of them is not destroyed.
	 * 
	 * @param way
	 */
	private void examinNode(final Way way) {
		final Way orig = allWay.get((int) way.getId());
		assertNotNull(orig);
		final Iterator<Node> nodeIter = way.getNodes().iterator();
		for (final Node origNode : orig.getNodes()) {
			assertTrue(nodeIter.hasNext());
			final Node node = nodeIter.next();
			assertEquals(origNode.getId(), node.getId());
			assertEquals(node.getId(), node.getLat());
			assertEquals(node.getLon(), node.getLat() + 1);
			// Way Node Resolver does not copy properties, so this should be
			// empty
			assertNull(node.getProperty("Resolved"));
		}
	}

	/**
	 * Test method for
	 * {@link oc.resolve.supp.WayNodeResolver#resolveNodes(java.io.File, java.io.File, java.io.File, java.io.File)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testResolveNodes() throws IOException {
		final File destFile = File.createTempFile("resolved", "dat");
		final File tempDir = File.createTempFile("waytemp", "");
		tempDir.delete();
		tempDir.mkdirs();

		WayNodeResolver.resolveNodes(destFile, wayFile, nodeFile, tempDir);
		final ExternalizableIterator<Way> externalizableIterator = new ExternalizableIterator<>(
				destFile, wayFactory);

		while (externalizableIterator.hasNext()) {
			final Way way = externalizableIterator.next();
			examinNode(way);
		}
		tempDir.delete();
		destFile.delete();
	}
}
