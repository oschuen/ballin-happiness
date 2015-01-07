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
 * @since 18.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.io.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;

import oc.io.StreamIo;

import org.junit.Test;

/**
 * @author oliver
 * 
 */
public class StreamIoTestCase {

	/**
	 * Test method for {@link oc.io.StreamIo#setDictionary(java.util.Set)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSetDictionary() throws IOException {
		final String testString = "Hello World";
		final String testString2 = "Hello You";
		StreamIo.setDictionary(new HashSet<String>(Arrays.asList(testString, testString2)));
		final ByteArrayOutputStream bao = new ByteArrayOutputStream();
		final ObjectOutput oos = new ObjectOutputStream(bao);
		StreamIo.writeString(oos, testString);
		oos.close();
		ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
		ObjectInput ooi = new ObjectInputStream(bai);
		assertTrue(ooi.readInt() < 0);
		ooi.close();
		bai = new ByteArrayInputStream(bao.toByteArray());
		ooi = new ObjectInputStream(bai);
		assertEquals(testString, StreamIo.readString(ooi));
		ooi.close();
		StreamIo.setDictionary(new HashSet<String>());
	}

	/**
	 * Test method for
	 * {@link oc.io.StreamIo#writeString(java.io.ObjectOutput, java.lang.String)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testWriteString() throws IOException {
		final String testString = "Hello Universe";
		final ByteArrayOutputStream bao = new ByteArrayOutputStream();
		final ObjectOutput oos = new ObjectOutputStream(bao);
		StreamIo.writeString(oos, testString);
		oos.close();
		final ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
		final ObjectInput ooi = new ObjectInputStream(bai);
		assertEquals(testString, StreamIo.readString(ooi));
		ooi.close();
	}
}
