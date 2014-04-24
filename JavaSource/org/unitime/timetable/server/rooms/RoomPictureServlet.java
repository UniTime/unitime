/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.rooms;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.dao.LocationPictureDAO;

/**
 * @author Tomas Muller
 */
public class RoomPictureServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String TEMP_ROOM_PICTURES = "ROOM_PICTURES";
	
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
