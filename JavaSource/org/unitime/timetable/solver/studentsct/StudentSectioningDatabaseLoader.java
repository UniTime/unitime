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
package org.unitime.timetable.solver.studentsct;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.studentsct.StudentSectioningLoader;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.AcademicAreaCode;
import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.DummyReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.solver.TimetableDatabaseLoader;
import org.unitime.timetable.solver.curricula.LastLikeStudentCourseDemands;
import org.unitime.timetable.solver.curricula.ProjectedStudentCourseDemands;
import org.unitime.timetable.solver.curricula.StudentCourseDemands;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.WeightedStudentId;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;


/**
 * @author Tomas Muller
 */
public class StudentSectioningDatabaseLoader extends StudentSectioningLoader {
    private static Log sLog = LogFactory.getLog(StudentSectioningDatabaseLoader.class);
    private boolean iIncludeCourseDemands = true;
    private boolean iIncludeUseCommittedAssignments = false;
    private boolean iMakeupAssignmentsFromRequiredPrefs = false;
    private boolean iLoadStudentInfo = true;
    private String iInitiative = null;
    private String iTerm = null;
    private String iYear = null;
    private Long iSessionId = null;
    private long iMakeupAssignmentId = 0;
	private BitSet iFreeTimePattern = null;
	private Date iDatePatternFirstDate = null;
	private boolean iTweakLimits = false;
	private boolean iLoadSectioningInfos = false;
	private boolean iProjections = false;
	private boolean iFixWeights = true;
	private boolean iCheckForNoBatchStatus = true;
    
    private Progress iProgress = null;
    
    private StudentCourseDemands iStudentCourseDemands = null;
    
    public StudentSectioningDatabaseLoader(StudentSectioningModel model, org.cpsolver.ifs.assignment.Assignment<Request, Enrollment> assignment) {
        super(model, assignment);
        iIncludeCourseDemands = model.getProperties().getPropertyBoolean("Load.IncludeCourseDemands", iIncludeCourseDemands);
        iIncludeUseCommittedAssignments = model.getProperties().getPropertyBoolean("Load.IncludeUseCommittedAssignments", iIncludeUseCommittedAssignments);
        iLoadStudentInfo = model.getProperties().getPropertyBoolean("Load.LoadStudentInfo", iLoadStudentInfo);
        iMakeupAssignmentsFromRequiredPrefs = model.getProperties().getPropertyBoolean("Load.MakeupAssignmentsFromRequiredPrefs", iMakeupAssignmentsFromRequiredPrefs);
        iInitiative = model.getProperties().getProperty("Data.Initiative");
        iYear = model.getProperties().getProperty("Data.Year");
        iTerm = model.getProperties().getProperty("Data.Term");
        iSessionId = model.getProperties().getPropertyLong("General.SessionId", null);
        iTweakLimits = model.getProperties().getPropertyBoolean("Load.TweakLimits", iTweakLimits);
        iLoadSectioningInfos = model.getProperties().getPropertyBoolean("Load.LoadSectioningInfos",iLoadSectioningInfos);
        iProgress = Progress.getInstance(getModel());
        iFixWeights = model.getProperties().getPropertyBoolean("Load.FixWeights", iFixWeights);
        iCheckForNoBatchStatus = model.getProperties().getPropertyBoolean("Load.CheckForNoBatchStatus", iCheckForNoBatchStatus);
        
        try {
        	String studentCourseDemandsClassName = getModel().getProperties().getProperty("StudentSct.ProjectedCourseDemadsClass", LastLikeStudentCourseDemands.class.getName());
        	if (studentCourseDemandsClassName.indexOf(' ') >= 0) studentCourseDemandsClassName = studentCourseDemandsClassName.replace(" ", "");
        	if (studentCourseDemandsClassName.indexOf('.') < 0) studentCourseDemandsClassName = "org.unitime.timetable.solver.curricula." + studentCourseDemandsClassName;
            Class studentCourseDemandsClass = Class.forName(studentCourseDemandsClassName);
            iStudentCourseDemands = (StudentCourseDemands)studentCourseDemandsClass.getConstructor(DataProperties.class).newInstance(getModel().getProperties());
            iProgress.info("Projected demands: " + getModel().getProperties().getProperty("StudentSct.ProjectedCourseDemadsClass", LastLikeStudentCourseDemands.class.getName()));
        } catch (Exception e) {
        	if (model.getProperties().getPropertyBoolean("Load.IncludeLastLikeStudents", false)) {
        		iStudentCourseDemands = new ProjectedStudentCourseDemands(model.getProperties());
            	iProgress.info("Projected demands: Projected Student Course Demands");
        	} else {
        		iProgress.info("Projected demands: None");
        	}
        }

        iProjections = "Projection".equals(model.getProperties().getProperty("StudentSctBasic.Mode", "Initial"));
    }
    
    public void load() {
        iProgress.setStatus("Loading input data ...");
        org.hibernate.Session hibSession = null;
        Transaction tx = null;
        try {
            hibSession = SessionDAO.getInstance().getSession();
            hibSession.setCacheMode(CacheMode.IGNORE);
            hibSession.setFlushMode(FlushMode.MANUAL);
            
            tx = hibSession.beginTransaction(); 

            Session session = null;
            if (iSessionId!=null) {
                session = SessionDAO.getInstance().get(iSessionId);
                if (session!=null) {
                    iYear = session.getAcademicYear();
                    iTerm = session.getAcademicTerm();
                    iInitiative = session.getAcademicInitiative();
                    getModel().getProperties().setProperty("Data.Year", iYear);
                    getModel().getProperties().setProperty("Data.Term", iTerm);
                    getModel().getProperties().setProperty("Data.Initiative", iInitiative);
                }
            } else {
                session = Session.getSessionUsingInitiativeYearTerm(iInitiative, iYear, iTerm);
                if (session!=null) {
                    iSessionId = session.getUniqueId();
                    getModel().getProperties().setProperty("General.SessionId", String.valueOf(iSessionId));
                }
            }
            
            if (session==null) throw new Exception("Session "+iInitiative+" "+iTerm+iYear+" not found!");
            
            iProgress.info("Loading data for "+iInitiative+" "+iTerm+iYear+"...");
            
            if (getModel().getDistanceConflict() != null)
            	TravelTime.populateTravelTimes(getModel().getDistanceConflict().getDistanceMetric(), iSessionId, hibSession);
            
            load(session, hibSession);
            
            tx.commit();
        } catch (Exception e) {
            iProgress.fatal("Unable to load sectioning problem, reason: "+e.getMessage(),e);
            sLog.error(e.getMessage(),e);
            tx.rollback();
        } finally {
            // here we need to close the session since this code may run in a separate thread
            if (hibSession!=null && hibSession.isOpen()) hibSession.close();
        }
    }
    
    private String getInstructorIds(Class_ clazz) {
        if (!clazz.isDisplayInstructor().booleanValue()) return null;
        String ret = null;
        TreeSet ts = new TreeSet(clazz.getClassInstructors());
        for (Iterator i=ts.iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (!ci.isLead().booleanValue()) continue;
            if (ret==null)
                ret = ci.getInstructor().getUniqueId().toString();
            else
                ret += ":"+ci.getInstructor().getUniqueId().toString();
        }
        return ret;
    }
    
    private String getInstructorNames(Class_ clazz) {
        if (!clazz.isDisplayInstructor().booleanValue()) return null;
        String ret = null;
        TreeSet ts = new TreeSet(clazz.getClassInstructors());
        for (Iterator i=ts.iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (!ci.isLead().booleanValue()) continue;
            if (ret==null)
                ret = ci.getInstructor().nameShort();
            else
                ret += ":"+ci.getInstructor().nameShort();
        }
        return ret;
    }
    
