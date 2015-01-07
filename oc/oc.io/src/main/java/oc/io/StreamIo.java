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
 * @since 03.07.2014
 * @version 1.0
 * @author oliver
 */
package oc.io;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * StreamIo is a helper to store String efficiently to an ObjectOutput. The
 * major difference is that it supports a dictionary of often used Strings, that
 * can be stored with a low footprint
 * 
 * @author oliver
 */
public final class StreamIo {

	private StreamIo() {

	}

	private static Map<String, Integer> dictionary = new HashMap<String, Integer>();
	private static List<String> dictList = new ArrayList<>();

	/**
	 * Sets a dictionary of often used text that can be stored more efficient
	 * afterwards.
	 * 
	 * @param dictionary
	 */
	public static void setDictionary(final Set<String> dictionary) {
		StreamIo.dictionary.clear();
		StreamIo.dictList.clear();
		int i = 1;
		for (final String string : dictionary) {
			StreamIo.dictionary.put(string, Integer.valueOf(i++));
			dictList.add(string);
		}
	}

	/**
	 * Writes a String to an ObjectOutput
	 * 
	 * @param out
	 *            the destination for the string
	 * @param str
	 *            the string that shall be stored
	 * @throws IOException
	 */
	public static void writeString(final ObjectOutput out, final String str) throws IOException {
		final byte b[] = str.getBytes();
		final Integer id = dictionary.get(str);
		if (id == null) {
			out.writeInt(b.length);
			out.write(b);
		} else {
			out.writeInt(-id.intValue());
		}
	}

	/**
	 * opposite function for write string. This method reads a string that was
	 * written with writeString
	 * 
	 * @param in
	 *            source from which the string will be read.
	 * @return the string
	 * @throws IOException
	 */
	public static String readString(final ObjectInput in) throws IOException {
		final int length = in.readInt();
		if (length < 0) {
			return dictList.get(-(length + 1));
		} else {
			final byte b[] = new byte[length];
			in.readFully(b);
			return new String(b);
		}
	}

	public static void writeInt(final ObjectOutput out, final int i) throws IOException {
		final byte b[] = new byte[5];
		byte count = 1;
		byte flag = 0;
		int li = i;
		if (li < 0) {
			flag |= 0x01;
			li = -li;
		}
		for (int j = 0; j < 4; ++j) {
			final byte c = (byte) (li & 0xff);
			if (c != 0) {
				b[count] = c;
				count++;
				flag |= 1 << count;
			}
			li = li >>> 8;
		}
		b[0] = flag;
		out.write(b, 0, count);
	}
}
