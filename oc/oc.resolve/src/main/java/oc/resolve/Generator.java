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
 * @since 27.09.2014
 * @version 1.0
 * @author oliver
 */
package oc.resolve;

import java.io.File;
import java.io.IOException;

import oc.io.ExternalizableWriter;
import oc.o5m.reader.O5mReader;
import oc.osm.handler.OsmHandler;
import oc.osm.type.Node;
import oc.osm.type.Relation;
import oc.osm.type.Way;
import oc.resolve.supp.RelationNodeResolver;
import oc.resolve.supp.RelationRelationResolver;
import oc.resolve.supp.RelationWayResolver;
import oc.resolve.supp.WayNodeResolver;
import oc.sax.reader.SaxReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class that brings all the resolver for ways nodes and relations together
 * and calls them in the correct order to resolve all of them. As a result there
 * will be 4 files in the output folder:
 * <ul style="list-style-type:disc">
 * <li>nodes.dat contains all nodes in the source file</li>
 * <li>resways.dat contains all resolved ways. That means all nodes belonging to
 * a way have correct coordinates set.</li>
 * <li>resfiltways.dat contains all resolved ways like the resways.dat file, but
 * the ways that belong to a relation are filtered out</li>
 * <li>resfiltways.dat contains all resolved ways like the resways.dat file, but
 * the ways that belong to a relation are filtered out</li>
 * <li>resrelation.dat contains all resolved relations. That means the ways and
 * nodes are completely copied from the source.
 * </ul>
 * 
 * @author oliver
 * 
 */
public class Generator {

	protected final File dataPath;
	protected final File tempPath;
	protected final File nodeFile;
	protected final File simpleNodeFile;
	protected final File wayFile;
	protected final File relationFile;
	protected final File resolvedWayFile;
	protected final File resolvedFilteredWayFile;

	protected final File resolvedRelationFile;
	protected final File tempRelationFile;

	private static final Logger logger = LogManager.getLogger(Generator.class.getName());

	/**
	 * Constructor configuring output and temp path.
	 * 
	 * @param dataPath
	 *            this folder will be used as output folder. All the resulting
	 *            files will be stored here
	 * @param tempPath
	 *            this will be used for intermediate results.
	 * @throws IOException
	 */
	public Generator(final File dataPath, final File tempPath) throws IOException {
		this.dataPath = dataPath;
		this.tempPath = tempPath;
		nodeFile = new File(dataPath, "nodes.dat");
		simpleNodeFile = new File(tempPath, "snodes.dat");
		wayFile = new File(dataPath, "ways.dat");
		relationFile = new File(dataPath, "relations.dat");
		resolvedWayFile = new File(dataPath, "resways.dat");
		resolvedFilteredWayFile = new File(dataPath, "resfiltways.dat");
		tempRelationFile = new File(tempPath, "temprelation.dat");
		resolvedRelationFile = new File(dataPath, "resrelation.dat");
	}

	/**
	 * this resolves the complete source file. Resolving means that all
	 * references ways and relations have are replaced by the referenced
	 * element. For ways that are nodes and for relations that are nodes, ways
	 * and other relations.
	 * 
	 * @param f
	 *            source file either in o5m or osm.gz format
	 * @param xmlFile
	 *            true when the source file is in osm.gz format
	 * @throws IOException
	 */
	public void readFile(final File f, final boolean xmlFile) throws IOException {
		final ExternalizableWriter<Node> nodeWriter = new ExternalizableWriter<>(nodeFile);
		final ExternalizableWriter<Node> sNodeWriter = new ExternalizableWriter<>(simpleNodeFile);
		final ExternalizableWriter<Way> wayWriter = new ExternalizableWriter<>(wayFile);
		final ExternalizableWriter<Relation> relationWriter = new ExternalizableWriter<>(
				relationFile);
		final LocalOsmHandler handler = new LocalOsmHandler(nodeWriter, sNodeWriter, wayWriter,
				relationWriter);
		logger.info("Start Reading source file");
		if (xmlFile) {
			SaxReader.readFile(f, handler);
		} else {
			final O5mReader o5mReader = new O5mReader();
			o5mReader.readFile(f, handler);
		}
		nodeWriter.close();
		sNodeWriter.close();
		wayWriter.close();
		relationWriter.close();
		logger.info("Resolve Nodes for Ways");
		WayNodeResolver.resolveNodes(resolvedWayFile, wayFile, simpleNodeFile, tempPath);
		logger.info("Resolve Nodes for Relations");
		RelationNodeResolver.resolveNodes(tempRelationFile, relationFile, simpleNodeFile, tempPath);
		logger.info("Resolve Ways for Relations");
		RelationWayResolver.resolveWays(resolvedRelationFile, tempRelationFile, resolvedWayFile,
				tempPath);
		logger.info("Resolve Relations for Relations");
		RelationRelationResolver.resolve(resolvedRelationFile, tempPath);
		logger.info("Drop Ways that are contained in Relations");
		RelationWayResolver.dropRelatedWays(resolvedFilteredWayFile, resolvedRelationFile,
				resolvedWayFile, tempPath);
		logger.info("Tidy up");
		simpleNodeFile.delete();
		wayFile.delete();
		relationFile.delete();
		tempRelationFile.delete();
		logger.info("Finished");
	}

	/**
	 * Callback handler from input file reader. This can handle the callbacks
	 * from O5M and OSM handler.
	 * 
	 * @author oliver
	 */
	private static class LocalOsmHandler implements OsmHandler {
		private final ExternalizableWriter<Node> nodeWriter;
		private final ExternalizableWriter<Node> sNodeWriter;
		private final ExternalizableWriter<Way> wayWriter;
		private final ExternalizableWriter<Relation> relationWriter;
		private final Node secondNode = new Node();

		public LocalOsmHandler(final ExternalizableWriter<Node> nodeWriter,
				final ExternalizableWriter<Node> sNodeWriter,
				final ExternalizableWriter<Way> wayWriter,
				final ExternalizableWriter<Relation> relationWriter) {
			super();
			this.nodeWriter = nodeWriter;
			this.sNodeWriter = sNodeWriter;
			this.wayWriter = wayWriter;
			this.relationWriter = relationWriter;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see oc.osm.handler.OsmHandler#newNode(oc.osm.type.Node)
		 */
		@Override
		public void newNode(final Node node) {
			try {
				secondNode.setId(node.getId());
				secondNode.setLat(node.getLat());
				secondNode.setLon(node.getLon());
				nodeWriter.writeExternalizable(node);
				sNodeWriter.writeExternalizable(secondNode);
			} catch (final IOException e) {
				logger.error("Unable to write Node", e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see oc.osm.handler.OsmHandler#newWay(oc.osm.type.Way)
		 */
		@Override
		public void newWay(final Way way) {
			try {
				wayWriter.writeExternalizable(way);
			} catch (final IOException e) {
				logger.error("Unable to write Way", e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see oc.osm.handler.OsmHandler#newRelation(oc.osm.type.Relation)
		 */
		@Override
		public void newRelation(final Relation relation) {
			try {
				relationWriter.writeExternalizable(relation);
			} catch (final IOException e) {
				logger.error("Unable to write Relation", e);
			}
		}
	}
}
