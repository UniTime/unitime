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
package org.unitime.timetable.gwt.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.TreeSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.util.HtmlUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.export.ExportServletHelper;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class UploadServlet extends HttpServlet {
	protected static Logger sLog = LogManager.getLogger(UploadServlet.class);
	protected static GwtMessages MSG = Localization.create(GwtMessages.class);
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_MAX_SIZE = 4096 * 1024;
	
	public static final String SESSION_LAST_FILE = SessionAttribute.LastUploadedFile.key();
	
	protected SessionContext getSessionContext() {
		return HttpSessionContext.getSessionContext(getServletContext());
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ExportServletHelper helper = new ExportServletHelper(request, response, getSessionContext());
		if (helper.getParameter("event") != null) {
			Long eventId = Long.parseLong(helper.getParameter("event"));
			String fileName = helper.getParameter("name");
			Long noteId = (helper.getParameter("note") == null ? null : Long.valueOf(helper.getParameter("note")));
			helper.getSessionContext().checkPermissionAnyAuthority(Long.valueOf(eventId), "Event", Right.EventDetail);
			Event event = EventDAO.getInstance().get(eventId);
			TreeSet<EventNote> notes = new TreeSet<EventNote>();
			if (event != null)
				for (EventNote note: event.getNotes()) {
					if (note.getAttachedName() == null || note.getAttachedName().isEmpty()) continue;
					if (fileName != null) {
						if (fileName.equals(note.getAttachedName()) && (noteId == null || noteId.equals(note.getUniqueId()))) notes.add(note);
					} else if (noteId != null) {
						if (noteId.equals(note.getUniqueId())) notes.add(note);
					} else {
						notes.add(note);
					}
				}
			if (!notes.isEmpty()) {
				EventNote note = notes.last();
				
				response.setContentType(note.getAttachedContentType());
				response.setHeader( "Content-Disposition", "attachment; filename=\"" + note.getAttachedName() + "\"" );
		        OutputStream out = response.getOutputStream();
		        out.write(note.getAttachedFile());
		        out.flush();
		        out.close();
		        
		        return;
			}
		}
		
		throw new ServletException("Nothing to download.");
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String maxSizeProperty = ApplicationProperty.MaxUploadSize.value();
			int maxSize = (maxSizeProperty == null ? DEFAULT_MAX_SIZE : Integer.parseInt(maxSizeProperty));
			
			DiskFileItemFactory diskFileItemFactory = DiskFileItemFactory.builder().setBufferSize(maxSize).setPath(ApplicationProperties.getTempFolder().toPath()).get();
			JakartaServletFileUpload upload = new JakartaServletFileUpload(diskFileItemFactory);
			upload.setSizeMax(maxSize);
			
			List<FileItem> files = (List<FileItem>)upload.parseRequest(request);
			
			String message = null;
			if (files.size() == 1) {
				FileItem file = files.get(0);
				if (file.getSize() <= 0) {
					request.getSession().removeAttribute(SESSION_LAST_FILE);
					message = MSG.failedUploadNoFile();
				} else {
					request.getSession().setAttribute(SESSION_LAST_FILE, file);
					message = MSG.fileUploaded(HtmlUtils.htmlEscape(file.getName()), file.getSize());
				}
			} else {
				request.getSession().removeAttribute(SESSION_LAST_FILE);
				message = MSG.failedUploadNoFile();
			}
			
			response.setContentType("text/html; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.print(message);
			out.flush();
			out.close();
		} catch (FileUploadException e) {
			sLog.error("Upload failed: " + e.getMessage(), e);
			response.setContentType("text/html; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.print("ERROR:" + MSG.failedUpload(e.getMessage()));
			out.flush();
			out.close();
		}
	}
}
