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
package org.unitime.timetable.dataexchange;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.util.ToolBox;

import org.dom4j.Element;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.util.Constants;

/**
 * 
 * @author Tomas Muller
 *
 */
public class StudentSectioningImport extends BaseImport {
    public static boolean sStudentId10 = false;
    public static boolean sUseCache = true;
    private static DecimalFormat sDF10 = new DecimalFormat("0000000000");
    
    public StudentSectioningImport() {}
    
    public void loadXml(Element rootElement) {
        try {
            beginTransaction();
            String version = rootElement.attributeValue("version","1.0");
            if (!checkVersion(version)) {
                fatal("Version "+version+" not supported.");
                return;
            }
            if (rootElement.getName().equals("request")) {
                importRequest(rootElement);
            }
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
    }
    
    public boolean checkVersion(String version) {
        return true;
    }
    
    public void importRequest(Element requestElement) {
        String campus = requestElement.attributeValue("campus");
        if (campus==null) {
            fatal("Campus attribute is missing.");
            return;
        }
        debug("Campus: "+campus);
        String year = requestElement.attributeValue("year");
        String term = requestElement.attributeValue("term");
        if (year==null) {
            fatal("Year attribute is missing.");
            return;
        }
        if (term==null) {
            fatal("Term attribute is missing.");
            return;
        }
        debug("Year: "+year);
        debug("Term: "+term);
        
        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
        if (session==null) {
            fatal("Session "+year+term+" for "+campus+" not found.");
            return;
        }
        
        for (Iterator i=requestElement.elementIterator("student");i.hasNext();) {
            importStudent(session, (Element)i.next());
            flushIfNeeded(false);
        }
    }
    
    public void importStudent(Session session, Element studentElement) {
        String studentId = studentElement.attributeValue("key");
        if (studentId==null) {
            error("No student id given (attribute key of element student).");
            return;
        }
        debug("Processing student "+studentId);
        Element demographicsElement = studentElement.element("updateDemographics");
        Student student = Student.findByExternalId(session.getUniqueId(),sStudentId10?sDF10.format(Long.valueOf(studentId)):studentId);
        if (student==null) {
            debug("Student "+studentId+" not found.");
            if (demographicsElement==null) {
                error("Student "+studentId+" not found, but no demographics information provided.");
                return;
            }
            student = createStudent(session, studentId, demographicsElement);
        } else {
            if (demographicsElement!=null)
                updateStudent(student, demographicsElement);
        }
        Element reqCoursesElement = studentElement.element("updateCourseRequests");
        if (reqCoursesElement!=null) {
            importCourseRequests(student, reqCoursesElement);
        }
    }
    
    public Student createStudent(Session session, String studentId, Element demographicsElement) {
        Student student = new Student();
        student.setSession(session);
        student.setExternalUniqueId(studentId);
        student.setFreeTimeCategory(new Integer(0));
        student.setSchedulePreference(new Integer(0));
        updateStudent(student, demographicsElement);
        return student;
    }
    
    public void updateStudent(Student student, Element demographicsElement) {
        Element name = demographicsElement.element("name");
        if (name!=null) {
            student.setFirstName(name.attributeValue("first"));
            student.setMiddleName(name.attributeValue("middle"));
            student.setLastName(name.attributeValue("last"));
        }
        Element email = demographicsElement.element("email");
        if (email!=null) {
            student.setEmail(email.attributeValue("email"));
        }
        
        //import majors
        if (student.getPosMajors()==null) {
            student.setPosMajors(new HashSet());
        } else {
            student.getPosMajors().clear();
        }
        for (Iterator i=demographicsElement.elementIterator("major");i.hasNext();) {
            Element majorElement = (Element)i.next();
            String code = majorElement.attributeValue("code");
            PosMajor major = PosMajor.findByCode(student.getSession().getUniqueId(), code);
            if (major==null) {
                warn("Major "+code+" not found."); continue;
            }
            student.getPosMajors().add(major);
        }
        
        //import minors
        if (student.getPosMinors()==null) {
            student.setPosMinors(new HashSet());
        } else {
            student.getPosMinors().clear();
        }
        for (Iterator i=demographicsElement.elementIterator("minor");i.hasNext();) {
            Element minorElement = (Element)i.next();
            String code = minorElement.attributeValue("code");
            PosMinor minor = PosMinor.findByCode(student.getSession().getUniqueId(), code);
            if (minor==null) {
                warn("Major "+code+" not found."); continue;
            }
            student.getPosMinors().add(minor);
        }

        //Import academic areas and classifications
        if (student.getAcademicAreaClassifications()==null) {
            student.setAcademicAreaClassifications(new HashSet());
        } else {
            for (Iterator i=student.getAcademicAreaClassifications().iterator();i.hasNext();) {
                AcademicAreaClassification aac = (AcademicAreaClassification)i.next();
                getHibSession().delete(aac);
                i.remove();
            }
        }
        for (Iterator i=demographicsElement.elementIterator("acadArea");i.hasNext();) {
            Element acadAreaElement = (Element)i.next();
            String abbv = acadAreaElement.attributeValue("abbv");
            String code = acadAreaElement.attributeValue("classification");
            AcademicArea area = AcademicArea.findByAbbv(student.getSession().getUniqueId(),abbv);
            if (area==null) {
                warn("Academic area "+abbv+" not found."); continue;
            }
            AcademicClassification clasf = AcademicClassification.findByCode(student.getSession().getUniqueId(),code); 
            if (area==null) {
                warn("Academic classification "+code+" not found."); continue;
            }
            AcademicAreaClassification aac = new AcademicAreaClassification();
            aac.setAcademicArea(area);
            aac.setAcademicClassification(clasf);
            aac.setStudent(student);
            student.getAcademicAreaClassifications().add(aac);
            for (Iterator j=acadAreaElement.elementIterator("major");j.hasNext();) {
                Element majorElement = (Element)j.next();
                String majorCode = majorElement.attributeValue("code");
                PosMajor major = PosMajor.findByCodeAcadAreaId(student.getSession().getUniqueId(), majorCode, area.getUniqueId());
                if (major==null) {
                    warn("Major "+code+" not found."); continue;
                }
                student.getPosMajors().add(major);
            }
            for (Iterator j=acadAreaElement.elementIterator("minor");j.hasNext();) {
                Element minorElement = (Element)j.next();
                String minorCode = minorElement.attributeValue("code");
                PosMinor minor = PosMinor.findByCodeAcadAreaId(student.getSession().getUniqueId(), minorCode, area.getUniqueId());
                if (minor==null) {
                    warn("Major "+code+" not found."); continue;
                }
                student.getPosMinors().add(minor);
            }
        }
        
        //import accomodations
        if (student.getAccomodations()==null) {
            student.setAccomodations(new HashSet());
        } else {
            student.getAccomodations().clear();
        }
        for (Iterator i=demographicsElement.elementIterator("disability");i.hasNext();) {
            Element disabilityElement = (Element)i.next();
            String code = disabilityElement.attributeValue("code");
            StudentAccomodation accomodation = StudentAccomodation.findByAbbv(student.getSession().getUniqueId(), code);
            if (accomodation==null) {
                warn("Accomodation "+code+" not found."); continue;
            }
            student.getAccomodations().add(accomodation);
        }

        //import groups
        if (student.getGroups()==null) {
            student.setGroups(new HashSet());
        } else {
            student.getGroups().clear();
        }
        for (Iterator i=demographicsElement.elementIterator("groupAffiliation");i.hasNext();) {
            Element disabilityElement = (Element)i.next();
            String code = disabilityElement.attributeValue("code");
            StudentGroup group = StudentGroup.findByAbbv(student.getSession().getUniqueId(), code);
            if (group==null) {
                warn("Accomodation "+code+" not found."); continue;
            }
            student.getGroups().add(group);
        }

        getHibSession().saveOrUpdate(student);
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
        int nrSlots = 0;
        if (length!=null) {
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
    
    public void importCourseRequests(Student student, Element reqCoursesElement) {
        if (!"true".equals(reqCoursesElement.attributeValue("commit"))) return;
        if (student.getCourseDemands()==null) {
            student.setCourseDemands(new HashSet());
        } else {
            for (Iterator i=student.getCourseDemands().iterator();i.hasNext();) {
                CourseDemand demand = (CourseDemand)i.next();
                if (demand.getFreeTime()!=null) getHibSession().delete(demand.getFreeTime());
                getHibSession().delete(demand);
                i.remove();
            }
        }
        int priority = 0;
        for (Iterator i=reqCoursesElement.elementIterator();i.hasNext();priority++) {
            Element requestElement = (Element)i.next();
            String waitList = requestElement.attributeValue("waitlist");
            String alternative = requestElement.attributeValue("alternative");
            if (requestElement.getName().equals("courseOffering")) {
                CourseDemand demand = new CourseDemand();
                demand.setAlternative(new Boolean("true".equals(alternative)));
                demand.setPriority(new Integer(priority));
                demand.setStudent(student);
                demand.setWaitlist(new Boolean("true".equals(waitList)));
                demand.setTimestamp(new Date());
                demand.setCourseRequests(new HashSet<CourseRequest>());
                debug("  "+(priority+1)+". demand (wait="+demand.isWaitlist()+", alt="+demand.isAlternative()+")");
                String subjectArea = requestElement.attributeValue("subjectArea");
                String courseNbr = requestElement.attributeValue("courseNumber");
                Integer credit = Integer.valueOf(requestElement.attributeValue("credit","0"));
                CourseOffering courseOffering = getCourseOffering(student.getSession().getUniqueId(), subjectArea, courseNbr);
                if (courseOffering==null) {
                    warn("Course "+subjectArea+" "+courseNbr+" not found."); continue;
                }
                int ord = 0;
                CourseRequest request = new CourseRequest();
                request.setCourseOffering(courseOffering);
                request.setAllowOverlap(Boolean.FALSE);
                request.setCredit(credit);
                request.setOrder(new Integer(ord));
                request.setCourseDemand(demand);
                demand.getCourseRequests().add(request);
                debug("    "+courseOffering.getCourseName());
                for (Iterator j=requestElement.elementIterator("alternative");j.hasNext();) {
                    Element altElement = (Element)j.next();
                    String altSubjectArea = altElement.attributeValue("subjectArea");
                    String altCourseNbr = altElement.attributeValue("courseNumber");
                    CourseOffering altCourseOffering = getCourseOffering(student.getSession().getUniqueId(), altSubjectArea, altCourseNbr);
                    if (altCourseOffering==null) {
                        warn("Course "+altSubjectArea+" "+altCourseNbr+" not found."); continue;
                    }
                    Integer altCredit = Integer.valueOf(altElement.attributeValue("credit","0"));
                    ord++;
                    CourseRequest altRequest = new CourseRequest();
                    altRequest.setCourseOffering(altCourseOffering);
                    altRequest.setAllowOverlap(Boolean.FALSE);
                    altRequest.setCredit(altCredit);
                    altRequest.setOrder(new Integer(ord));
                    altRequest.setCourseDemand(demand);
                    demand.getCourseRequests().add(altRequest);
                    debug("    "+altCourseOffering.getCourseName());
                }
                getHibSession().save(demand);
                student.getCourseDemands().add(demand);
            } else if (requestElement.getName().equals("freeTime")) {
                String days = requestElement.attributeValue("days");
                String startTime = requestElement.attributeValue("startTime");
                String length = requestElement.attributeValue("length");
                String endTime = requestElement.attributeValue("endTime");
                TimeLocation time = makeTime(student.getSession().getDefaultDatePattern(), days, startTime, endTime, length);
                FreeTime ft = new FreeTime();
                ft.setCategory(new Integer(time.getBreakTime()));
                ft.setDayCode(new Integer(time.getDayCode()));
                ft.setLength(new Integer(time.getLength()));
                ft.setName(time.getLongName());
                ft.setSession(student.getSession());
                ft.setStartSlot(new Integer(time.getStartSlot()));
                getHibSession().save(ft);
                CourseDemand demand = new CourseDemand();
                demand.setAlternative(new Boolean("true".equals(alternative)));
                demand.setPriority(new Integer(priority));
                demand.setStudent(student);
                demand.setWaitlist(new Boolean("true".equals(waitList)));
                demand.setTimestamp(new Date());
                demand.setFreeTime(ft);
                getHibSession().save(demand);
                student.getCourseDemands().add(demand);
                debug("  "+(priority+1)+". demand (wait="+demand.isWaitlist()+", alt="+demand.isAlternative()+")");
                debug("    free "+time.getLongName());
            } else warn("Request element "+requestElement.getName()+" not recognized.");
        }
        getHibSession().saveOrUpdate(student);
    }
    
    private Hashtable iCourseTable = null;
    protected CourseOffering getCourseOffering(Long sessionId, String subjectArea, String courseNbr) {
        if (!sUseCache) return CourseOffering.findBySubjectAreaCourseNbr(sessionId, subjectArea, courseNbr);
        if (iCourseTable==null) {
            iCourseTable = new Hashtable();
            debug("Creating course table...");
            for (Iterator i=CourseOffering.findAll(sessionId).iterator();i.hasNext();) {
                CourseOffering courseOffering = (CourseOffering)i.next();
                Hashtable subjTable = (Hashtable)iCourseTable.get(courseOffering.getSubjectArea().getSubjectAreaAbbreviation());
                if (subjTable==null) {
                    subjTable = new Hashtable();
                    iCourseTable.put(courseOffering.getSubjectArea().getSubjectAreaAbbreviation(), subjTable);
                }
                subjTable.put(courseOffering.getCourseNbr(), courseOffering);
            }
        }
        Hashtable subjTable = (Hashtable)iCourseTable.get(subjectArea);
        return (subjTable==null?null:(CourseOffering)subjTable.get(courseNbr));
    }
    
    public static void main(String[] args) {
        try {
            if (args.length==0)
                args = new String[] {
                    "jdbc:oracle:thin:@tamarind.smas.purdue.edu:1521:xe", 
                    "c:\\test\\studentRequests.xml"};

            ToolBox.configureLogging();
            
            HibernateUtil.configureHibernate(args[0]);
            
            new StudentSectioningImport().loadXml(args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
