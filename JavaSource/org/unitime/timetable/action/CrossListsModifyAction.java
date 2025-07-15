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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.CrossListsModifyForm;
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
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller, Heston Fernandes, Stephanie Schluttenhofer, Zuzana Mullerova
 */
@Action(value="crossListsModify", results = {
		@Result(name = "crossListsModify", type = "tiles", location = "crossListsModify.tiles"),
		@Result(name = "instructionalOfferingDetail", type = "redirect", location = "/instructionalOfferingDetail.action", 
				params = { "io", "${form.instrOfferingId}", "op", "view"})
	})
@TilesDefinition(name = "crossListsModify.tiles", extend =  "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Instructional Offering Cross Lists"),
		@TilesPutAttribute(name = "body", value = "/user/crossListsModify.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true")
})
public class CrossListsModifyAction extends UniTimeAction<CrossListsModifyForm> {
	private static final long serialVersionUID = -6417943409851586772L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	protected String op2 = null;
	protected Long uid = null;
	protected Long deletedCourseOfferingId = null;

	public String getHdnOp() { return op2; }
	public void setHdnOp(String hdnOp) { this.op2 = hdnOp; }
	public Long getUid() { return uid; }
	public void setUid(Long uid) { this.uid = uid; }
	public Long getDeletedCourseOfferingId() { return deletedCourseOfferingId; }
	public void setDeletedCourseOfferingId(Long deletedCourseOfferingId) { this.deletedCourseOfferingId = deletedCourseOfferingId; }
	
