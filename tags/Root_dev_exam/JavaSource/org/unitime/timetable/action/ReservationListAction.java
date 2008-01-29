/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ReservationListForm;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.ReservationsTableBuilder;
import org.unitime.timetable.webutil.pdf.PdfReservationsTableBuilder;


/** 
 * MyEclipse Struts
 * Creation date: 08-30-2006
 * 
 * XDoclet definition:
 * @struts:action path="/reservationList" name="reservationListForm" input="/user/reservationList.jsp" scope="request"
 */
public class ReservationListAction extends Action {

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
        
	    HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        MessageResources rsc = getResources(request);
        User user = Web.getUser(httpSession);        
        ReservationListForm frm = (ReservationListForm) form;
	    ActionMessages errors = null;
        
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
            op = rsc.getMessage("op.view");

        // Set up Lists
        frm.setOp(op);
        frm.setSubjectAreas(TimetableManager.getSubjectAreas(user));
        
        // First access to screen
        if (op.equals(rsc.getMessage("op.view"))
                || op.equals(rsc.getMessage("button.saveReservation"))
                || op.equals(rsc.getMessage("button.updateReservation"))
                || op.equals(rsc.getMessage("button.deleteReservation")) 
                || op.equals(rsc.getMessage("button.addReservation")) 
                || op.equals(rsc.getMessage("button.reservationNextStep")) ) {
            
            setupFilters(frm, request);

            String subjectAreaId = (String) request.getAttribute("subjectAreaId");
        	if (subjectAreaId==null)
        	    subjectAreaId = request.getParameter("subjectAreaId");
        	if (subjectAreaId==null)
        	    subjectAreaId = (String) httpSession.getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME);
        	