    public TimeLocation makeupTime(Class_ c) {
        DatePattern datePattern = c.effectiveDatePattern(); 
        if (datePattern==null) {
            iProgress.warn("        -- makup time for "+c.getClassLabel()+": no date pattern set");
            return null;
        }        
        for (Iterator i=c.getEffectiveTimePreferences().iterator();i.hasNext();) {
            TimePref tp = (TimePref)i.next();
            TimePatternModel pattern = tp.getTimePatternModel();
            if (pattern.isExactTime()) {
                int length = ExactTimeMins.getNrSlotsPerMtg(pattern.getExactDays(),c.getSchedulingSubpart().getMinutesPerWk().intValue());
                int breakTime = ExactTimeMins.getBreakTime(pattern.getExactDays(),c.getSchedulingSubpart().getMinutesPerWk().intValue()); 
                return new TimeLocation(pattern.getExactDays(),pattern.getExactStartSlot(),length,PreferenceLevel.sIntLevelNeutral,0,datePattern.getUniqueId(),datePattern.getName(),datePattern.getPatternBitSet(),breakTime);
            } else {
                for (int time=0;time<pattern.getNrTimes(); time++) {
                    for (int day=0;day<pattern.getNrDays(); day++) {
                        String pref = pattern.getPreference(day,time);
                        if (pref.equals(PreferenceLevel.sRequired)) {
                        return new TimeLocation(
                                pattern.getDayCode(day),
                                pattern.getStartSlot(time),
                                pattern.getSlotsPerMtg(),
                                PreferenceLevel.prolog2int(pattern.getPreference(day, time)),
                                pattern.getNormalizedPreference(day,time,0.77),
                                datePattern.getUniqueId(),
                                datePattern.getName(),
                                datePattern.getPatternBitSet(),
                                pattern.getBreakTime());
                        }
                    }
                }
            }
        }
        if (c.getEffectiveTimePreferences().isEmpty())
            iProgress.warn("        -- makup time for "+c.getClassLabel()+": no time preference set");
        else
            iProgress.warn("        -- makup time for "+c.getClassLabel()+": no required time set");
        return null;
    }
    
    public Vector makeupRooms(Class_ c) {
        Vector rooms = new Vector();
        for (Iterator i=c.getEffectiveRoomPreferences().iterator();i.hasNext();) {
            RoomPref rp = (RoomPref)i.next();
            if (!PreferenceLevel.sRequired.equals(rp.getPrefLevel().getPrefProlog())) {
                iProgress.warn("        -- makup room for "+c.getClassLabel()+": preference for "+rp.getRoom().getLabel()+" is not required");
                continue;
            }
            Location room = (Location)rp.getRoom();
            RoomLocation roomLocation = new RoomLocation(
                    room.getUniqueId(),
                    room.getLabel(),
                    (room instanceof Room? ((Room)room).getBuilding().getUniqueId() : null),
                    0,
                    room.getCapacity().intValue(),
                    room.getCoordinateX(),
                    room.getCoordinateY(),
                    room.isIgnoreTooFar().booleanValue(),
                    null);
            rooms.addElement(roomLocation);
        }        
        return rooms;
    }
    
    public Placement makeupPlacement(Class_ c) {
        TimeLocation time = makeupTime(c);
        if (time==null) return null;
        Vector rooms = makeupRooms(c);
        Vector times = new Vector(1); times.addElement(time);
        Lecture lecture = new Lecture(c.getUniqueId(), null, c.getSchedulingSubpart().getUniqueId(), c.getClassLabel(), times, rooms, rooms.size(), new Placement(null,time,rooms), 0, 0, 1.0);
        lecture.setNote(c.getNotes());
        Placement p = (Placement)lecture.getInitialAssignment();
        p.setAssignmentId(new Long(iMakeupAssignmentId++));
        lecture.setBestAssignment(p, 0l);
        iProgress.trace("makup placement for "+c.getClassLabel()+": "+p.getLongName());
        return p;
    }
    
