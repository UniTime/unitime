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
package org.unitime.timetable.solver.studentsct;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.DateUtils;

import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.studentsct.StudentSectioningLoader;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.Test;
import net.sf.cpsolver.studentsct.model.AcademicAreaCode;
import net.sf.cpsolver.studentsct.model.Choice;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

/**
 * @author Tomas Muller
 */
public class StudentSectioningDatabaseLoader extends StudentSectioningLoader {
    private static Log sLog = LogFactory.getLog(StudentSectioningDatabaseLoader.class);
    private boolean iIncludeCourseDemands = true;
    private boolean iIncludeLastLikeStudents = true;
    private boolean iIncludeUseCommittedAssignments = false;
    private boolean iMakeupAssignmentsFromRequiredPrefs = false;
    private boolean iLoadStudentInfo = false;
    private String iInitiative = null;
    private String iTerm = null;
    private String iYear = null;
    private Long iSessionId = null;
    private long iMakeupAssignmentId = 0;
	private BitSet iFreeTimePattern = null;
	private Date iDatePatternFirstDate = null;
	private boolean iTweakLimits = false;
	private boolean iLoadSectioningInfos = false;
    
    private Progress iProgress = null;

    public StudentSectioningDatabaseLoader(StudentSectioningModel model) {
        super(model);
        iIncludeCourseDemands = model.getProperties().getPropertyBoolean("Load.IncludeCourseDemands", iIncludeCourseDemands);
        iIncludeLastLikeStudents = model.getProperties().getPropertyBoolean("Load.IncludeLastLikeStudents", iIncludeLastLikeStudents);
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
    }
    