    	    String courseNbr = (String) request.getAttribute("courseNbr");
        	if (courseNbr==null)
        	    courseNbr = request.getParameter("courseNbr");
        	if (courseNbr==null)
        	    courseNbr = (String) httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME);
        	if (courseNbr==null)
        	    courseNbr = "";

        	if (subjectAreaId!=null && subjectAreaId.trim().length()>0) {
	            frm.setSubjectAreaId(subjectAreaId);
	            frm.setCourseNbr(courseNbr);
	            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, frm.getSubjectAreaId());
	            httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, frm.getCourseNbr());
	            doSearch(frm, request, errors);
        	}
        	else {
	        	Set s = (Set) frm.getSubjectAreas();
		        if(s.size()==1) {
		            Debug.debug("Exactly 1 subject area found ... ");
		            frm.setSubjectAreaId(((SubjectArea) s.iterator().next()).getUniqueId().toString());
		            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, frm.getSubjectAreaId());
		            doSearch(frm, request, errors);
		        }
            }
	        
	        BackTracker.markForBack(request, "reservationList.do", "Reservation List", true, true);
            return mapping.findForward("displayReservationList");
        }
        
        // View Button Clicked / Export PDF
        if( op.equals(rsc.getMessage("button.displayReservationList")) 
        		|| op.equals(rsc.getMessage("button.exportPDF")) ) {

		    errors = frm.validate(mapping, request);
		    
		    // Validation fails
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    return mapping.findForward("displayReservationList");
		    }
            
            request.setAttribute("filterApplied", "1");
            setupFilters(frm, request);

            if (op.equals(rsc.getMessage("button.exportPDF"))) {
            	doExportToPdf (frm, request, errors);
            }
           	doSearch(frm, request, errors);
            
            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, frm.getSubjectAreaId());
            httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, frm.getCourseNbr());
            
	        BackTracker.markForBack(request, "reservationList.do", "Reservation List", true, true);
            return mapping.findForward("displayReservationList");
        }            
        
        // Add new reservation
        if( op.equals(rsc.getMessage("button.addReservationIo")) && 
                (request.getAttribute("addNewError")==null || !request.getAttribute("addNewError").toString().equals("true")) ) {
		    errors = frm.validate(mapping, request);
		    
		    // Validation fails
		    if(errors.size()>0) {
		        frm.setOp(null);
			    saveErrors(request, errors);
			    return mapping.findForward("displayReservationList");
		    }
            
	        BackTracker.markForBack(request, "reservationList.do", "Reservation List", true, true);
            return mapping.findForward("addReservation");
        }
        
        return mapping.findForward("displayReservationList");
   }

    /**
     * Export to Pdf
     * @param frm
     * @param request
     * @param errors
     */
    private void doExportToPdf (
    		ReservationListForm frm, 
    		HttpServletRequest request, 
    		ActionMessages errors) throws Exception {
    	
        try {
              boolean result = new PdfReservationsTableBuilder().pdfTableForSubjectArea(     
            		request,   
      				Web.getUser(request.getSession()),
			        frm.getSubjectAreaId(), 
			        frm.getCourseNbr(), 
			        frm.getIoResv().booleanValue(), 
			        frm.getConfigResv().booleanValue(), 
			        frm.getClassResv().booleanValue(),
			        frm.getCourseResv().booleanValue(),
			        frm.getIndResv().booleanValue(),
			        frm.getSgResv().booleanValue(),
			        frm.getAaResv().booleanValue(),
			        frm.getPosResv().booleanValue(),
			        frm.getCrsResv().booleanValue() );
              
       } 
        catch (Exception e) {
        	Debug.error(e);
        	throw new Exception ("Pdf export failed - " + e.getMessage());
        } 
	}

	/**
     * @param frm
     * @param request
     * @param errors
     */
    private void doSearch(ReservationListForm frm, HttpServletRequest request, ActionMessages errors) throws Exception {

        HttpSession httpSession = request.getSession();
        User user = Web.getUser(request.getSession());        

        String html = new ReservationsTableBuilder().htmlTableForSubjectArea(
                				user,
						        frm.getSubjectAreaId(), 
						        frm.getCourseNbr(), 
						        frm.getIoResv().booleanValue(), 
						        frm.getConfigResv().booleanValue(), 
						        frm.getClassResv().booleanValue(),
						        frm.getCourseResv().booleanValue(),
						        frm.getIndResv().booleanValue(),
						        frm.getSgResv().booleanValue(),
						        frm.getAaResv().booleanValue(),
						        frm.getPosResv().booleanValue(),
						        frm.getCrsResv().booleanValue() );
        
        request.setAttribute("reservationList", html);
	    if(html==null) {
	        if (errors==null)
	            errors = new ActionErrors();
	        
            errors.add("search", 
                    new ActionMessage(
                            "errors.generic", 
                            "No reservations found matching the search criteria and filter settings") );
            
		    saveErrors(request, errors);
	    }
    }

    /**
     * @param frm
     * @param request
     */
    private void setupFilters(ReservationListForm frm, HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        boolean filterSet = UserData.getPropertyBoolean(httpSession, "resvListFilter", false);
        String filterApplied = (String) request.getAttribute("filterApplied");

        if (filterApplied!=null && !filterApplied.equals("1"))
            filterApplied = null;
        
        if (!filterSet) {
            frm.setIoResv(new Boolean(true));
            frm.setConfigResv(new Boolean(true));
            frm.setClassResv(new Boolean(true));
            frm.setCourseResv(new Boolean(true));
            frm.setIndResv(new Boolean(true));
            frm.setSgResv(new Boolean(true));
            frm.setAaResv(new Boolean(true));
            frm.setPosResv(new Boolean(true));
            frm.setCrsResv(new Boolean(true));
        } 
        else if (filterApplied == null) {            
            frm.setIoResv(new Boolean(UserData.getPropertyBoolean(httpSession, "displayIoResv", true)));
            frm.setConfigResv(new Boolean(UserData.getPropertyBoolean(httpSession, "displayConfigResv", true)));
            frm.setClassResv(new Boolean(UserData.getPropertyBoolean(httpSession, "displayClassResv", true)));
            frm.setCourseResv(new Boolean(UserData.getPropertyBoolean(httpSession, "displayCourseResv", true)));
            frm.setIndResv(new Boolean(UserData.getPropertyBoolean(httpSession, "includeIndResv", true)));
            frm.setSgResv(new Boolean(UserData.getPropertyBoolean(httpSession, "includeSgResv", true)));
            frm.setAaResv(new Boolean(UserData.getPropertyBoolean(httpSession, "includeAaResv", true)));
            frm.setPosResv(new Boolean(UserData.getPropertyBoolean(httpSession, "includePosResv", true)));
            frm.setCrsResv(new Boolean(UserData.getPropertyBoolean(httpSession, "includeCrsResv", true)));
        }
        
        if (frm.getIoResv()==null)
            frm.setIoResv(new Boolean(false));
        if (frm.getConfigResv()==null)
            frm.setConfigResv(new Boolean(false));
        if (frm.getClassResv()==null)
            frm.setClassResv(new Boolean(false));
        if (frm.getCourseResv()==null)
            frm.setCourseResv(new Boolean(false));
        if (frm.getIndResv()==null)
            frm.setIndResv(new Boolean(false));
        if (frm.getSgResv()==null)
            frm.setSgResv(new Boolean(false));
        if (frm.getAaResv()==null)
            frm.setAaResv(new Boolean(false));
        if (frm.getPosResv()==null)
            frm.setPosResv(new Boolean(false));
        if (frm.getCrsResv()==null)
            frm.setCrsResv(new Boolean(false));
        
        // check that at least one filter is checked
        if (!frm.getIoResv().booleanValue() && !frm.getConfigResv().booleanValue() 
                && !frm.getClassResv().booleanValue() && !frm.getCourseResv().booleanValue()) {
            frm.setIoResv(new Boolean(true));
        }
        
        if (!frm.getIndResv().booleanValue() && !frm.getSgResv().booleanValue() 
                && !frm.getAaResv().booleanValue() && !frm.getPosResv().booleanValue() && !frm.getCrsResv().booleanValue() ) {
            frm.setAaResv(new Boolean(true));
            frm.setCrsResv(new Boolean(true));
        }
        
        UserData.setPropertyBoolean(httpSession, "resvListFilter", true);
        UserData.setPropertyBoolean(httpSession, "displayIoResv", frm.getIoResv().booleanValue());
        UserData.setPropertyBoolean(httpSession, "displayConfigResv", frm.getConfigResv().booleanValue());
        UserData.setPropertyBoolean(httpSession, "displayClassResv", frm.getClassResv().booleanValue());        
        UserData.setPropertyBoolean(httpSession, "displayCourseResv", frm.getCourseResv().booleanValue());        
        UserData.setPropertyBoolean(httpSession, "includeIndResv", frm.getIndResv().booleanValue());        
        UserData.setPropertyBoolean(httpSession, "includeSgResv", frm.getSgResv().booleanValue());        
        UserData.setPropertyBoolean(httpSession, "includeAaResv", frm.getAaResv().booleanValue());        
        UserData.setPropertyBoolean(httpSession, "includePosResv", frm.getPosResv().booleanValue());        
        UserData.setPropertyBoolean(httpSession, "includeCrsResv", frm.getCrsResv().booleanValue());        
    }

}