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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.ArrangeCreditUnitConfig;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.util.Formats;

/**
 * @author Stephanie Schluttenhofer
 */
public class PointInTimeDataExport extends BaseExport {
    private static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy/M/d");
    private static Formats.Format<Date> sTimeFormat = Formats.getDateFormat("HHmm");
    private HashMap<Long, Element> departmentElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> studentElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> buildingElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> roomTypeElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> creditTypeElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> creditUnitTypeElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> positionTypeElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> teachingResponsibilityElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> locationElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> subjectElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> departmentalInstructorElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> academicClassificationElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> academicAreaElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> majorElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> minorElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> courseTypeElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> classDurationTypeElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> instructionalMethodElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> timePatternElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> datePatternElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> classElements = new HashMap<Long, Element>();
    private HashMap<Long, Element> classEventElements = new HashMap<Long, Element>();
    private Element departmentsElement = null;
    private Element roomTypesElement = null;
    private Element creditTypesElement = null;
    private Element creditUnitTypesElement = null;
    private Element positionTypesElement = null;
    private Element teachingResponsibilitiesElement = null;
    private Element locationsElement = null;
    private Element studentsElement = null;
    private Element offeringsElement = null;
    private Element courseTypesElement = null;
    private Element classDurationTypesElement = null;
    private Element instructionalMethodsElement = null;
    private Element timePatternsElement = null;
    private Element datePatternsElement = null;
    private Element academicAreasElement = null;
    private Element academicClassificationsElement = null;
    private Element majorsElement = null;
    private Element minorsElement = null;
    
