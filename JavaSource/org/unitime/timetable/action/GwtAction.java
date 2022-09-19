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
import org.unitime.timetable.form.BlankForm;

/**
 * @author Tomas Muller
 */
@Action(value = "gwt", results = {
		@Result(name = "gwt", type = "tiles", location = "gwt.tiles")
})
@TilesDefinition(name = "gwt.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = ""),
		@TilesPutAttribute(name = "body", value = "/gwt2.jsp"),
		@TilesPutAttribute(name = "checkLogin", value = "false"),
		@TilesPutAttribute(name = "checkRole", value = "false"),
		@TilesPutAttribute(name = "includeTimeStamp", value = "false")
	})
public class GwtAction extends UniTimeAction<BlankForm>{
	private static final long serialVersionUID = 578394783461408015L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	public String execute() throws Exception {
		return "gwt";
	}
}
