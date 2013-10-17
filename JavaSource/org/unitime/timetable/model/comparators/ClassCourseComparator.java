/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.comparators;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.solver.ClassAssignmentProxy;

/**
 * @author Tomas Muller
 */
public class ClassCourseComparator implements Comparator {
	static CourseMessages MSG = Localization.create(CourseMessages.class);
	SortBy iSortyBy;
	ClassAssignmentProxy iClassAssignmentProxy;
	boolean iKeepSubpart;
	
	public static enum SortBy {
		NAME, DIV_SEC, ENROLLMENT, LIMIT, ROOM_SIZE, DATE_PATTERN, TIME_PATTERN, INSTRUCTOR, ASSIGNED_TIME, ASSIGNED_ROOM, ASSIGNED_ROOM_CAP
	};
	
	public static String getName(SortBy sortBy) {
		switch (sortBy) {
		case NAME:
			return MSG.sortByName();
		case DIV_SEC:
			return MSG.sortByDivSec();
		case ENROLLMENT:
			return MSG.sortByEnrollment();
		case LIMIT:
			return MSG.sortByLimit();
		case ROOM_SIZE:
			return MSG.sortByRoomSize();
		case DATE_PATTERN:
			return MSG.sortByDatePattern();
		case TIME_PATTERN:
			return MSG.sortByTimePattern();
		case INSTRUCTOR:
			return MSG.sortByInstructor();
		case ASSIGNED_TIME:
			return MSG.sortByAssignedTime();
		case ASSIGNED_ROOM:
			return MSG.sortByAssignedRoom();
		case ASSIGNED_ROOM_CAP:
			return MSG.sortByAssignedRoomCapacity();
		default:
			return MSG.sortByName();
		}
	}
	
	public static SortBy getSortBy(String name) {
		for (SortBy s: SortBy.values())
			if (getName(s).equals(name)) return s;
		return SortBy.NAME;
	}
	
	public static String[] getNames() {
		String[] names = new String[SortBy.values().length];
		for (int i = 0; i < SortBy.values().length; i++)
			names[i] = getName(SortBy.values()[i]);
		return names;
	}

	public ClassCourseComparator (String sortBy, ClassAssignmentProxy classAssignmentProxy, boolean keepSubparts) {
    	iSortyBy = getSortBy(sortBy);
    	iClassAssignmentProxy = classAssignmentProxy;
    	iKeepSubpart = keepSubparts;
    }

	public boolean isParent(SchedulingSubpart s1, SchedulingSubpart s2) {
		SchedulingSubpart p1 = s1.getParentSubpart();
		if (p1==null) return false;
		if (p1.equals(s2)) return true;
		return isParent(p1, s2);
	}
	
	public int compareSubparts(SchedulingSubpart s1, SchedulingSubpart s2) {
        if (isParent(s1,s2)) return 1;
        if (isParent(s2,s1)) return -1;
        
        if (s1.getParentSubpart() != null || s2.getParentSubpart() != null) {
        	SchedulingSubpart p1 = s1; int d1 = 0;
        	while (p1.getParentSubpart() != null) { p1 = p1.getParentSubpart(); d1 ++; }
        	
        	SchedulingSubpart p2 = s2; int d2 = 0;
        	while (p2.getParentSubpart() != null) { p2 = p2.getParentSubpart(); d2 ++; }
        	
        	if (d1 < d2) {
        		int cmp = compareSubparts(s1, s2.getParentSubpart());
        		if (cmp != 0) return cmp;
        	} else if (d1 > d2) {
        		int cmp = compareSubparts(s1.getParentSubpart(), s2);
        		if (cmp != 0) return cmp;
        	} else {
        		int cmp = compareSubparts(s1.getParentSubpart(), s2.getParentSubpart());
        		if (cmp != 0) return cmp;
        	}
        }
        
        int cmp = s1.getItype().getItype().compareTo(s2.getItype().getItype());
        if (cmp!=0) return cmp;
        
        return s1.getUniqueId().compareTo(s2.getUniqueId());		
	}
	
    public boolean isParentSameIType(SchedulingSubpart s1, SchedulingSubpart s2) {
		SchedulingSubpart p1 = s1.getParentSubpart();
		if (p1==null) return false;
		if (p1.equals(s2)) return true;
		if (!p1.getItype().equals(s2.getItype())) return false;
		return isParentSameIType(p1, s2);
	}

