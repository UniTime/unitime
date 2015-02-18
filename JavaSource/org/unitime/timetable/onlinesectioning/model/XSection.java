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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.model.Section;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
@SerializeWith(XSection.XSectionSerializer.class)
public class XSection implements Serializable, Comparable<XSection>, Externalizable {
    private static final long serialVersionUID = 1L;
	private Long iUniqueId = null;
    private String iName = null;
    private Map<Long, String> iNameByCourse = new HashMap<Long, String>();
    private Long iSubpartId = null;
    private Long iParentId = null;
    private int iLimit = 0;
    private String iNote = null;
    private XTime iTime = null;
    private List<XRoom> iRooms = new ArrayList<XRoom>();
    private List<XInstructor> iInstructors = new ArrayList<XInstructor>();
    private boolean iAllowOverlap = false;
    private String iInstructionalType = null;
    private String iSubpartName = null;
    private String iExternalId = null;
    private Map<Long, String> iExternalIdByCourse = new HashMap<Long, String>();
    private boolean iEnabledForScheduling = true;

    public XSection() {
    }
    
    public XSection(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }

    public XSection(Class_ clazz, OnlineSectioningHelper helper) {
    	iUniqueId = clazz.getUniqueId();
    	iAllowOverlap = clazz.getSchedulingSubpart().isStudentAllowOverlap();
    	iName = (clazz.getClassSuffix() == null ? clazz.getSectionNumber(helper.getHibSession()) + clazz.getSchedulingSubpart().getSchedulingSubpartSuffix(helper.getHibSession()) : clazz.getClassSuffix());
        iInstructionalType = clazz.getSchedulingSubpart().getItypeDesc();
        iSubpartName = clazz.getSchedulingSubpart().getItype().getAbbv().trim();
    	Assignment assignment = clazz.getCommittedAssignment();
    	iEnabledForScheduling = clazz.isEnabledForStudentScheduling();
        if (!clazz.isEnabledForStudentScheduling()) {
        	iLimit = 0;
        } else if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) {
        	iLimit = -1;
        } else {
        	iLimit = clazz.getMaxExpectedCapacity();
        	if (clazz.getExpectedCapacity() < clazz.getMaxExpectedCapacity() && assignment != null && !assignment.getRooms().isEmpty()) {
        		int roomSize = Integer.MAX_VALUE;
        		for (Location room: assignment.getRooms())
        			roomSize = Math.min(roomSize, room.getCapacity() == null ? 0 : room.getCapacity());
        		int roomLimit = (int) Math.floor(roomSize / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
        		iLimit = Math.min(Math.max(clazz.getExpectedCapacity(), roomLimit), clazz.getMaxExpectedCapacity());
        	}
            if (iLimit >= 9999) iLimit = -1;
        }
        iParentId = (clazz.getParentClass() == null ? null : clazz.getParentClass().getUniqueId());
        iSubpartId = clazz.getSchedulingSubpart().getUniqueId();
        iNote = clazz.getSchedulePrintNote();
        iExternalId = clazz.getExternalUniqueId();
        if (iExternalId == null)
        	iExternalId = clazz.getClassLabel();
        for (CourseOffering course: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
        	iNameByCourse.put(course.getUniqueId(), clazz.getClassSuffix(course));
        	String extId = clazz.getExternalId(course);
        	if (extId == null)
        		extId = clazz.getClassLabel(course);
        	iExternalIdByCourse.put(course.getUniqueId(), extId);
        }
        iNameByCourse.put(-1l, clazz.getSectionNumberString(helper.getHibSession()));
        if (assignment != null) {
        	iTime = new XTime(assignment, helper.getExactTimeConversion());
        	for (Location room: assignment.getRooms())
        		iRooms.add(new XRoom(room));
        }
        
        if (clazz.isDisplayInstructor())
            for (ClassInstructor ci: clazz.getClassInstructors()) {
            	iInstructors.add(new XInstructor(ci.getInstructor(), helper));
            }
    }
    
