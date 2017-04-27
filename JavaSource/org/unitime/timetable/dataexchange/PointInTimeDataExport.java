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
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.util.Constants;
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
    
	public static String sRootElementName = "pointInTimeData";
	public static String sNameAttribute = "name";
	public static String sNoteAttribute = "note";
	public static String sCreatedAttribute = "created";
	public static String sDateFormatAttribute = "dateFormat";
	public static String sTimeFormatAttribute = "timeFormat";
	public static String sPointInTimeNameAttribute = "pointInTimeName";
	public static String sPointInTimeNoteAttribute = "pointInTimeNote";
	public static String sAcademicSessionUniqueIdAttribute = "academicSessionUniqueId";
	public static String sAcademicInitiativeAttribute = "academicInitiativeUniqueId";
	public static String sAcademicYearAttribute = "academicYearUniqueId";
	public static String sAcademicTermAttribute = "academicTermUniqueId";
	public static String sSessionBeginDateAttribute = "sessionBeginDate";
	public static String sSessionEndDateAttribute = "sessionEndDate";
	public static String sClassesEndDateAttribute = "classesEndDate";
	public static String sDurationTypeAttribute = "durationType";
	public static String sCourseTypesElementName = "courseTypes";
	public static String sCourseTypeElementName = "courseType";
	public static String sPositionTypesElementName = "positionTypes";
	public static String sPositionTypeElementName = "positionType";
	public static String sUniqueIdAttribute = "uniqueId";
	public static String sReferenceAttribute = "reference";
	public static String sLabelAttribute = "label";
	public static String sRoomTypesElementName = "roomTypes";
	public static String sRoomTypeElementName = "roomType";
	public static String sCreditTypesElementName = "creditTypes";
	public static String sCreditTypeElementName = "creditType";
	public static String sLegacyCourseMasterCodeAttribute = "legacyCourseMasterCode";
	public static String sCreditUnitTypesElementName = "creditUnitTypes";
	public static String sCreditUnitTypeElementName = "creditUnitType";
	public static String sIsRoomAttribute = "isRoom";
	public static String sOrderAttribute = "order";
	public static String sTeachingResponsibilitiesElementName = "teachingResponsibilities";
	public static String sTeachingResponsibilityElementName = "teachingResponsibility";
	public static String sInstructorAttribute = "instructor";
	public static String sCoordinatorAttribute = "coordinator";
	public static String sOptionsAttribute = "options";
	public static String sInstructionalMethodsElementName = "instructionalMethods";
	public static String sInstructionalMethodElementName = "instructionalMethod";
	public static String sVisibleAttribute = "visible";
	public static String sClassDurationTypesElementName = "classDurationTypes";
	public static String sClassDurationTypeElementName = "classDurationType";
	public static String sAbbreviationAttribute = "abbreviation";
	public static String sImplementationAttribute = "implementation";
	public static String sParameterAttribute = "parameter";
	public static String sDepartmentsElementName = "departments";
	public static String sDepartmentElementName = "department";
	public static String sDepartmentCode = "departmentCode";
	public static String sExternalIdAttribute = "externalId";
	public static String sSubjectAreaElementName = "subjectArea";
	public static String sSubjectAreaAbbreviationAttribute = "subjectAreaAbbreviation";
	public static String sTitleAttribute = "title";
	public static String sDeptInstructorElementName = "departmentalInstructor";
	public static String sFirstNameAttribute = "firstName";
	public static String sMiddleNameAttribute = "middleName";
	public static String sLastNameAttribute = "lastName";
	public static String sEmailAttribute = "email";
	public static String sCareerAcctAttribute = "careerAcct";
	public static String sPositionTypeUniqueIdAttribute = "positionTypeUniqueId";
	public static String sAcademicAreasElementName = "academicAreas";
	public static String sAcademicAreaElementName = "academicArea";
	public static String sAcademicClassificationsElementName = "academicClassifications";
	public static String sAcademicClassificationElementName = "academicClassification";
	public static String sCodeAttribute = "code";
	public static String sMajorsElementName = "majors";
	public static String sMajorElementName = "major";
	public static String sMinorsElementName = "minors";
	public static String sMinorElementName = "minor";
	public static String sLocationsElementName = "locations";
	public static String sBuildingElementName = "building";
	public static String sCoordinateXAttribute = "coordinateX";
	public static String sCoordinateYAttribute = "coordinateY";
	public static String sRoomElementName = "room";
	public static String sRoomNumberAttribute = "roomNbr";
	public static String sRoomTypeIdAttribute = "roomType";
	public static String sCapacityAttribute = "capacity";
	public static String sControllingDepartmentUniqueIdAttribute = "controllingDepartmentUniqueId";
	public static String sNonUniversityLocationElementName = "nonUniversityLocation";
	public static String sTimePatternsElementName = "timePatterns";
	public static String sTimePatternElementName = "timePattern";
	public static String sMinutesPerMeetingAttribute = "minutesPerMeeting";
	public static String sSlotsPerMeetingAttribute = "slotsPerMeeting";
	public static String sNumberOfMeetingsPerWeekAttribute = "numberOfMeetingsPerWeek";
	public static String sTypeAttribute = "type";
	public static String sBreakTimeAttribute = "breakTime";
	public static String sTimePatternDaysElementName = "timePatternDays";
	public static String sDayCodeAttribute = "dayCode";
	public static String sTimePatternTimeElementName = "timePatternTime";
	public static String sStartSlotAttribute = "startSlot";
	public static String sDatePatternsElementName = "datePatterns";
	public static String sDatePatternElementName = "datePattern";
	public static String sPatternAttribute = "pattern";
	public static String sOffsetAttribute = "offset";
	public static String sNumberOfWeeksAttribute = "numberOfWeeks";
	public static String sOfferingsElementName = "offerings";	
	public static String sOfferingElementName = "offering";
	public static String sPermanentIdAttribute = "permanentId";
	public static String sLimitAttribute = "limit";
	public static String sDemandAttribute = "demand";
	public static String sUniqueIdRolledForwardFromAttribute = "uniqueIdRolledForwardFrom";
	public static String sCourseElementName = "course";
	public static String sCourseNbrAttribute = "courseNbr";
	public static String sControllingAttribute = "controlling";
	public static String sProjectedDemandAttribute = "projectDemand";
	public static String sNumberExpectedStudentsAttribute = "numberExpectedStudents";
	public static String sLastlikeDemandAttribute = "lastlikeDemand";
	public static String sCourseTypeIdAttribute = "courseTypeId";
	public static String sOfferingCoordinatorElementName = "offeringCoordinator";
	public static String sConfigElementName = "config";
	public static String sUnlimitedEnrollmentAttributeName = "unlimitedEnrollment";
	public static String sInstructionalMethodAttribute = "instructionalMethod";
	public static String sSubpartElementName = "subpart";
	public static String sMinPerWeekAttribute = "minPerWeek";
	public static String sSuffixAttribute = "suffix";
	public static String sStudentAllowOverlapAttribute = "studentAllowOverlap";
	public static String sCreditTypeAttribute = "creditType";
	public static String sCreditUnitTypeAttribute = "creditUnitType";
	public static String sCreditAttribute = "credit";
	public static String sClassElementName = "class";
	public static String sParentClassUniqueIdAttribute = "parentClassUniqueId";
	public static String sDatePatternUniqueIdAttribute = "datePatternUniqueId";
	public static String sTimePatternUniqueIdAttribute = "timePatternUniqueId";
	public static String sStudentSchedulingAttribute = "studentScheduling";
	public static String sNumberOfRoomsAttribute = "numberOfRooms";
	public static String sManagingDepartmentUniqueIdAttribute = "managingDepartmentUniqueId";
	public static String sClassSuffixAttribute = "classSuffix";
	public static String sSectionNumberAttribute = "sectionNumber";
	public static String sClassInstructorElementName = "classInstructor";
	public static String sNormalizedPercentShareAttribute = "normalizedPercentShare";
	public static String sResponsibilityUniqueIdAttribute = "responsibilityUniqueId";
	public static String sLeadAttribute = "lead";
	public static String sDepartmentalInstructorUniqueIdAttribute = "departmentalInstructorUniqueId";
	public static String sShareAttribute = "share";
	public static String sClassEventElementName = "classEvent";
	public static String sClassMeetingElementName = "classMeeting";
	public static String sMeetingDateAttribute = "meetingDate";
	public static String sStartPeriodAttribute = "startPeriod";
	public static String sStopPeriodAttribute = "stopPeriod";
	public static String sStartOffsetAttribute = "startOffset";
	public static String sStopOffsetAttribute = "stopOffset";
	public static String sLocationUniqueIdAttribute = "locationUniqueId";
	public static String sTimePatternMinutesPerMeetingAttribute = "timePatternMinutesPerMeeting";
	public static String sCalculatedMinutesPerMeetingAttribute = "calculatedMinutesPerMeeting";
	public static String sClassMeetingUtilPeriodElementName = "classMeetingUtilPeriod";
	public static String sPeriodAttribute = "period";
	public static String sStudentsElementName = "students";
	public static String sStudentElementName = "student";
	public static String sAcadAreaMajorClassificationElementName = "acadAreaMajorClassification";
	public static String sAcademicAreaUniqueIdAttribute = "academicAreaUniqueId";
	public static String sAcademicClassificationUniqueIdAttribute = "academicClassificationUniqueId";
	public static String sMajorUniqueIdAttribute = "majorUniqueId";
	public static String sAcadAreaMinorClassificationElementName = "acadAreaMinorClassification";
	public static String sMinorUniqueIdAttribute = "minorUniqueId";
	public static String sEnrollmentElementName = "enrollment";
	public static String sClassUniqueIdAttribute = "classUniqueId";
	public static String sCourseOfferingUniqueIdAttribute = "courseOfferingUniqueId";
	public static String sTimestampAttribute = "timestamp";
	public static String sChangedByAttribute = "changedBy";

    
    
    
	@SuppressWarnings("unchecked")
	private TreeSet<InstructionalOffering> findOfferingsWithClasses(Session acadSession) {
		StringBuilder querySb =  new StringBuilder();
		querySb.append("select io, cco ")
		       .append(" from InstructionalOffering io, CourseOffering cco") 
		       .append(" join fetch io.courseOfferings as co")
		       .append(" join fetch cco.subjectArea as sa")
		       .append(" join fetch sa.department as sad")
		       .append(" left join fetch io.offeringCoordinators as oc")
		       .append(" left join fetch oc.instructor as oci")
		       .append(" left join fetch oci.department as ocid")
		       .append(" left join fetch oci.positionType as ocipt")
		       .append(" left join fetch oc.responsibility as tr")
		       .append(" join fetch io.instrOfferingConfigs as ioc")
		       .append(" join fetch ioc.schedulingSubparts as ss")
		       .append(" join fetch ss.itype as it")
		       .append(" join fetch ss.classes as c")
		       .append(" left join fetch co.courseType as ct")
		       .append(" left join fetch ioc.instructionalMethod as cim")
		       .append(" left join fetch ioc.classDurationType")
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
		       .append(" and cco.instructionalOffering.uniqueId = io.uniqueId")
		       .append(" and cco.isControl = true")
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
		       .append(" left join fetch s.areaClasfMajors as acma")
		       .append(" left join fetch s.areaClasfMinors as acmi")
		       .append(" left join fetch acma.academicArea as acmaaa")
		       .append(" left join fetch acma.academicClassification as acmaac")
		       .append(" left join fetch acma.major as acmama")
		       .append(" left join fetch acmi.academicArea as acmaa")
		       .append(" left join fetch acmi.academicClassification as acmiac")
		       .append(" left join fetch acmi.minor as acmimi")
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
            Element root = document.addElement(sRootElementName);
            root.addAttribute(sAcademicSessionUniqueIdAttribute, session.getUniqueId().toString());
            root.addAttribute(sAcademicInitiativeAttribute, session.getAcademicInitiative());
            root.addAttribute(sAcademicYearAttribute, session.getAcademicYear());
            root.addAttribute(sAcademicTermAttribute, session.getAcademicTerm());
            root.addAttribute(sDateFormatAttribute, sDateFormat.toPattern());
            root.addAttribute(sTimeFormatAttribute, sTimeFormat.toPattern());
            root.addAttribute(sCreatedAttribute, (sDateFormat.format(timestamp) + " " + sTimeFormat.format(timestamp)));
            root.addAttribute(sSessionBeginDateAttribute, sDateFormat.format(session.getSessionBeginDateTime()));
            root.addAttribute(sSessionEndDateAttribute, sDateFormat.format(session.getSessionEndDateTime()));
            root.addAttribute(sClassesEndDateAttribute, sDateFormat.format(session.getClassesEndDateTime()));
            if (session.getDefaultClassDurationType() != null) {
            	root.addAttribute(sDurationTypeAttribute, session.getDefaultClassDurationType().getReference());
            }
            String name = session.getAcademicInitiative()+session.getAcademicYear()+session.getAcademicTerm()+timestamp.getTime();
            String note = "This is a point in time data snapshot for session:  " + session.getLabel() + ", taken on:  " + timestamp.toString();
            root.addAttribute(sPointInTimeNameAttribute, name);
            root.addAttribute(sPointInTimeNoteAttribute, note);
                       
            document.addDocType(sRootElementName, "-//UniTime//DTD University Course Timetabling/EN", "http://www.unitime.org/interface/PointInTmeData.dtd");
            
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
                    
            departmentsElement = root.addElement(sDepartmentsElementName);
            roomTypesElement = root.addElement(sRoomTypesElementName);
            creditTypesElement = root.addElement(sCreditTypesElementName);
            creditUnitTypesElement = root.addElement(sCreditUnitTypesElementName);
            positionTypesElement = root.addElement(sPositionTypesElementName);
            teachingResponsibilitiesElement = root.addElement(sTeachingResponsibilitiesElementName);
            locationsElement = root.addElement(sLocationsElementName);
            studentsElement = root.addElement(sStudentsElementName);
            courseTypesElement = root.addElement(sCourseTypesElementName);
            classDurationTypesElement = root.addElement(sClassDurationTypesElementName);
            instructionalMethodsElement = root.addElement(sInstructionalMethodsElementName);
            timePatternsElement = root.addElement(sTimePatternsElementName);
            datePatternsElement = root.addElement(sDatePatternsElementName);
            academicAreasElement = root.addElement(sAcademicAreasElementName);
            academicClassificationsElement = root.addElement(sAcademicClassificationsElementName);
            majorsElement = root.addElement(sMajorsElementName);
            minorsElement = root.addElement(sMinorsElementName);
            offeringsElement = root.addElement(sOfferingsElementName);

            
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
        Element offeringElement = offeringsElement.addElement(sOfferingElementName);
        offeringElement.addAttribute(sUniqueIdAttribute, offering.getUniqueId().toString());
        offeringElement.addAttribute(sPermanentIdAttribute, offering.getInstrOfferingPermId().toString());
        offeringElement.addAttribute(sDemandAttribute, offering.getDemand().toString());
        offeringElement.addAttribute(sLimitAttribute, (offering.getLimit() == null?"0":offering.getLimit().toString()));
        offeringElement.addAttribute(sUniqueIdRolledForwardFromAttribute, (offering.getUniqueIdRolledForwardFrom() == null?"":offering.getUniqueIdRolledForwardFrom().toString()));
        offeringElement.addAttribute(sExternalIdAttribute, (offering.getExternalUniqueId()!=null?offering.getExternalUniqueId():offering.getUniqueId().toString()));
        
        for (CourseOffering course : offering.getCourseOfferings()) {
            exportCourse(offeringElement.addElement(sCourseElementName), course, session);
        }
        if (!offering.isNotOffered()) {
            for (InstrOfferingConfig config : offering.getInstrOfferingConfigs()) {
            	if (configHasAtLeastOneStudentEnrolled(config)) {
            		exportConfig(offeringElement.addElement(sConfigElementName), config, session);
            	}
            }
            if (offering.getOfferingCoordinators() != null) {
	            for (OfferingCoordinator oc : offering.getOfferingCoordinators()){
	            	exportOfferingCoordinator(offeringElement.addElement(sOfferingCoordinatorElementName), oc, session);
	            }
            }
        }
    }
    
    private void exportOfferingCoordinator(Element offeringCoordinatorElement, OfferingCoordinator oc, Session session) {
    	if (!departmentalInstructorElements.containsKey(oc.getInstructor().getUniqueId())){
 	       exportDepartmentalInstructor(oc.getInstructor());   		
     	}
    	offeringCoordinatorElement.addAttribute(sDepartmentalInstructorUniqueIdAttribute, oc.getInstructor().getUniqueId().toString());
     	if (oc.getResponsibility() != null) {
     		if (!teachingResponsibilityElements.containsKey(oc.getResponsibility().getUniqueId())) {
     			exportTeachingResponsibility(oc.getResponsibility());
     		}
     		offeringCoordinatorElement.addAttribute(sResponsibilityUniqueIdAttribute, oc.getResponsibility().getUniqueId().toString());
     	}
         if (oc.getPercentShare()!=null){
        	 offeringCoordinatorElement.addAttribute("share", oc.getPercentShare().toString());
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
	            subpartElement.addAttribute(sCreditTypeAttribute, credit.getCreditType().getUniqueId().toString());
	        }
	        if (credit.getCreditUnitType()!=null){
	        	if (!creditUnitTypeElements.containsKey(credit.getCreditUnitType().getUniqueId())){
	        		exportCreditUnitType(credit.getCreditUnitType());
	        	}
	        	subpartElement.addAttribute(sCreditUnitTypeAttribute, credit.getCreditUnitType().getUniqueId().toString());
	        }
	        if (credit instanceof ArrangeCreditUnitConfig) {
	        } else if (credit instanceof FixedCreditUnitConfig) {
	            FixedCreditUnitConfig fixedCredit = (FixedCreditUnitConfig)credit;
	            subpartElement.addAttribute(sCreditAttribute, fixedCredit.getFixedUnits().toString());
	        } else if (credit instanceof VariableRangeCreditUnitConfig) {
	            VariableRangeCreditUnitConfig variableCredit = (VariableRangeCreditUnitConfig)credit;
	            subpartElement.addAttribute(sCreditAttribute, variableCredit.getMinUnits().toString());
	        } else if (credit instanceof VariableFixedCreditUnitConfig) {
	            VariableFixedCreditUnitConfig variableCredit = (VariableFixedCreditUnitConfig)credit;
	            subpartElement.addAttribute(sCreditAttribute, variableCredit.getMinUnits().toString());
	        }
    	}
    }
    
    private void exportDepartment(Department department){
    	Element departmentElement = departmentsElement.addElement(sDepartmentElementName);
    	departmentElement.addAttribute(sUniqueIdAttribute, department.getUniqueId().toString());
    	departmentElement.addAttribute(sExternalIdAttribute, (department.getExternalUniqueId() == null?department.getUniqueId().toString():department.getExternalUniqueId()));
    	departmentElement.addAttribute(sNameAttribute, department.getName());
    	departmentElement.addAttribute(sAbbreviationAttribute, department.getAbbreviation());
    	departmentElement.addAttribute(sDepartmentCode, department.getDeptCode());
    	departmentElements.put(department.getUniqueId(), departmentElement);
    }


    private void exportSubjectArea(SubjectArea subjectArea){
    	if (!departmentElements.containsKey(subjectArea.getDepartment().getUniqueId())){
    		exportDepartment(subjectArea.getDepartment());
    	}
    	Element subjectElement = departmentElements.get(subjectArea.getDepartment().getUniqueId()).addElement(sSubjectAreaElementName);
    	subjectElement.addAttribute(sUniqueIdAttribute, subjectArea.getUniqueId().toString());
    	subjectElement.addAttribute(sExternalIdAttribute, (subjectArea.getExternalUniqueId() == null?subjectArea.getUniqueId().toString():subjectArea.getExternalUniqueId()));
    	subjectElement.addAttribute(sSubjectAreaAbbreviationAttribute, subjectArea.getSubjectAreaAbbreviation());
    	subjectElement.addAttribute(sTitleAttribute, subjectArea.getTitle());
    	subjectElements.put(subjectArea.getUniqueId(), subjectElement);
    	
    }
    
    private void exportCourseType(CourseType courseType){
    	Element courseTypeElement = courseTypesElement.addElement(sCourseTypeElementName);
    	courseTypeElement.addAttribute(sUniqueIdAttribute, courseType.getUniqueId().toString());
    	courseTypeElement.addAttribute(sReferenceAttribute, courseType.getReference());
    	courseTypeElement.addAttribute(sLabelAttribute, courseType.getLabel());
    	courseTypeElements.put(courseType.getUniqueId(), courseTypeElement);
    }
    
    private void exportCourse(Element courseElement, CourseOffering course, Session session) {
    	if (!subjectElements.containsKey(course.getSubjectArea().getUniqueId())){
    		exportSubjectArea(course.getSubjectArea());
    	}
        courseElement.addAttribute(sUniqueIdAttribute, course.getUniqueId().toString());
        courseElement.addAttribute(sExternalIdAttribute, (course.getExternalUniqueId()!=null?course.getExternalUniqueId():course.getUniqueId().toString()));
        courseElement.addAttribute(sSubjectAreaAbbreviationAttribute, course.getSubjectArea().getSubjectAreaAbbreviation());
        courseElement.addAttribute(sCourseNbrAttribute, course.getCourseNbr());
        courseElement.addAttribute(sControllingAttribute, course.isIsControl()?"true":"false");
        courseElement.addAttribute(sPermanentIdAttribute, (course.getPermId() == null ? course.getUniqueId().toString() : course.getPermId()));
        if (course.getProjectedDemand() != null){
            courseElement.addAttribute(sProjectedDemandAttribute,
            		course.getProjectedDemand().toString());
        }
        if (course.getTitle() != null) {
        	courseElement.addAttribute(sTitleAttribute, course.getTitle());
        }
        if (course.getNbrExpectedStudents() != null) {
        	courseElement.addAttribute(sNumberExpectedStudentsAttribute, course.getNbrExpectedStudents().toString());
        }
        if (course.getDemand() != null){
        	courseElement.addAttribute(sLastlikeDemandAttribute, course.getDemand().toString());
        }
        if (course.getCourseType() != null) {
        	if (!courseTypeElements.containsKey(course.getCourseType().getUniqueId())){
        		exportCourseType(course.getCourseType());
        	}
        	courseElement.addAttribute(sCourseTypeIdAttribute, (course.getCourseType().getUniqueId().toString()) );
        }
        if (course.getUniqueIdRolledForwardFrom() != null){
        	courseElement.addAttribute(sUniqueIdRolledForwardFromAttribute, course.getUniqueIdRolledForwardFrom().toString());
        }
    }
    
    private void exportClassDurationType(ClassDurationType classDurationType){
    	Element classDurationTypeElement = classDurationTypesElement.addElement(sClassDurationTypeElementName);
    	classDurationTypeElement.addAttribute(sUniqueIdAttribute, classDurationType.getUniqueId().toString());
    	classDurationTypeElement.addAttribute(sReferenceAttribute, classDurationType.getReference());
    	classDurationTypeElement.addAttribute(sLabelAttribute, classDurationType.getLabel());
    	classDurationTypeElement.addAttribute(sAbbreviationAttribute, classDurationType.getAbbreviation());
    	classDurationTypeElement.addAttribute(sImplementationAttribute, classDurationType.getImplementation());
    	classDurationTypeElement.addAttribute(sParameterAttribute, classDurationType.getParameter());
    	classDurationTypeElement.addAttribute(sVisibleAttribute, (classDurationType.getVisible()?"true":"false"));
    	classDurationTypeElements.put(classDurationType.getUniqueId(), classDurationTypeElement);
    }

    private void exportInstructionalMethod(InstructionalMethod instructionalMethod){
    	Element instructionalMethodElement = instructionalMethodsElement.addElement(sInstructionalMethodElementName);
    	instructionalMethodElement.addAttribute(sUniqueIdAttribute, instructionalMethod.getUniqueId().toString());
    	instructionalMethodElement.addAttribute(sReferenceAttribute, instructionalMethod.getReference());
    	instructionalMethodElement.addAttribute(sLabelAttribute, instructionalMethod.getLabel());
    	instructionalMethodElement.addAttribute(sVisibleAttribute, (instructionalMethod.getVisible()?"true":"false"));
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
        configElement.addAttribute(sUniqueIdAttribute, config.getUniqueId().toString());
        configElement.addAttribute(sNameAttribute, config.getName());
        configElement.addAttribute(sUnlimitedEnrollmentAttributeName, (config.isUnlimitedEnrollment() ? "true" : "false"));
        if (config.getClassDurationType() != null) {
        	if (!classDurationTypeElements.containsKey(config.getClassDurationType().getUniqueId())){
        		exportClassDurationType(config.getClassDurationType());
        	}
        	configElement.addAttribute(sDurationTypeAttribute, config.getClassDurationType().getReference());
        }
        if (config.getInstructionalMethod() != null) {
        	if (!instructionalMethodElements.containsKey(config.getInstructionalMethod().getUniqueId())){
        		exportInstructionalMethod(config.getInstructionalMethod());
        	}
        	configElement.addAttribute(sInstructionalMethodAttribute, config.getInstructionalMethod().getReference());
        }
        if (config.getUniqueIdRolledForwardFrom() != null) {
        	configElement.addAttribute(sUniqueIdRolledForwardFromAttribute, config.getUniqueIdRolledForwardFrom().toString());
        }
        for (SchedulingSubpart subpart : config.getSchedulingSubparts()) {
            if (subpart.getParentSubpart()==null) {
                exportSubpart(configElement.addElement(sSubpartElementName), subpart, session);
            }
        }
    }
    
    private void exportSubpart(Element subpartElement, SchedulingSubpart subpart, Session session) {
        subpartElement.addAttribute(sUniqueIdAttribute, subpart.getUniqueId().toString());
        subpartElement.addAttribute(sTypeAttribute, subpart.getItypeDesc().trim());
        subpartElement.addAttribute(sSuffixAttribute, subpart.getSchedulingSubpartSuffixCache());
        subpartElement.addAttribute(sMinPerWeekAttribute, subpart.getMinutesPerWk().toString());
        subpartElement.addAttribute(sStudentAllowOverlapAttribute, (subpart.getStudentAllowOverlap()?"true":"false"));
        if (subpart.getUniqueIdRolledForwardFrom() != null)
        	subpartElement.addAttribute(sUniqueIdRolledForwardFromAttribute, subpart.getUniqueIdRolledForwardFrom().toString());

        for (CourseCreditUnitConfig credit :subpart.getCreditConfigs()) {
            exportCredit(subpartElement, credit);
        }
        
        for (Class_ clazz : subpart.getClasses()){
        	if (!clazz.getCancelled() && clazz.getEnrollment().intValue() > 0){
        		exportClass(subpartElement.addElement(sClassElementName), clazz);
        	}
        }
        
        for (SchedulingSubpart childSubpart : subpart.getChildSubparts()) {
            exportSubpart(subpartElement.addElement(sSubpartElementName), childSubpart, session);
        }
    }
    
    private void exportDatePattern(DatePattern datePattern){
    	Element datePatternElement = datePatternsElement.addElement(sDatePatternElementName);
    	datePatternElement.addAttribute(sUniqueIdAttribute, datePattern.getUniqueId().toString());
    	datePatternElement.addAttribute(sNameAttribute, datePattern.getName());
    	datePatternElement.addAttribute(sPatternAttribute, datePattern.getPattern());
    	datePatternElement.addAttribute(sOffsetAttribute, datePattern.getOffset().toString());
    	datePatternElement.addAttribute(sTypeAttribute, datePattern.getType().toString());
    	datePatternElement.addAttribute(sVisibleAttribute, datePattern.getVisible()?"true":"false");
    	if (datePattern.getNumberOfWeeks() != null) {
    		datePatternElement.addAttribute(sNumberOfWeeksAttribute, datePattern.getNumberOfWeeks().toString());
    	}
    	datePatternElements.put(datePattern.getUniqueId(), datePatternElement);
    }

    private void exportTimePatternDays(Element timePatternDaysElement, TimePatternDays timePatternDays){
    	timePatternDaysElement.addAttribute(sDayCodeAttribute, timePatternDays.getDayCode().toString());
    }
    
    private void exportTimePatternTimes(Element timePatternTimesElement, TimePatternTime timePatternTimes){
    	timePatternTimesElement.addAttribute(sStartSlotAttribute, timePatternTimes.getStartSlot().toString());
    }

    private void exportTimePattern(TimePattern timePattern){
    	Element timePatternElement = timePatternsElement.addElement(sTimePatternElementName);
    	timePatternElement.addAttribute(sUniqueIdAttribute, timePattern.getUniqueId().toString());
    	timePatternElement.addAttribute(sNameAttribute, timePattern.getName());
    	timePatternElement.addAttribute(sMinutesPerMeetingAttribute, timePattern.getMinPerMtg().toString());
    	timePatternElement.addAttribute(sSlotsPerMeetingAttribute, timePattern.getSlotsPerMtg().toString());
    	timePatternElement.addAttribute(sNumberOfMeetingsPerWeekAttribute, timePattern.getNrMeetings().toString());
    	timePatternElement.addAttribute(sVisibleAttribute, timePattern.getVisible()?"true":"false");
    	timePatternElement.addAttribute(sTypeAttribute, timePattern.getType().toString());
    	timePatternElement.addAttribute(sBreakTimeAttribute, timePattern.getBreakTime().toString());
    	for(TimePatternDays day : timePattern.getDays()){
    		exportTimePatternDays(timePatternElement.addElement(sTimePatternDaysElementName), day);
    	}
    	
    	for(TimePatternTime time : timePattern.getTimes()){
    		exportTimePatternTimes(timePatternElement.addElement(sTimePatternTimeElementName), time);
    	}
    	
    	timePatternElements.put(timePattern.getUniqueId(), timePatternElement);
    }
    
    private void exportClass(Element classElement, Class_ clazz) {
        classElement.addAttribute(sUniqueIdAttribute, clazz.getUniqueId().toString());
        classElement.addAttribute(sExternalIdAttribute, getExternalUniqueId(clazz));
        if (clazz.getParentClass() != null){
            classElement.addAttribute(sParentClassUniqueIdAttribute, clazz.getParentClass().getUniqueId().toString());        	
        }
        classElement.addAttribute(sStudentSchedulingAttribute, clazz.isEnabledForStudentScheduling()?"true":"false");
        if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment()) {
        	classElement.addAttribute(sLimitAttribute, "inf");
        }
        else {
        	classElement.addAttribute(sLimitAttribute, String.valueOf(clazz.getClassLimit()));
        }
        if (clazz.getNbrRooms() != null){
        	classElement.addAttribute(sNumberOfRoomsAttribute, clazz.getNbrRooms().toString());        	
        }
        if (!datePatternElements.containsKey(clazz.effectiveDatePattern().getUniqueId())){
        	exportDatePattern(clazz.effectiveDatePattern());
        }
        classElement.addAttribute(sDatePatternUniqueIdAttribute, clazz.effectiveDatePattern().getUniqueId().toString());
        if (clazz.getCommittedAssignment() != null){
        	if (!timePatternElements.containsKey(clazz.getCommittedAssignment().getTimePattern().getUniqueId())){
        		exportTimePattern(clazz.getCommittedAssignment().getTimePattern());
        	}
            classElement.addAttribute(sTimePatternUniqueIdAttribute, clazz.getCommittedAssignment().getTimePattern().getUniqueId().toString());        	
        }
        classElement.addAttribute(sClassSuffixAttribute, getClassSuffix(clazz));
        classElement.addAttribute(sSectionNumberAttribute, clazz.getSectionNumber().toString());
        if (!departmentElements.containsKey(clazz.getManagingDept().getUniqueId())){
        	exportDepartment(clazz.getManagingDept());
        }
        classElement.addAttribute(sManagingDepartmentUniqueIdAttribute, clazz.getManagingDept().getUniqueId().toString());
        if (clazz.getUniqueIdRolledForwardFrom() != null)
        	classElement.addAttribute(sUniqueIdRolledForwardFromAttribute, clazz.getUniqueIdRolledForwardFrom().toString());

        for (ClassInstructor instructor : clazz.getClassInstructors()) {
            exportClassInstructor(classElement.addElement(sClassInstructorElementName), instructor);
        }
        
        classElements.put(clazz.getUniqueId(), classElement);
    }
    
    private void exportStudent(Student student){
    	Element studentElement = studentsElement.addElement(sStudentElementName);
    	studentElement.addAttribute(sUniqueIdAttribute, student.getUniqueId().toString());
    	studentElement.addAttribute(sExternalIdAttribute, (student.getExternalUniqueId() == null?student.getUniqueId().toString():student.getExternalUniqueId()));
    	studentElement.addAttribute(sFirstNameAttribute, student.getFirstName());
    	studentElement.addAttribute(sMiddleNameAttribute, student.getMiddleName());
    	studentElement.addAttribute(sLastNameAttribute, student.getLastName());
    	studentElements.put(student.getUniqueId(), studentElement);
    	for (StudentAreaClassificationMajor major: student.getAreaClasfMajors())
    		exportAcadAreaMajorClassification(studentElement.addElement(sAcadAreaMajorClassificationElementName), major);
    	for (StudentAreaClassificationMinor minor : student.getAreaClasfMinors())
    		exportAcadAreaMinorClassification(studentElement.addElement(sAcadAreaMinorClassificationElementName), minor);
    }
    
    private void exportAcadAreaMajorClassification(Element acadAreaMajorClassificationElement,
			StudentAreaClassificationMajor acm) {
    	if (!majorElements.containsKey(acm.getMajor().getUniqueId())){
    		exportMajor(acm.getMajor());
    	}
    	if (!academicAreaElements.containsKey(acm.getAcademicArea().getUniqueId())){
    		exportAcademicArea(acm.getAcademicArea());
    	}
    	if (!academicClassificationElements.containsKey(acm.getAcademicClassification().getUniqueId())){
    		exportAcademicClassification(acm.getAcademicClassification());
    	}
    	acadAreaMajorClassificationElement.addAttribute(sAcademicAreaUniqueIdAttribute, acm.getAcademicArea().getUniqueId().toString());
    	acadAreaMajorClassificationElement.addAttribute(sAcademicClassificationUniqueIdAttribute, acm.getAcademicClassification().getUniqueId().toString());
    	acadAreaMajorClassificationElement.addAttribute(sMajorUniqueIdAttribute, acm.getMajor().getUniqueId().toString());
	}

    private void exportAcadAreaMinorClassification(Element acadAreaMinorClassificationElement,
    		StudentAreaClassificationMinor acm) {
    	if (!majorElements.containsKey(acm.getMinor().getUniqueId())){
    		exportMinor(acm.getMinor());
    	}
    	if (!academicAreaElements.containsKey(acm.getAcademicArea().getUniqueId())){
    		exportAcademicArea(acm.getAcademicArea());
    	}
    	if (!academicClassificationElements.containsKey(acm.getAcademicClassification().getUniqueId())){
    		exportAcademicClassification(acm.getAcademicClassification());
    	}
    	acadAreaMinorClassificationElement.addAttribute(sAcademicAreaUniqueIdAttribute, acm.getAcademicArea().getUniqueId().toString());
    	acadAreaMinorClassificationElement.addAttribute(sAcademicClassificationUniqueIdAttribute, acm.getAcademicClassification().getUniqueId().toString());
    	acadAreaMinorClassificationElement.addAttribute(sMinorUniqueIdAttribute, acm.getMinor().getUniqueId().toString());
	}

	private void exportMajor(PosMajor major) {
    	Element majorElement = majorsElement.addElement(sMajorElementName);
    	majorElement.addAttribute(sUniqueIdAttribute, major.getUniqueId().toString());
    	majorElement.addAttribute(sCodeAttribute, major.getCode());
    	majorElement.addAttribute(sNameAttribute, major.getName());
    	majorElement.addAttribute(sExternalIdAttribute, (major.getExternalUniqueId() == null? major.getUniqueId().toString() : major.getExternalUniqueId()));
    	majorElements.put(major.getUniqueId(), majorElement);
	}

	private void exportMinor(PosMinor minor) {
    	Element minorElement = minorsElement.addElement(sMinorElementName);
    	minorElement.addAttribute(sUniqueIdAttribute, minor.getUniqueId().toString());
    	minorElement.addAttribute(sCodeAttribute, minor.getCode());
    	minorElement.addAttribute(sNameAttribute, minor.getName());
    	minorElement.addAttribute(sExternalIdAttribute, (minor.getExternalUniqueId() == null? minor.getUniqueId().toString() : minor.getExternalUniqueId()));
    	minorElements.put(minor.getUniqueId(), minorElement);
	}

	private void exportAcademicClassification(AcademicClassification academicClassification) {
    	Element academicClassificationElement = academicClassificationsElement.addElement(sAcademicClassificationElementName);
    	academicClassificationElement.addAttribute(sUniqueIdAttribute, academicClassification.getUniqueId().toString());
    	academicClassificationElement.addAttribute(sCodeAttribute, academicClassification.getCode());
    	academicClassificationElement.addAttribute(sNameAttribute, academicClassification.getName());
    	academicClassificationElement.addAttribute(sExternalIdAttribute, (academicClassification.getExternalUniqueId() == null? academicClassification.getUniqueId().toString() : academicClassification.getExternalUniqueId()));
    	academicClassificationElements.put(academicClassification.getUniqueId(), academicClassificationElement);
	}

	private void exportAcademicArea(AcademicArea academicArea) {
    	Element academicAreaElement = academicAreasElement.addElement(sAcademicAreaElementName);
    	academicAreaElement.addAttribute(sUniqueIdAttribute, academicArea.getUniqueId().toString());
    	academicAreaElement.addAttribute(sAbbreviationAttribute, academicArea.getAcademicAreaAbbreviation());
    	academicAreaElement.addAttribute(sTitleAttribute, academicArea.getTitle());
    	academicAreaElement.addAttribute(sExternalIdAttribute, (academicArea.getExternalUniqueId() == null? academicArea.getUniqueId().toString() : academicArea.getExternalUniqueId()));
    	academicAreaElements.put(academicArea.getUniqueId(), academicAreaElement);
	}
	
	
	private void exportStudentClassEnrollment(StudentClassEnrollment studentClassEnrollment){
    	if (!studentElements.containsKey(studentClassEnrollment.getStudent().getUniqueId())){
    		exportStudent(studentClassEnrollment.getStudent());
    	}
    	Element studentClassEnrollmentElement = studentElements.get(studentClassEnrollment.getStudent().getUniqueId()).addElement(sEnrollmentElementName);
    	studentClassEnrollmentElement.addAttribute(sClassUniqueIdAttribute, studentClassEnrollment.getClazz().getUniqueId().toString());
    	studentClassEnrollmentElement.addAttribute(sCourseOfferingUniqueIdAttribute, studentClassEnrollment.getCourseOffering().getUniqueId().toString());
    	studentClassEnrollmentElement.addAttribute(sTimestampAttribute, (sDateFormat.format(studentClassEnrollment.getTimestamp()) + " " + sTimeFormat.format(studentClassEnrollment.getTimestamp())));
    	studentClassEnrollmentElement.addAttribute(sChangedByAttribute, studentClassEnrollment.getChangedBy());
    }

    private void exportClassEvent(ClassEvent classEvent, Meeting meeting, Location location) {
    	Element classEventElement = classEventElements.get(classEvent.getUniqueId());
    	if (classEventElement == null){
	    	classEventElement = classElements.get(classEvent.getClazz().getUniqueId()).addElement(sClassEventElementName);
			classEventElement.addAttribute(sNameAttribute, classEvent.getEventName());
			classEventElements.put(classEvent.getUniqueId(), classEventElement);
    	}
		exportClassMeeting(classEventElement.addElement("classMeeting"), meeting, classEvent.getClazz(), location);
	}
    
    private int calcTotalMinPerMeeting(Meeting meeting) {
    	return(meeting.getStopPeriod().intValue() - meeting.getStartPeriod().intValue()) * Constants.SLOT_LENGTH_MIN - ( meeting.getStartOffset() == null ? 0 : meeting.getStartOffset()) + ( meeting.getStopOffset() == null ? 0 : meeting.getStopOffset().intValue());
    }
    
	private void exportClassMeetingUtilPeriods(Element classMeetingElement, Meeting meeting, Class_ clazz){

		int firstPeriod = ((meeting.getStartPeriod().intValue() * Constants.SLOT_LENGTH_MIN) 
							+ (meeting.getStartOffset() != null ? meeting.getStartOffset().intValue() : 0)) / Constants.SLOT_LENGTH_MIN;
		int lastPeriod = (new Double(Math.ceil(((meeting.getStopPeriod().doubleValue() * (double) Constants.SLOT_LENGTH_MIN) 
				+ (meeting.getStopOffset() != null ? meeting.getStopOffset().doubleValue() : (double) 0)) / (double) Constants.SLOT_LENGTH_MIN))).intValue();
		
		int totalMinPerMeeting = calcTotalMinPerMeeting(meeting); 
		int timePatMinPerMtg;
		if (clazz.getCommittedAssignment() == null) {
			timePatMinPerMtg = totalMinPerMeeting;
		} else {
			timePatMinPerMtg = clazz.getCommittedAssignment().getMinutesPerMeeting();
		}
		int totalTimePeriodsNeeded = (new Double(Math.ceil((double)timePatMinPerMtg / (double) Constants.SLOT_LENGTH_MIN))).intValue();
		int meetPeriodsCount = 0;
		int extraPeriodsCount = 0;
		if (totalMinPerMeeting <= timePatMinPerMtg) {
			for(int i = firstPeriod; i < lastPeriod; i++) {
				if (meetPeriodsCount < totalTimePeriodsNeeded) {
					Element classMeetingUtilPeriod = classMeetingElement.addElement(sClassMeetingUtilPeriodElementName);
					classMeetingUtilPeriod.addAttribute(sPeriodAttribute, Integer.toString(i));
					meetPeriodsCount++;
				} else {
					extraPeriodsCount++;
				}
			}
			
		} else {
			int totalIncorporatedBreakTime = totalMinPerMeeting - timePatMinPerMtg;
			int numIncorporatedBreaks = (new Double(Math.ceil((double)timePatMinPerMtg / ApplicationProperty.StandardClassMeetingLengthInMinutes.doubleValue()))).intValue() - 1;
			int minPerHourOfIncorporatedBreaks = (numIncorporatedBreaks == 0 ? 0 : totalIncorporatedBreakTime / numIncorporatedBreaks);
			int numBreakPeriods = minPerHourOfIncorporatedBreaks/Constants.SLOT_LENGTH_MIN;
			int numPeriodsPerStandardHour = (new Double(Math.ceil(ApplicationProperty.StandardClassMeetingLengthInMinutes.doubleValue()/Constants.SLOT_LENGTH_MIN))).intValue();
			boolean markAsMeeting = true;
			int meetCount = 0;
			int skipCount = 0;
			if ((lastPeriod - firstPeriod) < totalTimePeriodsNeeded) {
				Debug.info("Warning - Not enough " + Constants.SLOT_LENGTH_MIN + " minute periods will be stored for meeting: " + meeting.getUniqueId().toString());
			}
			for(int i = firstPeriod; i < lastPeriod; i++) {
				if (meetPeriodsCount < totalTimePeriodsNeeded) {
					if (markAsMeeting){
						Element classMeetingUtilPeriod = classMeetingElement.addElement(sClassMeetingUtilPeriodElementName);
						classMeetingUtilPeriod.addAttribute(sPeriodAttribute, Integer.toString(i));
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
			Debug.info("Info - More than enough "+ Constants.SLOT_LENGTH_MIN + " minute periods for meeting:  " + meeting.getUniqueId().toString() + " the last " + extraPeriodsCount + " were not marked as meeting.");					
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

		meetingElement.addAttribute(sMeetingDateAttribute, sDateFormat.format(meeting.getMeetingDate()));
		meetingElement.addAttribute(sStartPeriodAttribute, meeting.getStartPeriod().toString());
		meetingElement.addAttribute(sStopPeriodAttribute, meeting.getStopPeriod().toString());
		meetingElement.addAttribute(sStartOffsetAttribute, meeting.getStartOffset().toString());
		meetingElement.addAttribute(sStopOffsetAttribute, meeting.getStopOffset().toString());
		meetingElement.addAttribute(sTimePatternMinutesPerMeetingAttribute, Integer.toString(timePatternMinPerMeeting));
		meetingElement.addAttribute(sCalculatedMinutesPerMeetingAttribute, Integer.toString(calcMinutesPerMeeting));
		if (!locationElements.containsKey(location.getUniqueId())){
			exportLocation(location);
		}
		meetingElement.addAttribute(sLocationUniqueIdAttribute, location.getUniqueId().toString());
		
		exportClassMeetingUtilPeriods(meetingElement, meeting, clazz);
		
	}

	private void exportBuilding(Building building){
		Element buildingElement = locationsElement.addElement(sBuildingElementName);
		buildingElement.addAttribute(sUniqueIdAttribute, building.getUniqueId().toString());   	
		buildingElement.addAttribute(sAbbreviationAttribute, building.getAbbreviation());   	
		buildingElement.addAttribute(sNameAttribute, building.getName());   
		if (building.getCoordinateX() != null) {
			buildingElement.addAttribute(sCoordinateXAttribute, building.getCoordinateX().toString());   	
		}
		if (building.getCoordinateY() != null) {
			buildingElement.addAttribute(sCoordinateYAttribute, building.getCoordinateY().toString());   	
		}
		buildingElement.addAttribute(sExternalIdAttribute, (building.getExternalUniqueId() == null ? building.getUniqueId().toString() : building.getExternalUniqueId()));   	
		buildingElements.put(building.getUniqueId(), buildingElement);
	}
	
    private void exportRoomType(RoomType roomType){
    	Element roomTypeElement = roomTypesElement.addElement(sRoomTypeElementName);
    	roomTypeElement.addAttribute(sUniqueIdAttribute, roomType.getUniqueId().toString());
    	roomTypeElement.addAttribute(sReferenceAttribute, roomType.getReference());
    	roomTypeElement.addAttribute(sLabelAttribute, roomType.getLabel());
    	roomTypeElement.addAttribute(sOrderAttribute, roomType.getOrd().toString());
    	roomTypeElement.addAttribute(sIsRoomAttribute, (roomType.isRoom()?"true":"false"));

    	roomTypeElements.put(roomType.getUniqueId(), roomTypeElement);
    }
    
    private void exportCreditType(CourseCreditType creditType){
    	Element creditTypeElement = creditTypesElement.addElement(sCreditTypeElementName);
    	creditTypeElement.addAttribute(sUniqueIdAttribute, creditType.getUniqueId().toString());
    	creditTypeElement.addAttribute(sReferenceAttribute, creditType.getReference());
    	creditTypeElement.addAttribute(sLabelAttribute, creditType.getLabel());
    	creditTypeElement.addAttribute(sAbbreviationAttribute, creditType.getAbbreviation());
    	creditTypeElement.addAttribute(sLegacyCourseMasterCodeAttribute, creditType.getLegacyCourseMasterCode());
    	creditTypeElements.put(creditType.getUniqueId(), creditTypeElement);
    }

    private void exportCreditUnitType(CourseCreditUnitType creditUnitType){
    	Element creditUnitTypeElement = creditUnitTypesElement.addElement(sCreditUnitTypeElementName);
    	creditUnitTypeElement.addAttribute(sUniqueIdAttribute, creditUnitType.getUniqueId().toString());
    	creditUnitTypeElement.addAttribute(sReferenceAttribute, creditUnitType.getReference());
    	creditUnitTypeElement.addAttribute(sLabelAttribute, creditUnitType.getLabel());
    	creditUnitTypeElement.addAttribute(sAbbreviationAttribute, creditUnitType.getAbbreviation());
    	creditUnitTypeElements.put(creditUnitType.getUniqueId(), creditUnitTypeElement);
    }

    private void exportPositionType(PositionType positionType){
    	Element positionTypeElement = positionTypesElement.addElement(sPositionTypeElementName);
    	positionTypeElement.addAttribute(sUniqueIdAttribute, positionType.getUniqueId().toString());
    	positionTypeElement.addAttribute(sReferenceAttribute, positionType.getReference());
    	positionTypeElement.addAttribute(sLabelAttribute, positionType.getLabel());
    	positionTypeElement.addAttribute(sOrderAttribute, positionType.getSortOrder().toString());

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
			roomElement = buildingElements.get(room.getBuilding().getUniqueId()).addElement(sRoomElementName);
			roomElement.addAttribute(sRoomNumberAttribute, room.getRoomNumber());   	
		} else if (location instanceof NonUniversityLocation) {
			NonUniversityLocation nonUniversityLocation = (NonUniversityLocation) location;
			roomElement = locationsElement.addElement(sNonUniversityLocationElementName);
			roomElement.addAttribute(sNameAttribute, nonUniversityLocation.getName());   	
		}
		roomElement.addAttribute(sUniqueIdAttribute, location.getUniqueId().toString());   	
		roomElement.addAttribute(sPermanentIdAttribute, location.getPermanentId().toString());   	
		roomElement.addAttribute(sRoomTypeIdAttribute, location.getRoomType().getUniqueId().toString());   	
		roomElement.addAttribute(sCapacityAttribute, location.getCapacity().toString());  
		if (location.getControllingDepartment() != null){
			if (!departmentElements.containsKey(location.getControllingDepartment().getUniqueId())){
				exportDepartment(location.getControllingDepartment());
			}
			roomElement.addAttribute(sControllingDepartmentUniqueIdAttribute, location.getControllingDepartment().getUniqueId().toString());
		}
		if (location.getCoordinateX() != null) {
			roomElement.addAttribute(sCoordinateXAttribute, location.getCoordinateX().toString());   	
		}
		if (location.getCoordinateY() != null) {
			roomElement.addAttribute(sCoordinateYAttribute, location.getCoordinateY().toString());   	
		}
		roomElement.addAttribute(sExternalIdAttribute, (location.getExternalUniqueId() == null ? location.getUniqueId().toString() : location.getExternalUniqueId()));   	
		locationElements.put(location.getUniqueId(), roomElement);

		
	}


	private void exportClassInstructor(Element instructorElement, ClassInstructor instructor) {
    	if (!departmentalInstructorElements.containsKey(instructor.getInstructor().getUniqueId())){
	       exportDepartmentalInstructor(instructor.getInstructor());   		
    	}
    	instructorElement.addAttribute(sDepartmentalInstructorUniqueIdAttribute, instructor.getInstructor().getUniqueId().toString());
    	if (instructor.getResponsibility() != null) {
    		if (!teachingResponsibilityElements.containsKey(instructor.getResponsibility().getUniqueId())) {
    			exportTeachingResponsibility(instructor.getResponsibility());
    		}
    		instructorElement.addAttribute(sResponsibilityUniqueIdAttribute, instructor.getResponsibility().getUniqueId().toString());
    	}
        if (instructor.getPercentShare()!=null)
        	instructorElement.addAttribute("share", instructor.getPercentShare().toString());
        instructorElement.addAttribute("lead", instructor.isLead()?"true":"false");
    }
    
    private void exportTeachingResponsibility(TeachingResponsibility responsibility) {
    	Element responsibilityElement = teachingResponsibilitiesElement.addElement(sTeachingResponsibilityElementName);
    	responsibilityElement.addAttribute(sUniqueIdAttribute, responsibility.getUniqueId().toString());
    	responsibilityElement.addAttribute(sReferenceAttribute, responsibility.getReference());
    	responsibilityElement.addAttribute(sLabelAttribute, responsibility.getLabel());
    	responsibilityElement.addAttribute(sAbbreviationAttribute, responsibility.getAbbreviation());
    	responsibilityElement.addAttribute(sInstructorAttribute, (responsibility.isInstructor()?"true":"false"));
    	responsibilityElement.addAttribute(sCoordinatorAttribute, (responsibility.isCoordinator()?"true":"false"));
    	responsibilityElement.addAttribute(sOptionsAttribute, responsibility.getOptions().toString());

    	teachingResponsibilityElements.put(responsibility.getUniqueId(), responsibilityElement);
	}

	private void exportDepartmentalInstructor(DepartmentalInstructor instructor) {
    	if (!departmentElements.containsKey(instructor.getDepartment().getUniqueId())){
    		exportDepartment(instructor.getDepartment());
    	}
    	Element departmentalInstructorElement = departmentElements.get(instructor.getDepartment().getUniqueId()).addElement(sDeptInstructorElementName);
       	departmentalInstructorElement.addAttribute(sUniqueIdAttribute, instructor.getUniqueId().toString());   	
        if (instructor.getExternalUniqueId()!=null)
        	departmentalInstructorElement.addAttribute(sExternalIdAttribute, instructor.getExternalUniqueId());
        if (instructor.getFirstName()!=null)
        	departmentalInstructorElement.addAttribute(sFirstNameAttribute, instructor.getFirstName());
        if (instructor.getMiddleName()!=null)
        	departmentalInstructorElement.addAttribute(sMiddleNameAttribute, instructor.getMiddleName());
    	departmentalInstructorElement.addAttribute(sLastNameAttribute, (instructor.getLastName() == null?"NULL":instructor.getLastName()));
        if (instructor.getCareerAcct() != null)
        	departmentalInstructorElement.addAttribute(sCareerAcctAttribute, instructor.getCareerAcct());
        if (instructor.getEmail() != null)
        	departmentalInstructorElement.addAttribute(sEmailAttribute, instructor.getEmail());
        if (instructor.getPositionType() != null) {
        	if (!positionTypeElements.containsKey(instructor.getPositionType().getUniqueId())){
        		exportPositionType(instructor.getPositionType());
        	}
        	departmentalInstructorElement.addAttribute(sPositionTypeUniqueIdAttribute, instructor.getPositionType().getUniqueId().toString());
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
}
