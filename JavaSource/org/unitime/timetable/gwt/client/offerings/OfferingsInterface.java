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
package org.unitime.timetable.gwt.client.offerings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.shared.FilterInterface;

import com.google.gwt.user.client.rpc.IsSerializable;


public class OfferingsInterface {
	
	public static class OfferingsFilterRequest implements GwtRpcRequest<OfferingsFilterResponse> {}
	
	public static class OfferingsFilterResponse extends ClassesFilterResponse {
		private static final long serialVersionUID = 1L;
		private boolean iCanAdd = false;
		private boolean iCanWorksheet = false;
		
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }

		public boolean isCanWorksheet() { return iCanWorksheet; }
		public void setCanWorksheet(boolean canWorksheet) { iCanWorksheet = canWorksheet; }
	}
	
	public static class ClassesFilterRequest implements GwtRpcRequest<ClassesFilterResponse> {}
	
	public static class ClassesFilterResponse extends FilterInterface {
		private static final long serialVersionUID = 1L;
		private boolean iSticky = false;
		private boolean iCanExport = false;
		private Integer iMaxSubjectsToSearchAutomatically = null;
		private Long iSessionId = null;
		
		public boolean isSticky() { return iSticky; }
		public void setSticky(boolean sticky) { iSticky = sticky; }
		
		public boolean isCanExport() { return iCanExport; }
		public void setCanExport(boolean canExport) { iCanExport = canExport; }

		public Integer getMaxSubjectsToSearchAutomatically() { return iMaxSubjectsToSearchAutomatically; }
		public void setMaxSubjectsToSearchAutomatically(Integer max) { iMaxSubjectsToSearchAutomatically = max; }
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
	}
	
	public static class ClassAssignmentsFilterRequest implements GwtRpcRequest<ClassAssignmentsFilterResponse> {}
	
	public static class ClassAssignmentsFilterResponse extends ClassesFilterResponse {
		private static final long serialVersionUID = 1L;
		private boolean iCanExportPdf = false;

		public boolean isCanExportPdf() { return iCanExportPdf; }
		public void setCanExportPdf(boolean canExport) { iCanExportPdf = canExport; }
	}
	
	public static class OfferingsRequest implements GwtRpcRequest<GwtRpcResponseList<TableInterface>> {
		private FilterInterface iFilter;
		private String iBackId, iBackType;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
		
		public String getBackId() { return iBackId; }
		public void setBackId(String backId) { iBackId = backId; }
		public String getBackType() { return iBackType; }
		public void setBackType(String backType) { iBackType = backType; }
	}
	
	public static class ClassesRequest extends OfferingsRequest {}
	
	public static class ClassAssignmentsRequest extends OfferingsRequest {}
	
	public static class OfferingDetailRequest implements GwtRpcRequest<OfferingDetailResponse> {
		private Long iOfferingId;
		private String iBackId, iBackType, iExamId;
		private Action iAction;

		public static enum Action {
			Lock, Unlock, MakeOffered, MakeNotOffered, Delete,
		}
		
		public OfferingDetailRequest() {}
		
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }
		
		public String getBackId() { return iBackId; }
		public void setBackId(String backId) { iBackId = backId; }
		public String getBackType() { return iBackType; }
		public void setBackType(String backType) { iBackType = backType; }
		public String getExamId() { return iExamId; }
		public void setExamId(String examId) { iExamId = examId; }
		public Action getAction() { return iAction; }
		public void setAction(Action action) { iAction = action; }
	}
	
	public static class OfferingDetailResponse implements GwtRpcResponse {
		private Long iOfferingId, iPreviousId, iNextId;
		private Long iSubjectAreaId, iCourseId;
		private String iCourseNumber;
		private String iName;
		private TableInterface iCourses;
		private TableInterface iProperties;
		private List<OfferingConfigInterface> iConfigurations;
		private Set<String> iOperations;
		private TableInterface iExaminations;
		private TableInterface iDistributions;
		private TableInterface iLastChanges;
		private boolean iOffered;
		private boolean iConfirms;
		private String iUrl;
		private String iBackUrl, iBackTitle;
		
		public OfferingDetailResponse() {}

		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		public Long getPreviousId() { return iPreviousId; }
		public void setNextId(Long id) { iNextId = id; }
		public Long getNextId() { return iNextId; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public boolean isOffered() { return iOffered; }
		public void setOffered(boolean offered) { iOffered = offered; }
		public boolean isConfirms() { return iConfirms; }
		public void setConfirms(boolean confirms) { iConfirms = confirms; }
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }
		public Long getSubjectAreaId() { return iSubjectAreaId; }
		public void setSubjectAreaId(Long subjectAreaId) { iSubjectAreaId = subjectAreaId; }
		public String getCoruseNumber() { return iCourseNumber; }
		public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public boolean hasBackUrl() { return iBackUrl != null && !iBackUrl.isEmpty(); }
		public void setBackUrl(String backUrl) { iBackUrl = backUrl; }
		public String getBackUrl() { return iBackUrl; }
		public boolean hasBackTitle() { return iBackTitle != null && !iBackTitle.isEmpty(); }
		public void setBackTitle(String backTitle) { iBackTitle = backTitle; }
		public String getBackTitle() { return iBackTitle; }
		
		public boolean hasOperation(String operation) { return iOperations != null && iOperations.contains(operation); }
		public void addOperation(String operation) {
			if (iOperations == null) iOperations = new HashSet<String>();
			iOperations.add(operation);
		}
		
		public boolean hasProperties() { return iProperties != null && !iProperties.hasProperties(); }
		public void addProperty(PropertyInterface property) {
			if (iProperties == null) iProperties = new TableInterface();
			iProperties.addProperty(property);
		}
		public TableInterface getProperties() { return iProperties; }
		public CellInterface addProperty(String text) {
			PropertyInterface p = new PropertyInterface();
			p.setName(text);
			p.setCell(new CellInterface());
			addProperty(p);
			return p.getCell();
		}
		
		public boolean hasConfigs() { return iConfigurations != null && !iConfigurations.isEmpty(); }
		public List<OfferingConfigInterface> getConfigs() { return iConfigurations; }
		public void addConfig(OfferingConfigInterface configuration) {
			if (iConfigurations == null) iConfigurations = new ArrayList<OfferingConfigInterface>();
			iConfigurations.add(configuration);
		}
		
		public TableInterface getCourses() { return iCourses; }
		public void setCourses(TableInterface courses) { iCourses = courses; }

		public boolean hasExaminations() { return iExaminations != null; }
		public TableInterface getExaminations() { return iExaminations; }
		public void setExaminations(TableInterface examinations) { iExaminations = examinations; }

		public boolean hasDistributions() { return iDistributions != null; }
		public TableInterface getDistributions() { return iDistributions; }
		public void setDistributions(TableInterface distributions) { iDistributions = distributions; }

		public boolean hasLastChanges() { return iLastChanges != null; }
		public TableInterface getLastChanges() { return iLastChanges; }
		public void setLastChanges(TableInterface lastChanges) { iLastChanges = lastChanges; }
	}
	
	public static class OfferingConfigInterface extends TableInterface {
		private Set<String> iOperations;
		private Long iConfigId;
		
		public Long getConfigId() { return iConfigId; }
		public void setConfigId(Long configId) { iConfigId = configId; }
		
		public boolean hasOperation(String operation) { return iOperations != null && iOperations.contains(operation); }
		public void addOperation(String operation) {
			if (iOperations == null) iOperations = new HashSet<String>();
			iOperations.add(operation);
		}
	}
	
	public static class SubpartDetailRequest implements GwtRpcRequest<SubpartDetailReponse> {
		private Long iSubpartId;
		private Action iAction;

		public static enum Action {
			ClearPrefs,
		}
		
		public SubpartDetailRequest() {}
		
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }
		public Long getSubpartgId() { return iSubpartId; }
		public Action getAction() { return iAction; }
		public void setAction(Action action) { iAction = action; }
	}
	
	public static class SubpartDetailReponse implements GwtRpcResponse {
		private Long iOfferingId, iSubpartId, iPreviousId, iNextId;
		private String iCourseName, iSubpartName;
		private String iBackUrl, iBackTitle;
		private String iUrl;
		private boolean iConfirms;
		private Set<String> iOperations;
		
		private TableInterface iProperties;
		private TableInterface iPreferences;
		private TableInterface iClasses;
		private TableInterface iExaminations;
		private TableInterface iDistributions;
		
		public SubpartDetailReponse() {}
		
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }
		public Long getSubpartgId() { return iSubpartId; }
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		public Long getPreviousId() { return iPreviousId; }
		public void setNextId(Long id) { iNextId = id; }
		public Long getNextId() { return iNextId; }
		public String getCourseName() { return iCourseName; }
		public void setCourseName(String name) { iCourseName = name; }
		public String getSubparName() { return iSubpartName; }
		public void setSubparName(String name) { iSubpartName = name; }
		
		public boolean hasBackUrl() { return iBackUrl != null && !iBackUrl.isEmpty(); }
		public void setBackUrl(String backUrl) { iBackUrl = backUrl; }
		public String getBackUrl() { return iBackUrl; }
		public boolean hasBackTitle() { return iBackTitle != null && !iBackTitle.isEmpty(); }
		public void setBackTitle(String backTitle) { iBackTitle = backTitle; }
		public String getBackTitle() { return iBackTitle; }

		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }

		public boolean isConfirms() { return iConfirms; }
		public void setConfirms(boolean confirms) { iConfirms = confirms; }
		
		public boolean hasOperation(String operation) { return iOperations != null && iOperations.contains(operation); }
		public void addOperation(String operation) {
			if (iOperations == null) iOperations = new HashSet<String>();
			iOperations.add(operation);
		}
		
		public boolean hasProperties() { return iProperties != null && !iProperties.hasProperties(); }
		public void addProperty(PropertyInterface property) {
			if (iProperties == null) iProperties = new TableInterface();
			iProperties.addProperty(property);
		}
		public TableInterface getProperties() { return iProperties; }
		public CellInterface addProperty(String text) {
			PropertyInterface p = new PropertyInterface();
			p.setName(text);
			p.setCell(new CellInterface());
			addProperty(p);
			return p.getCell();
		}
		public void setProperties(TableInterface properties) { iProperties = properties; }
		
		public boolean hasPreferences() { return iPreferences != null; }
		public TableInterface getPreferences() { return iPreferences; }
		public void setPreferences(TableInterface preferences) { iPreferences = preferences; }

		public boolean hasClasses() { return iClasses != null; }
		public TableInterface getClasses() { return iClasses; }
		public void setClasses(TableInterface courses) { iClasses = courses; }

		public boolean hasExaminations() { return iExaminations != null; }
		public TableInterface getExaminations() { return iExaminations; }
		public void setExaminations(TableInterface examinations) { iExaminations = examinations; }

		public boolean hasDistributions() { return iDistributions != null; }
		public TableInterface getDistributions() { return iDistributions; }
		public void setDistributions(TableInterface distributions) { iDistributions = distributions; }
	}
	
	public static class ClassDetailRequest implements GwtRpcRequest<ClassDetailReponse> {
		private Long iClassId;
		private Action iAction;

		public static enum Action {
		}
		
		public ClassDetailRequest() {}
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		public Action getAction() { return iAction; }
		public void setAction(Action action) { iAction = action; }
	}
	
	public static class ClassDetailReponse implements GwtRpcResponse {
		private Long iOfferingId, iSubpartId, iClassId, iPreviousId, iNextId;
		private String iCourseName, iSubpartName, iClassName;
		private String iBackUrl, iBackTitle;
		private String iUrl;
		private boolean iConfirms;
		private Set<String> iOperations;
		
		private TableInterface iProperties;
		private TableInterface iTimetable;
		private TableInterface iConflicts;
		private TableInterface iEventConflicts;
		private TableInterface iPreferences;
		private TableInterface iExaminations;
		private TableInterface iDistributions;
		
		public ClassDetailReponse() {}
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }
		public Long getSubpartgId() { return iSubpartId; }
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		public Long getPreviousId() { return iPreviousId; }
		public void setNextId(Long id) { iNextId = id; }
		public Long getNextId() { return iNextId; }
		public String getCourseName() { return iCourseName; }
		public void setCourseName(String name) { iCourseName = name; }
		public String getSubparName() { return iSubpartName; }
		public void setSubparName(String name) { iSubpartName = name; }
		public String getClassName() { return iClassName; }
		public void setClassName(String name) { iClassName = name; }
		
		public boolean hasBackUrl() { return iBackUrl != null && !iBackUrl.isEmpty(); }
		public void setBackUrl(String backUrl) { iBackUrl = backUrl; }
		public String getBackUrl() { return iBackUrl; }
		public boolean hasBackTitle() { return iBackTitle != null && !iBackTitle.isEmpty(); }
		public void setBackTitle(String backTitle) { iBackTitle = backTitle; }
		public String getBackTitle() { return iBackTitle; }

		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }

		public boolean isConfirms() { return iConfirms; }
		public void setConfirms(boolean confirms) { iConfirms = confirms; }
		
		public boolean hasOperation(String operation) { return iOperations != null && iOperations.contains(operation); }
		public void addOperation(String operation) {
			if (iOperations == null) iOperations = new HashSet<String>();
			iOperations.add(operation);
		}
		
		public boolean hasProperties() { return iProperties != null && !iProperties.hasProperties(); }
		public void addProperty(PropertyInterface property) {
			if (iProperties == null) iProperties = new TableInterface();
			iProperties.addProperty(property);
		}
		public TableInterface getProperties() { return iProperties; }
		public CellInterface addProperty(String text) {
			PropertyInterface p = new PropertyInterface();
			p.setName(text);
			p.setCell(new CellInterface());
			addProperty(p);
			return p.getCell();
		}
		public void setProperties(TableInterface properties) { iProperties = properties; }
		
		public boolean hasTimetable() { return iTimetable != null; }
		public TableInterface getTimetable() { return iTimetable; }
		public void setTimetable(TableInterface timetable) { iTimetable = timetable; }

		public boolean hasConclicts() { return iConflicts != null; }
		public TableInterface getConflicts() { return iConflicts; }
		public void setConflicts(TableInterface conflicts) { iConflicts = conflicts; }

		public boolean hasEventConclicts() { return iEventConflicts != null; }
		public TableInterface getEventConflicts() { return iEventConflicts; }
		public void setEventConflicts(TableInterface conflicts) { iEventConflicts = conflicts; }

		public boolean hasPreferences() { return iPreferences != null; }
		public TableInterface getPreferences() { return iPreferences; }
		public void setPreferences(TableInterface preferences) { iPreferences = preferences; }

		public boolean hasExaminations() { return iExaminations != null; }
		public TableInterface getExaminations() { return iExaminations; }
		public void setExaminations(TableInterface examinations) { iExaminations = examinations; }

		public boolean hasDistributions() { return iDistributions != null; }
		public TableInterface getDistributions() { return iDistributions; }
		public void setDistributions(TableInterface distributions) { iDistributions = distributions; }
	}
	
	public static class CrossListGetRequest implements GwtRpcRequest<CrossListGetResponse>{
		private Long iOfferingId;
		
		public CrossListGetRequest() {}
		
		public Long getOfferingId() { return iOfferingId; }
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
	}
	
	public static class CrossListGetResponse implements GwtRpcResponse {
		private Long iOfferingId;
		private String iOfferingName;
		private TreeSet<CrossListedCourse> iCourses;
		private TreeSet<CrossListedCourse> iAvailableCourses;
		private Integer iLimit;
		private Boolean iUnlimited = false;
		private Boolean iSingleCourseLimit;
		
		public CrossListGetResponse() {}
		
		public Long getOfferingId() { return iOfferingId; }
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public String getOfferingName() { return iOfferingName; }
		public void setOfferingName(String name) { iOfferingName = name; }
		public boolean hasCourses() { return iCourses != null && !iCourses.isEmpty(); }
		public TreeSet<CrossListedCourse> getCourses() { return iCourses; }
		public void setCourses(TreeSet<CrossListedCourse> courses) { iCourses = courses; }
		public void addCourse(CrossListedCourse course) {
			if (iCourses == null) iCourses = new TreeSet<CrossListedCourse>();
			iCourses.add(course);
		}
		public CrossListedCourse getCourse(Long courseId) {
			if (iCourses == null) return null;
			for (CrossListedCourse course: iCourses)
				if (course.getCourseId().equals(courseId)) return course;
			return null;
		}
		
		public boolean hasAvailableCourses() { return iAvailableCourses != null && !iAvailableCourses.isEmpty(); }
		public TreeSet<CrossListedCourse> getAvailableCourses() { return iAvailableCourses; }
		public void addAvailableCourse(CrossListedCourse course) {
			if (iAvailableCourses == null) iAvailableCourses = new TreeSet<CrossListedCourse>();
			iAvailableCourses.add(course);
		}
		public CrossListedCourse getAvailableCourse(Long courseId) {
			if (iAvailableCourses == null) return null;
			for (CrossListedCourse course: iAvailableCourses)
				if (course.getCourseId().equals(courseId)) return course;
			return null;
		}
		
		public void setUnlimited(boolean unlimited) { iUnlimited = unlimited; }
		public boolean isUnlimited() { return iUnlimited == null || iUnlimited.booleanValue(); }
		public void setLimit(Integer limit) { iLimit = limit; }
		public boolean hasLimit() { return !isUnlimited() && iLimit != null; }
		public Integer getLimit() { return iLimit; }
		
		public void setSingleCourseLimitAllowed(boolean singleCourseLimit) { iSingleCourseLimit = singleCourseLimit; }
		public boolean isSingleCourseLimitAllowed() { return iSingleCourseLimit != null && iSingleCourseLimit.booleanValue(); }

	}
	
	public static class CrossListUpdateRequest implements GwtRpcRequest<GwtRpcResponseNull>{
		private Long iOfferingId;
		private List<CrossListedCourse> iCourses;
		
		public CrossListUpdateRequest() {}
		
		public Long getOfferingId() { return iOfferingId; }
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public boolean hasCourses() { return iCourses != null && !iCourses.isEmpty(); }
		public List<CrossListedCourse> getCourses() { return iCourses; }
		public void setCourses(List<CrossListedCourse> courses) { iCourses = courses; }
		public void addCourse(CrossListedCourse course) {
			if (iCourses == null) iCourses = new ArrayList<CrossListedCourse>();
			iCourses.add(course);
		}
		public CrossListedCourse getCourse(Long courseId) {
			if (iCourses == null) return null;
			for (CrossListedCourse course: iCourses)
				if (course.getCourseId().equals(courseId)) return course;
			return null;
		}
	}
	
	public static class CrossListedCourse implements IsSerializable, Comparable<CrossListedCourse> {
		private Long iCourseId;
		private String iCourseName;
		private Boolean iControl;
		private Integer iReserved, iProjected, iLastTerm;
		private Boolean iCanDelete, iCanEdit;
		
		public CrossListedCourse() {}
		
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public String getCourseName() { return iCourseName; }
		public void setCourseName(String courseName) { iCourseName = courseName; }
		public boolean isControl() { return iControl != null && iControl.booleanValue(); }
		public void setControl(boolean control) { iControl = control; }
		public boolean hasReserved() { return iReserved != null; }
		public Integer getReserved() { return iReserved; }
		public void setReserved(Integer reserved) { iReserved = reserved; }
		public boolean hasProjected() { return iProjected != null && iProjected > 0; }
		public Integer getProjected() { return iProjected; }
		public void setProjected(Integer projected) { iProjected = projected; }
		public boolean hasLastTerm() { return iLastTerm != null && iLastTerm > 0; }
		public Integer getLastTerm() { return iLastTerm; }
		public void setLastTerm(Integer lastTerm) { iLastTerm = lastTerm; }
		
		public boolean isCanDelete() { return iCanDelete == null || iCanDelete.booleanValue(); }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		public boolean isCanEdit() { return iCanEdit == null || iCanEdit.booleanValue(); }
		public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }

		@Override
		public int hashCode() { return getCourseId().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CrossListedCourse)) return false;
			return getCourseId().equals(((CrossListedCourse)o).getCourseId());
		}
		@Override
		public int compareTo(CrossListedCourse c) {
			if (isControl() != c.isControl()) return isControl() ? -1 : 1;
			return getCourseName().compareTo(c.getCourseName());
		}
	}
	
	public static class DistributionsFilterRequest implements GwtRpcRequest<DistributionsFilterResponse> {}
	
	public static class DistributionsFilterResponse extends FilterInterface {
		private static final long serialVersionUID = 1L;
		private boolean iSticky = false;
		private boolean iCanAdd = false;
		private Integer iMaxSubjectsToSearchAutomatically = null;
		private Long iSessionId = null;
		
		public boolean isSticky() { return iSticky; }
		public void setSticky(boolean sticky) { iSticky = sticky; }
		
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }

		public Integer getMaxSubjectsToSearchAutomatically() { return iMaxSubjectsToSearchAutomatically; }
		public void setMaxSubjectsToSearchAutomatically(Integer max) { iMaxSubjectsToSearchAutomatically = max; }
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
	}
	
	public static class DistributionsRequest implements GwtRpcRequest<DistributionsResponse> {
		private FilterInterface iFilter;
		private String iBackId, iBackType;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
		
		public String getBackId() { return iBackId; }
		public void setBackId(String backId) { iBackId = backId; }
		public String getBackType() { return iBackType; }
		public void setBackType(String backType) { iBackType = backType; }
	}
	
	public static class DistributionsResponse implements GwtRpcResponse {
		private List<TableInterface> iTables;
		private Set<String> iOperations;
		
		public boolean hasTables() { return iTables != null && !iTables.isEmpty(); }
		public void addTable(TableInterface table) {
			if (iTables == null) iTables = new ArrayList<TableInterface>();
			iTables.add(table);
		}
		public List<TableInterface> getTables() {
			return iTables;
		}
		
		public boolean hasOperation(String operation) { return iOperations != null && iOperations.contains(operation); }
		public void addOperation(String operation) {
			if (iOperations == null) iOperations = new HashSet<String>();
			iOperations.add(operation);
		}
	}
}
