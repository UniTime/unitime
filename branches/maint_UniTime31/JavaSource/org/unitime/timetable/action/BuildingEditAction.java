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

import java.io.File;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.BuildingEditForm;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
public class BuildingEditAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
        BuildingEditForm myForm = (BuildingEditForm) form;
		
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (op==null) {
            myForm.reset(mapping, request);
            myForm.setOp("Save");
        }
        
    	User user = Web.getUser(request.getSession());
    	Session session = Session.getCurrentAcadSession(user);

        // Return
        if ("Back".equals(op)) {
            return mapping.findForward("back");
        }
        
        if ("Add".equals(op)) {
            myForm.setOp("Save");
        }

        // Add / Update
        if ("Update".equals(op) || "Save".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("Save".equals(op)?"add":"edit");
            } else {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (new BuildingDAO()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	myForm.saveOrUpdate(request, hibSession, session);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                return mapping.findForward("back");
            }
        }

        // Edit
        if("Edit".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("externalId", new ActionMessage("errors.invalid", id));
                saveErrors(request, errors);
                return mapping.findForward("edit");
            } else {
                Building b = new BuildingDAO().get(Long.valueOf(id));
            	
                if (b==null) {
                    return mapping.findForward("back");
                } else {
                	myForm.load(b);
                }
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (new BuildingDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	myForm.delete(request, hibSession);
            	
    			tx.commit();
    			
    			HibernateUtil.clearCache();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

            return mapping.findForward("back");
        }
        
        if ("Export PDF".equals(op)) {
            DecimalFormat df5 = new DecimalFormat("####0");
            PdfWebTable table = new PdfWebTable( 5,
                    "Buildings", null,
                    new String[] {"Abbreviation", "Name", "External ID", "X-Coordinate", "Y-Coordinate"},
                    new String[] {"left", "left","left","right","right"},
                    new boolean[] {true,true,true,true,true} );
            for (Iterator i=session.getBldgsFast(null).iterator();i.hasNext();) {
                Building b = (Building)i.next();
                table.addLine(
                        null,
                        new String[] {
                            b.getAbbreviation(),
                            b.getName(),
                            b.getExternalUniqueId()==null?"@@ITALIC N/A @@END_ITALIC ":b.getExternalUniqueId().toString(),
                            (b.getCoordinateX()==null || b.getCoordinateX()<0?"":df5.format(b.getCoordinateX())),
                            (b.getCoordinateY()==null || b.getCoordinateY()<0?"":df5.format(b.getCoordinateY())),
                            }, 
                        new Comparable[] {
                            b.getAbbreviation(),
                            b.getName(),
                            b.getExternalUniqueId()==null?"":b.getExternalUniqueId(),
                            b.getCoordinateX(),
                            b.getCoordinateY(),
                            });
                
            }
            
            File file = ApplicationProperties.getTempFile("buildings", "pdf");
            
            table.exportPdf(file, PdfWebTable.getOrder(request.getSession(), "BuildingList.ord"));
            
            request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
            
            return mapping.findForward("back");
        }
        
        if ("Update Data".equals(op)) {
    		Room.addNewExternalRoomsToSession(session);
        	
            return mapping.findForward("back");
        }
     
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
	
}

