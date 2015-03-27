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
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.dom4j.Element;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.AssignmentInfo;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.duration.DurationModel;

/**
 * @author Tomas Muller
 */
public class CourseTimetableImport extends BaseImport {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	private DateFormat iDateFormat = null;
	private DateFormat iTimeFormat = null;
	private DateFormat iHHmm = new SimpleDateFormat("HHmm", Locale.US);
	private HashMap<String, Class_> iExtId2class, iName2class;
	private HashMap<Long, Solution> iOwnerId2solution;
	private HashMap<String, Room> iExtId2room, iName2room;
	private HashMap<String, Location> iExtId2location, iName2location;
	private HashMap<String, DatePattern> iName2dp;
	private List<DatePattern> iDatePatterns;
	private List<TimePattern> iTimePatterns;
	private HashMap<String, TimePattern> iName2tp;
	private Date iToday;
	private Session iSession;
	private boolean iInstructors, iPreferExtId, iNotes;
	
	public CourseTimetableImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
        if (!rootElement.getName().equalsIgnoreCase("timetable"))
        	throw new Exception("Given XML file is not a course timetable import file.");
        
		try {
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");
	        String action = rootElement.attributeValue("action");
	        iInstructors = "true".equalsIgnoreCase(rootElement.attributeValue("instructors", "false"));
	        iNotes = "true".equalsIgnoreCase(rootElement.attributeValue("notes", "false"));
	        iPreferExtId = "id".equalsIgnoreCase(rootElement.attributeValue("prefer", "id"));
	        iDateFormat = new SimpleDateFormat(rootElement.attributeValue("dateFormat", "yyyy/M/d"), Locale.US);
	        String timeFormat = rootElement.attributeValue("timeFormat", "HHmm");
	        if (!"HHmm".equals(timeFormat))
	        	iTimeFormat = new SimpleDateFormat(timeFormat, Locale.US);
	        		
	        beginTransaction();
	        
	        iSession = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        
	        if (iSession == null)
	           	throw new Exception("No session found for the given campus, year, and term.");

	    	iExtId2class = new HashMap<String, Class_>();
	    	iName2class = new HashMap<String, Class_>();

	    	info("Loading classes...");
	 		for (Object[] o: (List<Object[]>)getHibSession().createQuery(
	 				"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
    				"c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
    				.setLong("sessionId", iSession.getUniqueId()).list()) {
	 			Class_ clazz = (Class_)o[0];
	 			CourseOffering course = (CourseOffering)o[1];
				String extId = clazz.getExternalId(course);
				if (extId != null && !extId.isEmpty())
					iExtId2class.put(extId, clazz);
				String name = clazz.getClassLabel(course);
				iName2class.put(name, clazz);
			}
	 		
	 		iOwnerId2solution = new HashMap<Long, Solution>();
	 		
	 		if ("update".equals(action)) {
	 			info("Loading solutions...");
	 			for (Solution solution: (List<Solution>)getHibSession().createQuery(
	 					"select s from Solution s where s.commited = true and s.owner.session.uniqueId = :sessionId")
	 					.setLong("sessionId", iSession.getUniqueId()).list()) {
	 				iOwnerId2solution.put(solution.getOwner().getUniqueId(), solution);
	 			}
	 		}
	 		
	 		info("Loading rooms...");
	 		iExtId2room = new HashMap<String, Room>();
	 		iName2room = new HashMap<String, Room>();
	 		for (Room room: (List<Room>)getHibSession().createQuery(
	 				"select r from Room r where r.session.uniqueId = :sessionId")
	 				.setLong("sessionId", iSession.getUniqueId()).list()) {
	 			if (room.getExternalUniqueId() != null && !room.getExternalUniqueId().isEmpty())
	 				iExtId2room.put(room.getExternalUniqueId(), room);
	 			iName2room.put(room.getLabel(), room);
	 		}
	 		
	 		iExtId2location = new HashMap<String, Location>();
	 		iName2location = new HashMap<String, Location>();
	 		for (Location location: (List<Location>)getHibSession().createQuery(
	 				"select r from Location r where r.session.uniqueId = :sessionId")
	 				.setLong("sessionId", iSession.getUniqueId()).list()) {
	 			if (location.getExternalUniqueId() != null && !location.getExternalUniqueId().isEmpty())
	 				iExtId2location.put(location.getExternalUniqueId(), location);
	 			iName2location.put(location.getLabel(), location);
	 		}
	 		
	 		iName2dp = new HashMap<String, DatePattern>();
	 		iDatePatterns = DatePattern.findAll(iSession.getUniqueId(), true, null, null);
	 		for (DatePattern dp: iDatePatterns)
	 			iName2dp.put(dp.getName(), dp);
	 		
	 		iTimePatterns = TimePattern.findAll(iSession.getUniqueId(), true);
	 		iName2tp = new HashMap<String, TimePattern>();
	 		for (TimePattern tp: iTimePatterns)
	 			iName2tp.put(tp.getName(), tp);
	 		
    		Calendar cal = Calendar.getInstance(Locale.US);
    		cal.set(Calendar.HOUR_OF_DAY, 0);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MILLISECOND, 0);
    		iToday = cal.getTime();
	        
	        info("Importing assignments...");
 	        for (Iterator i = rootElement.elementIterator("class"); i.hasNext(); )
	            importClassAssignment((Element) i.next());
 	        
 	        info("Committing new solutions...");
 	        for (Solution solution: iOwnerId2solution.values()) {
 	        	if (solution.isCommited()) continue;
 	        	info("Committing solution for " + solution.getOwner().getName());
 	        	List<String> messages = new ArrayList<String>();
 	        	if (!solution.commitSolution(messages, getHibSession(), null)) {
 	        		error("Failed to commit solution for " + solution.getOwner().getName() + ", see the following problems:");
 	        		for (String message: messages) warn(message);
 	        	}
 	        }
 	        
            commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
	}
	