	@SuppressWarnings("unchecked")
	private TreeSet<InstructionalOffering> findOfferingsWithClasses(Session acadSession) {
		StringBuilder querySb =  new StringBuilder();
		querySb.append("select io, cco ")
		       .append(" from InstructionalOffering io, CourseOffering cco") 
		       .append(" join fetch io.courseOfferings as co")
		       .append(" join fetch cco.subjectArea as sa")
		       .append(" join fetch sa.department as sad")
		       .append(" join fetch io.instrOfferingConfigs as ioc")
		       .append(" join fetch ioc.schedulingSubparts as ss")
		       .append(" join fetch ss.itype as it")
		       .append(" join fetch ss.classes as c")
		       .append(" left join fetch co.courseType as ct")
		       .append(" left join fetch ioc.instructionalMethod as cim")
		       .append(" left join fetch ss.childSubparts as cs")
		       .append(" left join fetch ss.datePattern as sdp")
		       .append(" left join fetch ss.creditConfigs as scc")
		       .append(" left join fetch scc.creditType as sct")
		       .append(" left join fetch scc.creditUnitType as scut")
		       .append(" left join fetch c.childClasses as cc")
		       .append(" left join fetch c.classInstructors as ci")
		       .append(" left join fetch c.managingDept as md")
		       .append(" left join fetch c.datePattern as cdp")
		       .append(" left join fetch ci.instructor as i")
		       .append(" left join fetch i.department as d")
		       .append(" left join fetch i.positionType as pt")
		       .append(" left join fetch ci.responsibility as tr")
		       .append(" left join fetch c.committedAssignment as ca")
		       .append(" left join fetch ca.solution as s")
		       .append(" left join fetch s.owner as o")
		       .append(" left join fetch ca.datePattern as dp")
		       .append(" left join fetch ca.timePattern as tp")
		       .append(" left join fetch ca.rooms as r")
		       .append(" where io.session.uniqueId = :sessId" )
		       .append(" and cco.uniqueId = io.ctrlCourseId")
		       .append(" and sa.uniqueId = :saId " )
		       .append(" and c.cancelled is false " )
		       .append(" and c.studentEnrollments is not empty")
		       .append(" and io.notOffered is false")
		       ;
		
		
		TreeSet<InstructionalOffering> offerings = new TreeSet<InstructionalOffering>(new InstructionalOfferingComparator(null));
		for(SubjectArea sa : acadSession.getSubjectAreas()){
			info("Fetching Instructional Offerings for Subject Area:  " + sa.getSubjectAreaAbbreviation());
			for (Object[] objects : (List<Object[]>) getHibSession()
					.createQuery(querySb.toString())
					.setLong("sessId", acadSession.getUniqueId().longValue())
					.setLong("saId", sa.getUniqueId().longValue())
					.setFetchSize(1000)
					.list()) {
				offerings.add((InstructionalOffering) objects[0]);				
			}
		}
		return(offerings);
					
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<AcademicArea> findAcademicAreas(Session acadSession){
		StringBuilder querySb =  new StringBuilder();

		querySb.append("select aa ")
		       .append(" from AcademicArea aa") 
		       .append(" left join fetch aa.posMajors as ama")
		       .append(" left join fetch aa.posMinors as ami")
		       .append(" where aa.session.uniqueId = :sessId" )
		       ;
		
		ArrayList<AcademicArea> academicAreas = new ArrayList<AcademicArea>();
		info("Fetching AcademicArea");
		academicAreas.addAll((List<AcademicArea>) getHibSession()
			.createQuery(querySb.toString())
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.setFetchSize(1000)
			.list());
		return(academicAreas);
						
	}

	@SuppressWarnings("unchecked")
	private ArrayList<TimePattern> findTimePatterns(Session acadSession){
		StringBuilder querySb =  new StringBuilder();

		querySb.append("select tp ")
		       .append(" from TimePattern tp") 
		       .append(" left join fetch tp.days as d")
		       .append(" left join fetch tp.times as t")
		       .append(" where tp.session.uniqueId = :sessId" )
		       ;
		
		ArrayList<TimePattern> timePatterns = new ArrayList<TimePattern>();
		info("Fetching Time Patterns");
		timePatterns.addAll((List<TimePattern>) getHibSession()
			.createQuery(querySb.toString())
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.setFetchSize(1000)
			.list());
		return(timePatterns);
						
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Location> findLocations(Session acadSession){
		StringBuilder querySb1 =  new StringBuilder();

		querySb1.append("select r ")
		       .append(" from Room r") 
		       .append(" join fetch r.building")
		       .append(" left join fetch r.roomType as rt")
		       .append(" left join fetch r.roomDepts as rd")		  
		       .append(" left join fetch rd.department as rdd")		  
		       .append(" where r.session.uniqueId = :sessId" )
		       ;

		StringBuilder querySb2 =  new StringBuilder();

		querySb2.append("select n ")
		       .append(" from NonUniversityLocation n") 
		       .append(" left join fetch n.roomType as rt")
		       .append(" left join fetch n.roomDepts as rd")		  
		       .append(" left join fetch rd.department as rdd")		  
		       .append(" where n.session.uniqueId = :sessId" )
		       ;

		ArrayList<Location> locations = new ArrayList<Location>();
		info("Fetching Rooms");
		locations.addAll((List<Location>) getHibSession()
			.createQuery(querySb1.toString())
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.setFetchSize(1000)
			.list());
		info("Fetching Non Unversity Locations");
		locations.addAll((List<Location>) getHibSession()
			.createQuery(querySb2.toString())
			.setLong("sessId", acadSession.getUniqueId().longValue())
			.setFetchSize(1000)
			.list());
		return(locations);
						
	}

	
	@SuppressWarnings("unchecked")
	private ArrayList<StudentClassEnrollment> findStudentClassEnrollments(Session acadSession) {
		StringBuilder querySb =  new StringBuilder();

		querySb.append("select sce ")
		       .append(" from StudentClassEnrollment sce") 
		       .append(" join fetch sce.student s")
		       .append(" join fetch sce.clazz as c")
		       .append(" join fetch sce.courseOffering as co")
		       .append(" left join fetch co.subjectArea as sa")
		       .append(" left join fetch s.academicAreaClassifications as aac")
		       .append(" left join fetch s.posMajors as ma")
		       .append(" left join fetch s.posMinors as mi")
		       .append(" left join fetch aac.academicArea as aa")
		       .append(" left join fetch aac.academicClassification as ac")
		       .append(" where s.session.uniqueId = :sessId" )
		       .append(" and sa.uniqueId = :saId")
		       .append(" and s.classEnrollments is not empty" )
		       .append(" and c.cancelled is false " )
		       ;
		
		ArrayList<StudentClassEnrollment> students = new ArrayList<StudentClassEnrollment>();
		for(SubjectArea sa : acadSession.getSubjectAreas()){
				info("Fetching Student Class Enrollments for Subject Area:  " + sa.getSubjectAreaAbbreviation());
				students.addAll((List<StudentClassEnrollment>) getHibSession()
					.createQuery(querySb.toString())
					.setLong("sessId", acadSession.getUniqueId().longValue())
					.setLong("saId", sa.getUniqueId().longValue())
					.setFetchSize(1000)
					.list());
		}
		return(students);
					
	}
	
	
	@SuppressWarnings("unchecked")
	private ArrayList<Object[]> findClassEvents(Session acadSession) {
		StringBuilder querySb1 =  new StringBuilder();
		querySb1.append("select e, m, l")
		  .append(" from ClassEvent e, Room l") 
		  .append(" inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co")
		  .append(" inner join e.meetings as m")
		  .append(" left join fetch e.clazz as c")
		  .append(" left join fetch c.committedAssignment as ca")
		  .append(" left join fetch ca.datePattern as dp")
		  .append(" left join fetch ca.timePattern as tp")
		  .append(" left join fetch ca.rooms as r")
		  .append(" where e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessId")
	      .append(" and co.subjectArea.uniqueId = :saId")
	      .append(" and co.isControl = true")
	      .append(" and e.clazz.studentEnrollments is not empty")
	      .append(" and e.clazz.cancelled is false")
	      .append(" and l.permanentId = m.locationPermanentId")
	      .append(" and l.session.uniqueId = :sessId")
	      ;	
		
		StringBuilder querySb2 =  new StringBuilder();
		querySb2.append("select e, m, l")
		  .append(" from ClassEvent e, NonUniversityLocation l") 
		  .append(" inner join e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co")
		  .append(" inner join e.meetings as m")
		  .append(" left join fetch e.clazz as c")
		  .append(" left join fetch c.committedAssignment as ca")
		  .append(" left join fetch ca.datePattern as dp")
		  .append(" left join fetch ca.timePattern as tp")
		  .append(" left join fetch ca.rooms as r")
		  .append(" where e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessId")
	      .append(" and co.subjectArea.uniqueId = :saId")
	      .append(" and co.isControl = true")
	      .append(" and e.clazz.studentEnrollments is not empty")
	      .append(" and e.clazz.cancelled is false")
	      .append(" and l.permanentId = m.locationPermanentId")
	      .append(" and l.session.uniqueId = :sessId")
	      ;		
		

		ArrayList<Object[]> events = new ArrayList<Object[]>();
		for(SubjectArea sa : acadSession.getSubjectAreas()){
			info("Fetching Class Events for Subject Area:  " + sa.getSubjectAreaAbbreviation());
			events.addAll((List<Object[]>) getHibSession()
					.createQuery(querySb1.toString())
					.setLong("sessId", acadSession.getUniqueId().longValue())
					.setLong("saId", sa.getUniqueId().longValue())
					.setFetchSize(1000)
					.list());
			events.addAll((List<Object[]>) getHibSession()
					.createQuery(querySb2.toString())
					.setLong("sessId", acadSession.getUniqueId().longValue())
					.setLong("saId", sa.getUniqueId().longValue())
					.setFetchSize(1000)
					.list());
			}

		return(events);
					
	}

    public void saveXml(Document document, Session session, Properties parameters) throws Exception {
        try {
            beginTransaction();
            Date timestamp = new Date();
            info("Data extract for Point in Time Data started at:  " + timestamp.toString());
            Element root = document.addElement(PointInTimeDataImport.sRootElementName);
            root.addAttribute(PointInTimeDataImport.sAcademicSessionUniqueIdAttribute, session.getUniqueId().toString());
            root.addAttribute(PointInTimeDataImport.sAcademicInitiativeAttribute, session.getAcademicInitiative());
            root.addAttribute(PointInTimeDataImport.sAcademicYearAttribute, session.getAcademicYear());
            root.addAttribute(PointInTimeDataImport.sAcademicTermAttribute, session.getAcademicTerm());
            root.addAttribute(PointInTimeDataImport.sDateFormatAttribute, sDateFormat.toPattern());
            root.addAttribute(PointInTimeDataImport.sTimeFormatAttribute, sTimeFormat.toPattern());
            root.addAttribute(PointInTimeDataImport.sCreatedAttribute, (sDateFormat.format(timestamp) + " " + sTimeFormat.format(timestamp)));
            root.addAttribute(PointInTimeDataImport.sSessionBeginDateAttribute, sDateFormat.format(session.getSessionBeginDateTime()));
            root.addAttribute(PointInTimeDataImport.sSessionEndDateAttribute, sDateFormat.format(session.getSessionEndDateTime()));
            root.addAttribute(PointInTimeDataImport.sClassesEndDateAttribute, sDateFormat.format(session.getClassesEndDateTime()));
            if (session.getDefaultClassDurationType() != null) {
            	root.addAttribute(PointInTimeDataImport.sDurationTypeAttribute, session.getDefaultClassDurationType().getReference());
            }
            String name = session.getAcademicInitiative()+session.getAcademicYear()+session.getAcademicTerm()+timestamp.getTime();
            String note = "This is a point in time data snapshot for session:  " + session.getLabel() + ", taken on:  " + timestamp.toString();
            root.addAttribute(PointInTimeDataImport.sPointInTimeNameAttribute, name);
            root.addAttribute(PointInTimeDataImport.sPointInTimeNoteAttribute, note);
                       
            document.addDocType(PointInTimeDataImport.sRootElementName, "-//UniTime//DTD University Course Timetabling/EN", "http://www.unitime.org/interface/PointInTmeData.dtd");
            
            info("Loading Data...");
            TreeSet<InstructionalOffering> offerings = findOfferingsWithClasses(session);
            info("Loaded " + offerings.size() + " Instructional Offerings");
            ArrayList<AcademicArea> academicAreas = findAcademicAreas(session);
            info("Loaded " + academicAreas.size() + " Academic Areas");
            ArrayList<StudentClassEnrollment> studentClassEnrollments = findStudentClassEnrollments(session);
            info("Loaded " + studentClassEnrollments.size() + " Student Class Enrollments");
            ArrayList<Location> locations = findLocations(session);
            info("Loaded " + locations.size() + " Locations");
            ArrayList<TimePattern> timePatterns = findTimePatterns(session);
            info("Loaded " + timePatterns.size() + " Time Patterns");
            ArrayList<Object[]> eventMeetings = findClassEvents(session);
            info("Loaded " + eventMeetings.size() + " Class Events");
            info("Default Date Pattern:  " + session.getDefaultDatePatternNotNull());
            commitTransaction();
            timePatterns.size();
            locations.size();
            Date endTransTimestamp = new Date();
            info("Data extract for Point in Time Data ended at:  " + endTransTimestamp.toString());
            info("Milliseconds elapsed = " + (endTransTimestamp.getTime() - timestamp.getTime()));
                    
            departmentsElement = root.addElement(PointInTimeDataImport.sDepartmentsElementName);
            roomTypesElement = root.addElement(PointInTimeDataImport.sRoomTypesElementName);
            creditTypesElement = root.addElement(PointInTimeDataImport.sCreditTypesElementName);
            creditUnitTypesElement = root.addElement(PointInTimeDataImport.sCreditUnitTypesElementName);
            positionTypesElement = root.addElement(PointInTimeDataImport.sPositionTypesElementName);
            teachingResponsibilitiesElement = root.addElement(PointInTimeDataImport.sTeachingResponsibilitiesElementName);
            locationsElement = root.addElement(PointInTimeDataImport.sLocationsElementName);
            studentsElement = root.addElement(PointInTimeDataImport.sStudentsElementName);
            courseTypesElement = root.addElement(PointInTimeDataImport.sCourseTypesElementName);
            classDurationTypesElement = root.addElement(PointInTimeDataImport.sClassDurationTypesElementName);
            instructionalMethodsElement = root.addElement(PointInTimeDataImport.sInstructionalMethodsElementName);
            timePatternsElement = root.addElement(PointInTimeDataImport.sTimePatternsElementName);
            datePatternsElement = root.addElement(PointInTimeDataImport.sDatePatternsElementName);
            academicAreasElement = root.addElement(PointInTimeDataImport.sAcademicAreasElementName);
            academicClassificationsElement = root.addElement(PointInTimeDataImport.sAcademicClassificationsElementName);
            majorsElement = root.addElement(PointInTimeDataImport.sMajorsElementName);
            minorsElement = root.addElement(PointInTimeDataImport.sMinorsElementName);
            offeringsElement = root.addElement(PointInTimeDataImport.sOfferingsElementName);

            
            info("Exporting "+offerings.size()+" offerings ...");
            for (InstructionalOffering io : offerings) {
            	info("Exporting offering: " + io.getControllingCourseOffering().getCourseNameWithTitle());
                exportInstructionalOffering(offeringsElement, io, session);
            }
            int numMeetings = eventMeetings.size();
            info("Exporting student class enrollments ...");
            for(StudentClassEnrollment sce : studentClassEnrollments){
            	exportStudentClassEnrollment(sce);
            }
            info("Exporting "+  numMeetings +" class event meetings ...");
            int count = 0;
            for (Object[] objs : eventMeetings) {
            	count++;
            	ClassEvent classEvent = (ClassEvent)objs[0];
            	Meeting meeting = (Meeting)objs[1];
            	Location location = (Location)objs[2];
                exportClassEvent(classEvent, meeting, location);
                if (count % 10000 == 0){
                	info("Exported " + count + " of " + numMeetings + " class event meetings, " + 100 * count / numMeetings + "% complete.");
                }
            }
            info("Export of class event meetings complete.");
            Date endProcessingTimestamp = new Date();
            info("XML creation for Point in Time Data ended at:  " + endProcessingTimestamp.toString());
            info("Milliseconds elapsed since data extract = " + (endProcessingTimestamp.getTime() - endTransTimestamp.getTime()));

            
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
    }
    
    private void exportInstructionalOffering(Element offeringsElement, InstructionalOffering offering, Session session) {
        Element offeringElement = offeringsElement.addElement(PointInTimeDataImport.sOfferingElementName);
        offeringElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, offering.getUniqueId().toString());
        offeringElement.addAttribute(PointInTimeDataImport.sPermanentIdAttribute, offering.getInstrOfferingPermId().toString());
        offeringElement.addAttribute(PointInTimeDataImport.sDemandAttribute, offering.getDemand().toString());
        offeringElement.addAttribute(PointInTimeDataImport.sLimitAttribute, (offering.getLimit() == null?"0":offering.getLimit().toString()));
        offeringElement.addAttribute(PointInTimeDataImport.sUniqueIdRolledForwardFromAttribute, (offering.getUniqueIdRolledForwardFrom() == null?"":offering.getUniqueIdRolledForwardFrom().toString()));
        offeringElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (offering.getExternalUniqueId()!=null?offering.getExternalUniqueId():offering.getUniqueId().toString()));
        
        for (CourseOffering course : offering.getCourseOfferings()) {
            exportCourse(offeringElement.addElement(PointInTimeDataImport.sCourseElementName), course, session);
        }
        if (!offering.isNotOffered()) {
            for (InstrOfferingConfig config : offering.getInstrOfferingConfigs()) {
            	if (configHasAtLeastOneStudentEnrolled(config)) {
            		exportConfig(offeringElement.addElement(PointInTimeDataImport.sConfigElementName), config, session);
            	}
            }
        }
    }
    
    private void exportCredit(Element subpartElement, CourseCreditUnitConfig credit) {
    	/* This only exports the minimum credit number as the credit for variable credit subparts. 
    	 *    UniTime currently stores no information about the number of credits a student is 
    	 *    enrolled to received for a sections.  Point in time data will treat all enrollments
    	 *    as the minimum amount of credit for the section.  If the credit data known for students
    	 *    changes in the future this should be updated to reflect that change. */
    	if (credit != null){
	        if (credit.getCreditType()!=null) {
	        	if (!creditTypeElements.containsKey(credit.getCreditType().getUniqueId())){
	        		exportCreditType(credit.getCreditType());
	        	}
	            subpartElement.addAttribute(PointInTimeDataImport.sCreditTypeAttribute, credit.getCreditType().getUniqueId().toString());
	        }
	        if (credit.getCreditUnitType()!=null){
	        	if (!creditUnitTypeElements.containsKey(credit.getCreditUnitType().getUniqueId())){
	        		exportCreditUnitType(credit.getCreditUnitType());
	        	}
	        	subpartElement.addAttribute(PointInTimeDataImport.sCreditUnitTypeAttribute, credit.getCreditUnitType().getUniqueId().toString());
	        }
	        if (credit instanceof ArrangeCreditUnitConfig) {
	        } else if (credit instanceof FixedCreditUnitConfig) {
	            FixedCreditUnitConfig fixedCredit = (FixedCreditUnitConfig)credit;
	            subpartElement.addAttribute(PointInTimeDataImport.sCreditAttribute, fixedCredit.getFixedUnits().toString());
	        } else if (credit instanceof VariableRangeCreditUnitConfig) {
	            VariableRangeCreditUnitConfig variableCredit = (VariableRangeCreditUnitConfig)credit;
	            subpartElement.addAttribute(PointInTimeDataImport.sCreditAttribute, variableCredit.getMinUnits().toString());
	        } else if (credit instanceof VariableFixedCreditUnitConfig) {
	            VariableFixedCreditUnitConfig variableCredit = (VariableFixedCreditUnitConfig)credit;
	            subpartElement.addAttribute(PointInTimeDataImport.sCreditAttribute, variableCredit.getMinUnits().toString());
	        }
    	}
    }
    
    private void exportDepartment(Department department){
    	Element departmentElement = departmentsElement.addElement(PointInTimeDataImport.sDepartmentElementName);
    	departmentElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, department.getUniqueId().toString());
    	departmentElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (department.getExternalUniqueId() == null?department.getUniqueId().toString():department.getExternalUniqueId()));
    	departmentElement.addAttribute(PointInTimeDataImport.sNameAttribute, department.getName());
    	departmentElement.addAttribute(PointInTimeDataImport.sAbbreviationAttribute, department.getAbbreviation());
    	departmentElement.addAttribute(PointInTimeDataImport.sDepartmentCode, department.getDeptCode());
    	departmentElements.put(department.getUniqueId(), departmentElement);
    }


    private void exportSubjectArea(SubjectArea subjectArea){
    	if (!departmentElements.containsKey(subjectArea.getDepartment().getUniqueId())){
    		exportDepartment(subjectArea.getDepartment());
    	}
    	Element subjectElement = departmentElements.get(subjectArea.getDepartment().getUniqueId()).addElement(PointInTimeDataImport.sSubjectAreaElementName);
    	subjectElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, subjectArea.getUniqueId().toString());
    	subjectElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (subjectArea.getExternalUniqueId() == null?subjectArea.getUniqueId().toString():subjectArea.getExternalUniqueId()));
    	subjectElement.addAttribute(PointInTimeDataImport.sSubjectAreaAbbreviationAttribute, subjectArea.getSubjectAreaAbbreviation());
    	subjectElement.addAttribute(PointInTimeDataImport.sTitleAttribute, subjectArea.getTitle());
    	subjectElements.put(subjectArea.getUniqueId(), subjectElement);
    	
    }
    
    private void exportCourseType(CourseType courseType){
    	Element courseTypeElement = courseTypesElement.addElement(PointInTimeDataImport.sCourseTypeElementName);
    	courseTypeElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, courseType.getUniqueId().toString());
    	courseTypeElement.addAttribute(PointInTimeDataImport.sReferenceAttribute, courseType.getReference());
    	courseTypeElement.addAttribute(PointInTimeDataImport.sLabelAttribute, courseType.getLabel());
    	courseTypeElements.put(courseType.getUniqueId(), courseTypeElement);
    }
    
    private void exportCourse(Element courseElement, CourseOffering course, Session session) {
    	if (!subjectElements.containsKey(course.getSubjectArea().getUniqueId())){
    		exportSubjectArea(course.getSubjectArea());
    	}
        courseElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, course.getUniqueId().toString());
        courseElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (course.getExternalUniqueId()!=null?course.getExternalUniqueId():course.getUniqueId().toString()));
        courseElement.addAttribute(PointInTimeDataImport.sSubjectAreaAbbreviationAttribute, course.getSubjectArea().getSubjectAreaAbbreviation());
        courseElement.addAttribute(PointInTimeDataImport.sCourseNbrAttribute, course.getCourseNbr());
        courseElement.addAttribute(PointInTimeDataImport.sControllingAttribute, course.isIsControl()?"true":"false");
        courseElement.addAttribute(PointInTimeDataImport.sPermanentIdAttribute, (course.getPermId() == null ? course.getUniqueId().toString() : course.getPermId()));
        if (course.getProjectedDemand() != null){
            courseElement.addAttribute(PointInTimeDataImport.sProjectedDemandAttribute,
            		course.getProjectedDemand().toString());
        }
        if (course.getTitle() != null) {
        	courseElement.addAttribute(PointInTimeDataImport.sTitleAttribute, course.getTitle());
        }
        if (course.getNbrExpectedStudents() != null) {
        	courseElement.addAttribute(PointInTimeDataImport.sNumberExpectedStudentsAttribute, course.getNbrExpectedStudents().toString());
        }
        if (course.getDemand() != null){
        	courseElement.addAttribute(PointInTimeDataImport.sLastlikeDemandAttribute, course.getDemand().toString());
        }
        if (course.getCourseType() != null) {
        	if (!courseTypeElements.containsKey(course.getCourseType().getUniqueId())){
        		exportCourseType(course.getCourseType());
        	}
        	courseElement.addAttribute(PointInTimeDataImport.sCourseTypeIdAttribute, (course.getCourseType().getUniqueId().toString()) );
        }
        if (course.getUniqueIdRolledForwardFrom() != null){
        	courseElement.addAttribute(PointInTimeDataImport.sUniqueIdRolledForwardFromAttribute, course.getUniqueIdRolledForwardFrom().toString());
        }
    }
    
    private void exportClassDurationType(ClassDurationType classDurationType){
    	Element classDurationTypeElement = classDurationTypesElement.addElement(PointInTimeDataImport.sClassDurationTypeElementName);
    	classDurationTypeElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, classDurationType.getUniqueId().toString());
    	classDurationTypeElement.addAttribute(PointInTimeDataImport.sReferenceAttribute, classDurationType.getReference());
    	classDurationTypeElement.addAttribute(PointInTimeDataImport.sLabelAttribute, classDurationType.getLabel());
    	classDurationTypeElement.addAttribute(PointInTimeDataImport.sAbbreviationAttribute, classDurationType.getAbbreviation());
    	classDurationTypeElement.addAttribute(PointInTimeDataImport.sImplementationAttribute, classDurationType.getImplementation());
    	classDurationTypeElement.addAttribute(PointInTimeDataImport.sParameterAttribute, classDurationType.getParameter());
    	classDurationTypeElement.addAttribute(PointInTimeDataImport.sVisibleAttribute, (classDurationType.getVisible()?"true":"false"));
    	courseTypeElements.put(classDurationType.getUniqueId(), classDurationTypeElement);
    }

    private void exportInstructionalMethod(InstructionalMethod instructionalMethod){
    	Element instructionalMethodElement = instructionalMethodsElement.addElement(PointInTimeDataImport.sInstructionalMethodElementName);
    	instructionalMethodElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, instructionalMethod.getUniqueId().toString());
    	instructionalMethodElement.addAttribute(PointInTimeDataImport.sReferenceAttribute, instructionalMethod.getReference());
    	instructionalMethodElement.addAttribute(PointInTimeDataImport.sLabelAttribute, instructionalMethod.getLabel());
    	instructionalMethodElement.addAttribute(PointInTimeDataImport.sVisibleAttribute, (instructionalMethod.getVisible()?"true":"false"));
    	instructionalMethodElements.put(instructionalMethod.getUniqueId(), instructionalMethodElement);
    }

    private boolean configHasAtLeastOneStudentEnrolled(InstrOfferingConfig instrOfferingConfig){
    	for(SchedulingSubpart ss : instrOfferingConfig.getSchedulingSubparts()){
    		for(Class_ c : ss.getClasses()){
    			if (!c.isCancelled().booleanValue() && c.getEnrollment() > 0){
    				return(true);
    			}
    		}
    	}
    	return(false);
    }

    private void exportConfig(Element configElement, InstrOfferingConfig config, Session session) {
        configElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, config.getUniqueId().toString());
        configElement.addAttribute(PointInTimeDataImport.sNameAttribute, config.getName());
        configElement.addAttribute(PointInTimeDataImport.sUnlimitedEnrollmentAttributeName, (config.isUnlimitedEnrollment() ? "true" : "false"));
        if (config.getClassDurationType() != null) {
        	if (!classDurationTypeElements.containsKey(config.getClassDurationType().getUniqueId())){
        		exportClassDurationType(config.getClassDurationType());
        	}
        	configElement.addAttribute(PointInTimeDataImport.sDurationTypeAttribute, config.getClassDurationType().getReference());
        }
        if (config.getInstructionalMethod() != null) {
        	if (!instructionalMethodElements.containsKey(config.getInstructionalMethod().getUniqueId())){
        		exportInstructionalMethod(config.getInstructionalMethod());
        	}
        	configElement.addAttribute(PointInTimeDataImport.sInstructionalMethodAttribute, config.getInstructionalMethod().getReference());
        }
        if (config.getUniqueIdRolledForwardFrom() != null) {
        	configElement.addAttribute(PointInTimeDataImport.sUniqueIdRolledForwardFromAttribute, config.getUniqueIdRolledForwardFrom().toString());
        }
        for (SchedulingSubpart subpart : config.getSchedulingSubparts()) {
            if (subpart.getParentSubpart()==null) {
                exportSubpart(configElement.addElement(PointInTimeDataImport.sSubpartElementName), subpart, session);
            }
        }
    }
    
    private void exportSubpart(Element subpartElement, SchedulingSubpart subpart, Session session) {
        subpartElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, subpart.getUniqueId().toString());
        subpartElement.addAttribute(PointInTimeDataImport.sTypeAttribute, subpart.getItypeDesc().trim());
        subpartElement.addAttribute(PointInTimeDataImport.sSuffixAttribute, subpart.getSchedulingSubpartSuffixCache());
        subpartElement.addAttribute(PointInTimeDataImport.sMinPerWeekAttribute, subpart.getMinutesPerWk().toString());
        subpartElement.addAttribute(PointInTimeDataImport.sStudentAllowOverlapAttribute, (subpart.getStudentAllowOverlap()?"true":"false"));
        if (subpart.getUniqueIdRolledForwardFrom() != null)
        	subpartElement.addAttribute(PointInTimeDataImport.sUniqueIdRolledForwardFromAttribute, subpart.getUniqueIdRolledForwardFrom().toString());

        for (CourseCreditUnitConfig credit :subpart.getCreditConfigs()) {
            exportCredit(subpartElement, credit);
        }
        
        for (Class_ clazz : subpart.getClasses()){
        	if (!clazz.getCancelled() && clazz.getEnrollment().intValue() > 0){
        		exportClass(subpartElement.addElement(PointInTimeDataImport.sClassElementName), clazz);
        	}
        }
        
        for (SchedulingSubpart childSubpart : subpart.getChildSubparts()) {
            exportSubpart(subpartElement.addElement(PointInTimeDataImport.sSubpartElementName), childSubpart, session);
        }
    }
    
    private void exportDatePattern(DatePattern datePattern){
    	Element datePatternElement = datePatternsElement.addElement(PointInTimeDataImport.sDatePatternElementName);
    	datePatternElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, datePattern.getUniqueId().toString());
    	datePatternElement.addAttribute(PointInTimeDataImport.sNameAttribute, datePattern.getName());
    	datePatternElement.addAttribute(PointInTimeDataImport.sPatternAttribute, datePattern.getPattern());
    	datePatternElement.addAttribute(PointInTimeDataImport.sOffsetAttribute, datePattern.getOffset().toString());
    	datePatternElement.addAttribute(PointInTimeDataImport.sTypeAttribute, datePattern.getType().toString());
    	datePatternElement.addAttribute(PointInTimeDataImport.sVisibleAttribute, datePattern.getVisible()?"true":"false");
    	if (datePattern.getNumberOfWeeks() != null) {
    		datePatternElement.addAttribute(PointInTimeDataImport.sNumberOfWeeksAttribute, datePattern.getNumberOfWeeks().toString());
    	}
    	datePatternElements.put(datePattern.getUniqueId(), datePatternElement);
    }

    private void exportTimePatternDays(Element timePatternDaysElement, TimePatternDays timePatternDays){
    	timePatternDaysElement.addAttribute(PointInTimeDataImport.sDayCodeAttribute, timePatternDays.getDayCode().toString());
    }
    
    private void exportTimePatternTimes(Element timePatternTimesElement, TimePatternTime timePatternTimes){
    	timePatternTimesElement.addAttribute(PointInTimeDataImport.sStartSlotAttribute, timePatternTimes.getStartSlot().toString());
    }

    private void exportTimePattern(TimePattern timePattern){
    	Element timePatternElement = timePatternsElement.addElement(PointInTimeDataImport.sTimePatternElementName);
    	timePatternElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, timePattern.getUniqueId().toString());
    	timePatternElement.addAttribute(PointInTimeDataImport.sNameAttribute, timePattern.getName());
    	timePatternElement.addAttribute(PointInTimeDataImport.sMinutesPerMeetingAttribute, timePattern.getMinPerMtg().toString());
    	timePatternElement.addAttribute(PointInTimeDataImport.sSlotsPerMeetingAttribute, timePattern.getSlotsPerMtg().toString());
    	timePatternElement.addAttribute(PointInTimeDataImport.sNumberOfMeetingsPerWeekAttribute, timePattern.getNrMeetings().toString());
    	timePatternElement.addAttribute(PointInTimeDataImport.sVisibleAttribute, timePattern.getVisible()?"true":"false");
    	timePatternElement.addAttribute(PointInTimeDataImport.sTypeAttribute, timePattern.getType().toString());
    	timePatternElement.addAttribute(PointInTimeDataImport.sBreakTimeAttribute, timePattern.getBreakTime().toString());
    	for(TimePatternDays day : timePattern.getDays()){
    		exportTimePatternDays(timePatternElement.addElement(PointInTimeDataImport.sTimePatternDaysElementName), day);
    	}
    	
    	for(TimePatternTime time : timePattern.getTimes()){
    		exportTimePatternTimes(timePatternElement.addElement(PointInTimeDataImport.sTimePatternTimeElementName), time);
    	}
    	
    	timePatternElements.put(timePattern.getUniqueId(), timePatternElement);
    }
    
    private void exportClass(Element classElement, Class_ clazz) {
        classElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, clazz.getUniqueId().toString());
        classElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, getExternalUniqueId(clazz));
        if (clazz.getParentClass() != null){
            classElement.addAttribute(PointInTimeDataImport.sParentClassUniqueIdAttribute, clazz.getParentClass().getUniqueId().toString());        	
        }
        classElement.addAttribute(PointInTimeDataImport.sStudentSchedulingAttribute, clazz.isEnabledForStudentScheduling()?"true":"false");
        if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) {
        	classElement.addAttribute(PointInTimeDataImport.sLimitAttribute, "inf");
        }
        else {
        	classElement.addAttribute(PointInTimeDataImport.sLimitAttribute, String.valueOf(clazz.getClassLimit()));
        }
        if (clazz.getNbrRooms() != null){
        	classElement.addAttribute(PointInTimeDataImport.sNumberOfRoomsAttribute, clazz.getNbrRooms().toString());        	
        }
        if (!datePatternElements.containsKey(clazz.effectiveDatePattern().getUniqueId())){
        	exportDatePattern(clazz.effectiveDatePattern());
        }
        classElement.addAttribute(PointInTimeDataImport.sDatePatternUniqueIdAttribute, clazz.effectiveDatePattern().getUniqueId().toString());
        if (clazz.getCommittedAssignment() != null){
        	if (!timePatternElements.containsKey(clazz.getCommittedAssignment().getTimePattern().getUniqueId())){
        		exportTimePattern(clazz.getCommittedAssignment().getTimePattern());
        	}
            classElement.addAttribute(PointInTimeDataImport.sTimePatternUniqueIdAttribute, clazz.getCommittedAssignment().getTimePattern().getUniqueId().toString());        	
        }
        classElement.addAttribute(PointInTimeDataImport.sClassSuffixAttribute, (clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString()));
        classElement.addAttribute(PointInTimeDataImport.sSectionNumberAttribute, clazz.getSectionNumber().toString());
        if (!departmentElements.containsKey(clazz.getManagingDept().getUniqueId())){
        	exportDepartment(clazz.getManagingDept());
        }
        classElement.addAttribute(PointInTimeDataImport.sManagingDepartmentUniqueIdAttribute, clazz.getManagingDept().getUniqueId().toString());
        if (clazz.getUniqueIdRolledForwardFrom() != null)
        	classElement.addAttribute(PointInTimeDataImport.sUniqueIdRolledForwardFromAttribute, clazz.getUniqueIdRolledForwardFrom().toString());

        for (ClassInstructor instructor : clazz.getClassInstructors()) {
            exportClassInstructor(classElement.addElement(PointInTimeDataImport.sClassInstructorElementName), instructor);
        }
        
        classElements.put(clazz.getUniqueId(), classElement);
    }
    
    private void exportStudent(Student student){
    	Element studentElement = studentsElement.addElement(PointInTimeDataImport.sStudentElementName);
    	studentElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, student.getUniqueId().toString());
    	studentElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (student.getExternalUniqueId() == null?student.getUniqueId().toString():student.getExternalUniqueId()));
    	studentElement.addAttribute(PointInTimeDataImport.sFirstNameAttribute, student.getFirstName());
    	studentElement.addAttribute(PointInTimeDataImport.sMiddleNameAttribute, student.getMiddleName());
    	studentElement.addAttribute(PointInTimeDataImport.sLastNameAttribute, student.getLastName());
    	studentElements.put(student.getUniqueId(), studentElement);
    	for(PosMajor major : student.getPosMajors()){
    		for(AcademicAreaClassification aac : student.getAcademicAreaClassifications()){
    			if (aac.getAcademicArea().getPosMajors().contains(major)) {
    				exportAcadAreaMajorClassification(studentElement.addElement(PointInTimeDataImport.sAcadAreaMajorClassificationElementName), aac, major);
    				break;
    			}
    		}
    	}
    	for(PosMinor minor : student.getPosMinors()){
    		for(AcademicAreaClassification aac : student.getAcademicAreaClassifications()){
    			if (aac.getAcademicArea().getPosMinors().contains(minor)) {
    				exportAcadAreaMinorClassification(studentElement.addElement(PointInTimeDataImport.sAcadAreaMinorClassificationElementName), aac, minor);
    				break;
    			}
    		}
    	}
    }
    
    private void exportAcadAreaMajorClassification(Element acadAreaMajorClassificationElement,
			AcademicAreaClassification academicAreaClassification, PosMajor major) {
    	if (!majorElements.containsKey(major.getUniqueId())){
    		exportMajor(major);
    	}
    	if (!academicAreaElements.containsKey(academicAreaClassification.getAcademicArea().getUniqueId())){
    		exportAcademicArea(academicAreaClassification.getAcademicArea());
    	}
    	if (!academicClassificationElements.containsKey(academicAreaClassification.getAcademicClassification().getUniqueId())){
    		exportAcademicClassification(academicAreaClassification.getAcademicClassification());
    	}
    	acadAreaMajorClassificationElement.addAttribute(PointInTimeDataImport.sAcademicAreaUniqueIdAttribute, academicAreaClassification.getAcademicArea().getUniqueId().toString());
    	acadAreaMajorClassificationElement.addAttribute(PointInTimeDataImport.sAcademicClassificationUniqueIdAttribute, academicAreaClassification.getAcademicClassification().getUniqueId().toString());
    	acadAreaMajorClassificationElement.addAttribute(PointInTimeDataImport.sMajorUniqueIdAttribute, major.getUniqueId().toString());
	}

    private void exportAcadAreaMinorClassification(Element acadAreaMinorClassificationElement,
			AcademicAreaClassification academicAreaClassification, PosMinor minor) {
    	if (!majorElements.containsKey(minor.getUniqueId())){
    		exportMinor(minor);
    	}
    	if (!academicAreaElements.containsKey(academicAreaClassification.getAcademicArea().getUniqueId())){
    		exportAcademicArea(academicAreaClassification.getAcademicArea());
    	}
    	if (!academicClassificationElements.containsKey(academicAreaClassification.getAcademicClassification().getUniqueId())){
    		exportAcademicClassification(academicAreaClassification.getAcademicClassification());
    	}
    	acadAreaMinorClassificationElement.addAttribute(PointInTimeDataImport.sAcademicAreaUniqueIdAttribute, academicAreaClassification.getAcademicArea().getUniqueId().toString());
    	acadAreaMinorClassificationElement.addAttribute(PointInTimeDataImport.sAcademicClassificationUniqueIdAttribute, academicAreaClassification.getAcademicClassification().getUniqueId().toString());
    	acadAreaMinorClassificationElement.addAttribute(PointInTimeDataImport.sMinorUniqueIdAttribute, minor.getUniqueId().toString());
	}

	private void exportMajor(PosMajor major) {
    	Element majorElement = majorsElement.addElement(PointInTimeDataImport.sMajorElementName);
    	majorElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, major.getUniqueId().toString());
    	majorElement.addAttribute(PointInTimeDataImport.sCodeAttribute, major.getCode());
    	majorElement.addAttribute(PointInTimeDataImport.sNameAttribute, major.getName());
    	majorElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (major.getExternalUniqueId() == null? major.getUniqueId().toString() : major.getExternalUniqueId()));
    	majorElements.put(major.getUniqueId(), majorElement);
	}

	private void exportMinor(PosMinor minor) {
    	Element minorElement = minorsElement.addElement(PointInTimeDataImport.sMinorElementName);
    	minorElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, minor.getUniqueId().toString());
    	minorElement.addAttribute(PointInTimeDataImport.sCodeAttribute, minor.getCode());
    	minorElement.addAttribute(PointInTimeDataImport.sNameAttribute, minor.getName());
    	minorElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (minor.getExternalUniqueId() == null? minor.getUniqueId().toString() : minor.getExternalUniqueId()));
    	minorElements.put(minor.getUniqueId(), minorElement);
	}

	private void exportAcademicClassification(AcademicClassification academicClassification) {
    	Element academicClassificationElement = academicClassificationsElement.addElement(PointInTimeDataImport.sAcademicClassificationElementName);
    	academicClassificationElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, academicClassification.getUniqueId().toString());
    	academicClassificationElement.addAttribute(PointInTimeDataImport.sCodeAttribute, academicClassification.getCode());
    	academicClassificationElement.addAttribute(PointInTimeDataImport.sNameAttribute, academicClassification.getName());
    	academicClassificationElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (academicClassification.getExternalUniqueId() == null? academicClassification.getUniqueId().toString() : academicClassification.getExternalUniqueId()));
    	academicClassificationElements.put(academicClassification.getUniqueId(), academicClassificationElement);
	}

	private void exportAcademicArea(AcademicArea academicArea) {
    	Element academicAreaElement = academicAreasElement.addElement(PointInTimeDataImport.sAcademicAreaElementName);
    	academicAreaElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, academicArea.getUniqueId().toString());
    	academicAreaElement.addAttribute(PointInTimeDataImport.sAbbreviationAttribute, academicArea.getAcademicAreaAbbreviation());
    	academicAreaElement.addAttribute(PointInTimeDataImport.sTitleAttribute, academicArea.getTitle());
    	academicAreaElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (academicArea.getExternalUniqueId() == null? academicArea.getUniqueId().toString() : academicArea.getExternalUniqueId()));
    	academicAreaElements.put(academicArea.getUniqueId(), academicAreaElement);
	}
	
	
	private void exportStudentClassEnrollment(StudentClassEnrollment studentClassEnrollment){
    	if (!studentElements.containsKey(studentClassEnrollment.getStudent().getUniqueId())){
    		exportStudent(studentClassEnrollment.getStudent());
    	}
    	Element studentClassEnrollmentElement = studentElements.get(studentClassEnrollment.getStudent().getUniqueId()).addElement(PointInTimeDataImport.sEnrollmentElementName);
    	studentClassEnrollmentElement.addAttribute(PointInTimeDataImport.sClassUniqueIdAttribute, studentClassEnrollment.getClazz().getUniqueId().toString());
    	studentClassEnrollmentElement.addAttribute(PointInTimeDataImport.sCourseOfferingUniqueIdAttribute, studentClassEnrollment.getCourseOffering().getUniqueId().toString());
    	studentClassEnrollmentElement.addAttribute(PointInTimeDataImport.sTimestampAttribute, (sDateFormat.format(studentClassEnrollment.getTimestamp()) + " " + sTimeFormat.format(studentClassEnrollment.getTimestamp())));
    	studentClassEnrollmentElement.addAttribute(PointInTimeDataImport.sChangedByAttribute, studentClassEnrollment.getChangedBy());
    }

    private void exportClassEvent(ClassEvent classEvent, Meeting meeting, Location location) {
    	Element classEventElement = classEventElements.get(classEvent.getUniqueId());
    	if (classEventElement == null){
	    	classEventElement = classElements.get(classEvent.getClazz().getUniqueId()).addElement(PointInTimeDataImport.sClassEventElementName);
			classEventElement.addAttribute(PointInTimeDataImport.sNameAttribute, classEvent.getEventName());
			classEventElements.put(classEvent.getUniqueId(), classEventElement);
    	}
		exportClassMeeting(classEventElement.addElement("classMeeting"), meeting, classEvent.getClazz(), location);
	}
    
    private int calcTotalMinPerMeeting(Meeting meeting) {
    	return(meeting.getStopPeriod().intValue() - meeting.getStartPeriod().intValue()) * 5 - ( meeting.getStartOffset() == null ? 0 : meeting.getStartOffset()) + ( meeting.getStopOffset() == null ? 0 : meeting.getStopOffset().intValue());
    }
    
	private void exportClassMeetingUtilPeriods(Element classMeetingElement, Meeting meeting, Class_ clazz){

		int firstPeriod = ((meeting.getStartPeriod().intValue() * 5) 
							+ (meeting.getStartOffset() != null ? meeting.getStartOffset().intValue() : 0)) / 5;
		int lastPeriod = (new Double(Math.ceil(((meeting.getStopPeriod().doubleValue() * (double) 5) 
				+ (meeting.getStopOffset() != null ? meeting.getStopOffset().doubleValue() : (double) 0)) / (double) 5))).intValue();
		
		int totalMinPerMeeting = calcTotalMinPerMeeting(meeting); 
		int timePatMinPerMtg;
		if (clazz.getCommittedAssignment() == null) {
			timePatMinPerMtg = totalMinPerMeeting;
		} else {
			timePatMinPerMtg = clazz.getCommittedAssignment().getMinutesPerMeeting();
		}
		int totalTimePeriodsNeeded = (new Double(Math.ceil((double)timePatMinPerMtg / (double) 5))).intValue();
		int meetPeriodsCount = 0;
		int extraPeriodsCount = 0;
		if (totalMinPerMeeting <= timePatMinPerMtg) {
			for(int i = firstPeriod; i < lastPeriod; i++) {
				if (meetPeriodsCount < totalTimePeriodsNeeded) {
					Element classMeetingUtilPeriod = classMeetingElement.addElement(PointInTimeDataImport.sClassMeetingUtilPeriodElementName);
					classMeetingUtilPeriod.addAttribute(PointInTimeDataImport.sPeriodAttribute, Integer.toString(i));
					meetPeriodsCount++;
				} else {
					extraPeriodsCount++;
				}
			}
			
		} else {
			int totalIncorporatedBreakTime = totalMinPerMeeting - timePatMinPerMtg;
			int numIncorporatedBreaks = (new Double(Math.ceil((double)timePatMinPerMtg / ApplicationProperty.StandardClassMeetingLengthInMinutes.doubleValue()))).intValue() - 1;
			int minPerHourOfIncorporatedBreaks = (numIncorporatedBreaks == 0 ? 0 : totalIncorporatedBreakTime / numIncorporatedBreaks);
			int numBreakPeriods = minPerHourOfIncorporatedBreaks/5;
			int numPeriodsPerStandardHour = (new Double(Math.ceil(ApplicationProperty.StandardClassMeetingLengthInMinutes.doubleValue()/5))).intValue();
			boolean markAsMeeting = true;
			int meetCount = 0;
			int skipCount = 0;
			if ((lastPeriod - firstPeriod) < totalTimePeriodsNeeded) {
				Debug.info("Warning - Not enough 5 minute periods will be stored for meeting: " + meeting.getUniqueId().toString());
			}
			for(int i = firstPeriod; i < lastPeriod; i++) {
				if (meetPeriodsCount < totalTimePeriodsNeeded) {
					if (markAsMeeting){
						Element classMeetingUtilPeriod = classMeetingElement.addElement(PointInTimeDataImport.sClassMeetingUtilPeriodElementName);
						classMeetingUtilPeriod.addAttribute(PointInTimeDataImport.sPeriodAttribute, Integer.toString(i));
						meetCount++;
						meetPeriodsCount++;
						if (meetCount >= numPeriodsPerStandardHour  && numBreakPeriods > 0){
							markAsMeeting = false;
							meetCount = 0;
						}
					} else {
						skipCount++;
						if (skipCount >= numBreakPeriods){
							markAsMeeting = true;
							skipCount = 0;
						}
					}
				} else {
					extraPeriodsCount++;
				}
			}
		}
		if (extraPeriodsCount > 0){
			Debug.info("Info - More than enough 5 minute periods for meeting:  " + meeting.getUniqueId().toString() + " the last " + extraPeriodsCount + " were not marked as meeting.");					
		}

	}



	private void exportClassMeeting(Element meetingElement, Meeting meeting, Class_ clazz, Location location) {
		int calcMinutesPerMeeting = calcTotalMinPerMeeting(meeting); 
		
		int timePatternMinPerMeeting;
		if(clazz.getCommittedAssignment() == null) { 
			timePatternMinPerMeeting = calcMinutesPerMeeting;
			Debug.info("Info - class meeting: " + clazz.getClassLabel() + " missing committed assignement.  Using calculated minutes per meeting.");
		} else {
			timePatternMinPerMeeting = clazz.getCommittedAssignment().getMinutesPerMeeting();
		}
		if (calcMinutesPerMeeting > timePatternMinPerMeeting){
			calcMinutesPerMeeting = timePatternMinPerMeeting;
		}

		meetingElement.addAttribute(PointInTimeDataImport.sMeetingDateAttribute, sDateFormat.format(meeting.getMeetingDate()));
		meetingElement.addAttribute(PointInTimeDataImport.sStartPeriodAttribute, meeting.getStartPeriod().toString());
		meetingElement.addAttribute(PointInTimeDataImport.sStopPeriodAttribute, meeting.getStopPeriod().toString());
		meetingElement.addAttribute(PointInTimeDataImport.sStartOffsetAttribute, meeting.getStartOffset().toString());
		meetingElement.addAttribute(PointInTimeDataImport.sStopOffsetAttribute, meeting.getStopOffset().toString());
		meetingElement.addAttribute(PointInTimeDataImport.sTimePatternMinutesPerMeetingAttribute, Integer.toString(timePatternMinPerMeeting));
		meetingElement.addAttribute(PointInTimeDataImport.sCalculatedMinutesPerMeetingAttribute, Integer.toString(calcMinutesPerMeeting));
		if (!locationElements.containsKey(location.getUniqueId())){
			exportLocation(location);
		}
		meetingElement.addAttribute(PointInTimeDataImport.sLocationUniqueIdAttribute, location.getUniqueId().toString());
		
		exportClassMeetingUtilPeriods(meetingElement, meeting, clazz);
		
	}

	private void exportBuilding(Building building){
		Element buildingElement = locationsElement.addElement(PointInTimeDataImport.sBuildingElementName);
		buildingElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, building.getUniqueId().toString());   	
		buildingElement.addAttribute(PointInTimeDataImport.sAbbreviationAttribute, building.getAbbreviation());   	
		buildingElement.addAttribute(PointInTimeDataImport.sNameAttribute, building.getName());   
		if (building.getCoordinateX() != null) {
			buildingElement.addAttribute(PointInTimeDataImport.sCoordinateXAttribute, building.getCoordinateX().toString());   	
		}
		if (building.getCoordinateY() != null) {
			buildingElement.addAttribute(PointInTimeDataImport.sCoordinateYAttribute, building.getCoordinateY().toString());   	
		}
		buildingElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (building.getExternalUniqueId() == null ? building.getUniqueId().toString() : building.getExternalUniqueId()));   	
		buildingElements.put(building.getUniqueId(), buildingElement);
	}
	
    private void exportRoomType(RoomType roomType){
    	Element roomTypeElement = roomTypesElement.addElement(PointInTimeDataImport.sRoomTypeElementName);
    	roomTypeElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, roomType.getUniqueId().toString());
    	roomTypeElement.addAttribute(PointInTimeDataImport.sReferenceAttribute, roomType.getReference());
    	roomTypeElement.addAttribute(PointInTimeDataImport.sLabelAttribute, roomType.getLabel());
    	roomTypeElement.addAttribute(PointInTimeDataImport.sOrderAttribute, roomType.getOrd().toString());
    	roomTypeElement.addAttribute(PointInTimeDataImport.sIsRoomAttribute, (roomType.isRoom()?"true":"false"));

    	roomTypeElements.put(roomType.getUniqueId(), roomTypeElement);
    }
    
    private void exportCreditType(CourseCreditType creditType){
    	Element creditTypeElement = creditTypesElement.addElement(PointInTimeDataImport.sCreditTypeElementName);
    	creditTypeElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, creditType.getUniqueId().toString());
    	creditTypeElement.addAttribute(PointInTimeDataImport.sReferenceAttribute, creditType.getReference());
    	creditTypeElement.addAttribute(PointInTimeDataImport.sLabelAttribute, creditType.getLabel());
    	creditTypeElement.addAttribute(PointInTimeDataImport.sAbbreviationAttribute, creditType.getAbbreviation());
    	creditTypeElement.addAttribute(PointInTimeDataImport.sLegacyCourseMasterCodeAttribute, creditType.getLegacyCourseMasterCode());
    	creditTypeElements.put(creditType.getUniqueId(), creditTypeElement);
    }

    private void exportCreditUnitType(CourseCreditUnitType creditUnitType){
    	Element creditUnitTypeElement = creditUnitTypesElement.addElement(PointInTimeDataImport.sCreditUnitTypeElementName);
    	creditUnitTypeElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, creditUnitType.getUniqueId().toString());
    	creditUnitTypeElement.addAttribute(PointInTimeDataImport.sReferenceAttribute, creditUnitType.getReference());
    	creditUnitTypeElement.addAttribute(PointInTimeDataImport.sLabelAttribute, creditUnitType.getLabel());
    	creditUnitTypeElement.addAttribute(PointInTimeDataImport.sAbbreviationAttribute, creditUnitType.getAbbreviation());
    	creditUnitTypeElements.put(creditUnitType.getUniqueId(), creditUnitTypeElement);
    }

    private void exportPositionType(PositionType positionType){
    	Element positionTypeElement = positionTypesElement.addElement(PointInTimeDataImport.sPositionTypeElementName);
    	positionTypeElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, positionType.getUniqueId().toString());
    	positionTypeElement.addAttribute(PointInTimeDataImport.sReferenceAttribute, positionType.getReference());
    	positionTypeElement.addAttribute(PointInTimeDataImport.sLabelAttribute, positionType.getLabel());
    	positionTypeElement.addAttribute(PointInTimeDataImport.sOrderAttribute, positionType.getSortOrder().toString());

    	positionTypeElements.put(positionType.getUniqueId(), positionTypeElement);
    }

	private void exportLocation(Location location) {
		if (!roomTypeElements.containsKey(location.getRoomType().getUniqueId())){
			exportRoomType(location.getRoomType());
		}
		Element roomElement = null;
		if (location instanceof Room) {
			Room room = (Room) location;
			if (!buildingElements.containsKey(room.getBuilding().getUniqueId())){
				exportBuilding(room.getBuilding());
			}
			roomElement = buildingElements.get(room.getBuilding().getUniqueId()).addElement(PointInTimeDataImport.sRoomElementName);
			roomElement.addAttribute(PointInTimeDataImport.sRoomNumberAttribute, room.getRoomNumber());   	
		} else if (location instanceof NonUniversityLocation) {
			NonUniversityLocation nonUniversityLocation = (NonUniversityLocation) location;
			roomElement = locationsElement.addElement(PointInTimeDataImport.sNonUniversityLocationElementName);
			roomElement.addAttribute(PointInTimeDataImport.sNameAttribute, nonUniversityLocation.getName());   	
		}
		roomElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, location.getUniqueId().toString());   	
		roomElement.addAttribute(PointInTimeDataImport.sPermanentIdAttribute, location.getPermanentId().toString());   	
		roomElement.addAttribute(PointInTimeDataImport.sRoomTypeIdAttribute, location.getRoomType().getUniqueId().toString());   	
		roomElement.addAttribute(PointInTimeDataImport.sCapacityAttribute, location.getCapacity().toString());  
		if (location.getControllingDepartment() != null){
			if (!departmentElements.containsKey(location.getControllingDepartment().getUniqueId())){
				exportDepartment(location.getControllingDepartment());
			}
			roomElement.addAttribute(PointInTimeDataImport.sControllingDepartmentUniqueIdAttribute, location.getControllingDepartment().getUniqueId().toString());
		}
		if (location.getCoordinateX() != null) {
			roomElement.addAttribute(PointInTimeDataImport.sCoordinateXAttribute, location.getCoordinateX().toString());   	
		}
		if (location.getCoordinateY() != null) {
			roomElement.addAttribute(PointInTimeDataImport.sCoordinateYAttribute, location.getCoordinateY().toString());   	
		}
		roomElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, (location.getExternalUniqueId() == null ? location.getUniqueId().toString() : location.getExternalUniqueId()));   	
		locationElements.put(location.getUniqueId(), roomElement);

		
	}


	private void exportClassInstructor(Element instructorElement, ClassInstructor instructor) {
    	if (!departmentalInstructorElements.containsKey(instructor.getInstructor().getUniqueId())){
	       exportDepartmentalInstructor(instructor.getInstructor());   		
    	}
    	instructorElement.addAttribute(PointInTimeDataImport.sDepartmentalInstructorUniqueIdAttribute, instructor.getInstructor().getUniqueId().toString());
    	if (instructor.getResponsibility() != null) {
    		if (!teachingResponsibilityElements.containsKey(instructor.getResponsibility().getUniqueId())) {
    			exportTeachingResponsibility(instructor.getResponsibility());
    		}
    		instructorElement.addAttribute(PointInTimeDataImport.sResponsibilityUniqueIdAttribute, instructor.getResponsibility().getUniqueId().toString());
    	}
    	instructorElement.addAttribute(PointInTimeDataImport.sNormalizedPercentShareAttribute, Integer.toString(calculateNormalizedPercentShareForInstructor(instructor)));
        if (instructor.getPercentShare()!=null)
        	instructorElement.addAttribute("share", instructor.getPercentShare().toString());
        instructorElement.addAttribute("lead", instructor.isLead()?"true":"false");
    }
	
	private int calculateNormalizedPercentShareForInstructor(ClassInstructor classInstructor){
		if (classInstructor.getPercentShare() == null || classInstructor.getPercentShare().intValue() == 0){
			return(0);
		} else {
			if (classInstructor.getClassInstructing().getClassInstructors().size() == 1) {
				return(100);
			} else {
				int total = 0;
				for(ClassInstructor ci : classInstructor.getClassInstructing().getClassInstructors()){
					if(ci.getPercentShare() != null) {
						if ((classInstructor.getResponsibility() == null && ci.getResponsibility() == null)
							    || (classInstructor.getResponsibility() != null && ci.getResponsibility() != null
							        && classInstructor.getResponsibility().equals(ci.getResponsibility()))) {
							total += ci.getPercentShare().intValue();
						}
					}
				}
				if (total == 100) {
					return(classInstructor.getPercentShare().intValue());
				} else {
					return(classInstructor.getPercentShare().intValue()*100/total);
				}
			}
		}
		
	}

    
    private void exportTeachingResponsibility(TeachingResponsibility responsibility) {
    	Element responsibilityElement = teachingResponsibilitiesElement.addElement(PointInTimeDataImport.sTeachingResponsibilityElementName);
    	responsibilityElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, responsibility.getUniqueId().toString());
    	responsibilityElement.addAttribute(PointInTimeDataImport.sReferenceAttribute, responsibility.getReference());
    	responsibilityElement.addAttribute(PointInTimeDataImport.sLabelAttribute, responsibility.getLabel());
    	responsibilityElement.addAttribute(PointInTimeDataImport.sAbbreviationAttribute, responsibility.getAbbreviation());
    	responsibilityElement.addAttribute(PointInTimeDataImport.sInstructorAttribute, (responsibility.isInstructor()?"true":"false"));
    	responsibilityElement.addAttribute(PointInTimeDataImport.sCoordinatorAttribute, (responsibility.isCoordinator()?"true":"false"));

    	teachingResponsibilityElements.put(responsibility.getUniqueId(), responsibilityElement);
	}

	private void exportDepartmentalInstructor(DepartmentalInstructor instructor) {
    	if (!departmentElements.containsKey(instructor.getDepartment().getUniqueId())){
    		exportDepartment(instructor.getDepartment());
    	}
    	Element departmentalInstructorElement = departmentElements.get(instructor.getDepartment().getUniqueId()).addElement(PointInTimeDataImport.sDeptInstructorElementName);
       	departmentalInstructorElement.addAttribute(PointInTimeDataImport.sUniqueIdAttribute, instructor.getUniqueId().toString());   	
        if (instructor.getExternalUniqueId()!=null)
        	departmentalInstructorElement.addAttribute(PointInTimeDataImport.sExternalIdAttribute, instructor.getExternalUniqueId());
        if (instructor.getFirstName()!=null)
        	departmentalInstructorElement.addAttribute(PointInTimeDataImport.sFirstNameAttribute, instructor.getFirstName());
        if (instructor.getMiddleName()!=null)
        	departmentalInstructorElement.addAttribute(PointInTimeDataImport.sMiddleNameAttribute, instructor.getMiddleName());
        if (instructor.getLastName()!=null)
        	departmentalInstructorElement.addAttribute(PointInTimeDataImport.sLastNameAttribute, instructor.getLastName());
        if (instructor.getCareerAcct() != null)
        	departmentalInstructorElement.addAttribute(PointInTimeDataImport.sCareerAcctAttribute, instructor.getCareerAcct());
        if (instructor.getEmail() != null)
        	departmentalInstructorElement.addAttribute(PointInTimeDataImport.sEmailAttribute, instructor.getEmail());
        if (instructor.getPositionType() != null) {
        	if (!positionTypeElements.containsKey(instructor.getPositionType().getUniqueId())){
        		exportPositionType(instructor.getPositionType());
        	}
        	departmentalInstructorElement.addAttribute(PointInTimeDataImport.sPositionTypeUniqueIdAttribute, instructor.getPositionType().getUniqueId().toString());
        }
        departmentalInstructorElements.put(instructor.getUniqueId(), departmentalInstructorElement);
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
            
            new PointInTimeDataExport().saveXml(args[0], session, ApplicationProperties.getProperties());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	private String getExternalUniqueId(Class_ clazz) {
		if (clazz.getExternalUniqueId() != null)
			return clazz.getExternalUniqueId();
		else
			return clazz.getClassLabel();
	}    
}
