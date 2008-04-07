/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime.org
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

import java.io.IOException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.dom4j.Element;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventType;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;


public class CourseOfferingImport extends BaseImport {

	HashSet<Long> existingInstructionalOfferings = new HashSet<Long>();
	HashSet<Long> existingCourseOfferings = new HashSet<Long>();
	HashSet<Long> existingClasses = new HashSet<Long>();
	HashMap<String, SubjectArea> subjectAreas = new HashMap<String, SubjectArea>();
	HashMap<String, ItypeDesc> itypes = new HashMap<String, ItypeDesc>();
	private EventType classType = null;
	private DistributionType meetsWithType = null;
	Vector<String> offeringNotes = new Vector<String>();
	Vector<String> changeList = new Vector<String>();
	TimetableManager manager = null;
	Session session = null;
	String dateFormat = null;
	String timeFormat = null;
	boolean useMeetsWithElement = false;
	PreferenceLevel requiredPrefLevel = null;
	
	
	public void loadXml(Element rootElement, HttpServletRequest request) throws Exception {
		HttpSession httpSession = request.getSession();
        String userId = (String)httpSession.getAttribute("authUserExtId");
        User user = Web.getUser(httpSession);
        if (userId!=null) {
        	manager = TimetableManager.findByExternalId(userId);
        }
        if (manager==null && user!=null) {
            Debug.warning("No authenticated user defined, using "+user.getName());
        	manager = TimetableManager.getManager(user);
        }
        
		loadXml(rootElement);
	}
	
