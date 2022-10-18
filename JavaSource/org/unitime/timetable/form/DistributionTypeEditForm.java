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

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.PreferenceLevel;


/** 
 * @author Tomas Muller
 */
public class DistributionTypeEditForm implements UniTimeForm {
    private static final long serialVersionUID = 3252210646873060656L;

    private DistributionType iDistributionType;
    private String iOp = null;
    private List<Long> iDepartmentIds = new ArrayList<Long>();
    private Long iDepartmentId;

	public DistributionTypeEditForm() {
		reset();
	}
	
	@Override
	public void reset() {
		iOp = null; iDistributionType = new DistributionType();
		iDepartmentId = null; iDepartmentIds.clear();
	}
	
	@Override
	public void validate(UniTimeAction action) {
	}
	
	public void setDistributionType(DistributionType distributionType, Long sessionId) {
		iDistributionType = distributionType;
		iDepartmentIds.clear();
		for (Department d: distributionType.getDepartments(sessionId)) {
			iDepartmentIds.add(d.getUniqueId());
		}
	}
	
	public DistributionType getDistributionType() {
		return iDistributionType;
	}
	
	public void setSequencingRequired(boolean sequencingRequired){
	    iDistributionType.setSequencingRequired(sequencingRequired);
	}
	
	public boolean isSequencingRequired(){
	    return(iDistributionType.isSequencingRequired());
	}
	public void setRequirementId(String requirementId){
	    Integer reqId = Integer.valueOf(requirementId);
	    iDistributionType.setRequirementId(reqId);
	}
	
	public String getRequirementId(){
	    if (iDistributionType.getRequirementId() == null){
	        return ("");
	    } else {
	        return(iDistributionType.getRequirementId().toString());
	    }
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp=op; }
	public String getAbbreviation() {
		return iDistributionType.getAbbreviation();
	}
	public void setAbbreviation(String abbreviation) {
		iDistributionType.setAbbreviation(abbreviation);
	}
	public boolean isInstructorPref() {
		return (iDistributionType.isInstructorPref()==null?false:iDistributionType.isInstructorPref().booleanValue());
	}
	public void setInstructorPref(boolean instructorPref) {
		iDistributionType.setInstructorPref(Boolean.valueOf(instructorPref));
	}
    public boolean isExamPref() {
        return (iDistributionType.isExamPref()==null?false:iDistributionType.isExamPref().booleanValue());
    }
    public void setExamPref(boolean examPref) {
        iDistributionType.setExamPref(Boolean.valueOf(examPref));
    }
	public String getAllowedPref() {
		return iDistributionType.getAllowedPref();
	}
	public void setAllowedPref(String allowedPref) {
		iDistributionType.setAllowedPref(allowedPref);
	}
	public String getDescription() {
		return iDistributionType.getDescr();
	}
	public void setDescription(String description) {
		iDistributionType.setDescr(description);
	}
	public boolean isVisible() {
		return iDistributionType.isVisible();
	}
	public void setVisible(boolean visible) {
		iDistributionType.setVisible(visible);
	}
	
	public Long getUniqueId() {
		return iDistributionType.getUniqueId();
	}
	public void setUniqueId(Long uniqueId) {
		iDistributionType.setUniqueId(uniqueId);
	}
	public String getReference() {
		return iDistributionType.getReference();
	}
	public void setReference(String reference) {
		iDistributionType.setReference(reference);
	}
	public String getLabel() {
		return iDistributionType.getLabel();
	}
	public void setLabel(String label) {
		iDistributionType.setLabel(label);
	}
	
	public List<Long> getDepartmentIds() { return iDepartmentIds; }
	public void setDepartmentIds(List<Long> departmentIds) { iDepartmentIds = departmentIds; }
	public Long getDepartmentIds(int idx) { return iDepartmentIds.get(idx); }
	public void setDepartmentIds(int idx, Long departmentId) { iDepartmentIds.add(idx, departmentId); }
	public Long getDepartmentId() { return iDepartmentId; }
	public void setDepartmentId(Long deptId) { iDepartmentId = deptId; }
	
	public Boolean getAllowedPreference(int prefId) {
		PreferenceLevel pref = PreferenceLevel.getPreferenceLevel(prefId);
		return iDistributionType.getAllowedPref() != null && iDistributionType.getAllowedPref().indexOf(PreferenceLevel.prolog2char(pref.getPrefProlog())) >= 0;
	}
	
	public void setAllowedPreference(int prefId, Boolean value) {
		PreferenceLevel pref = PreferenceLevel.getPreferenceLevel(prefId);
		String original = iDistributionType.getAllowedPref();
		String allowedPref = "";
		for (PreferenceLevel p: PreferenceLevel.getPreferenceLevelList()) {
			char ch = PreferenceLevel.prolog2char(p.getPrefProlog());
			if (p.equals(pref)) {
				if (value)
					allowedPref += ch;
			} else {
				if (original != null && original.indexOf(ch) >= 0)
					allowedPref += ch;
			}
		}
		iDistributionType.setAllowedPref(allowedPref);
	}
}
