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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction;
import org.unitime.timetable.interfaces.ExternalSectionMonitoredUpdateMessage;
import org.unitime.timetable.interfaces.ExternalSectionMonitoredUpdateMessage.ExternalSectionCreationStatus;
import org.unitime.timetable.interfaces.ExternalVariableTitleDataLookup;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.rights.Right;

@Service("/api/var-title-crs")
public class VariableTitleCourseConnector extends ApiConnector {
	
	@Override
	protected String getName() {
		return "var-title-crs";
	}
	
	@Override
	public void doGet(ApiHelper helper) throws IOException {
		VariableTitleQuery vtq = helper.getRequest(VariableTitleQuery.class);		
		validateRequest(vtq, helper);
		helper.getSessionContext().checkPermissionAnyAuthority(getAcadSession(vtq, helper.getHibSession()), Right.ApiVariableTitleSectionLookup);
		findClassAndUpdateQueryData(vtq, findCourse(vtq, helper.getHibSession()), helper.getHibSession());
		helper.setResponse(vtq);
	}
	
	@Override
	public void doPost(ApiHelper helper) throws IOException {
		VariableTitleQuery vtq = helper.getRequest(VariableTitleQuery.class);
		
		validateRequest(vtq, helper);
		helper.getSessionContext().checkPermissionAnyAuthority(getAcadSession(vtq, helper.getHibSession()), Right.ApiVariableTitleSectionCreate);
		findOrCreateVariableTitleSection(vtq, helper);
		helper.setResponse(vtq);
	}

	private boolean validateRequest(VariableTitleQuery variableTitleQuery, ApiHelper helper) {
		
		if (variableTitleQuery == null) {
			throw new IllegalArgumentException("Missing Query Data.");	
		}
		if (variableTitleQuery.getCampus() == null) {
			throw new IllegalArgumentException("Missing Campus.");			
		}
		if (variableTitleQuery.getYear() == null) {
			throw new IllegalArgumentException("Missing Year.");			
		}
		if (variableTitleQuery.getTerm() == null) {
			throw new IllegalArgumentException("Missing Term.");			
		}
		if (variableTitleQuery.getSubjectArea() == null) {
			throw new IllegalArgumentException("Missing Subject Area.");			
		}
		if (variableTitleQuery.getCourseNumber() == null) {
			throw new IllegalArgumentException("Missing Course Number.");			
		}
		if (variableTitleQuery.getCourseTitle() == null) {
			throw new IllegalArgumentException("Missing Course Title.");			
		}
		if (ApplicationProperty.VariableTitleInstructorIdRequired.isTrue() && variableTitleQuery.getInstructorId() == null) {
			throw new IllegalArgumentException("Missing Instructor Id.");			
		}
		if (variableTitleQuery.getStartDate() == null) {
			throw new IllegalArgumentException("Missing Start Date.");			
		}
		if (variableTitleQuery.getEndDate() == null) {
			throw new IllegalArgumentException("Missing End Date.");			
		}
		Session s = getAcadSession(variableTitleQuery, helper.getHibSession());
		if ( s == null) {
			throw new IllegalArgumentException("Academic session not found for campus, year, and term.");
		}
		SubjectArea sa = getSubjectObject(variableTitleQuery, helper.getHibSession());
		if (sa == null) {
			throw new IllegalArgumentException("Subject area not found for campus, year, term, and subject area abbreviaton.");
		}
		
		ExternalVariableTitleDataLookup evtdl = null;
		try {
			evtdl = lookupExternalVariableTitleDataLookup();
		} catch (Exception e) {
			Debug.error("Unable to look up external variable title data.", e);
			throw new IllegalArgumentException("Unable to look up external variable title data.  Unable to instantiate lookup class.");			
		}
		if (evtdl != null) {
			if (!evtdl.isVariableTitleCourse(sa, variableTitleQuery.getCourseNumber(), s, helper.getHibSession())) {
				throw new IllegalArgumentException("Course for provided subject area and course number is not a variable title course.");							
			}
		}
		
		if (variableTitleQuery.getInstructorId() != null && DepartmentalInstructor.findByPuidDepartmentId(variableTitleQuery.getInstructorId(), sa.getDepartment().getUniqueId(), helper.getHibSession()) == null) {
			throw new IllegalArgumentException("Instructor with matching id not found for provided subject area.");			
		};
		
		if (ApplicationProperty.VariableTitleConfigName.value() == null || ApplicationProperty.VariableTitleConfigName.value().trim().equals("")) {
			throw new IllegalArgumentException("Variable Title Application Properties: Config Name must be set.");			
		}
		if (ApplicationProperty.VariableTitleDefaultLimit.value() == null || ApplicationProperty.VariableTitleDefaultLimit.value().trim().equals("")) {
			throw new IllegalArgumentException("Variable Title Application Properties: Default Limit must be set.");			
		}
		if (ApplicationProperty.VariableTitleInstructionalType.value() == null || ApplicationProperty.VariableTitleInstructionalType.value().trim().equals("")) {
			throw new IllegalArgumentException("Variable Title Application Properties: Instructional Type must be set.");			
		}
		if (ItypeDesc.findForReference(ApplicationProperty.VariableTitleInstructionalType.value(), helper.getHibSession()) == null) {
			throw new IllegalArgumentException("Variable Title Application Properties: Instructional Type Not Found.");						
		}
				
		return true;
	}
	
