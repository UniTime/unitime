/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.commons.web;

/** Simple extension of java.io.OutputStream. \n character is replaced by &lt;br&gt;
 *
 * @author Tomas Muller
 */
public class WebOutputStream extends java.io.OutputStream {

	/** buffer */
	StringBuffer iBuffer = null;

	/** constructor */
	public WebOutputStream() {
		super();
		iBuffer = new StringBuffer();
	}

	/** writes a byte to stream */
	public void write(int b) throws java.io.IOException {
		if (b == '\n') {
			iBuffer.append("<br>");
		}
		iBuffer.append((char) b);
	}

	/** returns content -- characters \n are replaced by tag &lt;br&gt; */
	public String toString() {
		return iBuffer.toString();
	}

}