    public String execute() throws Exception {
    	if (form == null) {
    		form = new CrossListsModifyForm();
    	}
        
        // Get operation
    	if (op == null) op = form.getOp();
    	if (op2 != null && !op2.isEmpty()) op = op2;
        
        if(op==null || op.trim().length()==0)
            throw new Exception (MSG.errorOperationNotInterpreted() + op);
        
        if (op.equals(MSG.actionBackToIODetail())) {
        	return "instructionalOfferingDetail";
        }

        // Set up Lists
        form.setOp(op);
        LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
			@Override
			public boolean accept(CourseOffering course) {
				return course.getInstructionalOffering().isNotOffered();
			}
		});
        
        // First access to screen
        if(op.equalsIgnoreCase(MSG.actionCrossLists())) {
            doLoad(uid);
        }
        
        // Add a course offering
        if(op.equalsIgnoreCase(MSG.actionAddCourseToCrossList())) {
            // Validate data input
        	form.validate(this);
        	if (!hasFieldErrors()) {
                Long addedOffering = form.getAddCourseOfferingId();
                CourseOfferingDAO cdao = CourseOfferingDAO.getInstance();
                CourseOffering co = cdao.get(addedOffering);
                
                // Check reservations limit
                form.addToCourseOfferings(co, sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent) || sessionContext.getUser().getCurrentAuthority().hasQualifier(co.getDepartment()), true);
                form.setAddCourseOfferingId(null);
            }
        }
        
        // Remove a course offering
        if (op.equalsIgnoreCase("Delete")) {
            if (deletedCourseOfferingId!=null)
                form.removeFromCourseOfferings(deletedCourseOfferingId);
        }
        
        // Update the course offering
        if(op.equalsIgnoreCase(MSG.actionUpdateCrossLists())) {
            // Validate data input
            form.validate(this);
            if (!hasFieldErrors()) {
                doUpdate();
                return "instructionalOfferingDetail";
            }
        }
        
        // Determine if a course offering cannot be deleted
        setReadOnlyCourseId();
        
        // Remove the courses that are already part of this offering from list of courses
        filterCourseOfferingList();
        
        return "crossListsModify";
    }

    /**
     * Ensures that all offerings that are part of the instructional offering
     * does not appear in the drop down list 
     */
    private void filterCourseOfferingList() {
        Collection<Long> existingOfferings = form.getCourseOfferingIds();
        Collection<CourseOffering> offerings = (Collection<CourseOffering>) request.getAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME);
        
        for (Iterator<CourseOffering> i=offerings.iterator(); i.hasNext(); ) {
            CourseOffering co = i.next();
            if ((!co.getInstructionalOffering().isNotOffered() && !co.getInstructionalOffering().getInstrOfferingConfigs().isEmpty())
            	|| co.getInstructionalOffering().getCourseOfferings().size() > 1) {
            	i.remove(); continue;
            }
            for (Iterator<Long> j=existingOfferings.iterator(); j.hasNext(); ) {
                Long courseId = (Long) j.next();
                if (courseId.equals(co.getUniqueId()))
                    i.remove();
            }
        }
        request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, offerings);        
    }

    /**
     * Compares the modified offering to the original offering
     * If more than one offering is in common then all offerings show the 'Delete' icon
     * If only one offering is in common that offering cannot be deleted
     */
    private void setReadOnlyCourseId() {
        short ct = 0;
        List<Long> originalOfferings = form.getOriginalOfferings();
        List<Long> courseOfferingIds = form.getCourseOfferingIds();
        
        for (Iterator<Long> i=courseOfferingIds.iterator(); i.hasNext(); ) {
            Long cid = i.next();
            if (originalOfferings.indexOf(cid)>=0) {
                
                // More than one course from the original offering exists in the modified one
                if(++ct>1) {
                    form.setReadOnlyCrsOfferingId(null);
                    break;
                }
                else {
                    form.setReadOnlyCrsOfferingId(Long.valueOf(cid));
                }                    
            }
        }        

        Debug.debug("Read Only Ctr Course: " + form.getReadOnlyCrsOfferingId());
    }

    /**
     * Update the instructional offering
     */
    private void doUpdate() 
    	throws Exception {
        
        // Get the modified offering
        List<Long> courseIds = form.getCourseOfferingIds();
        List<Long> origCourseIds = form.getOriginalOfferings();
        
        // Get Offering
        CourseOfferingDAO cdao = CourseOfferingDAO.getInstance();
        InstructionalOfferingDAO idao = InstructionalOfferingDAO.getInstance();
        InstructionalOffering io = idao.get(form.getInstrOfferingId());
        Session hibSession = idao.getSession();
        hibSession.setHibernateFlushMode(FlushMode.MANUAL);
        Transaction tx = null;
        List<CurriculumCourse> cc = new ArrayList<CurriculumCourse>();
        List<CourseRequest> courseRequests = new ArrayList<CourseRequest>();
        Map<String, List<AdvisorCourseRequest>> advCourseReqs = new HashMap<String, List<AdvisorCourseRequest>>();
        
        try {
	        tx = hibSession.beginTransaction();
	        
        	List<CourseOffering> deletedOfferings = new ArrayList<CourseOffering>();
	        for (Long origCrs: origCourseIds) {
		        // 1. For all deleted courses - create new offering and make 'not offered'
	            if (courseIds.indexOf(origCrs)<0) {
	                Debug.debug("Course removed from offering: " + origCrs);
	                
	                // Create new instructional offering 
	                InstructionalOffering io1 = new InstructionalOffering();
	                CourseOffering co1 = cdao.get(origCrs);
	                SubjectArea sa1 = co1.getSubjectArea();
	                
	                sessionContext.checkPermission(co1, Right.CourseOfferingDeleteFromCrossList);
	                
	                // Copy attributes of old instr offering - make not offered
	                io1.setNotOffered(Boolean.valueOf(true));
	                io1.setSession(io.getSession());
	                io1.setByReservationOnly(io.getByReservationOnly());
	                
	                // Copy attributes of old crs offering - set controlling	                
                    CourseOffering co2 = (CourseOffering)co1.clone();                    
                    co2.setIsControl(Boolean.valueOf(true));
                    
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
/*	                
	                hibSession.saveOrUpdate(io1);
	                hibSession.flush();
*/
	                // Remove from original inst offr
			        Set offerings = io.getCourseOfferings();
			        for (Iterator i=offerings.iterator(); i.hasNext(); ) {
			            
			            CourseOffering co3 = (CourseOffering) i.next();
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
	                CourseOffering co = cdao.get(Long.valueOf(origCrs));
	                if(form.getCtrlCrsOfferingId().equals(co.getUniqueId()))
	                    co.setIsControl(Boolean.valueOf(true));
	                else
	                    co.setIsControl(Boolean.valueOf(false));
	                
	                // Update course reservation
	                int indx = form.getIndex(origCrs);
	                try {
	                	if (ApplicationProperty.ModifyCrossListSingleCourseLimit.isTrue())
	                		co.setReservation(form.getLimits(indx));
	                	else
	                		co.setReservation(courseIds.size() > 1 ? form.getLimits(indx) : null);
	                } catch (NumberFormatException e) {
	                	co.setReservation(null);
	                }

	                hibSession.merge(co);

	                hibSession.flush();
	            }
	        }
	        
	        // 3. For all added courses - delete all preferences and change the instr offering id  
	        List<CourseOffering> addedOfferings = new ArrayList<CourseOffering>();
	        for (Long course: courseIds) {
	            // Course added to offering
	            if (origCourseIds.indexOf(course)<0) {
	                
	                Debug.debug("Course added to offering: " + course);
	
	                CourseOffering co1 = cdao.get(course);                
	                InstructionalOffering io1 = co1.getInstructionalOffering();
	                SubjectArea sa = io1.getControllingCourseOffering().getSubjectArea();
	                Set offerings = io1.getCourseOfferings();
	                
	                // Copy course offerings
	                for (Iterator i=offerings.iterator(); i.hasNext(); ) {
	                    CourseOffering co2 = (CourseOffering) i.next();
	                    SubjectArea sa2 = co2.getSubjectArea();
	                    
	                    // Create a copy
	                    CourseOffering co3 = (CourseOffering)co2.clone();
	                    if(form.getCtrlCrsOfferingId().equals(co2.getUniqueId()))
	                        co3.setIsControl(Boolean.valueOf(true));
	                    else
	                        co3.setIsControl(Boolean.valueOf(false));
	                    
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
	                    

    	                int indx = form.getIndex(course);
    	                try {
    	                	co3.setReservation(form.getLimits(indx));
    	                } catch (NumberFormatException e) {
    	                	co3.setReservation(null);
    	                }
		                
	                    // Remove from collection
	                    //i.remove();

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
	                
	                //io1.setCourseOfferings(offerings);
	                //hibSession.saveOrUpdate(io1);
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
	        Set cfgs = io.getInstrOfferingConfigs();
	        for (Iterator iterCfg=cfgs.iterator(); iterCfg.hasNext(); ) {
	        	InstrOfferingConfig cfg = (InstrOfferingConfig) iterCfg.next();
	        	Set subparts = cfg.getSchedulingSubparts();
		        for (Iterator iterSbp=subparts.iterator(); iterSbp.hasNext(); ) {
		        	SchedulingSubpart subpart = (SchedulingSubpart) iterSbp.next();
		        	Set classes = subpart.getClasses();
			        for (Iterator iterCls=classes.iterator(); iterCls.hasNext(); ) {
			        	Class_ cls = (Class_) iterCls.next();
			        	// Only change departmental class managing dept and not externally managed
			        	if (!cls.getManagingDept().isExternalManager()) {
				        	cls.setManagingDept(dept, sessionContext.getUser(), hibSession);
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
                    sessionContext, 
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

        }
        catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw e;
        }
    }

    /**
     * Loads the form with the offering detail
     */
    private void doLoad(Long courseOfferingId) throws Exception {
        
        // Check uniqueid
        if(courseOfferingId==null)
            throw new Exception (MSG.errorUniqueIdNeeded());
        
        // Load details
        CourseOfferingDAO coDao = CourseOfferingDAO.getInstance();
        CourseOffering co = coDao.get(courseOfferingId);
        InstructionalOffering io = co.getInstructionalOffering();
        
        sessionContext.checkPermission(io, Right.InstructionalOfferingCrossLists);

        // Sort Offerings
        ArrayList<CourseOffering> offerings = new ArrayList<CourseOffering>(io.getCourseOfferings());
        Collections.sort(
                offerings, 
                new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_CTRL_CRS));
        
        // Load form properties
        form.setInstrOfferingId(io.getUniqueId());
        form.setCtrlCrsOfferingId(io.getControllingCourseOffering().getUniqueId());
        form.setReadOnlyCrsOfferingId(null);
        form.setSubjectAreaId(co.getSubjectArea().getUniqueId());
        form.setInstrOfferingName(io.getCourseNameWithTitle());
        form.setOwnedInstrOffr(true); //?? Boolean.valueOf(io.isEditableBy(user)));
        form.setIoLimit(io.getLimit());
        form.setUnlimited(io.hasUnlimitedEnrollment());
        if (io.hasUnlimitedEnrollment())
        	form.setIoLimit(-1);

        for(Iterator<CourseOffering> i = offerings.iterator(); i.hasNext(); ) {
            CourseOffering co1 = i.next();
            form.addToCourseOfferings(co1, sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent) || sessionContext.getUser().getCurrentAuthority().hasQualifier(co1.getDepartment()), sessionContext.hasPermission(co1, Right.CourseOfferingDeleteFromCrossList));
            form.addToOriginalCourseOfferings(co1);
        }
    }
    
    public String getCrsNbr() {
    	return (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
    }
    
    public boolean isModifyCrossListSingleCourseLimit() {
    	return ApplicationProperty.ModifyCrossListSingleCourseLimit.isTrue();
    }
}
