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
 * @since 17.10.2014
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resolves references within Relations to other Relations. When this class
 * finished processing a file of relation, each relation contains a complete
 * copy of the relations it refers to. All these resolved relations have copies
 * of the relations they refer to as well.
 * 
 * @author oliver
 */
public class RelationRelationResolver {
	private final File relationFile;
	private final File tempPath;
	private final static int MAX_SIZE = 1000;

	private static final Logger logger = LogManager.getLogger(RelationRelationResolver.class
			.getName());

	private final ReferedHandler<Relation> manyHandler = new ReferedHandler<Relation>() {

		@Override
		public long getId(final Relation many) {
			return many.getId();
		}

		@Override
		public void setId(final Relation many, final long id) {
			many.setId(id);
		}
	};

	private final RefererHandler<Relation, Relation> oneHandler = new RefererHandler<Relation, Relation>() {

		@Override
		public long getId(final Relation one) {
			return one.getId();
		}

		@Override
		public List<Long> getRefs(final Relation one) {
			final List<Long> refs = new ArrayList<>();
			for (final Member relation : one.getRelations()) {
				refs.add(Long.valueOf(((Relation) relation.getRef()).getId()));
			}
			return refs;
		}

		@Override
		public void setResolvedRefs(final Relation one, final Map<Long, Relation> manies) {
			for (final Member member : one.getMembers()) {
				if (Member.Type.RELATION.equals(member.getType())) {
					final Relation newRelation = manies.get(((Relation) member.getRef()).getId());
					if (!(newRelation == null || newRelation.getId() == one.getId())) {
						member.setRef(newRelation);
					}
				}
			}
		}
	};

	public RelationRelationResolver(final File relationFile, final File tempPath) {
		super();
		this.relationFile = relationFile;
		this.tempPath = tempPath;
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private int generateIdFile(final File idFile, final File relationFile) throws IOException {
		final ExternalizableIterator<Relation> relationIter = new ExternalizableIterator<>(
				relationFile, new RelationFactory());
		final ExternalizableWriter<ID> idWriter = new ExternalizableWriter<>(idFile);
		int idCount = 0;
		while (relationIter.hasNext()) {
			final Relation relation = relationIter.next();
			for (final Member relation2 : relation.getRelations()) {
				idCount++;
				final ID id = new ID();
				id.setId(((Relation) relation2.getRef()).getId());
				idWriter.writeExternalizable(id);
			}
		}
		idWriter.close();
		relationIter.close();
		if (idCount > 1) {
			final ExternalizableSorter<ID> idSorter = new ExternalizableSorter<>(idFile, tempPath,
					new IDFactory(), new IDComparator());
			idSorter.process();
		}
		return idCount;
	}

	private void filterRelations(final File idFile, final File sourceFile, final File destFile)
			throws IOException {
		ExternalizableFilter.filter(sourceFile, destFile, idFile, new RelationFactory(),
				new RelationSourceHandler(), true);
	}

	private File mergeRelations(final int count, final File someRelationsFile,
			final File manyRelationsFile) throws IOException {

		final File idFile = File.createTempFile("filter", "id", tempPath);
		final File resultFile = File.createTempFile("refered", "n" + count, tempPath);
		final int nextCount = generateIdFile(idFile, someRelationsFile);
		File tempSomeReleationFile = someRelationsFile;
		if (nextCount >= count) {
			logger.warn("Circular reference between Relations");
			idFile.delete();
		} else if (nextCount > 0) {
			final File destRelFile = File.createTempFile("refered", "rel", tempPath);
			filterRelations(idFile, someRelationsFile, destRelFile);
			tempSomeReleationFile = mergeRelations(nextCount, destRelFile, someRelationsFile);
			destRelFile.delete();
			idFile.delete();
		} else if (nextCount == 0) {
			idFile.delete();
		}
		ReferenceResolver.resolveReferences(resultFile, manyRelationsFile, tempSomeReleationFile,
				tempPath, new Relation.RelationFactory(), new Relation.RelationFactory(),
				manyHandler, oneHandler, MAX_SIZE);
		tempSomeReleationFile.delete();
		return resultFile;
	}

	private void resolve() throws IOException {
		final File idFile = File.createTempFile("filter", "id", tempPath);
		final File destRelFile = File.createTempFile("refered", "rel", tempPath);
		final int count = generateIdFile(idFile, relationFile);
		if (count > 0) {
			filterRelations(idFile, relationFile, destRelFile);
			final File resultFile = mergeRelations(count, destRelFile, relationFile);
			final ExternalizableIterator<Relation> relIter = new ExternalizableIterator<>(
					resultFile, new RelationFactory());
			final ExternalizableWriter<Relation> relWriter = new ExternalizableWriter<>(
					relationFile);
			while (relIter.hasNext()) {
				relWriter.writeExternalizable(relIter.next());
			}
			relWriter.close();
			relIter.close();
			resultFile.delete();
		}
		idFile.delete();
		destRelFile.delete();
	}

	public static void resolve(final File relationFile, final File tempPath) throws IOException {
		final RelationRelationResolver resolver = new RelationRelationResolver(relationFile,
				tempPath);
		resolver.resolve();
	}

	private static class RelationSourceHandler implements SourceHandler<Relation> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * oc.io.ExternalizableFilter.SoureHandler#getId(oc.io
		 * .Externalizable)
		 */
		@Override
		public long getId(final Relation instance) {
			return instance.getId();
		}
	}
}