    public void load() {
        iProgress.setStatus("Loading input data ...");
        org.hibernate.Session hibSession = null;
        Transaction tx = null;
        try {
            hibSession = SessionDAO.getInstance().getSession();
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
                    room.getCoordinateX().intValue(),
                    room.getCoordinateY().intValue(),
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
        lecture.setBestAssignment(p);
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
            int projected = (co.getProjectedDemand()==null?0:co.getProjectedDemand().intValue());
            boolean unlimited = false;
            int limit = 0;
            for (Iterator<InstrOfferingConfig> j = io.getInstrOfferingConfigs().iterator(); j.hasNext(); ) {
            	InstrOfferingConfig ioc = j.next();
                if (ioc.isUnlimitedEnrollment()) unlimited = true;
                limit += ioc.getLimit();
            }
            for (Iterator<CourseOfferingReservation> k = co.getCourseReservations().iterator(); k.hasNext(); ) {
            	CourseOfferingReservation reservation = k.next();
                if (reservation.getCourseOffering().equals(co) && reservation.getReserved()!=null)
                    limit = reservation.getReserved();
            }
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
            Config config = new Config(ioc.getUniqueId(), courseName + " [" + ioc.getName() + "]", offering);
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
                		int roomLimit = Math.round((c.getRoomRatio() == null ? 1.0f : c.getRoomRatio()) * p.getRoomSize());
                		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
                	}
                    if (ioc.isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                    Section section = new Section(c.getUniqueId().longValue(), limit, (c.getExternalUniqueId() == null ? c.getClassSuffix() : c.getExternalUniqueId()), subpart, p,
                    		getInstructorIds(c), getInstructorNames(c), parentSection);
                    class2section.put(c.getUniqueId(), section);
                    classTable.put(c.getUniqueId(), section);
                }
            }
        }
        return offering;
    }
    
    public Student loadStudent(org.unitime.timetable.model.Student s, Hashtable<Long,Course> courseTable, Hashtable<Long,Section> classTable) {
        iProgress.debug("Loading student "+s.getUniqueId()+" (id="+s.getExternalUniqueId()+", name="+s.getName(DepartmentalInstructor.sNameFormatLastFist)+")");

        Student student = new Student(s.getUniqueId().longValue());
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
                        iProgress.warn("Student " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") requests course " + cr.getCourseOffering().getCourseName() + " that is not loaded.");
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
                        for (Iterator<StudentClassEnrollment> i = (cr.getClassEnrollments() == null ? s.getClassEnrollments() : cr.getClassEnrollments()).iterator(); i.hasNext(); ) {
                        	StudentClassEnrollment enrl = i.next();
                        	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                            if (section!=null) {
                                assignedSections.add(section);
                                if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                                	iProgress.error("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                                }
                                assignedConfig = section.getSubpart().getConfig();
                                if (!subparts.add(section.getSubpart().getId())) {
                                	iProgress.error("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                                }
                            } else {
                            	iProgress.error("There is a problem assigning " + course.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel() + " not known.");
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
                        cd.isWaitlist());
                request.getSelectedChoices().addAll(selChoices);
                request.getWaitlistedChoices().addAll(wlChoices);
                if (assignedConfig!=null && assignedSections.size() == assignedConfig.getSubparts().size()) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections);
                    request.setInitialAssignment(enrollment);
                }
                if (assignedConfig!=null && assignedSections.size() != assignedConfig.getSubparts().size()) {
                	iProgress.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") wrong number of classes (" +
                			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + ").");
                }
            }
        }

        if (student.getRequests().isEmpty() && !s.getClassEnrollments().isEmpty()) {
        	TreeSet<Course> courses = new TreeSet<Course>(new Comparator<Course>() {
        		public int compare(Course c1, Course c2) {
        			return (c1.getSubjectArea() + " " + c1.getCourseNumber()).compareTo(c2.getSubjectArea() + " " + c2.getCourseNumber());
        		}
        	});
        	for (Iterator<StudentClassEnrollment> i = s.getClassEnrollments().iterator(); i.hasNext(); ) {
        		StudentClassEnrollment enrl = i.next();
        		Course course = courseTable.get(enrl.getCourseOffering().getUniqueId());
                if (course==null) {
                    iProgress.warn("Student " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ") requests course " + enrl.getCourseOffering().getCourseName()+" that is not loaded.");
                    continue;
                }
                courses.add(course);
        	}
        	int priority = 0;
        	courses: for (Course course: courses) {
        		Vector<Course> cx = new Vector<Course>(); cx.add(course);
                CourseRequest request = new CourseRequest(
                        course.getId(),
                        priority++,
                        false,
                        student,
                        cx,
                        false);
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
                        	iProgress.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                        	continue courses;
                        }
                        assignedConfig = section.getSubpart().getConfig();
                        if (!subparts.add(section.getSubpart().getId())) {
                        	iProgress.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                        	continue courses;
                        }
                    } else {
                    	iProgress.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel() + " not known.");
                    	Section x = classTable.get(enrl.getClazz().getUniqueId());
                    	if (x != null) {
                    		iProgress.info("  but a class with the same id is loaded, but under offering " + x.getSubpart().getConfig().getOffering().getName() + " (id is " + x.getSubpart().getConfig().getOffering().getId() + 
                    				", expected " +course.getOffering().getId() + ")");
                    	}
                    	continue courses;
                    }
                }
                if (assignedConfig!=null && assignedSections.size() == assignedConfig.getSubparts().size()) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections);
                    request.setInitialAssignment(enrollment);
                }
                if (assignedConfig!=null && assignedSections.size() != assignedConfig.getSubparts().size()) {
                	iProgress.error("There is a problem assigning " + request.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + "): wrong number of classes (" +
                			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + ").");
                }
        	}
        }
        
        return student;
    }
    
    public void assignStudent(Student student, org.unitime.timetable.model.Student s) {
		for (Request r: student.getRequests()) {
			if (r instanceof CourseRequest && r.getInitialAssignment() != null) {
               if (r.getModel().conflictValues(r.getInitialAssignment()).isEmpty()) {
               	r.assign(0, r.getInitialAssignment());
               } else {
               	CourseRequest cr = (CourseRequest)r;
               	Enrollment enrl = (Enrollment)r.getInitialAssignment();
               	iProgress.error("There is a problem assigning " + cr.getName() + " to " + s.getName(DepartmentalInstructor.sNameFormatInitialLast) + " (" + s.getExternalUniqueId() + ")");
               	boolean hasLimit = false, hasOverlap = false;
               	sections: for (Iterator<Section> i = enrl.getSections().iterator(); i.hasNext();) {
               		Section section = i.next();
               		if (section.getTime() != null) {
                   		for (Request q: student.getRequests()) {
                   			if (q.getAssignment() == null || !(q instanceof CourseRequest)) continue;
                   			Enrollment enrlx = (Enrollment)q.getAssignment();
                   			for (Iterator<Section> j = enrlx.getSections().iterator(); j.hasNext();) {
                   				Section sectionx = j.next();
                   				if (sectionx.getTime() == null) continue;
                   				if (sectionx.getTime().hasIntersection(section.getTime())) {
                   					iProgress.info("  " + section.getSubpart().getName() + " " + section.getName() + " " + section.getTime().getLongName() +
                   							" overlaps with " + sectionx.getSubpart().getConfig().getOffering().getName() + " " + sectionx.getSubpart().getName() + " " +
                   							sectionx.getName() + " " + sectionx.getTime().getLongName());
                   					hasOverlap = true;
                   					continue sections;
                   				}
                   			}
                   		}
               		}
               		if (section.getLimit() >= section.getEnrollments().size()) {
       					iProgress.info("  " + section.getSubpart().getName() + " " + section.getName() + (section.getTime() == null ? "" : " " + section.getTime().getLongName()) +
       							" has no space available (limit is "+ section.getLimit() + ")");
       					if (iTweakLimits) {
       						section.setLimit(section.getEnrollments().size() + 1);
       						iProgress.info("    limit increased to "+section.getLimit());
       					}
               			hasLimit = true;
               			continue sections;
               		}
   					iProgress.info("  " + section.getSubpart().getName() + " " + section.getName() + (section.getTime() == null ? "" : " " + section.getTime().getLongName()));
               	}
               	if (!hasLimit && !hasOverlap) {
               		for (Iterator<Enrollment> i = r.getModel().conflictValues(r.getInitialAssignment()).iterator(); i.hasNext();) {
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
               	if (hasLimit && iTweakLimits && r.getModel().conflictValues(r.getInitialAssignment()).isEmpty()) {
                   	r.assign(0, r.getInitialAssignment());
               	}
               }
			}
		}
		for (Request r: student.getRequests()) {
			if (r instanceof FreeTimeRequest) {
				FreeTimeRequest ft = (FreeTimeRequest)r;
               Enrollment enrollment = ft.createEnrollment();
               if (r.getModel().conflictValues(enrollment).isEmpty()) {
                   ft.setInitialAssignment(enrollment);
                   ft.assign(0, enrollment);
               }
			}
		}
   }
    
    private void fixWeights() {
        Hashtable lastLike = new Hashtable();
        Hashtable real = new Hashtable();
        iProgress.setPhase("Computing last-like request weights...", 2*getModel().getStudents().size());
        for (Student student: getModel().getStudents()) {
        	iProgress.incProgress();
            for (Request request: student.getRequests()) {
                if (request instanceof CourseRequest) {
                    CourseRequest courseRequest = (CourseRequest)request;
                    Course course = (Course)courseRequest.getCourses().get(0);
                    Integer cnt = (Integer)(student.isDummy()?lastLike:real).get(course);
                    (student.isDummy()?lastLike:real).put(course, new Integer((cnt==null?0:cnt.intValue())+1));
                }
            }
        }
        for (Enumeration e=new Vector(getModel().getStudents()).elements();e.hasMoreElements();) {
            Student student = (Student)e.nextElement(); iProgress.incProgress();
            for (Enumeration f=new Vector(student.getRequests()).elements();f.hasMoreElements();) {
                Request request = (Request)f.nextElement();
                if (!student.isDummy()) {
                    request.setWeight(1.0); continue;
                }
                if (request instanceof CourseRequest) {
                    CourseRequest courseRequest = (CourseRequest)request;
                    Course course = (Course)courseRequest.getCourses().get(0);
                    Integer lastLikeCnt = (Integer)lastLike.get(course);
                    Integer realCnt = (Integer)real.get(course);
                    courseRequest.setWeight(Test.getLastLikeStudentWeight(course, realCnt==null?0:realCnt.intValue(), lastLikeCnt==null?0:lastLikeCnt.intValue()));
                } else request.setWeight(1.0);
                 if (request.getWeight()<=0.0) {
                    getModel().removeVariable(request);
                    student.getRequests().remove(request);
                }
            }
            if (student.getRequests().isEmpty()) {
                getModel().getStudents().remove(student);
            }
        }
    }
    
    public void loadLastLikeStudent(org.hibernate.Session hibSession, LastLikeCourseDemand d, org.unitime.timetable.model.Student s, Long courseOfferingId, Hashtable studentTable, Hashtable courseTable, Hashtable classTable, Hashtable classAssignments) {
        iProgress.debug("Loading last like demand of student "+s.getUniqueId()+" (id="+s.getExternalUniqueId()+", name="+s.getName(DepartmentalInstructor.sNameFormatLastFist)+") for "+courseOfferingId);
        Student student = (Student)studentTable.get(s.getUniqueId());
        if (student==null) {
            student = new Student(s.getUniqueId().longValue(),true);
            if (iLoadStudentInfo) loadStudentInfo(student,s);
            studentTable.put(s.getUniqueId(),student);
        }
        int priority = student.getRequests().size();
        Vector courses = new Vector();
        Course course = (Course)courseTable.get(courseOfferingId);
        if (course==null) {
            iProgress.warn("Course "+courseOfferingId+" not loaded");
            return;
        }
        courses.addElement(course);
        CourseRequest request = new CourseRequest(
                d.getUniqueId().longValue(),
                priority++,
                false,
                student,
                courses,
                false);
        iProgress.trace("added request "+request);
        if (classAssignments!=null && !classAssignments.isEmpty()) {
            HashSet assignedSections = new HashSet();
            HashSet classIds = (HashSet)classAssignments.get(s.getUniqueId());
            if (classIds!=null)
                for (Iterator i=classIds.iterator();i.hasNext();) {
                    Long classId = (Long)i.next();
                    Section section = (Section)request.getSection(classId.longValue());
                    if (section!=null) assignedSections.add(section);
                }
            if (!assignedSections.isEmpty()) {
                iProgress.trace("committed assignment: "+assignedSections);
                for (Enrollment enrollment: request.values()) {
                    if (enrollment.getAssignments().containsAll(assignedSections)) {
                        request.setInitialAssignment(enrollment);
                        iProgress.trace("found: "+enrollment);
                        break;
                    }
                }
            }
        }
    }
    
    public void loadStudentInfo(Student student, org.unitime.timetable.model.Student s) {
        HashSet majors = new HashSet();
        HashSet minors = new HashSet();
        for (Iterator i=s.getAcademicAreaClassifications().iterator();i.hasNext();) {
            AcademicAreaClassification aac = (AcademicAreaClassification)i.next();
            student.getAcademicAreaClasiffications().add(
                    new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),aac.getAcademicClassification().getCode()));
            iProgress.trace("aac: "+aac.getAcademicArea().getAcademicAreaAbbreviation()+":"+aac.getAcademicClassification().getCode());
            for (Iterator j=aac.getAcademicArea().getPosMajors().iterator();j.hasNext();) {
                PosMajor major = (PosMajor)j.next();
                if (s.getPosMajors().contains(major)) {
                    student.getMajors().add(
                            new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),major.getCode()));
                    majors.add(major);
                    iProgress.trace("mj: "+aac.getAcademicArea().getAcademicAreaAbbreviation()+":"+major.getCode());
                }
                    
            }
            for (Iterator j=aac.getAcademicArea().getPosMinors().iterator();j.hasNext();) {
                PosMinor minor = (PosMinor)j.next();
                if (s.getPosMinors().contains(minor)) {
                    student.getMinors().add(
                            new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),minor.getCode()));
                    minors.add(minor);
                    iProgress.trace("mn: "+aac.getAcademicArea().getAcademicAreaAbbreviation()+":"+minor.getCode());
                }
                    
            }
        }
        for (Iterator i=s.getPosMajors().iterator();i.hasNext();) {
            PosMajor major = (PosMajor)i.next();
            if (!majors.contains(major)) {
                student.getMajors().add(new AcademicAreaCode(null,major.getCode()));
                iProgress.trace("mj: "+major.getCode());
            }
        }
        for (Iterator i=s.getPosMinors().iterator();i.hasNext();) {
            PosMinor minor = (PosMinor)i.next();
            if (!minors.contains(minor)) {
                student.getMajors().add(new AcademicAreaCode(null,minor.getCode()));
                iProgress.trace("mn: "+minor.getCode());
            }
        }
    }
	public static BitSet getFreeTimeBitSet(Session session) {
		int startMonth = session.getStartMonth() - 3;
		int endMonth = session.getEndMonth() + 3;
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
        SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
    	return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    }

	public static Date getDatePatternFirstDay(Session s) {
		return DateUtils.getDate(1, s.getStartMonth() - 3, s.getSessionStartYear());
	}

    public void load(Session session, org.hibernate.Session hibSession) {
    	iFreeTimePattern = getFreeTimeBitSet(session);
		iDatePatternFirstDate = getDatePatternFirstDay(session);
    	
        Hashtable<Long, Course> courseTable = new Hashtable<Long, Course>();
        Hashtable<Long, Section> classTable = new Hashtable<Long, Section>();
        List offerings = hibSession.createQuery(
                "select distinct io from InstructionalOffering io " +
                "left join fetch io.courseOfferings as co "+
                "left join fetch io.instrOfferingConfigs as ioc "+
                "left join fetch ioc.schedulingSubparts as ss "+
                "left join fetch ss.classes as c "+
                "where " +
                "io.session.uniqueId=:sessionId and io.notOffered=false").
                setLong("sessionId",session.getUniqueId().longValue()).
                setFetchSize(1000).list();
        iProgress.setPhase("Loading course offerings...", offerings.size());
        for (Iterator i=offerings.iterator();i.hasNext();) {
            InstructionalOffering io = (InstructionalOffering)i.next(); iProgress.incProgress();
            if (io.isNotOffered()) continue;
            Offering offering = loadOffering(io, courseTable, classTable);
            if (offering!=null) getModel().addOffering(offering);
        }
        
        HashSet loadedStudentIds = new HashSet();
        if (iIncludeCourseDemands) {
            List students = hibSession.createQuery(
                    "select distinct s from Student s " +
                    "left join fetch s.courseDemands as cd "+
                    "left join fetch cd.courseRequests as cr "+
                    "left join fetch s.classEnrollments as e " +
                    "where s.session.uniqueId=:sessionId").
                    setLong("sessionId",session.getUniqueId().longValue()).
                    setFetchSize(1000).list();
            iProgress.setPhase("Loading student requests...", students.size());
            for (Iterator i=students.iterator();i.hasNext();) {
                org.unitime.timetable.model.Student s = (org.unitime.timetable.model.Student)i.next(); iProgress.incProgress();
                if (s.getCourseDemands().isEmpty() && s.getClassEnrollments().isEmpty()) continue;
                Student student = loadStudent(s, courseTable, classTable);
                if (student!=null)
                    getModel().addStudent(student);
                if (s.getExternalUniqueId()!=null)
                    loadedStudentIds.add(s.getExternalUniqueId());
            	assignStudent(student, s);
            }
        }
        
        if (iIncludeLastLikeStudents) {
            Hashtable classAssignments = null;
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
                    HashSet classIds = (HashSet)classAssignments.get(studentId);
                    if (classIds==null) {
                        classIds = new HashSet();
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
        			if (section.getLimit() >= 0 && (section.getLimit() - section.getEnrollments().size()) <= section.getSpaceExpected())
        				iProgress.info("Section " + section.getSubpart().getConfig().getOffering().getName() + " " + section.getSubpart().getName() + " " +
        						section.getName() + " has high demand (limit: " + section.getLimit() + ", enrollment: " + section.getEnrollments().size() +
        						", expected: " + section.getSpaceExpected() + ")");
        		}
        	}	
        }
        
        fixWeights();
        
        iProgress.setPhase("Done",1);iProgress.incProgress();
    }
    
    
}