	protected void importClassAssignment(Element classElement) {
		for (Iterator i = classElement.elementIterator("class"); i.hasNext(); )
			importClassAssignment((Element) i.next());
		
		Class_ clazz = lookupClass(classElement);
		
		if (clazz == null) return;
        
        if (iInstructors)
        	updateInstructors(clazz, classElement);
        
        if (iNotes) {
        	clazz.setSchedulePrintNote(classElement.attributeValue("scheduleNote"));
        	clazz.setDisplayInstructor("true".equalsIgnoreCase(classElement.attributeValue("displayInScheduleBook", clazz.isDisplayInstructor() ? "true" : "false")));
        	clazz.setEnabledForStudentScheduling("true".equalsIgnoreCase(classElement.attributeValue("studentScheduling", clazz.isEnabledForStudentScheduling() ? "true" : "false")));
        }
        
        Department department = clazz.getManagingDept();
        if (department == null) department = clazz.getSchedulingSubpart().getControllingDept();
        
        SolverGroup sg = department.getSolverGroup();
        if (sg == null) {
        	sg = new SolverGroup();
            sg.setDepartments(new HashSet()); sg.getDepartments().add(department); department.setSolverGroup(sg);
            sg.setAbbv(department.getAbbreviation()==null?department.getDeptCode():department.getAbbreviation());
            sg.setName(department.getName());
            sg.setSession(iSession);
            sg.setTimetableManagers(new HashSet(department.getTimetableManagers()));
            sg.setSolutions(new HashSet());
            getHibSession().save(sg);
            getHibSession().update(department);
        }
        
        Solution solution = iOwnerId2solution.get(sg.getUniqueId());
        if (solution == null) {
            solution = new Solution();
            solution.setCreated(new Date());
            solution.setCreator("MakeAssignmentsForClassEvents");
            solution.setOwner(sg); sg.getSolutions().add(solution);
            solution.setValid(true);
            solution.setAssignments(new HashSet());
            solution.setCommited(false);
            solution.setCommitDate(null);
            getHibSession().save(solution);
            getHibSession().update(sg);
            iOwnerId2solution.put(sg.getUniqueId(), solution);
        }
        
        Assignment assignment = null;
		for (Assignment a: solution.getAssignments())
			if (a.getClazz().equals(clazz)) assignment = a;
		
		DatePattern datePattern = null;
		TimePattern timePattern = null;
		Integer dayCode = null, startSlot = null;
		String datePatternName = null;
		for (Iterator j = classElement.elementIterator("time"); j.hasNext(); ) {
			Element timeElement = (Element)j.next();
			try {
				startSlot = parseTime(timeElement.attributeValue("startTime"));
			} catch (Exception e) {
				warn("Failed to parse start time " + timeElement.attributeValue("startTime") + " for " + clazz.getClassLabel());
				return;
			}
			dayCode = (dayCode == null ? 0 : dayCode) | parseDaysOfWeek(timeElement.attributeValue("days"));
			if (timeElement.attributeValue("datePattern") != null) {
				datePatternName = timeElement.attributeValue("datePattern");
				datePattern = iName2dp.get(timeElement.attributeValue("datePattern"));
			}
			if (timeElement.attributeValue("timePattern") != null)
				timePattern = iName2tp.get(timeElement.attributeValue("timePattern"));
		}
		
		if (dayCode == null) {
			if (assignment != null) {
				if (assignment.getSolution().isCommited()) {
    				ClassEvent event = clazz.getEvent();
    				if (event != null) {
    	            	if (ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue()) {
    	            		getHibSession().delete(event);
    	            	} else {
    	            		for (Iterator<Meeting> j = event.getMeetings().iterator(); j.hasNext(); )
    	                		if (!j.next().getMeetingDate().before(iToday)) j.remove();
    	                	if (event.getMeetings().isEmpty()) {
    	                		getHibSession().delete(event);
    	                	} else {
    	                		getHibSession().saveOrUpdate(event);
    	                	}
    	            	}
    	            }
    				clazz.setCommittedAssignment(null);
				}
	            
				assignment.getSolution().getAssignments().remove(assignment);
				clazz.getAssignments().remove(assignment);
				
				for (ConstraintInfo ci: assignment.getConstraintInfo()) {
	            	for (Assignment a: ci.getAssignments()) {
	            		if (!a.equals(assignment)) {
	            			a.getConstraintInfo().remove(ci);
	            		}
	            	}
	            	getHibSession().delete(ci);
	            }
	            
	        	getHibSession().delete(assignment);
			}
        	
			info(clazz.getClassLabel() + " := arrange hours");
        	return;
		}
		
		Set<Location> locations = getLocations(classElement);
		if (locations.size() != clazz.getNbrRooms()) {
			warn("Changed number of rooms for " + clazz.getClassLabel() + " to " + locations.size());
			clazz.setNbrRooms(locations.size());
		}
		
		DatePattern classDatePattern = clazz.effectiveDatePattern();
		if (classDatePattern != null) {
			if (classDatePattern.getType() == DatePattern.sTypePatternSet) {
				for (DatePattern d: iDatePatterns) {
					if (d.getParents().contains(classDatePattern) && match(d, classElement, dayCode)) {
						datePattern = classDatePattern;
						break;
					}
				}
			} else {
				if (match(classDatePattern, classElement, dayCode))
					datePattern = classDatePattern;
			}
		}
		
		if (datePattern == null) {
			BitSet pattern = parseDatePattern(classElement, iSession);
			if (pattern.length() > 0) {
				for (DatePattern dp: iDatePatterns) {
					if (dp.getPatternBitSet().equals(pattern)) {
						datePattern = dp; break;
					}
				}
				if (datePattern == null) {
					info("No date pattern found for " + clazz.getClassLabel() + " -- creating a new one.");
					datePattern = new DatePattern();
					datePattern.setName(datePatternName == null ? "import - " + clazz.getClassLabel() : datePatternName);
					datePattern.setSession(iSession);
					datePattern.setPatternBitSet(pattern);
					datePattern.setType(new Integer(3));
					datePattern.setVisible(false);
					datePattern.setParents(new HashSet<DatePattern>());
					getHibSession().saveOrUpdate(datePattern);
					iName2dp.put(datePattern.getName(), datePattern);
				}
			} else {
				datePattern = iSession.getDefaultDatePattern();
			}
		}
		
		if (datePattern == null) {
			warn("No date pattern found for " + clazz.getClassLabel());
			return;
		}
		
		if (classDatePattern == null || (!datePattern.equals(classDatePattern) && !datePattern.getParents().contains(classDatePattern))) {
			clazz.setDatePattern(datePattern);
			info("Changing date pattern for " + clazz.getClassLabel() + " (" + datePattern.getName() + ")");
		}
		
		for (TimePref tp: (Set<TimePref>)clazz.getEffectiveTimePreferences()) {
			TimePatternModel m = tp.getTimePatternModel();
			if (m.isExactTime()) {
				if (m.getExactDays() == dayCode && m.getExactStartSlot() == startSlot) {
					timePattern = tp.getTimePattern(); break;
				}
			} else {
				d: for (int d = 0; d < m.getNrDays(); d++)
					if (m.getDayCode(d) == dayCode)
						for (int t = 0; t < m.getNrTimes(); t++)
							if (m.getStartSlot(t) == startSlot) {
								timePattern = tp.getTimePattern(); break d;
							}
			}
		}
		
		DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
		if (timePattern == null) {
			TimePatternModel model = null;
			tp: for (TimePattern tp: iTimePatterns) {
				if (tp.getType() == TimePattern.sTypeExactTime || dm.isValidCombination(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, tp)) {
					TimePatternModel m = tp.getTimePatternModel();
					if (m.isExactTime()) {
						m.setExactDays(dayCode); m.setExactStartSlot(startSlot);
						model = m;
						timePattern = tp;
					} else {
	    				for (int d = 0; d < m.getNrDays(); d++)
	    					if (m.getDayCode(d) == dayCode)
	    						for (int t = 0; t < m.getNrTimes(); t++)
	    							if (m.getStartSlot(t) == startSlot) {
	    								m.setPreference(d, t, PreferenceLevel.sStronglyPreferred);
	    								model = m;
	    								timePattern = tp;
	    								break tp;
	    							}
						
					}
				}
			}
			if (model != null) {
				TimePref tp = new TimePref();
				tp.setOwner(clazz);
				tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				tp.setTimePatternModel(model);
				clazz.getPreferences().add(tp);
				info("Time pattern " + timePattern.getName() + " added to " + clazz.getClassLabel());
			}
		}
		
		if (timePattern == null) {
			warn("No time pattern found for " + clazz.getClassLabel());
			return;
		}
		
		if (assignment != null) {
			assignment.setDatePattern(datePattern);
			assignment.setTimePattern(timePattern);
            assignment.setSlotsPerMtg(-1);
            assignment.setBreakTime(-1);
			assignment.setDays(dayCode);
			assignment.setStartSlot(startSlot);
			assignment.setRooms(locations);
			assignment.setInstructors(new HashSet<DepartmentalInstructor>());
			if (solution.isCommited()) {
	            Event event = assignment.generateCommittedEvent(clazz.getEvent(), true);
	            if (event != null) {
	            	if (!event.getMeetings().isEmpty()) {
	            		getHibSession().saveOrUpdate(event);
	            	} else if (event.getMeetings().isEmpty() && event.getUniqueId() != null)
	            		getHibSession().delete(event);
	            }
			}
		} else {
			assignment = new Assignment();
			assignment.setDatePattern(datePattern);
			assignment.setTimePattern(timePattern);
            assignment.setSlotsPerMtg(-1);
            assignment.setBreakTime(-1);
			assignment.setDays(dayCode);
			assignment.setStartSlot(startSlot);
			assignment.setRooms(locations);
			assignment.setAssignmentInfo(new HashSet<AssignmentInfo>());
			assignment.setInstructors(new HashSet<DepartmentalInstructor>());
			assignment.setSolution(solution);
			assignment.setClazz(clazz);
			assignment.setClassId(clazz.getUniqueId());
			assignment.setClassName(clazz.getClassLabel());
			solution.getAssignments().add(assignment);
			clazz.getAssignments().add(assignment);
		}
		
        for (ClassInstructor instr: clazz.getClassInstructors()) 
        	if (instr.isLead()) assignment.getInstructors().add(instr.getInstructor());
        
        info(clazz.getClassLabel() + " := " + assignment.getPlacement().getLongName(CONSTANTS.useAmPm()));
		
        getHibSession().saveOrUpdate(assignment);
        getHibSession().saveOrUpdate(clazz);
	}
	
