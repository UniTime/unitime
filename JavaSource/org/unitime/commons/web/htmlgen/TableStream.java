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
package org.unitime.commons.web.htmlgen;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;
/**
 * 
 * @author Stephanie Schluttenhofer
 *
 */
public class TableStream extends Table {
	JspWriter outStream;
	
	public TableStream() {
		super();
	}
	
	public TableStream(JspWriter out){
		super();
		outStream = out;
	}

	public JspWriter getOutStream() {
		return outStream;
	}

	public void setOutStream(JspWriter outStream) {
		this.outStream = outStream;
	}
	
	public void addContent(Object obj){
		try {
			getOutStream().print(htmlForObject(obj));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void tableDefComplete(){
		try {
			getOutStream().print(startTagHtml());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void tableComplete(){
		try {
			getOutStream().print(endTagHtml());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
