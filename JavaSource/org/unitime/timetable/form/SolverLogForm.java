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
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.solver.ui.LogInfo;


/** 
 * @author Tomas Muller
 */
public class SolverLogForm extends ActionForm {
	private static final long serialVersionUID = -1547826903057198096L;
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
    private LogInfo[] iLogInfo = null;
    private String[] iOwnerName = null;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if(iLevel==null || iLevel.trim().length()==0)
            errors.add("level", new ActionMessage("errors.required", ""));
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iLevel = null; 
		iOp = null;
		iLogInfo = null;
		iOwnerName = null;
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
	public String getLog(int idx) {
		if (iLogInfo==null || idx<0 || idx>=iLogInfo.length || iLogInfo[idx]==null) return "";
		return iLogInfo[idx].getHtmlLog(getLevelInt(), true);
	}
	public String[] getOwnerNames() {
		return iOwnerName;
	}
	public void setOwnerNames(String[] ownerName) {
		iOwnerName = ownerName;
	}
	public int getNrLogs() {
		return (iLogInfo==null?0:iLogInfo.length);
	}
	public void setLogs(LogInfo[] logs) {
		iLogInfo = logs;
	}

}