    public int compareClasses(CourseOffering co1, CourseOffering co2, Class_ c1, Class_ c2) {
    	int cmp = 0;
    	Assignment a1, a2;
    	try {
    		switch (iSortyBy) {
    		case NAME:
    			if (!co1.equals(co2)) {
    				cmp = compareComparable(co1, co2);
    			} else  if (!c1.getSchedulingSubpart().equals(c2.getSchedulingSubpart()))
    				cmp = compareSubparts(c1.getSchedulingSubpart(), c2.getSchedulingSubpart());
        		else
        			cmp = c1.getSectionNumber().compareTo(c2.getSectionNumber());
    			break;
    		case DIV_SEC:
    			String sx1 = c1.getClassSuffix(co1);
    			String sx2 = c2.getClassSuffix(co2);
    			if (sx1==null) {
    				if (sx2==null) {
    					cmp = c1.getSectionNumber().compareTo(c2.getSectionNumber());
    				} else {
    					return -1;
    				}
    			} else {
    				if (sx2==null) {
    					return 1;
    				} else {
    					cmp = sx1.compareTo(sx2);
    					if (cmp!=0) return cmp;
    					cmp = c1.getSectionNumber().compareTo(c2.getSectionNumber());
    				}
    			}
    			break;
    		case TIME_PATTERN:
        		Set t1s = c1.effectiveTimePatterns();
        		Set t2s = c2.effectiveTimePatterns();
        		TimePattern p1 = (t1s==null || t1s.isEmpty() ? null : (TimePattern)t1s.iterator().next());
        		TimePattern p2 = (t2s==null || t2s.isEmpty() ? null : (TimePattern)t2s.iterator().next());
        		cmp = compareComparable(p1,p2);
        		break;
    		case LIMIT:
        		cmp = compareComparable(c1.getExpectedCapacity(),c2.getExpectedCapacity());
        		break;
    		case ROOM_SIZE:
        		cmp = compareComparable(c1.getMinRoomLimit(),c2.getMinRoomLimit());
        		break;
    		case DATE_PATTERN:
        		a1 = (iClassAssignmentProxy==null?null:iClassAssignmentProxy.getAssignment(c1));
        		a2 = (iClassAssignmentProxy==null?null:iClassAssignmentProxy.getAssignment(c2));
        		DatePattern d1 = (a1 == null ? c1.effectiveDatePattern() : a1.getDatePattern());
        		DatePattern d2 = (a2 == null ? c2.effectiveDatePattern() : a2.getDatePattern());
        		cmp = compareComparable(d1, d2);
        		break;
    		case INSTRUCTOR:
        		cmp = compareInstructors(c1,c2);
        		break;
    		case ASSIGNED_TIME:
        		a1 = (iClassAssignmentProxy==null?null:iClassAssignmentProxy.getAssignment(c1));
        		a2 = (iClassAssignmentProxy==null?null:iClassAssignmentProxy.getAssignment(c2));
        		if (a1==null) {
        			cmp = (a2==null?0:-1);
        		} else {
        			if (a2==null)
        				cmp = 1;
        			else {
        				TimeLocation t1 = a1.getPlacement().getTimeLocation();
        				TimeLocation t2 = a2.getPlacement().getTimeLocation();
        				cmp = t1.getStartSlots().nextElement().compareTo(t2.getStartSlots().nextElement());
        				if (cmp==0)
        					cmp = Double.compare(t1.getDayCode(), t2.getDayCode());
        				if (cmp==0)
        					cmp = Double.compare(t1.getLength(), t2.getLength());
        			}
        		}
        		break;
    		case ASSIGNED_ROOM:
        		a1 = (iClassAssignmentProxy==null?null:iClassAssignmentProxy.getAssignment(c1));
        		a2 = (iClassAssignmentProxy==null?null:iClassAssignmentProxy.getAssignment(c2));
        		if (a1==null) {
        			cmp = (a2==null?0:-1);
        		} else {
        			if (a2==null)
        				cmp = 1;
        			else
        				cmp = a1.getPlacement().getRoomName(",").compareTo(a2.getPlacement().getRoomName(","));
        		}
        		break;
    		case ASSIGNED_ROOM_CAP:
        		a1 = (iClassAssignmentProxy==null?null:iClassAssignmentProxy.getAssignment(c1));
        		a2 = (iClassAssignmentProxy==null?null:iClassAssignmentProxy.getAssignment(c2));
        		if (a1==null) {
        			cmp = (a2==null?0:-1);
        		} else {
        			if (a2==null)
        				cmp = 1;
        			else
        				cmp = Double.compare(a1.getPlacement().getRoomSize(),a2.getPlacement().getRoomSize());
        		}
        		break;
        	}
    	} catch (Exception e) {}
    	if (cmp!=0) return cmp;
    	if (!co1.equals(co2))
    		cmp = compareCourses(co1, co2);
    	else if (!c1.getSchedulingSubpart().equals(c2.getSchedulingSubpart()))
			cmp = compareSubparts(c1.getSchedulingSubpart(), c2.getSchedulingSubpart());
		else
			cmp = c1.getSectionNumber().compareTo(c2.getSectionNumber());
    	if (cmp!=0) return cmp;
    	cmp = c1.getSectionNumber().compareTo(c2.getSectionNumber());
    	if (cmp!=0) return cmp;
    	return c1.getUniqueId().compareTo(c2.getUniqueId());
	}

