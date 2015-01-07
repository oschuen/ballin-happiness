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
 * @since 20.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import oc.io.ExternalizableFactory;
import oc.io.ExternalizableIterator;
import oc.io.ExternalizableWriter;
import oc.osm.type.Member;
import oc.osm.type.Node;
import oc.osm.type.Relation;
import oc.osm.type.Way;
import oc.resolve.supp.RelationNodeResolver;
import oc.resolve.supp.RelationRelationResolver;
import oc.resolve.supp.RelationWayResolver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// import static org.junit.Assert.*;

/**
 * @author oliver
 * 
 */
public class ResolverTestCase {

	private File relationFile = null;
	private File wayFile = null;
	private File nodeFile = null;
	private final Random random = new Random(System.nanoTime());
	private final int numberOfRelations = 10000;
	private final int numberOfWays = 5000;
	private final int numberOfNodes = 5000;
	private final List<Relation> allRelation = new ArrayList<>();
	private final ExternalizableFactory<Relation> relationFactory = new Relation.RelationFactory();

	@Before
	public void setUp() throws IOException {
		relationFile = File.createTempFile("relation", "dat");
		wayFile = File.createTempFile("way", "dat");
		nodeFile = File.createTempFile("node", "dat");
		allRelation.clear();
		final List<Relation> firstLevel = new ArrayList<>();
		final List<Relation> secondLevel = new ArrayList<>();
		final List<Relation> thirdLevel = new ArrayList<>();
		final List<Way> ways = new ArrayList<>();
		final List<Node> nodes = new ArrayList<>();
		for (int i = 0; i < numberOfRelations; ++i) {
			final int level = random.nextInt(3);
			final Relation relation = new Relation();
			relation.setId(i);
			relation.addProperty("Resolved", "true");
			allRelation.add(relation);
			if (level == 0) {
				firstLevel.add(relation);
			} else if (level == 1) {
				secondLevel.add(relation);
			} else {
				thirdLevel.add(relation);
			}
		}
		for (int i = 0; i < numberOfWays; ++i) {
			final Way way = new Way();
			way.setId(i);
			way.addProperty("Resolved", "true");
			ways.add(way);
		}
		for (int i = 0; i < numberOfNodes; ++i) {
			final Node node = new Node();
			node.setId(i);
			node.addProperty("Resolved", "true");
			nodes.add(node);
		}
		for (final Relation relation : firstLevel) {
			addMember(relation, secondLevel);
		}
		for (final Relation relation : firstLevel) {
			addMember(relation, ways);
		}
		for (final Relation relation : firstLevel) {
			addMember(relation, nodes);
		}
		for (final Relation relation : secondLevel) {
			addMember(relation, ways);
		}
		for (final Relation relation : thirdLevel) {
			addMember(relation, ways);
		}
		for (final Relation relation : secondLevel) {
			addMember(relation, thirdLevel);
		}
		for (final Relation relation : secondLevel) {
			addMember(relation, nodes);
		}
		for (final Relation relation : thirdLevel) {
			addMember(relation, nodes);
		}
		final ExternalizableWriter<Relation> relationWriter = new ExternalizableWriter<>(
				relationFile);
		for (final Relation relation : allRelation) {
			relationWriter.writeExternalizable(relation);
		}
		relationWriter.close();
		final ExternalizableWriter<Way> wayWriter = new ExternalizableWriter<>(wayFile);
		for (final Way way : ways) {
			wayWriter.writeExternalizable(way);
		}
		wayWriter.close();
		final ExternalizableWriter<Node> nodeWriter = new ExternalizableWriter<>(nodeFile);
		for (final Node node : nodes) {
			nodeWriter.writeExternalizable(node);
		}
		nodeWriter.close();
	}

	private void addMember(final Relation relation,
			final List<? extends Externalizable> possibleMember) {
		final int count = random.nextInt(6);
		final List<Externalizable> members = new ArrayList<>(possibleMember);
		for (int i = 0; i < count && !members.isEmpty(); ++i) {
			final Externalizable addMember = members.remove(random.nextInt(members.size()));
			final Member member = new Member();
			final Externalizable ref;
			if (addMember instanceof Node) {
				final Node refNode = new Node();
				refNode.setId(((Node) addMember).getId());
				ref = refNode;
			} else if (addMember instanceof Way) {
				final Way refWay = new Way();
				refWay.setId(((Way) addMember).getId());
				ref = refWay;
			} else {
				final Relation refRelation = new Relation();
				refRelation.setId(((Relation) addMember).getId());
				ref = refRelation;
			}
			member.setRef(ref);
			relation.addMember(member);
		}
	}

	@After
	public void tearDown() {
		relationFile.delete();
		wayFile.delete();
		nodeFile.delete();
	}

	/**
	 * Examins a relation that all its Members of Type Relatoin are resolved
	 * correctly and that the order of them is not destroyed.
	 * 
	 * @param relation
	 */
	private void examinRelation(final Relation relation) {
		final Relation orig = allRelation.get((int) relation.getId());
		assertNotNull(orig);
		final Iterator<Member> memberIter = relation.getMembers().iterator();
		for (final Member origMember : orig.getMembers()) {
			assertTrue(memberIter.hasNext());
			final Member member = memberIter.next();
			assertEquals(origMember.getType(), member.getType());
			if (Member.Type.RELATION.equals(member.getType())) {
				final Relation origChildRelation = (Relation) origMember.getRef();
				final Relation childRelation = (Relation) member.getRef();
				assertEquals(childRelation.getId(), origChildRelation.getId());
				assertEquals("true", childRelation.getProperty("Resolved"));
				examinRelation(childRelation);
			}
		}
	}

