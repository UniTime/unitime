/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