    public XSection(Section section) {
    	iUniqueId = section.getId();
    	iAllowOverlap = section.getSubpart().isAllowOverlap();
    	iName = section.getName();
    	iNameByCourse = section.getNameByCourse();
    	iSubpartName = section.getSubpart().getName();
    	iLimit = section.getLimit();
    	iParentId = (section.getParent() == null ? null : section.getParent().getId());
    	iSubpartId = section.getSubpart().getId();
    	iInstructionalType = section.getSubpart().getInstructionalType();
    	iNote = section.getNote();
    	iTime = section.getTime() == null ? null : new XTime(section.getTime());
    	if (section.getNrRooms() > 0)
    		for (RoomLocation room: section.getRooms())
    			iRooms.add(new XRoom(room));
    	if (section.getChoice() != null && section.getChoice().getInstructorNames() != null) {
    		String[] ids = section.getChoice().getInstructorIds().split(":");
    		int i = 0;
    		for (String instructor: section.getChoice().getInstructorNames().split(":")) {
    			String[] nameEmail = instructor.split("\\|");
    			iInstructors.add(new XInstructor(
    					Long.valueOf(ids[i]),
    					null,
    					nameEmail[0],
    					nameEmail.length > 1 ? nameEmail[1] : null));
    			i++;
    		}
    	}
    }
    
    /** For testing only! */
    @Deprecated
    public XSection(String externalId) {
    	iExternalId = externalId;
    }
    
    /** Section id */
    public Long getSectionId() { return iUniqueId; }

    /**
     * Section limit. This is defines the maximal number of students that can be
     * enrolled into this section at the same time. It is -1 in the case of an
     * unlimited section
     */
    public int getLimit() { return iLimit; }

    /** Section name */
    public String getName() { return iName; }
    
    /** Scheduling subpart to which this section belongs */
    public Long getSubpartId() { return iSubpartId; }

    /**
     * Parent section of this section (can be null). If there is a parent
     * section defined, a student that is enrolled in this section has to be
     * enrolled in the parent section as well. Also, the same relation needs to
     * be defined between subpart of this section and the subpart of the parent
     * section.
     */
    public Long getParentId() { return iParentId; }
    
    /** Time placement of the section. */
    public XTime getTime() {
    	return iTime;
    }

    /** Number of rooms in which the section meet. */
    public int getNrRooms() {
        return (iRooms == null ? 0 : iRooms.size());
    }

    /**
     * Room placement -- list of
     * {@link org.cpsolver.coursett.model.RoomLocation}
     */
    public List<XRoom> getRooms() {
    	return iRooms;
    }
    
    public String toRoomString(String delim) {
    	String ret = "";
    	for (XRoom room: iRooms) {
    		if (!ret.isEmpty()) ret += delim;
    		ret += room.getName();
    	}
    	return ret;
    }

    @Override
    public String toString() {
    	return getSubpartName() + " " + getName() + " " + (getTime() == null ? "Arrange Hours" : getTime().toString()) + (getNrRooms() == 0 ? "" : " " + toRoomString(", "));
    }
    
    public String toString(Long coruseId) {
    	return getSubpartName() + " " + getName(coruseId) + " " + (getTime() == null ? "Arrange Hours" : getTime().toString()) + (getNrRooms() == 0 ? "" : " " + toRoomString(", "));
    }

    /**
     * Return course-dependent section name
     */
    public String getName(long courseId) {
        if (iNameByCourse == null) return getName();
        String name = iNameByCourse.get(courseId);
        return (name == null ? getName() : name);
    }
    
    /**
     * Return course-dependent external id
     */
    public String getExternalId(long courseId) {
        if (iExternalIdByCourse == null) return iExternalId;
        String externalId = iExternalIdByCourse.get(courseId);
        return (externalId == null ? iExternalId : externalId);
    }

