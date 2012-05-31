/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.command.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.util.Constants;

public class GwtRpcHelper {
	private HttpSession iHttpSession;
	private String iURL;
	
	protected GwtRpcHelper(HttpServletRequest httpRequest) {
		iHttpSession = httpRequest.getSession();
		iURL = httpRequest.getScheme() + "://" +httpRequest.getServerName() + ":" + httpRequest.getServerPort() + httpRequest.getContextPath();
	}
	
	public User getUser() {
		return Web.getUser(iHttpSession);
	}
	
	public String getUserId() {
		return (getUser() == null ? null : getUser().getId());
	}
	
	public Long getAcademicSessionId() {
		User user = getUser();
		return (user == null ? null : (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
	}
	
	public boolean isHttpSessionNew() {
		return iHttpSession.isNew();
	}
	
	public String getHttpSessionId() {
		return iHttpSession.getId();
	}
	
	public HttpSession getHttpSession() {
		return iHttpSession;
	}
	
	public String getRequestUrl() {
		return iURL;
	}
	
	public FileItem getLastUploadedFile() {
		return (FileItem)iHttpSession.getAttribute(UploadServlet.SESSION_LAST_FILE);
	}
	
	public boolean hasLastUploadedFile() {
		return iHttpSession.getAttribute(UploadServlet.SESSION_LAST_FILE) != null;
	}
	
	public void clearLastUploadedFile() {
		iHttpSession.removeAttribute(UploadServlet.SESSION_LAST_FILE);
	}
}
