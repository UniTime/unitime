/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.InstructionalOfferingDetailForm;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;
import org.unitime.timetable.webutil.ReservationsTableBuilder;


/** 
 * MyEclipse Struts
 * Creation date: 03-20-2006
 * 
 * XDoclet definition:
 * @struts:action path="/instructionalOfferingConfigDetail" name="instructionalOfferingConfigDetailForm" input="/user/instructionalOfferingConfigDetail.jsp" scope="request"
 */
public class InstructionalOfferingDetailAction extends Action {

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

        if(!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        HttpSession httpSession = request.getSession();
        MessageResources rsc = getResources(request);
        ActionMessages errors = new ActionMessages();
        InstructionalOfferingDetailForm frm = (InstructionalOfferingDetailForm) form;
        User user = Web.getUser(httpSession);
        
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
		    throw new Exception ("Operation could not be interpreted: " + op);
		
		if ("n".equals(request.getParameter("confirm")))
			op = rsc.getMessage("op.view");

		Debug.debug ("Op: " + op);

		// Delete insructional offering
		if(op.equals(rsc.getMessage("button.deleteIo"))
				&& request.getAttribute("cfgDelete")==null) {
			doDelete(request, frm);
			
			if (httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
				httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, null);
			
	        return mapping.findForward("showInstructionalOfferings");
		}
		
		// Display detail - default
		if(op.equals(rsc.getMessage("op.view"))
		        || op.equals(rsc.getMessage("button.createClasses")) 
		        || op.equals(rsc.getMessage("button.updateConfig")) 
		        || op.equals(rsc.getMessage("button.saveConfig")) 
		        || op.equals(rsc.getMessage("button.deleteConfig"))
		        || op.equals(rsc.getMessage("button.unassignAll")) ) {
		    String instrOfferingId = (request.getParameter("io")==null)
		    							? (request.getAttribute("io")==null)
		    							        ? null
		    							        : request.getAttribute("io").toString()
		    							: request.getParameter("io");
		    if (instrOfferingId==null && frm.getInstrOfferingId()!=null)
		    	instrOfferingId=frm.getInstrOfferingId().toString();
			if(instrOfferingId==null || instrOfferingId.trim().length()==0)
			    throw new Exception ("Instructional Offering data was not correct: " + instrOfferingId);
			else  {
			    doLoad(request, frm, instrOfferingId);
			}
			
			BackTracker.markForBack(
					request,
					"instructionalOfferingDetail.do?io="+frm.getInstrOfferingId(),
					"Instructional Offering ("+frm.getInstrOfferingNameNoTitle()+")",
					true, false);
			
			return mapping.findForward("showConfigDetail");
	        
		}

		// Add Configuration
		if(op.equals(rsc.getMessage("button.addConfig"))) {
		    // Redirect to config edit
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
		    request.setAttribute("uid",io.getControllingCourseOffering().getUniqueId().toString());
		    return mapping.findForward("modifyConfig");
		}
		
		// Make Offering 'Offered'
		if(op.equals(rsc.getMessage("button.makeOffered"))) {
		    doMakeOffered(request, frm);
		    
		    // Redirect to config edit
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
		    request.setAttribute("uid",io.getControllingCourseOffering().getUniqueId().toString());
		    return mapping.findForward("modifyConfig");
		}
		
		// Make Offering 'Not Offered'
		if(op.equals(rsc.getMessage("button.makeNotOffered"))) {
		    doMakeNotOffered(request, frm);
	        return mapping.findForward("showInstructionalOfferings");
		}
		
		// Change controlling course, add other offerings
		if(op.equals(rsc.getMessage("button.crossLists"))) {
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
		    request.setAttribute("uid",io.getControllingCourseOffering().getUniqueId().toString());
		    return mapping.findForward("modifyCrossLists");
		}
		
        if (op.equals(rsc.getMessage("button.nextInstructionalOffering"))) {
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.do?io="+frm.getNextId()));
        	return null;
        }
        
        if (op.equals(rsc.getMessage("button.previousInstructionalOffering"))) {
        	response.sendRedirect(response.encodeURL("instructionalOfferingDetail.do?io="+frm.getPreviousId()));
        	return null;
        }
		
		BackTracker.markForBack(
				request,
				"instructionalOfferingDetail.do?io="+frm.getInstrOfferingId(),
				"Instructional Offering ("+frm.getInstrOfferingName()+")",
				true, false);
		
		// Add Reservation
		if(op.equals(rsc.getMessage("button.addReservation"))) {
		    
		    //TODO Reservations Bypass - to be removed later
	        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(frm.getInstrOfferingId());
		    request.setAttribute("ownerId", frm.getInstrOfferingId());
		    request.setAttribute("ownerName", io.getCourseNameWithTitle());
		    request.setAttribute("ownerType", Constants.RESV_OWNER_IO);
		    request.setAttribute("ownerTypeLabel", Constants.RESV_OWNER_IO_LBL);
		    return mapping.findForward("displayCourseReservation");		    
		    // End Bypass
		    
	        //TODO Reservations - functionality to be made visible later
		    /*
		    request.setAttribute("ownerId", frm.getInstrOfferingId().toString());
		    request.setAttribute("ownerClassId", Constants.RESV_OWNER_IO);
		    return mapping.findForward("addReservation");
		    */
		}
		
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
	        io.deleteAllReservations(hibSession);
            Event.deleteFromEvents(hibSession, io);
	        Exam.deleteFromExams(hibSession, io);
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
        
        HttpSession httpSession = request.getSession();
        User user = Web.getUser(httpSession);

        // Load Instr Offering
        Long instrOfferingId = new Long(instrOfferingIdStr);
        InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
        InstructionalOffering io = idao.get(instrOfferingId);
        Long subjectAreaId = io.getControllingCourseOffering().getSubjectArea().getUniqueId();
        
	    // Set Session Variables
        httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, subjectAreaId.toString());
        if (httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null
                && httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString().length()>0)
            httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, io.getControllingCourseOffering().getCourseNbr());
        
        // Get Configuration
        TreeSet ts = new TreeSet();
        ts.add(io);
	    
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
        frm.setProjectedDemand(io.getProjectedDemand());
        frm.setLimit(io.getLimit());
        frm.setUnlimited(Boolean.FALSE);
        frm.setDesignatorRequired(io.isDesignatorRequired());
        frm.setCreditText((io.getCredit() != null)?io.getCredit().creditText():"");
        
        if (io.getConsentType()==null)
            frm.setConsentType("None Required");
        else
            frm.setConsentType(io.getConsentType().getLabel());
        
        for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();)
        	if (((InstrOfferingConfig)i.next()).isUnlimitedEnrollment().booleanValue()) {
        		frm.setUnlimited(Boolean.TRUE); break;
        	}
        frm.setNotOffered(io.isNotOffered());
        frm.setCourseOfferings(offerings);
	    frm.setIsEditable(new Boolean(io.isEditableBy(user)));
	    frm.setIsFullyEditable(new Boolean(io.getControllingCourseOffering().isFullyEditableBy(user)));
	    frm.setIsManager(new Boolean(io.getControllingCourseOffering().isEditableBy(user)));
	    
        // Check limits on courses if cross-listed
        if (io.getCourseOfferings().size()>1 && !frm.getUnlimited().booleanValue()) {
            Set resvs = io.getCourseReservations();
            int lim = 0;
            for (Iterator i = resvs.iterator(); i.hasNext(); ) {
                CourseOfferingReservation cor = (CourseOfferingReservation) i.next();
                lim += cor.getReserved().intValue();
            }
            
            if (io.getLimit()!=null && lim!=io.getLimit().intValue()) {
                request.setAttribute("limitsDoNotMatch", ""+lim);
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
        
	    InstructionalOffering next = io.getNextInstructionalOffering(request.getSession(), Web.getUser(request.getSession()), false, true);
        frm.setNextId(next==null?null:next.getUniqueId().toString());
        InstructionalOffering previous = io.getPreviousInstructionalOffering(request.getSession(), Web.getUser(request.getSession()), false, true);
        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
	    
		DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
        String html = tbl.getDistPrefsTableForInstructionalOffering(request, io, true);
        if (html!=null && html.indexOf("No preferences found")<0)
        	request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);	    
        
        ReservationsTableBuilder resvTbl = new ReservationsTableBuilder();
        String resvHtml = resvTbl.htmlTableForInstructionalOffering(
                user, io, true, true, true, true,
                true, true, true, true, true );
        if (resvHtml!=null) 
        	request.setAttribute(Reservation.RESV_REQUEST_ATTR, resvHtml);	    
            
    }

    /**
     * Make an offering 'Not Offered'
     * @param request
     * @param frm
     */
    private void doMakeNotOffered(
            HttpServletRequest request, 
            InstructionalOfferingDetailForm frm) throws Exception {
        
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
                    request, 
                    io, 
                    ChangeLog.Source.MAKE_NOT_OFFERED, 
                    ChangeLog.Operation.UPDATE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);

            hibSession.flush();
            hibSession.clear();
            
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
                    request, 
                    io, 
                    ChangeLog.Source.MAKE_OFFERED, 
                    ChangeLog.Operation.UPDATE, 
                    io.getControllingCourseOffering().getSubjectArea(),
                    null);
            
            hibSession.flush();
            hibSession.clear();
            
            // Update Form 
            frm.setNotOffered(io.isNotOffered());
        }
        catch (Exception e) {
			Debug.error(e);
            throw (e);
        }
    }

}
