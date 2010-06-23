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
package org.unitime.timetable.test;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.model.Model;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.StudentSctBBTest;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class StudentSectioningTest {
    private static Log sLog = LogFactory.getLog(StudentSectioningTest.class);
    private static DecimalFormat sTwoNumbersDF = new DecimalFormat("00");
    private static boolean sShuffle = false;
    private static double sAvailableThreshold = 0.001;
    
    public static Document testSectioning(Document request) {
        Element root = request.getRootElement();
        if (!"request".equals(root.getName())) {
            sLog.error("Root element is not 'request'.");
            return null;
        }
        String acadInitiative = root.attributeValue("campus");
        String acadYear = root.attributeValue("year");
        String acadTerm = root.attributeValue("term");
        Session session = Session.getSessionUsingInitiativeYearTerm(acadInitiative, acadYear, acadTerm);
        if (session==null) {
            sLog.error("Session not found.");
            return null;
        }
        Document response = DocumentHelper.createDocument();
        Element responseEl = response.addElement("response");
        responseEl.addAttribute("campus", acadInitiative);
        responseEl.addAttribute("year", acadYear);
        responseEl.addAttribute("term", acadTerm);
        responseEl.addAttribute("version", "1.1");
        responseEl.addAttribute("timestamp", new Date().toString());
        List students = root.elements("student");
        if (sShuffle) {
            students = new Vector(students);
            Collections.shuffle(students);
        }
        for (Iterator i=students.iterator();i.hasNext();) {
            testSectioning((Element)i.next(), responseEl, session);
        }
        return response;
    }
    
    private static HashSet generateAvailableChoices(Offering offering, Random rnd, double availProb) {
        HashSet ret = new HashSet();
        for (Iterator e=offering.getConfigs().iterator();e.hasNext();) {
            Config config = (Config)e.next();
            HashSet touchedSubparts = new HashSet();
            Vector subparts = new Vector(config.getSubparts());
            for (int i=subparts.size()-1;i>=0;i--) {
                Subpart subpart = (Subpart)subparts.elementAt(i);
                if (touchedSubparts.add(subpart)) {
                    boolean added = false;
                    for (Iterator f=subpart.getChoices().iterator();f.hasNext();) {
                        Choice choice = (Choice)f.next();
                        if (rnd.nextDouble()<availProb) {
                            Vector sections = new Vector(choice.getSections());
                            Section section = (Section)sections.elementAt((int)rnd.nextDouble()*sections.size());
                            while (section!=null) {
                                added = true;
                                ret.add(section.getChoice());
                                touchedSubparts.add(section.getSubpart());
                                section = section.getParent();
                            }
                        }
                    }
                    if (!added && subpart.getSections().size()>0) {
                        Section section = (Section)subpart.getSections().get((int)rnd.nextDouble()*subpart.getSections().size());
                        while (section!=null) {
                            ret.add(section.getChoice());
                            touchedSubparts.add(section.getSubpart());
                            section = section.getParent();
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    private static TimeLocation makeTime(DatePattern dp, String days, String startTime, String endTime, String length) {
        int dayCode = 0, idx = 0;
        for (int i=0;i<Constants.DAY_NAMES_SHORT.length;i++) {
            if (days.startsWith(Constants.DAY_NAMES_SHORT[i], idx)) {
                dayCode += Constants.DAY_CODES[i];
                idx += Constants.DAY_NAMES_SHORT[i].length();
            }
        }
        int startSlot = (((Integer.parseInt(startTime)/100)*60 + Integer.parseInt(startTime)%100) - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
        int breakTime = 0;
        int nrSlots = 0;
        if (length!=null) {
            breakTime = Integer.parseInt(length) - ((Integer.parseInt(endTime)/100)*60 + Integer.parseInt(endTime)%100) + ((Integer.parseInt(startTime)/100)*60 + Integer.parseInt(startTime)%100);
            nrSlots = Integer.parseInt(length) / Constants.SLOT_LENGTH_MIN; 
        } else {
            nrSlots = ((Integer.parseInt(endTime)/100)*60 + Integer.parseInt(endTime)%100) - ((Integer.parseInt(startTime)/100)*60 + Integer.parseInt(startTime)%100) / Constants.SLOT_LENGTH_MIN;
        }
        return new TimeLocation(
                dayCode,
                startSlot,
                nrSlots,
                0,
                0,
                dp.getUniqueId(),
                dp.getName(),
                dp.getPatternBitSet(),
                0);
    }
    
    public static String dayCode2days(int dayCode) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<Constants.DAY_CODES.length;i++)
            if ((dayCode & Constants.DAY_CODES[i])!=0)
                sb.append(Constants.DAY_NAMES_SHORT[i]);
        return sb.toString(); 
    }
    
    public static String startSlot2startTime(int startSlot) {
        int minHrs = startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }
    
    public static String timeLocation2endTime(TimeLocation time) {
        int minHrs = (time.getStartSlot()+time.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - time.getBreakTime();
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }
    
    private static String getInstructorIds(Class_ clazz) {
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
    
    private static String getInstructorNames(Class_ clazz) {
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
    
    public static double getPenalty(Section section, Class_ clazz, long studentId) {
        for (Iterator i=clazz.getStudentEnrollments().iterator();i.hasNext();) {
            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
            if (Long.parseLong(sce.getStudent().getExternalUniqueId())==studentId)
                return 0.0; //student already enrolled in this section
        }
        
        return section.getOnlineSectioningPenalty();
    }
    
    public static void updateSectioningInfos(org.hibernate.Session hibSession, org.unitime.timetable.model.Student s, CourseRequest courseRequest) {
        if (courseRequest.getAssignment()==null) return; //not enrolled --> no update
        Course course = null;
        for (Iterator i=s.getClassEnrollments().iterator();i.hasNext() && course!=null;) {
            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
            Section section = courseRequest.getSection(sce.getClazz().getUniqueId().longValue());
            if (section!=null) course = section.getSubpart().getConfig().getOffering().getCourse(courseRequest.getStudent());
        }
        if (course!=null) return; //student was already enrolled in the course --> no update
        
        Enrollment enrollment = (Enrollment)courseRequest.getAssignment();
        for (Iterator i=enrollment.getAssignments().iterator();i.hasNext();) {
            Section section = (Section)i.next();
            Class_ clazz = new Class_DAO().get(new Long(section.getId()));
            SectioningInfo si = clazz.getSectioningInfo();
            if (si==null || Math.round(si.getNbrHoldingStudents().doubleValue())<1) continue;
            si.setNbrHoldingStudents(new Double(si.getNbrHoldingStudents().doubleValue()-1));
            hibSession.saveOrUpdate(si);
            sLog.debug("  -- hold for "+clazz.getClassLabel()+" decreased by 1 (to "+si.getNbrHoldingStudents()+")");
       }
        Vector feasibleEnrollments = new Vector();
        for (Iterator g=courseRequest.values().iterator();g.hasNext();) {
            Enrollment enrl = (Enrollment)g.next();
            boolean overlaps = false;
            for (Iterator h=courseRequest.getStudent().getRequests().iterator();h.hasNext();) {
                Request otherRequest = (Request)h.next();
                if (otherRequest instanceof CourseRequest) {
                    CourseRequest otherCourseRequest = (CourseRequest)otherRequest;
                    if (otherCourseRequest.equals(courseRequest)) continue;
                    Enrollment otherErollment = (Enrollment)otherCourseRequest.getAssignment();
                    if (otherErollment==null) continue;
                    if (enrl.isOverlapping(otherErollment)) {
                        overlaps = true; break;
                    }
                }
            }
            if (!overlaps)
                feasibleEnrollments.add(enrl);
        }
        double decrement = courseRequest.getWeight() / feasibleEnrollments.size();
        for (Iterator g=feasibleEnrollments.iterator();g.hasNext();) {
            Enrollment feasibleEnrollment = (Enrollment)g.next();
            for (Iterator i=feasibleEnrollment.getAssignments().iterator();i.hasNext();) {
                Section section = (Section)i.next();
                Class_ clazz = new Class_DAO().get(new Long(section.getId()));
                SectioningInfo si = clazz.getSectioningInfo();
                if (si==null || si.getNbrExpectedStudents().doubleValue()<=0.0) continue;
                si.setNbrExpectedStudents(new Double(si.getNbrExpectedStudents().doubleValue()-decrement));
                hibSession.saveOrUpdate(si);
                sLog.debug("  -- expected for "+clazz.getClassLabel()+" decreased by "+decrement+" (to "+si.getNbrExpectedStudents()+")");
            }
        }
    }

    private static Course loadCourse(CourseOffering co, long studentId) {
        sLog.debug("  -- loading "+co.getCourseName());
        Offering offering = new Offering(co.getInstructionalOffering().getUniqueId().longValue(), co.getInstructionalOffering().getCourseName());
        int projected = (co.getProjectedDemand()==null?0:co.getProjectedDemand().intValue());
        int courseLimit = co.getInstructionalOffering().getLimit().intValue();
        for (Iterator j=co.getInstructionalOffering().getCourseReservations().iterator();j.hasNext();) {
            CourseOfferingReservation reservation = (CourseOfferingReservation)j.next();
            if (reservation.getCourseOffering().equals(co) && reservation.getReserved()!=null)
                courseLimit = reservation.getReserved().intValue();
        }
        Course course = new Course(co.getUniqueId().longValue(), co.getSubjectAreaAbbv(), co.getCourseNbr(), offering, courseLimit, projected);
        Hashtable class2section = new Hashtable();
        Hashtable ss2subpart = new Hashtable();
        for (Iterator i=co.getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
            InstrOfferingConfig ioc = (InstrOfferingConfig)i.next();
            Config config = new Config(ioc.getUniqueId().longValue(), ioc.getCourseName()+" ["+ioc.getName()+"]", offering);
            TreeSet subparts = new TreeSet(new SchedulingSubpartComparator());
            subparts.addAll(ioc.getSchedulingSubparts());
            for (Iterator j=subparts.iterator();j.hasNext();) {
                SchedulingSubpart ss = (SchedulingSubpart)j.next();
                String sufix = ss.getSchedulingSubpartSuffix();
                Subpart parentSubpart = (ss.getParentSubpart()==null?null:(Subpart)ss2subpart.get(ss.getParentSubpart()));
                Subpart subpart = new Subpart(ss.getUniqueId().longValue(), ss.getItype().getItype().toString()+sufix, ss.getItypeDesc().trim()+(sufix==null || sufix.length()==0?"":" ("+sufix+")"), config, parentSubpart);
                ss2subpart.put(ss, subpart);
                for (Iterator k=ss.getClasses().iterator();k.hasNext();) {
                    Class_ c = (Class_)k.next();
                    Assignment a = c.getCommittedAssignment();
                    int usedSpace = 0;
                    if (studentId>=0)
                        for (Iterator l=c.getStudentEnrollments().iterator();l.hasNext();) {
                            StudentClassEnrollment sce = (StudentClassEnrollment)l.next();
                            if (Long.parseLong(sce.getStudent().getExternalUniqueId())!=studentId) usedSpace++;
                        }
                    /*
                    Number usedSpace = (Number)new Class_DAO().getSession().createQuery(
                            "select count(sce) from StudentClassEnrollment sce where " +
                            "sce.clazz.uniqueId=:classId and sce.student.externalUniqueId!=:studentId").
                            setLong("classId", c.getUniqueId()).
                            setLong("studentId", studentId).uniqueResult();
                    if (studentId<0) usedSpace = new Integer(0);
                    */
                    int limit = c.getClassLimit() - usedSpace;
                    if (ioc.isUnlimitedEnrollment().booleanValue()) limit = -1;
                    Section parentSection = (c.getParentClass()==null?null:(Section)class2section.get(c.getParentClass()));
                    Section section = new Section(c.getUniqueId().longValue(), limit, c.getClassLabel(), subpart, (a==null?null:a.getPlacement()), getInstructorIds(c), getInstructorNames(c), parentSection);
                    if (c.getSectioningInfo()!=null) {
                        section.setSpaceExpected(c.getSectioningInfo().getNbrExpectedStudents().doubleValue());
                        section.setSpaceHeld(c.getSectioningInfo().getNbrHoldingStudents().doubleValue());
                        section.setPenalty(getPenalty(section, c, studentId));
                    }
                    class2section.put(c, section);
                    sLog.debug("    -- section "+section);
                }
            }
        }
        return course;
    }
    
    private static void exportDependencies(Element choiceEl, Choice choice, Set parentSections) {
        if (parentSections==null || parentSections.isEmpty()) {
            if (choice.getOffering().getConfigs().size()==1) return;
            HashSet configs = new HashSet();
            for (Iterator i=choice.getSections().iterator();i.hasNext();) {
                Section section = (Section)i.next();
                configs.add(section.getSubpart().getConfig());
            }
            if (choice.getOffering().getConfigs().size()==configs.size()) return;
            HashSet depends = new HashSet();
            for (Iterator e=choice.getOffering().getConfigs().iterator();e.hasNext();) {
                Config config = (Config)e.next();
                if (!configs.contains(config)) continue;
                Subpart subpartThisConfig = null;
                for (Iterator f=config.getSubparts().iterator();f.hasNext();) {
                    Subpart subpart = (Subpart)f.next();
                    if (subpart.getInstructionalType().equals(choice.getInstructionalType())) {
                        subpartThisConfig = subpart; break;
                    }
                }
                for (Iterator f=config.getSubparts().iterator();f.hasNext();) {
                    Subpart subpart = (Subpart)f.next();
                    if (subpart.compareTo(subpartThisConfig)>=0) continue;
                    if (subpart.getParent()!=null) continue;
                    for (Iterator g=subpart.getSections().iterator();g.hasNext();) {
                        Section section = (Section)g.next();
                        if (depends.add(section.getChoice())) {
                            Element depEl = choiceEl.addElement("depends");
                            depEl.addAttribute("class", section.getChoice().getInstructionalType());
                            depEl.addAttribute("choice", section.getChoice().getId());
                            //depEl.addAttribute("name", section.getChoice().getName());
                        }
                    }
                }                
            }
        } else {
            HashSet parentChoices = new HashSet();
            for (Iterator i=parentSections.iterator();i.hasNext();) {
                Section parentSection = (Section)i.next();
                parentChoices.add(parentSection.getChoice());
            }
            for (Iterator i=parentChoices.iterator();i.hasNext();) {
                Choice parentChoice = (Choice)i.next();
                Element depEl = choiceEl.addElement("depends");
                depEl.addAttribute("class", parentChoice.getInstructionalType());
                depEl.addAttribute("choice", parentChoice.getId());
                //depEl.addAttribute("name", parentChoice.getName());
                HashSet parentParentSections = new HashSet();
                for (Iterator j=parentSections.iterator();j.hasNext();) {
                    Section parentSection = (Section)j.next();
                    if (parentSection.getChoice().equals(parentChoice) && parentSection.getParent()!=null)
                        parentParentSections.add(parentSection.getParent());
                }
                exportDependencies(depEl, parentChoice, parentParentSections);
            }
        }
    }
    
    private static void loadStudent(Session session, Student student, Vector messages) {
        org.unitime.timetable.model.Student s = org.unitime.timetable.model.Student.findByExternalId(session.getUniqueId(), String.valueOf(student.getId()));
        if (s==null) {
            sLog.warn("  Student "+student+" not found.");
            return;
        }
        
        int priority = 0;
        for (Iterator i=new TreeSet(s.getCourseDemands()).iterator();i.hasNext();) {
            CourseDemand cd = (CourseDemand)i.next();
            Request request = null;
            if (cd.getFreeTime()!=null) {
                request = new FreeTimeRequest(
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
                                s.getSession().getDefaultDatePattern().getName(),
                                s.getSession().getDefaultDatePattern().getPatternBitSet(),
                                cd.getFreeTime().getCategory().intValue())
                        );
                sLog.info("  -- added request "+request);
            } else if (!cd.getCourseRequests().isEmpty()) {
                Vector courses = new Vector();
                HashSet wlChoices = new HashSet();
                HashSet selChoices = new HashSet();
                HashSet assignedSections = new HashSet();
                Config assignedConfig = null;
                for (Iterator j=new TreeSet(cd.getCourseRequests()).iterator();j.hasNext();) {
                    org.unitime.timetable.model.CourseRequest cr = (org.unitime.timetable.model.CourseRequest)j.next();
                    Course course = loadCourse(cr.getCourseOffering(), student.getId());
                    courses.addElement(course);
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
                        for (Iterator k=cr.getClassEnrollments().iterator();k.hasNext();) {
                            StudentClassEnrollment sce = (StudentClassEnrollment)k.next();
                            Section section = course.getOffering().getSection(sce.getClazz().getUniqueId().longValue());
                            if (section!=null) {
                                assignedSections.add(section);
                                assignedConfig = section.getSubpart().getConfig();
                            }
                        }
                    }
                }
                if (courses.isEmpty()) continue;
                request = new CourseRequest(
                        cd.getUniqueId().longValue(),
                        priority++,
                        cd.isAlternative().booleanValue(),
                        student,
                        courses,
                        cd.isWaitlist().booleanValue());
                ((CourseRequest)request).getWaitlistedChoices().addAll(wlChoices);
                ((CourseRequest)request).getSelectedChoices().addAll(selChoices);
                if (assignedConfig!=null && assignedSections.size()==assignedConfig.getSubparts().size()) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections);
                    request.setInitialAssignment(enrollment);
                }
                sLog.info("  -- added request "+request);
            } else {
                sLog.warn("  -- course demand "+cd.getUniqueId()+" has no course requests");
            }
            if (request!=null) {
                for (Iterator k=new TreeSet(cd.getEnrollmentMessages()).iterator();k.hasNext();) {
                    StudentEnrollmentMessage m = (StudentEnrollmentMessage)k.next();
                    messages.add(new StudentSctBBTest.Message(m.getLevel().intValue(),request,m.getMessage()));
                }
            }
        }
    }
    
    public static void saveStudent(Session session, Student student, Vector messages) {
        org.unitime.timetable.model.Student s = org.unitime.timetable.model.Student.findByExternalId(session.getUniqueId(), String.valueOf(student.getId()));
        if (s==null) {
            sLog.warn("  Student "+student+" not found.");
            return;
        }
        org.hibernate.Session hibSession = new StudentDAO().getSession();
        Transaction tx = hibSession.beginTransaction();
        try {
            for (Iterator e=student.getRequests().iterator();e.hasNext();) {
                Request request = (Request)e.next();
                if (request instanceof CourseRequest)
                    updateSectioningInfos(hibSession, s, (CourseRequest)request);
            }
            for (Iterator i=s.getClassEnrollments().iterator();i.hasNext();) {
                StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                sce.getClazz().getStudentEnrollments().remove(sce);
                sce.getClazz().setEnrollment(sce.getClazz().getEnrollment()-1);
                hibSession.delete(sce); i.remove();
            }
            for (Iterator i=s.getWaitlists().iterator();i.hasNext();) {
                WaitList wl = (WaitList)i.next();
                hibSession.delete(wl); i.remove();
            }
            for (Iterator i=s.getCourseDemands().iterator();i.hasNext();) {
                CourseDemand cd = (CourseDemand)i.next();
                if (cd.getFreeTime()!=null)
                    hibSession.delete(cd.getFreeTime());
                hibSession.delete(cd); i.remove();
            }
            hibSession.flush();
            for (Iterator e=student.getRequests().iterator();e.hasNext();) {
                Request request = (Request)e.next();
                CourseDemand cd = null;
                if (request instanceof FreeTimeRequest) {
                    FreeTimeRequest freeTime = (FreeTimeRequest)request;
                    FreeTime ft = new FreeTime();
                    ft.setCategory(new Integer(freeTime.getTime().getBreakTime()));
                    ft.setDayCode(new Integer(freeTime.getTime().getDayCode()));
                    ft.setLength(new Integer(freeTime.getTime().getLength()));
                    ft.setName(freeTime.getTime().getLongName());
                    ft.setSession(session);
                    ft.setStartSlot(new Integer(freeTime.getTime().getStartSlot()));
                    hibSession.save(ft);
                    cd = new CourseDemand();
                    cd.setStudent(s);
                    cd.setPriority(new Integer(request.getPriority()));
                    cd.setAlternative(new Boolean(request.isAlternative()));
                    cd.setWaitlist(Boolean.FALSE);
                    cd.setTimestamp(new Date());
                    cd.setFreeTime(ft);
                    hibSession.save(cd);
                } else if (request instanceof CourseRequest) {
                    CourseRequest courseRequest = (CourseRequest)request;
                    cd = new CourseDemand();
                    cd.setStudent(s);
                    cd.setPriority(new Integer(request.getPriority()));
                    cd.setAlternative(new Boolean(request.isAlternative()));
                    cd.setWaitlist(new Boolean(courseRequest.isWaitlist()));
                    cd.setTimestamp(new Date());
                    hibSession.save(cd);
                    int ord = 0;
                    Enrollment enrollment = (Enrollment)request.getAssignment();
                    org.unitime.timetable.model.CourseRequest crq = null;
                    for (Iterator f=courseRequest.getCourses().iterator();f.hasNext();ord++) {
                        Course course = (Course)f.next();
                        org.unitime.timetable.model.CourseRequest cr = new org.unitime.timetable.model.CourseRequest();
                        cr.setOrder(new Integer(ord));
                        cr.setAllowOverlap(Boolean.FALSE);
                        cr.setCourseDemand(cd);
                        cr.setCourseOffering(new CourseOfferingDAO().get(new Long(course.getId())));
                        cr.setCredit(new Integer(-1));
                        hibSession.save(cr);
                        if (enrollment!=null && course.getOffering().equals(enrollment.getOffering()))
                            crq = cr;
                        for (Iterator i=courseRequest.getSelectedChoices().iterator();i.hasNext();) {
                            Choice choice = (Choice)i.next();
                            if (!choice.getOffering().equals(course.getOffering())) continue;
                            for (Iterator j=choice.getSections().iterator();j.hasNext();) {
                                Section section = (Section)j.next();
                                ClassWaitList cwl = new ClassWaitList();
                                cwl.setClazz(new Class_DAO().get(new Long(section.getId())));
                                cwl.setCourseRequest(cr);
                                cwl.setStudent(s);
                                cwl.setTimestamp(new Date());
                                cwl.setType(ClassWaitList.TYPE_SELECTION);
                                hibSession.save(cwl);
                            }
                        }
                        for (Iterator i=courseRequest.getWaitlistedChoices().iterator();i.hasNext();) {
                            Choice choice = (Choice)i.next();
                            if (!choice.getOffering().equals(course.getOffering())) continue;
                            for (Iterator j=choice.getSections().iterator();j.hasNext();) {
                                Section section = (Section)j.next();
                                ClassWaitList cwl = new ClassWaitList();
                                cwl.setClazz(new Class_DAO().get(new Long(section.getId())));
                                cwl.setCourseRequest(cr);
                                cwl.setStudent(s);
                                cwl.setTimestamp(new Date());
                                cwl.setType(ClassWaitList.TYPE_WAITLIST);
                                hibSession.save(cwl);
                            }
                        }
                    }
                    if (enrollment==null) {
                        if (courseRequest.isWaitlist() && student.canAssign(courseRequest)) {
                            WaitList wl = new WaitList();
                            wl.setStudent(s);
                            wl.setCourseOffering(new CourseOfferingDAO().get(new Long(((Course)courseRequest.getCourses().get(0)).getId())));
                            wl.setTimestamp(new Date());
                            wl.setType(new Integer(0));
                            hibSession.save(wl);
                        }
                    } else {
                        for (Iterator i=enrollment.getAssignments().iterator();i.hasNext();) {
                            Section section = (Section)i.next();
                            StudentClassEnrollment sce = new StudentClassEnrollment();
                            sce.setStudent(s);
                            Class_ clazz = new Class_DAO().get(new Long(section.getId()));
                            sce.setClazz(clazz);
                            s.getClassEnrollments().add(sce);
                            clazz.getStudentEnrollments().add(sce);
                            sce.setCourseRequest(crq);
                            sce.setCourseOffering(crq.getCourseOffering());
                            sce.setTimestamp(new Date());
                            clazz.setEnrollment(clazz.getEnrollment()==null?1:clazz.getEnrollment()+1);
                            hibSession.save(sce);
                        }
                    }
                }
                if (cd!=null && messages!=null) {
                    int ord = 0;
                    for (Iterator f=messages.iterator();f.hasNext();) {
                        StudentSctBBTest.Message message = (StudentSctBBTest.Message)f.next();
                        if (request.equals(message.getRequest())) {
                            StudentEnrollmentMessage m = new StudentEnrollmentMessage();
                            m.setCourseDemand(cd);
                            m.setLevel(new Integer(message.getLevel()));
                            m.setType(new Integer(0));
                            m.setTimestamp(new Date());
                            m.setMessage(message.getMessage());
                            m.setOrder(new Integer(ord++));
                            hibSession.save(m);
                        }
                    }
                }
            }
            hibSession.saveOrUpdate(s);
            hibSession.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
        hibSession.refresh(s);
    }
    
    
    private static void testSectioning(Element studentElement, Element response, Session session) {
        try {
            System.out.print("Request:");
            new XMLWriter(System.out,OutputFormat.createPrettyPrint()).write(studentElement);
        } catch (Exception e) {}
        Student student = new Student(Long.parseLong(studentElement.attributeValue("key")));
        sLog.info("  loading student "+student.getId());
		String courseNumbersMustBeUnique = ApplicationProperties.getProperty("tmtbl.courseNumber.unique","true");

        StudentSctBBTest sbt = null;
        boolean commit = false;
        Vector messages = new Vector();
        
        if (studentElement.element("retrieveCourseRequests")!=null) {
            loadStudent(session, student, messages);
            sbt = new StudentSctBBTest(student);
            for (Iterator e=student.getRequests().iterator();e.hasNext();) {
                Request request = (Request)e.next();
                if (request.getInitialAssignment()!=null)
                    request.assign(0, request.getInitialAssignment());
            }
            for (Iterator e=student.getRequests().iterator();e.hasNext();) {
                Request request = (Request)e.next();
                if (request instanceof FreeTimeRequest) {
                    Enrollment enrollment = (Enrollment)request.values().get(0);
                    if (sbt.conflictValues(enrollment).isEmpty())
                        request.assign(0, enrollment);
                }
            }
        }
        
        Element courseRequestsElement = studentElement.element("updateCourseRequests");
        if (courseRequestsElement==null) {
            sLog.warn("  No course requests for student "+student.getId());
        } else {
            long reqId = 0; int priority = 0;
            commit = "true".equals(courseRequestsElement.attributeValue("commit"));
            for (Iterator i=courseRequestsElement.elementIterator();i.hasNext();) {
                Element requestElement = (Element)i.next();
                boolean alternative = "true".equals(requestElement.attributeValue("alternative"));
                if ("freeTime".equals(requestElement.getName())) {
                    String days = requestElement.attributeValue("days");
                    String startTime = requestElement.attributeValue("startTime");
                    String length = requestElement.attributeValue("length");
                    String endTime = requestElement.attributeValue("endTime");
                    FreeTimeRequest ftRequest = new FreeTimeRequest(reqId++, priority++, alternative, student, makeTime(session.getDefaultDatePattern(), days, startTime, endTime, length));
                    sLog.info("    added "+ftRequest);
                } else if ("courseOffering".equals(requestElement.getName())) {
                    String subjectArea = requestElement.attributeValue("subjectArea");
                    String courseNumber = requestElement.attributeValue("courseNumber");
                    boolean waitlist = "true".equals(requestElement.attributeValue("waitlist"));
                    CourseOffering co = null;

        	    	if (courseNumbersMustBeUnique.equalsIgnoreCase("true")){
        	    		co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subjectArea, courseNumber);
                    } else {
                        String title = requestElement.attributeValue("title");
                    	co = CourseOffering.findBySessionSubjAreaAbbvCourseNbrTitle(session.getUniqueId(), subjectArea, courseNumber, title);
                    }
                    if (co==null) {
                        sLog.warn("    Course "+subjectArea+" "+courseNumber+" not found.");
                        continue;
                    }
                    Vector courses = new Vector();
                    courses.add(loadCourse(co, student.getId()));
                    for (Iterator j=requestElement.elementIterator("alternative");j.hasNext();) {
                        Element altElement = (Element)j.next();
                        String altSubjectArea = altElement.attributeValue("subjectArea");
                        String altCourseNumber = altElement.attributeValue("courseNumber");
                        CourseOffering aco = null;
                        if (courseNumbersMustBeUnique.equalsIgnoreCase("true")){
                        	aco = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), altSubjectArea, altCourseNumber);
                        } else {
                            String altTitle = altElement.attributeValue("title");
                        	aco = CourseOffering.findBySessionSubjAreaAbbvCourseNbrTitle(session.getUniqueId(), altSubjectArea, altCourseNumber, altTitle);
                        }
                        if (aco!=null)
                            courses.add(loadCourse(aco, student.getId()));
                    }
                    CourseRequest cRequest = new CourseRequest(reqId++, priority++, alternative, student, courses, waitlist);
                    cRequest.values();
                    sLog.info("    added "+cRequest);
                }
            }
            Element requestScheduleElement = studentElement.element("requestSchedule");
            if (requestScheduleElement!=null) {
                for (Iterator i=requestScheduleElement.elementIterator("courseOffering");i.hasNext();) {
                    Element courseOfferingElement = (Element)i.next();
                    String subjectArea = courseOfferingElement.attributeValue("subjectArea");
                    String courseNumber = courseOfferingElement.attributeValue("courseNumber");
                    CourseOffering co = null;
                    if (courseNumbersMustBeUnique.equalsIgnoreCase("true")){
        	    		co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subjectArea, courseNumber);
        	    	} else {
                        String title = courseOfferingElement.attributeValue("title");
        	    		co = CourseOffering.findBySessionSubjAreaAbbvCourseNbrTitle(session.getUniqueId(), subjectArea, courseNumber, title);
        	    	}
                    if (co==null) {
                        sLog.warn("    Course "+subjectArea+" "+courseNumber+" not found.");
                        continue;
                    }
                    for (Iterator e=student.getRequests().iterator();e.hasNext();) {
                        Request request = (Request)e.next();
                        if (request instanceof CourseRequest) {
                            CourseRequest courseRequest = (CourseRequest)request;
                            Course course = courseRequest.getCourse(co.getUniqueId().longValue());
                            Config config = null;
                            if (course==null) continue;
                            Set assignedSections = new HashSet();
                            int nrClasses = 0;
                            for (Iterator j=courseOfferingElement.elementIterator("class");j.hasNext();nrClasses++) {
                                Element classEl = (Element)j.next();
                                String assignmentId = classEl.attributeValue("assignmentId");
                                Section section = (assignmentId==null?null:course.getOffering().getSection(Long.parseLong(assignmentId)));
                                if (section!=null) { 
                                    assignedSections.add(section);
                                    if (config==null) config = section.getSubpart().getConfig();
                                }
                                for (Iterator k=classEl.elementIterator("choice");k.hasNext();) {
                                    Element choiceEl = (Element)k.next();
                                    Choice choice = new Choice(course.getOffering(),choiceEl.attributeValue("id"));
                                    if ("select".equals(choiceEl.attributeValue("selection"))) {
                                        courseRequest.getSelectedChoices().add(choice);
                                        sLog.info("      add selection "+choice);
                                    } else {
                                        courseRequest.getWaitlistedChoices().add(choice);
                                        sLog.info("      add waitlist "+choice);
                                    }
                                }
                            }
                            if (nrClasses==assignedSections.size()) {
                                courseRequest.setInitialAssignment(new Enrollment(request, 1.0, config, assignedSections));
                                sLog.info("    initial assignment "+courseRequest.getInitialAssignment());
                            }
                        }
                    }
                }
            } else {
                sLog.warn("  No schedule requests for student "+student.getId());
            }
            sLog.info("  sectioning student "+student.getId());
            sbt = new StudentSctBBTest(student);
            
            Model model = sbt.getSolution().getModel();
            messages.addAll(sbt.getMessages());
            sLog.info("  info: "+model.getInfo());

            if (commit) saveStudent(session,student, messages);
        }
        Element studentResponseElement = response.addElement("student");
        studentResponseElement.addAttribute("key", String.valueOf(student.getId()));
        Element ackResponseElement = studentResponseElement.addElement("acknowledgement");
        ackResponseElement.addAttribute("result", "ok");
        Element courseReqResponseElement = studentResponseElement.addElement("courseRequests"); 
        for (Iterator e=messages.iterator();e.hasNext();) {
            StudentSctBBTest.Message message = (StudentSctBBTest.Message)e.next();
            ackResponseElement.addElement("message").addAttribute("type", message.getLevelString()).setText(message.getMessage());
        }
        for (Iterator e=student.getRequests().iterator();e.hasNext();) {
            Request request = (Request)e.next();
            Element reqElement = null;
            if (request instanceof FreeTimeRequest) {
                FreeTimeRequest ftRequest = (FreeTimeRequest)request;
                reqElement = courseReqResponseElement.addElement("freeTime");
                reqElement.addAttribute("days", dayCode2days(ftRequest.getTime().getDayCode()));
                reqElement.addAttribute("startTime", startSlot2startTime(ftRequest.getTime().getStartSlot()));
                reqElement.addAttribute("endTime", timeLocation2endTime(ftRequest.getTime()));
                reqElement.addAttribute("length", String.valueOf(Constants.SLOT_LENGTH_MIN*ftRequest.getTime().getLength()));
                sLog.info("  added "+ftRequest);
            } else {
                CourseRequest courseRequest = (CourseRequest)request;
                reqElement = courseReqResponseElement.addElement("courseOffering");
                for (Iterator f=courseRequest.getCourses().iterator();f.hasNext();) {
                    Course course = (Course)f.next();
                    Element element = (reqElement.attribute("subjectArea")==null?reqElement:reqElement.addElement("alternative"));
                    element.addAttribute("subjectArea", course.getSubjectArea());
                    element.addAttribute("courseNumber", course.getCourseNumber());
                    CourseOffering co = CourseOffering.findByUniqueId(course.getId());
                    element.addAttribute("title", (co.getTitle()!=null?co.getTitle():""));
                }
                reqElement.addAttribute("waitlist", (courseRequest.isWaitlist()?"true":"false"));
                sLog.info("  added "+courseRequest);
            }
            if (request.isAlternative()) reqElement.addAttribute("alternative", "true");
        }
        Comparator choiceComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                Choice c1 = (Choice)o1;
                Choice c2 = (Choice)o2;
                if (c1.getTime()==null) {
                    if (c2.getTime()!=null) return -1;
                } else if (c2.getTime()==null) return 1;
                if (c1.getTime()!=null) {
                    int cmp = -Double.compare(c1.getTime().getDayCode(),c2.getTime().getDayCode());
                    if (cmp!=0) return cmp;
                    cmp = Double.compare(c1.getTime().getStartSlot(),c2.getTime().getStartSlot());
                    if (cmp!=0) return cmp;
                    cmp = c1.getTime().getDatePatternName().compareTo(c2.getTime().getDatePatternName());
                    if (cmp!=0) return cmp;
                }
                if (c1.getInstructorNames()==null) {
                    if (c2.getInstructorNames()!=null) return -1;
                } else if (c2.getInstructorNames()==null) return 1; 
                if (c1.getInstructorNames()!=null) {
                    int cmp = c1.getInstructorNames().compareTo(c2.getInstructorNames());
                    if (cmp!=0) return cmp;
                }
                return c1.getId().compareTo(c2.getId());
            }
        };
        boolean generateRandomAvailability = (student.getId()<0);
        Element scheduleResponseElement = studentResponseElement.addElement("schedule");
        scheduleResponseElement.addAttribute("type", (commit?"actual":"proposed"));
        for (Iterator e=student.getRequests().iterator();e.hasNext();) {
            Request request = (Request)e.next();
            if (request.getAssignment()==null) {
                sLog.info("    request "+request+" has no assignment");
                if (request instanceof CourseRequest && ((CourseRequest)request).isWaitlist() && request.getStudent().canAssign(request)) {
                    Element courseOfferingElement = scheduleResponseElement.addElement("courseOffering");
                    Course course = (Course)((CourseRequest)request).getCourses().get(0);
                    courseOfferingElement.addAttribute("subjectArea", course.getSubjectArea());
                    courseOfferingElement.addAttribute("courseNumber", course.getCourseNumber());
                    CourseOffering co = CourseOffering.findByUniqueId(course.getId());
                    courseOfferingElement.addAttribute("title", co.getTitle());
                    courseOfferingElement.addAttribute("waitlist", "true");
                }
                continue;
            }
            if (request instanceof FreeTimeRequest) {
                FreeTimeRequest ftRequest = (FreeTimeRequest)request;
                Element ftElement = scheduleResponseElement.addElement("freeTime");
                ftElement.addAttribute("days", dayCode2days(ftRequest.getTime().getDayCode()));
                ftElement.addAttribute("startTime", startSlot2startTime(ftRequest.getTime().getStartSlot()));
                ftElement.addAttribute("endTime", timeLocation2endTime(ftRequest.getTime()));
                ftElement.addAttribute("length", String.valueOf(Constants.SLOT_LENGTH_MIN*ftRequest.getTime().getLength()));
                if (ftRequest.getTime()!=null)
                    ftElement.addAttribute("time", ftRequest.getTime().getDayHeader()+" "+ftRequest.getTime().getStartTimeHeader()+" - "+ftRequest.getTime().getEndTimeHeader());
                else
                    ftElement.addAttribute("time", "Arr Hrs");
            } else {
                CourseRequest courseRequest = (CourseRequest)request;
                Element courseOfferingElement = scheduleResponseElement.addElement("courseOffering");
                Enrollment enrollment = (Enrollment)request.getAssignment();
                Set unusedInstructionalTypes = null;
                Offering offering = null;
                HashSet availableChoices = null;
                Vector assignments = new Vector(enrollment.getAssignments());
                Collections.sort(assignments, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Section s1 = (Section)o1;
                        Section s2 = (Section)o2;
                        return s1.getSubpart().compareTo(s2.getSubpart());
                    }
                });
                for (Iterator i=assignments.iterator();i.hasNext();) {
                    Section section = (Section)i.next();
                    if (courseOfferingElement.attribute("subjectArea")==null) {
                        Course course = section.getSubpart().getConfig().getOffering().getCourse(student);
                        courseOfferingElement.addAttribute("subjectArea", course.getSubjectArea());
                        courseOfferingElement.addAttribute("courseNumber", course.getCourseNumber());
                        CourseOffering co = CourseOffering.findByUniqueId(course.getId());
                        courseOfferingElement.addAttribute("title", co.getTitle());
                    }
                    if (offering==null) {
                        offering=section.getSubpart().getConfig().getOffering();
                        if (generateRandomAvailability) {
                            availableChoices = generateAvailableChoices(offering, new Random(13031978l), 0.75);
                        } else {
                            availableChoices = new HashSet();
                            Enrollment assignment = (Enrollment)courseRequest.getAssignment();
                            for (Iterator j=courseRequest.getAvaiableEnrollmentsSkipSameTime().iterator();j.hasNext();) {
                                Enrollment enr = (Enrollment)j.next();
                                for (Iterator k=enr.getAssignments().iterator();k.hasNext();) {
                                    Section s = (Section)k.next();
                                    if (s.getLimit()>0 && s.getPenalty()<=sAvailableThreshold)
                                        availableChoices.add(s.getChoice());
                                }
                            }
                        }
                    }
                    if (unusedInstructionalTypes==null)
                        unusedInstructionalTypes=section.getSubpart().getConfig().getOffering().getInstructionalTypes();
                    unusedInstructionalTypes.remove(section.getSubpart().getInstructionalType());
                    Element classElement = courseOfferingElement.addElement("class");
                    classElement.addAttribute("id", section.getSubpart().getInstructionalType());
                    classElement.addAttribute("assignmentId", String.valueOf(section.getId()));
                    if (section.getSubpart().getParent()!=null)
                        classElement.addAttribute("parent", section.getSubpart().getParent().getInstructionalType());
                    classElement.addAttribute("name", section.getSubpart().getName());
                    if (section.getTime()!=null) {
                        classElement.addAttribute("days", dayCode2days(section.getTime().getDayCode()));
                        classElement.addAttribute("startTime", startSlot2startTime(section.getTime().getStartSlot()));
                        classElement.addAttribute("endTime", timeLocation2endTime(section.getTime()));
                        //classElement.addAttribute("length", String.valueOf(Constants.SLOT_LENGTH_MIN*section.getTime().getLength()));
                        if (section.getTime().getDatePatternName()!=null)
                            classElement.addAttribute("date", section.getTime().getDatePatternName());
                        classElement.addAttribute("time", section.getTime().getDayHeader()+" "+section.getTime().getStartTimeHeader()+" - "+section.getTime().getEndTimeHeader());
                    } else classElement.addAttribute("time", "Arr Hrs");
                    if (section.getNrRooms()>0) {
                        String location = "";
                        for (Iterator f=section.getRooms().iterator();f.hasNext();) {
                            RoomLocation rl = (RoomLocation)f.next();
                            location += rl.getName();
                            if (f.hasNext()) location+=",";
                        }
                        classElement.addAttribute("location", location);
                    }
                    if (section.getChoice().getInstructorNames()!=null)
                        classElement.addAttribute("instructor", section.getChoice().getInstructorNames());
                    Vector choices = new Vector(section.getSubpart().getConfig().getOffering().getChoices(section.getSubpart().getInstructionalType()));
                    Collections.sort(choices, choiceComparator);
                    for (Iterator f = choices.iterator();f.hasNext();) {
                        Choice choice = (Choice)f.next();
                        Element choiceEl = classElement.addElement("choice");
                        choiceEl.addAttribute("id", choice.getId());
                        choiceEl.addAttribute("available", (availableChoices==null?"true":availableChoices.contains(choice)?"true":"false"));
                        if (choice.getTime()!=null) {
                            choiceEl.addAttribute("days", dayCode2days(choice.getTime().getDayCode()));
                            choiceEl.addAttribute("startTime", startSlot2startTime(choice.getTime().getStartSlot()));
                            choiceEl.addAttribute("endTime", timeLocation2endTime(choice.getTime()));
                            if (choice.getTime().getDatePatternName()!=null)
                                choiceEl.addAttribute("date", choice.getTime().getDatePatternName());
                            choiceEl.addAttribute("time", choice.getTime().getDayHeader()+" "+choice.getTime().getStartTimeHeader()+" - "+choice.getTime().getEndTimeHeader());
                        } else
                            choiceEl.addAttribute("time", "Arr Hrs");
                        if (choice.equals(section.getChoice()))
                            choiceEl.addAttribute("available", "true");
                        if (courseRequest.getSelectedChoices().isEmpty() && choice.equals(section.getChoice())) {
                            choiceEl.addAttribute("selection", "select");
                        } else if (courseRequest.getSelectedChoices().contains(choice)) {
                            choiceEl.addAttribute("selection", "select");
                            if (generateRandomAvailability) choiceEl.addAttribute("available", "true");
                        } else if (courseRequest.getWaitlistedChoices().contains(choice)) {
                            choiceEl.addAttribute("selection", "wait");
                            if (generateRandomAvailability) choiceEl.addAttribute("available", "false");
                        }
                        if (choice.getInstructorNames()!=null)
                            choiceEl.addAttribute("instructor", choice.getInstructorNames());
                        exportDependencies(choiceEl, choice, choice.getParentSections());
                    }
                }
                if (unusedInstructionalTypes!=null) {
                    for (Iterator i=unusedInstructionalTypes.iterator();i.hasNext();) {
                        String unusedInstructionalType = (String)i.next();
                        Element classElement = courseOfferingElement.addElement("class");
                        classElement.addAttribute("id", unusedInstructionalType);
                        classElement.addAttribute("name", ((Subpart)offering.getSubparts(unusedInstructionalType).iterator().next()).getName());
                        Vector choices = new Vector(offering.getChoices(unusedInstructionalType));
                        Collections.sort(choices, choiceComparator);
                        for (Iterator f = choices.iterator();f.hasNext();) {
                            Choice choice = (Choice)f.next();
                            Element choiceEl = classElement.addElement("choice");
                            choiceEl.addAttribute("id", choice.getId());
                            choiceEl.addAttribute("available", (availableChoices==null?"true":availableChoices.contains(choice)?"true":"false"));
                            if (choice.getTime()!=null) {
                                choiceEl.addAttribute("days", dayCode2days(choice.getTime().getDayCode()));
                                choiceEl.addAttribute("startTime", startSlot2startTime(choice.getTime().getStartSlot()));
                                choiceEl.addAttribute("endTime", timeLocation2endTime(choice.getTime()));
                                if (choice.getTime().getDatePatternName()!=null)
                                    choiceEl.addAttribute("date", choice.getTime().getDatePatternName());
                                choiceEl.addAttribute("time", choice.getTime().getDayHeader()+" "+choice.getTime().getStartTimeHeader()+" - "+choice.getTime().getEndTimeHeader());
                            } else
                                choiceEl.addAttribute("time", "Arr Hrs");
                            if (courseRequest.getWaitlistedChoices().contains(choice))
                                choiceEl.addAttribute("selection", "wait");
                            if (choice.getInstructorNames()!=null)
                                choiceEl.addAttribute("instructor", choice.getInstructorNames());
                            exportDependencies(choiceEl, choice, choice.getParentSections());
                        }
                    }
                }
            }
            sLog.info("    added "+request.getAssignment());
        }
        /*
        try {
            System.out.print("Response:");
            new XMLWriter(System.out,OutputFormat.createPrettyPrint()).write(studentResponseElement);
        } catch (Exception e) {}
        */
    }
    
    public static void main(String[] args) {
        try {
            if (args.length==0)
                args = new String[] { 
                    "jdbc:oracle:thin:@tamarind.smas.purdue.edu:1521:xe", 
                    "c:\\test\\sectioningRequest.xml", 
                    "c:\\test\\sectioningResponse.xml"};
            
            if (args.length>=4)
                ToolBox.configureLogging(args[3], null, true, false);
            else
                ToolBox.configureLogging();
            
            HibernateUtil.configureHibernate(args[0]);
            
            sLog.info("Loading "+args[1]+" ...");
            
            Document request = (new SAXReader()).read(new File(args[1]));
            
            Document response = testSectioning(request);
            
            sLog.info("Saving "+args[2]+" ...");
            
            XMLWriter out = new XMLWriter(new FileOutputStream(new File(args[2])),OutputFormat.createPrettyPrint());
            
            out.write(response);
            
            out.flush(); out.close();
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            e.printStackTrace();
        }
    }
    
}
