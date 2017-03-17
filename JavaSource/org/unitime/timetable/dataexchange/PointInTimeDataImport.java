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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Element;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassEvent;
import org.unitime.timetable.model.PitClassInstructor;
import org.unitime.timetable.model.PitClassMeeting;
import org.unitime.timetable.model.PitClassMeetingUtilPeriod;
import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.PitDepartmentalInstructor;
import org.unitime.timetable.model.PitInstrOfferingConfig;
import org.unitime.timetable.model.PitInstructionalOffering;
import org.unitime.timetable.model.PitOfferingCoordinator;
import org.unitime.timetable.model.PitSchedulingSubpart;
import org.unitime.timetable.model.PitStudent;
import org.unitime.timetable.model.PitStudentAcadAreaMajorClassification;
import org.unitime.timetable.model.PitStudentAcadAreaMinorClassification;
import org.unitime.timetable.model.PitStudentClassEnrollment;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ProgressTracker;


/**
 * @author Stephanie Schluttenhofer
 */
public class PointInTimeDataImport extends EventRelatedImports {
	
	private PointInTimeData pointInTimeData = null;
	
	private HashMap<String, SubjectArea> subjectAreas = new HashMap<String, SubjectArea>();
	private HashMap<String, ItypeDesc> itypes = new HashMap<String, ItypeDesc>();
	private HashMap<String, ClassDurationType> classDurationTypes = new HashMap<String, ClassDurationType>();
	private HashMap<String, InstructionalMethod> instructionalMethods = new HashMap<String, InstructionalMethod>();
	private HashMap<String, Department> departmentsByCode = new HashMap<String, Department>();
	private HashMap<String, CourseType> courseTypesByRef = new HashMap<String, CourseType>(); 
	private HashMap<String, TeachingResponsibility> teachingResponsibilitiesByRef = new HashMap<String, TeachingResponsibility>();
	private HashMap<String, DatePattern> datePatternsByName = new HashMap<String, DatePattern>();
	private HashMap<String, TimePattern> timePatternsByName = new HashMap<String, TimePattern>();
	private HashMap<String, AcademicClassification> academicClassificationsByCode = new HashMap<String, AcademicClassification>();
	private HashMap<String, AcademicArea> academicAreasByAbbv = new HashMap<String, AcademicArea>();
	private HashMap<String, PosMajor> majorsByCode = new HashMap<String, PosMajor>();
	private HashMap<String, PosMinor> minorsByCode = new HashMap<String, PosMinor>();
	private HashMap<String, RoomType> roomTypesByRef = new HashMap<String, RoomType>();
	private HashMap<String, CourseCreditType> creditTypesByRef = new HashMap<String, CourseCreditType>();
	private HashMap<String, CourseCreditUnitType> creditUnitTypesByRef = new HashMap<String, CourseCreditUnitType>();
	private HashMap<String, PositionType> positionTypesByRef = new HashMap<String, PositionType>();
	private HashMap<String, Building> buildingsByAbbv = new HashMap<String, Building>();
	private HashMap<String, Location> locationsByName = new HashMap<String, Location>();
	private HashMap<String, DepartmentalInstructor> departmentalInstructorsByName = new HashMap<String, DepartmentalInstructor>();	
	private HashMap<Long, TimePattern> timePatterns = new HashMap<Long, TimePattern>();
	private HashMap<Long, DatePattern> datePatterns = new HashMap<Long, DatePattern>();
	private HashMap<Long, Location> locations = new HashMap<Long, Location>();
	private HashMap<Long, Building> buildings = new HashMap<Long, Building>();
	private HashMap<Long, InstructionalOffering> instructionalOfferings = new HashMap<Long, InstructionalOffering>();
	private HashMap<Long, CourseOffering> courseOfferings = new HashMap<Long, CourseOffering>();
	private HashMap<Long, PitCourseOffering> pitCourseOfferings = new HashMap<Long, PitCourseOffering>();
	private HashMap<Long, InstrOfferingConfig> instrOfferingConfigs = new HashMap<Long, InstrOfferingConfig>();
	private HashMap<Long, SchedulingSubpart> schedulingSubparts = new HashMap<Long, SchedulingSubpart>();
	private HashMap<Long, Class_> classes = new HashMap<Long, Class_>();
	private HashMap<Long, PitClass> pitClasses = new HashMap<Long, PitClass>();
	private HashMap<Long, RoomType> roomTypes = new HashMap<Long, RoomType>();
	private HashMap<Long, CourseCreditType> creditTypes = new HashMap<Long, CourseCreditType>();
	private HashMap<Long, CourseCreditUnitType> creditUnitTypes = new HashMap<Long, CourseCreditUnitType>();
	private HashMap<Long, PositionType> positionTypes = new HashMap<Long, PositionType>();
	private HashMap<Long, TeachingResponsibility> teachingResponsibilities = new HashMap<Long, TeachingResponsibility>();
	private HashMap<Long, Department> departments = new HashMap<Long, Department>();
	private HashMap<Long, PitDepartmentalInstructor> pitDepartmentInstructors = new HashMap<Long, PitDepartmentalInstructor>();
	private HashMap<Long, CourseType> courseTypes = new HashMap<Long, CourseType>();
	private HashMap<Long, AcademicArea> academicAreas = new HashMap<Long, AcademicArea>();
	private HashMap<Long, AcademicClassification> academicClassifications = new HashMap<Long, AcademicClassification>();
	private HashMap<Long, PosMajor> majors = new HashMap<Long, PosMajor>();
	private HashMap<Long, PosMinor> minors = new HashMap<Long, PosMinor>();
	private HashMap<Long, Student> students = new HashMap<Long, Student>();
	
	protected boolean courseNumbersMustBeUnique;

	public PointInTimeDataImport() {
//		super();
		
//		courseNumbersMustBeUnique = ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue();
	}

	public void loadXml(Element rootElement) throws Exception {
		initializeTrimLeadingZeros();
		try {
	        if (!rootElement.getName().equalsIgnoreCase(PointInTimeDataExport.sRootElementName)) {
	        	throw new Exception("Given XML file is not a Course Offerings load file.");
	        }
	        beginTransaction();
	        
	        initializeLoad(rootElement);
			preLoadAction();
	        loadOfferings(rootElement);
	        loadStudents(rootElement);
	        completeLoad();

		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		} finally {
			postLoadAction();
		}
		
		
		updateChangeList(true);
		reportMissingLocations();
		mailLoadResults();
	}
	

	private void completeLoad() {
		pointInTimeData.setSavedSuccessfully(true);
		getHibSession().update(pointInTimeData);
        flush(true);
        commitTransaction();
	}

	@SuppressWarnings("unchecked")
	private void loadStudents(Element rootElement) throws Exception {
        loadExistingStudents(session.getUniqueId());

        Element studentsElement = rootElement.element(PointInTimeDataExport.sStudentsElementName);
        int numStudents = studentsElement.elements().size();
        info("Loading data for " + numStudents + " students.");
        ProgressTracker progressTracker = new ProgressTracker("Students", numStudents, 5, this.getClass());
        String progress = null;
        int count = 0;
        for(Element studentElement : (List<Element>) studentsElement.elements()){
        	elementStudent(studentElement);
        	count++;
//        	if ((count % 100) == 0){
           	if ((count % 1) == 0){
        		flush(true);
        	}
        	progress = progressTracker.getProgressStringIfNeeded();
        	if (progress != null) {
        		info(progress);
        	}
        }
        flush(true);
        info("Loading of student data complete.");
//    	progress = progressTracker.getElapsedTimeAnalysisString();
//    	if (progress != null) {
//    		info(progress);
//    	}
        
	}

	@SuppressWarnings("unchecked")
	private void elementStudent(Element studentElement) throws Exception {
		String uidString = getRequiredStringAttribute(studentElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sStudentElementName);
		Long uid = new Long(uidString);
		String externalId = getRequiredStringAttribute(studentElement, PointInTimeDataExport.sExternalIdAttribute, PointInTimeDataExport.sStudentElementName);
		PitStudent s = new PitStudent();
		if (students.get(uid)!= null && students.get(uid).getExternalUniqueId().equals(externalId)) {
			s.setStudent(students.get(uid));
		}
		s.setExternalUniqueId(externalId);
		s.setFirstName(getOptionalStringAttribute(studentElement, PointInTimeDataExport.sFirstNameAttribute));
		s.setMiddleName(getOptionalStringAttribute(studentElement, PointInTimeDataExport.sMiddleNameAttribute));
		s.setLastName(getRequiredStringAttribute(studentElement, PointInTimeDataExport.sLastNameAttribute, PointInTimeDataExport.sStudentElementName));
		s.setPointInTimeData(pointInTimeData);
		s.setUniqueId((Long)getHibSession().save(s));
		for(Element element : (List<Element>)studentElement.elements()){
			if (element.getName().equals(PointInTimeDataExport.sEnrollmentElementName)) {
				elementEnrollment(element, s);
			} else if (element.getName().equals(PointInTimeDataExport.sAcadAreaMajorClassificationElementName)){
				elementAcadAreaMajorClassification(element, s);
			} else if (element.getName().equals(PointInTimeDataExport.sAcadAreaMinorClassificationElementName)){
				elementAcadAreaMinorClassification(element, s);
			}
		}
	}

	private void elementAcadAreaMajorClassification(Element element, PitStudent s) throws Exception {
		PitStudentAcadAreaMajorClassification aamc = new PitStudentAcadAreaMajorClassification();
		aamc.setAcademicArea(academicAreas.get(new Long(getRequiredStringAttribute(element, PointInTimeDataExport.sAcademicAreaUniqueIdAttribute, PointInTimeDataExport.sAcadAreaMajorClassificationElementName))));
		aamc.setAcademicClassification(academicClassifications.get(new Long(getRequiredStringAttribute(element, PointInTimeDataExport.sAcademicClassificationUniqueIdAttribute, PointInTimeDataExport.sAcadAreaMajorClassificationElementName))));
		aamc.setMajor(majors.get(new Long(getRequiredStringAttribute(element, PointInTimeDataExport.sMajorUniqueIdAttribute, PointInTimeDataExport.sAcadAreaMajorClassificationElementName))));
		aamc.setPitStudent(s);
		s.addTopitAcadAreaMajorClassifications(aamc);
		aamc.setUniqueId((Long) getHibSession().save(aamc));
	}

	private void elementAcadAreaMinorClassification(Element element, PitStudent s) throws Exception {
		PitStudentAcadAreaMinorClassification aamc = new PitStudentAcadAreaMinorClassification();
		aamc.setAcademicArea(academicAreas.get(new Long(getRequiredStringAttribute(element, PointInTimeDataExport.sAcademicAreaUniqueIdAttribute, PointInTimeDataExport.sAcadAreaMinorClassificationElementName))));
		aamc.setAcademicClassification(academicClassifications.get(new Long(getRequiredStringAttribute(element, PointInTimeDataExport.sAcademicClassificationUniqueIdAttribute, PointInTimeDataExport.sAcadAreaMinorClassificationElementName))));
		aamc.setMinor(minors.get(new Long(getRequiredStringAttribute(element, PointInTimeDataExport.sMinorUniqueIdAttribute, PointInTimeDataExport.sAcadAreaMinorClassificationElementName))));
		aamc.setPitStudent(s);
		s.addTopitAcadAreaMinorClassifications(aamc);
		aamc.setUniqueId((Long) getHibSession().save(aamc));
	}

