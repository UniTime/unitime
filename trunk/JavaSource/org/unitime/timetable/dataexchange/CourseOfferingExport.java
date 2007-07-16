package org.unitime.timetable.dataexchange;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.util.Constants;

public class CourseOfferingExport extends BaseExport {
    protected static DecimalFormat sTwoNumbersDF = new DecimalFormat("00");
    protected static DecimalFormat sShareDF = new DecimalFormat("0.00");
    
    public void saveXml(Document document, Session session, Properties parameters) throws Exception {
        try {
            beginTransaction();

            Element root = document.addElement("offerings");
            root.addAttribute("campus", session.getAcademicInitiative());
            root.addAttribute("year", session.getAcademicYear());
            root.addAttribute("term", session.getAcademicTerm());
            
            document.addDocType("offerings", "-//UniTime//DTD University Course Timetabling/EN", "http://www.unitime.org/interface/CourseOfferingExport.dtd");
            
            List offerings = getHibSession().createQuery(
                    "select distinct io from InstructionalOffering io " +
                    "left join fetch io.courseOfferings as co "+
                    "left join fetch io.instrOfferingConfigs as ioc "+
                    "left join fetch ioc.schedulingSubparts as ss "+
                    "left join fetch ss.classes as c "+
                    "where " +
                    "io.session.uniqueId=:sessionId").
                    setLong("sessionId",session.getUniqueId().longValue()).
                    setFetchSize(1000).list();
            for (Iterator i=offerings.iterator();i.hasNext();) {
                InstructionalOffering io = (InstructionalOffering)i.next(); 
                exportInstructionalOffering(root, io, session);
            }
            
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
    }
    
    protected void exportInstructionalOffering(Element offeringsElement, InstructionalOffering offering, Session session) {
        Element offeringElement = offeringsElement.addElement("offering");
        offeringElement.addAttribute("id", offering.getUniqueId().toString());
        offeringElement.addAttribute("offered", (offering.isNotOffered()?"false":"true"));
        offeringElement.addAttribute("action", "insert");
        if (offering.isDesignatorRequired()) offeringElement.addElement("designatorRequired");
        if (offering.getConsentType()!=null)
            offeringElement.addElement("consent").addAttribute("type", offering.getConsentType().getReference());
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
        if (course.getExternalUniqueId()!=null)
            courseElement.addAttribute("id", course.getExternalUniqueId());
        courseElement.addAttribute("subject", course.getSubjectArea().getSubjectAreaAbbreviation());
        courseElement.addAttribute("courseNbr", course.getCourseNbr());
        courseElement.addAttribute("controlling", course.isIsControl()?"true":"false");
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
        classElement.addAttribute("id", clazz.getUniqueId().toString());
        classElement.addAttribute("type", clazz.getItypeDesc().trim());
        classElement.addAttribute("suffix", clazz.getSectionNumberString());
        if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment())
            classElement.addAttribute("limit", "inf");
        else
            classElement.addAttribute("limit", String.valueOf(clazz.getClassLimit()));
        if (clazz.getSchedulePrintNote()!=null)
            classElement.addAttribute("scheduleNote", clazz.getSchedulePrintNote());
        classElement.addAttribute("displayInScheduleBook", clazz.isDisplayInScheduleBook()?"true":"false");
        for (Iterator i=clazz.getChildClasses().iterator();i.hasNext();) {
            Class_ childClazz = (Class_)i.next();
            exportClass(classElement.addElement("class"), childClazz, session);
        }
        if (clazz.getCommittedAssignment()!=null)
            exportAssignment(classElement, clazz.getCommittedAssignment(), session);
        else if (clazz.getManagingDept().getSolverGroup()!=null && clazz.getManagingDept().getSolverGroup().getCommittedSolution()!=null) {
            exportArrHours(classElement, clazz, session);
        }
        if (clazz.isDisplayInstructor())
            for (Iterator i=clazz.getClassInstructors().iterator();i.hasNext();) {
                ClassInstructor instructor = (ClassInstructor)i.next(); 
            }
    }
    
    protected void exportInstructor(Element instructorElement, ClassInstructor instructor, Session session) {
        if (instructor.getInstructor().getExternalUniqueId()!=null)
            instructorElement.addElement("id", instructor.getInstructor().getExternalUniqueId());
        if (instructor.getInstructor().getFirstName()!=null)
            instructorElement.addElement("fname", instructor.getInstructor().getFirstName());
        if (instructor.getInstructor().getMiddleName()!=null)
            instructorElement.addElement("mname", instructor.getInstructor().getMiddleName());
        if (instructor.getInstructor().getLastName()!=null)
            instructorElement.addElement("lname", instructor.getInstructor().getLastName());
        instructorElement.addElement("share", sShareDF.format(((double)instructor.getPercentShare())/100.0));
        instructorElement.addElement("lead", instructor.isLead()?"true":"false");
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
        exportDatePattern(classElement, assignment.getClazz(), session);
        exportTimeLocation(classElement, assignment, session);
        exportRooms(classElement, assignment, session);
    }
    
    protected void exportTimeLocation(Element classElement, Assignment assignment, Session session) {
        TimeLocation time = assignment.getTimeLocation();
        if (time!=null) {
            Element timeElement = classElement.addElement("time");
            timeElement.addAttribute("days", dayCode2days(time.getDayCode()));
            timeElement.addAttribute("startTime", startSlot2startTime(time.getStartSlot()));
            timeElement.addAttribute("endTime", timeLocation2endTime(time));
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
                roomElement.addAttribute("name", location.getLabel());
            }
        }
    }

    protected void exportArrHours(Element classElement, Class_ clazz, Session session) {
        exportDatePattern(classElement, clazz, session);
        exportRequiredRooms(classElement, clazz, session);
    }
    
    protected void exportDatePattern(Element classElement, Class_ clazz, Session session) {
        DatePattern dp = clazz.effectiveDatePattern();
        if (dp!=null && !dp.equals(session.getDefaultDatePattern())) {
            Calendar startDate = Calendar.getInstance(Locale.US);
            startDate.setTime(dp.getStartDate());
            Calendar endDate = Calendar.getInstance(Locale.US);
            endDate.setTime(dp.getEndDate());
            
            int startMonth = startDate.get(Calendar.MONTH);
            int endMonth = endDate.get(Calendar.MONTH);
            int startYear = startDate.get(Calendar.YEAR);
            
            String first = null, previous = null;
            char[] ptrn = dp.getPattern().toCharArray();
            int charPosition = 0;
            int dayOfWeek = startDate.get(Calendar.DAY_OF_WEEK);

            for (int m=startMonth;m<=endMonth;m++) {
                int daysOfMonth = session.getNrDaysOfMonth(m);
                int d = (m==startMonth?startDate.get(Calendar.DAY_OF_MONTH):1);
                for (;d<=daysOfMonth && charPosition<ptrn.length; d++) {
                    if (ptrn[charPosition]=='1' || (first!=null && dayOfWeek==Calendar.SUNDAY)) {
                        if (first==null) first = ((m<0?12+m:m%12)+1)+"/"+d;
                    } else {
                        if (first!=null) {
                            Element dateElement = classElement.addElement("date");
                            dateElement.addAttribute("startDate", first);
                            dateElement.addAttribute("endDate", previous);
                            first=null;
                        }
                    }
                    previous = ((m<0?12+m:m%12)+1)+"/"+d;
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
                    roomElement.addAttribute("name", rp.getRoom().getLabel());
                }
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
