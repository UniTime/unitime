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
package org.unitime.timetable.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.solver.CommitedClassAssignmentProxy;

import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class ExportPreferences {
	private static Log sLog = LogFactory.getLog(ExportPreferences.class);
	public CommitedClassAssignmentProxy proxy = new CommitedClassAssignmentProxy();
	
	public Comparator ioCmp = null;
	public Comparator subpartCmp = null;
	public Comparator classCmp = null;
	
	public void exportDatePattern(Element parent, DatePattern datePattern) {
		sLog.info("Exporting "+datePattern.getName());
		Element el = parent.addElement("datePattern");
		el.addAttribute("uniqueId", datePattern.getUniqueId().toString());
		el.addAttribute("name", datePattern.getName());
		el.addAttribute("pattern", datePattern.getPattern());
		el.addAttribute("visible", datePattern.isVisible().toString());
		el.addAttribute("type", datePattern.getType().toString());
		el.addAttribute("offset", datePattern.getOffset().toString());
	}
	
	public void exportTimePattern(Element parent, TimePattern timePattern) {
		sLog.info("Exporting "+timePattern.getName());
		Element el = parent.addElement("timePattern");
		el.addAttribute("uniqueId", timePattern.getUniqueId().toString());
		el.addAttribute("name", timePattern.getName());
		el.addAttribute("minPerMtg", timePattern.getMinPerMtg().toString());
		el.addAttribute("slotsPerMtg", timePattern.getSlotsPerMtg().toString());
		el.addAttribute("nrMeetings", timePattern.getNrMeetings().toString());
		el.addAttribute("visible", timePattern.isVisible().toString());
		el.addAttribute("type", timePattern.getType().toString());
		for (Iterator i=timePattern.getDays().iterator();i.hasNext();) {
			TimePatternDays d = (TimePatternDays)i.next();
			el.addElement("dayCode").setText(d.getDayCode().toString());
		}
		for (Iterator i=timePattern.getTimes().iterator();i.hasNext();) {
			TimePatternTime t = (TimePatternTime)i.next();
			el.addElement("startSlot").setText(t.getStartSlot().toString());
		}
	}
	
	public void exportSubpartStructure(Element parent, SchedulingSubpart s) {
		Element el = parent.addElement("schedulingSubpart");
		el.addAttribute("uniqueId",s.getUniqueId().toString());
		el.addAttribute("itype",s.getItypeDesc());
		el.addAttribute("suffix",s.getSchedulingSubpartSuffix());
		el.addAttribute("minutesPerWk",s.getMinutesPerWk().toString());
		TreeSet subparts = new TreeSet(subpartCmp);
		subparts.addAll(s.getChildSubparts());
		for (Iterator i=subparts.iterator();i.hasNext();) {
			exportSubpartStructure(el, (SchedulingSubpart)s);
		}
		TreeSet classes = new TreeSet(classCmp);
		classes.addAll(s.getClasses());
		for (Iterator i=classes.iterator();i.hasNext();) {
			Class_ c = (Class_)i.next();
			Element x = el.addElement("class");
			x.addAttribute("uniqueId", c.getUniqueId().toString());
			if (c.getParentClass()!=null)
				x.addAttribute("parent", c.getParentClass().getUniqueId().toString());
			x.addAttribute("expectedCapacity", c.getExpectedCapacity().toString());
			x.addAttribute("maxExpectedCapacity", c.getMaxExpectedCapacity().toString());
			x.addAttribute("roomRatio", c.getRoomRatio().toString());
			x.addAttribute("nbrRooms", c.getNbrRooms().toString());
			x.addAttribute("manager", c.getManagingDept().getDeptCode());
			x.addAttribute("sectionNumber", String.valueOf(c.getSectionNumber()));
		}
	}
	
	public void exportInstructionalOffering(Element parent, InstructionalOffering io) throws Exception {
		sLog.info("Exporting "+io.getCourseName());
		Element el = parent.addElement("instructionalOffering");
		el.addAttribute("uniqueId", io.getUniqueId().toString());
		el.addAttribute("subjectArea", io.getControllingCourseOffering().getSubjectAreaAbbv());
		el.addAttribute("courseNbr", io.getControllingCourseOffering().getCourseNbr());
		if (io.getInstrOfferingPermId()!=null)
			el.addAttribute("instrOfferingPermId", io.getInstrOfferingPermId().toString());
		for (Iterator i=io.getCourseOfferings().iterator();i.hasNext();) {
			CourseOffering co = (CourseOffering)i.next();
			Element x = el.addElement("courseOffering");
			x.addAttribute("uniqueId",co.getUniqueId().toString());
			x.addAttribute("subjectArea",co.getSubjectAreaAbbv());
			x.addAttribute("courseNbr",co.getCourseNbr());
			x.addAttribute("projectedDemand",co.getProjectedDemand().toString());
			x.addAttribute("demand",co.getDemand().toString());
			x.addAttribute("isControl",co.getIsControl().toString());
			if (co.getPermId()!=null)
			    x.addAttribute("permId",co.getPermId());
		}
		for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();) {
			InstrOfferingConfig c = (InstrOfferingConfig)i.next();
			Element x = el.addElement("instrOfferingConfig");
			x.addAttribute("uniqueId",c.getUniqueId().toString());
			x.addAttribute("limit",c.getLimit().toString());
			TreeSet subparts = new TreeSet(subpartCmp);
			subparts.addAll(c.getSchedulingSubparts());
			for (Iterator j=subparts.iterator();j.hasNext();) {
				SchedulingSubpart s = (SchedulingSubpart)j.next();
				if (s.getParentSubpart()==null)
					exportSubpartStructure(x, s);
			}
		}
	}
	
	public void exportClass(Element parent, Class_ clazz) throws Exception {
		sLog.info("Exporting "+clazz.getClassLabel());
		Element el = parent.addElement("class");
		el.addAttribute("uniqueId", clazz.getUniqueId().toString());
		el.addAttribute("subjectArea", clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv());
		el.addAttribute("courseNbr", clazz.getSchedulingSubpart().getControllingCourseOffering().getCourseNbr());
		el.addAttribute("itype", clazz.getSchedulingSubpart().getItypeDesc());
		el.addAttribute("section", String.valueOf(clazz.getSectionNumber()));
		el.addAttribute("suffix", clazz.getSchedulingSubpart().getSchedulingSubpartSuffix());
		el.addAttribute("manager", clazz.getManagingDept().getDeptCode());
		el.addAttribute("expectedCapacity", clazz.getExpectedCapacity().toString());
		el.addAttribute("numberOfRooms", clazz.getNbrRooms().toString());
		el.addAttribute("maxExpectedCapacity", clazz.getMaxExpectedCapacity().toString());
		el.addAttribute("roomRatio", clazz.getRoomRatio().toString());
		el.addAttribute("notes", clazz.getNotes());
		if (clazz.getDatePattern()!=null)
			el.addAttribute("datePattern", clazz.getDatePattern().getName());
		el.addAttribute("deptCode", clazz.getControllingDept().getDeptCode());
		for (Iterator i=clazz.getClassInstructors().iterator();i.hasNext();) {
			exportClassInstructor(el,(ClassInstructor)i.next());
		}
		for (Iterator i=clazz.getPreferences(TimePref.class).iterator();i.hasNext();) {
			exportTimePref(el,(TimePref)i.next());
		}
		for (Iterator i=clazz.getPreferences(RoomPref.class).iterator();i.hasNext();) {
			exportRoomPref(el,(RoomPref)i.next());
		}
		for (Iterator i=clazz.getPreferences(BuildingPref.class).iterator();i.hasNext();) {
			exportBuildingPref(el,(BuildingPref)i.next());
		}
		for (Iterator i=clazz.getPreferences(RoomFeaturePref.class).iterator();i.hasNext();) {
			exportRoomFeaturePref(el,(RoomFeaturePref)i.next());
		}
		for (Iterator i=clazz.getPreferences(RoomGroupPref.class).iterator();i.hasNext();) {
			exportRoomGroupPref(el,(RoomGroupPref)i.next());
		}
		Assignment assignment = proxy.getAssignment(clazz);
		if (assignment!=null) {
			el.addAttribute("assignedDays", assignment.getDays().toString());
			el.addAttribute("assignedSlot", assignment.getStartSlot().toString());
			el.addAttribute("assignedTimePattern", assignment.getTimePattern().getName());
			Element r = el.addElement("assignedRooms");
			for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
				Location location = (Location)i.next();
				r.addElement("room").addAttribute("uniqueId",location.getUniqueId().toString()).addAttribute("name",location.getLabel());
			}
		}
	}
	
	public void exportSchedulingSubpart(Element parent, SchedulingSubpart subpart) {
		sLog.info("Exporting "+subpart.getCourseName()+" "+subpart.getItypeDesc()+(subpart.getSchedulingSubpartSuffix().length()==0?"":" ("+subpart.getSchedulingSubpartSuffix()+")"));
		Element el = parent.addElement("schedulingSubpart");
		el.addAttribute("uniqueId", subpart.getUniqueId().toString());
		el.addAttribute("subjectArea", subpart.getControllingCourseOffering().getSubjectAreaAbbv());
		el.addAttribute("courseNbr", subpart.getControllingCourseOffering().getCourseNbr());
		el.addAttribute("itype", subpart.getItypeDesc());
		el.addAttribute("suffix", subpart.getSchedulingSubpartSuffix());
		el.addAttribute("manager", subpart.getManagingDept().getDeptCode());
		el.addAttribute("minutesPerWk", subpart.getMinutesPerWk().toString());
		if (subpart.getDatePattern()!=null)
			el.addAttribute("datePattern", subpart.getDatePattern().getName());
		el.addAttribute("deptCode", subpart.getControllingDept().getDeptCode());
		for (Iterator i=subpart.getPreferences(TimePref.class).iterator();i.hasNext();) {
			exportTimePref(el,(TimePref)i.next());
		}
		for (Iterator i=subpart.getPreferences(RoomPref.class).iterator();i.hasNext();) {
			exportRoomPref(el,(RoomPref)i.next());
		}
		for (Iterator i=subpart.getPreferences(BuildingPref.class).iterator();i.hasNext();) {
			exportBuildingPref(el,(BuildingPref)i.next());
		}
		for (Iterator i=subpart.getPreferences(RoomFeaturePref.class).iterator();i.hasNext();) {
			exportRoomFeaturePref(el,(RoomFeaturePref)i.next());
		}
		for (Iterator i=subpart.getPreferences(RoomGroupPref.class).iterator();i.hasNext();) {
			exportRoomGroupPref(el,(RoomGroupPref)i.next());
		}
	}

	public void exportClassInstructor(Element parent, ClassInstructor classInstructor) {
		Element el = parent.addElement("instructor");
		el.addAttribute("uniqueId", classInstructor.getInstructor().getUniqueId().toString());
		el.addAttribute("isLead", classInstructor.isLead().toString());
		el.addAttribute("percentShare", classInstructor.getPercentShare().toString());
		el.addAttribute("puid", classInstructor.getInstructor().getExternalUniqueId());
	}
	
	public void exportTimePref(Element parent, TimePref timePref) {
		Element el = parent.addElement("timePref");
		el.addAttribute("uniqueId", timePref.getUniqueId().toString());
		el.addAttribute("timePattern", timePref.getTimePattern().getName());
		el.addAttribute("level", timePref.getPrefLevel().getPrefProlog());
		el.addAttribute("preference", timePref.getPreference());
	}
		
	public void exportRoomPref(Element parent, RoomPref roomPref) {
		Element el = parent.addElement("roomPref");
		el.addAttribute("uniqueId", roomPref.getUniqueId().toString());
		el.addAttribute("level", roomPref.getPrefLevel().getPrefProlog());
		el.addAttribute("room", roomPref.getRoom().getLabel());
	}

	public void exportBuildingPref(Element parent, BuildingPref bldgPref) {
		Element el = parent.addElement("buildingPref");
		el.addAttribute("uniqueId", bldgPref.getUniqueId().toString());
		el.addAttribute("level", bldgPref.getPrefLevel().getPrefProlog());
		el.addAttribute("building", bldgPref.getBuilding().getAbbreviation());
	}

	public void exportRoomFeaturePref(Element parent, RoomFeaturePref roomFeaturePref) {
		Element el = parent.addElement("roomFeaturePref");
		el.addAttribute("uniqueId", roomFeaturePref.getUniqueId().toString());
		el.addAttribute("level", roomFeaturePref.getPrefLevel().getPrefProlog());
		el.addAttribute("feature", roomFeaturePref.getRoomFeature().getLabel());
	}

	public void exportRoomGroupPref(Element parent, RoomGroupPref roomGroupPref) {
		Element el = parent.addElement("roomGroupPref");
		el.addAttribute("uniqueId", roomGroupPref.getUniqueId().toString());
		el.addAttribute("level", roomGroupPref.getPrefLevel().getPrefProlog());
		el.addAttribute("group", roomGroupPref.getRoomGroup().getName());
	}

	public void exportDistributionPref(Element parent, DistributionPref distributionPref) {
		sLog.info("Exporting "+distributionPref.getDistributionType().getLabel());
		Element el = parent.addElement("distributionPref");
		el.addAttribute("uniqueId", distributionPref.getUniqueId().toString());
		el.addAttribute("level", distributionPref.getPrefLevel().getPrefProlog());
		el.addAttribute("type", distributionPref.getDistributionType().getReference());
		el.addAttribute("manager", ((Department)distributionPref.getOwner()).getDeptCode());
		el.addAttribute("grouping", (distributionPref.getGrouping()==null?"0":distributionPref.getGrouping().toString()));
		for (Iterator i=distributionPref.getDistributionObjects().iterator();i.hasNext();) {
			DistributionObject dobj = (DistributionObject)i.next();
			if (dobj.getPrefGroup() instanceof Class_) {
				Class_ clazz = (Class_)dobj.getPrefGroup();
				Element x = el.addElement("class");
				x.addAttribute("sequenceNumber", dobj.getSequenceNumber().toString());
				x.addAttribute("uniqueId", clazz.getUniqueId().toString()); 
				x.addAttribute("subjectArea", clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv());
				x.addAttribute("courseNbr", clazz.getSchedulingSubpart().getControllingCourseOffering().getCourseNbr());
				x.addAttribute("itype", clazz.getSchedulingSubpart().getItypeDesc());
				x.addAttribute("section", String.valueOf(clazz.getSectionNumber()));
				x.addAttribute("suffix", clazz.getSchedulingSubpart().getSchedulingSubpartSuffix());
			} else if (dobj.getPrefGroup() instanceof SchedulingSubpart) {
				SchedulingSubpart subpart = (SchedulingSubpart)dobj.getPrefGroup();
				Element x = el.addElement("schedulingSubpart");
				x.addAttribute("sequenceNumber", dobj.getSequenceNumber().toString());
				x.addAttribute("uniqueId", subpart.getUniqueId().toString()); 
				x.addAttribute("subjectArea", subpart.getControllingCourseOffering().getSubjectAreaAbbv());
				x.addAttribute("courseNbr", subpart.getControllingCourseOffering().getCourseNbr());
				x.addAttribute("itype", subpart.getItypeDesc());
				x.addAttribute("suffix", subpart.getSchedulingSubpartSuffix());
			}
		}
	}
	
	public void exportInstructor(Element parent, DepartmentalInstructor instructorDept) {
		sLog.info("Exporting "+instructorDept.getNameLastFirst()+" ("+instructorDept.getDepartment().getDeptCode()+")");
		Element el = parent.addElement("instructor");
		el.addAttribute("uniqueId", instructorDept.getUniqueId().toString());
		el.addAttribute("deptCode", instructorDept.getDepartment().getDeptCode());
		el.addAttribute("puid", instructorDept.getExternalUniqueId());
		for (Iterator i=instructorDept.getPreferences(TimePref.class).iterator();i.hasNext();) {
			exportTimePref(el,(TimePref)i.next());
		}
		for (Iterator i=instructorDept.getPreferences(RoomPref.class).iterator();i.hasNext();) {
			exportRoomPref(el,(RoomPref)i.next());
		}
		for (Iterator i=instructorDept.getPreferences(BuildingPref.class).iterator();i.hasNext();) {
			exportBuildingPref(el,(BuildingPref)i.next());
		}
		for (Iterator i=instructorDept.getPreferences(RoomFeaturePref.class).iterator();i.hasNext();) {
			exportRoomFeaturePref(el,(RoomFeaturePref)i.next());
		}
		for (Iterator i=instructorDept.getPreferences(RoomGroupPref.class).iterator();i.hasNext();) {
			exportRoomGroupPref(el,(RoomGroupPref)i.next());
		}
	}
	
	public void exportInstructors(Element parent, Department dept) {
		List ids = (new DepartmentDAO()).
			getSession().
			createQuery("select id from DepartmentalInstructor id where id.department.deptCode=:deptCode and id.department.sessionId=:sessionId").
			setString("deptCode", dept.getDeptCode()).
			setLong("sessionId", dept.getSessionId().longValue()).
			list();
		for (Iterator i=ids.iterator();i.hasNext();) {
			DepartmentalInstructor id = (DepartmentalInstructor)i.next();
			exportInstructor(parent, id);
		}
	}
	
	public void exportAll(Long solverGroupId, File outFile) throws Exception {
		SolverGroup solverGroup = (new SolverGroupDAO()).get(solverGroupId);
		Session session = solverGroup.getSession();
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("export");
		root.addAttribute("solverGroup", solverGroup.getUniqueId().toString());
		root.addAttribute("solverGroupName", solverGroup.getName());
		root.addAttribute("session", session.getUniqueId().toString());
		root.addAttribute("academicYearTerm", session.getAcademicYearTerm());
		root.addAttribute("academicInitiative", session.getAcademicInitiative());
		for (Iterator i=TimePattern.findAll(session,null).iterator();i.hasNext();) {
			TimePattern t = (TimePattern)i.next();
			exportTimePattern(root, t);
		}
		for (Iterator i=DatePattern.findAll(session,true,null,null).iterator();i.hasNext();) {
			DatePattern d = (DatePattern)i.next();
			exportDatePattern(root, d);
		}
		classCmp = new Comparator() {
			public int compare(Object o1, Object o2) {
				Class_ c1 = (Class_)o1;
				Class_ c2 = (Class_)o2;
				int cmp = c1.getCourseName().compareTo(c2.getCourseName());
				if (cmp!=0) return cmp;
				cmp = c1.getSchedulingSubpart().getItype().getItype().compareTo(c2.getSchedulingSubpart().getItype().getItype());
				if (cmp!=0) return cmp;
				cmp = c1.getSchedulingSubpart().getSchedulingSubpartSuffix().compareTo(c2.getSchedulingSubpart().getSchedulingSubpartSuffix());
				if (cmp!=0) return cmp;
				return c1.getUniqueId().compareTo(c2.getUniqueId());
			}
		};
		TreeSet classes = new TreeSet(classCmp);
		subpartCmp = new Comparator() {
			public int compare(Object o1, Object o2) {
				SchedulingSubpart s1 = (SchedulingSubpart)o1;
				SchedulingSubpart s2 = (SchedulingSubpart)o2;
				int cmp = s1.getCourseName().compareTo(s2.getCourseName());
				if (cmp!=0) return cmp;
				cmp = s1.getItype().getItype().compareTo(s2.getItype().getItype());
				if (cmp!=0) return cmp;
				return s1.getUniqueId().compareTo(s2.getUniqueId());
			}
		};
		TreeSet subparts = new TreeSet(subpartCmp);
		ioCmp = new Comparator() {
			public int compare(Object o1, Object o2) {
				InstructionalOffering i1 = (InstructionalOffering)o1;
				InstructionalOffering i2 = (InstructionalOffering)o2;
				int cmp = i1.getCourseName().compareTo(i2.getCourseName());
				if (cmp!=0) return cmp;
				return i1.getUniqueId().compareTo(i2.getUniqueId());
			}
		};
		TreeSet offerings = new TreeSet(ioCmp); 
		classes.addAll(solverGroup.getClasses());
		for (Iterator i=classes.iterator();i.hasNext();) {
			Class_ c = (Class_)i.next();
			exportClass(root, c);
			SchedulingSubpart s = c.getSchedulingSubpart();
			offerings.add(s.getInstrOfferingConfig().getInstructionalOffering());
			if (solverGroup.getDepartments().contains(s.getManagingDept())) {
				subparts.add(s);
			}
		}
		for (Iterator i=subparts.iterator();i.hasNext();) {
			SchedulingSubpart s = (SchedulingSubpart)i.next();
			exportSchedulingSubpart(root, s);
		}
		for (Iterator i=offerings.iterator();i.hasNext();) {
			InstructionalOffering io = (InstructionalOffering)i.next();
			exportInstructionalOffering(root, io);
		}
		for (Iterator i=solverGroup.getDepartments().iterator();i.hasNext();) {
			Department d = (Department)i.next();
			exportInstructors(root, d);
		}
		for (Iterator i=solverGroup.getDistributionPreferences().iterator();i.hasNext();) {
			DistributionPref d = (DistributionPref)i.next();
			exportDistributionPref(root, d);
		}
        FileOutputStream fos = null;
        try {
        	fos = new FileOutputStream(outFile);
        	(new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(document);
        	fos.flush();fos.close();fos=null;
        } finally {
        	try {
        		if (fos!=null) fos.close();
        	} catch (IOException e){}
        }
	}
	
	public static void main(String[] args) {
		// Example arguments: jdbc:oracle:thin:@tamarind.smas.purdue.edu:1521:sms8l 1 c:\\export.xml
		try {
			ToolBox.configureLogging();
			
			HibernateUtil.configureHibernate(args[0]);

	        (new ExportPreferences()).exportAll(new Long(args[1]), new File(args[2]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