	public void loadXml(Element rootElement) throws Exception {
		int changeCount = 0;
		try {
			String rootElementName = "offerings";
	        if (!rootElement.getName().equalsIgnoreCase(rootElementName)) {
	        	throw new Exception("Given XML file is not a Course Offerings load file.");
	        }
	        String campus = getRequiredStringAttribute(rootElement, "campus", rootElementName);
	        String year   = getRequiredStringAttribute(rootElement, "year", rootElementName);
	        String term   = getRequiredStringAttribute(rootElement, "term", rootElementName);
	        dateFormat = getOptionalStringAttribute(rootElement, "dateFormat");
	        timeFormat = getOptionalStringAttribute(rootElement, "timeFormat");
	        String created = getOptionalStringAttribute(rootElement, "created");
	        if(timeFormat == null){
	        	timeFormat = "HHmm";
	        }

	        Boolean useMeetsWith = getOptionalBooleanAttribute(rootElement, "useMeetsWith");
	        if (useMeetsWith != null && useMeetsWith.booleanValue()){
	        	useMeetsWithElement = true;
	        }
	        beginTransaction();
	
	        if (manager == null){
	        	manager = findDefaultManager();
	        }
	        session = findSession(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }
	
	        loadItypes();
	        loadSubjectAreas(session.getUniqueId());
	        loadExistingInstructionalOfferings(session.getUniqueId());
	        loadExistingCourseOfferings(session.getUniqueId());
	        loadExistingClasses(session.getUniqueId());
	        loadClassEventType();
	        loadRequiredPrefLevel();
	        loadMeetsWithDistributionType();
		    
	        if (created != null) {
		        addNote("Loading offerings XML file created on: " + created);
				ChangeLog.addChange(getHibSession(), manager, session, session, created, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.UPDATE, null, null);
				updateChangeList(true);
	        }
			String offeringElementName = "offering";
	        for ( Iterator<?> it = rootElement.elementIterator(); it.hasNext(); ) {
	            Element element = (Element) it.next();
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
	            	ArrayList<CourseOffering> courses = getCourses(element);
	            	if (courses == null || courses.isEmpty()){
	            		throw new Exception("Expected an 'offering' to have at least one course.");
	            	}
	            	if(courses.size() == 1){
	            		CourseOffering co = (CourseOffering) courses.get(0);
	            		CourseOffering existingCourseOffering = findExistingCourseOffering(co, session.getUniqueId());
	            		if(existingCourseOffering == null){
	            			io = new InstructionalOffering();
	    	            	io.setSession(session);
	    	            	io.setExternalUniqueId(externalId);
	            		} else {
	            			io = existingCourseOffering.getInstructionalOffering();
	            			existingIo = true;
	            		}
	            	} else {
	            		HashSet<InstructionalOffering> possibleOfferings = new HashSet<InstructionalOffering>();
	            		for(Iterator<CourseOffering> coIt = courses.iterator(); coIt.hasNext();){
	            			CourseOffering co = (CourseOffering) coIt.next();
	            			CourseOffering existingCourseOffering = findExistingCourseOffering(co, session.getUniqueId());
		            		if (existingCourseOffering != null){
		            			possibleOfferings.add(existingCourseOffering.getInstructionalOffering());
		            		}
	            		}
	            		if (possibleOfferings.isEmpty()){
	            			io = new InstructionalOffering();
	    	            	io.setSession(session);
	    	            	io.setExternalUniqueId(externalId);
	            		} else if (possibleOfferings.size() == 1){
	            			io = (InstructionalOffering) possibleOfferings.iterator().next();
	            			existingIo = true;
	            		} else {
	            			CourseOffering control = null;
	            			for(Iterator<CourseOffering> coIt = courses.iterator(); coIt.hasNext(); ){
	            				CourseOffering co = (CourseOffering) coIt.next();	            				
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
    			}

	            if (existingIo && action != null && action.equalsIgnoreCase("delete")){
	            	addNote("Deleted instructional offering: " + io.getCourseName());
	            	deleteInstructionalOffering(io);
	            	changeCount++;
	            } else if (!existingIo && action != null && action.equalsIgnoreCase("delete")){
	            	continue;
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
	            	continue;
	            }
	             
	            flush(true);
	        }
	        
	        flush(true);	        
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
	        if (existingClasses.size() > 0){
	           	HashSet<Long> deleteClasses = new HashSet<Long>();
            	deleteClasses.addAll(existingClasses);
               	addNote("Deleted Classes that were not in the input file:");
        	for(Iterator<Long> cIt = deleteClasses.iterator(); cIt.hasNext(); ){
	        		Long uniqueId = (Long) cIt.next();
		        	Class_ unusedC = findClassForUniqueId(uniqueId) ; 
		        	if (unusedC != null){
		        		addNote("\tDeleted: " + unusedC.getClassLabel());
			        	deleteClass(unusedC);
			        	changeCount++;
		        	}
	        	}
	        	flushIfNeeded(true);
	        	updateChangeList(true);
	        }
	        commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}	
		addNote("Records Changed: " + changeCount);
		updateChangeList(true);
		mailLoadResults();
	}
	
	private boolean isSameCourseOffering(CourseOffering originalCourseOffering, CourseOffering newCourseOffering){
		boolean isSame = false;
		if(originalCourseOffering.getExternalUniqueId() != null 
				&& originalCourseOffering.getExternalUniqueId().equals(newCourseOffering.getExternalUniqueId())){
			isSame = true;
		} else if (originalCourseOffering.getSubjectArea().getUniqueId().equals(newCourseOffering.getSubjectArea().getUniqueId())
				&& originalCourseOffering.getCourseNbr().equals(newCourseOffering.getCourseNbr())){
			isSame = true;
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
				&& originalMeeting.getStopPeriod().intValue() == newMeeting.getStopPeriod().intValue()
				&& originalMeeting.getEventType().equals(newMeeting.getEventType())){			
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

		if (elementDesignatorRequired(element, io)){
         	addNote("\tdesignator status changed");
			changed = true;
		}
		
		if (elementConsent(element, io)){
         	addNote("\tconsent changed");
			changed = true;
		}
		
		if (elementCourse(element, io, action)){
         	addNote("\tcourses changed");
			changed = true;
		}

		if (elementCourseCredit(element, io)){
         	addNote("\tcourse credit changed");
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
			ChangeLog.addChange(getHibSession(), manager, session, io, ChangeLog.Source.DATA_IMPORT, (action.equalsIgnoreCase("insert")?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), io.getControllingCourseOffering().getSubjectArea(), io.getDepartment());
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
		DatePattern dp = (DatePattern) this.
		getHibSession().
		createQuery("from DatePattern as d where d.session.uniqueId = :sessionId and d.pattern = :pattern and d.offset = :offset").
		setLong("sessionId", session.getUniqueId().longValue()).
		setString("pattern", pattern).
		setInteger("offset", offset).
		setCacheable(true).
		uniqueResult();
		
		if (dp == null){
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
			ChangeLog.addChange(getHibSession(), manager, session, dp, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.CREATE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
		}
		if (dp.isDefault()){
			return(null);
		} else {
			return(dp);
		}
		
	}

	private CourseOffering findCourseOfferingWithUniqueId(Long uniqueId, InstructionalOffering instructionalOffering){
		CourseOffering courseOffering = null;
		boolean found = false;
		if (instructionalOffering.getCourseOfferings() != null) {
			for (Iterator<?> it = instructionalOffering.getCourseOfferings().iterator(); it.hasNext() && !found;){
				courseOffering = (CourseOffering) it.next();
				if(courseOffering.getUniqueId().equals(uniqueId))
					found = true;
			}
		}
		if (found)
			return(courseOffering);
		else
			return(null);
	}
	
	private ArrayList<CourseOffering> getCourses(Element element) throws Exception {
		ArrayList<CourseOffering> courses = new ArrayList<CourseOffering>();
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
				courses.add(newCourseOffering);
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
			year = session.getYear();
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
	
	private class TimeObject {
		private Integer startPeriod;
		private Integer endPeriod;
		private Set<Integer> days;
		
		TimeObject(String startTime, String endTime, String daysOfWeek) throws Exception{
		
			startPeriod = str2Slot(startTime);
			endPeriod = str2Slot(endTime);
			if (startPeriod >= endPeriod){
				throw new Exception("Invalid time '"+startTime+"' must be before ("+endTime+").");
			}
			if (daysOfWeek == null || daysOfWeek.length() == 0){
				return;
			}
			setDaysOfWeek(daysOfWeek);	
		}
		
		private void setDaysOfWeek(String daysOfWeek){
			days = new TreeSet<Integer>();
			String tmpDays = daysOfWeek;
			if(tmpDays.contains("Th")){
				days.add(Calendar.THURSDAY);
				tmpDays = tmpDays.replace("Th", "..");
			}
			if(tmpDays.contains("R")){
				days.add(Calendar.THURSDAY);
				tmpDays = tmpDays.replace("R", "..");
			}
			if (tmpDays.contains("Su")){
				days.add(Calendar.SUNDAY);
				tmpDays = tmpDays.replace("Su", "..");
			}
			if (tmpDays.contains("U")){
				days.add(Calendar.SUNDAY);
				tmpDays = tmpDays.replace("U", "..");
			}
			if (tmpDays.contains("M")){
				days.add(Calendar.MONDAY);
				tmpDays = tmpDays.replace("M", ".");
			}
			if (tmpDays.contains("T")){
				days.add(Calendar.TUESDAY);
				tmpDays = tmpDays.replace("T", ".");
			}
			if (tmpDays.contains("W")){
				days.add(Calendar.WEDNESDAY);
				tmpDays = tmpDays.replace("W", ".");
			}
			if (tmpDays.contains("F")){
				days.add(Calendar.FRIDAY);
				tmpDays = tmpDays.replace("F", ".");
			}
			if (tmpDays.contains("S")){
				days.add(Calendar.SATURDAY);
				tmpDays = tmpDays.replace("S", ".");
			}						
		}
		
		public Integer getStartPeriod() {
			return startPeriod;
		}
		public void setStartPeriod(Integer startPeriod) {
			this.startPeriod = startPeriod;
		}
		public Integer getEndPeriod() {
			return endPeriod;
		}
		public void setEndPeriod(Integer endPeriod) {
			this.endPeriod = endPeriod;
		}
		public Set<Integer> getDays() {
			return days;
		}
		public void setDays(Set<Integer> days) {
			this.days = days;
		}
		
		public Meeting asMeeting(){
			Meeting meeting = new Meeting();
			
			meeting.setClassCanOverride(new Boolean(true));
			meeting.setStartOffset(new Integer(0));
			meeting.setStartPeriod(this.getStartPeriod());
			meeting.setStopOffset(new Integer(0));
			meeting.setStopPeriod(this.getEndPeriod());
			meeting.setEventType(classType);

			return(meeting);
		}
		public Integer str2Slot(String timeString) throws Exception {
			
			int slot = -1;
			try {
				Date date = CalendarUtils.getDate(timeString, timeFormat);
				SimpleDateFormat df = new SimpleDateFormat("HHmm");
				int time = Integer.parseInt(df.format(date));
				int hour = time/100;
				int min = time%100;
				if (hour>=24)
					throw new Exception("Invalid time '"+timeString+"' -- hour ("+hour+") must be between 0 and 23.");
				if (min>=60)
					throw new Exception("Invalid time '"+timeString+"' -- minute ("+min+") must be between 0 and 59.");
				if ((min%Constants.SLOT_LENGTH_MIN)!=0)
					throw new Exception("Invalid time '"+timeString+"' -- minute ("+min+") must be divisible by "+Constants.SLOT_LENGTH_MIN+".");
				slot = (hour*60+min - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
			} catch (NumberFormatException ex) {
				throw new Exception("Invalid time '"+timeString+"' -- not a number.");
			}
			if (slot<0)
				throw new Exception("Invalid time '"+timeString+"', did not meet format: " + timeFormat);
			return(slot);
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
					addNote("\tCould not find room '" + building + " " + roomNbr + "' not adding it to class '" + c.getClassLabel() + "'");

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
				
				NonUniversityLocation location = findNonUniversityLocation(name, c);
				if (location != null){
					locations.add(location);
				} else {
					addNote("\tCould not find location '" + name + "' not adding it to class '" + c.getClassLabel() + "'");
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
				
				TimeObject timeObject = new TimeObject(startTime, endTime, days);

				Vector<Room> rooms = new Vector<Room>();
				Vector<NonUniversityLocation> nonUniversityLocations = new Vector <NonUniversityLocation>();

				if (building != null && roomNbr != null){
					Room r = findRoom(null, building, roomNbr);
					if (r != null) {
						rooms.add(r);
					}
				} else if (location != null){
					NonUniversityLocation nul = findNonUniversityLocation(location, c);
					if (nul != null) {
						nonUniversityLocations.add(nul);
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
        	for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
				Element instructorElement = (Element) it.next();
				String id = getRequiredStringAttribute(instructorElement, "id", elementName);
				String firstName = getOptionalStringAttribute(instructorElement, "fname");
				String middleName = getOptionalStringAttribute(instructorElement, "mname");
				String lastName = getOptionalStringAttribute(instructorElement, "lname");
				boolean addNew = false;
				ClassInstructor ci = existingInstructors.get(id);
				if (ci == null){
					DepartmentalInstructor di = findDepartmentalInstructorWithExternalUniqueId(id, c.getSchedulingSubpart().getControllingDept());
					if (di == null) {
						di = new DepartmentalInstructor();
						di.setDepartment(c.getSchedulingSubpart().getControllingDept());
						di.setExternalUniqueId(id);
						di.setFirstName(firstName);
						di.setMiddleName(middleName);
						di.setLastName((lastName != null?lastName:"Unknown Name"));
						di.setIgnoreToFar(new Boolean(false));
						getHibSession().save(di);
						getHibSession().flush();
						getHibSession().refresh(di);
			        	ChangeLog.addChange(getHibSession(), manager, session, di, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.CREATE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
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
				String shareStr = getOptionalStringAttribute(instructorElement, "share");
				Integer share = null;
				if (shareStr != null){
					share = Integer.parseInt(shareStr);
				} else {
					share = new Integer(100);
				}
				if (ci.getPercentShare() == null || !ci.getPercentShare().equals(share)){
					ci.setPercentShare(share);
					changed = true;
				}
				
				Boolean lead = getOptionalBooleanAttribute(instructorElement, "lead");
				if (lead == null){
					lead = new Boolean(true);
				}
				if (ci.isLead() == null || !ci.isLead().equals(lead)){
					ci.setLead(lead);
					changed = true;
				}
				if (changed){
					getHibSession().saveOrUpdate(c);
					getHibSession().flush();
					getHibSession().refresh(c);
		        	ChangeLog.addChange(getHibSession(), manager, session, ci, ChangeLog.Source.DATA_IMPORT, (addNew?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
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

	private boolean elementCourseCredit(Element element, InstructionalOffering io) throws Exception {
		boolean changed = false;
		String elementName = "courseCredit";
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
	        CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(creditFormat, creditType, creditUnitType, minCredit, maxCredit, fractionalIncrementsAllowed, true);
	        if (io.getCredit() == null && ccuc != null){
	        	changed = true;
	        	addNote("\tadded offering credit");
	        } else if (io.getCredit() != null && !isSameCreditConfig(io.getCredit(),ccuc)) {
	        	addNote("\toffering credit values changed ");
	        	changed = true;
	        } 
	        if (changed){
	        	io.setCredit(ccuc);
	        	ccuc.setOwner(io);
	        	getHibSession().saveOrUpdate(io);
	        	getHibSession().flush();
	        	getHibSession().refresh(io);
	        	ChangeLog.addChange(getHibSession(), manager, session, io.getCredit(), ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.UPDATE, io.getControllingCourseOffering().getSubjectArea(), io.getControllingCourseOffering().getDepartment());
	        }
        }
		return(changed);
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
	        	ChangeLog.addChange(getHibSession(), manager, session, ss.getCredit(), ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.UPDATE, ss.getControllingCourseOffering().getSubjectArea(), ss.getControllingCourseOffering().getDepartment());
	        }
        }
		return(changed);
	}
	
	private boolean elementMeetsWith(Element element, Class_ c) throws Exception{
		if (!useMeetsWithElement){
			return(false);
		}
		boolean changed = false;
		String elementName = "meetsWith";
    	Vector<DistributionPref> existingMeetsWith = new Vector<DistributionPref>();
		if(c.getDistributionPreferences() != null){
			for(Iterator<?> dpIt = c.getDistributionPreferences().iterator(); dpIt.hasNext(); ){
				DistributionPref dp = (DistributionPref) dpIt.next();
				if (dp.getDistributionType().getUniqueId().equals(meetsWithType.getUniqueId())){
					existingMeetsWith.add(dp);						
				}
			}
		}
		Vector<String> classIds = new Vector<String>();
		if(element.element(elementName) != null){
        	classIds.add(c.getExternalUniqueId());
        	for (Iterator<?> it = element.elementIterator(elementName); it.hasNext();){
        		Element meetsWithElement = (Element) it.next();
        		classIds.add(getRequiredStringAttribute(meetsWithElement, "id", elementName));
        	}
       	
        }
      	if(existingMeetsWith.size() != 1){
      		if (existingMeetsWith.size() > 1) {
	      		addNote("\tMultiple meets with preferences exist -- deleted them");
				for (Iterator<?> dpIt = existingMeetsWith.iterator(); dpIt.hasNext();){
					DistributionPref dp = (DistributionPref) dpIt.next();				
					addNote("\t\tdeleted '" + dp.preferenceText() + "'");
					deleteDistributionPref(dp);									
				}
	      		changed = true;
      		}
      		if (classIds.size() > 1){
      			addDistributionPref(classIds, c);
      			changed = true;
      		}
		} else {
			DistributionPref dp = (DistributionPref)existingMeetsWith.firstElement();
			if (classIds.size() > 1){
				if (!isMatchingMeetsWith(dp, classIds)){
					changed = true;
					deleteDistributionPref(dp);
					addDistributionPref(classIds, c);
				}
				
			} else {
				addNote("Class  " + c.getClassLabel() +" is no longer a meets with, removed" + dp.toString());
				deleteDistributionPref(dp);
				changed = true;
			}
		}
		return(changed);
	}
	
	private boolean isMatchingMeetsWith(DistributionPref dp, Vector<String> classExternalIds){
		boolean isSame = false;
		DistributionObject distObj = null;
		String cei = null;
		boolean allFound = true;
		HashSet<?> existingDistObjs = new HashSet<Object>();
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
	
	private boolean elementCourse(Element element, InstructionalOffering io, String action) throws Exception{
		boolean changed = false;
		ArrayList<CourseOffering> courses = getCourses(element);
		if (action.equalsIgnoreCase("insert")){
			CourseOffering co = null;
			for(Iterator<CourseOffering> it = courses.iterator(); it.hasNext();){
				co = (CourseOffering) it.next();
				co.setInstructionalOffering(io);
				if (co.getNbrExpectedStudents() == null){
					co.setNbrExpectedStudents(new Integer(0));
				}
				if (co.getDemand() == null){
					co.setDemand(new Integer(0));
				}
				io.addTocourseOfferings(co);
				co.setSubjectAreaAbbv(co.getSubjectArea().getSubjectAreaAbbreviation());
				addNote("\tadded course: " + co.getSubjectArea().getSubjectAreaAbbreviation() + " " + co.getCourseNbr());
				getHibSession().saveOrUpdate(io);
				getHibSession().flush();
				getHibSession().refresh(io);
				ChangeLog.addChange(getHibSession(), manager, session, co, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.CREATE, co.getSubjectArea(), co.getDepartment());
			}
			changed = true;
		} else {
			CourseOffering nco = null;
			CourseOffering oco = null;
			for(Iterator<CourseOffering> nit = courses.iterator(); nit.hasNext();){
				nco = (CourseOffering) nit.next();
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
						if(changed){
				        	ChangeLog.addChange(getHibSession(), manager, session, oco, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.UPDATE, oco.getSubjectArea(), oco.getDepartment());
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
					io.addTocourseOfferings(nco);
					changed = true;
					getHibSession().saveOrUpdate(io);
					getHibSession().flush();
					getHibSession().refresh(io);
		        	ChangeLog.addChange(getHibSession(), manager, session, nco, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.CREATE, nco.getSubjectArea(), nco.getDepartment());
				}
			}
			List<?> removeCourses = new ArrayList<Object>();
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

	private boolean elementDesignatorRequired(Element element, InstructionalOffering io){
		boolean changed = false;
		Element desigElement = element.element("designatorRequired");
		if (desigElement != null){
			if (io.isDesignatorRequired() == null || !io.isDesignatorRequired().booleanValue()){
				io.setDesignatorRequired(new Boolean(true));
				changed = true;
				addNote("\tdesignatorRequired element changed");
			}
		} else if (io.isDesignatorRequired() == null || io.isDesignatorRequired().booleanValue()){
				io.setDesignatorRequired(new Boolean(false));
				changed = true;
				addNote("\tdesignatorRequired element changed");
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
		        	ChangeLog.addChange(getHibSession(), manager, session, ioc, ChangeLog.Source.DATA_IMPORT, (addNew?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), ioc.getControllingCourseOffering().getSubjectArea(), ioc.getControllingCourseOffering().getDepartment());
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
				String limitStr = getRequiredStringAttribute(classElement, "limit", elementName);
				Integer limit = new Integer(0);
				if (!limitStr.equalsIgnoreCase("inf")){
					limit = Integer.valueOf(limitStr);
				}
				String suffix = getRequiredStringAttribute(classElement, "suffix", elementName);
				String type = getRequiredStringAttribute(classElement, "type", elementName);
				String scheduleNote = getOptionalStringAttribute(classElement, "scheduleNote");
				Boolean displayInScheduleBook  = getOptionalBooleanAttribute(classElement, "displayInScheduleBook ");
				if (displayInScheduleBook == null){
					displayInScheduleBook = new Boolean(true);
				}
				Integer itypeId = ((ItypeDesc)itypes.get(type)).getItype();
				
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
					if (!clazz.getSchedulingSubpart().getItype().getItype().equals(itypeId) || !possibleSubpartsAtThisLevel.contains(clazz.getSchedulingSubpart())){
						clazz.getSchedulingSubpart().getClasses().remove(clazz);
						for (Iterator<SchedulingSubpart> ssIt = possibleSubpartsAtThisLevel.iterator(); ssIt.hasNext(); ){
							SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
							if (ss.getItype().getItype().equals(itypeId)){
								clazz.setSchedulingSubpart(ss);
								ss.addToclasses(clazz);
								break;
							}
						}
						addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' itype changed");
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
					clazz.setDisplayInScheduleBook(displayInScheduleBook);
					clazz.setSchedulePrintNote(scheduleNote);
					clazz.setDisplayInstructor(new Boolean(true));
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
				
				HashMap<String, Vector<Calendar>> dates = elementDates(classElement);
				DatePattern dp = null;
				if (dates != null){
					dp = findDatePattern(dates.get("startDates"), dates.get("endDates"), clazz);					
				}
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
				
				if (changed){
					this.getHibSession().saveOrUpdate(clazz);
				}

				if (elementMeetsWith(classElement, clazz)){
					addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' meets with preferences changed");
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
				}
				if (elementClass(classElement, ioc, clazz, allExistingClasses)){
					addNote("\t" + ioc.getCourseName() + " " + type + " " + suffix + " 'class' child classes changed");
					changed = true;
				}				
				if (changed){
					ChangeLog.addChange(getHibSession(), manager, session, clazz, ChangeLog.Source.DATA_IMPORT, (isAdd?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), ioc.getControllingCourseOffering().getSubjectArea(), ioc.getDepartment());
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
	private Vector<Meeting> getMeetings(Date startDate, Date stopDate, String pattern, TimeObject meetingTime, Vector<Room> rooms, Vector<NonUniversityLocation> locations){
		if (meetingTime != null){
			Meeting meeting = meetingTime.asMeeting();
			meeting.setApprovedDate(Calendar.getInstance().getTime());
			
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
	
	private void addDistributionPref(Vector<String> classIds, Class_ clazz) throws Exception{
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
			dp.setDistributionType(meetsWithType);
			dp.setGrouping(DistributionPref.sGroupingNone);
			dp.setPrefLevel(requiredPrefLevel);
			dp.setOwner(clazz.getSchedulingSubpart().getControllingDept());
			for (Iterator<Class_> cIt = classes.iterator(); cIt.hasNext();){
				c = (Class_) cIt.next();
				DistributionObject distObj = new DistributionObject();
				distObj.setDistributionPref(dp);
				distObj.setPrefGroup(c);
				dp.addTodistributionObjects(distObj);
			}
			getHibSession().save(dp);
			getHibSession().flush();
			getHibSession().refresh(dp);
	        ChangeLog.addChange(getHibSession(), manager, session, dp, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.CREATE, null, clazz.getSchedulingSubpart().getControllingDept());       
		}	
		
	}
	
	private boolean addUpdateClassEvent(Class_ c, TimeObject meetingTime, Vector<Room> rooms, Vector<NonUniversityLocation> locations) {
		return(addUpdateClassEvent(c, getMeetings(c.effectiveDatePattern(), meetingTime, rooms, locations)));
	}
	
	private boolean addUpdateClassEvent(Class_ c, Vector<Meeting> meetings) {
		boolean changed = false;
		
		List<?> originalClassEvents = null;
		if (c.getUniqueId() != null){
			originalClassEvents = Event.findCourseRelatedEventsOfTypeOwnedBy(this.getHibSession(), classType.getUniqueId(), c);
		} else {
			originalClassEvents = new ArrayList<Object>();
		}
		
		if(meetings != null && !meetings.isEmpty() && (originalClassEvents == null || originalClassEvents.isEmpty() || originalClassEvents.size() > 1)){
			if (originalClassEvents.size() > 1){
				addNote("\tfound multiple matching events, deleting them and adding new event: " + c.getSchedulingSubpart().getInstrOfferingConfig().getCourseName() + " " + c.getSchedulingSubpart().getItype().getAbbv().trim() + " " + c.getClassSuffix());
				for(Iterator<?> eIt = originalClassEvents.iterator(); eIt.hasNext(); ){
					Event event = (Event) eIt.next();
					getHibSession().delete(event);
				}
			}
			Event newEvent = new Event();
			newEvent.setEventType(classType);
			newEvent.setMaxCapacity(c.getMaxExpectedCapacity());
			newEvent.setMinCapacity(c.getExpectedCapacity());
			newEvent.setEventName(c.getSchedulingSubpart().getInstrOfferingConfig().getCourseName() + " " + c.getSchedulingSubpart().getItype().getAbbv().trim() + " " + c.getClassSuffix());
			RelatedCourseInfo rci = new RelatedCourseInfo();
			rci.setOwner(c);
			rci.setEvent(newEvent);
			newEvent.addTorelatedCourses(rci);
			for(Iterator<Meeting> mIt = meetings.iterator(); mIt.hasNext(); ){
				Meeting meeting = (Meeting) mIt.next();
				meeting.setEvent(newEvent);
				newEvent.addTomeetings(meeting);
			}
			getHibSession().save(newEvent);
			changed = true; 
			addNote("\tdid not find matching event, added new event: " + c.getSchedulingSubpart().getInstrOfferingConfig().getCourseName() + " " + c.getSchedulingSubpart().getItype().getAbbv().trim() + " " + c.getClassSuffix());
			ChangeLog.addChange(getHibSession(), manager, session, newEvent, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.CREATE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
		} else {
			if (!originalClassEvents.isEmpty()){
				Event origEvent = (Event) originalClassEvents.get(0);
				Set<?> origMeetings = new TreeSet<Object>();
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
						ChangeLog.addChange(getHibSession(), manager, session, m, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.DELETE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
						getHibSession().delete(m);
						changed = true;
					}
				}
				if (changed){
					ChangeLog.addChange(getHibSession(), manager, session, origEvent, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.UPDATE, c.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), c.getSchedulingSubpart().getControllingCourseOffering().getDepartment());	
					getHibSession().update(origEvent);
				}
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
				Integer minPerWeek = getRequiredIntegerAttribute(subpart, "minPerWeek", elementName);
				String type = getRequiredStringAttribute(subpart, "type", elementName);
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
					ss.setItype(itypes.get(type));
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
					ss.setStudentAllowOverlap(new Boolean(true));
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
					ChangeLog.addChange(getHibSession(), manager, session, ss, ChangeLog.Source.DATA_IMPORT, (isAdd?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), ioc.getControllingCourseOffering().getSubjectArea(), ioc.getDepartment());
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
				ChangeLog.addChange(getHibSession(), manager, session, parentSubpart, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.UPDATE, ioc.getControllingCourseOffering().getSubjectArea(), ioc.getDepartment());
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

	private boolean elementConsent(Element element, InstructionalOffering io) throws Exception{
		boolean changed = false;
		Element consentElement = element.element("consent");
		if (consentElement != null){
			String consentType = getRequiredStringAttribute(consentElement, "type", "consent");
			if (io.getConsentType() == null || !io.getConsentType().getReference().equals(consentType)){
				io.setConsentType(OfferingConsentType.getOfferingConsentTypeForReference(consentType));				
				changed = true;
				addNote("\tconsent changed");
			} 
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
        ChangeLog.addChange(getHibSession(), manager, session, dp, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.DELETE, null, dept);       
        getHibSession().delete(dp);
        getHibSession().saveOrUpdate(dept);
        
//        for (Iterator i=relatedInstructionalOfferings.iterator();i.hasNext();) {
//            InstructionalOffering io = (InstructionalOffering)i.next();
//            ChangeLog.addChange(getHibSession(), manager, session, io, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.DELETE, io.getControllingCourseOffering().getSubjectArea(), null);
//        }
	}

	private void deleteCourseOffering(CourseOffering co){
		InstructionalOffering io = co.getInstructionalOffering();
		if (io.getCourseOfferings().size() == 1){
			deleteInstructionalOffering(io);
		} else if (io.getCourseOfferings().size() > 1) {
			if (io.getCourseReservations() != null && !io.getCourseReservations().isEmpty()){
				if (io.getCourseReservations().size() <= 2){
					for (Iterator<?> it = io.getCourseReservations().iterator(); it.hasNext(); ){
						CourseOfferingReservation cor = (CourseOfferingReservation) it.next();
						io.getCourseReservations().remove(cor);
						co.getCourseReservations().remove(cor);
						cor.setCourseOffering(null);
						cor.setOwner(null);
						this.getHibSession().delete(cor);
					}
				} else {
					for (Iterator<?> it = io.getCourseReservations().iterator(); it.hasNext(); ){
						CourseOfferingReservation cor = (CourseOfferingReservation) it.next();
						if(cor.getCourseOffering().getUniqueId().equals(co.getUniqueId())){
							io.getCourseReservations().remove(cor);
							co.getCourseReservations().remove(cor);
							cor.setCourseOffering(null);
							cor.setOwner(null);
							this.getHibSession().delete(cor);
						}
					}
				}
			}
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

		ChangeLog.addChange(getHibSession(), manager, session, io, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.DELETE, io.getControllingCourseOffering().getSubjectArea(), io.getControllingCourseOffering().getDepartment());
		this.getHibSession().delete(io);
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
		ChangeLog.addChange(getHibSession(), manager, session, ioc, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.DELETE, ioc.getControllingCourseOffering().getSubjectArea(), ioc.getControllingCourseOffering().getDepartment());
		this.getHibSession().delete(ioc);
	}
	
	private void deleteSchedulingSubpart(SchedulingSubpart ss) {
		
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
		
		if (ss.getChildSubparts() != null){
			for(Iterator cssIt = ss.getChildSubparts().iterator(); cssIt.hasNext(); ){
				SchedulingSubpart css = (SchedulingSubpart) cssIt.next();
				deleteSchedulingSubpart(css);
			}
		}
		SchedulingSubpart parentSubpart = ss.getParentSubpart();
		if(parentSubpart != null){
			parentSubpart.getChildSubparts().remove(ss);
			ss.setParentSubpart(null);
		}
		ioc.getSchedulingSubparts().remove(ss);	
		this.getHibSession().update(ioc);
		ChangeLog.addChange(getHibSession(), manager, session, ss, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.DELETE, ioc.getControllingCourseOffering().getSubjectArea(), ioc.getControllingCourseOffering().getDepartment());
		this.getHibSession().delete(ss);
	}

	private void deleteClassInstructor(ClassInstructor ci){
		ci.getInstructor().getClasses().remove(ci);
		ci.getClassInstructing().getClassInstructors().remove(ci);
		Class_ clazz = ci.getClassInstructing();
		ci.setClassInstructing(null);
		ci.setInstructor(null);
		ChangeLog.addChange(getHibSession(), manager, session, ci, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.DELETE, clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea(), clazz.getSchedulingSubpart().getControllingCourseOffering().getDepartment());
		this.getHibSession().delete(ci);
	}

	private void deleteClass(Class_ c){
		c.deleteAllDependentObjects(this.getHibSession(), false);
		c.getSchedulingSubpart().getClasses().remove(c);
		SchedulingSubpart ss = c.getSchedulingSubpart();
		ChangeLog.addChange(getHibSession(), manager, session, c, ChangeLog.Source.DATA_IMPORT, ChangeLog.Operation.DELETE, ss.getControllingCourseOffering().getSubjectArea(), ss.getControllingCourseOffering().getDepartment());
		c.setSchedulingSubpart(null);
		existingClasses.remove(c.getUniqueId());
		this.getHibSession().delete(c);
	}

	private Session findSession(String academicInitiative, String academicYear, String academicTerm){
  		
		return(Session) this.
		getHibSession().
		createQuery("from Session as s where s.academicInitiative = :academicInititive and s.academicYear = :academicYear  and s.academicTerm = :academicTerm").
		setString("academicInititive", academicInitiative).
		setString("academicYear", academicYear).
		setString("academicTerm", academicTerm).
		setCacheable(true).
		uniqueResult();
	}
	

	
	private CourseOffering findExistingCourseOffering(CourseOffering courseOffering, Long sessionId){
		CourseOffering existingCo = null;
		if (courseOffering.getExternalUniqueId() != null) {
			existingCo = findCrsOffrForExternalId(courseOffering.getExternalUniqueId(), sessionId);
		}
        if (existingCo == null) {
			existingCo = findCrsOffrForSubjCrs(courseOffering.getSubjectArea().getSubjectAreaAbbreviation(), courseOffering.getCourseNbr(), sessionId);
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
	
	private NonUniversityLocation findNonUniversityLocation(String name, Class_ c){
		NonUniversityLocation location = null;
		if (name != null) {
			List<?> possibleLocations = this.
			getHibSession().
			createQuery("select distinct l from NonUniversityLocation as l where l.name=:name and l.session.uniqueId=:sessionId").
			setLong("sessionId", session.getUniqueId().longValue()).
			setString("name", name).
			setCacheable(true).
			list();
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
		}
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
	
	private void loadClassEventType() {
		classType = (EventType) this.getHibSession().createQuery("from EventType et where et.reference = 'class'").uniqueResult();
	}
	private void loadMeetsWithDistributionType() {
		meetsWithType = (DistributionType) this.getHibSession().createQuery("from DistributionType dt where dt.reference = 'MEET_WITH'").uniqueResult();
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

	private String getRequiredStringAttribute(Element element, String attributeName, String elementName) throws Exception{		
		String attributeValue = element.attributeValue(attributeName);
		if (attributeValue == null || attributeValue.trim().length() == 0){
			throw new Exception("For element '" + elementName + "' a '" + attributeName + "' is required");
		} else {
			attributeValue = attributeValue.trim().replace('\u0096', ' ').replace('\u0097', ' ');
		}						
		return(attributeValue);
	}
	
	private String getOptionalStringAttribute(Element element, String attributeName) {		
		String attributeValue = element.attributeValue(attributeName);
		if (attributeValue == null || attributeValue.trim().length() == 0){
			attributeValue = null;
		} else {
			attributeValue = attributeValue.trim().replace('\u0096', ' ').replace('\u0097', ' ');
		}						
		return(attributeValue);		
	}
	
	private Integer getRequiredIntegerAttribute(Element element, String attributeName, String elementName) throws Exception {
		String attributeStr = getRequiredStringAttribute(element, attributeName, elementName);
		return(new Integer(attributeStr));
	}
	
	private Integer getOptionalIntegerAttribute(Element element, String attributeName) {
		String attributeStr = getOptionalStringAttribute(element, attributeName);
		return(new Integer(attributeStr));
	}
	
	private Boolean getRequiredBooleanAttribute(Element element, String attributeName, String elementName) throws Exception {
		String attributeStr = getRequiredStringAttribute(element, attributeName, elementName);
		return(new Boolean(attributeStr));
	}
	
	private Boolean getOptionalBooleanAttribute(Element element, String attributeName) {
		String attributeStr = getOptionalStringAttribute(element, attributeName);
		return(new Boolean(attributeStr));
	}
	
	private void addNote(String note){
		offeringNotes.add(note);
	}
	
	private void clearNotes(){
		offeringNotes = new Vector<String>();
	}
	
	private void updateChangeList(boolean changed){
		if(changed && offeringNotes != null) {
			changeList.addAll(offeringNotes);
			String note = null;
			for(Iterator<String> it = offeringNotes.iterator(); it.hasNext(); ){
				note = (String) it.next();
				info(note);
			}
		}
		clearNotes();
	}
	
	private void mailLoadResults(){
       	Email email = new Email();
       	
       	String subject = "Course Offering Import Results";
       	String mail = "";
       	for (Iterator<String> it = changeList.iterator(); it.hasNext(); ){
       		mail += (String) it.next() + "\n";
       	}
    	
    	try {
			email.sendMail(
					(String)ApplicationProperties.getProperty("tmtbl.smtp.host", "smtp.purdue.edu"), 
					(String)ApplicationProperties.getProperty("tmtbl.smtp.domain", "smtp.purdue.edu"), 
					(String)ApplicationProperties.getProperty("tmtbl.inquiry.sender", "smasops@purdue.edu"), 
					(manager != null?manager.getEmailAddress():(String)ApplicationProperties.getProperty("tmtbl.inquiry.sender", "smasops@purdue.edu")), 
					(String)ApplicationProperties.getProperty("tmtbl.inquiry.email","smasops@purdue.edu"), 
					"Timetabling (Data Import): "+subject, 
					mail, 
					new Vector<Object>());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private TimetableManager findDefaultManager(){
		return((TimetableManager)getHibSession().createQuery("from TimetableManager as m where m.uniqueId = (select min(tm.uniqueId) from TimetableManager as tm inner join tm.managerRoles as mr inner join mr.role as r where r.reference = 'Administrator')").uniqueResult());
	}
}
