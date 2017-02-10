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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.SecurityMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.CourseOfferingEditForm;
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingAddAction;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
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
 *
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
@Service("/courseOfferingEdit")
public class CourseOfferingEditAction extends Action {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static SecurityMessages SEC = Localization.create(SecurityMessages.class);
	
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
		if (op==null || op.trim().length()==0) {
			op = "reload";
		}

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

			if (courseOfferingId==null || courseOfferingId.trim().isEmpty()) {
				throw new Exception (MSG.errorCourseDataNotCorrect() + courseOfferingId);
			} else  {
			    doLoad(request, frm, courseOfferingId);
			}

			return mapping.findForward("edit");
		}
		
		if (op.equals(MSG.actionAddCourseOffering())) {
			frm.setSubjectAreaId(request.getParameter("subjAreaId") == null ? null : Long.valueOf(request.getParameter("subjAreaId")));
			frm.setCourseNbr(request.getParameter("courseNbr"));
			TreeSet<SubjectArea> subjects = SubjectArea.getUserSubjectAreas(sessionContext.getUser());
			if (frm.getSubjectAreaId() == null && !subjects.isEmpty())
				frm.setSubjectAreaId(subjects.first().getUniqueId());
			frm.setIsControl(true);
			frm.setAllowDemandCourseOfferings(true);
			for (int i=0; i<Constants.PREF_ROWS_ADDED; i++)
                frm.getInstructors().add(Preference.BLANK_PREF_VALUE);
			frm.setAdd(true);
			Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
	        frm.setWkEnrollDefault(session.getLastWeekToEnroll());
	        frm.setWkChangeDefault(session.getLastWeekToChange());
	        frm.setWkDropDefault(session.getLastWeekToDrop());
	        frm.setWeekStartDayOfWeek(Localization.getDateFormat("EEEE").format(session.getSessionBeginDateTime()));
	        frm.setAllowAlternativeCourseOfferings(ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue());
			doReload(request, frm);
		}

		if (op.equals(MSG.actionUpdateCourseOffering()) || op.equals(MSG.actionSaveCourseOffering())) {
			errors = frm.validate(mapping, request);
			if (errors.size() == 0) {
				if (frm.isAdd())
					doSave(request, frm);
				else
					doUpdate(request, frm);

			    String cn = (String) sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
				if (cn!=null)
					sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, frm.getCourseNbr());

			    // Redirect to instr offering detail on success
                ActionRedirect redirect = new ActionRedirect(mapping.findForward("instructionalOfferingDetail"));
                redirect.addParameter("io", frm.getInstrOfferingId());
                redirect.addParameter("op", "view");
                return redirect;
			} else {
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
                if (deleteId>=0) {
                    frm.getInstructors().remove(deleteId);
                    frm.getResponsibilities().remove(deleteId);
                    frm.getPercentShares().remove(deleteId);
                }
            } catch (Exception e) {}
            doReload(request, frm);
        }
        
        if (op.equals("reload")) {
        	doReload(request, frm);
        }
        
        return mapping.findForward(frm.isAdd() ? "add" : "edit");
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

	        if ((!limitedEdit || updateCoordinators) && co.isIsControl().booleanValue()) {
		        boolean assignTeachingRequest = Department.isInstructorSchedulingCommitted(co.getDepartment().getUniqueId());
		        if (io.getOfferingCoordinators() == null) io.setOfferingCoordinators(new HashSet<OfferingCoordinator>());
		        List<OfferingCoordinator> coordinators = new ArrayList<OfferingCoordinator>(io.getOfferingCoordinators());
		        int idx = 0;
		        for (Iterator i = frm.getInstructors().iterator();i.hasNext();) {
		            String instructorId = (String)i.next();
		            String responsibilityId = frm.getResponsibilities(idx);
		            String percShare = frm.getPercentShares(idx);
		            idx++;
		            if (!Constants.BLANK_OPTION_VALUE.equals(instructorId) && !Preference.BLANK_PREF_VALUE.equals(instructorId)) {
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
		        }
		        for (OfferingCoordinator coordinator: coordinators) {
		        	coordinator.getInstructor().getOfferingCoordinators().remove(coordinator);
		        	io.getOfferingCoordinators().remove(coordinator);
		        	hibSession.delete(coordinator);
		        }
		        if (limitedEdit)
		        	hibSession.update(io);
	        }

	        if (!limitedEdit) {
		        if (co.getCourseNbr() != null && !co.getCourseNbr().equals(crsNbr) && co.getPermId() == null){
		        	LastLikeCourseDemand llcd = null;
		        	String permId = InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)new CourseOfferingDAO().getSession(), co).toString();
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
		        
		        if (ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue()) {
			        if (frm.getAlternativeCourseOfferingId() == null)
			        	co.setAlternativeOffering(null);
			        else {
			        	co.setAlternativeOffering(CourseOfferingDAO.getInstance().get(frm.getAlternativeCourseOfferingId(),hibSession));
			        }	
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

		        // Update credit
		        if (frm.getCreditFormat() == null || frm.getCreditFormat().length() == 0 || frm.getCreditFormat().equals(Constants.BLANK_OPTION_VALUE)){
		        	CourseCreditUnitConfig origConfig = co.getCredit();
		        	if (origConfig != null){
						co.setCredit(null);
						hibSession.delete(origConfig);
		        	}
		        } else {
		         	if(co.getCredit() != null){
		        		CourseCreditUnitConfig ccuc = co.getCredit();
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
									if (vrcuc.isFractionalIncrementsAllowed() == null || !vrcuc.isFractionalIncrementsAllowed().equals(frm.getFractionalIncrementsAllowed())){
										changed = true;
									}
								}
							}
		        			if (changed){
		        				CourseCreditUnitConfig origConfig = co.getCredit();
		            			co.setCredit(null);
		            			hibSession.delete(origConfig);
		            			co.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(true)));
		            			co.getCredit().setOwner(co);
		        			}
		        		} else {
		        			CourseCreditUnitConfig origConfig = co.getCredit();
		        			co.setCredit(null);
		        			hibSession.delete(origConfig);
		        			co.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(true)));
		        			co.getCredit().setOwner(co);
		        		}
		        	} else {
		    			co.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(true)));
		    			co.getCredit().setOwner(co);
		        	}

			        if (co.getCredit() != null){
			        	hibSession.saveOrUpdate(co.getCredit());
			        }
		        }
		        
		        if (co.isIsControl()) {
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
		        
		        if (ApplicationProperty.CourseOfferingEditExternalIds.isTrue())
		        	co.setExternalUniqueId(frm.getExternalId() == null || frm.getExternalId().isEmpty() ? null : frm.getExternalId());
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
            
        	String className = ApplicationProperty.ExternalActionCourseOfferingEdit.value();
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
    
    private void doSave(HttpServletRequest request, CourseOfferingEditForm frm) throws Exception {
    	sessionContext.checkPermission(frm.getSubjectAreaId(), "SubjectArea", Right.AddCourseOffering);
    	
		org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
        Transaction tx = null;

        try {
            tx = hibSession.beginTransaction();
            
            SubjectArea subjArea = SubjectAreaDAO.getInstance().get(frm.getSubjectAreaId(), hibSession);
            
            CourseOffering co = new CourseOffering();
            
            co.setSubjectArea(subjArea);
            co.setSubjectAreaAbbv(subjArea.getSubjectAreaAbbreviation());
		    co.setCourseNbr(frm.getCourseNbr());
		    co.setProjectedDemand(new Integer(0));
            co.setDemand(new Integer(0));
		    co.setNbrExpectedStudents(new Integer(0));
		    co.setIsControl(new Boolean(true));
		    co.setPermId(InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)new CourseOfferingDAO().getSession(), co).toString());
		    subjArea.getCourseOfferings().add(co);

	        // Add new Instructional Offering
		    InstructionalOffering io = new InstructionalOffering();
		    io.setNotOffered(new Boolean(false));
		    io.setSession(subjArea.getSession());
		    io.generateInstrOfferingPermId();
		    io.setLimit(new Integer(0));
		    
		    co.setInstructionalOffering(io);
		    io.addTocourseOfferings(co);

            co.setScheduleBookNote(frm.getScheduleBookNote());

            io.setOfferingCoordinators(new HashSet<OfferingCoordinator>());
            int idx = 0;
	        for (Iterator i = frm.getInstructors().iterator();i.hasNext();) {
	            String instructorId = (String)i.next();
	            String responsibilityId = frm.getResponsibilities(idx);
	            String percShare = frm.getPercentShares(idx);
	            idx++;
	            if (!Constants.BLANK_OPTION_VALUE.equals(instructorId) && !Preference.BLANK_PREF_VALUE.equals(instructorId)) {
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
	        }

	        co.setTitle(frm.getTitle());
	        
	        if (frm.getDemandCourseOfferingId()!=null)
	        	co.setDemandOffering(CourseOfferingDAO.getInstance().get(frm.getDemandCourseOfferingId(),hibSession));
	        
	        if (frm.getAlternativeCourseOfferingId() != null && ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue())
	        	co.setAlternativeOffering(CourseOfferingDAO.getInstance().get(frm.getAlternativeCourseOfferingId(),hibSession));

	        if (frm.getCourseTypeId() != null && !frm.getCourseTypeId().isEmpty()) 
	        	co.setCourseType(CourseTypeDAO.getInstance().get(Long.valueOf(frm.getCourseTypeId()), hibSession));
	        
	        if (frm.getConsent()!=null && frm.getConsent().intValue() > 0)
	        	co.setConsentType(OfferingConsentTypeDAO.getInstance().get(frm.getConsent()));
	        
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

	        if (ApplicationProperty.CourseOfferingEditExternalIds.isTrue())
	        	co.setExternalUniqueId(frm.getExternalId() == null || frm.getExternalId().isEmpty() ? null : frm.getExternalId());

	        frm.setInstrOfferingId((Long)hibSession.save(io));
	        
	        frm.setCourseOfferingId((Long)hibSession.save(co));

	        if (frm.getCreditFormat() != null && !frm.getCreditFormat().isEmpty() && !frm.getCreditFormat().equals(Constants.BLANK_OPTION_VALUE)) {
	        	co.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(frm.getCreditFormat(), frm.getCreditType(), frm.getCreditUnitType(), frm.getUnits(), frm.getMaxUnits(), frm.getFractionalIncrementsAllowed(), new Boolean(true)));
    			co.getCredit().setOwner(co);
	        }

	        if (co.getCredit() != null)
	        	hibSession.saveOrUpdate(co.getCredit());
	        
	        for (OfferingCoordinator coordinator: io.getOfferingCoordinators())
	        	hibSession.saveOrUpdate(coordinator);
	        
            ChangeLog.addChange(
                    hibSession,
                    sessionContext,
                    co,
                    ChangeLog.Source.COURSE_OFFERING_EDIT,
                    ChangeLog.Operation.CREATE,
                    co.getSubjectArea(),
                    co.getDepartment());
            
            hibSession.flush();
            tx.commit();

            hibSession.refresh(co);

            hibSession.refresh(io);
            
            if (sessionContext.hasPermission(io, Right.OfferingCanLock))
		    	io.getSession().lockOffering(io.getUniqueId());
            
        	if (permissionOfferingLockNeeded.check(sessionContext.getUser(), io))
        		StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), io.getSessionId(), io.getUniqueId());    
            
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
        } catch (Exception e) {
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
        final CourseOffering co = cdao.get(courseOfferingId);

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
        frm.setAlternativeCourseOfferingId(co.getAlternativeOffering() == null ? null : co.getAlternativeOffering().getUniqueId());
        frm.setAllowAlternativeCourseOfferings(ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue());
        if (ApplicationProperty.CourseOfferingShowExternalIds.isTrue() || ApplicationProperty.CourseOfferingEditExternalIds.isTrue())
        	frm.setExternalId(co.getExternalUniqueId());

        if (co.getConsentType()!=null)
            frm.setConsent(co.getConsentType().getUniqueId());
        else
            frm.setConsent(new Long(-1));
        LookupTables.setupConsentType(request);
        LookupTables.setupCoordinatorTeachingResponsibilities(request);

        for (OfferingCoordinator coordinator: new TreeSet<OfferingCoordinator>(io.getOfferingCoordinators())) {
            frm.getInstructors().add(coordinator.getInstructor().getUniqueId().toString());
            frm.getResponsibilities().add(coordinator.getResponsibility() == null ? Constants.BLANK_OPTION_VALUE : coordinator.getResponsibility().getUniqueId().toString());
            frm.getPercentShares().add(coordinator.getPercentShare() == null ? "0" : coordinator.getPercentShare().toString());
        }

        if (sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOfferingCoordinators) ||
        	sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOffering))
        	for (int i=0;i<Constants.PREF_ROWS_ADDED;i++)
        		frm.getInstructors().add(Constants.BLANK_OPTION_VALUE);
        
        if (frm.getCreditFormat() == null){
	        if (co.getCredit() != null){
	        	CourseCreditUnitConfig credit = co.getCredit();
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
        
        if (co.isIsControl().booleanValue()) {

            // Catalog Link
            @SuppressWarnings("deprecation")
			String linkLookupClass = ApplicationProperty.CourseCatalogLinkProvider.value();
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
            
            for (OfferingCoordinator coordinator: co.getInstructionalOffering().getOfferingCoordinators())
                deptIds.add(coordinator.getInstructor().getDepartment().getUniqueId());

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
        
        if (ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue())
            LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
    			@Override
    			public boolean accept(CourseOffering course) {
    				return !course.getInstructionalOffering().isNotOffered() && !course.equals(co);
    			}
    		}, "altOfferingList");
        
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
    	
    	if (frm.isAdd()) {
        	if (frm.getInstrOfferingId() != null && frm.getInstrOfferingId() == 0)
        		frm.setInstrOfferingId(null);
        	if (frm.getCourseOfferingId() != null && frm.getCourseOfferingId() == 0)
        		frm.setCourseOfferingId(null);
			LookupTables.setupConsentType(request);
			LookupTables.setupCoordinatorTeachingResponsibilities(request);
			LookupTables.setupCourseCreditFormats(request); // Course Credit Formats
            LookupTables.setupCourseCreditTypes(request); //Course Credit Types
            LookupTables.setupCourseCreditUnitTypes(request); //Course Credit Unit Types
            LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
    			@Override
    			public boolean accept(CourseOffering course) {
    				return course.getDemand() != null && course.getDemand() > 0;
    			}
    		});
            if (ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue())
                LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
        			@Override
        			public boolean accept(CourseOffering course) {
        				return !course.getInstructionalOffering().isNotOffered();
        			}
        		}, "altOfferingList");
            LookupTables.setupCourseTypes(request);
            List<SubjectArea> subjects = new ArrayList<SubjectArea>();
            boolean found = false;
            for (SubjectArea subject: SubjectArea.getUserSubjectAreas(sessionContext.getUser())) {
            	if (sessionContext.hasPermission(subject, Right.AddCourseOffering)) {
            		subjects.add(subject);
            		if (subject.getUniqueId().equals(frm.getSubjectAreaId())) found = true;
            	}
            }
            if (!found && !subjects.isEmpty())
            	frm.setSubjectAreaId(subjects.get(0).getUniqueId());
            request.setAttribute("subjects", subjects);
            if (frm.getSubjectAreaId() != null) {
            	SubjectArea subject = SubjectAreaDAO.getInstance().get(frm.getSubjectAreaId());
            	LookupTables.setupInstructors(request, sessionContext, subject.getDepartment().getUniqueId());
            }
            return;
    	}

    	if (!sessionContext.hasPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingNote) &&
        	!sessionContext.hasPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingCoordinators))
    		sessionContext.checkPermission(frm.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering);

    	frm.setAllowDemandCourseOfferings(true);
    	frm.setAllowAlternativeCourseOfferings(ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue());

        LookupTables.setupConsentType(request);
        LookupTables.setupCoordinatorTeachingResponsibilities(request);
        LookupTables.setupCourseCreditFormats(request); // Course Credit Formats
        LookupTables.setupCourseCreditTypes(request); //Course Credit Types
        LookupTables.setupCourseCreditUnitTypes(request); //Course Credit Unit Types

        final CourseOffering co = new CourseOfferingDAO().get(frm.getCourseOfferingId());
        LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
			@Override
			public boolean accept(CourseOffering course) {
				return course.getDemand() != null && course.getDemand() > 0;
			}
		});
        if (ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue())
            LookupTables.setupCourseOfferings(request, sessionContext, new LookupTables.CourseFilter() {
    			@Override
    			public boolean accept(CourseOffering course) {
    				return !course.getInstructionalOffering().isNotOffered() && !course.equals(co);
    			}
    		}, "altOfferingList");
        
        if (co.isIsControl()) {
            // Setup instructors
            Set<Long> deptIds = new HashSet<Long>();
            
            for (OfferingCoordinator coordinator: co.getInstructionalOffering().getOfferingCoordinators())
                deptIds.add(coordinator.getInstructor().getDepartment().getUniqueId());

            for (CourseOffering x: co.getInstructionalOffering().getCourseOfferings())
            	deptIds.add(x.getSubjectArea().getDepartment().getUniqueId());

            Long[] deptsIdsArray = new Long[deptIds.size()]; int idx = 0;
            for (Long departmentId: deptIds)
                deptsIdsArray[idx++] = departmentId;

            LookupTables.setupInstructors(request, sessionContext, deptsIdsArray);        	
        }
    }
    
}
