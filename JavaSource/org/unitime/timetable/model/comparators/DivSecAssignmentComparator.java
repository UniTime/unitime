/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;


/**
 * 
 * @author Tomas Muller
 *
 */
public class DivSecAssignmentComparator implements Comparator {
	private boolean iConsiderParentChildRelation = false;
	private boolean iMixConfigs = false;
	private ClassAssignmentProxy iProxy = null;
	
	public DivSecAssignmentComparator(ClassAssignmentProxy proxy, boolean considerParentChildRelation, boolean mixConfigs) {
		iProxy = proxy;
		iConsiderParentChildRelation = considerParentChildRelation;
		iMixConfigs = mixConfigs;
	}
	
	public int compare(Object o1, Object o2) {
        Class_ c1 = (o1 instanceof Assignment?((Assignment)o1).getClazz():(Class_)o1);
        Class_ c2 = (o2 instanceof Assignment?((Assignment)o2).getClazz():(Class_)o2);
        
        int cmp = compareClasses(c1, c2);
        if (cmp!=0) return cmp;
        
        Assignment a1 = (o1 instanceof Assignment?(Assignment)o1:null);
        Assignment a2 = (o2 instanceof Assignment?(Assignment)o2:null);
        
        if (a1==null) return (a2==null?c1.getSectionNumber().compareTo(c2.getSectionNumber()):1);
        if (a2==null) return -1;

        return compareAssignments(a1,a2);
	}
	
	public int compareAssignments(Assignment a1, Assignment a2) {
		int cmp = comparePlacements(a1.getClazz(), a2.getClazz(), a1.getPlacement(), a2.getPlacement());
		if (cmp!=0) return cmp;

		cmp = a1.getClazz().getSectionNumber().compareTo(a2.getClazz().getSectionNumber());
		if (cmp!=0) return cmp;
		
		return a1.getUniqueId().compareTo(a2.getUniqueId());
	}
	
	public int comparePlacements(Class_ c1, Class_ c2, Placement p1, Placement p2) {
		int cmp = compareTimeLocations(c1, c2, p1.getTimeLocation(), p2.getTimeLocation());
		if (cmp!=0) return cmp;
		
		if (iMixConfigs) {
			cmp = compareInstrOfferingConfigs(c1.getSchedulingSubpart().getInstrOfferingConfig(), c2.getSchedulingSubpart().getInstrOfferingConfig());
			if (cmp!=0) return cmp;
			return cmp;
		}
		
		/*
		if (p1.isMultiRoom()) {
			if (p2.isMultiRoom()) {
				cmp = compareRoomLocations(p1.getRoomLocations(), p2.getRoomLocations());
			} else {
				Vector rv2 = new Vector(1); rv2.addElement(p2.getRoomLocation());
				cmp = compareRoomLocations(p1.getRoomLocations(), rv2);
			}
		} else {
			if (p2.isMultiRoom()) {
				Vector rv1 = new Vector(1); rv1.addElement(p1.getRoomLocation());
				cmp = compareRoomLocations(rv1, p2.getRoomLocations());
			} else {
				cmp = compareRoomLocations(p1.getRoomLocation(), p2.getRoomLocation());
			}
		}
		if (cmp!=0) return cmp;
		*/
		
		return 0;
	}
	
	public int compareRoomLocations(Vector rv1, Vector rv2) {
		if (rv1.isEmpty()) return (rv2.isEmpty()?0:-1);
		if (rv2.isEmpty()) return 1;
		
		if (rv1.size()>1) Collections.sort(rv1);
		if (rv2.size()>1) Collections.sort(rv2);
		
		int min = Math.min(rv1.size(),rv2.size());
		for (int i=0;i<min;i++) {
			int cmp = compareRoomLocations((RoomLocation)rv1.elementAt(i),(RoomLocation)rv2.elementAt(i));
			if (cmp!=0) return cmp;
		}
		
		return Double.compare(rv1.size(),rv2.size());
		
	}
	
	public int compareRoomLocations(RoomLocation r1, RoomLocation r2) {
		int cmp = r1.getName().compareTo(r2.getName());
		if (cmp!=0) return cmp;
		return r1.getId().compareTo(r2.getId());
	}
	
