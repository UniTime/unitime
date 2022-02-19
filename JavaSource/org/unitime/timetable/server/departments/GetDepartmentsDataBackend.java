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
import java.util.Iterator;
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
			d.setSessionId( dept.getSessionId());
			d.setCoursesFundingDepartmentsEnabled(ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()); 
			d.setAcademicSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
			d.setDeptCode(dept.getDeptCode());
			d.setUniqueId(dept.getUniqueId());
			d.setAbbreviation(dept.getAbbreviation());
			d.setName(dept.getName());
			d.setExternalManager(dept.isExternalManager().booleanValue());
			d.setExternalMgrAbbv(dept.getExternalMgrAbbv());
			d.setExternalMgrLabel(dept.getExternalMgrLabel());
			d.setTimetableManagersCount(!dept.getTimetableManagers().isEmpty()?dept.getTimetableManagers().size():0);
			d.setSubjectAreaCount(!dept.getSubjectAreas().isEmpty()?dept.getSubjectAreas().size():0);
			d.setRoomDeptCount(!dept.getRoomDepts().isEmpty()?dept.getRoomDepts().size():0);
			d.setDistributionPrefPriority(dept.getDistributionPrefPriority().intValue());
			d.setStatusTypeStr(dept.effectiveStatusType().getLabel());
			d.setStatusTypeCode(dept.effectiveStatusType().getReference());
			d.setAllowReqTime(dept.getAllowReqTime());
			d.setAllowReqRoom(dept.getAllowReqRoom());
			d.setAllowReqDistribution(dept.getAllowReqDistribution());
			d.setExternalFundingDept(dept.getExternalFundingDept());
			d.setCanEdit(context.hasPermission(dept, Right.DepartmentEdit));
			d.setCanDelete(context.hasPermission(dept, Right.DepartmentDelete));
			d.setCanChangeExtManager(context.hasPermission(dept, Right.DepartmentEditChangeExternalManager));
			ArrayList<String> dependentStatuses = dependentStatuses(dept);			
			d.setDependentStatusesStr(dependentStatusesAsStr(dept));
			d.setInheritInstructorPreferences(dept.isInheritInstructorPreferences()) ;
			d.setAllowEvents(dept.isAllowEvents()) ;
			d.setAllowStudentScheduling(dept.isAllowStudentScheduling());
			d.setLastChangeStr(lastChangeStr(dept, context));
			d.setDependentDepartments(dependentDepartments(dept));
			d.setDependentStatuses(dependentStatuses);	
			d.setSatusOptions(getStatusOptions());
			d.setExtDepartmentOptions(getAllDependentDepartmentOptions(context));
			d.setExternalId(dept.getExternalUniqueId());
			d.setCanEdit(context.hasPermission(dept, Right.DepartmentEdit));
			response.addDepartment(d);
		}
		
		return response;
	}
	public ArrayList<String> dependentStatuses(Department dept){
		ArrayList<String> dependentStatuses = new ArrayList<String>();
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
        	}
        }
        return dependentStatuses;
	}

	/*
	 * for display in UI
	 */
	public List <String> dependentStatusesAsStr(Department dept){
        List dependentStatuses =  new ArrayList<String>();;
        if (dept.isExternalManager() && dept.getExternalStatusTypes() != null && !dept.getExternalStatusTypes().isEmpty()) {
        	TreeSet<ExternalDepartmentStatusType> set = new TreeSet<ExternalDepartmentStatusType>(new Comparator<ExternalDepartmentStatusType>() {
				@Override
				public int compare(ExternalDepartmentStatusType e1, ExternalDepartmentStatusType e2) {
					return e1.getDepartment().compareTo(e2.getDepartment());
				}
			});
        	set.addAll(dept.getExternalStatusTypes());
        	for (ExternalDepartmentStatusType t: set) {   
        		dependentStatuses.add(t.getDepartment().getDeptCode() + ": " +t.getStatusType().getLabel());
        	}
        }
        return dependentStatuses;
	}
		
	public ArrayList<String> dependentDepartments(Department dept){
		ArrayList<String> dependentDepartments = new ArrayList<String>();
        if (dept.isExternalManager() && dept.getExternalStatusTypes() != null && !dept.getExternalStatusTypes().isEmpty()) {
        	TreeSet<ExternalDepartmentStatusType> set = new TreeSet<ExternalDepartmentStatusType>(new Comparator<ExternalDepartmentStatusType>() {
				@Override
				public int compare(ExternalDepartmentStatusType e1, ExternalDepartmentStatusType e2) {
					return e1.getDepartment().compareTo(e2.getDepartment());
				}
			});
        	set.addAll(dept.getExternalStatusTypes());
        	for (ExternalDepartmentStatusType t: set) {
        		dependentDepartments.add(t.getDepartment().getUniqueId().toString());     		
        	}
        }
        return dependentDepartments;
	}
	

	
	public String lastChangeStr(Department dept, SessionContext context){
        String lastChangeStr = null;
    	if (context.hasPermission(Right.HasRole) && CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges))) {
            List changes = ChangeLog.findLastNChanges(dept.getSession().getUniqueId(), null, null, dept.getUniqueId(), 1);
            ChangeLog lastChange = (changes==null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
            lastChangeStr = (lastChange==null?"":ChangeLog.sDFdate.format(lastChange.getTimeStamp())+" by "+lastChange.getManager().getShortName());
   	}
        return lastChangeStr;
	}
	

	
	public HashMap<String, String>  getStatusOptions() { 
		ReferenceList ref = new ReferenceList();
		ref.addAll(DepartmentStatusType.findAllForDepartment());
		HashMap<String,String> map = new HashMap<String,String>();
		for (Iterator it = ref.iterator(); it.hasNext();) {
			RefTableEntry r = (RefTableEntry) it.next();
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
}
