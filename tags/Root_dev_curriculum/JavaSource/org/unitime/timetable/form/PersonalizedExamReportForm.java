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

public class PersonalizedExamReportForm extends ActionForm {
    /**
	 * 
	 */
	private static final long serialVersionUID = 9166328961282253491L;
	private String iOp = null;
    private boolean iCanExport = false;
    private boolean iAdmin = false;
    private boolean iLogout = false;
    private String iMessage = null;
    private String iUid = null;
    private String iFname = null;
    private String iMname = null;
    private String iLname = null;
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        iOp = null;
        iCanExport = false;
        iMessage = null;
        iUid = null;
        iAdmin = false;
    }
    
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public boolean getCanExport() { return iCanExport; }
    public void setCanExport(boolean canExport) { iCanExport = canExport;}
    public String getMessage() { return iMessage; }
    public void setMessage(String m) { iMessage = m; }
    public boolean getAdmin() { return iAdmin; }
    public void setAdmin(boolean admin) { iAdmin = admin; }
    public boolean getLogout() { return iLogout; }
    public void setLogout(boolean logout) { iLogout = logout; }
    public String getUid() { return iUid; }
    public void setUid(String uid) { iUid = uid; }
    public String getFname() { return iFname; }
    public void setFname(String fname) { iFname = fname; }
    public String getMname() { return iMname; }
    public void setMname(String mname) { iMname = mname; }
    public String getLname() { return iLname; }
    public void setLname(String lname) { iLname = lname; }
}