	protected Class_ lookupClass(Element classElement) {
		String externalId = classElement.attributeValue("id", classElement.attributeValue("externalId"));
        Class_ clazz = null;
        if (externalId != null) {
        	clazz = iExtId2class.get(externalId);
        	if (clazz == null)
        		clazz = iName2class.get(externalId);
        }
        
        String className = null;
        if (classElement.attributeValue("name") != null) {
        	className = classElement.attributeValue("name");
        } else if (classElement.attributeValue("subject") != null) {
        	className = classElement.attributeValue("subject") + " " +
        		classElement.attributeValue("courseNbr") + " " +
        		classElement.attributeValue("type") + " " +
        		classElement.attributeValue("suffix");
        }
        
        if (className != null) {
        	if (clazz == null) {
        		clazz = iName2class.get(className);
        	} else {
        		Class_ clazzByName = iName2class.get(className);
        		if (clazzByName != null && !clazzByName.equals(clazz)) {
        			warn("Class " + className + " has a different external id " + (clazz.getExternalUniqueId() + " != " + externalId) + ".");
        			if (!iPreferExtId) clazz = clazzByName;
        		}
        	}
        }
        
        if (clazz == null)
        	warn("Class " + (className != null ? className + (externalId == null ? "" : " (" + externalId + ")") : externalId) + " not found.");
        
        return clazz;
	}
	
