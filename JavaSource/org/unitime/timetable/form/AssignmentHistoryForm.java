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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.solver.interactive.SuggestionsModel;


/** 
 * @author Tomas Muller
 */
public class AssignmentHistoryForm extends ActionForm {
	private static final long serialVersionUID = 3346374266252617072L;
	private String iOp = null;
	private boolean iSimpleMode = false;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; iSimpleMode = false;
	}

	public void load(SuggestionsModel model) {
		iSimpleMode = model.getSimpleMode();
	}
	
	public void save(SuggestionsModel model) {
		model.setSimpleMode(getSimpleMode());
	}	
	
	public boolean getSimpleMode() { return iSimpleMode; }
	public void setSimpleMode(boolean simpleMode) { iSimpleMode = simpleMode; }
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
}

