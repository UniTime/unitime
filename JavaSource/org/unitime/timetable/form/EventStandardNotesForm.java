/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.form;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.model.StandardEventNote;

/**
 * @author Zuzana Mullerova
 */
public class EventStandardNotesForm extends ActionForm {

	private String iOp;
	
	public ActionErrors validate(
			ActionMapping mapping,
			HttpServletRequest request) {

			return null;
		}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		
	}

	public String getTable() {

		WebTable table = new WebTable( 2,
					null, 
					new String[] {"Note", "Reference"}, 
		    	    new String[] {"left", "left"}, 
		    	    new boolean[] {true, true});    
		
	    for (Iterator i=StandardEventNote.findAll().iterator();i.hasNext();) {
	    	StandardEventNote sen = (StandardEventNote) i.next();
			table.addLine(
		        "onclick=\"document.location='eventStandardNoteEdit.do?op=Edit&id="+sen.getUniqueId()+"';\"",
	        	new String[] {sen.getNote(), sen.getReference()},
	        	new Comparable[] {null, null});
		    }

	    return (table.getLines().isEmpty()?"":table.printTable());
	}
   
    
	public String getOp() {return iOp;}
	public void setOp(String op) {iOp = op;}
	
}