    /**
     * Return course-dependent section names
     */
    public Map<Long, String> getNameByCourse() { return iNameByCourse; }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XSection)) return false;
        return getSectionId().equals(((XSection)o).getSectionId());
    }
    
    @Override
    public int hashCode() {
        return (int) (getSectionId() ^ (getSectionId() >>> 32));
    }
    
    /**
     * Section note
     */
    public String getNote() { return iNote; }
    
    /**
     * Instructors
     */
    public List<XInstructor> getInstructors() { return iInstructors; }
    
    public String getInstructorIds() {
    	if (iInstructors == null || iInstructors.isEmpty()) return null;
    	StringBuffer ret = new StringBuffer();
    	for (XInstructor instructor: iInstructors) {
    		if (ret.length() > 0) ret.append(":");
    		ret.append(instructor.getIntructorId());
    	}
    	return ret.toString();
    }
    
    public String getInstructorNames() {
    	if (iInstructors == null || iInstructors.isEmpty()) return null;
    	StringBuffer ret = new StringBuffer();
    	for (XInstructor instructor: iInstructors) {
    		if (ret.length() > 0) ret.append(":");
    		ret.append(instructor.getName() + "|"  + (instructor.getEmail() == null ? "" : instructor.getEmail()));
    	}
    	return ret.toString();
    }
    
    /**
     * Returns true if student conflicts between this section and the given one are to be ignored
     */
    public boolean isToIgnoreStudentConflictsWith(Collection<XDistribution> distributions, Long sectionId) {
    	if (distributions == null) return false;
    	for (XDistribution distribution: distributions)
    		if (distribution.getDistributionType() == XDistributionType.IngoreConflicts &&
    			distribution.getSectionIds().contains(getSectionId()) &&
    			distribution.getSectionIds().contains(sectionId))
    			return true;
    	return false;
    }
    
	@Override
	public int compareTo(XSection section) {
		return getSectionId().compareTo(section.getSectionId());
	}
	
	private int getDistanceInMinutes(DistanceMetric m,  List<XRoom> other) {
        int dist = 0;
        for (XRoom r1 : getRooms())
            for (XRoom r2 : other) {
                dist = Math.max(dist, r1.getDistanceInMinutes(m, r2));
            }
        return dist;
	}
	
	public int getDistanceInMinutes(XSection other, DistanceMetric m) {
		if (getNrRooms() == 0 || other.getNrRooms() == 0) return 0;
		XTime t1 = getTime();
		XTime t2 = other.getTime();
		if (t1 == null || t2 == null || !t1.shareDays(t2) || !t1.shareWeeks(t2)) return 0;
        int a1 = t1.getSlot(), a2 = t2.getSlot();
        if (m.doComputeDistanceConflictsBetweenNonBTBClasses()) {
        	if (a1 + t1.getLength() <= a2) {
        		int dist = getDistanceInMinutes(m, other.getRooms());
        		if (dist > t1.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()))
        			return dist;
        	}
        } else {
        	if (a1 + t1.getLength() == a2)
        		return getDistanceInMinutes(m, other.getRooms());
        }
        return 0;
    }
	
    /** Return true if overlaps are allowed, but the number of overlapping slots should be minimized. */
    public boolean isAllowOverlap() {
        return iAllowOverlap;
    }
    
    public String getSubpartName() {
    	return iSubpartName;
    }
    
    public String getInstructionalType() {
    	return iInstructionalType;
    }
    
    public boolean isEnabledForScheduling() {
    	return iEnabledForScheduling;
    }
    
    /**
     * True, if this section overlaps with the given assignment in time and
     * space
     */
    public boolean isOverlapping(Collection<XDistribution> distributions, XSection other) {
        if (isAllowOverlap() || other.isAllowOverlap()) return false;
        if (getTime() == null || other.getTime() == null) return false;
        if (isToIgnoreStudentConflictsWith(distributions, other.getSectionId())) return false;
        return getTime().hasIntersection(other.getTime());
    }
	
    /**
     * True, if this section overlaps with one of the given set of assignments
     * in time and space
     */
    public boolean isOverlapping(Collection<XDistribution> distributions, Collection<XSection> assignments) {
        if (isAllowOverlap()) return false;
        if (getTime() == null || assignments == null)
            return false;
        for (XSection assignment : assignments) {
            if (assignment.isAllowOverlap())
                continue;
            if (assignment.getTime() == null)
                continue;
            if (isToIgnoreStudentConflictsWith(distributions, assignment.getSectionId()))
                continue;
            if (getTime().hasIntersection(assignment.getTime()))
                return true;
        }
        return false;
    }
    
    public Placement toPlacement() {
    	if (getTime() == null) return null;
        List<RoomLocation> rooms = new ArrayList<RoomLocation>();
        for (XRoom r: getRooms())
        	rooms.add(new RoomLocation(r.getUniqueId(), r.getName(), null, 0, 0, r.getX(), r.getY(), r.getIgnoreTooFar(), null));
        return new Placement(
        		new Lecture(getSectionId(), null, getSubpartId(), getName(), new ArrayList<TimeLocation>(), new ArrayList<RoomLocation>(), getNrRooms(), null, getLimit(), getLimit(), 1.0),
        		getTime().toTimeLocation(),
        		rooms);
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = in.readLong();
		iName = (String)in.readObject();
		
		int nrNames = in.readInt();
		iNameByCourse.clear();
		for (int i = 0; i < nrNames; i++)
			iNameByCourse.put(in.readLong(), (String)in.readObject());
		
		iSubpartId = in.readLong();
		iParentId = in.readLong();
		if (iParentId < 0) iParentId = null;
		iLimit = in.readInt();
		iNote = (String)in.readObject();
		iTime = (in.readBoolean() ? new XTime(in) : null);
		
		int nrRooms = in.readInt();
		iRooms.clear();
		for (int i = 0; i < nrRooms; i++)
			iRooms.add(new XRoom(in));
		
		int nrInstructors = in.readInt();
		iInstructors.clear();
		for (int i = 0; i < nrInstructors; i++)
			iInstructors.add(new XInstructor(in));
		
		iAllowOverlap = in.readBoolean();
		iInstructionalType = (String)in.readObject();
		iSubpartName = (String)in.readObject();
		
		iExternalId = (String)in.readObject();
		int nrExtIds = in.readInt();
		iExternalIdByCourse.clear();
		for (int i = 0; i < nrExtIds; i++)
			iExternalIdByCourse.put(in.readLong(), (String)in.readObject());
		iEnabledForScheduling = in.readBoolean();

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iUniqueId);
		out.writeObject(iName);
		
		out.writeInt(iNameByCourse.size());
		for (Map.Entry<Long, String> entry: iNameByCourse.entrySet()) {
			out.writeLong(entry.getKey());
			out.writeObject(entry.getValue());
		}
		
		out.writeLong(iSubpartId);
		out.writeLong(iParentId == null ? -1l : iParentId);
		out.writeInt(iLimit);
		out.writeObject(iNote);
		
		out.writeBoolean(iTime != null);
		if (iTime != null)
			iTime.writeExternal(out);
		
		out.writeInt(iRooms.size());
		for (XRoom room: iRooms)
			room.writeExternal(out);
		
		out.writeInt(iInstructors.size());
		for (XInstructor instructor: iInstructors)
			instructor.writeExternal(out);
		
		out.writeBoolean(iAllowOverlap);
		out.writeObject(iInstructionalType);
		out.writeObject(iSubpartName);
		
		out.writeObject(iExternalId);
		out.writeInt(iExternalIdByCourse.size());
		for (Map.Entry<Long, String> entry: iExternalIdByCourse.entrySet()) {
			out.writeLong(entry.getKey());
			out.writeObject(entry.getValue());
		}
		out.writeBoolean(iEnabledForScheduling);
	}
	
	public static class XSectionSerializer implements Externalizer<XSection> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XSection object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XSection readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XSection(input);
		}
	}
}