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

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.RefTableEntry;


/** 
 * MyEclipse Struts
 * Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * @struts:form name="distributionTypeEditForm"
 *
 * @author Tomas Muller
 */
public class DistributionTypeEditForm extends RefTableEntryEditForm {
	String iOp = null;
    private Vector iDepartmentIds = new Vector();
    private Long iDepartmentId;
	
	/**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3252210646873060656L;

 	// --------------------------------------------------------- Instance Variables
	public DistributionTypeEditForm() {
	    super();
	    refTableEntry = new DistributionType();
	}
	// --------------------------------------------------------- Methods
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; refTableEntry = new DistributionType();
		iDepartmentId = null; iDepartmentIds.clear();
	}
	
	public void setRefTableEntry(RefTableEntry refTableEntry, Long sessionId) {
		super.setRefTableEntry(refTableEntry);
		DistributionType distType = (DistributionType)refTableEntry;
		iDepartmentIds.clear();
		for (Iterator i=distType.getDepartments(sessionId).iterator();i.hasNext();) {
			Department d = (Department)i.next();
			iDepartmentIds.add(d.getUniqueId());
		}
	}

	
	public void setSequencingRequired(boolean sequencingRequired){
	    ((DistributionType) refTableEntry).setSequencingRequired(sequencingRequired);
	}
	
	public boolean isSequencingRequired(){
	    return(((DistributionType) refTableEntry).isSequencingRequired());
	}
	public void setRequirementId(String requirementId){
	    Integer reqId = Integer.valueOf(requirementId);
	    ((DistributionType) refTableEntry).setRequirementId(reqId);
	}
	
	public String getRequirementId(){
	    if (((DistributionType) refTableEntry).getRequirementId() == null){
	        return ("");
	    } else {
	        return(((DistributionType) refTableEntry).getRequirementId().toString());
	    }
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp=op; }
	public String getAbbreviation() {
		return ((DistributionType)refTableEntry).getAbbreviation();
	}
	public void setAbbreviation(String abbreviation) {
		((DistributionType)refTableEntry).setAbbreviation(abbreviation);
	}
	public boolean isInstructorPref() {
		return (((DistributionType)refTableEntry).isInstructorPref()==null?false:((DistributionType)refTableEntry).isInstructorPref().booleanValue());
	}
	public void setInstructorPref(boolean instructorPref) {
		((DistributionType)refTableEntry).setInstructorPref(new Boolean(instructorPref));
	}
    public boolean isExamPref() {
        return (((DistributionType)refTableEntry).isExamPref()==null?false:((DistributionType)refTableEntry).isExamPref().booleanValue());
    }
    public void setExamPref(boolean examPref) {
        ((DistributionType)refTableEntry).setExamPref(new Boolean(examPref));
    }
	public String getAllowedPref() {
		return ((DistributionType)refTableEntry).getAllowedPref();
	}
	public void setAllowedPref(String allowedPref) {
		((DistributionType)refTableEntry).setAllowedPref(allowedPref);
	}
	public String getDescription() {
		return ((DistributionType)refTableEntry).getDescr();
	}
	public void setDescription(String description) {
		((DistributionType)refTableEntry).setDescr(description);
	}
	public boolean isVisible() {
		return ((DistributionType)refTableEntry).isVisible();
	}
	public void setVisible(boolean visible) {
		((DistributionType)refTableEntry).setVisible(visible);
	}
	
	public Vector getDepartmentIds() { return iDepartmentIds; }
	public void setDepartmentIds(Vector departmentIds) { iDepartmentIds = departmentIds; }
	public Long getDepartmentId() { return iDepartmentId; }
	public void setDepartmentId(Long deptId) { iDepartmentId = deptId; }
	
}