	private Session getAcadSession(VariableTitleQuery variableTitleQuery, org.hibernate.Session hibSession) {
		return Session.getSessionUsingCampusYearTerm(variableTitleQuery.getCampus(), variableTitleQuery.getYear(), variableTitleQuery.getTerm(), hibSession);
	}
	
	private SubjectArea getSubjectObject(VariableTitleQuery variableTitleQuery, org.hibernate.Session hibSession) {
		return(SubjectArea.findUsingCampusYearTermExternalSubjectAbbreviation(variableTitleQuery.getCampus(), variableTitleQuery.getYear(), variableTitleQuery.getTerm(), variableTitleQuery.getSubjectArea(), hibSession));
	}

	private CourseOffering findCourse(VariableTitleQuery variableTitleQuery, org.hibernate.Session hibSession) {
		StringBuffer sb = new StringBuffer();
		sb.append("from CourseOffering co")
		  .append(" where co.instructionalOffering.session.uniqueId = ")
		  .append(getAcadSession(variableTitleQuery, hibSession).getUniqueId())
		  .append(" and co.subjectArea.uniqueId = ")
		  .append(getSubjectObject(variableTitleQuery, hibSession).getUniqueId())
		  .append(" and co.courseNbr like '")
		  .append(variableTitleQuery.getCourseNumber())
		  .append("%'")
		  ;
		
		@SuppressWarnings("unchecked")
		List<CourseOffering> courses = (List<CourseOffering>)hibSession.createQuery(sb.toString()).setCacheable(true).list();
		CourseOffering co = null;
		for (CourseOffering c : courses) {
			if (c.getTitle().trim().equalsIgnoreCase(variableTitleQuery.getCourseTitle().trim())) {
				co = c;
				break;
			}
		}
		return co; 
	}

