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
package org.unitime.timetable.server.rooms;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.dao.LocationPictureDAO;

/**
 * @author Tomas Muller
 */
public class RoomPictureServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String TEMP_ROOM_PICTURES = SessionAttribute.RoomPictures.key();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String idStr = request.getParameter("id");
		if (idStr != null) {
			Long id = Long.valueOf(idStr);
			LocationPicture picture = null;
			if (id >= 0) {
				picture = LocationPictureDAO.getInstance().get(id);
			} else {
				Map<Long, LocationPicture> temp = (Map<Long, LocationPicture>)request.getSession().getAttribute(TEMP_ROOM_PICTURES);
				if (temp != null)
					picture = temp.get(id);
			}
			if (picture != null) {
				response.setContentType(picture.getContentType());
				response.setHeader( "Content-Disposition", "attachment; filename=\"" + picture.getFileName() + "\"" );
				OutputStream out = response.getOutputStream(); 
				out.write(picture.getDataFile());
				out.flush();
			} else {
				new ServletException("Room picture of the given id does not exist.");
			}
		}
		new ServletException("No room picture id provided.");
	}

}
