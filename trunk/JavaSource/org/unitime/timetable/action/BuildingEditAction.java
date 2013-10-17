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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.form.BuildingEditForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Service("/buildingEdit")
public class BuildingEditAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
        BuildingEditForm myForm = (BuildingEditForm) form;
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (op==null) {
            myForm.reset(mapping, request);
            myForm.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
            myForm.setOp("Save");
        }
        
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
            	
            	if ("Save".equals(op))
            		sessionContext.checkPermission(Right.BuildingAdd);
            	else
            		sessionContext.checkPermission(myForm.getUniqueId(), "Building", Right.BuildingEdit);
            	
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (new BuildingDAO()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	saveOrUpdate(myForm, hibSession);
                	
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
                	sessionContext.checkPermission(b, Right.BuildingEdit);
                	myForm.load(b);
                }
            }
        }

        // Delete 
        if("Delete".equals(op)) {
        	
        	sessionContext.checkPermission(myForm.getUniqueId(), "Building", Right.BuildingDelete);
        	
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (new BuildingDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	delete(myForm, hibSession);
            	
    			tx.commit();
    			
    			HibernateUtil.clearCache();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

            return mapping.findForward("back");
        }
        
        if ("Export PDF".equals(op)) {
            DecimalFormat df5 = new DecimalFormat("####0.######");
            PdfWebTable table = new PdfWebTable( 5,
                    "Buildings", null,
                    new String[] {"Abbreviation", "Name", "External ID", "X-Coordinate", "Y-Coordinate"},
                    new String[] {"left", "left","left","right","right"},
                    new boolean[] {true,true,true,true,true} );
            for (Building b: Building.findAll(sessionContext.getUser().getCurrentAcademicSessionId())) {
                table.addLine(
                        null,
                        new String[] {
                            b.getAbbreviation(),
                            b.getName(),
                            b.getExternalUniqueId()==null?"@@ITALIC N/A @@END_ITALIC ":b.getExternalUniqueId().toString(),
                            (b.getCoordinateX()==null ? "" : df5.format(b.getCoordinateX())),
                            (b.getCoordinateY()==null ? "" : df5.format(b.getCoordinateY())),
                            }, 
                        new Comparable[] {
                            b.getAbbreviation(),
                            b.getName(),
                            b.getExternalUniqueId()==null ? "" : b.getExternalUniqueId(),
                            b.getCoordinateX(),
                            b.getCoordinateY(),
                            });
                
            }
            
            ExportUtils.exportPDF(
            		table,
            		PdfWebTable.getOrder(sessionContext, "BuildingList.ord"),
            		response, "buildings");
            
            return null;
        }
        
        if ("Update Data".equals(op)) {
        	sessionContext.checkPermission(Right.BuildingUpdateData);
        	
    		Room.addNewExternalRoomsToSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()));
        	
            return mapping.findForward("back");
        }
     
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	    
    public void saveOrUpdate(BuildingEditForm form, org.hibernate.Session hibSession) throws Exception {
        Building building = null;
        if (form.getUniqueId() != null) {
        	building = BuildingDAO.getInstance().get(form.getUniqueId());
        }
        if (building==null) {
        	building = new Building();
        	building.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId(), hibSession));
        }
        building.setName(form.getName());
        building.setAbbreviation(form.getAbbreviation());
        building.setExternalUniqueId(form.getExternalId()!=null && form.getExternalId().length()==0 ? null : form.getExternalId());
        building.setCoordinateX(form.getCoordX()==null || form.getCoordX().length()==0 ? null : Double.valueOf(form.getCoordX()));
        building.setCoordinateY(form.getCoordY()==null || form.getCoordY().length()==0 ? null : Double.valueOf(form.getCoordY()));
        hibSession.saveOrUpdate(building);
        ChangeLog.addChange(
                hibSession, 
                sessionContext, 
                building, 
                ChangeLog.Source.BUILDING_EDIT, 
                (form.getUniqueId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE), 
                null, 
                null);
    }
    
    public void delete(BuildingEditForm form, org.hibernate.Session hibSession) {
        Building building = BuildingDAO.getInstance().get(form.getUniqueId());
        if (building != null) {
            for (Iterator i= hibSession.createQuery("select r from Room r where r.building.uniqueId=:buildingId").setLong("buildingId", form.getUniqueId()).iterate(); i.hasNext();) {
                Room r = (Room)i.next();
                hibSession.createQuery("delete RoomPref p where p.room.uniqueId=:roomId").setLong("roomId", r.getUniqueId()).executeUpdate();
                for (Iterator j=r.getAssignments().iterator();j.hasNext();) {
                    Assignment a = (Assignment)j.next();
                    a.getRooms().remove(r);
                    hibSession.saveOrUpdate(a);
                    j.remove();
                }
                hibSession.delete(r);
            }
            
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    building, 
                    ChangeLog.Source.BUILDING_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);
            hibSession.delete(building);
        }
    }
}

