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
