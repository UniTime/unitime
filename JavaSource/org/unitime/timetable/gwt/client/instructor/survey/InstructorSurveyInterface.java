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
package org.unitime.timetable.gwt.client.instructor.survey;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityModel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class InstructorSurveyInterface implements IsSerializable {

	public static class InstructorSurveyData implements GwtRpcResponse {
		private Long iId;
		private Long iSessionId;
		private String iExternalId;
		private String iFormattedName;
		private String iEmail;
		private String iNote;
		private String iChangedBy, iAppliedDept;
		private Date iSubmitted, iApplied, iChanged;
		private List<InstructorDepartment> iDepartments;
		private InstructorTimePreferencesModel iTimePrefs;
		private List<Preferences> iRoomPrefs;
		private Preferences iDistPrefs;
		private List<PrefLevel> iPrefLevels;
		private List<Course> iCourses;
		private List<CustomField> iCustomFields;
		private boolean iEditable = true;
		private boolean iCanApply = true;
		private boolean iAdmin = true;
		private List<AcademicSessionInfo> iSessions = null;
		
		public InstructorSurveyData() {}
		public InstructorSurveyData(InstructorSurveyData data) {
			iId = data.iId;
			iSessionId = data.iSessionId;
			iExternalId = data.iExternalId;
			iFormattedName = data.iFormattedName;
			iEmail = data.iEmail;
			iNote = data.iNote;
			iChangedBy = data.iChangedBy; iAppliedDept = data.iAppliedDept;
			iSubmitted = data.iSubmitted; iApplied = data.iApplied; iChanged = data.iChanged;
			if (data.iDepartments != null)
				iDepartments = new ArrayList<InstructorDepartment>(data.iDepartments);
			if (data.iTimePrefs != null)
				iTimePrefs = new InstructorTimePreferencesModel(data.iTimePrefs);
			if (data.iRoomPrefs != null)
				for (Preferences p: data.iRoomPrefs)
					addRoomPreference(new Preferences(p));
			if (data.iDistPrefs != null)
				iDistPrefs = new Preferences(data.iDistPrefs);
			if (data.iPrefLevels != null)
				iPrefLevels = new ArrayList<InstructorSurveyInterface.PrefLevel>(data.iPrefLevels);
			if (data.iCourses != null)
				for (Course course: data.iCourses)
					addCourse(new Course(course));
			if (data.iCustomFields != null)
				iCustomFields = new ArrayList<CustomField>(data.iCustomFields);
			iEditable = data.iEditable;
			iCanApply = data.iCanApply;
			iAdmin = data.iAdmin;
			if (data.iSessions != null)
				iSessions = new ArrayList<AcademicSessionInfo>(data.iSessions);
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }

		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		public String getFormattedName() { return iFormattedName; }
		public void setFormattedName(String name) { iFormattedName = name; }
		
		public boolean isEditable() { return iEditable; }
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isCanApply() { return iCanApply; }
		public void setCanApply(boolean canApply) { iCanApply = canApply; }
		public boolean isAdmin() { return iAdmin; }
		public void setAdmin(boolean admin) { iAdmin = admin; }
		
		public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		
		public Date getSubmitted() { return iSubmitted; }
		public void setSubmitted(Date submitted) { iSubmitted = submitted; }
		public String getAppliedDeptCode() { return iAppliedDept; }
		public void setAppliedDeptCode(String deptCode) { iAppliedDept = deptCode; }
		public Date getApplied() { return iApplied; }
		public void setApplied(Date applied) { iApplied = applied; }
		public String getChangedBy() { return iChangedBy; }
		public void setChangedBy(String changedBy) { iChangedBy = changedBy; }
		public Date getChanged() { return iChanged; }
		public void setChanged(Date changed) { iChanged = changed; }
		
		public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		
		public boolean hasDepartments() { return iDepartments != null && !iDepartments.isEmpty(); }
		public boolean hasDepartment(Long id) {
			if (iDepartments == null || id == null) return false;
			for (InstructorDepartment d: iDepartments)
				if (d.getId().equals(id)) return true;
			return false;
		}
		public List<InstructorDepartment> getDepartments() { return iDepartments; }
		public void addDepartment(InstructorDepartment dept) {
			if (iDepartments == null) iDepartments = new ArrayList<InstructorDepartment>();
			iDepartments.add(dept);
		}
		
		public InstructorTimePreferencesModel getTimePrefs() { return iTimePrefs; }
		public void setTimePrefs(InstructorTimePreferencesModel timePrefs) { iTimePrefs = timePrefs; }
		
		public boolean hasRoomPreferences() { return iRoomPrefs != null && !iRoomPrefs.isEmpty(); }
		public List<Preferences> getRoomPreferences() { return iRoomPrefs; }
		public void addRoomPreference(Preferences pref) {
			if (iRoomPrefs == null) iRoomPrefs = new ArrayList<Preferences>();
			iRoomPrefs.add(pref);
		}
		public Preferences getRoomPreference(Long id) {
			if (iRoomPrefs == null) return null;
			for (Preferences p: iRoomPrefs)
				if (id.equals(p.getId())) return p;
			return null;
		}
		
		public boolean hasDistributionPreferences() { return iDistPrefs != null && iDistPrefs.hasItems(); }
		public Preferences getDistributionPreferences() { return iDistPrefs; }
		public void setDistributionPreferences(Preferences distPrefs) { iDistPrefs = distPrefs; }
		
		public void addPrefLevel(PrefLevel prefLevel) {
			if (iPrefLevels == null) iPrefLevels = new ArrayList<PrefLevel>();
			iPrefLevels.add(prefLevel);
		}
		public List<PrefLevel> getPrefLevels() { return iPrefLevels; }
		
		public boolean hasCourses() { return iCourses != null && !iCourses.isEmpty(); }
		public List<Course> getCourses() { return iCourses; }
		public void addCourse(Course course) {
			if (iCourses == null) iCourses = new ArrayList<Course>();
			iCourses.add(course);
		}
		public void setCourses(List<Course> courses) {
			iCourses = courses;
		}
		public void clearCourses() {
			if (iCourses != null) iCourses.clear();
		}
		
		public List<CustomField> getCustomFields() { return iCustomFields; }
		public boolean hasCustomFields() { return iCustomFields != null && !iCustomFields.isEmpty(); }
		public void addCustomField(CustomField f) {
			if (iCustomFields == null) iCustomFields = new ArrayList<CustomField>();
			iCustomFields.add(f);
		}
		
		public boolean hasSessions() { return iSessions != null && !iSessions.isEmpty(); }
		public List<AcademicSessionInfo> getSessions() { return iSessions; }
		public void addSession(AcademicSessionInfo session) {
			if (iSessions == null) iSessions = new ArrayList<AcademicSessionInfo>();
			iSessions.add(session);
		}
		
		public boolean isChanged(InstructorSurveyData data) {
			// check basic properties
			if (!InstructorSurveyInterface.equalsString(iExternalId, data.iExternalId)) return true;
			if (!InstructorSurveyInterface.equals(iSessionId, data.iSessionId)) return true;
			if (!InstructorSurveyInterface.equalsString(iEmail, data.iEmail)) return true;
			if (!InstructorSurveyInterface.equalsString(iNote, data.iNote)) return true;
			// check preferences
			if (!InstructorSurveyInterface.equals(iTimePrefs, data.iTimePrefs)) return true;
			if (!InstructorSurveyInterface.equals(iDistPrefs, data.iDistPrefs)) return true;
			if (iRoomPrefs != null) {
				for (Preferences p: iRoomPrefs) {
					if (!InstructorSurveyInterface.equals(p, data.getRoomPreference(p.getId()))) return true;		
				}
			}
			int courses = 0;
			if (iCourses != null) {
				course: for (Course course: iCourses) {
					if (!course.hasCustomFields()) continue;
					courses ++;
					if (data.iCourses != null)
						for (Course o: data.iCourses)
							if (course.equals(o)) continue course;
					return true;
				}
			}
			int other = 0;
			if (data.iCourses != null) {
				for (Course course: data.iCourses) {
					if (course.hasCustomFields()) other++;
				}
			}
			if (courses != other) return true;
			return false;
		}
		
		public String checkChanges(InstructorSurveyData data) {
			// check basic properties
			if (!InstructorSurveyInterface.equalsString(iExternalId, data.iExternalId)) return "external id " + iExternalId + "\n vs " + data.iExternalId;
			if (!InstructorSurveyInterface.equals(iSessionId, data.iSessionId)) return "session id " + iSessionId + "\n vs " + data.iSessionId;
			if (!InstructorSurveyInterface.equalsString(iEmail, data.iEmail)) return "email " + iEmail + "\n vs " + data.iEmail;
			if (!InstructorSurveyInterface.equalsString(iNote, data.iNote)) return "note " + iNote + "\n vs " + data.iNote;
			// check preferences
			if (!InstructorSurveyInterface.equals(iTimePrefs, data.iTimePrefs)) return "time " + iTimePrefs + "\n vs " + data.iTimePrefs;
			if (!InstructorSurveyInterface.equals(iDistPrefs, data.iDistPrefs)) return (iDistPrefs != null ? iDistPrefs.getType() : "dist " + iDistPrefs + "\n vs " + data.iDistPrefs);
			if (iRoomPrefs != null) {
				for (Preferences p: iRoomPrefs) {
					if (!InstructorSurveyInterface.equals(p, data.getRoomPreference(p.getId()))) return p.getType() + " " + p + "\n vs " + data.getRoomPreference(p.getId());		
				}
			}
			int courses = 0;
			if (iCourses != null) {
				course: for (Course course: iCourses) {
					if (!course.hasCustomFields()) continue;
					courses ++;
					if (data.iCourses != null)
						for (Course o: data.iCourses)
							if (course.equals(o)) continue course;
					return "course " + course;
				}
			}
			int other = 0;
			if (data.iCourses != null) {
				for (Course course: data.iCourses) {
					if (course.hasCustomFields()) other++;
				}
			}
			if (courses != other) return "@#courses " + courses + "/" + iCourses + "\n vs " +  other + "/" + data.iCourses;
			return "NO CHANGE";
		}
		
		@Override
		public String toString() {
			String ret = 
					"{ id : '" + iExternalId + "',\n  session : " + iSessionId + ",\n  email : " + (iEmail == null ? "null" : "'" + iEmail + "'") +
					",\n  note : " + (iNote == null ? "null" : "'" + iNote + "'") +
					",\n  time : " + iTimePrefs +
					",\n  dist : " + iDistPrefs;
			if (iRoomPrefs == null)
				ret += ",\n  room : null";
			else {
				ret += ",\n  room : [";
				for (Preferences p: iRoomPrefs) {
					ret += ",\n    " + p;
				}
				ret += "\n  ]";
			}
			if (iCourses == null)
				ret += ",\n  courses : null";
			else {
				ret += ",\n  courses : [";
				for (Course course: iCourses) {
					if (!course.hasCustomFields()) continue;
					ret += ",\n    " + course;
				}
				ret += "\n  ]";
			}
			return ret + "\n}";
		}
	}
	
	public static class Selection implements IsSerializable {
		private Long iItem;
		private Long iLevel;
		private Long iInstructorLevel;
		private String iNote;
		private Problem iProblem = Problem.NOT_APPLIED;
		
		public Selection() {}
		public Selection(Long item, Long level, String note) {
			iItem = item; iLevel = level; iNote = note;
		}
		
		public Long getItem() { return iItem; }
		public void setItem(Long item) { iItem = item; }
		public Long getLevel() { return iLevel; }
		public void setLevel(Long level) { iLevel = level; }
		public Long getInstructorLevel() { return iInstructorLevel; }
		public void setInstructorLevel(Long level) { iInstructorLevel = level; }
		public String getNote() { return iNote; }
		public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
		public void setNote(String note) { iNote = note; }
		
		public Problem getProblem() { return iProblem; }
		public void setProblem(Problem problem) { iProblem = problem; }
		public Selection withProblem(Problem problem) { iProblem = problem; return this; }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Selection)) return false;
			Selection s = (Selection)o;
			return
					InstructorSurveyInterface.equals(iItem, s.iItem) &&
					InstructorSurveyInterface.equals(iLevel, s.iLevel) &&
					InstructorSurveyInterface.equalsString(iNote, s.iNote);
		}
		
		@Override
		public String toString() {
			return "{ item : " + iItem + ", level : " + iLevel + ", note : "+ (iNote == null ? "null" : "'" + iNote + "'") + "}";
		}
	}
	
	public static class Preferences implements IsSerializable, Comparable<Preferences> {
		private Long iId;
		private String iType;
		private TreeSet<IdLabel> iItems;
		private List<Selection> iSelections;
		
		public Preferences() {}
		public Preferences(Long id, String type) {
			iId = id; iType = type;
		}
		public Preferences(Preferences p) {
			iId = p.iId;
			iType = p.iType;
			if (p.iItems != null)
				iItems = new TreeSet<IdLabel>(p.iItems);
			if (p.iSelections != null)
				iSelections = new ArrayList<Selection>(p.iSelections);
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public boolean hasItems() { return iItems != null && !iItems.isEmpty(); }
		public Set<IdLabel> getItems() { return iItems; }
		public IdLabel getItem(Long id) {
			if (iItems == null) return null;
			for (IdLabel item: iItems)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addItem(Long id, String label, String description) {
			if (iItems == null) iItems = new TreeSet<IdLabel>();
			IdLabel item = new IdLabel(id, label, description);
			if (!iItems.contains(item)) {
				iItems.add(item);
				return item;
			}
			return null;
		}
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		@Override
		public int compareTo(Preferences other) {
			return getType().compareTo(other.getType());
		}
		
		public boolean hasSelections() { return iSelections != null && !iSelections.isEmpty(); }
		public void clearSelections() {
			if (iSelections != null) iSelections.clear();
		}
		public List<Selection> getSelections() { return iSelections; }
		public void addSelection(Selection selection) {
			if (iSelections == null) iSelections = new ArrayList<Selection>();
			if (getItem(selection.getItem()) != null)
				iSelections.add(selection);
		}
		public void addInstructorSelection(Selection selection) {
			Selection original = getSelection(selection.getItem());
			if (original == null) {
				selection.setProblem(Problem.NOT_IN_SURVEY);
				selection.setInstructorLevel(selection.getLevel());
				selection.setLevel(null);
				addSelection(selection);
			} else if (original.getLevel().equals(selection.getLevel())) {
				original.setProblem(null);
			} else {
				original.setProblem(Problem.LEVEL_CHANGED);
				original.setInstructorLevel(selection.getLevel());
			}
		}
		public Selection getSelection(Long item) {
			if (iSelections == null) return null;
			for (Selection selection: iSelections)
				if (item.equals(selection.getItem())) return selection;
			return null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof Preferences)) return false;
			Preferences original = (Preferences)obj;
			if (!InstructorSurveyInterface.equals(iId, original.iId)) return false;
			int selections = (iSelections == null ? 0 : iSelections.size());
			int originalSelections = (original.iSelections == null ? 0 : original.iSelections.size());
			if (selections != originalSelections) return false; // different number of selections
			if (selections > 0) {
				s: for (Selection s: iSelections) {
					for (Selection o: original.iSelections)
						if (s.equals(o)) continue s;
					return false;
				}
			}
			return true;
		}
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", type : " + (iType == null ? "null" : "'" + iType + "'") + ", selections: " + iSelections + "}";
		}
	}
	
	public static class IdLabel implements IsSerializable, Comparable<IdLabel> {
		private Long iId;
		private String iLabel;
		private String iDescription;
		private Set<Long> iAllowedPrefs = null;

		public IdLabel() {}
		public IdLabel(Long id, String label, String description) {
			iId = id; iLabel = label; iDescription = description;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IdValue)) return false;
			return getId().equals(((IdValue)o).getId());
		}
		@Override
		public int compareTo(IdLabel other) {
			return getLabel().compareTo(other.getLabel());
		}
		
		public boolean hasAllowedPrefs() { return iAllowedPrefs != null; }
		public void addAllowedPref(Long id) {
			if (iAllowedPrefs == null) iAllowedPrefs = new HashSet<Long>();
			iAllowedPrefs.add(id);
		}
		public boolean isAllowedPref(Long id) {
			if (id == null || iAllowedPrefs == null) return true;
			return iAllowedPrefs.contains(id);
		}
		
		public boolean hasDescription() { return iDescription != null && !iDescription.isEmpty(); }
		public String getDescription() { return iDescription; }
		public void setDescription(String description) { iDescription = description; }
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", label : " + (iLabel == null ? "null" : "'" + iLabel + "'") + "}";
		}
	}
	
	public static class InstructorDepartment implements IsSerializable {
		private Long iId;
		private String iLabel;
		private String iDeptCode;
		private IdLabel iPosition;
		
		public InstructorDepartment() {}
		public InstructorDepartment(Long id, String deptCode, String label, IdLabel position) {
			iId = id; iDeptCode = deptCode; iLabel = label; iPosition = position;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getDeptCode() { return iDeptCode; }
		public void setDeptCode(String deptCode) { iDeptCode = deptCode; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public boolean hasPosition() { return iPosition != null; }
		public IdLabel getPosition() { return iPosition; }
		public void setPosition(IdLabel position) { iPosition = position; }
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", label : " + (iLabel == null ? "null" : "'" + iLabel + "'") + ", dept : " + (iDeptCode == null ? "null" : "'" + iDeptCode + "'") + "}";
		}
	}
	
	public static class PrefLevel implements IsSerializable {
		private Long iId;
		private String iLabel;
		private String iTitle;
		private String iColor;
		private String iCode;
		
		public PrefLevel() {}
		public PrefLevel(Long id, String code, String label, String title, String color) {
			iId = id; iCode = code; iLabel = label; iTitle = title; iColor = color;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code;} 
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		public boolean isHard() { return "R".equals(iCode) || "P".equals(iCode); }
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", label : " + (iLabel == null ? "null" : "'" + iLabel + "'") + "}";
		}
	}
	
	public static class InstructorSurveyRequest implements GwtRpcRequest<InstructorSurveyData> {
		private String iExternalId;
		private Long iInstructorId;
		private String iSession;
		
		public InstructorSurveyRequest() {}
		public InstructorSurveyRequest(String externalId, String session) { iExternalId = externalId; iSession = session; }
		public InstructorSurveyRequest(Long instructorId) { iInstructorId = instructorId; }
		
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		
		public Long getInstructorId() { return iInstructorId; }
		public void setInstructorId(Long instructorId) { iInstructorId = instructorId; }
		
		public String getSession() { return iSession; }
		public boolean hasSession() { return iSession != null && !iSession.isEmpty(); }
		public void setSession(String session) { iSession = session; }
	}
	
	public static class InstructorSurveyApplyRequest implements GwtRpcRequest<GwtRpcResponseNull> {
		private Long iInstructorId;
		
		public InstructorSurveyApplyRequest() {}
		public InstructorSurveyApplyRequest(Long instructorId) { iInstructorId = instructorId; }
		
		public Long getInstructorId() { return iInstructorId; }
		public void setInstructorId(Long instructorId) { iInstructorId = instructorId; }
	}
	
	public static class InstructorSurveySaveRequest implements GwtRpcRequest<InstructorSurveyData> {
		private InstructorSurveyData iData;
		private Long iInstructorId;
		private boolean iSubmit = false;
		private boolean iUnsubmit = false;
		private boolean iChanged = true;
		
		public InstructorSurveySaveRequest() {}
		public InstructorSurveySaveRequest(InstructorSurveyData data, boolean submit) {
			iData = data;
			iSubmit = submit;
		}
		
		public InstructorSurveyData getData() { return iData; }
		public void setData(InstructorSurveyData data) { iData = data; }
		public boolean isSubmit() { return iSubmit; }
		public void setSubmit(boolean submit) { iSubmit = submit; }
		public boolean isUnsubmit() { return iUnsubmit; }
		public void setUnsubmit(boolean unsubmit) { iUnsubmit = unsubmit; }
		public boolean isChanged() { return iChanged; }
		public void setChanged(boolean changed) { iChanged = changed; }
		
		public Long getInstructorId() { return iInstructorId; }
		public void setInstructorId(Long instructorId) { iInstructorId = instructorId; }
	}
	
	public static class InstructorTimePreferencesModel extends InstructorAvailabilityModel {
		private Problem iProblem = Problem.NOT_APPLIED;
		private String iInstructorPattern;
		
		public InstructorTimePreferencesModel() {
			super();
		}
		public InstructorTimePreferencesModel(InstructorTimePreferencesModel model) {
			super(model);
			iProblem = model.iProblem;
			iInstructorPattern = model.iInstructorPattern;
		}
		@Override
		public boolean hasNote() {
			return false;
		}
		
		public Problem getProblem() { return iProblem; }
		public void setProblem(Problem problem) { iProblem = problem; }
		public String getInstructorPattern() { return iInstructorPattern; }
		public void setInstructorPattern(String pattern) { iInstructorPattern = pattern; }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof InstructorTimePreferencesModel))
				return false;
			InstructorTimePreferencesModel p = (InstructorTimePreferencesModel)o;
			return
					((isEmpty() && p.isEmpty()) || InstructorSurveyInterface.equals(getPattern(), p.getPattern())) &&
					InstructorSurveyInterface.equalsString(getNote(), p.getNote());
		}
		
		@Override
		public String toString() {
			return "{ pattern : " + (isEmpty() ? "null" : "'" + getPattern() + "'") + ", note : " + (getNote() == null ? "null" : "'" + getNote() + "'") + "}";
		}
	}
	
	public static class CustomField implements IsSerializable {
		private Long iId;
		private String iName;
		private int iLength;
		
		public CustomField() {}
		public CustomField(Long id, String name, int length) {
			iId = id; iName = name; iLength = length;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public int getLength() { return iLength; }
		public void setLength(int length) { iLength = length; }
		
		@Override
		public int hashCode() { return getName().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CustomField)) return false;
			return getId().equals(((CustomField)o).getId());
		}
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", name : " + (iName == null ? "null" : "'" + iName + "'") + ", length: "  + iLength + "}";
		}
	}
	
	public static class Course extends CourseInterface {
		private Long iId;
		private String iCourseTitle;
		private Map<Long, String> iCustoms;
		
		public Course() {
			super();
		}
		public Course(Course course) {
			super(course);
			iId = course.iId;
			iCourseTitle = course.iCourseTitle;
			if (course.iCustoms != null)
				iCustoms = new HashMap<Long, String>(course.iCustoms);
		}
		
		public Long getReqId() { return iId; }
		public void setReqId(Long id) { iId = id; }

		public boolean hasCourseTitle() { return iCourseTitle != null && !iCourseTitle.isEmpty(); }
		public String getCourseTitle() { return iCourseTitle; }
		public void setCourseTitle(String courseTitle) { iCourseTitle = courseTitle; }
		
		public boolean hasCustomField(CustomField f) {
			String val = getCustomField(f);
			return val != null && !val.isEmpty();
		}
		public String getCustomField(CustomField f) {
			if (iCustoms == null) return null;
			return iCustoms.get(f.getId());
		}
		public String getCustomField(Long id) {
			if (iCustoms == null) return null;
			return iCustoms.get(id);
		}
		public void setCustomField(CustomField f, String value) {
			if (iCustoms == null) iCustoms = new HashMap<Long, String>();
			if (value != null && !value.isEmpty())
				iCustoms.put(f.getId(), value);
			else
				iCustoms.remove(f.getId());
		}
		
		public boolean hasCustomFields() { return iCustoms != null && !iCustoms.isEmpty(); }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Course)) return false;
			Course course = (Course)o;
			if (!InstructorSurveyInterface.equals(getId(), course.getId())) return false;
			if (!InstructorSurveyInterface.equalsString(getCourseName(), course.getCourseName())) return false;
			int custom = (iCustoms == null ? 0 : iCustoms.size());
			int otherCustom = (course.iCustoms == null ? 0 : course.iCustoms.size());
			if (custom != otherCustom) return false;
			if (custom > 0)
				for (Map.Entry<Long, String> e: iCustoms.entrySet())
					if (!InstructorSurveyInterface.equals(e.getValue(), course.iCustoms.get(e.getKey()))) return false; 
			return true;
		}
		
		@Override
		public String toString() {
			return "{ id : " + getId() + ", name : " + (getCourseName() == null ? "null" : "'" + getCourseName() + "'") + ", customs: "  + iCustoms + "}";
		}
	}
	
	public static enum CourseColumn {
		COURSE, CUSTOM,
	}
	
	public static enum Problem {
		NOT_APPLIED,
		LEVEL_CHANGED,
		NOT_IN_SURVEY,
		DIFFERENT_DEPT,
	}
	
	public static boolean equalsString(String o1, String o2) {
		return (o1 == null ? "" : o1).equals(o2 == null ? "" : o2);
	}
	
	public static boolean equals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }
}
