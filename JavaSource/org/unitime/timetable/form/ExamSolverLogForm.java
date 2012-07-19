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
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;


/** 
 * @author Tomas Muller
 */
public class ExamSolverLogForm extends ActionForm {
	private static final long serialVersionUID = -2461992974709948396L;
	public static String[] sLevels = new String[] {
		"Trace",
		"Debug",
		"Progress",
		"Info",
		"Stage",
		"Warn",
		"Error",
		"Fatal"
	};
    private String iLevel;
    private String iOp;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if(iLevel==null || iLevel.trim().length()==0)
            errors.add("level", new ActionMessage("errors.required", ""));
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iLevel = null; 
		iOp = null;
	}

	public String getLevel() { return (iLevel==null?"Info":iLevel); }
	public String getLevelNoDefault() { return iLevel; }
	public int getLevelInt() {
		for (int i=0;i<sLevels.length;i++)
			if (getLevel().equals(sLevels[i])) return i;
		return 0;
	}
	public void setLevel(String level) { iLevel = level; }
	public String[] getLevels() { return sLevels; }
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
}