    public int compareByParentChildSameIType(CourseOffering co, Class_ c1, Class_ c2) {
    	SchedulingSubpart s1 = c1.getSchedulingSubpart();
    	SchedulingSubpart s2 = c2.getSchedulingSubpart();
    	
    	if (s1.equals(s2)) {
			while (s1.getParentSubpart()!=null && s1.getParentSubpart().getItype().equals(s1.getItype()) && !c1.getParentClass().equals(c2.getParentClass())) {
				s1 = s1.getParentSubpart(); c1 = c1.getParentClass();
				s2 = s2.getParentSubpart(); c2 = c2.getParentClass();
			}
    		return compareClasses(co, co, c1,c2);
    	}
    	
    	if (s1.getItype().equals(s2.getItype())) {
    		if (isParentSameIType(s1,s2)) {
    			while (!s1.equals(s2)) {
    				s1 = s1.getParentSubpart(); c1 = c1.getParentClass();
    			}
    			while (s1.getParentSubpart()!=null && s1.getParentSubpart().getItype().equals(s1.getItype())) {
    				s1 = s1.getParentSubpart(); c1 = c1.getParentClass();
    				s2 = s2.getParentSubpart(); c2 = c2.getParentClass();
    			}
    			int cmp = compareClasses(co, co, c1,c2);
    			if (cmp!=0) return cmp;
    			return 1;
    		} else if (isParentSameIType(s2,s1)) {
    			while (!s2.equals(s1)) {
    				s2 = s2.getParentSubpart(); c2 = c2.getParentClass();
    			}
    			while (s1.getParentSubpart()!=null && s1.getParentSubpart().getItype().equals(s1.getItype())) {
    				s1 = s1.getParentSubpart(); c1 = c1.getParentClass();
    				s2 = s2.getParentSubpart(); c2 = c2.getParentClass();
    			}
    			int cmp = compareClasses(co, co, c1,c2);
    			if (cmp!=0) return cmp;
    			return -1;
    		}
    	}
    	
    	return compareSubparts(s1, s2);
    }
	
    public static int compareComparable(Comparable c1, Comparable c2) {
    	return (c1==null?(c2==null?0:-1):(c2==null?1:c1.compareTo(c2)));
    }
    
    public static int compareInstructors(Class_ c1, Class_ c2) {
    	TreeSet<DepartmentalInstructor> s1 = new TreeSet<DepartmentalInstructor>();
    	TreeSet<DepartmentalInstructor> s2 = new TreeSet<DepartmentalInstructor>();
    	for (ClassInstructor i: c1.getClassInstructors())
    		s1.add(i.getInstructor());
    	for (ClassInstructor i: c2.getClassInstructors())
    		s2.add(i.getInstructor());
    	Iterator<DepartmentalInstructor> i1 = s1.iterator();
    	Iterator<DepartmentalInstructor> i2 = s2.iterator();
    	while (i1.hasNext() || i2.hasNext()) {
    		if (!i1.hasNext()) return -1;
    		if (!i2.hasNext()) return 1;
    		int cmp = i1.next().compareTo(i2.next());
    		if (cmp != 0) return cmp;
    	}
    	return 0;
    }
    
    public int compareCourses(CourseOffering co1, CourseOffering co2) {
		int cmp = co1.getCourseName().compareTo(co2.getCourseName());
		if (cmp != 0) return cmp;
		return co1.getUniqueId().compareTo(co2.getUniqueId());
    }
	
	public int compare(Object o1, Object o2) {
		Class_ c1, c2; CourseOffering co1, co2;
		if (o1 instanceof Class_) {
			c1 = (Class_)o1;
			c2 = (Class_)o2;
			co1 = c1.getSchedulingSubpart().getControllingCourseOffering();
			co2 = c2.getSchedulingSubpart().getControllingCourseOffering();
		} else {
			c1 = (Class_)((Object[])o1)[0];
			c2 = (Class_)((Object[])o2)[0];
			co1 = (CourseOffering)((Object[])o1)[1];
			co2 = (CourseOffering)((Object[])o2)[1];
		}
		if (!co1.getSubjectArea().equals(co2.getSubjectArea())) {
			int cmp = co1.getSubjectAreaAbbv().compareToIgnoreCase(co2.getSubjectAreaAbbv());
			if (cmp != 0) return cmp;
			return co1.getSubjectArea().getUniqueId().compareTo(co2.getSubjectArea().getUniqueId());
		}
		if (iKeepSubpart) {
			if (!co1.equals(co2))
				return compareCourses(co1, co2);
			return compareByParentChildSameIType(co1, c1, c2);
		}
		return compareClasses(co1, co2, c1, c2);
	}
	
}
