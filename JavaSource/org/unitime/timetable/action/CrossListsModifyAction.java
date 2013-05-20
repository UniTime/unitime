/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
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
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.MessageResources;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.CrossListsModifyForm;
import org.unitime.timetable.interfaces.ExternalCourseCrosslistAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingRemoveAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingInCrosslistAddAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 07-15-2005
 * 
 * XDoclet definition:
 * @struts:action path="/courseOfferingEdit" name="instructionalOfferingListForm" input="/user/instructionalOfferingSearch.jsp" scope="request"
 */
@Service("/crossListsModify")
public class CrossListsModifyAction extends Action {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;

    // --------------------------------------------------------- Instance Variables

    // --------------------------------------------------------- Methods

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        MessageResources rsc = getResources(request);
        CrossListsModifyForm frm = (CrossListsModifyForm) form;
        
        // Get operation
        String op = (request.getParameter("op")==null) 
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");		        
        			        
        if(op==null)
            op = request.getParameter("hdnOp");
        
        if(op==null || op.trim().length()==0)
            throw new Exception (MSG.errorOperationNotInterpreted() + op);

        // Course Offering Id
        String courseOfferingId = "";

        // Set up Lists
        frm.setOp(op);
        LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
			@Override
			public boolean accept(CourseOffering course) {
				return course.getInstructionalOffering().isNotOffered();
			}
		});
        
        // First access to screen
        if(op.equalsIgnoreCase(MSG.actionCrossLists())) {
            
		    courseOfferingId = (request.getParameter("uid")==null)
								? (request.getAttribute("uid")==null)
								        ? null
								        : request.getAttribute("uid").toString()
								: request.getParameter("uid");

            doLoad(frm, courseOfferingId);
        }
        
        // Add a course offering
        if(op.equalsIgnoreCase(MSG.actionAddCourseToCrossList())) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);

            if(errors.size()==0) {
                Long addedOffering = frm.getAddCourseOfferingId();
                CourseOfferingDAO cdao = new CourseOfferingDAO();
                CourseOffering co = cdao.get(addedOffering);
                
                // Check reservations limit
                frm.addToCourseOfferings(co, sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent) || sessionContext.getUser().getCurrentAuthority().hasQualifier(co.getDepartment()));
                frm.setAddCourseOfferingId(null);
            }
            else {
                saveErrors(request, errors);
            }
        }
        
        // Remove a course offering
        if(op.equalsIgnoreCase(rsc.getMessage("button.delete"))) {
            String deletedOffering = request.getParameter("deletedCourseOfferingId");
            if(deletedOffering!=null && deletedOffering.trim().length()>0)
                frm.removeFromCourseOfferings(deletedOffering);
        }
        
        // Update the course offering
        if(op.equalsIgnoreCase(MSG.actionUpdateCrossLists())) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);
            
            if(errors.size()==0) {
                doUpdate(request, frm);
                ActionRedirect redirect = new ActionRedirect(mapping.findForward("instructionalOfferingDetail"));
                redirect.addParameter("io", frm.getInstrOfferingId());
                redirect.addParameter("op", "view");
                return redirect;
            }
            else {
                saveErrors(request, errors);
            }
        }
        
        // Determine if a course offering cannot be deleted
        setReadOnlyCourseId(request, frm);
        
        // Remove the courses that are already part of this offering from list of courses
        filterCourseOfferingList(request, frm);
        
        return mapping.findForward("crossListsModify");
    }

    /**
     * Ensures that all offerings that are part of the instructional offering
     * does not appear in the drop down list 
     * @param request
     * @param frm
     */
    private void filterCourseOfferingList(HttpServletRequest request, CrossListsModifyForm frm) {
        Collection existingOfferings = frm.getCourseOfferingIds();
        Collection offerings = (Collection) request.getAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME);
        
        for (Iterator i=offerings.iterator(); i.hasNext(); ) {
            CourseOffering co = (CourseOffering) i.next();
            if ((!co.getInstructionalOffering().isNotOffered() && !co.getInstructionalOffering().getInstrOfferingConfigs().isEmpty())
            	|| co.getInstructionalOffering().getCourseOfferings().size() > 1) {
            	i.remove(); continue;
            }
            for (Iterator j=existingOfferings.iterator(); j.hasNext(); ) {
                String course = (String) j.next();
                if (course.equals(co.getUniqueId().toString()))
                    i.remove();
            }
        }
        
        request.setAttribute(CourseOffering.CRS_OFFERING_LIST_ATTR_NAME, offerings);        
    }

    /**
     * Compares the modified offering to the original offering
     * If more than one offering is in common then all offerings show the 'Delete' icon
     * If only one offering is in common that offering cannot be deleted
     * @param request
     * @param frm
     */
    private void setReadOnlyCourseId(HttpServletRequest request, CrossListsModifyForm frm) {
        short ct = 0;
        String originalOfferings = frm.getOriginalOfferings();
        List courseOfferingIds = frm.getCourseOfferingIds();
        
        for (Iterator i=courseOfferingIds.iterator(); i.hasNext(); ) {
            String cid = i.next().toString();
            if(originalOfferings.indexOf(cid)>0) {
                
                // More than one course from the original offering exists in the modified one
                if(++ct>1) {
                    frm.setReadOnlyCrsOfferingId(null);
                    break;
                }
                else {
                    frm.setReadOnlyCrsOfferingId(new Long(cid));
                }                    
            }
        }        

        Debug.debug("Read Only Ctr Course: " + frm.getReadOnlyCrsOfferingId());
    }

    /**
     * Update the instructional offering
     * @param request
     * @param frm
     */
    private void doUpdate(HttpServletRequest request, CrossListsModifyForm frm) 
    	throws Exception {
        
        // Get the modified offering
        List ids = frm.getCourseOfferingIds();
        String courseIds = Constants.arrayToStr(ids.toArray(), "", " ");
        String origCourseIds = frm.getOriginalOfferings();
        
        // Get Offering
        CourseOfferingDAO cdao = new CourseOfferingDAO();
        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
        Session hibSession = idao.getSession();
        hibSession.setFlushMode(FlushMode.MANUAL);
        Transaction tx = null;
        HashMap saList = new HashMap();
        List<CurriculumCourse> cc = new ArrayList<CurriculumCourse>();
        
        try {
	        tx = hibSession.beginTransaction();
	        StringTokenizer strTok = new StringTokenizer(origCourseIds);
	        
	        while (strTok.hasMoreTokens()) {
	            
	            String origCrs = strTok.nextToken();
	            
		        // 1. For all deleted courses - create new offering and make 'not offered'
	            if (courseIds.indexOf(origCrs)<0) {
	                Debug.debug("Course removed from offering: " + origCrs);
	                
	                // Create new instructional offering 
	                InstructionalOffering io1 = new InstructionalOffering();
	                CourseOffering co1 = cdao.get(new Long(origCrs.trim()));
	                
	                // Copy attributes of old instr offering - make not offered
	                io1.setDemand(io.getDemand());
	                io1.setLimit(io.getLimit());
	                io1.setNotOffered(new Boolean(true));
	                io1.setSession(io.getSession());
	                io1.setByReservationOnly(io.getByReservationOnly());
	                
	                // Copy attributes of old crs offering - set controlling	                
                    CourseOffering co2 = (CourseOffering)co1.clone();                    
                    co2.setIsControl(new Boolean(true));
                    
                    for (CurriculumCourse x: (List<CurriculumCourse>)hibSession.createQuery(
                    		"from CurriculumCourse where course.uniqueId = :courseId")
                    		.setLong("courseId", co1.getUniqueId()).list()) {
                    	cc.add(x.clone(co2));
                    	x.getClassification().getCourses().remove(x);
                    	hibSession.delete(x);
                    }
                    
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
			                hibSession.saveOrUpdate(sa);
			                saList.put(sa.getSubjectAreaAbbreviation(), sa);
			            }
			        }
			        
			        // Delete old course offering
			        io.removeCourseOffering(co1);
			        
                    Event.deleteFromEvents(hibSession, co1);
		            Exam.deleteFromExams(hibSession, co1);
		            
                	String className = ApplicationProperties.getProperty("tmtbl.external.course_offering.remove_action.class");
                	if (className != null && className.trim().length() > 0){
                		ExternalCourseOfferingRemoveAction removeAction = (ExternalCourseOfferingRemoveAction) (Class.forName(className).newInstance());
        	       		removeAction.performExternalCourseOfferingRemoveAction(co1, hibSession);
                	}
			        hibSession.delete(co1);
			        
			        //io.setCourseOfferings(offerings);
			        
		            hibSession.saveOrUpdate(io);
		            hibSession.flush();
	                
	                // Add course to instructional offering
	                co2.setInstructionalOffering(io1);
	                io1.addTocourseOfferings(co2);

	                // Update
                    if (io1.getInstrOfferingPermId()==null) io1.generateInstrOfferingPermId();
	                hibSession.saveOrUpdate(io1);
	                hibSession.flush();

	    	        hibSession.refresh(io);
	                hibSession.refresh(io1);
	            	className = ApplicationProperties.getProperty("tmtbl.external.instr_offr_in_crosslist.add_action.class");
	            	if (className != null && className.trim().length() > 0){
	            		ExternalInstructionalOfferingInCrosslistAddAction addAction = (ExternalInstructionalOfferingInCrosslistAddAction) (Class.forName(className).newInstance());
	    	       		addAction.performExternalInstructionalOfferingInCrosslistAddAction(io1, hibSession);
	            	}
	            }
	            
	            // 2. For all existing courses - update controlling attribute and reservation limits
	            else {
	                Debug.debug("Updating controlling course  and course reservation: " + origCrs);	                

	                // Update controlling course attribute
	                CourseOffering co = cdao.get(new Long (origCrs));
	                if(frm.getCtrlCrsOfferingId().equals(co.getUniqueId()))
	                    co.setIsControl(new Boolean(true));
	                else
	                    co.setIsControl(new Boolean(false));
	                
	                // Update course reservation
	                int indx = frm.getIndex(origCrs);
	                try {
	                	co.setReservation(ids.size() > 1 ? Integer.valueOf(frm.getLimits(indx)) : null);
	                } catch (NumberFormatException e) {
	                	co.setReservation(null);
	                }

	                hibSession.saveOrUpdate(co);

	                hibSession.flush();
	                hibSession.refresh(co);
	            }
	        }
	        
	        // 3. For all added courses - delete all preferences and change the instr offering id  
	        Vector addedOfferings = new Vector();
	        StringTokenizer strTok2 = new StringTokenizer(courseIds);
	
	        while (strTok2.hasMoreTokens()) {
	            String course = strTok2.nextToken();
	            
	            // Course added to offering
	            if (origCourseIds.indexOf(course)<0) {
	                
	                Debug.debug("Course added to offering: " + course);
	
	                CourseOffering co1 = cdao.get(new Long(course.trim()));                
	                InstructionalOffering io1 = co1.getInstructionalOffering();
	                SubjectArea sa = io1.getControllingCourseOffering().getSubjectArea();
	                Set offerings = io1.getCourseOfferings();
	                
	                // Copy course offerings
	                for (Iterator i=offerings.iterator(); i.hasNext(); ) {
	                    CourseOffering co2 = (CourseOffering) i.next();
	                    SubjectArea sa2 = co2.getSubjectArea();
	                    
	                    // Create a copy
	                    CourseOffering co3 = (CourseOffering)co2.clone();
	                    if(frm.getCtrlCrsOfferingId().equals(co2.getUniqueId()))
	                        co3.setIsControl(new Boolean(true));
	                    else
	                        co3.setIsControl(new Boolean(false));
	                    
	                    for (CurriculumCourse x: (List<CurriculumCourse>)hibSession.createQuery(
	                    		"from CurriculumCourse where course.uniqueId = :courseId")
	                    		.setLong("courseId", co2.getUniqueId()).list()) {
	                    	cc.add(x.clone(co3));
	                    	x.getClassification().getCourses().remove(x);
	                    	hibSession.delete(x);
	                    }

	                    addedOfferings.addElement(co3);

    	                int indx = frm.getIndex(course);
    	                try {
    	                	co3.setReservation(Integer.valueOf(frm.getLimits(indx)));
    	                } catch (NumberFormatException e) {
    	                	co3.setReservation(null);
    	                }
		                
	                    // Remove from collection
	                    //i.remove();

                        sa2.getCourseOfferings().remove(co2);
	                    hibSession.saveOrUpdate(sa2);
		                saList.put(sa2.getSubjectAreaAbbreviation(), sa2);
                        
	                    // Delete course offering
                        io1.removeCourseOffering(co2);
                        Event.deleteFromEvents(hibSession, co2);
                        Exam.deleteFromExams(hibSession, co2);
                    	String className = ApplicationProperties.getProperty("tmtbl.external.course_offering.remove_action.class");
                    	if (className != null && className.trim().length() > 0){
                    		ExternalCourseOfferingRemoveAction removeAction = (ExternalCourseOfferingRemoveAction) (Class.forName(className).newInstance());
            	       		removeAction.performExternalCourseOfferingRemoveAction(co2, hibSession);
                    	}

	                    hibSession.delete(co2);
	                    hibSession.flush();

	                    //hibSession.refresh(sa2);
	                    
	                }
	                
	                //io1.setCourseOfferings(offerings);
	                //hibSession.saveOrUpdate(io1);
	                Event.deleteFromEvents(hibSession, io1);
	                Exam.deleteFromExams(hibSession, io1);

	                hibSession.delete(io1);
	                hibSession.flush();
	                
	                hibSession.saveOrUpdate(sa);
	                saList.put(sa.getSubjectAreaAbbreviation(), sa);

	    	        //hibSession.refresh(sa);
	                
	            }            
	        }
	
	        hibSession.flush();
	        
	        // Update Offering - Added Offerings       
	        for (int i=0; i<addedOfferings.size(); i++) {
	            CourseOffering co3 = (CourseOffering) addedOfferings.elementAt(i);
	            co3.setInstructionalOffering(io);
	            io.addTocourseOfferings(co3);
	            hibSession.saveOrUpdate(co3);
	            
	            hibSession.flush();
	            hibSession.refresh(co3);

	            hibSession.saveOrUpdate(io);
	        }
	        for (CurriculumCourse x: cc)
	        	hibSession.saveOrUpdate(x);
            
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
				        	cls.setManagingDept(dept);
				        	hibSession.saveOrUpdate(cls);
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
	        hibSession.refresh(io);
	        
	        // Refresh objects
	        for (Iterator i1=io.getInstrOfferingConfigs().iterator();i1.hasNext();) {
	        	InstrOfferingConfig cfg = (InstrOfferingConfig)i1.next();
	        	for (Iterator i2=cfg.getSchedulingSubparts().iterator();i2.hasNext();) {
	        		SchedulingSubpart ss = (SchedulingSubpart)i2.next();
	        		for (Iterator i3=ss.getClasses().iterator();i3.hasNext();) {
	        			Class_ c = (Class_)i3.next();
	        			hibSession.refresh(c);
	        		}
	        		hibSession.refresh(ss);
	        	}
	        }
	        
	        Set keys = saList.keySet();
	        for (Iterator i1=keys.iterator(); i1.hasNext();) {
	        	hibSession.refresh(saList.get(i1.next()));
	        }	
        	String className = ApplicationProperties.getProperty("tmtbl.external.instr_offr.crosslist_action.class");
        	if (className != null && className.trim().length() > 0){
	        	ExternalCourseCrosslistAction addAction = (ExternalCourseCrosslistAction) (Class.forName(className).newInstance());
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
     * @param frm Form object
     * @param courseOfferingId Course Offering Id of controlling course
     * @param user User object
     */
    private void doLoad(
            CrossListsModifyForm frm, 
            String courseOfferingId) throws Exception {
        
        // Check uniqueid
        if(courseOfferingId==null || courseOfferingId.trim().length()==0)
            throw new Exception (MSG.errorUniqueIdNeeded());
        
        // Load details
        CourseOfferingDAO coDao = new CourseOfferingDAO();
        CourseOffering co = coDao.get(Long.valueOf(courseOfferingId));
        InstructionalOffering io = co.getInstructionalOffering();
        
        sessionContext.checkPermission(io, Right.InstructionalOfferingCrossLists);

        // Sort Offerings
        ArrayList offerings = new ArrayList(io.getCourseOfferings());
        Collections.sort(
                offerings, 
                new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_CTRL_CRS));
        
        // Load form properties
        frm.setInstrOfferingId(io.getUniqueId());
        frm.setCtrlCrsOfferingId(io.getControllingCourseOffering().getUniqueId());
        frm.setReadOnlyCrsOfferingId(null);
        frm.setSubjectAreaId(co.getSubjectArea().getUniqueId());
        frm.setInstrOfferingName(io.getCourseNameWithTitle());
        frm.setOwnedInstrOffr(true); //?? new Boolean(io.isEditableBy(user)));
        frm.setIoLimit(io.getLimit());
        frm.setUnlimited(io.hasUnlimitedEnrollment());

        for(Iterator i = offerings.iterator(); i.hasNext(); ) {
            CourseOffering co1 = ((CourseOffering) i.next());
            frm.addToCourseOfferings(co1, sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent) || sessionContext.getUser().getCurrentAuthority().hasQualifier(co1.getDepartment()));
            frm.addToOriginalCourseOfferings(co1);
        }
    }
}
