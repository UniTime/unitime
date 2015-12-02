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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Set;


import org.cpsolver.studentsct.model.Course;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.model.CourseOffering;

/**
 * @author Tomas Muller
 */
@SerializeWith(XCourseId.XCourseIdSerializer.class)
public class XCourseId implements Serializable, Comparable<XCourseId>, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iOfferingId;
	private Long iCourseId;
	private String iCourseName;
	private String iTitle = null;
    private boolean iHasUniqueName = true;
    private String iType = null;

	public XCourseId() {}
	
	public XCourseId(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XCourseId(CourseOffering course) {
		iOfferingId = course.getInstructionalOffering().getUniqueId();
		iCourseId = course.getUniqueId();
		iCourseName = course.getCourseName().trim();
		iTitle = (course.getTitle() == null ? null : course.getTitle().trim());
		iType = (course.getCourseType() == null ? null : course.getCourseType().getReference());
	}
	
	public XCourseId(Long offeringId, Long courseId, String courseName) {
		iOfferingId = offeringId;
		iCourseId = courseId;
		iCourseName = courseName;
	}
	
	public XCourseId(XCourseId course) {
		iOfferingId = course.getOfferingId();
		iCourseId = course.getCourseId();
		iCourseName = course.getCourseName();
		iTitle = course.getTitle();
		iType = course.getType();
	}
	
	public XCourseId(Course course) {
		iOfferingId = course.getOffering().getId();
		iCourseId = course.getId();
		iCourseName = course.getName();
	}

	/** Instructional offering unique id */
	public Long getOfferingId() {
		return iOfferingId;
	}
	
    /** Course offering unique id */
	public Long getCourseId() {
		return iCourseId;
	}
	
	/** Course name */
	public String getCourseName() {
		return iCourseName;
	}
	
	/** Course title */
	public String getTitle() {
		return iTitle;
	}
	
	public boolean hasType() { return iType != null && !iType.isEmpty(); }
	public String getType() { return iType; }
	
	public String getCourseNameInLowerCase() {
		return getCourseName().toLowerCase();
	}

	public boolean hasUniqueName() { return iHasUniqueName; }
	public void setHasUniqueName(boolean hasUniqueName) { iHasUniqueName = hasUniqueName; }

	@Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XCourseId)) return false;
        return getCourseId().equals(((XCourseId)o).getCourseId());
    }
    
    @Override
    public int hashCode() {
        return (int) (getCourseId() ^ (getCourseId() >>> 32));
    }
    
	@Override
	public String toString() {
		return getCourseName();
	}
	
    public int compareTo(XCourseId c) {
		int cmp = NaturalOrderComparator.getInstance().compare(getCourseName(), c.getCourseName());
		if (cmp!=0) return cmp;
		cmp = (getTitle() == null ? "" : getTitle()).compareToIgnoreCase(c.getTitle() == null ? "" : c.getTitle());
		if (cmp!=0) return cmp;
		return (getCourseId() == null ? new Long(-1) : getCourseId()).compareTo(c.getCourseId() == null ? -1 : c.getCourseId());
	}
	
	public boolean matchCourseName(String queryInLowerCase) {
		if (getCourseName().toLowerCase().startsWith(queryInLowerCase)) return true;
		if (getTitle() == null) return false;
		if ((getCourseName() + " " + getTitle()).toLowerCase().startsWith(queryInLowerCase)) return true;
		if ((getCourseName() + " - " + getTitle()).toLowerCase().startsWith(queryInLowerCase)) return true;
		return false;
	}
	
	public boolean matchTitle(String queryInLowerCase) {
		if (getTitle() == null) return false;
		if (!matchCourseName(queryInLowerCase) && (getTitle().toLowerCase().startsWith(queryInLowerCase) || getTitle().toLowerCase().contains(" " + queryInLowerCase))) return true;
		return false;
	}
	
	public boolean matchType(boolean allCourseTypes, boolean noCourseType, Set<String> allowedCourseTypes) {
		if (allCourseTypes) return true;
		if (hasType()) {
			return allowedCourseTypes != null && allowedCourseTypes.contains(getType());
		} else {
			return noCourseType;
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iOfferingId = in.readLong();
		iCourseId = in.readLong();
		iCourseName = (String)in.readObject();
		iTitle = (String)in.readObject();
		iHasUniqueName = in.readBoolean();
		iType = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iOfferingId);
		out.writeLong(iCourseId);
		out.writeObject(iCourseName);
		out.writeObject(iTitle);
		out.writeBoolean(iHasUniqueName);
		out.writeObject(iType);
	}
	
	public static class XCourseIdSerializer implements Externalizer<XCourseId> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XCourseId object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XCourseId readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XCourseId(input);
		}
	}
}