	protected Set<Location> getLocations(Element classElement) {
		Set<Location> locations = new HashSet<Location>();
		for (Iterator j = classElement.elementIterator("room"); j.hasNext(); ) {
			Element roomElement = (Element)j.next();
			String id = roomElement.attributeValue("id", roomElement.attributeValue("externalId"));
			Room room = null;
			if (id != null) {
				room = iExtId2room.get(id);
				if (room == null) room = iName2room.get(id);
			}
			String name = null;
			if (roomElement.attributeValue("name") != null) {
				name = roomElement.attributeValue("name");
			} else if (roomElement.attributeValue("building") != null && roomElement.attributeValue("roomNbr") != null) {
				name = roomElement.attributeValue("building") + " " + roomElement.attributeValue("roomNbr");
			}
			if (name != null) {
				if (room == null) {
					room = iName2room.get(name);
				} else {
					Room roomByName = iName2room.get(name);
					if (roomByName != null && !roomByName.equals(room)) {
						warn("Room " + roomByName + " has a different external id " + (roomByName.getExternalUniqueId() + " != " + id) + ".");
            			if (!iPreferExtId) room = roomByName;
					}
				}
			}
			if (room == null)
				warn("Room " + (name != null ? name + (id == null ? "" : " (" + id + ")") : id) + " not found.");
			else
				locations.add(room);
		}
		for (Iterator j = classElement.elementIterator("location"); j.hasNext(); ) {
			Element locationElement = (Element)j.next();
			String id = locationElement.attributeValue("id", locationElement.attributeValue("externalId"));
			Location location = null;
			if (id != null) {
				location = iExtId2location.get(id);
				if (location == null) location = iName2location.get(id);
			}
			String name = null;
			if (locationElement.attributeValue("name") != null) {
				name = locationElement.attributeValue("name");
			} else if (locationElement.attributeValue("building") != null && locationElement.attributeValue("roomNbr") != null) {
				name = locationElement.attributeValue("building") + " " + locationElement.attributeValue("roomNbr");
			}
			if (name != null) {
				if (location == null) {
					location = iName2location.get(name);
				} else {
					Location locationByName = iName2location.get(name);
					if (locationByName != null && !locationByName.equals(location)) {
						warn("Location " + locationByName + " has a different external id " + (locationByName.getExternalUniqueId() + " != " + id) + ".");
            			if (!iPreferExtId) location = locationByName;
					}
				}
			}
			if (location == null)
				warn("Location " + (name != null ? name + (id == null ? "" : " (" + id + ")") : id) + " not found.");
			else
				locations.add(location);
		}
		return locations;
	}
	
