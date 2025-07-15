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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListUpdateRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.CrossListedCourse;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.interfaces.ExternalCourseCrosslistAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingRemoveAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingInCrosslistAddAction;
import org.unitime.timetable.model.AdvisorClassPref;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorSectioningPref;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(CrossListUpdateRequest.class)
public class CrossListUpdateBackend implements GwtRpcImplementation<CrossListUpdateRequest, GwtRpcResponseNull> {

	@Override
	public GwtRpcResponseNull execute(CrossListUpdateRequest request, SessionContext context) {
		InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(request.getOfferingId());
		context.checkPermission(io, Right.InstructionalOfferingCrossLists);
		
		Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
        hibSession.setHibernateFlushMode(FlushMode.MANUAL);
        
        Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
        	
    		Set<Long> courseIds = new HashSet<Long>();
            Set<Long> origCourseIds = new HashSet<Long>();
            for (CourseOffering course: io.getCourseOfferings())
            	origCourseIds.add(course.getUniqueId());
            if (request.hasCourses())
            	for (CrossListedCourse course: request.getCourses())
            		courseIds.add(course.getCourseId());
            
            List<CourseOffering> deletedOfferings = new ArrayList<CourseOffering>();
            List<CurriculumCourse> cc = new ArrayList<CurriculumCourse>();
            List<CourseRequest> courseRequests = new ArrayList<CourseRequest>();
            Map<String, List<AdvisorCourseRequest>> advCourseReqs = new HashMap<String, List<AdvisorCourseRequest>>();
            
            for (Long origCrs: origCourseIds) {
    	        // 1. For all deleted courses - create new offering and make 'not offered'
                if (!courseIds.contains(origCrs)) {
                    Debug.debug("Course removed from offering: " + origCrs);
                    
                    // Create new instructional offering 
                    InstructionalOffering io1 = new InstructionalOffering();
                    CourseOffering co1 = CourseOfferingDAO.getInstance().get(origCrs, hibSession);
                    SubjectArea sa1 = co1.getSubjectArea();
                    
                    context.checkPermission(co1, Right.CourseOfferingDeleteFromCrossList);
                    
                    // Copy attributes of old instr offering - make not offered
                    io1.setNotOffered(true);
                    io1.setSession(io.getSession());
                    io1.setByReservationOnly(io.getByReservationOnly());
                    
                    // Copy attributes of old crs offering - set controlling	                
                    CourseOffering co2 = (CourseOffering)co1.clone();                    
                    co2.setIsControl(true);
                    
                    for (CurriculumCourse x: hibSession.createQuery(
                    		"from CurriculumCourse where course.uniqueId = :courseId", CurriculumCourse.class)
                    		.setParameter("courseId", co1.getUniqueId()).list()) {
                    	cc.add(x.clone(co2));
                    	x.getClassification().getCourses().remove(x);
                    	hibSession.remove(x);
                    }
                    if (ApplicationProperty.ModifyCrossListKeepCourseRequests.isTrue())
                    	for (CourseRequest oldReq: hibSession.createQuery(
                    			"from CourseRequest where courseOffering.uniqueId = :courseId", CourseRequest.class)
                    			.setParameter("courseId", co1.getUniqueId()).list()) {
                    		CourseRequest newReq = new CourseRequest();
                    		newReq.setAllowOverlap(oldReq.getAllowOverlap());
                    		newReq.setOrder(oldReq.getOrder());
                    		newReq.setCredit(oldReq.getCredit());
                    		newReq.setCourseOffering(co2);
                    		newReq.setCourseDemand(oldReq.getCourseDemand());
                    		oldReq.getCourseDemand().getCourseRequests().remove(oldReq);
                    		courseRequests.add(newReq);
                    		hibSession.remove(oldReq);
                    	}
                    
                    List<AdvisorCourseRequest> acrs = hibSession.createQuery(
                			"from AdvisorCourseRequest where courseOffering.uniqueId = :courseId", AdvisorCourseRequest.class)
                			.setParameter("courseId", co1.getUniqueId()).list();
                    advCourseReqs.put(co2.getCourseName(), acrs);
                    for (AdvisorCourseRequest acr: acrs) acr.setCourseOffering(null);
                    
                    deletedOfferings.add(co2);

                    // Remove from original inst offr
    		        for (CourseOffering co3: io.getCourseOfferings()) {
    		            if (co3.equals(co1)) {
    	                    // Remove from Subject Area
    				        SubjectArea sa = co3.getSubjectArea();
    				        sa.getCourseOfferings().remove(co1);
    		                hibSession.merge(sa);
    		            }
    		        }
    		        
    		        sa1.getCourseOfferings().remove(co1);
                    hibSession.merge(sa1);
                    
                    // Delete old course offering
    		        io.removeCourseOffering(co1);
    		        
                    Event.deleteFromEvents(hibSession, co1);
    	            Exam.deleteFromExams(hibSession, co1);
    	            
                	String className = ApplicationProperty.ExternalActionCourseOfferingRemove.value(); 
                	if (className != null && className.trim().length() > 0){
                		ExternalCourseOfferingRemoveAction removeAction = (ExternalCourseOfferingRemoveAction) (Class.forName(className).getDeclaredConstructor().newInstance());
        	       		removeAction.performExternalCourseOfferingRemoveAction(co1, hibSession);
                	}
    		        hibSession.remove(co1);
    		        
    		        //io.setCourseOfferings(offerings);
    		        
    	            hibSession.merge(io);
    	            hibSession.flush();
                    
                    // Add course to instructional offering
                    co2.setInstructionalOffering(io1);
                    io1.addToCourseOfferings(co2);

                    // Update
                    if (io1.getInstrOfferingPermId()==null) io1.generateInstrOfferingPermId();
                    hibSession.persist(io1);
                    hibSession.flush();

                	className = ApplicationProperty.ExternalActionInstructionalOfferingInCrosslistAdd.value();
                	if (className != null && className.trim().length() > 0){
                		ExternalInstructionalOfferingInCrosslistAddAction addAction = (ExternalInstructionalOfferingInCrosslistAddAction) (Class.forName(className).getDeclaredConstructor().newInstance());
        	       		addAction.performExternalInstructionalOfferingInCrosslistAddAction(io1, hibSession);
                	}
                }
                
                // 2. For all existing courses - update controlling attribute and reservation limits
                else {
                    Debug.debug("Updating controlling course  and course reservation: " + origCrs);	                

                    // Update controlling course attribute
                    CourseOffering co = CourseOfferingDAO.getInstance().get(origCrs, hibSession);
                    CrossListedCourse clc = request.getCourse(origCrs);
                    co.setIsControl(clc.isControl());
                    
                    if (ApplicationProperty.ModifyCrossListSingleCourseLimit.isTrue())
                		co.setReservation(clc.getReserved());
                	else
                		co.setReservation(courseIds.size() > 1 ? clc.getReserved() : null);

                    hibSession.merge(co);
                    hibSession.flush();
                }
            }
            
            // 3. For all added courses - delete all preferences and change the instr offering id  
            List<CourseOffering> addedOfferings = new ArrayList<CourseOffering>();
            for (Long course: courseIds) {
                // Course added to offering
                if (!origCourseIds.contains(course)) {
                    
                    Debug.debug("Course added to offering: " + course);

                    CourseOffering co1 = CourseOfferingDAO.getInstance().get(course);                
                    InstructionalOffering io1 = co1.getInstructionalOffering();
                    SubjectArea sa = io1.getControllingCourseOffering().getSubjectArea();
                    
                    // Copy course offerings
                    for (CourseOffering co2: io1.getCourseOfferings()) {
                        SubjectArea sa2 = co2.getSubjectArea();
                        
                        // Create a copy
                        CourseOffering co3 = (CourseOffering)co2.clone();
                        CrossListedCourse clc = request.getCourse(course);
                        co3.setIsControl(clc.isControl());
                        
                        for (CurriculumCourse x: hibSession.createQuery(
                        		"from CurriculumCourse where course.uniqueId = :courseId", CurriculumCourse.class)
                        		.setParameter("courseId", co2.getUniqueId()).list()) {
                        	cc.add(x.clone(co3));
                        	x.getClassification().getCourses().remove(x);
                        	hibSession.remove(x);
                        }
                        if (ApplicationProperty.ModifyCrossListKeepCourseRequests.isTrue())
                        	for (CourseRequest oldReq: hibSession.createQuery(
                        			"from CourseRequest where courseOffering.uniqueId = :courseId", CourseRequest.class)
                        			.setParameter("courseId", co2.getUniqueId()).list()) {
                        		CourseRequest newReq = new CourseRequest();
                        		newReq.setAllowOverlap(oldReq.getAllowOverlap());
                        		newReq.setOrder(oldReq.getOrder());
                        		newReq.setCredit(oldReq.getCredit());
                        		newReq.setCourseOffering(co3);
                        		newReq.setCourseDemand(oldReq.getCourseDemand());
                        		oldReq.getCourseDemand().getCourseRequests().remove(oldReq);
                        		courseRequests.add(newReq);
                        		hibSession.remove(oldReq);
                        	}

                        List<AdvisorCourseRequest> acrs = hibSession.createQuery(
                    			"from AdvisorCourseRequest where courseOffering.uniqueId = :courseId", AdvisorCourseRequest.class)
                    			.setParameter("courseId", co2.getUniqueId()).list();
                        advCourseReqs.put(co3.getCourseName(), acrs);
                        addedOfferings.add(co3);
                        for (AdvisorCourseRequest acr: acrs) acr.setCourseOffering(null);
                        
                        co3.setReservation(clc.getReserved());    	                

                        sa2.getCourseOfferings().remove(co2);
                        hibSession.merge(sa2);
                        
                        // Delete course offering
                        io1.removeCourseOffering(co2);
                        Event.deleteFromEvents(hibSession, co2);
                        Exam.deleteFromExams(hibSession, co2);
                    	String className = ApplicationProperty.ExternalActionCourseOfferingRemove.value();
                    	if (className != null && className.trim().length() > 0){
                    		ExternalCourseOfferingRemoveAction removeAction = (ExternalCourseOfferingRemoveAction) (Class.forName(className).getDeclaredConstructor().newInstance());
            	       		removeAction.performExternalCourseOfferingRemoveAction(co2, hibSession);
                    	}

                        hibSession.remove(co2);
                        hibSession.flush();
                    }
                    
                    Event.deleteFromEvents(hibSession, io1);
                    Exam.deleteFromExams(hibSession, io1);

                    hibSession.remove(io1);
                    hibSession.flush();
                    
                    hibSession.merge(sa);
                    
                }            
            }

            hibSession.flush();
            
            // Update Offering - Added Offerings       
            for (int i=0; i<addedOfferings.size(); i++) {
                CourseOffering co3 = addedOfferings.get(i);
                co3.setInstructionalOffering(io);
                io.addToCourseOfferings(co3);
                hibSession.persist(co3);
                
                hibSession.flush();

                hibSession.merge(io);
            }
            for (CurriculumCourse x: cc)
            	hibSession.persist(x);
            for (CourseRequest x: courseRequests) {
    	        x.getCourseDemand().getCourseRequests().add(x);
            	hibSession.persist(x);
            }
            // for advisor course recommendations, keep the requests but remove class preferences as they no longer apply (courses moved away)
            for (CourseOffering co: deletedOfferings) {
            	List<AdvisorCourseRequest> acrs = advCourseReqs.get(co.getCourseName());
            	if (acrs != null) {
            		for (AdvisorCourseRequest req: acrs) {
            			req.setCourseOffering(co);
                    	for (Iterator<AdvisorSectioningPref> ip = req.getPreferences().iterator(); ip.hasNext(); ) {
                    		AdvisorSectioningPref p = ip.next();
                    		if (p instanceof AdvisorClassPref) {
                    			hibSession.remove(p);
                    			ip.remove();
                    		}
                    	}
        	        	hibSession.merge(req);
        	        }
            	}
            }
            // for advisor course recommendations, keep the requests (courses moved in)
            for (CourseOffering co: addedOfferings) {
            	List<AdvisorCourseRequest> acrs = advCourseReqs.get(co.getCourseName());
            	if (acrs != null) {
            		for (AdvisorCourseRequest req: acrs) {
            			req.setCourseOffering(co);
        	        	hibSession.merge(req);
        	        }
            	}
            }
            
            // Update managing department on all classes
            Department dept = io.getControllingCourseOffering().getDepartment();
            for (InstrOfferingConfig cfg: io.getInstrOfferingConfigs()) {
    	        for (SchedulingSubpart subpart: cfg.getSchedulingSubparts()) {
    		        for (Class_ cls: subpart.getClasses()) {
    		        	// Only change departmental class managing dept and not externally managed
    		        	if (!cls.getManagingDept().isExternalManager()) {
    			        	cls.setManagingDept(dept, context.getUser(), hibSession);
    			        	hibSession.merge(cls);
    		        	}
    		        	// Fix class instructors
    		        	for (Iterator<ClassInstructor> i = cls.getClassInstructors().iterator(); i.hasNext();) {
    		        		ClassInstructor ci = i.next();
    		        		if (!ci.getInstructor().getDepartment().equals(dept)) {
    		        			ci.getInstructor().getClasses().remove(ci);
    		        			DepartmentalInstructor di = (ci.getInstructor().getExternalUniqueId() == null ? null : 
    		        					DepartmentalInstructor.findByPuidDepartmentId(ci.getInstructor().getExternalUniqueId(), dept.getUniqueId(), hibSession));
    		        			if (di == null) {
    		        				hibSession.remove(ci);
    		        				i.remove();
    		        			} else {
    		        				ci.setInstructor(di);
    		        				di.getClasses().add(ci);
    		        				hibSession.merge(ci);
    		        			}
    		        		}
    		        	}
    		        }
    	        }
            }
            if (io.getOfferingCoordinators() != null) {
            	// Fix offering coordinators
            	for (Iterator<OfferingCoordinator> i = io.getOfferingCoordinators().iterator(); i.hasNext(); ) {
            		OfferingCoordinator oc = i.next();
            		if (!oc.getInstructor().getDepartment().equals(dept)) {
            			oc.getInstructor().getOfferingCoordinators().remove(oc);
            			DepartmentalInstructor di = (oc.getInstructor().getExternalUniqueId() == null ? null : 
            					DepartmentalInstructor.findByPuidDepartmentId(oc.getInstructor().getExternalUniqueId(), dept.getUniqueId(), hibSession));
            			if (di == null) {
            				hibSession.remove(oc);
            				i.remove();
            			} else {
            				oc.setInstructor(di);
            				di.getOfferingCoordinators().add(oc);
            				hibSession.merge(oc);
            			}
            		}
    	        }
            }
            
            
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    io, 
                    ChangeLog.Source.CROSS_LIST, 
                    ChangeLog.Operation.UPDATE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);
            
            tx.commit();
            hibSession.flush();
            hibSession.clear();
            
        	String className = ApplicationProperty.ExternalActionCourseCrosslist.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalCourseCrosslistAction addAction = (ExternalCourseCrosslistAction) (Class.forName(className).getDeclaredConstructor().newInstance());
           		addAction.performExternalCourseCrosslistAction(io, hibSession);
        	}

        } catch (Exception e) {
            Debug.error(e);
            try {
                if(tx!=null && tx.isActive())
                    tx.rollback();
            }
            catch (Exception e1) { }
            throw new GwtRpcException(e.getMessage(), e);
        }

        return null;
	}

}
