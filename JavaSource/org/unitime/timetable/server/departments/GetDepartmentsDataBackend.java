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
package org.unitime.timetable.server.departments;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.DepartmentInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentsDataResponse;
import org.unitime.timetable.gwt.shared.DepartmentInterface.GetDepartmentsRequest;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ReferenceList;

@GwtRpcImplements(GetDepartmentsRequest.class)
public class GetDepartmentsDataBackend implements GwtRpcImplementation<GetDepartmentsRequest, DepartmentsDataResponse>{
	
	@Override
	public DepartmentsDataResponse execute(GetDepartmentsRequest request, SessionContext context) {
		context.checkPermission(Right.Departments);		
		DepartmentsDataResponse response = new DepartmentsDataResponse();
		/*permission */
		response.setCanAdd(context.hasPermission(Right.DepartmentAdd));
		response.setCanExportPdf(context.hasPermission(Right.Departments));
		response.setFundingDeptEnabled(ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue());
 		
		/*department list */
		for (Department dept: Department.findAll(context.getUser().getCurrentAcademicSessionId())) {
			DepartmentInterface d = new DepartmentInterface();
			DependentDepartmentData dependentDeptData = new DependentDepartmentData(dept);
			d.setSessionId( dept.getSessionId());
			d.setAcademicSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			d.setDeptCode(dept.getDeptCode());
			d.setUniqueId(dept.getUniqueId());
			d.setAbbreviation(dept.getAbbreviation());
			d.setName(dept.getName());
			d.setExternalManager(dept.isExternalManager().booleanValue());
			d.setExternalMgrAbbv(dept.getExternalMgrAbbv());
			d.setExternalMgrLabel(dept.getExternalMgrLabel());
			d.setTimetableManagersCount(dept.getTimetableManagers().size());
			d.setSubjectAreaCount(dept.getSubjectAreas().size());
			d.setRoomDeptCount(dept.getRoomDepts().size());
			d.setDistributionPrefPriority(dept.getDistributionPrefPriority().intValue());
			d.setStatusTypeStr(dept.effectiveStatusType().getLabel());
			d.setStatusTypeCode(dept.effectiveStatusType().getReference());
			d.setAllowReqTime(dept.getAllowReqTime());
			d.setAllowReqRoom(dept.getAllowReqRoom());
			d.setAllowReqDistribution(dept.getAllowReqDistribution());
			d.setExternalFundingDept(dept.getExternalFundingDept());
			d.setDependentStatusesStr(dependentDeptData.getDependentDepartmentStatusStr());
			d.setInheritInstructorPreferences(dept.isInheritInstructorPreferences()) ;
			d.setAllowEvents(dept.isAllowEvents()) ;
			d.setAllowStudentScheduling(dept.isAllowStudentScheduling());
			d.setLastChangeStr(lastChangeStr(dept, context));
			d.setDependentDepartments(dependentDeptData.getDependentDepartments());
			d.setDependentStatuses(dependentDeptData.getDependentStatuses());	
			d.setExternalId(dept.getExternalUniqueId());
			response.addDepartment(d);
		}

		return response;
	}
	
	public String lastChangeStr(Department dept, SessionContext context){
        String lastChangeStr = null;
    	if (context.hasPermission(Right.HasRole) && CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges))) {
            List<ChangeLog> changes = ChangeLog.findLastNChanges(dept.getSession().getUniqueId(), null, null, dept.getUniqueId(), 1);
            ChangeLog lastChange = (changes==null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
            lastChangeStr = (lastChange==null?"":ChangeLog.sDFdate.format(lastChange.getTimeStamp())+" by "+lastChange.getManager().getShortName());
    	}
        return lastChangeStr;
	}
	
	
	public HashMap<String, String>  getStatusOptions() { 
		ReferenceList ref = new ReferenceList();
		ref.addAll(DepartmentStatusType.findAllForDepartment());
		HashMap<String,String> map = new HashMap<String,String>();
		for (RefTableEntry r : ref) {
			 map.put(r.getReference(),r.getLabel());
		}
		return map;
	}

	public HashMap<Long, String> getAllDependentDepartmentOptions( SessionContext context) { 
		TreeSet<Department> departments =  Department.findAllNonExternal(context.getUser().getCurrentAcademicSessionId());
		HashMap<Long, String> map = new HashMap<Long,String>();
		for (Department d: departments){
			  String deptCode = d.getDepartment().getDeptCode();
			  String deptName = d.getDepartment().getName();
			  String displayName = deptCode + " : " +deptName;
			  map.put(d.getDepartment().getUniqueId(),displayName);
		}
		return map;   
	}	
	protected class DependentDepartmentData {
        private ArrayList<String> dependentStatuses;
		private ArrayList<String> dependentDepartments;
        private ArrayList<String> dependentDepartmentStatusStr;
		protected DependentDepartmentData(Department dept) {
			dependentStatuses = new ArrayList<String>();
			dependentDepartments = new ArrayList<String>();
			dependentDepartmentStatusStr = new ArrayList<String>();
			
	        if (dept.isExternalManager() && dept.getExternalStatusTypes() != null && !dept.getExternalStatusTypes().isEmpty()) {
	        	TreeSet<ExternalDepartmentStatusType> set = new TreeSet<ExternalDepartmentStatusType>(new Comparator<ExternalDepartmentStatusType>() {
					@Override
					public int compare(ExternalDepartmentStatusType e1, ExternalDepartmentStatusType e2) {
						return e1.getDepartment().compareTo(e2.getDepartment());
					}
				});
	        	set.addAll(dept.getExternalStatusTypes());
	        	for (ExternalDepartmentStatusType t: set) {  
	        		dependentStatuses.add(t.getStatusType().getReference());
	        		dependentDepartmentStatusStr.add(t.getDepartment().getDeptCode() + ": " +t.getStatusType().getLabel());
	        		dependentDepartments.add(t.getDepartment().getUniqueId().toString());     		
	        	}
	        }
		}
		protected ArrayList<String> getDependentStatuses() {
			return dependentStatuses;
		}
		
		protected ArrayList<String> getDependentDepartments() {
			return dependentDepartments;
		}

		protected ArrayList<String> getDependentDepartmentStatusStr() {
			return dependentDepartmentStatusStr;
		}
	}
}
