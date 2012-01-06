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
