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
 * @since 14.10.2014
 * @version 1.0
 * @author oliver
 */
package oc.resolve.supp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oc.io.ReferenceResolver;
import oc.io.ReferenceResolver.ReferedHandler;
import oc.io.ReferenceResolver.RefererHandler;
import oc.osm.type.Member;
import oc.osm.type.Node;
import oc.osm.type.Relation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Finds all Nodes belonging to a Relation and copies them to it.
 * 
 * @author oliver
 */
public final class RelationNodeResolver {
	private static final int MAX_SIZE = 10000;
	private static final Logger logger = LogManager.getLogger(RelationNodeResolver.class.getName());

	/**
	 * Wrapper for Nodes, that resolves everything the algorithm needs to know
	 * about them.
	 * 
	 * @see oc.io.ReferenceResolver.ReferedHandler
	 */
	private final ReferedHandler<Node> manyHandler = new ReferedHandler<Node>() {

		@Override
		public long getId(final Node many) {
			return many.getId();
		}

		@Override
		public void setId(final Node many, final long id) {
			many.setId(id);
		}
	};

	/**
	 * Wrapper for Relations, that resolves everything the algorithm needs to
	 * know about them.
	 * 
	 * @see oc.io.ReferenceResolver.RefererHandler
	 */
	private final RefererHandler<Relation, Node> oneHandler = new RefererHandler<Relation, Node>() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see oc.io.ReferenceResolver.RefererHandler#getId(java.lang.Object)
		 */
		@Override
		public long getId(final Relation one) {
			return one.getId();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see oc.io.ReferenceResolver.RefererHandler#getRefs(java.lang.Object)
		 */
		@Override
		public List<Long> getRefs(final Relation one) {
			final List<Long> refs = new ArrayList<>();
			for (final Member node : one.getNodes()) {
				refs.add(Long.valueOf(((Node) node.getRef()).getId()));
			}
			return refs;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * oc.io.ReferenceResolver.RefererHandler#setResolvedRefs(java.lang.
		 * Object, java.util.List)
		 */
		@Override
		public void setResolvedRefs(final Relation one, final Map<Long, Node> manies) {
			for (final Member nodeMember : one.getNodes()) {
				final Node node = (Node) nodeMember.getRef();
				final Node resolvedNode = manies.get(Long.valueOf(node.getId()));
				if (resolvedNode == null) {
					logger.warn("Node not resolved : {}", node.getId());
				} else {
					node.setProps(resolvedNode.getProps());
					node.setLat(resolvedNode.getLat());
					node.setLon(resolvedNode.getLon());
				}
			}
		}
	};

	private RelationNodeResolver() {
		super();
	}

	/**
	 * Inner Method doing the real resolving job
	 * 
	 * @param destFile
	 *            File where resolved References shall be stored to
	 * @param relationFile
	 *            File containing the relations referencing the nodes
	 * @param tempDir
	 *            Directory for intermediate results
	 * @throws IOException
	 */
	private void resolveNodes1(final File destFile, final File relationFile, final File nodeFile,
			final File tempDir) throws IOException {
		ReferenceResolver.resolveReferences(destFile, relationFile, nodeFile, tempDir,
				new Relation.RelationFactory(), new Node.NodeFactory(), manyHandler, oneHandler,
				MAX_SIZE);
	}

	/**
	 * Finds all nodes the relations reference to and copies them to them.
	 * 
	 * @param destFile
	 *            File where resolved References shall be stored to
	 * @param relationFile
	 *            File containing the relations referencing the nodes
	 * @param tempDir
	 *            Directory for intermediate results
	 * @throws IOException
	 */
	public static void resolveNodes(final File destFile, final File relationFile,
			final File nodeFile, final File tempDir) throws IOException {
		final RelationNodeResolver resolver = new RelationNodeResolver();
		resolver.resolveNodes1(destFile, relationFile, nodeFile, tempDir);
	}
}
