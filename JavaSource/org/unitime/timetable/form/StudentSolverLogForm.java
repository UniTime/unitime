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
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.solver.ui.LogInfo;


/** 
 * @author Tomas Muller
 */
public class StudentSolverLogForm extends ActionForm {
	private static final long serialVersionUID = 4418486419309519223L;
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
    private LogInfo iLogInfo = null;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if(iLevel==null || iLevel.trim().length()==0)
            errors.add("level", new ActionMessage("errors.required", ""));
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iLevel = UserData.getProperty(request.getSession(), "SolverLog.level", null); 
		iOp = null;
		iLogInfo = null;
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
	public String getLog() {
		if (iLogInfo==null) return "";
		return iLogInfo.getHtmlLog(getLevelInt(), true);
	}
}

