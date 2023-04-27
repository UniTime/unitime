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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructionalOfferingDetailForm;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingDeleteAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingNotOfferedAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingOfferedAction;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.OfferingCoordinatorComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;
import org.unitime.timetable.webutil.WebInstrOfferingConfigTableBuilder;


/** 
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Action(value = "instructionalOfferingDetail", results = {
		@Result(name = "showConfigDetail", type = "tiles", location = "instructionalOfferingDetail.tiles"),
		@Result(name = "addConfig", type = "redirect", location = "/instructionalOfferingConfigEdit.action",
			params = { "form.instrOfferingId", "${form.instrOfferingId}", "uid", "${form.ctrlCrsOfferingId}", "op", "${op}"}),
		@Result(name = "showInstructionalOfferings", type = "redirect", location = "/instructionalOfferingSearch.action",
				params = { "backType", "InstructionalOffering", "backId", "${form.instrOfferingId}",
						"anchor", "back"}),
		@Result(name = "modifyCrossLists", type = "redirect", location = "/crossListsModify.action",
			params = { "instrOfferingId", "${form.instrOfferingId}", "uid", "${form.ctrlCrsOfferingId}", "op", "${op}"}),
		@Result(name = "editCourse", type = "redirect", location = "/courseOfferingEdit.action",
			params = { "courseOfferingId", "${form.crsOfferingId}", "op", "${op}"})
	})
@TilesDefinition(name = "instructionalOfferingDetail.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Instructional Offering Detail"),
		@TilesPutAttribute(name = "body", value = "/user/instructionalOfferingDetail.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
	})
public class InstructionalOfferingDetailAction extends UniTimeAction<InstructionalOfferingDetailForm> {
	private static final long serialVersionUID = -4648988999277363085L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static GwtMessages GMSG = Localization.create(GwtMessages.class);
	protected Long instrOfferingId = null;
	protected Long courseOfferingId = null;
	protected String op2 = null;
	protected String confirm = null;
	protected String crsNbr = null;
	
	public Long getIo() { return instrOfferingId; }
	public void setIo(Long instrOfferingId) { this.instrOfferingId = instrOfferingId; }
	public Long getCo() { return courseOfferingId; }
	public void setCo(Long courseOfferingId) { this.courseOfferingId = courseOfferingId; }
	public String getHdnOp() { return op2; }
	public void setHdnOp(String hdnOp) { this.op2 = hdnOp; }
	public String getConfirm() { return confirm; }
	public void setConfirm(String confirm) { this.confirm = confirm; }
	public String getCrsNbr() { return crsNbr; }
	public void setCrsNbr(String crsNbr) { this.crsNbr = crsNbr; }

	/** 
     * Method execute
     */
    public String execute() throws Exception {
    	if (form == null) form = new InstructionalOfferingDetailForm();
    	
    	setCrsNbr((String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber));

        // Read Parameters
    	if (op == null) op = form.getOp();
    	if (op2 != null && !op2.isEmpty()) op = op2;
		if ("n".equals(confirm)) op = "view";
    	
		// Check operation
		if (op==null || op.trim().isEmpty())
			throw new Exception (MSG.exceptionOperationNotInterpreted() + op);
		
		Debug.debug ("Op: " + op);
		// Delete insructional offering
		if (op.equals(MSG.actionDeleteIO())) {
	    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.OfferingDelete);
			doDelete(request, form);
			sessionContext.removeAttribute(SessionAttribute.OfferingsCourseNumber);
	        return "showInstructionalOfferings";
		}
		
		if (op.equals(MSG.actionEditCourseOffering()) ) {
			if (ApplicationProperty.LegacyCourseEdit.isTrue()) {
				return "editCourse";
			} else {
				response.sendRedirect("gwt.jsp?page=courseOffering&offering=" + form.getCrsOfferingId() + "&op=editCourseOffering");
				return null;
			}
		}
		
		// Display detail - default
		if (op.equals("view")
		        || op.equals(MSG.actionUpdateConfiguration()) 
		        || op.equals(MSG.actionSaveConfiguration()) 
		        || op.equals(MSG.actionDeleteConfiguration())
		        || op.equals(MSG.actionUnassignAllInstructorsFromConfig()) ) {
			
		    if (instrOfferingId == null && form.getInstrOfferingId() != null)
		    	instrOfferingId = form.getInstrOfferingId();
		    
		    if (instrOfferingId == null && courseOfferingId != null) {
		    	try {
		    		instrOfferingId = CourseOfferingDAO.getInstance().get(courseOfferingId).getInstructionalOffering().getUniqueId();
		    	} catch (Exception e) {}
		    }
		    
			if (instrOfferingId==null) {
			    throw new Exception(MSG.exceptionIODataNotCorrect() + instrOfferingId);
			} else {
		    	sessionContext.checkPermission(instrOfferingId, "InstructionalOffering", Right.InstructionalOfferingDetail);
		    	doLoad();
			}
			
			BackTracker.markForBack(
					request,
					"instructionalOfferingDetail.action?io="+form.getInstrOfferingId(),
					MSG.backInstructionalOffering(form.getInstrOfferingNameNoTitle()),
					true, false);
			
			return "showConfigDetail";
		}

		// Add Configuration
		if (op.equals(MSG.actionAddConfiguration())) {
	    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.InstrOfferingConfigAdd);
		    return "addConfig";
		}
		
		// Make Offering 'Offered'
		if(op.equals(MSG.actionMakeOffered())) {
	    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.OfferingMakeOffered);
		    doMakeOffered();
		    // Redirect to config edit
		    return "addConfig";
		}
		
		// Make Offering 'Not Offered'
		if(op.equals(MSG.actionMakeNotOffered())) {
	    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.OfferingMakeNotOffered);
	    	doMakeNotOffered();
	    	if (ApplicationProperty.MakeNotOfferedStaysOnDetail.isFalse()) {
	    		response.sendRedirect(response.encodeURL("instructionalOfferingSearch.action#A" + form.getInstrOfferingId()));
                return null;
	    	} else {
	    		response.sendRedirect(response.encodeURL("instructionalOfferingDetail.action?io="+form.getInstrOfferingId()));
	        	return null;
	    	}
		}
		
		// Change controlling course, add other offerings
		if(op.equals(MSG.actionCrossLists())) {
	    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.InstructionalOfferingCrossLists);
		    return "modifyCrossLists";
		}
		
        if (op.equals(MSG.actionNextIO())) {
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.action?io="+form.getNextId()));
        	return null;
        }
        
        if (op.equals(MSG.actionPreviousIO())) {
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.action?io="+form.getPreviousId()));
        	return null;
        }
        
        if (op.equals(MSG.actionLockIO())) {
		    InstructionalOfferingDAO idao = InstructionalOfferingDAO.getInstance();
	        InstructionalOffering io = idao.get(form.getInstrOfferingId());

	    	sessionContext.checkPermission(io, Right.OfferingCanLock);

	    	io.getSession().lockOffering(io.getUniqueId());
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.action?io="+io.getUniqueId()));
        	return null;
        }
		
        if (op.equals(MSG.actionUnlockIO())) {
	    	InstructionalOfferingDAO idao = InstructionalOfferingDAO.getInstance();
	        InstructionalOffering io = idao.get(form.getInstrOfferingId());

	    	sessionContext.checkPermission(io, Right.OfferingCanUnlock);

	        io.getSession().unlockOffering(io, sessionContext.getUser());
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.action?io="+io.getUniqueId()));
        	return null;
        }
        
    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.InstructionalOfferingDetail);

        BackTracker.markForBack(
				request,
				"instructionalOfferingDetail.action?io="+form.getInstrOfferingId(),
				MSG.backInstructionalOffering(form.getInstrOfferingName()),
				true, false);
        
		// Go back to instructional offerings
        return "showInstructionalOfferings";
        
    }

    /**
     * Delete Instructional Offering
	 * @param request
	 * @param form
	 */
	private void doDelete(
			HttpServletRequest request, 
			InstructionalOfferingDetailForm form) throws Exception {
		
        org.hibernate.Session hibSession = null;
        Transaction tx = null;
        
        try {
		    InstructionalOfferingDAO idao = InstructionalOfferingDAO.getInstance();
	        InstructionalOffering io = idao.get(form.getInstrOfferingId());

	        hibSession = idao.getSession();
	        tx = hibSession.beginTransaction();

			io.deleteAllDistributionPreferences(hibSession);
            Event.deleteFromEvents(hibSession, io);
	        Exam.deleteFromExams(hibSession, io);
        	String className = ApplicationProperty.ExternalActionInstructionalOfferingDelete.value();
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingDeleteAction deleteAction = (ExternalInstructionalOfferingDeleteAction) (Class.forName(className).getDeclaredConstructor().newInstance());
	       		deleteAction.performExternalInstructionalOfferingDeleteAction(io, hibSession);
        	}
        	
	        
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    io, 
                    ChangeLog.Source.OFFERING_DETAIL, 
                    ChangeLog.Operation.DELETE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);

            for (CourseOffering co: io.getCourseOfferings()) {
            	co.getSubjectArea().getCourseOfferings().remove(co);
            	hibSession.remove(co);
            }
	        hibSession.remove(io);
	        
	        tx.commit();
            hibSession.flush();
            hibSession.clear();
            
        }
        catch (Exception e) {
        	if (tx!=null)
        		tx.rollback();
			Debug.error(e);
            throw (e);
        }
	}
	
	private boolean hasConflicts(HttpServletRequest request, InstructionalOffering io) {
        if (RoomAvailability.getInstance()!=null && !(RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService)) {
            Session session = io.getSession();
            Date[] bounds = DatePattern.getBounds(session.getUniqueId());
            RoomAvailability.getInstance().activate(session.getUniqueId(),bounds[0],bounds[1],RoomAvailabilityInterface.sClassType, false);
            RoomAvailability.setAvailabilityWarning(request, session, true, true);
        }

        ClassAssignmentProxy proxy = getClassAssignmentService().getAssignment();
		try {
			if (proxy != null) return proxy.hasConflicts(io.getUniqueId());
		} catch (Exception e) {}
		
		return false;
	}

	/**
     * Loads the form initially
     */
    private void doLoad() throws Exception {
        
        // Load Instr Offering
        InstructionalOfferingDAO idao = InstructionalOfferingDAO.getInstance();
        InstructionalOffering io = idao.get(instrOfferingId);
        Long subjectAreaId = io.getControllingCourseOffering().getSubjectArea().getUniqueId();
        
    	sessionContext.checkPermission(io, Right.InstructionalOfferingDetail);
        
	    // Set Session Variables
    	InstructionalOfferingSearchAction.setLastInstructionalOffering(sessionContext, io);
        
        // Sort Offerings
        ArrayList offerings = new ArrayList(io.getCourseOfferings());
        Collections.sort(
                offerings, 
                new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_CTRL_CRS));
                
	    // Load Form
        form.setInstrOfferingId(instrOfferingId);
        form.setSubjectAreaId(subjectAreaId);
        form.setInstrOfferingName(io.getCourseNameWithTitle());
        form.setSubjectAreaAbbr(io.getControllingCourseOffering().getSubjectAreaAbbv());
        form.setCourseNbr(io.getControllingCourseOffering().getCourseNbr());
        form.setInstrOfferingNameNoTitle(io.getCourseName());
        form.setCtrlCrsOfferingId(io.getControllingCourseOffering().getUniqueId());
        form.setDemand(io.getDemand());
        form.setEnrollment(io.getEnrollment());
        form.setSnapshotLimit(io.getSnapshotLimit());
        form.setProjectedDemand(io.getProjectedDemand());
        form.setLimit(io.getLimit());
        form.setUnlimited(Boolean.FALSE);
        form.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(io)));
        form.setByReservationOnly(io.isByReservationOnly());
        form.setWkEnroll(io.getLastWeekToEnroll() == null ? "" : io.getLastWeekToEnroll().toString());
        form.setWkChange(io.getLastWeekToChange() == null ? "" : io.getLastWeekToChange().toString());
        form.setWkDrop(io.getLastWeekToDrop() == null ? "" : io.getLastWeekToDrop().toString());
        form.setWaitList(io.getEffectiveWaitListMode().name());
        form.setWeekStartDayOfWeek(Localization.getDateFormat("EEEE").format(io.getSession().getSessionBeginDateTime()));
        form.setHasConflict(hasConflicts(request, io));

        if (io.effectiveWaitList()) {
			OverrideType prohibitedOverride = OverrideType.findByReference(ApplicationProperty.OfferingWaitListProhibitedOverride.value());
			if (prohibitedOverride != null) {
				String message = null;
				for (CourseOffering co: io.getCourseOfferings()) {
					if (co.getDisabledOverrides() == null || !co.getDisabledOverrides().contains(prohibitedOverride)) {
						message = (message == null ? "" : message + "\n") + MSG.problemWaitListProhibitedOverride(co.getCourseName(), prohibitedOverride.getLabel());
					}
				}
				if (message != null)
					request.setAttribute("waitlistProblem", message);
			}
		}

        Department fundingDepartment = io.getEffectiveFundingDept();
        if (fundingDepartment != null) {
        	form.setFundingDepartment(fundingDepartment.toString());
        } else {
        	form.setFundingDepartment(null);
        }


        if (ApplicationProperty.OfferingShowClassNotes.isTrue()) {
        	StringBuffer notes = new StringBuffer();
        	List<InstrOfferingConfig> configs = new ArrayList<InstrOfferingConfig>(io.getInstrOfferingConfigs());
        	Collections.sort(configs, new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	for (InstrOfferingConfig config: configs) {
        		List<SchedulingSubpart> subparts = new ArrayList<SchedulingSubpart>(config.getSchedulingSubparts());
        		Collections.sort(subparts, new SchedulingSubpartComparator());
        		for (SchedulingSubpart subpart: subparts) {
        			List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        			Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE));
        			for (Class_ clazz: classes) {
        				if (clazz.getNotes() != null && !clazz.getNotes().isEmpty()) {
        					notes.append("<tr><th valign='top' align='left' nowrap>" + subpart.getItypeDesc().trim() + " " + clazz.getSectionNumberString() + "</th><td>" + clazz.getNotes() + "</td></tr>");
        				}
        			}
        		}
        	}
        	if (notes.length() == 0) {
        		form.setNotes(io.getNotes());
        	} else {
        		form.setNotes(
        				"<table border='0' cellspacing='2' cellpadding='0'>" +
        				(io.getNotes() != null && !io.getNotes().isEmpty() ? "<tr><th valign='top' align='left' nowrap>" + io.getControllingCourseOffering().getCourseName() + "&nbsp;</th><td>" + io.getNotes() + "</td></tr>" : "") +
        				notes +
        				"</table>");
        	}
        } else {
        	form.setNotes(io.getNotes());
        }
        String coordinators = "";
        String instructorNameFormat = sessionContext.getUser().getProperty(UserProperty.NameFormat);
        List<OfferingCoordinator> coordinatorList = new ArrayList<OfferingCoordinator>(io.getOfferingCoordinators());
        Collections.sort(coordinatorList, new OfferingCoordinatorComparator(sessionContext));
        for (OfferingCoordinator coordinator: coordinatorList) {
        	if (!coordinators.isEmpty()) coordinators += "<br>";
        	coordinators += "<a href='instructorDetail.action?instructorId=" + coordinator.getInstructor().getUniqueId() + "' class='noFancyLinks'>" +
        			coordinator.getInstructor().getName(instructorNameFormat) +
        			(coordinator.getResponsibility() == null ? 
        					(coordinator.getPercentShare() != 0 ? " (" + coordinator.getPercentShare() + "%)" : "") :
        					" (" + coordinator.getResponsibility().getLabel() + (coordinator.getPercentShare() > 0 ? ", " + coordinator.getPercentShare() + "%" : "") + ")") + 
        			"</a>";
        }
        form.setCoordinators(coordinators);
        form.setTeachingRequests(false);
        if (sessionContext.hasPermission(Right.InstructorScheduling)) {
            for (DepartmentalInstructor di: io.getDepartment().getInstructors()) {
            	if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
            		form.setTeachingRequests(true);
            		break;
            	}
            }
        }
        
        for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();)
        	if (((InstrOfferingConfig)i.next()).isUnlimitedEnrollment().booleanValue()) {
        		form.setUnlimited(Boolean.TRUE); break;
        	}
        form.setNotOffered(io.isNotOffered());
        form.setCourseOfferings(offerings);
	    
        // Check limits on courses if cross-listed
        if (io.getCourseOfferings().size()>1 && !form.getUnlimited().booleanValue()) {
            int lim = 0;
            boolean reservationSet = false;
            for (CourseOffering course: io.getCourseOfferings()) {
            	if (course.getReservation() != null) {
            		lim += course.getReservation();
            		reservationSet = true;
            	}
            }
            
            if (reservationSet && io.getLimit()!=null && lim < io.getLimit().intValue()) {
                request.setAttribute("limitsDoNotMatch", ""+lim);
            }
        }
        
        // Check configuration limits
        TreeSet<InstrOfferingConfig> configsWithTooHighLimit = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(null));
        for (InstrOfferingConfig config: io.getInstrOfferingConfigs()) {
        	if (config.isUnlimitedEnrollment()) continue;
        	Integer subpartLimit = null;
        	for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
        		int limit = 0;
        		for (Class_ clazz: subpart.getClasses()) {
        			limit += (clazz.getMaxExpectedCapacity() == null ? clazz.getExpectedCapacity() : clazz.getMaxExpectedCapacity());
        		}
        		if (subpartLimit == null || subpartLimit > limit) subpartLimit = limit;
        	}
        	if (subpartLimit != null && subpartLimit < config.getLimit())
        		configsWithTooHighLimit.add(config);
        }
        if (!configsWithTooHighLimit.isEmpty()) {
        	if (configsWithTooHighLimit.size() == 1)
        		request.setAttribute("configsWithTooHighLimit", MSG.errorConfigWithTooHighLimit(configsWithTooHighLimit.first().getName()));
        	else {
        		String names = "";
        		for (InstrOfferingConfig config: configsWithTooHighLimit) {
        			if (!names.isEmpty()) names += ", ";
        			names += config.getName();
        		}
        		request.setAttribute("configsWithTooHighLimit", MSG.errorConfigsWithTooHighLimit(names));
        	}
        }
    
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
        
	    InstructionalOffering next = io.getNextInstructionalOffering(sessionContext);
        form.setNextId(next==null?null:next.getUniqueId().toString());
        InstructionalOffering previous = io.getPreviousInstructionalOffering(sessionContext);
        form.setPreviousId(previous==null?null:previous.getUniqueId().toString());
	    
		DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
        String html = tbl.getDistPrefsTableForInstructionalOffering(request, sessionContext, io);
        if (html!=null && html.indexOf(MSG.noPreferencesFound())<0)
        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
        
        form.setInstructorSurvey(InstructorCourseRequirement.hasRequirementsForOffering(io));
    }

    /**
     * Make an offering 'Not Offered'
     */
    private void doMakeNotOffered() throws Exception {
        
    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.OfferingMakeNotOffered);

        org.hibernate.Session hibSession = null;
        
        try {
		    InstructionalOfferingDAO idao = InstructionalOfferingDAO.getInstance();
		    hibSession = idao.getSession();
	        InstructionalOffering io = idao.get(form.getInstrOfferingId());
	        
	        io.deleteAllDistributionPreferences(hibSession);
            
	        // Delete all classes only - config stays
            io.deleteAllClasses(hibSession);
            
            for (Iterator i=io.getCourseOfferings().iterator();i.hasNext();) {
                CourseOffering co = (CourseOffering)i.next();
                Event.deleteFromEvents(hibSession, co);
                Exam.deleteFromExams(hibSession, co);
            }
            
            Event.deleteFromEvents(hibSession, io);
            Exam.deleteFromExams(hibSession, io);
            
            for (Iterator<Reservation> i = io.getReservations().iterator(); i.hasNext(); ) {
            	Reservation r = i.next();
            	hibSession.remove(r);
            	i.remove();
            }
            
            for (Iterator<TeachingRequest> i = io.getTeachingRequests().iterator(); i.hasNext(); ) {
            	TeachingRequest tr = i.next();
            	hibSession.remove(tr);
            	i.remove();
            }
            
            // Set flag to not offered
            io.setNotOffered(Boolean.valueOf(true));
            
            hibSession.merge(io);

            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    io, 
                    ChangeLog.Source.MAKE_NOT_OFFERED, 
                    ChangeLog.Operation.UPDATE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);

            // Unlock the offering, if needed
            if (sessionContext.hasPermission(io, Right.OfferingCanUnlock))
            	io.getSession().unlockOffering(io, sessionContext.getUser());

            hibSession.flush();
            hibSession.clear();
            
        	String className = ApplicationProperty.ExternalActionInstructionalOfferingNotOffered.value();
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingNotOfferedAction notOfferedAction = (ExternalInstructionalOfferingNotOfferedAction) (Class.forName(className).getDeclaredConstructor().newInstance());
	       		notOfferedAction.performExternalInstructionalOfferingNotOfferedAction(io, hibSession);
        	}

            
            // Update Form 
            form.setNotOffered(io.isNotOffered());

        }
        catch (Exception e) {
			Debug.error(e);
            throw (e);
        }
    }

    /**
     * Make an offering 'Not Offered'
     */
    private void doMakeOffered() throws Exception {

    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.OfferingMakeOffered);

    	org.hibernate.Session hibSession = null;
        
        try {
		    InstructionalOfferingDAO idao = InstructionalOfferingDAO.getInstance();
		    hibSession = idao.getSession();
	        InstructionalOffering io = idao.get(form.getInstrOfferingId());
            
            // Set flag to offered
            io.setNotOffered(Boolean.valueOf(false));
            
            hibSession.merge(io);
            
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    io, 
                    ChangeLog.Source.MAKE_OFFERED, 
                    ChangeLog.Operation.UPDATE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);
            
            // Lock the offering, if needed
            if (sessionContext.hasPermission(io, Right.OfferingCanLock))
            	io.getSession().lockOffering(io.getUniqueId());

            hibSession.flush();
            hibSession.clear();

        	String className = ApplicationProperty.ExternalActionInstructionalOfferingOffered.value();
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingOfferedAction offeredAction = (ExternalInstructionalOfferingOfferedAction) (Class.forName(className).getDeclaredConstructor().newInstance());
	       		offeredAction.performExternalInstructionalOfferingOfferedAction(io, hibSession);
        	}

            // Update Form 
            form.setNotOffered(io.isNotOffered());            
        }
        catch (Exception e) {
			Debug.error(e);
            throw (e);
        }
    }
    
    public String printTable() throws Exception {
    	WebInstrOfferingConfigTableBuilder ioTableBuilder = new WebInstrOfferingConfigTableBuilder();
		ioTableBuilder.setDisplayDistributionPrefs(false);
		ioTableBuilder.setDisplayConfigOpButtons(true);
		ioTableBuilder.setDisplayConflicts(true);
		ioTableBuilder.setDisplayDatePatternDifferentWarning(true);
		ioTableBuilder.htmlConfigTablesForInstructionalOffering(
									sessionContext,
									getClassAssignmentService().getAssignment(),
									getExaminationSolverService().getSolver(),
									form.getInstrOfferingId(), 
				    		        getPageContext().getOut(),
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
		return "";
	}
}
