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
package org.unitime.timetable.test;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;

import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class ImportPreferences {
	private static Log sLog = LogFactory.getLog(ImportPreferences.class);
	
	private Hashtable iAllClasses = null;
	private Hashtable iAllSubparts = null;
	private Hashtable iAllInstructionalOfferings = null;
	private org.hibernate.Session hibSession = null;
	private Transaction tx = null;
	private Session iSession = null;
	private SolverGroup iManager = null;
	private Solution iSolution = null;
	
	public TimePattern importTimePattern(Element element) {
		String name = element.attributeValue("name");
		TimePattern timePattern = TimePattern.findByName(iSession, name);
		if (timePattern!=null) return timePattern;
		sLog.info("Creating time pattern "+name);
		timePattern = new TimePattern();
		timePattern.setName(name);
		timePattern.setSession(iSession);
		timePattern.setMinPerMtg(Integer.valueOf(element.attributeValue("minPerMtg")));
		timePattern.setSlotsPerMtg(Integer.valueOf(element.attributeValue("slotsPerMtg")));
		timePattern.setNrMeetings(Integer.valueOf(element.attributeValue("nrMeetings")));
		timePattern.setVisible(new Boolean(element.attributeValue("visible")));
		timePattern.setType(Integer.valueOf(element.attributeValue("type")));
		HashSet days = new HashSet();
		for (Iterator i=element.elementIterator("dayCode");i.hasNext();) {
			TimePatternDays d = new TimePatternDays();
			d.setDayCode(Integer.valueOf(((Element)i.next()).getText()));
			days.add(d);
		}
		HashSet times = new HashSet();
		for (Iterator i=element.elementIterator("startSlot");i.hasNext();) {
			TimePatternTime t = new TimePatternTime();
			t.setStartSlot(Integer.valueOf(((Element)i.next()).getText()));
			times.add(t);
		}
		timePattern.setDays(days);
		timePattern.setTimes(times);
		hibSession.save(timePattern);
		return timePattern;
	}
	
	public DatePattern importDatePattern(Element element) {
		String name = element.attributeValue("name");
		DatePattern datePattern = DatePattern.findByName(iSession, name);
		if (datePattern!=null) return datePattern;
		sLog.info("Creating date pattern "+name);
		datePattern = new DatePattern();
		datePattern.setName(name);
		datePattern.setVisible(new Boolean(element.attributeValue("visible")));
		datePattern.setType(Integer.valueOf(element.attributeValue("type")));
		datePattern.setSession(iSession);
		datePattern.setOffset(Integer.valueOf(element.attributeValue("offset")));
		datePattern.setPattern(element.attributeValue("pattern"));
		hibSession.save(datePattern);
		return datePattern;
	}
	
	public void importPreferences(PreferenceGroup owner, Element element) {
		if (!owner.getPreferences().isEmpty()) {
			sLog.info("  -- preference group "+owner+" already has the following preferences:");
			for (Iterator i=owner.getPreferences().iterator();i.hasNext();) {
				Preference p = (Preference)i.next();
				if (p instanceof TimePref)
					sLog.info("    -- "+p.getPrefLevel().getPrefName()+" "+((TimePref)p).getTimePattern().getName()+" "+((TimePref)p).getPreference());
				else if (p instanceof RoomPref)
					sLog.info("    -- "+p.getPrefLevel().getPrefName()+" "+((RoomPref)p).getRoom().getLabel());
				else if (p instanceof BuildingPref)
					sLog.info("    -- "+p.getPrefLevel().getPrefName()+" "+((BuildingPref)p).getBuilding().getAbbreviation());
				else if (p instanceof RoomFeaturePref)
					sLog.info("    -- "+p.getPrefLevel().getPrefName()+" "+((RoomFeaturePref)p).getRoomFeature().getLabel());
				else if (p instanceof RoomGroupPref)
					sLog.info("    -- "+p.getPrefLevel().getPrefName()+" "+((RoomGroupPref)p).getRoomGroup().getName());
				else
					sLog.info("    -- "+p.getPrefLevel().getPrefName()+" "+p.toString());
			}
			owner.getPreferences().clear();
		}

		for (Iterator i=element.elementIterator("timePref");i.hasNext();) {
			Element el = (Element)i.next();
			PreferenceLevel level = PreferenceLevel.getPreferenceLevel(el.attributeValue("level"));
			TimePattern timePattern = TimePattern.findByName(iSession,el.attributeValue("timePattern"));
			if (timePattern==null) {
				sLog.error("Unable to find time pattern with name "+el.attributeValue("timePattern"));
				continue;
			}
			TimePref tp = new TimePref();
			tp.setOwner(owner);
			tp.setPrefLevel(level);
			tp.setTimePattern(timePattern);
			tp.setPreference(el.attributeValue("preference"));
			hibSession.save(tp);
			sLog.info("  -- added time preference "+tp.getTimePattern().getName()+" "+tp.getPreference());
		}

		for (Iterator i=element.elementIterator("roomPref");i.hasNext();) {
			Element el = (Element)i.next();
			PreferenceLevel level = PreferenceLevel.getPreferenceLevel(el.attributeValue("level"));
			String roomName = el.attributeValue("room"); 
			Location location = null;
			for (Iterator j=owner.getAvailableRooms().iterator();j.hasNext();) {
				Location l = (Location)j.next();
				if (l.getLabel().equals(roomName)) { location = l; break; }
			}
			if (location==null) {
				sLog.error("Unable to find room with name "+roomName);
				continue;
			}
			RoomPref rp = new RoomPref();
			rp.setOwner(owner);
			rp.setPrefLevel(level);
			rp.setRoom(location);
			hibSession.save(rp);
			sLog.info("  -- added room preference "+rp.getPrefLevel().getPrefName()+" "+rp.getRoom().getLabel());
		}

		for (Iterator i=element.elementIterator("buildingPref");i.hasNext();) {
			Element el = (Element)i.next();
			PreferenceLevel level = PreferenceLevel.getPreferenceLevel(el.attributeValue("level"));
			String bldgName = el.attributeValue("building"); 
			Building building = null;
			for (Iterator j=owner.getAvailableBuildings().iterator();j.hasNext();) {
				Building b = (Building)j.next();
				if (b.getAbbreviation().equals(bldgName)) { building = b; break; }
			}
			if (building==null) {
				sLog.error("Unable to find building with name "+bldgName);
				continue;
			}
			BuildingPref bp = new BuildingPref();
			bp.setOwner(owner);
			bp.setPrefLevel(level);
			bp.setBuilding(building);
			hibSession.save(bp);
			sLog.info("  -- added building preference "+bp.getPrefLevel().getPrefName()+" "+bp.getBuilding().getName());
		}

		for (Iterator i=element.elementIterator("roomFeaturePref");i.hasNext();) {
			Element el = (Element)i.next();
			PreferenceLevel level = PreferenceLevel.getPreferenceLevel(el.attributeValue("level"));
			String featureName = el.attributeValue("feature"); 
			RoomFeature feature = null;
			for (Iterator j=owner.getAvailableRoomFeatures().iterator();j.hasNext();) {
				RoomFeature f = (RoomFeature)j.next();
				if (f.getLabel().equals(featureName)) { feature = f; break; }
			}
			if (feature==null) {
				sLog.error("Unable to find room feature with name "+featureName);
				continue;
			}
			RoomFeaturePref fp = new RoomFeaturePref();
			fp.setOwner(owner);
			fp.setPrefLevel(level);
			fp.setRoomFeature(feature);
			hibSession.save(fp);
			sLog.info("  -- added room feature preference "+fp.getPrefLevel().getPrefName()+" "+fp.getRoomFeature().getLabel());
		}

		for (Iterator i=element.elementIterator("roomGroupPref");i.hasNext();) {
			Element el = (Element)i.next();
			PreferenceLevel level = PreferenceLevel.getPreferenceLevel(el.attributeValue("level"));
			String groupName = el.attributeValue("group"); 
			RoomGroup group = null;
			for (Iterator j=owner.getAvailableRoomGroups().iterator();j.hasNext();) {
				RoomGroup g = (RoomGroup)j.next();
				if (g.getName().equals(groupName)) { group = g; break; }
			}
			if (group==null) {
				sLog.error("Unable to find room group with name "+groupName);
				continue;
			}
			RoomGroupPref gp = new RoomGroupPref();
			gp.setOwner(owner);
			gp.setPrefLevel(level);
			gp.setRoomGroup(group);
			hibSession.save(gp);
			sLog.info("  -- added time preference "+gp.getPrefLevel().getPrefName()+" "+gp.getRoomGroup().getName());
		}
	}
	
	public Department getDepartment(String deptCode) {
		if ("LLR".equals(deptCode) || "LAB".equals(deptCode)) {
			return (Department)hibSession.
			createQuery("select d from Department d where d.session.uniqueId=:sessionId and d.externalManager=1 and d.externalMgrAbbv=:deptCode").
			setLong("sessionId", iSession.getUniqueId().longValue()).
			setString("deptCode",deptCode).
			uniqueResult();
		}
		return (Department)hibSession.
		createQuery("select d from Department d where d.session.uniqueId=:sessionId and d.deptCode=:deptCode").
		setLong("sessionId", iSession.getUniqueId().longValue()).
		setString("deptCode",deptCode).
		uniqueResult();
	}

	public Class_ importClass(Element element) {
		String subjectArea = element.attributeValue("subjectArea");
		String courseNbr = element.attributeValue("courseNbr");
		String itype = element.attributeValue("itype");
		int section = Integer.parseInt(element.attributeValue("section"));
		String suffix = element.attributeValue("suffix");
		String notes = element.attributeValue("notes");
		Class_ clazz = (Class_)iAllClasses.get(new ClassHash(element));
		if (clazz==null) {
			sLog.error("Unable to find class "+subjectArea+" "+courseNbr+" "+itype+" "+section+suffix);
			return null;
		}
		sLog.info("Processing class "+clazz.getClassLabel());
		String manager = element.attributeValue("manager");
		if (!clazz.getManagingDept().getDeptCode().equals(manager)) {
			sLog.info("  -- changing managing department to "+manager+" (was "+clazz.getManagingDept().getDeptCode()+")");
			clazz.setManagingDept(getDepartment(manager));
			hibSession.update(clazz);
			hibSession.flush();
			hibSession.refresh(clazz);
		}
		int expectedCapacity = Integer.parseInt(element.attributeValue("expectedCapacity"));
		int maxExpectedCapacity = expectedCapacity;
		int numberOfRooms = Integer.parseInt(element.attributeValue("numberOfRooms"));
		float roomRatio = 1.0f;
		if (element.attributeValue("roomRatio")!=null) {
			roomRatio = Float.parseFloat(element.attributeValue("roomRatio"));
			maxExpectedCapacity = Integer.parseInt(element.attributeValue("maxExpectedCapacity"));
		} else {
			int roomCapacity = Integer.parseInt(element.attributeValue("roomCapacity"));
			if (expectedCapacity==0) {
				roomRatio = 0.0f;
				expectedCapacity = roomCapacity;
			} else {
				roomRatio = ((float)roomCapacity)/expectedCapacity;
			}
		}
		if (clazz.getExpectedCapacity().intValue()!=expectedCapacity) {
			sLog.info("  -- changing min. class limit to "+expectedCapacity+" (was "+clazz.getExpectedCapacity()+")");
			clazz.setExpectedCapacity(new Integer(expectedCapacity));
		}
		if (clazz.getMaxExpectedCapacity().intValue()!=maxExpectedCapacity) {
			sLog.info("  -- changing max. class limit to "+maxExpectedCapacity+" (was "+clazz.getMaxExpectedCapacity()+")");
			clazz.setMaxExpectedCapacity(new Integer(maxExpectedCapacity));
		}
		if (clazz.getNbrRooms().intValue()!=numberOfRooms) {
			sLog.info("  -- changing number of rooms to "+numberOfRooms+" (was "+clazz.getNbrRooms()+")");
			clazz.setNbrRooms(new Integer(numberOfRooms));
		}
		if (clazz.getRoomRatio().floatValue()!=roomRatio) {
			sLog.info("  -- changing room ratio to "+roomRatio+" (was "+clazz.getRoomRatio()+")");
			clazz.setRoomRatio(new Float(roomRatio));
		}
		if (!ToolBox.equals(notes,clazz.getNotes())) {
			sLog.info("  -- changing notes to "+notes+" (was "+clazz.getNotes()+")");
			clazz.setNotes(notes);
		}
		String datePattern = element.attributeValue("datePattern");
		if (datePattern==null && clazz.getDatePattern()!=null) {
			sLog.info("  -- changing date pattern to default (was "+clazz.getDatePattern().getName()+")");
			clazz.setDatePattern(null);
		}
		if (datePattern!=null && (clazz.getDatePattern()==null || !clazz.getDatePattern().getName().equals(datePattern))) {
			sLog.info("  -- changing date pattern to "+datePattern+" (was "+(clazz.getDatePattern()==null?"not set":clazz.getDatePattern().getName())+")");
			DatePattern dp = DatePattern.findByName(clazz.getSession(), datePattern);
			if (dp==null) {
				sLog.error("Unable to find date pattern named '"+datePattern+"'.");
			} else {
				clazz.setDatePattern(dp);
			}
		}
		if (!clazz.getClassInstructors().isEmpty()) {
			sLog.info("  -- class "+clazz.getClassLabel()+" already has the following instructors:");
			for (Iterator i=clazz.getClassInstructors().iterator();i.hasNext();) {
				ClassInstructor ci = (ClassInstructor)i.next();
				sLog.info("    -- "+ci.nameLastNameFirst()+" (lead:"+ci.isLead()+", share:"+ci.getPercentShare()+")");
				hibSession.delete(ci);i.remove();
			}
		}
		for (Iterator i=element.elementIterator("instructor");i.hasNext();) {
			Element el = (Element)i.next();
			String puid = el.attributeValue("puid");
			DepartmentalInstructor instructor = null;
			try {
				instructor = (DepartmentalInstructor)hibSession.
					createQuery("select i from DepartmentalInstructor i where i.puid=:puid and i.department.uniqueId=:deptId").
					setString("puid", puid).
					setLong("deptId", clazz.getControllingDept().getUniqueId().longValue()).
					uniqueResult();
			} catch (NonUniqueResultException e) {
				sLog.error("Two or more instructors with puid "+puid+" (department: "+clazz.getControllingDept().getDeptCode()+")");
				continue;
			}
			if (instructor==null) {
				Staff staff = null;
				try {
					staff = (Staff)hibSession.
						createQuery("select distinct s from Staff s where s.dept=:dept and s.puid=:puid").
						setString("dept", clazz.getControllingDept().getDeptCode()).
						setString("puid", puid).
						uniqueResult();
				} catch (NonUniqueResultException e) {
					sLog.error("Two or more staffs with puid "+puid+" (department: "+clazz.getControllingDept().getDeptCode()+")");
					continue;
				}
				if (staff==null) {
					List staffs = hibSession.
						createQuery("select distinct s from Staff s where s.puid=:puid").
						setString("puid", puid).
						list();
					if (!staffs.isEmpty())
						staff = (Staff)staffs.get(0);
				}
				if (staff!=null) {
					instructor = new DepartmentalInstructor();
					instructor.setExternalUniqueId(staff.getExternalUniqueId());
					instructor.setDepartment(clazz.getControllingDept());
					if (staff.getFirstName()!=null)
						instructor.setFirstName(staff.getFirstName());
					if (staff.getMiddleName()!=null)
						instructor.setMiddleName(staff.getMiddleName());
					if (staff.getLastName()!=null)
						instructor.setLastName(staff.getLastName());
					if (staff.getPositionType()!=null)
						instructor.setPositionType(staff.getPositionType());
					hibSession.save(instructor);
					hibSession.flush();
					hibSession.refresh(instructor);
					sLog.info("  -- instructor "+instructor.nameLastNameFirst()+" created");
				}
			}
			if (instructor==null) {
				sLog.error("Unable to find instructor with puid "+puid+" (department: "+clazz.getControllingDept().getDeptCode()+")");
				continue;
			}
			ClassInstructor ci = new ClassInstructor();
			ci.setInstructor(instructor);
			ci.setLead(new Boolean(el.attributeValue("isLead")));
			ci.setPercentShare(Integer.valueOf(el.attributeValue("percentShare")));
			ci.setInstructor(instructor);
			ci.setClassInstructing(clazz);
			hibSession.save(ci);
			sLog.info("  -- added instructor "+ci.nameLastNameFirst()+" (lead:"+ci.isLead()+", share:"+ci.getPercentShare()+")");
		}
		
		importPreferences(clazz, element);
		
		if (element.attributeValue("assignedDays")!=null) {
			Assignment assignment = new Assignment();
			assignment.setClazz(clazz);
			assignment.setSolution(iSolution);
			assignment.setClassName(clazz.getClassLabel());
			assignment.setDays(Integer.valueOf(element.attributeValue("assignedDays")));
			assignment.setStartSlot(Integer.valueOf(element.attributeValue("assignedSlot")));
			assignment.setTimePattern(TimePattern.findByName(iSession, element.attributeValue("assignedTimePattern")));
			HashSet rooms = new HashSet();
			for (Iterator i=element.element("assignedRooms").elementIterator("room");i.hasNext();) {
				String roomName = (String)((Element)i.next()).attributeValue("name");
				Location location = null;
				for (Iterator j=clazz.getAvailableRooms().iterator();j.hasNext();) {
					Location l = (Location)j.next();
					if (l.getLabel().equals(roomName)) { location = l; break; }
				}
				if (location==null) {
					sLog.error("Unable to find room with name "+roomName);
				} else {
					rooms.add(location);
				}
			}
			assignment.setRooms(rooms);
			hibSession.save(assignment);
			sLog.info("  -- assignment "+assignment.getPlacement().getName());
			
		}
		
		hibSession.update(clazz);
		hibSession.flush();
		hibSession.refresh(clazz);
		return clazz;
	}
	
	public SchedulingSubpart importSchedulingSubpart(Element element) {
		String subjectArea = element.attributeValue("subjectArea");
		String courseNbr = element.attributeValue("courseNbr");
		String itype = element.attributeValue("itype");
		String suffix = element.attributeValue("suffix");
		SchedulingSubpart subpart = (SchedulingSubpart)iAllSubparts.get(new ClassHash(element));
		if (subpart==null) {
			sLog.error("Unable to find scheduling subpart "+subjectArea+" "+courseNbr+" "+itype+" "+(suffix.length()==0?"":" ("+suffix+")"));
			return null;
		}
		sLog.info("Processing subpart "+subjectArea+" "+courseNbr+" "+itype+" "+(suffix.length()==0?"":" ("+suffix+")"));
		String datePattern = element.attributeValue("datePattern");
		if (datePattern==null && subpart.getDatePattern()!=null) {
			sLog.info("  -- changing date pattern to default");
			subpart.setDatePattern(null);
		}
		if (datePattern!=null && (subpart.getDatePattern()==null || !subpart.getDatePattern().getName().equals(datePattern))) {
			sLog.info("  -- changing date pattern to "+datePattern);
			DatePattern dp = DatePattern.findByName(subpart.getSession(), datePattern);
			if (dp==null) {
				sLog.error("Unable to find date pattern named '"+datePattern+"'.");
			} else {
				subpart.setDatePattern(dp);
			}
		}
		
		int minutesPerWk = Integer.parseInt(element.attributeValue("minutesPerWk"));
		if (subpart.getMinutesPerWk().intValue()!=minutesPerWk) {
			sLog.info("  -- changing minutes per meeting to "+minutesPerWk+" (was "+subpart.getMinutesPerWk()+")");
			subpart.setMinutesPerWk(new Integer(minutesPerWk));
		}
		
		importPreferences(subpart, element);

		hibSession.update(subpart);
		hibSession.flush();
		hibSession.refresh(subpart);
		return subpart;
	}
	
	public DepartmentalInstructor importInstructor(Element element) {
		String deptCode = element.attributeValue("deptCode");
		String puid = element.attributeValue("puid");
		DepartmentalInstructor instructor = (DepartmentalInstructor) hibSession.
			createQuery("select id from DepartmentalInstructor id where id.department.deptCode=:deptCode and id.department.sessionId=:sessionId and id.puid=:puid").
			setString("deptCode", deptCode).
			setLong("sessionId", iSession.getUniqueId().longValue()).
			setString("puid",puid).
			uniqueResult();
		if (instructor==null) {
			sLog.error("Unable to find instructor "+puid+" for department "+deptCode);
			return null;
		}
		sLog.info("Processing instructor "+instructor.getLastName()+" (puid:"+puid+")");
		
		importPreferences(instructor, element);

		hibSession.update(instructor);
		hibSession.flush();
		hibSession.refresh(instructor);
		return instructor;
	}
	
	public void clearExistingDistributionPrefs() {
		for (Iterator i=iManager.getDepartments().iterator();i.hasNext();) {
			Department d = (Department)i.next();
			for (Iterator j=d.getPreferences().iterator();j.hasNext();) {
				Preference p = (Preference)j.next();
				if (p instanceof DistributionPref) {
					DistributionPref dp = (DistributionPref)p;
					sLog.info("Removing existing distribution preference "+dp.getPrefLevel().getPrefName()+" "+dp.getDistributionType().getLabel()+" "+dp.getGroupingSufix()+" between");
					for (Iterator k=dp.getDistributionObjects().iterator();k.hasNext();) {
						DistributionObject dobj = (DistributionObject)k.next();
						if (dobj.getPrefGroup() instanceof Class_) {
							sLog.info("  -- class "+((Class_)dobj.getPrefGroup()).getClassLabel());
						} else if (dobj.getPrefGroup() instanceof SchedulingSubpart) {
							SchedulingSubpart s = (SchedulingSubpart)dobj.getPrefGroup();
							sLog.info("  -- scheduling subpart "+s.getCourseName()+" "+s.getItypeDesc()+(s.getSchedulingSubpartSuffix().length()==0?"":" ("+s.getSchedulingSubpartSuffix()+")"));
						}
					}
					hibSession.delete(dp); i.remove();
				}
			}
			hibSession.flush();
			hibSession.refresh(d);
		}
	}
	
	public DistributionPref importDistributionPref(Element element) {
		PreferenceLevel level = PreferenceLevel.getPreferenceLevel(element.attributeValue("level"));
		DistributionType type = (DistributionType)hibSession.
			createQuery("select t from DistributionType t where t.reference=:reference").
			setString("reference",element.attributeValue("type")).uniqueResult();
		if (type==null) {
			sLog.error("Unable to find distribution preference type "+element.attributeValue("type"));
			return null;
		}
		
		sLog.info("Processing distribution preference "+level.getPrefName()+" "+type.getLabel());
		
		DistributionPref distPref = new DistributionPref();
		
		distPref.setDistributionType(type);
		distPref.setPrefLevel(level);
		distPref.setGrouping(Integer.valueOf(element.attributeValue("grouping")));
		if (element.attributeValue("manager")!=null) {
			distPref.setOwner(getDepartment(element.attributeValue("manager")));
		}
		
		Set distObjects = new HashSet();
		
		for (Iterator i=element.elementIterator("class");i.hasNext();) {
			Element x = (Element)i.next();
			String subjectArea = x.attributeValue("subjectArea");
			String courseNbr = x.attributeValue("courseNbr");
			String itype = x.attributeValue("itype");
			int section = Integer.parseInt(x.attributeValue("section"));
			String suffix = x.attributeValue("suffix");
			Class_ clazz = (Class_)iAllClasses.get(new ClassHash(x));
			if (clazz==null) {
				sLog.error("Unable to find class "+subjectArea+" "+courseNbr+" "+itype+" "+section+suffix);
			} else {
				DistributionObject dobj = new DistributionObject();
				dobj.setPrefGroup(clazz);
				dobj.setSequenceNumber(Integer.valueOf(x.attributeValue("sequenceNumber")));
				dobj.setDistributionPref(distPref);
				distObjects.add(dobj);
				sLog.info("  -- added clazz "+clazz.getClassLabel());
				if (element.attributeValue("manager")==null) {
					if (distPref.getOwner()==null) {
						distPref.setOwner(clazz.getManagingDept());
					} else if (((Department)distPref.getOwner()).isExternalManager().booleanValue()) {
						if (!clazz.getManagingDept().isExternalManager().booleanValue())
							distPref.setOwner(clazz.getManagingDept());
					}
				}
			}
		}
			
		for (Iterator i=element.elementIterator("schedulingSubpart");i.hasNext();) {
			Element x = (Element)i.next();
			String subjectArea = x.attributeValue("subjectArea");
			String courseNbr = x.attributeValue("courseNbr");
			String itype = x.attributeValue("itype");
			String suffix = x.attributeValue("suffix");
			SchedulingSubpart subpart = (SchedulingSubpart)iAllSubparts.get(new ClassHash(x));
			if (subpart==null) {
				sLog.error("Unable to find scheduling subpart "+subjectArea+" "+courseNbr+" "+itype+" "+(suffix.length()==0?"":" ("+suffix+")"));
			} else {
				DistributionObject dobj = new DistributionObject();
				dobj.setPrefGroup(subpart);
				dobj.setSequenceNumber(Integer.valueOf(x.attributeValue("sequenceNumber")));
				dobj.setDistributionPref(distPref);
				distObjects.add(dobj);
				sLog.info("  -- added subpart "+subjectArea+" "+courseNbr+" "+itype+" "+(suffix.length()==0?"":" ("+suffix+")"));
				if (element.attributeValue("manager")==null) {
					if (distPref.getOwner()==null) {
						distPref.setOwner(subpart.getManagingDept());
					} else if (((Department)distPref.getOwner()).isExternalManager().booleanValue()) {
						if (!subpart.getManagingDept().isExternalManager().booleanValue())
							distPref.setOwner(subpart.getManagingDept());
					}
				}
			}
		}
		
		if (distObjects.isEmpty()) {
			sLog.error("No distribution objects found for constraint "+level.getPrefName()+" "+type.getLabel());
			return null;
		}

		distPref.setDistributionObjects(distObjects);
		hibSession.save(distPref);
		return distPref;
	}
	
	public void importInstructionalOffering(Element element) {
		InstructionalOffering io = (InstructionalOffering)iAllInstructionalOfferings.get(new ClassHash(element));
		sLog.info("Processing instructional offering "+element.attributeValue("subjectArea")+" "+element.attributeValue("courseNbr"));
		if (io==null) {
			sLog.info("  -- creating new offering");
			HashSet courseOfferings = new HashSet();
			for (Iterator i=element.elementIterator("courseOffering");i.hasNext();) {
				Element x = (Element)i.next();
				SubjectArea sa = (SubjectArea) hibSession.
					createQuery("select sa from SubjectArea sa where sa.subjectAreaAbbreviation=:subjectAreaAbbreviation and sa.sessionId=:sessionId").
					setLong("sessionId", iSession.getUniqueId().longValue()).
					setString("subjectAreaAbbreviation", x.attributeValue("subjectArea")).
					uniqueResult();
				CourseOffering co = (CourseOffering) hibSession.
					createQuery("select co from CourseOffering co where co.subjectArea.uniqueId=:subjectAreaId and co.courseNbr=:courseNbr").
					setInteger("subjectAreaId", sa.getUniqueId().intValue()).
					setString("courseNbr", x.attributeValue("courseNbr")).
					uniqueResult();
				if (co==null) {
					co = new CourseOffering();
					co.setSubjectArea(sa);
					co.setCourseNbr(x.attributeValue("courseNbr"));
				}
				co.setProjectedDemand(Integer.valueOf(x.attributeValue("projectedDemand")));
				co.setDemand(Integer.valueOf(x.attributeValue("demand","0")));
				co.setIsControl(new Boolean(x.attributeValue("isControl")));
				co.setPermId(x.attributeValue("permId"));
				courseOfferings.add(co);
			}
			io = new InstructionalOffering();
		    io.setNotOffered(new Boolean(false));
		    io.setSession(iSession);
		    io.setInstrOfferingPermId(Integer.valueOf(element.attributeValue("instrOfferingPermId")));
		    io.setCourseOfferings(courseOfferings);
		    io.setByReservationOnly(false);
		    for (Iterator i=courseOfferings.iterator();i.hasNext();) {
		    	((CourseOffering)i.next()).setInstructionalOffering(io);
		    }
		    hibSession.save(io);
			hibSession.flush();
			hibSession.refresh(io);
		} else if (io.isNotOffered().booleanValue()) {
			sLog.info("  -- changing not offered offering");
			io.setNotOffered(new Boolean(false));
			hibSession.update(io);
		}
		Hashtable classTable = new Hashtable();
		Iterator i = element.elementIterator("instrOfferingConfig");
		if (io.getInstrOfferingConfigs()!=null && !io.getInstrOfferingConfigs().isEmpty()) {
			for (Iterator j=io.getInstrOfferingConfigs().iterator();i.hasNext() && j.hasNext();) {
				InstrOfferingConfig c = (InstrOfferingConfig)j.next();
				Element x = (Element)i.next();
				int limit = Integer.parseInt(x.attributeValue("limit"));
				if (limit!=c.getLimit().intValue()) {
					sLog.info("  -- changing limit to "+limit+" (was "+c.getLimit()+")");
					c.setLimit(new Integer(limit));
				}
				hibSession.update(c);
				for (Iterator k=x.elementIterator("schedulingSubpart");k.hasNext();) {
					importSchedulingSubpartStructure((Element)k.next(),null,c,classTable);
				}
			}
		}
		while (i.hasNext()) {
			Element x = (Element)i.next();
			InstrOfferingConfig c = new InstrOfferingConfig();
			c.setInstructionalOffering(io);
			c.setLimit(Integer.valueOf(x.attributeValue("limit")));
			c.setUnlimitedEnrollment(Boolean.FALSE);
			if (x.attributeValue("unlimitedEnrollment")!=null)
				c.setUnlimitedEnrollment(new Boolean(x.attributeValue("unlimitedEnrollment")));
			hibSession.save(c);
			hibSession.flush();
			hibSession.refresh(c);
			for (Iterator j=x.elementIterator("schedulingSubpart");j.hasNext();) {
				importSchedulingSubpartStructure((Element)j.next(),null,c,classTable);
			}
		}
	}
	
	public void importSchedulingSubpartStructure(Element element,SchedulingSubpart parent,InstrOfferingConfig cfg,Hashtable classTable) {
		ClassHash subpartHash = new ClassHash(
				cfg.getControllingCourseOffering().getSubjectAreaAbbv(),
				cfg.getControllingCourseOffering().getCourseNbr(),
				element.attributeValue("itype"), -1, element.attributeValue("suffix"));
		SchedulingSubpart subpart = (SchedulingSubpart)iAllSubparts.get(subpartHash);
		if (subpart==null) {
			subpart = new SchedulingSubpart();
			subpart.setItype(
					(ItypeDesc)hibSession.
					createQuery("select i from ItypeDesc i where i.abbv=:abbv").
					setString("abbv", element.attributeValue("itype")).
					uniqueResult());
			subpart.setParentSubpart(parent);
			subpart.setInstrOfferingConfig(cfg);
			subpart.setMinutesPerWk(Integer.valueOf(element.attributeValue("minutesPerWk")));
			hibSession.save(subpart);
			iAllSubparts.put(subpartHash,subpart);
			sLog.info("  -- subpart "+subpartHash+" imported");
			hibSession.flush();
			hibSession.refresh(subpart);
		}
		for (Iterator i=element.elementIterator("class");i.hasNext();) {
			Element x = (Element)i.next();
			ClassHash clazzHash = new ClassHash(
					cfg.getControllingCourseOffering().getSubjectAreaAbbv(),
					cfg.getControllingCourseOffering().getCourseNbr(),
					subpart.getItypeDesc(),
					Integer.parseInt(x.attributeValue("sectionNumber")),
					element.attributeValue("suffix"));
			Class_ clazz = (Class_)iAllClasses.get(clazzHash);
			if (clazz==null) {
				clazz = new Class_();
				clazz.setSchedulingSubpart(subpart);
				if (x.attributeValue("manager")!=null) {
					clazz.setManagingDept(getDepartment(x.attributeValue("manager")));
				}
				int expectedCapacity = Integer.parseInt(x.attributeValue("expectedCapacity"));
				int maxExpectedCapacity = expectedCapacity;
				int numberOfRooms = Integer.parseInt(x.attributeValue("nbrRooms"));
				float roomRatio = 1.0f;
				if (x.attributeValue("roomRatio")!=null) {
					roomRatio = Float.parseFloat(x.attributeValue("roomRatio"));
					maxExpectedCapacity = Integer.parseInt(x.attributeValue("maxExpectedCapacity"));
				} else {
					int roomCapacity = Integer.parseInt(x.attributeValue("roomCapacity"));
					if (expectedCapacity==0) {
						roomRatio = 0.0f;
						expectedCapacity = roomCapacity;
					} else {
						roomRatio = ((float)roomCapacity)/expectedCapacity;
					}
				}
				clazz.setExpectedCapacity(new Integer(expectedCapacity));
				clazz.setMaxExpectedCapacity(new Integer(maxExpectedCapacity));
				clazz.setRoomRatio(new Float(roomRatio));
				clazz.setNbrRooms(new Integer(numberOfRooms));
				clazz.setDisplayInstructor(Boolean.TRUE);
				clazz.setEnabledForStudentScheduling(Boolean.TRUE);
				if (x.attributeValue("parent")!=null)
					clazz.setParentClass((Class_)classTable.get(Long.valueOf(x.attributeValue("parent"))));
				hibSession.save(clazz);
				hibSession.flush();
				hibSession.refresh(clazz);
				sLog.info("    -- class "+clazzHash+" imported");
				iAllClasses.put(clazzHash, clazz);
			}
			classTable.put(Long.valueOf(x.attributeValue("uniqueId")),clazz);
		}
		
		for (Iterator i=element.elementIterator("schedulingSubpart");i.hasNext();)
			importSchedulingSubpartStructure((Element)i.next(),subpart,cfg,classTable);
	}

	public void importAll(File file) throws Exception {
		hibSession = (new SessionDAO()).getSession();
		tx = hibSession.beginTransaction();
		try {
			Document document = (new SAXReader()).read(file);
			Element root = document.getRootElement();
			sLog.info("academicYearTerm:"+root.attributeValue("academicYearTerm"));
			sLog.info("academicInitiative:"+root.attributeValue("academicInitiative"));
			sLog.info("puid:"+root.attributeValue("puid"));
			sLog.info("solverGroupName:"+root.attributeValue("solverGroupName"));
			iSession = (Session) hibSession.
				createQuery("select s from Session s where s.academicYearTerm=:academicYearTerm and s.academicInitiative=:academicInitiative").
				setString("academicYearTerm", root.attributeValue("academicYearTerm")).
				setString("academicInitiative", root.attributeValue("academicInitiative")).
				uniqueResult();
			sLog.info("session:"+iSession);
			
			if (root.attributeValue("solverGroupName")!=null) {
				iManager = SolverGroup.findBySessionIdName(iSession.getUniqueId(),root.attributeValue("solverGroupName"));
			} else {
				String puid = root.attributeValue("puid"); 
				while (puid.length()<10) puid = "0"+puid;
				TimetableManager mgr = (TimetableManager) hibSession.createCriteria(TimetableManager.class).add(Restrictions.eq("puid",puid)).uniqueResult();
				sLog.info("manager:"+mgr);
				for (Iterator i=mgr.getSolverGroups().iterator();i.hasNext();) {
					SolverGroup sg = (SolverGroup)i.next();
					if (!sg.getSession().equals(iSession)) continue;
					if (iManager!=null)
						throw new Exception("Two or more solver groups associated with the manager.");
					iManager = sg;
				}
				if (iManager==null)
					throw new Exception("No solver group associated with the manager.");
			}
			sLog.info("manager:"+iManager);
			
			iAllClasses = new Hashtable();
			for (Iterator i=Class_.findAll(iSession.getUniqueId()).iterator();i.hasNext();) {
				Class_ c = (Class_)i.next();
				if (c.getSchedulingSubpart()!=null && c.getSchedulingSubpart().getControllingCourseOffering()!=null)
					iAllClasses.put(new ClassHash(c),c);
			}
			sLog.info("all classes:"+iAllClasses.size());
			iAllSubparts =  new Hashtable();
			for (Iterator i=SchedulingSubpart.findAll(iSession.getUniqueId()).iterator();i.hasNext();) {
				SchedulingSubpart s = (SchedulingSubpart)i.next(); 
				if (s.getControllingCourseOffering()!=null)
					iAllSubparts.put(new ClassHash(s),s);
			}
			sLog.info("all subparts:"+iAllSubparts.size());
			iAllInstructionalOfferings =  new Hashtable();
			for (Iterator i=InstructionalOffering.findAll(iSession.getUniqueId()).iterator();i.hasNext();) {
				InstructionalOffering io = (InstructionalOffering)i.next(); 
				if (io.getControllingCourseOffering()!=null)
					iAllInstructionalOfferings.put(new ClassHash(io),io);
			}
			sLog.info("all instructional offerings:"+iAllInstructionalOfferings.size());

			iSolution = new Solution();
			iSolution.setCreated(new Date());
			iSolution.setCommitDate(null);
			iSolution.setCommited(Boolean.FALSE);
			iSolution.setCreator("ImportPreferences");
			iSolution.setNote("Imported solution.");
			iSolution.setOwner(iManager);
			iSolution.setValid(Boolean.FALSE);
			hibSession.save(iSolution);
			
			for (Iterator i=root.elementIterator("instructionalOffering");i.hasNext();) {
				importInstructionalOffering((Element)i.next());
			}
			
			for (Iterator i=root.elementIterator("timePattern");i.hasNext();)
				importTimePattern((Element)i.next());
			for (Iterator i=root.elementIterator("datePattern");i.hasNext();)
				importDatePattern((Element)i.next());
			for (Iterator i=root.elementIterator("class");i.hasNext();)
				importClass((Element)i.next());
			for (Iterator i=root.elementIterator("schedulingSubpart");i.hasNext();)
				importSchedulingSubpart((Element)i.next());

			for (Iterator i=root.elementIterator("instructor");i.hasNext();)
				importInstructor((Element)i.next());

			//clearExistingDistributionPrefs();
			
			for (Iterator i=root.elementIterator("distributionPref");i.hasNext();)
				importDistributionPref((Element)i.next());

			tx.commit();
		} catch (Exception e) {
			sLog.error(e.getMessage(),e);
			tx.rollback();
		}
	}

	public static void main(String[] args) {
		// Example arguments: jdbc:oracle:thin:@tamarind.smas.purdue.edu:1521:sms8l c:\\export.xml
		try {
			ToolBox.configureLogging();
			
			HibernateUtil.configureHibernate(args[0]);
			
	        (new ImportPreferences()).importAll(new File(args[1]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class ClassHash {
		String subjectArea = null;
		String courseNbr = null;
		String itype = null;
		int section = -1;
		String suffix = null;
		Integer hashCode = null;
		public ClassHash(String subjectArea, String courseNbr, String itype, int section, String suffix) {
			this.subjectArea = subjectArea;
			this.courseNbr = courseNbr;
			this.itype = itype;
			this.section = section;
			this.suffix = suffix;
		}
		public ClassHash(Element element) {
			subjectArea = element.attributeValue("subjectArea");
			courseNbr = element.attributeValue("courseNbr");
			itype = element.attributeValue("itype");
			section = (element.attributeValue("section")==null?-1:Integer.parseInt(element.attributeValue("section")));
			suffix = element.attributeValue("suffix");
		}
		public ClassHash(Class_ clazz) {
			subjectArea = clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv();
			courseNbr = clazz.getSchedulingSubpart().getControllingCourseOffering().getCourseNbr();
			itype = clazz.getSchedulingSubpart().getItypeDesc();
			section = clazz.getSectionNumber().intValue();
			suffix = clazz.getSchedulingSubpart().getSchedulingSubpartSuffix();
		}
		public ClassHash(SchedulingSubpart subpart) {
			subjectArea = subpart.getControllingCourseOffering().getSubjectAreaAbbv();
			courseNbr = subpart.getControllingCourseOffering().getCourseNbr();
			itype = subpart.getItypeDesc();
			suffix = subpart.getSchedulingSubpartSuffix();
		}
		public ClassHash(InstructionalOffering io) {
			subjectArea = io.getControllingCourseOffering().getSubjectAreaAbbv();
			courseNbr = io.getControllingCourseOffering().getCourseNbr();
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof ClassHash)) return false;
			ClassHash x = (ClassHash)o;
			return 
				ToolBox.equals(subjectArea,x.subjectArea) && 
				ToolBox.equals(courseNbr,x.courseNbr) && 
				ToolBox.equals(itype,x.itype) && 
				section==x.section && 
				ToolBox.equals(suffix,x.suffix);
		}
		public String toString() {
			return subjectArea+" "+courseNbr+" "+(itype==null?"":itype)+" "+(section>=0?""+section:"")+(suffix==null?"":suffix);
		}
		public int hashCode() {
			if (hashCode==null) {
				hashCode = new Integer(toString().hashCode());
			}
			return hashCode.intValue();
		}
	}
}
