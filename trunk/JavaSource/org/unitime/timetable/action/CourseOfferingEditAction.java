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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.hibernate.Transaction;
import org.hibernate.impl.SessionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.CourseOfferingEditForm;
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.InstrOfferingPermIdGenerator;
import org.unitime.timetable.util.LookupTables;


/**
 * MyEclipse Struts
 * Creation date: 07-25-2006
 *
 * XDoclet definition:
 * @struts:action path="/courseOfferingEdit" name="courseOfferingEditForm" input="/user/courseOfferingEdit.jsp" scope="request"
 */
@Service("/courseOfferingEdit")
public class CourseOfferingEditAction extends Action {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

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

        ActionMessages errors = new ActionMessages();
        CourseOfferingEditForm frm = (CourseOfferingEditForm) form;

        // Read Parameters
        String op = (request.getParameter("op")==null)
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");
		if (op==null)
		    op = request.getParameter("hdnOp");

		// Check operation
		if(op==null || op.trim().length()==0)
		    throw new Exception (MSG.errorOperationNotInterpreted() + op);

		Debug.debug ("Op: " + op);

		if(op.equals(MSG.actionEditCourseOffering()) ) {

		    String courseOfferingId = (request.getParameter("courseOfferingId")==null)
		    							? (request.getAttribute("courseOfferingId")==null)
		    							        ? null
		    							        : request.getAttribute("courseOfferingId").toString()
		    							: request.getParameter("courseOfferingId");

		    if (courseOfferingId==null && frm.getCourseOfferingId()!=null) {
		        courseOfferingId=frm.getCourseOfferingId().toString();
		    }

			if(courseOfferingId==null || courseOfferingId.trim().length()==0) {
			    throw new Exception (MSG.errorCourseDataNotCorrect() + courseOfferingId);
			}
			else  {
			    doLoad(request, frm, courseOfferingId);
			}

			return mapping.findForward("displayCourseOffering");
		}

