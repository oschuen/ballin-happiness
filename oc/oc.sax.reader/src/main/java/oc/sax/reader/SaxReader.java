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
 * @since 14.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.sax.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import oc.osm.handler.OsmHandler;
import oc.osm.type.Member;
import oc.osm.type.Member.Type;
import oc.osm.type.Node;
import oc.osm.type.Relation;
import oc.osm.type.Way;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author oliver
 * 
 */
public final class SaxReader implements ContentHandler {

	/**
	 * 
	 */
	private static final String ROLE_ATTR_NAME = "role";
	/**
	 * 
	 */
	private static final String VALUE_ATTR_NAME = "v";
	/**
	 * 
	 */
	private static final String KEY_ATTR_NAME = "k";
	/**
	 * 
	 */
	private static final String TAG_NODE_NAME = "tag";
	/**
	 * 
	 */
	private static final String RELATION_NODE_NAME = "relation";
	/**
	 * 
	 */
	private static final String MEMBER_NODE_NAME = "member";
	/**
	 * 
	 */
	private static final String TYPE_ATTR_NAME = "type";
	/**
	 * 
	 */
	private static final String REF_ATTR_NAME = "ref";
	/**
	 * 
	 */
	private static final String LON_ATTR_NAME = "lon";
	/**
	 * 
	 */
	private static final String LAT_ATTR_NAME = "lat";
	/**
	 * 
	 */
	private static final String ID_ATTR_NAME = "id";
	/**
	 * 
	 */
	private static final String NODE_REF_NAME = "nd";
	/**
	 * 
	 */
	private static final String NODE_NODE_NAME = "node";
	/**
	 * 
	 */
	private static final String WAY_NODE_NAME = "way";
	private final OsmHandler handler;
	private Node node = null;
	private Way way = null;
	private Relation relation = null;
	private boolean relationActive = false;
	private boolean wayActive = false;
	private boolean nodeActive = false;
	private static final double scale = 1e7;
	private static final Logger logger = LogManager.getLogger(SaxReader.class.getName());

	private long numberOfNodes = 0;
	private long numberOfWays = 0;
	private long numberOfRelations = 0;
	private long numberOfNodeWayRefs = 0;
	private long numberOfNodeRelRefs = 0;
	private long numberOfWayRelRefs = 0;
	private long numberOfRelRelRefs = 0;

	private SaxReader(final OsmHandler handler) {
		this.handler = handler;
	}

	public static void readFile(final File f, final OsmHandler handler) throws IOException {
		final SaxReader saxReader = new SaxReader(handler);
		try {
			final XMLReader reader = XMLReaderFactory.createXMLReader();
			final GZIPInputStream gis = new GZIPInputStream(new FileInputStream(f));
			reader.setContentHandler(saxReader);
			reader.parse(new InputSource(gis));
			gis.close();
		} catch (final SAXException e) {
			throw new IOException("Error occured parsing Document", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes atts) throws SAXException {
		if (NODE_NODE_NAME.equals(localName)) {
			final long id = Long.parseLong(atts.getValue(ID_ATTR_NAME));
			final int lat = (int) (scale * Double.parseDouble(atts.getValue(LAT_ATTR_NAME)) + 0.5);
			final int lon = (int) (scale * Double.parseDouble(atts.getValue(LON_ATTR_NAME)) + 0.5);
			node = new Node();
			node.setId(id);
			node.setLat(lat);
			node.setLon(lon);
			nodeActive = true;
			numberOfNodes++;
		} else if (WAY_NODE_NAME.equals(localName)) {
			final long id = Long.parseLong(atts.getValue(ID_ATTR_NAME));
			way = new Way();
			way.setId(id);
			wayActive = true;
			numberOfWays++;
		} else if (NODE_REF_NAME.equals(localName) && wayActive) {
			final long ref = Long.parseLong(atts.getValue(REF_ATTR_NAME));
			final Node refNode = new Node();
			refNode.setId(ref);
			way.addNode(refNode);
			numberOfNodeWayRefs++;
		} else if (RELATION_NODE_NAME.equals(localName)) {
			final long id = Long.parseLong(atts.getValue(ID_ATTR_NAME));
			relationActive = true;
			relation = new Relation();
			relation.setId(id);
			atts.getLength();
			numberOfRelations++;
		} else if (MEMBER_NODE_NAME.equals(localName) && relationActive) {
			final Member member = new Member();
			final String type = atts.getValue(TYPE_ATTR_NAME);
			final long ref = Long.parseLong(atts.getValue(REF_ATTR_NAME));
			final String role = atts.getValue(ROLE_ATTR_NAME);
			if (NODE_NODE_NAME.equals(type)) {
				final Node node = new Node();
				node.setId(ref);
				member.setRef(node);
				numberOfNodeRelRefs++;
			} else if (WAY_NODE_NAME.equals(type)) {
				final Way way = new Way();
				way.setId(ref);
				member.setRef(way);
				numberOfWayRelRefs++;
			} else if (RELATION_NODE_NAME.equals(type)) {
				final Relation relation = new Relation();
				relation.setId(ref);
				member.setRef(relation);
				numberOfRelRelRefs++;
			}
			member.setRole(role == null ? "" : role);
			if (!Type.MAX.equals(member.getType())) {
				relation.addMember(member);
			}
		} else if (TAG_NODE_NAME.equals(localName)) {
			final String key = atts.getValue(KEY_ATTR_NAME);
			final String value = atts.getValue(VALUE_ATTR_NAME);
			if (nodeActive) {
				node.addProperty(key, value);
			} else if (wayActive) {
				way.addProperty(key, value);
			} else if (relationActive) {
				relation.addProperty(key, value);
			} else {
				throw new SAXException("Unexpected Tag '" + key + "'");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
		try {
			if (NODE_NODE_NAME.equals(localName) && nodeActive) {
				handler.newNode(node);
				nodeActive = false;
			} else if (WAY_NODE_NAME.equals(localName) && wayActive) {
				handler.newWay(way);
				wayActive = false;
			} else if (RELATION_NODE_NAME.equals(localName) && relationActive) {
				handler.newRelation(relation);
				relationActive = false;
			}
		} catch (final IOException e) {
			throw new SAXException("Unexpected IO Exception during parsing", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		logger.debug("Number of Nodes     : {}", numberOfNodes);
		logger.debug("Number of Ways      : {}", numberOfWays);
		logger.debug("Number of Relations : {}", numberOfRelations);
		logger.debug("Way Node References : {}", numberOfNodeWayRefs);
		logger.debug("Relation Refs Nodes : {}, Ways : {}, Relations : {}", numberOfNodeRelRefs,
				numberOfWayRelRefs, numberOfRelRelRefs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		// Not needed for OSM Data
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length)
			throws SAXException {
		// Not needed for OSM Data
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void processingInstruction(final String target, final String data) throws SAXException {
		// Not needed for OSM Data
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	@Override
	public void skippedEntity(final String name) throws SAXException {
		// Not needed for OSM Data
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	@Override
	public void setDocumentLocator(final Locator locator) {
		// Not needed for OSM Data
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		// Not needed for OSM Data
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
		// Not needed for OSM Data
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
		// Not needed for OSM Data
	}
}
