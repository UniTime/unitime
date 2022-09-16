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

import java.net.URLEncoder;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.timetable.form.BlankForm;

/**
 * Dummy action to redirect requests using tiles
 * @author Heston Fernandes, Tomas Muller
 */
@Action(value = "loginRequired", results = {
		@Result(name = "success", type = "tiles", location = "loginRequired.tiles")
	})
@TilesDefinition(name = "loginRequired.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Access Denied"),
		@TilesPutAttribute(name = "body", value = "/loginRequired.jsp"),
		@TilesPutAttribute(name = "checkLogin", value = "false"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
public class TilesRedirectAction extends UniTimeAction<BlankForm> {
    private static final long serialVersionUID = 8188150583963144193L;
    private String message;
    private String target;
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String execute() throws Exception {
    	Throwable e = (Throwable)request.getAttribute("exception");
    	if (e != null)
    		message = e.getMessage();
    	else if (request.getAttribute("message") != null)
    		message = request.getAttribute("message").toString();
    	target = null;
    	String uri = request.getRequestURI().substring(request.getContextPath().length() + 1);
    	if (!uri.equals("loginRequired.action") && "GET".equals(request.getMethod())) {
    		if (request.getQueryString() == null || request.getQueryString().isEmpty())
    			target = URLEncoder.encode(uri, "UTF-8");
    		else
    			target = URLEncoder.encode(uri + "?" + request.getQueryString(), "UTF-8");
    	}
        return "success";
    }
}
