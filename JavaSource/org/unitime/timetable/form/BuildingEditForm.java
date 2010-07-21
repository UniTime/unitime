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
package org.unitime.timetable.form;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.BuildingDAO;

/** 
 * 
 * @author Tomas Muller
 * 
 */
public class BuildingEditForm extends ActionForm {
    private Long iUniqueId = null;
	private String iOp = null;
    private String iExternalId = null;
    private String iName = null;
    private String iAbbreviation = null;
    private String iCoordX = null, iCoordY = null;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        try {
            Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
            
            if (iName==null || iName.trim().length()==0)
                errors.add("name", new ActionMessage("errors.required", ""));
            else {
                try {
                    Building building = Building.findByName(iName, session.getUniqueId());
                    if (building!=null && !building.getUniqueId().equals(iUniqueId))
                        errors.add("name", new ActionMessage("errors.exists", iName));
                } catch (Exception e) {
                    errors.add("name", new ActionMessage("errors.generic", e.getMessage()));
                }
            }

            if (iAbbreviation==null || iAbbreviation.trim().length()==0)
                errors.add("abbreviation", new ActionMessage("errors.required", ""));
            else {
                try {
                    Building building = Building.findByBldgAbbv(iAbbreviation, session.getUniqueId());
                    if (building!=null && !building.getUniqueId().equals(iUniqueId))
                        errors.add("abbreviation", new ActionMessage("errors.exists", iAbbreviation));
                } catch (Exception e) {
                    errors.add("abbreviation", new ActionMessage("errors.generic", e.getMessage()));
                }
            }
        } catch (Exception e) {
            Debug.error(e);
            errors.add("name", new ActionMessage("errors.generic", e.getMessage()));
        }
        
        return errors;
    }

	public void reset(ActionMapping mapping, HttpServletRequest request) {
        iUniqueId = null; iAbbreviation = null;
		iOp = null; iExternalId = null; iName = null;
        iCoordX = null; iCoordY = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
    public Long getUniqueId() { return iUniqueId; }
    public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
    public String getExternalId() { return iExternalId; }
    public void setExternalId(String externalId) { iExternalId = externalId; }
    public String getName() { return iName; }
    public void setName(String name) { iName = name; }
    public String getAbbreviation() { return iAbbreviation; }
    public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }
    public String getCoordX() { return iCoordX; }
    public void setCoordX(String coordX) { iCoordX = coordX; }
    public String getCoordY() { return iCoordY; }
    public void setCoordY(String coordY) { iCoordY = coordY; }
    
    public void load(Building building) {
        setOp("Update");
        setUniqueId(building.getUniqueId());
        setExternalId(building.getExternalUniqueId());
        setName(building.getName());
        setAbbreviation(building.getAbbreviation());
        setCoordX(building.getCoordinateX()==null ? null : building.getCoordinateX().toString());
        setCoordY(building.getCoordinateY()==null ? null : building.getCoordinateY().toString());
    }
    
    public void saveOrUpdate(HttpServletRequest request, org.hibernate.Session hibSession, Session session) throws Exception {
        Building building = null;
        if (getUniqueId()!=null) building = new BuildingDAO().get(getUniqueId());
        if (building==null) building = new Building();
        building.setName(getName());
        building.setAbbreviation(getAbbreviation());
        building.setExternalUniqueId(getExternalId()!=null && getExternalId().length()==0?null:getExternalId());
        building.setCoordinateX(getCoordX()==null || getCoordX().length()==0 ? null : Double.valueOf(getCoordX()));
        building.setCoordinateY(getCoordY()==null || getCoordY().length()==0 ? null : Double.valueOf(getCoordY()));
        building.setSession(session);
        hibSession.saveOrUpdate(building);
        ChangeLog.addChange(
                hibSession, 
                request, 
                building, 
                ChangeLog.Source.BUILDING_EDIT, 
                (getUniqueId()==null?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                null, 
                null);
    }
    
    public void delete(HttpServletRequest request, org.hibernate.Session hibSession) {
        Building building = new BuildingDAO().get(getUniqueId());
        if (building!=null) {
            for (Iterator i=
                hibSession.createQuery("select r from Room r where r.building.uniqueId=:buildingId").setLong("buildingId", getUniqueId()).iterate();
                i.hasNext();) {
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
                    request, 
                    building, 
                    ChangeLog.Source.BUILDING_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    null);
            hibSession.delete(building);
        }
    }
}