		if(op.equals(MSG.actionUpdateCourseOffering()) ) {
			errors = frm.validate(mapping, request);
			if (errors.size() == 0) {
			    doUpdate(request, frm);

			    String cn = (String) sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
				if (cn!=null)
					sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, frm.getCourseNbr());

			    // Redirect to instr offering detail on success
                ActionRedirect redirect = new ActionRedirect(mapping.findForward("instructionalOfferingDetail"));
                redirect.addParameter("io", frm.getInstrOfferingId());
                redirect.addParameter("op", "view");
                return redirect;
			}
			else {
				saveErrors(request, errors);
			    doReload(request, frm);
			}
		}
		
		if (op.equals(MSG.actionAddCoordinator()) ) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++)
                frm.getInstructors().add(Preference.BLANK_PREF_VALUE);
            doReload(request, frm);
		}
		
        if (op.equals(MSG.actionRemoveCoordinator()) && request.getParameter("deleteType")!=null && request.getParameter("deleteType").equals("coordinator")) {
            try {
                int deleteId = Integer.parseInt(request.getParameter("deleteId"));
                if (deleteId>=0)
                    frm.getInstructors().remove(deleteId);
            } catch (Exception e) {}
            doReload(request, frm);
        }
            
        return mapping.findForward("displayCourseOffering");
    }

    /**
     * @param request
     * @param frm
     */
    private void doUpdate(HttpServletRequest request, CourseOfferingEditForm frm) throws Exception {
    	boolean limitedEdit = false, updateNote = false, updateCoordinators = false; 
    	
    	if (sessionContext.hasPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingNote)) {
    		updateNote = true;
    	}
    	if (sessionContext.hasPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingCoordinators)) {
    		updateCoordinators = true;
    	}
    	if (updateNote || updateCoordinators) {
    		limitedEdit = !sessionContext.hasPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering);
    	} else {
    		sessionContext.checkPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering);
    	}

        String title = frm.getTitle();
        String note = frm.getScheduleBookNote();
        Long crsId = frm.getCourseOfferingId();
        String crsNbr = frm.getCourseNbr();

		org.hibernate.Session hibSession = null;
        Transaction tx = null;

        try {
            OfferingConsentTypeDAO odao = new OfferingConsentTypeDAO();
	        CourseOfferingDAO cdao = new CourseOfferingDAO();
            hibSession = cdao.getSession();
            tx = hibSession.beginTransaction();

	        CourseOffering co = cdao.get(crsId);
	        InstructionalOffering io = co.getInstructionalOffering();
	        
	        if (!limitedEdit || updateNote)
	        	co.setScheduleBookNote(note);

	        if (!limitedEdit || updateCoordinators || co.isIsControl().booleanValue()) {
		        if (io.getCoordinators() == null) io.setCoordinators(new HashSet<DepartmentalInstructor>());
		        for (Iterator<DepartmentalInstructor> i = io.getCoordinators().iterator(); i.hasNext(); ) {
		            DepartmentalInstructor instructor = i.next();
		            instructor.getOfferings().remove(io);
		            i.remove();
		        }
		        for (Iterator i=frm.getInstructors().iterator();i.hasNext();) {
		            String instructorId = (String)i.next();
		            if (!Constants.BLANK_OPTION_VALUE.equals(instructorId) && !Preference.BLANK_PREF_VALUE.equals(instructorId)) {
		                DepartmentalInstructor instructor = new DepartmentalInstructorDAO().get(Long.valueOf(instructorId));
		                if (instructor!=null) {
		                    io.getCoordinators().add(instructor);
		                    instructor.getOfferings().add(io);
		                }
		           }
		        }

		        if (limitedEdit)
		        	hibSession.update(io);
	        }

	        if (!limitedEdit) {
		        if (co.getCourseNbr() != null && !co.getCourseNbr().equals(crsNbr) && co.getPermId() == null){
		        	LastLikeCourseDemand llcd = null;
		        	String permId = InstrOfferingPermIdGenerator.getGenerator().generate((SessionImpl)new CourseOfferingDAO().getSession(), co).toString();
		        	for(Iterator it = co.getCourseOfferingDemands().iterator(); it.hasNext();){
		        		llcd = (LastLikeCourseDemand)it.next();
		        		if (llcd.getCoursePermId() == null){
			        		llcd.setCoursePermId(permId);
			        		hibSession.update(llcd);
		        		}
		        	}
	        		co.setPermId(permId);
		        }
		        co.setCourseNbr(crsNbr);
		        co.setTitle(title);

		        if (frm.getDemandCourseOfferingId()==null) {
		        	co.setDemandOffering(null);
		        } else {
		        	CourseOffering dco = cdao.get(frm.getDemandCourseOfferingId(),hibSession);
		        	co.setDemandOffering(dco==null?null:dco);
		        }
		        
		        if (frm.getCourseTypeId() == null || frm.getCourseTypeId().isEmpty()) {
		        	co.setCourseType(null);
		        } else {
		        	co.setCourseType(CourseTypeDAO.getInstance().get(Long.valueOf(frm.getCourseTypeId()), hibSession));
		        }

		        if (frm.getConsent()==null || frm.getConsent().intValue()<=0)
		            co.setConsentType(null);
		        else {
		            OfferingConsentType oct = odao.get(frm.getConsent());
		            co.setConsentType(oct);
		        }

		        // Update consent only if course is controlling
		        if (co.isIsControl().booleanValue()) {
			        if (frm.getCreditFormat() == null || frm.getCreditFormat().length() == 0 || frm.getCreditFormat().equals(Constants.BLANK_OPTION_VALUE)){
			        	CourseCreditUnitConfig origConfig = io.getCredit();
			        	if (origConfig != null){
							io.setCredit(null);
							hibSession.delete(origConfig);
			        	}
			        } else {
			         	if(io.getCredit() != null){
			        		CourseCreditUnitConfig ccuc = io.getCredit();
			        		if (ccuc.getCreditFormat().equals(frm.getCreditFormat())){
			        			boolean changed = false;
			        			if (!ccuc.getCreditType().getUniqueId().equals(frm.getCreditType())){
			        				changed = true;
			        			}
			        			if (!ccuc.getCreditUnitType().getUniqueId().equals(frm.getCreditUnitType())){
			        				changed = true;
			        			}
			        			if (ccuc instanceof FixedCreditUnitConfig) {
									FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) ccuc;
									if (!fcuc.getFixedUnits().equals(frm.getUnits())){
										changed = true;
									}
								} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
									VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) ccuc;
									if (!vfcuc.getMinUnits().equals(frm.getUnits())){
										changed = true;
									}
									if (!vfcuc.getMaxUnits().equals(frm.getMaxUnits())){
										changed = true;
									}
									if (vfcuc instanceof VariableRangeCreditUnitConfig) {
										VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) vfcuc;
										if (!vrcuc.isFractionalIncrementsAllowed().equals(frm.getFractionalIncrementsAllowed())){
											changed = true;
										}
									}
								}
			        			if (changed){
			        				CourseCreditUnitConfig origConfig = io.getCredit();
			            			io.setCredit(null);
			            			hibSession.delete(origConfig);
			            			io.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(true)));
			            			io.getCredit().setOwner(io);
			        			}
			        		} else {
			        			CourseCreditUnitConfig origConfig = io.getCredit();
			        			io.setCredit(null);
			        			hibSession.delete(origConfig);
			        			io.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(true)));
			        			io.getCredit().setOwner(io);
			        		}
			        	} else {
			    			io.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(true)));
			    			io.getCredit().setOwner(io);
			        	}
			        }

			        if (io.getCredit() != null){
			        	hibSession.saveOrUpdate(io.getCredit());
			        }

			        io.setByReservationOnly(frm.isByReservationOnly());
			        try {
			        	io.setLastWeekToEnroll(Integer.parseInt(frm.getWkEnroll()));
			        } catch (Exception e) {
			        	io.setLastWeekToEnroll(null);
			        }
			        try {
				        io.setLastWeekToChange(Integer.parseInt(frm.getWkChange()));
			        } catch (Exception e) {
				        io.setLastWeekToChange(null);
			        }
			        try{
				        io.setLastWeekToDrop(Integer.parseInt(frm.getWkDrop()));
			        } catch (Exception e) {
			        	io.setLastWeekToDrop(null);
			        }

			        hibSession.update(io);
		        }
	        }

	        hibSession.saveOrUpdate(co);

            ChangeLog.addChange(
                    hibSession,
                    sessionContext,
                    co,
                    ChangeLog.Source.COURSE_OFFERING_EDIT,
                    ChangeLog.Operation.UPDATE,
                    co.getSubjectArea(),
                    co.getDepartment());
            
        	if (limitedEdit && permissionOfferingLockNeeded.check(sessionContext.getUser(), io))
        		StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), io.getSessionId(), io.getUniqueId());        		

            hibSession.flush();
            tx.commit();

            hibSession.refresh(co);

            hibSession.refresh(io);
            
        	String className = ApplicationProperties.getProperty("tmtbl.external.course_offering.edit_action.class");
        	if (className != null && className.trim().length() > 0){
        		if (io == null){
        			io = co.getInstructionalOffering();
        		}
	        	ExternalCourseOfferingEditAction editAction = (ExternalCourseOfferingEditAction) (Class.forName(className).newInstance());
	       		editAction.performExternalCourseOfferingEditAction(io, hibSession);
        	}
        }
        catch (Exception e) {
            try {
	            if (tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }

			Debug.error(e);
            throw (e);
        }

    }

    /**
     * @param request
     * @param frm
     * @param courseOfferingId
     */
    private void doLoad(
            HttpServletRequest request,
            CourseOfferingEditForm frm,
            String crsOfferingId) throws Exception {
    	
    	if (!sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOfferingNote) &&
    		!sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOfferingCoordinators))
    		sessionContext.checkPermission(crsOfferingId, "CourseOffering", Right.EditCourseOffering);

        // Load Course Offering
        Long courseOfferingId = new Long(crsOfferingId);

        CourseOfferingDAO cdao = new CourseOfferingDAO();
        CourseOffering co = cdao.get(courseOfferingId);

        InstructionalOffering io = co.getInstructionalOffering();
        Long subjectAreaId = co.getSubjectArea().getUniqueId();//io.getControllingCourseOffering().getSubjectArea().getUniqueId();

        frm.setDemandCourseOfferingId(co.getDemandOffering()==null?null:co.getDemandOffering().getUniqueId());
        frm.setAllowDemandCourseOfferings(true);//co.getLastLikeSemesterCourseOfferingDemands().isEmpty());
        frm.setCourseName(co.getCourseName());
        frm.setCourseNbr(co.getCourseNbr());
        frm.setCourseOfferingId(courseOfferingId);
        frm.setInstrOfferingId(io.getUniqueId());
        frm.setScheduleBookNote(co.getScheduleBookNote());
        frm.setSubjectAreaId(subjectAreaId);
        frm.setTitle(co.getTitle());
        frm.setIsControl(co.getIsControl());
        frm.setIoNotOffered(io.getNotOffered());
        frm.setByReservationOnly(io.isByReservationOnly());
        frm.setWkEnroll(io.getLastWeekToEnroll() == null ? "" : io.getLastWeekToEnroll().toString());
        frm.setWkEnrollDefault(io.getSession().getLastWeekToEnroll());
        frm.setWkChange(io.getLastWeekToChange() == null ? "" : io.getLastWeekToChange().toString());
        frm.setWkChangeDefault(io.getSession().getLastWeekToChange());
        frm.setWkDrop(io.getLastWeekToDrop() == null ? "" : io.getLastWeekToDrop().toString());
        frm.setWkDropDefault(io.getSession().getLastWeekToDrop());
        frm.setWeekStartDayOfWeek(Localization.getDateFormat("EEEE").format(io.getSession().getSessionBeginDateTime()));
        frm.setCourseTypeId(co.getCourseType() == null ? "" : co.getCourseType().getUniqueId().toString());

        if (co.getConsentType()!=null)
            frm.setConsent(co.getConsentType().getUniqueId());
        else
            frm.setConsent(new Long(-1));
        LookupTables.setupConsentType(request);

        for (DepartmentalInstructor instructor: new TreeSet<DepartmentalInstructor>(io.getCoordinators()))
            frm.getInstructors().add(instructor.getUniqueId().toString());

        if (sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOfferingCoordinators) ||
        	sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOffering))
        	for (int i=0;i<Constants.PREF_ROWS_ADDED;i++)
        		frm.getInstructors().add(Constants.BLANK_OPTION_VALUE);
        
        // Consent Type and Credit can be edited only on the controlling course offering
        if (co.isIsControl().booleanValue()) {

            if (frm.getCreditFormat() == null){
    	        if (io.getCredit() != null){
    	        	CourseCreditUnitConfig credit = io.getCredit();
    	        	frm.setCreditText(credit.creditText());
    	        	frm.setCreditFormat(credit.getCreditFormat());
    	        	frm.setCreditType(credit.getCreditType().getUniqueId());
    	        	frm.setCreditUnitType(credit.getCreditUnitType().getUniqueId());
    	        	if (credit instanceof FixedCreditUnitConfig){
    	        		frm.setUnits(((FixedCreditUnitConfig) credit).getFixedUnits());
    	        	} else if (credit instanceof VariableFixedCreditUnitConfig){
    	        		frm.setUnits(((VariableFixedCreditUnitConfig) credit).getMinUnits());
    	        		frm.setMaxUnits(((VariableFixedCreditUnitConfig) credit).getMaxUnits());
    	        		if (credit instanceof VariableRangeCreditUnitConfig){
    	        			frm.setFractionalIncrementsAllowed(((VariableRangeCreditUnitConfig) credit).isFractionalIncrementsAllowed());
    	        		}
    	        	}
    	        }
            }
            LookupTables.setupCourseCreditFormats(request); // Course Credit Formats
            LookupTables.setupCourseCreditTypes(request); //Course Credit Types
            LookupTables.setupCourseCreditUnitTypes(request); //Course Credit Unit Types

            // Catalog Link
            String linkLookupClass = ApplicationProperties.getProperty("tmtbl.catalogLink.lookup.class");
            if (linkLookupClass!=null && linkLookupClass.trim().length()>0) {
            	ExternalLinkLookup lookup = (ExternalLinkLookup) (Class.forName(linkLookupClass).newInstance());
           		Map results = lookup.getLink(io);
                if (results==null)
                    throw new Exception (lookup.getErrorMessage());
                
                frm.setCatalogLinkLabel((String)results.get(ExternalLinkLookup.LINK_LABEL));
                frm.setCatalogLinkLocation((String)results.get(ExternalLinkLookup.LINK_LOCATION));
            }

            // Setup instructors
            Set<Long> deptIds = new HashSet<Long>();
            
            for (DepartmentalInstructor instructor: co.getInstructionalOffering().getCoordinators())
                deptIds.add(instructor.getDepartment().getUniqueId());

            for (CourseOffering x: co.getInstructionalOffering().getCourseOfferings())
            	deptIds.add(x.getSubjectArea().getDepartment().getUniqueId());

            Long[] deptsIdsArray = new Long[deptIds.size()]; int idx = 0;
            for (Long departmentId: deptIds)
                deptsIdsArray[idx++] = departmentId;

            LookupTables.setupInstructors(request, sessionContext, deptsIdsArray);
        }

        LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
			@Override
			public boolean accept(CourseOffering course) {
				return course.getDemand() != null && course.getDemand() > 0;
			}
		});
        
        LookupTables.setupCourseTypes(request);
    }

    /**
     * @param request
     * @param frm
     * @param courseOfferingId
     */
    private void doReload(
            HttpServletRequest request,
            CourseOfferingEditForm frm) throws Exception {

    	if (!sessionContext.hasPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingNote) &&
        	!sessionContext.hasPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingCoordinators))
    		sessionContext.checkPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering);

    	frm.setAllowDemandCourseOfferings(true);

        // Consent Type and Credit can be edited only on the controlling course offering
        if (frm.getIsControl().booleanValue()) {
            LookupTables.setupConsentType(request);
            LookupTables.setupCourseCreditFormats(request); // Course Credit Formats
            LookupTables.setupCourseCreditTypes(request); //Course Credit Types
            LookupTables.setupCourseCreditUnitTypes(request); //Course Credit Unit Types
        }

        CourseOffering co = new CourseOfferingDAO().get(frm.getCourseOfferingId());
        LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
			@Override
			public boolean accept(CourseOffering course) {
				return course.getDemand() != null && course.getDemand() > 0;
			}
		});
        
        if (co.isIsControl()) {
            // Setup instructors
            Set<Long> deptIds = new HashSet<Long>();
            
            for (DepartmentalInstructor instructor: co.getInstructionalOffering().getCoordinators())
                deptIds.add(instructor.getDepartment().getUniqueId());

            for (CourseOffering x: co.getInstructionalOffering().getCourseOfferings())
            	deptIds.add(x.getSubjectArea().getDepartment().getUniqueId());

            Long[] deptsIdsArray = new Long[deptIds.size()]; int idx = 0;
            for (Long departmentId: deptIds)
                deptsIdsArray[idx++] = departmentId;

            LookupTables.setupInstructors(request, sessionContext, deptsIdsArray);        	
        }
    }
    
}
