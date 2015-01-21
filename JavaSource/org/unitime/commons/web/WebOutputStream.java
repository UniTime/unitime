/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
