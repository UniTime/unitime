/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.ui;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspWriter;

import net.sf.cpsolver.coursett.constraint.ClassLimitConstraint;
import net.sf.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import net.sf.cpsolver.coursett.constraint.GroupConstraint;
import net.sf.cpsolver.coursett.constraint.InstructorConstraint;
import net.sf.cpsolver.coursett.constraint.JenrlConstraint;
import net.sf.cpsolver.coursett.constraint.MinimizeNumberOfUsedGroupsOfTime;
import net.sf.cpsolver.coursett.constraint.MinimizeNumberOfUsedRoomsConstraint;
import net.sf.cpsolver.coursett.constraint.RoomConstraint;
import net.sf.cpsolver.coursett.constraint.SpreadConstraint;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.ifs.extension.Assignment;
import net.sf.cpsolver.ifs.extension.ConflictStatistics;
import net.sf.cpsolver.ifs.model.Constraint;

import org.dom4j.Element;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.timegrid.SolverGridModel;

/**
 * @author Tomas Muller
 */
public class ConflictStatisticsInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 7L;
	public static int sVersion = 8; // to be able to do some changes in the future
	public static final int sConstraintTypeRoom = 1;
	public static final int sConstraintTypeInstructor = 2;
	public static final int sConstraintTypeGroup = 3;
	public static final int sConstraintTypeBalanc = 4;
	public static final int sConstraintTypeSpread = 5;
	public static final int sConstraintTypeMinNrRoomUsed = 6;
	public static final int sConstraintTypeClassLimit = 7;
	public static final int sConstraintTypeMinNrGroupsOfTime = 8;
	public static final int sConstraintTypeJoinEnrollment = 9;
	private Hashtable iVariables = new Hashtable();
	
	public Collection getCBS() { return iVariables.values(); } 
	public CBSVariable getCBS(Long classId) { return (CBSVariable)iVariables.get(classId); }
	
	public void load(ConflictStatistics cbs) {
		load(cbs, null);
	}
	
	public ConflictStatisticsInfo getConflictStatisticsSubInfo(List variables) {
		ConflictStatisticsInfo ret = new ConflictStatisticsInfo();
		for (Iterator e=variables.iterator();e.hasNext();) {
			Lecture lecture = (Lecture)e.next();
			CBSVariable var = (CBSVariable)iVariables.get(lecture.getClassId());
			if (var!=null)
				ret.iVariables.put(lecture.getClassId(),var);
		}
		return ret;
	}
	
	public void merge(ConflictStatisticsInfo info) {
		if (info!=null) iVariables.putAll(info.iVariables);
	}
	
	public void load(ConflictStatistics cbs, Long classId) {
		iVariables.clear();
		for (Iterator i1=cbs.getNoGoods().entrySet().iterator();i1.hasNext();) {
			Map.Entry entry = (Map.Entry)i1.next();
			Assignment assignment = (Assignment)entry.getKey();
			Placement placement = (Placement)assignment.getValue(); 
			Lecture lecture = (Lecture)placement.variable();
			if (classId!=null && !classId.equals(lecture.getClassId())) continue;
			
			CBSVariable var = (CBSVariable)iVariables.get(lecture.getClassId());
			if (var==null) {
				String pref = SolverGridModel.hardConflicts2pref(lecture,null);
				var = new CBSVariable(lecture.getClassId().longValue(),lecture.getName(),pref);
				iVariables.put(lecture.getClassId(),var);
			}
			
			CBSValue val = new CBSValue(var,
					lecture.getInstructorName(),
					placement.getRoomNames(), 
					placement.getTimeLocation().getDayCode(),
					placement.getTimeLocation().getStartSlot(),
					placement.getRoomIds(),
					placement.getTimeLocation().getPreference(),
					placement.getRoomPrefs(),
					placement.getTimeLocation().getLength(),
					placement.getTimeLocation().getDatePatternName(),
					placement.getTimeLocation().getTimePatternId(),
					placement.getTimeLocation().getBreakTime(),
					placement.getTimeLocation().getDatePatternId());
			var.values().add(val);
			
			List noGoods = (List)entry.getValue();
			
			Hashtable constr2assignments = new Hashtable();
			for (Iterator e2=noGoods.iterator();e2.hasNext();) {
				Assignment noGood = (Assignment)e2.next();
				if (noGood.getConstraint()==null) continue;
				List aaa = (List)constr2assignments.get(noGood.getConstraint());
				if (aaa == null) {
					aaa = new ArrayList();
					constr2assignments.put(noGood.getConstraint(), aaa);
				}
				aaa.add(noGood);
			}
			
			for (Iterator i2=constr2assignments.entrySet().iterator();i2.hasNext();) {
				Map.Entry entry2 = (Map.Entry)i2.next();
				Constraint constraint = (Constraint)entry2.getKey();
				List noGoodsThisConstraint = (List)entry2.getValue();
				
				CBSConstraint con = null;
				if (constraint instanceof RoomConstraint) {
					con = new CBSConstraint(val, sConstraintTypeRoom,((RoomConstraint)constraint).getResourceId(), constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof InstructorConstraint) {
					con = new CBSConstraint(val, sConstraintTypeInstructor,((InstructorConstraint)constraint).getResourceId(),constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof GroupConstraint) {
					con = new CBSConstraint(val, sConstraintTypeGroup, ((GroupConstraint)constraint).getId(), constraint.getName(), ((GroupConstraint)constraint).getPrologPreference());
				} else if (constraint instanceof DepartmentSpreadConstraint) {
					con = new CBSConstraint(val, sConstraintTypeBalanc, ((DepartmentSpreadConstraint)constraint).getDepartmentId().longValue(), constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof SpreadConstraint) {
					con = new CBSConstraint(val, sConstraintTypeSpread, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof MinimizeNumberOfUsedRoomsConstraint) {
					con = new CBSConstraint(val, sConstraintTypeMinNrRoomUsed, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof ClassLimitConstraint) {
					con = new CBSConstraint(val, sConstraintTypeClassLimit, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof MinimizeNumberOfUsedGroupsOfTime) {
					con = new CBSConstraint(val, sConstraintTypeMinNrGroupsOfTime, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof JenrlConstraint) {
					con = new CBSConstraint(val, sConstraintTypeJoinEnrollment, constraint.getId(), constraint.toString(), PreferenceLevel.sRequired);
				} else {
					con = new CBSConstraint(val, -1, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
				}
				val.constraints().add(con);
				
				for (Iterator e3=noGoodsThisConstraint.iterator();e3.hasNext();) {
					Assignment ass = (Assignment)e3.next();
					Placement p = (Placement)ass.getValue();
					Lecture l = (Lecture)p.variable();
					String pr = SolverGridModel.hardConflicts2pref(l,p);
					CBSAssignment a = new CBSAssignment(con,
							l.getClassId().longValue(),
							l.getName(),
							l.getInstructorName(),
							p.getRoomNames(),
							p.getTimeLocation().getDayCode(),
							p.getTimeLocation().getStartSlot(),
							p.getRoomIds(),
							pr,
							p.getTimeLocation().getPreference(),
							p.getRoomPrefs(),
							p.getTimeLocation().getLength(),
							p.getTimeLocation().getDatePatternName(),
							p.getTimeLocation().getTimePatternId(),
							p.getTimeLocation().getBreakTime(),
							p.getTimeLocation().getDatePatternId());
					con.assignments().add(a);
					a.incCounter((int)ass.getCounter(0));
				}
			}
			
		}
	}
	

	public void load(Element root) throws Exception {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==sVersion) {
			iVariables.clear();
			for (Iterator i1=root.elementIterator("var");i1.hasNext();) {
				CBSVariable var = new CBSVariable((Element)i1.next());
				iVariables.put(new Long(var.getId()),var);
			}
		}
	}
	
	public void save(Element root) throws Exception {
		root.addAttribute("version", String.valueOf(sVersion));
		for (Iterator i1=iVariables.values().iterator();i1.hasNext();) {
			((CBSVariable)i1.next()).save(root.addElement("var"));
		}
	}
	
	public static interface Counter {
		public int getCounter();
		public void incCounter(int value);
	}
	
	public static class CBSVariable implements Counter, Comparable, Serializable {
		private static final long serialVersionUID = 1L;
		int iCounter = 0;
		long iClassId;
		String iName;
		HashSet iValues = new HashSet();
		CBSConstraint iConstraint = null;
		String iPref = null;
		
		CBSVariable(long classId, String name, String pref) {
			iClassId = classId;
			iName = name;
			iPref = pref;
		}
		CBSVariable(CBSConstraint constraint, long classId, String name, String pref) {
			iConstraint = constraint;
			iClassId = classId;
			iName = name;
			iPref = pref;
		}
		CBSVariable(Element element) {
			iClassId = Long.parseLong(element.attributeValue("class"));
			iName = element.attributeValue("name");
			iPref = element.attributeValue("pref");
			for (Iterator i=element.elementIterator("val");i.hasNext();)
				iValues.add(new CBSValue(this,(Element)i.next())); 
		}
		
		public long getId() { return iClassId; }
		public int getCounter() { return iCounter; }
		public String getName() { return iName; }
		public String getPref() { return iPref; }
		public void incCounter(int value) { 
			iCounter+=value;
			if (iConstraint!=null) iConstraint.incCounter(value);
		}
		public Set values() { return iValues; }
		public int hashCode() {
			return (new Long(iClassId)).hashCode();
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof CBSVariable)) return false;
			return ((CBSVariable)o).getId()==getId();
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof CBSVariable)) return -1;
			int ret = -(new Integer(iCounter)).compareTo(new Integer(((CBSVariable)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public String toString() {
			//Class_ clazz = getClazz();
			//return clazz.getCourseName()+" "+clazz.getItypeDesc()+" "+clazz.getSectionNumber();
			return iName;
		}
		public void save(Element element) {
			element.addAttribute("class",String.valueOf(iClassId));
			element.addAttribute("name", iName);
			if (iPref!=null)
				element.addAttribute("pref", iPref);
			for (Iterator i=iValues.iterator();i.hasNext();)
				((CBSValue)i.next()).save(element.addElement("val"));
		}
	}

	public static class CBSValue implements Counter, Comparable, Serializable {
		private static final long serialVersionUID = 1L;
		int iCounter = 0;
		int iDays;
		int iStartSlot;
		List iRoomIds;
		String iInstructorName = null;
		List iRoomNames;
		List iRoomPrefs;
		int iTimePref;
		CBSVariable iVariable = null;
		HashSet iConstraints = new HashSet();
		HashSet iAssignments = new HashSet();
		int iLength;
		int iBreakTime;
		Long iDatePatternId = null;
		String iDatePatternName = null;
		Long iPatternId = null;
		
		CBSValue(CBSVariable var, String instructorName, List roomNames, int days, int startSlot, List roomIds, int timePref, List roomPrefs, int length, String datePatternName, Long patternId, int breakTime, Long datePatternId) {
			iStartSlot = startSlot; iDays = days; iRoomIds = roomIds;
			iVariable = var; iInstructorName = instructorName; iRoomNames = roomNames; iTimePref = timePref; iRoomPrefs = roomPrefs;
			iDatePatternName = datePatternName; iLength = length; iBreakTime = breakTime;
			iPatternId = patternId; iDatePatternId = datePatternId;
		}
		CBSValue(CBSVariable var, Element element) {
			iVariable = var;
			iDays = Integer.parseInt(element.attributeValue("days"));
			iStartSlot = Integer.parseInt(element.attributeValue("slot"));
			iRoomIds = new ArrayList();
			iRoomNames = new ArrayList();
			iRoomPrefs = new ArrayList();
			for (Iterator i=element.elementIterator("room");i.hasNext();) {
				Element r = (Element)i.next();
				iRoomIds.add(Integer.valueOf(r.attributeValue("id")));
				iRoomNames.add(r.attributeValue("name"));
				iRoomPrefs.add(Integer.valueOf(r.attributeValue("pref")));
			}
			iInstructorName = element.attributeValue("iName");
			iTimePref = Integer.parseInt(element.attributeValue("tpref"));
			iDatePatternName = element.attributeValue("datePattern");
			iLength = Integer.parseInt(element.attributeValue("length"));
			iBreakTime = (element.attributeValue("breakTime")==null?0:Integer.parseInt(element.attributeValue("breakTime")));
			iPatternId = Long.valueOf(element.attributeValue("pattern"));
			iDatePatternId = Long.valueOf(element.attributeValue("datePatternId"));
			for (Iterator i=element.elementIterator("cons");i.hasNext();)
				iConstraints.add(new CBSConstraint(this,(Element)i.next())); 
		}
		public CBSVariable variable() { return iVariable; }
		public int getDayCode() { return iDays; }
		public String getDays() {
			StringBuffer ret = new StringBuffer();
			for (int i=0;i<Constants.DAY_NAMES_SHORT.length;i++)
				if ((Constants.DAY_CODES[i] & iDays)!=0) ret.append(Constants.DAY_NAMES_SHORT[i]);
			return ret.toString(); 
		}
		public int getStartSlot() {
			return iStartSlot;
		}
		public String getStartTime() {
		    return Constants.toTime(iStartSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
		}
		public String getEndTime() {
            return Constants.toTime((iStartSlot+iLength) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - iBreakTime);
		}
		public int getLength() {
			return iLength;
		}
		public int getBreakTime() {
			return iBreakTime;
		}
		public String getDatePatternName() {
			return iDatePatternName;
		}
		public Long getDatePatternId() {
			return iDatePatternId;
		}
		public List getRoomNames() { return iRoomNames; }
		public String getInstructorName() { return iInstructorName; }
		public int getTimePref() { return iTimePref; }
		public List getRoomPrefs() { return iRoomPrefs; }
		public String toString() {
			//return getDays()+" "+getStartTime()+" "+getRoom().getRoomLabel();
			return getDays()+" "+getStartTime()+" "+iRoomNames+(iInstructorName==null?"":" "+iInstructorName);
		}
		public int getCounter() { return iCounter; }
		public void incCounter(int value) { 
			iCounter+=value;
			if (iVariable!=null) iVariable.incCounter(value);
		}
		public List getRoomIds() {
			return iRoomIds;
		}
		public Set constraints() { return iConstraints; }
		public Set assignments() { return iAssignments; }
		public int hashCode() {
			return combine(combine(iRoomIds==null?0:iRoomIds.hashCode(),combine(iStartSlot,iDays)),combine(iPatternId==null?0:iPatternId.intValue(),iDatePatternId==null?0:iDatePatternId.intValue()));
		}
		public Long getPatternId() {
			return iPatternId;
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof CBSValue)) return false;
			CBSValue v = (CBSValue)o;
			return v.getRoomIds().equals(getRoomIds()) && v.getDayCode()==getDayCode() && v.getStartSlot()==getStartSlot() && v.getPatternId().equals(getPatternId()) && v.getDatePatternId().equals(getDatePatternId());
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof CBSValue)) return -1;
			int ret = -(new Integer(iCounter)).compareTo(new Integer(((CBSValue)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public void save(Element element) {
			element.addAttribute("days",String.valueOf(iDays));
			element.addAttribute("slot",String.valueOf(iStartSlot));
			element.addAttribute("datePattern", getDatePatternName());
			element.addAttribute("length", String.valueOf(iLength));
			element.addAttribute("breakTime", String.valueOf(iBreakTime));
            if (iPatternId!=null)
                element.addAttribute("pattern", iPatternId.toString());
			for (int i=0;i<iRoomIds.size();i++) {
				Element r = element.addElement("room");
				r.addAttribute("id",iRoomIds.get(i).toString());
				r.addAttribute("name",iRoomNames.get(i).toString());
				r.addAttribute("pref",iRoomPrefs.get(i).toString());
			}
			if (iInstructorName!=null)
				element.addAttribute("iName",iInstructorName);
			element.addAttribute("tpref",String.valueOf(iTimePref));
			for (Iterator i=iConstraints.iterator();i.hasNext();)
				((CBSConstraint)i.next()).save(element.addElement("cons"));
			element.addAttribute("datePatternId",String.valueOf(iDatePatternId));
		}
	}
	
	public static class CBSConstraint implements Counter, Comparable, Serializable {
		private static final long serialVersionUID = 1L;
		CBSValue iValue;
		int iCounter = 0;
		long iId;
		String iName = null;
		int iType;
		HashSet iAssignments = new HashSet();
		HashSet iVariables = new HashSet();
		String iPref;

		CBSConstraint(int type, long id, String name, String pref) {
			iId = id;
			iType = type;
			iName = name;
			iPref = pref;
		}
		CBSConstraint(CBSValue value, int type, long id, String name, String pref) {
			iId = id;
			iType = type;
			iValue = value;
			iName = name;
			iPref = pref;
		}
		CBSConstraint(CBSValue value, Element element) {
			iValue = value;
			iId = Integer.parseInt(element.attributeValue("id"));
			iType = Integer.parseInt(element.attributeValue("type"));
			iName = element.attributeValue("name");
			iPref = element.attributeValue("pref");
			for (Iterator i=element.elementIterator("nogood");i.hasNext();)
				iAssignments.add(new CBSAssignment(this,(Element)i.next())); 
		}
		
		public long getId() { return iId; }
		public int getType() { return iType; }
		public String getName() { return iName; }
		public CBSValue value() { return iValue; }
		public Set variables() { return iVariables; }
		public Set assignments() { return iAssignments; }
		public String getPref() { return iPref; }
		public int getCounter() { return iCounter; }
		public void incCounter(int value) { 
			iCounter+=value;
			if (iValue!=null) iValue.incCounter(value);
		}
		public int hashCode() {
			return combine((int)iId,iType);
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof CBSConstraint)) return false;
			CBSConstraint c = (CBSConstraint)o;
			return c.getId()==getId() && c.getType()==getType();
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof CBSConstraint)) return -1;
			int ret = -(new Integer(iCounter)).compareTo(new Integer(((CBSConstraint)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public void save(Element element) {
			element.addAttribute("id",String.valueOf(iId));
			element.addAttribute("type",String.valueOf(iType));
			if (iName!=null)
				element.addAttribute("name", iName);
			if (iPref!=null)
				element.addAttribute("pref", iPref);
			for (Iterator i=iAssignments.iterator();i.hasNext();)
				((CBSAssignment)i.next()).save(element.addElement("nogood"));
		}
	}

	public static class CBSAssignment implements Counter, Comparable, Serializable {
		private static final long serialVersionUID = 1L;
		CBSConstraint iConstraint;
		long iClassId;
		int iDays;
		int iStartSlot;
		List iRoomIds;
		int iCounter = 0;
		String iVarName, iInstructorName;
		String iPref;
		int iTimePref;
		List iRoomPrefs;
		List iRoomNames;
		int iLength;
		int iBreakTime;
		String iDatePatternName = null;
		Long iPatternId, iDatePatternId;
		
		CBSAssignment(CBSConstraint constraint, long classId, String varName, String instructorName, List roomNames, int days, int startSlot, List roomIds, String pref, int timePref, List roomPrefs, int length, String datePatternName, Long patternId, int breakTime, Long datePatternId) {
			iClassId = classId; iStartSlot = startSlot; iDays = days; iRoomIds = roomIds;
			iConstraint = constraint;
			iVarName = varName; iInstructorName = instructorName; iRoomNames = roomNames;
			iPref = pref; iTimePref = timePref; iRoomPrefs = roomPrefs;
			iDatePatternName = datePatternName; iLength = length;
			iPatternId = patternId; iBreakTime = breakTime;
			iDatePatternId = datePatternId;
		}
		CBSAssignment(CBSConstraint constraint, Element element) {
			iConstraint = constraint;
			iClassId = Long.parseLong(element.attributeValue("class"));
			iStartSlot = Integer.parseInt(element.attributeValue("slot"));
			iDays = Integer.parseInt(element.attributeValue("days"));
			iRoomIds = new ArrayList();
			iRoomNames = new ArrayList();
			iRoomPrefs = new ArrayList();
			for (Iterator i=element.elementIterator("room");i.hasNext();) {
				Element r = (Element)i.next();
				iRoomIds.add(Integer.valueOf(r.attributeValue("id")));
				iRoomNames.add(r.attributeValue("name"));
				iRoomPrefs.add(Integer.valueOf(r.attributeValue("pref")));
			}
			iVarName = element.attributeValue("varName");
			iInstructorName = element.attributeValue("iName");
			iPref = element.attributeValue("pref");
			iTimePref = Integer.parseInt(element.attributeValue("tpref"));
			iDatePatternName = element.attributeValue("datePattern");
			iLength = Integer.parseInt(element.attributeValue("length"));
			iBreakTime = (element.attributeValue("breakTime")==null?0:Integer.parseInt(element.attributeValue("breakTime")));
            iPatternId = (element.attributeValue("pattern")==null?null:Long.valueOf(element.attributeValue("pattern")));
            iDatePatternId = (element.attributeValue("datePatternId")==null?null:Long.valueOf(element.attributeValue("datePatternId")));
			incCounter(Integer.parseInt(element.attributeValue("cnt")));
		}
		public long getId() { return iClassId; }
		public CBSConstraint getConstraint() { return iConstraint; }
		public int getDayCode() { return iDays; }
		public String getDays() {
			StringBuffer ret = new StringBuffer();
			for (int i=0;i<Constants.DAY_NAMES_SHORT.length;i++)
				if ((Constants.DAY_CODES[i] & iDays)!=0) ret.append(Constants.DAY_NAMES_SHORT[i]);
			return ret.toString(); 
		}
		public int getStartSlot() {
			return iStartSlot;
		}
		public String getStartTime() {
		    return Constants.toTime(iStartSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
		}
		public String getEndTime() {
		    return Constants.toTime((iStartSlot+iLength) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - iBreakTime);
		}
		public Long getPatternId() {
			return iPatternId;
		}
		public String toString() {
			//Class_ clazz = getClazz();
			//return clazz.getCourseName()+" "+clazz.getItypeDesc()+" "+clazz.getSectionNumber()+" "+getDays()+" "+getStartTime()+" "+getRoom().getRoomLabel();
			return iVarName+" "+getDays()+" "+getStartTime()+" "+iRoomNames+(iInstructorName==null?"":" "+iInstructorName);
		}
		public String getVariableName() { return iVarName; }
		public List getRoomNames() { return iRoomNames; }
		public String getInstructorName() { return iInstructorName; }
		public List getRoomIds() {
			return iRoomIds;
		}
		public String getPref() { return iPref; }
		public int getTimePref() { return iTimePref; }
		public List getRoomPrefs() { return iRoomPrefs; }
		public int getLength() {
			return iLength;
		}
		public int getBreakTime() {
			return iBreakTime;
		}
		public String getDatePatternName() {
			return iDatePatternName;
		}
		public Long getDatePatternId() {
			return iDatePatternId;
		}
		public int hashCode() {
			return combine(combine((int)iClassId,combine(iRoomIds.hashCode(),combine(iStartSlot,iDays))),combine(iPatternId==null?0:iPatternId.intValue(),iDatePatternId==null?0:iDatePatternId.intValue()));
		}
		public int getCounter() { return iCounter; }
		public void incCounter(int value) { 
			iCounter+=value;
			if (iConstraint!=null) iConstraint.incCounter(value);
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof CBSAssignment)) return false;
			CBSAssignment a = (CBSAssignment)o;
			return a.getId()==getId() && a.getRoomIds().equals(getRoomIds()) && a.getDayCode()==getDayCode() && a.getStartSlot()==getStartSlot() && a.getPatternId().equals(getPatternId()) && a.getDatePatternId().equals(getDatePatternId());
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof CBSAssignment)) return -1;
			int ret = -(new Integer(iCounter)).compareTo(new Integer(((CBSAssignment)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public void save(Element element) {
			element.addAttribute("class",String.valueOf(iClassId));
			for (int i=0;i<iRoomIds.size();i++) {
				Element r = element.addElement("room");
				r.addAttribute("id",iRoomIds.get(i).toString());
				r.addAttribute("name",iRoomNames.get(i).toString());
				r.addAttribute("pref",iRoomPrefs.get(i).toString());
			}
			element.addAttribute("datePattern", iDatePatternName);
			element.addAttribute("length", String.valueOf(iLength));
			element.addAttribute("breakTime", String.valueOf(iBreakTime));
			element.addAttribute("days",String.valueOf(iDays));
			element.addAttribute("slot",String.valueOf(iStartSlot));
			element.addAttribute("varName", iVarName);
			element.addAttribute("pattern", iPatternId.toString());
			element.addAttribute("datePatternId", iDatePatternId.toString());
			if (iInstructorName!=null)
				element.addAttribute("iName", iInstructorName);
			element.addAttribute("cnt", String.valueOf(iCounter));
			if (iPref!=null)
				element.addAttribute("pref", iPref);
			element.addAttribute("tpref", String.valueOf(iTimePref));
		}
	}	

    private static int combine(int a, int b) {
        int ret = 0;
        for (int i=0;i<15;i++) ret = ret | ((a & (1<<i))<<i) | ((b & (1<<i))<<(i+1));
        return ret;
    }
    
    //--------- toHtml -------------------------------------------------
    private static String IMG_BASE = "images/";
    private static String IMG_EXPAND = IMG_BASE+"expand_node_btn.gif";
    private static String IMG_COLLAPSE = IMG_BASE+"collapse_node_btn.gif";
    private static String IMG_LEAF = IMG_BASE+"end_node_btn.gif";
    
    public static int TYPE_VARIABLE_BASED = 0;
    public static int TYPE_CONSTRAINT_BASED = 1;
    
    private void menu_item(PrintWriter out, String id, String name, String description, String page, boolean isCollapsed) {
        out.println("<div style=\"margin-left:5px;\">");
        out.println("<A style=\"border:0;background:0\" id=\"__idMenu"+id+"\" href=\"javascript:toggle('"+id+"')\" name=\""+name+"\">");
        out.println("<img id=\"__idMenuImg"+id+"\" border=\"0\" src=\""+(isCollapsed ? IMG_EXPAND : IMG_COLLAPSE)+"\" align=\"absmiddle\"></A>");
        out.println("&nbsp;<A class='noFancyLinks' target=\"__idContentFrame\" "+(page == null ? "" : page+" onmouseover=\"this.style.cursor='hand';this.style.cursor='pointer';\" ")+"title=\""+(description == null ? "" : description)+"\" >"+ name+(description == null?"":" <font color='gray'>[" + description + "]</font>")+"</A><br>");
        out.println("</div>");
        out.println("<div ID=\"__idMenuDiv"+id+"\" style=\"display:"+(isCollapsed ? "none" : "block")+";position:relative;margin-left:18px;\">");
    }
    
    private void leaf_item(PrintWriter out, String name, String description, String page) {
        out.println("<div style=\"margin-left:5px;\">");
        out.println("<img border=\"0\" src=\""+IMG_LEAF+"\" align=\"absmiddle\">");
        out.println("&nbsp;<A class='noFancyLinks' target=\"__idContentFrame\" "+(page == null ? "" : page + " onmouseover=\"this.style.cursor='hand';this.style.cursor='pointer';\" ")+"title=\""+(description == null ? "" : description)+"\" >"+name+(description == null ? "" : " <font color='gray'>[" + description + "]</font>")+"</A><br>");
        out.println("</div>");
    }
    
    private void end_item(PrintWriter out) {
        out.println("</div>");
    }
    
    private void unassignedVariableMenuItem(PrintWriter out, String menuId, CBSVariable variable, boolean clickable) {
    	String name = 
    		"<font color='"+PreferenceLevel.prolog2color(variable.getPref())+"'>"+
    		variable.getName()+
    		"</font>";
    	String description = null;
    	String onClick = null;
    	if (clickable)
    		onClick = "onclick=\"(parent ? parent : window).showGwtDialog('Suggestions', 'suggestions.do?id="+variable.getId()+"&op=Reset','900','90%');\"";
    	menu_item(out, menuId, variable.getCounter() + "&times; " + name, description, onClick, true);
    }
    
    private void unassignmentMenuItem(PrintWriter out, String menuId, CBSValue value, boolean clickable) {
    	String name = 
    		"<font color='"+PreferenceLevel.int2color(value.getTimePref())+"'>"+
    		value.getDays()+" "+value.getStartTime()+" - "+value.getEndTime()+" "+value.getDatePatternName()+
    		"</font> ";
    	String roomLink = "";
    	for (int i=0;i<value.getRoomIds().size();i++) {
    		name += (i>0?", ":"")+"<font color='"+PreferenceLevel.int2color(((Integer)value.getRoomPrefs().get(i)).intValue())+"'>"+ value.getRoomNames().get(i)+"</font>";
    		roomLink += "&room"+i+"="+value.getRoomIds().get(i);
    	}
    	if (value.getInstructorName()!=null)
    		name += " "+value.getInstructorName();
    	String description = null;
    	String onClick = null;
    	if (clickable)
    		onClick = "onclick=\"(parent ? parent : window).showGwtDialog('Suggestions', 'suggestions.do?id="+value.variable().getId()+roomLink+"&days="+value.getDayCode()+"&pattern="+value.getPatternId()+"&slot="+value.getStartSlot()+"&dates="+value.getDatePatternId()+"&op=Try&reset=1','900','90%');\"";	
        menu_item(out, menuId, value.getCounter() + "&times; " + name, description, onClick, true);
    }
    
    private void constraintMenuItem(PrintWriter out, String menuId, CBSConstraint constraint, boolean clickable) {
    	String name = "<font color='"+PreferenceLevel.prolog2color(constraint.getPref())+"'>";
    	String link = null;
    	switch (constraint.getType()) {
    		case sConstraintTypeBalanc : 
    			name += "Balancing of department "+constraint.getName();
    			break;
    		case sConstraintTypeSpread : 
    			name += "Same subpart spread "+constraint.getName();
    			break;
    		case sConstraintTypeGroup :
    			name += "Distribution "+constraint.getName();
    			break;
    		case sConstraintTypeInstructor :
    			name += "Instructor "+constraint.getName();
    			if (clickable) link = "timetable.do?filter="+constraint.getName()+"&mode=i&op=Show";
    			break;
    		case sConstraintTypeRoom :
    			name += "Room "+constraint.getName();
    			if (clickable) link = "timetable.do?filter="+constraint.getName()+"&mode=r&op=Show";
    			break;
    		case sConstraintTypeClassLimit :
    			name += "Class limit "+constraint.getName();
    			break;
    		case sConstraintTypeMinNrRoomUsed :
    		case sConstraintTypeMinNrGroupsOfTime :
    			name += constraint.getName();
    			break;
    		default :
    			name += (constraint.getName()==null?"Unknown":constraint.getName());
    	}
    	name += "</font>";
    	String description = null;
    	String onClick = null;
    	if (link!=null)
    		onClick = "href=\""+link+"\"";
        menu_item(out, menuId, constraint.getCounter() + "&times; " + name, description, onClick, true);
    }
    
    private void assignmentLeafItem(PrintWriter out, CBSAssignment assignment, boolean clickable) {
    	String name = 
    		"<font color='"+PreferenceLevel.prolog2color(assignment.getPref())+"'>"+
    		assignment.getVariableName()+
    		"</font> &larr; "+
    		"<font color='"+PreferenceLevel.int2color(assignment.getTimePref())+"'>"+
    		assignment.getDays()+" "+assignment.getStartTime()+" - "+assignment.getEndTime()+" "+assignment.getDatePatternName()+
    		"</font> ";
    	String roomLink = "";
    	for (int i=0;i<assignment.getRoomIds().size();i++) {
    		name += (i>0?", ":"")+"<font color='"+PreferenceLevel.int2color(((Integer)assignment.getRoomPrefs().get(i)).intValue())+"'>"+ assignment.getRoomNames().get(i)+"</font>";
    		roomLink += "&room"+i+"="+assignment.getRoomIds().get(i);
    	}
    	if (assignment.getInstructorName()!=null)
    		name += " "+assignment.getInstructorName();
    	String onClick = null;
    	if (clickable)
    		onClick = "onclick=\"(parent ? parent : window).showGwtDialog('Suggestions', 'suggestions.do?id="+assignment.getId()+roomLink+"&days="+assignment.getDayCode()+"&pattern="+assignment.getPatternId()+"&slot="+assignment.getStartSlot()+"&dates="+assignment.getDatePatternId()+"&op=Try&reset=1','900','90%');\"";
        leaf_item(out, assignment.getCounter()+"&times; "+name, null, onClick);
    }
    
    public static void printHtmlHeader(JspWriter jsp) {
    	PrintWriter out = new PrintWriter(jsp);
    	printHtmlHeader(out, false);
    }
    
    public static void printHtmlHeader(PrintWriter out, boolean style) {
    	if (style) {
    		out.println("<style type=\"text/css\">");
    		out.println("<!--");
    		out.println("A:link     { color: blue; text-decoration: none; border:0; background:0; }");
    		out.println("A:visited  { color: blue; text-decoration: none; border:0; background:0; }");
    		out.println("A:active   { color: blue; text-decoration: none; border:0; background:0; }");
    		out.println("A:hover    { color: blue; text-decoration: none; border:0; background:0; }");
    		out.println(".TextBody  { background-color: white; color:black; font-size: 12px; }");
    		out.println(".WelcomeHead { color: black; margin-top: 0px; margin-left: 0px; font-weight: bold; text-align: right; font-size: 30px; font-family: Comic Sans MS}");
    		out.println("-->");
    		out.println("</style>");
    		out.println();
    	}
        out.println("<script language=\"javascript\" type=\"text/javascript\">");
        out.println("function toggle(item) {");
        out.println("	obj=document.getElementById(\"__idMenuDiv\"+item);");
        out.println("	visible=(obj.style.display!=\"none\");");
        out.println("	img=document.getElementById(\"__idMenuImg\" + item);");
        out.println("	menu=document.getElementById(\"__idMenu\" + item);");
        out.println("	if (visible) {obj.style.display=\"none\";img.src=\""+IMG_EXPAND+"\";}");
        out.println("	else {obj.style.display=\"block\";img.src=\""+IMG_COLLAPSE+"\";}");
        out.println("}");
        out.println("</script>");
        out.flush();
    }
    
    private List filter(Collection counters, double limit) {
    	List cnt = new ArrayList(counters);
    	Collections.sort(cnt);
    	int total = 0;
    	for (Iterator e=cnt.iterator();e.hasNext();)
    		total += ((Counter)e.next()).getCounter();
    	
    	int totalLimit = (int)Math.ceil(limit*total);
    	int current = 0;
    	
    	List ret = new ArrayList();
    	for (Iterator e=cnt.iterator();e.hasNext();) {
    		Counter c = (Counter)e.next();
    		ret.add(c);
    		current += c.getCounter();
    		if (current>=totalLimit) break;
    	}
    	
    	return ret;
    }
    
    /** Print conflict-based statistics in HTML format */
    public void printHtml(JspWriter jsp, double limit, int type, boolean clickable) {
    	printHtml(jsp, null, new double[] {limit,limit,limit,limit}, type, clickable);
    }

    /** Print conflict-based statistics in HTML format */
    public void printHtml(PrintWriter out, double limit, int type, boolean clickable) {
    	printHtml(out, null, new double[] {limit,limit,limit,limit}, type, clickable);
    }

    /** Print conflict-based statistics in HTML format */
    public void printHtml(JspWriter jsp, double[] limit, int type, boolean clickable) {
    	printHtml(jsp, null, limit, type, clickable);
    }
    
    /** Print conflict-based statistics in HTML format */
    public void printHtml(PrintWriter out, double[] limit, int type, boolean clickable) {
    	printHtml(out, null, limit, type, clickable);
    }

    /** Print conflict-based statistics in HTML format */
    public void printHtml(JspWriter jsp, Long classId, double limit, int type, boolean clickable) {
    	printHtml(jsp, classId, new double[] {limit,limit,limit,limit}, type, clickable);
    }
    
    /** Print conflict-based statistics in HTML format */
    public void printHtml(PrintWriter out, Long classId, double limit, int type, boolean clickable) {
    	printHtml(out, classId, new double[] {limit,limit,limit,limit}, type, clickable);
    }

    /** Print conflict-based statistics in HTML format */
    public void printHtml(JspWriter jsp, Long classId, double[] limit, int type, boolean clickable) {
    	PrintWriter out = new PrintWriter(jsp);
    	printHtml(out, classId, limit, type, clickable);
    }
    
    /** Print conflict-based statistics in HTML format */
    public void printHtml(PrintWriter out, Long classId, double[] limit, int type, boolean clickable) {
        if (type == TYPE_VARIABLE_BASED) {
        	List vars = filter(iVariables.values(), limit[0]);
        	if (classId!=null) {
        		CBSVariable var = (CBSVariable)iVariables.get(classId);
        		vars.clear(); 
        		if (var!=null) vars.add(var);
        	}
            for (Iterator e1 = vars.iterator(); e1.hasNext();) {
            	CBSVariable variable = (CBSVariable)e1.next();
            	String m1 = String.valueOf(variable.getId());
            	if (classId==null)
            		unassignedVariableMenuItem(out,m1,variable, clickable);
            	List vals = filter(variable.values(), limit[1]);
            	int id = 0;
            	for (Iterator e2 = vals.iterator();e2.hasNext();) {
            		CBSValue value = (CBSValue)e2.next();
            		String m2 = m1+"."+(id++);
                    unassignmentMenuItem(out,m2,value, clickable);
                    List constraints =filter(value.constraints(),limit[2]);
                    for (Iterator e3 = constraints.iterator(); e3.hasNext();) {
                    	CBSConstraint constraint = (CBSConstraint)e3.next();
                    	String m3 = m2 + constraint.getType()+"."+constraint.getId();
                    	constraintMenuItem(out,m3,constraint, clickable);
                    	List assignments = filter(constraint.assignments(),limit[3]);
                    	for (Iterator e4 = assignments.iterator();e4.hasNext();) {
                    		CBSAssignment assignment = (CBSAssignment)e4.next();
                    		assignmentLeafItem(out, assignment, clickable);
                        }
                        end_item(out);
                    }
                    end_item(out);
                }
                end_item(out);
            }
        } else if (type == TYPE_CONSTRAINT_BASED) {
        	Hashtable constraints = new Hashtable();
            for (Enumeration e1 = iVariables.elements(); e1.hasMoreElements();) {
            	CBSVariable variable = (CBSVariable)e1.nextElement();
            	if (classId!=null && classId.longValue()!=variable.getId())
            		continue;
            	for (Iterator e2=variable.values().iterator();e2.hasNext();) {
            		CBSValue value = (CBSValue)e2.next();
            		for (Iterator e3=value.constraints().iterator();e3.hasNext();) {
            			CBSConstraint constraint = (CBSConstraint)e3.next();
            			CBSConstraint xConstraint = (CBSConstraint)constraints.get(constraint.getType()+"."+constraint.getId());
            			if (xConstraint==null) {
            				xConstraint = new CBSConstraint(constraint.getType(),constraint.getId(),constraint.getName(),constraint.getPref());
            				constraints.put(constraint.getType()+"."+constraint.getId(),xConstraint);
            			}
            			CBSVariable xVariable = null;
            			for (Iterator i=xConstraint.variables().iterator();i.hasNext();) {
            				CBSVariable v = (CBSVariable)i.next();
            				if (v.getId()==variable.getId()) {
            					xVariable = v; break;
            				}
            			}
            			if (xVariable==null) {
            				xVariable = new CBSVariable(xConstraint,variable.getId(),variable.getName(),variable.getPref()); 
            				xConstraint.variables().add(xVariable);
            			}
            			CBSValue xValue = new CBSValue(xVariable,value.getInstructorName(), value.getRoomNames(), value.getDayCode(),value.getStartSlot(),value.getRoomIds(),value.getTimePref(),value.getRoomPrefs(),value.getLength(),value.getDatePatternName(),value.getPatternId(),value.getBreakTime(),value.getDatePatternId());
            			xVariable.values().add(xValue);
            			for (Iterator e4=constraint.assignments().iterator();e4.hasNext();) {
            				CBSAssignment assignment = (CBSAssignment)e4.next();
            				xValue.assignments().add(assignment);
            				xValue.incCounter(assignment.getCounter());
            			}
            		}
            	}
            }
        	List consts = filter(constraints.values(), limit[0]);
            for (Iterator e1 = consts.iterator(); e1.hasNext();) {
            	CBSConstraint constraint = (CBSConstraint)e1.next();
            	String m1 = constraint.getType()+"."+constraint.getId();
            	constraintMenuItem(out,m1,constraint, clickable);
            	List variables = filter(constraint.variables(), limit[1]);
            	Collections.sort(variables);
                for (Iterator e2 = variables.iterator(); e2.hasNext();) {
                	CBSVariable variable = (CBSVariable)e2.next();
                	String m2 = m1+"."+variable.getId();
                	if (classId==null)
                		unassignedVariableMenuItem(out,m2,variable, clickable);
                	List vals = filter(variable.values(), limit[2]);
                	int id = 0;
                	for (Iterator e3 = vals.iterator();e3.hasNext();) {
                		CBSValue value = (CBSValue)e3.next();
                		String m3 = m2+"."+(id++);
                		unassignmentMenuItem(out,m3,value, clickable);
                    	List assignments = filter(value.assignments(), limit[3]);
                    	for (Iterator e4 = assignments.iterator();e4.hasNext();) {
                    		CBSAssignment assignment = (CBSAssignment)e4.next();
                    		assignmentLeafItem(out, assignment, clickable);
                        }
                        end_item(out);
                    }
                	if (classId==null)
                		end_item(out);
                }
                end_item(out);
            }
        }
        out.flush();
    }

	public boolean saveToFile() {
		return true;
	}
}
