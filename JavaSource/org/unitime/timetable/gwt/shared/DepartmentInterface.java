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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class DepartmentInterface implements IsSerializable, Comparable<DepartmentInterface>, GwtRpcResponse {
	private  Long iUniqueId = null;
	private  Long iSessionId = null;
	private  String iAcademicSessionName = null;
	private  String iName = null;
	private  String iDeptCode = null;
	private  String iStatusType= null;
	private  String iStatusTypeCode= null;
	private  String iAbbreviation = null;
	private  String iExternalId = null;
	private  Integer iDistributionPrefPriority =0;	
	private  String iExternalMgrLabel;
	private  String iExternalMgrAbbv;
	private  Integer iTimetableManagerCount;
	private  String iLastChangeStr;
	private  Boolean iExternalManager= false;
	private  Boolean iAllowEvents= false;
	private  Boolean iAllowStudentScheduling  = false;
	private  Boolean iInheritInstructorPreferences = false;;
	private  Boolean iAllowReqTime = false;
	private  Boolean iAllowReqRoom = false;
	private  Boolean iAllowReqDistribution = false;
	private  Boolean iExternalFundingDept= false;
	private  Boolean iCoursesFundingDepartmentsEnabled = false;
	private  String iExternalStatusTypesStr;
	private  List<String> iDependentStatusesStr;

	private  Integer iSubjectAreaCount;
	private  Integer iRoomDeptsCount;
	private Boolean iCanEdit = false;
	private Boolean iCanDelete = false;
	private Boolean iCanChangeExtManager = true;
	
	private  HashMap<String, String> iStatuses;	
	private  HashMap<Long, String>  iExtDepartments;

	//used by UpdateDepartmentBackend
	public  List<String> iDependentStatuses;
	public  List<String> iDependentDepartments;	
	
	public DepartmentInterface() {}
	
	public String allowReq(){
		 String allowReq = "";
        int allowReqOrd = 0;
       if (isAllowReqRoom() != null && isAllowReqRoom().booleanValue()) {
       	if (!allowReq.isEmpty()) allowReq += ", ";
       	allowReq += "room";
       	allowReqOrd += 1;
       }
       if (isAllowReqTime() != null && isAllowReqTime().booleanValue()) {
       	if (!allowReq.isEmpty()) allowReq += ", ";
       	allowReq += "time";
       	allowReqOrd += 2;
       }
       if (isAllowReqDistribution() != null && isAllowReqDistribution().booleanValue()) {
       	if (!allowReq.isEmpty()) allowReq += ", ";
       	allowReq += "distribution";
       	allowReqOrd += 4;
       }
       if (allowReqOrd == 7) allowReq = "all";
       if (allowReqOrd == 0) allowReq = "";
       return allowReq;
	}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
	
	public Long getId() { return iUniqueId; }
	public void setId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }

	public String getAcademicSessionName() { return iAcademicSessionName; }
	public void setAcademicSessionName(String academicSessionName) { iAcademicSessionName = academicSessionName; }
	
	public boolean isCoursesFundingDepartmentsEnabled(){ return iCoursesFundingDepartmentsEnabled; }
	public void setCoursesFundingDepartmentsEnabled(boolean coursesFundingDepartmentsEnabled) { iCoursesFundingDepartmentsEnabled = coursesFundingDepartmentsEnabled; }

	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getDeptCode() { return iDeptCode; }
	public void setDeptCode(String deptCode) { iDeptCode = deptCode; }	

	public String getStatusTypeCode() { return iStatusTypeCode; }
	public void setStatusTypeCode (String statusType) { iStatusTypeCode = statusType; }
	
	public String getStatusTypeStr() { return iStatusType; }
	public void setStatusTypeStr(String statusType) { iStatusType = statusType; }
	
	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }
	
	public String getExternalId() {return iExternalId;}
	public void setExternalId(String externalId) {iExternalId = externalId;}
	
	public Integer getDistributionPrefPriority() { return iDistributionPrefPriority; }
	public void setDistributionPrefPriority(Integer distributionPrefPriority) { iDistributionPrefPriority = distributionPrefPriority; }
	
	public boolean getCanEdit() { return iCanEdit; }
	public void setCanEdit (boolean canEdit) { iCanEdit = canEdit; }

	public boolean getCanDelete() { return iCanDelete; }
	public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }

	public boolean isCanChangeExtManager() { return iCanChangeExtManager; }
	public void setCanChangeExtManager(boolean canChangeExtManager) { iCanChangeExtManager = canChangeExtManager; }	
	
	public String effectiveStatusType() {
		String t = getStatusTypeStr();
		if (t!=null) 
			return t;
		return t;
	}
	
	public String getExternalMgrLabel() { return iExternalMgrLabel; }
	public void setExternalMgrLabel(String externalMgrLabel) { iExternalMgrLabel = externalMgrLabel; }
	public String getExternalMgrAbbv() { return iExternalMgrAbbv; }
	public void setExternalMgrAbbv(String externalMgrAbbv) { iExternalMgrAbbv = externalMgrAbbv; }

	public Integer getTimetableManagersCount() { return iTimetableManagerCount; }
	public void setTimetableManagersCount(Integer timetableManagerCount) { iTimetableManagerCount = timetableManagerCount; }
	
	public String getLastChangeStr(){ return iLastChangeStr; }
	public void setLastChangeStr(String lastChangeStr){ iLastChangeStr = lastChangeStr; }


	public Boolean isExternalManager() { return iExternalManager; }
	public Boolean getExternalManager() { return iExternalManager; }
	public void setExternalManager(Boolean externalManager) { iExternalManager = externalManager; }
	
	public String getExternalStatusTypes() { return iExternalStatusTypesStr; }
	public void setExternalStatusTypes(String externalStatusTypes) { iExternalStatusTypesStr = externalStatusTypes; }
	
	public List <String>  getDependentStatuses() { return iDependentStatuses; }
	public void setDependentStatuses(List dependentStatuses) { iDependentStatuses = dependentStatuses; }
	public List <String>  getDependentDepartments() { return iDependentDepartments; }
	public void setDependentDepartments(List   dependentDepartmentIds) { iDependentDepartments = dependentDepartmentIds; }

	public Boolean isAllowEvents() { return iAllowEvents; }
	public Boolean getAllowEvents() { return iAllowEvents; }
	public void setAllowEvents(Boolean allowEvents) { iAllowEvents = allowEvents; }

	public Boolean isAllowStudentScheduling() { return iAllowStudentScheduling; }
	public Boolean getAllowStudentScheduling() { return iAllowStudentScheduling; }
	public void setAllowStudentScheduling(Boolean allowStudentScheduling) { iAllowStudentScheduling = allowStudentScheduling; }

	public Boolean isInheritInstructorPreferences() { return iInheritInstructorPreferences; }
	public Boolean getInheritInstructorPreferences() { return iInheritInstructorPreferences; }
	public void setInheritInstructorPreferences(Boolean inheritInstructorPreferences) { iInheritInstructorPreferences = inheritInstructorPreferences; }
	
	public Boolean isAllowReqTime() { return iAllowReqTime; }
	public Boolean getAllowReqTime() { return iAllowReqTime; }
	public void setAllowReqTime(Boolean allowReqTime) { iAllowReqTime = allowReqTime; }

	public Boolean isAllowReqRoom() { return iAllowReqRoom; }
	public Boolean getAllowReqRoom() { return iAllowReqRoom; }
	public void setAllowReqRoom(Boolean allowReqRoom) { iAllowReqRoom = allowReqRoom; }

	public Boolean isAllowReqDistribution() { return iAllowReqDistribution; }
	public Boolean getAllowReqDistribution() { return iAllowReqDistribution; }
	public void setAllowReqDistribution(Boolean allowReqDistribution) { iAllowReqDistribution = allowReqDistribution; }

	public Boolean isExternalFundingDept() { return iExternalFundingDept; }
	public Boolean getExternalFundingDept() { return iExternalFundingDept; }
	public void setExternalFundingDept(Boolean externalFundingDept) { iExternalFundingDept = externalFundingDept; }
	
	public List<String> getDependentStatusesStr() { return iDependentStatusesStr; }
	public void setDependentStatusesStr(List<String> dependentStatuses) { iDependentStatusesStr = dependentStatuses; }


	public Integer getSubjectAreaCount() { return iSubjectAreaCount; }
	public void setSubjectAreaCount(Integer subjectAreas) { iSubjectAreaCount = subjectAreas; }

	public Integer getRoomDeptCount() { return iRoomDeptsCount; }
	public void setRoomDeptCount(Integer roomDepts) { iRoomDeptsCount = roomDepts; }
	
	public void setSatusOptions( HashMap<String, String>  hashMap){  iStatuses = hashMap; }
	public HashMap<String,String>  getSatusOptions(){ return iStatuses; }	

	public HashMap<Long, String> getExtDepartmentOptions() { return iExtDepartments; }
	public void setExtDepartmentOptions(HashMap<Long, String> hashMap) { iExtDepartments = hashMap; }
	
	
	@Override
	public int hashCode() { return getId().hashCode(); }

	@Override
	public int compareTo(DepartmentInterface d) {
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(d.getUniqueId() == null ? -1 : d.getUniqueId()); 
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof DepartmentInterface)) return false;
		return getId().equals(((DepartmentInterface)object).getId());
	}
	
	public static enum DepartmentsColumn {
		CODE, ABBV, NAME, EXTERNAL_MANAGER, SUBJECTS, ROOMS, STATUS, DIST_PREF_PRIORITY, ALLOW_REQUIRED, INSTRUCTOR_PREF, EVENTS, STUDENT_SCHEDULING, EXT_FUNDING_DEPT, LAST_CHANGE,

	}	
	public static enum UpdateDepartmentAction {
		CREATE, UPDATE, DELETE, UPDATE_DATA,
	}
	
	/*
	 * Look Up properties for Department
	 */
	public static class DepartmentPropertiesRequest implements GwtRpcRequest<DepartmentPropertiesInterface> {
	}//end DepartmentPropertiesRequest
	
	public static class DepartmentPropertiesInterface  implements GwtRpcResponse {
		
		private boolean iCanAdd , iCanUpdateData;
		private boolean iCanExportPdf = true;
		private boolean iCoursesFundingDepartmentsEnabled = false;
		private String iAcademicSessionName = null;
		private HashMap<String, String> iStatuses;
		private HashMap<Long, String> iExtDepartments;
			
		public DepartmentPropertiesInterface() {}

		public boolean isCanExportPdf() { return iCanExportPdf; }
		public void setCanExportPdf(boolean canExportPdf) { iCanExportPdf = canExportPdf; }
		
		
		public boolean isCoursesFundingDepartmentsEnabled(){ return iCoursesFundingDepartmentsEnabled; }
		public void setCoursesFundingDepartmentsEnabled(boolean coursesFundingDepartmentsEnabled) { iCoursesFundingDepartmentsEnabled = coursesFundingDepartmentsEnabled; }
		
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }

		public boolean isCanUpdate() { return iCanUpdateData; }
		public void setCanUpdate(boolean canUpdate) { iCanUpdateData = canUpdate; }
		
		public String getAcademicSessionName() { return iAcademicSessionName; }
		public void setAcademicSessionName(String academicSessionName) { iAcademicSessionName = academicSessionName; }
		
		public HashMap<String,String>  getSatusOptions(){ return iStatuses; }
		public void setSatusOptions(HashMap<String,String> statuses){  iStatuses =statuses; }
		
		public HashMap<Long, String> getExtDepartmentOptions() { return iExtDepartments; }
		public void setExtDepartmentOptions(HashMap<Long, String> hashMap) { iExtDepartments = hashMap; }
		
	} //end DepartmentPropertiesInterface
	

	
	/*
	 * List all existing departments
	 */
	public static class GetDepartmentsRequest implements GwtRpcRequest<DepartmentsDataResponse> {
	}
	
	public static class DepartmentsDataResponse implements GwtRpcResponse {
		private List<DepartmentInterface> iDepartments;
		private boolean iCanAdd , iCanUpdateData, iDepartmentsEnabled;
		private boolean iCanExportPdf = true;	
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }

		public boolean isFundingDeptEnabled(){ return iDepartmentsEnabled; }
		public void setFundingDeptEnabled(boolean fundingDepartmentsEnabled) { iDepartmentsEnabled = fundingDepartmentsEnabled; }		
		public boolean isCanUpdate() { return iCanUpdateData; }
		public void setCanUpdate(boolean canUpdate) { iCanUpdateData = canUpdate; }
		
		public void addDepartment(DepartmentInterface department) {
			if (iDepartments == null) iDepartments = new ArrayList<DepartmentInterface>();
			iDepartments.add(department);
		}
	
		public DepartmentsDataResponse() {}				

		public boolean isCanExportPdf() { 				
		 return iCanExportPdf; 
		}
		
		public void setCanExportPdf(boolean canExportPdf) { 
			iCanExportPdf = canExportPdf; }
		
		public List<DepartmentInterface> getDepartments() { return iDepartments; }
		
		public boolean hasDepartments() { return iDepartments != null && !iDepartments.isEmpty(); }
		
	}
	

	/* 
	 * Can Edit, checked in DepartmentsPage
	 */
	public boolean isCanEdit() { 
		return iCanEdit != null && iCanEdit.booleanValue(); }
	

}