	private void elementEnrollment(Element element, PitStudent s) throws NumberFormatException, Exception {
		PitStudentClassEnrollment psce = new PitStudentClassEnrollment();
		psce.setPitClass(pitClasses.get(new Long(getRequiredStringAttribute(element, PointInTimeDataExport.sClassUniqueIdAttribute, PointInTimeDataExport.sEnrollmentElementName))));

		PitCourseOffering pco = pitCourseOfferings.get(new Long(getRequiredStringAttribute(element, PointInTimeDataExport.sCourseOfferingUniqueIdAttribute, PointInTimeDataExport.sEnrollmentElementName)));
		psce.setPitCourseOffering(pco);
		psce.setTimestamp(CalendarUtils.getDate(getRequiredStringAttribute(element, PointInTimeDataExport.sTimestampAttribute, PointInTimeDataExport.sEnrollmentElementName), (dateFormat + " " + timeFormat)));
		psce.setChangedBy(getOptionalStringAttribute(element, PointInTimeDataExport.sChangedByAttribute));
		psce.setPitStudent(s);
		s.addTopitClassEnrollments(psce);
		psce.setUniqueId((Long) getHibSession().save(psce));
	}

	// If a setup action needs to take place before the data is loaded override this method
	private void preLoadAction() {
		// ???
	}

	// If a post load action needs to take place before the data is loaded override this method
	private void postLoadAction(){
		// ????
	}

	@SuppressWarnings("unchecked")
	private void loadOfferings(Element rootElement) throws Exception{  
		Element offeringsElement = rootElement.element(PointInTimeDataExport.sOfferingsElementName);
		ProgressTracker progressTracker = new ProgressTracker("Instructional Offerings", offeringsElement.elements().size(), 2, this.getClass());
        info("Loading data for " + offeringsElement.elements().size() + " offerings.");
        String progress = null;
        int successCount = 0;
        int failCount = 0;
		for ( Element offeringElement : (List<Element>) offeringsElement.elements()) {
    		try {
            elementOffering(offeringElement);	             
            flush(true);
            successCount++;
    		} catch (Exception e) {
    			addNote("Not Loading 'offering' Error:  " + e.getMessage());
    			e.printStackTrace();
    			addNote("\t " + offeringElement.asXML());
    			failCount++;
    			throw(e);
    		}
    		progress = progressTracker.getProgressStringIfNeeded();
    		if (progress != null) {
    			info(progress);
    		}
    	}
        info("Loading of offering data complete.  " + successCount + " successfully loaded.  " + failCount + " failed to load.");
//		progress = progressTracker.getElapsedTimeAnalysisString();
//		if (progress != null) {
//			info(progress);
//		}

 	}
	
	@SuppressWarnings("unchecked")
	private void elementOffering(Element instructionalOfferingElement) throws Exception {

        if(!instructionalOfferingElement.getName().equalsIgnoreCase(PointInTimeDataExport.sOfferingElementName)){
        	throw new Exception("Expecting to find an '" + PointInTimeDataExport.sOfferingElementName + "' at this level, instead found '" + instructionalOfferingElement.getName() + "'.");
        }
        
        Long uid = new Long(getRequiredStringAttribute(instructionalOfferingElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sOfferingElementName));
        PitInstructionalOffering pio = new PitInstructionalOffering();
        if (instructionalOfferings.get(uid) != null) {
        	pio.setInstructionalOffering(instructionalOfferings.get(uid));
        }
        String externalId = getOptionalStringAttribute(instructionalOfferingElement, PointInTimeDataExport.sExternalIdAttribute);
        if (externalId != null) {
        	pio.setExternalUniqueId(externalId);
        }
        pio.setInstrOfferingPermId(getRequiredIntegerAttribute(instructionalOfferingElement, PointInTimeDataExport.sPermanentIdAttribute, PointInTimeDataExport.sOfferingElementName));
        String limitStr = getRequiredStringAttribute(instructionalOfferingElement, PointInTimeDataExport.sLimitAttribute, PointInTimeDataExport.sOfferingElementName);
        if (!"inf".equalsIgnoreCase(limitStr)){
            pio.setLimit(new Integer(limitStr));        	
        }
        pio.setDemand(getRequiredIntegerAttribute(instructionalOfferingElement, PointInTimeDataExport.sDemandAttribute, PointInTimeDataExport.sOfferingElementName));
        String uidRollFwdFrmStr = getOptionalStringAttribute(instructionalOfferingElement, PointInTimeDataExport.sUniqueIdRolledForwardFromAttribute);
        if (uidRollFwdFrmStr != null){
        	pio.setUniqueIdRolledForwardFrom(new Long(uidRollFwdFrmStr));
        }
        pio.setPointInTimeData(pointInTimeData);
        pio.setUniqueId((Long) getHibSession().save(pio));
        for(Element element: (List<Element>)instructionalOfferingElement.elements()){
        	if (PointInTimeDataExport.sCourseElementName.equals(element.getName())){
        		elementCourse(element, pio);
        	} else if (PointInTimeDataExport.sConfigElementName.equals(element.getName())){
        		elementInstrOffrConfig(element, pio);
        	} else if (PointInTimeDataExport.sOfferingCoordinatorElementName.equals(element.getName())){
        		elementOfferingCoordinator(element, pio);
        	}
        }
	}
	
	
	private void initializeLoad(Element rootElement) throws Exception{
        initializeDateTimeFormats(rootElement);
        initializeSessionIndependentData(rootElement);
        initializeSessionData(rootElement);
        loadSetupData();
        logXmlFileCreateInformation(rootElement);		
	}
	
	private void initializeSessionIndependentData(Element rootElement) throws Exception {
		initializeRoomTypeData(rootElement);
		initializePositionTypeData(rootElement);
		initializeTeachingResponsibilityData(rootElement);
		initializeCourseTypeData(rootElement);
		initializeClassDurationTypeData(rootElement);
		initializeCreditTypeData(rootElement);
		initializeCreditUnitTypeData(rootElement);
		initializeInstructionalMethodData(rootElement);
	}
	

	private void elementRoomType(Element roomTypeElement) throws Exception {
		String uidString = getRequiredStringAttribute(roomTypeElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sRoomTypeElementName);
		Long uid = new Long(uidString);
		String reference = getRequiredStringAttribute(roomTypeElement, PointInTimeDataExport.sReferenceAttribute, PointInTimeDataExport.sRoomTypeElementName);
		RoomType rt = roomTypesByRef.get(reference);
		if(rt == null){
			String label = getRequiredStringAttribute(roomTypeElement, PointInTimeDataExport.sLabelAttribute, PointInTimeDataExport.sRoomTypeElementName);
			Boolean isRoom = getRequiredBooleanAttribute(roomTypeElement, PointInTimeDataExport.sIsRoomAttribute, PointInTimeDataExport.sRoomTypeElementName);
			Integer order = getRequiredIntegerAttribute(roomTypeElement, PointInTimeDataExport.sOrderAttribute, PointInTimeDataExport.sRoomTypeElementName);
			rt = new RoomType();
			rt.setReference(reference);
			rt.setLabel(label);
			rt.setRoom(isRoom);
			rt.setOrd(order);
			rt.setUniqueId((Long)getHibSession().save(rt));
		}
		roomTypes.put(uid, rt);
	}

	private void elementPositionType(Element positionTypeElement) throws Exception {
		String uidString = getRequiredStringAttribute(positionTypeElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sRoomTypeElementName);
		Long uid = new Long(uidString);
		String reference = getRequiredStringAttribute(positionTypeElement, PointInTimeDataExport.sReferenceAttribute, PointInTimeDataExport.sRoomTypeElementName);
		PositionType pt = positionTypesByRef.get(reference);
		if(pt == null){
			String label = getRequiredStringAttribute(positionTypeElement, PointInTimeDataExport.sLabelAttribute, PointInTimeDataExport.sRoomTypeElementName);
			Integer order = getRequiredIntegerAttribute(positionTypeElement, PointInTimeDataExport.sOrderAttribute, PointInTimeDataExport.sRoomTypeElementName);
			pt = new PositionType();
			pt.setReference(reference);
			pt.setLabel(label);
			pt.setSortOrder(order);
			pt.setUniqueId((Long)getHibSession().save(pt));
		}
		positionTypes.put(uid, pt);
	}

	private void elementTeachingResponsibility(Element teachingResponsibilityElement) throws Exception {
		String uidString = getRequiredStringAttribute(teachingResponsibilityElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sTeachingResponsibilityElementName);
		Long uid = new Long(uidString);
		String reference = getRequiredStringAttribute(teachingResponsibilityElement, PointInTimeDataExport.sReferenceAttribute, PointInTimeDataExport.sTeachingResponsibilityElementName);
		TeachingResponsibility tr = teachingResponsibilitiesByRef.get(reference);
		if(tr == null){
			String label = getRequiredStringAttribute(teachingResponsibilityElement, PointInTimeDataExport.sLabelAttribute, PointInTimeDataExport.sTeachingResponsibilityElementName);
			String abbreviation = getRequiredStringAttribute(teachingResponsibilityElement, PointInTimeDataExport.sAbbreviationAttribute, PointInTimeDataExport.sTeachingResponsibilityElementName);
			Boolean instructor = getRequiredBooleanAttribute(teachingResponsibilityElement, PointInTimeDataExport.sInstructorAttribute, PointInTimeDataExport.sTeachingResponsibilityElementName);
			Boolean coordinator = getRequiredBooleanAttribute(teachingResponsibilityElement, PointInTimeDataExport.sCoordinatorAttribute, PointInTimeDataExport.sTeachingResponsibilityElementName);
			Integer options = getOptionalIntegerAttribute(teachingResponsibilityElement, PointInTimeDataExport.sOptionsAttribute);
			tr = new TeachingResponsibility();
			tr.setReference(reference);
			tr.setLabel(label);
			tr.setInstructor(instructor);
			tr.setCoordinator(coordinator);
			tr.setAbbreviation(abbreviation);
			tr.setOptions(options == null ? 0 : options.intValue());
			tr.setUniqueId((Long)getHibSession().save(tr));
		}
		teachingResponsibilities.put(uid, tr);
	}

