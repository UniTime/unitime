/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructionalOfferingDetailForm;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingDeleteAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingNotOfferedAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingOfferedAction;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;


/** 
 * MyEclipse Struts
 * Creation date: 03-20-2006
 * 
 * XDoclet definition:
 * @struts:action path="/instructionalOfferingConfigDetail" name="instructionalOfferingConfigDetailForm" input="/user/instructionalOfferingConfigDetail.jsp" scope="request"
 *
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Service("/instructionalOfferingDetail")
public class InstructionalOfferingDetailAction extends Action {
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
        InstructionalOfferingDetailForm frm = (InstructionalOfferingDetailForm) form;
        
        // Read Parameters
        String op = (request.getAttribute("op") != null ? request.getAttribute("op").toString() :
        	request.getParameter("op") != null ? request.getParameter("op") :
        	frm.getOp() != null && !frm.getOp().isEmpty() ? frm.getOp() :
        	request.getParameter("hdnOp"));
        
		// Check operation
		if(op==null || op.trim().length()==0)
		    throw new Exception (MSG.exceptionOperationNotInterpreted() + op);
		
		if ("n".equals(request.getParameter("confirm")))
			op = rsc.getMessage("op.view");

		Debug.debug ("Op: " + op);

		// Delete insructional offering
		if(op.equals(MSG.actionDeleteIO()) && request.getAttribute("cfgDelete") == null) {
			
	    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.OfferingDelete);

			doDelete(request, frm);
			
			sessionContext.removeAttribute(SessionAttribute.OfferingsCourseNumber);
			
	        return mapping.findForward("showInstructionalOfferings");
		}
		
		// Display detail - default
		if(op.equals(rsc.getMessage("op.view"))
		       // || op.equals(rsc.getMessage("button.createClasses"))
		        || op.equals(MSG.actionUpdateConfiguration()) 
		        || op.equals(MSG.actionSaveConfiguration()) 
		        || op.equals(MSG.actionDeleteConfiguration())
		        || op.equals(MSG.actionUnassignAllInstructorsFromConfig()) ) {
			
		    String instrOfferingId = (request.getParameter("io")==null)
		    							? (request.getAttribute("io")==null)
		    							        ? null
		    							        : request.getAttribute("io").toString()
		    							: request.getParameter("io");
		    if (instrOfferingId==null && frm.getInstrOfferingId()!=null)
		    	instrOfferingId=frm.getInstrOfferingId().toString();
			if(instrOfferingId==null || instrOfferingId.trim().length()==0)
			    throw new Exception (MSG.exceptionIODataNotCorrect() + instrOfferingId);
			else  {

		    	sessionContext.checkPermission(instrOfferingId, "InstructionalOffering", Right.InstructionalOfferingDetail);

		    	doLoad(request, frm, instrOfferingId);
			}
			
			BackTracker.markForBack(
					request,
					"instructionalOfferingDetail.do?io="+frm.getInstrOfferingId(),
					MSG.backInstructionalOffering(frm.getInstrOfferingNameNoTitle()),
					true, false);
			
			return mapping.findForward("showConfigDetail");
	        
		}

		// Add Configuration
		if(op.equals(MSG.actionAddConfiguration())) {
			
	    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.InstrOfferingConfigAdd);

		    // Redirect to config edit
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
		    request.setAttribute("uid",io.getControllingCourseOffering().getUniqueId().toString());
		    return mapping.findForward("modifyConfig");
		}
		
		// Make Offering 'Offered'
		if(op.equals(MSG.actionMakeOffered())) {
			
	    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.OfferingMakeOffered);

		    doMakeOffered(request, frm);
		    
		    // Redirect to config edit
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
		    request.setAttribute("uid",io.getControllingCourseOffering().getUniqueId().toString());
		    return mapping.findForward("modifyConfig");
		}
		
		// Make Offering 'Not Offered'
		if(op.equals(MSG.actionMakeNotOffered())) {
	    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.OfferingMakeNotOffered);

	    	doMakeNotOffered(request, frm);
	    	if ("false".equals(ApplicationProperties.getProperty("unitime.offering.makeNotOfferedStaysOnDetail", "false"))) {
	    		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showInstructionalOfferings"));
                redirect.setAnchor("A" + frm.getInstrOfferingId());
                return redirect;
	    	} else {
	    		response.sendRedirect(response.encodeURL("instructionalOfferingDetail.do?io="+frm.getInstrOfferingId()));
	        	return null;
	    	}
		}
		
		// Change controlling course, add other offerings
		if(op.equals(MSG.actionCrossLists())) {
	    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.InstructionalOfferingCrossLists);

		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
		    request.setAttribute("uid",io.getControllingCourseOffering().getUniqueId().toString());
		    return mapping.findForward("modifyCrossLists");
		}
		
        if (op.equals(MSG.actionNextIO())) {
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.do?io="+frm.getNextId()));
        	return null;
        }
        
        if (op.equals(MSG.actionPreviousIO())) {
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.do?io="+frm.getPreviousId()));
        	return null;
        }
        
        if (op.equals(MSG.actionLockIO())) {
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());

	    	sessionContext.checkPermission(io, Right.OfferingCanLock);

	    	io.getSession().lockOffering(io.getUniqueId());
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.do?io="+io.getUniqueId()));
        	return null;
        }
		
        if (op.equals(MSG.actionUnlockIO())) {
	    	InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());

	    	sessionContext.checkPermission(io, Right.OfferingCanUnlock);

	        io.getSession().unlockOffering(io, sessionContext.getUser());
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.do?io="+io.getUniqueId()));
        	return null;
        }
        
    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.InstructionalOfferingDetail);

        BackTracker.markForBack(
				request,
				"instructionalOfferingDetail.do?io="+frm.getInstrOfferingId(),
				MSG.backInstructionalOffering(frm.getInstrOfferingName()),
				true, false);
        
		// Go back to instructional offerings
        return mapping.findForward("showInstructionalOfferings");
        
    }

    /**
     * Delete Instructional Offering
	 * @param request
	 * @param frm
	 */
	private void doDelete(
			HttpServletRequest request, 
			InstructionalOfferingDetailForm frm) throws Exception {
		
        org.hibernate.Session hibSession = null;
        Transaction tx = null;
        
        try {
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());

	        hibSession = idao.getSession();
	        tx = hibSession.beginTransaction();

			io.deleteAllDistributionPreferences(hibSession);
            Event.deleteFromEvents(hibSession, io);
	        Exam.deleteFromExams(hibSession, io);
        	String className = ApplicationProperties.getProperty("tmtbl.external.instr_offr.delete_action.class");
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingDeleteAction deleteAction = (ExternalInstructionalOfferingDeleteAction) (Class.forName(className).newInstance());
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
            	hibSession.delete(co);
            }
	        hibSession.delete(io);
	        
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

	/**
     * Loads the form initially
     * @param request
     * @param frm
     * @param instrOfferingIdStr
     */
    private void doLoad(
            HttpServletRequest request, 
            InstructionalOfferingDetailForm frm, 
            String instrOfferingIdStr) throws Exception {
        
        // Load Instr Offering
        Long instrOfferingId = new Long(instrOfferingIdStr);
        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
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
        frm.setInstrOfferingId(instrOfferingId);
        frm.setSubjectAreaId(subjectAreaId);
        frm.setInstrOfferingName(io.getCourseNameWithTitle());
        frm.setSubjectAreaAbbr(io.getControllingCourseOffering().getSubjectAreaAbbv());
        frm.setCourseNbr(io.getControllingCourseOffering().getCourseNbr());
        frm.setInstrOfferingNameNoTitle(io.getCourseName());
        frm.setCtrlCrsOfferingId(io.getControllingCourseOffering().getUniqueId());
        frm.setDemand(io.getDemand());
        frm.setEnrollment(io.getEnrollment());
        frm.setProjectedDemand(io.getProjectedDemand());
        frm.setLimit(io.getLimit());
        frm.setUnlimited(Boolean.FALSE);
        frm.setCreditText((io.getCredit() != null)?io.getCredit().creditText():"");
        frm.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(io)));
        frm.setByReservationOnly(io.isByReservationOnly());
        frm.setWkEnroll(io.getLastWeekToEnroll() == null ? "" : io.getLastWeekToEnroll().toString());
        frm.setWkChange(io.getLastWeekToChange() == null ? "" : io.getLastWeekToChange().toString());
        frm.setWkDrop(io.getLastWeekToDrop() == null ? "" : io.getLastWeekToDrop().toString());
        frm.setWeekStartDayOfWeek(Localization.getDateFormat("EEEE").format(io.getSession().getSessionBeginDateTime()));
        String coordinators = "";
        String instructorNameFormat = sessionContext.getUser().getProperty(UserProperty.NameFormat);
        for (DepartmentalInstructor instructor: new TreeSet<DepartmentalInstructor>(io.getCoordinators())) {
        	if (!coordinators.isEmpty()) coordinators += "<br>";
        	coordinators += "<a href='instructorDetail.do?instructorId=" + instructor.getUniqueId() + "' class='noFancyLinks'>" +
        			instructor.getName(instructorNameFormat) + 
        			"</a>";
        }
        frm.setCoordinators(coordinators);
        
        for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();)
        	if (((InstrOfferingConfig)i.next()).isUnlimitedEnrollment().booleanValue()) {
        		frm.setUnlimited(Boolean.TRUE); break;
        	}
        frm.setNotOffered(io.isNotOffered());
        frm.setCourseOfferings(offerings);
	    
        // Check limits on courses if cross-listed
        if (io.getCourseOfferings().size()>1 && !frm.getUnlimited().booleanValue()) {
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
        			limit += clazz.getExpectedCapacity();
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
        String linkLookupClass = ApplicationProperties.getProperty("tmtbl.catalogLink.lookup.class");
        if (linkLookupClass!=null && linkLookupClass.trim().length()>0) {
        	ExternalLinkLookup lookup = (ExternalLinkLookup) (Class.forName(linkLookupClass).newInstance());
       		Map results = lookup.getLink(io);
            if (results==null)
                throw new Exception (lookup.getErrorMessage());
            
            frm.setCatalogLinkLabel((String)results.get(ExternalLinkLookup.LINK_LABEL));
            frm.setCatalogLinkLocation((String)results.get(ExternalLinkLookup.LINK_LOCATION));
        }
        
	    InstructionalOffering next = io.getNextInstructionalOffering(sessionContext);
        frm.setNextId(next==null?null:next.getUniqueId().toString());
        InstructionalOffering previous = io.getPreviousInstructionalOffering(sessionContext);
        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
	    
		DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
        String html = tbl.getDistPrefsTableForInstructionalOffering(request, sessionContext, io);
        if (html!=null && html.indexOf(MSG.noPreferencesFound())<0)
        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);	    
        
    }

    /**
     * Make an offering 'Not Offered'
     * @param request
     * @param frm
     */
    private void doMakeNotOffered(
            HttpServletRequest request, 
            InstructionalOfferingDetailForm frm) throws Exception {
        
    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.OfferingMakeNotOffered);

        org.hibernate.Session hibSession = null;
        
        try {
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
		    hibSession = idao.getSession();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
	        
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
            
            // Set flag to not offered
            io.setNotOffered(new Boolean(true));
            
            idao.saveOrUpdate(io);

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
            
        	String className = ApplicationProperties.getProperty("tmtbl.external.instr_offr.not_offered_action.class");
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingNotOfferedAction notOfferedAction = (ExternalInstructionalOfferingNotOfferedAction) (Class.forName(className).newInstance());
	       		notOfferedAction.performExternalInstructionalOfferingNotOfferedAction(io, hibSession);
        	}

            
            // Update Form 
            frm.setNotOffered(io.isNotOffered());

        }
        catch (Exception e) {
			Debug.error(e);
            throw (e);
        }
    }

    /**
     * Make an offering 'Not Offered'
     * @param request
     * @param frm
     */
    private void doMakeOffered(
            HttpServletRequest request, 
            InstructionalOfferingDetailForm frm) throws Exception {

    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.OfferingMakeOffered);

    	org.hibernate.Session hibSession = null;
        
        try {
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
		    hibSession = idao.getSession();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
            
            // Set flag to offered
            io.setNotOffered(new Boolean(false));
            
            idao.saveOrUpdate(io);
            
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

        	String className = ApplicationProperties.getProperty("tmtbl.external.instr_offr.offered_action.class");
        	if (className != null && className.trim().length() > 0){
	        	ExternalInstructionalOfferingOfferedAction offeredAction = (ExternalInstructionalOfferingOfferedAction) (Class.forName(className).newInstance());
	       		offeredAction.performExternalInstructionalOfferingOfferedAction(io, hibSession);
        	}

            // Update Form 
            frm.setNotOffered(io.isNotOffered());            
        }
        catch (Exception e) {
			Debug.error(e);
            throw (e);
        }
    }

}
