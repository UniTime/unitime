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


/**
 * @author Stephanie Schluttenhofer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestHtmlGen {

	/**
	 * 
	 */
	public TestHtmlGen() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String htmlOutput(){
		Table table = new Table();
		table.setBorder(3);
		table.setCellPadding(3);
		table.setCellSpacing(5);
		table.setBgColor("lightgrey");
		TableRow row = new TableRow();
		row.setBgColor("pink");
		row.setBorderColor("green");
		TableCell cell = new TableCell();
		cell.setBgColor("blue");
		cell.setBorderColor("red");
		cell.addContent("This is a test for cell 1.");
		cell.addContent("This should be in cell 1 as well");
		Span span = new Span();
		span.setTitle("span span span");
		span.addContent("this is my span");
		cell.addContent(span);
		cell.setAlign("center");
		row.addContent(cell);
		cell = new TableCell();
		cell.addContent("This is a test for cell 2.");
		cell.setNoWrap(true);
		cell.setAlign("right");
		cell.setValign("bottom");
		row.addContent(cell);
		table.addContent(row);
		row = new TableRow();
		cell = new TableCell();
		cell.addContent("This is a test for cell 1.");
		cell.addContent("This should be in cell 1 as well");
		cell.setAlign("center");
		row.addContent(cell);
		cell = new TableCell();
		cell.addContent("This is a test for cell 2.");
		cell.setNoWrap(true);
		cell.setAlign("right");
		cell.setValign("bottom");
		row.addContent(cell);
		table.addContent(row);
		return(table.toHtml());
	}

}