	protected int parseDaysOfWeek(String daysOfWeek){
		int ret = 0;
		String tmpDays = daysOfWeek;
		if(tmpDays.contains("Th")){
			ret |= Constants.DAY_CODES[Constants.DAY_THU];
			tmpDays = tmpDays.replace("Th", "..");
		}
		if(tmpDays.contains("R")){
			ret |= Constants.DAY_CODES[Constants.DAY_THU];
			tmpDays = tmpDays.replace("R", "..");
		}
		if (tmpDays.contains("Su")){
			ret |= Constants.DAY_CODES[Constants.DAY_SUN];
			tmpDays = tmpDays.replace("Su", "..");
		}
		if (tmpDays.contains("U")){
			ret |= Constants.DAY_CODES[Constants.DAY_SUN];
			tmpDays = tmpDays.replace("U", "..");
		}
		if (tmpDays.contains("M")){
			ret |= Constants.DAY_CODES[Constants.DAY_MON];
			tmpDays = tmpDays.replace("M", ".");
		}
		if (tmpDays.contains("T")){
			ret |= Constants.DAY_CODES[Constants.DAY_TUE];
			tmpDays = tmpDays.replace("T", ".");
		}
		if (tmpDays.contains("W")){
			ret |= Constants.DAY_CODES[Constants.DAY_WED];
			tmpDays = tmpDays.replace("W", ".");
		}
		if (tmpDays.contains("F")){
			ret |= Constants.DAY_CODES[Constants.DAY_FRI];
			tmpDays = tmpDays.replace("F", ".");
		}
		if (tmpDays.contains("S")){
			ret |= Constants.DAY_CODES[Constants.DAY_SAT];
			tmpDays = tmpDays.replace("S", ".");
		}
		return ret;
	}
	
