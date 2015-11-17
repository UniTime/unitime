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
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class DegreePlanInterface implements IsSerializable {
	private Long iStudentId, iSessionId;
	
	private String iId, iName, iDegree, iSchool, iTrack;
	private Date iModified;
	private DegreeGroupInterface iGroup;
	
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
	public DegreeGroupInterface getGroup() { return iGroup; }
	public void setGroup(DegreeGroupInterface group) { iGroup = group; }
	
	@Override
	public String toString() { return iName + ": " + iGroup; }
	
	public static abstract class DegreeItemInterface implements IsSerializable {
	}
	
	public static class DegreeGroupInterface extends DegreeItemInterface {
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
		
		public int getMaxDepth() {
			if (iGroups == null || iGroups.isEmpty()) return 1;
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
	}

	public static class DegreeCourseInterface extends DegreeItemInterface {
		private Long iCourseId = null;
		private String iSubject, iCourse, iTitle, iName;
		private Boolean iSelected = null;
		
		public DegreeCourseInterface() {}
		
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		public String getCourse() { return iCourse; }
		public void setCourse(String course) { iCourse = course; }
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public boolean hasSelected() { return iSelected != null; }
		public boolean isSelected() { return iSelected == null || iSelected.booleanValue(); }
		public void setSelected(boolean selected) { iSelected = selected; }
		public String getName() { return iName == null ? iSubject + " " + iCourse : iName; }
		public void setName(String name) { iName = name; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(long courseId) { iCourseId = courseId; }
		
		@Override
		public String toString() { return iSubject + " " + iCourse; }
	}
	
	public static class DegreePlaceHolderInterface extends DegreeItemInterface {
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
