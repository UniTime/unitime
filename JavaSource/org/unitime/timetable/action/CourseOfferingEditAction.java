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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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
import org.hibernate.impl.SessionImpl;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.CourseOfferingEditForm;
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
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
public class CourseOfferingEditAction extends Action {

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
		    throw new Exception ("Operation could not be interpreted: " + op);

		Debug.debug ("Op: " + op);

	    //TODO Reservations functionality to be removed later
		if(op.equals(rsc.getMessage("button.addReservation")) ) {
		    String courseOfferingId = request.getParameter("courseOfferingId");
	        CourseOffering co = new CourseOfferingDAO().get(new Long(courseOfferingId));

		    request.setAttribute("ownerId", co.getUniqueId() );
		    request.setAttribute("ownerName", co.getCourseNameWithTitle() );
		    request.setAttribute("ownerType", Constants.RESV_OWNER_COURSE);
		    request.setAttribute("ownerTypeLabel", Constants.RESV_OWNER_COURSE_LBL);
		    
		    InstructionalOffering io = co.getInstructionalOffering();
            request.setAttribute("ioLimit", io.getLimit()!=null ? io.getLimit().toString() : null);
            request.setAttribute("unlimited", io.hasUnlimitedEnrollment());

		    Collection ioResvs2 = io.getReservations(false, false, false, false, true);
	    	if (ioResvs2!=null && ioResvs2.size()>0) {
	    		for (Iterator it1=ioResvs2.iterator(); it1.hasNext(); ) {
	    			Object o = it1.next();
	    			if (o instanceof CourseOfferingReservation) {
	    				CourseOfferingReservation cor = (CourseOfferingReservation) o;
	    				if (cor.getCourseOffering().equals(co)) {
	    					request.setAttribute("crsLimit", cor.getReserved().toString());
	    					break;
	    				}
	    			}
	    		}
	    	}
            return mapping.findForward("displayAcadAreaReservation");
		}
	    // End

		if(op.equals(rsc.getMessage("button.editCourseOffering")) ) {

		    String courseOfferingId = (request.getParameter("courseOfferingId")==null)
		    							? (request.getAttribute("courseOfferingId")==null)
		    							        ? null
		    							        : request.getAttribute("courseOfferingId").toString()
		    							: request.getParameter("courseOfferingId");

		    if (courseOfferingId==null && frm.getCourseOfferingId()!=null) {
		        courseOfferingId=frm.getCourseOfferingId().toString();
		    }

			if(courseOfferingId==null || courseOfferingId.trim().length()==0) {
			    throw new Exception ("Course Offering data was not correct: " + courseOfferingId);
			}
			else  {
			    doLoad(request, frm, courseOfferingId);
			}

			return mapping.findForward("displayCourseOffering");
		}

		if(op.equals(rsc.getMessage("button.updateCourseOffering")) ) {
			errors = frm.validate(mapping, request);
			if (errors.size() == 0) {
			    doUpdate(request, frm);

			    String cn = (String) httpSession.getAttribute(Constants.CRS_NBR_ATTR_NAME);
				if (cn!=null)
					httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, frm.getCourseNbr());

			    // Redirect to instr offering detail on success
	            request.setAttribute("io", frm.getInstrOfferingId());
	            return mapping.findForward("instructionalOfferingDetail");
			}
			else {
				saveErrors(request, errors);
			    doReload(request, frm);
			}
		}

        return mapping.findForward("displayCourseOffering");
    }

    /**
     * @param request
     * @param frm
     */
    private void doUpdate(HttpServletRequest request, CourseOfferingEditForm frm) throws Exception {

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
	        co.setScheduleBookNote(note);

	        if (frm.getDemandCourseOfferingId()==null) {
	        	co.setDemandOffering(null);
	        } else {
	        	CourseOffering dco = cdao.get(frm.getDemandCourseOfferingId(),hibSession);
	        	co.setDemandOffering(dco==null?null:dco);
	        }

	        // Update designator required and consent only if course is controlling
	        InstructionalOffering io = null;
	        if (co.isIsControl().booleanValue()) {
		        io = co.getInstructionalOffering();

		        if (frm.getConsent()==null || frm.getConsent().intValue()<=0)
		            io.setConsentType(null);
		        else {
		            OfferingConsentType oct = odao.get(frm.getConsent());
		            io.setConsentType(oct);
		        }

		        if (frm.getDesignatorRequired()==null)
		            io.setDesignatorRequired(new Boolean(false));
		        else {
		            io.setDesignatorRequired(frm.getDesignatorRequired());
		        }

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


		        hibSession.update(io);
	        }

	        hibSession.saveOrUpdate(co);

            ChangeLog.addChange(
                    hibSession,
                    request,
                    co,
                    ChangeLog.Source.COURSE_OFFERING_EDIT,
                    ChangeLog.Operation.UPDATE,
                    co.getSubjectArea(),
                    co.getDepartment());

            hibSession.flush();
            tx.commit();

            hibSession.refresh(co);

            if (io!=null)
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
        
        // Consent Type, Credit and Designator Required can be edited only on the controlling course offering
        if (co.isIsControl().booleanValue()) {
            if (io.isDesignatorRequired()!=null)
                frm.setDesignatorRequired(io.isDesignatorRequired());
            else
                frm.setDesignatorRequired(new Boolean(false));

            if (io.getConsentType()!=null)
                frm.setConsent(io.getConsentType().getUniqueId());
            else
                frm.setConsent(new Long(-1));

            LookupTables.setupConsentType(request);

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

        }
        else
            frm.setConsent(null);

        LookupTables.setupCourseOfferingDemands(request, co.getDemandOffering());
    }

    /**
     * @param request
     * @param frm
     * @param courseOfferingId
     */
    private void doReload(
            HttpServletRequest request,
            CourseOfferingEditForm frm) throws Exception {

    	frm.setAllowDemandCourseOfferings(true);

        // Consent Type, Credit and Designator Required can be edited only on the controlling course offering
        if (frm.getIsControl().booleanValue()) {
            if (frm.getDesignatorRequired()==null)
                frm.setDesignatorRequired(new Boolean(false));

            if (frm.getConsent()==null)
                frm.setConsent(new Long(-1));

            LookupTables.setupConsentType(request);
            LookupTables.setupCourseCreditFormats(request); // Course Credit Formats
            LookupTables.setupCourseCreditTypes(request); //Course Credit Types
            LookupTables.setupCourseCreditUnitTypes(request); //Course Credit Unit Types

        }
        else
            frm.setConsent(null);

        CourseOffering co = new CourseOfferingDAO().get(frm.getCourseOfferingId());
        LookupTables.setupCourseOfferingDemands(request, co.getDemandOffering());
    }

}