	protected Integer parseTime(String timeString) throws Exception {
		int time = Integer.parseInt(iTimeFormat == null ? timeString : iHHmm.format(iTimeFormat.parse(timeString)));
		int hour = time / 100;
		int min = time % 100;
		return (60 * hour + min - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
	}
	
	protected BitSet parseDatePattern(Element classElement, Session session) {
		int startMonth = session.getPatternStartMonth();
		int endMonth = session.getPatternEndMonth();
		int year = session.getSessionStartYear();
		BitSet pattern = new BitSet(); int index = 0;
		String endDate = null;
		for (int m = startMonth; m <= endMonth; m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			for (int d = 1; d <= daysOfMonth; d++) {
				String date = iDateFormat.format(DateUtils.getDate(d, m, year));
				if (endDate == null) {
					for (Iterator i = classElement.elementIterator("date"); i.hasNext(); ) {
						Element dateElement = (Element)i.next();
						String startDate = dateElement.attributeValue("startDate");
						try {
							startDate = iDateFormat.format(iDateFormat.parse(startDate));
						} catch (ParseException e) {}
						if (date.equals(startDate)) {
							endDate = dateElement.attributeValue("endDate");
							try {
								endDate = iDateFormat.format(iDateFormat.parse(endDate));
							} catch (ParseException e) {}
						}
					}
				}
				pattern.set(index++, endDate != null);
				if (date.equals(endDate)) endDate = null;
			}
		}
		return pattern;
	}
	
	protected boolean match(DatePattern datePattern, Element classElement, int dayCode) {
		BitSet pattern = datePattern.getPatternBitSet();
		int startMonth = datePattern.getSession().getPatternStartMonth();
		int endMonth = datePattern.getSession().getPatternEndMonth();
		int year = datePattern.getSession().getSessionStartYear();
		int index = 0;
		String endDate = null;
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(DateUtils.getDate(1, startMonth, year)); cal.setLenient(true);
		for (int m = startMonth; m <= endMonth; m++) {
			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
			for (int d = 1; d <= daysOfMonth; d++) {
				String date = iDateFormat.format(cal.getTime());
				if (endDate == null) {
					for (Iterator i = classElement.elementIterator("date"); i.hasNext(); ) {
						Element dateElement = (Element)i.next();
						String startDate = dateElement.attributeValue("startDate");
						try {
							startDate = iDateFormat.format(iDateFormat.parse(startDate));
						} catch (ParseException e) {}
						if (date.equals(startDate)) {
							endDate = dateElement.attributeValue("endDate");
							try {
								endDate = iDateFormat.format(iDateFormat.parse(endDate));
							} catch (ParseException e) {}
						}
					}
				}
                boolean offered = false;
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_MON]) != 0); break;
                    case Calendar.TUESDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_TUE]) != 0); break;
                    case Calendar.WEDNESDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_WED]) != 0); break;
                    case Calendar.THURSDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_THU]) != 0); break;
                    case Calendar.FRIDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_FRI]) != 0); break;
                    case Calendar.SATURDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0); break;
                    case Calendar.SUNDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0); break;
                }
                if (offered && pattern.get(index) != (endDate != null))
                	return false;
				if (date.equals(endDate)) endDate = null;
				index ++; cal.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		return true;
	}
	
	protected void updateInstructors(Class_ clazz, Element classElement) {
		List<ClassInstructor> instructors = new ArrayList<ClassInstructor>(clazz.getClassInstructors());

        for (Iterator i = classElement.elementIterator("instructor"); i.hasNext(); ) {
			Element instructorElement = (Element)i.next();
			ClassInstructor instructor = null;
			for (ClassInstructor ci: instructors) {
				DepartmentalInstructor di = ci.getInstructor();
				if (di.getExternalUniqueId() != null && di.getExternalUniqueId().equals(instructorElement.attributeValue("id"))) {
					instructor = ci; break;
				}
				String name = (di.getFirstName() == null ? "" : di.getFirstName()) + ":" + (di.getMiddleName() == null ? "" : di.getMiddleName()) + ":" + (di.getLastName() == null ? "" : di.getLastName());
				if (name.equalsIgnoreCase(instructorElement.attributeValue("fname", "") + ":" + instructorElement.attributeValue("mname", "") + ":" + instructorElement.attributeValue("lname", ""))) {
					instructor = ci; break;
				}
			}
			if (instructor != null) {
				instructors.remove(instructor);
				instructor.setLead("true".equals(instructorElement.attributeValue("lead", instructor.isLead() ? "true" : "false")));
				instructor.setPercentShare(Integer.valueOf(instructorElement.attributeValue("share", instructor.getPercentShare() == null ? "100" : instructor.getPercentShare().toString())));
				continue;
			}
			String id = instructorElement.attributeValue("id");
			if (id == null) continue;
			DepartmentalInstructor di = findDepartmentalInstructorWithExternalUniqueId(id, clazz.getControllingDept());
			if (di == null) {
				di = new DepartmentalInstructor();
				di.setExternalUniqueId(id);
				di.setDepartment(clazz.getControllingDept());
				Staff staff = findStaffMember(id);
				if (staff == null) {
					di.setFirstName(instructorElement.attributeValue("fname", "Unkwnown"));
					di.setMiddleName(instructorElement.attributeValue("mname", null));
					di.setLastName(instructorElement.attributeValue("lname", "Instructor"));
					di.setAcademicTitle(instructorElement.attributeValue("title", null));
				} else {
					di.setFirstName(staff.getFirstName());
					di.setMiddleName(staff.getMiddleName());
					di.setLastName(staff.getLastName());
					di.setAcademicTitle(staff.getAcademicTitle());
				}
				di.setIgnoreToFar(false);
				getHibSession().save(di);
			}
			instructor = new ClassInstructor();
			instructor.setClassInstructing(clazz);
			instructor.setInstructor(di);
			instructor.setLead("true".equals(instructorElement.attributeValue("lead", "true")));
			instructor.setPercentShare(Integer.valueOf(instructorElement.attributeValue("share", "100")));				
			clazz.addToclassInstructors(instructor);
			di.addToclasses(instructor);
			getHibSession().saveOrUpdate(instructor);
			getHibSession().saveOrUpdate(di);
        }
        
        for (ClassInstructor instructor: instructors) {
        	DepartmentalInstructor di = instructor.getInstructor(); 
        	di.getClasses().remove(instructor);
        	instructor.getClassInstructing().getClassInstructors().remove(instructor);
        	getHibSession().delete(instructor);
        	getHibSession().saveOrUpdate(di);
        }
	}
	
	protected DepartmentalInstructor findDepartmentalInstructorWithExternalUniqueId(String externalId, Department department) {		
		return (DepartmentalInstructor)getHibSession().createQuery(
				"select distinct di from DepartmentalInstructor di where di.externalUniqueId=:externalId and di.department.uniqueId=:departmentId")
				.setString("externalId", externalId)
				.setLong("departmentId", department.getUniqueId())
				.setMaxResults(1).setCacheable(true).uniqueResult();
	}
	
	protected Staff findStaffMember(String id) {
		return (Staff)getHibSession().createQuery(
				"select distinct s from Staff s where s.externalUniqueId=:externalId")
				.setString("externalId", id)
				.setMaxResults(1).setCacheable(true).uniqueResult();
	}
}
