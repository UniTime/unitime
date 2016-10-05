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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class DegreePlanInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	private Long iStudentId, iSessionId;
	
	private String iId, iName, iDegree, iSchool, iTrack, iModifiedWho;
	private Date iModified;
	private DegreeGroupInterface iGroup;
	
	private boolean iLocked = false, iActive = false;
	
	public DegreePlanInterface() {
	}
	
	public Long getStudentId() { return iStudentId; }
	public void setStudentId(Long studentId) { iStudentId = studentId; }
	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }
	public String getId() { return iId; }
	public void setId(String id) { iId = id; }
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public String getDegree() { return iDegree; }
	public void setDegree(String degree) { iDegree = degree; }
	public String getSchool() { return iSchool; }
	public void setSchool(String school) { iSchool = school; }
	public String getTrackingStatus() { return iTrack; }
	public void setTrackingStatus(String track) { iTrack = track; }
	public Date getLastModified() { return iModified; }
	public void setLastModified(Date modified) { iModified = modified; }
	public String getModifiedWho() { return iModifiedWho; }
	public void setModifiedWho(String name) { iModifiedWho = name; }
	public DegreeGroupInterface getGroup() { return iGroup; }
	public void setGroup(DegreeGroupInterface group) { iGroup = group; }
	public boolean isActive() { return iActive; }
	public void setActive(boolean active) { iActive = active; }
	public boolean isLocked() { return iLocked; }
	public void setLocked(boolean locked) { iLocked = locked; }
	
	public List<DegreeCourseInterface> listSelected() {
		List<DegreeCourseInterface> ret = new ArrayList<DegreeCourseInterface>();
		if (iGroup != null) iGroup.listSelected(ret);
		return ret;
	}
	
	public boolean hasCourse(String course) {
		if (iGroup != null) return iGroup.hasCourse(course);
		return false;
	}
	
	public boolean isCourseSelected(String course) {
		if (iGroup != null) return iGroup.isCourseSelected(course);
		return false;
	}
	
	public boolean hasCourse(RequestedCourse course) {
		if (iGroup != null) return iGroup.hasCourse(course);
		return false;
	}
	
	public boolean isCourseSelected(RequestedCourse course) {
		if (iGroup != null) return iGroup.isCourseSelected(course);
		return false;
	}
	
	@Override
	public String toString() { return iName + ": " + iGroup; }
	
	public static abstract class DegreeItemInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iId = null;
		
		public String getId() { return iId; }
		public void setId(String id) { iId = id; }
	}
	
	public static class DegreeGroupInterface extends DegreeItemInterface {
		private static final long serialVersionUID = 1L;
		private boolean iChoice = false;
		List<DegreeCourseInterface> iCourses = null;
		List<DegreeGroupInterface> iGroups = null;
		List<DegreePlaceHolderInterface> iPlaceHolders = null;
		private Boolean iSelected = null;
		private String iDescription = null;
		
		public DegreeGroupInterface() {}
		
		public boolean isChoice() { return iChoice; }
		public void setChoice(boolean choice) { iChoice = choice; }
		public boolean hasCourses() { return iCourses != null && !iCourses.isEmpty(); }
		public boolean hasMultipleCourses() { 
			if (iCourses == null) return false;
			for (DegreeCourseInterface course: iCourses)
				if (course.hasMultipleCourses()) return true;
			return false;
		}
		public List<DegreeCourseInterface> getCourses() { return iCourses; }
		public void addCourse(DegreeCourseInterface course) {
			if (iCourses == null) iCourses = new ArrayList<DegreeCourseInterface>();
			iCourses.add(course);
		}
		public boolean hasGroups() { return iGroups != null && !iGroups.isEmpty(); }
		public List<DegreeGroupInterface> getGroups() { return iGroups; }
		public void addGroup(DegreeGroupInterface group) {
			if (iGroups == null) iGroups = new ArrayList<DegreeGroupInterface>();
			iGroups.add(group);
		}
		public void merge(DegreeGroupInterface group) {
			if (group.hasCourses())
				for (DegreeCourseInterface course: group.getCourses())
					addCourse(course);
			if (group.hasPlaceHolders())
				for (DegreePlaceHolderInterface ph: group.getPlaceHolders())
					addPlaceHolder(ph);
			if (group.hasGroups())
				for (DegreeGroupInterface g: group.getGroups()) {
					if (isChoice() == g.isChoice())
						merge(g);
					else
						addGroup(g);
				}
		}
		public boolean hasPlaceHolders() { return iPlaceHolders != null && !iPlaceHolders.isEmpty(); }
		public List<DegreePlaceHolderInterface> getPlaceHolders() { return iPlaceHolders; }
		public void addPlaceHolder(DegreePlaceHolderInterface placeHolder) {
			if (iPlaceHolders == null) iPlaceHolders = new ArrayList<DegreePlaceHolderInterface>();
			iPlaceHolders.add(placeHolder);
		}
		public boolean hasSelected() { return iSelected != null; }
		public boolean isSelected() { return iSelected == null || iSelected.booleanValue(); }
		public void setSelected(boolean selected) { iSelected = selected; }
		public boolean hasDescription() { return iDescription != null && !iDescription.isEmpty(); }
		public String getDescription() { return hasDescription() ? iDescription : toString(); }
		public void setDescription(String description) { iDescription = description; }
		
		public int countItems() {
			return (hasPlaceHolders() ? getPlaceHolders().size() : 0) + (hasGroups() ? getGroups().size() : 0) + (hasCourses() ? getCourses().size() : 0);
		}

		public boolean isUnionGroupWithOneChoice() {
			if (isChoice()) return false;
			int nrChoices = (hasPlaceHolders() ? getPlaceHolders().size() : 0) +
					(hasGroups() ? getGroups().size() : 0) +
					(hasCourses() ? getCourses().size() : 0);
			return nrChoices == 1;
		}
		
		public int getMaxDepth() {
			if (iGroups == null || iGroups.isEmpty()) return (!isChoice() && hasMultipleCourses() ? 2 : 1);
			int ret = 0;
			for (DegreeGroupInterface g: iGroups)
				if (ret < g.getMaxDepth())
					ret = g.getMaxDepth();
			return 1 + ret;
		}
		
		@Override
		public String toString() {
			String ret = "";
			if (iCourses != null)
				for (DegreeCourseInterface course: iCourses)
					ret += (ret.isEmpty() ? "" : iChoice ? " and " : " or ") + course;
			if (iGroups != null)
				for (DegreeGroupInterface group: iGroups)
					ret += (ret.isEmpty() ? "" : iChoice ? " and " : " or ") + "(" + group + ")";
			if (iPlaceHolders != null)
				for (DegreePlaceHolderInterface ph: iPlaceHolders)
					ret += (ret.isEmpty() ? "" : iChoice ? " and " : " or ") + ph;
			return ret;
		}
		
		public String toString(StudentSectioningMessages MESSAGES) {
			List<String> items = new ArrayList<String>();
			if (iCourses != null)
				for (DegreeCourseInterface course: iCourses)
					items.add(MESSAGES.course(course.getSubject(), course.getCourse()));
			if (iGroups != null)
				for (DegreeGroupInterface group: iGroups)
					items.add(MESSAGES.surroundWithBrackets(group.toString(MESSAGES)));
			if (iPlaceHolders != null)
				for (DegreePlaceHolderInterface ph: iPlaceHolders)
					items.add(ph.getType());
			switch (items.size()) {
			case 0:
				return "";
			case 1:
				return items.get(0);
			case 2:
				return (isChoice() ? MESSAGES.choiceSeparatorPair(items.get(0), items.get(1)) : MESSAGES.courseSeparatorPair(items.get(0), items.get(1)));
			default:
				String ret = null;
				for (Iterator<String> i = items.iterator(); i.hasNext(); ) {
					String item = i.next();
					if (ret == null)
						ret = item;
					else {
						if (i.hasNext()) {
							ret = (isChoice() ? MESSAGES.choiceSeparatorMiddle(ret, item) : MESSAGES.courseSeparatorMiddle(ret, item));
						} else {
							ret = (isChoice() ? MESSAGES.choiceSeparatorLast(ret, item) : MESSAGES.courseSeparatorLast(ret, item));
						}
					}
				}
				return ret;
			}
		}
		
		protected void listSelected(List<DegreeCourseInterface> requested) {
			if (hasCourses())
				for (DegreeCourseInterface course: getCourses())
					if (!isChoice() || course.isSelected()) requested.add(course);
			if (hasGroups())
				for (DegreeGroupInterface g: getGroups())
					if (!isChoice() || g.isSelected()) g.listSelected(requested);
		}
		
		public boolean hasSelection() {
			if (hasCourses())
				for (DegreeCourseInterface course: getCourses())
					if (!isChoice() || course.isSelected()) return true;
			if (hasGroups())
				for (DegreeGroupInterface g: getGroups())
					if (!isChoice() || g.isSelected()) return true;
			return false;
		}
		
		protected boolean hasCourse(String name) {
			if (iCourses != null)
				for (DegreeCourseInterface course: iCourses) {
					if (course.hasCourses()) {
						for (CourseAssignment ca: course.getCourses())
							if (name.equalsIgnoreCase(ca.getCourseName()) || name.equalsIgnoreCase(ca.getCourseNameWithTitle())) return true;
					}
					if (name.equals(course.getCourseName()) || name.equalsIgnoreCase(course.getCourseNameWithTitle())) return true;
				}
			if (iGroups != null)
				for (DegreeGroupInterface g: iGroups)
					if (g.hasCourse(name)) return true;
			return false;
		}
		
		protected boolean isCourseSelected(String name) {
			if (iCourses != null)
				for (DegreeCourseInterface course: iCourses) {
					if (isChoice() && !course.isSelected()) continue;
					if (course.hasCourses() && course.getCourseId() != null) {
						for (CourseAssignment ca: course.getCourses())
							if (course.getCourseId().equals(ca.getCourseId()) && (name.equalsIgnoreCase(ca.getCourseName()) || name.equalsIgnoreCase(ca.getCourseNameWithTitle()))) return true;
					} else {
						if (name.equals(course.getCourseName()) || name.equalsIgnoreCase(course.getCourseNameWithTitle())) return true;
					}
				}
			if (iGroups != null)
				for (DegreeGroupInterface g: iGroups) {
					if ((!isChoice() || g.isSelected()) && g.hasCourse(name)) return true;
				}
			return false;
		}
		
		protected boolean hasCourse(RequestedCourse rc) {
			if (iCourses != null)
				for (DegreeCourseInterface course: iCourses) {
					if (course.hasCourses()) {
						for (CourseAssignment ca: course.getCourses())
							if (rc.equals(ca)) return true;
					}
					if (rc.equals(course)) return true;
				}
			if (iGroups != null)
				for (DegreeGroupInterface g: iGroups)
					if (g.hasCourse(rc)) return true;
			return false;
		}
		
		protected boolean isCourseSelected(RequestedCourse rc) {
			if (iCourses != null)
				for (DegreeCourseInterface course: iCourses) {
					if (isChoice() && !course.isSelected()) continue;
					if (course.hasCourses() && course.getCourseId() != null) {
						for (CourseAssignment ca: course.getCourses())
							if (course.getCourseId().equals(ca.getCourseId()) && rc.equals(ca)) return true;
					} else {
						if (rc.equals(course)) return true;
					}
				}
			if (iGroups != null)
				for (DegreeGroupInterface g: iGroups) {
					if ((!isChoice() || g.isSelected()) && g.hasCourse(rc)) return true;
				}
			return false;
		}
	}

	public static class DegreeCourseInterface extends DegreeItemInterface {
		private static final long serialVersionUID = 1L;
		private Long iCourseId = null;
		private String iSubject, iCourse, iTitle;
		private Boolean iSelected = null;
		private List<CourseAssignment> iCourses;
		
		public DegreeCourseInterface() {}
		
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		public String getCourse() { return iCourse; }
		public void setCourse(String course) { iCourse = course; }
		public boolean hasTitle() { return iTitle != null && !iTitle.isEmpty(); }
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public boolean hasSelected() { return iSelected != null; }
		public boolean isSelected() { return iSelected == null || iSelected.booleanValue(); }
		public void setSelected(boolean selected) { iSelected = selected; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		
		public String getCourseName() {
			return getSubject() + " " + getCourse();
		}
		public String getCourseNameWithTitle() {
			return hasTitle() ? getSubject() + " " + getCourse() + " - " + getTitle() : getSubject() + " " + getCourse();
		}
		
		public boolean hasCourses() { return iCourses != null && !iCourses.isEmpty(); }
		public boolean hasMultipleCourses() { return iCourses != null && iCourses.size() > 1; }
		public List<CourseAssignment> getCourses() { return iCourses; }
		public void addCourse(CourseAssignment course) {
			if (iCourses == null) iCourses = new ArrayList<CourseAssignment>();
			iCourses.add(course);
		}
		
		public CourseAssignment getSelectedCourse() {
			if (iCourses != null && iCourseId != null)
				for (CourseAssignment course: iCourses)
					if (iCourseId.equals(course.getCourseId())) return course;
			return null;
		}
		
		@Override
		public String toString() {
			CourseAssignment ca = getSelectedCourse();
			if (ca != null) return ca.getCourseName();
			return getCourseName();
		}
	}
	
	public static class DegreePlaceHolderInterface extends DegreeItemInterface {
		private static final long serialVersionUID = 1L;
		private String iType;
		private String iName;
		
		public DegreePlaceHolderInterface() {}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		@Override
		public String toString() { return iType; }
	}
}
