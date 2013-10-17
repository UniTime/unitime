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
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
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
    
    public void load(SessionContext session) {
        super.load(session);
        setReverse("1".equals(session.getUser().getProperty("ExamChanges.reverse", "0")));
        setChangeType(session.getUser().getProperty("ExamChanges.changeType", sChangeInitial));
    }
        
    public void save(SessionContext session) {
        super.save(session);
        session.getUser().setProperty("ExamChanges.reverse", getReverse() ? "1" : "0");
        session.getUser().setProperty("ExamChanges.changeType", getChangeType());
    }
    
    public boolean getNoSolver() { return iNoSolver;}
    public void setNoSolver(boolean noSolver) { iNoSolver = noSolver;}
}
