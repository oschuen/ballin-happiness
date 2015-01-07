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

import oc.io.ExternalizableFilter;
import oc.io.ExternalizableFilter.ID;
import oc.io.ExternalizableFilter.IDComparator;
import oc.io.ExternalizableFilter.IDFactory;
import oc.io.ExternalizableFilter.SourceHandler;
import oc.io.ExternalizableIterator;
import oc.io.ExternalizableSorter;
import oc.io.ExternalizableWriter;
import oc.io.ReferenceResolver;
import oc.io.ReferenceResolver.ReferedHandler;
import oc.io.ReferenceResolver.RefererHandler;
import oc.osm.type.Member;
import oc.osm.type.Relation;
import oc.osm.type.Relation.RelationFactory;
import oc.osm.type.Way;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Finds all Ways belonging to a Relation and copies them to it. This class is
 * also able to drop ways from the way file, that are contained within a
 * relation. This is useful, because otherwise ways are contained twice in the
 * resulting database, once as member of the relation and once in the way file.
 * 
 * @author oliver
 */
public final class RelationWayResolver {
	private static final int MAX_SIZE = 10000;
	private static final Logger logger = LogManager.getLogger(RelationWayResolver.class.getName());

	/**
	 * Wrapper for Ways, that resolves everything the algorithm needs to know
	 * about them.
	 * 
	 * @see oc.io.ReferenceResolver.ReferedHandler
	 */
	private final ReferedHandler<Way> manyHandler = new ReferedHandler<Way>() {

		@Override
		public long getId(final Way many) {
			return many.getId();
		}

		@Override
		public void setId(final Way many, final long id) {
			many.setId(id);
		}
	};

	/**
	 * Wrapper for Relations, that resolves everything the algorithm needs to
	 * know about them.
	 * 
	 * @see oc.io.ReferenceResolver.RefererHandler
	 */
	private final RefererHandler<Relation, Way> oneHandler = new RefererHandler<Relation, Way>() {

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
			for (final Member way : one.getWays()) {
				refs.add(Long.valueOf(((Way) way.getRef()).getId()));
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
		public void setResolvedRefs(final Relation one, final Map<Long, Way> manies) {
			for (final Member wayMember : one.getWays()) {
				final Way way = (Way) wayMember.getRef();
				final Way resolvedWay = manies.get(Long.valueOf(way.getId()));
				if (resolvedWay == null) {
					logger.warn("Way not resolved : {}", way.getId());
				} else {
					way.setProps(resolvedWay.getProps());
					way.setNodes(resolvedWay.getNodes());
				}
			}
		}
	};

	private RelationWayResolver() {
		super();
	}

	/**
	 * Creates an ID File containing the IDs of all ways that are referenced by
	 * relations. This is needed for filtering them out of the way file
	 * 
	 * @param idFile
	 *            destination file for the IDs
	 * @param relationFile
	 *            File containing the relations referencing the ways where the
	 *            IDs shall be written to the files from.
	 * @param tempDir
	 *            Directory for intermediate results
	 * @return number Of Ids from Ways found in the relations file
	 * @throws IOException
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private int generateIdFile(final File idFile, final File relationFile, final File tempDir)
			throws IOException {
		final ExternalizableIterator<Relation> relationIter = new ExternalizableIterator<>(
				relationFile, new RelationFactory());
		final ExternalizableWriter<ID> idWriter = new ExternalizableWriter<>(idFile);
		int idCount = 0;
		// write the IDs of referred ways to the id file
		while (relationIter.hasNext()) {
			final Relation relation = relationIter.next();
			for (final Member wayMember : relation.getWays()) {
				idCount++;
				final ID id = new ID();
				id.setId(((Way) wayMember.getRef()).getId());
				idWriter.writeExternalizable(id);
			}
		}
		idWriter.close();
		relationIter.close();
		if (idCount > 1) {
			// Sort the ID file in ascending order
			final ExternalizableSorter<ID> idSorter = new ExternalizableSorter<>(idFile, tempDir,
					new IDFactory(), new IDComparator());
			idSorter.process();
		}
		return idCount;
	}

	/**
	 * Inner method doing the real filtering job
	 * 
	 * @param destFile
	 *            File where the not referenced Ways shall be stored to
	 * @param relationFile
	 *            File containing the relations referencing the ways
	 * @param tempDir
	 *            Directory for intermediate results
	 * @throws IOException
	 */
	private void removeRelationWays(final File destFile, final File relationFile,
			final File wayFile, final File tempDir) throws IOException {
		final File idFile = File.createTempFile("wayids", "id", tempDir);
		generateIdFile(idFile, relationFile, tempDir);
		ExternalizableFilter.filter(wayFile, destFile, idFile, new Way.WayFactory(),
				new SourceHandler<Way>() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * oc.io.ExternalizableFilter.SourceHandler#getId
					 * (de .yamap.mm.io.Externalizable)
					 */
					@Override
					public long getId(final Way instance) {
						return instance.getId();
					}
				}, false);
		idFile.delete();
	}

	/**
	 * Inner Method doing the real resolving job
	 * 
	 * @param destFile
	 *            File where resolved References shall be stored to
	 * @param relationFile
	 *            File containing the relations referencing the ways
	 * @param tempDir
	 *            Directory for intermediate results
	 * @throws IOException
	 */
	private void resolveWays1(final File destFile, final File relationFile, final File wayFile,
			final File tempDir) throws IOException {
		ReferenceResolver.resolveReferences(destFile, relationFile, wayFile, tempDir,
				new Relation.RelationFactory(), new Way.WayFactory(), manyHandler, oneHandler,
				MAX_SIZE);
	}

	/**
	 * Finds all ways the relations reference to and copies them to them.
	 * 
	 * @param destFile
	 *            File where resolved References shall be stored to
	 * @param relationFile
	 *            File containing the relations referencing the ways
	 * @param tempDir
	 *            Directory for intermediate results
	 * @throws IOException
	 */
	public static void resolveWays(final File destFile, final File relationFile,
			final File wayFile, final File tempDir) throws IOException {
		final RelationWayResolver resolver = new RelationWayResolver();
		resolver.resolveWays1(destFile, relationFile, wayFile, tempDir);
	}

	/**
	 * Filters out of the way file all ways, that are referenced by a relation.
	 * 
	 * @param destFile
	 *            File where the not referenced Ways shall be stored to
	 * @param relationFile
	 *            File containing the relations referencing the ways
	 * @param tempDir
	 *            Directory for intermediate results
	 * @throws IOException
	 */
	public static void dropRelatedWays(final File destFile, final File relationFile,
			final File wayFile, final File tempDir) throws IOException {
		final RelationWayResolver resolver = new RelationWayResolver();
		resolver.removeRelationWays(destFile, relationFile, wayFile, tempDir);
	}
}
