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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.security.context.HttpSessionContext;


/** 
* MyEclipse Struts
* Creation date: 02-18-2005
* 
* XDoclet definition:
* @struts:form name="roomListForm"
*/
/**
 * @author Tomas Muller
 */
public class RoomListForm extends ActionForm {

	private static final long serialVersionUID = 3256728385592768053L;
	/**
	 * 
	 */
	// --------------------------------------------------------- Instance Variables
	private Collection rooms;
	private String deptCode;
	
    private String iMinRoomSize = null, iMaxRoomSize = null;
	private Long[] iRoomFeatures = null;
	private Long[] iRoomTypes = null;
	private Long[] iRoomGroups = null;
	private String iFilter = null;
	private Long iSessionId = null;

	
	// --------------------------------------------------------- Methods

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		rooms = new ArrayList();
        iSessionId = HttpSessionContext.getSessionContext(request.getSession().getServletContext()).getUser().getCurrentAcademicSessionId();
	}
	
    public void load(HttpSession session) {
        iMinRoomSize = (String)session.getAttribute("RoomList.MinRoomSize");
        iMaxRoomSize = (String)session.getAttribute("RoomList.MaxRoomSize");
        iFilter = (String)session.getAttribute("RoomList.Filter");
        iRoomTypes = (Long[]) session.getAttribute("RoomList.RoomTypes");
        iRoomGroups = (Long[]) session.getAttribute("RoomList.RoomGroups");
		iRoomFeatures = (Long[]) session.getAttribute("RoomList.RoomFeatures");
    }
    
    public void save(HttpSession session) {
        if (iMinRoomSize==null)
            session.removeAttribute("RoomList.MinRoomSize");
        else
            session.setAttribute("RoomList.MinRoomSize", iMinRoomSize);
        if (iMaxRoomSize==null)
            session.removeAttribute("RoomList.MaxRoomSize");
        else
            session.setAttribute("RoomList.MaxRoomSize", iMaxRoomSize);

        if (iFilter==null)
            session.removeAttribute("RoomList.Filter");
        else
            session.setAttribute("RoomList.Filter", iFilter);
        
        if (iRoomTypes==null)
        	session.removeAttribute("RoomList.RoomTypes");
        else
        	session.setAttribute("RoomList.RoomTypes", iRoomTypes);
        if (iRoomGroups==null)
        	session.removeAttribute("RoomList.RoomGroups");
        else
        	session.setAttribute("RoomList.RoomGroups", iRoomGroups);
        if (iRoomFeatures==null)
        	session.removeAttribute("RoomList.RoomFeatures");
        else
        	session.setAttribute("RoomList.RoomFeatures", iRoomFeatures);
    }
	
	/**
	 * @return Returns the rooms.
	 */
	public Collection getRooms() {
		return rooms;
	}
	/**
	 * @param rooms The rooms to set.
	 */
	public void setRooms(Collection rooms) {
		this.rooms = rooms;
	}
	
	public String getDeptCodeX() {
		return deptCode;
	}

	public void setDeptCodeX(String deptCode) {
		this.deptCode = deptCode;
	}

	/* (non-Javadoc)
     * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
      
        if(deptCode==null || deptCode.equalsIgnoreCase("")) {
        	errors.add("deptCode", 
                    new ActionMessage("errors.required", "Department") );
        }
       
        return errors;
    }
    
    public String getMinRoomSize() { return iMinRoomSize; }
    public void setMinRoomSize(String minRoomSize) { iMinRoomSize = minRoomSize; }
    public String getMaxRoomSize() { return iMaxRoomSize; }
    public void setMaxRoomSize(String maxRoomSize) { iMaxRoomSize = maxRoomSize; }
    public String getFilter() { return iFilter; }
    public void setFilter(String filter) { iFilter = filter; }
    
    public Long[] getRoomTypes() { return iRoomTypes; }
	public void setRoomTypes(Long[] rts) { iRoomTypes = rts; }

    public Long[] getRoomGroups() { return iRoomGroups; }
    public void setRoomGroups(Long[] rgs) { iRoomGroups= rgs; }

    public Long[] getRoomFeatures() { return iRoomFeatures; }
    public void setRoomFeatures(Long[] rfs) { iRoomFeatures = rfs; }
    
    public Collection<RoomFeatureType> getRoomFeatureTypes() {
    	Set<RoomFeatureType> types = RoomFeatureType.getRoomFeatureTypes(iSessionId, false);
    	if (RoomFeatureType.hasRoomFeatureWithNoType(iSessionId, false)) {
    		RoomFeatureType f = new RoomFeatureType();
    		f.setUniqueId(-1l); f.setReference("Features"); f.setLabel("Room Features");
    		types.add(f);
    	}
    	return types;
    }
    
    public Collection<GlobalRoomFeature> getAllRoomFeatures(String featureType) {
    	return RoomFeature.getAllGlobalRoomFeatures(iSessionId, featureType == null || featureType.isEmpty() ? null : Long.valueOf(featureType));
    }
    
    public Collection<RoomGroup> getAllRoomGroups() {
        return RoomGroup.getAllGlobalRoomGroups(iSessionId);
    }

    public Collection<RoomType> getAllRoomTypes() {
        return RoomType.findAll(iSessionId);
    }
}
