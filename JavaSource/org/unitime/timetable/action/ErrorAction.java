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
import org.unitime.timetable.form.BlankForm;

/**
 * @author Tomas Muller
 */
@Action(value = "error", results = {
		@Result(name = "success", type = "tiles", location = "error.tiles")
	})
@TilesDefinition(name = "error.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Runtime Error"),
		@TilesPutAttribute(name = "body", value = "/error-struts2.jsp")
	})
public class ErrorAction extends UniTimeAction<BlankForm>{
	private static final long serialVersionUID = -5805604919312822390L;
	
	private String uri;
    
	public String execute() {
		uri = request.getRequestURI().substring(request.getContextPath().length() + 1);
		if (request.getQueryString() != null && !request.getQueryString().isEmpty())
			uri += "?" + request.getQueryString();
        return "success";
	}
	
	public String getURL() {
		return uri;
	}
	
}
