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
package org.unitime.timetable.gwt.client.exams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsFilterResponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingsRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ExamsInterface {
	public static class ExamsFilterRequest implements GwtRpcRequest<ExamsFilterResponse> {}
	
	public static class ExamsFilterResponse extends ClassesFilterResponse {
		private static final long serialVersionUID = 1L;
		private boolean iCanAdd = false;
		
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
	}

	public static class ExamsRequest extends OfferingsRequest {}
	
	public static class ExamDetailRequest implements GwtRpcRequest<ExamDetailReponse> {
		private Long iExamId;
		private Action iAction;

		public static enum Action {
			DELETE,
		}
		
		public void setExamId(Long examId) { iExamId = examId; }
		public Long getExamId() { return iExamId; }
		public Action getAction() { return iAction; }
		public void setAction(Action action) { iAction = action; }
	}
	
	public static class ExamDetailReponse implements GwtRpcResponse {
		private Long iExamId, iPreviousId, iNextId;
		private String iExamName;
		private String iBackUrl, iBackTitle;
		private String iUrl;
		private boolean iConfirms;
		private Set<String> iOperations;
		
		private TableInterface iProperties;
		private TableInterface iOwners;
		private TableInterface iAssignment;
		private TableInterface iPreferences;
		private TableInterface iDistributions;
		
		public void setExamId(Long examId) { iExamId = examId; }
		public Long getExamId() { return iExamId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		public Long getPreviousId() { return iPreviousId; }
		public void setNextId(Long id) { iNextId = id; }
		public Long getNextId() { return iNextId; }
		public String getExamName() { return iExamName; }
		public void setExamName(String name) { iExamName = name; }
		
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
		
		public boolean hasOwners() { return iOwners != null; }
		public TableInterface getOwners() { return iOwners; }
		public void setOwners(TableInterface conflicts) { iOwners = conflicts; }

		public boolean hasAssignment() { return iAssignment != null; }
		public TableInterface getAssignment() { return iAssignment; }
		public void setAssignment(TableInterface conflicts) { iAssignment = conflicts; }

		public boolean hasPreferences() { return iPreferences != null; }
		public TableInterface getPreferences() { return iPreferences; }
		public void setPreferences(TableInterface preferences) { iPreferences = preferences; }

		public boolean hasDistributions() { return iDistributions != null; }
		public TableInterface getDistributions() { return iDistributions; }
		public void setDistributions(TableInterface distributions) { iDistributions = distributions; }
	}
	
	public static class ExamDistributionsFilterRequest implements GwtRpcRequest<DistributionsFilterResponse> {}
	
	public static class ExamDistributionsRequest extends DistributionsRequest {}
	
	public static class ExamDistributionEditRequest implements GwtRpcRequest<ExamDistributionEditResponse> {
		private Long iPreferenceId, iExamId, iTypeId;
		private Operation iOperation;
		private ExamDistributionEditResponse iData;
		
		public static enum Operation {
			GET, SAVE, DELETE,
		}
		
		public ExamDistributionEditRequest() {}

		public Long getPreferenceId() { return iPreferenceId; }
		public void setPreferenceId(Long preferenceId) { iPreferenceId = preferenceId; }
		public Long getExamId() { return iExamId; }
		public void setExamId(Long examId) { iExamId = examId; }
		public Long getTypeId() { return iTypeId; }
		public void setTypeId(Long typeId) { iTypeId = typeId; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		public ExamDistributionEditResponse getData() { return iData; }
		public void setData(ExamDistributionEditResponse data) { iData = data; }		
	}
	
	public static class ExamDistributionEditResponse implements GwtRpcResponse {
		private Long iPreferenceId;
		private Long iPrefLevelId, iDistTypeId, iExamTypeId;
		private List<IdLabel> iExamTypes, iDistTypes, iPrefLevels, iSubjects;
		private List<ExamDistributionObjectInterface> iDistributionObjects;
		private boolean iCanDelete;
		private String iBackUrl, iBackTitle;
		private boolean iConfirms;
		
		public Long getPreferenceId() { return iPreferenceId; }
		public void setPreferenceId(Long preferenceId) { iPreferenceId = preferenceId; }

		public Long getPrefLevelId() { return iPrefLevelId; }
		public void setPrefLevelId(Long id) { iPrefLevelId = id; }
		public void addPrefLevel(Long id, String label, char pref) {
			if (iPrefLevels == null) iPrefLevels = new ArrayList<IdLabel>();
			iPrefLevels.add(new IdLabel(id, label, ""+pref));
		}
		public List<IdLabel> getPrefLevels() { return iPrefLevels; }
		public boolean hasPrefLevels() { return iPrefLevels != null && !iPrefLevels.isEmpty(); }
		public IdLabel getPrefLevel(Long id) {
			if (iPrefLevels == null) return null;
			for (IdLabel item: iPrefLevels)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public Long getExamTypeId() { return iExamTypeId; }
		public void setExamTypeId(Long id) { iExamTypeId = id; }
		public void addExamType(Long id, String label, String description) {
			if (iExamTypes == null) iExamTypes = new ArrayList<IdLabel>();
			IdLabel dt = new IdLabel(id, label, description);
			iExamTypes.add(dt);
		}
		public List<IdLabel> getExamTypes() { return iExamTypes; }
		public boolean hasExamTypes() { return iExamTypes != null && !iExamTypes.isEmpty(); }
		public IdLabel getExamType(Long id) {
			if (iExamTypes == null) return null;
			for (IdLabel item: iExamTypes)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public Long getDistTypeId() { return iDistTypeId; }
		public void setDistTypeId(Long id) { iDistTypeId = id; }
		public void addDistType(Long id, String label, String description, String allowedPrefs) {
			if (iDistTypes == null) iDistTypes = new ArrayList<IdLabel>();
			IdLabel dt = new IdLabel(id, label, description);
			dt.setAllowedPrefs(allowedPrefs);
			iDistTypes.add(dt);
		}
		public List<IdLabel> getDistTypes() { return iDistTypes; }
		public boolean hasDistTypes() { return iDistTypes != null && !iDistTypes.isEmpty(); }
		public IdLabel getDistType(Long id) {
			if (iDistTypes == null) return null;
			for (IdLabel item: iDistTypes)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public void addSubject(Long id, String label, String description) {
			if (iSubjects == null) iSubjects = new ArrayList<IdLabel>();
			iSubjects.add(new IdLabel(id, label, description));
		}
		public List<IdLabel> getSubjects() { return iSubjects; }
		public boolean hasSubjects() { return iSubjects != null && !iSubjects.isEmpty(); }
		public IdLabel getSubject(Long id) {
			if (iSubjects == null) return null;
			for (IdLabel item: iSubjects)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public void addDistributionObject(ExamDistributionObjectInterface dist) {
			if (iDistributionObjects == null) iDistributionObjects = new ArrayList<ExamDistributionObjectInterface>();
			iDistributionObjects.add(dist);
		}
		public List<ExamDistributionObjectInterface> getDistributionObjects() { return iDistributionObjects; }
		public boolean hasDistributionObjects() { return iDistributionObjects != null && !iDistributionObjects.isEmpty(); }
		public void setDistributionObjects(List<ExamDistributionObjectInterface> objects) {
			iDistributionObjects = objects;
		}
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		
		public boolean hasBackUrl() { return iBackUrl != null && !iBackUrl.isEmpty(); }
		public void setBackUrl(String backUrl) { iBackUrl = backUrl; }
		public String getBackUrl() { return iBackUrl; }
		public boolean hasBackTitle() { return iBackTitle != null && !iBackTitle.isEmpty(); }
		public void setBackTitle(String backTitle) { iBackTitle = backTitle; }
		public String getBackTitle() { return iBackTitle; }
		public boolean isConfirms() { return iConfirms; }
		public void setConfirms(boolean confirms) { iConfirms = confirms; }
	}
	
	public static class ExamDistributionObjectInterface implements IsSerializable {
		private Long iSubjectId, iCourseId, iExamId;
		private String iSubject, iCourse, iExam;
		
		public Long getSubjectId() { return iSubjectId; }
		public void setSubjectId(Long subjectId) { iSubjectId = subjectId; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public Long getExamId() { return iExamId; }
		public void setExamId(Long examId) { iExamId = examId; }
		
		public String getSubject() { return iSubject ; }
		public void setSubject(String subject) { iSubject = subject; }
		public String getCourse() { return iCourse; }
		public void setCourse(String course) { iCourse = course; }
		public String getExam() { return iExam; }
		public void setExam(String exam) { iExam = exam; }
	}

	public static class ExamDistributionsLookupCourses implements GwtRpcRequest<GwtRpcResponseList<IdLabel>> {
		private Long iSubjectId;
		public Long getSubjectId() { return iSubjectId; }
		public void setSubjectId(Long subjectId) { iSubjectId = subjectId; }
	}
	
	public static class ExamDistributionsLookupExams implements GwtRpcRequest<GwtRpcResponseList<IdLabel>> {
		private Long iCourseId;
		private Long iExamTypeId;
		public Long getExamTypeId() { return iExamTypeId; }
		public void setExamTypeId(Long typeId) { iExamTypeId = typeId; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
	}

}