	public int compareTimeLocations(Class_ c1, Class_ c2, TimeLocation t1, TimeLocation t2) {
		int cmp = t1.getStartSlots().nextElement().compareTo(t2.getStartSlots().nextElement());
		if (cmp!=0) return cmp;
		cmp = Double.compare(t1.getDayCode(), t2.getDayCode());
		if (cmp!=0) return cmp;
		cmp = Double.compare(t1.getLength(), t2.getLength());
		if (cmp!=0) return cmp;
		cmp = (new TimePatternDAO()).get(t1.getTimePatternId()).compareTo((new TimePatternDAO()).get(t2.getTimePatternId()));
		if (cmp!=0) return cmp;
		cmp = (new DatePatternDAO()).get(t1.getDatePatternId()).compareTo((new DatePatternDAO()).get(t2.getDatePatternId()));
		if (cmp!=0) return cmp;
		cmp = t1.getLongName().compareTo(t2.getLongName());
		if (cmp!=0) return cmp;
		
		if (c1.getSchedulingSubpart().equals(c2.getSchedulingSubpart()) && c1.getChildClasses().size()==1 && c2.getChildClasses().size()==1) { 
			Class_ cc1 = (Class_)c1.getChildClasses().iterator().next();
			Class_ cc2 = (Class_)c2.getChildClasses().iterator().next();
			if (iConsiderParentChildRelation || cc1.getSchedulingSubpart().getItype().equals(c1.getSchedulingSubpart().getItype())) {
				Assignment a1 = null;
				try { a1 = iProxy.getAssignment(cc1); } catch (Exception e) {}
				Assignment a2 = null;
				try { a2 = iProxy.getAssignment(cc2); } catch (Exception e) {}
				if (a1==null || a2==null) {
				if (cc1.getSchedulingSubpart().getItype().equals(c1.getSchedulingSubpart().getItype()))
					return c1.getUniqueId().compareTo(c2.getUniqueId());
				} else {
					return compareTimeLocations(cc1, cc2, a1.getTimeLocation(), a2.getTimeLocation());
				}
			}
		}

		return 0;
	}

	public int compareClasses(Class_ c1, Class_ c2) {
		if (iConsiderParentChildRelation && c1.getSchedulingSubpart().equals(c2.getSchedulingSubpart())) {
			// if (c1.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getNrClasses(c1.getSchedulingSubpart().getItype())<100) {
				int cmp = compareParentClasses(c1.getParentClass(), c2.getParentClass());
				if (cmp!=0) return cmp;
			//}
		}

		return compareSchedulingSubparts(c1.getSchedulingSubpart(), c2.getSchedulingSubpart());
	}
	
	public int compareParentClasses(Class_ c1, Class_ c2) {
		if (c1==null || c2==null) return 0;
		
		int cmp = compareParentClasses(c1.getParentClass(), c2.getParentClass());
		if (cmp!=0) return cmp;
		
		if (c1.getDivSecNumber()!=null && c2.getDivSecNumber()!=null) {
			cmp = c1.getDivSecNumber().compareTo(c2.getDivSecNumber());
			if (cmp!=0) return cmp;
		}
		
		Assignment a1 = null;
		try { a1 = iProxy.getAssignment(c1); } catch (Exception e) {}
		Assignment a2 = null;
		try { a2 = iProxy.getAssignment(c2); } catch (Exception e) {}
		
		if (a1!=null && a2!=null) {
			cmp = comparePlacements(c1, c2, a1.getPlacement(), a2.getPlacement());
			if (cmp!=0) return cmp;
		}
		
		return c1.getUniqueId().compareTo(c2.getUniqueId());
	}
	
    public boolean isParent(SchedulingSubpart s1, SchedulingSubpart s2) {
		SchedulingSubpart p2 = s2.getParentSubpart();
		if (p2==null) return false;
		if (p2.equals(s1)) return true;
		return isParent(s1, p2);
	}

    public int compareSchedulingSubparts(SchedulingSubpart s1, SchedulingSubpart s2) {
		if (s1.equals(s2)) return 0;
		
		if (iMixConfigs) {
			int cmp = compareInstructionalOfferings(s1.getInstrOfferingConfig().getInstructionalOffering(), s2.getInstrOfferingConfig().getInstructionalOffering());
			if (cmp!=0) return cmp;

			if (isParent(s1,s2)) return -1;
			if (isParent(s2,s1)) return 1;

			cmp = s1.getItype().getItype().compareTo(s2.getItype().getItype());
			if (cmp!=0) return cmp;

			return 0;
		}
		
		int cmp = compareInstrOfferingConfigs(s1.getInstrOfferingConfig(), s2.getInstrOfferingConfig());
		if (cmp!=0) return cmp;
		
		if (isParent(s1,s2)) return -1;
		if (isParent(s2,s1)) return 1;
        
		cmp = s1.getItype().getItype().compareTo(s2.getItype().getItype());
		if (cmp!=0) return cmp;
        
		return s1.getUniqueId().compareTo(s2.getUniqueId());
	}
	
	public int compareInstrOfferingConfigs(InstrOfferingConfig c1, InstrOfferingConfig c2) {
		if (c1.equals(c2)) return 0;
		int cmp = compareInstructionalOfferings(c1.getInstructionalOffering(), c2.getInstructionalOffering());
		if (cmp!=0) return cmp;
		return c1.getUniqueId().compareTo(c2.getUniqueId());
	}
	
	public int compareInstructionalOfferings(InstructionalOffering o1, InstructionalOffering o2) {
		int cmp = o1.getCourseName().compareTo(o2.getCourseName());
		if (cmp!=0) return cmp;
		return o1.getUniqueId().compareTo(o2.getUniqueId());
	}
	
	

}
