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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionImplementor;
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
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.InstructionalOffering.OfferingWaitListMode;
import org.unitime.timetable.model.comparators.OfferingCoordinatorComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.model.dao.OverrideTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.InstrOfferingPermIdGenerator;
import org.unitime.timetable.util.LookupTables;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
@Action(value="courseOfferingEdit", results = {
		@Result(name = "edit", type = "tiles", location = "courseOfferingEdit.tiles"),
		@Result(name = "add", type = "tiles", location = "courseOfferingAdd.tiles"),
		@Result(name = "instructionalOfferingDetail", type = "redirect", location = "/instructionalOfferingDetail.action", 
				params = { "io", "${form.instrOfferingId}", "op", "view"}),
		@Result(name = "instructionalOfferingsList", type = "redirect", location = "/instructionalOfferingSearch.action",
			params = { "backType", "InstructionalOffering", "backId", "${form.instrOfferingId}", "anchor", "back"})
	})
@TilesDefinitions({
	@TilesDefinition(name = "courseOfferingEdit.tiles", extend =  "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit Course Offering"),
			@TilesPutAttribute(name = "body", value = "/user/courseOfferingEdit.jsp"),
			@TilesPutAttribute(name = "showNavigation", value = "true")
	}),
	@TilesDefinition(name = "courseOfferingAdd.tiles", extend =  "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Add Course Offering"),
			@TilesPutAttribute(name = "body", value = "/user/courseOfferingEdit.jsp"),
			@TilesPutAttribute(name = "showNavigation", value = "true")
	})
})
public class CourseOfferingEditAction extends UniTimeAction<CourseOfferingEditForm> {
	private static final long serialVersionUID = -8547793378971178908L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static SecurityMessages SEC = Localization.create(SecurityMessages.class);
	
	protected String op2 = null;
	protected Long courseOfferingId;
	protected Long subjAreaId;
	protected String courseNbr;
	protected Integer deleteId;
	protected String deleteType;

	public String getHdnOp() { return op2; }
	public void setHdnOp(String hdnOp) { this.op2 = hdnOp; }
	public Long getCourseOfferingId() { return courseOfferingId; }
	public void setCourseOfferingId(Long courseOfferingId) { this.courseOfferingId = courseOfferingId; }
	public Long getSubjAreaId() { return subjAreaId; }
	public void setSubjAreaId(Long subjAreaId) { this.subjAreaId = subjAreaId; }
	public String getCourseNbr() { return courseNbr; }
	public void setCourseNbr(String courseNbr) { this.courseNbr = courseNbr; }
	public Integer getDeleteId() { return deleteId; }
	public void setDeleteId(Integer deleteId) { this.deleteId = deleteId; }
	public String getDeleteType() { return deleteType; }
	public void setDeleteType(String deleteType) { this.deleteType = deleteType; }

