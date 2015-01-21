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

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.jsp.JspWriter;


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
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.webutil.timegrid.ExamGridTable;

/**
 * @author Tomas Muller
 */
public class ExamConflictStatisticsInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 7L;
	public static int sVersion = 7; // to be able to do some changes in the future
	public static final int sConstraintTypeRoom = 1;
	public static final int sConstraintTypeInstructor = 2;
	public static final int sConstraintTypeGroup = 3;
	public static final int sConstraintTypeStudent = 4;
	private Hashtable iVariables = new Hashtable();
	
	public Collection getCBS() { return iVariables.values(); } 
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
		long iExamId;
		String iName;
		HashSet iValues = new HashSet();
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
		public Set values() { return iValues; }
		public int hashCode() {
			return (new Long(iExamId)).hashCode();
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
			return iName;
		}
		public void save(Element element) {
			element.addAttribute("exam",String.valueOf(iExamId));
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
		Long iPeriodId;
		String iPeriodName;
        int iPeriodPref;
		Vector iRoomIds;
		String iInstructorName = null;
		Vector iRoomNames;
		Vector iRoomPrefs;
		CBSVariable iVariable = null;
		HashSet iConstraints = new HashSet();
		HashSet iAssignments = new HashSet();
		int iLength;
		
		CBSValue(CBSVariable var, Long periodId, String periodName, int periodPref, Vector roomIds, Vector roomNames, Vector roomPrefs) {
			iVariable = var; iRoomIds = roomIds; iRoomNames = roomNames; iRoomPrefs = roomPrefs;
			iPeriodId = periodId; iPeriodName = periodName; iPeriodPref = periodPref;
		}
		CBSValue(CBSVariable var, Element element) {
			iVariable = var;
			iPeriodId = Long.valueOf(element.attributeValue("period"));
			iPeriodName = element.attributeValue("name");
            iPeriodPref = Integer.parseInt(element.attributeValue("pref"));
			iRoomIds = new Vector();
			iRoomNames = new Vector();
			iRoomPrefs = new Vector();
			for (Iterator i=element.elementIterator("room");i.hasNext();) {
				Element r = (Element)i.next();
				iRoomIds.addElement(Integer.valueOf(r.attributeValue("id")));
				iRoomNames.addElement(r.attributeValue("name"));
				iRoomPrefs.addElement(Integer.valueOf(r.attributeValue("pref")));
			}
			for (Iterator i=element.elementIterator("cons");i.hasNext();)
				iConstraints.add(new CBSConstraint(this,(Element)i.next())); 
		}
		public CBSVariable variable() { return iVariable; }
		public Long getPeriodId() { return iPeriodId; }
		public String getPeriodName() { return iPeriodName; }
		public int getPeriodPref() { return iPeriodPref; }
		public Vector getRoomNames() { return iRoomNames; }
		public Vector getRoomPrefs() { return iRoomPrefs; }
		public String toString() {
			return iPeriodName+" "+iRoomNames;
		}
		public int getCounter() { return iCounter; }
		public void incCounter(int value) { 
			iCounter+=value;
			if (iVariable!=null) iVariable.incCounter(value);
		}
		public Vector getRoomIds() {
			return iRoomIds;
		}
		public Set constraints() { return iConstraints; }
		public Set assignments() { return iAssignments; }
		public int hashCode() {
			return combine(iPeriodId.hashCode(), (iRoomIds==null?0:iRoomIds.hashCode()));
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof CBSValue)) return false;
			CBSValue v = (CBSValue)o;
			return v.getRoomIds().equals(getRoomIds()) && v.getPeriodId().equals(getPeriodId());
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof CBSValue)) return -1;
			int ret = -(new Integer(iCounter)).compareTo(new Integer(((CBSValue)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public void save(Element element) {
			element.addAttribute("period",String.valueOf(iPeriodId));
			element.addAttribute("pref",String.valueOf(iPeriodPref));
			element.addAttribute("name", iPeriodName);
			for (int i=0;i<iRoomIds.size();i++) {
				Element r = element.addElement("room");
				r.addAttribute("id",iRoomIds.elementAt(i).toString());
				r.addAttribute("name",iRoomNames.elementAt(i).toString());
				r.addAttribute("pref",iRoomPrefs.elementAt(i).toString());
			}
			for (Iterator i=iConstraints.iterator();i.hasNext();)
				((CBSConstraint)i.next()).save(element.addElement("cons"));
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
		Long iExamId;
		String iExamName;
		String iExamPref;
        Long iPeriodId;
        String iPeriodName;
        int iPeriodPref;
		int iCounter = 0;
		Vector iRoomIds;
		Vector iRoomPrefs;
		Vector iRoomNames;
		
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
				iRoomIds.addElement(Integer.valueOf(r.attributeValue("id")));
				iRoomNames.addElement(r.attributeValue("name"));
				iRoomPrefs.addElement(Integer.valueOf(r.attributeValue("pref")));
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
		public Vector getRoomNames() { return iRoomNames; }
		public Vector getRoomIds() {
			return iRoomIds;
		}
		public Vector getRoomPrefs() { return iRoomPrefs; }
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
		public int compareTo(Object o) {
			if (o==null || !(o instanceof CBSAssignment)) return -1;
			int ret = -(new Integer(iCounter)).compareTo(new Integer(((CBSAssignment)o).getCounter()));
			if (ret!=0) return ret;
			return toString().compareTo(o.toString());
		}
		public void save(Element element) {
			element.addAttribute("exam",String.valueOf(iExamId));
			element.addAttribute("name",iExamName);
			element.addAttribute("pref",iExamPref);
			for (int i=0;i<iRoomIds.size();i++) {
				Element r = element.addElement("room");
				r.addAttribute("id",iRoomIds.elementAt(i).toString());
				r.addAttribute("name",iRoomNames.elementAt(i).toString());
				r.addAttribute("pref",iRoomPrefs.elementAt(i).toString());
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
    		onClick = "onclick=\"(parent ? parent : window).showGwtDialog('Examination Assignment', 'examInfo.do?examId="+variable.getId()+"&op=Reset','900','90%');\"";
    	menu_item(out, menuId, variable.getCounter() + "&times; " + name, description, onClick, true);
    }
    
    private void unassignmentMenuItem(PrintWriter out, String menuId, CBSValue value, boolean clickable) {
    	String name = 
    		"<font color='"+PreferenceLevel.int2color(value.getPeriodPref())+"'>"+
    		value.getPeriodName()+
    		"</font> ";
    	String roomLink = "";
    	for (int i=0;i<value.getRoomIds().size();i++) {
    		name += (i>0?", ":"")+"<font color='"+PreferenceLevel.int2color(((Integer)value.getRoomPrefs().elementAt(i)).intValue())+"'>"+ value.getRoomNames().elementAt(i)+"</font>";
    		roomLink += (i>0?":":"")+value.getRoomIds().elementAt(i);
    	}
    	String description = null;
    	String onClick = null;
    	if (clickable)
    		onClick = "onclick=\"(parent ? parent : window).showGwtDialog('Examination Assignment', 'examInfo.do?examId="+value.variable().getId()+"&period="+value.getPeriodId()+"&room="+roomLink+"&op=Try&reset=1','900','90%');\"";	
        menu_item(out, menuId, value.getCounter() + "&times; " + name, description, onClick, true);
    }
    
    private void constraintMenuItem(PrintWriter out, String menuId, CBSConstraint constraint, boolean clickable) {
    	String name = "<font color='"+PreferenceLevel.prolog2color(constraint.getPref())+"'>";
    	String link = null;
    	switch (constraint.getType()) {
    		case sConstraintTypeGroup :
    			name += "Distribution "+constraint.getName();
    			break;
    		case sConstraintTypeInstructor :
    			name += "Instructor "+constraint.getName();
    			if (clickable) link = "examGrid.do?filter="+constraint.getName()+"&resource="+ExamGridTable.sResourceInstructor+"&op=Cbs";
    			break;
    		case sConstraintTypeRoom :
    			name += "Room "+constraint.getName();
    			if (clickable) link = "examGrid.do?filter="+constraint.getName()+"&resource="+ExamGridTable.sResourceRoom+"&op=Cbs";
    			break;
            case sConstraintTypeStudent :
                name += "Student "+constraint.getName();
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
    		assignment.getName()+
    		"</font> &larr; "+
    		"<font color='"+PreferenceLevel.int2color(assignment.getPeriodPref())+"'>"+
    		assignment.getPeriodName()+
    		"</font> ";
    	String roomLink = "";
    	for (int i=0;i<assignment.getRoomIds().size();i++) {
    		name += (i>0?", ":"")+"<font color='"+PreferenceLevel.int2color(((Integer)assignment.getRoomPrefs().elementAt(i)).intValue())+"'>"+ assignment.getRoomNames().elementAt(i)+"</font>";
    		roomLink += (i>0?":":"")+assignment.getRoomIds().elementAt(i);
    	}
    	String onClick = null;
    	if (clickable)
    	    onClick = "onclick=\"(parent ? parent : window).showGwtDialog('Examination Assignment', 'examInfo.do?examId="+assignment.getId()+"&period="+assignment.getPeriodId()+"&room="+roomLink+"&op=Try&reset=1','900','90%');\"";
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
    
    private Vector filter(Collection counters, double limit) {
    	Vector cnt = new Vector(counters);
    	Collections.sort(cnt);
    	int total = 0;
    	for (Enumeration e=cnt.elements();e.hasMoreElements();)
    		total += ((Counter)e.nextElement()).getCounter();
    	
    	int totalLimit = (int)Math.ceil(limit*total);
    	int current = 0;
    	
    	Vector ret = new Vector();
    	for (Enumeration e=cnt.elements();e.hasMoreElements();) {
    		Counter c = (Counter)e.nextElement();
    		ret.addElement(c);
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
        	Vector vars = filter(iVariables.values(), limit[0]);
        	if (classId!=null) {
        		CBSVariable var = (CBSVariable)iVariables.get(classId);
        		vars.clear(); 
        		if (var!=null) vars.add(var);
        	}
            for (Enumeration e1 = vars.elements(); e1.hasMoreElements();) {
            	CBSVariable variable = (CBSVariable)e1.nextElement();
            	String m1 = String.valueOf(variable.getId());
            	if (classId==null)
            		unassignedVariableMenuItem(out,m1,variable, clickable);
            	Vector vals = filter(variable.values(), limit[1]);
            	int id = 0;
            	for (Enumeration e2 = vals.elements();e2.hasMoreElements();) {
            		CBSValue value = (CBSValue)e2.nextElement();
            		String m2 = m1+"."+(id++);
                    unassignmentMenuItem(out,m2,value, clickable);
                    Vector constraints =filter(value.constraints(),limit[2]);
                    for (Enumeration e3 = constraints.elements(); e3.hasMoreElements();) {
                    	CBSConstraint constraint = (CBSConstraint)e3.nextElement();
                    	String m3 = m2 + constraint.getType()+"."+constraint.getId();
                    	constraintMenuItem(out,m3,constraint, clickable);
                    	Vector assignments = filter(constraint.assignments(),limit[3]);
                    	for (Enumeration e4 = assignments.elements();e4.hasMoreElements();) {
                    		CBSAssignment assignment = (CBSAssignment)e4.nextElement();
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
            			CBSValue xValue = new CBSValue(xVariable,
            			        value.getPeriodId(), value.getPeriodName(), value.getPeriodPref(), 
            			        value.getRoomIds(), value.getRoomNames(), value.getRoomPrefs());
            			xVariable.values().add(xValue);
            			for (Iterator e4=constraint.assignments().iterator();e4.hasNext();) {
            				CBSAssignment assignment = (CBSAssignment)e4.next();
            				xValue.assignments().add(assignment);
            				xValue.incCounter(assignment.getCounter());
            			}
            		}
            	}
            }
        	Vector consts = filter(constraints.values(), limit[0]);
            for (Enumeration e1 = consts.elements(); e1.hasMoreElements();) {
            	CBSConstraint constraint = (CBSConstraint)e1.nextElement();
            	String m1 = constraint.getType()+"."+constraint.getId();
            	constraintMenuItem(out,m1,constraint, clickable);
            	Vector variables = filter(constraint.variables(), limit[1]);
            	Collections.sort(variables);
                for (Enumeration e2 = variables.elements(); e2.hasMoreElements();) {
                	CBSVariable variable = (CBSVariable)e2.nextElement();
                	String m2 = m1+"."+variable.getId();
                	if (classId==null)
                		unassignedVariableMenuItem(out,m2,variable, clickable);
                	Vector vals = filter(variable.values(), limit[2]);
                	int id = 0;
                	for (Enumeration e3 = vals.elements();e3.hasMoreElements();) {
                		CBSValue value = (CBSValue)e3.nextElement();
                		String m3 = m2+"."+(id++);
                		unassignmentMenuItem(out,m3,value, clickable);
                    	Vector assignments = filter(value.assignments(), limit[3]);
                    	for (Enumeration e4 = assignments.elements();e4.hasMoreElements();) {
                    		CBSAssignment assignment = (CBSAssignment)e4.nextElement();
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
