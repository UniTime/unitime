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
package org.unitime.timetable.dataexchange;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Element;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class PreferencesImport  extends BaseImport {
	private Session iSession;
	private SimpleDateFormat iDateFormat, iTimeFormat;
	private DateFormat iHHmm = new SimpleDateFormat("HHmm", Locale.US);
	
    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("preferences")) {
        	throw new Exception("Given XML file is not reservations load file.");
        }
        try {
            beginTransaction();

            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            String created = root.attributeValue("created");
            iDateFormat = new SimpleDateFormat(root.attributeValue("dateFormat", "yyyy/M/d"), Locale.US);
	        String timeFormat = root.attributeValue("timeFormat", "HHmm");
	        if (!"HHmm".equals(timeFormat))
	        	iTimeFormat = new SimpleDateFormat(timeFormat, Locale.US);
            iSession = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if (iSession == null)
                throw new Exception("No session found for the given campus, year, and term.");

            if (created != null) {
                ChangeLog.addChange(getHibSession(), getManager(), iSession, iSession, created, ChangeLog.Source.DATA_IMPORT_PREFERENCES, ChangeLog.Operation.UPDATE, null, null);
            }
            
            for (Iterator i = root.elementIterator(); i.hasNext(); ) {
            	Element element = (Element)i.next();
            	PreferenceGroup group = lookupPrefGroup(element);
            	if (group == null) continue;
            	group.getPreferences().clear();
            	for (Iterator j = element.elementIterator(); j.hasNext(); ) {
                	Element prefElement = (Element)j.next();
                	Preference preference = createPreference(prefElement, group);
                	if (preference == null) continue;
                	preference.setOwner(group);
                	group.getPreferences().add(preference);
                }
            	getHibSession().saveOrUpdate(group);
            }
            
        	info("All done.");
        	
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
    
    private List<Department> iDepartments = null;
    protected Department lookupDepartment(Element element) {
    	if (iDepartments == null)
    		iDepartments = (List<Department>)getHibSession().createQuery(
	        		"select distinct d from Department d left join fetch d.preferences p where d.session.uniqueId = :sessionId")
	        		.setLong("sessionId", iSession.getUniqueId()).list();
    	String deptCode = element.attributeValue("code", "not-set");
    	for (Iterator<Department> i = iDepartments.iterator(); i.hasNext(); ) {
    		Department department = i.next();
    		if (deptCode.equals(department.getDeptCode())) { return department; }
    	}
    	warn("Department " + deptCode + " does not exist.");
    	return null;
    }
    
    private List<DepartmentalInstructor> iInstructors = null;
    protected DepartmentalInstructor lookupInstructor(Element element) {
    	if (iInstructors == null)
    		iInstructors = (List<DepartmentalInstructor>)getHibSession().createQuery(
	        		"select distinct i from DepartmentalInstructor i left join fetch i.preferences p where i.department.session.uniqueId = :sessionId " +
	        		"order by i.department.deptCode, i.lastName, i.firstName").setLong("sessionId", iSession.getUniqueId()).list();
    	String externalId = element.attributeValue("externalId");
    	String deptCode = element.attributeValue("department", "not-set");
    	if (externalId != null) {
        	for (Iterator<DepartmentalInstructor> i = iInstructors.iterator(); i.hasNext(); ) {
        		DepartmentalInstructor instructor = i.next();
        		if (externalId.equals(instructor.getExternalUniqueId()) && deptCode.equals(instructor.getDepartment().getDeptCode())) { return instructor; }
        	}
        	warn("Instructor " + externalId + " does not exist (department " + deptCode + ").");
        	return null;
    	}
    	String fname = element.attributeValue("firstName");
    	String mname = element.attributeValue("middleName");
    	String lname = element.attributeValue("lastName");
    	if (lname != null) {
    		for (Iterator<DepartmentalInstructor> i = iInstructors.iterator(); i.hasNext(); ) {
        		DepartmentalInstructor instructor = i.next();
        		if (lname.equals(instructor.getLastName()) && (fname == null || fname.equals(instructor.getFirstName())) &&
        			(mname == null || mname.equals(instructor.getMiddleName())) && deptCode.equals(instructor.getDepartment().getDeptCode())) {
        				return instructor;
        			}
        	}
        	warn("Instructor " + (fname == null ? "" : fname + " ") + (mname == null ? "" : mname + " ") + lname + " does not exist (department " + deptCode + ").");
        	return null;
    	}
    	return null;
    }
    
    protected DepartmentalInstructor lookupInstructor(String externalId, String deptCode) {
    	if (iInstructors == null)
    		iInstructors = (List<DepartmentalInstructor>)getHibSession().createQuery(
	        		"select distinct i from DepartmentalInstructor i left join fetch i.preferences p where i.department.session.uniqueId = :sessionId " +
	        		"order by i.department.deptCode, i.lastName, i.firstName").setLong("sessionId", iSession.getUniqueId()).list();
    	for (Iterator<DepartmentalInstructor> i = iInstructors.iterator(); i.hasNext(); ) {
    		DepartmentalInstructor instructor = i.next();
    		if (externalId.equals(instructor.getExternalUniqueId()) && deptCode.equals(instructor.getDepartment().getDeptCode())) { return instructor; }
    		if (instructor.getExternalUniqueId() == null && externalId.equals(instructor.getName("first-middle-last")) && deptCode.equals(instructor.getDepartment().getDeptCode())) { return instructor; }
    	}
    	warn("Instructor " + externalId + " does not exist (department " + deptCode + ").");
    	return null;
    }
    
    private Map<String, DatePattern> iDatePatterns = null;
    protected DatePattern lookupDatePattern(String name) {
    	if (iDatePatterns == null) {
    		iDatePatterns = new HashMap<String, DatePattern>();
    		for (DatePattern pattern: (List<DatePattern>)getHibSession().createQuery(
    				"from DatePattern where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			iDatePatterns.put(pattern.getName(), pattern);
    	}
    	return iDatePatterns.get(name);
    }
    
    private List<SchedulingSubpart> iSubparts = null;
    protected SchedulingSubpart lookupSubpart(Element element) {
    	if (iSubparts == null) {
    		iSubparts = (List<SchedulingSubpart>)getHibSession().createQuery(
	        		"select distinct ss from SchedulingSubpart ss " +
	        		"left join fetch ss.instrOfferingConfig as ioc " +
	        		"left join fetch ioc.instructionalOffering as io " +
	        		"left join fetch io.courseOfferings as co " +
	        		"left join fetch ss.classes c " +
	        		"left join fetch ss.preferences sp " +
	        		"left join fetch c.preferences cp " +
	        		"where ss.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId and co.isControl = true " +
	        		"order by co.subjectAreaAbbv, co.courseNbr, ioc.uniqueId, ss.uniqueId"
	        		).setLong("sessionId", iSession.getUniqueId()).list();
    		iClasses = new ArrayList<Class_>();
    		for (SchedulingSubpart subpart: iSubparts)
    			iClasses.addAll(subpart.getClasses());
    	}
    	String subject = element.attributeValue("subject", "not-set");
    	String course = element.attributeValue("course", "not-set");
    	String type = element.attributeValue("type", "not-set");
    	String config = element.attributeValue("config");
    	String suffix = element.attributeValue("suffix");
    	for (Iterator<SchedulingSubpart> i = iSubparts.iterator(); i.hasNext(); ) {
    		SchedulingSubpart subpart = i.next();
    		if (!type.equals(subpart.getItypeDesc().trim())) continue;
    		if (suffix == null && !subpart.getSchedulingSubpartSuffix().isEmpty()) continue;
    		if (suffix != null && !suffix.equals(subpart.getSchedulingSubpartSuffix())) continue;
    		if (config != null && !config.equals(subpart.getInstrOfferingConfig().getName())) continue;
    		for (CourseOffering co: subpart.getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
    			if (subject.equals(co.getSubjectAreaAbbv()) && course.equals(co.getCourseNbr())) {
    				return subpart;
    			}
    		}
    	}
    	warn("Scheduling subpart " + subject + " " + course + " " + type + (suffix == null ? "" : " " + suffix) + (config == null ? " (" + config + ")" : "") + " not found.");
    	return null;
    }
    
    private List<Class_> iClasses = null;
    protected Class_ lookupClass(Element element) {
    	if (iClasses == null) {
    		iSubparts = (List<SchedulingSubpart>)getHibSession().createQuery(
	        		"select distinct ss from SchedulingSubpart ss " +
	        		"left join fetch ss.instrOfferingConfig as ioc " +
	        		"left join fetch ioc.instructionalOffering as io " +
	        		"left join fetch io.courseOfferings as co " +
	        		"left join fetch ss.classes c " +
	        		"left join fetch ss.preferences sp " +
	        		"left join fetch c.preferences cp " +
	        		"where ss.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId and co.isControl = true " +
	        		"order by co.subjectAreaAbbv, co.courseNbr, ioc.uniqueId, ss.uniqueId"
	        		).setLong("sessionId", iSession.getUniqueId()).list();
    		iClasses = new ArrayList<Class_>();
    		for (SchedulingSubpart subpart: iSubparts)
    			iClasses.addAll(subpart.getClasses());
    	}
    	String subject = element.attributeValue("subject");
    	String course = element.attributeValue("course");
    	String type = element.attributeValue("type");
    	String suffix = element.attributeValue("suffix");
    	String externalId = element.attributeValue("externalId");
    	for (Iterator<Class_> i = iClasses.iterator(); i.hasNext(); ) {
    		Class_ clazz = i.next();
    		if (type != null && !type.equals(clazz.getSchedulingSubpart().getItypeDesc().trim())) continue;
    		if (suffix != null && !suffix.equals(getClassSuffix(clazz))) continue;
    		for (CourseOffering co: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
    			if (externalId != null && !externalId.equals(clazz.getExternalId(co))) continue;
    			if (subject != null && !subject.equals(co.getSubjectAreaAbbv())) continue;
    			if (course != null && !course.equals(co.getCourseNbr())) continue;
    			return clazz;
    		}
    	}
    	warn("Class " + (subject == null ? "" : subject + " ") + (course == null ? "" : course + " ") + (type == null ? "" : type + " ") + 
    			(suffix == null ? "" : suffix + " ") + (externalId == null ? "" : externalId + " ") + " not found.");
    	return null;
    }
    
    protected PreferenceGroup lookupPrefGroup(Element element) {
    	if ("class".equals(element.getName())) {
    		Class_ clazz = lookupClass(element);
    		if (clazz == null) return null;
        	String pattern = element.attributeValue("pattern");
    		clazz.setDatePattern(lookupDatePattern(pattern));
    		return clazz;
    	}
    	if ("subpart".equals(element.getName())) {
    		SchedulingSubpart subpart = lookupSubpart(element);
    		if (subpart == null) return null;
        	String pattern = element.attributeValue("pattern");
    		subpart.setDatePattern(lookupDatePattern(pattern));
    		return subpart;
    	}
    	if ("department".equals(element.getName()))
    		return lookupDepartment(element);
    	if ("instructor".equals(element.getName())) {
    		DepartmentalInstructor instructor = lookupInstructor(element);
    		if (instructor == null) return null;
    		Element tp = element.element("teachingPref");
    		instructor.setMaxLoad(tp == null ? null : Float.parseFloat(tp.attributeValue("maxLoad", "0.0")));
    		instructor.setTeachingPreference(tp == null ? null : PreferenceLevel.getPreferenceLevel(tp.attributeValue("level", PreferenceLevel.sProhibited)));
    		return instructor;
    	}
    	return null;
    }
    
    private Map<String, TimePattern> iTimePatterns = null;
    protected TimePattern lookupTimePattern(String name) {
    	if (iTimePatterns == null) {
    		iTimePatterns = new HashMap<String, TimePattern>();
    		for (TimePattern pattern: (List<TimePattern>)getHibSession().createQuery(
    				"from TimePattern where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			iTimePatterns.put(pattern.getName(), pattern);
    	}
    	TimePattern tp = iTimePatterns.get(name);
    	if (tp == null) warn("Time pattern " + name + " does not exist.");
    	return tp;
    }
    
    private Map<String, Building> iBuildings = null;
    protected Building lookupBuilding(String abbv) {
    	if (iBuildings == null) {
    		iBuildings = new HashMap<String, Building>();
    		for (Building building: (List<Building>)getHibSession().createQuery(
    				"from Building where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			iBuildings.put(building.getAbbreviation(), building);
    	}
    	Building b = iBuildings.get(abbv);
    	if (b == null) warn("Building " + abbv + " does not exist.");
    	return b;
    }
    
    private Map<String, Room> iRooms = null;
    protected Room lookupRoom(String buildingAbbv, String roomNbr) {
    	if (iRooms == null) {
    		iRooms = new HashMap<String, Room>();
    		for (Room room: (List<Room>)getHibSession().createQuery(
    				"from Room where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			iRooms.put(room.getBuildingAbbv() + " " + room.getRoomNumber(), room);
    	}
    	Room r = iRooms.get(buildingAbbv + " " + roomNbr);
    	if (r == null) warn("Room " + buildingAbbv + " " + roomNbr + " does not exist.");
    	return r;
    }
    
    private Map<String, Location> iLocations = null;
    protected Location lookupLocation(String label, String deptCode) {
    	if (iLocations == null) {
    		iLocations = new HashMap<String, Location>();
    		for (Location location: (List<Location>)getHibSession().createQuery(
    				"from NonUniversityLocation where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			for (RoomDept rd: location.getRoomDepts()) {
    				iLocations.put(location.getLabel() + "|" + rd.getDepartment().getDeptCode(), location);
    			}
    	}
    	Location l = iLocations.get(label + "|" + deptCode);
    	if (l == null) warn("Location " + label + " (" + deptCode + ") does not exist.");
    	return l;
    }
    
    private Map<String, RoomGroup> iRoomGroups = null;
    protected RoomGroup lookupRoomGroup(String name, String deptCode) {
    	if (iRoomGroups == null) {
    		iRoomGroups = new HashMap<String, RoomGroup>();
    		for (RoomGroup group: (List<RoomGroup>)getHibSession().createQuery(
    				"from RoomGroup where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			if (group.isGlobal())
    				iRoomGroups.put(group.getAbbv(), group);
    			else
    				iRoomGroups.put(group.getAbbv() + "|" + group.getDepartment().getDeptCode(), group);
    	}
    	RoomGroup g = iRoomGroups.get(name);
    	if (g == null) g = iRoomGroups.get(name + "|" + deptCode);
    	if (g == null) warn("Room Group " + name + " (" + deptCode + ") does not exist.");
    	return g;
    }
    
    private Map<String, RoomFeature> iRoomFeatures = null;
    protected RoomFeature lookupRoomFeature(String name, String deptCode) {
    	if (iRoomFeatures == null) {
    		iRoomFeatures = new HashMap<String, RoomFeature>();
    		for (GlobalRoomFeature feature: (List<GlobalRoomFeature>)getHibSession().createQuery(
    				"from GlobalRoomFeature where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			iRoomFeatures.put(feature.getAbbv(), feature);
    		for (DepartmentRoomFeature feature: (List<DepartmentRoomFeature>)getHibSession().createQuery(
    				"from DepartmentRoomFeature where department.session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			iRoomFeatures.put(feature.getAbbv() + "|" + feature.getDepartment().getDeptCode(), feature);
    	}
    	RoomFeature f = iRoomFeatures.get(name);
    	if (f == null) f = iRoomFeatures.get(name + "|" + deptCode);
    	if (f == null) warn("Room Feature " + name + " (" + deptCode + ") does not exist.");
    	return f;
    }
    
    private Map<String, DistributionType> iDistributionTypes = null;
    protected DistributionType lookupDistributionType(String name) {
    	if (iDistributionTypes == null) {
    		iDistributionTypes = new HashMap<String, DistributionType>();
    		for (DistributionType type: (List<DistributionType>)getHibSession().createQuery(
    				"from DistributionType").list())
    			iDistributionTypes.put(type.getReference(), type);
    	}
    	DistributionType type = iDistributionTypes.get(name);
    	if (type == null) warn("Distribution Type " + name + " does not exist.");
    	return type;
    }
    
    private Map<String, CourseOffering> iCourseOfferings = null;
    protected CourseOffering lookupCourse(String subjectAbbv, String courseNbr) {
    	if (iCourseOfferings == null) {
    		iCourseOfferings = new HashMap<String, CourseOffering>();
    		for (CourseOffering course: (List<CourseOffering>)getHibSession().createQuery(
    				"from CourseOffering co where co.instructionalOffering.session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			iCourseOfferings.put(course.getSubjectAreaAbbv() + "|" + course.getCourseNbr(), course);
    	}
    	CourseOffering course = iCourseOfferings.get(subjectAbbv + "|" + courseNbr);
    	if (course == null) warn("Course Offering " + subjectAbbv + " " + courseNbr + " does not exist.");
    	return course;
    }
    
    private Map<String, InstructorAttribute> iInstructorAttributes = null;
    protected InstructorAttribute lookupAttribute(String name, String deptCode) {
    	if (iInstructorAttributes == null) {
    		iInstructorAttributes = new HashMap<String, InstructorAttribute>();
    		for (InstructorAttribute attribute: (List<InstructorAttribute>)getHibSession().createQuery(
    				"from InstructorAttribute where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list())
    			if (attribute.getDepartment() == null)
    				iInstructorAttributes.put(attribute.getCode(), attribute);
    			else
    				iInstructorAttributes.put(attribute.getCode() + "|" + attribute.getDepartment().getDeptCode(), attribute);
    	}
    	InstructorAttribute attribute = iInstructorAttributes.get(name);
    	if (attribute == null) attribute = iInstructorAttributes.get(name + "|" + deptCode);
    	if (attribute == null) warn("Instructor Attribute " + name + " (" + deptCode + ") does not exist.");
    	return attribute;
    }
    
    private Map<String, ExamPeriod> iExamPeriods = null;
    protected ExamPeriod lookupExamPeriod(String date, String start, String type) {
    	if (iExamPeriods == null) {
    		iExamPeriods = new HashMap<String, ExamPeriod>();
    		for (ExamPeriod period: (List<ExamPeriod>)getHibSession().createQuery(
    				"from ExamPeriod where session.uniqueId = :sessionId").setLong("sessionId", iSession.getUniqueId()).list()) {
    			iExamPeriods.put(iDateFormat.format(period.getStartDate()) + "|" + iTimeFormat.format(period.getStartTime()), period);
    			iExamPeriods.put(iDateFormat.format(period.getStartDate()) + "|" + iTimeFormat.format(period.getStartTime()) + "|" + period.getExamType().getReference(), period);
    		}
    	}
    	ExamPeriod period = iExamPeriods.get(date + "|" + start + "|" + type);
    	if (period == null) period = iExamPeriods.get(date + "|" + start);
    	if (period == null) warn("Examination Period " + date + " " + start + " (" + type + ") does not exist.");
    	return period;
    }
    
	protected Integer parseTime(String timeString) throws NumberFormatException, ParseException {
		if (timeString == null || timeString.isEmpty()) return 0;
		int time = Integer.parseInt(iTimeFormat == null ? timeString : iHHmm.format(iTimeFormat.parse(timeString)));
		int hour = time / 100;
		int min = time % 100;
		return (60 * hour + min - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
	}
	
    protected Preference createPreference(Element element, PreferenceGroup group) throws NumberFormatException, ParseException {
    	Department ctrDept = group.getDepartment();
    	Department mngDept = ctrDept;
    	if (group instanceof Class_)
    		mngDept = ((Class_)group).getManagingDept();
    	if (group instanceof SchedulingSubpart)
    		mngDept = ((SchedulingSubpart)group).getManagingDept();
    	
    	if ("datePref".equals(element.getName())) {
    		DatePattern pattern = lookupDatePattern(element.attributeValue("pattern", "not-set"));
    		if (pattern == null) {
    	    	warn("Date pattern " + element.attributeValue("pattern", "not-set") + " does not exist.");
    	    	return null;
    		}
    		DatePatternPref pref = new DatePatternPref();
    		pref.setDatePattern(pattern);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		return pref;
    	}
    	if ("timePref".equals(element.getName())) {
    		TimePref pref = new TimePref();
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		String pattern = element.attributeValue("pattern");
    		if (pattern != null) {
    			TimePattern tp = lookupTimePattern(pattern);
    			if (tp == null) return null;
    			pref.setTimePattern(tp);
    		}
    		TimePatternModel model = pref.getTimePatternModel();
    		for (Iterator j = element.elementIterator("pref"); j.hasNext(); ) {
            	Element pe = (Element)j.next();
            	int dayCode = DayCode.toInt(DayCode.toDayCodes(pe.attributeValue("days", pe.attributeValue("day", ""))));
        		int startSlot = parseTime(pe.attributeValue("time", pe.attributeValue("start")));
        		String level = pe.attributeValue("level", PreferenceLevel.sNeutral);
            	if (model.isExactTime()) {
            		model.setExactDays(dayCode);
        			model.setExactStartSlot(startSlot);
        			break;
            	} else {
            		int endSlot = startSlot + model.getSlotsPerMtg();
            		if (pe.attributeValue("stop") != null)
            			endSlot = parseTime(pe.attributeValue("stop"));
            		if (pe.attributeValue("end") != null)
            			endSlot = parseTime(pe.attributeValue("end"));
            		for (int d = 0; d < model.getNrDays(); d++)
            			if (model.getDayCode(d) == dayCode)
            				for (int t = 0; t < model.getNrTimes(); t++)
            					if (model.getStartSlot(t) >= startSlot && model.getStartSlot(t) + model.getSlotsPerMtg() <= endSlot)
            						model.setPreference(d, t, level);
            	}
    		}
    		pref.setPreference(model.getPreferences());
    		return pref;
    	}
    	if ("buildingPref".equals(element.getName())) {
    		Building building = lookupBuilding(element.attributeValue("building", "not-set"));
    		if (building == null) return null;
    		BuildingPref pref = new BuildingPref();
    		pref.setBuilding(building);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		return pref;
    	}
    	if ("roomPref".equals(element.getName())) {
    		String buildingAbbv = element.attributeValue("building");
    		String roomNbr = element.attributeValue("room");
    		String label = element.attributeValue("location");
    		if (buildingAbbv != null && roomNbr != null) {
    			Room room = lookupRoom(buildingAbbv, roomNbr);
    			if (room == null) return null;
    			RoomPref pref = new RoomPref();
    			pref.setRoom(room);
    			pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
        		return pref;
    		} else if (label != null) {
    			Location location = lookupLocation(label, element.attributeValue("department", mngDept.getDeptCode()));
    			if (location == null) return null;
    			RoomPref pref = new RoomPref();
    			pref.setRoom(location);
    			pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
        		return pref;
    		} else {
    			return null;
    		}
    	}
    	if ("groupPref".equals(element.getName())) {
    		RoomGroup rg = lookupRoomGroup(element.attributeValue("group", "not-set"), element.attributeValue("department", mngDept.getDeptCode()));
    		if (rg == null) return null;
    		RoomGroupPref pref = new RoomGroupPref();
    		pref.setRoomGroup(rg);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		return pref;
    	}
    	if ("featurePref".equals(element.getName())) {
    		RoomFeature feature = lookupRoomFeature(element.attributeValue("feature", "not-set"), element.attributeValue("department", mngDept.getDeptCode()));
    		if (feature == null) return null;
    		RoomFeaturePref pref = new RoomFeaturePref();
    		pref.setRoomFeature(feature);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		return pref;
    	}
    	if ("distributionPref".equals(element.getName())) {
    		DistributionType type = lookupDistributionType(element.attributeValue("type", "not-set"));
    		if (type == null) return null;
    		DistributionPref pref = new DistributionPref();
    		pref.setDistributionType(type);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		pref.setStructure(DistributionPref.Structure.valueOf(element.attributeValue("structure", DistributionPref.Structure.values()[Integer.parseInt(element.attributeValue("grouping", "0"))].name())));
    		pref.setDistributionObjects(new HashSet<DistributionObject>());
    		for (Iterator j = element.elementIterator(); j.hasNext(); ) {
            	Element de = (Element)j.next();
            	if ("subpart".equals(de.getName())) {
            		SchedulingSubpart subpart = lookupSubpart(de);
            		if (subpart != null) {
            			DistributionObject obj = new DistributionObject();
            			obj.setDistributionPref(pref);
            			obj.setPrefGroup(subpart);
            			obj.setSequenceNumber(1 + pref.getDistributionObjects().size());
            			pref.getDistributionObjects().add(obj);
            		}
            	} else if ("class".equals(de.getName())) {
            		Class_ clazz = lookupClass(de);
            		if (clazz != null) {
            			DistributionObject obj = new DistributionObject();
            			obj.setDistributionPref(pref);
            			obj.setPrefGroup(clazz);
            			obj.setSequenceNumber(1 + pref.getDistributionObjects().size());
            			pref.getDistributionObjects().add(obj);
            		}
            	}
    		}
    		return pref;
    	}
    	if ("instructorPref".equals(element.getName())) {
    		DepartmentalInstructor instructor = lookupInstructor(element.attributeValue("instructor", "not-set"), element.attributeValue("department", ctrDept.getDeptCode()));
    		if (instructor == null) return null;
    		InstructorPref pref = new InstructorPref();
    		pref.setInstructor(instructor);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		return pref;
    	}
    	if ("coursePref".equals(element.getName())) {
    		CourseOffering course = lookupCourse(element.attributeValue("subject", "not-set"), element.attributeValue("course", "not-set"));
    		if (course == null) return null;
    		InstructorCoursePref pref = new InstructorCoursePref();
    		pref.setCourse(course);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		return pref;
    	}
    	if ("attributePref".equals(element.getName())) {
    		InstructorAttribute attribute = lookupAttribute(element.attributeValue("attribute", "not-set"), element.attributeValue("department", ctrDept.getDeptCode()));
    		if (attribute == null) return null;
    		InstructorAttributePref pref = new InstructorAttributePref();
    		pref.setAttribute(attribute);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		return pref;
    	}
    	if ("periodPref".equals(element.getName())) {
    		ExamPeriod period = lookupExamPeriod(element.attributeValue("date", "not-set"), element.attributeValue("start", "not-set"), element.attributeValue("type", "not-set"));
    		if (period == null) return null;
    		ExamPeriodPref pref = new ExamPeriodPref();
    		pref.setExamPeriod(period);
    		pref.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("level", PreferenceLevel.sRequired)));
    		return pref;
    	}
    	return null;
    }
    
}