	@Override
    public String execute() throws Exception {
		if (form == null) {
			form = new CourseOfferingEditForm();
		}

        // Read Parameters
    	if (op == null) op = form.getOp();
    	if (op2 != null && !op2.isEmpty()) op = op2;

		// Check operation
		if (op==null || op.trim().isEmpty()) {
			op = "reload";
		}

		Debug.debug ("Op: " + op);
		
		if (op.equals(MSG.actionBackToIODetail()) && !form.isAdd()) {
			return "instructionalOfferingDetail";
		}
		if (op.equals(MSG.actionBackToIOList()) && form.isAdd()) {
			return "instructionalOfferingsList";
		}

		if (op.equals(MSG.actionEditCourseOffering()) ) {
			if (courseOfferingId==null && form.getCourseOfferingId()!=null) {
				courseOfferingId = form.getCourseOfferingId();
			}
			
			if (courseOfferingId==null) {
				throw new Exception (MSG.errorCourseDataNotCorrect() + courseOfferingId);
			} else  {
				if (ApplicationProperty.LegacyCourseEdit.isTrue()) {
					doLoad(courseOfferingId);
					return "edit";
			    } else {
			    	response.sendRedirect("courseOffering?offering=" + (courseOfferingId == null ? "" : courseOfferingId) + "&op=editCourseOffering");
			    	return null;
			    }
			}
		}
		
		if (op.equals(MSG.actionAddCourseOffering())) {
			form.setSubjectAreaId(subjAreaId);
			form.setCourseNbr(courseNbr);
			TreeSet<SubjectArea> subjects = SubjectArea.getUserSubjectAreas(sessionContext.getUser());
			if (form.getSubjectAreaId() == null && !subjects.isEmpty())
				form.setSubjectAreaId(subjects.first().getUniqueId());
			form.setIsControl(true);
			form.setAllowDemandCourseOfferings(true);
			for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
                form.getInstructors().add(Preference.BLANK_PREF_VALUE);
                form.getPercentShares().add("");
                form.getResponsibilities().add(form.getDefaultTeachingResponsibilityId());
			}
			form.setAdd(true);
			Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
	        form.setWkEnrollDefault(session.getLastWeekToEnroll());
	        form.setWkChangeDefault(session.getLastWeekToChange());
	        form.setWkDropDefault(session.getLastWeekToDrop());
	        form.setWeekStartDayOfWeek(Localization.getDateFormat("EEEE").format(session.getSessionBeginDateTime()));
	        form.setAllowAlternativeCourseOfferings(ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue());
			doReload();
		}

