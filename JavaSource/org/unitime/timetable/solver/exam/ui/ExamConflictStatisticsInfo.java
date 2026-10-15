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
package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamDistributionConstraint;
import org.cpsolver.exam.model.ExamInstructor;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.exam.model.ExamRoom;
import org.cpsolver.exam.model.ExamRoomPlacement;
import org.cpsolver.exam.model.ExamStudent;
import org.cpsolver.ifs.extension.AssignedValue;
import org.cpsolver.ifs.extension.ConflictStatistics;
import org.cpsolver.ifs.model.Constraint;
import org.dom4j.Element;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.solver.ui.TimetableInfo;

/**
 * @author Tomas Muller
 */
public class ExamConflictStatisticsInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 7L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	public static int sVersion = 7; // to be able to do some changes in the future
	public static final int sConstraintTypeRoom = 1;
	public static final int sConstraintTypeInstructor = 2;
	public static final int sConstraintTypeGroup = 3;
	public static final int sConstraintTypeStudent = 4;
	private Map<Long, CBSVariable> iVariables = new HashMap<Long, CBSVariable>();
	
	public Collection<CBSVariable> getCBS() { return iVariables.values(); } 
	public CBSVariable getCBS(Long classId) { return (CBSVariable)iVariables.get(classId); }
	
	public void load(ConflictStatistics cbs) {
		load(cbs, null);
	}
	
	public ExamConflictStatisticsInfo getConflictStatisticsSubInfo(Vector variables) {
	    ExamConflictStatisticsInfo ret = new ExamConflictStatisticsInfo();
		for (Enumeration e=variables.elements();e.hasMoreElements();) {
			Exam exam = (Exam)e.nextElement();
			CBSVariable var = (CBSVariable)iVariables.get(exam.getId());
			if (var!=null)
				ret.iVariables.put(exam.getId(),var);
		}
		return ret;
	}
	
	public void merge(ExamConflictStatisticsInfo info) {
		if (info!=null) iVariables.putAll(info.iVariables);
	}
	
	public void load(ConflictStatistics cbs, Long examId) {
		iVariables.clear();
		
		for (Iterator i1=cbs.getNoGoods().entrySet().iterator();i1.hasNext();) {
			Map.Entry entry = (Map.Entry)i1.next();
			AssignedValue assignment = (AssignedValue)entry.getKey();
			ExamPlacement placement = (ExamPlacement)assignment.getValue(); 
			Exam exam = (Exam)placement.variable();
			if (examId!=null && !examId.equals(exam.getId())) continue;
			
			CBSVariable var = (CBSVariable)iVariables.get(exam.getId());
			if (var==null) {
				String pref = PreferenceLevel.sNeutral;//SolverGridModel.hardConflicts2pref(exam,null);
				var = new CBSVariable(exam.getId(),exam.getName(),pref);
				iVariables.put(exam.getId(),var);
			}
			
			Vector roomIds = new Vector();
			Vector roomNames = new Vector();
			Vector roomPrefs = new Vector();
			for (Iterator i=new TreeSet(placement.getRoomPlacements()).iterator();i.hasNext();) {
			    ExamRoomPlacement room = (ExamRoomPlacement)i.next();
			    roomIds.add(room.getId());
			    roomNames.add(room.getName());
			    roomPrefs.add(exam.getRoomPlacements().size()==placement.getRoomPlacements().size()?PreferenceLevel.sIntLevelRequired:room.getPenalty(placement.getPeriod()));
			}
			CBSValue val = new CBSValue(var,
			        placement.getPeriod().getId(),
			        placement.getPeriod().getDayStr()+" "+placement.getPeriod().getTimeStr(),
			        (exam.getPeriodPlacements().size()==1?PreferenceLevel.sIntLevelRequired:placement.getPeriodPlacement().getPenalty()),
			        roomIds, roomNames, roomPrefs);
			var.values().add(val);
			
			List noGoods = (List)entry.getValue();
			
			Hashtable constr2assignments = new Hashtable();
			for (Iterator e2=noGoods.iterator();e2.hasNext();) {
				AssignedValue noGood = (AssignedValue)e2.next();
				if (noGood.getConstraint()==null) continue;
				Vector aaa = (Vector)constr2assignments.get(noGood.getConstraint());
				if (aaa == null) {
					aaa = new Vector();
					constr2assignments.put(noGood.getConstraint(), aaa);
				}
				aaa.addElement(noGood);
			}
			
			for (Iterator i2=constr2assignments.entrySet().iterator();i2.hasNext();) {
				Map.Entry entry2 = (Map.Entry)i2.next();
				Constraint constraint = (Constraint)entry2.getKey();
				Vector noGoodsThisConstraint = (Vector)entry2.getValue();
				
				CBSConstraint con = null;
				if (constraint instanceof ExamRoom) {
					con = new CBSConstraint(val, sConstraintTypeRoom, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof ExamInstructor) {
				    con = new CBSConstraint(val, sConstraintTypeInstructor, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
                } else if (constraint instanceof ExamStudent) {
                    con = new CBSConstraint(val, sConstraintTypeStudent, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
				} else if (constraint instanceof ExamDistributionConstraint) {
					con = new CBSConstraint(val, sConstraintTypeGroup, constraint.getId(), ((ExamDistributionConstraint)constraint).getTypeString(), (constraint.isHard()?PreferenceLevel.sRequired:PreferenceLevel.int2prolog(((ExamDistributionConstraint)constraint).getWeight())));
				} else {
					con = new CBSConstraint(val, -1, constraint.getId(), constraint.getName(), PreferenceLevel.sRequired);
				}
				val.constraints().add(con);
				
				for (Enumeration e3=noGoodsThisConstraint.elements();e3.hasMoreElements();) {
					AssignedValue ass = (AssignedValue)e3.nextElement();
					ExamPlacement p = (ExamPlacement)ass.getValue();
					Exam x = (Exam)p.variable();
					String pr = PreferenceLevel.sNeutral;//SolverGridModel.hardConflicts2pref(x,p);
		            Vector aroomIds = new Vector();
		            Vector aroomNames = new Vector();
		            Vector aroomPrefs = new Vector();
		            for (Iterator i=new TreeSet(p.getRoomPlacements()).iterator();i.hasNext();) {
		                ExamRoomPlacement room = (ExamRoomPlacement)i.next();
		                aroomIds.add(room.getId());
		                aroomNames.add(room.getName());
		                aroomPrefs.add(x.getRoomPlacements().size()==p.getRoomPlacements().size()?PreferenceLevel.sIntLevelRequired:room.getPenalty(p.getPeriod()));
		            }
					CBSAssignment a = new CBSAssignment(con,
							x.getId(),
							x.getName(),
							pr,
							p.getPeriod().getId(),
							p.getPeriod().getDayStr()+" "+p.getPeriod().getTimeStr(),
							(x.getPeriodPlacements().size()==1?PreferenceLevel.sIntLevelRequired:p.getPeriodPlacement().getPenalty()),
							aroomIds,
							aroomNames,
							aroomPrefs);
					con.assignments().add(a);
					a.incCounter((int)ass.getCounter(0));
				}
			}
			
		}
	}
	

	public void load(Element root) {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==sVersion) {
			iVariables.clear();
			for (Iterator i1=root.elementIterator("var");i1.hasNext();) {
				CBSVariable var = new CBSVariable((Element)i1.next());
				iVariables.put(Long.valueOf(var.getId()),var);
			}
		}
	}
	
	public void save(Element root) {
		root.addAttribute("version", String.valueOf(sVersion));
		for (Iterator i1=iVariables.values().iterator();i1.hasNext();) {
			((CBSVariable)i1.next()).save(root.addElement("var"));
		}
	}
	
	public static interface Counter extends Comparable<Counter> {
		public int getCounter();
		public void incCounter(int value);
	}
	
	public static class CBSVariable implements Counter, Serializable {
		private static final long serialVersionUID = 1L;
		int iCounter = 0;
		long iExamId;
		String iName;
		Set<CBSValue> iValues = new HashSet<CBSValue>();
		CBSConstraint iConstraint = null;
		String iPref = null;
		
		CBSVariable(long examId, String name, String pref) {
			iExamId = examId;
			iName = name;
			iPref = pref;
		}
		CBSVariable(CBSConstraint constraint, long classId, String examId, String pref) {
			iConstraint = constraint;
			iExamId = classId;
			iName = examId;
			iPref = pref;
		}
		CBSVariable(Element element) {
			iExamId = Long.parseLong(element.attributeValue("exam"));
			iName = element.attributeValue("name");
			iPref = element.attributeValue("pref");
			for (Iterator i=element.elementIterator("val");i.hasNext();)
				iValues.add(new CBSValue(this,(Element)i.next())); 
		}
		
		public long getId() { return iExamId; }
		public int getCounter() { return iCounter; }
		public String getName() { return iName; }
		public String getPref() { return iPref; }
		public void incCounter(int value) { 
			iCounter+=value;
			if (iConstraint!=null) iConstraint.incCounter(value);
		}
		public Set<CBSValue> values() { return iValues; }
		public int hashCode() {
			return (Long.valueOf(iExamId)).hashCode();
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof CBSVariable)) return false;
			return ((CBSVariable)o).getId()==getId();
		}
		public int compareTo(Counter o) {
			if (o==null || !(o instanceof CBSVariable)) return -1;
			int ret = -(Integer.valueOf(iCounter)).compareTo(Integer.valueOf(((CBSVariable)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public String toString() {
			return iName;
		}
		public void save(Element element) {
			element.addAttribute("exam",String.valueOf(iExamId));
			element.addAttribute("name", iName);
			if (iPref!=null)
				element.addAttribute("pref", iPref);
			for (CBSValue v: iValues)
				v.save(element.addElement("val"));
		}
	}

	public static class CBSValue implements Counter, Serializable {
		private static final long serialVersionUID = 1L;
		int iCounter = 0;
		Long iPeriodId;
		String iPeriodName;
        int iPeriodPref;
        List<Long> iRoomIds;
		String iInstructorName = null;
		List<String> iRoomNames;
		List<Integer> iRoomPrefs;
		CBSVariable iVariable = null;
		HashSet<CBSConstraint> iConstraints = new HashSet<CBSConstraint>();
		HashSet<CBSAssignment> iAssignments = new HashSet<CBSAssignment>();
		int iLength;
		
		CBSValue(CBSVariable var, Long periodId, String periodName, int periodPref, List<Long> roomIds, List<String> roomNames, List<Integer> roomPrefs) {
			iVariable = var; iRoomIds = roomIds; iRoomNames = roomNames; iRoomPrefs = roomPrefs;
			iPeriodId = periodId; iPeriodName = periodName; iPeriodPref = periodPref;
		}
		CBSValue(CBSVariable var, Element element) {
			iVariable = var;
			iPeriodId = Long.valueOf(element.attributeValue("period"));
			iPeriodName = element.attributeValue("name");
            iPeriodPref = Integer.parseInt(element.attributeValue("pref"));
			iRoomIds = new ArrayList<Long>();
			iRoomNames = new ArrayList<String>();
			iRoomPrefs = new ArrayList<Integer>();
			for (Iterator i=element.elementIterator("room");i.hasNext();) {
				Element r = (Element)i.next();
				iRoomIds.add(Long.valueOf(r.attributeValue("id")));
				iRoomNames.add(r.attributeValue("name"));
				iRoomPrefs.add(Integer.valueOf(r.attributeValue("pref")));
			}
			for (Iterator i=element.elementIterator("cons");i.hasNext();)
				iConstraints.add(new CBSConstraint(this,(Element)i.next())); 
		}
		public CBSVariable variable() { return iVariable; }
		public Long getPeriodId() { return iPeriodId; }
		public String getPeriodName() { return iPeriodName; }
		public int getPeriodPref() { return iPeriodPref; }
		public List<String> getRoomNames() { return iRoomNames; }
		public List<Integer> getRoomPrefs() { return iRoomPrefs; }
		public String toString() {
			return iPeriodName+" "+iRoomNames;
		}
		public int getCounter() { return iCounter; }
		public void incCounter(int value) { 
			iCounter+=value;
			if (iVariable!=null) iVariable.incCounter(value);
		}
		public List<Long> getRoomIds() {
			return iRoomIds;
		}
		public Set<CBSConstraint> constraints() { return iConstraints; }
		public Set<CBSAssignment> assignments() { return iAssignments; }
		public int hashCode() {
			return combine(iPeriodId.hashCode(), (iRoomIds==null?0:iRoomIds.hashCode()));
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof CBSValue)) return false;
			CBSValue v = (CBSValue)o;
			return v.getRoomIds().equals(getRoomIds()) && v.getPeriodId().equals(getPeriodId());
		}
		public int compareTo(Counter o) {
			if (o==null || !(o instanceof CBSValue)) return -1;
			int ret = -(Integer.valueOf(iCounter)).compareTo(Integer.valueOf(((CBSValue)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public void save(Element element) {
			element.addAttribute("period",String.valueOf(iPeriodId));
			element.addAttribute("pref",String.valueOf(iPeriodPref));
			element.addAttribute("name", iPeriodName);
			for (int i=0;i<iRoomIds.size();i++) {
				Element r = element.addElement("room");
				r.addAttribute("id",iRoomIds.get(i).toString());
				r.addAttribute("name",iRoomNames.get(i).toString());
				r.addAttribute("pref",iRoomPrefs.get(i).toString());
			}
			for (Iterator i=iConstraints.iterator();i.hasNext();)
				((CBSConstraint)i.next()).save(element.addElement("cons"));
		}
	}
	
	public static class CBSConstraint implements Counter, Serializable {
		private static final long serialVersionUID = 1L;
		CBSValue iValue;
		int iCounter = 0;
		long iId;
		String iName = null;
		int iType;
		HashSet<CBSAssignment> iAssignments = new HashSet<CBSAssignment>();
		HashSet<CBSVariable> iVariables = new HashSet<CBSVariable>();
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
		public Set<CBSVariable> variables() { return iVariables; }
		public Set<CBSAssignment> assignments() { return iAssignments; }
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
		public int compareTo(Counter o) {
			if (o==null || !(o instanceof CBSConstraint)) return -1;
			int ret = -(Integer.valueOf(iCounter)).compareTo(Integer.valueOf(((CBSConstraint)o).getCounter()));
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

	public static class CBSAssignment implements Counter, Serializable {
		private static final long serialVersionUID = 1L;
		CBSConstraint iConstraint;
		Long iExamId;
		String iExamName;
		String iExamPref;
        Long iPeriodId;
        String iPeriodName;
        int iPeriodPref;
		int iCounter = 0;
		List<Long> iRoomIds;
		List<Integer> iRoomPrefs;
		List<String> iRoomNames;
		
		CBSAssignment(CBSConstraint constraint, Long examId, String examName, String examPref, Long periodId, String periodName, int periodPref, Vector roomIds, Vector roomNames, Vector roomPrefs) {
			iExamId = examId; iExamName = examName; iExamPref = examPref;
			iPeriodId = periodId; iPeriodName = periodName; iPeriodPref = periodPref;
			iRoomIds = roomIds; iRoomNames = roomNames; iRoomPrefs = roomPrefs;
			iConstraint = constraint;
		}
		CBSAssignment(CBSConstraint constraint, Element element) {
			iConstraint = constraint;
			iExamId = Long.valueOf(element.attributeValue("exam"));
			iExamName = element.attributeValue("name");
			iExamPref = element.attributeValue("pref");
			iRoomIds = new Vector();
			iRoomNames = new Vector();
			iRoomPrefs = new Vector();
			for (Iterator i=element.elementIterator("room");i.hasNext();) {
				Element r = (Element)i.next();
				iRoomIds.add(Long.valueOf(r.attributeValue("id")));
				iRoomNames.add(r.attributeValue("name"));
				iRoomPrefs.add(Integer.valueOf(r.attributeValue("pref")));
			}
			iPeriodId = Long.valueOf(element.attributeValue("period"));
			iPeriodName = element.attributeValue("periodName");
			iPeriodPref = Integer.parseInt(element.attributeValue("periodPref"));
			incCounter(Integer.parseInt(element.attributeValue("cnt")));
		}
		public Long getId() { return iExamId; }
		public CBSConstraint getConstraint() { return iConstraint; }
		public String getName() { return iExamName; }
		public String getPref() { return iExamPref; }
		public Long  getPeriodId() { return iPeriodId; }
		public String getPeriodName() { return iPeriodName; }
		public int getPeriodPref() { return iPeriodPref; }
		public String toString() {
			return iExamName+" "+iPeriodName+" "+iRoomNames;
		}
		public List<String> getRoomNames() { return iRoomNames; }
		public List<Long> getRoomIds() {
			return iRoomIds;
		}
		public List<Integer> getRoomPrefs() { return iRoomPrefs; }
		public int hashCode() {
			return combine(iExamId.hashCode(),combine(iRoomIds.hashCode(),iPeriodId.hashCode()));
		}
		public int getCounter() { return iCounter; }
		public void incCounter(int value) { 
			iCounter+=value;
			if (iConstraint!=null) iConstraint.incCounter(value);
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof CBSAssignment)) return false;
			CBSAssignment a = (CBSAssignment)o;
			return a.getId().equals(getId()) && a.getRoomIds().equals(getRoomIds()) && a.getPeriodId().equals(getPeriodId());
		}
		public int compareTo(Counter o) {
			if (o==null || !(o instanceof CBSAssignment)) return -1;
			int ret = -(Integer.valueOf(iCounter)).compareTo(Integer.valueOf(((CBSAssignment)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public void save(Element element) {
			element.addAttribute("exam",String.valueOf(iExamId));
			element.addAttribute("name",iExamName);
			element.addAttribute("pref",iExamPref);
			for (int i=0;i<iRoomIds.size();i++) {
				Element r = element.addElement("room");
				r.addAttribute("id",iRoomIds.get(i).toString());
				r.addAttribute("name",iRoomNames.get(i).toString());
				r.addAttribute("pref",iRoomPrefs.get(i).toString());
			}
			element.addAttribute("period", String.valueOf(iPeriodId));
			element.addAttribute("periodName", iPeriodName);
			element.addAttribute("periodPref", String.valueOf(iPeriodPref));
			element.addAttribute("cnt", String.valueOf(iCounter));
		}
	}	

    private static int combine(int a, int b) {
        int ret = 0;
        for (int i=0;i<15;i++) ret = ret | ((a & (1<<i))<<i) | ((b & (1<<i))<<(i+1));
        return ret;
    }
    
    public static <E extends Counter> List<E> filter(Collection<E> counters, double limit) {
    	List<E> cnt = new ArrayList<E>(counters);
    	Collections.sort(cnt);
    	int total = 0;
    	for (Iterator<E> e=cnt.iterator();e.hasNext();)
    		total += e.next().getCounter();
    	
    	int totalLimit = (int)Math.ceil(limit*total);
    	int current = 0;
    	
    	List<E> ret = new ArrayList<E>();
    	for (Iterator<E> e=cnt.iterator();e.hasNext();) {
    		E c = e.next();
    		ret.add(c);
    		current += c.getCounter();
    		if (current>=totalLimit) break;
    	}
    	
    	return ret;
    }

	public boolean saveToFile() {
		return true;
	}
	
	public static Collection<CBSConstraint> transpose(Collection<CBSVariable> variables, Long classId) {
		Map<String, CBSConstraint> constraints = new HashMap<String, CBSConstraint>();
        for (CBSVariable variable: variables) {
        	if (classId != null && !classId.equals(variable.getId())) continue;
        	for (CBSValue value: variable.values())
        		for (CBSConstraint constraint: value.constraints()) {
        			CBSConstraint xConstraint = constraints.get(constraint.getType()+"."+constraint.getId());
        			if (xConstraint == null) {
        				xConstraint = new CBSConstraint(constraint.getType(), constraint.getId(), constraint.getName(), constraint.getPref());
        				constraints.put(constraint.getType()+"."+constraint.getId(), xConstraint);
        			}
        			CBSVariable xVariable = null;
        			for (CBSVariable v: xConstraint.variables()) {
        				if (v.getId() == variable.getId()) {
        					xVariable = v; break;
        				}
        			}
        			if (xVariable==null) {
        				xVariable = new CBSVariable(xConstraint,variable.getId() ,variable.getName(), variable.getPref()); 
        				xConstraint.variables().add(xVariable);
        			}
        			CBSValue xValue = new CBSValue(xVariable, value.getPeriodId(), value.getPeriodName(), value.getPeriodPref(), value.getRoomIds(), value.getRoomNames(), value.getRoomPrefs());
        			xVariable.values().add(xValue);
        			for (CBSAssignment assignment: constraint.assignments()) {
        				xValue.assignments().add(assignment);
        				xValue.incCounter(assignment.getCounter());
        			}
        		}
        	}
        return constraints.values();
	}
}
