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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ReservationEditForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * MyEclipse Struts
 * Creation date: 08-30-2006
 * 
 * XDoclet definition:
 * @struts:action path="/reservationEdit" name="reservationEditForm" input="/user/reservationEdit.jsp" scope="request"
 */
public class ReservationEditAction extends ReservationAction {

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
        User user = Web.getUser(request.getSession());        
        ReservationEditForm frm = (ReservationEditForm) form;
	    ActionMessages errors = null;
        String[] excludeList = null;
        
        // Get operation
        String op = (request.getParameter("op")==null) 
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");		        
        			        
        if(op==null || op.trim().length()==0)
            throw new Exception ("Invalid operation");

        frm.setOp(op);

       // Reservation class selected - redirect to appropriate reservation edit screen
        if ( op.equals(rsc.getMessage("button.reservationNextStep")) ) {
            
    	    // Validate data
		    errors = frm.validate(mapping, request);
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    return mapping.findForward("displayReservationList");
		    }    	    
    	    
            String resvClass = frm.getReservationClass();
            if (frm.getOwnerName()==null) {
                doLoad(request, frm, frm.getOwnerId().toString(), frm.getOwnerType());
                request.setAttribute("ownerName", frm.getOwnerName());
                request.setAttribute("ownerTypeLabel", frm.getOwnerTypeLabel());
                request.setAttribute("ioLimit", frm.getIoLimit());
                request.setAttribute("crsLimit", frm.getCrsLimit());
                request.setAttribute("unlimited", frm.getUnlimited());
            }

            if (resvClass.equalsIgnoreCase(Constants.RESV_ACAD_AREA)) 
                return mapping.findForward("displayAcadAreaReservation");

            if (resvClass.equalsIgnoreCase(Constants.RESV_COURSE)) 
                return mapping.findForward("displayCourseReservation");
            
            if (resvClass.equalsIgnoreCase(Constants.RESV_POS)) 
                return mapping.findForward("displayPosReservation");
            
            if (resvClass.equalsIgnoreCase(Constants.RESV_INDIVIDUAL)) 
                return mapping.findForward("displayIndividualReservation");
            