		if (op.equals(MSG.actionUpdateCourseOffering()) || op.equals(MSG.actionSaveCourseOffering())) {
			form.validate(this);
			if (!hasFieldErrors()) {
				if (form.isAdd())
					doSave();
				else
					doUpdate();

			    String cn = (String) sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
				if (cn!=null)
					sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, form.getCourseNbr());

			    // Redirect to instr offering detail on success
				form.setOp("view");
				return "instructionalOfferingDetail";
			} else {
			    doReload();
			}
		}
		
		if (op.equals(MSG.actionAddCoordinator()) ) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
                form.getInstructors().add(Preference.BLANK_PREF_VALUE);
                form.getPercentShares().add("");
                form.getResponsibilities().add(form.getDefaultTeachingResponsibilityId());
            }
            doReload();
		}
		
        if (op.equals(MSG.actionRemoveCoordinator()) && "coordinator".equals(deleteType)) {
            try {
                if (deleteId != null && deleteId>=0) {
                    form.getInstructors().remove((int)deleteId);
                    form.getResponsibilities().remove((int)deleteId);
                    form.getPercentShares().remove((int)deleteId);
                }
            } catch (Exception e) {}
            doReload();
        }
        
        if (op.equals("reload")) {
        	doReload();
        }
        
        return (form.isAdd() ? "add" : "edit");
    }

    private void doUpdate() throws Exception {
    	boolean limitedEdit = false, updateNote = false, updateCoordinators = false; 
    	
    	if (sessionContext.hasPermission(form.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingNote)) {
    		updateNote = true;
    	}
    	if (sessionContext.hasPermission(form.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingCoordinators)) {
    		updateCoordinators = true;
    	}
    	if (updateNote || updateCoordinators) {
    		limitedEdit = !sessionContext.hasPermission(form.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering);
    	} else {
    		sessionContext.checkPermission(form.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering);
    	}

        String title = form.getTitle();
        String note = form.getScheduleBookNote();
        Long crsId = form.getCourseOfferingId();
        String crsNbr = form.getCourseNbr();

		org.hibernate.Session hibSession = null;
        Transaction tx = null;

        try {
            OfferingConsentTypeDAO odao = OfferingConsentTypeDAO.getInstance();
	        CourseOfferingDAO cdao = CourseOfferingDAO.getInstance();
            hibSession = cdao.getSession();
            tx = hibSession.beginTransaction();

	        CourseOffering co = cdao.get(crsId);
	        InstructionalOffering io = co.getInstructionalOffering();
	        
	        if (!limitedEdit || updateNote)
	        	co.setScheduleBookNote(note);
	        
	        co.getDisabledOverrides().clear();
            for (String override: form.getCourseOverrides())
            	co.getDisabledOverrides().add(OverrideTypeDAO.getInstance().get(Long.valueOf(override)));
            
	        // Update wait-list
	        if (co.isIsControl()) {
		        if (form.getWaitList() == null || form.getWaitList().isEmpty())
		        	io.setWaitListMode(null);
		        else if ("waitlist".equalsIgnoreCase(form.getWaitList()))
		        	io.setWaitListMode(OfferingWaitListMode.WaitList);
		        else if ("reschedule".equalsIgnoreCase(form.getWaitList()))
		        	io.setWaitListMode(OfferingWaitListMode.ReSchedule);
		        else
		        	io.setWaitListMode(OfferingWaitListMode.Disabled);
		        if (limitedEdit)
		        	hibSession.merge(io);
	        }

	        if ((!limitedEdit || updateCoordinators) && co.isIsControl().booleanValue()) {
		        boolean assignTeachingRequest = Department.isInstructorSchedulingCommitted(co.getDepartment().getUniqueId());
		        if (io.getOfferingCoordinators() == null) io.setOfferingCoordinators(new HashSet<OfferingCoordinator>());
		        List<OfferingCoordinator> coordinators = new ArrayList<OfferingCoordinator>(io.getOfferingCoordinators());
		        int idx = 0;
		        for (Iterator i = form.getInstructors().iterator();i.hasNext();) {
		            String instructorId = (String)i.next();
		            String responsibilityId = form.getResponsibilities(idx);
		            String percShare = form.getPercentShares(idx);
		            idx++;
		            if (!Constants.BLANK_OPTION_VALUE.equals(instructorId) && !Preference.BLANK_PREF_VALUE.equals(instructorId)) {
		                DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(Long.valueOf(instructorId));
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
		                	if (coordinator.getUniqueId() == null)
		                		hibSession.persist(coordinator);
		                	else
		                		hibSession.merge(coordinator);
		                }
		           }
		        }
		        for (OfferingCoordinator coordinator: coordinators) {
		        	coordinator.getInstructor().getOfferingCoordinators().remove(coordinator);
		        	io.getOfferingCoordinators().remove(coordinator);
		        	hibSession.remove(coordinator);
		        }
		        if (limitedEdit)
		        	hibSession.merge(io);
	        }

	        if (!limitedEdit) {
		        if (co.getCourseNbr() != null && !co.getCourseNbr().equals(crsNbr) && co.getPermId() == null){
		        	LastLikeCourseDemand llcd = null;
		        	String permId = InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)CourseOfferingDAO.getInstance().getSession(), co).toString();
		        	for(Iterator it = co.getCourseOfferingDemands().iterator(); it.hasNext();){
		        		llcd = (LastLikeCourseDemand)it.next();
		        		if (llcd.getCoursePermId() == null){
			        		llcd.setCoursePermId(permId);
			        		hibSession.merge(llcd);
		        		}
		        	}
	        		co.setPermId(permId);
		        }
		        co.setCourseNbr(crsNbr);
		        co.setTitle(title);

		        if (form.getDemandCourseOfferingId()==null) {
		        	co.setDemandOffering(null);
		        } else {
		        	CourseOffering dco = cdao.get(form.getDemandCourseOfferingId(),hibSession);
		        	co.setDemandOffering(dco==null?null:dco);
		        }
		        
		        if (ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue()) {
			        if (form.getAlternativeCourseOfferingId() == null)
			        	co.setAlternativeOffering(null);
			        else {
			        	co.setAlternativeOffering(CourseOfferingDAO.getInstance().get(form.getAlternativeCourseOfferingId(),hibSession));
			        }	
		        }
		        
		        if (form.getCourseTypeId() == null || form.getCourseTypeId().isEmpty()) {
		        	co.setCourseType(null);
		        } else {
		        	co.setCourseType(CourseTypeDAO.getInstance().get(Long.valueOf(form.getCourseTypeId()), hibSession));
		        }

		        if (form.getConsent()==null || form.getConsent().intValue()<=0)
		            co.setConsentType(null);
		        else {
		            OfferingConsentType oct = odao.get(form.getConsent());
		            co.setConsentType(oct);
		        }

		        // Update credit
		        if (form.getCreditFormat() == null || form.getCreditFormat().length() == 0 || form.getCreditFormat().equals(Constants.BLANK_OPTION_VALUE)){
		        	CourseCreditUnitConfig origConfig = co.getCredit();
		        	if (origConfig != null){
						co.setCredit(null);
						hibSession.remove(origConfig);
		        	}
		        } else {
		         	if(co.getCredit() != null){
		        		CourseCreditUnitConfig ccuc = co.getCredit();
		        		if (ccuc.getCreditFormat().equals(form.getCreditFormat())){
		        			boolean changed = false;
		        			if (!ccuc.getCreditType().getUniqueId().equals(form.getCreditType())){
		        				changed = true;
		        			}
		        			if (!ccuc.getCreditUnitType().getUniqueId().equals(form.getCreditUnitType())){
		        				changed = true;
		        			}
		        			if (ccuc instanceof FixedCreditUnitConfig) {
								FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) ccuc;
								if (!fcuc.getFixedUnits().equals(form.getUnits())){
									changed = true;
								}
							} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
								VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) ccuc;
								if (!vfcuc.getMinUnits().equals(form.getUnits())){
									changed = true;
								}
								if (!vfcuc.getMaxUnits().equals(form.getMaxUnits())){
									changed = true;
								}
								if (vfcuc instanceof VariableRangeCreditUnitConfig) {
									VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) vfcuc;
									if (vrcuc.isFractionalIncrementsAllowed() == null || !vrcuc.isFractionalIncrementsAllowed().equals(form.getFractionalIncrementsAllowed())){
										changed = true;
									}
								}
							}
		        			if (changed){
		        				CourseCreditUnitConfig origConfig = co.getCredit();
		            			co.setCredit(null);
		            			hibSession.remove(origConfig);
		            			co.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(form.getCreditFormat(), form.getCreditType(), form.getCreditUnitType(), form.getUnits(), form.getMaxUnits(), form.getFractionalIncrementsAllowed(), Boolean.valueOf(true)));
		            			co.getCredit().setOwner(co);
		        			}
		        		} else {
		        			CourseCreditUnitConfig origConfig = co.getCredit();
		        			co.setCredit(null);
		        			hibSession.remove(origConfig);
		        			co.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(form.getCreditFormat(), form.getCreditType(), form.getCreditUnitType(), form.getUnits(), form.getMaxUnits(), form.getFractionalIncrementsAllowed(), Boolean.valueOf(true)));
		        			co.getCredit().setOwner(co);
		        		}
		        	} else {
		    			co.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(form.getCreditFormat(), form.getCreditType(), form.getCreditUnitType(), form.getUnits(), form.getMaxUnits(), form.getFractionalIncrementsAllowed(), Boolean.valueOf(true)));
		    			co.getCredit().setOwner(co);
		        	}

			        if (co.getCredit() != null){
			        	if (co.getCredit().getUniqueId() == null)
	                		hibSession.persist(co.getCredit());
	                	else
	                		hibSession.merge(co.getCredit());
			        }
		        }
		        
		        if (co.isIsControl()) {
			        io.setByReservationOnly(form.isByReservationOnly());
			        try {
			        	io.setLastWeekToEnroll(Integer.parseInt(form.getWkEnroll()));
			        } catch (Exception e) {
			        	io.setLastWeekToEnroll(null);
			        }
			        try {
				        io.setLastWeekToChange(Integer.parseInt(form.getWkChange()));
			        } catch (Exception e) {
				        io.setLastWeekToChange(null);
			        }
			        try{
				        io.setLastWeekToDrop(Integer.parseInt(form.getWkDrop()));
			        } catch (Exception e) {
			        	io.setLastWeekToDrop(null);
			        }
			        io.setNotes(form.getNotes() == null || form.getNotes().length() <= 2000 ? form.getNotes() : form.getNotes().substring(0, 2000));

			        hibSession.merge(io);
		        }
		        
		        if (ApplicationProperty.CourseOfferingEditExternalIds.isTrue())
		        	co.setExternalUniqueId(form.getExternalId() == null || form.getExternalId().isEmpty() ? null : form.getExternalId());
	        }

	        hibSession.merge(co);

            ChangeLog.addChange(
                    hibSession,
                    sessionContext,
                    co,
                    ChangeLog.Source.COURSE_OFFERING_EDIT,
                    ChangeLog.Operation.UPDATE,
                    co.getSubjectArea(),
                    co.getDepartment());
            
        	if (limitedEdit && getPermissionOfferingLockNeeded().check(sessionContext.getUser(), io))
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
	        	ExternalCourseOfferingEditAction editAction = (ExternalCourseOfferingEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
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
    
    private void doSave() throws Exception {
    	sessionContext.checkPermission(form.getSubjectAreaId(), "SubjectArea", Right.AddCourseOffering);
    	
		org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
        Transaction tx = null;

        try {
            tx = hibSession.beginTransaction();
            
            SubjectArea subjArea = SubjectAreaDAO.getInstance().get(form.getSubjectAreaId(), hibSession);
            
            CourseOffering co = new CourseOffering();
            
            co.setSubjectArea(subjArea);
            co.setSubjectAreaAbbv(subjArea.getSubjectAreaAbbreviation());
		    co.setCourseNbr(form.getCourseNbr());
		    co.setProjectedDemand(Integer.valueOf(0));
            co.setDemand(Integer.valueOf(0));
		    co.setNbrExpectedStudents(Integer.valueOf(0));
		    co.setIsControl(Boolean.valueOf(true));
		    co.setPermId(InstrOfferingPermIdGenerator.getGenerator().generate((SessionImplementor)CourseOfferingDAO.getInstance().getSession(), co).toString());
		    subjArea.getCourseOfferings().add(co);

	        // Add new Instructional Offering
		    InstructionalOffering io = new InstructionalOffering();
		    io.setNotOffered(Boolean.valueOf(false));
		    io.setSession(subjArea.getSession());
		    io.generateInstrOfferingPermId();
		    
		    co.setInstructionalOffering(io);
		    io.addToCourseOfferings(co);

            co.setScheduleBookNote(form.getScheduleBookNote());
            
            co.setDisabledOverrides(new HashSet<OverrideType>());
            for (String override: form.getCourseOverrides())
            	co.getDisabledOverrides().add(OverrideTypeDAO.getInstance().get(Long.valueOf(override)));	

            io.setOfferingCoordinators(new HashSet<OfferingCoordinator>());
            int idx = 0;
	        for (Iterator i = form.getInstructors().iterator();i.hasNext();) {
	            String instructorId = (String)i.next();
	            String responsibilityId = form.getResponsibilities(idx);
	            String percShare = form.getPercentShares(idx);
	            idx++;
	            if (!Constants.BLANK_OPTION_VALUE.equals(instructorId) && !Preference.BLANK_PREF_VALUE.equals(instructorId)) {
	                DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(Long.valueOf(instructorId));
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

	        co.setTitle(form.getTitle());
	        
	        if (form.getDemandCourseOfferingId()!=null)
	        	co.setDemandOffering(CourseOfferingDAO.getInstance().get(form.getDemandCourseOfferingId(),hibSession));
	        
	        if (form.getAlternativeCourseOfferingId() != null && ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue())
	        	co.setAlternativeOffering(CourseOfferingDAO.getInstance().get(form.getAlternativeCourseOfferingId(),hibSession));

	        if (form.getCourseTypeId() != null && !form.getCourseTypeId().isEmpty()) 
	        	co.setCourseType(CourseTypeDAO.getInstance().get(Long.valueOf(form.getCourseTypeId()), hibSession));
	        
	        if (form.getConsent()!=null && form.getConsent().intValue() > 0)
	        	co.setConsentType(OfferingConsentTypeDAO.getInstance().get(form.getConsent()));
	        
	        io.setByReservationOnly(form.isByReservationOnly());
	        
	        try {
	        	io.setLastWeekToEnroll(Integer.parseInt(form.getWkEnroll()));
	        } catch (Exception e) {
	        	io.setLastWeekToEnroll(null);
	        }
	        try {
		        io.setLastWeekToChange(Integer.parseInt(form.getWkChange()));
	        } catch (Exception e) {
		        io.setLastWeekToChange(null);
	        }
	        try{
		        io.setLastWeekToDrop(Integer.parseInt(form.getWkDrop()));
	        } catch (Exception e) {
	        	io.setLastWeekToDrop(null);
	        }
	        
	        if (form.getWaitList() == null || form.getWaitList().isEmpty())
	        	io.setWaitListMode(null);
	        else if ("waitlist".equalsIgnoreCase(form.getWaitList()))
	        	io.setWaitListMode(OfferingWaitListMode.WaitList);
	        else if ("reschedule".equalsIgnoreCase(form.getWaitList()))
	        	io.setWaitListMode(OfferingWaitListMode.ReSchedule);
	        else
	        	io.setWaitListMode(OfferingWaitListMode.Disabled);
	        
	        io.setNotes(form.getNotes() == null || form.getNotes().length() <= 2000 ? form.getNotes() : form.getNotes().substring(0, 2000));

	        if (ApplicationProperty.CourseOfferingEditExternalIds.isTrue())
	        	co.setExternalUniqueId(form.getExternalId() == null || form.getExternalId().isEmpty() ? null : form.getExternalId());

	        hibSession.persist(io);
	        
	        hibSession.persist(co);

	        if (form.getCreditFormat() != null && !form.getCreditFormat().isEmpty() && !form.getCreditFormat().equals(Constants.BLANK_OPTION_VALUE)) {
	        	co.setCredit(CourseCreditUnitConfig.createCreditUnitConfigOfFormat(form.getCreditFormat(), form.getCreditType(), form.getCreditUnitType(), form.getUnits(), form.getMaxUnits(), form.getFractionalIncrementsAllowed(), Boolean.valueOf(true)));
    			co.getCredit().setOwner(co);
	        }

	        if (co.getCredit() != null)
	        	hibSession.persist(co.getCredit());
	        
	        for (OfferingCoordinator coordinator: io.getOfferingCoordinators())
	        	hibSession.persist(coordinator);
	        
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
            
            form.setInstrOfferingId(io.getUniqueId());
	        
	        form.setCourseOfferingId(co.getUniqueId());

            hibSession.refresh(co);

            hibSession.refresh(io);
            
            if (sessionContext.hasPermission(io, Right.OfferingCanLock))
		    	io.getSession().lockOffering(io.getUniqueId());
            
        	if (getPermissionOfferingLockNeeded().check(sessionContext.getUser(), io))
        		StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), io.getSessionId(), io.getUniqueId());    
            
		    String className1 = ApplicationProperty.ExternalActionInstructionalOfferingAdd.value();
        	if (className1 != null && className1.trim().length() > 0){
	        	ExternalInstructionalOfferingAddAction addAction = (ExternalInstructionalOfferingAddAction) (Class.forName(className1).getDeclaredConstructor().newInstance());
	       		addAction.performExternalInstructionalOfferingAddAction(io, hibSession);
        	}

        	String className2 = ApplicationProperty.ExternalActionCourseOfferingEdit.value();
        	if (className2 != null && className2.trim().length() > 0){
	        	ExternalCourseOfferingEditAction editAction = (ExternalCourseOfferingEditAction) (Class.forName(className2).getDeclaredConstructor().newInstance());
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

    private void doLoad(Long crsOfferingId) throws Exception {
    	
    	if (!sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOfferingNote) &&
    		!sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOfferingCoordinators))
    		sessionContext.checkPermission(crsOfferingId, "CourseOffering", Right.EditCourseOffering);

        // Load Course Offering
        Long courseOfferingId = Long.valueOf(crsOfferingId);

        CourseOfferingDAO cdao = CourseOfferingDAO.getInstance();
        final CourseOffering co = cdao.get(courseOfferingId);

        InstructionalOffering io = co.getInstructionalOffering();
        Long subjectAreaId = co.getSubjectArea().getUniqueId();//io.getControllingCourseOffering().getSubjectArea().getUniqueId();

        form.setDemandCourseOfferingId(co.getDemandOffering()==null?null:co.getDemandOffering().getUniqueId());
        form.setAllowDemandCourseOfferings(true);//co.getLastLikeSemesterCourseOfferingDemands().isEmpty());
        form.setCourseName(co.getCourseName());
        form.setCourseNbr(co.getCourseNbr());
        form.setCourseOfferingId(courseOfferingId);
        form.setInstrOfferingId(io.getUniqueId());
        form.setScheduleBookNote(co.getScheduleBookNote());
        form.setNotes(io.getNotes());
        form.setSubjectAreaId(subjectAreaId);
        form.setTitle(co.getTitle());
        form.setIsControl(co.getIsControl());
        form.setIoNotOffered(io.getNotOffered());
        form.setByReservationOnly(io.isByReservationOnly());
        form.setWkEnroll(io.getLastWeekToEnroll() == null ? "" : io.getLastWeekToEnroll().toString());
        form.setWkEnrollDefault(io.getSession().getLastWeekToEnroll());
        form.setWkChange(io.getLastWeekToChange() == null ? "" : io.getLastWeekToChange().toString());
        form.setWkChangeDefault(io.getSession().getLastWeekToChange());
        form.setWkDrop(io.getLastWeekToDrop() == null ? "" : io.getLastWeekToDrop().toString());
        form.setWkDropDefault(io.getSession().getLastWeekToDrop());
        form.setWaitList(io.getWaitlistMode() == null ? "" : io.getWaitListMode().name());
        form.setWeekStartDayOfWeek(Localization.getDateFormat("EEEE").format(io.getSession().getSessionBeginDateTime()));
        form.setCourseTypeId(co.getCourseType() == null ? "" : co.getCourseType().getUniqueId().toString());
        form.setAlternativeCourseOfferingId(co.getAlternativeOffering() == null ? null : co.getAlternativeOffering().getUniqueId());
        form.setAllowAlternativeCourseOfferings(ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue());
        if (ApplicationProperty.CourseOfferingShowExternalIds.isTrue() || ApplicationProperty.CourseOfferingEditExternalIds.isTrue())
        	form.setExternalId(co.getExternalUniqueId());

        if (co.getConsentType()!=null)
            form.setConsent(co.getConsentType().getUniqueId());
        else
            form.setConsent(Long.valueOf(-1));
        LookupTables.setupConsentType(request);
        LookupTables.setupCoordinatorTeachingResponsibilities(request);

        List<OfferingCoordinator> coordinatorList = new ArrayList<OfferingCoordinator>(io.getOfferingCoordinators());
        Collections.sort(coordinatorList, new OfferingCoordinatorComparator(sessionContext));
        for (OfferingCoordinator coordinator: coordinatorList) {
            form.getInstructors().add(coordinator.getInstructor().getUniqueId().toString());
            form.getResponsibilities().add(coordinator.getResponsibility() == null ? Constants.BLANK_OPTION_VALUE : coordinator.getResponsibility().getUniqueId().toString());
            form.getPercentShares().add(coordinator.getPercentShare() == null ? "0" : coordinator.getPercentShare().toString());
        }
        
        for (OverrideType override: co.getDisabledOverrides())
        	form.addCourseOverride(override.getUniqueId().toString());

        if (sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOfferingCoordinators) ||
        	sessionContext.hasPermission(crsOfferingId, "CourseOffering", Right.EditCourseOffering)) {
        	for (int i=0;i<Constants.PREF_ROWS_ADDED;i++) {
        		form.getInstructors().add(Constants.BLANK_OPTION_VALUE);
        		form.getResponsibilities().add(form.getDefaultTeachingResponsibilityId());
        	}
        }
        
        if (form.getCreditFormat() == null){
	        if (co.getCredit() != null){
	        	CourseCreditUnitConfig credit = co.getCredit();
	        	form.setCreditText(credit.creditText());
	        	form.setCreditFormat(credit.getCreditFormat());
	        	form.setCreditType(credit.getCreditType().getUniqueId());
	        	form.setCreditUnitType(credit.getCreditUnitType().getUniqueId());
	        	if (credit instanceof FixedCreditUnitConfig){
	        		form.setUnits(((FixedCreditUnitConfig) credit).getFixedUnits());
	        	} else if (credit instanceof VariableFixedCreditUnitConfig){
	        		form.setUnits(((VariableFixedCreditUnitConfig) credit).getMinUnits());
	        		form.setMaxUnits(((VariableFixedCreditUnitConfig) credit).getMaxUnits());
	        		if (credit instanceof VariableRangeCreditUnitConfig){
	        			form.setFractionalIncrementsAllowed(((VariableRangeCreditUnitConfig) credit).isFractionalIncrementsAllowed());
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
            	ExternalLinkLookup lookup = (ExternalLinkLookup) (Class.forName(linkLookupClass).getDeclaredConstructor().newInstance());
           		Map results = lookup.getLink(io);
                if (results==null)
                    throw new Exception (lookup.getErrorMessage());
                
                form.setCatalogLinkLabel((String)results.get(ExternalLinkLookup.LINK_LABEL));
                form.setCatalogLinkLocation((String)results.get(ExternalLinkLookup.LINK_LOCATION));
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
     * @param form
     * @param courseOfferingId
     */
    private void doReload() throws Exception {
    	
    	if (form.isAdd()) {
        	if (form.getInstrOfferingId() != null && form.getInstrOfferingId() == 0)
        		form.setInstrOfferingId(null);
        	if (form.getCourseOfferingId() != null && form.getCourseOfferingId() == 0)
        		form.setCourseOfferingId(null);
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
            		if (subject.getUniqueId().equals(form.getSubjectAreaId())) found = true;
            	}
            }
            if (!found && !subjects.isEmpty())
            	form.setSubjectAreaId(subjects.get(0).getUniqueId());
            request.setAttribute("subjects", subjects);
            if (form.getSubjectAreaId() != null) {
            	SubjectArea subject = SubjectAreaDAO.getInstance().get(form.getSubjectAreaId());
            	LookupTables.setupInstructors(request, sessionContext, subject.getDepartment().getUniqueId());
            }
            return;
    	}

    	if (!sessionContext.hasPermission(form.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingNote) &&
        	!sessionContext.hasPermission(form.getCourseOfferingId(), "CourseOffering", Right.EditCourseOfferingCoordinators))
    		sessionContext.checkPermission(form.getCourseOfferingId(), "CourseOffering", Right.EditCourseOffering);

    	form.setAllowDemandCourseOfferings(true);
    	form.setAllowAlternativeCourseOfferings(ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue());

        LookupTables.setupConsentType(request);
        LookupTables.setupCoordinatorTeachingResponsibilities(request);
        LookupTables.setupCourseCreditFormats(request); // Course Credit Formats
        LookupTables.setupCourseCreditTypes(request); //Course Credit Types
        LookupTables.setupCourseCreditUnitTypes(request); //Course Credit Unit Types

        final CourseOffering co = CourseOfferingDAO.getInstance().get(form.getCourseOfferingId());
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
    
    protected Permission<InstructionalOffering> getPermissionOfferingLockNeeded() {
    	return getPermission("permissionOfferingLockNeeded");
    }
    
    public String getCrsNbr() {
    	return (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
    }
}
