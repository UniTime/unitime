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
package org.unitime.timetable.form;


import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;

/** 
 * 
 * @author Tomas Muller
 * 
 */
public class BuildingEditForm extends ActionForm {
	private static final long serialVersionUID = -4104780400760573687L;
	private Long iUniqueId = null;
	private Long iSessionId = null;
	private String iOp = null;
    private String iExternalId = null;
    private String iName = null;
    private String iAbbreviation = null;
    private String iCoordX = null, iCoordY = null;
    private Boolean iUpdateRoomCoordinates = null;
    
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		if (iSessionId == null || iSessionId <= 0) {
			SessionContext context = HttpSessionContext.getSessionContext(request.getSession().getServletContext());
			iSessionId = (context.isAuthenticated() ? context.getUser().getCurrentAcademicSessionId() : null);
		}
		
        ActionErrors errors = new ActionErrors();
        
        try {
            if (iName==null || iName.trim().length()==0)
                errors.add("name", new ActionMessage("errors.required", ""));
            else {
                try {
                    Building building = Building.findByName(iName, iSessionId);
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
                    Building building = Building.findByBldgAbbv(iAbbreviation, iSessionId);
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
        iUpdateRoomCoordinates = null;
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
    
    public Long getSessionId() { return iSessionId; }
    public void setSessionId(Long sessionId) { iSessionId = sessionId; }
    
    public void setUpdateRoomCoordinates(Boolean updateRoomCoordinates) { iUpdateRoomCoordinates = updateRoomCoordinates; }
    public Boolean getUpdateRoomCoordinates() { return iUpdateRoomCoordinates; }

    public void load(Building building) {
        setOp("Update");
        setUniqueId(building.getUniqueId());
        setSessionId(building.getSession().getUniqueId());
        setExternalId(building.getExternalUniqueId());
        setName(building.getName());
        setAbbreviation(building.getAbbreviation());
        setCoordX(building.getCoordinateX()==null ? null : building.getCoordinateX().toString());
        setCoordY(building.getCoordinateY()==null ? null : building.getCoordinateY().toString());
        setUpdateRoomCoordinates(false);
    }

}

