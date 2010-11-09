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
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.UserData;

public class ExamChangesForm extends ExamReportForm {
	private static final long serialVersionUID = 4093360180461644275L;
	private String iChage = sChangeInitial;
    public static final String sChangeInitial = "Initial";
    public static final String sChangeBest = "Best";
    public static final String sChangeSaved = "Saved";
    public static final String[] sChanges = new String[] { sChangeInitial, sChangeBest, sChangeSaved }; 
    private boolean iReverse = false;
    private boolean iNoSolver = false;
    
    public boolean getReverse() { return iReverse; }
    public void setReverse(boolean reverse) { iReverse = reverse; }
    public String getChangeType() { return iChage; }
    public void setChangeType(String changeType) { iChage = changeType; }
    public String[] getChangeTypes() { return sChanges; }
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        iChage = sChangeInitial; iReverse = false;
    }
    
    public void load(HttpSession session) {
        super.load(session);
        setReverse(UserData.getPropertyBoolean(session,"ExamChanges.reverse", false));
        setChangeType(UserData.getProperty(session,"ExamChanges.changeType", sChangeInitial));
    }
        
    public void save(HttpSession session) {
        super.save(session);
        UserData.setPropertyBoolean(session,"ExamChanges.reverse", getReverse());
        UserData.setProperty(session,"ExamChanges.changeType", getChangeType());
    }
    
    public boolean getNoSolver() { return iNoSolver;}
    public void setNoSolver(boolean noSolver) { iNoSolver = noSolver;}
}
