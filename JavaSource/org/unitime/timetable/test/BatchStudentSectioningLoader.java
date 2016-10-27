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
package org.unitime.timetable.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.studentsct.StudentSectioningLoader;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.Test;
import org.cpsolver.studentsct.model.AcademicAreaCode;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.hibernate.Transaction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
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
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.duration.DurationModel;


/**
 * @author Tomas Muller
 */
public class BatchStudentSectioningLoader extends StudentSectioningLoader {
    private static Log sLog = LogFactory.getLog(BatchStudentSectioningLoader.class);
    private boolean iIncludeCourseDemands = true;
    private boolean iIncludeLastLikeStudents = true;
    private boolean iIncludeUseCommittedAssignments = false;
    private boolean iMakeupAssignmentsFromRequiredPrefs = false;
    private boolean iLoadStudentInfo = false;
    private String iInitiative = null;
    private String iTerm = null;
    private String iYear = null;
    private long iMakeupAssignmentId = 0;
    private NameFormat iInstructorNameFormat;

    public BatchStudentSectioningLoader(StudentSectioningModel model, org.cpsolver.ifs.assignment.Assignment<Request, Enrollment> assignment) {
        super(model, assignment);
        iIncludeCourseDemands = model.getProperties().getPropertyBoolean("Load.IncludeCourseDemands", iIncludeCourseDemands);
        iIncludeLastLikeStudents = model.getProperties().getPropertyBoolean("Load.IncludeLastLikeStudents", iIncludeLastLikeStudents);
        iIncludeUseCommittedAssignments = model.getProperties().getPropertyBoolean("Load.IncludeUseCommittedAssignments", iIncludeUseCommittedAssignments);
        iLoadStudentInfo = model.getProperties().getPropertyBoolean("Load.LoadStudentInfo", iLoadStudentInfo);
        iMakeupAssignmentsFromRequiredPrefs = model.getProperties().getPropertyBoolean("Load.MakeupAssignmentsFromRequiredPrefs", iMakeupAssignmentsFromRequiredPrefs);
        iInitiative = model.getProperties().getProperty("Data.Initiative");
        iYear = model.getProperties().getProperty("Data.Year");
        iTerm = model.getProperties().getProperty("Data.Term");
        iInstructorNameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
    }
    
    public void load() throws Exception {
        Session session = Session.getSessionUsingInitiativeYearTerm(iInitiative, iYear, iTerm);
        
        if (session==null) throw new Exception("Session "+iInitiative+" "+iTerm+iYear+" not found!");
        
        sLog.info("Loading data for "+iInitiative+" "+iTerm+iYear+"...");
        
        load(session);
    }
    
    private List<Instructor> getInstructors(Class_ clazz) {
        if (!clazz.isDisplayInstructor().booleanValue()) return null;
        List<Instructor> ret = new ArrayList<Instructor>();
        TreeSet ts = new TreeSet(clazz.getClassInstructors());
        for (Iterator i=ts.iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (!ci.isLead().booleanValue()) continue;
            ret.add(new Instructor(ci.getInstructor().getUniqueId(), ci.getInstructor().getExternalUniqueId(), iInstructorNameFormat.format(ci.getInstructor()), ci.getInstructor().getEmail()));
        }
        return ret;
    }