	/**
	 * Examins a relation that all its Members of Type Way are resolved
	 * correctly and that the order of them is not destroyed.
	 * 
	 * @param relation
	 */
	private void examinWay(final Relation relation) {
		final Relation orig = allRelation.get((int) relation.getId());
		assertNotNull(orig);
		final Iterator<Member> memberIter = relation.getMembers().iterator();
		for (final Member origMember : orig.getMembers()) {
			assertTrue(memberIter.hasNext());
			final Member member = memberIter.next();
			assertEquals(origMember.getType(), member.getType());
			if (Member.Type.WAY.equals(member.getType())) {
				final Way origChildWay = (Way) origMember.getRef();
				final Way childWay = (Way) member.getRef();
				assertEquals(childWay.getId(), origChildWay.getId());
				assertEquals("true", childWay.getProperty("Resolved"));
			}
		}
	}

	/**
	 * Examins a relation that all its Members of Type Node are resolved
	 * correctly and that the order of them is not destroyed.
	 * 
	 * @param relation
	 */
	private void examinNode(final Relation relation) {
		final Relation orig = allRelation.get((int) relation.getId());
		assertNotNull(orig);
		final Iterator<Member> memberIter = relation.getMembers().iterator();
		for (final Member origMember : orig.getMembers()) {
			assertTrue(memberIter.hasNext());
			final Member member = memberIter.next();
			assertEquals(origMember.getType(), member.getType());
			if (Member.Type.NODE.equals(member.getType())) {
				final Node origChildNode = (Node) origMember.getRef();
				final Node childNode = (Node) member.getRef();
				assertEquals(childNode.getId(), origChildNode.getId());
				assertEquals("true", childNode.getProperty("Resolved"));
			}
		}
	}

	/**
	 * Test method for
	 * {@link oc.resolve.supp.RelationRelationResolver#resolve(java.io.File, java.io.File)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testResolveRelations() throws IOException {
		final File tempDir = File.createTempFile("relationtemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		RelationRelationResolver.resolve(relationFile, tempDir);
		final ExternalizableIterator<Relation> externalizableIterator = new ExternalizableIterator<>(
				relationFile, relationFactory);

		while (externalizableIterator.hasNext()) {
			final Relation relation = externalizableIterator.next();
			examinRelation(relation);
		}
		tempDir.delete();
	}

	/**
	 * Test method for
	 * {@link oc.resolve.supp.RelationWayResolver#resolveWays(java.io.File, java.io.File, java.io.File, java.io.File)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testResolveWay() throws IOException {
		final File destFile = File.createTempFile("resolved", "dat");
		final File tempDir = File.createTempFile("relationtemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		RelationWayResolver.resolveWays(destFile, relationFile, wayFile, tempDir);
		final ExternalizableIterator<Relation> externalizableIterator = new ExternalizableIterator<>(
				destFile, relationFactory);

		while (externalizableIterator.hasNext()) {
			final Relation relation = externalizableIterator.next();
			examinWay(relation);
		}
		tempDir.delete();
		destFile.delete();
	}

	/**
	 * Test method for
	 * {@link oc.resolve.supp.RelationWayResolver#dropRelatedWays(java.io.File, java.io.File, java.io.File, java.io.File)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDropRelated() throws IOException {
		final Set<Long> wayIds = new TreeSet<>();
		final File destFile = File.createTempFile("resolved", "dat");
		final File tempDir = File.createTempFile("relationtemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		RelationWayResolver.dropRelatedWays(destFile, relationFile, wayFile, tempDir);
		final ExternalizableIterator<Relation> relationIter = new ExternalizableIterator<>(
				destFile, relationFactory);
		while (relationIter.hasNext()) {
			final Relation relation = relationIter.next();
			for (final Member member : relation.getMembers()) {
				if (Member.Type.WAY.equals(member.getType())) {
					wayIds.add(Long.valueOf(((Way) member.getRef()).getId()));
				}
			}
		}
		final ExternalizableIterator<Way> wayIter = new ExternalizableIterator<>(destFile,
				new Way.WayFactory());
		while (wayIter.hasNext()) {
			assertFalse(wayIds.contains(wayIter.next().getId()));
		}
		tempDir.delete();
		destFile.delete();
	}

	/**
	 * Test method for
	 * {@link oc.resolve.supp.RelationNodeResolver#resolveNodes(java.io.File, java.io.File, java.io.File, java.io.File)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testResolveNode() throws IOException {
		final File destFile = File.createTempFile("resolved", "dat");
		final File tempDir = File.createTempFile("relationtemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		RelationNodeResolver.resolveNodes(destFile, relationFile, nodeFile, tempDir);
		final ExternalizableIterator<Relation> externalizableIterator = new ExternalizableIterator<>(
				destFile, relationFactory);

		while (externalizableIterator.hasNext()) {
			final Relation relation = externalizableIterator.next();
			examinNode(relation);
		}
		tempDir.delete();
		destFile.delete();
	}
}