    private Offering loadOffering(InstructionalOffering io, Hashtable<Long, Course> courseTable, Hashtable<Long, Section> classTable) {
    	if (io.getInstrOfferingConfigs().isEmpty()) {
    		return null;
    	}
    	String courseName = io.getCourseName();
        Offering offering = new Offering(io.getUniqueId().longValue(), courseName);
        for (Iterator<CourseOffering> i = io.getCourseOfferings().iterator(); i.hasNext(); ) {
        	CourseOffering co = i.next();
            int projected = (co.getProjectedDemand()==null ? 0 : co.getProjectedDemand().intValue());
            boolean unlimited = false;
            int limit = 0;
            for (Iterator<InstrOfferingConfig> j = io.getInstrOfferingConfigs().iterator(); j.hasNext(); ) {
            	InstrOfferingConfig ioc = j.next();
                if (ioc.isUnlimitedEnrollment()) unlimited = true;
                limit += ioc.getLimit();
            }
            if (co.getReservation() != null)
            	limit = co.getReservation();
            if (limit >= 9999) unlimited = true;
            if (unlimited) limit=-1;
            Course course = new Course(co.getUniqueId(), co.getSubjectArea().getSubjectAreaAbbreviation(), co.getCourseNbr(), offering, limit, projected);
            courseTable.put(co.getUniqueId(), course);
        }
        Hashtable<Long,Section> class2section = new Hashtable<Long,Section>();
        Hashtable<Long,Subpart> ss2subpart = new Hashtable<Long, Subpart>();
        DecimalFormat df = new DecimalFormat("000");
        for (Iterator<InstrOfferingConfig> i = io.getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
        	InstrOfferingConfig ioc = i.next();
        	int configLimit = (ioc.isUnlimitedEnrollment() ? -1 : ioc.getLimit());
        	if (configLimit >= 9999) configLimit = -1;
            Config config = new Config(ioc.getUniqueId(), configLimit, courseName + " [" + ioc.getName() + "]", offering);
            TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator());
            subparts.addAll(ioc.getSchedulingSubparts());
            for (SchedulingSubpart ss: subparts) {
                String sufix = ss.getSchedulingSubpartSuffix();
                Subpart parentSubpart = (ss.getParentSubpart() == null ? null : (Subpart)ss2subpart.get(ss.getParentSubpart().getUniqueId()));
                if (ss.getParentSubpart() != null && parentSubpart == null) {
                    iProgress.error("Subpart " + ss.getSchedulingSubpartLabel() + " has parent " + 
                    		ss.getSchedulingSubpartLabel() +", but the appropriate parent subpart is not loaded.");
                }
                Subpart subpart = new Subpart(ss.getUniqueId().longValue(), df.format(ss.getItype().getItype()) + sufix,
                		ss.getItype().getAbbv().trim(), config, parentSubpart);
                subpart.setAllowOverlap(ss.isStudentAllowOverlap());
                ss2subpart.put(ss.getUniqueId(), subpart);
                for (Iterator<Class_> j = ss.getClasses().iterator(); j.hasNext(); ) {
                	Class_ c = j.next();
                    Section parentSection = (c.getParentClass() == null ? null : (Section)class2section.get(c.getParentClass().getUniqueId()));
                    if (c.getParentClass()!=null && parentSection==null) {
                        iProgress.error("Class " + c.getClassLabel() + " has parent " + c.getClassLabel() + ", but the appropriate parent section is not loaded.");
                    }
                    Assignment a = c.getCommittedAssignment();
                    Placement p = null;
                    if (iMakeupAssignmentsFromRequiredPrefs) {
                        p = makeupPlacement(c);
                    } else if (a != null) {
                        p = a.getPlacement();
                    }
                    if (p != null && p.getTimeLocation() != null) {
                    	p.getTimeLocation().setDatePattern(
                    			p.getTimeLocation().getDatePatternId(),
                    			datePatternName(p.getTimeLocation()),
                    			p.getTimeLocation().getWeekCode());
                    }
                    int minLimit = c.getExpectedCapacity();
                	int maxLimit = c.getMaxExpectedCapacity();
                	int limit = maxLimit;
                	if (minLimit < maxLimit && p != null) {
                		int roomLimit = (int) Math.floor(p.getRoomSize() / (c.getRoomRatio() == null ? 1.0f : c.getRoomRatio()));
                		// int roomLimit = Math.round((c.getRoomRatio() == null ? 1.0f : c.getRoomRatio()) * p.getRoomSize());
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (ioc.isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                    if (!c.isEnabledForStudentScheduling()) limit = 0;
                    Section section = new Section(c.getUniqueId().longValue(), limit, (c.getExternalUniqueId() == null ? c.getClassSuffix() == null ? c.getSectionNumberString() : c.getClassSuffix() : c.getExternalUniqueId()), subpart, p,
                    		getInstructorIds(c), getInstructorNames(c), parentSection);
                    class2section.put(c.getUniqueId(), section);
                    classTable.put(c.getUniqueId(), section);
                }
            }
        }
        for (org.unitime.timetable.model.Reservation reservation: io.getReservations()) {
        	Reservation r = null;
        	if (reservation instanceof org.unitime.timetable.model.IndividualReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((org.unitime.timetable.model.IndividualReservation)reservation).getStudents())
        			studentIds.add(s.getUniqueId());
        		r = new IndividualReservation(reservation.getUniqueId(), offering, studentIds);
        	} else if (reservation instanceof StudentGroupReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((StudentGroupReservation)reservation).getGroup().getStudents())
        			studentIds.add(s.getUniqueId());
        		r = new GroupReservation(reservation.getUniqueId(), (reservation.getLimit() == null ? -1.0 : reservation.getLimit()),
        				offering, studentIds);
        	} else if (reservation instanceof org.unitime.timetable.model.CurriculumReservation) {
        		org.unitime.timetable.model.CurriculumReservation cr = (org.unitime.timetable.model.CurriculumReservation)reservation;
        		List<String> classifications = new ArrayList<String>();
        		for (AcademicClassification clasf: cr.getClassifications())
        			classifications.add(clasf.getCode());
        		List<String> majors = new ArrayList<String>();
        		for (PosMajor major: cr.getMajors())
        			majors.add(major.getCode());
        		r = new CurriculumReservation(reservation.getUniqueId(), (reservation.getLimit() == null ? -1.0 : reservation.getLimit()),
        				offering, cr.getArea().getAcademicAreaAbbreviation(), classifications, majors);
        	} else if (reservation instanceof org.unitime.timetable.model.CourseReservation) {
        		CourseOffering co = ((org.unitime.timetable.model.CourseReservation)reservation).getCourse();
        		for (Course course: offering.getCourses()) {
        			if (co.getUniqueId().equals(course.getId()))
        				r = new CourseReservation(reservation.getUniqueId(), course);
        		}
        	}
        	if (r == null) {
        		iProgress.warn("Failed to load reservation " + reservation.getUniqueId() + "."); continue;
        	}
        	r.setExpired(reservation.isExpired());
        	configs: for (InstrOfferingConfig ioc: reservation.getConfigurations()) {
        		for (Config config: offering.getConfigs()) {
        			if (ioc.getUniqueId().equals(config.getId())) {
        				r.addConfig(config);
        				continue configs;
        			}
        		}
        	}
        	classes: for (Class_ c: reservation.getClasses()) {
        		for (Config config: offering.getConfigs()) {
        			for (Subpart subpart: config.getSubparts()) {
        				for (Section section: subpart.getSections()) {
        					if (c.getUniqueId().equals(section.getId())) {
        						r.addSection(section);
        						continue classes;
        					}
        				}
        			}
        		}
        	}
        }
        if (io.isByReservationOnly())
        	new DummyReservation(offering);
        return offering;
    }
    
    public void skipStudent(org.unitime.timetable.model.Student s, Hashtable<Long,Course> courseTable, Hashtable<Long,Section> classTable) {
    	iProgress.debug("Skipping student "+s.getUniqueId()+" (id="+s.getExternalUniqueId()+", name="+NameFormat.defaultFormat().format(s)+")");
    	
    	// If the student is enrolled in some classes, decrease the space in these classes accordingly
    	Map<Course, List<Section>> assignment = new HashMap<Course, List<Section>>();
    	for (StudentClassEnrollment enrollment: s.getClassEnrollments()) {
    		Section section = classTable.get(enrollment.getClazz().getUniqueId());
    		Course course = courseTable.get(enrollment.getCourseOffering().getUniqueId());
    		if (section == null || course == null) continue;
    		
    		List<Section> sections = assignment.get(course);
    		if (sections == null) {
    			sections = new ArrayList<Section>();
    			assignment.put(course, sections);
    			
				// If there is space in the course, decrease it by one
				if (course.getLimit() > 0)
					course.setLimit(course.getLimit() - 1);

				// If there is space in the configuration, decrease it by one
				Config config = section.getSubpart().getConfig();
				if (config.getLimit() > 0) {
					config.setLimit(config.getLimit() - 1);
				}
    		}
    		
    		// If there is space in the section, decrease it by one
    		if (section.getLimit() > 0) {
    			section.setLimit(section.getLimit() - 1);
    		}
    		sections.add(section);
    	}
    	
    	// For each offering the student is enrolled in
    	for (Map.Entry<Course, List<Section>> entry: assignment.entrySet()) {
    		Course course = entry.getKey();
    		List<Section> sections = entry.getValue();
    		
    		// Look for a matching reservation
    		Reservation reservation = null;
    		for (Reservation r: course.getOffering().getReservations()) {
    			// Skip reservations with no space that can be skipped
    			if (r.getLimit() >= 0.0 && r.getLimit() < 1.0 && !r.mustBeUsed()) continue;
    			
    			// Check applicability
    			boolean applicable = false;
    			if (r instanceof GroupReservation) {
    				applicable = ((GroupReservation)r).getStudentIds().contains(s.getUniqueId());
    			} else if (r instanceof IndividualReservation) {
    				applicable = ((IndividualReservation)r).getStudentIds().contains(s.getUniqueId());
    			}  else if (r instanceof CourseReservation) {
    				applicable = course.equals(((CourseReservation)r).getCourse());
    			} else if (r instanceof CurriculumReservation) {
    				CurriculumReservation c = (CurriculumReservation)r;
    				aac: for (AcademicAreaClassification aac: s.getAcademicAreaClassifications()) {
    					if (aac.getAcademicArea().equals(c.getAcademicArea())) {
    						if (c.getClassifications().isEmpty() || c.getClassifications().contains(aac.getAcademicClassification().getCode())) {
    							if (c.getMajors().isEmpty()) {
    								applicable = true; break aac;
    							}
    							for (PosMajor major: aac.getAcademicArea().getPosMajors()) {
    								if (s.getPosMajors().contains(major) && c.getMajors().contains(major.getCode())) {
    									applicable = true; break aac;        									
    								}
    							}
    		                }
    					}
    				}
    			}
    			if (!applicable) continue;
    			
    			// If it does not need to be used, check if actually used
				if (!r.mustBeUsed()) {
    				boolean included = true;
    				for (Section section: sections) {
    					if (!r.getConfigs().isEmpty() && !r.getConfigs().contains(section.getSubpart().getConfig())) {
    						included = false; break;
    					}
    					Set<Section> sectionsThisSubpart = r.getSections(section.getSubpart());
    					if (sectionsThisSubpart != null && !sectionsThisSubpart.contains(section)) {
    						included = false; break;
    					}
    				}
    				if (!included) continue;
				}
				
				if (reservation == null || r.compareTo(getAssignment(), reservation) < 0)
					reservation = r;
    		}
    		
			// Update reservation
    		if (reservation != null) {
    			if (reservation instanceof GroupReservation) {
					GroupReservation g = (GroupReservation)reservation;
					g.getStudentIds().remove(s.getUniqueId());
					if (g.getReservationLimit() >= 1.0)
						g.setReservationLimit(g.getReservationLimit() - 1.0);
				} else if (reservation instanceof IndividualReservation) {
					IndividualReservation i = (IndividualReservation)reservation;
					i.getStudentIds().remove(s.getUniqueId());
				} else if (reservation instanceof CourseReservation) {
					// nothing to do here
				} else if (reservation instanceof CurriculumReservation) {
					CurriculumReservation c = (CurriculumReservation)reservation;
					if (c.getReservationLimit() >= 1.0)
						c.setReservationLimit(c.getReservationLimit() - 1.0);
				}
    		}
    	}
    	
    	// Update curriculum counts
    	updateCurriculumCounts(s);    	
    }
    
    public Student loadStudent(org.unitime.timetable.model.Student s, Hashtable<Long,Course> courseTable, Hashtable<Long,Section> classTable) {
    	// Check for nobatch sectioning status
        if (iCheckForNoBatchStatus && s.hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) {
        	skipStudent(s, courseTable, classTable);
        	
        	return null;
        }
        
        NameFormat nameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingStudentNameFormat.value());
        iProgress.debug("Loading student "+s.getUniqueId()+" (id="+s.getExternalUniqueId()+", name="+nameFormat.format(s)+")");
        Student student = new Student(s.getUniqueId().longValue());
        student.setExternalId(s.getExternalUniqueId());
        student.setName(nameFormat.format(s));
        student.setStatus(s.getSectioningStatus() == null ? null : s.getSectioningStatus().getReference());
        if (iLoadStudentInfo) loadStudentInfo(student,s);

		TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
			public int compare(CourseDemand d1, CourseDemand d2) {
				if (d1.isAlternative() && !d2.isAlternative()) return 1;
				if (!d1.isAlternative() && d2.isAlternative()) return -1;
				int cmp = d1.getPriority().compareTo(d2.getPriority());
				if (cmp != 0) return cmp;
				return d1.getUniqueId().compareTo(d2.getUniqueId());
			}
		});
		demands.addAll(s.getCourseDemands());
        for (CourseDemand cd: demands) {
            if (cd.getFreeTime()!=null) {
            	TimeLocation ft = new TimeLocation(
                        cd.getFreeTime().getDayCode(),
                        cd.getFreeTime().getStartSlot(),
                        cd.getFreeTime().getLength(),
                        0, 0, -1l, "", iFreeTimePattern, 0);
                new FreeTimeRequest(
                        cd.getUniqueId(),
                        cd.getPriority(),
                        cd.isAlternative(),
                        student, ft);
            } else if (!cd.getCourseRequests().isEmpty()) {
                Vector<Course> courses = new Vector<Course>();
                HashSet<Choice> selChoices = new HashSet<Choice>();
                HashSet<Choice> wlChoices = new HashSet<Choice>();
                HashSet<Section> assignedSections = new HashSet<Section>();
                Config assignedConfig = null;
                TreeSet<org.unitime.timetable.model.CourseRequest> crs = new TreeSet<org.unitime.timetable.model.CourseRequest>(new Comparator<org.unitime.timetable.model.CourseRequest>() {
                	public int compare(org.unitime.timetable.model.CourseRequest r1, org.unitime.timetable.model.CourseRequest r2) {
                		return r1.getOrder().compareTo(r2.getOrder());
                	}
				});
                crs.addAll(cd.getCourseRequests());
                for (org.unitime.timetable.model.CourseRequest cr: crs) {
                    Course course = courseTable.get(cr.getCourseOffering().getUniqueId());
                    if (course==null) {
                        iProgress.warn("Student " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + ") requests course " + cr.getCourseOffering().getCourseName() + " that is not loaded.");
                        continue;
                    }
                    for (Iterator k=cr.getClassWaitLists().iterator();k.hasNext();) {
                        ClassWaitList cwl = (ClassWaitList)k.next();
                        Section section = course.getOffering().getSection(cwl.getClazz().getUniqueId().longValue());
                        if (section!=null) {
                            if (cwl.getType().equals(ClassWaitList.TYPE_SELECTION))
                                selChoices.add(section.getChoice());
                            else if (cwl.getType().equals(ClassWaitList.TYPE_WAITLIST))
                                wlChoices.add(section.getChoice());
                        }
                    }
                    if (assignedConfig==null) {
                        HashSet<Long> subparts = new HashSet<Long>();
                        for (Iterator<StudentClassEnrollment> i = cr.getClassEnrollments().iterator(); i.hasNext(); ) {
                        	StudentClassEnrollment enrl = i.next();
                        	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                            if (section!=null) {
                                assignedSections.add(section);
                                if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                                	iProgress.error("There is a problem assigning " + course.getName() + " to " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                                }
                                assignedConfig = section.getSubpart().getConfig();
                                if (!subparts.add(section.getSubpart().getId())) {
                                	iProgress.error("There is a problem assigning " + course.getName() + " to " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                                }
                            } else {
                            	iProgress.error("There is a problem assigning " + course.getName() + " to " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel() + " not known.");
                            }
                        }
                    }
                    courses.addElement(course);
                }
                if (courses.isEmpty()) continue;
                CourseRequest request = new CourseRequest(
                        cd.getUniqueId(),
                        cd.getPriority(),
                        cd.isAlternative(),
                        student,
                        courses,
                        cd.isWaitlist(), 
                        cd.getTimestamp().getTime());
                request.getSelectedChoices().addAll(selChoices);
                request.getWaitlistedChoices().addAll(wlChoices);
                if (assignedConfig!=null && assignedSections.size() == assignedConfig.getSubparts().size()) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections, getAssignment());
                    request.setInitialAssignment(enrollment);
                }
                if (assignedConfig!=null && assignedSections.size() != assignedConfig.getSubparts().size()) {
                	iProgress.error("There is a problem assigning " + request.getName() + " to " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + ") wrong number of classes (" +
                			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + ").");
                }
            }
        }

        if (!s.getClassEnrollments().isEmpty() || !s.getWaitlists().isEmpty()) {
        	TreeSet<Course> courses = new TreeSet<Course>(new Comparator<Course>() {
        		public int compare(Course c1, Course c2) {
        			return (c1.getSubjectArea() + " " + c1.getCourseNumber()).compareTo(c2.getSubjectArea() + " " + c2.getCourseNumber());
        		}
        	});
        	Map<Long, Long> timeStamp = new Hashtable<Long, Long>();
        	for (StudentClassEnrollment enrl: s.getClassEnrollments()) {
        		if (enrl.getCourseRequest() != null) continue; // already loaded
        		Course course = courseTable.get(enrl.getCourseOffering().getUniqueId());
                if (course==null) {
                    iProgress.warn("Student " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + ") requests course " + enrl.getCourseOffering().getCourseName()+" that is not loaded.");
                    continue;
                }
                if (enrl.getTimestamp() != null) timeStamp.put(enrl.getCourseOffering().getUniqueId(), enrl.getTimestamp().getTime());
                courses.add(course);
        	}
        	for (WaitList w: s.getWaitlists()) {
        		Course course = courseTable.get(w.getCourseOffering().getUniqueId());
                if (course==null) {
                    iProgress.warn("Student " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + ") requests course " + w.getCourseOffering().getCourseName()+" that is not loaded.");
                    continue;
                }
                if (w.getTimestamp() != null) timeStamp.put(w.getCourseOffering().getUniqueId(), w.getTimestamp().getTime());
                courses.add(course);
        	}
        	int priority = 0;
        	courses: for (Course course: courses) {
        		Vector<Course> cx = new Vector<Course>(); cx.add(course);
        		CourseRequest request = null;
        		for (Request r: student.getRequests()) {
        			if (r instanceof CourseRequest && getAssignment().getValue(r) == null && ((CourseRequest)r).getCourses().contains(course)) {
        				request = (CourseRequest)r;
        				break;
        			}
        		}
        		if (request == null) {
        			request = new CourseRequest(
                        course.getId(),
                        priority++,
                        false,
                        student,
                        cx,
                        true,
                        timeStamp.get(course.getId()));
        		}
                HashSet<Section> assignedSections = new HashSet<Section>();
                Config assignedConfig = null;
                HashSet<Long> subparts = new HashSet<Long>();
                for (Iterator<StudentClassEnrollment> i = s.getClassEnrollments().iterator(); i.hasNext(); ) {
                	StudentClassEnrollment enrl = i.next();
                	if (course.getId() != enrl.getCourseOffering().getUniqueId()) continue;
                	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                    if (section!=null) {
                        assignedSections.add(section);
                        if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                        	iProgress.error("There is a problem assigning " + request.getName() + " to " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                        	continue courses;
                        }
                        assignedConfig = section.getSubpart().getConfig();
                        if (!subparts.add(section.getSubpart().getId())) {
                        	iProgress.error("There is a problem assigning " + request.getName() + " to " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                        	continue courses;
                        }
                    } else {
                    	iProgress.error("There is a problem assigning " + request.getName() + " to " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel() + " not known.");
                    	Section x = classTable.get(enrl.getClazz().getUniqueId());
                    	if (x != null) {
                    		iProgress.info("  but a class with the same id is loaded, but under offering " + x.getSubpart().getConfig().getOffering().getName() + " (id is " + x.getSubpart().getConfig().getOffering().getId() + 
                    				", expected " +course.getOffering().getId() + ")");
                    	}
                    	continue courses;
                    }
                }
                if (assignedConfig!=null && assignedSections.size() == assignedConfig.getSubparts().size()) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections, getAssignment());
                    request.setInitialAssignment(enrollment);
                }
                if (assignedConfig!=null && assignedSections.size() != assignedConfig.getSubparts().size()) {
                	iProgress.error("There is a problem assigning " + request.getName() + " to " + nameFormat.format(s) + " (" + s.getExternalUniqueId() + "): wrong number of classes (" +
                			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + ").");
                }
        	}
        }
        
        return student;
    }
    
    public void assignStudent(Student student) {
		for (Request r: student.getRequests()) {
			if (r.getInitialAssignment() != null && r.getModel().conflictValues(getAssignment(), r.getInitialAssignment()).isEmpty())
				getAssignment().assign(0, r.getInitialAssignment());
		}
		for (Request r: student.getRequests()) {
			if (r instanceof FreeTimeRequest) {
				FreeTimeRequest ft = (FreeTimeRequest)r;
				Enrollment enrollment = ft.createEnrollment();
				if (r.getModel().conflictValues(getAssignment(), enrollment).isEmpty()) {
					ft.setInitialAssignment(enrollment);
					getAssignment().assign(0, enrollment);
				}
			}
		}
    }
    
    public void checkForConflicts(Student student) {
    	for (Request r: student.getRequests()) {
    		if (getAssignment().getValue(r) != null || r.getInitialAssignment() == null || !(r instanceof CourseRequest)) continue;
    		if (r.getModel().conflictValues(getAssignment(), r.getInitialAssignment()).isEmpty()) {
    			getAssignment().assign(0, r.getInitialAssignment());
    			continue;
    		}
           	CourseRequest cr = (CourseRequest)r;
           	Enrollment enrl = (Enrollment)r.getInitialAssignment();
           	org.unitime.timetable.model.Student s = (student.getId() >= 0 ? StudentDAO.getInstance().get(student.getId()) : null);
           	iProgress.error("There is a problem assigning " + cr.getName() + " to " + (s == null ? student.getId() : NameFormat.defaultFormat().format(s) + " (" + s.getExternalUniqueId() + ")" ));
           	boolean hasLimit = false, hasOverlap = false;
           	for (Iterator<Section> i = enrl.getSections().iterator(); i.hasNext();) {
           		Section section = i.next();
           		if (section.getTime() != null) {
               		for (Request q: student.getRequests()) {
               			Enrollment enrlx = getAssignment().getValue(q);
               			if (enrlx == null || !(q instanceof CourseRequest)) continue;
               			for (Iterator<Section> j = enrlx.getSections().iterator(); j.hasNext();) {
               				Section sectionx = j.next();
               				if (sectionx.getTime() == null) continue;
               				if (sectionx.isOverlapping(section)) {
               					iProgress.info("  " + section.getSubpart().getName() + " " + section.getName() + " " + section.getTime().getLongName() +
               							" overlaps with " + sectionx.getSubpart().getConfig().getOffering().getName() + " " + sectionx.getSubpart().getName() + " " +
               							sectionx.getName() + " " + sectionx.getTime().getLongName());
               					hasOverlap = true;
               				}
               			}
               		}
           		}
           		if (section.getLimit() >= 0 && section.getLimit() < 1 + section.getEnrollments(getAssignment()).size()) {
    					iProgress.info("  " + section.getSubpart().getName() + " " + section.getName() + (section.getTime() == null ? "" : " " + section.getTime().getLongName()) +
    							" has no space available (limit is "+ section.getLimit() + ")");
    					if (iTweakLimits) {
    						section.setLimit(section.getEnrollments(getAssignment()).size() + 1);
    						section.clearReservationCache();
    						iProgress.info("    limit increased to "+section.getLimit());
    					}
           			hasLimit = true;
           		}
    				iProgress.info("  " + section.getSubpart().getName() + " " + section.getName() + (section.getTime() == null ? "" : " " + section.getTime().getLongName()));
           	}
           	if (enrl.getConfig().getLimit() >= 0 && enrl.getConfig().getLimit() < 1 + enrl.getConfig().getEnrollments(getAssignment()).size()) {
    				iProgress.info("  config " + enrl.getConfig().getName() + " has no space available (limit is "+ enrl.getConfig().getLimit() + ")");
    				if (iTweakLimits) {
    					enrl.getConfig().setLimit(enrl.getConfig().getEnrollments(getAssignment()).size() + 1);
    					enrl.getConfig().clearReservationCache();
    					iProgress.info("    limit increased to "+enrl.getConfig().getLimit());
    				}
       			hasLimit = true;
           	}
           	if (enrl.getCourse() != null && enrl.getCourse().getLimit() >= 0 && enrl.getCourse().getLimit() < 1 + enrl.getCourse().getEnrollments(getAssignment()).size()) {
    				iProgress.info("  course " + enrl.getCourse().getName() + " has no space available (limit is "+ enrl.getCourse().getLimit() + ")");
    				if (iTweakLimits) {
    					enrl.getCourse().setLimit(enrl.getCourse().getEnrollments(getAssignment()).size() + 1);
    					iProgress.info("    limit increased to "+enrl.getCourse().getLimit());
    				}
       			hasLimit = true;
           	}
           	if (!hasLimit && !hasOverlap) {
           		for (Iterator<Enrollment> i = r.getModel().conflictValues(getAssignment(), r.getInitialAssignment()).iterator(); i.hasNext();) {
           			Enrollment enrlx = i.next();
           			for (Iterator<Section> j = enrlx.getSections().iterator(); j.hasNext();) {
           				Section sectionx = j.next();
           				iProgress.info("    conflicts with " + sectionx.getSubpart().getConfig().getOffering().getName() + " " + sectionx.getSubpart().getName() + " " +
       							sectionx.getName() + (sectionx.getTime() == null ? "" : " " + sectionx.getTime().getLongName()));
           			}
       				if (enrlx.getRequest().getStudent().getId() != student.getId())
       					iProgress.info("    of a different student");
           		}
           	}
           	if (hasLimit && !hasOverlap && iTweakLimits && r.getModel().conflictValues(getAssignment(), r.getInitialAssignment()).isEmpty()) {
           		getAssignment().assign(0, r.getInitialAssignment());
           	}
    	}
    }
    
    private String curriculum(Student student) {
    	return (student.getAcademicAreaClasiffications().isEmpty() ? "" : student.getAcademicAreaClasiffications().get(0).getArea() + ":" + student.getAcademicAreaClasiffications().get(0).getCode()) + ":" +
			(student.getMajors().isEmpty() ? "" : student.getMajors().get(0).getCode());
    }
    
    private String curriculum(org.unitime.timetable.model.Student student) {
    	String curriculum = "";
    	for (AcademicAreaClassification aac: student.getAcademicAreaClassifications()) {
    		curriculum = aac.getAcademicArea().getAcademicAreaAbbreviation() + ":" + aac.getAcademicClassification().getCode();
    		for (PosMajor major: aac.getAcademicArea().getPosMajors()) {
    			if (student.getPosMajors().contains(major)) {
    				curriculum += ":" + major.getCode();
    				break;
    			}
    		}
    		break;
    	}
    	return curriculum;
    }
    
    Map<Long, Map<String, Integer>> iCourse2Curricula2Weight = new Hashtable<Long, Map<String, Integer>>();
    private void updateCurriculumCounts(Student student) {
    	String curriculum = curriculum(student);
    	for (Request request: student.getRequests()) {
    		if (request instanceof CourseRequest) {
    			Course course = (request.getInitialAssignment() != null ? request.getInitialAssignment().getCourse() : ((CourseRequest)request).getCourses().get(0));
    			Map<String, Integer> c2w = iCourse2Curricula2Weight.get(course.getId());
    			if (c2w == null) {
    				c2w = new Hashtable<String, Integer>();
    				iCourse2Curricula2Weight.put(course.getId(), c2w);
    			}
    			Integer cx = c2w.get(curriculum);
    			c2w.put(curriculum, 1 + (cx == null ? 0 : cx));
    		}
    	}
    }
    
    private void updateCurriculumCounts(org.unitime.timetable.model.Student student) {
    	String curriculum = curriculum(student);
    	Set<Long> courses = new HashSet<Long>();
    	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
    		Long courseId = enrollment.getCourseOffering().getUniqueId();
    		if (courses.add(courseId)) {
    			Map<String, Integer> c2w = iCourse2Curricula2Weight.get(courseId);
    			if (c2w == null) {
    				c2w = new Hashtable<String, Integer>();
    				iCourse2Curricula2Weight.put(courseId, c2w);
    			}
    			Integer cx = c2w.get(curriculum);
    			c2w.put(curriculum, 1 + (cx == null ? 0 : cx));
    		}
    	}
    	demands: for (CourseDemand demand: student.getCourseDemands()) {
    		org.unitime.timetable.model.CourseRequest request = null;
    		for (org.unitime.timetable.model.CourseRequest r: demand.getCourseRequests()) {
    			if (courses.contains(r.getCourseOffering().getUniqueId())) continue demands;
    			if (request == null || r.getOrder() < request.getOrder())
    				request = r;
    		}
    		if (request != null) {
    			Long courseId = request.getCourseOffering().getUniqueId();
        		courses.add(courseId);
    			Map<String, Integer> c2w = iCourse2Curricula2Weight.get(courseId);
    			if (c2w == null) {
    				c2w = new Hashtable<String, Integer>();
    				iCourse2Curricula2Weight.put(courseId, c2w);
    			}
    			Integer cx = c2w.get(curriculum);
    			c2w.put(curriculum, 1 + (cx == null ? 0 : cx));
    		}
    	}
    }
    
    private void fixWeights(org.hibernate.Session hibSession, Collection<Course> courses) {
    	iProgress.setPhase("Computing projected request weights...", courses.size());
    	for (Course course: courses) {
    		iProgress.incProgress();
    		
    		Map<String, Integer> cur2real = iCourse2Curricula2Weight.get(course.getId());
    		if (cur2real != null) {
        		Map<String, Double> cur2proj = new Hashtable<String, Double>();
        		for (CourseRequest request: course.getRequests()) {
        			if (!request.getCourses().get(0).equals(course) || !request.getStudent().isDummy()) continue;
    				Double proj = cur2proj.get(curriculum(request.getStudent()));
    				cur2proj.put(curriculum(request.getStudent()), request.getWeight() + (proj == null ? 0.0 : proj));
        		}
        		
        		for (String cur: cur2proj.keySet()) {
        			double proj = cur2proj.get(cur);
        			Integer real = cur2real.get(cur);
        			if (real == null) continue;
        			iProgress.debug("Projected demands for course " + course.getName() + ": " + cur.replace(':', ' ') + " multiplies by " + (real >= proj ? 0.0 : (proj - real) / proj) + " (projected=" + proj + ", real=" + real + ")");
        		}
        		
        		for (CourseRequest request: course.getRequests()) {
        			if (!request.getCourses().get(0).equals(course) || !request.getStudent().isDummy()) continue;
        			double proj = cur2proj.get(curriculum(request.getStudent()));
        			Integer real = cur2real.get(curriculum(request.getStudent()));
        			if (real == null) continue;
        			request.setWeight(request.getWeight() * (real >= proj ? 0.0 : (proj - real) / proj));
        		}
    		}

    		double nrStudents = 0.0;
    		double nrLastLike = 0.0;
    		int lastLikeCount = 0;
    		for (CourseRequest request: course.getRequests()) {
    			if (!request.getCourses().get(0).equals(course)) continue;
    			nrStudents += request.getWeight();
    			if (request.getStudent().isDummy()) {
    				nrLastLike += request.getWeight();
    				lastLikeCount ++;
    			}
    		}
    		
	        double projected = course.getProjected();
	        double limit = course.getLimit();
	        if (limit >= 9999) limit = -1;
	        
	        int configLimit = 0;
	        for (Config config: course.getOffering().getConfigs()) {
	        	if (config.getLimit() < 0 || config.getLimit() >= 9999) { configLimit = -1; break; }
	        	int cLimit = config.getLimit();
	        	for (Subpart subpart: config.getSubparts()) {
	        		int subpartLimit = 0;
	        		for (Section section: subpart.getSections()) {
	        			if (section.getLimit() < 0 || section.getLimit() >= 9999) { subpartLimit = -1; break; }
	        			subpartLimit += section.getLimit();
	        		}
	        		if (subpartLimit >= 0 && subpartLimit < cLimit) { cLimit = subpartLimit; }
	        	}
	        	configLimit += cLimit;
	        }
	        
	        if (course.getOffering().getCourses().size() > 1) {
		        int offeringLimit = 0;
		        for (Course c: course.getOffering().getCourses()) {
		        	if (c.getLimit() < 0 || c.getLimit() >= 9999) { offeringLimit = -1; break; }
	        		offeringLimit += c.getLimit();
		        }
		        if (configLimit >= 0 && configLimit < offeringLimit)
		        	limit = (limit * configLimit) / offeringLimit;
		        else if (configLimit >= 0 && offeringLimit < 0)
		        	limit = configLimit / course.getOffering().getCourses().size();
	        } else {
	        	if (configLimit >= 0 && (configLimit < limit || limit < 0))
	        		limit = configLimit;
	        }
	        
	        if (limit < 0) {
	            iProgress.debug("Course " + course.getName() + " is unlimited.");
	            continue;
	        }
	        if (projected <= 0) {
	        	iProgress.info("No projected demand for course " + course.getName() + ", using course limit (" + Math.round(limit) + ")");
	            projected = limit;
	        } else if (limit < projected) {
	        	if (!iProjections)
	        		iProgress.info("Projected number of students is over course limit for course " + course.getName() + " (" + Math.round(projected) + ">" + Math.round(limit) + ")");
	        	projected = limit;
	        }
    		if (lastLikeCount <= 0) {
				iProgress.info("No projected course demands for course " + course.getName());
    			continue;
    		}
	        double weight = (nrLastLike <= 0 ? 0.0 : Math.max(0.0, projected - (nrStudents - nrLastLike)) / nrLastLike);
	        iProgress.debug("Projected student weight for " + course.getName() + " is " + weight + " (projected=" + nrLastLike + ", real=" + (nrStudents - nrLastLike) + ", limit=" + projected + ")");
	        int left = 0;
	        for (CourseRequest request: new ArrayList<CourseRequest>(course.getRequests())) {
	        	if (request.getStudent().isDummy()) {
	        		request.setWeight(weight * request.getWeight());
	        		if (request.getWeight() <= 0.0) {
	                    Student student = request.getStudent();
	                    getModel().removeVariable(request);
	                    student.getRequests().remove(request);
	                    for (Course c: request.getCourses())
	                    	c.getRequests().remove(request);
	                    if (student.getRequests().isEmpty())
	                    	getModel().removeStudent(student);
	        		} else {
	        			left++;
	        		}
	        	}
	        }
	        if (left <= 0)
	        	iProgress.info("No projected course demands needed for course " + course.getName());
    	}
    	getModel().requestWeightsChanged(getAssignment());
    }
    
    
    public void loadStudentInfo(Student student, org.unitime.timetable.model.Student s) {
        for (Iterator i=s.getAcademicAreaClassifications().iterator();i.hasNext();) {
            AcademicAreaClassification aac = (AcademicAreaClassification)i.next();
            student.getAcademicAreaClasiffications().add(
                    new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),aac.getAcademicClassification().getCode()));
            for (Iterator j=aac.getAcademicArea().getPosMajors().iterator();j.hasNext();) {
                PosMajor major = (PosMajor)j.next();
                if (s.getPosMajors().contains(major)) {
                    student.getMajors().add(
                            new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),major.getCode()));
                }
                    
            }
        }
        for (StudentGroup g: s.getGroups())
        	student.getMinors().add(new AcademicAreaCode("", g.getGroupAbbreviation()));
        for (StudentAccomodation a: s.getAccomodations())
        	student.getMinors().add(new AcademicAreaCode("A", a.getAbbreviation()));
    }
    
	public static BitSet getFreeTimeBitSet(Session session) {
		int startMonth = session.getPatternStartMonth();
		int endMonth = session.getPatternEndMonth();
		int size = DateUtils.getDayOfYear(0, endMonth + 1, session.getSessionStartYear()) - DateUtils.getDayOfYear(1, startMonth, session.getSessionStartYear());
		BitSet ret = new BitSet(size);
		for (int i = 0; i < size; i++)
			ret.set(i);
		return ret;
	}
	
    private String datePatternName(TimeLocation time) {
    	if (time.getWeekCode().isEmpty()) return time.getDatePatternName();
    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(iDatePatternFirstDate);
    	int idx = time.getWeekCode().nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date first = null;
    	while (idx < time.getWeekCode().size() && first == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) first = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (first == null) return time.getDatePatternName();
    	cal.setTime(iDatePatternFirstDate);
    	idx = time.getWeekCode().length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) last = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;
    	}
    	if (last == null) return time.getDatePatternName();
        Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
    	return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    }

	public static Date getDatePatternFirstDay(Session s) {
		return DateUtils.getDate(1, s.getPatternStartMonth(), s.getSessionStartYear());
	}
	
	public static interface SectionProvider {
		public Section get(Long classId);
	}
	
	public static List<Collection<Section>> getSections(DistributionPref pref, SectionProvider classTable) {
		List<Collection<Section>> ret = new ArrayList<Collection<Section>>();
    	int groupingType = (pref.getGrouping() == null ? DistributionPref.sGroupingNone : pref.getGrouping().intValue());
    	if (groupingType == DistributionPref.sGroupingProgressive) {
    		int maxSize = 0;
    		for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_)
        			maxSize = Math.max(maxSize, 1);
        		else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart)
        			maxSize = Math.max(maxSize, ((SchedulingSubpart)distributionObject.getPrefGroup()).getClasses().size());
    		}
    		Set<Section> sections[] = new Set[maxSize];
    		for (int i=0;i<sections.length;i++)
    			sections[i] = new HashSet<Section>();

    		List<DistributionObject> distributionObjects = new ArrayList<DistributionObject>(pref.getDistributionObjects());
    		Collections.sort(distributionObjects, new TimetableDatabaseLoader.ChildrenFirstDistributionObjectComparator());
    		for (DistributionObject distributionObject: distributionObjects) {
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Section section = classTable.get(distributionObject.getPrefGroup().getUniqueId());
        			if (section!=null)
        				for (int j = 0; j < sections.length; j++)
        					sections[j].add(section);
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (int j = 0; j < sections.length; j++) {
        	    		Section section = null;
        	    		sections: for (Section s: sections[j]) {
        	    			Section p = s.getParent();
        	    			while (p != null) {
        	    				if (p.getSubpart().getId() == subpart.getUniqueId()) {
        	    					section = s;
        	    					break sections;
        	    				}
        	    				p  = p.getParent();
        	    			}
        	    		}
        	    		if (section == null)
        	    			section = classTable.get(classes.get(j%classes.size()).getUniqueId());
        	    		if (section!=null)
        	    			sections[j].add(section);
        	    	}
        		}
    		}
    		for (Set<Section> s: sections)
    			ret.add(s);
		} else {
    		List<Section> sections = new ArrayList<Section>();
        	for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Section section = classTable.get(distributionObject.getPrefGroup().getUniqueId());
        			if (section != null)
        				sections.add(section);
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Class_ clazz: classes) {
        	    		Section section = classTable.get(clazz.getUniqueId());
	        			if (section != null)
	        				sections.add(section);
        	    	}        	        	    		
        		}
        	}
			if (groupingType == DistributionPref.sGroupingPairWise) {
	        	if (sections.size() >= 2) {
	        		for (int idx1 = 0; idx1 < sections.size() - 1; idx1++) {
	        			Section s1 = sections.get(idx1);
	        			for (int idx2 = idx1 + 1; idx2 < sections.size(); idx2++) {
	        				Section s2 = sections.get(idx2);
	        				Set<Section> s = new HashSet<Section>();
	        				s.add(s1); s.add(s2);
	        				ret.add(s);
	        			}
	        		}
	        	}
			} else if (groupingType == DistributionPref.sGroupingNone) {
				ret.add(sections);
			} else {
				List<Section> s = new ArrayList<Section>();
				for (Section section: sections) {
					s.add(section);
					if (s.size() == groupingType) {
						ret.add(s); s = new ArrayList<Section>();
					}
				}
				if (s.size() >= 2)
					ret.add(new HashSet<Section>(s));
			}
	    }		
		return ret;
	}
	
    public void load(Session session, org.hibernate.Session hibSession) {
    	iFreeTimePattern = getFreeTimeBitSet(session);
		iDatePatternFirstDate = getDatePatternFirstDay(session);
    	
        Hashtable<Long, Course> courseTable = new Hashtable<Long, Course>();
        final Hashtable<Long, Section> classTable = new Hashtable<Long, Section>();
        List<InstructionalOffering> offerings = hibSession.createQuery(
                "select distinct io from InstructionalOffering io " +
                "left join fetch io.courseOfferings as co "+
                "left join fetch io.instrOfferingConfigs as ioc "+
                "left join fetch ioc.schedulingSubparts as ss "+
                "left join fetch ss.classes as c "+
                "left join fetch io.reservations as r "+
                "where " +
                "io.session.uniqueId = :sessionId and io.notOffered = false").
                setLong("sessionId",session.getUniqueId().longValue()).
                setFetchSize(1000).list();
        iProgress.setPhase("Loading course offerings...", offerings.size());
        for (InstructionalOffering io: offerings) {
        	iProgress.incProgress();
            Offering offering = loadOffering(io, courseTable, classTable);
            if (offering!=null) getModel().addOffering(offering);
        }
        
        if (iIncludeCourseDemands || iProjections) {
            List students = hibSession.createQuery(
                    "select distinct s from Student s " +
                    "left join fetch s.courseDemands as cd "+
                    "left join fetch cd.courseRequests as cr "+
                    "left join fetch cr.classWaitLists as cw " +
                    "left join fetch s.classEnrollments as e " +
                    "left join fetch s.waitlists as w " +
                    (iLoadStudentInfo ? "left join fetch s.academicAreaClassifications as a left join fetch s.posMajors as mj left join fetch s.groups as g " : "") +
                    "where s.session.uniqueId=:sessionId").
                    setLong("sessionId",session.getUniqueId().longValue()).
                    setFetchSize(1000).list();
            iProgress.setPhase("Loading student requests...", students.size());
            for (Iterator i=students.iterator();i.hasNext();) {
                org.unitime.timetable.model.Student s = (org.unitime.timetable.model.Student)i.next(); iProgress.incProgress();
                if (s.getCourseDemands().isEmpty() && s.getClassEnrollments().isEmpty() && s.getWaitlists().isEmpty()) continue;
                Student student = loadStudent(s, courseTable, classTable);
                if (student == null) continue;
                updateCurriculumCounts(student);
                if (iProjections) {
                	// Decrease the limits accordingly
                	for (Request request: student.getRequests()) {
                		if (request.getInitialAssignment() != null && request.getInitialAssignment().isCourseRequest()) {
                			Enrollment enrollment = request.getInitialAssignment();
                			if (enrollment.getConfig().getLimit() > 0)
                				enrollment.getConfig().setLimit(enrollment.getConfig().getLimit() - 1);
                			for (Section section: enrollment.getSections())
                				if (section.getLimit() > 0)
                					section.setLimit(section.getLimit() - 1);
                			if (enrollment.getCourse() != null && enrollment.getCourse().getLimit() > 0)
                				enrollment.getCourse().setLimit(enrollment.getCourse().getLimit() - 1);
                			if (enrollment.getReservation() != null) {
                				if (enrollment.getReservation() instanceof GroupReservation && enrollment.getReservation().getReservationLimit() >= 1.0) {
                					((GroupReservation)enrollment.getReservation()).getStudentIds().remove(student.getId());
                					((GroupReservation)enrollment.getReservation()).setReservationLimit(((GroupReservation)enrollment.getReservation()).getReservationLimit() - 1.0);
                				} else if (enrollment.getReservation() instanceof IndividualReservation) {
                					((IndividualReservation)enrollment.getReservation()).getStudentIds().remove(student.getId());
                				} else if (enrollment.getReservation() instanceof CurriculumReservation && enrollment.getReservation().getReservationLimit() >= 1.0) {
                					((CurriculumReservation)enrollment.getReservation()).setReservationLimit(enrollment.getReservation().getReservationLimit() - 1.0);
                				}
                			}
                		}
                		if (request instanceof CourseRequest) {
                			for (Course course: ((CourseRequest)request).getCourses()) {
                				course.getRequests().remove(request);
                			}
                		}
                	}
                } else {
                    getModel().addStudent(student);
                    assignStudent(student);
                }
            }
        }
        
        List<DistributionPref> distPrefs = hibSession.createQuery(
        		"select p from DistributionPref p, Department d where p.distributionType.reference in (:ref1, :ref2) and d.session.uniqueId = :sessionId" +
        		" and p.owner = d and p.prefLevel.prefProlog = :pref")
        		.setString("ref1", GroupConstraint.ConstraintType.LINKED_SECTIONS.reference())
        		.setString("ref2", IgnoreStudentConflictsConstraint.REFERENCE)
        		.setString("pref", PreferenceLevel.sRequired)
        		.setLong("sessionId", iSessionId)
        		.list();
        if (!distPrefs.isEmpty()) {
        	iProgress.setPhase("Loading distribution preferences...", distPrefs.size());
        	SectionProvider p = new SectionProvider() {
				@Override
				public Section get(Long classId) {
					return classTable.get(classId);
				}
			};
        	for (DistributionPref pref: distPrefs) {
        		iProgress.incProgress();
        		for (Collection<Section> sections: getSections(pref, p)) {
        			if (GroupConstraint.ConstraintType.LINKED_SECTIONS.reference().equals(pref.getDistributionType().getReference())) {
        				getModel().addLinkedSections(sections);        				
        			} else {
        				for (Section s1: sections)
                			for (Section s2: sections)
                				if (!s1.equals(s2)) s1.addIgnoreConflictWith(s2.getId());
        			}
        		}
        	}
        }
        
        iProgress.setPhase("Checking for student conflicts...", getModel().getStudents().size());
        for (Student student: getModel().getStudents()) {
        	iProgress.incProgress();
        	checkForConflicts(student);
        }
        
        if (iStudentCourseDemands != null) {
        	iStudentCourseDemands.init(hibSession, iProgress, SessionDAO.getInstance().get(iSessionId, hibSession), offerings);
    		Hashtable<Long, Student> students = new Hashtable<Long, Student>();
    		
            Hashtable<Long, Set<Long>> classAssignments = null;
            if (iIncludeUseCommittedAssignments && !iStudentCourseDemands.isMakingUpStudents()) {
                classAssignments = new Hashtable();
                List enrollments = hibSession.createQuery("select distinct se.studentId, se.clazz.uniqueId from StudentEnrollment se where "+
                    "se.solution.commited=true and se.solution.owner.session.uniqueId=:sessionId").
                    setLong("sessionId",session.getUniqueId().longValue()).setFetchSize(1000).list();
                iProgress.setPhase("Loading projected class assignments...", enrollments.size());
                for (Iterator i=enrollments.iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next(); iProgress.incProgress();
                    Long studentId = (Long)o[0];
                    Long classId = (Long)o[1];
                    Set<Long> classIds = classAssignments.get(studentId);
                    if (classIds==null) {
                        classIds = new HashSet<Long>();
                        classAssignments.put(studentId, classIds);
                    }
                    classIds.add(classId);
                }
            }
    		
            iProgress.setPhase("Loading projected course requests...", offerings.size());
        	long requestId = -1;
            for (InstructionalOffering io: offerings) {
            	iProgress.incProgress();
            	for (CourseOffering co: io.getCourseOfferings()) {
        	        Course course = courseTable.get(co.getUniqueId());
        	        if (course == null) continue;
            		Set<WeightedStudentId> demands = iStudentCourseDemands.getDemands(co);
            		if (demands == null) continue;
            		for (WeightedStudentId demand: demands) {
            	        Student student = (Student)students.get(demand.getStudentId());
            	        if (student == null) {
            	            student = new Student(demand.getStudentId(), true);
            	            if (demand.getArea() != null && demand.getClasf() != null)
            	            	student.getAcademicAreaClasiffications().add(new AcademicAreaCode(demand.getArea(), demand.getClasf()));
            	            if (demand.getArea() != null && demand.getMajor() != null && !demand.getMajor().isEmpty())
            	            	for (String mj: demand.getMajor().split("\\|"))
            	            		student.getMajors().add(new AcademicAreaCode(demand.getArea(), mj));
            	            students.put(demand.getStudentId(), student);
            	        }
            	        List<Course> courses = new ArrayList<Course>();
            	        courses.add(course);
            	        CourseRequest request = new CourseRequest(
            	        		requestId--,
            	                0,
            	                false,
            	                student,
            	                courses,
            	                false,
            	                null);
            	        request.setWeight(demand.getWeight());
            	        if (classAssignments != null && !classAssignments.isEmpty()) {
            	        	Set<Long> classIds = classAssignments.get(demand.getStudentId());
            	        	if (classIds != null) {
            	                enrollments: for (Enrollment enrollment: request.values()) {
            	                	for (Section section: enrollment.getSections())
            	                		if (!classIds.contains(section.getId())) continue enrollments;
            	                	request.setInitialAssignment(enrollment);
            	                	break;
            	                }
            	        	}
            	        }
            		}
            	}
            }
            
            for (Student student: students.values()) {
                getModel().addStudent(student);
                assignStudent(student);
            }
            
            for (Student student: students.values()) {
            	checkForConflicts(student);
            }
            
            if (iFixWeights)
            	fixWeights(hibSession, courseTable.values());
            
            getModel().createAssignmentContexts(getAssignment(), true);
        }
        
        /*
        if (iIncludeLastLikeStudents) {
            Hashtable<Long, Set<Long>> classAssignments = null;
            if (iIncludeUseCommittedAssignments) {
                classAssignments = new Hashtable();
                List enrollments = hibSession.createQuery("select distinct se.studentId, se.clazz.uniqueId from StudentEnrollment se where "+
                    "se.solution.commited=true and se.solution.owner.session.uniqueId=:sessionId").
                    setLong("sessionId",session.getUniqueId().longValue()).setFetchSize(1000).list();
                iProgress.setPhase("Loading last-like class assignments...", enrollments.size());
                for (Iterator i=enrollments.iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next(); iProgress.incProgress();
                    Long studentId = (Long)o[0];
                    Long classId = (Long)o[1];
                    Set<Long> classIds = classAssignments.get(studentId);
                    if (classIds==null) {
                        classIds = new HashSet<Long>();
                        classAssignments.put(studentId, classIds);
                    }
                    classIds.add(classId);
                }
            }
        
            Hashtable<Long, org.unitime.timetable.model.Student> students = new Hashtable<Long, org.unitime.timetable.model.Student>();
            List enrollments = hibSession.createQuery(
                    "select d, c.uniqueId from LastLikeCourseDemand d left join fetch d.student s, CourseOffering c left join c.demandOffering cx " +
                    "where d.subjectArea.session.uniqueId=:sessionId and c.subjectArea.session.uniqueId=:sessionId and " +
                    "((c.permId=null and d.subjectArea=c.subjectArea and d.courseNbr=c.courseNbr ) or "+
                    " (c.permId!=null and c.permId=d.coursePermId) or "+
                    " (cx.permId=null and d.subjectArea=cx.subjectArea and d.courseNbr=cx.courseNbr) or "+
                    " (cx.permId!=null and cx.permId=d.coursePermId)) "+
                    "order by s.uniqueId, d.priority, d.uniqueId").
                    setLong("sessionId",session.getUniqueId().longValue()).setFetchSize(1000).list();
            iProgress.setPhase("Loading last-like course requests...", enrollments.size());
            Hashtable lastLikeStudentTable = new Hashtable();
            for (Iterator i=enrollments.iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();iProgress.incProgress();
                LastLikeCourseDemand d = (LastLikeCourseDemand)o[0];
                org.unitime.timetable.model.Student s = (org.unitime.timetable.model.Student)d.getStudent();
                Long courseOfferingId = (Long)o[1];
                if (s.getExternalUniqueId()!=null && loadedStudentIds.contains(s.getExternalUniqueId())) continue;
                loadLastLikeStudent(hibSession, d, s, courseOfferingId, lastLikeStudentTable, courseTable, classTable, classAssignments);
                students.put(s.getUniqueId(), s);
            }
            for (Enumeration e=lastLikeStudentTable.elements();e.hasMoreElements();) {
                Student student = (Student)e.nextElement();
                getModel().addStudent(student);
            	assignStudent(student, students.get(student.getId()));
            }
        }
		*/
        
        if (iLoadSectioningInfos) {
        	List<SectioningInfo> infos = hibSession.createQuery(
				"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId")
				.setLong("sessionId", iSessionId)
				.list();
        	iProgress.setPhase("Loading sectioning infos...", infos.size());
        	for (SectioningInfo info : infos) {
        		iProgress.incProgress();
        		Section section = classTable.get(info.getClazz().getUniqueId());
        		if (section != null) {
        			section.setSpaceExpected(info.getNbrExpectedStudents());
        			section.setSpaceHeld(info.getNbrHoldingStudents());
        			if (section.getLimit() >= 0 && (section.getLimit() - section.getEnrollments(getAssignment()).size()) <= section.getSpaceExpected())
        				iProgress.info("Section " + section.getSubpart().getConfig().getOffering().getName() + " " + section.getSubpart().getName() + " " +
        						section.getName() + " has high demand (limit: " + section.getLimit() + ", enrollment: " + section.getEnrollments(getAssignment()).size() +
        						", expected: " + section.getSpaceExpected() + ")");
        		}
        	}	
        }
        
        iProgress.setPhase("Done",1);iProgress.incProgress();
    }
    
    
}