    public TimeLocation makeupTime(Class_ c) {
        DatePattern datePattern = c.effectiveDatePattern(); 
        if (datePattern==null) {
            sLog.warn("        -- makup time for "+c.getClassLabel()+": no date pattern set");
            return null;
        }        
        for (Iterator i=c.getEffectiveTimePreferences().iterator();i.hasNext();) {
            TimePref tp = (TimePref)i.next();
            TimePatternModel pattern = tp.getTimePatternModel();
            if (pattern.isExactTime()) {
    			DurationModel dm = c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
    			int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(c.getSchedulingSubpart().getMinutesPerWk(), c.effectiveDatePattern(), pattern.getExactDays());
                int length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
                int breakTime = ExactTimeMins.getBreakTime(minsPerMeeting); 
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
            sLog.warn("        -- makup time for "+c.getClassLabel()+": no time preference set");
        else
            sLog.warn("        -- makup time for "+c.getClassLabel()+": no required time set");
        return null;
    }
    
    public Vector makeupRooms(Class_ c) {
        Vector rooms = new Vector();
        for (Iterator i=c.getEffectiveRoomPreferences().iterator();i.hasNext();) {
            RoomPref rp = (RoomPref)i.next();
            if (!PreferenceLevel.sRequired.equals(rp.getPrefLevel().getPrefProlog())) {
                sLog.warn("        -- makup room for "+c.getClassLabel()+": preference for "+rp.getRoom().getLabel()+" is not required");
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
        sLog.debug("        -- makup placement for "+c.getClassLabel()+": "+p.getLongName(true));
        return p;
    }
    
    private Section loadSection(Subpart subpart, Section parentSection, Class_ c, int limit) {
        Placement p = null;
        if (iMakeupAssignmentsFromRequiredPrefs) {
            p = makeupPlacement(c);
        } else {
            Assignment a = c.getCommittedAssignment();
            p = (a==null?null:a.getPlacement());
        }
        Section section = new Section(c.getUniqueId().longValue(), limit, c.getClassLabel(), subpart, p, getInstructors(c), parentSection);
        if (section.getTime()!=null && section.getTime().getDatePatternId().equals(c.getSession().getDefaultDatePattern().getUniqueId()))
            section.getTime().setDatePattern(section.getTime().getDatePatternId(),"",section.getTime().getWeekCode());
        if (section.getTime()!=null && section.getTime().getDatePatternName().startsWith("generated")) {
        	Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
            section.getTime().setDatePattern(
                    section.getTime().getDatePatternId(), 
                    sdf.format(c.effectiveDatePattern().getStartDate())+" - "+sdf.format(c.effectiveDatePattern().getEndDate()), 
                    section.getTime().getWeekCode());
        }
        return section;
    }

    private Offering loadOffering(InstructionalOffering io, Hashtable courseTable, Hashtable classTable) {
        sLog.debug("Loading offering "+io.getCourseName());
        if (!io.hasClasses()) {
            sLog.debug("  -- offering "+io.getCourseName()+" has no class");
            return null;
        }
        Offering offering = new Offering(io.getUniqueId().longValue(), io.getCourseName());
        boolean unlimited = false;
        for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();) {
            InstrOfferingConfig ioc = (InstrOfferingConfig)i.next();
            if (ioc.isUnlimitedEnrollment().booleanValue()) unlimited = true;
        }
        for (Iterator i=io.getCourseOfferings().iterator();i.hasNext();) {
            CourseOffering co = (CourseOffering)i.next();
            int projected = (co.getProjectedDemand()==null?0:co.getProjectedDemand().intValue());
            int limit = co.getInstructionalOffering().getLimit().intValue();
            if (unlimited) limit=-1;
            if (co.getReservation() != null)
            	limit = co.getReservation();
            Course course = new Course(co.getUniqueId().longValue(), co.getSubjectAreaAbbv(), co.getCourseNbr(), offering, limit, projected);
            courseTable.put(co.getUniqueId(), course);
            sLog.debug("  -- created course "+course);
        }
        Hashtable class2section = new Hashtable();
        Hashtable ss2subpart = new Hashtable();
        for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();) {
            InstrOfferingConfig ioc = (InstrOfferingConfig)i.next();
            if (!ioc.hasClasses()) {
                sLog.debug("  -- config "+ioc.getName()+" has no class");
                continue;
            }
            Config config = new Config(ioc.getUniqueId().longValue(), (ioc.isUnlimitedEnrollment() ? -1 : ioc.getLimit()), ioc.getCourseName()+" ["+ioc.getName()+"]", offering);
            if (ioc.getInstructionalMethod() != null) {
            	config.setInstructionalMethodId(ioc.getInstructionalMethod().getUniqueId());
            	config.setInstructionalMethodName(ioc.getInstructionalMethod().getLabel());
            }
            sLog.debug("  -- created config "+config);
            TreeSet subparts = new TreeSet(new SchedulingSubpartComparator());
            subparts.addAll(ioc.getSchedulingSubparts());
            for (Iterator j=subparts.iterator();j.hasNext();) {
                SchedulingSubpart ss = (SchedulingSubpart)j.next();
                String sufix = ss.getSchedulingSubpartSuffix();
                Subpart parentSubpart = (ss.getParentSubpart()==null?null:(Subpart)ss2subpart.get(ss.getParentSubpart()));
                if (ss.getParentSubpart()!=null && parentSubpart==null) {
                    sLog.error("    -- subpart "+ss.getSchedulingSubpartLabel()+" has parent "+ss.getParentSubpart().getSchedulingSubpartLabel()+", but the appropriate parent subpart is not loaded.");
                }
                Subpart subpart = new Subpart(ss.getUniqueId().longValue(), ss.getItype().getItype().toString()+sufix, ss.getItypeDesc().trim()+(sufix==null || sufix.length()==0?"":" ("+sufix+")"), config, parentSubpart);
                subpart.setAllowOverlap(ss.isStudentAllowOverlap());
                ss2subpart.put(ss, subpart);
                sLog.debug("    -- created subpart "+subpart);
                for (Iterator k=ss.getClasses().iterator();k.hasNext();) {
                    Class_ c = (Class_)k.next();
                    int limit = c.getClassLimit();
                    if (ioc.isUnlimitedEnrollment().booleanValue()) limit = -1;
                    if (!c.isEnabledForStudentScheduling()) limit = 0;
                    Section parentSection = (c.getParentClass()==null?null:(Section)class2section.get(c.getParentClass()));
                    if (c.getParentClass()!=null && parentSection==null) {
                        sLog.error("    -- class "+c.getClassLabel()+" has parent "+c.getParentClass().getClassLabel()+", but the appropriate parent section is not loaded.");
                    }
                    Section section = loadSection(subpart, parentSection, c, limit);
                    class2section.put(c, section);
                    classTable.put(c.getUniqueId(), section);
                    sLog.debug("      -- created section "+section);
                }
            }
        }
        return offering;
    }
    
    public Student loadStudent(org.unitime.timetable.model.Student s, Hashtable courseTable, Hashtable classTable) {
        sLog.debug("Loading student "+s.getUniqueId()+" (id="+s.getExternalUniqueId()+", name="+s.getFirstName()+" "+s.getMiddleName()+" "+s.getLastName()+")");
        Student student = new Student(s.getUniqueId().longValue());
        if (iLoadStudentInfo) loadStudentInfo(student,s);
        int priority = 0;
        for (Iterator i=new TreeSet(s.getCourseDemands()).iterator();i.hasNext();) {
            CourseDemand cd = (CourseDemand)i.next();
            if (cd.getFreeTime()!=null) {
                Request request = new FreeTimeRequest(
                        cd.getUniqueId().longValue(),
                        priority++,
                        cd.isAlternative().booleanValue(),
                        student,
                        new TimeLocation(
                                cd.getFreeTime().getDayCode().intValue(),
                                cd.getFreeTime().getStartSlot().intValue(),
                                cd.getFreeTime().getLength().intValue(),
                                0, 0, 
                                s.getSession().getDefaultDatePattern().getUniqueId(),
                                "",
                                s.getSession().getDefaultDatePattern().getPatternBitSet(),
                                0)
                        );
                sLog.debug("  -- added request "+request);
            } else if (!cd.getCourseRequests().isEmpty()) {
                Vector courses = new Vector();
                HashSet selChoices = new HashSet();
                HashSet wlChoices = new HashSet();
                HashSet assignedSections = new HashSet();
                Config assignedConfig = null;
                for (Iterator j=new TreeSet(cd.getCourseRequests()).iterator();j.hasNext();) {
                    org.unitime.timetable.model.CourseRequest cr = (org.unitime.timetable.model.CourseRequest)j.next();
                    Course course = (Course)courseTable.get(cr.getCourseOffering().getUniqueId());
                    if (course==null) {
                        sLog.warn("  -- course "+cr.getCourseOffering().getCourseName()+" not loaded");
                        continue;
                    }
                    if (assignedConfig==null) {
                        for (Iterator k=cr.getClassEnrollments().iterator();k.hasNext();) {
                            StudentClassEnrollment sce = (StudentClassEnrollment)k.next();
                            Section section = course.getOffering().getSection(sce.getClazz().getUniqueId().longValue());
                            if (section!=null) {
                                assignedSections.add(section);
                                assignedConfig = section.getSubpart().getConfig();
                            }
                        }
                    }
                    courses.addElement(course);
                }
                if (courses.isEmpty()) continue;
                CourseRequest request = new CourseRequest(
                        cd.getUniqueId().longValue(),
                        priority++,
                        cd.isAlternative().booleanValue(),
                        student,
                        courses,
                        cd.isWaitlist().booleanValue(),
                        cd.getTimestamp().getTime());
                request.getSelectedChoices().addAll(selChoices);
                request.getWaitlistedChoices().addAll(wlChoices);
                if (assignedConfig!=null && assignedSections.size()==assignedConfig.getSubparts().size()) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections, getAssignment());
                    request.setInitialAssignment(enrollment);
                }
                sLog.debug("  -- added request "+request);
            } else {
                sLog.warn("  -- course demand "+cd.getUniqueId()+" has no course requests");
            }
        }
        
        return student;
    }
    
    
    private void fixWeights() {
        Hashtable lastLike = new Hashtable();
        Hashtable real = new Hashtable();
        for (Student student: getModel().getStudents()) {
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
            Student student = (Student)e.nextElement();
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
        sLog.debug("Loading last like demand of student "+s.getUniqueId()+" (id="+s.getExternalUniqueId()+", name="+s.getFirstName()+" "+s.getMiddleName()+" "+s.getLastName()+") for "+courseOfferingId);
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
            sLog.warn("  -- course "+courseOfferingId+" not loaded");
            return;
        }
        courses.addElement(course);
        CourseRequest request = new CourseRequest(
                d.getUniqueId().longValue(),
                priority++,
                false,
                student,
                courses,
                false,
                null);
        sLog.debug("  -- added request "+request);
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
                sLog.debug("    -- committed assignment: "+assignedSections);
                for (Enrollment enrollment: request.values(getAssignment())) {
                    if (enrollment.getAssignments().containsAll(assignedSections)) {
                        request.setInitialAssignment(enrollment);
                        sLog.debug("      -- found: "+enrollment);
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
            sLog.debug("  -- aac: "+aac.getAcademicArea().getAcademicAreaAbbreviation()+":"+aac.getAcademicClassification().getCode());
            for (Iterator j=aac.getAcademicArea().getPosMajors().iterator();j.hasNext();) {
                PosMajor major = (PosMajor)j.next();
                if (s.getPosMajors().contains(major)) {
                    student.getMajors().add(
                            new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),major.getCode()));
                    majors.add(major);
                    sLog.debug("  -- mj: "+aac.getAcademicArea().getAcademicAreaAbbreviation()+":"+major.getCode());
                }
                    
            }
            for (Iterator j=aac.getAcademicArea().getPosMinors().iterator();j.hasNext();) {
                PosMinor minor = (PosMinor)j.next();
                if (s.getPosMinors().contains(minor)) {
                    student.getMinors().add(
                            new AcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(),minor.getCode()));
                    minors.add(minor);
                    sLog.debug("  -- mn: "+aac.getAcademicArea().getAcademicAreaAbbreviation()+":"+minor.getCode());
                }
                    
            }
        }
        for (Iterator i=s.getPosMajors().iterator();i.hasNext();) {
            PosMajor major = (PosMajor)i.next();
            if (!majors.contains(major)) {
                student.getMajors().add(new AcademicAreaCode(null,major.getCode()));
                sLog.debug("  -- mj: "+major.getCode());
            }
        }
        for (Iterator i=s.getPosMinors().iterator();i.hasNext();) {
            PosMinor minor = (PosMinor)i.next();
            if (!minors.contains(minor)) {
                student.getMajors().add(new AcademicAreaCode(null,minor.getCode()));
                sLog.debug("  -- mj: "+minor.getCode());
            }
        }
    }

    public void load(Session session) {
        org.hibernate.Session hibSession = new SessionDAO().getSession();
        Transaction tx = hibSession.beginTransaction();
        
        try {
            
            Hashtable courseTable = new Hashtable();
            Hashtable classTable = new Hashtable();
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
            for (Iterator i=offerings.iterator();i.hasNext();) {
                InstructionalOffering io = (InstructionalOffering)i.next(); 
                Offering offering = loadOffering(io, courseTable, classTable);
                if (offering!=null) getModel().addOffering(offering);
            }
            
            HashSet loadedStudentIds = new HashSet();
            if (iIncludeCourseDemands) {
                List students = hibSession.createQuery(
                        "select distinct s from Student s " +
                        "left join fetch s.courseDemands as cd "+
                        "left join fetch cd.courseRequests as cr "+
                        "where s.session.uniqueId=:sessionId").
                        setLong("sessionId",session.getUniqueId().longValue()).
                        setFetchSize(1000).list();
                for (Iterator i=students.iterator();i.hasNext();) {
                    org.unitime.timetable.model.Student s = (org.unitime.timetable.model.Student)i.next();
                    if (s.getCourseDemands().isEmpty()) continue;
                    Student student = loadStudent(s, courseTable, classTable);
                    if (student!=null)
                        getModel().addStudent(student);
                    if (s.getExternalUniqueId()!=null)
                        loadedStudentIds.add(s.getExternalUniqueId());
                }
            }
            
            if (iIncludeLastLikeStudents) {
                Hashtable classAssignments = null;
                if (iIncludeUseCommittedAssignments) {
                    classAssignments = new Hashtable();
                    for (Iterator i=hibSession.createQuery("select distinct se.studentId, se.clazz.uniqueId from StudentEnrollment se where "+
                            "se.solution.commited=true and se.solution.owner.session.uniqueId=:sessionId").
                            setLong("sessionId",session.getUniqueId().longValue()).iterate();i.hasNext();) {
                        Object[] o = (Object[])i.next();
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
            
                Hashtable lastLikeStudentTable = new Hashtable();
                for (Iterator i=hibSession.createQuery(
                    "select d, c.uniqueId from LastLikeCourseDemand d left join fetch d.student s, CourseOffering c left join c.demandOffering cx " +
                    "where d.subjectArea.session.uniqueId=:sessionId and c.subjectArea.session.uniqueId=:sessionId and " +
                    "((c.permId=null and d.subjectArea=c.subjectArea and d.courseNbr=c.courseNbr ) or "+
                    " (c.permId!=null and c.permId=d.coursePermId) or "+
                    " (cx.permId=null and d.subjectArea=cx.subjectArea and d.courseNbr=cx.courseNbr) or "+
                    " (cx.permId!=null and cx.permId=d.coursePermId)) "+
                    "order by s.uniqueId, d.priority, d.uniqueId").
                    setLong("sessionId",session.getUniqueId().longValue()).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    LastLikeCourseDemand d = (LastLikeCourseDemand)o[0];
                    org.unitime.timetable.model.Student s = (org.unitime.timetable.model.Student)d.getStudent();
                    Long courseOfferingId = (Long)o[1];
                    if (s.getExternalUniqueId()!=null && loadedStudentIds.contains(s.getExternalUniqueId())) continue;
                    loadLastLikeStudent(hibSession, d, s, courseOfferingId, lastLikeStudentTable, courseTable, classTable, classAssignments);
                }
                for (Enumeration e=lastLikeStudentTable.elements();e.hasMoreElements();) {
                    Student student = (Student)e.nextElement();
                    getModel().addStudent(student);
                }
                if (classAssignments!=null && !classAssignments.isEmpty()) {
                    for (Request request: getModel().variables()) {
                        if (request.getInitialAssignment()==null) continue;
                        Set conflicts = getModel().conflictValues(getAssignment(), request.getInitialAssignment());
                        if (conflicts.isEmpty())
                        	getAssignment().assign(0, request.getInitialAssignment());
                        else
                            sLog.debug("Unable to assign "+request.getInitialAssignment()+", conflicts: "+conflicts);
                    }
                }
                fixWeights();
            }
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        } finally {
            hibSession.close();
        }
    }
    
    
}