            if (resvClass.equalsIgnoreCase(Constants.RESV_STU_GROUP)) 
                return mapping.findForward("displayStudentGroupReservation");         
        }

        // Add Reservation - from reservation list screen
        if ( op.equals(rsc.getMessage("button.addReservationIo")) ) {
            
    	    // Validate data
		    errors = frm.validate(mapping, request);
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    return mapping.findForward("displayReservationList");
		    }    	    
    	    
    		String courseNbr = frm.getCourseNbr();
    	    String subjAreaId = frm.getSubjectAreaId();

    	    // Offering does not exist - redirect back
            Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
    	    List l = CourseOffering.search(sessionId, subjAreaId, courseNbr);
    	    if(l==null || l.size()==0) {
			    errors.add("courseNbr", 
			            	new ActionMessage("errors.generic", "Instructional Offering does not exist."));
			    saveErrors(request, errors);
			    request.setAttribute("addNewError", "true");
			    return mapping.findForward("displayReservationList");
    	    }

            //TODO Reservations - functionality to be removed later
            CourseOffering co = (CourseOffering) l.get(0);
            InstructionalOffering io = co.getInstructionalOffering();
            if (io.getCourseOfferings()==null || io.getCourseOfferings().size()<=1) {
			    errors.add("courseNbr", 
		            	new ActionMessage("errors.generic", "No reservations can be set on the instructional offering as it does not have cross-listed courses."));
			    saveErrors(request, errors);
			    request.setAttribute("addNewError", "true");
			    return mapping.findForward("displayReservationList");
            }

    	    // Set Session Variables
            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, subjAreaId);
            httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, courseNbr);        

            request.setAttribute("ownerId", io.getUniqueId());
            request.setAttribute("ownerName", io.getCourseNameWithTitle());
            request.setAttribute("ownerType", Constants.RESV_OWNER_IO);
            request.setAttribute("ownerTypeLabel", Constants.RESV_OWNER_IO_LBL);
            
            return mapping.findForward("displayCourseReservation");
            
            // End Bypass
            /*
    	    // Set Session Variables
            httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, subjAreaId);
            httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, courseNbr);        
    	    
            // Set owner type and class
            CourseOffering co = (CourseOffering) l.get(0);
            InstructionalOffering io = co.getInstructionalOffering();
            frm.setOwnerId(io.getUniqueId());
            frm.setOwnerName(io.getCourseNameWithTitle());
            frm.setOwnerType(Constants.RESV_OWNER_IO);
            frm.setOwnerTypeLabel(Constants.RESV_OWNER_IO_LBL);
            if (io.getCourseOfferings()==null || io.getCourseOfferings().size()<=1) {
                excludeList = new String[] { Constants.RESV_COURSE };
            }
            */
        }

        // Reservation class selected - redirect to appropriate reservation edit screen
        if ( op.equals(rsc.getMessage("button.addReservation")) ) {
            String ownerId = request.getAttribute("ownerId") != null 
            					? (String) request.getAttribute("ownerId")
            					: request.getParameter("ownerId");
            String ownerClassId = request.getAttribute("ownerClassId") != null 
								? (String) request.getAttribute("ownerClassId")
								: request.getParameter("ownerClassId");
            
            if (ownerId==null || ownerClassId==null) {
                errors = new ActionMessages();
                errors.add("ownerId", 
                        new ActionMessage ("errors.generic", "Reservation owner could not be determined") );
                saveErrors(request, errors);
                return mapping.findForward("displayReservationList");
        	}
    
            excludeList = doLoad(request, frm, ownerId, ownerClassId);
        }
        
        // Set up lists
        LookupTables.setupReservationClass(request, excludeList);        
        
        return mapping.findForward("displayReservationDetail");
    }

    
    /**
     * Load form
     * @param frm
     * @param ownerId
     * @param ownerClassId
     * @return
     */
    private String[] doLoad(HttpServletRequest request, ReservationEditForm frm, String ownerId, String ownerClassId) {
        String[] excludeList = null;
        InstructionalOffering io = null;
        frm.setOwnerId(new Long(ownerId));
        frm.setOwnerType(ownerClassId);
        frm.setCrsLimit("-");

        if (ownerClassId.equals(Constants.RESV_OWNER_IO)) {
            frm.setOwnerTypeLabel(Constants.RESV_OWNER_IO_LBL);
            io = new InstructionalOfferingDAO().get(new Long(ownerId));
            frm.setOwnerName(io.getCourseNameWithTitle());
        }
        
        if (ownerClassId.equals(Constants.RESV_OWNER_CONFIG)) {
            frm.setOwnerTypeLabel(Constants.RESV_OWNER_CONFIG_LBL);
            InstrOfferingConfig config = new InstrOfferingConfigDAO().get(new Long(ownerId));
            io = config.getInstructionalOffering();
            frm.setOwnerName(config.getCourseNameWithTitle() + " - Configuration " + config.getName());
        }
        
        if (ownerClassId.equals(Constants.RESV_OWNER_CLASS)) {
            frm.setOwnerTypeLabel(Constants.RESV_OWNER_CLASS_LBL);
            Class_ c = new Class_DAO().get(new Long(ownerId));
            io = c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
            frm.setOwnerName(c.getClassLabelWithTitle());
        }

        //TODO Reservations - functionality to be removed later
        if (ownerClassId.equals(Constants.RESV_OWNER_COURSE)) {
            frm.setOwnerTypeLabel(Constants.RESV_OWNER_COURSE_LBL);
            CourseOffering crs = new CourseOfferingDAO().get(new Long(ownerId));
            io = crs.getInstructionalOffering();
            frm.setOwnerName(crs.getCourseNameWithTitle());

		    Collection ioResvs2 = io.getReservations(false, false, false, false, true);
	    	if (ioResvs2!=null && ioResvs2.size()>0) {
	    		for (Iterator it1=ioResvs2.iterator(); it1.hasNext(); ) {
	    			Object o = it1.next();
	    			if (o instanceof CourseOfferingReservation) {
	    				CourseOfferingReservation cor = (CourseOfferingReservation) o;
	    				if (cor.getCourseOffering().equals(crs)) {
	    					frm.setCrsLimit(cor.getReserved().toString());
	    					break;
	    				}
	    			}
	    		}
	    	}
        }
        //End Bypass

        if (io.getCourseOfferings()==null || io.getCourseOfferings().size()<=1) {
            excludeList = new String[] { Constants.RESV_COURSE };
        }
        
        frm.setIoLimit(io.getLimit()!=null ? io.getLimit().toString() : "-");
        frm.setUnlimited(io.hasUnlimitedEnrollment());
        
        Vector backList = BackTracker.getBackList(request.getSession());
        if (backList!=null && backList.size()>0) {
        	String[] lastBack = (String[])backList.elementAt(backList.size()-1);
        	if (lastBack[0].indexOf("reservationList")>=0 && lastBack[0].indexOf("#")<0) {
        		lastBack[0] += "#" +io.getUniqueId();
        	}
        }
        return excludeList;
    }
    
}