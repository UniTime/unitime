/* 
 * UniTime 3.1 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.dom4j.Element;
import org.hibernate.FlushMode;
import org.hibernate.engine.spi.SessionImplementor;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.test.MakeAssignmentsForClassEvents;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.InstrOfferingPermIdGenerator;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public abstract class BaseCourseOfferingImport extends EventRelatedImports {

	HashSet<Long> existingInstructionalOfferings = new HashSet<Long>();
	HashSet<Long> existingCourseOfferings = new HashSet<Long>();
	HashSet<Long> existingClasses = new HashSet<Long>();
	HashMap<String, SubjectArea> subjectAreas = new HashMap<String, SubjectArea>();
	HashMap<String, TimePattern> timePatterns = new HashMap<String, TimePattern>();
	HashMap<String, ItypeDesc> itypes = new HashMap<String, ItypeDesc>();
	HashMap<String, ItypeDesc> itypesBySisRef = new HashMap<String, ItypeDesc>();
	protected DistributionType meetsWithType = null;
	protected DistributionType canShareRoomType = null;
	boolean useMeetsWithElement = false;
	boolean useCanShareRoomElement = false;
	boolean incremental = false;
	PreferenceLevel requiredPrefLevel = null;
	MakeAssignmentsForClassEvents assignmentHelper = null;
	protected String rootElementName;
	int changeCount;
	protected boolean courseNumbersMustBeUnique;

	public BaseCourseOfferingImport() {
		super();
		changeCount = 0;
		
		courseNumbersMustBeUnique = ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue();

	}

	public void loadXml(Element rootElement) throws Exception {
		initializeTrimLeadingZeros(); 

		try {
	        if (!rootElement.getName().equalsIgnoreCase(rootElementName)) {
	        	throw new Exception("Given XML file is not a Course Offerings load file.");
	        }
	        beginTransaction();
	        
	        incremental = "true".equalsIgnoreCase(rootElement.attributeValue("incremental", "false"));
	        if (incremental)
	        	info("Incremental mode.");

	        initializeLoad(rootElement, rootElementName);
			preLoadAction();
	        loadOfferings(rootElement);
	        
	        if (!incremental) {
		        deleteUnmatchedInstructionalOfferings();
		        deleteUnmatchedCourseOfferings();
	        }
	        deleteUnmatchedClasses();
	        commitTransaction();
	        
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		} finally {
			postLoadAction();
		}
		addNote("Records Changed: " + changeCount);
		updateChangeList(true);
		reportMissingLocations();
		mailLoadResults();
	}
	

	// If a setup action needs to take place before the data is loaded override this method
	protected abstract void preLoadAction();

	// If a post load action needs to take place before the data is loaded override this method
	protected abstract void postLoadAction();

	protected void loadOfferings(Element rootElement) throws Exception{    
			for ( Iterator<?> it = rootElement.elementIterator(); it.hasNext(); ) {
	    		Element element = (Element) it.next();
	    		try {
	            elementOffering(element);	             
	            flush(true);
	    		} catch (Exception e) {
	    			addNote("Not Loading 'offering' Error:  " + e.getMessage());
	    			e.printStackTrace();
	    			addNote("\t " + element.asXML());
	    			updateChangeList(true);
	    		}	
	    	}
 	}
	
	protected void elementOffering(Element element) throws Exception{
		String offeringElementName = "offering";

        if(!element.getName().equalsIgnoreCase(offeringElementName)){
        	throw new Exception("Expecting to find an '" + offeringElementName + "' at this level, instead found '" + element.getName() + "'.");
        }
        
        String externalId = getOptionalStringAttribute(element, "id");
        String action = getOptionalStringAttribute(element, "action");
        
        InstructionalOffering io = null;
        boolean existingIo = false;
        if (externalId != null){
        	io = findInstrOffrForExternalId(externalId, session.getUniqueId());
	       	if (io != null){
	        		existingIo = true;
	        	}
        }
        if (io == null) {
        	ArrayList<ImportCourseOffering> courses = getCourses(element);
        	if (courses == null || courses.isEmpty()){
        		throw new Exception("Expected an 'offering' to have at least one course.");
        	}
        	if(courses.size() == 1){
        		ImportCourseOffering ico = (ImportCourseOffering) courses.get(0);
        		CourseOffering co = ico.getCourseOffering();
        		CourseOffering existingCourseOffering = findExistingCourseOffering(co, session.getUniqueId());
        		if(existingCourseOffering == null){
        			io = new InstructionalOffering();
	            	io.setSession(session);
	            	io.setExternalUniqueId(externalId);
	            	io.setByReservationOnly(false);
        		} else {
        			io = existingCourseOffering.getInstructionalOffering();
        			existingIo = true;
        		}
        	} else {
        		HashSet<InstructionalOffering> possibleOfferings = new HashSet<InstructionalOffering>();
        		for(Iterator<ImportCourseOffering> coIt = courses.iterator(); coIt.hasNext();){
        			ImportCourseOffering ico = (ImportCourseOffering) coIt.next();
        			CourseOffering co = ico.getCourseOffering() ;
        			CourseOffering existingCourseOffering = findExistingCourseOffering(co, session.getUniqueId());
            		if (existingCourseOffering != null){
            			possibleOfferings.add(existingCourseOffering.getInstructionalOffering());
            		}
        		}
        		if (possibleOfferings.isEmpty()){
        			io = new InstructionalOffering();
	            	io.setSession(session);
	            	io.setExternalUniqueId(externalId);
	            	io.setByReservationOnly(false);
        		} else if (possibleOfferings.size() == 1){
        			io = (InstructionalOffering) possibleOfferings.iterator().next();
        			existingIo = true;
        		} else {
        			CourseOffering control = null;
        			for(Iterator<ImportCourseOffering> coIt = courses.iterator(); coIt.hasNext(); ){
        				ImportCourseOffering ico = (ImportCourseOffering) coIt.next();
        				CourseOffering co = ico.getCourseOffering();	            				
        				if(co.isIsControl().booleanValue()){
        					control = co;
        					break;
        				}
        			}
        			if (control == null ){
        				throw new Exception("Expected an 'offering' to have a controlling course.");
        			}
        			InstructionalOffering offeringForControllingCourse = null;
        			for(Iterator<InstructionalOffering> ioIt = possibleOfferings.iterator(); ioIt.hasNext(); ){
        				InstructionalOffering possibleIntructionalOffering = (InstructionalOffering)ioIt.next();
        				if (isSameCourseOffering(possibleIntructionalOffering.getControllingCourseOffering(), control)){
        					offeringForControllingCourse = possibleIntructionalOffering;
        					break;
        				} 
        			}
        			if (offeringForControllingCourse != null){
        				io = offeringForControllingCourse;
        				possibleOfferings.remove(io);
        				existingIo = true;
        				for(Iterator<InstructionalOffering> ioIt = possibleOfferings.iterator(); ioIt.hasNext(); ){
            				InstructionalOffering oldIo = (InstructionalOffering)ioIt.next();
            				deleteInstructionalOffering(oldIo);
            			}
        			} else {
        				io = new InstructionalOffering();
    	            	io.setSession(session);
    	            	io.setExternalUniqueId(externalId);
    	            	io.setByReservationOnly(false);
    	            	for(Iterator<InstructionalOffering> ioIt = possibleOfferings.iterator(); ioIt.hasNext(); ){
            				InstructionalOffering oldIo = (InstructionalOffering)ioIt.next();
            				deleteInstructionalOffering(oldIo);
            			}
        			}
        		}
        	}
        }
		if (externalId != null && io.getExternalUniqueId() != null && !io.getExternalUniqueId().equals(externalId)){
			existingIo = false;
			deleteInstructionalOffering(io);
			io = new InstructionalOffering();
        	io.setSession(session);
        	io.setExternalUniqueId(externalId);
        	io.setByReservationOnly(false);
		}

        if (existingIo && action != null && action.equalsIgnoreCase("delete")){
        	addNote("Deleted instructional offering: " + io.getCourseName());
        	deleteInstructionalOffering(io);
        	changeCount++;
        } else if (!existingIo && action != null && action.equalsIgnoreCase("delete")){
        	return;
        } else if (io != null) {
        	if (existingIo) {
        		if(!existingInstructionalOfferings.remove(io.getUniqueId())){
    				throw new Exception("could not remove io uniqueid from existing");
    			}
        		action = "update";
        		addNote("Changes for instructional offering: " + io.getCourseName());
        	} else {
        		action = "insert";
        	}
        	if(doAddUpdate(element, io, action) || action.equalsIgnoreCase("insert")){
        		changeCount++;
        		if (action.equals("insert")){
        			clearNotes();
        			addNote("Added instructional offering: " + io.getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation() + " " + io.getControllingCourseOffering().getCourseNbr());
        		} 
        		updateChangeList(true);
        	} else {
        		updateChangeList(false);
        	}
        } else {
        	return;
        }
     
	}
	
	protected void deleteUnmatchedInstructionalOfferings(){
        if (existingInstructionalOfferings.size() > 0){
        	HashSet<Long> deleteOfferings = new HashSet<Long>();
        	deleteOfferings.addAll(existingInstructionalOfferings);
        	addNote("Deleted Instructional Offerings that were not in the input file:");
        	for(Iterator<Long> ioIt = deleteOfferings.iterator(); ioIt.hasNext(); ){
	        		Long uniqueId = (Long) ioIt.next();
		        	InstructionalOffering unusedIo = findInstrOffrForUniqueId(uniqueId) ; 
		        	if (unusedIo != null){
			        	addNote("\tdeleted: " + unusedIo.getCourseName());
		        		deleteInstructionalOffering(unusedIo);
		        		changeCount++;
		        	}
	        	}
        	flushIfNeeded(true);
        	updateChangeList(true);
        }

	}
	
	protected void deleteUnmatchedCourseOfferings(){
        if (existingCourseOfferings.size() > 0){
        	HashSet<Long> deleteOfferings = new HashSet<Long>();
        	deleteOfferings.addAll(existingCourseOfferings);
        	addNote("Deleted Course Offerings that were not in the input file:");
        	for(Iterator<Long> coIt = deleteOfferings.iterator(); coIt.hasNext(); ){
        		Long uniqueId = (Long) coIt.next();
	        	CourseOffering unusedCo = findCourseOffrForUniqueId(uniqueId) ; 
	        	if (unusedCo != null) {
	        		addNote("\tDeleted: " + unusedCo.getCourseName());
	        		deleteCourseOffering(unusedCo);
	        		changeCount++;
	        	}
        	}
        	flushIfNeeded(true);
        	updateChangeList(true);
        }
	}
	
	protected void deleteUnmatchedClasses(){
        if (existingClasses.size() > 0){
           	HashSet<Long> deleteClasses = new HashSet<Long>();
        	deleteClasses.addAll(existingClasses);
           	addNote("Deleted Classes that were not in the input file:");
    	for(Iterator<Long> cIt = deleteClasses.iterator(); cIt.hasNext(); ){
        		Long uniqueId = (Long) cIt.next();
	        	Class_ unusedC = findClassForUniqueId(uniqueId);
	        	if (unusedC != null){
	        		if (incremental) {
	        			try {
		        			if (existingInstructionalOfferings.contains(unusedC.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId()) ||
		        				existingCourseOfferings.contains(unusedC.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering().getUniqueId()))
		        				continue;
	        			} catch (NullPointerException e) {}
	        		}
	        		addNote("\tDeleted: " + unusedC.getClassLabel());
		        	deleteClass(unusedC);
		        	changeCount++;
	        	}
        	}
        	flushIfNeeded(true);
        	updateChangeList(true);
        }
	}
	
	protected void initializeLoad(Element rootElement, String rootElementName) throws Exception{
        initializeDateTimeFormats(rootElement);
        initializeSessionData(rootElement, rootElementName);
        initializeMeetsWith(rootElement);
        initializeCanShareRoom(rootElement);
        initializeAssignmentHelper();
        loadSetupData();
        logXmlFileCreateInformation(rootElement);		
	}
	
	protected void logXmlFileCreateInformation(Element rootElement) {
        String created = getOptionalStringAttribute(rootElement, "created");
        if (created != null) {
	        addNote("Loading offerings XML file created on: " + created);
			ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.UPDATE, null, null);
			updateChangeList(true);
        }		
	}

	protected void initializeAssignmentHelper() {
        assignmentHelper = new MakeAssignmentsForClassEvents(session, getHibSession());	
	}

	protected void initializeMeetsWith(Element rootElement) {
        Boolean useMeetsWith = getOptionalBooleanAttribute(rootElement, "useMeetsWith");
        if (useMeetsWith != null && useMeetsWith.booleanValue()){
        	useMeetsWithElement = true;
        }		
	}
	protected void initializeCanShareRoom(Element rootElement) {
        Boolean useCanShareRoom = getOptionalBooleanAttribute(rootElement, "useCanShareRoom");
        if (useCanShareRoom != null && useCanShareRoom.booleanValue()){
        	useCanShareRoomElement = true;
        }		
	}

	protected void initializeSessionData(Element rootElement, String rootElementName) throws Exception {
        String campus = getRequiredStringAttribute(rootElement, "campus", rootElementName);
        String year   = getRequiredStringAttribute(rootElement, "year", rootElementName);
        String term   = getRequiredStringAttribute(rootElement, "term", rootElementName);
        session = findSession(campus, year, term);
        if(session == null) {
           	throw new Exception("No session found for the given campus, year, and term.");
        }
	}

	protected void initializeDateTimeFormats(Element rootElement) {
        dateFormat = getOptionalStringAttribute(rootElement, "dateFormat");
        timeFormat = getOptionalStringAttribute(rootElement, "timeFormat");
        if(timeFormat == null){
        	timeFormat = "HHmm";
        }		
	}

	protected void initializeTrimLeadingZeros() {
		trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();
	}

	protected void loadSetupData() throws Exception{
        loadItypes();
        loadSubjectAreas(session.getUniqueId());
        loadTimePatterns(session.getUniqueId());
        loadExistingInstructionalOfferings(session.getUniqueId());
        loadExistingCourseOfferings(session.getUniqueId());
        loadExistingClasses(session.getUniqueId());
        loadRequiredPrefLevel();
        loadMeetsWithDistributionType();
        loadCanShareRoomDistributionType();

	}
	private void loadTimePatterns(Long sessionId) {
		List<?> patterns = new ArrayList<Object>();
		patterns = this.
			getHibSession().
			createQuery("select distinct tp from TimePattern as tp where tp.session.uniqueId=:sessionId and ( tp.type = :standard or tp.type = :evening )").
			setLong("sessionId", sessionId.longValue()).
			setInteger("standard", TimePattern.sTypeStandard).
			setInteger("evening", TimePattern.sTypeEvening).
			setCacheable(true).
			list();
		for (Iterator<?> tpIt = patterns.iterator(); tpIt.hasNext();) {
			TimePattern tp = (TimePattern) tpIt.next();
			for (Iterator<?> dIt = tp.getDays().iterator(); dIt.hasNext();){
				TimePatternDays tpd = (TimePatternDays) dIt.next();
				for (Iterator<?> timesIt = tp.getTimes().iterator(); timesIt.hasNext();){
					TimePatternTime tpt = (TimePatternTime) timesIt.next();
					timePatterns.put((tpd.getDayCode().toString()+"x"+tp.getMinPerMtg().toString()+"x"+tpt.getStartSlot()), tp);
				}
			}
		}
	}

	private boolean isSameCourseOffering(CourseOffering originalCourseOffering, CourseOffering newCourseOffering){
		boolean isSame = false;
		if(originalCourseOffering.getExternalUniqueId() != null 
				&& originalCourseOffering.getExternalUniqueId().equals(newCourseOffering.getExternalUniqueId())){
			isSame = true;
		} else { 
			if (courseNumbersMustBeUnique){
				if (originalCourseOffering.getSubjectArea().getUniqueId().equals(newCourseOffering.getSubjectArea().getUniqueId())
					&& originalCourseOffering.getCourseNbr().equals(newCourseOffering.getCourseNbr())){
					isSame = true;
				}
			} else {
				if (originalCourseOffering.getSubjectArea().getUniqueId().equals(newCourseOffering.getSubjectArea().getUniqueId())
						&& originalCourseOffering.getCourseNbr().equals(newCourseOffering.getCourseNbr())
						&& originalCourseOffering.getTitle().equals(newCourseOffering.getTitle())){
						isSame = true;
				}
			}
		}
		return (isSame);
	}
	
	
	private boolean isSameCreditConfig(CourseCreditUnitConfig originalCourseCreditUnitConfig, CourseCreditUnitConfig courseCreditUnitConfig){
		boolean different = false;
		if (originalCourseCreditUnitConfig.getCreditFormat() == null || courseCreditUnitConfig.getCreditFormat() == null || (originalCourseCreditUnitConfig.getCreditFormat().equals(courseCreditUnitConfig.getCreditFormat()))) {
        	if (!originalCourseCreditUnitConfig.getCreditType().getUniqueId().equals(courseCreditUnitConfig.getCreditType().getUniqueId())){
        		different = true;
        	} else if (!originalCourseCreditUnitConfig.getCreditUnitType().getUniqueId().equals(courseCreditUnitConfig.getCreditUnitType().getUniqueId())){
        		different = true;
        	} else {
        		if (originalCourseCreditUnitConfig instanceof FixedCreditUnitConfig) {
					FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) originalCourseCreditUnitConfig;
					FixedCreditUnitConfig newFcuc = (FixedCreditUnitConfig) courseCreditUnitConfig;
					if (!fcuc.getFixedUnits().equals(newFcuc.getFixedUnits())){
						different = true;
					}
				} else if (originalCourseCreditUnitConfig instanceof VariableRangeCreditUnitConfig) {
					VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) originalCourseCreditUnitConfig;
					VariableRangeCreditUnitConfig newVrcuc = (VariableRangeCreditUnitConfig) courseCreditUnitConfig;
					if (!vrcuc.getMinUnits().equals(newVrcuc.getMinUnits())){
						different = true;
					} else if (!vrcuc.getMaxUnits().equals(newVrcuc.getMaxUnits())){
						different = true;
					} else if (!vrcuc.isFractionalIncrementsAllowed().equals(newVrcuc.isFractionalIncrementsAllowed())){
						different = true;
					}
				} else if (originalCourseCreditUnitConfig instanceof VariableFixedCreditUnitConfig) {
					VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) originalCourseCreditUnitConfig;
					VariableFixedCreditUnitConfig newVfcuc = (VariableFixedCreditUnitConfig) courseCreditUnitConfig;
					if (!vfcuc.getMinUnits().equals(newVfcuc.getMinUnits())){
						different = true;
					} else if (!vfcuc.getMaxUnits().equals(newVfcuc.getMaxUnits())){
						different = true;
					}
				}
        	}
        	
        } else {	        	
        	different = true;
        }
		if (different){
			return(false);
		} else {
			return(true);
		}

	}

	private boolean isSameMeeting(Meeting originalMeeting, Meeting newMeeting){
		boolean isSame = false;
		if(getDateString(originalMeeting.getMeetingDate()).equals(getDateString(newMeeting.getMeetingDate()))
				&& ((originalMeeting.getLocationPermanentId() != null && newMeeting.getLocationPermanentId() != null
				        && originalMeeting.getLocationPermanentId().longValue() == newMeeting.getLocationPermanentId().longValue())
						|| (originalMeeting.getLocationPermanentId() == null && newMeeting.getLocationPermanentId() == null))
				&& originalMeeting.getStartOffset().intValue() == newMeeting.getStartOffset().intValue()
				&& originalMeeting.getStartPeriod().intValue() == newMeeting.getStartPeriod().intValue()
				&& originalMeeting.getStopOffset().intValue() == newMeeting.getStopOffset().intValue()
				&& originalMeeting.getStopPeriod().intValue() == newMeeting.getStopPeriod().intValue()){			
			isSame = true;
		}
		return (isSame);
	}

	private boolean doAddUpdate(Element element, InstructionalOffering io, String action) throws Exception {
		boolean changed = false;
		
		if (io.getInstrOfferingPermId() == null){
			io.generateInstrOfferingPermId();
		}
       
        Boolean offered = getRequiredBooleanAttribute(element, "offered", "offered");
        if (io.isNotOffered() == null || io.isNotOffered().equals(offered)){
         	io.setNotOffered(new Boolean(!offered.booleanValue()));
         	addNote("\toffered status changed");
         	changed = true;
        }

		if (elementCourse(element, io, action)){
         	addNote("\tcourses changed");
			changed = true;
		}

		if (changed){
			this.getHibSession().saveOrUpdate(io);
		}
		
		if (elementInstrOffrConfig(element, io)){
			changed = true;
		}
		
		if (changed){
			this.getHibSession().saveOrUpdate(io);
			ChangeLog.addChange(getHibSession(), getManager(), session, io, ChangeLog.Source.DATA_IMPORT_OFFERINGS, (action.equalsIgnoreCase("insert")?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), io.getControllingCourseOffering().getSubjectArea(), io.getDepartment());
		}
		
		return(changed);
	}
	

	private DepartmentalInstructor findDepartmentalInstructorWithExternalUniqueId(String externalId, Department department){		
		return(DepartmentalInstructor) this.
		getHibSession().
		createQuery("select distinct di from DepartmentalInstructor di where di.externalUniqueId=:externalId and di.department.uniqueId=:departmentId").
		setString("externalId", externalId).
		setLong("departmentId", department.getUniqueId()).
		setCacheable(true).
		uniqueResult();
	}
	
	private String createPatternString(Vector<Calendar> startDates, Vector<Calendar> endDates){
		Iterator<Calendar> startDateIt = startDates.iterator();
		Iterator<Calendar> endDateIt = endDates.iterator();
				
		StringBuffer patternString = new StringBuffer();
		Calendar lastDate = null;
		while(startDateIt.hasNext() && endDateIt.hasNext()){
			Calendar startDate = Calendar.getInstance();
			startDate.setTime(((Calendar) startDateIt.next()).getTime());
			
			Calendar endDate = Calendar.getInstance();
			endDate.setTime(((Calendar) endDateIt.next()).getTime());
			

			if (lastDate != null){
				lastDate.add(Calendar.DAY_OF_MONTH, 1);
				while(getCalendarDateString(lastDate).compareTo(getCalendarDateString(startDate)) < 0){
					patternString.append("0");
					lastDate.add(Calendar.DAY_OF_MONTH, 1);
				}
			} 
			lastDate = endDate;
			
			while(getCalendarDateString(startDate).compareTo(getCalendarDateString(endDate)) <= 0) {
				patternString.append("1");
				startDate.add(Calendar.DAY_OF_MONTH, 1);				
			}
		}
		return(patternString.toString());
	}
	
	private DatePattern findDatePattern(Vector<Calendar> startDates, Vector<Calendar> endDates, Class_ c){
		//Calculate offset from start of session
		Calendar firstDate = Calendar.getInstance();
		firstDate.setTime(((Calendar)startDates.firstElement()).getTime());
		Calendar sessionStartDate = Calendar.getInstance();
		sessionStartDate.setTime(session.getSessionBeginDateTime());
		int offset = 0;
		if (getCalendarDateString(firstDate).compareTo(getCalendarDateString(sessionStartDate)) < 0){
			while (getCalendarDateString(firstDate).compareTo(getCalendarDateString(sessionStartDate)) < 0){
				offset++;
				firstDate.add(Calendar.DAY_OF_MONTH, 1);
			}
		} else if (getCalendarDateString(firstDate).compareTo(getCalendarDateString(sessionStartDate)) > 0){
			while (getCalendarDateString(firstDate).compareTo(getCalendarDateString(sessionStartDate)) > 0){
				offset--;
				firstDate.add(Calendar.DAY_OF_MONTH, -1);
			}			
		}
		String pattern = createPatternString(startDates, endDates);
		DatePattern dp = null;
		
		List patterns = this.
		getHibSession().
		createQuery("from DatePattern as d where d.session.uniqueId = :sessionId and d.pattern = :pattern and d.offset = :offset and d.type = (select min(dd.type) from DatePattern as dd where dd.session.uniqueId = :sessionId and dd.pattern = :pattern and dd.offset = :offset)").
		setLong("sessionId", session.getUniqueId().longValue()).
		setString("pattern", pattern).
		setInteger("offset", offset).
		setCacheable(true).
		list();
		
		if (!patterns.isEmpty()) {
			dp = (DatePattern)patterns.get(0);
		} else {
			dp = new DatePattern();
			dp.setName("import - " + c.getClassLabel());
			dp.setPattern(pattern);
			dp.setOffset(new Integer(offset));			
			dp.setSession(session);
			dp.setType(new Integer(3));
			dp.setVisible(new Boolean(false));
			this.getHibSession().save(dp);
			this.getHibSession().flush();
			this.getHibSession().refresh(dp);
			ChangeLog.addChange(getHibSession(), getManager(), session, dp, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.CREATE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
		}
		if (dp.isDefault()){
			return(null);
		} else {
			return(dp);
		}
		
	}

	protected ArrayList<ImportCourseOffering> getCourses(Element element) throws Exception {
		ArrayList<ImportCourseOffering> courses = new ArrayList<ImportCourseOffering>();
		String elementName = "course";
		if(element.element(elementName) != null) {
			for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element courseElement = (Element) it.next();
				
				String externalUid = getOptionalStringAttribute(courseElement, "id");
				Boolean controlling  = getRequiredBooleanAttribute(courseElement, "controlling", elementName);
				String courseNbr = getRequiredStringAttribute(courseElement, "courseNbr", elementName);
				String scheduleBookNote = getOptionalStringAttribute(courseElement, "scheduleBookNote");
				String subjAbbv = getRequiredStringAttribute(courseElement, "subject", elementName);
				SubjectArea subjectArea = subjectAreas.get(subjAbbv);
				String title = getOptionalStringAttribute(courseElement, "title");
				CourseOffering newCourseOffering = new CourseOffering();
				newCourseOffering.setSubjectArea(subjectArea);
				if (courseNbr != null && courseNbr.trim().length() > 0){
					newCourseOffering.setCourseNbr(courseNbr.trim());
				}
				if(externalUid != null && externalUid.trim().length() > 0){
					newCourseOffering.setExternalUniqueId(externalUid.trim());
				}
				newCourseOffering.setIsControl(controlling);
				if (scheduleBookNote != null && scheduleBookNote.trim().length() > 0){
					newCourseOffering.setScheduleBookNote(scheduleBookNote.trim());
				}
				if (title != null && title.trim().length() > 0){
					newCourseOffering.setTitle(title.trim());
				}
				
				Element consentElement = courseElement.element("consent");
				if (consentElement == null)
					consentElement = element.element("consent");
				if (consentElement != null) { // no consent on course -- try to take consent from the parent (offering)
					String consentType = getRequiredStringAttribute(consentElement, "type", "consent");
					newCourseOffering.setConsentType(OfferingConsentType.getOfferingConsentTypeForReference(consentType));
				}
				
				elementCourseCredit(courseElement, newCourseOffering);
				
				ImportCourseOffering importcourseOffering = new ImportCourseOffering(newCourseOffering, courseElement);
				courses.add(importcourseOffering);
			}
		} else {
			throw new Exception("'course' element is required.");
		}

		return(courses);
	}
	
	private String getDateString(Date date){
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		return(df.format(date));	
	}
	private String getCalendarDateString(Calendar calendar){
		return(getDateString(calendar.getTime()));
	}

	private Calendar getCalendarForDate(String date){
		if(date.length() < 3 || date.indexOf("/") < 0){
			return(null);
		}
		Calendar cal = Calendar.getInstance();
		int index1 = date.indexOf("/");
		int index2 = date.lastIndexOf("/");
		if (index2 == index1){
			index2 = date.length();
		}
		int month = Integer.parseInt(date.substring(0, index1 ));
		int day = Integer.parseInt(date.substring(index1 + 1, index2));
		
		int year;
		if (index2 == date.length()){
			year = session.getSessionStartYear();
		} else {
			year = Integer.parseInt(date.substring(index2+1, date.length()));
		}
		
		cal.set(year, (month - 1), day, 0, 0, 0);

		return(cal);
	}
	
	private HashMap<String, Vector<Calendar>> elementDates(Element element) throws Exception {
		Vector<Calendar> startDates = new Vector<Calendar>();
		Vector<Calendar> endDates = new Vector<Calendar>();
		String elementName = "date";
        if(element.element(elementName) != null){
        	for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element dateElement = (Element) it.next();
				Calendar startDate = null;
				Calendar endDate = null;
				if(dateFormat == null) {								
					startDate = getCalendarForDate(getRequiredStringAttribute(dateElement, "startDate", elementName));
					endDate = getCalendarForDate(getRequiredStringAttribute(dateElement, "endDate", elementName));;
				} else {
					startDate = Calendar.getInstance();
					startDate.setTime(CalendarUtils.getDate(getRequiredStringAttribute(dateElement, "startDate", elementName), dateFormat));
					endDate = Calendar.getInstance();
					endDate.setTime(CalendarUtils.getDate(getRequiredStringAttribute(dateElement, "endDate", elementName), dateFormat));
				}
				if (startDate == null){
					throw new Exception("For element 'date' a 'startDate' is required, unable to parse given date");
				}
				if (endDate == null){
					throw new Exception("For element 'date' a 'endDate' is required, unable to parse given date");
				}				
				if (endDate.before(startDate)){
					endDate.add(Calendar.YEAR, 1);
				}
				startDates.add(startDate);
				endDates.add(endDate);				
			}
        }
        if (startDates.size() > 0){
        	HashMap<String, Vector<Calendar>> dates = new HashMap<String, Vector<Calendar>>();
        	dates.put("startDates", startDates);
        	dates.put("endDates", endDates);
        	return(dates);
        } else {
        	return(null);
        }
	}
	
	private TimeObject elementTime(Element element) throws Exception {
		TimeObject meetingTime = null;
		String elementName = "time";
        if(element.element(elementName) != null){       
			Element timeElement = element.element(elementName);
			
			String startTime = getRequiredStringAttribute(timeElement, "startTime", elementName);
			String endTime = getRequiredStringAttribute(timeElement, "endTime", elementName);
			String days = getRequiredStringAttribute(timeElement, "days", elementName);
			meetingTime = new TimeObject(startTime, endTime, days);
			if(meetingTime.getDays() == null || meetingTime.getDays().isEmpty()){
				meetingTime = null;
			}
        }
        return(meetingTime);
	}
	
	private Vector<Room> elementRoom(Element element, Class_ c) throws Exception {
		Vector<Room> rooms = new Vector<Room>();
		String elementName = "room";
        if(element.element(elementName) != null){
        	for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element roomElement = (Element) it.next();
				
				String building = getRequiredStringAttribute(roomElement, "building", elementName);
				String roomNbr = getRequiredStringAttribute(roomElement, "roomNbr", elementName);
				String id = getOptionalStringAttribute(roomElement, "id");
				Room room = findRoom(id, building, roomNbr);
				
				if (room != null){
					rooms.add(room);
				} else {
					addMissingLocation(building + " " + roomNbr + " - " + c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation());
				}
			}
        }
        if (rooms.isEmpty()){
        	return(null);
        } else {
        	return(rooms);
        }
	}	

	private Vector<NonUniversityLocation> elementLocation(Element element, Class_ c) throws Exception {
		Vector<NonUniversityLocation> locations = new Vector<NonUniversityLocation>();
		String elementName = "location";
        if(element.element(elementName) != null){
        	for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element roomElement = (Element) it.next();
				
				String name = getRequiredStringAttribute(roomElement, "name", elementName);
				String id = getOptionalStringAttribute(roomElement, "id");
				
				NonUniversityLocation location = findNonUniversityLocation(id, name, c);
				if (location != null){
					locations.add(location);
				} else {
					addMissingLocation(name + " - " + c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation());
					addMissingLocation("\tCould not find location '" + name + "' not adding it to class '" + c.getClassLabel() + "'");
				}
			}
        }
        if (locations.isEmpty()){
        	return(null);
        } else {
        	return(locations);
        }
	}	
	
	private boolean elementMeetings(Element element, Class_ c) throws Exception {
		String elementName = "meeting";
		boolean changed = false;
		Vector<Meeting> meetings = new Vector<Meeting>();
        if(element.element(elementName) != null){
			Calendar sessionStartDate = Calendar.getInstance();
			sessionStartDate.setTime(session.getSessionBeginDateTime());
			
			Calendar sessionClassesEndDate = Calendar.getInstance();
			sessionClassesEndDate.setTime(session.getExamBeginDate());				
			sessionClassesEndDate.add(Calendar.DAY_OF_MONTH, -1);
			
			Calendar sessionEndDate = Calendar.getInstance();
			sessionEndDate.setTime(session.getSessionEndDateTime());
			
			for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element meetingElement = (Element) it.next();
				String startDateStr = getOptionalStringAttribute(meetingElement, "startDate");
				String endDateStr = getOptionalStringAttribute(meetingElement, "endDate");
				String startTime = getRequiredStringAttribute(meetingElement, "startTime", elementName);
				String endTime = getRequiredStringAttribute(meetingElement, "endTime", elementName);
				String days = getRequiredStringAttribute(meetingElement, "days", elementName);
				String building = getOptionalStringAttribute(meetingElement, "building");
				String roomNbr = getOptionalStringAttribute(meetingElement, "room");
				String location = getOptionalStringAttribute(meetingElement, "location");
				Calendar startDate = null;
				Calendar endDate = null;
				if (startDateStr == null && endDateStr == null){
					startDate = sessionStartDate;
					endDate = sessionClassesEndDate;			
				} else if (dateFormat != null) {
					startDate = Calendar.getInstance();
					startDate.setTime(CalendarUtils.getDate(startDateStr, dateFormat));
					endDate = Calendar.getInstance();
					endDate.setTime(CalendarUtils.getDate(endDateStr, dateFormat));
				} else {
					startDate = getCalendarForDate(startDateStr);
					endDate = getCalendarForDate(endDateStr);
				}
				if(endDate.before(startDate)){
					endDate.add(Calendar.YEAR, 1);
				}
				
				if (endDate.equals(sessionEndDate) || (endDate.before(sessionEndDate) && endDate.after(sessionClassesEndDate))){
					if(startDate.before(sessionClassesEndDate)){
						endDate = sessionClassesEndDate;
					}
				}
				
				TimeObject timeObject = new TimeObject(startTime, endTime, days);

				Vector<Room> rooms = new Vector<Room>();
				Vector<NonUniversityLocation> nonUniversityLocations = new Vector <NonUniversityLocation>();

				if (building != null && roomNbr != null){
					Room r = findRoom(null, building, roomNbr);
					if (r != null) {
						rooms.add(r);
					} else {
						addMissingLocation(building + " " + roomNbr + " - " + c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation());
					}
				} else if (location != null){
					NonUniversityLocation nul = findNonUniversityLocation(null, location, c);
					if (nul != null) {
						nonUniversityLocations.add(nul);
					} else {
						addMissingLocation(location + " - " + c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation());
					}
				}
				
				Vector<Calendar> startDates = new Vector<Calendar>();
				startDates.add(startDate);
				
				Vector<Calendar> endDates = new Vector<Calendar>();
				endDates.add(endDate);
								
				Vector<Meeting> m = null;
				if (startDate.equals(sessionStartDate) && (endDate.equals(sessionClassesEndDate) || endDate.equals(sessionEndDate))){
					m = getMeetings(session.getDefaultDatePattern(), timeObject, rooms, nonUniversityLocations);	
				} else {
					m = getMeetings(
							startDate.getTime(), 
							endDate.getTime(), 
							createPatternString(startDates, endDates), 
							timeObject, rooms, nonUniversityLocations);
				}
				if(m != null && !m.isEmpty()){
					meetings.addAll(m);
				}
        	}
        	changed = addUpdateClassEvent(c, meetings);
        	if (changed)
        		c.setDatePattern(assignmentHelper.getDatePattern(c.getEvent()));
        }
        
        return(changed);
	}

	private boolean elementInstructor(Element element, Class_ c) throws Exception {
		boolean changed = false;
		String elementName = "instructor";
		HashMap<String, ClassInstructor> existingInstructors = new HashMap<String, ClassInstructor>();
		if(c.getClassInstructors() != null){
			for(Iterator<?> ciIt = c.getClassInstructors().iterator(); ciIt.hasNext(); ){
				ClassInstructor ci = (ClassInstructor) ciIt.next();
				existingInstructors.put(ci.getInstructor().getExternalUniqueId(), ci);
			}
		}
        if(element.element(elementName) != null){
        	HashSet<String> ids = new HashSet<String>();
        	HashMap<String, String> firstNames = new HashMap<String, String>();
        	HashMap<String, String> middleNames = new HashMap<String, String>();
        	HashMap<String, String> lastNames = new HashMap<String, String>();
        	HashMap<String, Integer> shares = new HashMap<String, Integer>();
        	HashMap<String, Boolean> leads = new HashMap<String, Boolean>();
        	for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element instructorElement = (Element) it.next();
				String id = getRequiredStringAttribute(instructorElement, "id", elementName);
	            if (trimLeadingZerosFromExternalId){
	            	try {
	            		Integer num = new Integer(id);
	            		id = num.toString();
					} catch (Exception e) {
						// do nothing
					}
	            }
				ids.add(id);
				firstNames.put(id, getOptionalStringAttribute(instructorElement, "fname"));
				middleNames.put(id, getOptionalStringAttribute(instructorElement, "mname"));
				lastNames.put(id, getOptionalStringAttribute(instructorElement, "lname"));
				Integer share = getOptionalIntegerAttribute(instructorElement, "share");;
				if (share == null){
					share = new Integer(100);
				}
				shares.put(id, share);
				Boolean lead = getOptionalBooleanAttribute(instructorElement, "lead");
				if (lead == null){
					lead = new Boolean(true);
				}
				leads.put(id, lead);
        	}
        	for(Iterator<String> it = ids.iterator(); it.hasNext(); ){
				boolean addNew = false;
				String id = (String) it.next();
				ClassInstructor ci = existingInstructors.get(id);
				if (ci == null){
					DepartmentalInstructor di = findDepartmentalInstructorWithExternalUniqueId(id, c.getSchedulingSubpart().getControllingDept());
					if (di == null) {
						di = new DepartmentalInstructor();
						di.setDepartment(c.getSchedulingSubpart().getControllingDept());
						di.setExternalUniqueId(id);
						if (lastNames.get(id)  == null){
							Staff staffData = findStaffMember(id);
							if (staffData != null){
								firstNames.put(id, staffData.getFirstName());
								middleNames.put(id, staffData.getMiddleName());
								lastNames.put(id, staffData.getLastName());
							}
						}
						di.setFirstName(firstNames.get(id));
						di.setMiddleName(middleNames.get(id));
						String lastName = lastNames.get(id);
						di.setLastName((lastName != null?lastName:"Unknown Name"));
						di.setIgnoreToFar(new Boolean(false));
						getHibSession().save(di);
						getHibSession().flush();
						getHibSession().refresh(di);
			        	ChangeLog.addChange(getHibSession(), getManager(), session, di, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.CREATE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
					}
					ci = new ClassInstructor();
					ci.setClassInstructing(c);
					c.addToclassInstructors(ci);
					ci.setInstructor(di);
					di.addToclasses(ci);
					changed = true;
					addNew = true;
					
				} else {
					existingInstructors.remove(id);
				}
				Integer share = shares.get(id);
				if (ci.getPercentShare() == null || !ci.getPercentShare().equals(share)){
					ci.setPercentShare(share);
					changed = true;
				}
				
				Boolean lead = leads.get(id);
				if (ci.isLead() == null || !ci.isLead().equals(lead)){
					ci.setLead(lead);
					changed = true;
				}
				
				if (changed){
					getHibSession().saveOrUpdate(c);
					getHibSession().flush();
					getHibSession().refresh(c);
		        	ChangeLog.addChange(getHibSession(), getManager(), session, ci, ChangeLog.Source.DATA_IMPORT_OFFERINGS, (addNew?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
				}
			}
        }
        if(existingInstructors.size() > 0){
        	for(Iterator<ClassInstructor> ciIt = existingInstructors.values().iterator(); ciIt.hasNext(); ){
        		ClassInstructor ci = (ClassInstructor) ciIt.next();
        		deleteClassInstructor(ci);
        		changed = true;
        	}
        }
        return(changed);
	}

	private Staff findStaffMember(String id) {
		return(Staff) this.
		getHibSession().
		createQuery("select distinct s from Staff s where s.externalUniqueId=:externalId").
		setString("externalId", id).
		setCacheable(true).
		uniqueResult();
	}
	
	private void elementCourseCredit(Element element, CourseOffering co) throws Exception {
		String elementName = "courseCredit";
        Element credit = element.element(elementName);
        // if there is no credit on the course element, check the offering element instead
        if (credit == null) 
        	credit = element.getParent().element(elementName);
        if(credit != null) {
        	String creditFormat = getRequiredStringAttribute(credit, "creditFormat", elementName);     		
        	String creditType = getRequiredStringAttribute(credit, "creditType", elementName);  		
        	String creditUnitType = getRequiredStringAttribute(credit, "creditUnitType", elementName);
	        Boolean fractionalIncrementsAllowed = getOptionalBooleanAttribute(credit, "fractionalCreditAllowed");
       	
	        String minCreditStr = getOptionalStringAttribute(credit, "fixedCredit");
	        if(minCreditStr == null) {
	        	minCreditStr = getOptionalStringAttribute(credit, "minimumCredit");
	        }
	        Float minCredit = null;
	        if (minCreditStr != null){
	        	minCredit = new Float(minCreditStr);
	        }
	        String maxCreditStr = getOptionalStringAttribute(credit, "maximumCredit");
	        Float maxCredit = null;
	        if (maxCreditStr != null){
	        	maxCredit = Float.parseFloat(maxCreditStr);
	        }
	        CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(creditFormat, creditType, creditUnitType, minCredit, maxCredit, fractionalIncrementsAllowed, true);
	        if (ccuc != null) {
	        	co.setCredit(ccuc);
	        	ccuc.setOwner(co);
	        }
        }
        return;
	}

	private boolean elementSubpartCredit(Element element, SchedulingSubpart ss) throws Exception {
		boolean changed = false;
		String elementName = "subpartCredit";
        Element credit = element.element(elementName);
   
        if(credit != null) {
        	String creditFormat = getRequiredStringAttribute(credit, "creditFormat", elementName);     		
        	String creditType = getRequiredStringAttribute(credit, "creditType", elementName);  		
        	String creditUnitType = getRequiredStringAttribute(credit, "creditUnitType", elementName);
	        Boolean fractionalIncrementsAllowed = getOptionalBooleanAttribute(credit, "fractionalCreditAllowed");
       	
	        String minCreditStr = getOptionalStringAttribute(credit, "fixedCredit");
	        if(minCreditStr == null) {
	        	minCreditStr = getOptionalStringAttribute(credit, "minimumCredit");
	        }
	        Float minCredit = null;
	        if (minCreditStr != null){
	        	minCredit = new Float(minCreditStr);
	        }
	        String maxCreditStr = getOptionalStringAttribute(credit, "maximumCredit");
	        Float maxCredit = null;
	        if (maxCreditStr != null){
	        	maxCredit = Float.parseFloat(maxCreditStr);
	        }
	        CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(creditFormat, creditType, creditUnitType, minCredit, maxCredit, fractionalIncrementsAllowed, false);
	        if (ss.getCredit() == null && ccuc != null){
	        	addNote("\tadded subpart credit");
	        	changed = true;
	        } else if (ss.getCredit() != null && !isSameCreditConfig(ss.getCredit(),ccuc)) {
	        	addNote("\tsubpart credit values changed");
	        	changed = true;
	        } 
	        if (changed){
	        	ss.setCredit(ccuc);
	        	ccuc.setOwner(ss);
	        	getHibSession().saveOrUpdate(ss);
	        	getHibSession().flush();
	        	getHibSession().refresh(ss);
	        	ChangeLog.addChange(getHibSession(), getManager(), session, ss.getCredit(), ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.UPDATE, ss.getControllingCourseOffering().getSubjectArea(), ss.getControllingCourseOffering().getDepartment());
	        }
        }
		return(changed);
	}
	
	private boolean handleDistributionPrefElement(Element element, Class_ c, String elementName, DistributionType distributionType) throws Exception{
		boolean changed = false;
    	Vector<DistributionPref> existingDistPrefs = new Vector<DistributionPref>();
		if(c.getDistributionPreferences() != null){
			for(Iterator<?> dpIt = c.getDistributionPreferences().iterator(); dpIt.hasNext(); ){
				DistributionPref dp = (DistributionPref) dpIt.next();
				if (dp.getDistributionType().getUniqueId().equals(distributionType.getUniqueId())){
					existingDistPrefs.add(dp);						
				}
			}
		}
		Vector<String> classIds = new Vector<String>();
		if(element.element(elementName) != null){
        	classIds.add(c.getExternalUniqueId());
        	for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
        		Element distPrefElement = (Element) it.next();
        		classIds.add(getRequiredStringAttribute(distPrefElement, "id", elementName));
        	}
       	
        }
      	if(existingDistPrefs.size() != 1){
      		if (existingDistPrefs.size() > 1) {
	      		addNote("\tMultiple " + distributionType.getLabel() + " distribution preferences exist -- deleted them");
				for (Iterator<?> dpIt = existingDistPrefs.iterator(); dpIt.hasNext();){
					DistributionPref dp = (DistributionPref) dpIt.next();				
					addNote("\t\tdeleted '" + dp.preferenceText() + "'");
					deleteDistributionPref(dp);									
				}
	      		changed = true;
      		}
      		if (classIds.size() > 1){
      			addDistributionPref(classIds, c, distributionType);
      			changed = true;
      		}
		} else {
			DistributionPref dp = (DistributionPref)existingDistPrefs.firstElement();
			if (classIds.size() > 1){
				if (!isMatchingDistPref(dp, classIds)){
					changed = true;
					deleteDistributionPref(dp);
					addDistributionPref(classIds, c, distributionType);
				}
				
			} else {
				addNote("Class  " + c.getClassLabel() +" is no longer a " + distributionType.getLabel() + ", removed" + dp.toString());
				deleteDistributionPref(dp);
				changed = true;
			}
		}
		return(changed);
	}
	
	private boolean elementCanShareRoom(Element element, Class_ c) throws Exception{
		if (!useCanShareRoomElement){
			return(false);
		}
		return(handleDistributionPrefElement(element, c, "canShareRoom", canShareRoomType));
	}


	private boolean elementMeetsWith(Element element, Class_ c) throws Exception{
		if (!useMeetsWithElement){
			return(false);
		}
		return(handleDistributionPrefElement(element, c, "meetsWith", meetsWithType));
	}
	
	private boolean isMatchingDistPref(DistributionPref dp, Vector<String> classExternalIds){
		boolean isSame = false;
		DistributionObject distObj = null;
		String cei = null;
		boolean allFound = true;
		HashSet<DistributionObject> existingDistObjs = new HashSet<DistributionObject>();
		existingDistObjs.addAll(dp.getDistributionObjects());
		for (Iterator<?> ceiIt = classExternalIds.iterator(); ceiIt.hasNext();){
			cei = (String) ceiIt.next();
			boolean found = false;
			for(Iterator<?> doIt = dp.getDistributionObjects().iterator(); doIt.hasNext();){
				distObj = (DistributionObject) doIt.next();
				if (distObj.getPrefGroup() instanceof Class_) {
					Class_ c = (Class_) distObj.getPrefGroup();
					if (cei.equals(c.getExternalUniqueId())){
						found = true;
						existingDistObjs.remove(distObj);
						break;
					}
				}
			}
			if (!found){
				allFound = false;
				break;
			}
		}
		if(allFound && existingDistObjs.isEmpty()){
			isSame = true;
		}
		return(isSame);
	}
	
	protected abstract boolean handleCustomCourseChildElements(CourseOffering courseOffering, Element courseOfferingElement) throws Exception;
	
	private boolean elementCourse(Element element, InstructionalOffering io, String action) throws Exception{
		boolean changed = false;
		if (io.getControllingCourseOffering() != null){
			Debug.info("Checking Offering:  " + io.getCourseName());
		}
		ArrayList<ImportCourseOffering> courses = getCourses(element);
		if (action.equalsIgnoreCase("insert")){
			for(Iterator<ImportCourseOffering> it = courses.iterator(); it.hasNext();){
				ImportCourseOffering ico = it.next();
				CourseOffering co = ico.getCourseOffering();
				co.setInstructionalOffering(io);
				if (co.getNbrExpectedStudents() == null){
					co.setNbrExpectedStudents(new Integer(0));
				}
				if (co.getDemand() == null){
					co.setDemand(new Integer(0));
				}
				io.addTocourseOfferings(co);
				co.setPermId(InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)new CourseOfferingDAO().getSession(), this).toString());
				co.setSubjectAreaAbbv(co.getSubjectArea().getSubjectAreaAbbreviation());
				addNote("\tadded course: " + co.getSubjectArea().getSubjectAreaAbbreviation() + " " + co.getCourseNbr());
				if (co.getCredit() != null)
					addNote("\tadded credit: " + co.getCredit().creditAbbv());
				getHibSession().saveOrUpdate(io);
				getHibSession().flush();
				getHibSession().refresh(io);
				ChangeLog.addChange(getHibSession(), getManager(), session, co, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.CREATE, co.getSubjectArea(), co.getDepartment());
				handleCustomCourseChildElements(co, ico.getElement());
			}
			changed = true;
		} else {
			CourseOffering nco = null;
			CourseOffering oco = null;
			for(Iterator<ImportCourseOffering> nit = courses.iterator(); nit.hasNext();){
				ImportCourseOffering ico = (ImportCourseOffering) nit.next();
				nco = ico.getCourseOffering();
				boolean exists = false;
				for(Iterator<?> oit = io.getCourseOfferings().iterator(); oit.hasNext();){
					oco = (CourseOffering) oit.next();
					if (isSameCourseOffering(oco, nco)){
						exists = true;
						existingCourseOfferings.remove(oco.getUniqueId());
						if (!oco.getSubjectArea().getUniqueId().equals(nco.getSubjectArea().getUniqueId())){
							oco.setSubjectArea(nco.getSubjectArea());
							addNote("\tchanged subject area: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						}
						if (!oco.getCourseNbr().equals(nco.getCourseNbr())){
							oco.setCourseNbr(nco.getCourseNbr());
							addNote("\tchanged course number: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						}
						if (!oco.isIsControl().equals(nco.getIsControl())){
							oco.setIsControl(nco.getIsControl());
							addNote("\tchanged control flag: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						}
						if (oco.getScheduleBookNote() == null && nco.getScheduleBookNote() != null){
							oco.setScheduleBookNote(nco.getScheduleBookNote());
							addNote("\tadded schedule book note: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						} else if (oco.getScheduleBookNote() != null && (nco.getScheduleBookNote() == null || nco.getScheduleBookNote().length() == 0)){
							oco.setScheduleBookNote(null);
							addNote("\tremoved schedule book note: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						} else if (oco.getScheduleBookNote() != null && nco.getScheduleBookNote() != null && !oco.getScheduleBookNote().equals(nco.getScheduleBookNote())){
							oco.setScheduleBookNote(nco.getScheduleBookNote());
							addNote("\tchanged schedule book note: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						}
						if (oco.getTitle() == null && nco.getTitle() != null && nco.getTitle().length() > 0){
							oco.setTitle(nco.getTitle());
							addNote("\tadded title: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						} else if (oco.getTitle() != null && (nco.getTitle() == null || nco.getTitle().length() == 0)){
							oco.setTitle(null);
							addNote("\tremoved title: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						} else if (oco.getTitle() != null && nco.getTitle() != null && !oco.getTitle().equals(nco.getTitle())){
							oco.setTitle(nco.getTitle());
							addNote("\tchanged title: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						}	
						if (oco.getConsentType() == null && nco.getConsentType() != null) {
							oco.setConsentType(nco.getConsentType());
							addNote("\tadded consent: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						} else if (oco.getConsentType() != null && nco.getConsentType() == null) {
							oco.setConsentType(null);
							addNote("\tremoved consent: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						} else if (oco.getConsentType() != null && nco.getConsentType() != null && !oco.getConsentType().equals(nco.getConsentType())) {
							oco.setConsentType(nco.getConsentType());
							addNote("\tchanged consent: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						}
						if (oco.getCredit() == null && nco.getCredit() != null) {
							CourseCreditUnitConfig credit = nco.getCredit();
							oco.setCredit(credit); credit.setOwner(oco);
							addNote("\tadded credit: " + nco.getCredit().creditAbbv());
							changed = true;
						} else if (oco.getCredit() != null && nco.getCredit() == null) {
							CourseCreditUnitConfig old = oco.getCredit();
							getHibSession().delete(old);
							oco.setCredit(null);
							addNote("\tremoved credit: " + old);
							changed = true;
						} else  if (oco.getCredit() != null && !isSameCreditConfig(oco.getCredit(),nco.getCredit())) {
							CourseCreditUnitConfig old = oco.getCredit();
							getHibSession().delete(old);
							CourseCreditUnitConfig credit = nco.getCredit();
							oco.setCredit(credit); credit.setOwner(oco);
							addNote("\tchanged credit: " + oco.getCredit().creditAbbv());
							changed = true;
						}
						if (oco.getPermId() == null){
							oco.setPermId(InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)new CourseOfferingDAO().getSession(), this).toString());
							addNote("\tadded missing permId: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
							changed = true;
						}
						if (handleCustomCourseChildElements(oco, ico.getElement())){
							changed = true;
						}
						if(changed){
				        	ChangeLog.addChange(getHibSession(), getManager(), session, oco, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.UPDATE, oco.getSubjectArea(), oco.getDepartment());
						}					
					}	
					
				}
				if (!exists){
					addNote("\tmatching course offering not found, added new: " + nco.getSubjectArea().getSubjectAreaAbbreviation() + " " + nco.getCourseNbr());
					if (nco.getNbrExpectedStudents() == null){
						nco.setNbrExpectedStudents(new Integer(0));
					}
					if (nco.getDemand() == null){
						nco.setDemand(new Integer(0));
					}
					nco.setSubjectAreaAbbv(nco.getSubjectArea().getSubjectAreaAbbreviation());
					nco.setInstructionalOffering(io);
					if (nco.getCredit() != null)
						addNote("\tadded credit: " + nco.getCredit().creditAbbv());					
					io.addTocourseOfferings(nco);
					changed = true;
					getHibSession().saveOrUpdate(io);
					getHibSession().flush();
					getHibSession().refresh(io);
		        	handleCustomCourseChildElements(nco, ico.getElement());
		        	ChangeLog.addChange(getHibSession(), getManager(), session, nco, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.CREATE, nco.getSubjectArea(), nco.getDepartment());
				}
			}
			List removeCourses = new ArrayList<Object>();
			removeCourses.addAll(io.getCourseOfferings());
			for (Iterator<?> coIt = removeCourses.iterator(); coIt.hasNext(); ){
				CourseOffering co = (CourseOffering) coIt.next();
				if (co.getUniqueId() != null && existingCourseOfferings.contains(co.getUniqueId())){
					addNote("\tremoved course offering from instructional offering: " + co.getCourseName());
					deleteCourseOffering(co);
					changed = true;
				}
			}
			boolean hasControl = false;
			for (Iterator<?> coIt = io.getCourseOfferings().iterator(); coIt.hasNext(); ){
				CourseOffering co = (CourseOffering) coIt.next();
				if (co.isIsControl().booleanValue()){
					hasControl = true;
				}
			}
			if (!hasControl){
				throw new Exception("Expected 'offering' to have a course marked as control.");
			}	
		}
		
		return(changed);

	}

	private boolean elementInstrOffrConfig(Element element, InstructionalOffering io) throws Exception{
		boolean changed = false;
		HashMap<String, InstrOfferingConfig> existingConfigs = new HashMap<String, InstrOfferingConfig>();
		if (io.getInstrOfferingConfigs() != null){
			for (Iterator<?> it = io.getInstrOfferingConfigs().iterator(); it.hasNext();){
				InstrOfferingConfig ioc = (InstrOfferingConfig) it.next();
				existingConfigs.put(ioc.getName(), ioc);
			}
		}
		
		String elementName = "config";
		if(element.element(elementName) != null) {
			for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element configElement = (Element) it.next();
				boolean addNew = false;
				String name = getRequiredStringAttribute(configElement, "name", elementName);
				String limitStr = getRequiredStringAttribute(configElement, "limit", elementName);
				Integer limit = new Integer(0);
				Boolean unlimited = new Boolean(false);
				if (limitStr.equalsIgnoreCase("inf")){
					unlimited = new Boolean(true);
				} else {
					limit = Integer.valueOf(limitStr);
				}
				
				InstrOfferingConfig ioc = null;
				if (existingConfigs.containsKey(name)){
					ioc = (InstrOfferingConfig)existingConfigs.get(name);
					existingConfigs.remove(name);
				} else {
					addNote("\tdid not find matching config element, adding new config: " + name);
					ioc = new InstrOfferingConfig();
					ioc.setName(name);
					ioc.setInstructionalOffering(io);
					io.addToinstrOfferingConfigs(ioc);
					changed = true;
					addNew = true;
				}
				if (ioc.getLimit() == null || !ioc.getLimit().equals(limit)){
					addNote("\tconfig limit changed");
					ioc.setLimit(limit);
					changed = true;
				}
				if (ioc.isUnlimitedEnrollment() == null || !ioc.isUnlimitedEnrollment().equals(unlimited)){
					addNote("\tconfig unlimited changed");
					ioc.setUnlimitedEnrollment(unlimited);
					changed = true;
				}
				
				if (changed){
					this.getHibSession().saveOrUpdate(ioc);
				}

				if (handleCustomInstrOffrConfigChildElements(ioc, configElement)){
					addNote("\tconfig changed by custom element");
					changed = true;
				}
				if (elementSubpart(configElement, ioc, null, null)){
					addNote("\tconfig subparts changed");
					changed = true;
				}
				if (changed){
					this.getHibSession().saveOrUpdate(ioc);
				}
				
				if (elementClass(configElement, ioc, null, null)){
					addNote("\tconfig classes changed");
					changed = true;
				}

				if (changed){
					addNote("\tconfig element changed: " + name);
		        	ChangeLog.addChange(getHibSession(), getManager(), session, ioc, ChangeLog.Source.DATA_IMPORT_OFFERINGS, (addNew?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), ioc.getControllingCourseOffering().getSubjectArea(), ioc.getControllingCourseOffering().getDepartment());
					this.getHibSession().saveOrUpdate(ioc);
				}
			}
		} 

		if (existingConfigs.size() > 0){
			for(Iterator<InstrOfferingConfig> cIt = existingConfigs.values().iterator(); cIt.hasNext();){
				InstrOfferingConfig ioc = (InstrOfferingConfig) cIt.next();
				deleteInstrOffrConfig(ioc);
			}
			changed = true;
		}
		return(changed);
	}
	
	
	private boolean elementClass(Element element, InstrOfferingConfig ioc, Class_ parentClass, HashSet<Class_> allExistingClasses) throws Exception{
		boolean changed = false;
		HashMap<String, Class_> possibleClassesAtThisLevel = new HashMap<String, Class_>();
		ArrayList<SchedulingSubpart> possibleSubpartsAtThisLevel = new ArrayList<SchedulingSubpart>();
		if (parentClass == null){
			allExistingClasses = new HashSet<Class_>();
			if (ioc.getSchedulingSubparts() != null){
				for (Iterator<?> ssIt = ioc.getSchedulingSubparts().iterator(); ssIt.hasNext(); ){
					SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
					if (ss.getClasses() != null){
						for (Iterator<?> cIt = ss.getClasses().iterator(); cIt.hasNext(); ){
							Class_ c = (Class_) cIt.next();
							allExistingClasses.add(c);
							if (c.getParentClass() == null){
								if (c.getExternalUniqueId() != null){
									possibleClassesAtThisLevel.put(c.getExternalUniqueId(), c);
								} else {
									possibleClassesAtThisLevel.put(c.getSchedulingSubpart().getItype().getAbbv().trim() + c.getClassSuffix(), c);
								}
							}
						}
					}
					if (ss.getParentSubpart() == null){
						possibleSubpartsAtThisLevel.add(ss);
					}
				}
			}
		} else {
			if (parentClass.getChildClasses() != null){
				for (Iterator<?> it = parentClass.getChildClasses().iterator(); it.hasNext();){
					Class_ c = (Class_) it.next();
					if (c.getExternalUniqueId() != null){
						possibleClassesAtThisLevel.put(c.getExternalUniqueId(), c);
					} else {
						possibleClassesAtThisLevel.put(c.getSchedulingSubpart().getItype().getAbbv().trim() + c.getClassSuffix(), c);
					}
				}
			}
			if (parentClass.getSchedulingSubpart().getChildSubparts() != null){
				possibleSubpartsAtThisLevel.addAll(parentClass.getSchedulingSubpart().getChildSubparts());
			}
		}
		
		String elementName = "class";
		if (element.element(elementName) != null){
			if (parentClass == null && (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().isEmpty())){
				throw new Exception(ioc.getCourseName() + " - If a 'config' has 'class' elements it must also have matching 'subpart' elements");
			}
			for (Iterator<?> cIt = element.elementIterator(elementName); cIt.hasNext();){
				Element classElement = (Element) cIt.next();
				boolean isAdd = false;
				String id = getOptionalStringAttribute(classElement, "id");
				String managingDeptStr = getOptionalStringAttribute(classElement, "managingDept");
				Department managingDept = null;
				if (managingDeptStr != null && managingDeptStr.trim().length() > 0){
					managingDept = Department.findByDeptCode(managingDeptStr.trim(), session.getUniqueId());
				}
				String limitStr = getRequiredStringAttribute(classElement, "limit", elementName);
				Integer limit = new Integer(0);
				if (!limitStr.equalsIgnoreCase("inf")){
					limit = Integer.valueOf(limitStr);
				}
				String suffix = getRequiredStringAttribute(classElement, "suffix", elementName);
				String type = getRequiredStringAttribute(classElement, "type", elementName);
				String scheduleNote = getOptionalStringAttribute(classElement, "scheduleNote");
				Boolean enabledForStudentScheduling = getOptionalBooleanAttribute(classElement, "studentScheduling", getOptionalBooleanAttribute(classElement, "displayInScheduleBook", true));
				Integer itypeId = findItypeForString(type).getItype();
				
				Class_ clazz = null;
				Class_ origClass = null;
				if (id != null){
					origClass = (Class_) possibleClassesAtThisLevel.get(id);
					if (origClass != null){
						possibleClassesAtThisLevel.remove(id);
						if (!origClass.getClassSuffix().equals(suffix)){
							changed = true;
							origClass.setClassSuffix(suffix);
							Integer origSectionNbr = origClass.getSectionNumberCache();
							try {
								origClass.setSectionNumberCache(new Integer(suffix));
							} catch (Exception e) {
								origClass.setSectionNumberCache(origSectionNbr);			
							}
							addNote("\t suffix for class changed: " + origClass.getClassLabel());

						}
					}
				} 
				if (origClass == null){
					origClass = (Class_) possibleClassesAtThisLevel.get(type+suffix);
					if (origClass != null && origClass.getExternalUniqueId() != null && id != null && !origClass.getExternalUniqueId().equals(id)){
						origClass = null;
					} else if(origClass != null){
						possibleClassesAtThisLevel.remove(type+suffix);
					}
				}
				if (origClass != null) {
					clazz = origClass;
					allExistingClasses.remove(origClass);
					existingClasses.remove(clazz.getUniqueId());
					SchedulingSubpart prevSubpart = clazz.getSchedulingSubpart();
					if (!clazz.getSchedulingSubpart().getItype().getItype().equals(itypeId) || !possibleSubpartsAtThisLevel.contains(clazz.getSchedulingSubpart())){
						for (Iterator<SchedulingSubpart> ssIt = possibleSubpartsAtThisLevel.iterator(); ssIt.hasNext(); ){
							SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
							if (ss.getItype().getItype().equals(itypeId)){
								org.hibernate.Session hSess = this.getHibSession();
								clazz.setSchedulingSubpart(ss);
								ss.addToclasses(clazz);
								hSess.update(ss);
								hSess.flush();
								hSess.refresh(ss);
								hSess.refresh(prevSubpart);
								break;
							}
						}
						prevSubpart.getClasses().remove(clazz);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' itype changed");
						changed = true;
					}
					if ((clazz.getSchedulePrintNote() != null && !clazz.getSchedulePrintNote().equals(scheduleNote))
							 || (clazz.getSchedulePrintNote() == null && scheduleNote != null)){
						clazz.setSchedulePrintNote(scheduleNote);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' schedule note changed");
						changed = true;
					}
					if ((clazz.getExpectedCapacity() != null && !clazz.getExpectedCapacity().equals(limit))
							 || (clazz.getExpectedCapacity() == null && limit != null)){
						clazz.setExpectedCapacity(limit);
						clazz.setMaxExpectedCapacity(limit);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' limit changed");
						changed = true;						
					}
					if ((clazz.getManagingDept() != null && managingDept != null && !clazz.getManagingDept().getUniqueId().equals(managingDept.getUniqueId()))
							|| (clazz.getManagingDept() == null && managingDept != null)){
						clazz.setManagingDept(managingDept);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' managing department changed");
						changed = true;						
					}
					if (!enabledForStudentScheduling.equals(clazz.isEnabledForStudentScheduling())) {
						clazz.setEnabledForStudentScheduling(enabledForStudentScheduling);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' display in schedule book changed");
						changed = true;						
					}
				} else {
					isAdd = true;
					clazz = new Class_();
					clazz.setExternalUniqueId(id);
					clazz.setClassSuffix(suffix);
					try {
						clazz.setSectionNumberCache(new Integer(suffix));
					} catch (Exception e) {
						// Ignore Exception						
					}
					
					clazz.setExpectedCapacity(limit);
					clazz.setMaxExpectedCapacity(limit);
					clazz.setRoomRatio(new Float(1.0));
					clazz.setNbrRooms(new Integer(1));
					clazz.setEnabledForStudentScheduling(enabledForStudentScheduling);
					clazz.setSchedulePrintNote(scheduleNote);
					clazz.setDisplayInstructor(new Boolean(true));
					if (managingDept != null){
						clazz.setManagingDept(managingDept);
					}
					if(parentClass != null){
						clazz.setParentClass(parentClass);
						parentClass.addTochildClasses(clazz);
					}
					for (Iterator<SchedulingSubpart> ssIt = possibleSubpartsAtThisLevel.iterator(); ssIt.hasNext(); ){
						SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
						if (ss.getItype().getItype().equals(itypeId)){
							clazz.setSchedulingSubpart(ss);
							ss.addToclasses(clazz);
							break;
						}
					}
					addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' matching class not found adding new class");
					changed = true;
				}
				if (clazz.getSchedulingSubpart() == null){
					throw new Exception(ioc.getCourseName() + " " + type + " " + suffix + " 'class' does not have matching 'subpart'");
				}
				
				if (elementInstructor(classElement, clazz)){
					addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' instructor data changed");
					changed = true;
				}
				
				DatePattern dp = null;
				if (classElement.element("time") != null && classElement.element("time").attributeValue("datePattern") != null) {
					dp = DatePattern.findByName(session, classElement.element("time").attributeValue("datePattern"));
				}
				HashMap<String, Vector<Calendar>> dates = elementDates(classElement);
				if (dp == null && dates != null){
					dp = findDatePattern(dates.get("startDates"), dates.get("endDates"), clazz);					
				}
				if (classElement.element("meeting") == null){
					if (dp == null && clazz.getDatePattern() != null){
						if (!clazz.getDatePattern().isDefault() && clazz.getSchedulingSubpart().effectiveDatePattern().isDefault()){
							clazz.setDatePattern(dp);
							addNote("\t" + ioc.getCourseName() + " " + type + suffix + " 'class' date pattern changed back to default");
							changed = true;
						} else if (!clazz.getDatePattern().isDefault() && !clazz.getSchedulingSubpart().effectiveDatePattern().isDefault()){
							clazz.setDatePattern(session.getDefaultDatePatternNotNull());
							addNote("\t" + ioc.getCourseName() + " " + type + suffix + " 'class' date pattern changed to default");
							changed = true;
						}
					} else if (dp != null && clazz.getDatePattern() == null){
						clazz.setDatePattern(dp);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + "'class' date pattern changed from default");
						changed = true;
					} else if (dp != null && clazz.getDatePattern() != null && !clazz.getDatePattern().getUniqueId().equals(dp.getUniqueId())){
						clazz.setDatePattern(dp);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' date pattern changed");
						changed = true;
					}
				}
				if (changed){
					this.getHibSession().saveOrUpdate(clazz);
				}

				if (elementMeetsWith(classElement, clazz)){
					addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' meets with preferences changed");
					changed = true;
				}
				
				if (elementCanShareRoom(classElement, clazz)){
					addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' can share room preferences changed");
					changed = true;
				}
				
				if (classElement.element("meeting") != null){
					if(elementMeetings(classElement, clazz)){
						changed = true;
					}
					int numRooms = 1;
					if (clazz.getNbrRooms() != null && !clazz.getNbrRooms().equals(new Integer(numRooms))){
						clazz.setNbrRooms(new Integer(numRooms));
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " number of rooms changed");
						changed = true;
					}

				} else if (classElement.element("time") != null){
					TimeObject meetingTime = elementTime(classElement);
					Vector<Room> rooms = elementRoom(classElement, clazz);
					Vector<NonUniversityLocation> locations = elementLocation(classElement, clazz);
					int numRooms = 0;
					if (rooms != null && !rooms.isEmpty()){
						numRooms += rooms.size();
					} 
					if (locations != null && !locations.isEmpty()){
						numRooms += locations.size();
					}
					if (clazz.getNbrRooms() != null && !clazz.getNbrRooms().equals(new Integer(numRooms))){
						clazz.setNbrRooms(new Integer(numRooms));
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " number of rooms changed");
						changed = true;
					}
					if (addUpdateClassEvent(clazz, meetingTime, rooms, locations)){
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' events for class changed");
						changed = true;
					}	
					TimePattern tp = findTimePatternForMeetingInfo(clazz, meetingTime);
					if (tp != null && clazz.getTimePatterns() != null && !clazz.getTimePatterns().contains(tp)){
						for (Iterator it = clazz.getTimePreferences().iterator(); it.hasNext();){
							TimePref pref = (TimePref) it.next();
							clazz.getPreferences().remove(pref);
						}
						TimePref tpref = new TimePref();
						tpref.setTimePattern(tp);
						tpref.setOwner(clazz);
						tpref.setPrefLevel(requiredPrefLevel);
						clazz.addTopreferences(tpref);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' time pattern for class changed");
						changed = true;
					} else if (tp != null && (clazz.getTimePatterns() == null || clazz.getTimePatterns().isEmpty())){
						TimePref tpref = new TimePref();
						tpref.setTimePattern(tp);
						tpref.setOwner(clazz);
						tpref.setPrefLevel(requiredPrefLevel);
						clazz.addTopreferences(tpref);
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' time pattern for class added");
						changed = true;						
					} else if (tp == null && clazz.getTimePatterns() != null && !clazz.getTimePatterns().isEmpty()){
						for (Iterator it = clazz.getTimePreferences().iterator(); it.hasNext();){
							TimePref pref = (TimePref) it.next();
							clazz.getPreferences().remove(pref);
						}
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' time pattern for class removed");
						changed = true;						
					}
				}
				if (handleCustomClassChildElements(classElement, ioc, clazz)){
					changed = true;
				}
				if (elementClass(classElement, ioc, clazz, allExistingClasses)){
					addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' child classes changed");
					changed = true;
				}				
				if (changed){
					getHibSession().saveOrUpdate(clazz);
					getHibSession().flush();
					getHibSession().refresh(clazz);
				}
				if (changed){
					ChangeLog.addChange(getHibSession(), getManager(), session, clazz, ChangeLog.Source.DATA_IMPORT_OFFERINGS, (isAdd?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), ioc.getControllingCourseOffering().getSubjectArea(), ioc.getDepartment());
				}
			}
		} 
		
		if (possibleClassesAtThisLevel.size() > 0){
			addNote("\t" + ioc.getCourseName() + " 'class' not all classes at this level had matches");
			for(Iterator<Class_> cIt = possibleClassesAtThisLevel.values().iterator(); cIt.hasNext(); ) {
				Class_ c = (Class_) cIt.next();
				if (c.getParentClass() != null && c.getParentClass().equals(parentClass)){
					parentClass.getChildClasses().remove(c);
					c.setParentClass(null);
				}
			}
			changed = true;
		}
		
		if (parentClass == null && allExistingClasses.size() > 0){
			info(ioc.getCourseName() + " 'class' not all classes had matches, removing those without matches");
			for (Iterator<Class_> cIt = allExistingClasses.iterator(); cIt.hasNext(); ){
				Class_ c = (Class_) cIt.next();
				deleteClass(c);
			}
		}
		return changed;
	}
	
	private TimePattern findTimePatternForMeetingInfo(Class_ clazz, TimeObject timeObject){
		int days = 0;
		for(Integer dayOfWeek : timeObject.getDays()){
			if (dayOfWeek == Calendar.MONDAY){
				days += Constants.DAY_CODES[Constants.DAY_MON];
			} else if (dayOfWeek == Calendar.TUESDAY){
				days += Constants.DAY_CODES[Constants.DAY_TUE];
			} else if (dayOfWeek == Calendar.WEDNESDAY){
				days += Constants.DAY_CODES[Constants.DAY_WED];
			} else if (dayOfWeek == Calendar.THURSDAY){
				days += Constants.DAY_CODES[Constants.DAY_THU];
			} else if (dayOfWeek == Calendar.FRIDAY){
				days += Constants.DAY_CODES[Constants.DAY_FRI];
			} else if (dayOfWeek == Calendar.SATURDAY){
				days += Constants.DAY_CODES[Constants.DAY_SAT];
			} else if (dayOfWeek == Calendar.SUNDAY){
				days += Constants.DAY_CODES[Constants.DAY_SUN];
			}
		}
		String timePatternLookupString = days+"x"+(clazz.getSchedulingSubpart().getMinutesPerWk()/timeObject.getDays().size()+"x"+timeObject.getStartPeriod().toString());
		return(timePatterns.get(timePatternLookupString));
	}
	protected abstract boolean handleCustomClassChildElements(Element classElement, InstrOfferingConfig ioc, Class_ clazz)  throws Exception ;
	protected abstract boolean handleCustomInstrOffrConfigChildElements(InstrOfferingConfig instrOfferingConfig, Element instrOfferingConfigElement)  throws Exception ;

	private Vector<Meeting> getMeetings(Date startDate, Date stopDate, String pattern, TimeObject meetingTime, Vector<Room> rooms, Vector<NonUniversityLocation> locations){
		if (meetingTime != null){
			Meeting meeting = meetingTime.asMeeting();
            meeting.setStatus(Meeting.Status.APPROVED);
			meeting.setApprovalDate(Calendar.getInstance().getTime());
			
			Calendar startDateCal = Calendar.getInstance();
			startDateCal.setTime(startDate);
			Calendar stopDateCal = Calendar.getInstance();
			stopDateCal.setTime(stopDate);
			int index = 0;
			Vector<Meeting> meetingsForDates = new Vector<Meeting>();
			while (!startDateCal.after(stopDateCal)){
				if (meetingTime.getDays().contains(startDateCal.get(Calendar.DAY_OF_WEEK)) && pattern.charAt(index) == '1'){
					Meeting dateMeeting = (Meeting)meeting.clone();
					dateMeeting.setMeetingDate(startDateCal.getTime());
					meetingsForDates.add(dateMeeting);
				}
				index++;
				startDateCal.add(Calendar.DAY_OF_MONTH, 1);
			}
			
			if ((rooms == null || rooms.isEmpty()) && (locations == null || locations.isEmpty())){
				return(meetingsForDates);
			}
			
			Vector<Meeting> meetingsForLocations = new Vector<Meeting>();
			if (rooms != null){
				for (Iterator<Room> rIt = rooms.iterator(); rIt.hasNext(); ){
					Room r = (Room) rIt.next();
					for(Iterator<Meeting> dmIt = meetingsForDates.iterator(); dmIt.hasNext(); ){
						Meeting dateMeeting = (Meeting) dmIt.next();
						Meeting roomMeeting = (Meeting) dateMeeting.clone();
						roomMeeting.setLocationPermanentId(r.getPermanentId());
						meetingsForLocations.add(roomMeeting);
					}
				}
			}
			if (locations != null){
				for (Iterator<NonUniversityLocation> rIt = locations.iterator(); rIt.hasNext(); ){
					NonUniversityLocation nul = (NonUniversityLocation) rIt.next();
					for(Iterator<Meeting> dmIt = meetingsForDates.iterator(); dmIt.hasNext(); ){
						Meeting dateMeeting = (Meeting) dmIt.next();
						Meeting roomMeeting = (Meeting) dateMeeting.clone();
						roomMeeting.setLocationPermanentId(nul.getPermanentId());
						meetingsForLocations.add(roomMeeting);
					}
				}

			}
			if (!meetingsForLocations.isEmpty()){
				return(meetingsForLocations);
			} 
		}
		return(null);

	}
	private Vector<Meeting> getMeetings(DatePattern dp, TimeObject meetingTime, Vector<Room> rooms, Vector<NonUniversityLocation> locations){
		return(getMeetings(dp.getStartDate(), dp.getEndDate(), dp.getPattern(), meetingTime, rooms, locations));
	}
	
	private void addDistributionPref(Vector<String> classIds, Class_ clazz, DistributionType distributionType) throws Exception{
		if (classIds.size() <= 1){
			throw (new Exception("There must be at least two classes to have a meets with distribution preference: " + clazz.getClassLabel()));
		}
		Class_ c = null;
		Vector<String> tmpClassIds = new Vector<String>();
		tmpClassIds.addAll(classIds);
		Vector<Class_> classes = new Vector<Class_>();
		String externalId = null;
		for (Iterator<?> eIt = classIds.iterator(); eIt.hasNext();){
			externalId = (String) eIt.next();
			if (externalId.equals(clazz.getExternalUniqueId())){
				classes.add(clazz);
				tmpClassIds.remove(externalId);
			} else {
				 c = findClassForExternalUniqueId(externalId);
				 if (c == null){
					 break;
				 } else {
					 classes.add(c);
					 tmpClassIds.remove(externalId);
				 }
			}
		}
		if (!tmpClassIds.isEmpty()){
			addNote("\t not all classes for this meets with pref exist yet, will add it later:" + clazz.getClassLabel());
		} else {
			DistributionPref dp = new DistributionPref();
			dp.setDistributionType(distributionType);
			dp.setGrouping(DistributionPref.sGroupingNone);
			dp.setPrefLevel(requiredPrefLevel);
			dp.setOwner(clazz.getSchedulingSubpart().getControllingDept());
			for (Iterator<Class_> cIt = classes.iterator(); cIt.hasNext();){
				c = (Class_) cIt.next();
				DistributionObject distObj = new DistributionObject();
				distObj.setDistributionPref(dp);
				distObj.setPrefGroup(c);
				dp.addTodistributionObjects(distObj);
				c.addTodistributionObjects(distObj);
			}
			getHibSession().save(dp);
			getHibSession().flush();
			getHibSession().refresh(dp);
	        ChangeLog.addChange(getHibSession(), getManager(), session, dp, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.CREATE, null, clazz.getSchedulingSubpart().getControllingDept());       
		}	
		
	}
	
	private boolean addUpdateClassEvent(Class_ c, TimeObject meetingTime, Vector<Room> rooms, Vector<NonUniversityLocation> locations) {
		return(addUpdateClassEvent(c, getMeetings(c.effectiveDatePattern(), meetingTime, rooms, locations)));
	}
	
	private boolean addUpdateClassEvent(Class_ c, Vector<Meeting> meetings) {
		boolean changed = false;
		Date approvedTime = new Date();
		ClassEvent origEvent = c.getEvent();
		if(meetings != null && !meetings.isEmpty() && origEvent==null){
			ClassEvent newEvent = new ClassEvent();
			newEvent.setClazz(c); c.setEvent(newEvent);
			newEvent.setMaxCapacity(c.getMaxExpectedCapacity());
			newEvent.setMinCapacity(c.getExpectedCapacity());
			newEvent.setEventName(c.getSchedulingSubpart().getInstrOfferingConfig().getCourseName() + " " + c.getSchedulingSubpart().getItype().getAbbv().trim() + " " + c.getClassSuffix());
			for(Iterator<Meeting> mIt = meetings.iterator(); mIt.hasNext(); ){
				Meeting meeting = (Meeting) mIt.next();
				meeting.setEvent(newEvent);
				meeting.setStatus(Meeting.Status.APPROVED);
				meeting.setApprovalDate(approvedTime);
				newEvent.addTomeetings(meeting);
			}
			getHibSession().save(newEvent);
			assignmentHelper.createAssignment(newEvent);
			changed = true; 
			addNote("\tdid not find matching event, added new event: " + c.getSchedulingSubpart().getInstrOfferingConfig().getCourseName() + " " + c.getSchedulingSubpart().getItype().getAbbv().trim() + " " + c.getClassSuffix());
			ChangeLog.addChange(getHibSession(), getManager(), session, newEvent, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.CREATE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
		} else if (origEvent!=null) {
			if (!origEvent.getEventName().equals(c.getSchedulingSubpart().getInstrOfferingConfig().getCourseName() + " " + c.getSchedulingSubpart().getItype().getAbbv().trim() + " " + c.getClassSuffix())){
				origEvent.setEventName(c.getSchedulingSubpart().getInstrOfferingConfig().getCourseName() + " " + c.getSchedulingSubpart().getItype().getAbbv().trim() + " " + c.getClassSuffix());
				changed = true;
				addNote("\tevent name changed");
			}
			if (origEvent.getMinCapacity() != null && c.getExpectedCapacity() != null && !origEvent.getMinCapacity().equals(c.getExpectedCapacity())
					|| (origEvent.getMinCapacity() != null && c.getExpectedCapacity() == null)
					|| (origEvent.getMinCapacity() == null && c.getExpectedCapacity() != null)){
				origEvent.setMinCapacity(c.getExpectedCapacity());
				changed = true;
				addNote("\tevent minimum capacity changed.");
			}
			if (origEvent.getMaxCapacity() != null && c.getMaxExpectedCapacity() != null && !origEvent.getMaxCapacity().equals(c.getMaxExpectedCapacity())
					|| (origEvent.getMaxCapacity() != null && c.getMaxExpectedCapacity() == null)
					|| (origEvent.getMaxCapacity() == null && c.getMaxExpectedCapacity() != null)){
				origEvent.setMaxCapacity(c.getMaxExpectedCapacity());
				changed = true;
				addNote("\tevent maximum capacity changed.");
			}
            Set origMeetings = new TreeSet<Object>();
            origMeetings.addAll(origEvent.getMeetings());
            if (meetings != null){
                for(Iterator<Meeting> nmIt = meetings.iterator(); nmIt.hasNext(); ){
                    Meeting newMeeting = (Meeting) nmIt.next();
                    boolean found = false;
                    for(Iterator<?> omIt = origMeetings.iterator(); omIt.hasNext(); ){
                        Meeting origMeeting = (Meeting) omIt.next();
                        if(isSameMeeting(origMeeting, newMeeting)){
                            found = true;
                            origMeetings.remove(origMeeting);
                            break;
                        }
                    }
                    if (!found){
                        addNote("\tdid not find matching meeting, adding new meeting to event: " + c.getClassLabel());
                        newMeeting.setEvent(origEvent);
                        newMeeting.setStatus(Meeting.Status.APPROVED);
                        newMeeting.setApprovalDate(approvedTime);
                        origEvent.addTomeetings(newMeeting);
                        changed = true;
                    }
                }
            }
            if (!origMeetings.isEmpty()){
                addNote("\tsome existing meetings did not have matches in input, deleted them: " + c.getClassLabel());
                for(Iterator<?> mIt = origMeetings.iterator(); mIt.hasNext(); ){
                    Meeting m = (Meeting) mIt.next();
                    origEvent.getMeetings().remove(m);
                    m.setEvent(null);
                    ChangeLog.addChange(getHibSession(), getManager(), session, m, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.DELETE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
                    getHibSession().delete(m);
                    changed = true;
                }
            }
            if (changed){
                assignmentHelper.createAssignment(origEvent);
                ChangeLog.addChange(getHibSession(), getManager(), session, origEvent, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.UPDATE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());   
                getHibSession().update(origEvent);
            }
		}
				
		return(changed);
	}

	private boolean elementSubpart(Element element, InstrOfferingConfig ioc, SchedulingSubpart parentSubpart, HashSet<SchedulingSubpart> allExistingSubparts) throws Exception {
		boolean changed = false;
		HashMap<String, SchedulingSubpart> thisLevelSubparts = new HashMap<String, SchedulingSubpart>();
		if(parentSubpart == null){
			allExistingSubparts = new HashSet<SchedulingSubpart>();
			if (ioc.getSchedulingSubparts() != null){
				for (Iterator<?> it = ioc.getSchedulingSubparts().iterator(); it.hasNext();){
					SchedulingSubpart ss = (SchedulingSubpart) it.next();
					allExistingSubparts.add(ss);
					if (ss.getParentSubpart() == null){
						thisLevelSubparts.put(ss.getItype().getAbbv().trim()+(ss.getSchedulingSubpartSuffixCache() == null?"":(ss.getSchedulingSubpartSuffixCache().equals("-")?"":ss.getSchedulingSubpartSuffixCache())), ss);
					}
				}
			}
		} else {
			if (parentSubpart.getChildSubparts() != null){
				for (Iterator<?> it = parentSubpart.getChildSubparts().iterator(); it.hasNext();){
					SchedulingSubpart ss = (SchedulingSubpart) it.next();
					thisLevelSubparts.put(ss.getItype().getAbbv().trim()+(ss.getSchedulingSubpartSuffixCache() == null?"":(ss.getSchedulingSubpartSuffixCache().equals("-")?"":ss.getSchedulingSubpartSuffixCache())), ss);
				}
			}			
		}

		String elementName = "subpart";
		if(element.element(elementName) != null) {
			for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element subpart = (Element) it.next();
				boolean isAdd = false;
				Integer minPerWeek = getOptionalIntegerAttribute(subpart, "minPerWeek");
				if (minPerWeek == null){
					minPerWeek = new Integer(0);
				}
				String typeStr = getRequiredStringAttribute(subpart, "type", elementName);
				ItypeDesc itype = findItypeForString(typeStr);
				String type = "";
				if (itype != null){
					type = itype.getAbbv().trim();
				}
				String suffix = getOptionalStringAttribute(subpart, "suffix");
				if(suffix != null){
					suffix = suffix.trim();
				} else {
					suffix = "";
				}				
				SchedulingSubpart ss = null;
				if (thisLevelSubparts.containsKey(type+suffix)){
					ss = (SchedulingSubpart)thisLevelSubparts.get(type+suffix);
					allExistingSubparts.remove(ss);
					if (thisLevelSubparts.containsKey(type+suffix)){
						thisLevelSubparts.remove(type+suffix);
					} 
				} else if (parentSubpart != null && parentSubpart.getItype().getAbbv().trim().equals(type) && suffix.length() == 0 && thisLevelSubparts.containsKey(type+"a")){
					ss = (SchedulingSubpart)thisLevelSubparts.get(type+"a");
					allExistingSubparts.remove(ss);
					if (thisLevelSubparts.containsKey(type+"a")){
						thisLevelSubparts.remove(type+"a");
					} 
				} else {
					ss = new SchedulingSubpart();
					ss.setItype(itype);
					ss.setSchedulingSubpartSuffixCache(suffix);
					ss.setInstrOfferingConfig(ioc);
					ss.setSession(ioc.getSession());
					ss.setCourseName(ioc.getInstructionalOffering().getCourseName());
					ioc.addToschedulingSubparts(ss);
					if(parentSubpart != null){
						ss.setParentSubpart(parentSubpart);
						parentSubpart.addTochildSubparts(ss);
					}
					changed = true;
					isAdd = true;
					addNote("\tdid not find existing matching scheduling subpart, created new one: " + ss.getItypeDesc());
				}
				if (ss.isAutoSpreadInTime() == null){
					ss.setAutoSpreadInTime(new Boolean(true));
				}
				
				if (ss.isStudentAllowOverlap() == null){
					ss.setStudentAllowOverlap(new Boolean(false));
				}
				
				if (ss.getMinutesPerWk() == null || !ss.getMinutesPerWk().equals(minPerWeek)){
					ss.setMinutesPerWk(minPerWeek);
					addNote("\tsubpart minutes per week changed");
					changed = true;
				}
				
				if (parentSubpart != null && ss.getParentSubpart() == null){
					ss.setParentSubpart(parentSubpart);
					parentSubpart.addTochildSubparts(ss);
					addNote("\tsubpart now has parent");
					changed = true;
				} else if (parentSubpart != null && !ss.getParentSubpart().getUniqueId().equals(parentSubpart.getUniqueId())){
					ss.getParentSubpart().getChildSubparts().remove(ss);
					ss.setParentSubpart(parentSubpart);
					parentSubpart.addTochildSubparts(ss);
					addNote("\tsubpart has different parent");
					changed = true;
				} else if (parentSubpart == null && ss.getParentSubpart() != null){
					ss.getParentSubpart().getChildSubparts().remove(ss);
					ss.setParentSubpart(null);
					addNote("\tsubpart no longer has parent");
					changed = true;
				}
				
				if (elementSubpartCredit(subpart, ss)){
					addNote("\tsubpart credit changed");
					changed = true;
				}
				
				if (changed){
					this.getHibSession().saveOrUpdate(ss);
				}
				
				if (elementSubpart(subpart, ioc, ss, allExistingSubparts)){
					changed = true;
				}	
				if (changed){
					ChangeLog.addChange(getHibSession(), getManager(), session, ss, ChangeLog.Source.DATA_IMPORT_OFFERINGS, (isAdd?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), ioc.getControllingCourseOffering().getSubjectArea(), ioc.getDepartment());
				}
			}
		} 
		if (!thisLevelSubparts.isEmpty()){
			addNote("\tnot all subparts at this level had matches, deleted them");
			for (Iterator<?> it = thisLevelSubparts.values().iterator(); it.hasNext();){
				SchedulingSubpart ss = (SchedulingSubpart) it.next();
				allExistingSubparts.remove(ss);
				deleteSchedulingSubpart(ss);
			}
			if(parentSubpart != null) {
				this.getHibSession().saveOrUpdate(parentSubpart);
				ChangeLog.addChange(getHibSession(), getManager(), session, parentSubpart, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.UPDATE, ioc.getControllingCourseOffering().getSubjectArea(), ioc.getDepartment());
			}
			changed = true;
		}
		if (parentSubpart == null && !allExistingSubparts.isEmpty()){
			addNote("\tnot all existing subparts had matches, deleted them");
			for (Iterator<?> it = allExistingSubparts.iterator(); it.hasNext();){
				SchedulingSubpart ss = (SchedulingSubpart) it.next();
				if(ss.getParentSubpart() != null){
					ss.getParentSubpart().getChildSubparts().remove(ss);
				}
				deleteSchedulingSubpart(ss);
			}
			changed = true;
		}
		return(changed);
	}

	private void deleteDistributionPref(DistributionPref dp){
		addNote("\tdeleting meets with distribution preference:  " + dp.preferenceText(true, false, "", ", ", ""));
        HashSet relatedInstructionalOfferings = new HashSet();
        Department dept = (Department) dp.getOwner();
        dept.getPreferences().remove(dp);
		for (Iterator i=dp.getDistributionObjects().iterator();i.hasNext();) {
			DistributionObject dObj = (DistributionObject)i.next();
			PreferenceGroup pg = dObj.getPrefGroup();
            relatedInstructionalOfferings.add((pg instanceof Class_ ?((Class_)pg).getSchedulingSubpart():(SchedulingSubpart)pg).getInstrOfferingConfig().getInstructionalOffering());
			pg.getDistributionObjects().remove(dObj);
			getHibSession().saveOrUpdate(pg);
		}
        ChangeLog.addChange(getHibSession(), getManager(), session, dp, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.DELETE, null, dept);       
        getHibSession().delete(dp);
        getHibSession().saveOrUpdate(dept);
        
//        for (Iterator i=relatedInstructionalOfferings.iterator();i.hasNext();) {
//            InstructionalOffering io = (InstructionalOffering)i.next();
//            ChangeLog.addChange(getHibSession(), getManager(), session, io, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.DELETE, io.getControllingCourseOffering().getSubjectArea(), null);
//        }
	}

	private void deleteCourseOffering(CourseOffering co){
		//TODO: may need to add a hook for deleting custom data from the database, could also just add an 
		// abstract cleanup method that is run at the end and let it take care of any customized things
		// that might need to be removed after their corresponding course offering has been deleted
		InstructionalOffering io = co.getInstructionalOffering();
		if (io.getCourseOfferings().size() == 1){
			deleteInstructionalOffering(io);
		} else if (io.getCourseOfferings().size() > 1) {
			io.getCourseOfferings().remove(co);
			if (co.isIsControl().booleanValue()){
				CourseOffering newControl = (CourseOffering) io.getCourseOfferings().iterator().next();
				newControl.setIsControl(new Boolean(true));
			}
			co.setInstructionalOffering(null);
			existingCourseOfferings.remove(co.getUniqueId());
			this.getHibSession().delete(co);
		} else {
			existingCourseOfferings.remove(co.getUniqueId());
			this.getHibSession().delete(co);
		}
	}
	
	private void deleteInstructionalOffering(InstructionalOffering io) {
		// remove the instructionalOffering from the list of existing instructional offerings
		existingInstructionalOfferings.remove(io.getUniqueId());
		
		// remove all course offering uniqueIds from the existing course offerings list 
		for (Iterator<?> it = io.getCourseOfferings().iterator(); it.hasNext(); ){
			CourseOffering co = (CourseOffering) it.next();
			existingCourseOfferings.remove(co.getUniqueId());
		}
		
		// remove all class uniqueIds from the existing classes list and get rid of any dependent objects so the
		//    class can be deleted
		if (io.getInstrOfferingConfigs() != null) {
			for (Iterator<?> iocIt = io.getInstrOfferingConfigs().iterator(); iocIt.hasNext(); ){
				InstrOfferingConfig ioc = (InstrOfferingConfig) iocIt.next();
				if (ioc.getSchedulingSubparts() != null){
					for(Iterator<?> ssIt = ioc.getSchedulingSubparts().iterator(); ssIt.hasNext(); ){
						SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
						if (ss.getClasses() != null){
							for (Iterator<?> cIt = ss.getClasses().iterator(); cIt.hasNext();){
								Class_ c = (Class_) cIt.next();
								existingClasses.remove(c.getUniqueId());
								c.deleteAllDependentObjects(this.getHibSession(), false);
							}
						}
					}					
				}
			}
		}

		ChangeLog.addChange(getHibSession(), getManager(), session, io, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.DELETE, (io.getControllingCourseOffering() == null?null:io.getControllingCourseOffering().getSubjectArea()), (io.getControllingCourseOffering() == null?null:io.getControllingCourseOffering().getDepartment()));
		this.getHibSession().delete(io);
		flush(true);
	}
	
	private void deleteInstrOffrConfig(InstrOfferingConfig ioc) {
		
		// remove all class uniqueIds from the existing classes list and get rid of any dependent objects so the
		//    class can be deleted
		if (ioc.getSchedulingSubparts() != null){
			for(Iterator<?> ssIt = ioc.getSchedulingSubparts().iterator(); ssIt.hasNext(); ){
				SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
				if (ss.getClasses() != null){
					for (Iterator<?> cIt = ss.getClasses().iterator(); cIt.hasNext();){
						Class_ c = (Class_) cIt.next();
						existingClasses.remove(c.getUniqueId());
						c.deleteAllDependentObjects(this.getHibSession(), false);
					}
				}
			}					
		}
		ioc.getInstructionalOffering().getInstrOfferingConfigs().remove(ioc);
		ChangeLog.addChange(getHibSession(), getManager(), session, ioc, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.DELETE, ioc.getControllingCourseOffering().getSubjectArea(), ioc.getControllingCourseOffering().getDepartment());
		this.getHibSession().delete(ioc);
	}
	
	private void deleteSchedulingSubpart(SchedulingSubpart ss) {
		
		if (ss.getChildSubparts() != null){
			for(Iterator cssIt = ss.getChildSubparts().iterator(); cssIt.hasNext(); ){
				SchedulingSubpart css = (SchedulingSubpart) cssIt.next();
				deleteSchedulingSubpart(css);
			}
		}
		// remove all class uniqueIds from the existing classes list and get rid of any dependent objects so the
		//    class can be deleted
		if (ss.getClasses() != null){
			for (Iterator<?> cIt = ss.getClasses().iterator(); cIt.hasNext();){
				Class_ c = (Class_) cIt.next();
				existingClasses.remove(c.getUniqueId());
				c.deleteAllDependentObjects(this.getHibSession(), false);
				
			}
		}
		
		InstrOfferingConfig ioc = ss.getInstrOfferingConfig();
		
		SchedulingSubpart parentSubpart = ss.getParentSubpart();
		if(parentSubpart != null){
			parentSubpart.getChildSubparts().remove(ss);
			ss.setParentSubpart(null);
		}
		ioc.getSchedulingSubparts().remove(ss);	
		this.getHibSession().update(ioc);
		ChangeLog.addChange(getHibSession(), getManager(), session, ss, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.DELETE, ioc.getControllingCourseOffering().getSubjectArea(), ioc.getControllingCourseOffering().getDepartment());
//		this.getHibSession().delete(ss);
	}

	private void deleteClassInstructor(ClassInstructor ci){
		ci.getInstructor().getClasses().remove(ci);
		ci.getClassInstructing().getClassInstructors().remove(ci);
		Class_ clazz = ci.getClassInstructing();
		ci.setClassInstructing(null);
		ci.setInstructor(null);
		ChangeLog.addChange(getHibSession(), getManager(), session, ci, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.DELETE, clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), clazz.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
		this.getHibSession().delete(ci);
	}

	private void deleteClass(Class_ c){
		c.deleteAllDependentObjects(this.getHibSession(), false);
		c.getSchedulingSubpart().getClasses().remove(c);
		SchedulingSubpart ss = c.getSchedulingSubpart();
		ss.getClasses().remove(c);
		ChangeLog.addChange(getHibSession(), getManager(), session, c, ChangeLog.Source.DATA_IMPORT_OFFERINGS, ChangeLog.Operation.DELETE, ss.getControllingCourseOffering().getSubjectArea(), ss.getControllingCourseOffering().getDepartment());
		existingClasses.remove(c.getUniqueId());
		this.getHibSession().delete(c);
	}
	
	private CourseOffering findExistingCourseOffering(CourseOffering courseOffering, Long sessionId){
		CourseOffering existingCo = null;
		if (courseOffering.getExternalUniqueId() != null) {
			existingCo = findCrsOffrForExternalId(courseOffering.getExternalUniqueId(), sessionId);
		}
        if (existingCo == null) {
        	if (courseNumbersMustBeUnique){
        		existingCo = findCrsOffrForSubjCrs(courseOffering.getSubjectArea().getSubjectAreaAbbreviation(), courseOffering.getCourseNbr(), sessionId);
        	} else {
          		existingCo = findCrsOffrForSubjCrsTitle(courseOffering.getSubjectArea().getSubjectAreaAbbreviation(), courseOffering.getCourseNbr(), courseOffering.getTitle(), sessionId);
          	}
		}
		return(existingCo);
	}
	private CourseOffering findCrsOffrForExternalId(String externalId, Long sessionId){
		return(CourseOffering) this.
		getHibSession().
		createQuery("select distinct co from CourseOffering as co where co.externalUniqueId=:externalId and co.subjectArea.session.uniqueId=:sessionId").
		setLong("sessionId", sessionId.longValue()).
		setString("externalId", externalId).
		setCacheable(true).
		uniqueResult();
	}
	
	private CourseOffering findCrsOffrForSubjCrsTitle(String subjectAbbv, String crsNbr, String title, Long sessionId){
		return(CourseOffering) this.
		getHibSession().
		createQuery("select distinct co from CourseOffering as co where co.subjectArea.session.uniqueId=:sessionId and co.subjectArea.subjectAreaAbbreviation=:subjectAbbv and co.courseNbr=:courseNbr and co.title=:title").
		setLong("sessionId", sessionId.longValue()).
		setString("subjectAbbv", subjectAbbv).
		setString("courseNbr", crsNbr).
		setString("title", title).
		setCacheable(true).
		uniqueResult();
	}

	private CourseOffering findCrsOffrForSubjCrs(String subjectAbbv, String crsNbr, Long sessionId){
		return(CourseOffering) this.
		getHibSession().
		createQuery("select distinct co from CourseOffering as co where co.subjectArea.session.uniqueId=:sessionId and co.subjectArea.subjectAreaAbbreviation=:subjectAbbv and co.courseNbr=:courseNbr").
		setLong("sessionId", sessionId.longValue()).
		setString("subjectAbbv", subjectAbbv).
		setString("courseNbr", crsNbr).
		setCacheable(true).
		uniqueResult();
	}

	private InstructionalOffering findInstrOffrForExternalId(String externalId, Long sessionId){
		return (InstructionalOffering) this.
		getHibSession().
		createQuery("select distinct io from InstructionalOffering as io where io.externalUniqueId=:externalId and io.session.uniqueId=:sessionId").
		setLong("sessionId", sessionId.longValue()).
		setString("externalId", externalId).
		setCacheable(true).
		setFlushMode(FlushMode.MANUAL).
		uniqueResult();
	}
	
	private InstructionalOffering findInstrOffrForUniqueId(Long uniqueId){
		return (InstructionalOffering) this.
		getHibSession().
		createQuery("select distinct io from InstructionalOffering as io where io.uniqueId=:uniqueId").
		setLong("uniqueId", uniqueId.longValue()).
		setCacheable(true).
		uniqueResult();
	}
	
	private CourseOffering findCourseOffrForUniqueId(Long uniqueId){
		return (CourseOffering) this.
		getHibSession().
		createQuery("select distinct co from CourseOffering as co where co.uniqueId=:uniqueId").
		setLong("uniqueId", uniqueId.longValue()).
		setCacheable(true).
		uniqueResult();
	}
	
	private Class_ findClassForUniqueId(Long uniqueId){
		return (Class_) this.
		getHibSession().
		createQuery("select distinct c from Class_ as c where c.uniqueId=:uniqueId").
		setLong("uniqueId", uniqueId.longValue()).
		setCacheable(true).
		uniqueResult();
	}
	
	private Class_ findClassForExternalUniqueId(String externalUniqueId){
		return (Class_) this.
		getHibSession().
		createQuery("select distinct c from Class_ as c where c.externalUniqueId=:externalUniqueId and c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId=:sessionId" ).
		setString("externalUniqueId", externalUniqueId).
		setLong("sessionId", session.getUniqueId().longValue()).
		setCacheable(true).
		uniqueResult();
	}
	
	private Room findRoom(String id, String building, String roomNbr){
		Room room = null;
		if (id != null) {
			room = (Room) this.
			getHibSession().
			createQuery("select distinct r from Room as r where r.externalUniqueId=:externalId and r.building.session.uniqueId=:sessionId").
			setLong("sessionId", session.getUniqueId().longValue()).
			setString("externalId", id).
			setCacheable(true).
			uniqueResult();
		} 
		if (room == null) {
			room = (Room) this.
			getHibSession().
			createQuery("select distinct r from Room as r where r.roomNumber=:roomNbr and r.building.abbreviation = :building and r.session.uniqueId=:sessionId").
			setLong("sessionId", session.getUniqueId().longValue()).
			setString("building", building).
			setString("roomNbr", roomNbr).
			setCacheable(true).
			uniqueResult();
			if (id != null && room != null && room.getExternalUniqueId() != null && !room.getExternalUniqueId().equals(id)){
				room = null;
			}
		}
		return(room);
	}
	
	private NonUniversityLocation findNonUniversityLocation(String id, String name, Class_ c){
		NonUniversityLocation location = null;
		List<?> possibleLocations = findNonUniversityLocationsWithIdOrName(id, name);
		if (possibleLocations != null){
			for(Iterator<?> lIt = possibleLocations.iterator(); lIt.hasNext(); ){
				NonUniversityLocation l = (NonUniversityLocation) lIt.next();
				if (l.getRoomDepts() != null) {
					for(Iterator<?> rdIt = l.getRoomDepts().iterator(); rdIt.hasNext(); ){
						RoomDept rd = (RoomDept) rdIt.next();
						if (rd.getDepartment().getUniqueId().equals(c.getSchedulingSubpart().getControllingDept().getUniqueId())){
							location = l;
							break;
						}
					}
				}
				if(location != null){
					break;
				}
			}
		}
		return(location);
	}
	
	private void loadSubjectAreas(Long sessionId) {
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
	
	private void loadItypes() {
		Set<?> itypeDescs = ItypeDesc.findAll(false);
		for (Iterator<?> it = itypeDescs.iterator(); it.hasNext();) {
			ItypeDesc itype = (ItypeDesc) it.next();
			itypes.put(itype.getAbbv().trim(), itype);
			itypesBySisRef.put(itype.getSis_ref(), itype);
		}
	}
	
	private ItypeDesc findItypeForString(String itypeRef){
		ItypeDesc itype = itypes.get(itypeRef);
		if (itype == null){
			itype = itypesBySisRef.get(itypeRef);
		}
		return(itype);
	}
	private void loadExistingInstructionalOfferings(Long sessionId) throws Exception {
		
		for (Iterator<?> it = InstructionalOffering.findAll(sessionId).iterator(); it.hasNext();) {
			InstructionalOffering io = (InstructionalOffering) it.next();
			existingInstructionalOfferings.add(io.getUniqueId());			
		}
	}

	private void loadExistingCourseOfferings(Long sessionId) throws Exception {
		
		for (Iterator<?> it = CourseOffering.findAll(sessionId).iterator(); it.hasNext();) {
			CourseOffering courseOffering = (CourseOffering) it.next();
			existingCourseOfferings.add(courseOffering.getUniqueId());
		}
	}
	
	private void loadMeetsWithDistributionType() {
		meetsWithType = (DistributionType) this.getHibSession().createQuery("from DistributionType dt where dt.reference = 'MEET_WITH'").uniqueResult();
	}
	private void loadCanShareRoomDistributionType() {
		canShareRoomType = (DistributionType) this.getHibSession().createQuery("from DistributionType dt where dt.reference = 'CAN_SHARE_ROOM'").uniqueResult();
	}
	private void loadRequiredPrefLevel() {
		requiredPrefLevel = (PreferenceLevel) this.getHibSession().createQuery("from PreferenceLevel pl where pl.prefName = 'Required'").uniqueResult();
	}
	
	private void loadExistingClasses(Long sessionId) throws Exception {
 		for (Iterator<?> it = Class_.findAll(sessionId).iterator(); it.hasNext();) {
			Class_ c = (Class_) it.next();
			if (c.getExternalUniqueId() != null){
				existingClasses.add(c.getUniqueId());
			} else {
				existingClasses.add(c.getUniqueId());				
			} 
		}
	}

	@Override
	protected String getEmailSubject() {
		return("Course Offering Import Results - " + session.getAcademicYearTerm());
	}

	protected class ImportCourseOffering {

		/**
		 * 
		 */
		private CourseOffering courseOffering;
		private Element element;

		public ImportCourseOffering(CourseOffering courseOffering, Element element) {
			this.courseOffering = courseOffering;
			this.element = element;
		}

		public CourseOffering getCourseOffering() {
			return courseOffering;
		}

		public void setCourseOffering(CourseOffering courseOffering) {
			this.courseOffering = courseOffering;
		}

		public Element getElement() {
			return element;
		}

		public void setElement(Element element) {
			this.element = element;
		}
		
		
		
	}
	
}
