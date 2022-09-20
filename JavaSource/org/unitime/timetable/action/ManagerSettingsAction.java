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
package org.unitime.timetable.action;

import java.util.StringTokenizer;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.criterion.Order;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.ManagerSettingsForm;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.dao.SettingsDAO;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Action(value = "managerSettings", results = {
		@Result(name = "showManagerSettings", type = "tiles", location = "managerSettings.tiles"),
		@Result(name = "editManagerSettings", type = "tiles", location = "editManagerSettings.tiles")
	})
@TilesDefinitions({
	@TilesDefinition(name = "managerSettings.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Manager Settings"),
			@TilesPutAttribute(name = "body", value = "/user/managerSettings.jsp")
	}),
	@TilesDefinition(name = "editManagerSettings.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit Manager Setting"),
			@TilesPutAttribute(name = "body", value = "/user/managerSettings.jsp")
	})
})
public class ManagerSettingsAction extends UniTimeAction<ManagerSettingsForm> {
	private static final long serialVersionUID = 1306771389501347777L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

    public String execute() throws Exception {
        // Check Access
    	sessionContext.checkPermission(Right.SettingsUser);

    	if (form == null) {
    		form = new ManagerSettingsForm();
    		form.reset();
    	}

    	if (op == null) op = form.getOp();
        else form.setOp(op);
        
        // Reset Form
        if (MSG.actionBackToManagerSettings().equals(op)) {
            form.reset();
            form.setOp("List");
        }

        // Edit - Load setting with allowed values for user to update
        if ("Edit".equals(op)) {
            // Load Settings object
            Settings s = SettingsDAO.getInstance().get(Long.valueOf(request.getParameter("id")));

            // Set Form values
            form.setOp("Edit");            
            form.setAllowedValues(s.getAllowedValues());
            form.setKey(s.getKey());
            form.setName(s.getDescription());
            form.setDefaultValue(s.getDefaultValue());
            form.setValue(sessionContext.getUser().getProperty(s.getKey(), s.getDefaultValue()));

            return "editManagerSettings";
        }
 
        // Save changes made by the user
        if (MSG.actionUpdateManagerSetting().equals(op)) {
        	form.validate(this);
        	if (hasFieldErrors()) {
        		form.setOp("Edit");
        		form.setAllowedValues(Settings.getSetting(form.getKey()).getAllowedValues());
        		return "editManagerSettings";
        	} else {
        		sessionContext.getUser().setProperty(form.getKey(), form.getValue());
        		// form.setOp("List");
        	}
        }

        // Read all existing settings and store in request
        getSettingsList();        
        return "showManagerSettings";
    }

    /**
     * Retrieve all existing defined settings
     */
    private void getSettingsList() throws Exception {
        WebTable.setOrder(sessionContext,"managerSettings.ord",request.getParameter("ord"),1);

		// Create web table instance 
        WebTable webTable = new WebTable( 2,
			    MSG.sectionManagerSettings(), "managerSettings.action?ord=%%",
			    new String[] {MSG.columnManagerSettingKey(), MSG.columnManagerSettingValue()},
			    new String[] {"left", "left"},
			    null );
        
        for (Settings s: SettingsDAO.getInstance().findAll(Order.asc("key"))) {
        	String onClick = "onClick=\"document.location='managerSettings.action?op=Edit&id=" + s.getUniqueId() + "';\"";
        	String value = sessionContext.getUser().getProperty(s.getKey(), s.getDefaultValue());
        	String label = value;
        	for (StringTokenizer k = new StringTokenizer(s.getAllowedValues(), ","); k.hasMoreTokens(); ) {
    			String v = k.nextToken().trim();
    			if (v.startsWith(value + ":")) {
    				label = v.substring(v.indexOf(':') + 1);
    				break;
    			}
    		}
        	webTable.addLine(onClick, new String[] {s.getDescription(), label}, new String[] {s.getDescription(), label});
        }

	    request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext,"managerSettings.ord")));
    }   
    
}
