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
package org.unitime.timetable.server.courses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.CoordinatorInterface;
import org.unitime.timetable.gwt.shared.CourseOfferingInterface.UpdateCourseOfferingRequest;
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingAddAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.model.dao.OverrideTypeDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.InstrOfferingPermIdGenerator;


@GwtRpcImplements(UpdateCourseOfferingRequest.class)
public class UpdateCourseOfferingBackend implements GwtRpcImplementation<UpdateCourseOfferingRequest, CourseOfferingInterface> {
	
	@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	Logger logger = java.util.logging.Logger.getLogger("UpdateCourseOfferingBackend");
	
	boolean limitedEdit = false, updateNote = false, updateCoordinators = false; 

	@Override
	public CourseOfferingInterface execute(UpdateCourseOfferingRequest request, SessionContext context) {
		CourseOffering courseOffering;
		Boolean hasErrors = false;
		OverrideType prohibitedOverride = OverrideType.findByReference(ApplicationProperty.OfferingWaitListProhibitedOverride.value());
		hasErrors = isWaitlistingProhibited(request.getCourseOffering(), prohibitedOverride);
		if (hasErrors) {
			request.getCourseOffering().setErrorMessage(MSG.errorWaitListingOverrideMustBeProhibited(prohibitedOverride.getLabel()));
			return request.getCourseOffering();
		}
		switch (request.getAction()) {
		case CREATE:
			context.checkPermission(request.getCourseOffering().getSubjectAreaId(), "SubjectArea", Right.AddCourseOffering);
			courseOffering = save(request.getCourseOffering(), context);
			request.getCourseOffering().setInstrOfferingId(courseOffering.getInstructionalOffering().getUniqueId());
			break;
		case UPDATE:
	    	if (context.hasPermission(request.getCourseOffering().getId(), "CourseOffering", Right.EditCourseOfferingNote)) {
	    		updateNote = true;
	    	}
	    	if (context.hasPermission(request.getCourseOffering().getId(), "CourseOffering", Right.EditCourseOfferingCoordinators)) {
	    		updateCoordinators = true;
	    	}
	    	if (updateNote || updateCoordinators) {
	    		limitedEdit = !context.hasPermission(request.getCourseOffering().getId(), "CourseOffering", Right.EditCourseOffering);
	    	} else {
	    		context.checkPermission(request.getCourseOffering().getId(), "CourseOffering", Right.EditCourseOffering);
	    	}
			courseOffering = update(request.getCourseOffering(), context);
			request.getCourseOffering().setInstrOfferingId(courseOffering.getInstructionalOffering().getUniqueId());
			break;
		}
		return request.getCourseOffering();
	}
	
