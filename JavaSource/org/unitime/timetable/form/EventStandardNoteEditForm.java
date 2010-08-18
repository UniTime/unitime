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
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.dao.StandardEventNoteDAO;

/**
 * @author Zuzana Mullerova
 */
public class EventStandardNoteEditForm extends ActionForm {

	private static final long serialVersionUID = 4715197445617849102L;
	private String iScreen = "edit";
	private String iNote;
	private String iReference;
	private String iOp;
	private StandardEventNote iStandardNote;
	private Long iId;
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();
		
		if (iNote==null || iNote.length()==0) {
			errors.add("note", new ActionMessage("errors.generic", "Please enter the text for the note."));
		} else {
			for (Iterator i=StandardEventNote.findAll().iterator(); i.hasNext();) {
				StandardEventNote sen2 = (StandardEventNote) i.next();
				if (iNote.compareToIgnoreCase(sen2.getNote())==0) {
					if (iId != null) {
						if (iId.compareTo(sen2.getUniqueId())!=0) {
							errors.add("noteExists", new ActionMessage("errors.generic", "Another standard note with this text already exists."));
							break;
						}
					} else {
						errors.add("orgNameExists", new ActionMessage("errors.generic", "Another standard note with this text already exists."));
						break;
					}
				}
			}
		}
		
		if (iReference==null || iReference.length()==0) {
			errors.add("note", new ActionMessage("errors.generic", "Please enter the reference."));
		} else {
			for (Iterator i=StandardEventNote.findAll().iterator(); i.hasNext();) {
				StandardEventNote sen2 = (StandardEventNote) i.next();
				if (iReference.compareToIgnoreCase(sen2.getReference())==0) {
					if (iId != null) {
						if (iId.compareTo(sen2.getUniqueId())!=0) {
							errors.add("referenceExists", new ActionMessage("errors.generic", "Another standard note with this reference already exists."));
							break;
						}
					} else {
						errors.add("orgNameExists", new ActionMessage("errors.generic", "Another standard note with this reference already exists."));
						break;
					}
				}
			}
		}
		
		
		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		if ("add".equals(request.getAttribute("op"))) {
			iScreen = "add";
			iStandardNote = null;
		} else if (request.getParameter("id")!=null && request.getParameter("id").length()>0) {
			iId = Long.valueOf(request.getParameter("id"));
			iStandardNote = StandardEventNoteDAO.getInstance().get(iId);
		}
		iNote=(iStandardNote==null?"":iStandardNote.getNote());
		iReference=(iStandardNote==null?"":iStandardNote.getReference());
	}
	
	public String getScreen() {return iScreen;}
	public void setScreen(String screen) {iScreen = screen;}

	public String getNote() {return iNote;}
	public void setNote(String note) {iNote = note;}
	
	public String getReference() {return iReference;}
	public void setReference(String reference) {iReference = reference;}
	
	public String getOp() {return iOp;}
	public void setOp(String op) {iOp = op;}
	
	public Long getId() {return iId;}
	public void setId(Long id) {iId = id;}
	
	public StandardEventNote getStandardNote() {return iStandardNote;}
	public void setStandardNote(StandardEventNote sen) {iStandardNote = sen;}
	
}
