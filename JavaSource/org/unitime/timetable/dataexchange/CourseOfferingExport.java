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
package org.unitime.timetable.dataexchange;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.util.ToolBox;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ArrangeCreditUnitConfig;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class CourseOfferingExport extends BaseExport {
    protected static Formats.Format<Number> sTwoNumbersDF = Formats.getNumberFormat("00");
    protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy/M/d");
    protected static Formats.Format<Date> sTimeFormat = Formats.getDateFormat("HHmm");
    protected Hashtable<Long, TreeSet<Exam>> iExams = null;
    protected boolean iExportAssignments = true;
    
    public void saveXml(Document document, Session session, Properties parameters) throws Exception {
        try {
            beginTransaction();
            
            iExportAssignments= "true".equals(parameters.getProperty("tmtbl.export.timetable","true"));
            boolean examsOnly = "true".equals(parameters.getProperty("tmtbl.export.exam"));
            Element root = document.addElement(examsOnly?"exams":"offerings");
            root.addAttribute("campus", session.getAcademicInitiative());
            root.addAttribute("year", session.getAcademicYear());
            root.addAttribute("term", session.getAcademicTerm());
            root.addAttribute("dateFormat", sDateFormat.toPattern());
            root.addAttribute("timeFormat", sTimeFormat.toPattern());
            root.addAttribute("created", new Date().toString());
            if (examsOnly)
                root.addAttribute("type", parameters.getProperty("tmtbl.export.exam.type", "all"));
            
            document.addDocType(examsOnly?"exams":"offerings", "-//UniTime//DTD University Course Timetabling/EN", "http://www.unitime.org/interface/CourseOfferingExport.dtd");
            
            if (examsOnly) {
                if ("all".equals(parameters.getProperty("tmtbl.export.exam.type", "all")) || "final".equals(parameters.getProperty("tmtbl.export.exam.type", "all"))) {
                    for (Iterator i=new TreeSet(Exam.findAllFinal(session.getUniqueId())).iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        exportExam(root, null, exam, session);
                    }
                }
                if ("all".equals(parameters.getProperty("tmtbl.export.exam.type", "all")) || "midterm".equals(parameters.getProperty("tmtbl.export.exam.type", "all"))) {
                    for (Iterator i=new TreeSet(Exam.findAllMidterm(session.getUniqueId())).iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        exportExam(root, null, exam, session);
                    }
                }
            } else {
                info("Loading offerings...");
                List offerings = getHibSession().createQuery(
                    "select distinct io from InstructionalOffering io " +
                    "inner join fetch io.courseOfferings as co inner join fetch co.subjectArea sa "+
                    "left join fetch io.instrOfferingConfigs as ioc "+
                    "left join fetch ioc.schedulingSubparts as ss "+
                    "left join fetch ss.classes as c "+
                    "where " +
                    "io.session.uniqueId=:sessionId "+
                    "order by sa.subjectAreaAbbreviation, co.courseNbr").
                    setLong("sessionId",session.getUniqueId().longValue()).
                    setFetchSize(1000).list();
                
                if (!"none".equals(parameters.getProperty("tmtbl.export.exam.type", "all"))) {
                    info("Loading exams...");
                    List allExams = getHibSession().createQuery(
                            "select x from Exam x left join fetch x.owners o " +
                            "where x.session.uniqueId=:sessionId"+
                            ("midterm".equals(parameters.getProperty("tmtbl.export.exam.type", "all"))?" and x.examType.type="+ExamType.sExamTypeMidterm:"")+
                            ("final".equals(parameters.getProperty("tmtbl.export.exam.type", "all"))?" and x.examType.type="+ExamType.sExamTypeFinal:"")
                            ).
                            setLong("sessionId",session.getUniqueId().longValue()).
                            setFetchSize(1000).list();
                    
                    iExams = new Hashtable();
                    info("Checking exams...");
                    for (Iterator i=allExams.iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        for (Iterator j=exam.getOwners().iterator();j.hasNext();) {
                            ExamOwner owner = (ExamOwner)j.next();
                            Long offeringId = owner.getCourse().getInstructionalOffering().getUniqueId();
                            TreeSet<Exam> exams = iExams.get(offeringId);
                            if (exams==null) {
                                exams = new TreeSet();
                                iExams.put(offeringId,exams); 
                            }
                            exams.add(exam);
                        }
                    }
                }
            
                
                info("Exporting "+offerings.size()+" offerings ...");
                for (Iterator i=offerings.iterator();i.hasNext();) {
                    InstructionalOffering io = (InstructionalOffering)i.next();
                    exportInstructionalOffering(root, io, session);
                }
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
    }
    
    protected void exportInstructionalOffering(Element offeringsElement, InstructionalOffering offering, Session session) {
        Element offeringElement = offeringsElement.addElement("offering");
        offeringElement.addAttribute("id", (offering.getExternalUniqueId()!=null?offering.getExternalUniqueId():offering.getUniqueId().toString()));
        offeringElement.addAttribute("offered", (offering.isNotOffered()?"false":"true"));
        offeringElement.addAttribute("action", "insert");
        for (Iterator i=offering.getCreditConfigs().iterator();i.hasNext();) {
            CourseCreditUnitConfig credit = (CourseCreditUnitConfig)i.next();
            exportCredit(offeringElement.addElement("courseCredit"), credit, session);
        }
        for (Iterator i=offering.getCourseOfferings().iterator();i.hasNext();) {
            CourseOffering course = (CourseOffering)i.next();
            exportCourse(offeringElement.addElement("course"), course, session);
        }
        if (!offering.isNotOffered())
            for (Iterator i=offering.getInstrOfferingConfigs().iterator();i.hasNext();) {
                InstrOfferingConfig config = (InstrOfferingConfig)i.next();
                exportConfig(offeringElement.addElement("config"), config, session);
            }
        if (iExams!=null) {
            TreeSet<Exam> exams = iExams.get(offering.getUniqueId());
            if (exams!=null) for (Exam exam : exams)
                exportExam(offeringElement, offering, exam, session);
        }
    }
    
    protected void exportCredit(Element creditElement, CourseCreditUnitConfig credit, Session session) {
        if (credit.getCreditType()!=null)
            creditElement.addAttribute("creditType", credit.getCreditType().getReference());
        if (credit.getCreditUnitType()!=null)
            creditElement.addAttribute("creditUnitType", credit.getCreditUnitType().getReference());
        if (credit.getCreditFormat()!=null)
            creditElement.addAttribute("creditFormat", credit.getCreditFormat());
        if (credit instanceof ArrangeCreditUnitConfig) {
        } else if (credit instanceof FixedCreditUnitConfig) {
            FixedCreditUnitConfig fixedCredit = (FixedCreditUnitConfig)credit;
            creditElement.addAttribute("fixedCredit", fixedCredit.getFixedUnits().toString());
        } else if (credit instanceof VariableRangeCreditUnitConfig) {
            VariableRangeCreditUnitConfig variableCredit = (VariableRangeCreditUnitConfig)credit;
            creditElement.addAttribute("minimumCredit", variableCredit.getMinUnits().toString());
            creditElement.addAttribute("maximumCredit", variableCredit.getMaxUnits().toString());
            creditElement.addAttribute("fractionalCreditAllowed", (variableCredit.isFractionalIncrementsAllowed().booleanValue()?"true":"false"));
        } else if (credit instanceof VariableFixedCreditUnitConfig) {
            VariableFixedCreditUnitConfig variableCredit = (VariableFixedCreditUnitConfig)credit;
            creditElement.addAttribute("minimumCredit", variableCredit.getMinUnits().toString());
            creditElement.addAttribute("maximumCredit", variableCredit.getMaxUnits().toString());
        }
    }
    
    protected void exportCourse(Element courseElement, CourseOffering course, Session session) {
        courseElement.addAttribute("id", (course.getExternalUniqueId()!=null?course.getExternalUniqueId():course.getUniqueId().toString()));
        courseElement.addAttribute("subject", course.getSubjectArea().getSubjectAreaAbbreviation());
        courseElement.addAttribute("courseNbr", course.getCourseNbr());
        courseElement.addAttribute("controlling", course.isIsControl()?"true":"false");
        if (course.getConsentType()!=null)
        	courseElement.addElement("consent").addAttribute("type", course.getConsentType().getReference());
        if (course.getTitle()!=null)
            courseElement.addAttribute("title", course.getTitle());
        if (course.getScheduleBookNote()!=null)
            courseElement.addAttribute("scheduleBookNote", course.getScheduleBookNote());
    }

    protected void exportConfig(Element configElement, InstrOfferingConfig config, Session session) {
        configElement.addAttribute("name", config.getName());
        configElement.addAttribute("limit", (config.isUnlimitedEnrollment()?"inf":config.getLimit().toString()));
        for (Iterator i=config.getSchedulingSubparts().iterator();i.hasNext();) {
            SchedulingSubpart subpart = (SchedulingSubpart)i.next();
            if (subpart.getParentSubpart()==null) {
                exportSubpart(configElement.addElement("subpart"), subpart, session);
            }
        }
        for (Iterator i=config.getSchedulingSubparts().iterator();i.hasNext();) {
            SchedulingSubpart subpart = (SchedulingSubpart)i.next();
            if (subpart.getParentSubpart()==null) {
                for (Iterator j=subpart.getClasses().iterator();j.hasNext();)
                    exportClass(configElement.addElement("class"), (Class_)j.next(), session);
            }
        }
    }
    
    protected void exportSubpart(Element subpartElement, SchedulingSubpart subpart, Session session) {
        subpartElement.addAttribute("type", subpart.getItypeDesc().trim());
        subpartElement.addAttribute("suffix", subpart.getSchedulingSubpartSuffix());
        subpartElement.addAttribute("minPerWeek", subpart.getMinutesPerWk().toString());
        for (Iterator i=subpart.getCreditConfigs().iterator();i.hasNext();) {
            CourseCreditUnitConfig credit = (CourseCreditUnitConfig)i.next();
            exportCredit(subpartElement.addElement("subpartCredit"), credit, session);
        }
        for (Iterator i=subpart.getChildSubparts().iterator();i.hasNext();) {
            SchedulingSubpart childSubpart = (SchedulingSubpart)i.next();
            exportSubpart(subpartElement.addElement("subpart"), childSubpart, session);
        }
    }
    
    protected void exportClass(Element classElement, Class_ clazz, Session session) {
        if (clazz.getExternalUniqueId()!=null)
            classElement.addAttribute("id", clazz.getExternalUniqueId());
        else
            classElement.addAttribute("id", clazz.getUniqueId().toString());
        classElement.addAttribute("type", clazz.getItypeDesc().trim());
        classElement.addAttribute("suffix", (clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString()));
        if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment())
            classElement.addAttribute("limit", "inf");
        else
            classElement.addAttribute("limit", String.valueOf(clazz.getClassLimit()));
        if (clazz.getSchedulePrintNote()!=null)
            classElement.addAttribute("scheduleNote", clazz.getSchedulePrintNote());
        classElement.addAttribute("studentScheduling", clazz.isEnabledForStudentScheduling()?"true":"false");
        classElement.addAttribute("displayInScheduleBook", clazz.isEnabledForStudentScheduling()?"true":"false");
        for (Iterator i=clazz.getChildClasses().iterator();i.hasNext();) {
            Class_ childClazz = (Class_)i.next();
            exportClass(classElement.addElement("class"), childClazz, session);
        }
        if (iExportAssignments) {
            if (clazz.getCommittedAssignment()!=null)
                exportAssignment(classElement, clazz.getCommittedAssignment(), session);
            else if (clazz.getManagingDept().getSolverGroup()!=null && clazz.getManagingDept().getSolverGroup().getCommittedSolution()!=null) {
                exportArrHours(classElement, clazz, session);
            }
        }
        if (clazz.isDisplayInstructor())
            for (Iterator i=clazz.getClassInstructors().iterator();i.hasNext();) {
                ClassInstructor instructor = (ClassInstructor)i.next();
                if (instructor.getInstructor().getExternalUniqueId()!=null)
                    exportInstructor(classElement.addElement("instructor"), instructor, session);
            }
    }
    
    protected void exportInstructor(Element instructorElement, ClassInstructor instructor, Session session) {
        exportInstructor(instructorElement, instructor.getInstructor(), session);
        if (instructor.getPercentShare()!=null)
        	instructorElement.addAttribute("share", instructor.getPercentShare().toString());
        instructorElement.addAttribute("lead", instructor.isLead()?"true":"false");
    }
    
    protected void exportInstructor(Element instructorElement, DepartmentalInstructor instructor, Session session) {
        if (instructor.getExternalUniqueId()!=null)
            instructorElement.addAttribute("id", instructor.getExternalUniqueId());
        if (instructor.getFirstName()!=null)
            instructorElement.addAttribute("fname", instructor.getFirstName());
        if (instructor.getMiddleName()!=null)
            instructorElement.addAttribute("mname", instructor.getMiddleName());
        if (instructor.getLastName()!=null)
            instructorElement.addAttribute("lname", instructor.getLastName());
    }
    
    private static String dayCode2days(int dayCode) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<Constants.DAY_CODES.length;i++)
            if ((dayCode & Constants.DAY_CODES[i])!=0)
                sb.append(Constants.DAY_NAMES_SHORT[i]);
        return sb.toString(); 
    }

    private static String startSlot2startTime(int startSlot) {
        int minHrs = startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }
    
    private static String timeLocation2endTime(TimeLocation time) {
        int minHrs = (time.getStartSlot()+time.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - time.getBreakTime();
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }

    protected void exportAssignment(Element classElement, Assignment assignment, Session session) {
        exportDatePattern(classElement, assignment.getDatePattern(), session);
        exportTimeLocation(classElement, assignment, session);
        exportRooms(classElement, assignment, session);
        //if (assignment.getEvent()!=null) exportEvent(classElement, assignment.getEvent(), session);
    }
    
    protected void exportTimeLocation(Element classElement, Assignment assignment, Session session) {
        TimeLocation time = assignment.getTimeLocation();
        if (time!=null) {
            Element timeElement = classElement.addElement("time");
            timeElement.addAttribute("days", dayCode2days(time.getDayCode()));
            timeElement.addAttribute("startTime", startSlot2startTime(time.getStartSlot()));
            timeElement.addAttribute("endTime", timeLocation2endTime(time));
            DatePattern dp = assignment.getDatePattern();
            if (dp != null && (!dp.isDefault() || "true".equals(ApplicationProperties.getProperty("tmtbl.export.defaultDatePattern", "false"))))
            	timeElement.addAttribute("datePattern", dp.getName());
            if (assignment.getTimePattern() != null)
            	timeElement.addAttribute("timePattern", assignment.getTimePattern().getName());
        }
    }
    
    protected void exportRooms(Element classElement, Assignment assignment, Session session) {
        for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
            Location location = (Location)i.next();
            if (location instanceof Room) {
                Room room = (Room)location;
                Element roomElement = classElement.addElement("room");
                if (room.getExternalUniqueId()!=null)
                    roomElement.addAttribute("id", room.getExternalUniqueId());
                roomElement.addAttribute("building", room.getBuildingAbbv());
                roomElement.addAttribute("roomNbr", room.getRoomNumber());
            } else {
                Element roomElement = classElement.addElement("location");
                if (location.getExternalUniqueId()!=null)
                    roomElement.addAttribute("id", location.getExternalUniqueId());
                roomElement.addAttribute("name", location.getLabel());
            }
        }
    }

    protected void exportArrHours(Element classElement, Class_ clazz, Session session) {
        exportDatePattern(classElement, clazz.effectiveDatePattern(), session);
        exportRequiredRooms(classElement, clazz, session);
    }
    
    protected void exportDatePattern(Element classElement, DatePattern dp, Session session) {
        if (dp!=null && (!dp.equals(session.getDefaultDatePattern()) || "true".equals(ApplicationProperties.getProperty("tmtbl.export.defaultDatePattern", "false")))) {
            Calendar startDate = Calendar.getInstance(Locale.US);
            startDate.setTime(dp.getStartDate());
            Calendar endDate = Calendar.getInstance(Locale.US);
            endDate.setTime(dp.getEndDate());
            
            int startMonth = startDate.get(Calendar.MONTH);
            int endMonth = endDate.get(Calendar.MONTH);
            int startYear = startDate.get(Calendar.YEAR);
            int endYear = endDate.get(Calendar.YEAR);
            if (endYear > startYear){
            	endMonth += (12 * (endYear - startYear));
            }
            
            String first = null, previous = null;
            char[] ptrn = dp.getPattern().toCharArray();
            int charPosition = 0;
            int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK);

            for (int m=startMonth;m<=endMonth;m++) {
                int daysOfMonth = DateUtils.getNrDaysOfMonth(m, startYear);
                int d = (m==startMonth?startDate.get(Calendar.DAY_OF_MONTH):1);
                for (;d<=daysOfMonth && charPosition<ptrn.length; d++) {
                    if (ptrn[charPosition]=='1') { // || (first!=null && dayOfWeek==Calendar.SUNDAY)) {
                        if (first==null) first = (m<0?startYear-1:m>=12?startYear+1:startYear)+"/"+((m<0?12+m:m%12)+1)+"/"+d;
                    } else {
                        if (first!=null) {
                            Element dateElement = classElement.addElement("date");
                            dateElement.addAttribute("startDate", first);
                            dateElement.addAttribute("endDate", previous);
                            first=null;
                        }
                    }
                    previous = (m<0?startYear-1:m>=12?startYear+1:startYear)+"/"+((m<0?12+m:m%12)+1)+"/"+d;
                    charPosition++;
                    dayOfWeek++;
                    if (dayOfWeek>Calendar.SATURDAY) dayOfWeek = Calendar.SUNDAY;
                }
            }
            if (first!=null) {
                Element dateElement = classElement.addElement("date");
                dateElement.addAttribute("startDate", first);
                dateElement.addAttribute("endDate", previous);
                first=null;
            }
        }
    }
    
    protected void exportRequiredRooms(Element classElement, Class_ clazz, Session session) {
        for (Iterator i=clazz.getEffectiveRoomPreferences().iterator();i.hasNext();) {
            RoomPref rp = (RoomPref)i.next();
            if (PreferenceLevel.sRequired.equals(rp.getPrefLevel().getPrefProlog())) {
                if (rp.getRoom() instanceof Room) {
                    Room room = (Room)rp.getRoom();
                    Element roomElement = classElement.addElement("room");
                    if (room.getExternalUniqueId()!=null)
                        roomElement.addAttribute("id", room.getExternalUniqueId());
                    roomElement.addAttribute("building", room.getBuildingAbbv());
                    roomElement.addAttribute("roomNbr", room.getRoomNumber());
                } else {
                    Element roomElement = classElement.addElement("location");
                    if (rp.getRoom().getExternalUniqueId()!=null)
                        roomElement.addAttribute("id", rp.getRoom().getExternalUniqueId());
                    roomElement.addAttribute("name", rp.getRoom().getLabel());
                }
            }
        }
    }
    
    protected void exportExam(Element offeringElement, InstructionalOffering offering, Exam exam, Session session) {
        Element examElement = offeringElement.addElement("exam");
        examElement.addAttribute("id", exam.getUniqueId().toString());
        examElement.addAttribute("name", (exam.getName()==null?exam.generateName():exam.getName()));
        examElement.addAttribute("size", String.valueOf(exam.getSize())); 
        if (exam.getNote()!=null)
            examElement.addAttribute("note", exam.getNote());
        examElement.addAttribute("seatingType", exam.getSeatingType()==Exam.sSeatingTypeExam?"exam":"normal");
        examElement.addAttribute("type", exam.getExamType().getReference());
        Element courseElement = null; CourseOffering lastCourse = null;
        for (Iterator i=exam.getOwnerObjects().iterator();i.hasNext();) {
            Object owner = (Object)i.next();
            if (owner instanceof Class_) {
                Class_ clazz = (Class_)owner;
                if (offering==null) {
                    if (lastCourse==null || !lastCourse.equals(clazz.getSchedulingSubpart().getControllingCourseOffering())) {
                        lastCourse = clazz.getSchedulingSubpart().getControllingCourseOffering();
                        courseElement = examElement.addElement("course");
                        courseElement.addAttribute("id", (lastCourse.getExternalUniqueId()!=null?lastCourse.getExternalUniqueId():lastCourse.getUniqueId().toString()));
                        courseElement.addAttribute("subject", lastCourse.getSubjectArea().getSubjectAreaAbbreviation());
                        courseElement.addAttribute("courseNbr", lastCourse.getCourseNbr());
                    }
                    courseElement.addElement("class")
                    .addAttribute("id", (clazz.getExternalUniqueId()!=null?clazz.getExternalUniqueId():clazz.getUniqueId().toString()))
                    .addAttribute("type", clazz.getItypeDesc().trim())
                    .addAttribute("suffix", (clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString()));
                } else {
                    if (!clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().equals(offering)) continue;
                    examElement.addElement("class")
                    .addAttribute("id", (clazz.getExternalUniqueId()!=null?clazz.getExternalUniqueId():clazz.getUniqueId().toString()))
                    .addAttribute("type", clazz.getItypeDesc().trim())
                    .addAttribute("suffix", (clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString()));
                }
            } else if (owner instanceof InstrOfferingConfig) {
                InstrOfferingConfig config = (InstrOfferingConfig)owner;
                if (offering==null) {
                    if (lastCourse==null || !lastCourse.equals(config.getControllingCourseOffering())) {
                        lastCourse = config.getControllingCourseOffering();
                        courseElement = examElement.addElement("course");
                        courseElement.addAttribute("id", (lastCourse.getExternalUniqueId()!=null?lastCourse.getExternalUniqueId():lastCourse.getUniqueId().toString()));
                        courseElement.addAttribute("subject", lastCourse.getSubjectArea().getSubjectAreaAbbreviation());
                        courseElement.addAttribute("courseNbr", lastCourse.getCourseNbr());
                    }
                    TreeSet subparts = new TreeSet(new SchedulingSubpartComparator()); subparts.addAll(config.getSchedulingSubparts());
                    for (Iterator j=((SchedulingSubpart)subparts.first()).getClasses().iterator();j.hasNext();) {
                        Class_ clazz = (Class_)j.next();
                        courseElement.addElement("class")
                            .addAttribute("id", (clazz.getExternalUniqueId()!=null?clazz.getExternalUniqueId():clazz.getUniqueId().toString()))
                            .addAttribute("type", clazz.getItypeDesc().trim())
                            .addAttribute("suffix", (clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString()));
                    }
                } else {
                    if (!config.getInstructionalOffering().equals(offering)) continue;
                    if (config.getSchedulingSubparts().isEmpty()) continue;
                    TreeSet subparts = new TreeSet(new SchedulingSubpartComparator()); subparts.addAll(config.getSchedulingSubparts());
                    for (Iterator j=((SchedulingSubpart)subparts.first()).getClasses().iterator();j.hasNext();) {
                        Class_ clazz = (Class_)j.next();
                        examElement.addElement("class")
                            .addAttribute("id", (clazz.getExternalUniqueId()!=null?clazz.getExternalUniqueId():clazz.getUniqueId().toString()))
                            .addAttribute("type", clazz.getItypeDesc().trim())
                            .addAttribute("suffix", (clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString()));
                    }
                }
            } else if (owner instanceof CourseOffering) {
                CourseOffering course = (CourseOffering)owner;
                if (offering!=null && !course.getInstructionalOffering().equals(offering)) continue;
                if (offering!=null && offering.getCourseOfferings().size()==1) continue;
                courseElement = examElement.addElement("course"); lastCourse = course;
                courseElement.addAttribute("id", (course.getExternalUniqueId()!=null?course.getExternalUniqueId():course.getUniqueId().toString()));
                courseElement.addAttribute("subject", course.getSubjectArea().getSubjectAreaAbbreviation());
                courseElement.addAttribute("courseNbr", course.getCourseNbr());
            } else if (owner instanceof CourseOffering) {
                InstructionalOffering o = (InstructionalOffering)owner;
                if (offering!=null) continue;
                for (Iterator j=o.getCourseOfferings().iterator();j.hasNext();) {
                    CourseOffering course = (CourseOffering)j.next();
                    courseElement = examElement.addElement("course"); lastCourse = course;
                    courseElement.addAttribute("id", (course.getExternalUniqueId()!=null?course.getExternalUniqueId():course.getUniqueId().toString()));
                    courseElement.addAttribute("subject", course.getSubjectArea().getSubjectAreaAbbreviation());
                    courseElement.addAttribute("courseNbr", course.getCourseNbr());
                }
            }
        }
        for (Iterator i=exam.getInstructors().iterator();i.hasNext();) {
            DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
            if (instructor.getExternalUniqueId()!=null)
                exportInstructor(examElement.addElement("instructor"), instructor, session);
        }
        if (exam.getAssignedPeriod()!=null) {
            Element periodElement = examElement.addElement("period");
            periodElement.addAttribute("date", sDateFormat.format(exam.getAssignedPeriod().getStartDate()));
            periodElement.addAttribute("startTime", sTimeFormat.format(exam.getStartTime(exam.getAssignedPeriod())));
            periodElement.addAttribute("endTime", sTimeFormat.format(exam.getEndTime(exam.getAssignedPeriod())));
            for (Iterator i=exam.getAssignedRooms().iterator();i.hasNext();) {
                Location location = (Location)i.next();
                if (location instanceof Room) {
                    Room room = (Room)location;
                    Element roomElement = examElement.addElement("room");
                    if (room.getExternalUniqueId()!=null)
                        roomElement.addAttribute("id", room.getExternalUniqueId());
                    roomElement.addAttribute("building", room.getBuildingAbbv());
                    roomElement.addAttribute("roomNbr", room.getRoomNumber());
                } else {
                    Element roomElement = examElement.addElement("location");
                    if (location.getExternalUniqueId()!=null)
                        roomElement.addAttribute("id", location.getExternalUniqueId());
                    roomElement.addAttribute("name", location.getLabel());
                }
            }
        }
    }
    
    protected void exportEvent(Element classElement, Event event, Session session) {
        for (Iterator i=event.getMeetings().iterator();i.hasNext();) {
            Meeting meeting = (Meeting)i.next();
            Element meetingElement = classElement.addElement("meeting");
            meetingElement.addAttribute("startDate", sDateFormat.format(meeting.getMeetingDate()));
            meetingElement.addAttribute("endDate", sDateFormat.format(meeting.getMeetingDate()));
            meetingElement.addAttribute("startTime", sDateFormat.format(meeting.getStartTime()));
            meetingElement.addAttribute("endTime", sDateFormat.format(meeting.getStopTime()));
            Calendar c = Calendar.getInstance(Locale.US); c.setTime(meeting.getMeetingDate());
            switch (c.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY : meetingElement.addAttribute("days","M"); break;
                case Calendar.TUESDAY : meetingElement.addAttribute("days","T"); break;
                case Calendar.WEDNESDAY : meetingElement.addAttribute("days","W"); break;
                case Calendar.THURSDAY : meetingElement.addAttribute("days","R"); break;
                case Calendar.FRIDAY : meetingElement.addAttribute("days","F"); break;
                case Calendar.SATURDAY : meetingElement.addAttribute("days","S"); break;
                case Calendar.SUNDAY : meetingElement.addAttribute("days","U"); break;
            }
            if (meeting.getLocation()==null) {
            } else if (meeting.getLocation() instanceof Room) {
                meetingElement.addAttribute("building", ((Room)meeting.getLocation()).getBuildingAbbv());
                meetingElement.addAttribute("room", ((Room)meeting.getLocation()).getRoomNumber());
            } else {
                meetingElement.addAttribute("location", meeting.getLocation().getLabel());
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            if (args.length==0)
                args = new String[] {
                    "c:\\test\\courseOfferings.xml",
                    "puWestLafayetteTrdtn",
                    "2007",
                    "Fal"};

            ToolBox.configureLogging();
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
            
            Session session = Session.getSessionUsingInitiativeYearTerm(args[1], args[2], args[3]);
            
            if (session==null) throw new Exception("Session "+args[1]+" "+args[2]+args[3]+" not found!");
            
            new CourseOfferingExport().saveXml(args[0], session, ApplicationProperties.getProperties());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