	private Boolean isWaitlistingProhibited(CourseOfferingInterface courseOfferingInterface, OverrideType prohibitedOverride) {
		if (prohibitedOverride != null && (courseOfferingInterface.getWaitList() == null ? ApplicationProperty.OfferingWaitListDefault.isTrue() : courseOfferingInterface.getWaitList() == true) && !courseOfferingInterface.getCourseOverrides().contains(prohibitedOverride.getUniqueId().toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	protected CourseOffering update(CourseOfferingInterface courseOfferingInterface, SessionContext context) throws GwtRpcException {
		Transaction tx = null;

		String title = courseOfferingInterface.getTitle();
        String note = courseOfferingInterface.getScheduleBookNote();
        String crsNbr = courseOfferingInterface.getCourseNbr();
		
        try {
        	org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
        	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        		tx = hibSession.beginTransaction();
        	
            CourseOffering courseOffering = CourseOfferingDAO.getInstance().get(courseOfferingInterface.getId(), hibSession);
            InstructionalOffering io = courseOffering.getInstructionalOffering();
            
            if (!limitedEdit || updateNote) {
            	courseOffering.setScheduleBookNote(note);
            }
            
            courseOffering.getDisabledOverrides().clear();
            if (courseOfferingInterface.getCourseOverrides() != null) {
            	for (String override: courseOfferingInterface.getCourseOverrides()) {
        			courseOffering.getDisabledOverrides().add(OverrideTypeDAO.getInstance().get(Long.valueOf(override)));
        		}
            }
            
            if (courseOffering.isIsControl()) {
    			if (courseOfferingInterface.getWaitList() == null)
					io.setWaitlist(null);
				else
					io.setWaitlist(courseOfferingInterface.getWaitList());
				if (limitedEdit)
					hibSession.update(io);
    		}

    		if ((!limitedEdit || updateCoordinators) && courseOffering.isIsControl().booleanValue()) {
    			
    			boolean assignTeachingRequest = Department.isInstructorSchedulingCommitted(courseOffering.getDepartment().getUniqueId());
    			if (io.getOfferingCoordinators() == null) io.setOfferingCoordinators(new HashSet<OfferingCoordinator>());
    			List<OfferingCoordinator> coordinators = new ArrayList<OfferingCoordinator>(io.getOfferingCoordinators());
    			
            	for (int i = 0; i < courseOfferingInterface.getSendCoordinators().size(); i++) {
	        		CoordinatorInterface coordinatorObject = courseOfferingInterface.getSendCoordinators().get(i);
	        		String instructorId = coordinatorObject.getInstructorId();
	        		String responsibilityId = coordinatorObject.getResponsibilityId();
			    	String percShare = coordinatorObject.getPercShare();
	
	    			DepartmentalInstructor instructor = new DepartmentalInstructorDAO().get(Long.valueOf(instructorId));
	    			TeachingResponsibility responsibility = (Constants.BLANK_OPTION_VALUE.equals(responsibilityId) || Preference.BLANK_PREF_VALUE.equals(responsibilityId) ? null : TeachingResponsibilityDAO.getInstance().get(Long.valueOf(responsibilityId)));
	    			if (instructor != null) {
	    				OfferingCoordinator coordinator = null;
	    				for (Iterator<OfferingCoordinator> j = coordinators.iterator(); j.hasNext(); ) {
	    					OfferingCoordinator c = j.next();
	    					if (instructor.equals(c.getInstructor())) { coordinator = c; j.remove(); break; } 
	    				}
	    				if (coordinator == null) {
	    					coordinator = new OfferingCoordinator();
	    					coordinator.setInstructor(instructor);
	    					coordinator.setOffering(io);
        					if (assignTeachingRequest) {
        						for (TeachingRequest tr: io.getTeachingRequests()) {
        							if (tr.getAssignCoordinator() && tr.getAssignedInstructors().contains(instructor)) {
        								coordinator.setTeachingRequest(tr);
        								break;
        							}
        						}
        					}
	    				}
	    				coordinator.setResponsibility(responsibility);
	    				try {
	    					coordinator.setPercentShare(percShare == null ? 0 : Integer.parseInt(percShare));
	    				} catch (NumberFormatException e) {
	    					coordinator.setPercentShare(0);
	    				}
	    				io.getOfferingCoordinators().add(coordinator);
	    				instructor.getOfferingCoordinators().add(coordinator);
	    				hibSession.saveOrUpdate(coordinator);
	    			}	
	        	}

    			for (OfferingCoordinator coordinator: coordinators) {
    				coordinator.getInstructor().getOfferingCoordinators().remove(coordinator);
    				io.getOfferingCoordinators().remove(coordinator);
    				hibSession.delete(coordinator);
    			}
    			
    			Boolean coursesFundingDepartmentsEnabled = ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue();
                
                if (coursesFundingDepartmentsEnabled) {
                	if (courseOfferingInterface.getFundingDepartmentId()==null) {
                    	courseOffering.setFundingDept(null);
        			} else {
        				Department dept = DepartmentDAO.getInstance().get(courseOfferingInterface.getFundingDepartmentId(), hibSession);
        				if (dept.equals(courseOffering.getEffectiveFundingDept())) {
        					courseOffering.setFundingDept(null);
        				} else {
        					courseOffering.setFundingDept(dept==null?null:dept);
        				}
        			}
                }
    			
    			if (limitedEdit)
    				hibSession.update(io);
    		}

    		if (!limitedEdit) {
    			
    			if (courseOffering.getCourseNbr() != null && !courseOffering.getCourseNbr().equals(crsNbr) && courseOffering.getPermId() == null){
    				LastLikeCourseDemand llcd = null;
    				String permId = InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)new CourseOfferingDAO().getSession(), courseOffering).toString();
    				for(Iterator it = courseOffering.getCourseOfferingDemands().iterator(); it.hasNext();){
    					llcd = (LastLikeCourseDemand)it.next();
    					if (llcd.getCoursePermId() == null){
    						llcd.setCoursePermId(permId);
    						hibSession.update(llcd);
    					}
    				}
    				courseOffering.setPermId(permId);
    			}

    			courseOffering.setCourseNbr(crsNbr);
    			courseOffering.setTitle(title);

    			if (courseOfferingInterface.getDemandOfferingId()==null) {
                	courseOffering.setDemandOffering(null);
    			} else {
    				CourseOffering dco = CourseOfferingDAO.getInstance().get(courseOfferingInterface.getDemandOfferingId(), hibSession);
    				courseOffering.setDemandOffering(dco==null?null:dco);
    			}

    			Boolean allowAlternativeCourseOfferings = ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue();
                
                if (allowAlternativeCourseOfferings) {
                	if (courseOfferingInterface.getAlternativeCourseOfferingId()==null) {
                    	courseOffering.setAlternativeOffering(null);
        			} else {
        				CourseOffering dco = CourseOfferingDAO.getInstance().get(courseOfferingInterface.getAlternativeCourseOfferingId(), hibSession);
        				courseOffering.setAlternativeOffering(dco==null?null:dco);
        			}
                }

                if (courseOfferingInterface.getCourseTypeId()==null) {
                	courseOffering.setCourseType(null);
    			} else {
    				CourseType courseType = CourseTypeDAO.getInstance().get(courseOfferingInterface.getCourseTypeId(), hibSession);
    				courseOffering.setCourseType(courseType==null?null:courseType);
    			}

                if (courseOfferingInterface.getConsent() != null) {
        			courseOffering.setConsentType(OfferingConsentTypeDAO.getInstance().get(courseOfferingInterface.getConsent()));
        		}

                // Update credit
    			if (courseOfferingInterface.getCreditFormat() == null || courseOfferingInterface.getCreditFormat().length() == 0 || courseOfferingInterface.getCreditFormat().equals(Constants.BLANK_OPTION_VALUE)){
    				CourseCreditUnitConfig origConfig = courseOffering.getCredit();
    				if (origConfig != null){
    					courseOffering.setCredit(null);
    					hibSession.delete(origConfig);
    				}
    			} else {
    				if(courseOffering.getCredit() != null){
    					CourseCreditUnitConfig ccuc = courseOffering.getCredit();
    					if (ccuc.getCreditFormat().equals(courseOfferingInterface.getCreditFormat())){
    						boolean changed = false;
    						if (!ccuc.getCreditType().getUniqueId().equals(courseOfferingInterface.getCreditType())){
    							changed = true;
    						}
    						if (!ccuc.getCreditUnitType().getUniqueId().equals(courseOfferingInterface.getCreditUnitType())){
    							changed = true;
    						}
    						if (ccuc instanceof FixedCreditUnitConfig) {
    							FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) ccuc;
    							if (!fcuc.getFixedUnits().equals(courseOfferingInterface.getUnits())){
    								changed = true;
    							}
    						} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
    							VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) ccuc;
    							if (!vfcuc.getMinUnits().equals(courseOfferingInterface.getUnits())){
    								changed = true;
    							}
    							if (!vfcuc.getMaxUnits().equals(courseOfferingInterface.getMaxUnits())){
    								changed = true;
    							}
    							if (vfcuc instanceof VariableRangeCreditUnitConfig) {
    								VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) vfcuc;
    								if (vrcuc.isFractionalIncrementsAllowed() == null || !vrcuc.isFractionalIncrementsAllowed().equals(courseOfferingInterface.getFractionalIncrementsAllowed())){
    									changed = true;
    								}
    							}
    						}
    						if (changed){
    							CourseCreditUnitConfig origConfig = courseOffering.getCredit();
    							courseOffering.setCredit(null);
    							hibSession.delete(origConfig);
    							courseOffering.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseOfferingInterface.getCreditFormat(), courseOfferingInterface.getCreditType(), courseOfferingInterface.getCreditUnitType(), courseOfferingInterface.getUnits(), courseOfferingInterface.getMaxUnits(), courseOfferingInterface.getFractionalIncrementsAllowed(), new Boolean(true)));
    							courseOffering.getCredit().setOwner(courseOffering);
    						}
    					} else {
    						CourseCreditUnitConfig origConfig = courseOffering.getCredit();
    						courseOffering.setCredit(null);
    						hibSession.delete(origConfig);
    						courseOffering.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseOfferingInterface.getCreditFormat(), courseOfferingInterface.getCreditType(), courseOfferingInterface.getCreditUnitType(), courseOfferingInterface.getUnits(), courseOfferingInterface.getMaxUnits(), courseOfferingInterface.getFractionalIncrementsAllowed(), new Boolean(true)));
    						courseOffering.getCredit().setOwner(courseOffering);
    					}
    				} else {
    					courseOffering.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseOfferingInterface.getCreditFormat(), courseOfferingInterface.getCreditType(), courseOfferingInterface.getCreditUnitType(), courseOfferingInterface.getUnits(), courseOfferingInterface.getMaxUnits(), courseOfferingInterface.getFractionalIncrementsAllowed(), new Boolean(true)));
    					courseOffering.getCredit().setOwner(courseOffering);
    				}

    				if (courseOffering.getCredit() != null){
    					hibSession.saveOrUpdate(courseOffering.getCredit());
    				}
    			}

    			if (courseOffering.isIsControl()) {
    				io.setByReservationOnly(courseOfferingInterface.getByReservationOnly());

    				try {
    					io.setLastWeekToEnroll(courseOfferingInterface.getLastWeekToEnroll());
    				} catch (Exception e) {
    					io.setLastWeekToEnroll(null);
    				}
    				try {
    					io.setLastWeekToChange(courseOfferingInterface.getLastWeekToChange());
    				} catch (Exception e) {
    					io.setLastWeekToChange(null);
    				}
    				try{
    					io.setLastWeekToDrop(courseOfferingInterface.getLastWeekToDrop());
    				} catch (Exception e) {
    					io.setLastWeekToDrop(null);
    				}
    				io.setNotes(courseOfferingInterface.getNotes() == null || courseOfferingInterface.getNotes().length() <= 2000 ? courseOfferingInterface.getNotes() : courseOfferingInterface.getNotes().substring(0, 2000));

    				hibSession.update(io);
    			}
    			if (ApplicationProperty.CourseOfferingEditExternalIds.isTrue()) {
    				courseOffering.setExternalUniqueId(courseOfferingInterface.getExternalId() == null || courseOfferingInterface.getExternalId().isEmpty() ? null : courseOfferingInterface.getExternalId());
    			}
    		}

    		hibSession.saveOrUpdate(courseOffering);
    		
    		ChangeLog.addChange(
                    hibSession, 
                    context, 
                    courseOffering, 
                    ChangeLog.Source.COURSE_OFFERING_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    courseOffering.getSubjectArea(), 
                    courseOffering.getDepartment());

    		
    		if (limitedEdit && permissionOfferingLockNeeded.check(context.getUser(), io)) {
    			StudentSectioningQueue.offeringChanged(hibSession, context.getUser(), io.getSessionId(), io.getUniqueId()); 
    		}
        	
            hibSession.flush();
            tx.commit();

            hibSession.refresh(courseOffering);

            hibSession.refresh(io);
            
            String className = ApplicationProperty.ExternalActionCourseOfferingEdit.value();
        	if (className != null && className.trim().length() > 0){
        		if (io == null){
        			io = courseOffering.getInstructionalOffering();
        		}
	        	ExternalCourseOfferingEditAction editAction = (ExternalCourseOfferingEditAction) (Class.forName(className).newInstance());
	       		editAction.performExternalCourseOfferingEditAction(io, hibSession);
        	}
            
        	return courseOffering;

	    } catch (Exception e) {
	    	try {
				if (tx!=null && tx.isActive())
					tx.rollback();
			}
			catch (Exception e1) { }
	    	throw new GwtRpcException(e.getMessage(), e);
	    }
	}
	
	protected CourseOffering save(CourseOfferingInterface courseOfferingInterface, SessionContext context) throws GwtRpcException {
		Transaction tx = null;
		InstructionalOffering io = new InstructionalOffering();

        try {
        	org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
        	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        		tx = hibSession.beginTransaction();
        	
            CourseOffering courseOffering = new CourseOffering();

            courseOffering.setCourseNbr(courseOfferingInterface.getCourseNbr()); 
            courseOffering.setTitle(courseOfferingInterface.getTitle());
            courseOffering.setScheduleBookNote(courseOfferingInterface.getScheduleBookNote());
             
        	SubjectArea subjArea = SubjectAreaDAO.getInstance().get(courseOfferingInterface.getSubjectAreaId(), hibSession);
            courseOffering.setSubjectArea(subjArea);
            courseOffering.setSubjectAreaAbbv(subjArea.getSubjectAreaAbbreviation());
            courseOffering.setProjectedDemand(new Integer(0));
            courseOffering.setDemand(new Integer(0));
            courseOffering.setNbrExpectedStudents(new Integer(0));
            courseOffering.setIsControl(new Boolean(true));
            courseOffering.setPermId(InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)new CourseOfferingDAO().getSession(), courseOffering).toString());
            subjArea.getCourseOfferings().add(courseOffering);

		    io.setNotOffered(new Boolean(false));
		    io.setSession(subjArea.getSession());
		    io.generateInstrOfferingPermId();
		    io.setLimit(new Integer(0));
		    io.setWaitlist(courseOfferingInterface.getWaitList());

		    courseOffering.setInstructionalOffering(io);
		    io.addTocourseOfferings(courseOffering);
		    
		    courseOffering.setDisabledOverrides(new HashSet<OverrideType>());
		    if (courseOfferingInterface.getCourseOverrides() != null) {
		    	for (String override: courseOfferingInterface.getCourseOverrides()) {
					courseOffering.getDisabledOverrides().add(OverrideTypeDAO.getInstance().get(Long.valueOf(override)));
				}
		    }

		    io.setByReservationOnly(courseOfferingInterface.getByReservationOnly());
		    
		    io.setLastWeekToChange(courseOfferingInterface.getLastWeekToChange());
		    io.setLastWeekToEnroll(courseOfferingInterface.getLastWeekToEnroll());
		    io.setLastWeekToDrop(courseOfferingInterface.getLastWeekToDrop());

		    io.setNotes(courseOfferingInterface.getNotes() == null || courseOfferingInterface.getNotes().length() <= 2000 ? courseOfferingInterface.getNotes() : courseOfferingInterface.getNotes().substring(0, 2000));
		    
		    io.setOfferingCoordinators(new HashSet<OfferingCoordinator>());
		    
		    for (int i = 0; i < courseOfferingInterface.getSendCoordinators().size(); i++) {
		    	
		    	CoordinatorInterface coordinatorObject = courseOfferingInterface.getSendCoordinators().get(i);
		    	
		    	String instructorId = coordinatorObject.getInstructorId();
		    	String responsibilityId = coordinatorObject.getResponsibilityId();
		    	String percShare = coordinatorObject.getPercShare();
		    		    
    		    DepartmentalInstructor instructor = new DepartmentalInstructorDAO().get(Long.valueOf(instructorId));
    			TeachingResponsibility responsibility = (Constants.BLANK_OPTION_VALUE.equals(responsibilityId) || Preference.BLANK_PREF_VALUE.equals(responsibilityId) ? null : TeachingResponsibilityDAO.getInstance().get(Long.valueOf(responsibilityId)));
    			if (instructor != null) {
    				OfferingCoordinator coordinator = new OfferingCoordinator();
    				coordinator.setInstructor(instructor);
    				coordinator.setOffering(io);
    				coordinator.setResponsibility(responsibility);
    				try {
    					coordinator.setPercentShare(percShare == null ? 0 : Integer.parseInt(percShare));
    				} catch (NumberFormatException e) {
    					coordinator.setPercentShare(0);
    				}	
    				io.getOfferingCoordinators().add(coordinator);
    				instructor.getOfferingCoordinators().add(coordinator);
    			}
		    }

            if (courseOfferingInterface.getDemandOfferingId()==null) {
            	courseOffering.setDemandOffering(null);
			} else {
				CourseOffering dco = CourseOfferingDAO.getInstance().get(courseOfferingInterface.getDemandOfferingId(), hibSession);
				courseOffering.setDemandOffering(dco==null?null:dco);
			}
            
            Boolean allowAlternativeCourseOfferings = ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue();
            
            if (allowAlternativeCourseOfferings) {
            	if (courseOfferingInterface.getAlternativeCourseOfferingId()==null) {
                	courseOffering.setAlternativeOffering(null);
    			} else {
    				CourseOffering dco = CourseOfferingDAO.getInstance().get(courseOfferingInterface.getAlternativeCourseOfferingId(), hibSession);
    				courseOffering.setAlternativeOffering(dco==null?null:dco);
    			}
            }
            
            Boolean coursesFundingDepartmentsEnabled = ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue();
            
            if (coursesFundingDepartmentsEnabled) {
            	if (courseOfferingInterface.getFundingDepartmentId()==null) {
                	courseOffering.setFundingDept(null);
    			} else {
    				Department dept = DepartmentDAO.getInstance().get(courseOfferingInterface.getFundingDepartmentId(), hibSession);
    				if (dept.equals(courseOffering.getEffectiveFundingDept())) {
    					courseOffering.setFundingDept(null);
    				} else {
    					courseOffering.setFundingDept(dept==null?null:dept);
    				}
    			}
            }
            
            Boolean canEditExternalIds = ApplicationProperty.CourseOfferingEditExternalIds.isTrue();
            
            if (canEditExternalIds) {
            	if (courseOfferingInterface.getExternalId() == null || courseOfferingInterface.getExternalId().isEmpty()) {
                	courseOffering.setExternalUniqueId(null);
    			} else {
    				courseOffering.setExternalUniqueId(courseOfferingInterface.getExternalId());
    			}
            }

            if (courseOfferingInterface.getCourseTypeId()==null) {
            	courseOffering.setCourseType(null);
			} else {
				CourseType courseType = CourseTypeDAO.getInstance().get(courseOfferingInterface.getCourseTypeId(), hibSession);
				courseOffering.setCourseType(courseType==null?null:courseType);
			}
            
    		if (courseOfferingInterface.getConsent() != null) {
    			courseOffering.setConsentType(OfferingConsentTypeDAO.getInstance().get(courseOfferingInterface.getConsent()));
    		}
    		
            if (courseOffering.getUniqueId() == null) {
           	
            	if (courseOffering.getUniqueId() == null) {
    		    	hibSession.save(io);
    		    }
            	courseOfferingInterface.setId((Long)hibSession.save(courseOffering));
            	
            	courseOffering.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseOfferingInterface.getCreditFormat(), courseOfferingInterface.getCreditType(), courseOfferingInterface.getCreditUnitType(), courseOfferingInterface.getUnits(), courseOfferingInterface.getMaxUnits(), courseOfferingInterface.getFractionalIncrementsAllowed(), new Boolean(true)));
            	if (courseOffering.getCredit() != null) {
            		courseOffering.getCredit().setOwner(courseOffering);
            		hibSession.saveOrUpdate(courseOffering.getCredit());
            	}

                for (OfferingCoordinator coordinator: io.getOfferingCoordinators()) {
                	hibSession.saveOrUpdate(coordinator);
                }

            } else {
            	hibSession.update(courseOffering);
            }
            
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    courseOffering, 
                    ChangeLog.Source.COURSE_OFFERING_EDIT, 
                    ChangeLog.Operation.CREATE, 
                    courseOffering.getSubjectArea(), 
                    courseOffering.getDepartment());
            
            
            hibSession.flush();
			tx.commit();
			HibernateUtil.clearCache();
			

			if (context.hasPermission(io, Right.OfferingCanLock)) {
				io.getSession().lockOffering(io.getUniqueId());
			}
			    	
			if (permissionOfferingLockNeeded.check(context.getUser(), io)) {
				StudentSectioningQueue.offeringChanged(hibSession, context.getUser(), io.getSessionId(), io.getUniqueId());
			}
        		 
			String className1 = ApplicationProperty.ExternalActionInstructionalOfferingAdd.value();
        	if (className1 != null && className1.trim().length() > 0){
	        	ExternalInstructionalOfferingAddAction addAction = (ExternalInstructionalOfferingAddAction) (Class.forName(className1).newInstance());
	       		addAction.performExternalInstructionalOfferingAddAction(io, hibSession);
        	}
        	
        	String className2 = ApplicationProperty.ExternalActionCourseOfferingEdit.value();
			if (className2 != null && className2.trim().length() > 0){
				ExternalCourseOfferingEditAction editAction = (ExternalCourseOfferingEditAction) (Class.forName(className2).newInstance());
				editAction.performExternalCourseOfferingEditAction(io, hibSession);
			}

			return courseOffering;
	    } catch (Exception e) {
	    	try {
				if (tx!=null && tx.isActive())
					tx.rollback();
			}
			catch (Exception e1) { }
	    	throw new GwtRpcException(e.getMessage(), e);
	    }
	}
}
