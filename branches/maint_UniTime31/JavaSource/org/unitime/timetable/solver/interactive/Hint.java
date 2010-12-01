/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.interactive;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.dom4j.Element;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;

import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.model.Model;
import net.sf.cpsolver.ifs.solver.Solver;

/**
 * @author Tomas Muller
 */
public class Hint implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long iClassId;
	private int iDays;
	private int iStartSlot;
	private Vector iRoomIds;
	private Long iPatternId;
	private AssignmentPreferenceInfo iInfo = null;
	private ClassAssignmentDetails iDetails = null;
	public Hint(Long classId, int days, int startSlot, Vector roomIds, Long patternId) {
		iClassId = classId; iDays = days; iStartSlot = startSlot; iRoomIds = new Vector(1); iRoomIds = new Vector(roomIds); iPatternId = patternId;
	}
	public Hint(Solver solver, Placement placement) {
		this(solver, placement, false);
	}
	public Hint(Solver solver, Placement placement, boolean populateInfo) {
		iClassId = ((Lecture)placement.variable()).getClassId();
		iDays = placement.getTimeLocation().getDayCode();
		iStartSlot = placement.getTimeLocation().getStartSlot();
		iRoomIds = placement.getRoomIds();
		iPatternId = placement.getTimeLocation().getTimePatternId();
		if (populateInfo && solver!=null)
			iInfo = new AssignmentPreferenceInfo(solver, placement, false);
		if (((Lecture)placement.variable()).isCommitted() && solver!=null)
			iDetails = new ClassAssignmentDetails(solver,(Lecture)placement.variable(),placement,false);
	}
	public Placement getPlacement(Model model) {
		return getPlacement(model, true);
	}
	public Placement getPlacement(Model model, boolean checkValidity) {
		for (Enumeration e1=model.variables().elements();e1.hasMoreElements();) {
			Lecture lecture = (Lecture)e1.nextElement();
			if (!lecture.getClassId().equals(iClassId)) continue;
    		TimeLocation timeLocation = null;
        	for (Enumeration e2=lecture.timeLocations().elements();e2.hasMoreElements();) {
        		TimeLocation t = (TimeLocation)e2.nextElement();
        		if (t.getDayCode()!=iDays) continue;
        		if (t.getStartSlot()!=iStartSlot) continue;
        		if (!t.getTimePatternId().equals(iPatternId)) continue;
        		timeLocation = t; break;
        	}
        	Vector roomLocations = new Vector();
        	if (lecture.getNrRooms()>0) {
            	for (Enumeration e2=iRoomIds.elements();e2.hasMoreElements();) {
    				Long roomId = (Long)e2.nextElement();
                	for (Enumeration e3=lecture.roomLocations().elements();e3.hasMoreElements();) {
                		RoomLocation r = (RoomLocation)e3.nextElement();
                		if (r.getId().equals(roomId))
                			roomLocations.add(r);
                	}
        		}
        	}
    		if (timeLocation!=null && roomLocations.size()==lecture.getNrRooms()) {
    			Placement placement = new Placement(lecture,timeLocation,roomLocations);
    			if (checkValidity && !placement.isValid()) return null;
    			return placement;
    		}
    		/*
			Vector values = lecture.values();
	    	if (!lecture.allowBreakHard()) values = lecture.computeValues(true);
			for (Enumeration e2=values.elements();e2.hasMoreElements();) {
				Placement placement = (Placement)e2.nextElement();
				if (placement.getTimeLocation().getDayCode()!=iDays) continue;
				if (placement.getTimeLocation().getStartSlot()!=iStartSlot) continue;
				boolean sameRooms = true;
				for (Enumeration e3=iRoomIds.elements();sameRooms && e3.hasMoreElements();) {
					Long roomId = (Integer)e3.nextElement();
					if (!placement.hasRoomLocation(roomId)) sameRooms = false;
				}
				if (!sameRooms) continue;
				return placement;
			}
			*/
		}
		return null;
	}
	public AssignmentPreferenceInfo getInfo(Solver solver) {
		Placement p = getPlacement(solver.currentSolution().getModel());
		if (p==null) return null;
		return new AssignmentPreferenceInfo(solver, p, false);
	}
	
	public String getNotValidReason(Solver solver) {
		Placement p = getPlacement(solver.currentSolution().getModel(), false);
		if (p==null) return "Selected placement is not valid (room or instructor not avaiable).";
		if (p.isValid()) return "Selected placement is valid.";
		String reason = p.getNotValidReason();
		return (reason==null?"Selected placement is not valid (room or instructor not avaiable).":"Selected placement is not valid ("+reason+")");
	}
	public Long getClassId() { return iClassId; }
	public int getDays() { return iDays; }
	public int getStartSlot() { return iStartSlot; }
	public Vector getRoomIds() { return iRoomIds; }
	public Long getPatternId() { return iPatternId; }
	
	public boolean equals(Object o) {
		if (o==null || !(o instanceof Hint)) return false;
		return iClassId.equals(((Hint)o).getClassId());
	}
	public int hashCode() { return iClassId.hashCode(); }
	
	public ClassAssignmentDetails getDetails(HttpSession session, boolean includeConstraints) throws Exception {
		if (iDetails!=null) return iDetails;
		if (iInfo==null) iInfo = WebSolver.getSolver(session).getInfo(this);
		iDetails = ClassAssignmentDetails.createClassAssignmentDetails(session, iClassId, includeConstraints);
		if (iDetails!=null) iDetails.setAssigned(iInfo, iRoomIds,iDays,iStartSlot);
		return iDetails;
	}
	public ClassAssignmentDetails getDetailsUnassign(HttpSession session, boolean includeConstraints) throws Exception {
		if (iDetails!=null) return iDetails;
		iDetails = ClassAssignmentDetails.createClassAssignmentDetails(session, iClassId, includeConstraints);
		return iDetails;
	}
	public void setDetails(ClassAssignmentDetails details) {
		iDetails = details;
	}

    public static class HintComparator implements Comparator {
    	private Vector iOrder; 
        public HintComparator(Vector order) { iOrder = order; }
        public int compare(Object o1, Object o2) {
            Hint p1 = (Hint)o1;
            Hint p2 = (Hint)o2;
            int i1 = iOrder.indexOf(p1.getClassId());
            int i2 = iOrder.indexOf(p2.getClassId());
            return (new Integer(i1)).compareTo(new Integer(i2));
        }
    }
    
    public void toXml(Element element) {
    	element.addAttribute("id", String.valueOf(iClassId));
    	element.addAttribute("days", String.valueOf(iDays));
    	element.addAttribute("start", String.valueOf(iStartSlot));
    	if (iRoomIds!=null) {
    		for (Enumeration e=iRoomIds.elements();e.hasMoreElements();) {
                Long roomId = (Long)e.nextElement();
    			if (roomId!=null)
    				element.addElement("room").addAttribute("id", roomId.toString());
    		}
    	}
    	if (iPatternId!=null)
    		element.addAttribute("pattern", iPatternId.toString());
    }
    
    public static Hint fromXml(Element element) {
    	Vector roomIds = new Vector();
    	for (Iterator i=element.elementIterator("room");i.hasNext();)
    		roomIds.addElement(Long.valueOf(((Element)i.next()).attributeValue("id")));
    	return new Hint(
    			Long.valueOf(element.attributeValue("id")),
    			Integer.parseInt(element.attributeValue("days")),
    			Integer.parseInt(element.attributeValue("start")),
    			roomIds,
    			(element.attributeValue("pattern")==null?null:Long.valueOf(element.attributeValue("pattern")))
    			);
    }
    
    public String toString() {
        return "Hint{classId = "+iClassId+", days = "+iDays+", startSlot = "+iStartSlot+", roomIds = "+iRoomIds+"}";
    }
}