	@SuppressWarnings("unchecked")
	private void initializeRoomTypeData(Element rootElement) throws Exception {
		for (RoomType roomType : RoomType.findAll()) {
			roomTypesByRef.put(roomType.getReference(), roomType);
		}
				
		Element roomTypesElement = rootElement.element(PointInTimeDataExport.sRoomTypesElementName);
		for(Element roomTypeElement : (List<Element>) roomTypesElement.elements()){
			elementRoomType(roomTypeElement);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void initializeCreditTypeData(Element rootElement) throws Exception {
		for (CourseCreditType creditType : CourseCreditType.getCourseCreditTypeList()) {
			creditTypesByRef.put(creditType.getReference(), creditType);
		}
				
		Element creditTypesElement = rootElement.element(PointInTimeDataExport.sCreditTypesElementName);
		for(Element creditTypeElement : (List<Element>) creditTypesElement.elements()){
			elementCreditType(creditTypeElement);
		}
	}

	private void elementCreditType(Element creditTypeElement) throws Exception {
		String uidString = getRequiredStringAttribute(creditTypeElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sCreditTypeElementName);
		Long uid = new Long(uidString);
		String reference = getRequiredStringAttribute(creditTypeElement, PointInTimeDataExport.sReferenceAttribute, PointInTimeDataExport.sCreditTypeElementName);
		CourseCreditType ct = creditTypesByRef.get(reference);
		if(ct == null){
			String label = getRequiredStringAttribute(creditTypeElement, PointInTimeDataExport.sLabelAttribute, PointInTimeDataExport.sCreditTypeElementName);
			String abbreviation = getRequiredStringAttribute(creditTypeElement, PointInTimeDataExport.sAbbreviationAttribute, PointInTimeDataExport.sCreditTypeElementName);
			String masterCode = getOptionalStringAttribute(creditTypeElement, PointInTimeDataExport.sLegacyCourseMasterCodeAttribute);
			ct = new CourseCreditType();
			ct.setReference(reference);
			ct.setLabel(label);
			ct.setAbbreviation(abbreviation);
			if (masterCode != null){
				ct.setLegacyCourseMasterCode(masterCode);
			}
			ct.setUniqueId((Long)getHibSession().save(ct));
		}
		creditTypes.put(uid, ct);
		
	}

	@SuppressWarnings("unchecked")
	private void initializeCreditUnitTypeData(Element rootElement) throws Exception {
		for (CourseCreditUnitType creditUnitType : CourseCreditUnitType.getCourseCreditUnitTypeList()) {
			creditUnitTypesByRef.put(creditUnitType.getReference(), creditUnitType);
		}
				
		Element creditUnitTypesElement = rootElement.element(PointInTimeDataExport.sCreditUnitTypesElementName);
		for(Element creditUnitTypeElement : (List<Element>) creditUnitTypesElement.elements()){
			elementCreditUnitType(creditUnitTypeElement);
		}
	
	}
	
	private void elementCreditUnitType(Element creditUnitTypeElement) throws Exception {
		String uidString = getRequiredStringAttribute(creditUnitTypeElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sCreditUnitTypeElementName);
		Long uid = new Long(uidString);
		String reference = getRequiredStringAttribute(creditUnitTypeElement, PointInTimeDataExport.sReferenceAttribute, PointInTimeDataExport.sCreditUnitTypeElementName);
		CourseCreditUnitType cut = creditUnitTypesByRef.get(reference);
		if(cut == null){
			String label = getRequiredStringAttribute(creditUnitTypeElement, PointInTimeDataExport.sLabelAttribute, PointInTimeDataExport.sCreditUnitTypeElementName);
			String abbreviation = getRequiredStringAttribute(creditUnitTypeElement, PointInTimeDataExport.sAbbreviationAttribute, PointInTimeDataExport.sCreditUnitTypeElementName);
			cut = new CourseCreditUnitType();
			cut.setReference(reference);
			cut.setLabel(label);
			cut.setAbbreviation(abbreviation);
			cut.setUniqueId((Long)getHibSession().save(cut));
		}
		creditUnitTypes.put(uid, cut);
	}

	@SuppressWarnings("unchecked")
	private void initializePositionTypeData(Element rootElement) throws Exception {
		for (PositionType positionType : (TreeSet<PositionType>)PositionType.findAll()) {
			positionTypesByRef.put(positionType.getReference(), positionType);
		}
				
		Element positionTypesElement = rootElement.element(PointInTimeDataExport.sPositionTypesElementName);
		for(Element positionTypeElement : (List<Element>) positionTypesElement.elements()){
			elementPositionType(positionTypeElement);
		}
		
	}
	@SuppressWarnings("unchecked")
	private void initializeTeachingResponsibilityData(Element rootElement) throws Exception {
		for (TeachingResponsibility instructorTeachingResponsibility : TeachingResponsibility.getInstructorTeachingResponsibilities()) {
			teachingResponsibilitiesByRef.put(instructorTeachingResponsibility.getReference(), instructorTeachingResponsibility);
		}
		for (TeachingResponsibility coordinatorTeachingResponsibility : TeachingResponsibility.getCoordinatorTeachingResponsibilities()) {
			teachingResponsibilitiesByRef.put(coordinatorTeachingResponsibility.getReference(), coordinatorTeachingResponsibility);
		}
				
		Element responsibilitiesElement = rootElement.element(PointInTimeDataExport.sTeachingResponsibilitiesElementName);
		for(Element responsibilityElement : (List<Element>) responsibilitiesElement.elements()){
			elementTeachingResponsibility(responsibilityElement);
		}
		
	}

	private void elementInstructionalMethod(Element instructionalMethodElement) throws Exception {
		String reference = getRequiredStringAttribute(instructionalMethodElement, PointInTimeDataExport.sReferenceAttribute, PointInTimeDataExport.sInstructionalMethodElementName);
		if(!instructionalMethods.containsKey(reference)){
			String label = getRequiredStringAttribute(instructionalMethodElement, PointInTimeDataExport.sLabelAttribute, PointInTimeDataExport.sInstructionalMethodElementName);
			Boolean visible = getRequiredBooleanAttribute(instructionalMethodElement, PointInTimeDataExport.sVisibleAttribute, PointInTimeDataExport.sInstructionalMethodElementName);
			InstructionalMethod im = new InstructionalMethod();
			im.setReference(reference);
			im.setLabel(label);
			im.setVisible(visible);
			im.setUniqueId((Long)getHibSession().save(im));
			instructionalMethods.put(im.getReference(), im);
		}		
	}

	@SuppressWarnings("unchecked")
	private void initializeInstructionalMethodData(Element rootElement) throws Exception {
		for (InstructionalMethod instructionalMethod : InstructionalMethod.findAll()) {
			instructionalMethods.put(instructionalMethod.getReference(), instructionalMethod);
		}
		
		Element instructionalMethodsElement = rootElement.element(PointInTimeDataExport.sInstructionalMethodsElementName);
		for(Element instructionalMethodElement : (List<Element>) instructionalMethodsElement.elements()){
			elementInstructionalMethod(instructionalMethodElement);
		}
		
	}

	private void elementClassDurationType(Element classDurationTypeElement) throws Exception {
		String reference = getRequiredStringAttribute(classDurationTypeElement, PointInTimeDataExport.sReferenceAttribute, PointInTimeDataExport.sClassDurationTypeElementName);
		if(!classDurationTypes.containsKey(reference)){
			String label = getRequiredStringAttribute(classDurationTypeElement, PointInTimeDataExport.sLabelAttribute, PointInTimeDataExport.sClassDurationTypeElementName);
			String abbreviation = getRequiredStringAttribute(classDurationTypeElement, PointInTimeDataExport.sAbbreviationAttribute, PointInTimeDataExport.sClassDurationTypeElementName);
			String implementation = getRequiredStringAttribute(classDurationTypeElement, PointInTimeDataExport.sImplementationAttribute, PointInTimeDataExport.sClassDurationTypeElementName);
			String parameter = getRequiredStringAttribute(classDurationTypeElement, PointInTimeDataExport.sParameterAttribute, PointInTimeDataExport.sClassDurationTypeElementName);
			Boolean visible = getRequiredBooleanAttribute(classDurationTypeElement, PointInTimeDataExport.sVisibleAttribute, PointInTimeDataExport.sClassDurationTypeElementName);
			ClassDurationType cdt = new ClassDurationType();
			cdt.setReference(reference);
			cdt.setLabel(label);
			cdt.setAbbreviation(abbreviation);
			cdt.setImplementation(implementation);
			cdt.setParameter(parameter);
			cdt.setVisible(visible);
			cdt.setUniqueId((Long)getHibSession().save(cdt));
			classDurationTypes.put(cdt.getReference(), cdt);
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void initializeClassDurationTypeData(Element rootElement) throws Exception {
		for (ClassDurationType classDurationType : ClassDurationType.findAll()) {
			classDurationTypes.put(classDurationType.getReference(), classDurationType);
		}
				
		Element classDurationTypesElement = rootElement.element(PointInTimeDataExport.sClassDurationTypesElementName);
		for(Element classDurationTypeElement : (List<Element>) classDurationTypesElement.elements()){
			elementClassDurationType(classDurationTypeElement);
		}
		
	}
	
	private void elementCourseType(Element courseTypeElement) throws Exception{
		String uidString = getRequiredStringAttribute(courseTypeElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sCourseTypeElementName);
		Long uid = new Long(uidString);
		String reference = getRequiredStringAttribute(courseTypeElement, PointInTimeDataExport.sReferenceAttribute, PointInTimeDataExport.sCourseTypeElementName);
		CourseType ct = courseTypesByRef.get(reference);
		if(ct == null){
			String label = getRequiredStringAttribute(courseTypeElement, PointInTimeDataExport.sLabelAttribute, PointInTimeDataExport.sCourseTypeElementName);
			ct = new CourseType();
			ct.setReference(reference);
			ct.setLabel(label);
			ct.setUniqueId((Long)getHibSession().save(ct));
		}
		courseTypes.put(uid, ct);		
	}

	@SuppressWarnings("unchecked")
	private void initializeCourseTypeData(Element rootElement) throws Exception {
		for (CourseType courseType : (List<CourseType>)getHibSession().createQuery("from CourseType").list()) {
			courseTypesByRef.put(courseType.getReference(), courseType);
		}
			
		Element courseTypesElement = rootElement.element(PointInTimeDataExport.sCourseTypesElementName);
		for(Element courseTypeElement : (List<Element>) courseTypesElement.elements()){
			elementCourseType(courseTypeElement);
		}
	}

	private void logXmlFileCreateInformation(Element rootElement) {
        String created = getOptionalStringAttribute(rootElement, "created");
        if (created != null) {
	        addNote("Loading offerings XML file created on: " + created);
			ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.UPDATE, null, null);
			updateChangeList(true);
        }		
	}
	
	protected void initializeSession(Element rootElement, String rootElementName) throws Exception {

		String sessionUidString = getRequiredStringAttribute(rootElement, PointInTimeDataExport.sAcademicSessionUniqueIdAttribute, rootElementName);
        String campus = getRequiredStringAttribute(rootElement, PointInTimeDataExport.sAcademicInitiativeAttribute, rootElementName);
        String year   = getRequiredStringAttribute(rootElement, PointInTimeDataExport.sAcademicYearAttribute, rootElementName);
        String term   = getRequiredStringAttribute(rootElement, PointInTimeDataExport.sAcademicTermAttribute, rootElementName);
		Long sessionUid = null;
		try {
			sessionUid = new Long(sessionUidString);			
		} catch (Exception e) {
			info("Could not convert sessionUidString to long: " + sessionUidString);
			info("Looking up session using academicInitiative:  " + campus+", academicYear:  " + year + " and academicTerm:  " + term);
			sessionUid = null;
		}
        if (sessionUid != null){
        	session = Session.getSessionById(sessionUid);
			info("Could find session using uniqueId: " + sessionUidString);
			info("Looking up session using academicInitiative:  " + campus+", academicYear:  " + year + " and academicTerm:  " + term);
        }
        if (session == null){
            session = findSession(campus, year, term);        	
        }
        if(session == null) {
           info ("No session found for academicInitiative:  " + campus+", academicYear:  " + year + " and academicTerm:  " + term);
           info("Creating a new academic session based on data from import file");
           
           session = new Session();
           session.setAcademicInitiative(campus);
           session.setAcademicTerm(term);
           session.setAcademicYear(year);
           session.setSessionBeginDateTime(CalendarUtils.getDate(getRequiredStringAttribute(rootElement, PointInTimeDataExport.sSessionBeginDateAttribute, rootElementName), dateFormat));
           session.setSessionEndDateTime(CalendarUtils.getDate(getRequiredStringAttribute(rootElement, PointInTimeDataExport.sSessionEndDateAttribute, rootElementName), dateFormat));
           session.setClassesEndDateTime(CalendarUtils.getDate(getRequiredStringAttribute(rootElement, PointInTimeDataExport.sClassesEndDateAttribute, rootElementName), dateFormat));
           session.setExamBeginDate(session.getClassesEndDateTime());
           session.setEventBeginDate(session.getSessionBeginDateTime());
           session.setEventEndDate(session.getSessionEndDateTime());
           session.setStatusType(DepartmentStatusType.findByRef("initial"));
           session.setLastWeekToEnroll(new Integer(1));
           session.setLastWeekToChange(new Integer(1));
           session.setLastWeekToDrop(new Integer(1));
           String defaultDurationType = getOptionalStringAttribute(rootElement, PointInTimeDataExport.sDurationTypeAttribute);
           if (defaultDurationType != null){
               session.setDefaultClassDurationType(classDurationTypes.get(defaultDurationType));        	   
           }
           session.setUniqueId((Long)getHibSession().save(session));
        }
        pointInTimeData = new PointInTimeData();
        pointInTimeData.setName(getRequiredStringAttribute(rootElement, PointInTimeDataExport.sPointInTimeNameAttribute, rootElementName));
        pointInTimeData.setNote(getRequiredStringAttribute(rootElement, PointInTimeDataExport.sPointInTimeNoteAttribute, rootElementName));
        pointInTimeData.setSavedSuccessfully(new Boolean(false));
        pointInTimeData.setSession(session);
        pointInTimeData.setTimestamp(CalendarUtils.getDate(getRequiredStringAttribute(rootElement, PointInTimeDataExport.sCreatedAttribute, rootElementName), (dateFormat + " " + timeFormat)));
        pointInTimeData.setUniqueId((Long)getHibSession().save(pointInTimeData));
	}

	
	protected void initializeSessionData(Element rootElement) throws Exception {

		initializeSession(rootElement, PointInTimeDataExport.sRootElementName);
		initializeDepartmentData(rootElement);
		initializeCurriculaData(rootElement);
		initializeDatePatternData(rootElement);
		initializeTimePatternData(rootElement);
		initializeLocationData(rootElement);
		
	}

	@SuppressWarnings("unchecked")
	private void initializeCurriculaData(Element rootElement) throws Exception {
		loadExistingAcademicAreas(session.getUniqueId());
		loadExistingAcademicClassifications(session.getUniqueId());
		loadExistingMajors(session.getUniqueId());
		loadExistingMinors(session.getUniqueId());
		
		Element academicAreasElement =  rootElement.element(PointInTimeDataExport.sAcademicAreasElementName);
		if (academicAreasElement != null) {
			for(Element academicAreaElement : (List<Element>)academicAreasElement.elements()){
				elementAcademicArea(academicAreaElement);
			}			
		}
		Element academicClassificationsElement =  rootElement.element(PointInTimeDataExport.sAcademicClassificationsElementName);
		if (academicClassificationsElement != null) {
			for(Element academicClassificationElement : (List<Element>)academicClassificationsElement.elements()){
				elementAcademicClassification(academicClassificationElement);
			}			
		}

		Element majorsElement =  rootElement.element(PointInTimeDataExport.sMajorsElementName);
		if (majorsElement != null) {
			for(Element majorElement : (List<Element>)majorsElement.elements()){
				elementMajor(majorElement);
			}			
		}
	
		Element minorsElement =  rootElement.element(PointInTimeDataExport.sMinorsElementName);
		if (minorsElement != null) {
			for(Element minorElement : (List<Element>)minorsElement.elements()){
				elementMinor(minorElement);
			}			
		}
	
	}

	private void initializeDateTimeFormats(Element rootElement) {
        dateFormat = getOptionalStringAttribute(rootElement, "dateFormat");
        timeFormat = getOptionalStringAttribute(rootElement, "timeFormat");
        if(timeFormat == null){
        	timeFormat = "HHmm";
        }		
	}

	protected void initializeTrimLeadingZeros() {
		trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();
	}

	@SuppressWarnings("unchecked")
	private void initializeDatePatternData(Element rootElement) throws Exception {
        loadExistingDatePatterns(session.getUniqueId());

        Element datePatternsElement = rootElement.element(PointInTimeDataExport.sDatePatternsElementName);
        for(Element datePatternElement : (List<Element>) datePatternsElement.elements()){
        	elementDatePattern(datePatternElement);
        }
	
	}
	
	

	@SuppressWarnings("unchecked")
	private void initializeTimePatternData(Element rootElement) throws Exception {
        loadExistingTimePatterns(session.getUniqueId());

        Element timePatternsElement = rootElement.element(PointInTimeDataExport.sTimePatternsElementName);
        for(Element timePatternElement : (List<Element>) timePatternsElement.elements()){
        	elementTimePattern(timePatternElement);
        }
		
	}
	
	private void elementDatePattern(Element datePatternElement) throws Exception {
		String uidString = getRequiredStringAttribute(datePatternElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sDatePatternElementName);
		Long uid = new Long(uidString);
		String name = getRequiredStringAttribute(datePatternElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sDatePatternElementName);
		DatePattern dp = datePatternsByName.get(name);
		if(dp == null){
			dp = new DatePattern();
			dp.setName(name);
			String numWksStr = getOptionalStringAttribute(datePatternElement, PointInTimeDataExport.sNumberOfWeeksAttribute);
			if (numWksStr != null) {
				dp.setNumberOfWeeks(new Float(numWksStr));
			}
			dp.setOffset(getRequiredIntegerAttribute(datePatternElement, PointInTimeDataExport.sOffsetAttribute, PointInTimeDataExport.sDatePatternElementName));
			dp.setPattern(getRequiredStringAttribute(datePatternElement, PointInTimeDataExport.sPatternAttribute, PointInTimeDataExport.sDatePatternElementName));
			dp.setVisible(getRequiredBooleanAttribute(datePatternElement, PointInTimeDataExport.sVisibleAttribute, PointInTimeDataExport.sDatePatternElementName));
			dp.setType(getRequiredIntegerAttribute(datePatternElement, PointInTimeDataExport.sTypeAttribute, PointInTimeDataExport.sDatePatternElementName));
			dp.setSession(session);
			dp.setUniqueId((Long)getHibSession().save(dp));
		}		
		datePatterns.put(uid, dp);
	}


	@SuppressWarnings("unchecked")
	private void elementTimePattern(Element timePatternElement) throws Exception {
		String uidString = getRequiredStringAttribute(timePatternElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sTimePatternElementName);
		Long uid = new Long(uidString);
		String name = getRequiredStringAttribute(timePatternElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sTimePatternElementName);
		TimePattern tp = timePatternsByName.get(name);

		if(tp == null){
			tp = new TimePattern();
			tp.setName(name);
			tp.setMinPerMtg(getRequiredIntegerAttribute(timePatternElement, PointInTimeDataExport.sMinutesPerMeetingAttribute, PointInTimeDataExport.sTimePatternElementName));
			tp.setSlotsPerMtg(getRequiredIntegerAttribute(timePatternElement, PointInTimeDataExport.sSlotsPerMeetingAttribute, PointInTimeDataExport.sTimePatternElementName));
			tp.setNrMeetings(getRequiredIntegerAttribute(timePatternElement, PointInTimeDataExport.sNumberOfMeetingsPerWeekAttribute, PointInTimeDataExport.sTimePatternElementName));
			tp.setVisible(getRequiredBooleanAttribute(timePatternElement, PointInTimeDataExport.sVisibleAttribute, PointInTimeDataExport.sTimePatternElementName));
			tp.setType(getRequiredIntegerAttribute(timePatternElement, PointInTimeDataExport.sTypeAttribute, PointInTimeDataExport.sTimePatternElementName));
			tp.setBreakTime(getRequiredIntegerAttribute(timePatternElement, PointInTimeDataExport.sBreakTimeAttribute, PointInTimeDataExport.sTimePatternElementName));
			tp.setSession(session);
			for(Element element : (List<Element>)timePatternElement.elements()){
				if (element.getName().equals(PointInTimeDataExport.sTimePatternDaysElementName)) {
					elementTimePatternDays(element, tp);
				} else if (element.getName().equals(PointInTimeDataExport.sTimePatternTimeElementName)){
					elementTimePatternTime(element, tp);
				}
			}
			tp.setUniqueId((Long)getHibSession().save(tp));
		}		
		timePatterns.put(uid, tp);
	}

	private void elementTimePatternTime(Element element, TimePattern tp) throws Exception {
		TimePatternTime timePatternTime = new TimePatternTime();
		timePatternTime.setStartSlot(getRequiredIntegerAttribute(element, PointInTimeDataExport.sStartSlotAttribute, PointInTimeDataExport.sTimePatternDaysElementName));
		tp.addTotimes(timePatternTime);
	}

	private void elementTimePatternDays(Element element, TimePattern tp) throws Exception {
		TimePatternDays timePatternDays = new TimePatternDays();
		timePatternDays.setDayCode(getRequiredIntegerAttribute(element, PointInTimeDataExport.sDayCodeAttribute, PointInTimeDataExport.sTimePatternDaysElementName));
		tp.addTodays(timePatternDays);
		
	}

	@SuppressWarnings("unchecked")
	private void initializeLocationData(Element rootElement) throws Exception {
        loadExistingBuildings(session.getUniqueId());
        loadExistingLocations(session.getUniqueId());

        Element locationsElement = rootElement.element(PointInTimeDataExport.sLocationsElementName);
        for(Element element : (List<Element>)locationsElement.elements()) {
        	if (element.getName().equals(PointInTimeDataExport.sBuildingElementName)) {
        		elementBuilding(element);
        	} else if (element.getName().equals(PointInTimeDataExport.sNonUniversityLocationElementName)){
        		elementNonUniversityLocation(element);
        	}
        }
		
	}

	private void elementNonUniversityLocation(Element nonUniversityLocationElement) throws Exception {
		String uidString = getRequiredStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sNonUniversityLocationElementName);
		Long uid = new Long(uidString);
		Long permId = new Long(getRequiredStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sPermanentIdAttribute, PointInTimeDataExport.sNonUniversityLocationElementName));
		String name = getRequiredStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sNonUniversityLocationElementName);
		Location l = locationsByName.get(name + permId.toString());
		if(l == null){
			NonUniversityLocation n = new NonUniversityLocation();
			n.setName(name);
			n.setSession(session);
			session.addTorooms(n);
			String roomTypeStr = getRequiredStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sRoomTypeIdAttribute, PointInTimeDataExport.sNonUniversityLocationElementName);
			n.setRoomType(roomTypes.get(new Long(roomTypeStr)));
			String capacityStr = getRequiredStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sCapacityAttribute, PointInTimeDataExport.sNonUniversityLocationElementName);
			n.setCapacity(new Integer(capacityStr));
			String externalId = getOptionalStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sExternalIdAttribute);
			if (externalId != null){
				n.setExternalUniqueId(externalId);
			}
			String coordinateXstr = getOptionalStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sCoordinateXAttribute);
			if (coordinateXstr != null){
				Double coordinateX = new Double(coordinateXstr);
				n.setCoordinateX(coordinateX);
			}
			String coordinateYstr = getOptionalStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sCoordinateXAttribute);
			if (coordinateYstr != null){
				Double coordinateY = new Double(coordinateYstr);
				n.setCoordinateY(coordinateY);
			}
			n.setIgnoreRoomCheck(new Boolean(false));
			n.setIgnoreTooFar(new Boolean(false));
			n.setPermanentId(new Long(getRequiredStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sPermanentIdAttribute, PointInTimeDataExport.sNonUniversityLocationElementName)));
			n.setUniqueId((Long)getHibSession().save(n));
			String ctrlDeptIdStr = getOptionalStringAttribute(nonUniversityLocationElement, PointInTimeDataExport.sControllingDepartmentUniqueIdAttribute);
			if (ctrlDeptIdStr != null){
				RoomDept rd = new RoomDept();
				rd.setControl(new Boolean(true));
				rd.setDepartment(departments.get(new Long(ctrlDeptIdStr)));
				rd.setRoom(n);
				n.addToroomDepts(rd);
				rd.setUniqueId((Long)getHibSession().save(rd));
			}
			l = n;
		}
		locations.put(uid, l);
	}

	@SuppressWarnings("unchecked")
	private void elementBuilding(Element buildingElement) throws Exception {
		String uidString = getRequiredStringAttribute(buildingElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sBuildingElementName);
		Long uid = new Long(uidString);
		String abbreviation = getRequiredStringAttribute(buildingElement, PointInTimeDataExport.sAbbreviationAttribute, PointInTimeDataExport.sBuildingElementName);
		
		Building b = buildingsByAbbv.get(abbreviation);
		if(b == null || (!b.getAbbreviation().equals(abbreviation))){
			b = new Building();
			b.setAbbreviation(abbreviation);
			b.setSession(session);
			session.addTobuildings(b);
			String name = getRequiredStringAttribute(buildingElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sBuildingElementName);
			b.setName(name);
			String externalId = getOptionalStringAttribute(buildingElement, PointInTimeDataExport.sExternalIdAttribute);
			if (externalId != null){
				b.setExternalUniqueId(externalId);
			}
			String coordinateXstr = getOptionalStringAttribute(buildingElement, PointInTimeDataExport.sCoordinateXAttribute);
			if (coordinateXstr != null){
				Double coordinateX = new Double(coordinateXstr);
				b.setCoordinateX(coordinateX);
				}
			String coordinateYstr = getOptionalStringAttribute(buildingElement, PointInTimeDataExport.sCoordinateXAttribute);
			if (coordinateYstr != null){
				Double coordinateY = new Double(coordinateYstr);
				b.setCoordinateY(coordinateY);
				}
			b.setUniqueId((Long)getHibSession().save(b));
		}
		buildings.put(uid, b);
		for(Element roomElement : (List<Element>)buildingElement.elements()){
			elementRoom(roomElement, b);
		}
	}
	

	private void elementRoom(Element roomElement, Building building) throws Exception {
		String uidString = getRequiredStringAttribute(roomElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sRoomElementName);
		Long uid = new Long(uidString);
		String roomNumber = getRequiredStringAttribute(roomElement, PointInTimeDataExport.sRoomNumberAttribute, PointInTimeDataExport.sRoomElementName);
		Long permId = new Long(getRequiredStringAttribute(roomElement, PointInTimeDataExport.sPermanentIdAttribute, PointInTimeDataExport.sRoomElementName));
		Location l = locationsByName.get(building.getAbbreviation() + roomNumber + permId.toString());
		
		if(l == null){
		
			Room r = new Room();
			r.setBuilding(building);
			r.setRoomNumber(roomNumber);
			r.setSession(session);
			session.addTorooms(r);
			String roomTypeStr = getRequiredStringAttribute(roomElement, PointInTimeDataExport.sRoomTypeIdAttribute, PointInTimeDataExport.sRoomElementName);
			r.setRoomType(roomTypes.get(new Long(roomTypeStr)));
			String capacityStr = getRequiredStringAttribute(roomElement, PointInTimeDataExport.sCapacityAttribute, PointInTimeDataExport.sRoomElementName);
			r.setCapacity(new Integer(capacityStr));
			String externalId = getOptionalStringAttribute(roomElement, PointInTimeDataExport.sExternalIdAttribute);
			if (externalId != null){
				r.setExternalUniqueId(externalId);
			}
			r.setPermanentId(permId);
			r.setIgnoreRoomCheck(new Boolean(false));
			r.setIgnoreTooFar(new Boolean(false));
			String coordinateXstr = getOptionalStringAttribute(roomElement, PointInTimeDataExport.sCoordinateXAttribute);
			if (coordinateXstr != null){
				Double coordinateX = new Double(coordinateXstr);
				r.setCoordinateX(coordinateX);
			}
			String coordinateYstr = getOptionalStringAttribute(roomElement, PointInTimeDataExport.sCoordinateXAttribute);
			if (coordinateYstr != null){
				Double coordinateY = new Double(coordinateYstr);
				r.setCoordinateY(coordinateY);
			}
			r.setUniqueId((Long)getHibSession().save(r));
			String ctrlDeptIdStr = getOptionalStringAttribute(roomElement, PointInTimeDataExport.sControllingDepartmentUniqueIdAttribute);
			if (ctrlDeptIdStr != null){
				RoomDept rd = new RoomDept();
				rd.setControl(new Boolean(true));
				rd.setDepartment(departments.get(new Long(ctrlDeptIdStr)));
				rd.setRoom(r);
				r.addToroomDepts(rd);
				rd.setUniqueId((Long)getHibSession().save(rd));
			}
			l = r;
		}
		locations.put(uid, l);
	}

	@SuppressWarnings("unchecked")
	private void initializeDepartmentData(Element rootElement) throws Exception {
		loadExistingDepartments(session.getUniqueId());
        loadExistingSubjectAreas(session.getUniqueId());
        loadDepartmentalInstructors(session.getUniqueId());
		
		Element departmentsElement = rootElement.element(PointInTimeDataExport.sDepartmentsElementName);
		for(Element departmentElement : (List<Element>) departmentsElement.elements()){
			elementDepartment(departmentElement);
		}
		
	}

	@SuppressWarnings("unchecked")
	private void elementDepartment(Element departmentElement) throws Exception{
		String uniqueIdStr = getRequiredStringAttribute(departmentElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sDepartmentElementName);
		Long uid = new Long(uniqueIdStr);
		String deptCode = getRequiredStringAttribute(departmentElement, PointInTimeDataExport.sDepartmentCode, PointInTimeDataExport.sDepartmentElementName);
		Department d = departmentsByCode.get(deptCode);
		if(d == null){
			String name = getRequiredStringAttribute(departmentElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sDepartmentElementName);
			String abbreviation = getOptionalStringAttribute(departmentElement, PointInTimeDataExport.sAbbreviationAttribute);
			String externalId = getRequiredStringAttribute(departmentElement, PointInTimeDataExport.sExternalIdAttribute, PointInTimeDataExport.sDepartmentElementName);
			d = new Department();
			if (abbreviation != null) {
				d.setAbbreviation(abbreviation);
			}
			d.setName(name);
			d.setDeptCode(deptCode);
			d.setExternalUniqueId(externalId);
			d.setSession(session);
			d.setAllowEvents(new Boolean(false));
			d.setAllowReqTime(new Boolean(true));
			d.setAllowReqRoom(new Boolean(true));
			d.setAllowReqDistribution(new Boolean(true));
			d.setAllowStudentScheduling(new Boolean(true));
			d.setInheritInstructorPreferences(new Boolean(true));
			d.setExternalManager(new Boolean(false));
			d.setDistributionPrefPriority(new Integer(0));
			session.addTodepartments(d);
			d.setUniqueId((Long)getHibSession().save(d));
			departmentsByCode.put(deptCode, d);
		}
		departments.put(uid, d);
		for(Element element : (List<Element>)departmentElement.elements()){
			if (element.getName().equals(PointInTimeDataExport.sDeptInstructorElementName)){
				elementDepartmentalInstructor(element, d);
			}				
			if (element.getName().equals(PointInTimeDataExport.sSubjectAreaElementName)){
				elementSubjectArea(element, d);
			}				
		}
	}
	private void elementDepartmentalInstructor(Element departmentalInstructorElement,
			Department department) throws Exception {
		
		if (departmentalInstructorElement.getName().equals(PointInTimeDataExport.sDeptInstructorElementName)){
			PitDepartmentalInstructor pitDeptInstr = new PitDepartmentalInstructor();

			String uniqueIdStr = getRequiredStringAttribute(departmentalInstructorElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sDeptInstructorElementName);
			Long uid = new Long(uniqueIdStr);
			String externalId = getOptionalStringAttribute(departmentalInstructorElement, PointInTimeDataExport.sExternalIdAttribute);			
			if (externalId != null) {
				pitDeptInstr.setExternalUniqueId(externalId);
			}
			String firstName = getOptionalStringAttribute(departmentalInstructorElement, PointInTimeDataExport.sFirstNameAttribute);
			if (firstName != null) { 
				pitDeptInstr.setFirstName(firstName);
			}
			String middleName = getOptionalStringAttribute(departmentalInstructorElement, PointInTimeDataExport.sMiddleNameAttribute);
			if (middleName != null) { 
				pitDeptInstr.setMiddleName(middleName);
			}
			String lastName = getRequiredStringAttribute(departmentalInstructorElement, PointInTimeDataExport.sLastNameAttribute, PointInTimeDataExport.sDeptInstructorElementName);
			pitDeptInstr.setLastName(lastName);
			DepartmentalInstructor di = departmentalInstructorsByName.get(
					(lastName == null? "":lastName)
					+ (firstName == null? "" : firstName)
					+ (middleName == null? "": middleName)
					+ (externalId == null? "" : externalId)
					);
			if (di != null){
				pitDeptInstr.setDepartmentalInstructor(di);
			}
			String careerAcct = getOptionalStringAttribute(departmentalInstructorElement, PointInTimeDataExport.sCareerAcctAttribute);
			if (careerAcct != null){
				pitDeptInstr.setCareerAcct(careerAcct);
			}
			String email = getOptionalStringAttribute(departmentalInstructorElement, PointInTimeDataExport.sEmailAttribute);
			if (email != null){
				pitDeptInstr.setEmail(email);
			}
			String positionTypeIdStr = getOptionalStringAttribute(departmentalInstructorElement, PointInTimeDataExport.sPositionTypeUniqueIdAttribute);
			if (positionTypeIdStr != null){
				pitDeptInstr.setPositionType(positionTypes.get(new Long(positionTypeIdStr)));
			}
			
			pitDeptInstr.setDepartment(department);
			pitDeptInstr.setPointInTimeData(pointInTimeData);
			pitDeptInstr.setUniqueId((Long)getHibSession().save(pitDeptInstr));
			pitDepartmentInstructors.put(uid, pitDeptInstr);
		}
	}

	private void elementSubjectArea(Element subjectAreaElement, Department department) throws Exception {
		
		if (subjectAreaElement.getName().equals(PointInTimeDataExport.sSubjectAreaElementName)){
			String subjectAreaAbbreviation = getRequiredStringAttribute(subjectAreaElement, PointInTimeDataExport.sSubjectAreaAbbreviationAttribute, PointInTimeDataExport.sSubjectAreaElementName);
			if(!subjectAreas.containsKey(subjectAreaAbbreviation)){
				String title = getRequiredStringAttribute(subjectAreaElement, PointInTimeDataExport.sTitleAttribute, PointInTimeDataExport.sSubjectAreaElementName);
				String externalId = getRequiredStringAttribute(subjectAreaElement, PointInTimeDataExport.sExternalIdAttribute, PointInTimeDataExport.sSubjectAreaElementName);
				SubjectArea sa = new SubjectArea();
				sa.setSubjectAreaAbbreviation(subjectAreaAbbreviation);
				sa.setTitle(title);
				sa.setExternalUniqueId(externalId);
				sa.setDepartment(department);
				department.addTosubjectAreas(sa);
				sa.setSession(session);
				session.addTosubjectAreas(sa);
				sa.setUniqueId((Long)getHibSession().save(sa));
				subjectAreas.put(sa.getSubjectAreaAbbreviation(), sa);
			}
		}		
	}
	
	private void elementMajor(Element majorElement) throws Exception {
		String uidString = getRequiredStringAttribute(majorElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sMajorElementName);
		Long uid = new Long(uidString);
		String code = getRequiredStringAttribute(majorElement, PointInTimeDataExport.sCodeAttribute, PointInTimeDataExport.sMajorElementName);
		PosMajor major = majorsByCode.get(code);
		if(major == null){
			String name = getRequiredStringAttribute(majorElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sMajorElementName);
			major = new PosMajor();
			major.setCode(code);
			major.setName(name);
			major.setSession(session);
			String externalId = getOptionalStringAttribute(majorElement, PointInTimeDataExport.sExternalIdAttribute);
			if (externalId != null){
				major.setExternalUniqueId(externalId);
			}
			major.setUniqueId((Long)getHibSession().save(major));
		}		
		majors.put(uid, major);
	}

	private void elementMinor(Element minorElement) throws Exception {
		String uidString = getRequiredStringAttribute(minorElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sMinorElementName);
		Long uid = new Long(uidString);
		String code = getRequiredStringAttribute(minorElement, PointInTimeDataExport.sCodeAttribute, PointInTimeDataExport.sMinorElementName);
		PosMinor minor = minorsByCode.get(code);
		if(minor == null){
			String name = getRequiredStringAttribute(minorElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sMinorElementName);
			minor = new PosMinor();
			minor.setCode(code);
			minor.setName(name);
			minor.setSession(session);
			String externalId = getOptionalStringAttribute(minorElement, PointInTimeDataExport.sExternalIdAttribute);
			if (externalId != null){
				minor.setExternalUniqueId(externalId);
			}
			minor.setUniqueId((Long)getHibSession().save(minor));
		}		
		minors.put(uid, minor);
	}

	private void elementAcademicClassification(Element academicClassificationElement) throws Exception {
		String uidString = getRequiredStringAttribute(academicClassificationElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sAcademicClassificationElementName);
		Long uid = new Long(uidString);
		String code = getRequiredStringAttribute(academicClassificationElement, PointInTimeDataExport.sCodeAttribute, PointInTimeDataExport.sAcademicClassificationElementName);
		AcademicClassification ac = academicClassificationsByCode.get(code);
		if(ac == null){
			String name = getRequiredStringAttribute(academicClassificationElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sAcademicClassificationElementName);
			ac = new AcademicClassification();
			ac.setCode(code);
			ac.setName(name);
			ac.setSession(session);
			String externalId = getOptionalStringAttribute(academicClassificationElement, PointInTimeDataExport.sExternalIdAttribute);
			if (externalId != null){
				ac.setExternalUniqueId(externalId);
			}
			ac.setUniqueId((Long)getHibSession().save(ac));
		}			
		academicClassifications.put(uid, ac);
	}

	private void elementAcademicArea(Element academicAreaElement) throws Exception {
		String uidString = getRequiredStringAttribute(academicAreaElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sAcademicAreaElementName);
		Long uid = new Long(uidString);
		String abbreviation = getRequiredStringAttribute(academicAreaElement, PointInTimeDataExport.sAbbreviationAttribute, PointInTimeDataExport.sAcademicAreaElementName);
		AcademicArea aa = academicAreasByAbbv.get(abbreviation);
		if(aa == null){
			String title = getRequiredStringAttribute(academicAreaElement, PointInTimeDataExport.sTitleAttribute, PointInTimeDataExport.sAcademicAreaElementName);
			aa = new AcademicArea();
			aa.setAcademicAreaAbbreviation(abbreviation);
			aa.setTitle(title);
			aa.setSession(session);
			String externalId = getOptionalStringAttribute(academicAreaElement, PointInTimeDataExport.sExternalIdAttribute);
			if (externalId != null){
				aa.setExternalUniqueId(externalId);
			}
			aa.setUniqueId((Long)getHibSession().save(aa));
		}		
		academicAreas.put(uid, aa);
	}



	private void loadSetupData() throws Exception{
        loadItypes();
        loadExistingInstructionalOfferings(session.getUniqueId());
        loadExistingCourseOfferings(session.getUniqueId());
        loadExistingInstrOfferingConfigs(session.getUniqueId());
        loadExistingSchedulingSubparts(session.getUniqueId());
        loadExistingClasses(session.getUniqueId());
	}
	
	@SuppressWarnings("unchecked")
	private void loadExistingStudents(Long sessionId) {
		for (Student s : (List<Student>) this.
				getHibSession().
				createQuery("select distinct s from Student as s where s.session.uniqueId=:sessionId").
				setLong("sessionId", sessionId.longValue()).
				setCacheable(true).
				list()) {
			students.put(s.getUniqueId(), s);
		}

	}

	private void loadExistingTimePatterns(Long sessionId) {
		@SuppressWarnings("unchecked")
		List<TimePattern> patterns = (List<TimePattern>) this.
			getHibSession().
			createQuery("select distinct tp from TimePattern as tp where tp.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list();
		for (TimePattern tp : patterns) {
			timePatternsByName.put(tp.getName(), tp);
		}
	}

	private void loadExistingDatePatterns(Long sessionId) {
		@SuppressWarnings("unchecked")
		List<DatePattern> patterns = (List<DatePattern>) this.
			getHibSession().
			createQuery("select distinct dp from DatePattern as dp where dp.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list();
		for (DatePattern dp : patterns) {
			datePatternsByName.put(dp.getName(), dp);
		}
	}
	
	private void loadExistingBuildings(Long sessionId) {
		@SuppressWarnings("unchecked")
		List<Building> existingBuildings = (List<Building>) this.
			getHibSession().
			createQuery("select distinct b from Building as b where b.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list();
		for (Building building : existingBuildings) {
			buildingsByAbbv.put(building.getAbbreviation(), building);
		}
	}

	private void loadExistingLocations(Long sessionId) {
		@SuppressWarnings("unchecked")
		List<Room> existingRooms = (List<Room>) this.
			getHibSession().
			createQuery("select distinct r from Room as r where r.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list();
		for (Room room : existingRooms) {
			locationsByName.put((room.getBuildingAbbv() + room.getRoomNumber() + room.getPermanentId().toString()), room);
		}
		@SuppressWarnings("unchecked")
		List<NonUniversityLocation> existingNonUniversityLocation = (List<NonUniversityLocation>) this.
			getHibSession().
			createQuery("select distinct n from NonUniversityLocation as n where n.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list();
		for (NonUniversityLocation nonUniversityLocation : existingNonUniversityLocation) {
			locationsByName.put((nonUniversityLocation.getName() + nonUniversityLocation.getPermanentId().toString()), nonUniversityLocation);
		}
	}
		
		
	private void elementClassInstructor(Element classInstructorElement, PitClass pitClass) throws Exception {
		PitClassInstructor pci = new PitClassInstructor();
		pci.setPitClassInstructing(pitClass);
		pitClass.addTopitClassInstructors(pci);
		
		pci.setPitDepartmentalInstructor(pitDepartmentInstructors.get(new Long(getRequiredStringAttribute(classInstructorElement, PointInTimeDataExport.sDepartmentalInstructorUniqueIdAttribute, PointInTimeDataExport.sClassInstructorElementName))));
		pci.setPercentShare(getRequiredIntegerAttribute(classInstructorElement, PointInTimeDataExport.sShareAttribute, PointInTimeDataExport.sClassInstructorElementName));
		pci.setNormalizedPercentShare(new Integer(0));
		String responsibilityId = getOptionalStringAttribute(classInstructorElement, PointInTimeDataExport.sResponsibilityUniqueIdAttribute);
		if (responsibilityId != null) {
			pci.setResponsibility(teachingResponsibilities.get(new Long(responsibilityId)));
		}
		
		pci.setLead(new Boolean(getOptionalBooleanAttribute(classInstructorElement, PointInTimeDataExport.sLeadAttribute, true)));

		pci.setUniqueId((Long)getHibSession().save(pci));
		
	}
	
	private void elementOfferingCoordinator(Element classInstructorElement, PitInstructionalOffering pitInstructionalOffering) throws Exception {
		PitOfferingCoordinator pci = new PitOfferingCoordinator();
		pci.setPitInstructionalOffering(pitInstructionalOffering);
		pitInstructionalOffering.addTopitOfferingCoordinators(pci);
		
		pci.setPitDepartmentalInstructor(pitDepartmentInstructors.get(new Long(getRequiredStringAttribute(classInstructorElement, PointInTimeDataExport.sDepartmentalInstructorUniqueIdAttribute, PointInTimeDataExport.sClassInstructorElementName))));
		pci.setPercentShare(getRequiredIntegerAttribute(classInstructorElement, PointInTimeDataExport.sShareAttribute, PointInTimeDataExport.sClassInstructorElementName));
		String responsibilityId = getOptionalStringAttribute(classInstructorElement, PointInTimeDataExport.sResponsibilityUniqueIdAttribute);
		if (responsibilityId != null) {
			pci.setResponsibility(teachingResponsibilities.get(new Long(responsibilityId)));
		}
		
		pci.setUniqueId((Long)getHibSession().save(pci));
		
	}
		
	private void elementCourse(Element courseElement, PitInstructionalOffering pitInstructionalOffering) throws Exception{
		if (courseElement.getName().equals(PointInTimeDataExport.sCourseElementName)){
			PitCourseOffering pco = new PitCourseOffering();
			pco.setPitInstructionalOffering(pitInstructionalOffering);
			pitInstructionalOffering.addTopitCourseOfferings(pco);
			
			pco.setSubjectArea(subjectAreas.get(getRequiredStringAttribute(courseElement, PointInTimeDataExport.sSubjectAreaAbbreviationAttribute, PointInTimeDataExport.sCourseElementName)));
			pco.setCourseNbr(getRequiredStringAttribute(courseElement, PointInTimeDataExport.sCourseNbrAttribute, PointInTimeDataExport.sCourseElementName));
			pco.setIsControl(getOptionalBooleanAttribute(courseElement, PointInTimeDataExport.sControllingAttribute, true));
			pco.setPermId(getRequiredStringAttribute(courseElement, PointInTimeDataExport.sPermanentIdAttribute, PointInTimeDataExport.sCourseElementName));
			pco.setExternalUniqueId(getRequiredStringAttribute(courseElement, PointInTimeDataExport.sExternalIdAttribute, PointInTimeDataExport.sCourseElementName));

			Long uid = new Long( getRequiredStringAttribute(courseElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sCourseElementName));
			CourseOffering co = courseOfferings.get(uid);
			if (co != null 
					&& co.getSubjectArea().getUniqueId().equals(pco.getSubjectArea().getUniqueId())
					&& co.getCourseNbr().equals(pco.getCourseNbr())){
				pco.setCourseOffering(co);
			}
			
			String title = getOptionalStringAttribute(courseElement, PointInTimeDataExport.sTitleAttribute);
			if (title != null) {
				pco.setTitle(title);
			}

			Integer projectedDemand = getOptionalIntegerAttribute(courseElement, PointInTimeDataExport.sProjectedDemandAttribute);
			if (projectedDemand != null){
				pco.setProjectedDemand(projectedDemand);
			}
			
			Integer lastlikeDemand = getOptionalIntegerAttribute(courseElement, PointInTimeDataExport.sLastlikeDemandAttribute);
			if (lastlikeDemand != null){
				pco.setDemand(lastlikeDemand);
			}
			
			Integer nbrExpectedStudents = getOptionalIntegerAttribute(courseElement, PointInTimeDataExport.sNumberExpectedStudentsAttribute);
			if (nbrExpectedStudents != null) {
				pco.setNbrExpectedStudents(nbrExpectedStudents);
			}
			
			String courseTypeIdStr = getOptionalStringAttribute(courseElement, PointInTimeDataExport.sCourseTypeIdAttribute);
			if (courseTypeIdStr != null){
				pco.setCourseType(courseTypes.get(new Long(courseTypeIdStr)));
			}
	        String uidRollFwdFrmStr = getOptionalStringAttribute(courseElement, PointInTimeDataExport.sUniqueIdRolledForwardFromAttribute);
	        if (uidRollFwdFrmStr != null){
	        	pco.setUniqueIdRolledForwardFrom(new Long(uidRollFwdFrmStr));
	        }
	        
	        String externalId = getOptionalStringAttribute(courseElement, PointInTimeDataExport.sExternalIdAttribute);
	        if (externalId != null) {
	        	pco.setExternalUniqueId(externalId);
	        }

			pco.setUniqueId((Long)getHibSession().save(pco));
			pitCourseOfferings.put(uid, pco);
		}		
	}

	@SuppressWarnings("unchecked")
	private void elementInstrOffrConfig(Element configElement, PitInstructionalOffering pitInstructionalOffering) throws Exception{
		PitInstrOfferingConfig pioc = new PitInstrOfferingConfig();
		
		InstrOfferingConfig ioc = instrOfferingConfigs.get(new Long( getRequiredStringAttribute(configElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sConfigElementName)));
		if (ioc != null){
			pioc.setInstrOfferingConfig(ioc);
		}
		
		pioc.setName(getRequiredStringAttribute(configElement, PointInTimeDataExport.sNameAttribute,PointInTimeDataExport. sConfigElementName));
		pioc.setPitInstructionalOffering(pitInstructionalOffering);
		pitInstructionalOffering.addTopitInstrOfferingConfigs(pioc);
		pioc.setUnlimitedEnrollment(getRequiredBooleanAttribute(configElement, PointInTimeDataExport.sUnlimitedEnrollmentAttributeName, PointInTimeDataExport.sConfigElementName));
		
		String durationTypeStr = getOptionalStringAttribute(configElement, PointInTimeDataExport.sDurationTypeAttribute);
		if (durationTypeStr != null) {
				pioc.setClassDurationType(classDurationTypes.get(durationTypeStr));
		}
		
		String instructionalMethodStr = getOptionalStringAttribute(configElement, PointInTimeDataExport.sInstructionalMethodAttribute);
		if (instructionalMethodStr != null) {
				pioc.setInstructionalMethod(instructionalMethods.get(instructionalMethodStr));
		}
		
        String uidRollFwdFrmStr = getOptionalStringAttribute(configElement, PointInTimeDataExport.sUniqueIdRolledForwardFromAttribute);
        if (uidRollFwdFrmStr != null){
        	pioc.setUniqueIdRolledForwardFrom(new Long(uidRollFwdFrmStr));
        }

		pioc.setUniqueId((Long) getHibSession().save(pioc));
		for (Element subpartElement : (List<Element>)configElement.elements()){
			elementSubpart(subpartElement, pioc, null);
		}
	}
	
	
	private void elementClass(Element classElement, PitSchedulingSubpart pitSchedulingSubpart) throws Exception {

		PitClass pc = new PitClass();
		Long uid = new Long( getRequiredStringAttribute(classElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sClassElementName));
		Class_ c = classes.get(uid);
		if (c != null){
			pc.setClazz(c);
		}
		pc.setPitSchedulingSubpart(pitSchedulingSubpart);
		pitSchedulingSubpart.addTopitClasses(pc);
		String parentClassUidStr = getOptionalStringAttribute(classElement, PointInTimeDataExport.sParentClassUniqueIdAttribute);
		if (parentClassUidStr != null){
			PitClass parentPitClass = pitClasses.get(new Long(parentClassUidStr));
			pc.setPitParentClass(parentPitClass);
			parentPitClass.addTopitChildClasses(pc);
		}
		
		pc.setDatePattern(datePatterns.get(new Long(getRequiredStringAttribute(classElement, PointInTimeDataExport.sDatePatternUniqueIdAttribute, PointInTimeDataExport.sClassElementName))));
		
		String timePatternIdStr = getOptionalStringAttribute(classElement, PointInTimeDataExport.sTimePatternUniqueIdAttribute);
		if (timePatternIdStr != null) {
			pc.setTimePattern(timePatterns.get(new Long(timePatternIdStr)));
		}
		
		pc.setManagingDept(departments.get(new Long(getRequiredStringAttribute(classElement, PointInTimeDataExport.sManagingDepartmentUniqueIdAttribute, PointInTimeDataExport.sClassElementName))));

		String classSuffix = getOptionalStringAttribute(classElement, PointInTimeDataExport.sClassSuffixAttribute);
		if (classSuffix != null) {
			pc.setClassSuffix(classSuffix);
		}
		
		pc.setSectionNumber(getRequiredIntegerAttribute(classElement, PointInTimeDataExport.sSectionNumberAttribute, PointInTimeDataExport.sClassElementName));
		pc.setEnabledForStudentScheduling(new Boolean(getOptionalBooleanAttribute(classElement, PointInTimeDataExport.sStudentSchedulingAttribute, true)));
		
		String limitStr = getOptionalStringAttribute(classElement, PointInTimeDataExport.sLimitAttribute);
		if (limitStr != null && !limitStr.equalsIgnoreCase("inf")) {
			pc.setLimit(new Integer(limitStr));
		}
		
		Integer numberOfRooms = getOptionalIntegerAttribute(classElement, PointInTimeDataExport.sNumberOfRoomsAttribute);
		if (numberOfRooms != null){
			pc.setNbrRooms(numberOfRooms);
		}
		
        String uidRollFwdFrmStr = getOptionalStringAttribute(classElement, PointInTimeDataExport.sUniqueIdRolledForwardFromAttribute);
        if (uidRollFwdFrmStr != null){
        	pc.setUniqueIdRolledForwardFrom(new Long(uidRollFwdFrmStr));
        }
        
        String externalId = getOptionalStringAttribute(classElement, PointInTimeDataExport.sExternalIdAttribute);
        if (externalId != null) {
        	pc.setExternalUniqueId(externalId);
        }

        pc.setUniqueId((Long)getHibSession().save(pc));
        pitClasses.put(uid, pc);

        @SuppressWarnings("unchecked")
		Iterator<Element> classInstructorElementIt = classElement.elementIterator(PointInTimeDataExport.sClassInstructorElementName);
        while (classInstructorElementIt.hasNext()){
        	Element classInstructorElement = classInstructorElementIt.next();
        	elementClassInstructor(classInstructorElement, pc);
        }
        
        updateNormalizedPercentShare(pc);
        
        @SuppressWarnings("unchecked")
		Iterator<Element> classEventElementIt = classElement.elementIterator(PointInTimeDataExport.sClassEventElementName);
        while (classEventElementIt.hasNext()){
        	Element classEventElement = classEventElementIt.next();
        	elementClassEvent(classEventElement, pc);
        }
	
	}
	
	private void updateNormalizedPercentShare(PitClass pc) {
		ArrayList<PitClassInstructor> ciList = new ArrayList<PitClassInstructor>();
		if (pc.getPitClassInstructors() == null || pc.getPitClassInstructors().isEmpty()){
			return;
		}
		for(PitClassInstructor pci : pc.getPitClassInstructors()){
			if (pci.getResponsibility() == null || !pci.getResponsibility().hasOption(TeachingResponsibility.Option.auxiliary)){
				if (pci.getPercentShare().intValue() > 0) {
					ciList.add(pci);
				}
			}
		}
		if (ciList.size() == 0){
			return;
		}
		if (ciList.size() == 1){
			ciList.get(0).setNormalizedPercentShare(new Integer(100));
		} else {
			int totalShare = 0;
			for (PitClassInstructor pci : ciList){
				if (pci.getPercentShare() != null) {
					totalShare += pci.getPercentShare().intValue();
				}
			}
			int totalNormalizedShare = 0;
			for (PitClassInstructor pci : ciList){
				if (pci.getPercentShare() != null) {
					pci.setNormalizedPercentShare(new Integer(100*pci.getPercentShare().intValue()/totalShare));
					totalNormalizedShare += pci.getNormalizedPercentShare().intValue();
				}
			}
			
			if (totalNormalizedShare != 100){
				int difference = 100 - totalNormalizedShare;
				int checkNormalizedShare = 0;
				int numToAddToAll = difference / ciList.size();
				int remainderToSpreadUntilGone = difference % ciList.size();
				for (PitClassInstructor pci : ciList){
					if (pci.getPercentShare() != null) {
						pci.setNormalizedPercentShare(new Integer(pci.getNormalizedPercentShare().intValue() + numToAddToAll + (remainderToSpreadUntilGone > 0? 1 : (remainderToSpreadUntilGone < 0) ? -1 : 0 )));
						checkNormalizedShare += pci.getNormalizedPercentShare().intValue();
						remainderToSpreadUntilGone += (remainderToSpreadUntilGone > 0 ? -1 : (remainderToSpreadUntilGone < 0 ? 1 : 0));
					}
				}
				if (checkNormalizedShare != 100) {
					Debug.info(pc.getUniqueId().toString() + ":  Normalized percent share for class instructors does not equal 100:  " + checkNormalizedShare);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void elementClassEvent(Element classEventElement, PitClass pc) throws Exception {
		PitClassEvent pce = new PitClassEvent();
		pce.setPitClass(pc);
		pc.addTopitClassEvents(pce);
		pce.setEventName(getRequiredStringAttribute(classEventElement, PointInTimeDataExport.sNameAttribute, PointInTimeDataExport.sClassEventElementName));
		pce.setUniqueId((Long) getHibSession().save(pce));
		for(Element classMeetingElement : (List<Element>) classEventElement.elements()){
			elementClassMeeting(classMeetingElement, pce);
		}
	}

	@SuppressWarnings("unchecked")
	private void elementClassMeeting(Element classMeetingElement, PitClassEvent pce) throws Exception {
		PitClassMeeting pcm = new PitClassMeeting();
		pcm.setPitClassEvent(pce);
		pce.addTopitClassMeetings(pcm);
		pcm.setMeetingDate(CalendarUtils.getDate(getRequiredStringAttribute(classMeetingElement, PointInTimeDataExport.sMeetingDateAttribute, PointInTimeDataExport.sClassMeetingElementName), dateFormat));
		pcm.setStartPeriod(getRequiredIntegerAttribute(classMeetingElement, PointInTimeDataExport.sStartPeriodAttribute, PointInTimeDataExport.sClassMeetingElementName));
		pcm.setStopPeriod(getRequiredIntegerAttribute(classMeetingElement, PointInTimeDataExport.sStopPeriodAttribute, PointInTimeDataExport.sClassMeetingElementName));
		String startOffsetStr = getOptionalStringAttribute(classMeetingElement, PointInTimeDataExport.sStartOffsetAttribute);
		if (startOffsetStr != null) {
			pcm.setStartOffset(new Integer(startOffsetStr));
		}
		String stopOffsetStr = getOptionalStringAttribute(classMeetingElement, PointInTimeDataExport.sStopOffsetAttribute);
		if (stopOffsetStr != null) {
			pcm.setStopOffset(new Integer(stopOffsetStr));
		}
		Location loc = locations.get(new Long(getRequiredStringAttribute(classMeetingElement, PointInTimeDataExport.sLocationUniqueIdAttribute, PointInTimeDataExport.sClassMeetingElementName)));
		pcm.setLocationPermanentId(loc.getPermanentId());
		pcm.setTimePatternMinPerMtg(getRequiredIntegerAttribute(classMeetingElement, PointInTimeDataExport.sTimePatternMinutesPerMeetingAttribute, PointInTimeDataExport.sClassMeetingElementName));
		pcm.setCalculatedMinPerMtg(getRequiredIntegerAttribute(classMeetingElement, PointInTimeDataExport.sCalculatedMinutesPerMeetingAttribute, PointInTimeDataExport.sClassMeetingElementName));
		pcm.setUniqueId((Long) getHibSession().save(pcm));
	
		for(Element classMeetingUtilPeriodElement : (List<Element>) classMeetingElement.elements()){
			elementClassMeetingUtilPeriod(classMeetingUtilPeriodElement, pcm);
		}

	}



	private void elementClassMeetingUtilPeriod(Element classMeetingUtilPeriodElement, PitClassMeeting pitClassMeeting) throws Exception {
		PitClassMeetingUtilPeriod pcmup = new PitClassMeetingUtilPeriod();
		pcmup.setTimeSlot(getRequiredIntegerAttribute(classMeetingUtilPeriodElement, PointInTimeDataExport.sPeriodAttribute, PointInTimeDataExport.sClassMeetingUtilPeriodElementName));
		pcmup.setPitClassMeeting(pitClassMeeting);
		pitClassMeeting.addTopitClassMeetingUtilPeriods(pcmup);
		pcmup.setUniqueId((Long) getHibSession().save(pcmup));
	}

	private void elementSubpart(Element subpartElement, PitInstrOfferingConfig pitInstructionalOfferingConfig, PitSchedulingSubpart parentPitSubpart) throws Exception {
		if (PointInTimeDataExport.sSubpartElementName.equals(subpartElement.getName())) {
			PitSchedulingSubpart pss = new PitSchedulingSubpart();
			SchedulingSubpart ss = schedulingSubparts.get(new Long( getRequiredStringAttribute(subpartElement, PointInTimeDataExport.sUniqueIdAttribute, PointInTimeDataExport.sSubpartElementName)));
			if (ss != null){
				pss.setSchedulingSubpart(ss);
			}
			if (parentPitSubpart != null){
				pss.setPitParentSubpart(parentPitSubpart);
				parentPitSubpart.addTopitChildSubparts(pss);
			}
			pss.setPitInstrOfferingConfig(pitInstructionalOfferingConfig);
			pitInstructionalOfferingConfig.addToschedulingSubparts(pss);
			pss.setMinutesPerWk(getRequiredIntegerAttribute(subpartElement, PointInTimeDataExport.sMinPerWeekAttribute, PointInTimeDataExport.sSubpartElementName));
			pss.setSchedulingSubpartSuffixCache(getRequiredStringAttribute(subpartElement, PointInTimeDataExport.sSuffixAttribute, PointInTimeDataExport.sSubpartElementName));
			pss.setItype(itypes.get(getRequiredStringAttribute(subpartElement, PointInTimeDataExport.sTypeAttribute, PointInTimeDataExport.sSubpartElementName)));
			pss.setStudentAllowOverlap(getRequiredBooleanAttribute(subpartElement, PointInTimeDataExport.sStudentAllowOverlapAttribute, PointInTimeDataExport.sSubpartElementName));
			String creditTypeStr = getOptionalStringAttribute(subpartElement, PointInTimeDataExport.sCreditTypeAttribute);
			if (creditTypeStr != null) {
				pss.setCreditType(creditTypes.get(new Integer(creditTypeStr)));
			}
			String creditUnitTypeStr = getOptionalStringAttribute(subpartElement, PointInTimeDataExport.sCreditUnitTypeAttribute);
			if (creditUnitTypeStr != null) {
				pss.setCreditUnitType(creditUnitTypes.get(new Integer(creditUnitTypeStr)));
			}
			String creditStr = getOptionalStringAttribute(subpartElement, PointInTimeDataExport.sCreditAttribute);
			if (creditStr != null){
				pss.setCredit(new Float(creditStr));
			}
	        String uidRollFwdFrmStr = getOptionalStringAttribute(subpartElement, PointInTimeDataExport.sUniqueIdRolledForwardFromAttribute);
	        if (uidRollFwdFrmStr != null){
	        	pss.setUniqueIdRolledForwardFrom(new Long(uidRollFwdFrmStr));
	        }
	        pss.setUniqueId((Long)getHibSession().save(pss));
	        
	        @SuppressWarnings("unchecked")
			Iterator<Element> classElementIterator = (Iterator<Element>)subpartElement.elementIterator(PointInTimeDataExport.sClassElementName);
	        while(classElementIterator.hasNext()){
	        	Element classElement = classElementIterator.next();
	        	elementClass(classElement, pss);
	        }

	        @SuppressWarnings("unchecked")
			Iterator<Element> childSubpartElementIterator = (Iterator<Element>)subpartElement.elementIterator(PointInTimeDataExport.sSubpartElementName);
	        while(childSubpartElementIterator.hasNext()){
	        	Element childSubpartElement = childSubpartElementIterator.next();
	        	elementSubpart(childSubpartElement, pitInstructionalOfferingConfig, pss);
	        }

		}
	}

		
	private void loadExistingSubjectAreas(Long sessionId) {
		List<?> subjects = new ArrayList<Object>();
		subjects = this.
			getHibSession().
			createQuery("select distinct sa from SubjectArea as sa where sa.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list();
		for (Iterator<?> it = subjects.iterator(); it.hasNext();) {
			SubjectArea sa = (SubjectArea) it.next();
			subjectAreas.put(sa.getSubjectAreaAbbreviation(), sa);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadDepartmentalInstructors(Long sessionId) {
		for (DepartmentalInstructor di : (List<DepartmentalInstructor>)this.
				getHibSession().
				createQuery("select distinct di from DepartmentalInstructor as di where di.department.session.uniqueId=:sessionId").
				setLong("sessionId", sessionId.longValue()).
				setCacheable(true).
				list()) {
			departmentalInstructorsByName.put(
					((di.getLastName() == null? "":di.getLastName())
					+ (di.getFirstName() == null? "" : di.getFirstName())
					+ (di.getMiddleName() == null? "": di.getMiddleName())
					+ (di.getExternalUniqueId() == null? "" : di.getExternalUniqueId())), di);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExistingDepartments(Long sessionId) {
		for (Department department : (List<Department>)this.
			getHibSession().
			createQuery("select distinct d from Department as d where d.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list()) {
			departmentsByCode.put(department.getDeptCode(), department);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadExistingMinors(Long sessionId) {
		for (PosMinor minor : (List<PosMinor>)this.
			getHibSession().
			createQuery("select distinct m from PosMinor as m where m.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list()) {
			minorsByCode.put(minor.getCode(), minor);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExistingMajors(Long sessionId) {
		for (PosMajor major : (List<PosMajor>)this.
			getHibSession().
			createQuery("select distinct m from PosMajor as m where m.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list()) {
			majorsByCode.put(major.getCode(), major);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExistingAcademicClassifications(Long sessionId) {
		for (AcademicClassification ac : (List<AcademicClassification>)this.
			getHibSession().
			createQuery("select distinct ac from AcademicClassification as ac where ac.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list()) {
			academicClassificationsByCode.put(ac.getCode(), ac);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExistingAcademicAreas(Long sessionId) {
		for (AcademicArea aa : (List<AcademicArea>)this.
			getHibSession().
			createQuery("select distinct ac from AcademicArea as ac where ac.session.uniqueId=:sessionId").
			setLong("sessionId", sessionId.longValue()).
			setCacheable(true).
			list()) {
			academicAreasByAbbv.put(aa.getAcademicAreaAbbreviation(), aa);
		}
	}


	private void loadItypes() {
		Set<?> itypeDescs = ItypeDesc.findAll(false);
		for (Iterator<?> it = itypeDescs.iterator(); it.hasNext();) {
			ItypeDesc itype = (ItypeDesc) it.next();
			itypes.put(itype.getAbbv().trim(), itype);
		}
	}
		
	@SuppressWarnings("unchecked")
	private void loadExistingInstructionalOfferings(Long sessionId) throws Exception {
		
		for (InstructionalOffering io : (List<InstructionalOffering>)InstructionalOffering.findAll(sessionId)) {
			instructionalOfferings.put(io.getUniqueId(), io);			
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExistingCourseOfferings(Long sessionId) throws Exception {
		
		for (CourseOffering courseOffering : (List<CourseOffering>)CourseOffering.findAll(sessionId)) {
			courseOfferings.put(courseOffering.getUniqueId(), courseOffering);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExistingInstrOfferingConfigs(Long sessionId) throws Exception {
		String qs = "from InstrOfferingConfig ioc where ioc.instructionalOffering.session.uniqueId = :sessionId";
		
		for (InstrOfferingConfig instrOfferingConfig : (List<InstrOfferingConfig>)getHibSession().createQuery(qs).setLong("sessionId", sessionId).list()) {
			instrOfferingConfigs.put(instrOfferingConfig.getUniqueId(), instrOfferingConfig);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadExistingSchedulingSubparts(Long sessionId) throws Exception {
		String qs = "from SchedulingSubpart ss where ss.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId";
		
		for (SchedulingSubpart schedulingSubpart : (List<SchedulingSubpart>)getHibSession().createQuery(qs).setLong("sessionId", sessionId).list()) {
			schedulingSubparts.put(schedulingSubpart.getUniqueId(), schedulingSubpart);
		}
	}

	
	@SuppressWarnings("unchecked")
	private void loadExistingClasses(Long sessionId) throws Exception {
 		for (Class_ c : (List<Class_>) Class_.findAll(sessionId)) {
			classes.put(c.getUniqueId(), c);
		}
	}

	@Override
	protected String getEmailSubject() {
		return("Point In Time Data Import Results - " + session.getAcademicYearTerm());
	}

	
    protected static boolean equals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }
		
	
	
	
	public Integer str2Slot(String timeString, Integer printOffset) throws Exception {
		int slot = -1;
		try {
			Date date = CalendarUtils.getDate(timeString, timeFormat);
			SimpleDateFormat df = new SimpleDateFormat("HHmm");
			int time = Integer.parseInt(df.format(date));
			if (printOffset != null)
				time -= printOffset.intValue();
			int hour = time/100;
			int min = time%100;
			if (hour >= 24)
				throw new Exception("Invalid time '"+timeString+"' -- hour ("+hour+") must be between 0 and 23.");
			if (min >= 60)
				throw new Exception("Invalid time '"+timeString+"' -- minute ("+min+") must be between 0 and 59.");
			
			if ((min % Constants.SLOT_LENGTH_MIN) != 0){
				min = min - (min % Constants.SLOT_LENGTH_MIN);
			}
			slot = (hour * 60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
		} catch (NumberFormatException ex) {
			throw new Exception("Invalid time '"+timeString+"' -- not a number.");
		}
		if (slot < 0)
			throw new Exception("Invalid time '"+timeString+"', did not meet format: " + timeFormat);
		return slot;
	}
	
}
