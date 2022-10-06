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

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.ExactTimeEditForm;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Action(value = "exactTimeEdit", results = {
		@Result(name = "display", type = "tiles", location = "exactTimeEdit.tiles")
	})
@TilesDefinition(name = "exactTimeEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Exact Time Pattern"),
		@TilesPutAttribute(name = "body", value = "/admin/exactTimeEdit.jsp")
	})
public class ExactTimeEditAction extends UniTimeAction<ExactTimeEditForm> {
	private static final long serialVersionUID = -741923666126157505L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	public String execute() throws Exception {
		if (form == null)
			form = new ExactTimeEditForm();
		
        // Check Access
		sessionContext.checkPermission(Right.ExactTimes);
        
        // Read operation to be performed
        if (MSG.actionUpdateExactTimeMins().equals(op)) {
        	form.save();
        }
        
        return "display";
	}
}

