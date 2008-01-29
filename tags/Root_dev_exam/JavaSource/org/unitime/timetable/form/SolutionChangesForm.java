/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.solver.interactive.SuggestionsModel;


/** 
 * @author Tomas Muller
 */
public class SolutionChangesForm extends ActionForm {
	private String iOp = null;
	private static String[] sReferences = {"Best Solution", "Initial Solution", "Selected Solution"};
	public static int sReferenceBest = 0;
	public static int sReferenceInitial = 1;
	public static int sReferenceSelected = 2;
	private String iReference = null;
	private boolean iSimpleMode = false;
	private boolean iReversedMode = false;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iReference = sReferences[UserData.getPropertyInt(request.getSession(),"SolutionChanges.reference", sReferenceInitial)];
		iSimpleMode = false; iReversedMode = false;
	}
	
	public void load(SuggestionsModel model) {
		iSimpleMode = model.getSimpleMode();
		iReversedMode = model.getReversedMode();
	}
	
	public void save(SuggestionsModel model) {
		model.setSimpleMode(getSimpleMode());
		model.setReversedMode(getReversedMode());
	}	
	
	public boolean getSimpleMode() { return iSimpleMode; }
	public void setSimpleMode(boolean simpleMode) { iSimpleMode = simpleMode; }
	public boolean getReversedMode() { return iReversedMode; }
	public void setReversedMode(boolean reversedMode) { iReversedMode = reversedMode; }
	public String getReference() {
		return iReference;
	}
	public void setReference(String reference) {
		iReference = reference;
	}
	public int getReferenceInt() {
		for (int i=0;i<sReferences.length;i++)
			if (sReferences[i].equals(iReference)) return i;
		return sReferenceBest;
	}
	public String[] getReferences() { return sReferences; }
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
}