	private Class_ findClass(VariableTitleQuery variableTitleQuery, CourseOffering courseOffering, org.hibernate.Session hibSession) {
		if (ApplicationProperty.VariableTitleInstructorIdRequired.isTrue() && variableTitleQuery.getInstructorId() == null) {
			return null;
		}
		if (courseOffering != null && courseOffering.getInstructionalOffering() != null 
				&& courseOffering.getInstructionalOffering().getInstrOfferingConfigs() != null 
				&& !courseOffering.getInstructionalOffering().getInstrOfferingConfigs().isEmpty()) {
			for (InstrOfferingConfig ioc : courseOffering.getInstructionalOffering().getInstrOfferingConfigs()) {
				if (ioc.getSchedulingSubparts().size() == 1 
						&& ((ApplicationProperty.VariableTitleConfigName.value() != null 
						        && ApplicationProperty.VariableTitleConfigName.value() != ""
						        && ioc.getName().equals(ApplicationProperty.VariableTitleConfigName.value()))
					        || (ApplicationProperty.VariableTitleConfigName.value() == null
					           || ApplicationProperty.VariableTitleConfigName.value().trim() == ""))) {
					for (SchedulingSubpart ss : ioc.getSchedulingSubparts()) {
						if (ss.getItype().getSis_ref().equals(ApplicationProperty.VariableTitleInstructionalType.value())) {
							for (Class_ c : ss.getClasses()) {
								if (c.effectiveDatePattern().getStartDate().equals(variableTitleQuery.getStartDate()) 
										&& c.effectiveDatePattern().getEndDate().equals(variableTitleQuery.getEndDate())) {
									if ((ApplicationProperty.VariableTitleInstructorIdRequired.isTrue() || variableTitleQuery.getInstructorId() != null) && c.getClassInstructors().size() == 1) {
										for(ClassInstructor ci : c.getClassInstructors()) {
											if (ci.getInstructor().getExternalUniqueId() != null && ci.getInstructor().getExternalUniqueId().equalsIgnoreCase(variableTitleQuery.getInstructorId())) {
													return c;
											}
										} 
									} else if (ApplicationProperty.VariableTitleInstructorIdRequired.isFalse() && variableTitleQuery.getInstructorId() == null && c.getClassInstructors().size() == 0) {
										return c;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private void findClassAndUpdateQueryData(VariableTitleQuery variableTitleQuery, CourseOffering courseOffering, org.hibernate.Session hibSession) {
		Class_ clazz = findClass(variableTitleQuery, courseOffering, hibSession);
		if (clazz != null) {
			variableTitleQuery.setFullCourseNumber(courseOffering.getCourseNbr());
			variableTitleQuery.setExternalId(clazz.getExternalId(courseOffering));
		}
		variableTitleQuery.setStatus(findClassCreationStatus(courseOffering, clazz, hibSession));
	}

	private String findClassCreationStatus(CourseOffering courseOffering, Class_ clazz,
			org.hibernate.Session hibSession) {
		ExternalInstrOffrConfigChangeAction externalInstrOffrConfigChangeAction = null;
		try {
			externalInstrOffrConfigChangeAction = lookupExternalConfigChangeAction();
		} catch (Exception e) {
			Debug.error("Error trying to instantiate External Config Change Action, section will not be sent to the External System, FAILED will be returned by default", e);
			return ExternalSectionCreationStatus.FAILED.toString();
		}
		
		return createExternalSectionAndReturnStatus(courseOffering, clazz, (externalInstrOffrConfigChangeAction != null), externalInstrOffrConfigChangeAction, hibSession);
	}

	private String createExternalSectionAndReturnStatus(CourseOffering co, Class_ clazz, boolean externalConfigChangeActionExists, ExternalInstrOffrConfigChangeAction configChangeAction, org.hibernate.Session hibSession) {
		if (co == null || co.getUniqueId() == null)  {
			return ExternalSectionCreationStatus.DOES_NOT_EXIST.toString();
		}
		else if (clazz == null || clazz.getUniqueId() == null) {
			return ExternalSectionCreationStatus.DOES_NOT_EXIST.toString();
		} else {
			if (externalConfigChangeActionExists && configChangeAction == null){
				return ExternalSectionCreationStatus.FAILED.toString();			
			}
			else if (configChangeAction == null){
				return ExternalSectionCreationStatus.SUCCESS.toString();
			} else {
				ExternalSectionMonitoredUpdateMessage externalSectionMonitoredUpdateMessage = null;
				try {
					externalSectionMonitoredUpdateMessage = lookupExternalSectionMonitoredUpdateMessageAction();
				} catch (Exception e) {
					Debug.error("Error trying to instantiate External Section Monitored Message Action, section will not be sent to the External System, FAILED will be returned by default", e);
					return ExternalSectionCreationStatus.FAILED.toString();
				}
				if (externalSectionMonitoredUpdateMessage != null) {
					 return externalSectionMonitoredUpdateMessage.monitorExternalSectionUpdate(co, clazz, configChangeAction, ApplicationProperty.VariableTitleExternalSystemWaitTime.intValue(), hibSession).toString();
				} else {
					return ExternalSectionCreationStatus.SUCCESS.toString();
				}
			}
		}
	
	}

	@SuppressWarnings("unchecked")
	private String generateCourseNumber(SubjectArea subjectArea, String courseNumber, org.hibernate.Session hibSession) {
		HashSet<String> existingNumbers = new HashSet<String>();
		String query = "select co.courseNbr from CourseOffering co where co.subjectArea.uniqueId = :subjId and co.courseNbr like '" + courseNumber + "%'";
		existingNumbers.addAll((List<String>)hibSession.createQuery(query).setLong("subjId", subjectArea.getUniqueId()).list());
		
		char char1 = 'A', char2 = 'A';
		boolean needSecondCharacter = false;
		while (char1 <= 'Z' && char2 <=  'Z') {
			StringBuffer chkCrsNumber = new StringBuffer();
			chkCrsNumber.append(courseNumber)
			.append(ApplicationProperty.VariableTitleCourseSuffixDefaultStartCharacter.value())
			.append(char1)
			;
			if (needSecondCharacter) {
				chkCrsNumber.append(char2);
			}
			if (existingNumbers.contains(chkCrsNumber.toString())) {
				if (!needSecondCharacter) {
					if (char1 == 'Z') {
						char1 = 'A';
						needSecondCharacter = true;
					} else {
						char1++;
					}
				} else {
					if (char2 == 'Z') {
						char2 = 'A';
						char1++;
					} else {
						char2++;
					}
				}
			} else {
				return chkCrsNumber.toString();					
			}
		}
		return null;
	}

	private InstructionalOffering createOffering(VariableTitleQuery variableTitleQuery, SubjectArea subjectArea, org.hibernate.Session hibSession) {

		String newCourseNumber = generateCourseNumber(subjectArea, variableTitleQuery.getCourseNumber(), hibSession);
		CourseOffering courseOffering = new CourseOffering();
		OfferingConsentType consent = OfferingConsentType.getOfferingConsentTypeForReference(ApplicationProperty.VariableTitleDefaultConsentType.value());
		courseOffering.setSubjectArea(subjectArea);
		subjectArea.addTocourseOfferings(courseOffering);					
		courseOffering.setSubjectAreaAbbv(subjectArea.getSubjectAreaAbbreviation());
		courseOffering.setCourseNbr(newCourseNumber);
		courseOffering.setTitle(variableTitleQuery.getCourseTitle());
		courseOffering.setIsControl(true);
		courseOffering.setNbrExpectedStudents(0);
		courseOffering.setDemand(0);
		courseOffering.setConsentType(consent);

		InstructionalOffering instructionalOffering = new InstructionalOffering();
		instructionalOffering.setNotOffered(false);
		instructionalOffering.addTocourseOfferings(courseOffering);
		courseOffering.setInstructionalOffering(instructionalOffering);
		instructionalOffering.setSession(subjectArea.getSession());
		instructionalOffering.setByReservationOnly(false);
		instructionalOffering.generateInstrOfferingPermId();
		
		instructionalOffering.setUniqueId((Long)hibSession.save(instructionalOffering));
		courseOffering.setUniqueId((Long)hibSession.save(courseOffering));
		ExternalVariableTitleDataLookup externalVariableTitleDataLookup = null;
		try {
			externalVariableTitleDataLookup = lookupExternalVariableTitleDataLookup();
		} catch (Exception e) {
			Debug.error("Error trying to instantiate variable title data lookup class, course credit config will not be created.", e);
		}
		if (externalVariableTitleDataLookup != null) {
			CourseCreditFormat ccf = externalVariableTitleDataLookup.courseCreditFormatForCourse(subjectArea, variableTitleQuery.getCourseNumber(), subjectArea.getSession(), hibSession);
			Float minCredit = externalVariableTitleDataLookup.minCreditForCourse(subjectArea, variableTitleQuery.getCourseNumber(), subjectArea.getSession(), hibSession);
			Float maxCredit = externalVariableTitleDataLookup.maxCreditForCourse(subjectArea, variableTitleQuery.getCourseNumber(), subjectArea.getSession(), hibSession);
			if (ccf != null && minCredit != null) {
				if (ApplicationProperty.VariableTitleDefaultCourseCreditType.value() != null 
						&& ApplicationProperty.VariableTitleDefaultCourseCreditType.value().trim().length() != 0
						&& ApplicationProperty.VariableTitleDefaultCourseCreditUnitType.value() != null 
						&& ApplicationProperty.VariableTitleDefaultCourseCreditUnitType.value().trim().length() != 0
						) {
					CourseCreditType cct = CourseCreditType.getCourseCreditTypeForReference(ApplicationProperty.VariableTitleDefaultCourseCreditType.value());
					CourseCreditUnitType ccut = CourseCreditUnitType.getCourseCreditUnitTypeForReference(ApplicationProperty.VariableTitleDefaultCourseCreditUnitType.value());
					if (cct != null & ccut != null) {
						CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(ccf.getReference(), cct, ccut, minCredit, maxCredit, Boolean.TRUE, Boolean.TRUE);
						ccuc.setOwner(courseOffering);
						courseOffering.addTocreditConfigs(ccuc);
						ccuc.setUniqueId((Long) hibSession.save(ccuc));
					}
				}
			}
		}

		return instructionalOffering;
	}

	private InstrOfferingConfig createConfiguration(InstructionalOffering instructionalOffering, String configName, org.hibernate.Session hibSession) {
		InstrOfferingConfig instrOfferingConfig = new InstrOfferingConfig();
		instrOfferingConfig.setInstructionalOffering(instructionalOffering);
		instructionalOffering.addToinstrOfferingConfigs(instrOfferingConfig);
		if (ApplicationProperty.VariableTitleDefaultLimit.intValue() < 0) {
			instrOfferingConfig.setUnlimitedEnrollment(Boolean.TRUE);
			instrOfferingConfig.setLimit(0);
		} else {
			instrOfferingConfig.setUnlimitedEnrollment(Boolean.FALSE);
			instrOfferingConfig.setLimit(ApplicationProperty.VariableTitleDefaultLimit.intValue());
		}
		instrOfferingConfig.setName(configName);
		instrOfferingConfig.setUniqueId((Long) hibSession.save(instrOfferingConfig));
		
		return instrOfferingConfig;
	}
	
	private SchedulingSubpart createSubpart(InstrOfferingConfig instrOfferingConfig, org.hibernate.Session hibSession) {
		ItypeDesc itype = ItypeDesc.findForReference(ApplicationProperty.VariableTitleInstructionalType.value(), hibSession);
		SchedulingSubpart ss = new SchedulingSubpart();
		ss.setInstrOfferingConfig(instrOfferingConfig);
		instrOfferingConfig.addToschedulingSubparts(ss);
		ss.setItype(itype);
		ss.setMinutesPerWk(0);
        ss.setAutoSpreadInTime(ApplicationProperty.SchedulingSubpartAutoSpreadInTimeDefault.isTrue());
        ss.setStudentAllowOverlap(ApplicationProperty.SchedulingSubpartStudentOverlapsDefault.isTrue());
        ss.setChildSubparts(new HashSet<SchedulingSubpart>());
        ss.setSession(instrOfferingConfig.getSession());
        ss.setUniqueId((Long) hibSession.save(ss));
        
        return ss;
	}
	
	private Class_ createClass(VariableTitleQuery variableTitleQuery, SubjectArea subjectArea, SchedulingSubpart ss, org.hibernate.Session hibSession) {
		DatePattern datePattern = lookupDatePattern(variableTitleQuery, hibSession);
        if (datePattern == null) {
        	datePattern = createVariableTitleDatePattern(variableTitleQuery, hibSession);
        }
        
        Class_ clazz = new Class_();
        clazz.setCancelled(false);
        clazz.setControllingDept(subjectArea.getDepartment());
        clazz.setManagingDept(subjectArea.getDepartment());
        if (!datePattern.isDefault()) {
        	clazz.setDatePattern(datePattern);
        }
        clazz.setDisplayInstructor(true);
        if (ApplicationProperty.VariableTitleDefaultLimit.intValue() >= 0) {
        	clazz.setExpectedCapacity(ApplicationProperty.VariableTitleDefaultLimit.intValue());
        	clazz.setMaxExpectedCapacity(ApplicationProperty.VariableTitleDefaultLimit.intValue());
        } else {
        	clazz.setExpectedCapacity(0);
        	clazz.setMaxExpectedCapacity(0);        	
        }
        clazz.setEnabledForStudentScheduling(true);
        clazz.setNbrRooms(0);
        clazz.setSchedulingSubpart(ss);
        clazz.setRoomRatio(Float.valueOf(0));
        ss.addToclasses(clazz);
        clazz.setUniqueId((Long) hibSession.save(clazz));
        
        if (variableTitleQuery.getInstructorId() != null) {
        	DepartmentalInstructor di = DepartmentalInstructor.findByPuidDepartmentId(variableTitleQuery.getInstructorId(), subjectArea.getDepartment().getUniqueId(), hibSession);
	        ClassInstructor ci = new ClassInstructor();
	        ci.setLead(true);
	        ci.setPercentShare(100);
	        ci.setInstructor(di);
	        ci.setClassInstructing(clazz);
	        clazz.addToclassInstructors(ci);
	        ci.setUniqueId((Long)hibSession.save(ci));
        }
        return clazz;
	}

	private InstrOfferingConfig findConfigWithName(InstructionalOffering instructionalOffering, String name) {
		for (InstrOfferingConfig ioc : instructionalOffering.getInstrOfferingConfigs()) {
			if (ioc.getName().equals(name.trim())) {
				return ioc;
			}
		}
		return null;
	}
	
	private boolean isConfigValidForVariableTitle(InstrOfferingConfig instrOfferingConfig) {
		if (instrOfferingConfig.getSchedulingSubparts() == null || instrOfferingConfig.getSchedulingSubparts().isEmpty()) {
			return true;
		}
		if (instrOfferingConfig.getSchedulingSubparts().size() > 1) {
			return false;
		}
		for (SchedulingSubpart ss : instrOfferingConfig.getSchedulingSubparts()) {
			if (!ss.getItype().getSis_ref().equals(ApplicationProperty.VariableTitleInstructionalType.value())) {
				return false;
			}
		}
		return true;
	}
	
	private InstrOfferingConfig findOrCreateVariableTitleConfig(InstructionalOffering instructionalOffering, org.hibernate.Session hibSession) {
		InstrOfferingConfig instrOfferingConfig = null;
		String vtConfigName = ApplicationProperty.VariableTitleConfigName.value().trim();
		int cnt = 0;
		while (instrOfferingConfig == null) {
			if (instructionalOffering.getInstrOfferingConfigs() != null && instructionalOffering.existsConfig(vtConfigName, null)) {
				InstrOfferingConfig checkIoc = findConfigWithName(instructionalOffering, vtConfigName);
				if (isConfigValidForVariableTitle(checkIoc)) {
					instrOfferingConfig = checkIoc;
				} else {
					cnt++;
					vtConfigName = ApplicationProperty.VariableTitleConfigName.value().trim() + cnt;
				}
			} else {
				instrOfferingConfig = createConfiguration(instructionalOffering, vtConfigName, hibSession);
			}
			
		}
		return instrOfferingConfig;

	}
	
	private void findOrCreateVariableTitleSection(VariableTitleQuery variableTitleQuery, ApiHelper helper) {
		CourseOffering courseOffering = findCourse(variableTitleQuery, helper.getHibSession());
		Class_ clazz = null;
		if (courseOffering != null) {
			clazz = findClass(variableTitleQuery, courseOffering, helper.getHibSession());
			if (clazz != null) {
				variableTitleQuery.setFullCourseNumber(courseOffering.getCourseNbr());
				variableTitleQuery.setExternalId(clazz.getExternalId(courseOffering));
				variableTitleQuery.setStatus(findClassCreationStatus(courseOffering, clazz, helper.getHibSession()));
				return;
			}	
		}
        ExternalInstrOffrConfigChangeAction configChangeAction = null;
        boolean configChangeActionExists = false;
		try {
			configChangeAction = lookupExternalConfigChangeAction();
		} catch (Exception e2) {
			Debug.error("Error trying to instantiate External Config Change Action, this will result in a FAILED response.", e2);
			configChangeActionExists = true;
		} 
		if (configChangeAction != null) {
			configChangeActionExists = true;
		}
		InstructionalOffering instructionalOffering = null;
		Transaction trans = null;
		try {
			trans = helper.getHibSession().beginTransaction();
			SubjectArea subjectArea = getSubjectObject(variableTitleQuery, helper.getHibSession());
			if (courseOffering == null) {
				instructionalOffering = createOffering(variableTitleQuery, subjectArea, helper.getHibSession());
				courseOffering = instructionalOffering.getControllingCourseOffering();
			} else {
				instructionalOffering = courseOffering.getInstructionalOffering();
				if (instructionalOffering.isNotOffered()) {
					instructionalOffering.setNotOffered(false);
					helper.getHibSession().update(instructionalOffering);
				}
			}
			InstrOfferingConfig instrOfferingConfig = findOrCreateVariableTitleConfig(instructionalOffering, helper.getHibSession());
			
			SchedulingSubpart schedulingSubpart = null;
			if (instrOfferingConfig.getSchedulingSubparts() == null || instrOfferingConfig.getSchedulingSubparts().isEmpty()) {
				schedulingSubpart = createSubpart(instrOfferingConfig, helper.getHibSession());		
			} else {
				for (SchedulingSubpart ss : instrOfferingConfig.getSchedulingSubparts()) {
					schedulingSubpart = ss;
				}
			}
            clazz = createClass(variableTitleQuery, subjectArea, schedulingSubpart, helper.getHibSession());
            
        	if (configChangeAction != null && !configChangeAction.validateConfigChangeCanOccur(instructionalOffering, helper.getHibSession())){
        		throw new Exception("Configuration change violates rules for Add On, rolling back the change.");
        	}
        	
        	
            instructionalOffering.computeLabels(helper.getHibSession());

            ChangeLog.addChange(
                    helper.getHibSession(),
                    helper.getSessionContext(),
                    clazz,
                    ChangeLog.Source.VARIABLE_TITLE_API,
                    ChangeLog.Operation.CREATE,
                    instrOfferingConfig.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    null);
            
            helper.getHibSession().flush();
            trans.commit();
            
		} catch (Exception e){
			Debug.error("Failed to create variable title section.", e);
			try {
	            if (trans!=null && trans.isActive())
	            	trans.rollback();
            }
            catch (Exception e1) {
            	Debug.error("Unable to roll back transaction.", e1);
            }
			variableTitleQuery.setStatus(ExternalSectionCreationStatus.FAILED.toString());	
			return;
		} 
        variableTitleQuery.setStatus(createExternalSectionAndReturnStatus(courseOffering, clazz, configChangeActionExists, configChangeAction, helper.getHibSession()));
		if (courseOffering != null && courseOffering.getUniqueId() != null) {
			variableTitleQuery.setFullCourseNumber(courseOffering.getCourseNbr());
		}
		if (courseOffering != null && courseOffering.getUniqueId() != null && clazz != null && clazz.getUniqueId() != null) {
			variableTitleQuery.setExternalId(clazz.getExternalId(courseOffering));
		}
		
		// Notify the online scheduling server that the variable course title has changed
		StudentSectioningQueue.offeringChanged(helper.getHibSession(), helper.getSessionContext().getUser(), instructionalOffering.getSessionId(), instructionalOffering.getUniqueId());
		helper.getHibSession().flush();
	}
	
	private ExternalInstrOffrConfigChangeAction lookupExternalConfigChangeAction() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        String className = ApplicationProperty.ExternalActionInstrOffrConfigChange.value();
        ExternalInstrOffrConfigChangeAction configChangeAction = null;
    	if (className != null && className.trim().length() > 0){
        	configChangeAction = (ExternalInstrOffrConfigChangeAction) Class.forName(className).getDeclaredConstructor().newInstance();
    	}
    	return configChangeAction;
	}
	
	private ExternalVariableTitleDataLookup lookupExternalVariableTitleDataLookup() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        String className = ApplicationProperty.ExternalVariableTitleDataLookup.value();
        ExternalVariableTitleDataLookup externalVariableTitleDataLookup = null;
    	if (className != null && className.trim().length() > 0){
    		externalVariableTitleDataLookup = (ExternalVariableTitleDataLookup) Class.forName(className).getDeclaredConstructor().newInstance();
    	}
    	return externalVariableTitleDataLookup;
	}
	
	private ExternalSectionMonitoredUpdateMessage lookupExternalSectionMonitoredUpdateMessageAction() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
        String className = ApplicationProperty.ExternalActionSectionMonitoredUpdateMessage.value();
        ExternalSectionMonitoredUpdateMessage esmum = null;
    	if (className != null && className.trim().length() > 0){
    		esmum = (ExternalSectionMonitoredUpdateMessage) Class.forName(className).getDeclaredConstructor().newInstance();
    	}
    	return esmum;
	}

	
	private DatePattern createVariableTitleDatePattern(VariableTitleQuery variableTitleQuery, org.hibernate.Session hibSession) {
		DatePattern dp = new DatePattern();
		dp.setName(generatedVariableTitleDatePatternName(variableTitleQuery));
		dp.setSession(this.getAcadSession(variableTitleQuery, hibSession));
		dp.setPatternOffset(variableTitleQuery.getStartDate());
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(variableTitleQuery.getStartDate());
		while (cal.getTime().compareTo(variableTitleQuery.getEndDate())<=0) {
	        sb.append(1); 
	        cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		dp.setPattern(sb.toString());
		dp.setType(DatePattern.sTypeExtended);
		dp.setVisible(true);
		dp.setUniqueId((Long)hibSession.save(dp));
		    
		return dp;
	}
	private String generatedVariableTitleDatePatternName(VariableTitleQuery variableTitleQuery) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		StringBuffer sb = new StringBuffer();
		sb.append(ApplicationProperty.VariableTitleDatePatternPrefix.value())
		  .append(sdf.format(variableTitleQuery.getStartDate()))
		  .append("_")
		  .append(sdf.format(variableTitleQuery.getEndDate()));
		return sb.toString();
	}
	
	private DatePattern lookupDatePattern(VariableTitleQuery variableTitleQuery, org.hibernate.Session hibSession) {
		Session s = getAcadSession(variableTitleQuery, hibSession);
		if (s.getDefaultDatePattern() != null
				&& s.getDefaultDatePattern().getStartDate().equals(variableTitleQuery.getStartDate())
				&& s.getDefaultDatePattern().getEndDate().equals(variableTitleQuery.getEndDate())) {
			return(s.getDefaultDatePattern());
		}
		StringBuffer sb = new StringBuffer();
		sb.append("from DatePattern dp")
		  .append(" where dp.session.uniqueId = :sessionId")
		  .append(" and dp.type = ")
		  .append(DatePattern.sTypeExtended)
		  .append(" and dp.name = '")
		  .append(generatedVariableTitleDatePatternName(variableTitleQuery))
		  .append("'");
		DatePattern dp = (DatePattern)hibSession.createQuery(sb.toString()).setLong("sessionId", s.getUniqueId()).uniqueResult();
		if (dp == null) {
			for (DatePattern sdp : DatePattern.findAll(s, false, null, null)) {
				if ((sdp.getType().equals(DatePattern.sTypeStandard) 
						|| sdp.getType().equals(DatePattern.sTypeNonStandard)) 
					&& sdp.getStartDate().equals(variableTitleQuery.getStartDate())
					&& sdp.getEndDate().equals(variableTitleQuery.getEndDate())) {
					if (sdp.getPattern().contains("0") && sdp.respectsSessionHolidaysAndHasNoNonHolidayBreaks()) {
						dp = sdp;
						break;
					}
				}
			}
		}
		return dp;
	}

	
	public class VariableTitleQuery implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		String iCampus;
		String iYear;
		String iTerm;
		String iSubjectArea;
		String iCourseNumber;
		String iCourseTitle;
		String iInstructorId;
		Date iStartDate;
		Date iEndDate;
		String iFullCourseNumber;
		String iExternalId;
		String iStatus;
		
		public VariableTitleQuery() {
			super();
		}
		
		public String getCampus() {
			return iCampus;
		}
		public void setCampus(String campus) {
			this.iCampus = campus;
		}
		public String getYear() {
			return iYear;
		}
		public void setYear(String year) {
			this.iYear = year;
		}
		public String getTerm() {
			return iTerm;
		}
		public void setTerm(String term) {
			this.iTerm = term;
		}
		public String getSubjectArea() {
			return iSubjectArea;
		}
		public void setSubjectArea(String subjectArea) {
			this.iSubjectArea = subjectArea;
		}
		public String getCourseNumber() {
			return iCourseNumber;
		}
		public void setCourseNumber(String courseNumber) {
			this.iCourseNumber = courseNumber;
		}
		public String getCourseTitle() {
			return iCourseTitle;
		}
		public void setCourseTitle(String courseTitle) {
			this.iCourseTitle = courseTitle;
		}
		public String getInstructorId() {
			if (iInstructorId != null && ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue()) {
				return iInstructorId.trim().replaceFirst("^0+(?!$)", "");
			} else {
				return iInstructorId;
			}
		}
		public void setInstructorId(String instructorId) {
			this.iInstructorId = instructorId;
		}
		public Date getStartDate() {
			return iStartDate;
		}
		public void setStartDate(Date startDate) {
			this.iStartDate = startDate;
		}
		public Date getEndDate() {
			return iEndDate;
		}
		public void setEndDate(Date endDate) {
			this.iEndDate = endDate;
		}
		public String getFullCourseNumber() {
			return iFullCourseNumber;
		}
		public void setFullCourseNumber(String fullCourseNumber) {
			this.iFullCourseNumber = fullCourseNumber;
		}
		public String getExternalId() {
			return iExternalId;
		}
		public void setExternalId(String externalId) {
			this.iExternalId = externalId;
		}
		public String getStatus() {
			return iStatus;
		}
		public void setStatus(String status) {
			this.iStatus = status;
		}
				
	}
	
}
