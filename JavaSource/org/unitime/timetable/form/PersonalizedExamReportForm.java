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

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
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
    private Long iSessionId = null;
    
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
        iSessionId = null;
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
    public Long getSessionId() { return iSessionId; }
    public void setSessionId(Long sessionId) { iSessionId = (sessionId == null || sessionId == 0 ? null : sessionId); }
}
