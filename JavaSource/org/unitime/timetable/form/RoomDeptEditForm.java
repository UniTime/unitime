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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.form name="roomDeptEditForm"
 *
 * @author Tomas Muller
 */
public class RoomDeptEditForm extends ActionForm {

    private String iOp = null;
    private String iTable = null;
    private String iName = null;
	private Long iId = null;
	private Long iExamType = null;
	private Set<Long> iAssigned = new HashSet<Long>();

	private static final long serialVersionUID = 5225503750129395914L;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
	    iOp = null; iId = null; iExamType = null; iAssigned.clear();
	}

	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id;}
	public Long getExamType() { return iExamType; }
	public void setExamType(Long examType) { iExamType = examType; }
	public Set<Long> getAssignedSet() { return iAssigned; }
	public String[] getAssigned() { 
	    String[] ret = new String[iAssigned.size()];
	    int idx = 0;
	    for (Iterator i=iAssigned.iterator();i.hasNext();) {
	        ret[idx]=i.next().toString();
	    }
	    return ret;
	}
	public void setAssigned(String[] assigned) {
	    iAssigned.clear();
	    for (int i=0;i<assigned.length;i++)
	        iAssigned.add(Long.valueOf(assigned[i]));
	}
	public void setTable(String table) { iTable = table; }
	public String getTable() { return iTable; }
	public void setName(String name) { iName = name; }
	public String getName() { return iName; }
}

